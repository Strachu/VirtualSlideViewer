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
import java.util.ArrayList;
import java.util.List;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;

/**
 * A prefetching strategy which prefetches neighbouring tiles in specified radius.
 */
public class NeighbourPrefetchingStrategy implements PrefetchingStrategy
{
	private final int mPrefetchingRadius;
	
	/**
	 * @param prefetchingRadius The radius in pixels of area to prefetch.
	 */
	public NeighbourPrefetchingStrategy(int prefetchingRadius)
	{
		mPrefetchingRadius = prefetchingRadius;
	}
	
	@Override
	public List<Tile> getTilesToPrefetch(BufferedVirtualSlideImage image, Rectangle loadedImagePart, ImageIndex imageIndex)
	{
		Rectangle loadedTiles       = getLoadedTilesBounds(loadedImagePart, image.getTileSize(imageIndex.getResolutionIndex()));
		Rectangle prefetchingRegion = getPrefetchingRegion(loadedTiles, image, imageIndex.getResolutionIndex());
				
		List<Tile> tilesToPrefetch  = new ArrayList<Tile>();
		
		for(int y = prefetchingRegion.y; y < prefetchingRegion.getMaxY(); y++)
		{ 
			for(int x = prefetchingRegion.x; x < prefetchingRegion.getMaxX(); x++)
			{
				if(!loadedTiles.contains(x, y))
				{
					tilesToPrefetch.add(new Tile(x, y, imageIndex));
				}
			}
		}

		return tilesToPrefetch;
	}
	
	private Rectangle getLoadedTilesBounds(Rectangle loadedImagePart, Dimension tileSize)
	{
		int x1 = (int)Math.floor(loadedImagePart.getX()     / tileSize.getWidth());
		int y1 = (int)Math.floor(loadedImagePart.getY()     / tileSize.getHeight());
		int x2 = (int)Math.ceil((loadedImagePart.getMaxX()) / tileSize.getWidth());
		int y2 = (int)Math.ceil((loadedImagePart.getMaxY()) / tileSize.getHeight());

		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	
	private Rectangle getPrefetchingRegion(Rectangle loadedTilesBounds, BufferedVirtualSlideImage image, int resIndex)
	{
		Dimension tileSize              = image.getTileSize(resIndex);
		Dimension tilePrefetchingRadius = getPrefetchingRadiusInTiles(tileSize);
		
		int tileColumns = (int)Math.ceil(image.getImageSize(resIndex).getWidth()  / tileSize.getWidth());
		int tileRows    = (int)Math.ceil(image.getImageSize(resIndex).getHeight() / tileSize.getHeight());
		
		int x1 = (int)Math.max(loadedTilesBounds.getX()    - tilePrefetchingRadius.getWidth(),  0);
		int y1 = (int)Math.max(loadedTilesBounds.getY()    - tilePrefetchingRadius.getHeight(), 0);
		int x2 = (int)Math.min(loadedTilesBounds.getMaxX() + tilePrefetchingRadius.getWidth(),  tileColumns);
		int y2 = (int)Math.min(loadedTilesBounds.getMaxY() + tilePrefetchingRadius.getHeight(), tileRows);
		
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	
	private Dimension getPrefetchingRadiusInTiles(Dimension tileSize)
	{
		int tilesInX = (int)Math.ceil(mPrefetchingRadius / tileSize.getWidth());
		int tilesInY = (int)Math.ceil(mPrefetchingRadius / tileSize.getHeight());
		
		return new Dimension(tilesInX, tilesInY);
	}
}
