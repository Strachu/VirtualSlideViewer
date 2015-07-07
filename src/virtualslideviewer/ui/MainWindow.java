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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import virtualslideviewer.config.ApplicationConfiguration;
import virtualslideviewer.ui.utils.Region;
import virtualslideviewer.ui.utils.SwingRegion;

public class MainWindow extends JFrame implements MainView
{
	private static final long serialVersionUID = 1L;
	
	private JMenuItem            mUndoMenu;
	private JMenuItem            mRedoMenu;

	private SwingRegion          mMainRegion;
	
	private JPanel               mStatusBarPanel;
	private JLabel               mStatusLabel;
	
	private final List<Listener> mListeners = new ArrayList<>();
	
	public MainWindow(JMenu fileMenu)
	{
		if(fileMenu == null)
			throw new IllegalArgumentException("fileMenu cannot be null.");
		
		super.setTitle("Virtual Slide Viewer");
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		initializeMenu(fileMenu);
		
		initializeMainRegion();
		
		createStatusBar();
		
		layoutComponents();
	}
	
	private void initializeMenu(JMenu fileMenu)
	{
		JMenuBar menuBar = new JMenuBar();
		
		menuBar.add(fileMenu);
		
		initializeEditMenu(menuBar);
		initializeHelpMenu(menuBar);
		
		super.setJMenuBar(menuBar);
	}
	
	private void initializeEditMenu(JMenuBar menuBar)
	{		
		JMenu editMenu = new JMenu("Edit");
		
		mUndoMenu = new JMenuItem("Undo");
		mRedoMenu = new JMenuItem("Redo");
		
		mUndoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		mRedoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		
		mUndoMenu.addActionListener((x) -> mListeners.forEach(l -> l.onUndo()));
		mRedoMenu.addActionListener((x) -> mListeners.forEach(l -> l.onRedo()));
		
		editMenu.add(mUndoMenu);
		editMenu.add(mRedoMenu);
		
		editMenu.addSeparator();
		
		JMenuItem preferencesMenu = new JMenuItem("Preferences");
		
		preferencesMenu.addActionListener(x -> mListeners.forEach(l -> l.onOpenPreferences()));
		
		editMenu.add(preferencesMenu);
		
		menuBar.add(editMenu);
	}
	
	private void initializeHelpMenu(JMenuBar menuBar)
	{		
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem aboutMenu = new JMenuItem("About");
		
		aboutMenu.addActionListener((x) -> mListeners.forEach(l -> l.onAboutClick()));
		
		helpMenu.add(aboutMenu);
		
		menuBar.add(helpMenu);
	}
	
	private void initializeMainRegion()
	{
		mMainRegion = new SwingRegion();
		
		mMainRegion.changeView(new NoSlideLoadedMainPanelPlaceholder());
	}
	
	private void createStatusBar()
	{
		mStatusBarPanel = new JPanel();
		mStatusBarPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		mStatusBarPanel.setLayout(new BoxLayout(mStatusBarPanel, BoxLayout.X_AXIS));
		mStatusBarPanel.setVisible(false);
		
		mStatusLabel = new JLabel();
		mStatusLabel.setFont(new Font("Dialog", Font.PLAIN, 11));
		mStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		mStatusBarPanel.add(mStatusLabel);	
	}
	
	private void layoutComponents()
	{
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(3, 3, 3, 3));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		contentPane.add(mMainRegion);
		
		contentPane.add(mStatusBarPanel, BorderLayout.SOUTH);
	}
	
	@Override
	public void setStatusMessage(String message)
	{
		if(message == null)
			throw new IllegalArgumentException("message cannot be null.");
		
		mStatusBarPanel.setVisible(true);
		
		mStatusLabel.setText(message);
	}

	@Override
	public void addListener(Listener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		
		mListeners.add(listener);
	}

	@Override
	public void setUndoEnabled(boolean enable)
	{
		mUndoMenu.setEnabled(enable);
	}

	@Override
	public void setRedoEnabled(boolean enable)
	{
		mRedoMenu.setEnabled(enable);
	}
	
	@Override
	public void displayErrorMessage(String message)
	{
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	@Override
	public void restoreLayoutFromConfiguration(ApplicationConfiguration configuration)
	{
		if(configuration == null)
			throw new IllegalArgumentException("configuration cannot be null.");
		
		super.setBounds(configuration.getWindowBounds());
	}

	@Override
	public void saveLayoutToConfiguration(ApplicationConfiguration configuration)
	{
		if(configuration == null)
			throw new IllegalArgumentException("configuration cannot be null.");
		
		configuration.setWindowBounds(super.getBounds());
	}
	
	public Region getMainRegion()
	{
		return mMainRegion;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void show()
	{
		EventQueue.invokeLater(() -> 
		{
			// Cannot invoke setVisible(true) due to a conflict between JFrame.show() and MainView.show().
			// Invoking setVisible(true) will internally invoke MainWindow.show() instead of JFrame.show(), which is not what we want.
			// An explicit interface implementation construct from a C# would help here, but Java does not support it.
			super.show();
		});
	}

	@Override
	protected void processWindowEvent(WindowEvent e)
	{
		if(e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			mListeners.forEach(l -> l.onViewClosing());
		}
		
		super.processWindowEvent(e);
	}
}
