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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import virtualslideviewer.UncheckedInterruptedException;

public class ThreadPoolUtil
{
	/**
	 * Method similar to ExecutorService.invokeAll() but this one does not swallow exceptions and uses UncheckedInterruptedException.
	 */
	public static <T> void scheduleAndWait(ExecutorService threadPool, List<Callable<T>> tasks) throws UncheckedInterruptedException
	{
		ParameterValidator.throwIfNull(threadPool, "threadPool");
		ParameterValidator.throwIfNull(tasks, "tasks");
		
		List<Future<?>> futures = new ArrayList<Future<?>>();

		for(Callable<T> task : tasks)
		{
			futures.add(threadPool.submit(task));
		}
		
		try
		{
			for(Future<?> future : futures)
			{
				future.get();
			}
		}
		catch(InterruptedException e)
		{
			for(Future<?> future : futures)
			{
				future.cancel(true);
			}
			
			throw new UncheckedInterruptedException(e.getMessage());
		}
		catch(ExecutionException e)
		{
			throw new RuntimeException(e.getCause());
		}
		finally
		{
			for(Future<?> future : futures)
			{
				future.cancel(false);
			}
		}
	}
}
