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

package virtualslideviewer.core;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import virtualslideviewer.util.ByteArrayPool;
import virtualslideviewer.util.ImageUtil;
import virtualslideviewer.util.ParameterValidator;

/**
 * Single image from a virtual slide.
 */
public abstract class VirtualSlideImage implements AutoCloseable
{
	private final ByteArrayPool         mTileBufferPool    = new ByteArrayPool();
	private final PropertyChangeSupport mPropertyListeners = new PropertyChangeSupport(this);

	/**
	 * Returns image size at specified resolution level.
	 * The resolutions are in increasing order, that is, the higher the resolution index, the bigger the image is.
	 * 
	 * @param resIndex Index of resolution level.
	 */
	public abstract Dimension getImageSize(int resIndex);
	
	/**
	 * Returns number of resolution levels this image has.
	 */
	public abstract int getResolutionCount();
	
	public abstract int getChannelCount();
	public abstract int getZPlaneCount();
	public abstract int getTimePointCount();
	
	/**
	 * Returns tile size at specified resolution level.
	 * 
	 * Beware that the tiles lying at the edges of image will be clipped to image size.
	 * To get accurate tile size use Tile.getBounds().
	 * 
	 * @param resIndex Index of resolution level.
	 */
	public abstract Dimension getTileSize(int resIndex);
	
	/**
	 * Informs whether the image's pixels are in RGB format.
	 */
	public abstract boolean isRGB();
	
	/**
	 * Reads pixels of specified tile.
	 * 
	 * In this function overload, the buffer for pixels is allocated automatically.
	 * If the pixels are in RGB format, the layout of pixels in returned data is always RGBRGB...
	 * 
	 * @param tile The tile whose pixels should be loaded.
	 * 
	 * @return Buffer containing tile's pixels.
	 */
	public byte[] getTileData(Tile tile)
	{
		Dimension tileSize = tile.getBounds(this).getSize();
		
		byte[] tileData = new byte[tileSize.width * tileSize.height * (isRGB() ? 3 : 1)];
		
		getTileData(tileData, tile);
		return tileData;
	}
	
	/**
	 * Reads pixels of specified tile.
	 * 
	 * If the pixels are in RGB format, the layout of pixels in returned data is always RGBRGB...
	 * 
	 * This method HAS to be Thread Safe.
	 * 
	 * @param dst  Preallocated buffer where the pixels will be stored.
	 * @param tile The tile whose pixels should be loaded.
	 */
	public abstract void getTileData(byte[] dst, Tile tile);
	
	/**
	 * Reads pixels from specified region.
	 * 
	 * In this function overload, the buffer for pixels is allocated automatically.
	 * If the pixels are in RGB format, the layout of pixels in returned data is always RGBRGB...
	 * 
	 * @param bounds     The bounds of region from which the pixels will be read.
	 * @param imageIndex The image index.
	 * 
	 * @return Buffer containing read pixels.
	 */
	public byte[] getPixels(Rectangle bounds, ImageIndex imageIndex)
	{
		if(bounds == null)
			throw new IllegalArgumentException("bounds cannot be null.");
		
		byte[] tileData = new byte[bounds.width * bounds.height * (isRGB() ? 3 : 1)];	
		
		getPixels(tileData, bounds, imageIndex);
		return tileData;
	}

	/**
	 * Reads pixels from specified region.
	 * 
	 * If the pixels are in RGB format, the layout of pixels in returned data is always RGBRGB...
	 * 
	 * @param dst      Preallocated buffer where the pixels will be stored.
	 * @param bounds   The bounds of region from which the pixels will be read.
	 * @param resIndex The resolution index.
	 */
	public void getPixels(byte[] dst, Rectangle bounds, ImageIndex imageIndex)
	{
		if(dst == null)
			throw new IllegalArgumentException("dst cannot be null.");

		if(bounds == null)
			throw new IllegalArgumentException("bounds cannot be null.");

		if(imageIndex == null)
			throw new IllegalArgumentException("imageIndex cannot be null.");
		
		if(!imageIndex.isValid(this))
			throw new IllegalArgumentException("Invalid resolution index.");
		
		final int channelsCount = (isRGB() ? 3 : 1);
		
		Dimension tileSize = getTileSize(imageIndex.getResolutionIndex());
	
		byte[] tileData = mTileBufferPool.borrow(tileSize.width * tileSize.height * channelsCount);
		{
			for(Tile tile : ImageUtil.getTilesInArea(bounds, tileSize, imageIndex))
			{
				getTileData(tileData, tile);
				
				ImageUtil.copyIntersectingPartOfImage(tileData, tile.getBounds(this), dst, bounds, channelsCount);
			}
		}
		mTileBufferPool.putBack(tileData);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		ParameterValidator.throwIfNull(listener, "listener");
		
		mPropertyListeners.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener)
	{
		ParameterValidator.throwIfNull(listener, "listener");
		
		mPropertyListeners.addPropertyChangeListener(property, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		ParameterValidator.throwIfNull(listener, "listener");
		
		mPropertyListeners.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String property, PropertyChangeListener listener)
	{
		ParameterValidator.throwIfNull(listener, "listener");
		
		mPropertyListeners.removePropertyChangeListener(property, listener);
	}
	
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		mPropertyListeners.firePropertyChange(propertyName, oldValue, newValue);
	}
		
	/**
	 * Returns the name of image.
	 */
	public abstract String getName();

	/**
	 * Sets the name of image.
	 */
	public abstract void setName(String newName);
	
	/**
	 * Returns unique ID representing the image.
	 * 
	 * This ID is used to identify the image during caching of image's data, so the following should apply:
	 * - when two different instances of image returns identical data they should have identical ID,
	 * - when pixels data returned by two instances of image are different, their IDs <b>must</b> be different,
	 * - the ID does not have to be unique across different virtual slides, as the cache is cleared when a virtual slide is changed.
	 */
	public abstract String getID();
	
	@Override
	public abstract void close();
	
	@Override
	public String toString()
	{
		return getName();
	}
}
