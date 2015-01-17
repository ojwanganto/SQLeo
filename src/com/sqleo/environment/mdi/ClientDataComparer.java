/*
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
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

package com.sqleo.environment.mdi;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.util.DataComparerConfig;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.DataComparer;
import com.sqleo.environment.io.FileHelper;


public class ClientDataComparer extends MDIClient
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6741254332433687641L;

	public static final String DEFAULT_TITLE = "Data comparer";
	
	private DataComparer control;
	private JMenuItem[] m_actions;
	private Toolbar toolbar;
	private static final FileFilter setUpFileFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory()
					|| file.getName().endsWith(".xml");
		}

		@Override
		public String getDescription() {
			return "Datacomparer setup files (*.xml)";
		}
	};
	
	ClientDataComparer()
	{
		super(DEFAULT_TITLE);
		
		setComponentCenter(control = new DataComparer());
		control.setBorder(new EmptyBorder(2,2,2,2));

		initMenuActions();
		
		toolbar = new Toolbar(Toolbar.HORIZONTAL);
		toolbar.add(new ActionOpen());
		Action saveAction = new ActionSave();
		toolbar.getActionMap().put("save",saveAction);
		toolbar.add(saveAction);

	}
	
	private class ActionOpen extends MDIActions.AbstractBase {
		private ActionOpen() {
			setIcon(Application.ICON_EDITOR_OPEN);
			setTooltip("Load setup");
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			final String currentDirectory = Preferences.getString("lastDirectory",
					System.getProperty("user.home"));

			final JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			fc.setFileFilter(setUpFileFilter);

			if (fc.showOpenDialog(Application.window) == JFileChooser.APPROVE_OPTION) {
				Preferences.set("lastDirectory", fc.getCurrentDirectory()
						.toString());
				final File file = fc.getSelectedFile();
				final DataComparerConfig setup = FileHelper.loadXml(file, DataComparerConfig.class);
				ClientDataComparer.this.control.loadSetup(setup);
				Application.alertInfo("Datacomparer setup reloaded succesfully.");
			}
		}
	}

	private class ActionSave extends MDIActions.AbstractBase {
		private ActionSave() {
			setIcon(Application.ICON_EDITOR_SAVE);
			setTooltip("Save setup");
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			final String currentDirectory = Preferences.getString("lastDirectory",
					System.getProperty("user.home"));
			final JFileChooser fc = new JFileChooser(currentDirectory);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setFileFilter(setUpFileFilter);

			if (fc.showSaveDialog(Application.window) == JFileChooser.APPROVE_OPTION) {
				Preferences.set("lastDirectory", fc.getCurrentDirectory()
						.toString());
				String filename = fc.getSelectedFile().toString();
				if (fc.getFileFilter().getDescription().endsWith("(*.xml)")) {
					if (!filename.endsWith(".xml")) {
						filename += ".xml";
					}
				}
				final DataComparerConfig setup = 
					ClientDataComparer.this.control.getSetup();
				FileHelper.saveAsXml(filename,setup);
				Application.alertInfo("Datacomparer setup saved succesfully.");
			}
		}
	}
	
	private void initMenuActions()
	{
		m_actions = new JMenuItem[]
		{
			new JMenuItem("<empty>")
		};
		m_actions[0].setEnabled(false);
	}
	
	
	public JMenuItem[] getMenuActions()
	{
		return m_actions;
	}

	public Toolbar getSubToolbar()
	{
		return toolbar;
	}
    
	public final String getName()
	{
		return DEFAULT_TITLE;
	}

	protected void setPreferences()
	{
	}
	

}
