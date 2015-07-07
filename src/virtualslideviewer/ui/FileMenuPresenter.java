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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.persistence.SaveProgressReporter;
import virtualslideviewer.core.persistence.VirtualSlideLoadException;
import virtualslideviewer.core.persistence.VirtualSlidePersistenceService;
import virtualslideviewer.ui.progress.ProgressViewFactory;
import virtualslideviewer.ui.utils.BackgroundOperationUtil;
import virtualslideviewer.undo.UndoableAction;
import virtualslideviewer.undo.UndoableActionSystem;
import virtualslideviewer.undo.UndoableActionSystemListener;
import virtualslideviewer.util.NullVirtualSlide;
import virtualslideviewer.util.ParameterValidator;

public class FileMenuPresenter
{
	private FileMenuView                   mView;
	private ProgressViewFactory            mProgressViewFactory;
	private VirtualSlidePersistenceService mSlideLoadingService;
	private UndoableActionSystem           mUndoSystem;

	private VirtualSlide                   mCurrentVirtualSlide     = new NullVirtualSlide();
	private Path                           mCurrentVirtualSlidePath;
	
	public FileMenuPresenter(FileMenuView view, ProgressViewFactory progressViewFactory,
	                         VirtualSlidePersistenceService slideLoadingService,
	                         UndoableActionSystem undoSystem)
	{	
		ParameterValidator.throwIfNull(view, "view");
		ParameterValidator.throwIfNull(progressViewFactory, "progressViewFactory");
		ParameterValidator.throwIfNull(slideLoadingService, "slideLoadingService");
		ParameterValidator.throwIfNull(undoSystem, "undoSystem");
		
		mView                = view;
		mProgressViewFactory = progressViewFactory;
		mUndoSystem          = undoSystem;
		mSlideLoadingService = slideLoadingService;
		
		mUndoSystem.addListener(new UndoableActionSystemListener()
		{
			@Override
			public void onUndo(UndoableAction undoneAction) { updateSaveAvailability(); }
			@Override
			public void onRedo(UndoableAction redoneAction) { updateSaveAvailability(); }
			@Override
			public void onExecute(UndoableAction executedAction) { updateSaveAvailability(); }
			@Override
			public void onHistoryCleared() { updateSaveAvailability(); }
		});
		
		mView.addListener(new FileMenuView.Listener()
		{
			@Override
			public void onOpenFile() { openFile(); }
			@Override
			public void onSaveFile() { saveFile(); }
			@Override
			public void onSaveFileAs() { saveFileAs(); }
		});
		
		initializeView();
	}

	private void initializeView()
	{
		mView.setSaveEnabled(false);
		mView.setSaveAsEnabled(false);
	}
	
	private void updateSaveAvailability()
	{
		mView.setSaveEnabled(mUndoSystem.canUndo());
	}
	
	public void openFile()
	{
		String fileToOpen = mView.askForFileToOpen();
		if(fileToOpen == null)
			return;
		
		BackgroundOperationUtil.startBackgroundOperation(() ->
		{
			return mProgressViewFactory.showProgressForNextLoading(fileToOpen);
		},
		x -> 
		{
			load(Paths.get(fileToOpen));
		});
	}
	
	private void load(Path fileToOpen)
	{
		try
		{
			VirtualSlide previousVirtualSlide = mCurrentVirtualSlide;
			
			mCurrentVirtualSlide     = mSlideLoadingService.load(fileToOpen);
			mCurrentVirtualSlidePath = fileToOpen;
			
			mUndoSystem.clearHistory();

			previousVirtualSlide.close();		
			
			mView.setSaveAsEnabled(true);
		}
		catch(UncheckedInterruptedException e)
		{
			// Nothing
		}
		catch(UnsupportedOperationException e)
		{
			mView.displayErrorMessage("File " + fileToOpen + " is in unsupported format.");
		}
		catch(VirtualSlideLoadException e)
		{
			mView.displayErrorMessage("An error occured during the loading of a virtual slide.\nDetails: " + e.getMessage());
		}
	}
	
	private void saveFile()
	{
		BackgroundOperationUtil.startBackgroundOperation(() ->
		{
			return mProgressViewFactory.showProgressForNextSaving(mCurrentVirtualSlidePath.toString());
		},
		progressReporter -> 
		{
			try
			{
				save(mCurrentVirtualSlidePath, progressReporter, mCurrentVirtualSlide.getFormat());
			}
			catch(UnsupportedOperationException e)
			{
				saveFileAs();
			}
		});
	}
	
	private void saveFileAs()
	{
		String destinationPath = mView.askForDestinationPath(mSlideLoadingService.getFormatsWithSaveSupport());
		if(destinationPath == null)
			return;
		
		String destinationPathWithExtension = appendExtension(destinationPath);
		
		BackgroundOperationUtil.startBackgroundOperation(() ->
		{
			return mProgressViewFactory.showProgressForNextSaving(destinationPathWithExtension);
		},
		progressReporter -> 
		{
			save(Paths.get(destinationPathWithExtension), progressReporter, "OME-TIFF");
		});
	}
	
	private String appendExtension(String path)
	{
		return (!path.contains(".")) ? path.concat(".ome.tiff") : path;
	}
	
	private void save(Path destinationPath, SaveProgressReporter progressView, String destinationFormat)
	{
		try
		{
			mSlideLoadingService.save(mCurrentVirtualSlide, destinationPath, destinationFormat, progressView);
			
			load(destinationPath);
		}
		catch(UncheckedInterruptedException e)
		{
			// Nothing
		}
		catch(IOException e)
		{
			mView.displayErrorMessage("An error occured during the saving of a virtual slide.\nDetails: " + e.getMessage());
		}
	}
}
