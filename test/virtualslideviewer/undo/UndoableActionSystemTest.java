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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import virtualslideviewer.undo.UndoableAction;
import virtualslideviewer.undo.UndoableActionSystem;
import virtualslideviewer.undo.UndoableActionSystemListener;

public class UndoableActionSystemTest
{
	private UndoableAction       mActionMock;
	private UndoableAction       mActionMock2;
	private UndoableAction       mActionMock3;
	private UndoableActionSystem mTestedSystem;

	@Before
	public void setUp() throws Exception
	{
		mActionMock   = Mockito.mock(UndoableAction.class);
		mActionMock2  = Mockito.mock(UndoableAction.class);
		mActionMock3  = Mockito.mock(UndoableAction.class);
		
		mTestedSystem = new UndoableActionSystem();
	}
	
	@Test
	public void testSystemExecutesAction()
	{
		mTestedSystem.execute(mActionMock);
		
		Mockito.verify(mActionMock).execute();
	}
	
	@Test
	public void testSystemAllowsToUndoExecutedAction()
	{
		mTestedSystem.execute(mActionMock);
		
		assertThat(mTestedSystem.canUndo(), is(true));
	}

	@Test
	public void testUndoCallsActionsUndoMethod()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		Mockito.verify(mActionMock).undo();
	}
	
	@Test
	public void testSystemDoesNotAllowToUndoWhenLastActionHasBeenUndone()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		assertThat(mTestedSystem.canUndo(), is(false));
	}
	
	@Test
	public void testSystemAllowsToUndoWhenThereIsStillActionToUndoAfterOneUndo()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		assertThat(mTestedSystem.canUndo(), is(true));
	}

	@Test
	public void testSystemAllowsToRedoUndoneAction()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		assertThat(mTestedSystem.canRedo(), is(true));
	}

	@Test
	public void testRedoCallsActionExecuteMethod()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		mTestedSystem.redo();
		
		Mockito.verify(mActionMock, Mockito.times(2)).execute();
	}

	@Test
	public void testSystemAllowsToUndoRedoneAction()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		mTestedSystem.redo();
		
		assertThat(mTestedSystem.canUndo(), is(true));
	}

	@Test
	public void testExecutionOfNewActionClearsRedoHistory()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		mTestedSystem.execute(mActionMock);
		
		assertThat(mTestedSystem.canRedo(), is(false));
	}
	
	@Test
	public void testNewSystemDoesNotAllowToUndoNorRedo()
	{
		assertThat(mTestedSystem.canUndo(), is(false));
		assertThat(mTestedSystem.canRedo(), is(false));
	}
	
	@Test
	public void testUndoWorksLikeAStack()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.execute(mActionMock2);
		mTestedSystem.execute(mActionMock3);
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.undo();
		
		InOrder order = Mockito.inOrder(mActionMock, mActionMock2, mActionMock3);
		order.verify(mActionMock3).undo();
		order.verify(mActionMock2).undo();
		order.verify(mActionMock ).undo();
	}
	
	@Test
	public void testRedoExecutesActionsInOriginalOrder()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.execute(mActionMock2);
		mTestedSystem.execute(mActionMock3);
		
		Mockito.reset(mActionMock, mActionMock2, mActionMock3);
		
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.redo();
		mTestedSystem.redo();
		mTestedSystem.redo();
		
		InOrder order = Mockito.inOrder(mActionMock, mActionMock2, mActionMock3);
		order.verify(mActionMock ).execute();
		order.verify(mActionMock2).execute();
		order.verify(mActionMock3).execute();
	}
	
	@Test
	public void testUndoExecutesActionsLikeAStackAfterRedoingThem()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.execute(mActionMock2);
		mTestedSystem.execute(mActionMock3);		
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.redo();
		mTestedSystem.redo();
		mTestedSystem.redo();
		
		Mockito.reset(mActionMock, mActionMock2, mActionMock3);
		
		mTestedSystem.undo();
		mTestedSystem.undo();
		mTestedSystem.undo();
		
		InOrder order = Mockito.inOrder(mActionMock, mActionMock2, mActionMock3);
		order.verify(mActionMock3).undo();
		order.verify(mActionMock2).undo();
		order.verify(mActionMock ).undo();
	}
	
	@Test
	public void testUndoAndRedoIsNotAllowedAfterClearingHistory()
	{
		mTestedSystem.execute(mActionMock);
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		mTestedSystem.clearHistory();
		
		assertThat(mTestedSystem.canUndo(), is(false));
		assertThat(mTestedSystem.canRedo(), is(false));
	}
	
	@Test
	public void testActionExecutionNotifiesListeners()
	{
		UndoableActionSystemListener listenerMock = Mockito.mock(UndoableActionSystemListener.class);
		mTestedSystem.addListener(listenerMock);
		
		mTestedSystem.execute(mActionMock);
		
		Mockito.verify(listenerMock).onExecute(mActionMock);
	}	
	
	@Test
	public void testActionUndoNotifiesListeners()
	{
		UndoableActionSystemListener listenerMock = Mockito.mock(UndoableActionSystemListener.class);
		mTestedSystem.addListener(listenerMock);
		
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		
		Mockito.verify(listenerMock).onUndo(mActionMock);
	}	

	@Test
	public void testActionRedoNotifiesListeners()
	{
		UndoableActionSystemListener listenerMock = Mockito.mock(UndoableActionSystemListener.class);
		mTestedSystem.addListener(listenerMock);
		
		mTestedSystem.execute(mActionMock);
		mTestedSystem.undo();
		mTestedSystem.redo();
		
		Mockito.verify(listenerMock).onRedo(mActionMock);
	}
}
