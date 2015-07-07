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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.core.BufferedVirtualSlideImage;
import virtualslideviewer.core.ImageIndex;
import virtualslideviewer.core.Tile;
import virtualslideviewer.imageviewing.DifferentResolutionsTileGenerator;
import virtualslideviewer.testutils.TestUtil;

public class DifferentResolutionsTileGeneratorTest
{
	private BufferedVirtualSlideImage mImageMock = TestUtil.createImageMockWithDefaultParameters();

	@Before
	public void setUp() throws Exception
	{
		Mockito.when(mImageMock.getResolutionCount()).thenReturn(4);
		Mockito.when(mImageMock.getImageSize(3)).thenReturn(new Dimension(40, 40));
		Mockito.when(mImageMock.getImageSize(2)).thenReturn(new Dimension(20, 20));
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(10, 10));
		Mockito.when(mImageMock.getImageSize(0)).thenReturn(new Dimension( 5,  5));
		Mockito.when(mImageMock.getTileSize(Mockito.anyInt())).thenReturn(new Dimension(4, 4));
		
		Mockito.when(mImageMock.isImageInCache(Mockito.any(), Mockito.any())).thenReturn(false);
	}

	@Test
	public void testGeneratingOfTileFromLowerResolutionWhenItIsInCache()
	{
		Tile      tileToGenerate       = new Tile(1, 2, new ImageIndex(2));
		int       resolutionToUse      = 1;
		Rectangle tileBoundsInLowerRes = new Rectangle(2, 4, 2, 2);
		byte[]    pixelsInLowerRes     = new byte[] {
		                                               0, 1,
		                                               2, 3
		                                            };
		byte[]    expectedResult       = new byte[] {
                                                     0, 0, 1, 1,
                                                     0, 0, 1, 1,
                                                     2, 2, 3, 3,
                                                     2, 2, 3, 3
                                                  };
		
		testGeneratedTileIsEqualToExpected(tileToGenerate, resolutionToUse, tileBoundsInLowerRes, pixelsInLowerRes, expectedResult);
	}

	@Test
	public void testGeneratingOfTileFromSecondLowerResolutionWhenItIsInCache()
	{
		Tile      tileToGenerate             = new Tile(1, 2, new ImageIndex(3));
		int       resolutionToUse            = 1;
		Rectangle tileBoundsInSecondLowerRes = new Rectangle(1, 2, 1, 1);
		byte[]    pixelsInSecondLowerRes     = new byte[] { 8 };
		byte[]    expectedResult             = new byte[] {
                                                           8, 8, 8, 8,
                                                           8, 8, 8, 8,
                                                           8, 8, 8, 8,
                                                           8, 8, 8, 8
                                                        };
		
		testGeneratedTileIsEqualToExpected(tileToGenerate, resolutionToUse, tileBoundsInSecondLowerRes, pixelsInSecondLowerRes, expectedResult);
	}
	
	@Test
	public void testGeneratingOfTileFromHigherResolutionWhenItIsInCache()
	{
		Tile      tileToGenerate        = new Tile(1, 2, new ImageIndex(2));
		int       resolutionToUse       = 3;
		Rectangle tileBoundsInHigherRes = new Rectangle(8, 16, 8, 8);
		byte[]    pixelsInHigherInRes   = new byte[] {
		                                                 0,  1,  2,  3,  4,  5,  6,  7,
		                                                 8,  9, 10, 11, 12, 13, 14, 15,
		                                                16, 17, 18, 19, 20, 21, 22, 23,
		                                                24, 25, 26, 27, 28, 29, 30, 31,
		                                                32, 33, 34, 35, 36, 37, 38, 39,
		                                                40, 41, 42, 43, 44, 45, 46, 47,
		                                                48, 49, 50, 51, 52, 53, 54, 55,
		                                                56, 57, 58, 59, 60, 61, 62, 63
		                                             };
		// Nearest Neighbour interpolation
		byte[]    expectedResult        = new byte[] {
                                                       9, 11, 13, 15,
		                                                25, 27, 29, 31,
		                                                41, 43, 45, 47,
		                                                57, 59, 61, 63
                                                   };
		
		testGeneratedTileIsEqualToExpected(tileToGenerate, resolutionToUse, tileBoundsInHigherRes, pixelsInHigherInRes, expectedResult);
	}

	@Test
	public void testGeneratingOfTileFromThumbnailWhenOtherResolutionsAreNotInCache()
	{
		Tile      tileToGenerate        = new Tile(1, 2, new ImageIndex(2));
		int       resolutionToUse       = 0;
		Rectangle tileBoundsInThumbnail = new Rectangle(1, 2, 1, 1);
		byte[]    pixelsInThumbnail     = new byte[] { 5 };
		byte[]    expectedResult        = new byte[] {
                                                      5, 5, 5, 5,
                                                      5, 5, 5, 5,
                                                      5, 5, 5, 5,
                                                      5, 5, 5, 5
                                                   };
		
		testGeneratedTileIsEqualToExpected(tileToGenerate, resolutionToUse, tileBoundsInThumbnail, pixelsInThumbnail, expectedResult);
	}
	
	@Test
	public void testGeneratingOfTileHandlesPartialTiles()
	{
		Tile      tileToGenerate       = new Tile(2, 1, new ImageIndex(1));
		int       resolutionToUse      = 0;
		Rectangle tileBoundsInLowerRes = new Rectangle(4, 2, 1, 2);
		byte[]    pixelsInLowerRes     = new byte[] {
		                                               5,
		                                               6
		                                            };
		byte[]    expectedResult       = new byte[] {
                                                     5, 5,
                                                     5, 5,
                                                     6, 6,
                                                     6, 6
                                                  };
		
		testGeneratedTileIsEqualToExpected(tileToGenerate, resolutionToUse, tileBoundsInLowerRes, pixelsInLowerRes, expectedResult);
	}
	
	private void testGeneratedTileIsEqualToExpected(Tile tileToGenerate, int resolutionToUse, Rectangle tileBoundsInUsedResolution,
	                                                byte[] pixelsInTileBounds, byte[] expectedResult)
	{
		Mockito.when(mImageMock.isImageInCache(tileBoundsInUsedResolution, new ImageIndex(resolutionToUse))).thenReturn(true);
	
		TestUtil.copyToParameter(pixelsInTileBounds)
		        .when(mImageMock).getPixels(Mockito.any(), Mockito.eq(tileBoundsInUsedResolution), Mockito.eq(new ImageIndex(resolutionToUse)));		
		
		byte[] result = new byte[expectedResult.length];
		
		new DifferentResolutionsTileGenerator().getTilePlaceholder(result, mImageMock, tileToGenerate);
		
		Mockito.verify(mImageMock).getPixels(Mockito.any(), Mockito.eq(tileBoundsInUsedResolution), Mockito.eq(new ImageIndex(resolutionToUse)));
		
		assertThat(result, is(expectedResult));
	}
}
