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

package virtualslideviewer.ui.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import virtualslideviewer.ui.progress.ProgressView;

public class BackgroundOperationUtil
{
	public static <T extends ProgressView> void startBackgroundOperation(Supplier<T> progressViewFactory, Consumer<T> operation)
	{
		T progressView = progressViewFactory.get();

		Thread operationThread = new Thread(() ->
		{
			try
			{
				operation.accept(progressView);
			}
			finally
			{
				SwingUtilities.invokeLater(() -> progressView.hide());
			}
		});

		progressView.addCancelListener(() ->
		{
			operationThread.interrupt();
		});
		
		operationThread.start();
	}
}
