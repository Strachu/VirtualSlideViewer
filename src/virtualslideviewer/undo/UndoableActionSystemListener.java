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
 * A listener notified on operations done by the {@link UndoableActionSystem}.
 */
public interface UndoableActionSystemListener
{
	/**
	 * Called when an action is executed and added to undo list with a call to {@link UndoableActionSystem#execute(UndoableAction)}
	 * 
	 * @param executedAction Just executed action.
	 */
	public void onExecute(UndoableAction executedAction);
	
	/**
	 * Called when an action is redone by a call to {@link UndoableActionSystem#redo()}
	 * 
	 * @param redoneAction Just redone action.
	 */
	public void onRedo(UndoableAction redoneAction);
	
	/**
	 * Called when an action is undone by a call to {@link UndoableActionSystem#undo()}
	 * 
	 * @param undoneAction Just undone action.
	 */
	public void onUndo(UndoableAction undoneAction);
	
	/**
	 * Called when the undo history has been cleared.
	 */
	public void onHistoryCleared();
}
