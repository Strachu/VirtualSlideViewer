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
import java.util.List;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.Tile;

/**
 * An algorithm used to sort tiles to load by their priority.
 */
public interface TileLoadingPrioritizer
{
	/**
	 * Sorts given list of tiles by their priority to load.
	 * The tiles will be loaded in the order in which they are in the list.
	 * 
	 * @param tilesToSort List of tiles to sort.
	 * @param image       Image to which the tiles belong to.
	 * @param imageBounds The bounds of loaded region of the image.
	 */
	void sortTilesByPriority(List<Tile> tilesToSort, BufferedVirtualSlideImage image, Rectangle imageBounds);
}
