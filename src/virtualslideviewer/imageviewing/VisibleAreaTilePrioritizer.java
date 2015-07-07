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

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.Tile;

/**
 * An implementation of TileLoadingPrioritizer which sorts the tiles by their visible area.
 * Tiles with the largest visible come first.
 */
public class VisibleAreaTilePrioritizer implements TileLoadingPrioritizer
{
	@Override
	public void sortTilesByPriority(List<Tile> tilesToSort, BufferedVirtualSlideImage image, Rectangle imageBounds)
	{
		Collections.sort(tilesToSort, (tile1, tile2) -> 
		{
			Rectangle firstTileVisibleRegionBounds  = tile1.getBounds(image).intersection(imageBounds);
			Rectangle secondTileVisibleRegionBounds = tile2.getBounds(image).intersection(imageBounds);
			
			int firstTileVisibleRegionArea  = firstTileVisibleRegionBounds.width  * firstTileVisibleRegionBounds.height;
			int secondTileVisibleRegionArea = secondTileVisibleRegionBounds.width * secondTileVisibleRegionBounds.height;
			
			return secondTileVisibleRegionArea - firstTileVisibleRegionArea;
		});
	}
}
