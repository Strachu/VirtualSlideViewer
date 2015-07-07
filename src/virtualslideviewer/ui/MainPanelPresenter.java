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

import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.persistence.VirtualSlidePersistenceService;
import virtualslideviewer.ui.utils.Region;

public class MainPanelPresenter implements VirtualSlidePersistenceService.Listener
{
	private final MainPanel mPanel;
	private final Region    mMainPanelRegion;
	private boolean         mPanelAlreadySwitched = false;
	
	public MainPanelPresenter(MainPanel mainPanel, Region mainPanelRegion)
	{
		if(mainPanel == null)
			throw new IllegalArgumentException("mainPanel cannot be null.");
		
		if(mainPanelRegion == null)
			throw new IllegalArgumentException("mainPanelRegion cannot be null.");	
		
		mPanel           = mainPanel;
		mMainPanelRegion = mainPanelRegion;
	}
	
	@Override
	public void onVirtualSlideLoaded(VirtualSlide loadedSlide, Path loadedFilePath)
	{
		if(!mPanelAlreadySwitched)
		{
			mMainPanelRegion.changeView(mPanel);
			mPanelAlreadySwitched = true;
		}
	}
}
