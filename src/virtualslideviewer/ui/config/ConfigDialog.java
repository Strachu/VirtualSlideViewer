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

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.text.DecimalFormat;

import virtualslideviewer.config.ApplicationConfiguration;
import virtualslideviewer.util.ImageUtil;

public class ConfigDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private static final String RESOLUTION_THRESHOLD_TOOLTIP = "<html>"
	                                                         + "A threshold controlling how early the application starts to retrieve pixels"
	                                                         + "from higher resolution when zoomed between resolutions.<br>"
	                                                         + "The lower the value, the better is the quality, but it also greatly "
	                                                         + "lowers the application's performance.</html>";
	
	private static final double MIN_ZOOM_INCREMENT = 1.01;
	private static final double MAX_ZOOM_INCREMENT = 5.00;
	
	private final ApplicationConfiguration mConfiguration;
	
	private JSpinner            mZoomIncrementSpinner;
	private JSlider             mResolutionThresholdSlider;
	private JFormattedTextField mResolutionThresholdTextField;
	private JButton             mOKButton;
	private JButton             mCancelButton;

	public ConfigDialog(Component parent, ApplicationConfiguration configuration)
	{
		if(configuration == null)
			throw new IllegalArgumentException("configuration cannot be null.");
		
		mConfiguration = configuration;
		
		super.setTitle("Preferences");
		super.setType(Type.NORMAL);
		super.setModalityType(ModalityType.APPLICATION_MODAL);
				
		ToolTipManager.sharedInstance().setDismissDelay(15000);

		createZoomIncrementSpinner();
		createResolutionThresholdSlider();
		createResolutionThresholdTextField();
		createOkButton();
		createCancelButton();
		
		layoutComponents();
		
		centerOnParent(parent);
	}

	private void centerOnParent(Component parent)
	{
		if(parent != null)
		{
			Point positionRelativeToParent = ImageUtil.getCenteredPosition(super.getSize(), parent.getSize());
			Point positionRelativeToScreen = new Point(positionRelativeToParent.x + parent.getLocationOnScreen().x,
			                                           positionRelativeToParent.y + parent.getLocationOnScreen().y);
			
			super.setLocation(positionRelativeToScreen);
		}
	}
	
	private void createZoomIncrementSpinner()
	{
		mZoomIncrementSpinner = new JSpinner();
		mZoomIncrementSpinner.setToolTipText("A multiplier to be used when zooming");
		mZoomIncrementSpinner.setPreferredSize(new Dimension(100, 20));
		mZoomIncrementSpinner.setMinimumSize(mZoomIncrementSpinner.getPreferredSize());

		mZoomIncrementSpinner.setModel(new ClampingSpinnerDoubleModel(mConfiguration.getZoomIncrement(), MIN_ZOOM_INCREMENT, MAX_ZOOM_INCREMENT, 0.01));
	}

	private void createResolutionThresholdSlider()
	{	
		mResolutionThresholdSlider = new JSlider();
		mResolutionThresholdSlider.setToolTipText(RESOLUTION_THRESHOLD_TOOLTIP);
		mResolutionThresholdSlider.setMinimum(0);
		mResolutionThresholdSlider.setValue((int)(mConfiguration.getResolutionTransitionThreshold() * 100));
		mResolutionThresholdSlider.setMaximum(100);
		
		mResolutionThresholdSlider.addChangeListener(e ->
		{
			mResolutionThresholdTextField.setValue(mResolutionThresholdSlider.getValue() * 0.01);
		});
	}
	
	private void createResolutionThresholdTextField()
	{
		mResolutionThresholdTextField = new JFormattedTextField(new DecimalFormat("##0.0%"));
		mResolutionThresholdTextField.setToolTipText(RESOLUTION_THRESHOLD_TOOLTIP);
		mResolutionThresholdTextField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
		mResolutionThresholdTextField.setHorizontalAlignment(SwingConstants.CENTER);
		mResolutionThresholdTextField.setColumns(6);
		mResolutionThresholdTextField.setValue(mConfiguration.getResolutionTransitionThreshold());
		mResolutionThresholdTextField.setMinimumSize(mResolutionThresholdTextField.getPreferredSize());

		mResolutionThresholdTextField.addPropertyChangeListener("value", e ->
		{
			double textFieldValue = ((Number)e.getNewValue()).doubleValue();
			if(textFieldValue < 0.0 || textFieldValue > 1.0)
			{
				mResolutionThresholdTextField.setValue(Math.min(Math.max(textFieldValue, 0.0), 1.0));
				return; // setValue will invoke next change event, and slider will be update there
			}
			
			mResolutionThresholdSlider.setValue((int)(textFieldValue * 100));
		});
	}
	
	private void createOkButton()
	{
		mOKButton = new JButton("OK");
		
		mOKButton.addActionListener(x ->
		{
			Number zoomIncrement       = (Number)mZoomIncrementSpinner.getValue();
			Number resolutionThreshold = (Number)mResolutionThresholdTextField.getValue();
			
			mConfiguration.setZoomIncrement(zoomIncrement.doubleValue());
			mConfiguration.setResolutionTransitionThreshold(resolutionThreshold.doubleValue());
			
			super.setVisible(false);
		});
	}
	
	private void createCancelButton()
	{
		mCancelButton = new JButton("Cancel");
		
		mCancelButton.addActionListener(x -> super.setVisible(false));
	}
	
	private void layoutComponents()
	{
		getContentPane().setLayout(new BorderLayout());
		
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		layoutZoomIncrementOption(contentPanel);
		layoutResolutionThresholdOption(contentPanel);
		layoutButtons();
		
		super.pack();
		super.setMinimumSize(super.getSize());
	}

	private void layoutZoomIncrementOption(JPanel mainPanel)
	{	
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Zoom increment"));
		
		GridBagConstraints spinnerContraints = new GridBagConstraints();
		spinnerContraints.gridx  = 0;
		spinnerContraints.gridy  = 0;
		spinnerContraints.anchor = GridBagConstraints.WEST;
		spinnerContraints.insets = new Insets(0, 5, 0, 5);
		panel.add(mZoomIncrementSpinner, spinnerContraints);
		
		GridBagConstraints glueContraints = new GridBagConstraints();
		glueContraints.gridx   = 1;
		glueContraints.gridy   = 0;
		glueContraints.weighty = 1.0;
		glueContraints.weightx = 1.0;
		panel.add(Box.createGlue(), glueContraints);
		
		GridBagConstraints panelContraints = new GridBagConstraints();
		panelContraints.gridx   = 0;
		panelContraints.gridy   = 0;
		panelContraints.weighty = 1.0;
		panelContraints.weightx = 1.0;
		panelContraints.insets  = new Insets(5, 0, 5, 0);	
		panelContraints.fill    = GridBagConstraints.BOTH;
		
		mainPanel.add(panel, panelContraints);
	}
	
	private void layoutResolutionThresholdOption(JPanel mainPanel)
	{		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(new TitledBorder("Resolution transition threshold"));
		panel.setToolTipText(RESOLUTION_THRESHOLD_TOOLTIP);
			
		GridBagConstraints sliderContraints = new GridBagConstraints();
		sliderContraints.gridx   = 1;
		sliderContraints.gridy   = 0;
		sliderContraints.weightx = 1.0;
		sliderContraints.insets  = new Insets(0, 0, 0, 5);
		sliderContraints.fill    = GridBagConstraints.HORIZONTAL;
		panel.add(mResolutionThresholdSlider, sliderContraints);
		
		GridBagConstraints textFieldConstraints = new GridBagConstraints();
		textFieldConstraints.gridx = 2;
		textFieldConstraints.gridy = 0;
		textFieldConstraints.anchor = GridBagConstraints.NORTHWEST;
		panel.add(mResolutionThresholdTextField, textFieldConstraints);
		
		GridBagConstraints panelContraints = new GridBagConstraints();
		panelContraints.gridx   = 0;
		panelContraints.gridy   = 1;
		panelContraints.weighty = 1.0;
		panelContraints.weightx = 1.0;
		panelContraints.insets  = new Insets(0, 0, 3, 0);
		panelContraints.fill    = GridBagConstraints.BOTH;
		mainPanel.add(panel, panelContraints);
	}
	
	private void layoutButtons()
	{
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		buttonPane.add(mOKButton);
		buttonPane.add(mCancelButton);
		
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
	}
}
