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

import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.Tile;
import virtualslideviewer.imageviewing.VisibleAreaTilePrioritizer;
import virtualslideviewer.testutils.TestUtil;

public class VisibleAreaTilePrioritizerTest
{
	@Test
	public void testSortTilesByPriority()
	{
		BufferedVirtualSlideImage imageMock = TestUtil.createImageMockWithDefaultParameters();
		
		List<Tile> tileList = Arrays.asList(new Tile(0, 0, 0), new Tile(1, 0, 0),
		                                    new Tile(0, 1, 0), new Tile(1, 1, 0));

		Mockito.when(imageMock.getResolutionCount()).thenReturn(1);
		Mockito.when(imageMock.getTileSize(0)).thenReturn(new Dimension(10, 10));
		Mockito.when(imageMock.getImageSize(0)).thenReturn(new Dimension(20, 15));
		
		new VisibleAreaTilePrioritizer().sortTilesByPriority(tileList, imageMock, new Rectangle(7, 7, 10, 8));
		
		List<Tile> tilesInExpectedOrder = Arrays.asList(new Tile(1, 1, 0),
		                                                new Tile(1, 0, 0),
                                                      new Tile(0, 1, 0),
                                                      new Tile(0, 0, 0));
		
		assertThat(tileList, is(tilesInExpectedOrder));
	}
}
