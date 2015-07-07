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

package virtualslideviewer.testutils;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import virtualslideviewer.core.BufferedVirtualSlideImage;

public class TestUtil
{
	public static Stubber copyToParameter(byte[] result)
	{
		return Mockito.doAnswer((x) ->
		{
			byte[] dst = (byte[])x.getArguments()[0];
			
			System.arraycopy(result, 0, dst, 0, result.length);
			return null;
		});
	}
	
	/**
	 * Creates and returns a preconfigured to default, safe values, image mock.
	 * 
	 * It prevents NullPointerException from occuring when methods not important in particular test
	 * but used in methods of tested class are called, such as getTileSize() and getImageSize().
	 * 
	 * This way it is not required to stub the methods in every tests, only in those that will need it.
	 */
	public static BufferedVirtualSlideImage createImageMockWithDefaultParameters()
	{
		BufferedVirtualSlideImage imageMock = Mockito.mock(BufferedVirtualSlideImage.class);
		
		Mockito.when(imageMock.getResolutionCount()).thenReturn(10);
		for(int i = 0; i < 10; i++)
		{
			Mockito.when(imageMock.getImageSize(i)).thenReturn(new Dimension(10000, 10000));
			Mockito.when(imageMock.getTileSize(i)).thenReturn(new Dimension(1000, 1000));
		}
		
		Mockito.when(imageMock.getChannelCount()).thenReturn(1);
		Mockito.when(imageMock.getZPlaneCount()).thenReturn(1);
		Mockito.when(imageMock.getTimePointCount()).thenReturn(1);
		
		Mockito.when(imageMock.isRGB()).thenReturn(false);
		
		copyToParameter(new byte[3]).when(imageMock).getTileData(Mockito.any(), Mockito.any());
		
		// Use real implementation in helper methods.
		Mockito.when(imageMock.getTileData(Mockito.any())).thenCallRealMethod();
		Mockito.when(imageMock.getPixels(Mockito.any(), Mockito.any())).thenCallRealMethod();
		Mockito.doCallRealMethod().when(imageMock).getPixels(Mockito.any(), Mockito.any(), Mockito.any());
		
		Mockito.when(imageMock.isImageInCache(Mockito.any(), Mockito.any())).thenReturn(false);
		
		return imageMock;
	}
	
	/**
	 * Creates a matcher for Rectangle2D which correctly compares floating point values.
	 */
	public static Matcher<Rectangle2D> is(Rectangle2D expectedRectangle)
	{
		return new TypeSafeMatcher<Rectangle2D>()
		{
			private final static double EPSILON = 0.0000001;
			
			@Override
			public void describeTo(Description description)
			{
				description.appendValue(expectedRectangle);
			}
			
			@Override
			protected boolean matchesSafely(Rectangle2D item)
			{
				return (Math.abs(expectedRectangle.getX()      - item.getX())      < EPSILON) &&
				       (Math.abs(expectedRectangle.getY()      - item.getY())      < EPSILON) &&
				       (Math.abs(expectedRectangle.getWidth()  - item.getWidth())  < EPSILON) &&
				       (Math.abs(expectedRectangle.getHeight() - item.getHeight()) < EPSILON);
			}
		};
	}
}
