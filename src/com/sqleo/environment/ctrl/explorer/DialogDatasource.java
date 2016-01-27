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

import java.awt.Cursor;

import javax.swing.JCheckBox;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.environment.Application;


public class DialogDatasource extends AbstractDialogConfirm
{
	public static final short ITEM_NEW = 0;
	public static final short ITEM_MODIFY = 1;
	public static final short ITEM_DUPLICATE = 2;
	
	private short request;
	private SideNavigator navigator;
	
	private UoDatasource uoDs;
	private MaskDatasource mDs;
	
	private JCheckBox cbxConnect;
	
	public DialogDatasource(SideNavigator navigator, short request)
	{
		super(Application.window,"datasource." + (request == ITEM_MODIFY ? "edit":"new"), INITIAL_WIDTH, INITIAL_HEIGHT+50);
		
		this.request = request;
		this.navigator = navigator;
		
		getContentPane().add(mDs = new MaskDatasource());
		bar.add(cbxConnect = new JCheckBox("connect"),0);
	}

	protected void onOpen()
	{		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)navigator.getSelectionPath().getLastPathComponent();
		
		switch(request)
		{
			case ITEM_NEW:
				mDs.load(uoDs = new UoDatasource((UoDriver)node.getUserObject()));
				break;
			case ITEM_DUPLICATE:
				UoDriver uoDv = (UoDriver)((DefaultMutableTreeNode)node.getParent()).getUserObject();
				
				uoDs = new UoDatasource(uoDv);
				uoDs.url = ((UoDatasource)node.getUserObject()).url;
				uoDs.uid = ((UoDatasource)node.getUserObject()).uid;
				uoDs.pwd = ((UoDatasource)node.getUserObject()).pwd;
				uoDs.schema = ((UoDatasource)node.getUserObject()).schema;
				uoDs.remember = ((UoDatasource)node.getUserObject()).remember;
				uoDs.auto_connect = ((UoDatasource)node.getUserObject()).auto_connect;
				uoDs.readonly = ((UoDatasource)node.getUserObject()).readonly;
				uoDs.color = ((UoDatasource)node.getUserObject()).color;
				
				mDs.load(uoDs);
				break;
			case ITEM_MODIFY:
				mDs.load(uoDs = (UoDatasource)node.getUserObject());
				break;
		}		
		
		mDs.setEnabled(!uoDs.isConnected());
		cbxConnect.setSelected(uoDs.isConnected());		
	}
	
	protected boolean onConfirm()
	{
		boolean bContinue;
		
		if(bContinue = mDs.unload(uoDs))
		{
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			try
			{
				if(cbxConnect.isSelected() && !uoDs.isConnected())
				{
					uoDs.connect();
					Application.window.connectionOpened(uoDs.getKey());
				}
				else if(!cbxConnect.isSelected() && uoDs.isConnected())
				{
					uoDs.disconnect();
					Application.window.connectionClosed(uoDs.getKey());
				}
				
				if(request == ITEM_MODIFY)
					navigator.reloadSelection();
				else
					navigator.add(uoDs);
			}
			catch(Exception e)
			{
				Application.println(e,true);
				bContinue = false;
			}
			finally
			{
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		
		return bContinue;
	}
}