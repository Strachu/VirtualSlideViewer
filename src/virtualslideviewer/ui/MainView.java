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

import virtualslideviewer.config.ApplicationConfiguration;

public interface MainView
{
	/**
	 * Listener which will be notified about user's actions.
	 */
	public interface Listener
	{
		/**
		 * Called when a user wants to undo his last action.
		 */
		public void onUndo();
		
		/**
		 * Called when a user wants to redo last undone action.
		 */
		public void onRedo();
		
		public void onOpenPreferences();
		
		public void onAboutClick();
		
		public void onViewClosing();
	}
	
	/**
	 * Adds an listener to this view which will be notified about user's actions.
	 */
	public void addListener(Listener listener);

	/**
	 * Enables or disables undo widget.
	 */
	public void setUndoEnabled(boolean enable);
	
	/**
	 * Enables or disables redo widget.
	 */
	public void setRedoEnabled(boolean enable);
	
	public void setStatusMessage(String message);
	
	public void displayErrorMessage(String message);
	
	public void setTitle(String title);
	
	public void restoreLayoutFromConfiguration(ApplicationConfiguration configuration);
	
	public void saveLayoutToConfiguration(ApplicationConfiguration configuration);
	
	public void show();
}
