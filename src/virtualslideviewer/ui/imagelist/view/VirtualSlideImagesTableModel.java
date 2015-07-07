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

package virtualslideviewer.ui.imagelist.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.table.AbstractTableModel;

import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideChangeListener;
import virtualslideviewer.core.VirtualSlideImage;

/**
 * An adapter to use a virtual slide's images as a data source for JTable.
 */
public class VirtualSlideImagesTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1832310162129272001L;
	
	private final VirtualSlide mVirtualSlide;
	
	/**
	 * @param virtualSlide A virtual slide which will be used as a source of images.
	 */
	public VirtualSlideImagesTableModel(VirtualSlide virtualSlide)
	{
		if(virtualSlide == null)
			throw new IllegalArgumentException("virtualSlide cannot be null.");
		
		mVirtualSlide = virtualSlide;
		
		addListenersToVirtualSlide();
	}
	
	private void addListenersToVirtualSlide()
	{
		mVirtualSlide.addChangeListener(new VirtualSlideListener());
		
		for(VirtualSlideImage image : mVirtualSlide.getImageList())
		{
			image.addPropertyChangeListener(new ImagePropertyListener());
		}
	}
	
	@Override
	public int getColumnCount()
	{
		return 1;
	}
	
	@Override
	public int getRowCount()
	{
		return mVirtualSlide.getImageList().size();
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		return mVirtualSlide.getImageList().get(rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return VirtualSlideImage.class;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return null;
	}

	private class VirtualSlideListener implements VirtualSlideChangeListener
	{
		@Override
		public void onImageAdd(VirtualSlide source, VirtualSlideImage newImage)
		{
			newImage.addPropertyChangeListener(new ImagePropertyListener());

			int imageIndex = mVirtualSlide.getImageList().indexOf(newImage);
			
			fireTableRowsInserted(imageIndex, imageIndex);
		}

		@Override
		public void onImageRemove(VirtualSlide source, VirtualSlideImage removedImage)
		{
			fireTableDataChanged();
		}
	}
	
	private class ImagePropertyListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent e)
		{
			int imageIndex = mVirtualSlide.getImageList().indexOf(e.getSource());
			
			fireTableRowsUpdated(imageIndex, imageIndex);
		}
	}
}