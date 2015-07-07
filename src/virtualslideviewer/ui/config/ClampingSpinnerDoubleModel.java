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

package virtualslideviewer.ui.config;

import javax.swing.SpinnerNumberModel;

/**
 * A SpinnerNumberModel which clamps the value to the minimum or maximum if specified value
 * is out of bounds instead of revering to previous value.
 */
public class ClampingSpinnerDoubleModel extends SpinnerNumberModel
{
	private static final long serialVersionUID = 1L;
	
	private final double mMinimumValue;
	private final double mMaximumValue;
	
	public ClampingSpinnerDoubleModel(double value, double minimumValue, double maximumValue, double stepSize)
	{
		super(value, -Double.MAX_VALUE, Double.MAX_VALUE, stepSize);
		
		mMinimumValue = minimumValue;
		mMaximumValue = maximumValue;
	}
	
	@Override
	public void setValue(Object value)
	{
		double dValue = (double)value;
		
		double clampedValue = Math.min(Math.max(dValue, mMinimumValue), mMaximumValue);
		
		super.setValue(clampedValue);
	}
}
