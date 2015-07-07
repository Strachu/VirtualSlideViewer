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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class NoSlideLoadedMainPanelPlaceholder extends JPanel
{
	private static final long serialVersionUID = 1L;

	public NoSlideLoadedMainPanelPlaceholder()
	{
		setLayout(new BorderLayout(0, 0));
		
		JLabel label = new JLabel("<html><center>No slide loaded.</center><br>" +
		                          "Please load a virtual slide by selecting File->Open... menu entry.</html>");
		
		label.setHorizontalAlignment(SwingConstants.CENTER);
		super.add(label, BorderLayout.CENTER);
	}
}
