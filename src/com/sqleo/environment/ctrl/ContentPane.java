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
 * short
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.sqleo.environment.ctrl;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.JdbcUtils;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.content.ContentModel;
import com.sqleo.environment.ctrl.content.ContentView;
import com.sqleo.environment.ctrl.content.TaskRetrieve;
import com.sqleo.environment.ctrl.content.TaskUpdate;
import com.sqleo.environment.ctrl.content.UpdateModel;
import com.sqleo.environment.mdi.ClientQueryBuilder;
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
	private Integer retrievedRowCount;
	private boolean update = false;
	private ClientQueryBuilder clientQB;
	private Color connectionBackgroundColor;
	
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
				
		this.getActionMap().put("task-go"		,new ActionRelaunch());
		this.getActionMap().put("changes-save"	,new ActionSaveChanges());
		this.getActionMap().put("record-insert"	,new ActionInsertRecord());
		this.getActionMap().put("record-delete"	,new ActionDeleteRecord());
		this.getActionMap().put("task-stop"		,new ActionStopTask());
		
		status = new JLabel("...");
		status.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(2,4,2,4)));
		
		JScrollPane scroll = new JScrollPane(syntax = new JTextArea());
		syntax.setRows(3);
		
		syntax.setText(query);
		syntax.setWrapStyleWord(true);
		syntax.setLineWrap(true);
		syntax.setEditable(false);
		syntax.setOpaque(false);
		
		final BorderLayoutPanel pnlSouth = new BorderLayoutPanel(2,2);
		pnlSouth.setComponentCenter(status);
		pnlSouth.setComponentEast(new JButton(new ActionCountRows()));
		pnlSouth.setComponentNorth(scroll);
		
		setComponentSouth(pnlSouth);
		setComponentCenter(view = new ContentView(this));		
	}
	
	private Statement countQueryStmt;
	private Statement exportPivotStmt;
	public void setExportPivotStmt(final Statement stmt){
		this.exportPivotStmt = stmt;
	}
	
	private class ActionCountRows extends AbstractAction {

		ActionCountRows(){this.putValue(NAME,I18n.getString("datacontent.menu.CountRecords","count records"));}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			new Thread((new Runnable() {
				@Override
				public void run() {
					
					int records = 0;
					ResultSet rs = null;
					try
					{
						toggleActions(true);
						
						String originalQuery = getQuery();
						// #344 remove last ORDER BY, that is not supported in MonetDB derived table
						int orderByPosition = originalQuery.toUpperCase().lastIndexOf("ORDER BY");
						if (orderByPosition > 0) originalQuery = originalQuery.substring(0,orderByPosition);
						String countQuery = "SELECT count(*) FROM ( " + originalQuery +" ) X ";
						countQuery = SQLHelper.getSQLeoFunctionQuery(countQuery,keycah);

						final ConnectionHandler ch = ConnectionAssistant.getHandler(keycah);
						countQueryStmt = ch.get().createStatement();
						
						rs = JdbcUtils.executeQuery(ch, countQuery, countQueryStmt);
						
						records = rs.next() ? rs.getInt(1) : 0;
						retrievedRowCount = Integer.valueOf(records);
						doRefreshStatus(false);
					}
					catch(SQLException sqle)
					{
						Application.println(sqle,true);
					}finally{
							try {
								if(rs!=null){
									rs.close();
								}
								if(countQueryStmt!=null){
									countQueryStmt.close();
								}
								countQueryStmt = null;
								toggleActions(false);
								JOptionPane.showMessageDialog(Application.window,I18n.getString("datacontent.message.TotalRecords","Total records count : ")+records);
							} catch (SQLException e) {
								Application.println(e, true);
							}
					}
					
				}
			})).start();
			
		}
		
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
	
	public void setQuery(String query){
		this.query = query;
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
		retrievedRowCount = null;
		retrievingTask = new TaskRetrieve(this);
		onBeginTask(retrievingTask);
	}
	
	public void fetchNextRecords(){
		if(retrievingTask!=null){
			onResumeTask();
			retrievingTask.setNextResultSet();
		}
	}
	
	public boolean areAllRowsFetched(){
		return retrievingTask!=null? retrievingTask.areAllRowsFetched(): false;
	}
	
	public void doRetrieve(int limit)
	{
		onBeginTask(new TaskRetrieve(this,limit));
	}
	
	public void doUpdate()
	{
		update = true;
		onBeginTask(new TaskUpdate(this));
	}
	
	public void toggleActions(final boolean taskRunning){
		
		this.getActionMap().get("task-go").setEnabled(!taskRunning);
		this.getActionMap().get("task-stop").setEnabled(taskRunning);
		this.getActionMap().get("changes-save").setEnabled(!taskRunning);
		this.getActionMap().get("record-insert").setEnabled(!taskRunning);
		this.getActionMap().get("record-delete").setEnabled(!taskRunning);
		
	}
	
	private void onBeginTask(Runnable r)
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		toggleActions(true);
		
		task = new Thread(r);
		task.start();		
	}
	
	private void onResumeTask()
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		toggleActions(true);
	}
	
	private void closeRetrievingTask(){
		if(retrievingTask!=null){
			try {
				retrievingTask.close();
			} catch (Exception e) {
				Application.println(e, true);
			}
		}
	}
	
	private void closeStatement(final Statement stmt){
		JdbcUtils.cancelAndCloseStatement(stmt);
		toggleActions(false);
	}
	
	
	private void onEndTask()
	{
		if(countQueryStmt!=null){
			closeStatement(countQueryStmt);
			countQueryStmt = null;
			return;
		}
		if(exportPivotStmt!=null){
			closeStatement(exportPivotStmt);
			exportPivotStmt = null;
			return;
		}
		
		closeRetrievingTask();
		
		task = null;
		retrievingTask = null;
		
		toggleActions(false);
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		if(update){
			update = false;
			this.getActionMap().get("task-go").actionPerformed(null);
		}
	}
	
	private void onSuspendTask()
	{
		toggleActions(false);
		
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
			if(retrievedRowCount!=null){
				appendOf =""+retrievedRowCount;
			}
			status.setText(	I18n.getString("datacontent.Record","record ") + view.getLineAt(0) + " ... " + view.getLineAt(view.getRowCount()-1) + " / " + appendOf +
					"  | " + I18n.getString("datacontent.Changes","changes ") + view.getChanges().count());
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
			this.putValue(SHORT_DESCRIPTION, I18n.getString("datacontent.InsertRecord","insert record"));
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
			this.putValue(SHORT_DESCRIPTION, I18n.getString("datacontent.DeleteRecord","delete record"));
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
			this.putValue(SHORT_DESCRIPTION, I18n.getString("datacontent.ApplyChangesToDB","apply changes to db"));
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ContentPane.this.getUpdateModel() !=null &&
					(ContentPane.this.getView().getChanges().existsOnlyInsert()
							||ContentPane.this.getUpdateModel().getRowIdentifierCount() > 0) ){
					doUpdate();
			}
			else
			{
				Application.alert(Application.PROGRAM,I18n.getString("datacontent.message.NoUpdateCriteria","No update criteria defined!"));
			}
		}
	}
	
	private class ActionRelaunch extends AbstractAction
	{
		ActionRelaunch()
		{
			this.putValue(NAME, I18n.getString("datacontent.menu.RelaunchQuery","relaunch query"));
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
	
	public void setClientQB(ClientQueryBuilder clientQB) {
		this.clientQB = clientQB;
	}
	public ClientQueryBuilder getClientQB() {
		return clientQB;
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

	public void setBackgroundColor(Color connectionBackgroundColor) {
		this.connectionBackgroundColor = connectionBackgroundColor;
	}
	public Color getBackgroundColor(){
		return this.connectionBackgroundColor;
	}
}