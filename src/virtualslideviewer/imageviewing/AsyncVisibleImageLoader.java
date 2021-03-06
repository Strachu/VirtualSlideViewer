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

package virtualslideviewer.imageviewing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ByteArrayPool;
import virtualslideviewer.util.ImageUtil;
import virtualslideviewer.util.ParameterValidator;

/**
 * Asynchronous loader for extracting a visible part from virtual slide image.
 * It uses threading, caching and prefetching to provide the best performance and is highly configurable.
 */
public class AsyncVisibleImageLoader implements VisibleImageLoader
{
	private final ExecutorService                 mThreadPool;
	private final TileLoadingPrioritizer          mLoadingPrioritizer;
	private final PrefetchingStrategy             mPrefetchingStrategy;
	private final LoadingTilePlaceholderGenerator mPlaceholderGenerator;
	
	private final ByteArrayPool                   mTempTileDataBufferPool   = new ByteArrayPool();
	private final List<Future<?>>                 mPreviouslyLoadingFutures = new ArrayList<>();

	/**
	 * @param tileLoadingExecutor  A thread pool which the loader will use to schedule tile loading tasks.
	 * @param placeholderGenerator A data generator for tiles which has been not loaded yet.
	 * @param prefetchingStrategy  A strategy to prefetch not visible yet but possibly soon needed tiles.
	 * @param loadingPrioritizer   A prioritizer to select which parts of the image should be loaded first.
	 */
	public AsyncVisibleImageLoader(ExecutorService tileLoadingExecutor,     LoadingTilePlaceholderGenerator placeholderGenerator,
	                               PrefetchingStrategy prefetchingStrategy, TileLoadingPrioritizer          loadingPrioritizer)
	{
		ParameterValidator.throwIfNull(tileLoadingExecutor, "tileLoadingExecutor");
		ParameterValidator.throwIfNull(placeholderGenerator, "placeholderGenerator");
		ParameterValidator.throwIfNull(prefetchingStrategy, "prefetchingStrategy");
		ParameterValidator.throwIfNull(loadingPrioritizer, "loadingPrioritizer");
		
		mThreadPool           = tileLoadingExecutor;
		mPlaceholderGenerator = placeholderGenerator;
		mPrefetchingStrategy  = prefetchingStrategy;
		mLoadingPrioritizer   = loadingPrioritizer;
	}
	
	/**
	 * Asynchronously loads visible part of pixels from the image.
	 * 
	 * The pixels are loaded in their original form if they are in cache, otherwise they data is loaded in background
	 * and data generated by the tile placeholder generator will be returned in the place of real data until its in cache.
	 * 
	 * After the data is loaded, user specified callback will be called and real data can be retrieved by calling this function again.
	 * 
	 * Calling this function stops any not yet finished loading tasks started in previous call.
	 * 
	 * @param image               Image from which visible part will be loaded.
	 * @param dst                 Buffer which will receive already the data.
	 * @param visibleImageBounds  The bounds of currently visible image.
	 * @param imageIndex          The image index from which to retrieve data.
	 * @param dataUpdatedCallback A callback which will be called once for every part of an image when is it made available.
	 *                            The callback will be called from a <b>different thread</b>.
	 */
	@Override
	public void getVisibleImageData(BufferedVirtualSlideImage image, byte[] dst, Rectangle visibleImageBounds, ImageIndex imageIndex,
	                                Runnable dataUpdatedCallback)
	{
		validateArguments(image, dst, visibleImageBounds, imageIndex, dataUpdatedCallback);
		
		cancelPreviousLoading();
		
		loadDataInto(image, dst, visibleImageBounds, imageIndex, dataUpdatedCallback);
		
		prefetchTiles(image, visibleImageBounds, imageIndex);
	}
	
	private void validateArguments(BufferedVirtualSlideImage image, byte[] dst, Rectangle visibleImageBounds, ImageIndex imageIndex,
	                               Runnable dataUpdatedCallback)
	{
		ParameterValidator.throwIfNull(image, "image");
		ParameterValidator.throwIfNull(dst, "dst");
		ParameterValidator.throwIfNull(visibleImageBounds, "visibleImageBounds");
		ParameterValidator.throwIfNull(dataUpdatedCallback, "dataUpdatedCallback");

		if(!imageIndex.isValid(image))
			throw new IllegalArgumentException("Invalid image index.");
		
		if(isNotASubImage(image, visibleImageBounds, imageIndex.getResolutionIndex()))
			throw new IllegalArgumentException("Invalid visible image bounds.");
		
		int minimumBufferSize = visibleImageBounds.width * visibleImageBounds.height * (image.isRGB() ? 3 : 1);
		if(dst.length < minimumBufferSize)
		{
			throw new IllegalArgumentException("Destination buffer is too small. Capacity of at least " +
		                                      minimumBufferSize + " bytes is needed.");
		}
	}
	
	private boolean isNotASubImage(VirtualSlideImage image, Rectangle subImageBounds, int resIndex)
	{
		Rectangle fullImageBounds = new Rectangle(new Point(0, 0), image.getImageSize(resIndex));
		
		return !fullImageBounds.contains(subImageBounds);
	}
	
	private void cancelPreviousLoading()
	{
		for(Future<?> taskHandle : mPreviouslyLoadingFutures)
		{
			taskHandle.cancel(false);
		}
		
		mPreviouslyLoadingFutures.clear();
	}

	private void prefetchTiles(BufferedVirtualSlideImage image, Rectangle visibleImageBounds, ImageIndex imageIndex)
	{
		for(Tile tile : mPrefetchingStrategy.getTilesToPrefetch(image, visibleImageBounds, imageIndex))
		{
			if(!image.isImageInCache(tile.getBounds(image), imageIndex))
			{
				Future<?> taskHandle = mThreadPool.submit(() -> image.ensureTileDataCached(tile));
				
				mPreviouslyLoadingFutures.add(taskHandle);
			}
		}
	}

	private void loadDataInto(BufferedVirtualSlideImage image, byte[] dst, Rectangle visibleImageBounds, ImageIndex imageIndex,
	                          Runnable dataUpdatedCallback)
	{
		List<Tile> tilesToLoad = ImageUtil.getTilesInArea(visibleImageBounds, image.getTileSize(imageIndex.getResolutionIndex()), imageIndex);
		
		mLoadingPrioritizer.sortTilesByPriority(tilesToLoad, image, visibleImageBounds);
		
		byte[] tempTileBuffer = mTempTileDataBufferPool.borrow(getRequiredTileBufferSize(image, imageIndex.getResolutionIndex()));
		{
			for(Tile tile : tilesToLoad)
			{
				getTileData(image, tempTileBuffer, tile, dataUpdatedCallback);
	
				int imageChannelCount = (image.isRGB() ? 3 : 1);
	
				ImageUtil.copyIntersectingPartOfImage(tempTileBuffer, tile.getBounds(image),
				                                      dst, visibleImageBounds, imageChannelCount);
			}
		}
		mTempTileDataBufferPool.putBack(tempTileBuffer);
	}
	
	private int getRequiredTileBufferSize(VirtualSlideImage image, int resIndex)
	{
		Dimension fullTileSize = image.getTileSize(resIndex);
		
		return fullTileSize.width * fullTileSize.height * (image.isRGB() ? 3 : 1);
	}
	
	private void getTileData(BufferedVirtualSlideImage image, byte[] dst, Tile tile, Runnable dataUpdatedCallback)
	{
		if(image.isImageInCache(tile.getBounds(image), tile.getImageIndex()))
		{
			image.getTileData(dst, tile);
			return;
		}
		
		startTileLoading(image, tile, dataUpdatedCallback);
		
		mPlaceholderGenerator.getTilePlaceholder(dst, image, tile);
	}

	private void startTileLoading(BufferedVirtualSlideImage image, Tile tile, Runnable dataUpdatedCallback)
	{
		Future<?> taskHandle = mThreadPool.submit(() -> 
		                       {
		                          image.ensureTileDataCached(tile);
		                          
		                          dataUpdatedCallback.run();
		                       });
		
		mPreviouslyLoadingFutures.add(taskHandle);
	}
}
