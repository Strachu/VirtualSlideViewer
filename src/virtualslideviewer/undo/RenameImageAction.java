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

import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ParameterValidator;

/**
 * An undoable action which renames the image.
 */
public class RenameImageAction implements UndoableAction
{
	private final VirtualSlideImage mImage;
	private final String            mOldName;
	private final String            mNewName;
	
	/**
	 * @param image   The image to rename.
	 * @param newName A new name for the image.
	 */
	public RenameImageAction(VirtualSlideImage image, String newName)
	{
		ParameterValidator.throwIfNull(image, "image");
		ParameterValidator.throwIfNull(newName, "newName");
		
		mImage   = image;
		mOldName = image.getName();
		mNewName = newName;
	}

	@Override
	public void execute()
	{
		mImage.setName(mNewName);
	}

	@Override
	public void undo()
	{
		mImage.setName(mOldName);
	}

	@Override
	public String getExecuteMessage()
	{
		return String.format("Changed image \"%s\" name to \"%s\"", mOldName, mNewName);
	}

	@Override
	public String getUndoMessage()
	{
		return String.format("Undid the change of image's name from \"%s\" name to \"%s\"", mOldName, mNewName);
	}
}
