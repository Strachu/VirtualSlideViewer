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

package virtualslideviewer.ui;

import java.util.List;

import virtualslideviewer.core.SupportedFormatDescription;

public interface FileMenuView
{
	public interface Listener
	{
		/**
		 * Called when the user wants to open new file.
		 */
		public void onOpenFile();
		
		public void onSaveFile();
		
		public void onSaveFileAs();
	}
	
	public void addListener(Listener listener);
	
	public void setSaveEnabled(boolean enable);
	
	public void setSaveAsEnabled(boolean enable);
	
	public String askForFileToOpen();
	
	public String askForDestinationPath(List<SupportedFormatDescription> supportedFormats);
	
	public void displayErrorMessage(String message);
}
