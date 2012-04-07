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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.QueryBuilder;


public class ViewLinks extends AbstractViewObjects implements ItemListener
{
	private String keycah = null;
	private UoLinks uoLk = null;
	
	private JCheckBox cbxAll;
	private JComboBox groups;
	
	ViewLinks()
	{
		Toolbar bar = new Toolbar(Toolbar.HORIZONTAL);
		bar.add(cbxAll = new JCheckBox("all groups"));
		bar.add(groups = new JComboBox());
		bar.addSeparator();
		bar.add(new ActionAddGroup());
		bar.add(new ActionRemoveGroup());
		
		cbxAll.addItemListener(this);
		groups.addItemListener(this);
		
		BorderLayoutPanel north = new BorderLayoutPanel();
		north.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(),new EmptyBorder(2,2,2,2)));
		north.setComponentCenter(bar);
		
		setComponentNorth(north);
	
		addColumn("TABLE_SCHEM");
		addColumn("TABLE_NAME");
		addColumn("TABLE_TYPE");
	}

	protected String getHandlerKey()
	{
		if(ConnectionAssistant.hasHandler(keycah))
		{
			QueryBuilder.identifierQuoteString = ConnectionAssistant.getHandler(keycah).getObject("$identifierQuoteString").toString();
			QueryBuilder.maxColumnNameLength = ((Integer)ConnectionAssistant.getHandler(keycah).getObject("$maxColumnNameLength")).intValue();
		}
		
		return keycah;
	}
	
	protected void list(DefaultMutableTreeNode node)
	{
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
		keycah = ((UoDatasource)parent.getUserObject()).getKey();

		uoLk = (UoLinks)node.getUserObject();
		groups.setModel(new DefaultComboBoxModel(uoLk.getGroups().toArray()));
		groups.setSelectedItem(null);
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		removeAllRows();
		groups.setEnabled(!cbxAll.isSelected());
		
		if(cbxAll.isSelected())
		{
			for(int i=0; i<groups.getItemCount(); i++)
			{
				Object group = groups.getItemAt(i);
				
				ArrayList links = uoLk.getLinks(group.toString());
				for(int j=0; j<links.size(); j++)
				{
					addRow((String[])links.get(j));
				}
			}
		}
		else
		{
			Object group = groups.getSelectedItem();
			if(group!=null)
			{
				ArrayList links = uoLk.getLinks(group.toString());
				for(int i=0; i<links.size(); i++)
				{
					addRow((String[])links.get(i));
				}
			}
		}
	}
	
	public void mouseReleased(MouseEvent me)
	{
		if(SwingUtilities.isRightMouseButton(me))
		{
			int row = getJavaComponent().rowAtPoint(me.getPoint());
			getJavaComponent().setRowSelectionInterval(row,row);
			
			JPopupMenu popup = new JPopupMenu();
			popup.add(new ActionQuery()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			popup.add(new ActionRemoveLink()).setEnabled(!cbxAll.isSelected());
			popup.addSeparator();
			popup.add(new ActionCommand()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			popup.addSeparator();
			popup.add(new ActionDeleteContent()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			popup.add(new ActionDropObject()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			popup.addSeparator();
			popup.add(new ActionShowContent()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			popup.add(new ActionShowDefinition()).setEnabled(ConnectionAssistant.hasHandler(keycah));
			
			popup.show(getJavaComponent(),me.getX(),me.getY());
		}
	}
	
	private class ActionAddGroup extends AbstractAction
	{
		ActionAddGroup()
		{
			this.putValue(NAME, "add group");
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_EXPLORER_ADD_GROUP));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object group = Application.input(Application.PROGRAM,"group name:");
			if(group!=null)
			{
				if( ((DefaultComboBoxModel)groups.getModel()).getIndexOf(group) == -1 )
					groups.addItem(group);				
				groups.setSelectedItem(group);
			}
		}
	}
	
	private class ActionRemoveGroup extends AbstractAction
	{
		ActionRemoveGroup()
		{
			this.putValue(NAME, "remove group");
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_EXPLORER_REMOVE_GROUP));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object group = groups.getSelectedItem();
			if(group!=null)
			{
				if(!Application.confirm("confirm delete","deleting \"" + group + "\" continue?")) return;
				
				groups.removeItem(group);
				uoLk.removeGroup(group.toString());
			}
		}
	}
	
	protected class ActionRemoveLink extends AbstractAction
	{
		ActionRemoveLink()
		{
			super("remove from group");
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object group = groups.getSelectedItem();
			if(group!=null)
			{
				int row = ViewLinks.this.getSelectedRow();
				uoLk.getLinks(group.toString()).remove(row);
				ViewLinks.this.removeRow(row);
			}
		}
	}
}
