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

package virtualslideviewer.core.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import virtualslideviewer.UncheckedInterruptedException;
import virtualslideviewer.bioformats.OmeTiffSavingService;
import virtualslideviewer.core.SupportedFormatDescription;
import virtualslideviewer.core.TileCache;
import virtualslideviewer.core.VirtualSlide;
import virtualslideviewer.util.ParameterValidator;

/**
 * A class providing services for loading and saving of virtual slides in many formats.
 */
public class VirtualSlidePersistenceService
{
	public interface Listener
	{
		public void onVirtualSlideLoaded(VirtualSlide loadedSlide, Path loadedFilePath);
	}
	
	private final TileCache                mCache;
	private final OmeTiffSavingService     mOmeTiffSavingService;
	private final List<VirtualSlideLoader> mLoaders = new ArrayList<>();

	private final List<Listener> mListeners = new ArrayList<>();

	public VirtualSlidePersistenceService(TileCache cache, ExecutorService threadPool)
	{
		ParameterValidator.throwIfNull(cache, "cache");
		ParameterValidator.throwIfNull(threadPool, "threadPool");
		
		mCache                = cache;
		mOmeTiffSavingService = new OmeTiffSavingService(threadPool);
	}
	
	public void addListener(Listener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		
		mListeners.add(listener);
	}
	
	public void addLoader(VirtualSlideLoader loader)
	{
		if(loader == null)
			throw new IllegalArgumentException("loader cannot be null.");
		
		mLoaders.add(loader);
	}

	/**
	 * Loads a virtual slide from specified file.
	 * 
	 * @param fileToOpen The path with a file to load.
	 * @throws UnsupportedOperationException No loader is available for this file.
	 * @throws VirtualSlideLoadException Loading failed.
	 * @throws UncheckedInterruptedException The loading has been interrupted.
	 */
	public VirtualSlide load(Path fileToOpen) throws UnsupportedOperationException, VirtualSlideLoadException, UncheckedInterruptedException
	{
		ParameterValidator.throwIfNull(fileToOpen, "fileToOpen");

		Optional<VirtualSlideLoader> capableLoader = mLoaders.stream().filter(loader -> loader.canLoad(fileToOpen)).findFirst();
		
		if(!capableLoader.isPresent())
			throw new UnsupportedOperationException("Loading of " + fileToOpen + " is not supported.");
		
		VirtualSlide virtualSlide = capableLoader.get().load(fileToOpen);
		
		mCache.clear();
		
		mListeners.forEach(l -> l.onVirtualSlideLoaded(virtualSlide, fileToOpen));
		
		return virtualSlide;
	}
	
	public List<SupportedFormatDescription> getFormatsWithSaveSupport()
	{
		return Arrays.asList(new SupportedFormatDescription("OME-TIFF", "OME-TIFF (*.ome.tif, *.ome.tiff)", "ome.tif", "ome.tiff"));
	}
	
	public void save(VirtualSlide slide, Path destinationPath, String destinationFormat) throws UnsupportedOperationException, IOException,
	                                                                                            UncheckedInterruptedException
	{	
		save(slide, destinationPath, destinationFormat, new NullSaveProgressReporter());
	}
	
	/**
	 * Saves the specified slide to a file in specified format.
	 * 
	 * @param slide The virtual slide to save.
	 * @param destinationPath The path to save the slide to.
	 * @param destinationFormat The format to save the slide in.
	 * @param progress The object which will receive information about the progress of the operation. Optional.
	 * 
	 * @throws UnsupportedOperationException Saving in specified format is not supported.
	 * @throws IOException
	 */
	public void save(VirtualSlide slide, Path destinationPath, String destinationFormat,
	                 SaveProgressReporter progress) throws UnsupportedOperationException, IOException, UncheckedInterruptedException
	{
		ParameterValidator.throwIfNull(slide, "slide");
		ParameterValidator.throwIfNull(destinationPath, "destinationPath");
		ParameterValidator.throwIfNull(destinationFormat, "destinationFormat");
		
		if(progress == null) 
			progress = new NullSaveProgressReporter();
		
		if(!destinationFormat.equals("OME-TIFF"))
			throw new UnsupportedOperationException("Saving to " + destinationFormat + " is not supported.");
		
		mOmeTiffSavingService.save(slide, destinationPath, progress);
	}
}
