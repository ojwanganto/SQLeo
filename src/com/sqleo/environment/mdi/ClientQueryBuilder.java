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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.Toolbar;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.I18n;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.environment.ctrl.content.UpdateModel;
import com.sqleo.environment.ctrl.define.TableMetaData;
import com.sqleo.environment.ctrl.editor.DialogFindReplace;
import com.sqleo.environment.io.FileStreamSQL;
import com.sqleo.environment.io.FileStreamXLQ;
import com.sqleo.querybuilder.DiagramLayout;
import com.sqleo.querybuilder.QueryActions;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class ClientQueryBuilder extends MDIClient {
	public static final String DEFAULT_TITLE = "QUERY";
	public static int counter = 0;

	private QueryBuilder builder;
	private ClientContent client;
	private BorderLayoutPanel previewPanel;
	private JMenuItem[] m_actions;
	private Toolbar toolbar;

	private String keycah = null;
	private String filename = null;
	private String schema = null;
	private DialogFindReplace dlg;
	private JSplitPane splitPane;
	private boolean previewAlreadyLoadedOnce = false;
	
	public DialogFindReplace getFindReplaceDialog(){
		return dlg;
	}
	public void setFindReplaceDialog(final DialogFindReplace dlg){
		this.dlg = dlg;
	}

	public QueryBuilder getQueryBuilder() {
		return builder;
	}
	public ClientQueryBuilder(String keycah) {
		this(keycah, null);
	}
	public ClientQueryBuilder(String keycah,String schema) {
		super(DEFAULT_TITLE);
		setMaximizable(true);
		setResizable(true);
		builder = new QueryBuilder();
		builder.setClientQueryBuilder(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	
		previewPanel = new BorderLayoutPanel();
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.setLeftComponent(builder);
		splitPane.setRightComponent(previewPanel);
		setComponentCenter(splitPane);
		this.keycah = keycah;

		createToolbar();
		initMenuActions();

		if (keycah != null) {
			builder.setConnection(ConnectionAssistant.getHandler(keycah).get());
			builder.setConnectionHandlerKey(keycah);
			loadPrefixTree(keycah,schema);
			builder.setBackgroundColor(getConnectionBackgroundColor(keycah));
		}
		setFileName(null);

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated(InternalFrameEvent ife) {
				ClientQueryBuilder.this.setQueryParameters();
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				openSaveQueryDialog();
			}
		});
		
		this.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent evt)
			{
				adjustSplitPaneDivider();
			}
		});
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
			    new PropertyChangeListener() {
			        @Override
			        public void propertyChange(PropertyChangeEvent pce) {
			        	 SwingUtilities.invokeLater(new Runnable() {
			        		 @Override
			                 public void run() {
			        			 BorderLayoutPanel designer = (BorderLayoutPanel)getQueryBuilder().getComponentAt(0);
					             JSplitPane split = (JSplitPane)designer.getComponent(0);
					             JSplitPane split2 = (JSplitPane)split.getLeftComponent();
					             split2.setDividerLocation(1.0);
				    			 split2.validate();
			        		 }
			        	 });
			        }
			});
		
		adjustSplitPaneDivider();
		
	}
	
	private void adjustSplitPaneDivider() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	BorderLayoutPanel designer = (BorderLayoutPanel)getQueryBuilder().getComponentAt(0);
            	JSplitPane split = (JSplitPane)designer.getComponent(0);
            	JSplitPane split2 = (JSplitPane)split.getLeftComponent();
            	if(client!=null){
            		if(!previewAlreadyLoadedOnce){
            			previewAlreadyLoadedOnce = true;
            			splitPane.setDividerLocation(0.66);
            			splitPane.validate();
            		}
    				split2.setDividerLocation(1.0);
    				split2.validate();
            	}else{
            		splitPane.setDividerLocation(1.0);
            		splitPane.validate();
            		split2.setDividerLocation(0.5);
            		split2.validate();
            	}
            }
        });
    }
	
	private void openSaveQueryDialog(){
		int selectLength = _ReservedWords.SELECT.length();
		if(builder.getSyntax().getDocument().getLength()>selectLength ||
				builder.getDiagramLayout().getQueryModel().toString().length() > selectLength ){
			int option = JOptionPane.showConfirmDialog(Application.window,I18n.getString("application.message.saveQuery","Do you want to save query to a file ?"),Application.PROGRAM,JOptionPane.YES_NO_CANCEL_OPTION);
			if(option == JOptionPane.YES_OPTION){
				toolbar.getActionMap().get("save").actionPerformed(null);
			}
			if(option != JOptionPane.CANCEL_OPTION)
				ClientQueryBuilder.this.dispose();
		} else
			ClientQueryBuilder.this.dispose();
	}
	private void createToolbar() {
		JButton btn = new JButton(builder.getActionMap().get(
				QueryActions.DIAGRAM_SAVE_AS_IMAGE));
		btn.setIcon(Application.resources
				.getIcon(Application.ICON_DIAGRAM_SAVE));
		btn.setToolTipText(I18n.getString("querybuilder.action.saveDiagramAsImage","save as image"));
		btn.setText(null);

		toolbar = new Toolbar(Toolbar.HORIZONTAL);
		ActionLaunch actionExecuteQuery = new ActionLaunch();
		toolbar.add(actionExecuteQuery);
		Action saveAction = new ActionSave();
		toolbar.getActionMap().put("save",saveAction);
		toolbar.add(saveAction);
		toolbar.add(btn);
		toolbar.addSeparator();
		Action dialogFindReplaceAction = builder.getActionMap().get(
				QueryActions.FIND_AND_REPLACE);
		toolbar.add(dialogFindReplaceAction);
		builder.getSyntax().getViewInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK),
				dialogFindReplaceAction);
		builder.getSyntax().getViewInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
				actionExecuteQuery);
		
		toolbar.addSeparator();

		setComponentEast(toolbar);
	}

	private void initMenuActions() {
		JCheckBoxMenuItem cbxm = new JCheckBoxMenuItem(builder.getActionMap()
				.get(QueryActions.FIELDS_DRAGGABLE));
		cbxm.setSelected(builder.isDragAndDropEnabled());

		m_actions = new JMenuItem[] {
				cbxm,
				null,
				MDIMenubar.createItem(builder.getActionMap().get(
						QueryActions.ENTITIES_PACK)),
				MDIMenubar.createItem(builder.getActionMap().get(
						QueryActions.ENTITIES_ARRANGE_GRID)),
				MDIMenubar.createItem(builder.getActionMap().get(
						QueryActions.ENTITIES_ARRANGE_SPRING)),
				MDIMenubar.createItem(builder.getActionMap().get(
						QueryActions.ENTITIES_REMOVE)),
				null,
				MDIMenubar.createItem(builder.getActionMap().get(
						QueryActions.COPY_SYNTAX)), };
	}

	@Override
	public JMenuItem[] getMenuActions() {
		return m_actions;
	}

	@Override
	public Toolbar getSubToolbar() {
		return toolbar;
	}

	@Override
	protected void setPreferences() {
	}

	public final void setFileName(String filename) {
		this.filename = filename;

		String filename2 = filename == null ? "<Untitled" + ++counter + ">"
				: filename;
		super.setTitle(getID() + " - " + DEFAULT_TITLE + " : " + filename2
				+ " : " + keycah);
	}
	public String getFilename(){
		return filename;
	}
	public boolean isSQLFile(){
		return filename!=null && filename.toLowerCase().endsWith(".sql");
	}
	public void setSchema(String schema){
		this.schema = schema;
	}
	public String getSchema(){
		return schema;
	}

	public final void setDiagramLayout(DiagramLayout layout) {
		builder.setDiagramLayout(layout);
	}

	private void setQueryParameters() {
		if (ConnectionAssistant.hasHandler(keycah)) {
			QueryBuilder.identifierQuoteString = ConnectionAssistant
					.getHandler(keycah).getObject("$identifierQuoteString")
					.toString();
			QueryBuilder.maxColumnNameLength = ((Integer) ConnectionAssistant
					.getHandler(keycah).getObject("$maxColumnNameLength"))
					.intValue();
		}
	}

	private class ActionLaunch extends MDIActions.AbstractBase {
		private ActionLaunch() {
			super(I18n.getString("application.launchQuery", "Launch query"));
			setIcon(Application.ICON_QUERY_LAUNCH);
			setTooltip(I18n
					.getString("application.launchQuery", "Launch query"));
		}
		
		private void onLaunchFromDesigner(String subtitle){
			UpdateModel um = null;
			QueryModel qm = null;
			try {
				qm = (QueryModel) ClientQueryBuilder.this.builder
						.getQueryModel().clone();
			} catch (CloneNotSupportedException cnse) {
				qm = ClientQueryBuilder.this.builder.getQueryModel();
			}

			if (qm.getQueryExpression().getQuerySpecification().getFromClause().length == 1
					&& qm.getQueryExpression().getQuerySpecification()
							.getFromClause()[0] instanceof QueryTokens.Table) {
				QueryTokens.Table qtoken = (QueryTokens.Table) qm
						.getQueryExpression().getQuerySpecification()
						.getFromClause()[0];
				TableMetaData tmd = new TableMetaData(
						ClientQueryBuilder.this.keycah, qtoken.getSchema(),
						qtoken.getName());
				um = tmd.createUpdateModel();
				um.getTable().setAlias(qtoken.getAlias());

				QueryTokens._Expression[] e = qm.getQueryExpression()
						.getQuerySpecification().getSelectList();
				for (int i = 0; i < um.getRowIdentifierCount(); i++) {
					QueryTokens.Column cpk = um.getRowIdentifier(i);
					for (int j = 0; j < e.length; j++) {
						if (e[j] instanceof QueryTokens.Column) {
							QueryTokens.Column c = (QueryTokens.Column) e[j];
							if (cpk.getName().equalsIgnoreCase(c.getName())) {
								cpk.setAlias(c.getAlias());
							}
						}
					}
				}
			}
			onLaunch(subtitle, new ClientContent(ClientQueryBuilder.this.keycah, qm, um));
		}
		
		private void onLaunchFromSyntax(String subtitle){
			if(ClientQueryBuilder.this.builder.hasMultipleQueries()){
				Application.alert(I18n.getString("querybuilder.message.multipleQueries", "Cannot execute multiple queries"));
				return;
			}
			final String query = ClientQueryBuilder.this.builder.getSyntax().getText().trim();
			onLaunch(subtitle, new ClientContent(ClientQueryBuilder.this.keycah, query,true));
		}
		
		private void onLaunch(String subtitle,ClientContent newClient){
			Application.session.addSQLToHistory(new SQLHistoryData(new Date(), 
					ClientQueryBuilder.this.keycah, "QueryBuilder", newClient.getControl().getQuery().toString()));
			Vector<Integer> prevColWidths = null;
			if(client!=null){
				client.getControl().getView().cacheColumnWidths();
				if(!client.getControl().getView().getColumnWidths().isEmpty()){
					prevColWidths = new Vector<Integer>(client.getControl().getView().getColumnWidths());
				}
				client.dispose();
			}
			client = newClient;
			client.setTitle(ClientContent.PREVIEW_TITLE+" : " + subtitle);
			
			//Application.window.add(client); //adds client as window
			
			// adds the content and buttons to previewpanel
			previewPanel.removeAll();
			//add content toolbar
			previewPanel.setComponentNorth(client.getContentPane().getComponent(1)); 
			//add content view
			ContentPane content = (ContentPane)client.getContentPane().getComponent(0);
			content.setClientQB(ClientQueryBuilder.this);
			if(prevColWidths!=null){
				content.getView().setColumnWidths(prevColWidths);
			}
			//remove sql status component
			BorderLayoutPanel pnlSouth = (BorderLayoutPanel)content.getComponent(0);
			pnlSouth.remove(pnlSouth.getComponent(2));
			previewPanel.setComponentCenter(content);
			
			//append client menu actions inside builder menu actions
			int len1 = getMenuActions().length;
			int len2 = client.getMenuActions().length;
			if(len1 == 8){
				JMenuItem[] allMenuItems = new JMenuItem[len1+len2];
				System.arraycopy(getMenuActions(), 0, allMenuItems, 0, len1);
			    System.arraycopy(client.getMenuActions(), 0, allMenuItems, len1, len2);
			    m_actions = allMenuItems;
			}else{
				System.arraycopy(client.getMenuActions(), 0, m_actions, 8, len2);
			}
			Application.window.menubar.internalFrameActivated(
					new InternalFrameEvent(ClientQueryBuilder.this,
					0));
			previewPanel.revalidate();
			previewPanel.repaint();
			adjustSplitPaneDivider();

		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ClientQueryBuilder.this.builder.getConnection() == null) {
				Application.alert(Application.PROGRAM, "No connection!");
				return;
			}

			int pos = getTitle().indexOf(":");
			String subtitle = getTitle().substring(pos);
			if(ClientQueryBuilder.this.builder.getSelectedIndex()==0){
				onLaunchFromDesigner(subtitle);
			}else{
				onLaunchFromSyntax(subtitle);
			}
		}
	}

	public class ActionSave extends MDIActions.AbstractBase {
		private ActionSave() {
			setText(I18n.getString("application.saveQuery", "Save query"));
			setIcon(Application.ICON_SAVE);
			setTooltip(I18n.getString("application.saveQuery", "Save query"));
		}

		private void saveAs() {
			ClientQueryBuilder.this.builder.getDiagramLayout().freeze();
			ClientQueryBuilder.this.builder.getQueryModel().resetExtrasMap(ClientQueryBuilder.this.builder.getDiagramLayout().getExtras());
			Object[] ret = DialogQuery.showSave(ClientQueryBuilder.this.builder);
			if (ret[0] != null) {
				setFileName(ret[0].toString());
			}
		}

		private void replace() {
			try {
				String fn = ClientQueryBuilder.this.filename;
				if (fn.endsWith(".sql")) {
					if(ClientQueryBuilder.this.builder.getSelectedIndex()==1){
						//save from syntax view
						FileStreamSQL.writeSQL(fn,ClientQueryBuilder.this.builder.getSyntax().getText());
					}else {
						ClientQueryBuilder.this.builder.getDiagramLayout().freeze();
						ClientQueryBuilder.this.builder.getQueryModel().resetExtrasMap(ClientQueryBuilder.this.builder.getDiagramLayout().getExtras());
						QueryModel qm = ClientQueryBuilder.this.builder.getQueryModel();
						//save from design view
						FileStreamSQL.write(fn, qm);
					}
					
				} else {
					if (!fn.endsWith(".xlq")) {
						fn += ".xlq";
					}
					FileStreamXLQ.write(fn,
							ClientQueryBuilder.this.builder.getDiagramLayout());
				}
				setFileName(fn);
			} catch (Exception e) {
				Application.println(e, true);
				e.printStackTrace();
			}
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			if (ClientQueryBuilder.this.filename == null) {
				saveAs();
			} else {
				String message = I18n.getFormattedString(
						"application.message.replaceFile",
						"{0}\nReplace existing file?", new Object[] { ""
								+ ClientQueryBuilder.this.filename });
				int ret = JOptionPane
						.showConfirmDialog(Application.window, message,
								"query.save", JOptionPane.YES_NO_CANCEL_OPTION);

				if (ret == JOptionPane.YES_OPTION) {
					replace();
				} else if (ret == JOptionPane.NO_OPTION) {
					saveAs();
				}
			}
		}
	}
}