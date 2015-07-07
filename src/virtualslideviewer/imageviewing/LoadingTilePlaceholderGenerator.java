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

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.Tile;

/**
 * A generator for temporary tile data to use while the tile has not been loaded yet.
 */
public interface LoadingTilePlaceholderGenerator
{
	/**
	 * Generates a tile placeholder and saves it to specified buffer.
	 * 
	 * @param dst        Destination buffer.
	 * @param tileSource Image for which the tile should be generated.
	 * @param tile       The tile for which the placeholder should be generated.
	 */
	void getTilePlaceholder(byte[] dst, BufferedVirtualSlideImage tileSource, Tile tile);
}
