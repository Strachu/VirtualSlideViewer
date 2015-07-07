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

package virtualslideviewer.ui.imageviewing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import virtualslideviewer.util.ImageUtil;

/**
 * A camera used to control the visible region for rendering purposes.
 * 
 * It maintains the state of a visible region, such as its position, size and current zoom.
 */
public class Camera
{
	/**
	 * Listener notified when visible region changes.
	 */
	public interface Listener
	{
		/**
		 * Called when visible region has changed.
		 */
		public void onVisibleRegionUpdate();
	}
	
	private Point2D.Double mPosition     = new Point2D.Double();
	private Dimension      mViewportSize = new Dimension(0, 0);
	private Dimension      mImageSize    = new Dimension(0, 0);
	private double         mZoom         = 1.0;
	
	private List<Listener> mListeners    = new ArrayList<>();
	
	/**
	 * Adds a listener which will be notified of any changes of visible region.
	 */
	public void addChangeListener(Listener newListener)
	{
		if(newListener == null)
			throw new IllegalArgumentException("newListener cannot be null.");
		
		mListeners.add(newListener);
	}
	
	/**
	 * Sets the size of an image which can be explored by this camera.
	 */
	public void setImageSize(Dimension imageSize)
	{
		if(imageSize == null)
			throw new IllegalArgumentException("regionSize cannot be null!");
		
		mImageSize.setSize(imageSize);
		
		mListeners.forEach(l -> l.onVisibleRegionUpdate());
	}

	/**
	 * Sets the camera position in relative coordinates.
	 * 
	 * Camera position represents the center of visible region.
	 * 
	 * @param position Camera position in relative coordinates, i.e. (0.0, 0.0) - top left corner of explorable region,
	 *                                                               (1.0, 1.0) - bottom right corner
	 */
	public void setPosition(Point2D position)
	{
		if(position == null)
			throw new IllegalArgumentException("position cannot be null!");
		
		mPosition.setLocation(position);
		
		mListeners.forEach(l -> l.onVisibleRegionUpdate());
	}

	public Dimension getViewportSize()
	{
		return new Dimension(mViewportSize);
	}
	
	/**
	 * Sets the size of visible region.
	 * 
	 * @param viewportSize The size of viewport in pixels.
	 */
	public void setViewportSize(Dimension viewportSize)
	{
		if(viewportSize == null)
			throw new IllegalArgumentException("regionSize cannot be null!");
		
		mViewportSize.setSize(viewportSize);
		
		mListeners.forEach(l -> l.onVisibleRegionUpdate());
	}
	
	/**
	 * Translates the camera by specified amount.
	 * 
	 * @param translation The translation in pixels.
	 */
	public void pan(Point translation)
	{
		if(translation == null)
			throw new IllegalArgumentException("translation cannot be null.");
		
		Rectangle2D correctedVisibleRegionBounds = getVisibleRegionBounds();
		Dimension   currentImageSize             = getImageSizeAtZoom(mZoom);
		
		mPosition.x = correctedVisibleRegionBounds.getCenterX() + translation.getX() / currentImageSize.getWidth();
		mPosition.y = correctedVisibleRegionBounds.getCenterY() + translation.getY() / currentImageSize.getHeight();
		
		mListeners.forEach(l -> l.onVisibleRegionUpdate());
	}

	private Dimension getImageSizeAtZoom(double zoom)
	{
		return new Dimension((int)(mImageSize.getWidth() * zoom), (int)(mImageSize.getHeight() * zoom));
	}
	
	/**
	 * Sets new zoom level.
	 * 
	 * @param zoom The percentage of a zoom. 1.0 means native image size.
	 */
	public void setZoom(double zoom)
	{
		if(zoom <= 0.0)
			throw new IllegalArgumentException("Zoom has to be larger than 0%.");
		
		mZoom = zoom;
		
		mListeners.forEach(l -> l.onVisibleRegionUpdate());
	}

	/**
	 * Zooms the camera at specified point.
	 * 
	 * @param zoomIncrement Zoom increment to use. Positive values zooms in, while negative the view out.
	 * @param zoomAt        The point in <b>pixels</b> <b>relative</b> to the upper left corner of visible region
	 *                      at which the image will be zoomed. Has to be positive.
	 */
	public void setZoom(double zoom, Point zoomAt)
	{	
		if(zoomAt == null)
			throw new IllegalArgumentException("zoomAt cannot be null.");
		
		if(zoomAt.x < 0 || zoomAt.y < 0)
			throw new IllegalArgumentException("Point to zoom at cannot be negative.");			
		
		Rectangle2D oldRegionBounds = getVisibleRegionBounds();
		double      oldZoomLevel    = mZoom;
	
		setZoom(zoom);
		
		mPosition.setLocation(computeNewPosition(oldZoomLevel, oldRegionBounds, zoomAt));
	}
	
	/**
	 * Zooms the camera to fit the entire image.
	 */
	public void zoomToFit()
	{
		double zoomPercentToFit = ImageUtil.getScaleToFit(mImageSize, mViewportSize);
		
		setZoom(Math.min(zoomPercentToFit, 1.0));	
	}
	
	/**
	 * Computes new camera position after zooming.
	 * 
	 * The new position is computed in such a way that the pixel pointed at by <b>zoomAt</b> parameter
	 * is in the same place relative to upper left corner of visible region as it was before zooming.
	 */
	private Point2D computeNewPosition(double previousZoomLevel, Rectangle2D previousRegionBounds, Point zoomAt)
	{
		double zoomAtInRelativeCoordX     = zoomAt.getX() / getImageSizeAtZoom(previousZoomLevel).getWidth();
		double zoomAtInRelativeCoordY     = zoomAt.getY() / getImageSizeAtZoom(previousZoomLevel).getHeight();
		
		double zoomAtInImageSpaceX        = previousRegionBounds.getX() + zoomAtInRelativeCoordX;
		double zoomAtInImageSpaceY        = previousRegionBounds.getY() + zoomAtInRelativeCoordY;
		
		double originalCenterToPointDiffX = previousRegionBounds.getCenterX() - zoomAtInImageSpaceX;
		double originalCenterToPointDiffY = previousRegionBounds.getCenterY() - zoomAtInImageSpaceY;
		
		double scale = mZoom / previousZoomLevel;
		
		double newX = zoomAtInImageSpaceX + originalCenterToPointDiffX / scale;
		double newY = zoomAtInImageSpaceY + originalCenterToPointDiffY / scale;
		
		return new Point2D.Double(newX, newY);
	}
	
	/**
	 * Returns the bounds of currently visible region in relative coordinates.
	 * 
	 * In the case of a position, (0.0, 0.0) means upper left corner of image and (1.0, 1.0) is a lower right corner.
	 * In the case of a size, it is percentages of image size at current zoom level, i.e. 1.0 means that entire image is visible.
	 * 
	 * The returned value is corrected before returning to ensure that region outside of image is not visible.
	 */
	public Rectangle2D getVisibleRegionBounds()
	{
		double relativeWidth  = Math.min(mViewportSize.getWidth()  / getImageSizeAtZoom(mZoom).getWidth(),  1.0);
		double relativeHeight = Math.min(mViewportSize.getHeight() / getImageSizeAtZoom(mZoom).getHeight(), 1.0);
		
		double newCenterX     = Math.min(Math.max(mPosition.getX(), relativeWidth  * 0.5), 1.0 - relativeWidth  * 0.5);
		double newCenterY     = Math.min(Math.max(mPosition.getY(), relativeHeight * 0.5), 1.0 - relativeHeight * 0.5);
		
		double relativeX      = newCenterX - relativeWidth  * 0.5;
		double relativeY      = newCenterY - relativeHeight * 0.5;
		
		return new Rectangle2D.Double(relativeX, relativeY, relativeWidth, relativeHeight);	
	}
	
	/**
	 * Gets an <b>absolute</b> bounds of visible region of image in pixels at current zoom.
	 */
	public Rectangle getAbsoluteVisibleRegionBounds()
	{
		return relativeToAbsoluteBounds(getVisibleRegionBounds(), getImageSizeAtZoom(mZoom));
	}
	
	/**
	 * Returns current zoom level.
	 */
	public double getZoom()
	{
		return mZoom;
	}
	
	/**
	 * Converts bounds in relative coordinates to an absolute ones.
	 * 
	 * Relative coordinates are in 0.0 - 1.0 range, while absolute coordinates are in range 0 - (maxsize - 1).
	 * 
	 * @param relativeBounds Relative bounds to convert to absolute bounds.
	 * @param areaSize       The maximum value of an absolute position.
	 * 
	 * @return Bounds in absolute coordinates.
	 */
	public static Rectangle relativeToAbsoluteBounds(Rectangle2D relativeBounds, Dimension areaSize)
	{
		if(relativeBounds == null)
			throw new IllegalArgumentException("relativeBounds cannot be null.");
		
		if(areaSize == null)
			throw new IllegalArgumentException("areaSize cannot be null.");
		
		int absoluteX      = (int)Math.round(relativeBounds.getX()    * areaSize.getWidth());
		int absoluteY      = (int)Math.round(relativeBounds.getY()    * areaSize.getHeight());
		int absoluteWidth  = (int)Math.max(relativeBounds.getWidth()  * areaSize.getWidth(),  1);
		int absoluteHeight = (int)Math.max(relativeBounds.getHeight() * areaSize.getHeight(), 1);
		
		return new Rectangle(absoluteX, absoluteY, absoluteWidth, absoluteHeight);
	}
		
	/**
	 * Returns the best resolution level to use at current zoom.
	 */
	public int getBestResolutionForCurrentZoom(List<Dimension> resolutions, double transitionThreshold)
	{
		if(resolutions == null)
			throw new IllegalArgumentException("resolutions cannot be null.");
		
		int widthAtCurrentZoom = getImageSizeAtZoom(mZoom).width;
		
		if(widthAtCurrentZoom <= resolutions.get(0).width)
			return 0;
		
		for(int i = 1; i < resolutions.size(); i++)
		{
			if(widthAtCurrentZoom < resolutions.get(i).width)
			{
				int lowerResolutionLevel  = i - 1;
				int higherResolutionLevel = i;
				
				int min = resolutions.get(lowerResolutionLevel).width;
				int max = resolutions.get(higherResolutionLevel).width;
				
				double transition = (widthAtCurrentZoom - min) / (double)(max - min);
				return (transition < transitionThreshold) ? lowerResolutionLevel : higherResolutionLevel;
			}
		}
		
		return resolutions.size() - 1;
	}
}

