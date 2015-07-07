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

package virtualslideviewer.ui.imagelist;

import virtualslideviewer.core.VirtualSlide;

/**
 * A view whose responsibility is to display the list of virtual slide's images and provide the ability to either
 * choose the image to display, rename or to remove it from the virtual slide.
 */
public interface ImageListView
{
	/**
	 * Listener which will be notified about user's actions.
	 */
	public interface Listener
	{
		/**
		 * Called when an user chooses an image to display.
		 */
		void onImageShow();
		
		/**
		 * Called when an user wants to rename an image.
		 */
		void onImageRename();
		
		/**
		 * Called when an user entered and confirmed a new name for an image.
		 */
		void onImageNameConfirmed();
		
		/**
		 * Called when user wants to remove an image the from virtual slide.
		 */
		void onImageRemove();
	}
	
	/**
	 * Adds an listener to this view which will be notified about user's actions.
	 */
	void addListener(Listener listener);

	/**
	 * Changes the virtual slide whose list of images will be displayed.
	 */
	void setSourceVirtualSlide(VirtualSlide virtualSlide);
	
	/**
	 * Gets an index of currently selected image.
	 * 
	 * Returns negative number if there is no image selected.
	 */
	int getSelectedImageIndex();

	void setSelectedImageIndex(int index);
	
	/**
	 * Shows an editor to give the user possibly to enter new name for an image.
	 * 
	 * @param imageToRenameIndex An index of image which will be renamed.
	 */
	void showNameEditor(int imageToRenameIndex);

	/**
	 * Gets an index of image whose name is currently edited.
	 * 
	 * Returns negative number if none image's name is currently edited.
	 */
	int getEditedImageIndex();
	
	/**
	 * Gets a name currently entered in already open name editor.
	 */
	String getNameEditorValue();

	/**
	 * Closes the name editor.
	 */
	void closeNameEditor();

	/**
	 * Informs user about an error.
	 * 
	 * @param error Error message which should be displayed to the user.
	 */
	void displayErrorMessage(String error);
}
