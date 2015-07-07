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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;

/**
 * Helper functions related to images.
 */
public class ImageUtil
{
	/**
	 * Returns a list of tiles spanning the region.
	 * 
	 * @param area       The bounds of region.
	 * @param tileSize   The size of single tile.
	 * @param imageIndex The image index to which the tiles belong.
	 * 
	 * @return List containing tiles from given area.
	 */
	public static List<Tile> getTilesInArea(Rectangle area, Dimension tileSize, ImageIndex imageIndex)
	{
		ParameterValidator.throwIfNull(area, "area");
		ParameterValidator.throwIfNull(tileSize, "tileSize");
		ParameterValidator.throwIfNull(imageIndex, "imageIndex");
		
		int firstTileX = (int)Math.floor(area.getX()   / tileSize.getWidth());
		int firstTileY = (int)Math.floor(area.getY()   / tileSize.getHeight());
		int lastTileX  = (int)Math.ceil(area.getMaxX() / tileSize.getWidth());
		int lastTileY  = (int)Math.ceil(area.getMaxY() / tileSize.getHeight());		

		List<Tile> tiles = new ArrayList<>();

		for(int y = firstTileY; y < lastTileY; y++)
		{
			for(int x = firstTileX; x < lastTileX; x++)
			{
				tiles.add(new Tile(x, y, imageIndex));
			}
		}
		
		return tiles;
	}
	
	/**
	 * Copies part of image data into part of specified data buffer.
	 * 
	 * <p>
	 * The bounds of region to copy is computed as an intersection of source and destination buffers' bounds
	 * relative to the bounds of destination buffer. For example, when source region bounds are (350, 250, 25, 25)
	 * and destination region bounds are (300, 200, 100, 100), then entire source buffer will be copied into a
	 * destination buffer with offset of (50, 50).
	 * 
	 * @param src                   source buffer
	 * @param srcBoundsInImageSpace bounds of source buffer in image space
	 * @param dst                   destination buffer
	 * @param dstBoundsInImageSpace bounds of destination buffer in image space
	 * @param imageChannelCount     number of channels in image
	 */
	public static void copyIntersectingPartOfImage(byte[] src, Rectangle srcBoundsInImageSpace,
	                                               byte[] dst, Rectangle dstBoundsInImageSpace, int imageChannelCount)
	{
		ParameterValidator.throwIfNull(src, "src");
		ParameterValidator.throwIfNull(srcBoundsInImageSpace, "srcBoundsInImageSpace");
		ParameterValidator.throwIfNull(dst, "dst");
		ParameterValidator.throwIfNull(dstBoundsInImageSpace, "dstBoundsInImageSpace");
		
		Rectangle intersectionInImageSpace = srcBoundsInImageSpace.intersection(dstBoundsInImageSpace);
		
		Point srcOffset = new Point(intersectionInImageSpace.x - srcBoundsInImageSpace.x,
		                            intersectionInImageSpace.y - srcBoundsInImageSpace.y);
		
		Point dstOffset = new Point(intersectionInImageSpace.x - dstBoundsInImageSpace.x,
		                            intersectionInImageSpace.y - dstBoundsInImageSpace.y);
		
		copyPartOfImage(src, srcOffset, srcBoundsInImageSpace.width,
		                dst, dstOffset, dstBoundsInImageSpace.width,
		                imageChannelCount, intersectionInImageSpace.getSize());		
	}
	
	/**
	 * Copies part of image data using explicitly specified region to copy.
	 * 
	 * @param src               source buffer with pixels
	 * @param srcOffset         offset of a pixel in source buffer from which the copy will start
	 * @param srcWidth          width of source buffer in pixels
	 * @param dst               destination buffer with pixels
	 * @param dstOffset         offset of a pixel in destination buffer
	 * @param dstWidth          width of destination buffer in pixels
	 * @param imageChannelCount number of channels in the image
	 * @param areaToCopy        area to copy
	 */
	private static void copyPartOfImage(byte[] src, Point srcOffset, int srcWidth,
	                                    byte[] dst, Point dstOffset, int dstWidth,
	                                    int imageChannelCount, Dimension areaToCopy)
	{
		int srcOffsetInBytes  = (srcOffset.y * srcWidth + srcOffset.x) * imageChannelCount;
		int dstOffsetInBytes  = (dstOffset.y * dstWidth + dstOffset.x) * imageChannelCount;
		
		copyRectangleOfData(src, srcOffsetInBytes, srcWidth * imageChannelCount,
		                    dst, dstOffsetInBytes, dstWidth * imageChannelCount,
		                    new Dimension(areaToCopy.width * imageChannelCount, areaToCopy.height));
	}
	
	private static void copyRectangleOfData(byte[] src, int srcOffset, int srcWidth,
	                                        byte[] dst, int dstOffset, int dstWidth,
	                                        Dimension areaToCopy)
	{
		for(int y = 0; y < areaToCopy.height ;++y)
		{
			System.arraycopy(src, srcOffset + y * srcWidth,
			                 dst, dstOffset + y * dstWidth,
			                 areaToCopy.width);
		}
	}
	
	/**
	 * Computes scale required to apply to an image with size passed in <code>originalSize</code> parameter to fit it
	 * into a rectangle with a size equal to <code>destinationSize</code>.
	 * 
	 * @param originalSize    Size of image to compute scale for.
	 * @param destinationSize The size of rectangle which the image should be fit into.
	 * 
	 * @return The scale.
	 */
	public static double getScaleToFit(Dimension originalSize, Dimension destinationSize)
	{
		ParameterValidator.throwIfNull(originalSize, "originalSize");
		ParameterValidator.throwIfNull(destinationSize, "destinationSize");
		
		double xScale = destinationSize.getWidth()  / originalSize.getWidth();
		double yScale = destinationSize.getHeight() / originalSize.getHeight();
		
		return Math.min(xScale, yScale);
	}
	
	/**
	 * Scales the image using high quality filtering to fit into given dimensions while preserving the aspect ratio of image.
	 * 
	 * @param imageToScale    The image to scale.
	 * @param destinationSize The size of area to which the image should be fit.
	 * 
	 * @return Scaled image.
	 */
	public static BufferedImage scaleToFitPreservingAspectRatio(BufferedImage imageToScale, Dimension destinationSize)
	{
		ParameterValidator.throwIfNull(imageToScale, "imageToScale");
		ParameterValidator.throwIfNull(destinationSize, "destinationSize");
		
		Dimension imageSize  = new Dimension(imageToScale.getWidth(null), imageToScale.getHeight(null));
		double    scaleToFit = getScaleToFit(imageSize, destinationSize);
		
		int newWidth  = (int)(imageSize.width  * scaleToFit);
		int newHeight = (int)(imageSize.height * scaleToFit);
		
		// getScaledInstance() makes the scaled image a lot brighter than the original one.
	//	return imageToScale.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
		
		return scaleImage(imageToScale, new Dimension(newWidth, newHeight), RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}
	
	/**
	 * Scale the image.
	 * 
	 * @param imageToScale    The image to scale.
	 * @param destinationSize New size of the image.
	 * @param filtering       The filtering method determining the speed and quality of the scaling.
	 *                        Available values are: {@link java.awt.RenderingHints#VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *                        {@link java.awt.RenderingHints#VALUE_INTERPOLATION_BILINEAR} and 
	 *                        {@link java.awt.RenderingHints#VALUE_INTERPOLATION_BICUBIC}
	 *                        
	 * @return The scaled image.
	 */
	public static BufferedImage scaleImage(BufferedImage imageToScale, Dimension destinationSize, Object filtering)
	{
		ParameterValidator.throwIfNull(imageToScale, "imageToScale");
		ParameterValidator.throwIfNull(destinationSize, "destinationSize");
		ParameterValidator.throwIfNull(filtering, "filtering");
		
		BufferedImage resizedImage = new BufferedImage(destinationSize.width, destinationSize.height, imageToScale.getType());
		
	   Graphics2D drawingSurface = resizedImage.createGraphics();
	   drawingSurface.setRenderingHint(RenderingHints.KEY_INTERPOLATION, filtering);
		drawingSurface.drawImage(imageToScale, 0, 0, destinationSize.width, destinationSize.height, null);
	   drawingSurface.dispose();
	   
	   return resizedImage;
	}

	/**
	 * Computes a position of an object with specified size in such a way that it is centered if placed inside a panel with given size.
	 * 
	 * @param objectSize A size of an object which should be centered.
	 * @param areaSize A size of an area in which the object should be centered.
	 */
	public static Point getCenteredPosition(Dimension objectSize, Dimension areaSize)
	{
		ParameterValidator.throwIfNull(objectSize, "objectSize");
		ParameterValidator.throwIfNull(areaSize, "areaSize");
		
		int x = (areaSize.width  - objectSize.width)  / 2;
		int y = (areaSize.height - objectSize.height) / 2;

		return new Point(x, y);
	}
	
	/**
	 * Loads data using specified loader into a buffered image.
	 * 
	 * The data is injected directly into specified image without any allocations nor copying.
	 * 
	 * @param image  Image to load data into.
	 * @param loader Loader which will be used to load data. It should store loaded data into given buffer.
	 *               If the data is RGB it should be loaded in RGBRGB... pattern.
	 */
	public static void loadDataIntoBufferedImage(BufferedImage image, Consumer<byte[]> loader)
	{
		ParameterValidator.throwIfNull(image, "image");
		ParameterValidator.throwIfNull(loader, "loader");
		
		byte[] imageDataBuffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();

		loader.accept(imageDataBuffer);

		// BufferedImage stores its pixels in BGR order, while we use RGB.
		if(image.getType() == BufferedImage.TYPE_3BYTE_BGR)
		{
			PixelDataUtil.swapRgbColorComponents(imageDataBuffer);
		}
	}
	
	/**
	 * Gets the biggest resolution index of image whose size is at most the specified byte count.
	 * 
	 * When there is no resolution smaller than specified value, the first resolution is returned (index == 0).
	 */
	public static int getResolutionIndexWithSizeNotBiggerThan(VirtualSlideImage image, long byteCount)
	{
		ParameterValidator.throwIfNull(image, "image");
		
		for(int i = 1; i < image.getResolutionCount() ;++i)
		{
			long width        = image.getImageSize(i).width;
			long height       = image.getImageSize(i).height;
			long channelCount = image.isRGB() ? 3 : 1;
			
			if(width * height * channelCount > byteCount)
			{
				return i - 1;
			}
		}
		
		return 0;
	}
}
