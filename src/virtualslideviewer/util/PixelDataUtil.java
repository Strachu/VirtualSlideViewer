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

package virtualslideviewer.util;

import java.awt.Dimension;

public class PixelDataUtil
{
	/**
	 * Swaps every R channel with B channel for every pixel in passed array thus converting beetwen RGB and BGR.
	 * 
	 * @param pixelArray Pixel array which channels will be swapped.
	 */
	public static void swapRgbColorComponents(byte[] pixelArray)
	{
		if(pixelArray == null) throw new IllegalArgumentException("pixelArray cannot be null.");
		
		for(int i = 0; i < pixelArray.length ;i += 3)
		{
			byte temp  = pixelArray[i];
			
			pixelArray[i]   = pixelArray[i+2];
			pixelArray[i+2] = temp;
		}
	}

	/**
	 * Converts pixels in src buffer from planar format to an interleaved one, storing the result in dst.
	 */
	public static void convertPlanarToInterleaved(byte[] src, byte[] dst, Dimension imageSize, int channelCount)
	{
		if(src == null) throw new IllegalArgumentException("src cannot be null.");
		if(dst == null) throw new IllegalArgumentException("dst cannot be null.");
		if(imageSize == null) throw new IllegalArgumentException("imageSize cannot be null.");
		if(channelCount <= 0) throw new IllegalArgumentException("channelCount must be positive.");
		
		int channelLength = imageSize.width * imageSize.height;
		
		for(int pixel = 0; pixel < channelLength ;++pixel)
		{
			for(int channel = 0; channel < channelCount; channel++)
			{
				dst[pixel * channelCount + channel] = src[pixel + channel * channelLength];
			}
		}	
	}
}
