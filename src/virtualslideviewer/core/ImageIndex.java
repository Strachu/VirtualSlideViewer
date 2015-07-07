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

import java.io.Serializable;

public class ImageIndex implements Serializable
{
	private static final long serialVersionUID = 7107531761830573431L;
	
	private final int mResIndex;
	private final int mChannel;
	private final int mZPlane;
	private final int mTimePoint;
	
	public ImageIndex(int resIndex)
	{
		this(resIndex, 0, 0, 0);
	}
	
	public ImageIndex(int resIndex, int channel, int zPlane, int timePoint)
	{
		mResIndex  = resIndex;
		mChannel   = channel;
		mZPlane    = zPlane;
		mTimePoint = timePoint;
	}
	
	/**
	 * Returns the index of resolution level in image at which the tile can be found.
	 */
	public int getResolutionIndex()
	{
		return mResIndex;
	}
	
	public int getChannel()
	{
		return mChannel;
	}
	
	public int getZPlane()
	{
		return mZPlane;
	}
	
	public int getTimePoint()
	{
		return mTimePoint;
	}
	
	public boolean isValid(VirtualSlideImage sourceImage)
	{
		return mResIndex  >= 0 && mResIndex  < sourceImage.getResolutionCount() &&
		       mChannel   >= 0 && mChannel   < sourceImage.getChannelCount()    &&
		       mZPlane    >= 0 && mZPlane    < sourceImage.getZPlaneCount()     &&
		       mTimePoint >= 0 && mTimePoint < sourceImage.getTimePointCount();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + mChannel;
		result = prime * result + mResIndex;
		result = prime * result + mTimePoint;
		result = prime * result + mZPlane;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		
		ImageIndex other = (ImageIndex)obj;
		return (mChannel   == other.mChannel)   && (mResIndex == other.mResIndex) &&
		       (mTimePoint == other.mTimePoint) && (mZPlane   == other.mZPlane);
	}
}
