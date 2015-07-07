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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SwingRegion extends JPanel implements Region
{
	private static final long serialVersionUID = 1L;

	public SwingRegion()
	{
		super.setLayout(new BorderLayout());
	}
	
	@Override
	public void changeView(Object view)
	{
		if(!(view instanceof Component))
			throw new IllegalArgumentException("Specified view is not in Swing technology.");
		
		SwingUtilities.invokeLater(() ->
		{
			super.removeAll();
			super.add((Component)view, BorderLayout.CENTER);
			
			super.revalidate();
		});
	}
}
