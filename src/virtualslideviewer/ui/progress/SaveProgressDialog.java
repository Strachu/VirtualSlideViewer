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

import java.awt.Component;

public class SaveProgressDialog extends ProgressDialog implements SaveProgressView
{
	private static final long serialVersionUID = 1L;
	
	private long mTotalBytesToSave = 0;

	public SaveProgressDialog(Component parent, String fileName)
	{
		super(parent);
		
		super.setTitle("Saving...");
		super.setDescription(String.format("Saving %s. Please wait...", fileName));
	}

	@Override
	public void reportTotalBytes(long totalBytes)
	{
		mTotalBytesToSave = totalBytes;
	}

	@Override
	public void reportWrittenBytes(long writtenBytes)
	{
		double progress = (double)writtenBytes / mTotalBytesToSave;
	
		// TODO show info about copied bytes count? such as: "Copied x GB / xx GB"
		
		super.setProgress(progress);
		
		super.setTitle(String.format("%.0f%% Saving...", progress * 100.0));
	}

}
