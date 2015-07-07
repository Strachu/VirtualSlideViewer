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

import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.imageviewing.NeighbourPrefetchingStrategy;
import virtualslideviewer.testutils.TestUtil;

public class NeighbourPrefetchingStrategyTest
{
	private BufferedVirtualSlideImage mImageMock;

	@Before
	public void setUp() throws Exception
	{
		mImageMock = TestUtil.createImageMockWithDefaultParameters();
		
		Mockito.when(mImageMock.getResolutionCount()).thenReturn(3);
		Mockito.when(mImageMock.getImageSize(2)).thenReturn(new Dimension(100, 190));
		Mockito.when(mImageMock.getTileSize(2)).thenReturn(new Dimension(10, 20));
	}

	@Test
	public void testPrefetchingInNormalConditions()
	{		
		List<Tile> returnedTiles = new NeighbourPrefetchingStrategy(15).getTilesToPrefetch(mImageMock, new Rectangle(45, 50, 15, 30), new ImageIndex(2));
		
		List<Tile> expectedResult = Arrays.asList(
			new Tile(2, 1, 2), new Tile(3, 1, 2), new Tile(4, 1, 2), new Tile(5, 1, 2), new Tile(6, 1, 2), new Tile(7, 1, 2),
      	new Tile(2, 2, 2), new Tile(3, 2, 2),    /* Loaded */      /* Loaded */     new Tile(6, 2, 2), new Tile(7, 2, 2),
      	new Tile(2, 3, 2), new Tile(3, 3, 2),    /* Loaded */      /* Loaded */     new Tile(6, 3, 2), new Tile(7, 3, 2),
      	new Tile(2, 4, 2), new Tile(3, 4, 2), new Tile(4, 4, 2), new Tile(5, 4, 2), new Tile(6, 4, 2), new Tile(7, 4, 2));
		
		assertArrayEqualsInAnyOrder(expectedResult, returnedTiles);
	}
	
	@Test
	public void testPrefetchingWhenLoadedImageWasInCorner()
	{		
		List<Tile> returnedTiles = new NeighbourPrefetchingStrategy(10).getTilesToPrefetch(mImageMock, new Rectangle(80, 140, 20, 50), new ImageIndex(2));
		
		List<Tile> expectedResult = Arrays.asList(new Tile(7, 6, 2), new Tile(8, 6, 2), new Tile(9, 6, 2),
                                                new Tile(7, 7, 2),
                                                new Tile(7, 8, 2),
                                                new Tile(7, 9, 2));
		
		assertArrayEqualsInAnyOrder(expectedResult, returnedTiles);
	}
	
	private void assertArrayEqualsInAnyOrder(Collection<?> expected, Collection<?> actual)
	{
		assertTrue("Expected " + expected + " \n but was \n " + actual, expected.containsAll(actual) && actual.containsAll(expected));
	}
}
