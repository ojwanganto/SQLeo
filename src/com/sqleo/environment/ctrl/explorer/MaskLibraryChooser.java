/*
 * SQLeonardo :: java database frontend
 * Copyright (C) 2004 nickyb@users.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.sqleo.environment.ctrl.explorer;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.sqleo.environment.Preferences;


public class MaskLibraryChooser extends JFileChooser
{
	MaskLibraryChooser()
	{
		String currentDirectory = Preferences.getString("lastDirectory",System.getProperty("user.home"));
		setCurrentDirectory(new File(currentDirectory));
		
		setBorder(null);
		setAcceptAllFileFilterUsed(false);
		setControlButtonsAreShown(false);
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		setFileFilter(new FileFilter()
		{
			public boolean accept(File file)
			{
				return file.isDirectory() || file.getName().toLowerCase().endsWith(".jar") || file.getName().toLowerCase().endsWith(".zip");
			}
			public String getDescription()
			{
				return "library files (*.jar, *.zip)";
			}
		});
	}
	
	String getLibrary()
	{
		return isFileSelected() ? getSelectedFile().toString() : null;
	}
	
	public boolean isFileSelected()
	{
		return getSelectedFile()!=null && getSelectedFile().isFile();
	}
}
