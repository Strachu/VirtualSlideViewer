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
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;

/**
 * An algorithm used to obtain a list of tiles to prefetch in the background after the visible image has been loaded.
 */
public interface PrefetchingStrategy
{
	/**
	 * Returns a list of tiles to prefetch.
	 * 
	 * @param image           The image from which the tiles will be loaded.
	 * @param loadedImagePart The bounds of part of image which has been just loaded.
	 * @param imageIndex      The image index from which the loading occured.
	 * 
	 * @return List of tiles to prefetch.
	 */
	List<Tile> getTilesToPrefetch(BufferedVirtualSlideImage image, Rectangle loadedImagePart, ImageIndex imageIndex);
}
