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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.testutils.InPlaceExecutingThreadMarshaller;
import virtualslideviewer.ui.imagelist.ImageListPresenter;
import virtualslideviewer.ui.imagelist.ImageListView;
import virtualslideviewer.undo.RemoveImageAction;
import virtualslideviewer.undo.RenameImageAction;
import virtualslideviewer.undo.UndoableActionSystem;

public class ImageListPresenterTest
{
	private VirtualSlide         mVirtualSlideMock;
	private UndoableActionSystem mActionSystemMock;
	private ImageListView	     mViewMock;
	private ImageListPresenter   mTestedPresenter;

	@Before
	public void setUp() throws Exception
	{
		mVirtualSlideMock = Mockito.mock(VirtualSlide.class);
		mActionSystemMock = Mockito.mock(UndoableActionSystem.class);
		mViewMock         = Mockito.mock(ImageListView.class);
		
		mTestedPresenter  = new ImageListPresenter(mViewMock, mActionSystemMock, new InPlaceExecutingThreadMarshaller());
		
		Mockito.when(mVirtualSlideMock.getImageList()).thenReturn(Collections.nCopies(5, Mockito.mock(VirtualSlideImage.class)));
		
		mTestedPresenter.onVirtualSlideLoaded(mVirtualSlideMock, null);
	}

	@Test
	public void testShowImageCallsListenerWithCorrectImage()
	{
		ImageListPresenter.ImageShowListener listenerMock = Mockito.mock(ImageListPresenter.ImageShowListener.class);
		VirtualSlideImage                    imageMock    = Mockito.mock(VirtualSlideImage.class);
		
		Mockito.when(mVirtualSlideMock.getImageList()).thenReturn(Arrays.asList(null, null, imageMock));
		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(2);	
		
		mTestedPresenter.addImageShowListener(listenerMock);
		mTestedPresenter.showImage();
		
		Mockito.verify(listenerMock).onImageShowRequest(imageMock);
	}

	@Test
	public void testShowImageDoesNotCallListenersWhenThereIsNoSelection()
	{
		ImageListPresenter.ImageShowListener listenerMock = Mockito.mock(ImageListPresenter.ImageShowListener.class);

		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(-5);
		
		mTestedPresenter.addImageShowListener(listenerMock);
		mTestedPresenter.showImage();
		
		Mockito.verify(listenerMock, Mockito.never()).onImageShowRequest(Mockito.any());
	}
	
	@Test
	public void testRenameImageStartsNameEditing()
	{
		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(2);
		
		mTestedPresenter.renameImage();
		
		Mockito.verify(mViewMock).showNameEditor(2);
	}
	
	@Test
	public void testRenameImageDoesNotShowNameEditorWhenThereIsNoSelection()
	{
		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(-1);
		
		mTestedPresenter.renameImage();
		
		Mockito.verify(mViewMock, Mockito.never()).showNameEditor(Mockito.anyInt());
	}
	
	@Test
	public void testStopImageNameEditingClosesTheNameEditor()
	{
		Mockito.when(mViewMock.getEditedImageIndex()).thenReturn(1);
		Mockito.when(mViewMock.getNameEditorValue()).thenReturn("TestName");
		
		mTestedPresenter.stopImageNameEditing();
		
		Mockito.verify(mViewMock).closeNameEditor();
	}
	
	@Test
	public void testStopImageNameAddsRenameActionToActionSystem()
	{
		Mockito.when(mViewMock.getEditedImageIndex()).thenReturn(1);
		Mockito.when(mViewMock.getNameEditorValue()).thenReturn("TestName");
		
		mTestedPresenter.stopImageNameEditing();
		
		Mockito.verify(mActionSystemMock).execute(Mockito.isA(RenameImageAction.class));
	}
	
	@Test
	public void testStopImageNameEditingDisplaysErrorMessageWhenTheNameIsEmpty()
	{
		Mockito.when(mViewMock.getEditedImageIndex()).thenReturn(2);
		Mockito.when(mViewMock.getNameEditorValue()).thenReturn("  	 ");
		
		mTestedPresenter.stopImageNameEditing();
		
		Mockito.verify(mViewMock).displayErrorMessage(Mockito.any());
		Mockito.verify(mViewMock, Mockito.never()).closeNameEditor();
		Mockito.verifyZeroInteractions(mActionSystemMock);
	}
	
	@Test
	public void testStopImageNameEditingDoesNothingWhenEditingHasNotBeenStarted()
	{
		Mockito.when(mViewMock.getEditedImageIndex()).thenReturn(-1);
		
		mTestedPresenter.stopImageNameEditing();

		Mockito.verify(mViewMock, Mockito.never()).closeNameEditor();
		Mockito.verifyZeroInteractions(mActionSystemMock);
	}
	
	@Test
	public void testStopImageNameEditingClosesNameEditorAndDoesNothingElseWhenTheNameHasNotChanged()
	{
		VirtualSlideImage imageMock = Mockito.mock(VirtualSlideImage.class);
		Mockito.when(mVirtualSlideMock.getImageList()).thenReturn(Arrays.asList(null, imageMock, null));
		Mockito.when(imageMock.getName()).thenReturn("TestName");
		
		Mockito.when(mViewMock.getEditedImageIndex()).thenReturn(1);
		Mockito.when(mViewMock.getNameEditorValue()).thenReturn("TestName");
		
		mTestedPresenter.stopImageNameEditing();
		
		Mockito.verify(mViewMock).closeNameEditor();
		Mockito.verifyZeroInteractions(mActionSystemMock);
	}
	
	@Test
	public void testRemoveImageExecutesRemoveAction()
	{
		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(1);
		
		mTestedPresenter.removeImage();
		
		Mockito.verify(mActionSystemMock).execute(Mockito.isA(RemoveImageAction.class));
	}

	@Test
	public void testRemoveImageDoesNothingWhenThereIsNoSelection()
	{
		Mockito.when(mViewMock.getSelectedImageIndex()).thenReturn(-1);
		
		mTestedPresenter.removeImage();
		
		Mockito.verifyZeroInteractions(mActionSystemMock);
	}
}
