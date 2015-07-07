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

package virtualslideviewer.config;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;

public class ApplicationConfiguration
{
	private final Preferences           mUserPreferences;
	private final PropertyChangeSupport mPropertyListeners = new PropertyChangeSupport(this);

	public ApplicationConfiguration()
	{
		mUserPreferences = Preferences.userNodeForPackage(this.getClass());
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		mPropertyListeners.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		mPropertyListeners.addPropertyChangeListener(propertyName,listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		mPropertyListeners.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		mPropertyListeners.removePropertyChangeListener(propertyName, listener);
	}
	
	public Rectangle getWindowBounds()
	{
		int x      = mUserPreferences.getInt("WindowBounds.x",      100);
		int y      = mUserPreferences.getInt("WindowBounds.y",      100);
		int width  = mUserPreferences.getInt("WindowBounds.width",  800);
		int height = mUserPreferences.getInt("WindowBounds.height", 800);
		
		return new Rectangle(x, y, width, height);
	}
	
	public void setWindowBounds(Rectangle bounds)
	{
		if(bounds == null)
			throw new IllegalArgumentException("bounds cannot be null.");
		
		Rectangle oldValue = getWindowBounds();
		
		mUserPreferences.putInt("WindowBounds.x",      bounds.x);
		mUserPreferences.putInt("WindowBounds.y",      bounds.y);
	   mUserPreferences.putInt("WindowBounds.width",  bounds.width);
		mUserPreferences.putInt("WindowBounds.height", bounds.height);
		
		mPropertyListeners.firePropertyChange("WindowBounds", oldValue, bounds);
	}
	
	public double getZoomIncrement()
	{
		return mUserPreferences.getDouble("ZoomIncrement", 1.1);
	}
	
	public void setZoomIncrement(double increment)
	{
		double oldValue = getZoomIncrement();
		
		mUserPreferences.putDouble("ZoomIncrement", increment);
		
		mPropertyListeners.firePropertyChange("ZoomIncrement", oldValue, increment);
	}
	
	public double getResolutionTransitionThreshold()
	{
		return mUserPreferences.getDouble("ResolutionTransitionThreshold", 0.5);
	}
	
	public void setResolutionTransitionThreshold(double threshold)
	{
		double oldValue = getResolutionTransitionThreshold();
		
		mUserPreferences.putDouble("ResolutionTransitionThreshold", threshold);
		
		mPropertyListeners.firePropertyChange("ResolutionTransitionThreshold", oldValue, threshold);
	}
}
