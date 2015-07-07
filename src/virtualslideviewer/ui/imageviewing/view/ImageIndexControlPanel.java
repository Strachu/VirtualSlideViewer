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

package virtualslideviewer.ui.imageviewing.view;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;

import java.awt.Insets;
import java.awt.event.KeyEvent;

import virtualslideviewer.ui.imageviewing.ImagePresentationModel;
import virtualslideviewer.ui.imageviewing.ImagePresentationModel.Listener;

public class ImageIndexControlPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final ImagePresentationModel mModel;
	
	private final JLabel     mChannelLabel;
	private final JScrollBar mChannelScrollBar;
	
	private final JLabel     mZPlaneLabel;
	private final JScrollBar mZPlaneScrollBar;
	
	private final JLabel     mTimePointLabel;
	private final JScrollBar mTimePointScrollBar;

	public ImageIndexControlPanel(ImagePresentationModel model)
	{
		super(new GridBagLayout());
		
		if(model == null)
			throw new IllegalArgumentException("model cannot be null.");
		
		mChannelLabel       = createLabel(0);
		mChannelScrollBar   = createScrollBar(0);
		
		mZPlaneLabel        = createLabel(1);
		mZPlaneScrollBar    = createScrollBar(1);
		
		mTimePointLabel     = createLabel(2);
		mTimePointScrollBar = createScrollBar(2);
		
		mModel = model;
		
		mModel.addListener(new Listener()
		{
			@Override
			public void onVisibleImageContentUpdate() { }
			
			@Override
			public void onImageChange()
			{
				refreshInfo();
			}
		});
		
		mChannelScrollBar.addAdjustmentListener(e ->
		{
			mModel.setCurrentChannel(e.getValue());
			refreshInfo();
		});
		
		mZPlaneScrollBar.addAdjustmentListener(e ->
		{
			mModel.setCurrentZPlane(e.getValue());
			refreshInfo();
		});	
		
		mTimePointScrollBar.addAdjustmentListener(e ->
		{
			mModel.setCurrentTimePoint(e.getValue());
			refreshInfo();
		});
		
		setHotKeys();
	}
	
	private JLabel createLabel(int row)
	{
		JLabel label = new JLabel();
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.insets = new Insets(0, 0, 5, 5);
		labelConstraints.gridx = 0;
		labelConstraints.gridy = row;
		super.add(label, labelConstraints);
		
		return label;
	}
	
	private JScrollBar createScrollBar(int row)
	{
		JScrollBar scrollBar = new JScrollBar();
		scrollBar.setMinimum(0);
		scrollBar.setVisibleAmount(1);
		scrollBar.setBlockIncrement(1);
		scrollBar.setOrientation(JScrollBar.HORIZONTAL);
		
		GridBagConstraints scrollBarContraints = new GridBagConstraints();
		scrollBarContraints.fill    = GridBagConstraints.HORIZONTAL;
		scrollBarContraints.weightx = 1.0;
		scrollBarContraints.gridx   = 1;
		scrollBarContraints.gridy   = row;
		super.add(scrollBar, scrollBarContraints);
		
		return scrollBar;
	}
	
	private void setHotKeys()
	{
		InputMap inputMap = mChannelScrollBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "negativeUnitIncrement");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "positiveUnitIncrement");		
		
		inputMap = mZPlaneScrollBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "negativeUnitIncrement");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "positiveUnitIncrement");		
		
		inputMap = mTimePointScrollBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "negativeUnitIncrement");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "positiveUnitIncrement");		
	}
	
	private void refreshInfo()
	{
		int channelsCount = mModel.getChannelCount();
		mChannelLabel.setVisible(channelsCount > 1);
		mChannelScrollBar.setVisible(channelsCount > 1);
		
		mChannelLabel.setText(String.format("Channel: %s / %s", mModel.getCurrentChannel() + 1, channelsCount));
		mChannelScrollBar.setValue(mModel.getCurrentChannel());
		mChannelScrollBar.setMaximum(channelsCount);
		
		int zPlanesCount = mModel.getZPlanesCount();
		mZPlaneLabel.setVisible(zPlanesCount > 1);
		mZPlaneScrollBar.setVisible(zPlanesCount > 1);

		mZPlaneLabel.setText(String.format("Z plane: %s / %s", mModel.getCurrentZPlane() + 1, zPlanesCount));
		mZPlaneScrollBar.setValue(mModel.getCurrentZPlane());
		mZPlaneScrollBar.setMaximum(zPlanesCount);
		
		int timePointsCount = mModel.getTimePointCount();
		mTimePointScrollBar.setVisible(timePointsCount > 1);
		mTimePointLabel.setVisible(timePointsCount > 1);
		
		mTimePointLabel.setText(String.format("Time point: %s / %s", mModel.getCurrentTimePoint() + 1, timePointsCount));
		mTimePointScrollBar.setValue(mModel.getCurrentTimePoint());
		mTimePointScrollBar.setMaximum(timePointsCount);
	}
}
