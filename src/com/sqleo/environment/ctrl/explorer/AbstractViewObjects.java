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

package com.sqleo.environment.ctrl.explorer;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.ListView;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.content.AbstractActionContent;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.environment.ctrl.editor.DialogCommand;
import com.sqleo.environment.mdi.ClientCommandEditor;
import com.sqleo.environment.mdi.ClientContent;
import com.sqleo.environment.mdi.ClientDefinition;
import com.sqleo.environment.mdi.ClientMetadataExplorer;
import com.sqleo.environment.mdi.ClientQueryBuilder;
import com.sqleo.environment.mdi.MDIActions;
import com.sqleo.environment.mdi.MDIClient;
import com.sqleo.querybuilder.DiagramLayout;
import com.sqleo.querybuilder.DiagramLoader;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.beans.Entity;
import com.sqleo.querybuilder.syntax.QueryTokens;



public abstract class AbstractViewObjects extends ListView implements MouseListener
{
	private JLabel info;
	private JLabel counter;
	
	protected AbstractViewObjects()
	{
		addMouseListener(this);
		
		BorderLayoutPanel statusbar = new BorderLayoutPanel(3,0);
		setComponentSouth(statusbar);
		
		statusbar.setComponentCenter(info = new JLabel("..."));
		info.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(2,4,2,4)));
		
		statusbar.setComponentEast(counter = new JLabel("Objects : 0"));
		counter.setBorder(new CompoundBorder(LineBorder.createGrayLineBorder(), new EmptyBorder(2,4,2,4)));
	}
	
	public void addRow(Object[] rowdata)
	{
		super.addRow(rowdata);
		counter.setText("Objects : " + this.getRowCount());
	}
	
	public void reset()
	{
		super.reset();
		
		if(counter!=null)
			counter.setText("Objects : 0");
	}
	
	protected void setInfo(String s)
	{
		info.setText(s);
	}
	
	protected abstract String getHandlerKey();
	
	/* bad */
	protected Connection getConnection()
	{
		return ConnectionAssistant.hasHandler(getHandlerKey()) ? ConnectionAssistant.getHandler(getHandlerKey()).get() : null;
	}
	
	private int findColumn(String name)
	{
		for(int i=0; i<getJavaComponent().getColumnModel().getColumnCount(); i++)
		{
			String cname = getJavaComponent().getColumnModel().getColumn(i).getHeaderValue().toString();
			if(cname.equalsIgnoreCase(name)) return i;
		}
		
		return -1;
	}
	
	private static DefaultMutableTreeNode findChild(DefaultMutableTreeNode parent, String label, boolean like)
	{
		for(int i=0; i<parent.getChildCount(); i++)
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)parent.getChildAt(i);
			
			if(like && child.getUserObject().toString().startsWith(label)) return child;
			if(!like && child.getUserObject().toString().equals(label)) return child;
		}
			
		return null;			
	}	
	
	protected String getTableSchema()
	{
		int idx = findColumn("TABLE_SCHEM");
		if(idx == -1) return null;
		
		Object schema	= this.getValueAt(this.getSelectedRow(),idx);
		return schema == null ? null : schema.toString();
	}
	
	protected String getTableName()
	{
		int idx = findColumn("TABLE_NAME");
		if(idx == -1) return null;
		
		Object name	= this.getValueAt(this.getSelectedRow(),idx);
		return name == null ? null : name.toString();
	}
	
	protected String getTableType()
	{
		int idx = findColumn("TABLE_TYPE");
		if(idx == -1) return null;
		
		Object type	= this.getValueAt(this.getSelectedRow(),idx);
		return type == null ? null : type.toString();
	}
	
	public void mouseClicked(MouseEvent me)
	{
		if(me.getClickCount() == 2)
		{
			if(this.getSelectedRow() == -1) return;
			
			if(this.getConnection() == null)
				Application.alert(Application.PROGRAM,"No connection!");
			else
				new ActionShowContent().actionPerformed(null);
		}
	}

	public void mouseEntered(MouseEvent me){}
	public void mouseExited(MouseEvent me){}
	public void mousePressed(MouseEvent me){}

	public void mouseReleased(MouseEvent me)
	{
		if(SwingUtilities.isRightMouseButton(me))
		{
			int row = getJavaComponent().rowAtPoint(me.getPoint());
			getJavaComponent().setRowSelectionInterval(row,row);
			
			JPopupMenu popup = new JPopupMenu();
			popup.add(new ActionQuery());
			
			MDIClient [] queryWindows = getQueryWindows();
			JMenu submenu = new JMenu(I18n.getString("metadataexplorer.menu.AddtoQuery","Add to query"));
			submenu.setEnabled(false);
			
			if(queryWindows.length>0) {
				submenu.setEnabled(true);
				ActionAppendQuery q = new ActionAppendQuery();
				for(int i = 0; i < queryWindows.length ; i++){
					JMenuItem item = new JMenuItem(queryWindows[i].getTitle());
					item.setActionCommand(queryWindows[i].getName());
					item.addActionListener(q);
					submenu.add(item);
					
				}
			}
			
			popup.add(submenu);
			
			popup.add(new ActionAddLink());
			popup.addSeparator();
			popup.add(new ActionCommand());
			popup.addSeparator();
			popup.add(new ActionDeleteContent());
			popup.add(new ActionDropObject());
			popup.addSeparator();
			popup.add(new ActionShowContent());
			popup.add(new ActionShowDefinition());
			
			popup.show(getJavaComponent(),me.getX(),me.getY());
		}
	}
	
//	/////////////////////////////////////////////////////////////////////////////
//	Popup Actions
//	/////////////////////////////////////////////////////////////////////////////
	protected class ActionQuery extends AbstractAction implements Runnable
	{
		ActionQuery()
		{
 			super(I18n.getString("metadataexplorer.menu.AddtoNewQuery","Add to new query"));
		}
		
		public void run()
		{
			AbstractViewObjects.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			String schema	= AbstractViewObjects.this.getTableSchema();
			String name		= AbstractViewObjects.this.getTableName();
		
			QueryTokens.Table table = new QueryTokens.Table(Preferences.getBoolean("querybuilder.use-schema") ? schema : null,name);
			if(QueryBuilder.autoAlias && table.getAlias()==null) table.setAlias(table.getName());
			
			DiagramLayout layout = new DiagramLayout();
			if(!Preferences.getBoolean("querybuilder.use-schema"))
				layout.getQueryModel().setSchema(schema);
			layout.getQueryModel().getQueryExpression().getQuerySpecification().addFromClause(table);			
			
			if(QueryBuilder.selectAllColumns)
			{
				TableMetaData tmd = new TableMetaData(AbstractViewObjects.this.getHandlerKey(),schema,name);
				ArrayList al = tmd.getColumns();
				for(int i=0; i<al.size(); i++)
				{
					QueryTokens.Column column = new QueryTokens.Column(table,tmd.getColumnProperty(i,TableMetaData.IDX_COL_COLUMN_NAME));
					if (QueryBuilder.autoAlias)
					{
						String alias = column.getTable().getAlias() + "." + column.getName();

						if (alias.length() > QueryBuilder.maxColumnNameLength && QueryBuilder.maxColumnNameLength > 0)
							alias = alias.substring(0, QueryBuilder.maxColumnNameLength);

						column.setAlias(alias);
					}					
					
					layout.getQueryModel().getQueryExpression().getQuerySpecification().addSelectList(column);
				}
			}
			
			ClientQueryBuilder client = new ClientQueryBuilder(AbstractViewObjects.this.getHandlerKey());
			Application.window.add(client);
			client.setDiagramLayout(layout);
			
			AbstractViewObjects.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			new Thread(this).start();
		}
	}
	
	private MDIClient[] getQueryWindows(){
		return Application.window.getClientsOfConnection(ClientQueryBuilder.DEFAULT_TITLE,AbstractViewObjects.this.getHandlerKey());
	}
	
	protected class ActionAppendQuery extends AbstractAction implements Runnable
	{
		private String queryWindow;
		public void run()
		{
			AbstractViewObjects.this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			String schema	= null;
			String name		= AbstractViewObjects.this.getTableName();
			
			ClientQueryBuilder client = (ClientQueryBuilder) Application.window.getClient(queryWindow);
			Application.window.showClient(client);
			if(Preferences.getBoolean("querybuilder.use-schema")){
				schema = AbstractViewObjects.this.getTableSchema();
			}
			QueryTokens.Table token = new QueryTokens.Table(schema,name);
			DiagramLoader.run(DiagramLoader.DEFAULT,client.getQueryBuilder(), token, true);
			
			AbstractViewObjects.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			queryWindow =  e.getActionCommand();
			new Thread(this).start();
		}
	}


	protected class ActionAddLink extends AbstractAction
	{
		ActionAddLink()
		{
			super(I18n.getString("metadataexplorer.menu.AddtoGroup","Add to group..."));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int iDot = AbstractViewObjects.this.getHandlerKey().indexOf('.');
			int iAt = AbstractViewObjects.this.getHandlerKey().indexOf('@');
			
			String driver = AbstractViewObjects.this.getHandlerKey().substring(0,iDot);
			String datasource = AbstractViewObjects.this.getHandlerKey().substring(iDot+1,iAt);
			
			ClientMetadataExplorer cme = (ClientMetadataExplorer)Application.window.getClient(ClientMetadataExplorer.DEFAULT_TITLE);
			
			DefaultMutableTreeNode parent = cme.getControl().getNavigator().getRootNode();
			parent = AbstractViewObjects.findChild(parent,driver,false);
			
			DefaultMutableTreeNode node = AbstractViewObjects.findChild(parent,datasource,false);
			if(node==null) node = AbstractViewObjects.findChild(parent,datasource,true);
			UoLinks uoLk = (UoLinks)node.getLastLeaf().getUserObject();
			
			Object group = null;
			if(uoLk.getGroups().size() == 0)
				group = Application.input(Application.PROGRAM,"Group name:");
			else
				group = JOptionPane.showInputDialog(Application.window,"Choose group:",Application.PROGRAM,JOptionPane.PLAIN_MESSAGE,null,uoLk.getGroups().toArray(),null);
			
			if(group!=null)
				uoLk.add(group.toString(),AbstractViewObjects.this.getTableSchema(),AbstractViewObjects.this.getTableName(),AbstractViewObjects.this.getTableType());
		}
	}

	protected class ActionDeleteContent extends AbstractActionContent
	{
		ActionDeleteContent(){this.putValue(NAME,I18n.getString("application.tool.delete","Delete content"));}
		
		protected boolean isShowCountRecordsPopup(){
			return true;
		}
		
		protected TableMetaData getTableMetaData()
		{
			return new TableMetaData(AbstractViewObjects.this.getHandlerKey(),
									 AbstractViewObjects.this.getTableSchema(),
									 AbstractViewObjects.this.getTableName(),
									 AbstractViewObjects.this.getTableType());
		}

		protected void onActionPerformed(int records, int option)
		{
			if(option != JOptionPane.YES_OPTION) return;
			
			try
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(this.getTableMetaData().getHandlerKey());
				Statement stmt = ch.get().createStatement();
				stmt.executeUpdate("DELETE FROM " + this.getTableMetaData());
				stmt.close();
			}
			catch(SQLException sqle)
			{
				Application.println(sqle,true);
			}
		}
	}

	protected class ActionDropObject extends AbstractActionContent
	{
		ActionDropObject(){this.putValue(NAME,I18n.getString("application.tool.drop","Drop <object>"));}
		
		protected boolean isShowCountRecordsPopup(){
			return true;
		}
		
		protected TableMetaData getTableMetaData()
		{
			return new TableMetaData(AbstractViewObjects.this.getHandlerKey(),
									 AbstractViewObjects.this.getTableSchema(),
									 AbstractViewObjects.this.getTableName(),
									 AbstractViewObjects.this.getTableType());
		}

		protected void onActionPerformed(int records, int option)
		{
			if(option != JOptionPane.YES_OPTION) return;
			
			try
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(this.getTableMetaData().getHandlerKey());
				Statement stmt = ch.get().createStatement();
				stmt.execute("DROP " + this.getTableMetaData().getType() + " " + this.getTableMetaData());
				stmt.close();
				
				AbstractViewObjects.this.removeSelectedRow();
			}
			catch(SQLException sqle)
			{
				Application.println(sqle,true);
			}
		}
	}

	protected class ActionShowContent extends AbstractActionContent
	{
		ActionShowContent(){this.putValue(NAME,I18n.getString("application.tool.content","Show content"));}
		
		protected boolean isShowCountRecordsPopup(){
			return false;
		}
		

		protected TableMetaData getTableMetaData()
		{
			return new TableMetaData(AbstractViewObjects.this.getHandlerKey(),
									 AbstractViewObjects.this.getTableSchema(),
									 AbstractViewObjects.this.getTableName(),
									 AbstractViewObjects.this.getTableType());
		}
		
		protected void onActionPerformed(int records, int option)
		{
			if((option != JOptionPane.YES_OPTION && option != JOptionPane.NO_OPTION)
			|| (records == 0 && option == JOptionPane.NO_OPTION)) return;
			
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

	protected class ActionShowDefinition extends AbstractAction
	{
		ActionShowDefinition()
		{
			super(I18n.getString("application.tool.definition","Show definition"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			String type 	= AbstractViewObjects.this.getTableType();
			String schema	= AbstractViewObjects.this.getTableSchema();
			String name		= AbstractViewObjects.this.getTableName();
			
			Application.window.add(new ClientDefinition(AbstractViewObjects.this.getHandlerKey(), new QueryTokens.Table(schema,name), type));
		}
	}

	protected class ActionCommand extends MDIActions.ShowCommandEditor
	{
		ActionCommand()
		{
			setText(I18n.getString("application.tool.NewCommand","New command..."));
			setAccelerator(null);
			setIcon(null);
		}

		public void actionPerformed(ActionEvent e)
		{
			super.actionPerformed(e);
			
			String schema	= getTableSchema();
			String name		= getTableName();
			String keycah	= AbstractViewObjects.this.getHandlerKey();
		
			((ClientCommandEditor)Application.window.getClient(ClientCommandEditor.DEFAULT_TITLE)).setActiveConnection(keycah);			
			new DialogCommand(AbstractViewObjects.this.getHandlerKey(),new QueryTokens.Table(schema,name)).setVisible(true);
		}
	}
}