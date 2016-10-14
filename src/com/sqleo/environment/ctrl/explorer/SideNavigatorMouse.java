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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;


public class SideNavigatorMouse extends MouseAdapter
{
	private SideNavigator navigator;
	private JPopupMenu popup;
	
	SideNavigatorMouse(SideNavigator navigator)
	{
		this.navigator = navigator;
		
		popup = new JPopupMenu();
		popup.add(new ActionConnect());
		popup.add(new ActionReconnect());
		popup.add(new ActionNewDriver());
		popup.add(new ActionNewDatasource());
		popup.addSeparator();
		popup.add(new ActionSchemaFilter(I18n.getString("metadataexplorer.menu.setAsDefaultSchema", "set as default schema")));
		popup.add(new ActionSchemaFilter(I18n.getString("metadataexplorer.menu.removeSchemaFilter", "remove schema filter")));
		popup.addSeparator();
		popup.add(new ActionDelete());
		popup.add(new ActionDuplicate());
		popup.addSeparator();
		popup.add(new ActionProperties());
	}

	public void mouseReleased(MouseEvent me)
	{
		if(SwingUtilities.isRightMouseButton(me))
		{
			popup.getComponent(0).setVisible(false);
			popup.getComponent(1).setVisible(false);
			popup.getComponent(2).setVisible(false);
			popup.getComponent(3).setVisible(false);
			popup.getComponent(4).setVisible(false);
			popup.getComponent(5).setVisible(false);
			popup.getComponent(6).setVisible(false);
					
			for(int i=0; i<popup.getComponentCount(); i++)
				popup.getComponent(i).setEnabled(false);		
			
			navigator.setSelectionForLocation(me.getPoint());
			if(!navigator.isSelectionEmpty())
			{
				if(navigator.getSelectionPath().getPathCount() == 3)
				{
					UoDatasource uoDs = (UoDatasource)navigator.getSelectionNode().getUserObject();
					((JCheckBoxMenuItem)popup.getComponent(0)).setSelected(uoDs.isConnected());
					
					popup.getComponent(0).setVisible(true);
					popup.getComponent(0).setEnabled(true);
					popup.getComponent(1).setVisible(true);
					popup.getComponent(1).setEnabled(true);
					
					ConnectionHandler ch = uoDs.isConnected() ? ConnectionAssistant.getHandler(uoDs.getKey()) : null;
					if(uoDs.schema!=null || (ch!=null && ((Boolean)ch.getObject("$supportsSchema")).booleanValue()))
					{
						popup.getComponent(4).setVisible(true);
						popup.getComponent(6).setVisible(true);
						popup.getComponent(6).setEnabled(uoDs.schema!=null);
					}
				}
				else if(navigator.getSelectionPath().getPathCount() == 4)
				{
					if(navigator.getSelectionNode().getAllowsChildren())
					{
						popup.getComponent(5).setVisible(true);
						popup.getComponent(5).setEnabled(true);
					}
				}
				else
				{
					popup.getComponent(3).setVisible(navigator.getSelectionPath().getPathCount() == 2);
					popup.getComponent(3).setEnabled(navigator.getSelectionPath().getPathCount() == 2);
				}
				
				popup.getComponent(8).setEnabled(navigator.getSelectionPath().getPathCount() == 2 || navigator.getSelectionPath().getPathCount() == 3);
				popup.getComponent(9).setEnabled(navigator.getSelectionPath().getPathCount() == 2 || navigator.getSelectionPath().getPathCount() == 3);
				popup.getComponent(11).setEnabled(navigator.getSelectionPath().getPathCount() == 2 || navigator.getSelectionPath().getPathCount() == 3);
			}
			else
			{
				popup.getComponent(2).setEnabled(true);				
				popup.getComponent(2).setVisible(true);				
			}		
		
			if(navigator.isSelectionEmpty() || navigator.getSelectionNode().getAllowsChildren())
				popup.show((JTree)me.getSource(),me.getX(),me.getY());
		}
	}
	
	public void mouseClicked(MouseEvent me)
	{
		if(me.getModifiers() == (MouseEvent.BUTTON1_MASK + MouseEvent.SHIFT_MASK))
		{
			doSchemaFilter();
		}
	}
	
	private void doSchemaFilter()
	{
		if(!navigator.isSelectionEmpty())
		{
			DefaultMutableTreeNode node = navigator.getSelectionNode();
			if(node.getUserObject() instanceof UoDatasource)
			{
				UoDatasource uoDs = (UoDatasource)node.getUserObject();
				if(uoDs.schema!=null)
				{
					uoDs.schema = null;
					navigator.reload(node);
				}
			}
			else
			{
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
				if(parent.getUserObject() instanceof UoDatasource && node.getAllowsChildren())
				{
					UoDatasource uoDs = (UoDatasource)parent.getUserObject();
					uoDs.schema = node.toString();
					navigator.reload(parent);
				}
			}
		}		
	}

	private class ActionNewDriver extends AbstractAction
	{
		private ActionNewDriver()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.newDriver", "new driver..."));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			DialogDriver dlg = new DialogDriver(navigator,DialogDriver.ITEM_NEW);
			dlg.setVisible(true);
		}
	}
	
	private class ActionNewDatasource extends AbstractAction
	{
		private ActionNewDatasource()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.newDatasource", "new datasource..."));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			if(navigator.isSelectionEmpty())
			{
				Application.alert(Application.PROGRAM,"select one driver!");
				return;
			}
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)navigator.getSelectionPath().getPathComponent(1);
			navigator.setSelectionNode(node);
		
			DialogDatasource dlg = new DialogDatasource(navigator,DialogDatasource.ITEM_NEW);
			dlg.setVisible(true);
		}
	}
	
	private class ActionDelete extends AbstractAction
	{
		private ActionDelete()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.delete", "delete"));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			navigator.remove();
		}
	}
	
	private class ActionDuplicate extends AbstractAction
	{
		private ActionDuplicate()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.duplicate", "duplicate..."));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			if(navigator.isSelectionEmpty()) return;
			
			DefaultMutableTreeNode node = navigator.getSelectionNode();
			if(node.getUserObject() instanceof UoDriver)
			{
				DialogDriver dlg = new DialogDriver(navigator,DialogDriver.ITEM_DUPLICATE);
				dlg.setVisible(true);
			}
			else if(node.getUserObject() instanceof UoDatasource)
			{
				DialogDatasource dlg = new DialogDatasource(navigator,DialogDatasource.ITEM_DUPLICATE);
				dlg.setVisible(true);
			}			
		}
	}
	
	private class ActionProperties extends AbstractAction
	{
		private ActionProperties()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.properties", "properties..."));
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			if(navigator.isSelectionEmpty()) return;
			
			DefaultMutableTreeNode node = navigator.getSelectionNode();
			if(node.getUserObject() instanceof UoDriver)
			{
				DialogDriver dlg = new DialogDriver(navigator,DialogDriver.ITEM_MODIFY);
				dlg.setVisible(true);
			}
			else if(node.getUserObject() instanceof UoDatasource)
			{
				DialogDatasource dlg = new DialogDatasource(navigator,DialogDatasource.ITEM_MODIFY);
				dlg.setVisible(true);
			}
		}
	}
	
	private class ActionReconnect extends AbstractAction
	{
		ActionReconnect()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.reconnect", "reconnect"));
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			UoDatasource uoDs = (UoDatasource)navigator.getSelectionNode().getUserObject();
			boolean disconnected = false;
			try
			{
				if(uoDs.isConnected()){
					uoDs.disconnect();
					disconnected = true;
				}
				uoDs.connect();
			
			}
			catch(Exception e)
			{
				Application.println(e,true);
			}
			finally
			{
//#386 command editor: current connexion should be emptyed after disconnect
//				if(disconnected){
//					Application.window.connectionClosed(uoDs.getKey());
//				}
				final boolean connected = uoDs.isConnected();
				if(connected){
//					Application.window.connectionOpened(uoDs.getKey());
				}else{
					Application.window.connectionClosed(uoDs.getKey());
				}
				navigator.reloadSelection();
				((JCheckBoxMenuItem)popup.getComponent(0)).setSelected(connected);
			}
		}
	}
	
	private class ActionConnect extends JCheckBoxMenuItem implements ActionListener
	{
		ActionConnect()
		{
			super(I18n.getString("metadataexplorer.menu.connect", "connect"));
			
			this.addActionListener(this);
			this.setSelected(false);
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			UoDatasource uoDs = (UoDatasource)navigator.getSelectionNode().getUserObject();
			try
			{
				if(this.isSelected() && !uoDs.isConnected())
				{
					uoDs.connect();
				}
				else if(!this.isSelected() && uoDs.isConnected())
				{
					uoDs.disconnect();
				}
			
			}
			catch(Exception e)
			{
				Application.println(e,true);
			}
			finally
			{
				final boolean connected = uoDs.isConnected();
				if(connected){
					Application.window.connectionOpened(uoDs.getKey());
				}else{
					Application.window.connectionClosed(uoDs.getKey());
				}
				navigator.reloadSelection();
				this.setSelected(connected);
			}
		}
	}
	
	private class ActionSchemaFilter extends AbstractAction
	{
		private ActionSchemaFilter(String label)
		{
			putValue(NAME,label);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			SideNavigatorMouse.this.doSchemaFilter();
		}
	}	
}
