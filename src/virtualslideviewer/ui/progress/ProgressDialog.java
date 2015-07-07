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

package virtualslideviewer.ui.progress;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.List;

public class ProgressDialog extends JDialog implements ProgressView
{
	private static final long serialVersionUID = 1L;

	private static final int PROGRESS_BAR_HEIGHT = 25;
	
	private final Component mParent;
	private JLabel          mLabel;
	private JProgressBar    mProgressBar;
	private JButton         mCancelButton;

	private final List<CancelingListener> mCancelingListeners = new ArrayList<>();
	
	public ProgressDialog(Component parent)
	{
		mParent = parent;
		
		super.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		super.setType(Type.NORMAL);
		super.setModalityType(ModalityType.APPLICATION_MODAL);
		super.setResizable(false);
		
		createLabel();
		createProgressBar();
		createCancelButton();
		
		layoutComponents();
		
		super.setLocationRelativeTo(mParent);
	}
	
	private void createLabel()
	{
		mLabel = new JLabel();
		mLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}	
	
	private void createProgressBar()
	{
		mProgressBar = new JProgressBar();
		mProgressBar.setIndeterminate(true);
		mProgressBar.setMaximum(100000);
		mProgressBar.setPreferredSize(new Dimension(0, PROGRESS_BAR_HEIGHT));
	}	
	
	private void createCancelButton()
	{
		mCancelButton = new JButton("Cancel");
		
		mCancelButton.addActionListener(e ->
		{
			mCancelingListeners.forEach(l -> l.onCanceled());
			super.setVisible(false);
		});
	}
	
	private void layoutComponents()
	{
		BorderLayout borderLayout = new BorderLayout();
		getContentPane().setLayout(borderLayout);
		
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		GridBagConstraints labelConstraints = new GridBagConstraints();
		labelConstraints.gridx   = 0;
		labelConstraints.gridy   = 0;
		labelConstraints.weightx = 1.0;
		labelConstraints.fill    = GridBagConstraints.HORIZONTAL;
		labelConstraints.insets  = new Insets(0, 10, 5, 10);
		contentPanel.add(mLabel, labelConstraints);

		GridBagConstraints progressBarConstraints = new GridBagConstraints();
		progressBarConstraints.gridx   = 0;
		progressBarConstraints.gridy   = 1;
		progressBarConstraints.weightx = 1.0;
		progressBarConstraints.fill    = GridBagConstraints.HORIZONTAL;
		progressBarConstraints.insets  = new Insets(0, 10, 0, 10);
		contentPanel.add(mProgressBar, progressBarConstraints);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPane.add(mCancelButton);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		super.pack();
	}

	@Override
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		
		if(e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			mCancelingListeners.forEach(l -> l.onCanceled());
			super.setVisible(false);
		}
	}
	
	@Override
	public void addCancelListener(CancelingListener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		
		mCancelingListeners.add(listener);
	}
	
	public void setDescription(String description)
	{
		if(description == null)
			throw new IllegalArgumentException("description cannot be null.");
		
		mLabel.setText(description);
		
		super.pack();
		super.setLocationRelativeTo(mParent);
	}
	
	protected void setProgress(double progress)
	{
		mProgressBar.setIndeterminate(false);
		mProgressBar.setValue((int)(progress * mProgressBar.getMaximum()));		
	}
}
