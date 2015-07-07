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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import virtualslideviewer.ThreadMarshaller;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.core.persistence.VirtualSlidePersistenceService;
import virtualslideviewer.ui.imagelist.ImageListView.Listener;
import virtualslideviewer.undo.RemoveImageAction;
import virtualslideviewer.undo.RenameImageAction;
import virtualslideviewer.undo.UndoableActionSystem;
import virtualslideviewer.util.ParameterValidator;

/**
 * Presenter for an {@link ImageListView}.
 */
public class ImageListPresenter implements VirtualSlidePersistenceService.Listener
{
	/**
	 * A listener which will be notified when user wants to see new image.
	 */
	public interface ImageShowListener
	{
		/**
		 * Called when user requests to show new image.
		 * 
		 * @param imageToShow An image chosen by the user.
		 */
		public void onImageShowRequest(VirtualSlideImage imageToShow);
	}
	
	private final ImageListView        mView;
	private final UndoableActionSystem mUndoSystem;
	private final ThreadMarshaller     mUIThread;
	
	private VirtualSlide               mSlide;

	private final List<ImageShowListener> mListeners = new ArrayList<>();
	
	/**
	 * @param view       A view controlled by this presenter.
	 * @param undoSystem An undo system which will be used to provide the ability to undo executed actions.
	 * @param uiThread   An object allowing the code to be executed on UI thread.
	 */
	public ImageListPresenter(ImageListView view, UndoableActionSystem undoSystem, ThreadMarshaller uiThread)
	{
		ParameterValidator.throwIfNull(undoSystem, "undoSystem");
		ParameterValidator.throwIfNull(view, "view");
		ParameterValidator.throwIfNull(uiThread, "uiThread");
		
		mSlide        = null;
		mView         = view;
		mUndoSystem   = undoSystem;
		mUIThread     = uiThread;
		
		mView.addListener(new Listener()
		{
			@Override
			public void onImageShow()          { showImage(); }
			@Override
			public void onImageRename()        { renameImage(); }
			@Override
			public void onImageNameConfirmed() { stopImageNameEditing(); }
			@Override
			public void onImageRemove()        { removeImage(); }
		});
	}

	@Override
	public void onVirtualSlideLoaded(VirtualSlide loadedSlide, Path loadedFilePath)
	{
		mUIThread.executeAsynchronously(() ->
		{
			mSlide = loadedSlide;
			
			mView.setSourceVirtualSlide(loadedSlide);
			
			mView.setSelectedImageIndex(0);
			
			showImage();
		});
	}

	/**
	 * Adds a listener which will be notified when user wants to see new image.
	 */
	public void addImageShowListener(ImageShowListener listener)
	{
		ParameterValidator.throwIfNull(listener, "listener");
		
		mListeners.add(listener);
	}
	
	/**
	 * Notifies interested parties that user has selected an image to show.
	 */
	public void showImage()
	{
		int imageToShowIndex = mView.getSelectedImageIndex();
		
		if(imageToShowIndex >= 0)
		{
			VirtualSlideImage imageToShow = mSlide.getImageList().get(imageToShowIndex);
			
			mListeners.forEach(l -> l.onImageShowRequest(imageToShow));
		}
	}
	
	/**
	 * Tells the view to show an editor to edit selected image.
	 */
	public void renameImage()
	{
		int imageToRenameIndex = mView.getSelectedImageIndex();
		
		if(imageToRenameIndex >= 0)
		{
			mView.showNameEditor(imageToRenameIndex);
		}
	}

	/**
	 * Tells the view to close image's name editor if entered name is valid and renames the image.
	 */
	public void stopImageNameEditing()
	{
		int editedImageIndex = mView.getEditedImageIndex();
		if(editedImageIndex < 0)
		{
			return;
		}
		
		String enteredName = mView.getNameEditorValue().trim();
		if(enteredName.isEmpty())
		{
			mView.displayErrorMessage("The image name cannot be empty.");
			return;
		}

		mView.closeNameEditor();
		
		VirtualSlideImage editedImage = mSlide.getImageList().get(editedImageIndex);
		
		if(!enteredName.equals(editedImage.getName()))
		{
			mUndoSystem.execute(new RenameImageAction(editedImage, enteredName));
		}	
	}
	
	/**
	 * Removes currently selected image.
	 */
	public void removeImage()
	{
		int imageToRemoveIndex = mView.getSelectedImageIndex();
		
		if(imageToRemoveIndex >= 0)
		{
			mUndoSystem.execute(new RemoveImageAction(mSlide, imageToRemoveIndex));
		}
	}
}
