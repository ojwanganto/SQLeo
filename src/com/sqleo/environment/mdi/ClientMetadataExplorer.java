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

package com.sqleo.environment.mdi;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.Toolbar;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.MetadataExplorer;
import com.sqleo.environment.ctrl.explorer.DialogDatasource;
import com.sqleo.environment.ctrl.explorer.DialogDriver;


public class ClientMetadataExplorer extends MDIClient
{
	public static final String DEFAULT_TITLE = "Metadata explorer";
	
	private MetadataExplorer control;
	private JMenuItem[] m_actions;
	private Toolbar toolbar;
	
	ClientMetadataExplorer()
	{
		super(DEFAULT_TITLE);
		setClosable(false);
		setComponentCenter(control = new MetadataExplorer());
		control.setBorder(new EmptyBorder(2,2,2,2));
		
		initMenuActions();
	}

	private void initMenuActions()
	{
		m_actions = new JMenuItem[]
		{
			MDIMenubar.createItem(control.getActionMap().get("choose-columns")),
			null,
			MDIMenubar.createItem(control.getActionMap().get("list-copy")),
			MDIMenubar.createItem(control.getActionMap().get("list-export")),
			null,
			MDIMenubar.createItem(control.getActionMap().get("list-refresh")),
		};
	}
	
	public final void dispose()
	{
		control.unloadNavigator();
		super.dispose();
	}

	public final MetadataExplorer getControl()
	{
		return control;
	}
	
	public final String getName()
	{
		return DEFAULT_TITLE;
	}

	public JMenuItem[] getMenuActions()
	{
		return m_actions;
	}

	public Toolbar getSubToolbar()
	{
		if(toolbar == null)
		{
			toolbar = new Toolbar(Toolbar.HORIZONTAL);
			toolbar.add(new ActionNewDriver());
			toolbar.add(new ActionNewDatasource()).setEnabled(false);
			toolbar.addSeparator();
			toolbar.add(new ActionDelete()).setEnabled(false);
		}
		
		return toolbar;
	}
    
	protected void setPreferences()
	{
		control.setPreferences();
	}
	
	public void onSomethingChanged(boolean browsing)
	{
		toolbar.getAction(0).setEnabled(false);
		toolbar.getAction(1).setEnabled(false);
		toolbar.getAction(3).setEnabled(false);
		
		if(browsing)
		{
			toolbar.getAction(0).setEnabled(true);
			if(!control.getNavigator().isSelectionEmpty())
			{
				toolbar.getAction(1).setEnabled(true);
				if(control.getNavigator().getSelectionPath().getPathCount() < 4)
				{
					toolbar.getAction(3).setEnabled(true);
				}
			}
		}
	}
	
	private class ActionNewDriver extends AbstractAction
	{
		private ActionNewDriver()
		{
			putValue(SHORT_DESCRIPTION,"New driver...");
			putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_EXPLORER_DRIVER_NEW));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			DialogDriver dlg = new DialogDriver(control.getNavigator(),DialogDriver.ITEM_NEW);
			dlg.setVisible(true);
		}
	}
	
	private class ActionNewDatasource extends AbstractAction
	{
		private ActionNewDatasource()
		{
			putValue(SHORT_DESCRIPTION,"New datasource...");
			putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_EXPLORER_DATASOURCE_NEW));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			if(control.getNavigator().isSelectionEmpty())
			{
				Application.alert(Application.PROGRAM,"Select one driver!");
				return;
			}
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)control.getNavigator().getSelectionPath().getPathComponent(1);
			control.getNavigator().setSelectionNode(node);
		
			DialogDatasource dlg = new DialogDatasource(control.getNavigator(),DialogDatasource.ITEM_NEW);
			dlg.setVisible(true);
		}
	}
	
	private class ActionDelete extends AbstractAction
	{
		private ActionDelete()
		{
			putValue(SHORT_DESCRIPTION,"Delete selection");
			putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_DELETE));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			control.getNavigator().remove();
		}
	}	
}