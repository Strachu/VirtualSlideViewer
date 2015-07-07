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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

/**
 * A JTable cell editor which enables the editing of a name of thumbnail.
 * 
 * The editing can be started only programmatically by calling JTable.editCellAt(row, column).
 * 
 * Editor requires that the cell renderer renders thumbnails using a JLabel.
 */
public class ThumbnailNameEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = -7462649416578289285L;
	
	private final JPanel     mPanel      = new JPanel();
	private final JLabel     mImageLabel = new JLabel();
	private final JTextField mEditField  = new JTextField();

	public ThumbnailNameEditor()
	{
		mEditField.addActionListener((x) -> stopCellEditing());
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		JLabel labelWithThumbnail = (JLabel)table.getCellRenderer(row, column).getTableCellRendererComponent(table, value, isSelected,
		                                                                                                     false, row, column);

		mImageLabel.setIcon(labelWithThumbnail.getIcon());
		mImageLabel.setHorizontalAlignment(labelWithThumbnail.getHorizontalAlignment());
		mImageLabel.setHorizontalTextPosition(labelWithThumbnail.getHorizontalTextPosition());
		mImageLabel.setVerticalTextPosition(labelWithThumbnail.getVerticalTextPosition());

		mEditField.setText(labelWithThumbnail.getText());
		mEditField.setHorizontalAlignment(JTextField.CENTER);

		mPanel.setLayout(new BorderLayout());
		mPanel.add(mImageLabel, BorderLayout.CENTER);
		mPanel.add(mEditField,  BorderLayout.SOUTH);

     	SwingUtilities.invokeLater(() -> mEditField.requestFocusInWindow());

		return mPanel;
	}

	@Override
	public boolean isCellEditable(EventObject e)
	{
		return isEditingStartedProgrammatically(e);
	}

	/**
	 * Checks whether the editing has been started by a call to JTable.editCellAt(row, column).
	 */
	private boolean isEditingStartedProgrammatically(EventObject e)
	{
		return e == null;
	}
	
	@Override
	public Object getCellEditorValue()
	{
		return mEditField.getText();
	}
	
	@Override
	public boolean shouldSelectCell(EventObject anEvent)
	{
		return true;
	}
}