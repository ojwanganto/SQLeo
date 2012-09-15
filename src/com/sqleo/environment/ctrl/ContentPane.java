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

import java.awt.Cursor;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.content.ContentModel;
import com.sqleo.environment.ctrl.content.ContentView;
import com.sqleo.environment.ctrl.content.TaskRetrieve;
import com.sqleo.environment.ctrl.content.TaskUpdate;
import com.sqleo.environment.ctrl.content.UpdateModel;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.QueryModel;


public class ContentPane extends BorderLayoutPanel 
{
	private JLabel status;
	private JTextArea syntax;
	private ContentView view;
	
	private Thread task;
	private String keycah;
	private QueryModel qmodel;
	private UpdateModel umodel;
	private String query;
	private TaskRetrieve retrievingTask;
	
	public ContentPane(String keycah, QueryModel qmodel, UpdateModel umodel)
	{
		this(keycah,qmodel,umodel,qmodel.toString(false));	
	}
	public ContentPane(String keycah, String query)
	{
		this(keycah,null,null,query);
	}
	
	private ContentPane(String keycah, QueryModel qmodel, UpdateModel umodel, String query)
	{
		super(2,2);
		
		this.keycah = keycah;
		this.qmodel = qmodel;
		this.umodel = umodel;
		this.query = query;
				
		this.getActionMap().put("changes-save"	,new ActionSaveChanges());
		this.getActionMap().put("record-insert"	,new ActionInsertRecord());
		this.getActionMap().put("record-delete"	,new ActionDeleteRecord());
		this.getActionMap().put("task-stop"		,new ActionStopTask());
		this.getActionMap().put("task-go"		,new ActionRelaunch());
		
		status = new JLabel("...");
		status.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(2,4,2,4)));
		
		JScrollPane scroll = new JScrollPane(syntax = new JTextArea());
		syntax.setRows(3);
		
		syntax.setText(query);
		syntax.setWrapStyleWord(true);
		syntax.setLineWrap(true);
		syntax.setEditable(false);
		syntax.setOpaque(false);
		
		BorderLayoutPanel pnlSouth = new BorderLayoutPanel(2,2);
		pnlSouth.setComponentCenter(status);
		pnlSouth.setComponentNorth(scroll);
		
		setComponentSouth(pnlSouth);
		setComponentCenter(view = new ContentView(this));		
	}
	
	public boolean isReadOnly()
	{
		return umodel==null;
	}
	
	public String getHandlerKey()
	{
		if(ConnectionAssistant.hasHandler(keycah))
		{
			QueryBuilder.identifierQuoteString = ConnectionAssistant.getHandler(keycah).getObject("$identifierQuoteString").toString();
			QueryBuilder.maxColumnNameLength = ((Integer)ConnectionAssistant.getHandler(keycah).getObject("$maxColumnNameLength")).intValue();
		}		
		
		return keycah;
	}
	
	public QueryModel getQueryModel()
	{
		return qmodel;
	}

	public String getQuery() {
		return getQueryModel()!=null?getQueryModel().toString(false):query;
	}
	public UpdateModel getUpdateModel()
	{
		return umodel;
	}

	public void setUpdateModel(UpdateModel model)
	{
		umodel = model;
	}

	public ContentView getView()
	{
		return view;
	}

	public boolean isBusy()
	{
		return task!=null && this.getActionMap().get("task-stop").isEnabled();
	}

	public void doStop()
	{
		// gestire cancel dello statement se attivo!!!
		onEndTask();
	}
	public void doSuspend()
	{
		onSuspendTask();
	}
	
	public void doRetrieve()
	{
		retrievingTask = new TaskRetrieve(this);
		onBeginTask(retrievingTask);
	}
	
	public boolean fetchNextRecords(){
		if(retrievingTask!=null){
			onResumeTask();
			return retrievingTask.setNextResultSet();
		}else{
			return true;
		}
	}
	
	public void doRetrieve(int limit)
	{
		onBeginTask(new TaskRetrieve(this,limit));
	}
	
	public void doUpdate()
	{
		onBeginTask(new TaskUpdate(this));
	}
	
	private void onBeginTask(Runnable r)
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		this.getActionMap().get("task-go").setEnabled(false);
		this.getActionMap().get("task-stop").setEnabled(true);
		this.getActionMap().get("changes-save").setEnabled(false);
		this.getActionMap().get("record-insert").setEnabled(false);
		this.getActionMap().get("record-delete").setEnabled(false);
		
		task = new Thread(r);
		task.start();		
	}
	
	private void onResumeTask()
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		this.getActionMap().get("task-go").setEnabled(false);
		this.getActionMap().get("task-stop").setEnabled(true);
		this.getActionMap().get("changes-save").setEnabled(false);
		this.getActionMap().get("record-insert").setEnabled(false);
		this.getActionMap().get("record-delete").setEnabled(false);
		
	}
	
	private void onEndTask()
	{
		task = null;
		
		this.getActionMap().get("task-go").setEnabled(true);
		this.getActionMap().get("task-stop").setEnabled(false);
		this.getActionMap().get("changes-save").setEnabled(true);
		this.getActionMap().get("record-insert").setEnabled(true);
		this.getActionMap().get("record-delete").setEnabled(true);
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	private void onSuspendTask()
	{
		this.getActionMap().get("task-go").setEnabled(true);
		this.getActionMap().get("task-stop").setEnabled(false);
		this.getActionMap().get("changes-save").setEnabled(true);
		this.getActionMap().get("record-insert").setEnabled(true);
		this.getActionMap().get("record-delete").setEnabled(true);
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	

	public void doRefreshStatus()
	{


		if(view.getRowCount() > 0){
			status.setText(	" record " + view.getLineAt(0) + " to " + view.getLineAt(view.getRowCount()-1) + " of " + view.getFlatRowCount() +
					" | changes " + view.getChanges().count());
		}
		else
			status.setText("0 records");
	}
	
	public void doRefreshStatus(boolean lastFetch)
	{
		if(view.getRowCount() > 0){
			String appendOf = !lastFetch ? "...." : ""+view.getFlatRowCount();
			status.setText(	" record " + view.getLineAt(0) + " to " + view.getLineAt(view.getRowCount()-1) + " of " + appendOf +
					" | changes " + view.getChanges().count());
		}
		else
			status.setText("0 records");
	}
	
	public void setStatus(String text)
	{
		status.setText(text);
	}
	
	private class ActionInsertRecord extends AbstractAction
	{
		ActionInsertRecord()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_CONTENT_INSERT));
			this.putValue(SHORT_DESCRIPTION, "insert record");
			this.putValue(NAME, "insert record");
		}

		public void actionPerformed(ActionEvent ae)
		{
			int row = ContentPane.this.view.getRow();
			int col = ContentPane.this.view.getColumn();
			
			ContentPane.this.view.insertRow(++row);
			ContentPane.this.doRefreshStatus();
			
			if(row == ContentModel.MAX_BLOCK_RECORDS)
			{
				row = 0;
			}
			ContentPane.this.view.setSelectedCell(row,(col == -1 ? 0 : col));
		}
	}
	
	private class ActionDeleteRecord extends AbstractAction
	{
		ActionDeleteRecord()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_CONTENT_DELETE));
			this.putValue(SHORT_DESCRIPTION, "delete record");
			this.putValue(NAME, "delete record");
		}

		public void actionPerformed(ActionEvent ae)
		{
			int row = ContentPane.this.view.getRow();
			int col = ContentPane.this.view.getColumn();
			
			if(row==-1) return;
			
			ContentPane.this.view.deleteRow(row);
			ContentPane.this.doRefreshStatus();
			
			if(ContentPane.this.view.getRowCount() == 0) return;

			if(row >= ContentPane.this.view.getRowCount()) row = ContentPane.this.view.getRowCount()-1;
			ContentPane.this.view.setSelectedCell(row,(col == -1 ? 0 : col));
		}
	}
	
	private class ActionSaveChanges extends AbstractAction
	{
		ActionSaveChanges()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_CONTENT_UPDATE));
			this.putValue(SHORT_DESCRIPTION, "apply changes to db");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ContentPane.this.getUpdateModel() !=null && ContentPane.this.getUpdateModel().getRowIdentifierCount() > 0)
			{
				doUpdate();
			}
			else
			{
				Application.alert(Application.PROGRAM,"No update criteria defined!");
			}
		}
	}
	
	private class ActionRelaunch extends AbstractAction
	{
		ActionRelaunch()
		{
			this.putValue(NAME, "relaunch query");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(!ContentPane.this.isBusy())
			{
				ContentPane.this.syntax.setText(ContentPane.this.getQuery());
				ContentPane.this.view.reset();
				ContentPane.this.doRetrieve();
			}
		}
	}
	
	public void relaunchQuery(){
		new ActionRelaunch().actionPerformed(null);
	}
	
	private class ActionStopTask extends AbstractAction
	{
		ActionStopTask()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_STOP));
		}

		public void actionPerformed(ActionEvent ae)
		{
			ContentPane.this.onEndTask();
		}
	}
}