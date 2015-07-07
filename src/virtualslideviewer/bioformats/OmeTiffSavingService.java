/*
 * Copyright (C) 2015 Patryk Strach
 * 
 * This file is part of Virtual Slide Viewer.
 * 
 * Virtual Slide Viewer is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * Virtual Slide Viewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Virtual Slide Viewer.
 * If not, see <http://www.gnu.org/licenses/>.
*/

package virtualslideviewer.bioformats;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.out.TiffWriter;
import loci.formats.tiff.IFD;
import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.core.persistence.SaveProgressReporter;
import virtualslideviewer.util.ParameterValidator;
import virtualslideviewer.util.ThreadPoolUtil;

public class OmeTiffSavingService
{
	private final static int TILE_WIDTH  = 256;
	private final static int TILE_HEIGHT = 256;
	
	// When batch copying of multiple tiles at once, only 1 tile vertically can be copied at once due to how bioformats calculate offsets
	// Copying data in rectangular fashion also increases reading performance, because reading is done sequentially.
	private final static int MAX_HORIZONTAL_PIXELS_TO_COPY_AT_ONCE = TILE_WIDTH * 75;
	
	private final ExecutorService mThreadPool;
	
	public OmeTiffSavingService(ExecutorService threadPool)
	{
		ParameterValidator.throwIfNull(threadPool, "threadPool");
		
		mThreadPool = threadPool;
	}
	
	public void save(VirtualSlide slide, Path destinationPath, SaveProgressReporter progress) throws IOException, UncheckedInterruptedException
	{	
		ParameterValidator.throwIfNull(slide, "slide");
		ParameterValidator.throwIfNull(destinationPath, "destinationPath");
		ParameterValidator.throwIfNull(progress, "progress");
		
		Path tempFilePath = Files.createTempFile(destinationPath.getParent(), null, null);

		try
		{
			IMetadata metadata = MetadataConstructor.constructMetadata(slide);
			
			savePixelsTo(slide, tempFilePath, metadata, progress);
			
			// Needs to close the slide if saving to the same file as original under Windows
			slide.close();
			
			Files.move(tempFilePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(Exception e)
		{
			Files.delete(tempFilePath);
			throw e;
		}
	}
	
	private void savePixelsTo(VirtualSlide slide, Path destinationPath, IMetadata metadata, SaveProgressReporter progress) throws IOException, UncheckedInterruptedException
	{
		reportTotalBytesToSave(slide, progress);
		
		// TODO Maybe generating resolution pyramid from scratch would be good idea, especially when original image does not have one
		//      or it's too sparse, as in .svs in which shrink factor of each edge is 4 causing a lot more power required to view it than
		//      if it had shrink factor equal to 2.
		//      The downside is that the saving will be probably a lot longer.
		
		// TODO Copying without recompression when the source file and destination has the same tile size and the same compression method
		//      would be A LOT faster.
		//      It would be good especially when saving the file in place to not trigger full, long save when user only changes image name
		//      of file already in OME-TIFF format.
		//      The problem is that Bioformats does not allow to do it directly:
		//      - bypassing compression should be easy - just override IFD.getCompression()
		//      - reading compressed data in raw format is impossible with bioformats for all formats, but for OME-TIFF
		//        using TiffParser.getSample(IFD) and overriding IFD.getCompression() possibly can work
		//        - if not, you can still parse the IFDs with TiffParser and read the data directly
		
		AtomicLong totalBytesWritten = new AtomicLong(0);
		
		try(OMETiffWriter writer = new OMETiffWriter())
		{
	  		writer.setBigTiff(true);
	      writer.setInterleaved(true);
	      writer.setValidBitsPerPixel(8);
	      writer.setMetadataRetrieve(metadata);
	      writer.setCompression(OMETiffWriter.COMPRESSION_JPEG);
	      
			writer.setId(destinationPath.toString());
			
			int seriesIndex = 0;
			for(VirtualSlideImage image : slide.getImageList())
			{
				for(int resIndex = image.getResolutionCount() - 1; resIndex >= 0 ;--resIndex)
				{
					writer.setSeries(seriesIndex);

					for(int c = 0; c < image.getChannelCount() ;++c)
					{
						for(int z = 0; z < image.getZPlaneCount() ;++z)
						{
							for(int t = 0; t < image.getTimePointCount() ;++t)
							{
								saveImagePixels(writer, image, new ImageIndex(resIndex, c, z, t), progress, totalBytesWritten);
							}
						}
					}
      
					seriesIndex++;
				}
			}
		}
		catch(FormatException e)
		{
			throw new IOException(e);
		}
	}
	
	private void saveImagePixels(TiffWriter writer, VirtualSlideImage image, ImageIndex imageIndex, SaveProgressReporter progress, AtomicLong totalBytesWritten) throws UncheckedInterruptedException
	{
		Dimension imageSize = image.getImageSize(imageIndex.getResolutionIndex());
		
   	IFD ifd = new IFD();
   	ifd.put(IFD.TILE_WIDTH,  Math.min(TILE_WIDTH,  imageSize.width));
   	ifd.put(IFD.TILE_LENGTH, Math.min(TILE_HEIGHT, imageSize.height));
   	
      int columns = (int)Math.ceil(imageSize.getWidth()  / (double)MAX_HORIZONTAL_PIXELS_TO_COPY_AT_ONCE);
      int rows    = (int)Math.ceil(imageSize.getHeight() / (double)TILE_HEIGHT);
      
 		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();

 		for(int row = 0; row < rows ;++row)
      {
      	for(int col = 0; col < columns ;++col)
      	{
      		Tile firstTile = new Tile(col, row, imageIndex);
      		
				tasks.add(() ->
				{
					Rectangle regionBounds = new Rectangle();
            	regionBounds.x      = firstTile.getColumn() * MAX_HORIZONTAL_PIXELS_TO_COPY_AT_ONCE;
            	regionBounds.y      = firstTile.getRow()    * TILE_HEIGHT;
            	regionBounds.width  = Math.min(MAX_HORIZONTAL_PIXELS_TO_COPY_AT_ONCE, imageSize.width  - regionBounds.x);
            	regionBounds.height = Math.min(TILE_HEIGHT,                           imageSize.height - regionBounds.y);
            	
            	byte[] tileData = image.getPixels(regionBounds, firstTile.getImageIndex());
            	
            	int imageNumber = getImageNumber(image, imageIndex);
					writer.saveBytes(imageNumber, tileData, ifd, regionBounds.x, regionBounds.y, regionBounds.width, regionBounds.height);
            	
            	synchronized(this)
            	{
            		long bytesWritten = calculateByteCount(regionBounds.getSize(), image.isRGB());
            		
            		totalBytesWritten.getAndAdd(bytesWritten);
	            	progress.reportWrittenBytes(totalBytesWritten.get());
            	}
            	
            	return null;
				});
      	}
      }
		
      ThreadPoolUtil.scheduleAndWait(mThreadPool, tasks);		
	}
	
	private int getImageNumber(VirtualSlideImage image, ImageIndex imageIndex)
	{
		int c = imageIndex.getChannel();
		int z = imageIndex.getZPlane();
		int t = imageIndex.getTimePoint();
		
		int zSize = image.getZPlaneCount();
		int tSize = image.getTimePointCount();
		
		return c * zSize * tSize + z * tSize + t;
	}
	
	private void reportTotalBytesToSave(VirtualSlide slide, SaveProgressReporter progress)
	{
		long totalBytes = 0;
		
		for(VirtualSlideImage image : slide.getImageList())
		{
			for(int resIndex = 0; resIndex < image.getResolutionCount() ;++resIndex)
			{
				int planeCount = image.getChannelCount() * image.getZPlaneCount() * image.getTimePointCount();
				
				totalBytes += calculateByteCount(image.getImageSize(resIndex), image.isRGB()) * planeCount;
			}
		}
		
		progress.reportTotalBytes(totalBytes);
	}
	
	private long calculateByteCount(Dimension regionSize, boolean isRGB)
	{
		// NOTE: these variables HAVE to be long to avoid int overflow during multiplication
		long channelCount = isRGB ? 3 : 1;
		long width        = regionSize.width;
		long height       = regionSize.height;
		
		return width * height * channelCount;
	}
}
