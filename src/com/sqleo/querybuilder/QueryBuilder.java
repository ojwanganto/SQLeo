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

package com.sqleo.querybuilder;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.TextView;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.ClientQueryBuilder;
import com.sqleo.querybuilder.BrowserItems.DefaultTreeItem;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Column;
import com.sqleo.querybuilder.syntax.QueryTokens.Condition;
import com.sqleo.querybuilder.syntax.QueryTokens.DefaultExpression;
import com.sqleo.querybuilder.syntax.SQLFormatter;
import com.sqleo.querybuilder.syntax.SQLParser;
import com.sqleo.querybuilder.syntax.SubQuery;
import com.sqleo.querybuilder.syntax.DerivedTable;


public class QueryBuilder extends JTabbedPane implements ChangeListener
{
	private Connection connection;
	
	private boolean loading = false;
	
	public static boolean autoJoin = true;
	public static boolean autoAlias = true;
	public static boolean useAlwaysQuote = true;

	/* querybuilder.objetctype.TABLE */
	public static boolean loadObjectsAtOnce = true;
	public static boolean selectAllColumns = true;
	
	public static String identifierQuoteString 	= "\"";
	public static int maxColumnNameLength = 0;
	
	private DiagramLayout layout;
	private ClientQueryBuilder cqb;
	
	private TextView syntax;
	ViewBrowser browser;
	ViewDiagram diagram;
	ViewObjects objects;
	
	private String keycah;

	public QueryBuilder()
	{
		this(null);
	}

	public QueryBuilder(Connection connection)
	{
		super(JTabbedPane.BOTTOM);
		
		QueryActions.init(this);
		
		this.initComponents();
		this.setConnection(connection);
		
		this.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent evt)
			{
				BorderLayoutPanel designer = (BorderLayoutPanel)QueryBuilder.this.getComponentAt(0);
				JSplitPane split = (JSplitPane)designer.getComponent(0);
				JSplitPane split2 = (JSplitPane)split.getLeftComponent();
                // Value changed to 0.5 by Giulio Toffoli
				split2.setDividerLocation(0.5);
				split2.validate();
			}
		});
		
		this.transferFocus();
	}
	
	private void initComponents()
	{
		browser = new ViewBrowser(this);
		diagram = new ViewDiagram(this);
		objects = new ViewObjects(this);
		
		JSplitPane split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split2.setOneTouchExpandable(true);
		split2.setDividerLocation(250);
		split2.setLeftComponent(browser);
		split2.setRightComponent(objects);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setTopComponent(split2);
		split.setBottomComponent(diagram);
		split.setOneTouchExpandable(true);
		
		BorderLayoutPanel designer = new BorderLayoutPanel(2,2);
		designer.setComponentCenter(split);
		
		add(I18n.getString("querybuilder.designer","designer"),designer);
		add(I18n.getString("querybuilder.syntax","syntax"),syntax = new TextView(new QueryStyledDocument()));
		addChangeListener(this);		
	}
	
	public boolean isDragAndDropEnabled()
	{
		return diagram.isDragAndDropEnabled();
	}
	
	public void setDragAndDropEnabled(boolean b)
	{
		diagram.setDragAndDropEnabled(b);
	}

	public final DiagramLayout getDiagramLayout()
	{
		layout.freeze();
		return layout;
	}
	
	public final void setDiagramLayout(DiagramLayout layout)
	{
		this.layout = layout;
		layout.setQueryBuilder(this);
		
		onModelChanged();
	}
	
	public final QueryModel getQueryModel()
	{
		if(layout == null)
		{
			layout = new DiagramLayout();
			layout.setQueryBuilder(this);
		}
		return layout.getQueryModel();
	}
	
	public final void setQueryModel(QueryModel qm)
	{
		layout = new DiagramLayout();
		layout.setQueryBuilder(this);
		layout.setQueryModel(qm);

		onModelChanged();
	}
	
	private void onModelChanged()
	{
		loading = true;
		
		diagram.onModelChanged();
		browser.onModelChanged();
		objects.onModelChanged();
		
		loading = false;		
	}

	boolean isLoading()
	{
		return loading;
	}
	
	public String getConnectionHandlerKey(){
		return keycah;
	}
	public void setConnectionHandlerKey(String keycah){
		this.keycah = keycah;
	}
	
	public Connection getConnection()
	{
        // ticket #84 check if Connection.isClosed() before getMetaData
		try {
			if(connection!=null && connection.isClosed()){
				ConnectionHandler ch = ConnectionAssistant.getHandler(keycah);
				if(ch!=null){
					setConnection(ch.get());
				}else{
					Application.alert("No connection exists!");
				}
			}
		} catch (SQLException sqle) {
			System.out.println("[ QueryBuilder::getConnection ]\n" + sqle);
		}
		return connection;
	}
	
	public void setConnection(Connection connection)
	{
		try
		{
			this.connection = connection;
			
			if(connection!=null)
			{
				QueryBuilder.identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
				QueryBuilder.maxColumnNameLength = connection.getMetaData().getMaxColumnNameLength();
			}
			
			objects.onConnectionChanged();
		}
		catch(SQLException sqle)
		{
			System.out.println("[ QueryBuilder::setConnection ]\n" + sqle);
		}
	}
	
	void onLoad()
	{
		loading = true;
		
		load(browser.getQuerySpecification().getFromClause());
		load(browser.getQuerySpecification().getSelectList());
		load(browser.getQuerySpecification().getWhereClause());
		
		loading = false;
		
		convertJoins(browser.getQuerySpecification().getWhereClause());
	}
	
	
	private void load(Condition[] whereClauses) {
		for(Condition whereClause : whereClauses){
			for(DiagramField field : getDiagramFieldsFromWhereClause(whereClause)){
				if(field!=null) field.setWhereIcon();
			}
		}
	}
	public List<DiagramField> getDiagramFieldsFromWhereClause(Condition whereClause){
		List<DiagramField> fields = new ArrayList<DiagramField>(2);
		DiagramField left = getDiagramFieldFromWhereToken(whereClause.getLeft());
		DiagramField right = getDiagramFieldFromWhereToken(whereClause.getRight());
		if(left!=null && right!=null){
			//don't set icons if both left and right clause belongs to diagram field
			return fields;
		}
		fields.add(left);
		fields.add(right);
		return fields;
	}
	private DiagramField getDiagramFieldFromWhereToken(QueryTokens._Expression whereToken){
		if(whereToken!=null){
			if(whereToken instanceof Column){
				QueryTokens.Column token = (QueryTokens.Column)whereToken;
				DiagramEntity entity = diagram.getEntity(token.getTable());
				if(entity!=null){
					return entity.getField(token.getName());
				}
			}else if (whereToken instanceof DefaultExpression){
				DiagramAbstractEntity[] entities = diagram.getEntities();
				for(int j=0; j<entities.length; j++){
					if(!(entities[j] instanceof DiagramEntity)) continue;
					DiagramEntity entity = (DiagramEntity)entities[j];
					DiagramField field =  entity.getField(whereToken.toString());
					if(field!=null){
						return field;
					}
				}
			}
		}
		return null;
	}

	private void load(QueryTokens._TableReference[] tokens)
	{
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i] instanceof QueryTokens.Table)
			{
				DiagramLoader.run(DiagramLoader.DEFAULT,this,(QueryTokens.Table)tokens[i],false);
			}
			else if(tokens[i] instanceof DerivedTable) // added for ticket #80
			{
				DiagramQuery entity = new DiagramQuery(this,(DerivedTable)tokens[i]);
				this.diagram.addEntity(entity);
				// to do display derived table fields
				entity.setColumnSelections(true);
				Application.alert("!!! Displaying reversed SQL for Derived table is not finished yet !!!");
			}
			else
			{
				doJoin((QueryTokens.Join)tokens[i]);
			}
		}
		layout.resume();
	}
	
	private void load(QueryTokens._Expression[] tokens)
	{
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i] instanceof QueryTokens.Column)
			{
				QueryTokens.Column token = (QueryTokens.Column)tokens[i];
				
				DiagramEntity entity = diagram.getEntity(token.getTable());
				if(entity!=null)
				{
					DiagramField field = entity.getField(token.getName());
					if(field!=null)
					{
						field.setQueryToken(token);
						field.setSelected(true);
					}
				}				
			}
			else if(tokens[i] instanceof QueryTokens.DefaultExpression)
			{
				//F-1730329 : verifica se è una colonna senza nome tabella specificato
				DiagramAbstractEntity[] entities = diagram.getEntities();
				for(int j=0; j<entities.length; j++)
				{
					if(!(entities[j] instanceof DiagramEntity)) continue;
					DiagramEntity entity = (DiagramEntity)entities[j];
					
					DiagramField field = entity.getField(tokens[i].toString());
					if(field!=null)
					{
						QueryTokens.Column token = new QueryTokens.Column(entity.getQueryToken(),tokens[i].toString());
						field.setQueryToken(token);

						BrowserItems.DefaultTreeItem item = (BrowserItems.DefaultTreeItem)browser.getQueryItem().getChildAt(0);
						for(int k=0; k<item.getChildCount(); k++)
						{
							DefaultTreeItem child = (DefaultTreeItem)item.getChildAt(k);
							if(child.getUserObject().toString().equalsIgnoreCase(tokens[i].toString()))
							{
								browser.getQuerySpecification().setSelectList(k,token);
								child.setUserObject(token);
								browser.reload(child);
								field.setSelected(true);
							}
						}
					}
				}
			}
		}
	}
	
	private void convertJoins(QueryTokens.Condition[] tokens)
	{
		DiagramAbstractEntity[] entities = diagram.getEntities();
		if(entities.length < 2) return;

		Hashtable tables = new Hashtable();
		for(int i=0; i<entities.length; i++)
		{
			if(!(entities[i] instanceof DiagramEntity)) continue;
			DiagramEntity entity = (DiagramEntity)entities[i];
			
			QueryTokens._TableReference token = entity.getQueryToken();
			tables.put(SQLFormatter.stripQuote(((QueryTokens.Table)token).getReference()),token);
		}
		
		for(int i=0; i<tokens.length; i++)
		{
			if(tokens[i].getLeft() instanceof SubQuery || tokens[i].getRight() instanceof SubQuery) continue;

			try
			{
				QueryTokens.Column left = SQLParser.doConvertColumn(tables,tokens[i].getLeft());
				QueryTokens.Column right = SQLParser.doConvertColumn(tables,tokens[i].getRight());
				
				if(left == null || right == null) continue;
				
				//System.out.println("**** dovrebbe essere una join ****");
				QueryTokens.Join token = new QueryTokens.Join(left,tokens[i].getOperator(),right);
				DiagramRelation relation = doJoin(token);
				if(relation!=null) browser.removeWhereClause(tokens[i]);
			}
			catch(IOException e)
			{
			}
		}
	}
	
	private DiagramRelation doJoin(QueryTokens.Join token)
	{
		DiagramRelation relation = null;
		
		DiagramEntity entityP = diagram.getEntity(token.getPrimary().getTable());
		if(entityP == null)
		{
			DiagramLoader.run(DiagramLoader.DEFAULT,this,token.getPrimary().getTable(),false);
			entityP = diagram.getEntity(token.getPrimary().getTable());
		}
		
		DiagramEntity entityF = diagram.getEntity(token.getForeign().getTable());
		if(entityF == null)
		{
			DiagramLoader.run(DiagramLoader.DEFAULT,this,token.getForeign().getTable(),false);
			entityF = diagram.getEntity(token.getForeign().getTable());
		}
		
		DiagramField fieldP = entityP.getField(token.getPrimary().getName());
		DiagramField fieldF = entityF.getField(token.getForeign().getName());
		
		if(fieldP!=null && fieldF!=null)
		{
			diagram.join(entityP,fieldP);
			diagram.join(entityF,fieldF);
			
			relation = diagram.getRelation(token);
			if(relation!=null) relation.setQueryToken(token);
		}
		
		return relation;
	}
	
	public void stateChanged(ChangeEvent ce)
	{
		this.getActionMap().get(QueryActions.DIAGRAM_SAVE_AS_IMAGE).setEnabled(this.getSelectedIndex() == 0);
		this.getActionMap().get(QueryActions.ENTITIES_ARRANGE).setEnabled(this.getSelectedIndex() == 0);
		this.getActionMap().get(QueryActions.ENTITIES_REMOVE).setEnabled(this.getSelectedIndex() == 0);
		this.getActionMap().get(QueryActions.ENTITIES_PACK).setEnabled(this.getSelectedIndex() == 0);

		if(this.getSelectedIndex() == 0)
		{
			final ClientQueryBuilder cqb = getClientQueryBuilder();
			final boolean isSQLFile = cqb.isSQLFile() && "SELECT".equals(layout.getQueryModel().toString(true));
			if(isSQLFile ){
				try {
					QueryModel model = SQLParser.toQueryModel(syntax.getText());
					model.setSchema(cqb.getSchema());
					layout.setQueryModel(model);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			String msql = layout.getQueryModel().toString(true);
			String tsql = syntax.getText();
			
			if(!tsql.equals(msql))
			{
				int choice = JOptionPane.showConfirmDialog(this,I18n.getString("querybuilder.syntaxChanged","syntax changed!\ndo you want to apply changes (designer need to reload)?"));
				if(choice == JOptionPane.YES_OPTION)
				{
					// this thread resolve: IllegalComponentStateException
					new Thread(new Runnable()
					{
						public void run()
						{
							while(!QueryBuilder.this.getComponentAt(0).isVisible());
							try
							{
								if(isSQLFile){
									cqb.setDiagramLayout(layout);
								}
								QueryModel qm = SQLParser.toQueryModel(syntax.getText());
								qm.setSchema(QueryBuilder.this.layout.getQueryModel().getSchema());
								QueryBuilder.this.setQueryModel(qm);
							}
							catch(IOException e)
							{
							}
						}
					}).start();
				}
			}else if(isSQLFile){
				cqb.setDiagramLayout(layout);
			}
		}
		else
			syntax.setText(this.getQueryModel().toString(true));
	}

	public TextView getSyntax() {
		return syntax;
	}

	public ClientQueryBuilder getClientQueryBuilder() {
		return cqb;
	}

	public void setClientQueryBuilder(ClientQueryBuilder cqb) {
		this.cqb = cqb;
	}
}