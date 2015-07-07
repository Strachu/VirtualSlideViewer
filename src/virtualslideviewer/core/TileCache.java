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

import java.util.NoSuchElementException;

/**
 * Cache for storing tile's data.
 */
public interface TileCache
{
	/**
	 * Adds tile's data to cache.
	 * 
	 * @param image    Image the tile belongs to.
	 * @param tile     Tile ID.
	 * @param tileData Tile data.
	 */
	void addTile(VirtualSlideImage image, Tile tile, byte[] tileData);
	
	/**
	 * Checks whether the tile's data is in cache.
	 * 
	 * @param image Image the tile belongs to.
	 * @param tile  Tile ID.
	 * 
	 * @return True, if data of tile with specified ID is in cache, false in opposite case.
	 */
	boolean hasTile(VirtualSlideImage image, Tile tile);
	
	/**
	 * Gets tile's data from cache.
	 * 
	 * @param image Image the tile belongs to.
	 * @param tile  Tile ID.
	 * 
	 * @return Data of tile with specified ID.
	 * 
	 * @throws NoSuchElementException When data of tile with specified ID is not in cache.
	 */
	byte[] getTileData(VirtualSlideImage image, Tile tile) throws NoSuchElementException;
	
	/**
	 * Clears the cache.
	 */
	void clear();
}
