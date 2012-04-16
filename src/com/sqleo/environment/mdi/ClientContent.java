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
import java.awt.event.KeyEvent;
import java.sql.ResultSetMetaData;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.environment.ctrl.content.DialogFilters;
import com.sqleo.environment.ctrl.content.DialogFindReplace;
import com.sqleo.environment.ctrl.content.DialogPreview;
import com.sqleo.environment.ctrl.content.DialogStream;
import com.sqleo.environment.ctrl.content.DialogUpdateCriteria;
import com.sqleo.environment.ctrl.content.UpdateModel;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.querybuilder.QueryModel;


public class ClientContent extends MDIClientWithCRActions
{
	public static final String DEFAULT_TITLE = "CONTENT";	
	public static final String PREVIEW_TITLE = "PREVIEW";	
	
	private ContentPane control;
	private JMenuItem[] m_actions;
	private Toolbar toolbar;
	
	private DialogFindReplace dlg;

	public ClientContent(TableMetaData tmd, boolean retrieve)
	{
		this(tmd.getHandlerKey(),tmd.createQueryModel(),tmd.createUpdateModel(),retrieve);
		
		if(!retrieve)
		{
			for(int i=0; i<tmd.getColumns().size(); i++)
			{
				String name = tmd.getColumnProperty(i,TableMetaData.IDX_COL_COLUMN_NAME);
				String type = tmd.getColumnProperty(i,TableMetaData.IDX_COL_DATA_TYPE);
				
				control.getView().addColumn(name,Integer.valueOf(type).intValue());
			}
			
			control.getView().onTableChanged(false);
			
			for(int i=0; i<tmd.getColumns().size(); i++)
			{
				String t = tmd.getColumnProperty(i,TableMetaData.IDX_COL_COLUMN_NAME) + " : " + tmd.getColumnProperty(i,TableMetaData.IDX_COL_TYPE_NAME);
				t = t + (Integer.valueOf(tmd.getColumnProperty(i,TableMetaData.IDX_COL_NULLABLE)).intValue() == ResultSetMetaData.columnNullable ? "(null)" : "(not null)");
				control.getView().setToolTipText(i,t);
			}			
			
			control.doRefreshStatus();
		}
	}
	
	public ClientContent(String keycah, QueryModel qmodel, UpdateModel umodel)
	{
		this(keycah,qmodel,umodel,true);
	}
	
	private boolean isContentChanged(){
		return ClientContent.this.control.getView().getChanges().count() > 0;
	}
	
	private ClientContent(String keycah, QueryModel qmodel, UpdateModel umodel, boolean retrieve)
	{
		this(new ContentPane(keycah,qmodel,umodel),retrieve);
	}
	public ClientContent(String keycah, String query, boolean retrieve)
	{
		this(new ContentPane(keycah,query),retrieve);
	}
	private ClientContent(ContentPane control2,boolean retrieve){
		super(DEFAULT_TITLE);
		setMaximizable(true);
		setResizable(true);
		
		control = control2;
		setComponentCenter(control);
		control.setBorder(new EmptyBorder(2,2,2,2));
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		InternalFrameListener ifl = new InternalFrameAdapter()
		{
			public void internalFrameDeactivated(InternalFrameEvent e)
			{
				if(ClientContent.this.dlg!=null)
					ClientContent.this.dlg.setVisible(false);
			}

			public void internalFrameClosing(InternalFrameEvent evt)
			{
				if(!ClientContent.this.control.isBusy()
				&& !ClientContent.this.control.isReadOnly()){
					
				  if(isContentChanged()){
					int option = JOptionPane.showConfirmDialog(Application.window,"do you want to apply changes to db?",Application.PROGRAM,JOptionPane.YES_NO_CANCEL_OPTION);
					if(option == JOptionPane.YES_OPTION){
						ClientContent.this.control.getActionMap().get("changes-save").actionPerformed(null);
					}
					else if (option != JOptionPane.NO_OPTION)
						return;
				  }
				}
				if(showAutoCommitPopup()==0){
					ClientContent.this.control.doStop();
					ClientContent.this.dispose();
				}
			}
		};
		addInternalFrameListener(ifl);
		
		if(control.getQueryModel()!=null){
			createToolbar();
		}
		initMenuActions();
		
		if(retrieve)
			control.doRetrieve();
		else
			control.doStop();
	}
    
	private int showAutoCommitPopup(){
		if(!ConnectionAssistant.getAutoCommitPrefered() && isContentChanged()){
			int acoption = JOptionPane.showConfirmDialog(Application.window,"Commit Y/N ?",Application.PROGRAM,JOptionPane.YES_NO_CANCEL_OPTION);
			if(acoption == JOptionPane.YES_OPTION){
				toolbar.getActionMap().get("action-commit").actionPerformed(null);
				//control.getActionMap().get("task-go").actionPerformed(null);
				return 0;
			}else if(acoption == JOptionPane.NO_OPTION){
				toolbar.getActionMap().get("action-rollback").actionPerformed(null);
				//control.getActionMap().get("task-go").actionPerformed(null);
				ClientContent.this.dispose();
				return 0;
			}
			else {
				return -1;
			}
		}
		return 0;
	}
	
	private void createToolbar()
	{
		toolbar = new Toolbar(Toolbar.HORIZONTAL);
		toolbar.add(control.getActionMap().get("changes-save"));
		toolbar.add(control.getActionMap().get("task-stop"));
		toolbar.addSeparator();
		toolbar.add(control.getActionMap().get("record-insert"));
		toolbar.add(control.getActionMap().get("record-delete"));
		toolbar.addSeparator();
		toolbar.add(new ActionShowFilter());
		toolbar.add(new ActionShowFindReplace());

		toolbar.addSeparator();
		Action commit = new ActionCommit(control.getHandlerKey().toString());
		Action rollback = new ActionRollback(control.getHandlerKey().toString());
		toolbar.getActionMap().put("action-commit",commit);
		toolbar.getActionMap().put("action-rollback",rollback);
		toolbar.add(commit);
		toolbar.add(rollback);
		toolbar.addSeparator();
		
		setComponentEast(toolbar);
	}
	@Override
	public void notifyResponseToView(boolean isCommitNotify){
		if(isCommitNotify){
			control.setStatus("\nCommit successfull!\n");
		}else{
			control.setStatus("\nRollback successfull!\n");
		}
		
	};
	private void initMenuActions()
	{
		Action a = control.getActionMap().get("task-go");
		a.putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		
		m_actions = new JMenuItem[]
		{
			MDIMenubar.createItem(new ActionShowUpdateCriteria()),
			MDIMenubar.createItem(new ActionShowChanges()),
			null,
			MDIMenubar.createItem(new ActionShowExport()),
			MDIMenubar.createItem(new ActionShowImport()),
			null,
			MDIMenubar.createItem(control.getActionMap().get("task-go"))
		};
		m_actions[0].setEnabled(control.getQueryModel()!=null);
		m_actions[1].setEnabled(control.getQueryModel()!=null);
	}
	
	public final void dispose()
	{
		if(ClientContent.this.dlg!=null) dlg.dispose();
		super.dispose();
	}
	
	public final ContentPane getControl()
	{
		return control;
	}	
	
	protected String getMessage()
	{
		int rows = control.getView().getRowCount();
		if(rows == 0) return null;
		
		String first = control.getView().getValueAt(0,0).toString();
		String last = control.getView().getValueAt(rows-1,0).toString();
		
		return "block " + control.getView().getBlock() + " of " + control.getView().getBlockCount() + " - record(s) " + first + " to " + last + " of " + control.getView().getFlatRowCount();
	}

	public JMenuItem[] getMenuActions()
	{
		return m_actions;
	}

	public Toolbar getSubToolbar()
	{
		return toolbar;
	}
    
	protected void setPreferences()
	{
	}

	private class ActionShowUpdateCriteria extends AbstractAction
	{
		ActionShowUpdateCriteria()
		{
			super("update criteria...");			
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			new DialogUpdateCriteria(ClientContent.this.control).setVisible(true);
		}
	}
		
	private class ActionShowChanges extends AbstractAction
	{
		ActionShowChanges()
		{
			super("show changes...");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			
			if(ClientContent.this.control.getUpdateModel() !=null && ClientContent.this.control.getUpdateModel().getRowIdentifierCount() > 0)
			{
				new DialogPreview(ClientContent.this.control).setVisible(true);
			}
			else
			{
				Application.alert(Application.PROGRAM,"No update criteria defined!");
			}			
		}
	}	

	private class ActionShowImport extends AbstractAction
	{
		ActionShowImport()
		{
			this.putValue(NAME, "import data...");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			DialogStream.showImport(ClientContent.this.control);
		}
	}
		
	private class ActionShowExport extends AbstractAction
	{
		ActionShowExport()
		{
			this.putValue(NAME, "export data...");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			DialogStream.showExport(ClientContent.this.control);
		}
	}
	
	private class ActionShowFilter extends AbstractAction
	{
		ActionShowFilter()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_FILTER));
			this.putValue(SHORT_DESCRIPTION, "filter");
			this.putValue(NAME, "filter...");
		}

		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			new DialogFilters(ClientContent.this.control).setVisible(true);
		}
	}	
	
	private class ActionShowFindReplace extends AbstractAction
	{
		ActionShowFindReplace()
		{
			this.putValue(SMALL_ICON, Application.resources.getIcon(Application.ICON_FIND));
			this.putValue(SHORT_DESCRIPTION, "find/replace...");
			this.putValue(NAME, "find/replace...");			
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			if(ClientContent.this.control.isBusy()) return;
			if(ClientContent.this.dlg == null)
				ClientContent.this.dlg = new DialogFindReplace(ClientContent.this.control);
			dlg.setVisible(true);
		}
	}
}
