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

package virtualslideviewer.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.util.ImageUtil;

public class ImageUtilTest
{
	@Test
	public void testGetTilesInAreaWithAreaSmallerThanOneTile()
	{
		List<Tile> returnedTileList = ImageUtil.getTilesInArea(new Rectangle(100, 150, 30, 50), new Dimension(100, 70), new ImageIndex(0, 0, 0, 0));
		
		assertThat(returnedTileList, is(Arrays.asList(new Tile(1, 2, 0))));
 	}

	@Test
	public void testGetTilesInAreaWithAreaSpanningMultipleTiles()
	{
		List<Tile> returnedTileList = ImageUtil.getTilesInArea(new Rectangle(20, 300, 250, 100), new Dimension(100, 50), new ImageIndex(1, 0, 0, 0));
		
		List<Tile> expectedTileList = Arrays.asList(new Tile(0, 6, 1), new Tile(1, 6, 1), new Tile(2, 6, 1),
		                                            new Tile(0, 7, 1), new Tile(1, 7, 1), new Tile(2, 7, 1));
		
		assertThat(returnedTileList, is(expectedTileList));
 	}

	@Test
	public void testCopyIntersectingPartOfImageWithIdenticalBoundsForSourceAndDestinationJustCopyArray()
	{
		byte[] src = {
		               0, 1,
				         2, 3
				       };

		byte[] dst = new byte[4];
		
		ImageUtil.copyIntersectingPartOfImage(src, new Rectangle(10, 20, 2, 2), dst, new Rectangle(10, 20, 2, 2), 1);
		
		assertThat(dst, is(src));
	}
	
	@Test
	public void testCopyIntersectingPartOfImageWhenSrcBoundsIsEntirelyInsideDst()
	{
		byte[] src = {
		               5,  6,
				         9, 10
				       };

		byte[] dst = {
		                0,  1,  2,  3,
				          4,  0,  0,  7,
				          8,  0,  0, 11,
				         12, 13, 14, 15
				       };
		
		ImageUtil.copyIntersectingPartOfImage(src, new Rectangle(11, 21, 2, 2), dst, new Rectangle(10, 20, 4, 4), 1);
		
		byte[] expectedResult = {
                                  0,  1,  2,  3,
		                            4,  5,  6,  7,
		                            8,  9, 10, 11,
		                           12, 13, 14, 15
		                        };
		
		assertThat(dst, is(expectedResult));
	}
	
	@Test
	public void testCopyIntersectingPartOfImageWhenSrcBoundsIntersectsDst()
	{
		byte[] src = {
		               7,  99,
				         11, 99
				       };

		byte[] dst = {
		                0,  1,  2,  3,
				          4,  5,  6,  0,
				          8,  9, 10,  0,
				         12, 13, 14, 15
				       };
		
		ImageUtil.copyIntersectingPartOfImage(src, new Rectangle(13, 21, 2, 2), dst, new Rectangle(10, 20, 4, 4), 1);
		
		byte[] expectedResult = {
                                  0,  1,  2,  3,
		                            4,  5,  6,  7,
		                            8,  9, 10, 11,
		                           12, 13, 14, 15
		                        };
		
		assertThat(dst, is(expectedResult));
	}
	
	@Test
	public void testCopyIntersectingPartOfImageWhenSrcBoundsIntersectsDstWith3Channels()
	{
		byte[] src = {
		               97, 98, 99,  8,  8,  8,
				         97, 98, 99, 12, 12, 12,
				       };

		byte[] dst = {
		                0, 0, 0,  1,  1,  1,  2,  2,  2,  3,  3,  3,
				          4, 4, 4,  5,  5,  5,  6,  6,  6,  7 , 7,  7,
				          0, 0, 0,  9,  9,  9, 10, 10, 10, 11, 11, 11,
				          0, 0, 0, 13, 13, 13, 14, 14, 14, 15, 15, 15
				       };
		
		ImageUtil.copyIntersectingPartOfImage(src, new Rectangle(29, 42, 2, 2), dst, new Rectangle(30, 40, 4, 4), 3);
		
		byte[] expectedResult = {
		                            0,  0,  0,  1,  1,  1,  2,  2,  2,  3,  3,  3,
				                      4,  4,  4,  5,  5,  5,  6,  6,  6,  7 , 7,  7,
				                      8,  8,  8,  9,  9,  9, 10, 10, 10, 11, 11, 11,
				                     12, 12, 12, 13, 13, 13, 14, 14, 14, 15, 15, 15
				                  };
		
		assertThat(dst, is(expectedResult));
	}
	
	@Test
	public void testGetScaleToFitReturnsLowerScaleWhenHeightsRatioIsLower()
	{
		Dimension originalSize = new Dimension(300, 500);
		Dimension newSize      = new Dimension(100, 300);
		
		double returnedScale   = ImageUtil.getScaleToFit(originalSize, newSize);
		
		assertEquals(returnedScale, 0.3333, 0.0001);
	}
	
	@Test
	public void testGetScaleToFitReturnsLowerScaleWhenWidthsRatioIsLower()
	{
		Dimension originalSize = new Dimension(500, 400);
		Dimension newSize      = new Dimension(250,  80);
		
		double returnedScale   = ImageUtil.getScaleToFit(originalSize, newSize);
		
		assertThat(returnedScale, is(0.2));
	}
	
	@Test
	public void testScaleToFitPreservesAspectRatioWhenImageWidthIsBiggerThanItsHeight()
	{
		BufferedImage originalImage = new BufferedImage(1000, 100, BufferedImage.TYPE_3BYTE_BGR);
		
		BufferedImage scaledImage = ImageUtil.scaleToFitPreservingAspectRatio(originalImage, new Dimension(200, 50));
		
		assertThat(scaledImage.getWidth(null), is(200));
		assertThat(scaledImage.getHeight(null), is(20));
	}
	
	@Test
	public void testScaleToFitPreservesAspectRatioWhenImageHeightIsBiggerThanItsWidth()
	{
		BufferedImage originalImage = new BufferedImage(200, 500, BufferedImage.TYPE_3BYTE_BGR);
		
		BufferedImage scaledImage = ImageUtil.scaleToFitPreservingAspectRatio(originalImage, new Dimension(150, 300));
		
		assertThat(scaledImage.getWidth(null),  is(120));
		assertThat(scaledImage.getHeight(null), is(300));
	}
	
	@Test
	public void testGetCenteredPositionReturnsCorrectValue()
	{
		Dimension imageSize = new Dimension(200, 300);
		Dimension areaSize  = new Dimension(500, 400);
		
		Point result = ImageUtil.getCenteredPosition(imageSize, areaSize);
		
		assertThat(result, is(new Point(150, 50)));
	}
}
