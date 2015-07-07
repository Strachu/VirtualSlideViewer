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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import virtualslideviewer.util.ParameterValidator;

/**
 * A system which allows to execute actions and then undo it later.
 */
public class UndoableActionSystem
{
	private final Deque<UndoableAction>              mUndoList  = new ArrayDeque<>();
	private final Deque<UndoableAction>              mRedoList  = new ArrayDeque<>();
	
	private final List<UndoableActionSystemListener> mListeners = new ArrayList<>();
	
	/**
	 * Adds a listener which will be notified when any action is executed.
	 */
	public void addListener(UndoableActionSystemListener newListener)
	{
		ParameterValidator.throwIfNull(newListener, "newListener");
		
		mListeners.add(newListener);
	}

	/**
	 * Executes given action adds it to an undo list.
	 * 
	 * @param action Action to execute.
	 */
	public void execute(UndoableAction action)
	{
		ParameterValidator.throwIfNull(action, "action");
		
		action.execute();
		
		mUndoList.addFirst(action);
		mRedoList.clear();
		
		mListeners.forEach(l -> l.onExecute(action));
	}

	/**
	 * Checks whether there is some action to undo.
	 */
	public boolean canUndo()
	{
		return !mUndoList.isEmpty();
	}

	/**
	 * Checks whether there is some action to redo.
	 */
	public boolean canRedo()
	{
		return !mRedoList.isEmpty();
	}

	/**
	 * Undoes an recently executed action.
	 */
	public void undo()
	{
		if(canUndo())
		{
			UndoableAction actionToUndo = mUndoList.removeFirst();
			mRedoList.addFirst(actionToUndo);
			
			actionToUndo.undo();
			
			mListeners.forEach(l -> l.onUndo(actionToUndo));
		}
	}

	/**
	 * Redoes an recently undone action.
	 */
	public void redo()
	{
		if(canRedo())
		{
			UndoableAction actionToRedo = mRedoList.removeFirst();
			mUndoList.addFirst(actionToRedo);
			
			actionToRedo.execute();
			
			mListeners.forEach(l -> l.onRedo(actionToRedo));
		}
	}

	/**
	 * Removes all actions from undo and redo history.
	 */
	public void clearHistory()
	{
		mUndoList.clear();
		mRedoList.clear();
		
		mListeners.forEach(l -> l.onHistoryCleared());
	}
}
