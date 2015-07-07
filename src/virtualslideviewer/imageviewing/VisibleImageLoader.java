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

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;

/**
 * An interface for objects which can be used to load currently visible part of a virtual slide image.
 */
public interface VisibleImageLoader
{
	/**
	 * Loads visible part of pixels from the image.
	 * 
	 * The loaded pixels will be written into specified buffer if the data is already available.
	 * If it is not, a placeholder pixels will be written and after the real data is available, an user specified callback
	 * will be called.
	 * 
	 * After the callback has been called, new data can be retrieved by calling this function once again.
	 * 
	 * @param image               Image from which visible part will be loaded.
	 * @param dst                 Buffer which will receive already the data.
	 * @param visibleImageBounds  The bounds of currently visible image.
	 * @param imageIndex          The image index from which to retrieve data.
	 * @param dataUpdatedCallback A callback which will be called when new data has been made available.
	 *                            The callback may be called from a <b>different thread</b>.
	 */
	void getVisibleImageData(BufferedVirtualSlideImage image, byte[] dst, Rectangle visibleImageBounds, ImageIndex imageIndex,
	                         Runnable dataUpdatedCallback);
}
