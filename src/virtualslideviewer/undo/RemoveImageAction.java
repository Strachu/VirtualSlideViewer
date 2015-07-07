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

package virtualslideviewer.undo;

import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ParameterValidator;

/**
 * An undoable action which removes an image from a virtual slide.
 */
public class RemoveImageAction implements UndoableAction
{
	private final VirtualSlide      mVirtualSlide;
	private final VirtualSlideImage mImage;
	private final int               mImageIndex;
	
	/**
	 * @param virtualSlide A virtual slide to remove image from.
	 * @param imageIndex   The index of image to remove.
	 */
	public RemoveImageAction(VirtualSlide virtualSlide, int imageIndex)
	{
		ParameterValidator.throwIfNull(virtualSlide, "virtualSlide");
		
		mVirtualSlide = virtualSlide;
		mImage        = virtualSlide.getImageList().get(imageIndex);
		mImageIndex   = imageIndex;
	}

	@Override
	public void execute()
	{
		mVirtualSlide.removeImage(mImageIndex);
	}

	@Override
	public void undo()
	{
		mVirtualSlide.addImage(mImageIndex, mImage);
	}

	@Override
	public String getExecuteMessage()
	{
		return String.format("Removed image \"%s\"", mImage.getName());
	}

	@Override
	public String getUndoMessage()
	{
		return String.format("Restored image \"%s\"", mImage.getName());
	}
}
