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

package virtualslideviewer.ui.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import virtualslideviewer.core.SupportedFormatDescription;

/**
 * A FileFilter which filters by extensions of specified format.
 * 
 * A javax.swing.filechooser.FileNameExtensionFilter class is not suitable because it does not work with compound extensions such as .ome.tiff
 */
public class SupportedFormatFileFilter extends FileFilter
{
	private final SupportedFormatDescription mFormatDescription;
	
	public SupportedFormatFileFilter(SupportedFormatDescription format)
	{
		if(format == null)
			throw new IllegalArgumentException("format cannot be null.");
		
		mFormatDescription = format;
	}
	
	@Override
	public boolean accept(File file)
	{
		if(file.isDirectory())
			return true;
		
		return mFormatDescription.getExtensions().stream().anyMatch(ext -> endsWithCaseInsensitive(file.getName(), ext));
	}
	
	private boolean endsWithCaseInsensitive(String first, String second)
	{
		return first.toLowerCase().endsWith(second.toLowerCase());
	}

	@Override
	public String getDescription()
	{
		return mFormatDescription.getDescription();
	}

}
