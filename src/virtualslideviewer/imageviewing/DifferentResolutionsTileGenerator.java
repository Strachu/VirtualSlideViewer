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

package virtualslideviewer.imageviewing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ImageUtil;

/**
 * Tile placeholder generator which generates tiles from other resolutions if required data is already in cache.
 * If the data is not available in other resolutions, the tile will be generated from its thumbnail.
 */
public class DifferentResolutionsTileGenerator implements LoadingTilePlaceholderGenerator
{
	private final static long MAX_THUMBNAIL_SIZE = 1024 * 1024 * 3;
	
	@Override
	public void getTilePlaceholder(byte[] dst, BufferedVirtualSlideImage tileSource, Tile tileToGenerate)
	{
		boolean generatedFromOtherResolution = tryToGenerateTileFromOtherResolutions(dst, tileSource, tileToGenerate);
		
		if(!generatedFromOtherResolution)
		{
			generateTileFromThumbnail(dst, tileSource, tileToGenerate);
		}	
	}
	
	private boolean tryToGenerateTileFromOtherResolutions(byte[] dst, BufferedVirtualSlideImage tileSource, Tile tileToGenerate)
	{		
		int highestResToCheck = Math.min(tileToGenerate.getImageIndex().getResolutionIndex() + 1, tileSource.getResolutionCount() - 1);
		int thumbnailResIndex = ImageUtil.getResolutionIndexWithSizeNotBiggerThan(tileSource, MAX_THUMBNAIL_SIZE);
		
		for(int currentRes = highestResToCheck; currentRes >= thumbnailResIndex; currentRes--)
		{
			Rectangle originalTileBounds  = tileToGenerate.getBounds(tileSource);
			Rectangle tileBoundsInThisRes = scaleBoundsToMatchDifferentResolution(tileSource, originalTileBounds,
			                                                                      tileToGenerate.getImageIndex().getResolutionIndex(), currentRes);
			
			ImageIndex imageIndexAtThisRes = new ImageIndex(currentRes,
			                                                tileToGenerate.getImageIndex().getChannel(),
			                                                tileToGenerate.getImageIndex().getZPlane(),
			                                                tileToGenerate.getImageIndex().getTimePoint());
			
			if(tileSource.isImageInCache(tileBoundsInThisRes, imageIndexAtThisRes))
			{
				byte[] generatedTile = getScaledPixels(tileSource, tileBoundsInThisRes, imageIndexAtThisRes, originalTileBounds.getSize());
		
				System.arraycopy(generatedTile, 0, dst, 0, generatedTile.length);	
				return true;
			}
		}
		
		return false;
	}
	
	private void generateTileFromThumbnail(byte[] dst, VirtualSlideImage source, Tile tileToGenerate)
	{
		// TODO It should read data from some pregenerated thumbnail which is persistent in memory
		
		// The lowest resolution of some images is far too small.
		int thumbnailResIndex           = ImageUtil.getResolutionIndexWithSizeNotBiggerThan(source, MAX_THUMBNAIL_SIZE);
		
		Rectangle tileBounds            = tileToGenerate.getBounds(source);
		Rectangle tileBoundsInThumbnail = scaleBoundsToMatchDifferentResolution(source, tileBounds, tileToGenerate.getImageIndex().getResolutionIndex(),
		                                                                        thumbnailResIndex);
		
		ImageIndex thumbnailImageIndex = new ImageIndex(thumbnailResIndex,
		                                                tileToGenerate.getImageIndex().getChannel(),
		                                                tileToGenerate.getImageIndex().getZPlane(),
		                                                tileToGenerate.getImageIndex().getTimePoint());

		byte[] generatedTile = getScaledPixels(source, tileBoundsInThumbnail, thumbnailImageIndex, tileBounds.getSize());
		
		System.arraycopy(generatedTile, 0, dst, 0, generatedTile.length);	
	}
	
	/**
	 * Scales given bounds in original resolution to match the same region in different resolution.
	 */
	private Rectangle scaleBoundsToMatchDifferentResolution(VirtualSlideImage sourceImage, Rectangle originalBounds,
	                                                        int originalResIndex, int newResIndex)
	{
		double scaleX = sourceImage.getImageSize(newResIndex).getWidth()  / sourceImage.getImageSize(originalResIndex).getWidth();
		double scaleY = sourceImage.getImageSize(newResIndex).getHeight() / sourceImage.getImageSize(originalResIndex).getHeight();

		int x      = (int)(originalBounds.x * scaleX);
		int y      = (int)(originalBounds.y * scaleY);
		int width  = (int)Math.ceil(originalBounds.width  * scaleX);
		int height = (int)Math.ceil(originalBounds.height * scaleY);
		
		return new Rectangle(x, y, width, height);		
	}
	
	private byte[] getScaledPixels(VirtualSlideImage source, Rectangle originalPixelsBounds, ImageIndex imageIndex, Dimension newSize)
	{
		BufferedImage originalImage = new BufferedImage(originalPixelsBounds.width, originalPixelsBounds.height,
		                                               (source.isRGB() ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY));
		
		byte[] originalImageDataBuffer = ((DataBufferByte)originalImage.getRaster().getDataBuffer()).getData();
		
		source.getPixels(originalImageDataBuffer, originalPixelsBounds, imageIndex);
		
		BufferedImage scaledImage = ImageUtil.scaleImage(originalImage, newSize, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		
		return ((DataBufferByte)scaledImage.getRaster().getDataBuffer()).getData();	
	}
}
