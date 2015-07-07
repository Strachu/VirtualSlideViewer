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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import virtualslideviewer.ui.imageviewing.ImagePresentationModel;
import virtualslideviewer.ui.imageviewing.ImagePresentationModel.Listener;
import virtualslideviewer.util.ImageUtil;

/**
 * A JPanel which handles rendering of a virtual slide image.
 */
public class ImageRenderingPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final ImagePresentationModel mUIModel;
	private BufferedImage                mCachedImage = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
	
	/**
	 * @param presentationModel A presentation model with panel's logic.
	 */
	public ImageRenderingPanel(ImagePresentationModel presentationModel)
	{
		if(presentationModel == null)
			throw new IllegalArgumentException("presentationModel cannot be null.");
		
		mUIModel = presentationModel;
		mUIModel.addListener(new Listener()
		{
			@Override
			public void onVisibleImageContentUpdate() { SwingUtilities.invokeLater(() -> repaint()); }
			
			@Override
			public void onImageChange() { SwingUtilities.invokeLater(() -> repaint()); }
		});
		
		MouseHandler handler = new MouseHandler();
		
		super.addMouseListener(handler);
		super.addMouseMotionListener(handler);
		super.addMouseWheelListener(handler);
		super.addComponentListener(new ResizeHandler());
	}

	@Override
	protected void paintComponent(Graphics graphics)
	{
		super.paintComponent(graphics);

		if(!mUIModel.isImageLoaded())
			return;
		
		renderImage((Graphics2D)graphics, loadImage());
	}
	
	private void renderImage(Graphics2D graphics, Image image)
	{
		Dimension imageSize = mUIModel.getVisibleImageRegionSize();
		Point     imagePos  = ImageUtil.getCenteredPosition(imageSize, super.getSize());
		
	   graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	   graphics.drawImage(image, imagePos.x, imagePos.y, imageSize.width, imageSize.height, null);
	}
	
	private Image loadImage()
	{
		reallocateCachedImageIfNeeded(mUIModel.getImageDataSize(), mUIModel.isImageRGB());
		
		ImageUtil.loadDataIntoBufferedImage(mCachedImage, (dst) -> mUIModel.loadImageDataInto(dst));

		return mCachedImage;
	}
	
	/**
	 * Reallocates cached buffered image if is needed.
	 * 
	 * The image is cached in order to reduce the amount of unneeded allocations and gargabe collection runs.
	 */
	private void reallocateCachedImageIfNeeded(Dimension imageSize, boolean isRGB)
	{
		if(hasDifferentSize(mCachedImage, imageSize) || hasInvalidPixelType(mCachedImage, isRGB))
		{
			mCachedImage = new BufferedImage(imageSize.width, imageSize.height,
			                                 isRGB ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY);
		}
	}
	
	private boolean hasDifferentSize(BufferedImage image, Dimension size)
	{
		return image.getWidth() != size.width || image.getHeight() != size.height;
	}
	
	private boolean hasInvalidPixelType(BufferedImage image, boolean shouldBeRGB)
	{
		boolean isRGB = (image.getType() != BufferedImage.TYPE_BYTE_GRAY);
		
		return shouldBeRGB != isRGB;
	}
	
	private class ResizeHandler extends ComponentAdapter
	{
		@Override
		public void componentResized(ComponentEvent e)
		{
			mUIModel.setViewportSize(ImageRenderingPanel.super.getSize());
		}
	}
	
	private class MouseHandler extends MouseAdapter
	{
		private Point mLastMousePosition;
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			mLastMousePosition = e.getPoint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if(!SwingUtilities.isLeftMouseButton(e))
				return;
			
			mUIModel.pan(mLastMousePosition.x - e.getX(), mLastMousePosition.y - e.getY());
			
			mLastMousePosition = e.getPoint();
		}
 
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			mUIModel.zoomAt(-e.getWheelRotation(), e.getPoint());
		}
	}
}
