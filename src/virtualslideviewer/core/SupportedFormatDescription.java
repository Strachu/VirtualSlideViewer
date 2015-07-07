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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import virtualslideviewer.util.ParameterValidator;

public class SupportedFormatDescription
{
	private final String       mIdentifier;
	private final String       mDescription;
	private final List<String> mExtensions;
	
	public SupportedFormatDescription(String identifier, String description, String... extensions)
	{
		ParameterValidator.throwIfNull(identifier, "identifier");
		ParameterValidator.throwIfNull(description, "description");
		ParameterValidator.throwIfNull(extensions, "extensions");
		
		mIdentifier  = identifier;
		mDescription = description;
		mExtensions  = Arrays.asList(extensions);
	}
	
	public String getIdentifier()
	{
		return mIdentifier;
	}
	
	public String getDescription()
	{
		return mDescription;
	}
	
	public List<String> getExtensions()
	{
		return Collections.unmodifiableList(mExtensions);
	}
}
