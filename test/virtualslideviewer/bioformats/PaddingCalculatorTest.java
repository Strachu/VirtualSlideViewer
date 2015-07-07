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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.bioformats.PaddingCalculator;
import virtualslideviewer.core.Tile;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.testutils.TestUtil;

public class PaddingCalculatorTest
{
	private VirtualSlideImage mImageMock;
	private PaddingCalculator mTestedCalculator;

	@Before
	public void setUp() throws Exception
	{
		mImageMock        = TestUtil.createImageMockWithDefaultParameters();
		mTestedCalculator = new PaddingCalculator(Executors.newSingleThreadExecutor());
	}

	@Test
	public void testPaddingComputationForDataWithPadding() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(4, 6));
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(2, 3));
		
		setTileData(mImageMock, new Tile(0, 0, 1), new byte[] {
		                                                       (byte)255, (byte)255,
                                                                   111, (byte)255,
		                                                       (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 0, 1), new byte[] {
                                                                    83, (byte)255,
                                                                    32, (byte)255,
                                                             (byte)255, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(0, 1, 1), new byte[] {
		                                                             100, (byte)255,
		                                                       (byte)255, (byte)255,
		                                                       (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 1, 1), new byte[] {
		                                                             115, (byte)255,
		                                                       (byte)255, (byte)255,
                                                             (byte)255, (byte)255
                                                            });
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 1);
		
		assertThat(computedPadding, is(new Dimension(1, 2)));
	}
	
	@Test
	public void testPaddingComputationForDataWithoutPadding() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(2)).thenReturn(new Dimension(4, 4));
		Mockito.when(mImageMock.getTileSize(2)).thenReturn(new Dimension(2, 2));
		
		setTileData(mImageMock, new Tile(0, 0, 2), new byte[] {
		                                                             100, (byte)255,
		                                                       (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 0, 2), new byte[] {
		                                                             100, (byte)255,
                                                                    32,        43
                                                            });
		setTileData(mImageMock, new Tile(0, 1, 2), new byte[] {
		                                                             100, (byte)255,
		                                                       (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 1, 2), new byte[] {       
		                                                             100, (byte)255,
                                                                     5, (byte)255
                                                            });
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 2);
		
		assertThat(computedPadding, is(new Dimension(0, 0)));
	}
		
	@Test
	public void testPaddingComputationForRGBImage() throws InterruptedException
	{
		Mockito.when(mImageMock.isRGB()).thenReturn(true);
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(4, 6));
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(2, 3));
		
		setTileData(mImageMock, new Tile(0, 0, 1), new byte[] {
		                                                               4, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255,
		                                                       (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 0, 1), new byte[] {
		                                                              83, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255,
                                                                    32,        43, (byte)255, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255
                                                             });
		setTileData(mImageMock, new Tile(0, 1, 1), new byte[] {
		                                                             110,        52, (byte)255, (byte)255, (byte)255, (byte)255,
		                                                       (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255,
		                                                       (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 1, 1), new byte[] {
		                                                             100,        21, (byte)255, (byte)255, (byte)255, (byte)255,
		                                                       (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255, (byte)255, (byte)255
                                                            });
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 1);
		
		assertThat(computedPadding, is(new Dimension(1, 2)));
	}
	
	@Test
	public void testPaddingComputationCalculatesPaddingOnlyInOneDirectionWhenTheOtherIsNotAlignedToTileSize() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(8, 7));
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(4, 4));
		
		setTileData(mImageMock, new Tile(0, 0, 1), new byte[] {
		                                                             120,       50,         43,       23,
		                                                              32,       43,         42,       1,
		                                                               5,       19,         90,       95,
		                                                              32,        0,         25,       10
		                                                      });
		setTileData(mImageMock, new Tile(1, 0, 1), new byte[] {
		                                                              13, (byte)255, (byte)255, (byte)255,
                                                                    32,        43, (byte)255, (byte)255,
                                                                    53, (byte)255, (byte)255, (byte)255,
                                                                     0,        11, (byte)255, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(0, 1, 1), new byte[] {
		                                                               4, (byte)255,        54, (byte)255,
                                                                    32, (byte)255,        23, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(1, 1, 1), new byte[] {
		                                                             105, (byte)255, (byte)255, (byte)255,
                                                                    32, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255, (byte)255
                                                            });
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 1);
		
		assertThat(computedPadding, is(new Dimension(2, 0)));
	}
	
	@Test
	public void testPaddingComputationIsSkippedWhenImageSizeIsNotAlignedToTileSize() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(0)).thenReturn(new Dimension(110, 45));
		Mockito.when(mImageMock.getTileSize(0)).thenReturn(new Dimension(20, 20));
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 0);
		
		assertThat(computedPadding, is(new Dimension(0, 0)));
	}
	
	@Test
	public void testPaddingComputationReturnsZeroWhenEntireImageIsWhite() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(20, 10));
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(20, 10));
		
		byte[] whiteArray = new byte[200];
		Arrays.fill(whiteArray, (byte)255);
		setTileData(mImageMock, new Tile(0, 0, 1), whiteArray);
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 1);
		
		assertThat(computedPadding, is(new Dimension(0, 0)));
	}

	/**
	 * One of the test samples of .vsi virtual slides had some entirely black tiles in lower right corner of image.
	 * It occured only at 3 highest resolutions and had increasing sizes, respectively, 1x1, 2x1 and 3x1 tiles.
	 * 
	 * This test is to ensure that these black tiles are ignored during padding calculations.
	 */
	@Test
	public void testPaddingComputationIgnoresSomeBlackTilesFromSomeVSIFiles() throws InterruptedException
	{
		Mockito.when(mImageMock.getImageSize(1)).thenReturn(new Dimension(9, 4));
		Mockito.when(mImageMock.getTileSize(1)).thenReturn(new Dimension(3, 2));
		
		setTileData(mImageMock, new Tile(0, 0, 1), new byte[] {
		                                                             100, (byte)255, (byte)255,
                                                             (byte)255, (byte)255, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(1, 0, 1), new byte[] {
		                                                               0, (byte)255, (byte)255,
                                                                    32,        43, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(2, 0, 1), new byte[] {
		                                                             100, (byte)255, (byte)255,
                                                                    32,        43, (byte)255
                                                            });
		setTileData(mImageMock, new Tile(0, 1, 1), new byte[] {
		                                                             100,        52, (byte)255,
		                                                       (byte)255, (byte)255, (byte)255
		                                                      });
		setTileData(mImageMock, new Tile(1, 1, 1), new byte[] {
		                                                               0,         0,         0,
		                                                               0,         0,         0
		                                                      });
		setTileData(mImageMock, new Tile(2, 1, 1), new byte[] {
		                                                               0,         0,         0,
                                                                     0,         0,         0
                                                            });
		
		Dimension computedPadding = mTestedCalculator.computePadding(mImageMock, 1);
		
		assertThat(computedPadding, is(new Dimension(1, 1)));
	}
	
	private void setTileData(VirtualSlideImage imageMock, Tile tile, byte[] data)
	{
		TestUtil.copyToParameter(data).when(imageMock).getTileData(Mockito.any(), Mockito.eq(tile));
	}
}
