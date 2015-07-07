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

/**
 * An user action which can be undone.
 */
public interface UndoableAction
{
	/**
	 * Executes the action.
	 */
	public void execute();
	
	/**
	 * Undoes the action.
	 */
	public void undo();
	
	/**
	 * Returns user-friendly string explaining what has been done by the {@link #execute()} method.
	 */
	public String getExecuteMessage();
	
	/**
	 * Returns user-friendly string explaining what happened in the {@link #undo()} method.
	 */
	public String getUndoMessage();
}
