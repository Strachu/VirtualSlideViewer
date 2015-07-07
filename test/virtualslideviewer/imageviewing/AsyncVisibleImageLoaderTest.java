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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.imageviewing.AsyncVisibleImageLoader;
import virtualslideviewer.imageviewing.LoadingTilePlaceholderGenerator;
import virtualslideviewer.imageviewing.PrefetchingStrategy;
import virtualslideviewer.imageviewing.TileLoadingPrioritizer;
import virtualslideviewer.imageviewing.VisibleImageLoader;
import virtualslideviewer.testutils.TestUtil;

public class AsyncVisibleImageLoaderTest
{
	private ExecutorService                 mExecutorMock;
	private TileLoadingPrioritizer          mLoadingPrioritizerMock;
	private PrefetchingStrategy             mPrefetchingStrategyMock;
	private LoadingTilePlaceholderGenerator mPlaceholderGeneratorMock;
	private BufferedVirtualSlideImage       mImageMock;
	
	private VisibleImageLoader              mTestedLoader;

	@Before
	public void setUp() throws Exception
	{
		mExecutorMock             = Mockito.mock(ExecutorService.class);
		mLoadingPrioritizerMock   = Mockito.mock(TileLoadingPrioritizer.class);
		mPrefetchingStrategyMock  = Mockito.mock(PrefetchingStrategy.class);
		mPlaceholderGeneratorMock = Mockito.mock(LoadingTilePlaceholderGenerator.class);
		mImageMock                = TestUtil.createImageMockWithDefaultParameters();
		
		// Execute submitted tasks synchronously
		Mockito.when(mExecutorMock.submit(Mockito.any())).then((x) -> 
		{
			Runnable task = (Runnable)x.getArguments()[0];
			task.run();
			return null;
		});
		
		mTestedLoader = new AsyncVisibleImageLoader(mExecutorMock, mPlaceholderGeneratorMock, mPrefetchingStrategyMock, mLoadingPrioritizerMock);
	}
		
	@Test(expected = IllegalArgumentException.class)
	public void testLoaderThrowsOnTooSmallBuffer()
	{
		mTestedLoader.getVisibleImageData(mImageMock, new byte[2000], new Rectangle(0, 0, 1000, 1500), new ImageIndex(1), () -> {});
	}
	
	@Test
	public void testLoaderCancelsAllTasksFromPreviousCallToGetSubImageData()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));

		Future<?>[] mockedFutures = { Mockito.mock(Future.class), Mockito.mock(Future.class), Mockito.mock(Future.class) } ;
		
		Mockito.doReturn(mockedFutures[0])
		       .doReturn(mockedFutures[1])
		       .doReturn(mockedFutures[2]).when(mExecutorMock).submit(Mockito.any());
	
		Mockito.when(mPrefetchingStrategyMock.getTilesToPrefetch(Mockito.any(), Mockito.any(), Mockito.any()))
		       .thenReturn(Arrays.asList(new Tile(1, 2, new ImageIndex(1))));
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1100], new Rectangle(20, 120, 10, 60), new ImageIndex(1), () -> {});
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1200], new Rectangle(20, 120, 20, 60), new ImageIndex(1), () -> {});
		
		Mockito.verify(mockedFutures[0]).cancel(Mockito.anyBoolean());
		Mockito.verify(mockedFutures[1]).cancel(Mockito.anyBoolean());
		Mockito.verify(mockedFutures[2]).cancel(Mockito.anyBoolean());
	}
	
	@Test
	public void testLoaderPrefetchesAllTilesReturnedByPrefetchingStrategy()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Rectangle  regionToLoad = new Rectangle(10, 15, 25, 30);
		List<Tile> tilesWhichShouldBePrefetched = Arrays.asList(new Tile(10, 10, new ImageIndex(1)),
		                                                        new Tile(12, 15, new ImageIndex(1)),
		                                                        new Tile(8,   5, new ImageIndex(1)));
		
		Mockito.when(mPrefetchingStrategyMock.getTilesToPrefetch(mImageMock, regionToLoad, new ImageIndex(1)))
		       .thenReturn(tilesWhichShouldBePrefetched);
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], regionToLoad, new ImageIndex(1), () -> {});
		
		Mockito.verify(mPrefetchingStrategyMock).getTilesToPrefetch(mImageMock, regionToLoad, new ImageIndex(1));
		Mockito.verify(mImageMock).ensureTileDataCached(tilesWhichShouldBePrefetched.get(0));
		Mockito.verify(mImageMock).ensureTileDataCached(tilesWhichShouldBePrefetched.get(1));
		Mockito.verify(mImageMock).ensureTileDataCached(tilesWhichShouldBePrefetched.get(2));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testLoaderLoadsTilesInOrderSpecifiedByTilePrioritizer()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Rectangle  regionToLoad      = new Rectangle(52, 250, 16, 30);
		List<Tile> expectedTileOrder = Arrays.asList(new Tile(6, 9, new ImageIndex(1)),
		                                             new Tile(5, 8, new ImageIndex(1)),
		                                             new Tile(6, 8, new ImageIndex(1)),
		                                             new Tile(5, 9, new ImageIndex(1)));
		
		Mockito.doAnswer((x) -> 
		{
			List<Tile> result = (List<Tile>)x.getArguments()[0];
			
			result.clear();
			result.addAll(expectedTileOrder);
			return null;
		}).when(mLoadingPrioritizerMock).sortTilesByPriority(Mockito.any(), Mockito.eq(mImageMock), Mockito.eq(regionToLoad));
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], regionToLoad, new ImageIndex(1), () -> {});
		
		InOrder inOrder = Mockito.inOrder(mPlaceholderGeneratorMock);
		inOrder.verify(mPlaceholderGeneratorMock).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock), Mockito.eq(expectedTileOrder.get(0)));
		inOrder.verify(mPlaceholderGeneratorMock).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock), Mockito.eq(expectedTileOrder.get(1)));
		inOrder.verify(mPlaceholderGeneratorMock).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock), Mockito.eq(expectedTileOrder.get(2)));
		inOrder.verify(mPlaceholderGeneratorMock).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock), Mockito.eq(expectedTileOrder.get(3)));
	}
	
	@Test
	public void testLoaderDoesNotCallPlaceholderGeneratorNorThreadPoolWhenAllTilesAreInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(50,  90, 10, 30), new ImageIndex(1))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(60,  90, 10, 30), new ImageIndex(1))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(50, 120, 10, 30), new ImageIndex(1))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(60, 120, 10, 30), new ImageIndex(1))).thenReturn(true);
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], new Rectangle(55, 110, 13, 20), new ImageIndex(1), () -> {});
		
		Mockito.verifyZeroInteractions(mPlaceholderGeneratorMock, mExecutorMock);
	}
	
	@Test
	public void testLoaderSubmitsTilesForLoadingWhenItIsNotInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(80, 150, 10, 30), new ImageIndex(1))).thenReturn(true);
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], new Rectangle(85, 160, 10, 40), new ImageIndex(1), () -> {});
		
		Mockito.verify(mExecutorMock, Mockito.times(3)).submit(Mockito.any());
		Mockito.verify(mImageMock).ensureTileDataCached(new Tile(8, 6, new ImageIndex(1)));
		Mockito.verify(mImageMock).ensureTileDataCached(new Tile(9, 5, new ImageIndex(1)));
		Mockito.verify(mImageMock).ensureTileDataCached(new Tile(9, 6, new ImageIndex(1)));
	}
	
	@Test
	public void testLoaderCallsCallbackAfterLoadingOnlyAndForAllLoadedTiles()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(80, 150, 10, 30), new ImageIndex(1))).thenReturn(true);
		
		Mockito.when(mPrefetchingStrategyMock.getTilesToPrefetch(Mockito.any(), Mockito.any(), Mockito.any()))
		       .thenReturn(Arrays.asList(new Tile(1, 1, new ImageIndex(1)), new Tile(1, 2, new ImageIndex(1))));

		Runnable callbackMock = Mockito.mock(Runnable.class);

		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], new Rectangle(75, 160, 12, 30), new ImageIndex(1), callbackMock);
		
		Mockito.verify(callbackMock, Mockito.times(3)).run();
	}
	
	@Test
	public void testLoaderReadsDataFromCacheWhenIsItAvailableAndPlaceholderGeneratorIfItIsNot()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(10, 30));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(80, 150, 10, 30), new ImageIndex(1))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(90, 180, 10, 30), new ImageIndex(1))).thenReturn(true);
		
		mTestedLoader.getVisibleImageData(mImageMock, new byte[1000], new Rectangle(85, 170, 10, 30), new ImageIndex(1), () -> {});
		
		Mockito.verify(mPlaceholderGeneratorMock, Mockito.never()).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock),
		                                                                              Mockito.eq(new Tile(8, 5, new ImageIndex(1))));
		Mockito.verify(mPlaceholderGeneratorMock).                 getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock),
		                                                                              Mockito.eq(new Tile(8, 6, new ImageIndex(1))));
		Mockito.verify(mPlaceholderGeneratorMock).                 getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock),
		                                                                              Mockito.eq(new Tile(9, 5, new ImageIndex(1))));
		Mockito.verify(mPlaceholderGeneratorMock, Mockito.never()).getTilePlaceholder(Mockito.any(), Mockito.eq(mImageMock),
		                                                                              Mockito.eq(new Tile(9, 6, new ImageIndex(1))));
		
		Mockito.verify(mImageMock).                 getTileData(Mockito.any(), Mockito.eq(new Tile(8, 5, new ImageIndex(1))));
		Mockito.verify(mImageMock, Mockito.never()).getTileData(Mockito.any(), Mockito.eq(new Tile(8, 6, new ImageIndex(1))));
		Mockito.verify(mImageMock, Mockito.never()).getTileData(Mockito.any(), Mockito.eq(new Tile(9, 5, new ImageIndex(1))));
		Mockito.verify(mImageMock).                 getTileData(Mockito.any(), Mockito.eq(new Tile(9, 6, new ImageIndex(1))));
	}
	
	@Test
	public void testLoaderReturnsCorrectDataWithGrayImages()
	{
		Mockito.when(mImageMock.getTileSize(0)).thenReturn(new Dimension(2, 3));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(14,  9, 2, 3), new ImageIndex(0))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(16, 12, 2, 3), new ImageIndex(0))).thenReturn(true);
		
		setImageTileDataToItsCoordsPlus100(new Tile(7, 3, new ImageIndex(0)), 2 * 3);
		setImageTileDataToItsCoordsPlus100(new Tile(8, 4, new ImageIndex(0)), 2 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(6, 3, new ImageIndex(0)), 2 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(8, 3, new ImageIndex(0)), 2 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(6, 4, new ImageIndex(0)), 2 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(7, 4, new ImageIndex(0)), 2 * 3);
		
		byte[] expectedResult = {
                                 63, (byte)173, (byte)173,        83,
                                 63, (byte)173, (byte)173,        83,
                                 63, (byte)173, (byte)173,        83,
                                 64,        74,        74, (byte)184,
                                 64,        74,        74, (byte)184
		                        };
		
		byte[] result = new byte[4 * 5];
		mTestedLoader.getVisibleImageData(mImageMock, result, new Rectangle(13, 9, 4, 5), new ImageIndex(0), () -> {});
		
		assertThat(result, is(expectedResult));
	}
	
	@Test
	public void testLoaderReturnsCorrectDataWithRGBImages()
	{
		Mockito.when(mImageMock.isRGB()).thenReturn(true);
		Mockito.when(mImageMock.getTileSize(0)).thenReturn(new Dimension(2, 3));
		
		Mockito.when(mImageMock.isImageInCache(new Rectangle(2, 6, 2, 3), new ImageIndex(0))).thenReturn(true);
		Mockito.when(mImageMock.isImageInCache(new Rectangle(2, 9, 2, 3), new ImageIndex(0))).thenReturn(true);
		
		setImageTileDataToItsCoordsPlus100(new Tile(1, 2, new ImageIndex(0)), 2 * 3 * 3);
		setImageTileDataToItsCoordsPlus100(new Tile(1, 3, new ImageIndex(0)), 2 * 3 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(2, 2, new ImageIndex(0)), 2 * 3 * 3);
		setPlaceholderTileDataToItsCoords( new Tile(2, 3, new ImageIndex(0)), 2 * 3 * 3);
		
		byte[] expectedResult = {
		                           112, 112, 112, 112, 112, 112, 22, 22, 22,
		                           112, 112, 112, 112, 112, 112, 22, 22, 22,
		                           112, 112, 112, 112, 112, 112, 22, 22, 22,
                                 113, 113, 113, 113, 113, 113, 23, 23, 23
		                        };
		
		byte[] result = new byte[3 * 4 * 3];
		mTestedLoader.getVisibleImageData(mImageMock, result, new Rectangle(2, 6, 3, 4), new ImageIndex(0), () -> {});
		
		assertThat(result, is(expectedResult));
	}
	
	/**
	 * Sets data returned from mImageMock.getTileData() to tile's coordinates + 100, i. e. 
	 * tile with coordinates (4, 2) will have all its bytes set to 142, tile (1, 5) to 115 and so on.
	 */
	private void setImageTileDataToItsCoordsPlus100(Tile tile, int tileDataSize)
	{
		byte[] tileData = new byte[tileDataSize];
		
		for(int i = 0; i < tileDataSize; i++)
		{
			tileData[i] = (byte)(100 + tile.getColumn() * 10 + tile.getRow());
		}

		TestUtil.copyToParameter(tileData).when(mImageMock).getTileData(Mockito.any(), Mockito.eq(tile));
	}
	
	/**
	 * Sets data returned from mPlaceholderGeneratorMock.getTilePlaceholder() to tile's coordinates, i. e. 
	 * tile with coordinates (4, 2) will have all its bytes set to 42, tile (1, 5) to 15 and so on.
	 */
	private void setPlaceholderTileDataToItsCoords(Tile tile, int tileDataSize)
	{
		byte[] tileData = new byte[tileDataSize];
		
		for(int i = 0; i < tileDataSize; i++)
		{
			tileData[i] = (byte)(tile.getColumn() * 10 + tile.getRow());
		}

		TestUtil.copyToParameter(tileData).when(mPlaceholderGeneratorMock).getTilePlaceholder(Mockito.any(), Mockito.any(), Mockito.eq(tile));
	}
}
