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
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Path;

import loci.formats.*;
import loci.formats.meta.IMetadata;
import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.persistence.VirtualSlideLoadException;
import virtualslideviewer.core.persistence.VirtualSlideLoader;
import virtualslideviewer.util.ParameterValidator;

public class BioformatsLoader implements VirtualSlideLoader
{
	private final PaddingCalculator mPaddingCalculator;
	
	public BioformatsLoader(PaddingCalculator paddingCalculator)
	{
		ParameterValidator.throwIfNull(paddingCalculator, "paddingCalculator");
		
		mPaddingCalculator = paddingCalculator;
	}
	
	@Override
	public boolean canLoad(Path filePath)
	{
		try(IFormatReader reader = new ImageReader())
		{
			return reader.isThisType(filePath.toString());
		}
		catch(IOException e)
		{
			return false;
		}
	}

	/**
	 * @param filePath Path of the file to open.
	 * 
	 * @throws VirtualSlideLoadException When virtual slide could not be loaded because file does not exist or is in unsupported format.
	 */
	@Override
	public VirtualSlide load(Path filePath) throws VirtualSlideLoadException, UncheckedInterruptedException
	{
		ParameterValidator.throwIfNull(filePath, "filePath");
		
		IMetadata  metadata   = MetadataTools.createOMEXMLMetadata();
		ReaderPool readerPool = new ReaderPool(() -> initReader(filePath, metadata));
		
		try
		{
			BioformatsVirtualSlide newSlide = new BioformatsVirtualSlide(metadata, getFileFormat(readerPool));

			loadImages(newSlide, readerPool);
			
			return newSlide;
		}
		catch(Exception e)
		{
			readerPool.close();
			throw e;
		}
	}
	
	private String getFileFormat(ReaderPool readerPool)
	{
		String fileFormat;
		
		IFormatReader reader = readerPool.borrow();
		{	
			fileFormat = reader.getFormat();
		}
		readerPool.putBack(reader);
		
		return fileFormat;
	}
	
	/**
	 * Adds all images found in a virtual slide file to a list of images.
	 */
	protected void loadImages(BioformatsVirtualSlide slide, ReaderPool readerPool) throws UncheckedInterruptedException
	{
		IFormatReader reader = readerPool.borrow();
		try
		{
			for(int seriesIndex = 0; seriesIndex < reader.getSeriesCount(); seriesIndex++)
			{
				reader.setSeries(seriesIndex);
				
				String imageName = slide.getBioformatsMetadata().getImageName(seriesIndex);
				int    resCount  = reader.getResolutionCount();
				
				BioformatsVirtualSlideImage image = new BioformatsVirtualSlideImage(imageName, seriesIndex, resCount, readerPool);
				
				computePaddingForEveryResolutionOfImage(image);
				
				slide.addImage(seriesIndex, image);
			}
		}
		finally
		{
			readerPool.putBack(reader);
		}
	}
	
	protected void computePaddingForEveryResolutionOfImage(BioformatsVirtualSlideImage image) throws UncheckedInterruptedException
	{
		for(int i = 0; i < image.getResolutionCount(); i++)
		{
			Dimension padding = mPaddingCalculator.computePadding(image, i);
			
			image.setPadding(i, padding);
		}
	}

	/**
	 * Inits the reader which will be used to load the data of the virtual slide.
	 */
	private IFormatReader initReader(Path filePath, IMetadata metadataStore)
	{
		try
		{
			IFormatReader newReader = new ChannelFiller(new ImageReader());
		
			newReader.setMetadataStore(metadataStore);
			newReader.setFlattenedResolutions(false);
			newReader.setId(filePath.toString());
		
			return newReader;
		}
		catch(ClosedByInterruptException e)
		{
			// Cannot throw checked exception from a lambda...
			throw new UncheckedInterruptedException(e.getMessage());
		}
		catch(FormatException | IOException e)
		{
			// Had to create new (unchecked) exception type because checked exception cannot be thrown from lamdba in which it is called...
			throw new VirtualSlideLoadException(e.getMessage(), e);
		}
	}
}
