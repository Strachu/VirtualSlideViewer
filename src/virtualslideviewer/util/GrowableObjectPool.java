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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A thread-safe object pool which creates new objects when cached one is not available.
 * 
 * Useful for caching copies of objects when they are expensive to create.
 * 
 * @param <T> Type of objects
 */
public class GrowableObjectPool<T>
{
	private final Deque<T>    mAvailableObjects = new LinkedList<T>();
	private final Supplier<T> mNewObjectFactory;
	
	/**
	 * @param newObjectFactory A delegate which will create new object when there is no available objects.
	 */
	public GrowableObjectPool(Supplier<T> newObjectFactory)
	{
		ParameterValidator.throwIfNull(newObjectFactory, "newObjectFactory");
		
		mNewObjectFactory = newObjectFactory;
	}
	
	public synchronized T borrow()
	{
		if(mAvailableObjects.isEmpty())
		{
			mAvailableObjects.addLast(mNewObjectFactory.get());
		}
		
		return mAvailableObjects.pollLast();
	}
	
	public synchronized List<T> borrowAllAvailableObjects()
	{
		List<T> listToReturn = new ArrayList<T>(mAvailableObjects);
		
		mAvailableObjects.clear();
		
		return listToReturn;
	}
	
	public synchronized void putBack(T object)
	{
		ParameterValidator.throwIfNull(object, "object");
		
		mAvailableObjects.addLast(object);
	}
}
