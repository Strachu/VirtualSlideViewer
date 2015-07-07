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

import java.util.ArrayList;
import java.util.List;

public class ByteArrayPool
{
	private final List<byte[]> mAvailableArrays = new ArrayList<byte[]>();
	
	public synchronized byte[] borrow(int minimumSizeRequired)
	{
		for(int i = mAvailableArrays.size() - 1; i > 0 ;i--)
		{
			if(mAvailableArrays.get(i).length >= minimumSizeRequired)
			{
				return mAvailableArrays.remove(i);
			}
		}
		
		return new byte[minimumSizeRequired];
	}
	
	public synchronized void putBack(byte[] array)
	{
		if(array == null)
			throw new IllegalArgumentException("array cannot be null.");
		
		mAvailableArrays.add(array);
	}
}
