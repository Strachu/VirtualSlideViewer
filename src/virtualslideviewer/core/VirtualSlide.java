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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import virtualslideviewer.util.ParameterValidator;

/**
 * A virtual slide.
 */
public abstract class VirtualSlide implements AutoCloseable
{
	private final List<VirtualSlideChangeListener> mListeners = new ArrayList<>();

	/**
	 * The list of images in a virtual slide.
	 */
	protected List<VirtualSlideImage> mImages = new ArrayList<>();

	/**
	 * Adds a listener which will be notified about changes made to this virtual slide.
	 */
	public void addChangeListener(VirtualSlideChangeListener newListener)
	{
		ParameterValidator.throwIfNull(newListener, "newListener");
		
		mListeners.add(newListener);
	}
	
	/**
	 * Removes a listener thus disabling notifications about changes for it.
	 */
	public void removeChangeListener(VirtualSlideChangeListener newListener)
	{
		mListeners.remove(newListener);
	}
	
	/**
	 * Adds an image to virtual slide.
	 * 
	 * @param imageIndex The position at which the image will be put.
	 * @param image      New image.
	 */
	public void addImage(int imageIndex, VirtualSlideImage image)
	{
		ParameterValidator.throwIfNull(image, "image");
		
		mImages.add(imageIndex, image);
		
		mListeners.forEach(l -> l.onImageAdd(this, image));
	}

	/**
	 * Returns a read only list of images in this virtual slide.
	 */
	public List<VirtualSlideImage> getImageList()
	{
		return Collections.unmodifiableList(mImages);
	}
	
	/**
	 * Removes an image from virtual slide.
	 * 
	 * @param imageIndex An index of image to remove.
	 */
	public void removeImage(int imageIndex)
	{
		VirtualSlideImage removedImage = mImages.remove(imageIndex);
		
		mListeners.forEach(l -> l.onImageRemove(this, removedImage));
	}

	public abstract String getFormat();

	@Override
	public void close()
	{
		for(VirtualSlideImage image : mImages)
		{
			image.close();
		}
	}
}
