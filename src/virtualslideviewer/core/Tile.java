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
import java.awt.Rectangle;
import java.io.Serializable;

import virtualslideviewer.util.ParameterValidator;

/**
 * A class representing a position of a tile with image's data in a rectangular grid of tiles in the image.
 */
public class Tile implements Serializable
{
	private static final long serialVersionUID = 7107531761830573431L;
	
	private final int        mCol;
	private final int        mRow;
	private final ImageIndex mImageIndex;
	
	/**
	 * @param col        The column index at which the tile can be found.
	 * @param row        The row index at which the tile can be found.
	 * @param imageIndex The image index.
	 */
	public Tile(int col, int row, ImageIndex imageIndex)
	{
		ParameterValidator.throwIfNull(imageIndex, "imageIndex");
		
		mCol        = col;
		mRow        = row;
		mImageIndex = imageIndex;
	}
	
	/**
	 * @param col        The column index at which the tile can be found.
	 * @param row        The row index at which the tile can be found.
	 * @param imageIndex The image index.
	 */
	public Tile(int col, int row, int resIndex)
	{
		mCol        = col;
		mRow        = row;
		mImageIndex = new ImageIndex(resIndex, 0, 0, 0);
	}
	
	/**
	 * Returns the index of column at which the tile can be found.
	 */
	public int getColumn()
	{
		return mCol;
	}

	/**
	 * Returns the index of row at which the tile can be found.
	 */
	public int getRow()
	{
		return mRow;
	}

	public ImageIndex getImageIndex()
	{
		return mImageIndex;
	}
	
	/**
	 * Computes the bounds of pixels contained in tile in image space.
	 * 
	 * @param sourceImage The image which the tile belong to.
	 * 
	 * @return Tile's bounds.
	 */
	public Rectangle getBounds(VirtualSlideImage sourceImage)
	{
		if(sourceImage == null)
			throw new IllegalArgumentException("sourceImage cannot be null.");
		
		if(!isValid(sourceImage))
			throw new IllegalArgumentException(toString() + " is invalid.");
		
		Dimension imageSize        = sourceImage.getImageSize(mImageIndex.getResolutionIndex());
		Dimension ordinaryTileSize = sourceImage.getTileSize(mImageIndex.getResolutionIndex());
		
		int tileX      = mCol * ordinaryTileSize.width;
		int tileY      = mRow * ordinaryTileSize.height;
		int tileWidth  = Math.min(ordinaryTileSize.width,  imageSize.width  - tileX);
		int tileHeight = Math.min(ordinaryTileSize.height, imageSize.height - tileY);

		return new Rectangle(tileX, tileY, tileWidth, tileHeight);
	}
	
	/**
	 * Checks whether the tile exists in specified image.
	 * 
	 * @param sourceImage The image.
	 * 
	 * @return True, if the tile is valid, false in opposite case.
	 */
	public boolean isValid(VirtualSlideImage sourceImage)
	{
		return mImageIndex.isValid(sourceImage) &&
		       mCol >= 0 && mCol < getTileCount(sourceImage).width  &&
		       mRow >= 0 && mRow < getTileCount(sourceImage).height;
	}

	private Dimension getTileCount(VirtualSlideImage sourceImage)
	{
		Dimension imageSize = sourceImage.getImageSize(mImageIndex.getResolutionIndex());
		Dimension tileSize  = sourceImage.getTileSize(mImageIndex.getResolutionIndex());
		
		int tilesInX = (int)Math.ceil(imageSize.getWidth()  / tileSize.getWidth());
		int tilesInY = (int)Math.ceil(imageSize.getHeight() / tileSize.getHeight());
		
		return new Dimension(tilesInX, tilesInY);
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + mImageIndex.hashCode();
		result = prime * result + mCol;
		result = prime * result + mRow;
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
		
		Tile other = (Tile)obj;
		return (mCol == other.mCol) && (mRow == other.mRow) && (mImageIndex.equals(other.mImageIndex));
	}

	@Override
	public String toString()
	{
		return String.format("Tile (%s, %s) at res %s and c = %s, z = %s, t = %s", mCol, mRow, mImageIndex.getResolutionIndex(),
		                     mImageIndex.getChannel(), mImageIndex.getZPlane(), mImageIndex.getTimePoint());
	}
}
