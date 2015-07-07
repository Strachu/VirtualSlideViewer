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

import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.testutils.TestUtil;

public class TileTest
{
	private VirtualSlideImage mImageMock;
	
	@Before
	public void setUp() throws Exception
	{
		mImageMock = TestUtil.createImageMockWithDefaultParameters();
		
		Mockito.when(mImageMock.getResolutionCount()).thenReturn(3);
		Mockito.when(mImageMock.getImageSize(2)).thenReturn(new Dimension(105, 230));
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(52, 115));
		Mockito.when(mImageMock.getTileSize(Mockito.anyInt())).thenReturn(new Dimension(10, 20));
	}

	@Test
	public void testGetBoundsForOrdinaryTile()
	{
		Tile testedTile = new Tile(2, 3, 2);
		
		assertThat(testedTile.getBounds(mImageMock), is(new Rectangle(20, 60, 10, 20)));
	}

	@Test
	public void testGetBoundsForTileAtTheEdge()
	{
		Tile testedTile = new Tile(10, 11, 2);
		
		assertThat(testedTile.getBounds(mImageMock), is(new Rectangle(100, 220, 5, 10)));
	}
	
	@Test
	public void testIsValidReturnsTrueForValidTile()
	{
		Tile testedTile = new Tile(5, 3, 1);
		
		assertThat(testedTile.isValid(mImageMock), is(true));
	}
	
	@Test
	public void testIsValidReturnsFalseForTileWithInvalidColumn()
	{
		Tile testedTile = new Tile(7, 4, 1);
		
		assertThat(testedTile.isValid(mImageMock), is(false));
	}
	
	@Test
	public void testIsValidReturnsFalseForTileWithInvalidRow()
	{
		Tile testedTile = new Tile(7, 15, 2);
		
		assertThat(testedTile.isValid(mImageMock), is(false));
	}
	
	@Test
	public void testIsValidReturnsFalseForTileWithInvalidResIndex()
	{
		Tile testedTile = new Tile(2, 2, 3);
		
		assertThat(testedTile.isValid(mImageMock), is(false));
	}
}
