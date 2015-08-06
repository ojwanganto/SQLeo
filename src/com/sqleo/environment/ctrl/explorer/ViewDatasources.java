/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.gui.ListView;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.Classpath;
//import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;


public class ViewDatasources extends ListView
{
	private InstallBar installBar;
	private SideNavigator navigator;
	
	public ViewDatasources(SideNavigator navigator)
	{
		this.navigator = navigator;
		
// this breaks auto-connect
//		addColumn(I18n.getString("datasource.message.name","name"));
//		addColumn(I18n.getString("datasource.message.url","url"));
//		addColumn(I18n.getString("datasource.message.status","status"));
//		addColumn(I18n.getString("datasource.message.joinFile","join definition file"));
		addColumn("name");
		addColumn("url");
		addColumn("status");
		addColumn("join definition file");
		
		setComponentSouth(installBar = new InstallBar());
	}
	
	public void list(DefaultMutableTreeNode node)
	{
		UoDriver uoDv = (UoDriver)node.getUserObject();
		installBar.setVisible(uoDv.message != null);
		installBar.message.setText(uoDv.message);
		
		removeAllRows();
		for(Enumeration e = node.children(); e.hasMoreElements();)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
			UoDatasource uoDs = (UoDatasource)child.getUserObject();
			
			Object[] rowdata = new Object[4];
			rowdata[0] = uoDs.name;
			rowdata[1] = uoDs.url;
			rowdata[2] = uoDs.isConnected() ? "connected" : "disconnected";
			rowdata[3] = uoDs.selectedFkDefFileName!=null?uoDs.selectedFkDefFileName:"";
			
			addRow(rowdata);
		}
	}
	
	private class InstallBar extends BorderLayoutPanel implements ActionListener
	{
		JLabel message;
		
		InstallBar()
		{
			message = new JLabel(Application.resources.getIcon(Application.ICON_EXPLORER_DRIVER_KO));
			setComponentCenter(message);
			setComponentEast(new CommandButton("install",this));
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String currentDirectory = Preferences.getString("lastDirectory",System.getProperty("user.home"));
			
			JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
		
			fc.setFileFilter(new FileFilter()
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

			if(fc.showOpenDialog(Application.window) == JFileChooser.APPROVE_OPTION)
			{
				Preferences.set("lastDirectory",fc.getCurrentDirectory().toString());
				
				UoDriver uoDv = (UoDriver)ViewDatasources.this.navigator.getSelectionNode().getUserObject();
				try
				{
					String file = fc.getSelectedFile().toString();					
					ConnectionAssistant.declare(file,uoDv.classname,!Classpath.isRuntime(file));
					
					uoDv.library = file;
					uoDv.message = null;
					
					ViewDatasources.this.navigator.reloadSelection();
				}
				catch(Exception e)
				{
					Application.alert(Application.PROGRAM,"Installation failed!");
					uoDv.message = e.getMessage();
				}				
			}
		}
	}
}
