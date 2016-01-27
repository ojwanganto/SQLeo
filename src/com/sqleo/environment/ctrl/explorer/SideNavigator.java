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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import com.sqleo.common.gui.TreeView;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.MetadataExplorer;


public class SideNavigator extends TreeView implements TreeSelectionListener
{
	private CardLayout	rLayout;
	private JPanel		rContainer;
	
	private ViewConnection	rvConnection;
	private ViewDatasources	rvDatasources;
	private ViewMetadata	rvMetadata;
	private ViewLinks		rvLinks;
	
	public SideNavigator()
	{
		super("JDBC",false);
		
		addTreeSelectionListener(this);
		this.getJavaComponent().setCellRenderer(new InternalTreeCellRenderer());
		this.getJavaComponent().addMouseListener(new SideNavigatorMouse(this));
		
		JPanel blank = new JPanel();
		blank.setBackground(Color.white);
		blank.setBorder(LineBorder.createGrayLineBorder());
		
		JLabel wait = new JLabel("wait, loading...",JLabel.CENTER);
		wait.setOpaque(true);
		wait.setBackground(Color.white);
		wait.setBorder(LineBorder.createGrayLineBorder());
		
		rContainer = new JPanel();
		rContainer.setLayout(rLayout = new CardLayout());
		rContainer.add("blank"			, blank);
		rContainer.add("wait"			, wait);
		rContainer.add("connection"		, rvConnection	= new ViewConnection(this));
		rContainer.add("datasources"	, rvDatasources	= new ViewDatasources(this));
		rContainer.add("metadata"		, rvMetadata	= new ViewMetadata());
		rContainer.add("links"			, rvLinks		= new ViewLinks());
	}
	
	public void add(UoDriver uoDv,boolean doReload)
	{
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(uoDv,true);
		getRootNode().add(child);
		
		if(doReload)
		{
			reloadRoot();
			setSelectionNode(child);
		}
	}
	
	public void add(UoDriver uoDv)
	{
		add(uoDv,true);
	}

	public void add(UoDatasource uoDs,boolean doReload)
	{
		if(this.isSelectionEmpty()) return;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getSelectionPath().getPathComponent(1);
		setSelectionNode(node);
		
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(uoDs,true);
		node.add(child);
				
		if(doReload)
		{
			reload(node);
			setSelectionNode(child);
			reloadSelection();
		}
	}
	
	public void add(UoDatasource uoDs)
	{
		add(uoDs,true);
	}
	
	private void removeChildrens(DefaultMutableTreeNode node)
	{
		while(node.getChildCount()>0)
			node.remove(0);
	}
	
	private void remove(UoDatasource uoDs)
	{
		try
		{
			if(uoDs.isConnected())
			{
				uoDs.disconnect();
				Application.window.connectionClosed(uoDs.getKey());
			}
		}
		catch(Exception e)
		{
			Application.println(e,false);
		}
	}
	
	public void remove()
	{
		if(this.isSelectionEmpty() || this.getSelectionNode().getLevel() > 2
		|| !Application.confirm("confirm delete","deleting \"" + this.getSelectionNode() + "\" continue?")) return;
		
		DefaultMutableTreeNode node = getSelectionNode();
		if(node.getUserObject() instanceof UoDriver)
		{
			for(int i=0; i<node.getChildCount(); i++)
			{
				remove((UoDatasource)((DefaultMutableTreeNode)node.getChildAt(i)).getUserObject());
			}
			
			Application.session.home();
			Application.session.ujump("metaview." + node.toString());
		}
		else if(node.getUserObject() instanceof UoDatasource)
		{
			remove((UoDatasource)node.getUserObject());
		}
		
		removeNode(node);
	}
	
	/* for auto-connection */
	public void onFireAutoConnect()
	{
		DefaultMutableTreeNode root = getRootNode();
		for(int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode driver = (DefaultMutableTreeNode)root.getChildAt(i);
			for(int j = 0; j < driver.getChildCount(); j++)
			{
				DefaultMutableTreeNode source = (DefaultMutableTreeNode)driver.getChildAt(j);
				UoDatasource uoDs = (UoDatasource)source.getUserObject();
				if(uoDs.auto_connect)
				{
					try
					{
						uoDs.connect();
						reload(source);
						Application.window.connectionOpened(uoDs.getKey());
					}
					catch (Exception e)
					{
						Application.alert("auto connect on " + uoDs.name,Text.wrap(e.toString(),100));
					}
				}
			}			
		}
	}
	
	public boolean connect(final String datasource) throws Exception 
	{
		DefaultMutableTreeNode root = getRootNode();
		for(int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode driver = (DefaultMutableTreeNode)root.getChildAt(i);
			for(int j = 0; j < driver.getChildCount(); j++)
			{
				DefaultMutableTreeNode source = (DefaultMutableTreeNode)driver.getChildAt(j);
				UoDatasource uoDs = (UoDatasource)source.getUserObject();
				if(datasource.equals(uoDs.getKey()))
				{
					uoDs.connect();
					reload(source);
					Application.window.connectionOpened(uoDs.getKey());
					return true;
				}
			}			
		}
		return false;
	}
	
	public UoDatasource findDatasource(final String chKey){
		DefaultMutableTreeNode root = getRootNode();
		for(int i = 0; i < root.getChildCount(); i++)
		{
			DefaultMutableTreeNode driver = (DefaultMutableTreeNode)root.getChildAt(i);
			for(int j = 0; j < driver.getChildCount(); j++)
			{
				DefaultMutableTreeNode source = (DefaultMutableTreeNode)driver.getChildAt(j);
				UoDatasource uoDs = (UoDatasource)source.getUserObject();
				if(chKey.equals(uoDs.getKey()))
				{
					return uoDs;
				}
			}			
		}
		return null;
	}
	
	public void clearSelection()
	{
		super.clearSelection();
		
		for(int i=0; i<getJavaComponent().getRowCount();i++)
			getJavaComponent().collapseRow(i);
	}
	
	public JComponent getRightView()
	{
		return rContainer;
	}
	
	public void setPreferences()
	{
		rvDatasources.setColumnWidth("name"		,Preferences.getInteger("explorer.navigator.datasources.name.width"));
		rvDatasources.setColumnWidth("url"		,Preferences.getInteger("explorer.navigator.datasources.url.width"));
		rvDatasources.setColumnWidth("status"	,Preferences.getInteger("explorer.navigator.datasources.status.width"));
	}
	
	public void reload(DefaultMutableTreeNode node)
	{
		boolean expanded = getJavaComponent().isExpanded(new TreePath(node.getPath()));
		
		if(node.getUserObject() instanceof UoDatasource)
		{
			UoLinks uoLk = node.getChildCount() > 0 ? (UoLinks)node.getLastLeaf().getUserObject() : new UoLinks();
			removeChildrens(node);
			
			UoDatasource uoDs = (UoDatasource)node.getUserObject();
			if(uoDs.isConnected())
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(uoDs.getKey());
				
				ArrayList alNames = ch.getArrayList("$schema_names");
				ArrayList alTypes = ch.getArrayList("$table_types");
				
				if(alNames.isEmpty() || uoDs.schema != null)
				{
					for(Iterator i=alTypes.iterator(); i.hasNext();)
						node.add(new DefaultMutableTreeNode(i.next().toString(),false));
					
					node.add(new DefaultMutableTreeNode(MetadataExplorer.ALL_TABLE_TYPES_LABEL,false));
				}
				else
				{
					for(Iterator i1=alNames.iterator(); i1.hasNext();)
					{
						DefaultMutableTreeNode child;
						node.add(child = new DefaultMutableTreeNode(i1.next().toString(),true));
						
						for(Iterator i2=alTypes.iterator(); i2.hasNext();)
							child.add(new DefaultMutableTreeNode(i2.next().toString(),false));
						
						child.add(new DefaultMutableTreeNode(MetadataExplorer.ALL_TABLE_TYPES_LABEL,false));
					}
				}
			}
			
			node.add(new DefaultMutableTreeNode(uoLk,false));				
		}
		sort((DefaultMutableTreeNode)node.getParent());
		
		super.reload(node);
		super.clearSelection();
		
		setSelectionNode(node);
		
		if(expanded)
			getJavaComponent().expandPath(new TreePath(node.getPath()));
	}
	
	// sort childrens
	public void sort(DefaultMutableTreeNode node)
	{
		for(int i=0; i<node.getChildCount()-1; i++)
		{
			String value1 = node.getChildAt(i).toString();
			for(int j=i+1; j<node.getChildCount(); j++)
			{
				String value2 = node.getChildAt(j).toString();
				if(value1.toLowerCase().compareTo(value2.toLowerCase()) > 0)
				{
					node.insert((DefaultMutableTreeNode)node.getChildAt(j),i);
					value1 = value2;
				}
			}
		}
		super.reload(node);
	}
	
	public void valueChanged(TreeSelectionEvent tse)
	{
		if(!this.isVisible()) return;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
		rLayout.show(rContainer,"blank");
		
		if(tse.getNewLeadSelectionPath()!=null)
		{		
			if(node.getUserObject() instanceof UoDriver)
			{
				rvDatasources.list(node);
				rLayout.show(rContainer,"datasources");				
			}
			else if(node.getUserObject() instanceof UoDatasource)
			{
				rvConnection.list(node);
				rLayout.show(rContainer,"connection");
			}
			else if(node.getUserObject() instanceof UoLinks)
			{
				String path = tse.getPath().toString();
				rvLinks.setInfo(Text.replaceText(path, ", ", " >> "));
				rvLinks.list(node);
				rLayout.show(rContainer,"links");
			}
			else if(!node.getAllowsChildren())
			{
				String path = tse.getPath().toString();
				rvMetadata.setInfo(Text.replaceText(path, ", ", " >> "));
				
				LeftLoader ll = new LeftLoader(node);
				new Thread(ll).start();
			}
		}
	}

	private class LeftLoader implements Runnable
	{
		DefaultMutableTreeNode node;
		
		LeftLoader(DefaultMutableTreeNode node)
		{
			this.node = node;
		}
		
		public void run()
		{
			rLayout.show(rContainer,"wait");
			SideNavigator.this.rvMetadata.list(node);
			rLayout.show(SideNavigator.this.rContainer,"metadata");
		}
	}
	
	private class InternalTreeCellRenderer extends DefaultTreeCellRenderer
	{
		public Icon getLeafIcon()
		{
			return Application.resources.getIcon(Application.ICON_EXPLORER_TYPES);
		}
			
		public Icon getOpenIcon()
		{
			return Application.resources.getIcon(Application.ICON_EXPLORER_SCHEMA);
		}
			
		public Icon getClosedIcon()
		{
			return Application.resources.getIcon(Application.ICON_EXPLORER_SCHEMA);
		}
			
		public Component getTreeCellRendererComponent(JTree tree,Object value,boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
				
			TreePath path = tree.getPathForRow(row);
			if(path != null)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
				if(node.getUserObject() instanceof UoDriver)
				{
					UoDriver uoDv = (UoDriver)node.getUserObject();
					if(uoDv.message == null)
						setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_DRIVER_OK));
					else
						setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_DRIVER_KO));
				}
				else if(node.getUserObject() instanceof UoDatasource)
				{
					if(((UoDatasource)node.getUserObject()).isConnected())
						setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_DATASOURCE_OK));
					else
						setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_DATASOURCE_KO));
				}
				else if(node.getUserObject() instanceof UoLinks)
				{
					setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_LINKS));
				}
				else if(node.getUserObject().toString() == MetadataExplorer.ALL_TABLE_TYPES_LABEL)
				{
					setIcon(Application.resources.getIcon(Application.ICON_EXPLORER_ALL));
				}
			}
				
			return this;
		}			
	}	
}
	