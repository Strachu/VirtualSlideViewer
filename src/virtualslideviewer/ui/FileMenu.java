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

import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import virtualslideviewer.core.SupportedFormatDescription;
import virtualslideviewer.ui.utils.SupportedFormatFileFilter;

public class FileMenu extends JMenu implements FileMenuView
{
	private static final long serialVersionUID = 1L;
	
	private final JMenuItem      mSaveMenu;
	private final JMenuItem      mSaveAsMenu;
	
	private File                 mLastJFileChooserDirectory = null;
	
	private final List<Listener> mListeners = new ArrayList<>();
	
	public FileMenu()
	{
		super("File");
		
		JMenuItem openMenu = new JMenuItem("Open...");
		mSaveMenu          = new JMenuItem("Save");
		mSaveAsMenu        = new JMenuItem("Save as...");
		
		openMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mSaveMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		
		openMenu.addActionListener(   (x) -> mListeners.forEach(l -> l.onOpenFile()));
		mSaveMenu.addActionListener(  (x) -> mListeners.forEach(l -> l.onSaveFile()));
		mSaveAsMenu.addActionListener((x) -> mListeners.forEach(l -> l.onSaveFileAs()));
		
		super.add(openMenu);
		super.add(mSaveMenu);
		super.add(mSaveAsMenu);
	}

	@Override
	public void addListener(Listener listener)
	{
		if(listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		
		mListeners.add(listener);
	}
	
	@Override
	public void setSaveEnabled(boolean enable)
	{
		mSaveMenu.setEnabled(enable);
	}

	@Override
	public void setSaveAsEnabled(boolean enable)
	{
		mSaveAsMenu.setEnabled(enable);
	}
	
	@Override
	public String askForFileToOpen()
	{
		JFileChooser openDialog = new JFileChooser(mLastJFileChooserDirectory);
		
		int dialogReturnValue = openDialog.showOpenDialog(SwingUtilities.getWindowAncestor(this));
      if(dialogReturnValue != JFileChooser.APPROVE_OPTION)
      	return null;
      
      mLastJFileChooserDirectory = openDialog.getCurrentDirectory();
      
		return openDialog.getSelectedFile().getAbsolutePath();
	}
	
	@Override
	public String askForDestinationPath(List<SupportedFormatDescription> supportedFormats)
	{
		JFileChooser saveDialog = new JFileChooser(mLastJFileChooserDirectory);
		
		saveDialog.setAcceptAllFileFilterUsed(false);
	
		addFormatsFilter(saveDialog, supportedFormats);
		
		int dialogReturnValue = saveDialog.showSaveDialog(SwingUtilities.getWindowAncestor(this));
      if(dialogReturnValue != JFileChooser.APPROVE_OPTION)
      	return null;
      
      mLastJFileChooserDirectory = saveDialog.getCurrentDirectory();
      
		return saveDialog.getSelectedFile().getAbsolutePath();
	}
	
	private void addFormatsFilter(JFileChooser fileChooser, List<SupportedFormatDescription> formats)
	{
		for(SupportedFormatDescription format : formats)
		{
			fileChooser.addChoosableFileFilter(new SupportedFormatFileFilter(format));
		}
	}	
	
	@Override
	public void displayErrorMessage(String message)
	{
		Window mainWindow = SwingUtilities.getWindowAncestor(this);
		
		JOptionPane.showMessageDialog(mainWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
