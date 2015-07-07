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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ByteArrayPool;
import virtualslideviewer.util.ParameterValidator;
import virtualslideviewer.util.ThreadPoolUtil;

/**
 * Class used to calculate a padding of virtual slide's image.
 * 
 * The main purpose of this class was to calculate the amount of padding in a .vsi file but since v.5.1.0 bioformats started to read real
 * image size it is not needed anymore. It is kept nonetheless in case reading of some different format still behaves like reading
 * .vsi file before version 5.1.0 of bioformats library.
 */
public class PaddingCalculator
{
	private final ByteArrayPool   mTileDataBufferPool = new ByteArrayPool();
	private final ExecutorService mThreadPool;
	
	public PaddingCalculator(ExecutorService threadPool)
	{
		ParameterValidator.throwIfNull(threadPool, "threadPool");
		
		mThreadPool = threadPool;
	}
	
	/**
	 * Computes the padding of an image at specified resolution index.
	 * 
	 * The function computes padding in border tiles, only if image size is aligned to tile size, which is true in the case of .vsi files.
	 * This function assumes that the padding has either pure white (255, 255, 255) or pure black (0, 0, 0) color.
	 * Only the padding for first image index at specified resolution index is computed as the padding should
	 * be the same regardless of channel, z plane and time point.
	 * 
	 * @param image    Image to compute padding for.
	 * @param resIndex Resolution index at which the padding should be computed.
	 * 
	 * @return The padding of image in pixels.
	 */
	public Dimension computePadding(VirtualSlideImage image, int resIndex) throws UncheckedInterruptedException
	{	
		ParameterValidator.throwIfNull(image, "image");
		
		int horizontalPadding = computeHorizontalPadding(image, resIndex);
		int verticalPadding   = computeVerticalPadding(image, resIndex);

		if(horizontalPadding == image.getTileSize(resIndex).width || verticalPadding == image.getTileSize(resIndex).height)
		{
			// There is no padding, it's just white or black image.
			return new Dimension(0, 0);
		}

		return new Dimension(horizontalPadding, verticalPadding);
	}
	
	private int computeHorizontalPadding(VirtualSlideImage image, int resIndex) throws UncheckedInterruptedException
	{
		Dimension tileSize    = image.getTileSize(resIndex);
		Dimension imageSize   = image.getImageSize(resIndex);
		int tileColumns       = imageSize.width  / tileSize.width;
		int tileRows          = imageSize.height / tileSize.height;
		
		if(imageSize.width % tileSize.width != 0)
			return 0;
		
		AtomicInteger maxPadding   = new AtomicInteger(tileSize.width);
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		
		for(int row = 0; row < tileRows; row++)
		{
			Tile tile = new Tile(tileColumns - 1, row, new ImageIndex(resIndex, 0, 0, 0));
			
			tasks.add(new TilePaddingComputationTask(image, tile, maxPadding, this::addTileToMaxHorizontalPaddingCalculation));
		}

		ThreadPoolUtil.scheduleAndWait(mThreadPool, tasks);
		
		return maxPadding.get();
	}
	
	private int addTileToMaxHorizontalPaddingCalculation(byte[] tileData, Dimension tileSize, int imageChannelCount, int maxPadding)
	{
		for(int y = 0; y < tileSize.height; y++)
		{
			for(int x = tileSize.width - 1; x >= tileSize.width - maxPadding; x--)
			{
				if(isPadding(tileData, x, y, tileSize.width, imageChannelCount))
				{
					maxPadding = tileSize.width - (x + 1);
					break;
				}
			}
			
			if(maxPadding == 0)
			{
				return 0;
			}
		}
		
		return maxPadding;
	}
	
	private int computeVerticalPadding(VirtualSlideImage image, int resIndex) throws UncheckedInterruptedException
	{
		Dimension tileSize    = image.getTileSize(resIndex);
		Dimension imageSize   = image.getImageSize(resIndex);
		int tileColumns       = imageSize.width  / tileSize.width;
		int tileRows          = imageSize.height / tileSize.height;
		
		if(imageSize.height % tileSize.height != 0)
			return 0;
		
		AtomicInteger maxPadding   = new AtomicInteger(tileSize.height);
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		
		for(int column = 0; column < tileColumns; column++)
		{
			Tile tile = new Tile(column, tileRows - 1, new ImageIndex(resIndex, 0, 0, 0));

			tasks.add(new TilePaddingComputationTask(image, tile, maxPadding, this::addTileToMaxVerticalPaddingCalculation));
		}

		ThreadPoolUtil.scheduleAndWait(mThreadPool, tasks);
		
		return maxPadding.get();
	}
		
	private int addTileToMaxVerticalPaddingCalculation(byte[] tileData, Dimension tileSize, int imageChannelCount, int maxPadding)
	{
		for(int x = 0; x < tileSize.width; x++)
		{
			for(int y = tileSize.height - 1; y >= tileSize.height - maxPadding; y--)
			{
				if(isPadding(tileData, x, y, tileSize.width, imageChannelCount))
				{
					maxPadding = tileSize.height - (y + 1);
					break;
				}
			}
			
			if(maxPadding == 0)
			{
				return 0;
			}
		}
		
		return maxPadding;
	}
	
	private boolean isPadding(byte[] data, int x, int y, int width, int imageChannelCount)
	{
		// The 0s remove some entirely black tiles which one of the test samples of .vsi virtual slides had in lower right corner of image.
	   // It occured only at 3 highest resolutions and had increasing sizes, respectively, 1x1, 2x1 and 3x1 tiles.
		
		int pixelIndex = (y * width + x) * imageChannelCount;
		for(int c = 0; c < imageChannelCount; c++)
		{
			if(data[pixelIndex + c] != (byte)255 &&
				data[pixelIndex + c] != (byte)0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	private interface PaddingCalculationStrategy
	{
		public int calculatePadding(byte[] imageData, Dimension imageSize, int imageChannelCount, int maxPadding);
	}
	
	private class TilePaddingComputationTask implements Callable<Void>
	{
		private final PaddingCalculationStrategy mCalculationStrategy;
		
		private final VirtualSlideImage mImage;
		private final AtomicInteger     mMaxPadding;
		private final Tile              mTile;
		private final Dimension         mTileSize;
		private final int               mImageChannelCount;
		
		public TilePaddingComputationTask(VirtualSlideImage image, Tile tile, AtomicInteger paddingReference, PaddingCalculationStrategy strategy)
		{
			ParameterValidator.throwIfNull(image, "image");
			ParameterValidator.throwIfNull(paddingReference, "paddingReference");
			ParameterValidator.throwIfNull(tile, "tile");
			ParameterValidator.throwIfNull(strategy, "strategy");
			
			mCalculationStrategy = strategy;
			
			mImage               = image;
			mMaxPadding          = paddingReference;
			mTile                = tile;
			mTileSize            = mTile.getBounds(mImage).getSize();
			mImageChannelCount   = (mImage.isRGB() ? 3 : 1);
		}

		@Override
		public Void call()
		{
			if(mMaxPadding.get() == 0)
				return null;
			
			byte[] tileData = mTileDataBufferPool.borrow(mTileSize.width * mTileSize.height * mImageChannelCount);
			{		
				mImage.getTileData(tileData, mTile);
				
				int currentPadding = mCalculationStrategy.calculatePadding(tileData, mTileSize, mImageChannelCount, mMaxPadding.get());
				
				mMaxPadding.accumulateAndGet(currentPadding, Math::min);
			}
			mTileDataBufferPool.putBack(tileData);
			
			return null;
		}
	}
}
