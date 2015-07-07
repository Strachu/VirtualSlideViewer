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

import java.nio.file.Path;

import javax.swing.SwingUtilities;

import virtualslideviewer.config.ApplicationConfiguration;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.persistence.VirtualSlidePersistenceService;
import virtualslideviewer.ui.MainView.Listener;
import virtualslideviewer.ui.about.AboutView;
import virtualslideviewer.ui.config.ConfigView;
import virtualslideviewer.undo.UndoableAction;
import virtualslideviewer.undo.UndoableActionSystem;
import virtualslideviewer.undo.UndoableActionSystemListener;
import virtualslideviewer.util.ParameterValidator;

public class MainPresenter implements UndoableActionSystemListener, VirtualSlidePersistenceService.Listener
{
	private final MainView                 mView;
	private final ConfigView               mPreferencesView;
	private final AboutView                mAboutView;
	private final UndoableActionSystem     mUndoSystem;
	private final ApplicationConfiguration mUserConfiguration;
	
	public MainPresenter(MainView view, ConfigView preferencesView, AboutView aboutView, 
	                     VirtualSlidePersistenceService slideLoadingService, UndoableActionSystem undoSystem,
	                     ApplicationConfiguration userConfiguration)
	{	
		ParameterValidator.throwIfNull(view, "view");
		ParameterValidator.throwIfNull(preferencesView, "preferencesView");
		ParameterValidator.throwIfNull(aboutView, "aboutView");
		ParameterValidator.throwIfNull(slideLoadingService, "slideLoadingService");
		ParameterValidator.throwIfNull(undoSystem, "undoSystem");
		ParameterValidator.throwIfNull(userConfiguration, "userConfiguration");

		mView              = view;
		mPreferencesView   = preferencesView;
		mAboutView         = aboutView;
		mUndoSystem        = undoSystem;
		mUserConfiguration = userConfiguration;
		
		slideLoadingService.addListener(this);
		
		mUndoSystem.addListener(this);
		
		mView.addListener(new Listener()
		{
			@Override
			public void onUndo() { undo(); }
			@Override
			public void onRedo() { redo(); }
			@Override
			public void onOpenPreferences() { openPreferencesDialog(); }
			@Override
			public void onAboutClick() { openAboutDialog(); }
			@Override
			public void onViewClosing() { saveWindowLayout(); }
		});
		
		initializeView();
	}

	private void initializeView()
	{
		mView.restoreLayoutFromConfiguration(mUserConfiguration);
		
		mView.setUndoEnabled(false);
		mView.setRedoEnabled(false);
	}
	
	@Override
	public void onVirtualSlideLoaded(VirtualSlide loadedSlide, Path loadedFilePath)
	{
		SwingUtilities.invokeLater(() -> mView.setTitle("Virtual Slide Viewer - " + loadedFilePath.getFileName()));
	}
	
	/**
	 * Undoes last action executed by a user.
	 */
	public void undo()
	{
		mUndoSystem.undo();
	}
	
	/**
	 * Redoes recently undone action.
	 */
	public void redo()
	{
		mUndoSystem.redo();
	}

	/**
	 * Activates / deactives the entries related to undo functionality depending on its availability.
	 */
	public void updateUndoRedoAvailability()
	{
		mView.setUndoEnabled(mUndoSystem.canUndo());
		mView.setRedoEnabled(mUndoSystem.canRedo());
	}
	
	@Override
	public void onExecute(UndoableAction executedAction)
	{
		updateUndoRedoAvailability();
		
		mView.setStatusMessage(executedAction.getExecuteMessage());
	}

	@Override
	public void onRedo(UndoableAction redoneAction)
	{
		updateUndoRedoAvailability();
		
		mView.setStatusMessage(redoneAction.getExecuteMessage());
	}

	@Override
	public void onUndo(UndoableAction undoneAction)
	{
		updateUndoRedoAvailability();
		
		mView.setStatusMessage(undoneAction.getUndoMessage());
	}

	@Override
	public void onHistoryCleared()
	{
		updateUndoRedoAvailability();
		
		mView.setStatusMessage("");
	}

	private void openPreferencesDialog()
	{
		mPreferencesView.show();
	}

	private void openAboutDialog()
	{
		mAboutView.show();
	}
	
	private void saveWindowLayout()
	{
		mView.saveLayoutToConfiguration(mUserConfiguration);
	}
}
