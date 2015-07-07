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

package virtualslideviewer.ui.progress;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import virtualslideviewer.util.ParameterValidator;

public class ProgressDialogFactory implements ProgressViewFactory
{
	private final JFrame mMainWindow;
	
	public ProgressDialogFactory(JFrame mainWindow)
	{
		ParameterValidator.throwIfNull(mainWindow, "mainWindow");
		
		mMainWindow = mainWindow;
	}
	
	@Override
	public ProgressView showProgressForNextLoading(String fileName)
	{
		ParameterValidator.throwIfNull(fileName, "fileName");
		
		ProgressDialog dialog = new ProgressDialog(mMainWindow);
				
		dialog.setTitle("Loading...");
		dialog.setDescription(String.format("Loading %s. Please wait...", fileName));

		SwingUtilities.invokeLater(() ->
		{
			dialog.setVisible(true);
		});
		
		return dialog;
	}

	@Override
	public SaveProgressView showProgressForNextSaving(String fileName)
	{
		if(fileName == null)
			throw new IllegalArgumentException("fileName cannot be null.");
		
		SaveProgressDialog dialog = new SaveProgressDialog(mMainWindow, fileName);
		
		SwingUtilities.invokeLater(() ->
		{
			dialog.setVisible(true);
		});
		
		return dialog;
	}
}
