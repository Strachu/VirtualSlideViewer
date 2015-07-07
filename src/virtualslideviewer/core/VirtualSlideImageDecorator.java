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

import java.awt.Dimension;
import java.beans.PropertyChangeListener;

import virtualslideviewer.util.ParameterValidator;

public abstract class VirtualSlideImageDecorator extends VirtualSlideImage
{
	protected VirtualSlideImage mDecoratedImage;
	
	public VirtualSlideImageDecorator(VirtualSlideImage imageToDecorate)
	{
		ParameterValidator.throwIfNull(imageToDecorate, "imageToDecorate");
		
		mDecoratedImage = imageToDecorate;
	}
	
	@Override
	public Dimension getImageSize(int resIndex)
	{
		return mDecoratedImage.getImageSize(resIndex);
	}

	@Override
	public int getResolutionCount()
	{
		return mDecoratedImage.getResolutionCount();
	}
	
	@Override
	public int getChannelCount()
	{
		return mDecoratedImage.getChannelCount();
	}

	@Override
	public int getZPlaneCount()
	{
		return mDecoratedImage.getZPlaneCount();
	}

	@Override
	public int getTimePointCount()
	{
		return mDecoratedImage.getTimePointCount();
	}
	
	@Override
	public Dimension getTileSize(int resIndex)
	{
		return mDecoratedImage.getTileSize(resIndex);
	}

	@Override
	public boolean isRGB()
	{
		return mDecoratedImage.isRGB();
	}

	@Override
	public void getTileData(byte[] dst, Tile tile)
	{
		mDecoratedImage.getTileData(dst, tile);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		mDecoratedImage.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		mDecoratedImage.removePropertyChangeListener(listener);
	}
	
	@Override
	public String getName()
	{
		return mDecoratedImage.getName();
	}

	@Override
	public void setName(String newName)
	{
		mDecoratedImage.setName(newName);
	}

	@Override
	public String getID()
	{
		return mDecoratedImage.getID();
	}

	@Override
	public void close()
	{
		mDecoratedImage.close();
	}
}
