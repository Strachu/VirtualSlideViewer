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

package virtualslideviewer.ui.about;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;

public class AboutDialog extends JDialog implements AboutView
{
	private static final long serialVersionUID = 1L;
	
	private Component   mParent;
	private JLabel      mApplicationNameLabel;
	private JLabel      mCopyrightsLabel;
	private JLabel      mApplicationDescriptionLabel;
	private JScrollPane mCreditsPanel;
	private JLabel      mApplicationWebsiteLabel;
	private JButton     mOkButton;
	
	public AboutDialog(Component parent)
	{
		mParent = parent;
		
		super.setTitle("About");
		super.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		super.setType(Type.NORMAL);
		super.setModalityType(ModalityType.APPLICATION_MODAL);
		super.setResizable(false);
		
		createApplicationNameLabel();
		createCopyrightLabel();
		createApplicationDescriptionLabel();
		createCreditsPanel();
		createWebsiteLabel();
		createOkButton();
		
		layoutComponents();
	}
	
	private void createApplicationNameLabel()
	{
		mApplicationNameLabel = new JLabel("Virtual Slide Viewer 1.0.0");
		mApplicationNameLabel.setFont(new Font("Dialog", Font.BOLD, 18));
		mApplicationNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mApplicationNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}	
	
	private void createCopyrightLabel()
	{
		mCopyrightsLabel = new JLabel("Copyright © 2015 Patryk Strach");
		mCopyrightsLabel.setFont(new Font("Dialog", Font.BOLD, 11));
		mCopyrightsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mCopyrightsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}	
	
	private void createApplicationDescriptionLabel()
	{
		mApplicationDescriptionLabel = new JLabel("<html><div style=text-align:center>"
		                                        + "Virtual Slide Viewer is an application allowing for a viewing of<br/>"
		                                        + "a virtual slide saved in any of more than 100 supported formats."
		                                        + "</div></html>");
		
		mApplicationDescriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mApplicationDescriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}
	
	private void createCreditsPanel()
	{
		JEditorPane	creditsView = new JEditorPane();
		creditsView.setEditable(false);
		
		try
		{
			URL creditsFile = getClass().getResource("Credits.html");
			
			creditsView.setPage(creditsFile);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(this, "Failed to load credits file.");
		}
		
		creditsView.addHyperlinkListener(e ->
		{
			if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
			{
				openBrowser(e.getURL());
			}
		});
		
		mCreditsPanel = new JScrollPane();
		mCreditsPanel.setBorder(new TitledBorder("Credits"));
		mCreditsPanel.setViewportView(creditsView);
		mCreditsPanel.setPreferredSize(new Dimension(mApplicationDescriptionLabel.getPreferredSize().width, 200));
	}
	
	private void openBrowser(URL url)
	{
		try
		{
			Desktop.getDesktop().browse(url.toURI());
		}
		catch(IOException | URISyntaxException ex)
		{
			ex.printStackTrace();
		}		
	}
	
	private void createWebsiteLabel()
	{
		mApplicationWebsiteLabel = new JLabel("<html><a href=\"\">Application website</a></html>");
		mApplicationWebsiteLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mApplicationWebsiteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		mApplicationWebsiteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					openBrowser(new URL("http://github.com/Strachu/VirtualSlideViewer"));
				}
				catch(MalformedURLException ex)
				{
					ex.printStackTrace();
				}
			}
		});
	}
	
	private void createOkButton()
	{
		mOkButton = new JButton("OK");
		mOkButton.setActionCommand("OK");
		
		mOkButton.addActionListener(e ->
		{
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
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 0;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, 5, 0, 5);
		contentPanel.add(mApplicationNameLabel, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 1;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, 5, 5, 5);
		contentPanel.add(mCopyrightsLabel, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 2;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, 5, 5, 5);
		contentPanel.add(mApplicationDescriptionLabel, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 3;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, 5, 5, 5);
		contentPanel.add(mCreditsPanel, constraints);
		
		JPanel bottomPane = new JPanel(new GridBagLayout());
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 4;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, 5, 0, 5);
		contentPanel.add(bottomPane, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 0;
		constraints.gridy   = 0;
		constraints.weightx = 1.0;
		constraints.fill    = GridBagConstraints.HORIZONTAL;
		constraints.insets  = new Insets(0, mOkButton.getPreferredSize().width, 0, 0);
		bottomPane.add(mApplicationWebsiteLabel, constraints);
		
		constraints = new GridBagConstraints();
		constraints.gridx   = 1;
		constraints.gridy   = 0;
		constraints.weightx = 0.0;
		constraints.fill    = GridBagConstraints.NONE;
		constraints.anchor  = GridBagConstraints.EAST;
		bottomPane.add(mOkButton, constraints);
		
		super.pack();
	}
	
	@Override
	public void show()
	{
		super.setLocationRelativeTo(mParent);
		
		super.show();
	}
}
