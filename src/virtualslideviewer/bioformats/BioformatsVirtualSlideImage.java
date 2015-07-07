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

package virtualslideviewer.bioformats;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.*;
import loci.common.DataTools;
import loci.formats.*;

public class BioformatsVirtualSlideImage extends VirtualSlideImage
{
	private final ReaderPool      mReaderPool;
	private final ByteArrayPool   mCacheBuffersPool = new ByteArrayPool();
	
	private final int             mSeriesIndex;
	private       String          mName;
	
	private final int             mColorChannelCount;
	private final int             mBitsPerPixel;
	private final boolean         mLittleEndian;
	private final int             mPixelType;
	private final boolean         mInterleaved;
	private final int             mResolutionCount;
	private final List<Dimension> mTileSize  = new ArrayList<>();
	private final List<Dimension> mImageSize = new ArrayList<>();
	private final List<Dimension> mPadding   = new ArrayList<>();
	
	private final int             mChannelCount;
	private final int             mZPlanesCount;
	private final int             mTimePointsCount;
	
	/**
	 * @param metadata    Metadata of the virtual slide.
	 * @param seriesIndex The index of series.
	 * @param nameSuffix  The suffix to append to image name.
	 */
	public BioformatsVirtualSlideImage(String name, int seriesIndex, int resolutionCount, ReaderPool readerPool)
	{
		ParameterValidator.throwIfNull(name, "name");
		ParameterValidator.throwIfNull(readerPool, "readerPool");
		
		mReaderPool  = readerPool;
		
		mSeriesIndex = seriesIndex;
		mName        = name;
		
		IFormatReader reader = mReaderPool.borrow();
		{
			reader.setSeries(mSeriesIndex);
			
			mColorChannelCount = reader.getRGBChannelCount();
			mBitsPerPixel      = reader.getBitsPerPixel();
			mLittleEndian      = reader.isLittleEndian();
			mPixelType         = reader.getPixelType();
			mInterleaved       = reader.isInterleaved();
			mChannelCount      = reader.getEffectiveSizeC();
			mZPlanesCount      = reader.getSizeZ();
			mTimePointsCount   = reader.getSizeT();
			mResolutionCount   = resolutionCount;
		
			for(int i = 0; i < mResolutionCount; i++)
			{
				configureReader(reader, i);
				
				mTileSize.add(new Dimension(reader.getOptimalTileWidth(), reader.getOptimalTileHeight()));
				mImageSize.add(new Dimension(reader.getSizeX(), reader.getSizeY()));
				mPadding.add(new Dimension(0, 0));
			}
		}
		mReaderPool.putBack(reader);
	}
	
	@Override
	public void getTileData(byte[] dst, Tile tile)
	{
		validateGetTileDataArguments(dst, tile);
				
		Rectangle tileBounds = tile.getBounds(this);	
		
		// TODO refactor the ifs to a strategy object?
		
		if(mBitsPerPixel != 8)
		{
			byte[] rawDataTempBuffer = mCacheBuffersPool.borrow(getRawDataBufferMinimumSize(tile));
			{		
				readRawTileData(rawDataTempBuffer, tileBounds, tile.getImageIndex());
	
				if(isRGB() && !mInterleaved)
				{
					byte[] tempByteBufffer = mCacheBuffersPool.borrow(getOutputBufferMinimumSize(tile));
					{
						convertToBytes(tempByteBufffer, rawDataTempBuffer, tileBounds.getSize());
						PixelDataUtil.convertPlanarToInterleaved(tempByteBufffer, dst, tileBounds.getSize(), mColorChannelCount);
					}
					mCacheBuffersPool.putBack(tempByteBufffer);
				}
				else
				{
					convertToBytes(dst, rawDataTempBuffer, tileBounds.getSize());
				}
			}
			mCacheBuffersPool.putBack(rawDataTempBuffer);
		}
		else
		{
			if(isRGB() && !mInterleaved)
			{
				byte[] tempBuffer = mCacheBuffersPool.borrow(getOutputBufferMinimumSize(tile));
				{
					readRawTileData(tempBuffer, tileBounds, tile.getImageIndex());
	
					PixelDataUtil.convertPlanarToInterleaved(tempBuffer, dst, tileBounds.getSize(), mColorChannelCount);
				}
				mCacheBuffersPool.putBack(tempBuffer);
			}
			else
			{
				readRawTileData(dst, tileBounds, tile.getImageIndex());
			}
		}
	}
	
	private void validateGetTileDataArguments(byte[] dst, Tile tile)
	{
		ParameterValidator.throwIfNull(dst, "dst");
		ParameterValidator.throwIfNull(tile, "tile");
		
		if(!tile.isValid(this))
			throw new IllegalArgumentException(tile + " does not exist.");
		
		if(dst.length < getOutputBufferMinimumSize(tile))
		{
			throw new IllegalArgumentException("Passed buffer is not big enough to store entire tile's data. " + 
		                                      "It should have the capacity of at least " + getOutputBufferMinimumSize(tile) + " bytes.");
		}
	}
	
	private int getOutputBufferMinimumSize(Tile tile)
	{
		Dimension tileBounds = tile.getBounds(this).getSize();
		
		return tileBounds.width * tileBounds.height * mColorChannelCount;
	}
	
	private int getRawDataBufferMinimumSize(Tile tile)
	{
		// Output buffer always has 8-bit per color component, while raw data buffer can have more.
		
		return getOutputBufferMinimumSize(tile) * FormatTools.getBytesPerPixel(mPixelType);
	}
	
	private void readRawTileData(byte[] dst, Rectangle tileBounds, ImageIndex imageIndex)
	{
		IFormatReader reader = mReaderPool.borrow();
		try
		{
			while(true)
			{
				try
				{
					configureReader(reader, imageIndex.getResolutionIndex());
			
					int readerImageIndex = reader.getIndex(imageIndex.getZPlane(), imageIndex.getChannel(), imageIndex.getTimePoint());
		
					reader.openBytes(readerImageIndex, dst, tileBounds.x, tileBounds.y, tileBounds.width, tileBounds.height);
					break;
				}
				catch(ClosedByInterruptException e)
				{
					throw new UncheckedInterruptedException(e.getMessage());
				}
				catch(ClosedChannelException e)
				{
					// Bioformats reader is unusable after an exception has been thrown (such as is the case during canceling)...
					// Get a new reader and discard the old one by not returning it into the pool.
					reader.close();
					reader = mReaderPool.borrow();
				}
			}
		}
		catch(FormatException | IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			mReaderPool.putBack(reader);
		}
	}
	
	protected void configureReader(IFormatReader reader, int resIndex)
	{
		reader.setSeries(mSeriesIndex);
		reader.setResolution(mResolutionCount - 1 - resIndex);
	}
	
	/**
	 * Converts pixels from native type to 8-bits per color component.
	 * 
	 * Based on loci.formats.ImageTools.autoscale().
	 */
	private void convertToBytes(byte[] dst, byte[] src, Dimension tileSize)
	{
		long[] minmax = FormatTools.defaultMinMax(mPixelType);
		
		int min = (int)minmax[0];
		int max = (int)minmax[1];
		
		int bytesPerPixel = FormatTools.getBytesPerPixel(mPixelType);
		int srcSize       = tileSize.width * tileSize.height * mColorChannelCount * bytesPerPixel;

		for(int i = 0; i < srcSize; i += bytesPerPixel)
		{
			int s = DataTools.bytesToInt(src, i, bytesPerPixel, mLittleEndian);

			float diff = max - min;
			float dist = (s - min) / diff;

			dst[i / bytesPerPixel] = (byte)(dist * 255);
		}
	}
	
	@Override
	public Dimension getTileSize(int resIndex)
	{
		return new Dimension(mTileSize.get(resIndex));
	}

	@Override
	public Dimension getImageSize(int resIndex)
	{
		return new Dimension(mImageSize.get(resIndex).width  - mPadding.get(resIndex).width,
		                     mImageSize.get(resIndex).height - mPadding.get(resIndex).height);
	}
	
	@Override
	public int getResolutionCount()
	{
		return mResolutionCount;
	}
	
	@Override
	public int getChannelCount()
	{
		return mChannelCount;
	}

	@Override
	public int getZPlaneCount()
	{
		return mZPlanesCount;
	}

	@Override
	public int getTimePointCount()
	{
		return mTimePointsCount;
	}
	
	@Override
	public boolean isRGB()
	{
		return mColorChannelCount == 3;
	}
	
	@Override
	public String getName()
	{
		return mName;
	}
	
	@Override
	public void setName(String newName)
	{
		ParameterValidator.throwIfNull(newName, "newName");
		
		if(newName.trim().isEmpty())
			throw new IllegalArgumentException("Image name cannot be empty.");
		
		String oldName = mName;
		
		mName = newName;
		
		super.firePropertyChange("name", oldName, newName);
	}

	@Override
	public String getID()
	{
		return Integer.toString(mSeriesIndex);
	}
	
	public int getSeriesIndex()
	{
		return mSeriesIndex;
	}
	
	public void setPadding(int resIndex, Dimension padding)
	{
		ParameterValidator.throwIfNull(padding, "padding");
		
		if(resIndex < 0 || resIndex >= mResolutionCount)
			throw new IllegalArgumentException("Invalid resolution index.");
		
		mPadding.set(resIndex, new Dimension(padding));
	}

	@Override
	public void close()
	{
		mReaderPool.close();
	}
}
