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

import loci.formats.IFormatReader;

/**
 * Special case for Pyramidal OME-TIFF support.
 * 
 * Standard OME-TIFF files as handled by bioformats don't have support for resolution pyramids.
 * Resolution pyramid is simulated by using multiple series per images.
 */
public class OmeTiffVirtualSlideImage extends BioformatsVirtualSlideImage
{	
	public OmeTiffVirtualSlideImage(String name, int seriesIndex, int resolutionCount,
	                                ReaderPool readerPool)
	{
		super(name, seriesIndex, resolutionCount, readerPool);
	}
	
	@Override
	protected void configureReader(IFormatReader reader, int resIndex)
	{
		int resolutionIndexInReader = super.getResolutionCount() - 1 - resIndex;
		
		reader.setSeries(super.getSeriesIndex() + resolutionIndexInReader);
	}
}
