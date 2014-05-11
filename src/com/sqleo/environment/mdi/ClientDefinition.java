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

package com.sqleo.environment.mdi;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import com.sqleo.common.gui.Toolbar;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.DefinitionPane;
import com.sqleo.environment.ctrl.content.AbstractActionContent;
import com.sqleo.environment.ctrl.define.ColumnsChooser;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class ClientDefinition extends MDIClient
{
	public static final String DEFAULT_TITLE = "DEFINITION";
	
	private DefinitionPane control;
	private JMenuItem[] m_actions;
	
	private String type;
	
	public ClientDefinition(String keycah, QueryTokens.Table table, String type)
	{
		super(DEFAULT_TITLE + " : " + table.getIdentifier() + " : " + keycah);
		setMaximizable(true);
		setResizable(true);
		
		this.type = type;
		
		setComponentCenter(control = new DefinitionPane(keycah,table));
		control.setBorder(new EmptyBorder(2,2,2,2));
		
		initMenuActions();
	}

	private void initMenuActions()
	{
		m_actions = new JMenuItem[]
		{
			MDIMenubar.createItem(new ActionChooseColumns()),
			null,
			MDIMenubar.createItem(new ActionCopyList()),
			MDIMenubar.createItem(new ActionRefreshList()),
			null,			
			MDIMenubar.createItem(new ActionShowContent())
		};
	}

	public JMenuItem[] getMenuActions()
	{
		return m_actions;
	}

	public Toolbar getSubToolbar()
	{
		return null;
	}
    
	protected void setPreferences()
	{
	}

	private class ActionChooseColumns extends AbstractAction
	{
		private ActionChooseColumns()
		{
			putValue(NAME,"Choose columns...");
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			int i = ClientDefinition.this.getTitle().lastIndexOf(':');
			int j = ClientDefinition.this.getTitle().lastIndexOf('.');
			
			String dvname = ClientDefinition.this.getTitle().substring(i+2,j);
			String mvname = ClientDefinition.this.control.getSelectedTitle();
			
			ColumnsChooser.showDialog(dvname,mvname);
		}
	}
	
	private class ActionCopyList extends AbstractAction
	{
		private ActionCopyList()
		{
			putValue(NAME,"Copy list");
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			ClientDefinition.this.control.getSelectedView().copyAllRows();
		}
	}

	private class ActionRefreshList extends AbstractAction
	{
		private ActionRefreshList()
		{
			putValue(NAME,"Refresh list");
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			ClientDefinition.this.control.getSelectedView().reset();
			ClientDefinition.this.control.stateChanged(null);
		}
	}

	private class ActionShowContent extends AbstractActionContent
	{
		ActionShowContent(){this.putValue(NAME,"Show content");}
		
		protected boolean isShowCountRecordsPopup(){
			return false;
		}
		
		protected TableMetaData getTableMetaData()
		{
			int i = ClientDefinition.this.getTitle().indexOf(':');
			int j = ClientDefinition.this.getTitle().lastIndexOf(':');
			
			String keycah = ClientDefinition.this.getTitle().substring(j+2).trim();
			String name	= ClientDefinition.this.getTitle().substring(i+2,j).trim();
			
			String schema = null;
			if((i = name.indexOf('.')) != -1)
			{
				schema = name.substring(0,i);
				name = name.substring(i+1);
			}			
			
			return new TableMetaData(keycah,schema,name,ClientDefinition.this.type);
		}

		protected void onActionPerformed(int records, int option)
		{
			if(option == JOptionPane.CANCEL_OPTION || (records == 0 && option == JOptionPane.NO_OPTION)) return;
			boolean retrieve = records > 0 && option == JOptionPane.YES_OPTION;
			
			ClientContent client = new ClientContent(this.getTableMetaData(),retrieve);
			client.setTitle(ClientContent.DEFAULT_TITLE+" : " + this.getTableMetaData() + " : " + this.getTableMetaData().getHandlerKey());
			
			Application.window.add(client);
		}
		
		protected int showConfirmDialog(int records)
		{
			if(records == 0)
			{
				String message = this.getDefaultMessage(records) + "\nDo you want continue?";
				return JOptionPane.showConfirmDialog(Application.window,message,"show content",JOptionPane.YES_NO_OPTION);
			}
			else
			{
				String message = this.getDefaultMessage(records) + "\nDo you want retrieve?";
				return JOptionPane.showConfirmDialog(Application.window,message,"show content",JOptionPane.YES_NO_CANCEL_OPTION);
			}
		}
	}
}