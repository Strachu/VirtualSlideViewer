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

package virtualslideviewer.core;

import java.awt.Rectangle;
import java.util.List;

import virtualslideviewer.util.ImageUtil;
import virtualslideviewer.util.ParameterValidator;

/**
 * Virtual slide image decorator which adds buffering to functions retrieving image pixels.
 */
public class BufferedVirtualSlideImage extends VirtualSlideImageDecorator
{
	private final TileCache mTileCache;
	
	/**
	 * @param imageToDecorate Image to which add buffering.
	 * @param tileCache       Cache for storing tiles.
	 */
	public BufferedVirtualSlideImage(VirtualSlideImage imageToDecorate, TileCache tileCache)
	{
		super(imageToDecorate);
		
		ParameterValidator.throwIfNull(tileCache, "tileCache");
		
		mTileCache = tileCache;
	}

	@Override
	public void getTileData(byte[] dst, Tile tile)
	{
		ParameterValidator.throwIfNull(dst, "dst");
		
		ensureTileDataCached(tile);
		
		byte[] cachedData = mTileCache.getTileData(this, tile);
		
		System.arraycopy(cachedData, 0, dst, 0, cachedData.length);
	}

	/**
	 * Ensures that the tile is in cache by loading it if it's not there already.
	 * 
	 * @param tile Tile which should be in cache.
	 */
	public void ensureTileDataCached(Tile tile)
	{
		ParameterValidator.throwIfNull(tile, "tile");
		
		if(!mTileCache.hasTile(this, tile))
		{
			byte[] tileData = mDecoratedImage.getTileData(tile);
			
			mTileCache.addTile(this, tile, tileData);
		}
	}
	
	/**
	 * Checks whether subimage with specified bounds is already in cache, thus guaranting fast pixel retrieving.
	 * 
	 * @param imageBounds Subimage bounds.
	 * @param imageIndex  Image index.
	 * 
	 * @return True, when the entire specified image is in cache, false in opposite case.
	 */
	public boolean isImageInCache(Rectangle imageBounds, ImageIndex imageIndex)
	{
		List<Tile> imageTiles = ImageUtil.getTilesInArea(imageBounds, mDecoratedImage.getTileSize(imageIndex.getResolutionIndex()), imageIndex);
		
		for(Tile tile : imageTiles)
		{
			if(!mTileCache.hasTile(this, tile))
			{
				return false;
			}
		}
		
		return true;
	}
}
