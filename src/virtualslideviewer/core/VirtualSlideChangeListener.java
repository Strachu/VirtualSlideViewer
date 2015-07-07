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


/**
 * Listener used to get notifications about changes made to a virtual slide.
 */
public interface VirtualSlideChangeListener
{
	/**
	 * Called when an image has been added to observed virtual slide.
	 * 
	 * @param source   The virtual slide which has been changed.
	 * @param newImage Added image.
	 */
	void onImageAdd(VirtualSlide source, VirtualSlideImage newImage);
	
	/**
	 * Called when an image has been remove from observed virtual slide.
	 * 
	 * @param source       The virtual slide which has been changed.
	 * @param removedImage Removed image.
	 */
	void onImageRemove(VirtualSlide source, VirtualSlideImage removedImage);
}
