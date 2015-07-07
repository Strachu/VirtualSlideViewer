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

package virtualslideviewer;

import java.io.Serializable;
import java.util.NoSuchElementException;

import virtualslideviewer.core.Tile;
import virtualslideviewer.core.TileCache;
import virtualslideviewer.core.VirtualSlideImage;
import virtualslideviewer.util.ParameterValidator;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Adapter for TileCache interface for ehcache library.
 */
public class EhcacheTileCacheAdapter implements TileCache
{
	private final Ehcache mCache;

	/**
	 * @param cache Cache to use for storing tiles.
	 */
	public EhcacheTileCacheAdapter(Ehcache cache)
	{
		ParameterValidator.throwIfNull(cache, "cache");
		
		mCache = cache;
	}
	
	@Override
	public void addTile(VirtualSlideImage image, Tile tile, byte[] tileData)
	{
		ParameterValidator.throwIfNull(image, "image");
		ParameterValidator.throwIfNull(tile, "tile");
		ParameterValidator.throwIfNull(tileData, "tileData");
		
		// Adding to cache should not update access statistics.
		// It is needed in prefetching to prevent situation when prefetching removes just loaded tiles in very low memory situation.
	//	mCache.putQuiet(new Element(new UniqueTileID(image.getID(), tile), tileData));
		
		// TODO That's not good either... putQuiet also causes problems when loading the tile.
		// When the cache is full there are temporary artifacts during loading if using putQuiet().
		// Probably different call will have to be used in the case of loading and different when prefetching.
		mCache.put(new Element(new UniqueTileID(image.getID(), tile), tileData));
	}

	@Override
	public boolean hasTile(VirtualSlideImage image, Tile tile)
	{
		ParameterValidator.throwIfNull(image, "image");
		ParameterValidator.throwIfNull(tile, "tile");
		
		return mCache.isKeyInCache(new UniqueTileID(image.getID(), tile));
	}

	@Override
	public byte[] getTileData(VirtualSlideImage image, Tile tile) throws NoSuchElementException
	{
		if(!hasTile(image, tile))
			throw new NoSuchElementException(tile + " of \"" + image + "\" is not in cache.");
		
		return (byte[])mCache.get(new UniqueTileID(image.getID(), tile)).getObjectValue();
	}
	
	@Override
	public void clear()
	{
		mCache.removeAll();
	}
	
	/**
	 * Tile ID unique to given image.
	 * 
	 * Tile with the same coordinates in two different images has different unique ID.
	 */
	private static class UniqueTileID implements Serializable
	{
		private static final long serialVersionUID = -8405926210569156066L;
		
		private final String mImageID;
		private final Tile   mTile;
		
		public UniqueTileID(String imageID, Tile tile)
		{
			mImageID = imageID;
			mTile    = tile;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + mImageID.hashCode();
			result = prime * result + mTile.hashCode();
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
			
			UniqueTileID other = (UniqueTileID)obj;
			return mTile.equals(other.mTile) && mImageID.equals(other.mImageID);
		}
	}
}
