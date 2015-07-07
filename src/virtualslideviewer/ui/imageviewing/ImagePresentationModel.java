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
import java.util.ArrayList;
import java.util.List;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.imageviewing.VisibleImageLoader;
import virtualslideviewer.util.ImageUtil;
import virtualslideviewer.util.ParameterValidator;

/**
 * A presentation model containing state and behavior for an image rendering view.
 */
public class ImagePresentationModel
{
	public interface Listener
	{
		/**
		 * Called when content of visible part of an image has changed either due to camera movement or new data availability.
		 */
		public void onVisibleImageContentUpdate();
		
		/**
		 * Called when the image to render has been changed.
		 */
		public void onImageChange();
	}
	
	private static final long         MAX_THUMBNAIL_SIZE      = 1024 * 1024 * 3;
	private static final double       MAX_ZOOM                = 4.0;
	
	private BufferedVirtualSlideImage mImage;
	private final Camera              mCamera;
	private final VisibleImageLoader  mImageLoader;
	
	private double                    mZoomIncrement                 = 1.1;
	private double                    mResolutionTransitionThreshold = 0.5;
	
	private int                       mCurrentChannel;
	private int                       mCurrentZ;
	private int                       mCurrentTimePoint;
	
	private final List<Listener>      mListeners = new ArrayList<>();

	/**
	 * @param camera      Camera used to control the visible region of an image.
	 * @param imageLoader Loader used to load visible part of an image.
	 */
	public ImagePresentationModel(Camera camera, VisibleImageLoader imageLoader)
	{
		ParameterValidator.throwIfNull(camera, "camera");
		ParameterValidator.throwIfNull(imageLoader, "imageLoader");
		
		mCamera      = camera;
		mImageLoader = imageLoader;
		
		mCamera.addChangeListener(() -> 
		{
			if(mImage == null)
				return;
			
			mListeners.forEach(l -> l.onVisibleImageContentUpdate());
		});
	}
	
	/**
	 * Adds a listener.
	 */
	public void addListener(Listener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("newListener cannot be null.");
		
		mListeners.add(listener);
	}
	
	/**
	 * Sets an image which will be displayed in the panel.
	 */
	public void setImage(BufferedVirtualSlideImage image)
	{
		if(image == null)
			throw new IllegalArgumentException("image cannot be null.");
		
		mCamera.setImageSize(image.getImageSize(image.getResolutionCount() - 1));
		
		if(mImage != null)
			mCamera.zoomToFit();
		
		mImage = image;
		
		mCurrentChannel   = 0;
		mCurrentZ         = 0;
		mCurrentTimePoint = 0;
		
		mListeners.forEach(l -> l.onImageChange());
	}
	
	public boolean isImageLoaded()
	{
		return mImage != null;
	}
	
	/**
	 * @see Camera#pan(Point)
	 */
	public void pan(int x, int y)
	{
		mCamera.pan(new Point(x, y));
	}

	/**
	 * Zooms the image at specified point.
	 * 
	 * @param increment Zoom increment to use. Positive values zooms in, while negative zooms the view out.
	 * @param zoomAt    The point in <b>pixels</b> <b>relative</b> to the upper left corner of visible region
	 *                  at which the image will be zoomed. Has to be positive.
	 */
	public void zoomAt(int increment, Point zoomAtPoint)
	{
		double realIncrement = mZoomIncrement * increment;
		double newZoom       = (realIncrement >= 0.0f) ? (mCamera.getZoom() * realIncrement) : (mCamera.getZoom() / -realIncrement);
	
		newZoom = Math.min(Math.max(newZoom, getMinZoom()), getMaxZoom());
		
		mCamera.setZoom(newZoom, zoomAtPoint);
	}
	
	public double getMinZoom()
	{
		Dimension halfViewportSize = new Dimension(mCamera.getViewportSize().width / 2, mCamera.getViewportSize().height / 2);
		
		double zoomToFitHalfScreen = ImageUtil.getScaleToFit(mImage.getImageSize(mImage.getResolutionCount() - 1), halfViewportSize);
		
		return Math.min(zoomToFitHalfScreen, 1.0);
	}
	
	public double getMaxZoom()
	{
		return MAX_ZOOM;
	}

	/**
	 * @see Camera#setZoom(double)
	 */
	public void setZoom(double zoom)
	{
		mCamera.setZoom(zoom);
	}

	/**
	 * Sets the size of a viewport into which the visible part of image will be rendered.
	 */
	public void setViewportSize(Dimension size)
	{
		boolean firstTimeInitialization = mCamera.getViewportSize().equals(new Dimension(0, 0));
		
		mCamera.setViewportSize(size);
		
		if(firstTimeInitialization)
		{
			// zoomToFit() doesn't work in setImage() on first time because the viewport size is unknown yet.
			mCamera.zoomToFit();
		}
	}
	
	/**
	 * @see Camera#getZoom()
	 */
	public double getZoom()
	{
		return mCamera.getZoom();
	}

	/**
	 * Gets the size of visible part of the image which should be used during the rendering.
	 * 
	 * Note that this size can be smaller than the size of the viewport when the image at current zoom is smaller.
	 */
	public Dimension getVisibleImageRegionSize()
	{
		return mCamera.getAbsoluteVisibleRegionBounds().getSize();
	}

	/**
	 * Gets the size of image data returned by a call to {@link #loadImageDataInto(byte[])}.
	 * 
	 * Because the returned image is NOT scaled to a destination size the result from a call to this function will most of the time differ
	 * from the result of a {@link #getVisibleImageRegionSize()} call.
	 */
	public Dimension getImageDataSize()
	{
		Dimension imageSize = mImage.getImageSize(getResolutionToUseDuringLoading());
		
		return Camera.relativeToAbsoluteBounds(mCamera.getVisibleRegionBounds(), imageSize).getSize();
	}
	
	/**
	 * Loads the data of currently visible part of the image.
	 * 
	 * The image is <b>NOT</b> scaled to destination size due to performance reasons, because the only purpose of returned image
	 * is to be rendered, it is more efficient to do the scaling on the fly during rendering.
	 * 
	 * @param dst The buffer to load data into.
	 * 
	 * @see #getImageDataSize()
	 */
	public void loadImageDataInto(byte[] dst)
	{
		int       bestResIndex       = getResolutionToUseDuringLoading();
		Rectangle visibleImageBounds = Camera.relativeToAbsoluteBounds(mCamera.getVisibleRegionBounds(), mImage.getImageSize(bestResIndex));

		mImageLoader.getVisibleImageData(mImage, dst, visibleImageBounds, getCurrentImageIndexForResolution(bestResIndex),
		                                 () -> mListeners.forEach(l -> l.onVisibleImageContentUpdate()));
	}
	
	private int getResolutionToUseDuringLoading()
	{
		List<Dimension> imageResolutions = new ArrayList<>(mImage.getResolutionCount());
		for(int i = 0; i < mImage.getResolutionCount(); i++)
		{
			imageResolutions.add(mImage.getImageSize(i));
		}
		
		return mCamera.getBestResolutionForCurrentZoom(imageResolutions, mResolutionTransitionThreshold);
	}
	
	public int getCurrentChannel()
	{
		return mCurrentChannel;
	}
	
	public int getChannelCount()
	{
		return mImage.getChannelCount();
	}
	
	public void setCurrentChannel(int channel)
	{
		if(channel >= getChannelCount())
			throw new IllegalArgumentException("The index of channel has to be lower than the number of channels in the image.");
		
		mCurrentChannel = channel;
		
		mListeners.forEach(l -> l.onImageChange());
	}
	
	public int getCurrentZPlane()
	{
		return mCurrentZ;
	}
	
	public int getZPlanesCount()
	{
		return mImage.getZPlaneCount();
	}
	
	public void setCurrentZPlane(int z)
	{
		if(z >= getZPlanesCount())
			throw new IllegalArgumentException("The index of z plane has to be lower than the number of z planes in the image.");
		
		mCurrentZ = z;
		
		mListeners.forEach(l -> l.onImageChange());
	}
	
	public int getCurrentTimePoint()
	{
		return mCurrentTimePoint;
	}
	
	public int getTimePointCount()
	{
		return mImage.getTimePointCount();
	}
	
	public void setCurrentTimePoint(int timePoint)
	{
		if(timePoint >= getTimePointCount())
			throw new IllegalArgumentException("The index of time point has to be lower than the number of time points in the image.");
		
		mCurrentTimePoint = timePoint;
		
		mListeners.forEach(l -> l.onImageChange());
	}
	
	private ImageIndex getCurrentImageIndexForResolution(int resIndex)
	{
		return new ImageIndex(resIndex, mCurrentChannel, mCurrentZ, mCurrentTimePoint);
	}
	
	/**
	 * Gets the size of image's thumbnail.
	 */
	public Dimension getThumbnailSize()
	{
		return mImage.getImageSize(getThumbnailResIndex());
	}
	
	/**
	 * Loads the data of image's thumbnail.
	 * 
	 * @param dst The buffer to load data into.
	 * 
	 * @see #getThumbnailSize()
	 */
	public void getThumbnailData(byte[] dst)
	{
		mImage.getPixels(dst, new Rectangle(new Point(0, 0), getThumbnailSize()), getCurrentImageIndexForResolution(getThumbnailResIndex()));
	}
	
	private int getThumbnailResIndex()
	{
		return ImageUtil.getResolutionIndexWithSizeNotBiggerThan(mImage, MAX_THUMBNAIL_SIZE);
	}
	
	/**
	 * Checks whether the image is in RGB color space.
	 */
	public boolean isImageRGB()
	{
		return mImage.isRGB();
	}	
	
	/**
	 * Handle the mouse click on an preview image.
	 * 
	 * @param clickPointInImageSpace The position at which the click was made.
	 * @param previewImageSize       The size of preview image.
	 */
	public void handleImagePreviewClick(Point clickPointInImageSpace, Dimension previewImageSize)
	{
		mCamera.setPosition(absolutePositionToRelative(clickPointInImageSpace, previewImageSize));
	}
	
	/**
	 * Converts given position in absolute coordinates into relative ones (that is in the range 0.0 - 1.0).
	 * 
	 * @param absolutePos The absolute position to convert.
	 * @param areaSize    The maximum value of an absolute position.
	 * 
	 * @return The position in relative coordinates.
	 */
	private Point2D absolutePositionToRelative(Point absolutePos, Dimension areaSize)
	{
		double x = absolutePos.getX() / areaSize.getWidth();
		double y = absolutePos.getY() / areaSize.getHeight();
		
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Gets the bounds of visible region marker as visible on image preview.
	 * 
	 * @param imagePreviewBounds The bounds of image preview.
	 */
	public Rectangle getVisibleRegionMarkerBounds(Rectangle imagePreviewBounds)
	{
		int x      = (int)(mCamera.getVisibleRegionBounds().getX()      * imagePreviewBounds.getWidth());
		int y      = (int)(mCamera.getVisibleRegionBounds().getY()      * imagePreviewBounds.getHeight());
		int width  = (int)(mCamera.getVisibleRegionBounds().getWidth()  * imagePreviewBounds.getWidth());
		int height = (int)(mCamera.getVisibleRegionBounds().getHeight() * imagePreviewBounds.getHeight());
		
		return new Rectangle(x + imagePreviewBounds.x, y + imagePreviewBounds.y, width, height);
	}
	
	/**
	 * Gets the zoom increment.
	 * 
	 * @see #setZoomIncrement(double)
	 */
	public double getZoomIncrement()
	{
		return mZoomIncrement;
	}

	/**
	 * Sets a zoom increment to be used when a user wants to zoom the image.
	 * 
	 * @param zoomIncrement New zoom increment. Has to be larger than 1.0
	 */
	public void setZoomIncrement(double zoomIncrement)
	{
		if(zoomIncrement <= 1.0f)
			throw new IllegalArgumentException("Zoom increment has to be larger than 1.0");
		
		mZoomIncrement = zoomIncrement;
	}
	
	/**
	 * Gets the threshold of transition beetwen image resolutions.
	 * @see #setResolutionTransitionThreshold(double)
	 */
	public double getResolutionTransitionThreshold()
	{
		return mResolutionTransitionThreshold;
	}
	
	/**
	 * Sets the threshold of a transition between image resolutions.
	 * 
	 * This threshold is used to determine from which resolution level to get pixels at zoom level in between 2 resolutions.
	 * After the percentage of distance from lower to higher resolution level is higher than specified threshold, the function
	 * {@link #loadImageDataInto(byte[])} will use pixels from higher resolution.
	 * 
	 * The lower the threshold the sooner the controller will retrieve pixels from higher resolution of image resulting in better
	 * quality and less visible transition, but beware that fetching pixels from higher resolution means that a lot more pixels
	 * needs to be retrieved causing severe perfomance loss.
	 * 
	 * @param newValue New threshold, its value has to be in [0.0, 1.0] range.
	 */
	public void setResolutionTransitionThreshold(double newValue)
	{
		if(newValue < 0.0 || newValue > 1.0)
			throw new IllegalArgumentException("Invalid value. The valid values are in range [0.0, 1.0]");
		
		mResolutionTransitionThreshold = newValue;
	}
}
