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

package virtualslideviewer.ui.imageviewing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import virtualslideviewer.testutils.TestUtil;
import virtualslideviewer.ui.imageviewing.Camera;

public class CameraTest
{
	private List<Dimension> mImageResolutions = new ArrayList<>();
	private Camera          mTestedCamera;
	
	@Before
	public void setUp() throws Exception
	{
		mTestedCamera = new Camera();
		
		mTestedCamera.setImageSize(new Dimension(16000, 8000));
		
		mImageResolutions.add(new Dimension( 1000,  500));
		mImageResolutions.add(new Dimension( 2000, 1000));
		mImageResolutions.add(new Dimension( 4000, 2000));
		mImageResolutions.add(new Dimension( 8000, 4000));
		mImageResolutions.add(new Dimension(16000, 8000));
	}
	
	@Test
	public void testGettingVisibleRegionBoundsWithValidValuesAndWithoutZoom()
	{
		mTestedCamera.setZoom(1.0);
		mTestedCamera.setPosition(new Point2D.Double(0.625, 0.5));
		mTestedCamera.setViewportSize(new Dimension(160, 400));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.62, 0.475, 0.01, 0.05)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(9920, 3800, 160, 400)));
	}
	
	@Test
	public void testGettingVisibleRegionBoundsWithValidValuesAndWithZoom()
	{
		mTestedCamera.setZoom(0.25);
		mTestedCamera.setPosition(new Point2D.Double(0.625, 0.5));
		mTestedCamera.setViewportSize(new Dimension(160, 400));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.605, 0.4, 0.04, 0.2)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(2420, 800, 160, 400)));
	}
	
	@Test
	public void testInvalidRegionCenterIsCorrected()
	{
		mTestedCamera.setZoom(0.5);
		mTestedCamera.setPosition(new Point2D.Double(0.5, 0.9));
		mTestedCamera.setViewportSize(new Dimension(1000, 2000));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.4375, 0.5, 0.125, 0.5)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(3500, 2000, 1000, 2000)));
	}
	
	@Test
	public void testPanningWithValidTranslation()
	{
		mTestedCamera.setZoom(0.5);
		mTestedCamera.setPosition(new Point2D.Double(0.5, 0.75));
		mTestedCamera.setViewportSize(new Dimension(160, 100));
		
		mTestedCamera.pan(new Point(1000, -500));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.615, 0.6125, 0.02, 0.025)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(4920, 2450, 160, 100)));
	}	
	
	@Test
	public void testPanningClipsResultingPositionToImageBounds()
	{
		mTestedCamera.setZoom(0.75);
		mTestedCamera.setPosition(new Point2D.Double(0.25, 0.8));
		mTestedCamera.setViewportSize(new Dimension(600, 360));
		
		mTestedCamera.pan(new Point(-900, 3000));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.15, 0.94, 0.05, 0.06)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(1800, 5640, 600, 360)));
	}	
	
	@Test
	public void testPanningStartsFromImageEdgeWhenCenterHasBeenSetToOutsideOfImageBounds()
	{
		mTestedCamera.setZoom(0.25);
		mTestedCamera.setPosition(new Point2D.Double(2.0, 0.5));
		mTestedCamera.setViewportSize(new Dimension(600, 360));
		
		mTestedCamera.pan(new Point(-500, 180));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.725, 0.5, 0.15, 0.18)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(2900, 1000, 600, 360)));
	}

	@Test
	public void testZoomingAtCenterCorrectlyUpdatesVisibleRegionBounds()
	{
		mTestedCamera.setZoom(1.0);
		mTestedCamera.setPosition(new Point2D.Double(0.4, 0.25));
		mTestedCamera.setViewportSize(new Dimension(320, 800));
		mTestedCamera.setZoom(0.5);
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.38, 0.15, 0.04, 0.2)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(3040, 600, 320, 800)));
	}	
	
	@Test
	public void testZoomingAtArbitraryPointCorrectlyUpdatesVisibleRegionBounds()
	{
		mTestedCamera.setZoom(0.25);
		mTestedCamera.setPosition(new Point2D.Double(0.4, 0.25));
		mTestedCamera.setViewportSize(new Dimension(320, 800));
		mTestedCamera.setZoom(0.5, new Point(50, 25));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.36625, 0.05625, 0.04, 0.2)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(2930, 225, 320, 800)));
	}

	@Test
	public void testZoomingAtArbitraryPointCorrectlyUpdatesVisibleRegionBoundsWhenRegionCenterIsOutOfImageBounds()
	{
		mTestedCamera.setZoom(1.0);
		mTestedCamera.setPosition(new Point2D.Double(0.4, -0.5));
		mTestedCamera.setViewportSize(new Dimension(320, 800));
		mTestedCamera.setZoom(2.0, new Point(50, 25));
		
		assertThat(mTestedCamera.getVisibleRegionBounds(), TestUtil.is(new Rectangle2D.Double(0.3915625, 0.0015625, 0.01, 0.05)));
		assertThat(mTestedCamera.getAbsoluteVisibleRegionBounds(), is(new Rectangle(12530, 25, 320, 800)));
	}	
	
	@Test
	public void testZoomToFitWhenImageIsLargerThanViewport()
	{
		mTestedCamera.setViewportSize(new Dimension(500, 1000));
		mTestedCamera.setImageSize(new Dimension(2000, 3000));
		
		mTestedCamera.zoomToFit();
		
		assertThat(mTestedCamera.getZoom(), is(0.25));
	}

	@Test
	public void testZoomToFitWhenImageIsSmallerThanViewportDoesNotUpscaleIt()
	{
		mTestedCamera.setViewportSize(new Dimension(800, 1000));
		mTestedCamera.setImageSize(new Dimension(500, 600));
		
		mTestedCamera.zoomToFit();
		
		assertThat(mTestedCamera.getZoom(), is(1.0));
	}
	
	@Test
	public void testRelativeToAbsoluteBoundsConversion()
	{
		Rectangle2D relativeBounds = new Rectangle2D.Double(0.25, 0.4, 0.125, 0.6);
		Dimension   areaSize       = new Dimension(1000, 2000);

		Rectangle returnedBounds   = Camera.relativeToAbsoluteBounds(relativeBounds, areaSize);
		
		assertThat(returnedBounds, is(new Rectangle(250, 800, 125, 1200)));
	}
	
	@Test
	public void testGetBestResolutionReturnsTheLowestResolutionAtLowZoomLevel()
	{
		mTestedCamera.setZoom(0.003);
		
		int returnedResIndex = mTestedCamera.getBestResolutionForCurrentZoom(mImageResolutions, 0.25);
		
		assertThat(returnedResIndex, is(0));
	}	
	
	@Test
	public void testGetBestResolutionReturnsTheHighestResolutionAtZoomAbove100Percent()
	{
		mTestedCamera.setZoom(123.32);
		
		int returnedResIndex = mTestedCamera.getBestResolutionForCurrentZoom(mImageResolutions, 0.25);
		
		assertThat(returnedResIndex, is(mImageResolutions.size() - 1));
	}
	
	@Test
	public void testGetBestResolutionReturnsCorrectResolutionLevelAccordingToGivenThreshold()
	{
		mTestedCamera.setZoom(0.45);
		
		int returnedResIndexWithHigherThreshold = mTestedCamera.getBestResolutionForCurrentZoom(mImageResolutions, 0.9);
		int returnedResIndexWithLowerThreshold  = mTestedCamera.getBestResolutionForCurrentZoom(mImageResolutions, 0.7);
		
		assertThat(returnedResIndexWithHigherThreshold, is(2));
		assertThat(returnedResIndexWithLowerThreshold,  is(3));
	}
	
	@Test
	public void testListenerIsCalledWhenCameraPositionChanges()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.setPosition(new Point2D.Double(0.35, 0.24));
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenViewportSizeChanges()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.setViewportSize(new Dimension(100, 200));
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenCameraIsTranslated()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.pan(new Point(100, 200));
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenCameraZoomChanges()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.setZoom(0.2);
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenCameraZoomChangesWhenZoomingAtSpecifingPoint()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.setZoom(0.2, new Point(50, 100));
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenCameraZoomsToFitEntireImage()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.setViewportSize(new Dimension(500, 1000));
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.zoomToFit();
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
	
	@Test
	public void testListenerIsCalledWhenImageSizeIsChanged()
	{
		Camera.Listener listenerMock = Mockito.mock(Camera.Listener.class);
		
		mTestedCamera.addChangeListener(listenerMock);
		
		mTestedCamera.setImageSize(new Dimension(1234, 5678));
		
		Mockito.verify(listenerMock).onVisibleRegionUpdate();
	}
}
