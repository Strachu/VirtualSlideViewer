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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ImageUtil;
import virtualslideviewer.util.ParameterValidator;

/**
 * A JTable cell renderer which renders a thumbnail of virtual slide's image along with its name.
 * 
 * The renderer automatically sets cell size to fit the entire thumbnail and its name.
 */
public class ThumbnailTableCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -6305574378144421759L;
	
	private static final int  THUMBNAIL_NAME_HEIGHT         = 25;
	private static final long MAX_THUMBNAIL_RESOLUTION_SIZE = 512 * 512 * 3;
	
	private final Dimension mMaxThumbnailSize;
	
	/**
	 * @param thumbnailSize Maximum size for single thumbnail.
	 *                      The images will be scaled to fit specified area while preserving its aspect ratio.
	 */
	public ThumbnailTableCellRenderer(Dimension thumbnailSize)
	{
		ParameterValidator.throwIfNull(thumbnailSize, "thumbnailSize");
		
		mMaxThumbnailSize = thumbnailSize;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		label.setOpaque(false);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		
		VirtualSlideImage imageToRender = (VirtualSlideImage)value;
		label.setIcon(new ImageIcon(getThumbnail(imageToRender)));
		label.setText(imageToRender.getName());
		
		setCellSizeToFitThumbnail(table, row, column, new Dimension(label.getIcon().getIconWidth(), label.getIcon().getIconHeight()));
		
		return label;
	}
	
	private Image getThumbnail(VirtualSlideImage image)
	{
		BufferedImage thumbnailImage = loadImageFromThumbnailResolution(image);
		
		return ImageUtil.scaleToFitPreservingAspectRatio(thumbnailImage, mMaxThumbnailSize);
	}
	
	private BufferedImage loadImageFromThumbnailResolution(VirtualSlideImage image)
	{
		int thumbnailResIndex = ImageUtil.getResolutionIndexWithSizeNotBiggerThan(image, MAX_THUMBNAIL_RESOLUTION_SIZE);
		
		Dimension imageSize   = image.getImageSize(thumbnailResIndex);
		
		BufferedImage lowestResolution = new BufferedImage(imageSize.width, imageSize.height,
		                                                   image.isRGB() ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY);
	
		ImageUtil.loadDataIntoBufferedImage(lowestResolution, (dst) -> 
		{
			image.getPixels(dst, new Rectangle(new Point(0, 0), imageSize), new ImageIndex(thumbnailResIndex));
		});
		
		return lowestResolution;
	}
	
	private void setCellSizeToFitThumbnail(JTable table, int rowIndex, int columnIndex, Dimension realThumbnailSize)
	{
		int preferredRowHeight = realThumbnailSize.height + THUMBNAIL_NAME_HEIGHT + table.getRowMargin();
		
		if(table.getRowHeight(rowIndex) != preferredRowHeight)
		{
			table.setRowHeight(rowIndex, preferredRowHeight);
		}
		
		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		
		if(column.getWidth() < realThumbnailSize.width)
		{
			column.setPreferredWidth(realThumbnailSize.width);
		}
	}
}
