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

package com.sqleo.environment.ctrl;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.ListView;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.Classpath;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.define.ColumnsChooser;
import com.sqleo.environment.ctrl.explorer.DialogExport;
import com.sqleo.environment.ctrl.explorer.SideNavigator;
import com.sqleo.environment.ctrl.explorer.SideSearchCriteria;
import com.sqleo.environment.ctrl.explorer.UoDatasource;
import com.sqleo.environment.ctrl.explorer.UoDriver;
import com.sqleo.environment.ctrl.explorer.UoLinks;
import com.sqleo.environment.mdi.ClientMetadataExplorer;
import com.sqleo.querybuilder.ViewObjects;


public class MetadataExplorer extends BorderLayoutPanel implements ChangeListener,TreeSelectionListener
{
	public static final String ALL_TABLE_TYPES_LABEL = I18n.getString("querybuilder.objetctype.all", ViewObjects.ALL_TABLE_TYPES);	
	
	private CardLayout cv;
	private JTabbedPane tp;
	
	private SideNavigator navigator;
	private SideSearchCriteria search;
	
	public MetadataExplorer()
	{
		super(2,2);
		
		this.getActionMap().put("choose-columns"	,new ActionChooseColumns());
		this.getActionMap().put("list-copy"	,new ActionCopyList());
		this.getActionMap().put("list-export"	,new ActionExportList());
		this.getActionMap().put("list-refresh"	,new ActionRefreshList());
		initComponents();
	    loadNavigator();
	}

	private void initComponents()
	{
		navigator = new SideNavigator();
		search = new SideSearchCriteria();
		
		JSplitPane split = new JSplitPane();
		split.setDividerLocation(250);
		split.setOneTouchExpandable(true);
		setComponentCenter(split);
		
		Container container = new Container();
		container.setLayout(cv = new CardLayout());
		container.add("First", navigator.getRightView());
		container.add("Last", search.getRightView());
		split.setRightComponent(container);
		
		tp = new JTabbedPane(JTabbedPane.BOTTOM);
		tp.addTab(I18n.getString("metadataexplorer.browse", "Browse"),navigator);
		tp.addTab(I18n.getString("metadataexplorer.search", "Search"),search);
		tp.addChangeListener(this);
		split.setLeftComponent(tp);        
	}
	
	public SideNavigator getNavigator()
	{
		return navigator;
	}
	
	private void loadNavigator()
	{
		if(Application.session.canMount(Application.ENTRY_JDBC))
		{
			Application.session.mount(Application.ENTRY_JDBC);
			Application.session.home();
			for(Enumeration eDv = Application.session.jumps(); eDv.hasMoreElements();)
			{
				UoDriver uoDv = new UoDriver();
				uoDv.name = eDv.nextElement().toString();
				
				Application.session.jump(uoDv.name);
				
				Object[] dvInfo = (Object[])Application.session.jump().get(0);
				uoDv.library	= dvInfo[0] == null ? "" : dvInfo[0].toString();
				uoDv.classname	= dvInfo[1] == null ? "" : dvInfo[1].toString();
				uoDv.example	= dvInfo[2] == null ? "" : dvInfo[2].toString();
				
				try
				{
					ConnectionAssistant.declare(uoDv.library,uoDv.classname,!Classpath.isRuntime(uoDv.library));
				}
				catch (Exception e)
				{
					uoDv.message = e.toString();
				}
				finally
				{
					navigator.add(uoDv);
				}
				
				for(Enumeration eDs = Application.session.jumps(); eDs.hasMoreElements();)
				{
					UoDatasource uoDs = new UoDatasource(uoDv);
					uoDs.name = eDs.nextElement().toString();
					navigator.add(uoDs);
					
					Application.session.home();
					Application.session.jump(new String[]{uoDv.name,uoDs.name});
				
					Object[] dsInfo = (Object[])Application.session.jump().get(0);
					uoDs.url = dsInfo[0] == null ? "" : dsInfo[0].toString();
					uoDs.uid = dsInfo[1] == null ? "" : dsInfo[1].toString();
					
					/* reload password */
					if(dsInfo.length >= 3 && (uoDs.remember = dsInfo[2]!=null) )
						uoDs.pwd = dsInfo[2].toString();
					else
						uoDs.pwd = "";
					
					/* auto connection */
					if(dsInfo.length >= 4)
						uoDs.auto_connect = ((Boolean)dsInfo[3]).booleanValue();
					
					/* schema filter */
					if(dsInfo.length >= 5)
						uoDs.schema = dsInfo[4] == null ? null : dsInfo[4].toString();
					// FK definition file
					if(dsInfo.length >= 6)
						uoDs.selectedFkDefFileName = dsInfo[5] == null ? null : dsInfo[5].toString();

					if(dsInfo.length >= 7)
						uoDs.readonly = ((Boolean)dsInfo[6]).booleanValue();
					
					if(dsInfo.length >= 8)
						uoDs.color = (Color) dsInfo[7];


					
					/* links */
					UoLinks uoLk = (UoLinks)navigator.getSelectionNode().getLastLeaf().getUserObject();
					for(Enumeration eLk = Application.session.jumps(); eLk.hasMoreElements();)
					{
						String group = eLk.nextElement().toString();
					
						Application.session.home();
						Application.session.jump(new String[]{uoDv.name,uoDs.name,group});
						
						for(int i=0; i<Application.session.jump().size(); i++)
						{
							Object[] link = (Object[])Application.session.jump().get(i);
							uoLk.add(group, (link[0] == null ? null : link[0].toString()), link[1].toString(), link[2].toString());
						}
					}
				}
				Application.session.home();
			}
		}
		else
		{
			UoDriver.loadDefaults(this);
		}
		
		navigator.clearSelection();
		navigator.sort(navigator.getRootNode());
		navigator.addTreeSelectionListener(this);
	}

	public void unloadNavigator()
	{
		Application.session.umount(Application.ENTRY_JDBC);
		Application.session.mount(Application.ENTRY_JDBC);
		
		DefaultMutableTreeNode root = navigator.getRootNode();
		for(int i=0; i<root.getChildCount();i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(i);
			UoDriver uoDv = (UoDriver)node.getUserObject();
			
			Application.session.jump(uoDv.name).add(new String[]{uoDv.library,uoDv.classname,uoDv.example});		
			for(int j=0; j<node.getChildCount();j++)
			{
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(j);
				UoDatasource uoDs = (UoDatasource)child.getUserObject();
				
				if(uoDs.isConnected())
				{
					try
					{
						uoDs.disconnect();
					}
					catch(Exception e)
					{
						Application.println(e,false);
					}
				}
				
				Application.session.home();
				Application.session.jump(new String[]{uoDv.name,uoDs.name});
				Application.session.jump().add(new Object[]{uoDs.url,uoDs.uid,
						(uoDs.remember?uoDs.pwd:null),new Boolean(uoDs.auto_connect),
						uoDs.schema,uoDs.selectedFkDefFileName,new Boolean(uoDs.readonly), uoDs.color});
				
				/* links */
				UoLinks uoLk = (UoLinks)child.getLastLeaf().getUserObject();
				Iterator iG = uoLk.getGroups().iterator();
				while(iG.hasNext())
				{
					String group = iG.next().toString();
				
					Application.session.home();
					Application.session.jump(new String[]{uoDv.name,uoDs.name,group});
					
					Iterator iK = uoLk.getLinks(group).iterator();
					while(iK.hasNext())
					{
						Application.session.jump().add(iK.next());
					}
				}
			}
			Application.session.home();
		}
	}
	
	public void setPreferences()
	{
		navigator.setPreferences();
		search.setPreferences();
	}

	private void onSomethingChanged()
	{
		this.getActionMap().get("choose-columns").setEnabled(false);
		this.getActionMap().get("list-refresh").setEnabled(false);
		this.getActionMap().get("list-copy").setEnabled(false);
		this.getActionMap().get("list-export").setEnabled(false);
		
		if(tp.getSelectedIndex() == 1)
		{
			this.getActionMap().get("list-copy").setEnabled(true);
			this.getActionMap().get("list-export").setEnabled(true);
		}
		else if(!navigator.isSelectionEmpty() && !navigator.getSelectionNode().getAllowsChildren())
		{
			this.getActionMap().get("list-copy").setEnabled(true);
			this.getActionMap().get("list-export").setEnabled(true);			
			this.getActionMap().get("list-refresh").setEnabled(!(navigator.getSelectionNode().getUserObject() instanceof UoLinks));
			this.getActionMap().get("choose-columns").setEnabled(!(navigator.getSelectionNode().getUserObject() instanceof UoLinks));
		}
		
		ClientMetadataExplorer cme = (ClientMetadataExplorer)Application.window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
		cme.onSomethingChanged(tp.getSelectedIndex() == 0);
	}
	
	private void showFirst()
	{
		cv.first((Container)((JSplitPane)this.getComponent(0)).getRightComponent());
	}
    
	private void showLast()
	{
		cv.last((Container)((JSplitPane)this.getComponent(0)).getRightComponent());
	}
	
	public void stateChanged(ChangeEvent ce)
	{
		if(tp.getSelectedIndex() == 0)
			showFirst();
		else
			showLast();
		
		onSomethingChanged();
	}
	
	public void valueChanged(TreeSelectionEvent tse)
	{
		onSomethingChanged();
	}
	
	private class ActionChooseColumns extends AbstractAction
	{
		private ActionChooseColumns()
		{
			putValue(NAME,I18n.getString("metadataexplorer.menu.chooseCol", "Choose columns..."));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			String dvname = MetadataExplorer.this.navigator.getSelectionNode().getPath()[1].toString();
			ColumnsChooser.showDialog(dvname,"Table types");
		}
	}

	private class ActionCopyList extends AbstractAction
	{
		private ActionCopyList()
		{

			putValue(NAME,I18n.getString("metadataexplorer.menu.listCopy", "Copy list"));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			if(MetadataExplorer.this.tp.getSelectedIndex() == 0)
			{
				for(int i=0; i<navigator.getRightView().getComponentCount(); i++)
				{
					Component c = navigator.getRightView().getComponent(i);					
					if(c.isShowing() && c instanceof ListView) ((ListView)c).copyAllRows();
				}
			}
			else
				((ListView)search.getRightView()).copyAllRows();
		}
	}
	
	private class ActionExportList extends AbstractAction
	{
		private ActionExportList()
		{

			putValue(NAME,I18n.getString("metadataexplorer.menu.listExport", "Export list..."));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			ListView lv = null;
			if(MetadataExplorer.this.tp.getSelectedIndex() == 0)
			{
				int last = navigator.getRightView().getComponentCount() - 1;
				lv = (ListView)navigator.getRightView().getComponent(last);
			}
			else
				lv = (ListView)search.getRightView();
			
			new DialogExport(lv).setVisible(true);
		}
	}
	
	private class ActionRefreshList extends AbstractAction
	{
		private ActionRefreshList()
		{
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
			putValue(NAME,I18n.getString("metadataexplorer.menu.listRefresh", "Refresh"));
			setEnabled(false);
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			TreePath path = MetadataExplorer.this.navigator.getSelectionPath();
			MetadataExplorer.this.navigator.clearSelection();
			MetadataExplorer.this.navigator.setSelectionPath(path);
		}
	}
}