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
import static org.junit.Assert.assertThat;

import org.junit.Test;

import virtualslideviewer.util.PixelDataUtil;

public class PixelDataUtilTest
{
	@Test
	public void testSwapRgbColorComponents()
	{
		byte[] swappedArray = { 
		                         0, 1, 2,
		                         3, 4, 5,
		                         6, 7, 8
		                      };
		
		PixelDataUtil.swapRgbColorComponents(swappedArray);
		
		byte[] expectedArray = { 
		                          2, 1, 0,
		                          5, 4, 3,
		                          8, 7, 6
		                       };
		
		assertThat(swappedArray, is(expectedArray));
	}
}
