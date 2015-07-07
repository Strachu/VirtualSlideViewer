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

package virtualslideviewer.ui.config;

import javax.swing.JFrame;

import virtualslideviewer.config.ApplicationConfiguration;

public class ConfigDialogFactory implements ConfigView
{
	private final JFrame                   mMainWindow;
	private final ApplicationConfiguration mConfiguration;

	public ConfigDialogFactory(JFrame mainWindow, ApplicationConfiguration configuration)
	{
		if(mainWindow == null)
			throw new IllegalArgumentException("mainWindow cannot be null.");
		
		if(configuration == null)
			throw new IllegalArgumentException("configuration cannot be null.");
		
		mMainWindow    = mainWindow;
		mConfiguration = configuration;
	}
	
	@Override
	public void show()
	{
		ConfigDialog configDialog = new ConfigDialog(mMainWindow, mConfiguration);
		configDialog.setVisible(true);
	}

}
