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

package virtualslideviewer.ui;

import java.awt.*;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import virtualslideviewer.ui.imageviewing.ImagePresentationModel;
import virtualslideviewer.ui.imageviewing.view.ImageIndexControlPanel;
import virtualslideviewer.ui.imageviewing.view.ImagePreviewPanel;
import virtualslideviewer.ui.imageviewing.view.ImageRenderingPanel;

public class MainPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JComponent             mImageIndexControlPanel;
	private final JComponent             mImageRenderingPanel;
	private final JComponent             mImageListPanel;

	private final ImagePreviewPanel      mImagePreviewPanel;
	private JSlider	                   mZoomSlider;
	private JFormattedTextField          mZoomTextField;
	
	private boolean                      mZoomSliderSetProgrammatically = false;

	private final ImagePresentationModel mImagePresentationModel;
	
	public MainPanel(JComponent imageListPanel, ImagePresentationModel imagePresentationModel)
	{
		mImagePresentationModel = imagePresentationModel;
		
		mImageIndexControlPanel = new ImageIndexControlPanel(mImagePresentationModel);
		mImageRenderingPanel    = new ImageRenderingPanel(mImagePresentationModel);
		mImageListPanel         = imageListPanel;
		mImagePreviewPanel      = new ImagePreviewPanel(mImagePresentationModel);		
		
		createZoomSlider();
		createZoomTextField();
		
		layoutComponents();
	}

	private void createZoomSlider()
	{
		mZoomSlider = new JSlider();
		mZoomSlider.setMinimum(1);
		mZoomSlider.setMaximum(4000);
		
		mImagePresentationModel.addListener(new ImagePresentationModel.Listener()
		{
			@Override
			public void onVisibleImageContentUpdate()
			{
				mZoomSliderSetProgrammatically = true;
				
				mZoomSlider.setValue(percentToZoomLevel(mImagePresentationModel.getZoom()));
			}
			
			@Override
			public void onImageChange() { onVisibleImageContentUpdate(); }
		});

		mZoomSlider.addChangeListener(x ->
		{
			if(mZoomSliderSetProgrammatically)
			{
				mZoomSliderSetProgrammatically = false;
				return;
			}
			
			mImagePresentationModel.setZoom(zoomLevelToPercent(mZoomSlider.getValue()));
		});
	}
	
	private void createZoomTextField()
	{
		mZoomTextField = new JFormattedTextField(new DecimalFormat("##0.00%"));
		mZoomTextField.setFocusLostBehavior(JFormattedTextField.REVERT);
		mZoomTextField.setHorizontalAlignment(SwingConstants.CENTER);
		mZoomTextField.setColumns(6);
		mZoomTextField.setMinimumSize(mZoomTextField.getPreferredSize());

		mZoomTextField.addActionListener((x) -> mImagePresentationModel.setZoom(((Number)mZoomTextField.getValue()).doubleValue()));
		
		mImagePresentationModel.addListener(new ImagePresentationModel.Listener()
		{
			@Override
			public void onVisibleImageContentUpdate()
			{
				mZoomTextField.setValue(mImagePresentationModel.getZoom());
			}
			
			@Override
			public void onImageChange() { onVisibleImageContentUpdate(); }
		});
	}
	
	private double zoomLevelToPercent(int zoomLevel)
	{
		double minZoomPercent  = mImagePresentationModel.getMinZoom();
		double zoomIncrement   = computeZoomIncrementToMakeExactlyNIncrements();
		
		return minZoomPercent * Math.pow(zoomIncrement, zoomLevel - 1);
	}
	
	private int percentToZoomLevel(double percent)
	{
		double minZoomPercent  = mImagePresentationModel.getMinZoom();
		double zoomIncrement   = computeZoomIncrementToMakeExactlyNIncrements();
		
		return (int)Math.round((Math.log(percent / minZoomPercent) / Math.log(zoomIncrement)) + 1);
	}
		
	private double computeZoomIncrementToMakeExactlyNIncrements()
	{
		double minZoomPercent  = mImagePresentationModel.getMinZoom();
		double maxZoomPercent  = mImagePresentationModel.getMaxZoom();
		double incrementsCount = mZoomSlider.getMaximum();

		return Math.pow(maxZoomPercent / minZoomPercent, 1.0 / (incrementsCount - 1));
	}
	
	private void layoutComponents()
	{
		super.setLayout(new BorderLayout(0, 0));
		
		JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setResizeWeight(1.0);
		mainSplitPane.setOneTouchExpandable(true);
		
		layoutLeftPart(mainSplitPane);
		layoutRightPart(mainSplitPane);
		
		super.add(mainSplitPane);
	}

	private void layoutLeftPart(JSplitPane mainSplitPane)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints imageIndexControlPanelConstraints = new GridBagConstraints();
		imageIndexControlPanelConstraints.gridx   = 0;
		imageIndexControlPanelConstraints.gridy   = 0;
		imageIndexControlPanelConstraints.weightx = 1.0;
		imageIndexControlPanelConstraints.fill    = GridBagConstraints.HORIZONTAL;
		panel.add(mImageIndexControlPanel, imageIndexControlPanelConstraints);
		
		GridBagConstraints imageRenderingPanelConstraints = new GridBagConstraints();
		imageRenderingPanelConstraints.gridx   = 0;
		imageRenderingPanelConstraints.gridy   = 1;
		imageRenderingPanelConstraints.weightx = 1.0;
		imageRenderingPanelConstraints.weighty = 1.0;
		imageRenderingPanelConstraints.fill    = GridBagConstraints.BOTH;
		panel.add(mImageRenderingPanel, imageRenderingPanelConstraints);
		
		mainSplitPane.setLeftComponent(panel);
	}
	
	private void layoutRightPart(JSplitPane mainSplitPane)
	{
		JSplitPane rightSplitPanel = new JSplitPane();
		rightSplitPanel.setResizeWeight(0.4);
		rightSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		layoutTopRightPart(rightSplitPanel);
		layoutBottomRightPart(rightSplitPanel);
		
		mainSplitPane.setRightComponent(rightSplitPanel);
	}

	private void layoutTopRightPart(JSplitPane rightSplitPanel)
	{
		JPanel mainPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints previewPanelConstraints = new GridBagConstraints();
		previewPanelConstraints.gridx   = 0;
		previewPanelConstraints.gridy   = 0;
		previewPanelConstraints.weightx = 1.0;
		previewPanelConstraints.weighty = 1.0;
		previewPanelConstraints.fill    = GridBagConstraints.BOTH;
		mainPanel.add(mImagePreviewPanel, previewPanelConstraints);
		
		JPanel zoomBoxPanel = new JPanel(new GridBagLayout());
		zoomBoxPanel.setBorder(new TitledBorder("Zoom"));
		
		{
			GridBagConstraints zoomSlideConstraints = new GridBagConstraints();
			zoomSlideConstraints.gridx   = 0;
			zoomSlideConstraints.gridy   = 0;
			zoomSlideConstraints.weightx = 1.0;
			zoomSlideConstraints.fill    = GridBagConstraints.HORIZONTAL;
			zoomBoxPanel.add(mZoomSlider, zoomSlideConstraints);
			
			GridBagConstraints zoomTextFieldConstaints = new GridBagConstraints();
			zoomTextFieldConstaints.gridx   = 1;
			zoomTextFieldConstaints.gridy   = 0;
			zoomTextFieldConstaints.weightx = 0.0;
			zoomBoxPanel.add(mZoomTextField, zoomTextFieldConstaints);
		}
		
		GridBagConstraints zoomBoxPanelConstraints = new GridBagConstraints();
		zoomBoxPanelConstraints.gridx   = 0;
		zoomBoxPanelConstraints.gridy   = 1;
		zoomBoxPanelConstraints.weightx = 1.0;
		zoomBoxPanelConstraints.weighty = 0.0;
		zoomBoxPanelConstraints.fill    = GridBagConstraints.HORIZONTAL;
		mainPanel.add(zoomBoxPanel, zoomBoxPanelConstraints);
		
		rightSplitPanel.setTopComponent(mainPanel);
	}

	private void layoutBottomRightPart(JSplitPane rightSplitPanel)
	{
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		rightSplitPanel.setBottomComponent(tabbedPane);
		
		tabbedPane.addTab("Images", null, mImageListPanel, null);
	}
}
