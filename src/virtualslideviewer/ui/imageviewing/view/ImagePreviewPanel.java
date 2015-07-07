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

package virtualslideviewer.ui.imageviewing.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import virtualslideviewer.ui.imageviewing.ImagePresentationModel;
import virtualslideviewer.ui.imageviewing.ImagePresentationModel.Listener;
import virtualslideviewer.util.ImageUtil;

/**
 * A panel for displaying the preview of the image.
 * 
 * Besides the downscaled image, this panel also displays a marker representing the region of currently visible image part.
 */
public class ImagePreviewPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final ImagePresentationModel mUIModel;
	private Image                        mCachedImage;
	
	/**
	 * @param presentationModel The model with panel's logic.
	 */
	public ImagePreviewPanel(ImagePresentationModel presentationModel)
	{
		if(presentationModel == null)
			throw new IllegalArgumentException("presentationModel cannot be null.");
		
		mUIModel = presentationModel;
		mUIModel.addListener(new Listener()
		{
			@Override
			public void onVisibleImageContentUpdate() { SwingUtilities.invokeLater(() -> repaint()); }
			
			@Override
			public void onImageChange()
			{
				regenerateImage();
				SwingUtilities.invokeLater(() -> repaint());
			}
		});		
		
		super.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				regenerateImage();
			}
		});
		
		super.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				onMouseClick(e.getPoint());
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics drawingSurface)
	{
		super.paintComponent(drawingSurface);
		
		if(mCachedImage != null)
		{
			drawImage(drawingSurface);
			drawVisibleRegionMarker(drawingSurface);
		}
	}
	
	private void drawImage(Graphics drawingSurface)
	{
		drawingSurface.drawImage(mCachedImage, getImageBounds().x, getImageBounds().y, null);
	}
	
	private void drawVisibleRegionMarker(Graphics drawingSurface)
	{
		Rectangle visibleRegionMarkerBounds = mUIModel.getVisibleRegionMarkerBounds(getImageBounds());
		
		drawingSurface.setColor(Color.GREEN);
		
		drawingSurface.drawRect(visibleRegionMarkerBounds.x,     visibleRegionMarkerBounds.y,
		                        visibleRegionMarkerBounds.width, visibleRegionMarkerBounds.height);
	}
	
	/**
	 * Gets image bounds as it is displayed.
	 * 
	 * The image is centered if it's smaller than the panel.
	 */
	private Rectangle getImageBounds()
	{
		Dimension imageSize = new Dimension(mCachedImage.getWidth(null), mCachedImage.getHeight(null));
		Point     imagePos  = ImageUtil.getCenteredPosition(imageSize, super.getSize());
		
		return new Rectangle(imagePos, imageSize);
	}
	
	private void onMouseClick(Point clickPointInPanelSpace)
	{
		Rectangle imageBounds = getImageBounds();
		
		if(imageBounds.contains(clickPointInPanelSpace))
		{
			Point clickPointInImageSpace = new Point(clickPointInPanelSpace.x - imageBounds.x, clickPointInPanelSpace.y - imageBounds.y);
			
			mUIModel.handleImagePreviewClick(clickPointInImageSpace, getImageBounds().getSize());
		}
	}
	
	/**
	 * Regenerates the cached image rendered as the preview.
	 * 
	 * Image resizing is done offline when panel's size change because scaling it with good quality on the fly is too costly.
	 */
	private void regenerateImage()
	{
		if(super.getSize().equals(new Dimension(0, 0)))
			return; // The panel's size is not set yet, so no point in generating image.
		
		BufferedImage originalImage = new BufferedImage(mUIModel.getThumbnailSize().width, mUIModel.getThumbnailSize().height,
		                                                mUIModel.isImageRGB() ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY);
	
		ImageUtil.loadDataIntoBufferedImage(originalImage, (dst) -> mUIModel.getThumbnailData(dst));

		mCachedImage = ImageUtil.scaleToFitPreservingAspectRatio(originalImage, super.getSize());
	}
}
