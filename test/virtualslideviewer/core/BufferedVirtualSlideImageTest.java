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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.TileCache;
import virtualslideviewer.core.VirtualSlideImage;

public class BufferedVirtualSlideImageTest
{
	private VirtualSlideImage         mImageMock;
	private TileCache                 mCacheMock;
	private BufferedVirtualSlideImage mTestedImage;
	
	@Before
	public void setUp() throws Exception
	{
		mImageMock = Mockito.mock(VirtualSlideImage.class);
		mCacheMock = Mockito.mock(TileCache.class);
		
		mTestedImage = new BufferedVirtualSlideImage(mImageMock, mCacheMock);
					
		Mockito.when(mCacheMock.getTileData(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(new byte[] { 1, 2, 3 });
	}

	@Test
	public void testGetTileDataAddsTileToCacheIfItIsNotThereAlreadyAndThenReturnsIt()
	{
		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(false);
		
		Tile tileToGet = new Tile(0, 0, 0);
		
		byte[] returnedBytes = new byte[3];
		mTestedImage.getTileData(returnedBytes, tileToGet);
		
		Mockito.verify(mImageMock).getTileData(Mockito.eq(tileToGet));
		Mockito.verify(mCacheMock).addTile(Mockito.eq(mTestedImage), Mockito.eq(tileToGet), Mockito.any());
		
		assertThat(returnedBytes, is(new byte[] { 1, 2, 3 }));
	}

	@Test
	public void testGetTileDataDoesNotLoadDataIfItIsAlreadyInCacheAndReturnsDataFromCache()
	{
		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(true);
		
		Tile tileToGet = new Tile(0, 0, 0);
		
		byte[] returnedBytes = new byte[3];
		mTestedImage.getTileData(returnedBytes, tileToGet);
		
		Mockito.verify(mCacheMock).getTileData(Mockito.eq(mTestedImage), Mockito.eq(tileToGet));
		Mockito.verify(mImageMock, Mockito.never()).getTileData(Mockito.any());
		
		assertThat(returnedBytes, is(new byte[] { 1, 2, 3 }));
	}
	
	@Test
	public void testGetTileDataPadsBiggerBufferWithZeros()
	{
		byte[] returnedBytes = new byte[5];
		mTestedImage.getTileData(returnedBytes, new Tile(0, 0, 0));
		
		assertThat(returnedBytes, is(new byte[] { 1, 2, 3, 0, 0 }));
	}

	@Test
	public void testEnsureTileDataCachedAddsTileToCacheIfItIsNotThereAlready()
	{
		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(false);
		
		Tile tile = new Tile(0, 0, 0);
		
		mTestedImage.ensureTileDataCached(tile);
		
		Mockito.verify(mImageMock).getTileData(Mockito.eq(tile));
		Mockito.verify(mCacheMock).addTile(Mockito.eq(mTestedImage), Mockito.eq(tile), Mockito.any());
	}

	@Test
	public void testEnsureTileDataCachedDoesNotLoadDataIfItIsAlreadyInCache()
	{
		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(true);
		
		Tile tile = new Tile(0, 0, 0);
		
		mTestedImage.ensureTileDataCached(tile);
		
		Mockito.verify(mImageMock, Mockito.never()).getTileData(Mockito.any());
	}
	
	@Test
	public void testIsImageInCacheReturnsTrueIfSingleTileIsInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(100, 150));
		
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(5, 6, 1))).thenReturn(true);
		
		assertTrue(mTestedImage.isImageInCache(new Rectangle(500, 900, 100, 150), new ImageIndex(1)));
	}
	
	@Test
	public void testIsImageInCacheReturnsFalseIfSingleTileIsNotInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(100, 150));

		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(false);
		
		assertFalse(mTestedImage.isImageInCache(new Rectangle(500, 900, 100, 150), new ImageIndex(1)));
	}
	
	@Test
	public void testIsImageInCacheReturnsFalseIfResolutionIndexesAreDifferent()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(100, 150));

		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(5, 6, 2))).thenReturn(true);
		
		assertFalse(mTestedImage.isImageInCache(new Rectangle(500, 900, 100, 150), new ImageIndex(1)));
	}
	
	@Test
	public void testIsImageInCacheReturnsTrueForImageComposedFromMultipleTilesIfAllOfItsTilesAreInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(100, 150));

		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(false);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(1, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(2, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(3, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(4, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(1, 4, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(2, 4, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(3, 4, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(4, 4, 1))).thenReturn(true);
		
		assertTrue(mTestedImage.isImageInCache(new Rectangle(170, 450, 220, 175), new ImageIndex(1)));
	}
	
	@Test
	public void testIsImageInCacheReturnsFalseForImageComposedFromMultipleTilesWhenSomeOfItTilesAreNotInCache()
	{
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(100, 150));

		Mockito.when(mCacheMock.hasTile(Mockito.eq(mTestedImage), Mockito.any())).thenReturn(false);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(2, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(3, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(4, 3, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(1, 4, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(3, 4, 1))).thenReturn(true);
		Mockito.when(mCacheMock.hasTile(mTestedImage, new Tile(4, 4, 1))).thenReturn(true);
		
		assertFalse(mTestedImage.isImageInCache(new Rectangle(170, 450, 220, 175), new ImageIndex(1)));
	}
}
