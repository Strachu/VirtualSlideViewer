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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.ui.imagelist.ImageListView;

/**
 * A Swing panel which displays the thumbnails of virtual slide's images along with their names.
 */
public class ImageListSwingPanel extends JScrollPane implements ImageListView
{
	private static final long serialVersionUID = -4643457695900323340L;

	private static final int RENAME_IMAGE_KEY = KeyEvent.VK_F2;
	private static final int SHOW_IMAGE_KEY   = KeyEvent.VK_ENTER;
	private static final int REMOVE_IMAGE_KEY = KeyEvent.VK_DELETE;
	
	protected JTable         mThumbnailTable  = new JTable();
	protected JPopupMenu     mContextMenu     = new JPopupMenu();
	
	protected List<Listener> mListeners       = new ArrayList<>();
	
	/**
	 * @param maxThumbnailSize The size of area to which the thumbnail of a single image will be fit.
	 */
	public ImageListSwingPanel(Dimension maxThumbnailSize)
	{
		mThumbnailTable.setOpaque(false);
		mThumbnailTable.setShowGrid(false);
		mThumbnailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		mThumbnailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mThumbnailTable.setRowMargin(20);
		
		// Some of JTable's embedded key bindings conflict with our needs.
		mThumbnailTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(RENAME_IMAGE_KEY, 0), "none");
		mThumbnailTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(SHOW_IMAGE_KEY,   0), "none");
		mThumbnailTable.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(REMOVE_IMAGE_KEY, 0), "none");
		
		mThumbnailTable.addKeyListener(new ThumbnailTableKeyHandler());
		mThumbnailTable.addMouseListener(new ThumbnailTableMouseListener());
		
		mThumbnailTable.setDefaultRenderer(VirtualSlideImage.class, new ThumbnailTableCellRenderer(maxThumbnailSize));
		mThumbnailTable.setDefaultEditor(  VirtualSlideImage.class, new ThumbnailNameEditor()
		{
			private static final long serialVersionUID = -2457231613728535089L;

			@Override
			public boolean stopCellEditing()
			{
				mListeners.forEach(l -> l.onImageNameConfirmed());
				return false;
			}
		});
		
		layoutComponents();
		
		initializeContextMenu();
		
		super.getVerticalScrollBar().setUnitIncrement(maxThumbnailSize.height);
	}

	private void layoutComponents()
	{
		JPanel mainPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints thumbnailTableConstraints = new GridBagConstraints();
		thumbnailTableConstraints.gridx   = 0;
		thumbnailTableConstraints.gridy   = 0;
		thumbnailTableConstraints.weighty = 0.0;
		mainPanel.add(mThumbnailTable, thumbnailTableConstraints);
	
		GridBagConstraints bottomGlueConstraints = new GridBagConstraints();
		bottomGlueConstraints.gridx   = 0;
		bottomGlueConstraints.gridy   = 1;
		bottomGlueConstraints.weighty = 1.0;
		mainPanel.add(Box.createGlue(), bottomGlueConstraints);
		
		super.setViewportView(mainPanel);
	}
	
	private void initializeContextMenu()
	{
		JMenuItem showImageMenu   = new JMenuItem("Show");
		JMenuItem renameImageMenu = new JMenuItem("Rename");
		JMenuItem removeImageMenu = new JMenuItem("Remove");
		
		// Mainly in order to show the shortcuts to a user.
		showImageMenu.  setAccelerator(KeyStroke.getKeyStroke(SHOW_IMAGE_KEY,   0));
		renameImageMenu.setAccelerator(KeyStroke.getKeyStroke(RENAME_IMAGE_KEY, 0));
		removeImageMenu.setAccelerator(KeyStroke.getKeyStroke(REMOVE_IMAGE_KEY, 0));
		
		showImageMenu.  addActionListener((x) -> mListeners.forEach(l -> l.onImageShow()));
		renameImageMenu.addActionListener((x) -> mListeners.forEach(l -> l.onImageRename()));
		removeImageMenu.addActionListener((x) -> mListeners.forEach(l -> l.onImageRemove()));
		
		mContextMenu.add(showImageMenu);
		mContextMenu.add(renameImageMenu);
		mContextMenu.add(removeImageMenu);
	}	
	
	@Override
	public void addListener(Listener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		
		mListeners.add(listener);
	}
	
	@Override
	public void setSourceVirtualSlide(VirtualSlide virtualSlide)
	{
		if(virtualSlide == null)
			throw new IllegalArgumentException("virtualSlide cannot be null.");
		
		mThumbnailTable.setModel(new VirtualSlideImagesTableModel(virtualSlide));
	}

	@Override
	public int getSelectedImageIndex()
	{
		return mThumbnailTable.getSelectedRow();
	}
	
	@Override
	public void setSelectedImageIndex(int index)
	{
		selectRow(index);
	}

	@Override
	public void showNameEditor(int imageToRenameIndex)
	{
		mThumbnailTable.editCellAt(imageToRenameIndex, 0);
	}

	@Override
	public int getEditedImageIndex()
	{
		return mThumbnailTable.getEditingRow();
	}
	
	@Override
	public String getNameEditorValue()
	{
		return (String)mThumbnailTable.getCellEditor().getCellEditorValue();
	}

	@Override
	public void closeNameEditor()
	{
		mThumbnailTable.editingStopped(null);
	}

	@Override
	public void displayErrorMessage(String error)
	{
		JOptionPane.showMessageDialog(this, error);
	}
	
	/**
	 * Returns a JTable used internally to display thumbnails of the images.
	 */
	public JTable getThumbnailTable()
	{
		return mThumbnailTable;
	}
	
	/**
	 * Returns a context menu which is displayed to a user after right clicking on some image entry.
	 */
	public JPopupMenu getContextMenu()
	{
		return mContextMenu;
	}
	
	private void selectRow(int rowIndex)
	{
		mThumbnailTable.requestFocusInWindow();
		mThumbnailTable.changeSelection(rowIndex, 0, false, false);		
	}
	
	private class ThumbnailTableMouseListener extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			mListeners.forEach(l -> l.onImageNameConfirmed());
			
			if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
			{
				mListeners.forEach(l -> l.onImageShow());
			}
			
			if(e.isPopupTrigger())
			{
				mContextMenu.show(mThumbnailTable, e.getX(), e.getY());
			}
			
			// Right clicking should select images too
			selectRow(mThumbnailTable.rowAtPoint(e.getPoint()));
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if(e.isPopupTrigger())
			{
				mContextMenu.show(mThumbnailTable, e.getX(), e.getY());
			}
		}
	}

	private class ThumbnailTableKeyHandler extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyCode())
			{
				case SHOW_IMAGE_KEY:
					mListeners.forEach(l -> l.onImageShow());
					break;
				
				case RENAME_IMAGE_KEY:
					mListeners.forEach(l -> l.onImageRename());
					break;
				
				case REMOVE_IMAGE_KEY:
					mListeners.forEach(l -> l.onImageRemove());
					break;
			}
		}
	}
}
