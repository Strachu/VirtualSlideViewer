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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import loci.formats.IFormatReader;
import loci.formats.in.OMETiffReader;
import loci.formats.meta.IMetadata;
import virtualslideviewer.UncheckedInterruptedException;

public class OmeTiffLoader extends BioformatsLoader
{
	public OmeTiffLoader(PaddingCalculator paddingCalculator)
	{
		super(paddingCalculator);
	}
	
	@Override
	public boolean canLoad(Path filePath)
	{
		try(IFormatReader reader = new OMETiffReader())
		{
			return reader.isThisType(filePath.toString());
		}
		catch(IOException e)
		{
			return false;
		}
	}

	@Override
	protected void loadImages(BioformatsVirtualSlide slide, ReaderPool readerPool) throws UncheckedInterruptedException
	{
		IFormatReader reader = readerPool.borrow();
		try
		{	
			List<Integer> seriesResolutionCount = getSeriesResolutionCount(slide.getBioformatsMetadata());
			
			int readerSeriesIndex = 0;
			for(int seriesIndex = 0; seriesIndex < seriesResolutionCount.size(); seriesIndex++)
			{
				reader.setSeries(readerSeriesIndex);
				
				String imageName = slide.getBioformatsMetadata().getImageName(readerSeriesIndex);
				int    resCount  = seriesResolutionCount.get(seriesIndex);
				
				OmeTiffVirtualSlideImage image = new OmeTiffVirtualSlideImage(imageName, readerSeriesIndex, resCount, readerPool);
				
				computePaddingForEveryResolutionOfImage(image);
				
				slide.addImage(seriesIndex, image);
				
				readerSeriesIndex += seriesResolutionCount.get(seriesIndex);
			}
		}
		finally
		{
			readerPool.putBack(reader);
		}
	}
	
	private List<Integer> getSeriesResolutionCount(IMetadata metadata)
	{
		String lowerResolutionTagID = getLowerResolutionTagID(metadata);
		
		List<Integer> seriesResolutionCount = new ArrayList<>();
		for(int i = 0; i < metadata.getImageCount() ;++i)
		{
			if(isLowerResolution(metadata, i, lowerResolutionTagID))
			{
				int currentResolutionCount = seriesResolutionCount.get(seriesResolutionCount.size() - 1);
				
				seriesResolutionCount.set(seriesResolutionCount.size() - 1, currentResolutionCount + 1);
			}
			else
			{
				seriesResolutionCount.add(1);
			}
		}
		return seriesResolutionCount;
	}
	
	private String getLowerResolutionTagID(IMetadata metadata)
	{
		int lowerResolutionTagIndex = MetadataConstructor.getLowerResolutionTagIndex(metadata);
		
		return (lowerResolutionTagIndex != -1) ? metadata.getTagAnnotationID(lowerResolutionTagIndex) : null;
	}
	
	private boolean isLowerResolution(IMetadata metadata, int imageIndex, String lowerResolutionTagID)
	{
		if(lowerResolutionTagID == null)
			return false;
		
		for(int annotationIndex = 0; annotationIndex < metadata.getImageAnnotationRefCount(imageIndex) ;++annotationIndex)
		{
			String annotationID = metadata.getImageAnnotationRef(imageIndex, annotationIndex);
	        
			if(annotationID.equals(lowerResolutionTagID))
			{
				return true;
			}
		}
		
		return false;
	}
}
