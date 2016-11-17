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

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.io.ManualDBMetaData;
import com.sqleo.environment.io.ManualTableMetaData;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class DiagramLoader extends JDialog implements Runnable
{
	static public final int DEFAULT = 0;
	static public final int ALL_FOREIGN_TABLES = 1;
	static public final int ALL_PRIMARY_TABLES = 2;
	
	private JLabel message;
	
	private int mode;
	private boolean autoJoinRequested;
	
	private QueryBuilder builder;
	private QueryTokens.Table table;
	
	private DiagramLoader(Frame owner)
	{
		super(owner);
	}
	
	private DiagramLoader(Dialog owner)
	{
		super(owner);
	}

	public static void run(int mode, QueryBuilder builder, QueryTokens.Table table, boolean autojoin)
	{
		DiagramLoader loader = null;
		
		if(SwingUtilities.getWindowAncestor(builder) instanceof Frame)
			loader = new DiagramLoader((Frame)SwingUtilities.getWindowAncestor(builder));
		else if(SwingUtilities.getWindowAncestor(builder) instanceof Dialog)
			loader = new DiagramLoader((Dialog)SwingUtilities.getWindowAncestor(builder));
		
		if(loader!=null)
		{
			loader.setModal(true);
			loader.setSize(275,55);
			loader.setTitle(I18n.getString("querybuilder.message.wait","wait..."));
			loader.setResizable(false);
			loader.setLocationRelativeTo(builder);
			loader.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
					
			loader.getContentPane().add(loader.message = new JLabel("",JLabel.CENTER));
			
			loader.autoJoinRequested = autojoin;
			loader.builder = builder;
			loader.table = table;
			loader.mode = mode;
			
			loader.show();
		}
	}
	
	public void show()
	{
		new Thread(this).start();
		super.show();
	}
	
	public void run()
	{
		try
		{
			switch(mode)
			{
				case ALL_FOREIGN_TABLES: addAllForeignTables();break;
				case ALL_PRIMARY_TABLES: addAllPrimaryTables();break;
				default: addTable(table);
			}
		}
		catch(SQLException sqle)
		{
			// #394 Designer: reversing query doesn't warn on closed connection 
			// System.out.println("[ DiagramLoader::run ]\n" + sqle);
			Application.alert("[ DiagramLoader::run ]\n" + sqle);
		}
		finally
		{
			this.dispose();
		}
	}

	private void addTable(QueryTokens.Table table)
		throws SQLException
	{
		message.setText( I18n.getFormattedString("querybuilder.message.loading","Loading: {0}", new Object[]{"" + table.getIdentifier()}));
		boolean tableExists = checkTable(table);

		// fix #78 do not autoalias fields in subqueries
		// if(( QueryBuilder.autoAlias || (builder.browser.getQueryItem() instanceof BrowserItems.DiagramQueryTreeItem)) && table.getAlias()==null)
		if( QueryBuilder.autoAlias && table.getAlias()==null)
		{
			table.setAlias(table.getName());
		
			for(int i=0; builder.diagram.getEntity(table)!=null; i++)
			{
				if(mode==DEFAULT)
					table.setAlias(table.getName() + "_" + (char)(65+i));
				else
					return;
			}
		}
		else if(builder.diagram.getEntity(table)!=null)
		{
			if(mode==DEFAULT)
			{
				this.setVisible(false);
				JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.tableLoadedAliasDisabled","Table already loaded and aliasing disabled!"), table.getIdentifier(), JOptionPane.WARNING_MESSAGE);
			}
			return;		    		
		}
		boolean added=false;
		if(!QueryBuilder.autoAlias){
			DatabaseMetaData dbmd = builder.getConnection().getMetaData();
			String schema = builder.getQueryModel().getSchema() == null ? table.getSchema() : builder.getQueryModel().getSchema();		
			String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
			ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
			if(md!=null){
				ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema, table.getName());
				if(mtd!=null){
					boolean noSchema = !Preferences.getBoolean("querybuilder.use-schema");
					Set<String> expAliases = new HashSet<String>();
					for(Object rows : mtd.getExportedKeys())
					{
						String[] row = (String[])rows;
						DiagramEntity t =  builder.diagram.getEntity(noSchema?null:row[5], row[6]);
						if(t!=null){
							
							DiagramEntity t1 =  builder.diagram.getEntity(noSchema?null:row[1], row[2]);
							String alias = t1!=null?t1.getQueryToken().getAlias():null;
							System.out.println(alias);
							if(alias==null || alias.length() ==0 ){
								if(row[15].length()>=1){
								expAliases.add(row[15]);
								System.out.println("ADDING ALIAS TABLESs:"+row[15]);}
							}else if (alias.equals(row[15])){
								added = true;
								break;
							}else{
								if(row[15].length()>=1){
									expAliases.add(row[15]);
									System.out.println("ADDING ALIAS TABLESs:"+row[15]);}
							}
						}
					}
					if(!added){
						
						boolean woAliasExists = false;
						Set<String> impAliases = new HashSet<String>();
						Map<String,DiagramEntity> impADE = new HashMap<String,DiagramEntity>();
						for(Object rows : mtd.getImportedKeys()){
							String[] row = (String[])rows;
							DiagramEntity t =  builder.diagram.getEntity(noSchema?null:row[1], row[2]);
							if(t!=null ){
								String alias = t.getQueryToken().getAlias();
								if(alias==null){
									woAliasExists = true;
									alias="";
								}
								if(row[15].length()>=1 && !row[15].equals(alias)){
									impAliases.add(row[15]);
									impADE.put(row[15], t);
								}
							}
						}
						if(impAliases.size()>=1){
							createAndJoin(table, true);
							added = true;
						}
					
						for(String alias: impAliases){
							QueryTokens.Table qt = impADE.get(alias).getQueryToken();
							QueryTokens.Table tablex = new QueryTokens.Table(noSchema?null:qt.getSchema(), qt.getName()); 
							tablex.setAlias(alias);
							createAndJoin(tablex, true);
							added=true;
						}
						
						for(String alias : expAliases){
							QueryTokens.Table tablex = new QueryTokens.Table(noSchema?null:table.getSchema(), table.getName()); 
							tablex.setAlias(alias);
							createAndJoin(tablex, true);
							added = true;
						}
						if(added && woAliasExists){
							JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.tableAliasedCorrectJoin","Same table added with an alias,please choose the expected one and corret joins accordingly!"), table.getIdentifier(), JOptionPane.WARNING_MESSAGE);
							return;
						}
					}else{
					JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.tableLoadedAliasDisabled","Table already loaded and aliasing disabled!"), table.getIdentifier(), JOptionPane.WARNING_MESSAGE);
					return;
					}
				}
			}
		}
		if(!added){
			createAndJoin(table, tableExists);
		}
		
	}
	
	private void createAndJoin(QueryTokens.Table table,boolean tableExists) throws SQLException{
		DiagramEntity item = creatEntity(table,tableExists);
		// #157
		if(Application.isFullVersion() || builder.diagram.getEntities().length < 3){
			builder.diagram.addEntity(item);
		}
		else{
			JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.VersionWith3tablesMax","Version with 3 tables max per graph, Please Donate for more"), table.getIdentifier() + " " + table.getAlias(), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		
		/* lo deve fare se provengo da: click su browser, da reference o da open all
		non lo deve fare se sto facendo setQueryModel! */
		if(!builder.isLoading() && QueryBuilder.selectAllColumns)
			item.setColumnSelections(true);

		if(autoJoinRequested && QueryBuilder.autoJoin)
			doAutoJoin(item);
	}

	
	
	private void addTables(ArrayList mtdKeys,int rsSchemaIndex,int rsTableIndex) throws SQLException{
		
		ArrayList list = new ArrayList();
		
		for(Object rows : mtdKeys)
		{
			String[] row = (String[])rows; 
			String schemaName = row[rsSchemaIndex-1];
			String tableName = row[rsTableIndex-1].trim();
			
			if(builder.getQueryModel().getSchema()!=null) schemaName = null;
			if(schemaName!=null) schemaName = schemaName.trim();
			QueryTokens.Table qt =new QueryTokens.Table(schemaName,tableName);
//			if(row[15].length()>=1){
//				qt.setAlias(row[15]);
//			}
			list.add(qt);
		}
					
		for(ListIterator iter = list.listIterator(); iter.hasNext();)
		{
			addTable((QueryTokens.Table)iter.next());
		}
		
	}
	
	private void addTables(ResultSet rs, int rsSchemaIndex, int rsTableIndex)
		throws SQLException
	{
		ArrayList list = new ArrayList();
		
		while(rs.next())
		{
			String schemaName = rs.getString(rsSchemaIndex);
			String tableName = rs.getString(rsTableIndex).trim();
			
			if(builder.getQueryModel().getSchema()!=null) schemaName = null;
			if(schemaName!=null) schemaName = schemaName.trim();
			
			list.add(new QueryTokens.Table(schemaName,tableName));
		}
		rs.close();
					
		for(ListIterator iter = list.listIterator(); iter.hasNext();)
		{
			addTable((QueryTokens.Table)iter.next());
		}
	}
	
	private void addAllForeignTables()
		throws SQLException
	{
		DatabaseMetaData dbmd = builder.getConnection().getMetaData();
		message.setText(I18n.getString("querybuilder.message.reading","reading...") );
		
		String schema = builder.getQueryModel().getSchema() == null ? table.getSchema() : builder.getQueryModel().getSchema();		
		String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
		ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
		if(md!=null){
			ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema, table.getName());
			if(mtd!=null){
				addTables(mtd.getExportedKeys(),6,7);
			}else{
				addTables(dbmd.getExportedKeys(catalog, schema, table.getName()) ,6,7);
			}
		}else{
			addTables(dbmd.getExportedKeys(catalog, schema, table.getName()) ,6,7);
		}

	}
	
	private void addAllPrimaryTables()
		throws SQLException
	{
		DatabaseMetaData dbmd = builder.getConnection().getMetaData();
		message.setText(I18n.getString("querybuilder.message.reading","reading..."));
		
		String schema = builder.getQueryModel().getSchema() == null ? table.getSchema() : builder.getQueryModel().getSchema();
		String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
		
		ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
		if(md!=null){
			ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema, table.getName());
			if(mtd!=null){
				addTables(mtd.getImportedKeys() ,2,3);
			}else{
				addTables(dbmd.getImportedKeys(catalog, schema, table.getName()) ,2,3);
			}
		}else{
			addTables(dbmd.getImportedKeys(catalog, schema, table.getName()) ,2,3);
		}
	}
	
	private boolean checkTable(QueryTokens.Table table)
		throws SQLException
	{
		DatabaseMetaData dbmd = builder.getConnection().getMetaData();
		
		String name = table.getName();
		String schema = builder.getQueryModel().getSchema() == null ? table.getSchema() : builder.getQueryModel().getSchema();
		String catalog = schema == null ? null : dbmd.getConnection().getCatalog();

		ResultSet rs = dbmd.getTables(catalog,schema,name,null);
		boolean exists = rs.next();
		rs.close();
		
		if(!exists)
		{
			if(dbmd.storesLowerCaseIdentifiers())
			{
				name = name!=null ? name.toLowerCase() : null;
				schema = schema!=null ? schema.toLowerCase() : null;
				catalog = catalog!=null ? catalog.toLowerCase() : null;
			}
			else if(dbmd.storesUpperCaseIdentifiers())
			{
				name = name!=null ? name.toUpperCase() : null;
				schema = schema!=null ? schema.toUpperCase() : null;
				catalog = catalog!=null ? catalog.toUpperCase() : null;
			}
			
			rs = dbmd.getTables(catalog,schema,name,null);
			if(exists = rs.next())
			{
				table.setName(name);
				if(builder.getQueryModel().getSchema() == null)
					table.setSchema(schema);
			}
			rs.close();
		}
		
//		if(!exists) // fix ticket #119
//		{
//			Application.alert(Application.PROGRAM,"Object " + table.getIdentifier() + " doesn't exists!");
//		}
		return exists;
	}
	
	private DiagramEntity creatEntity(QueryTokens.Table table, boolean tableExists)
		throws SQLException
	{
		DiagramEntity item = new DiagramEntity(builder,table);
		if(!tableExists){
			item.setFontColorAndToolTip(Color.red, table.getName()  + " : !!! missing !!! ");
		}
		item.setEnabled(builder.getConnection()!=null);
		
		if(builder.getConnection()!=null)
		{
			DatabaseMetaData dbmetadata = builder.getConnection().getMetaData();
			Hashtable primary = this.getPrimaryKeys(dbmetadata,item);
			
			String name = item.getQueryToken().getName();
			String schema = builder.getQueryModel().getSchema() == null ? item.getQueryToken().getSchema() : builder.getQueryModel().getSchema();
			String catalog = schema == null ? null : dbmetadata.getConnection().getCatalog();

			ResultSet rsColumns = dbmetadata.getColumns(catalog, schema, name, "%");
			while(rsColumns.next())
			{
				String columnName	= rsColumns.getString(4).trim();
				String typeName		= rsColumns.getString(6);
				int size	= rsColumns.getInt(7);
				int pos		= rsColumns.getInt(17);
				
				DiagramField field = item.addField(pos,columnName,primary.get(columnName));
				field.setToolTipText(columnName + " : " + typeName + "(" + size + ")");
			}
			rsColumns.close();
		}
		item.pack();
		
		return item;
	}
	
	private Hashtable getPrimaryKeys(DatabaseMetaData dbmetadata, DiagramEntity item)
	{
		Hashtable primary = new Hashtable();
		
		try
		{
			String name = item.getQueryToken().getName();
			String schema = builder.getQueryModel().getSchema() == null ? item.getQueryToken().getSchema() : builder.getQueryModel().getSchema();
			String catalog = schema == null ? null : dbmetadata.getConnection().getCatalog();

			ResultSet rsPK = dbmetadata.getPrimaryKeys(catalog, schema, name);
			while(rsPK.next())
				// catch null for SQLite
				primary.put(rsPK.getString(4).trim(), rsPK.getString(6)== null ? "PRIMARY" : rsPK.getString(6));
			rsPK.close();
		}
		catch (SQLException sqle)
		{
			System.out.println("[ DiagramLoader::getPrimaryKeys ]\n" + sqle);
		}
		
		return primary;
	}
	
	private void doAutoJoin(DiagramEntity source)
		throws SQLException
	{
		if(builder.diagram.getEntities().length > 1)
		{
			DatabaseMetaData dbmetadata = builder.getConnection().getMetaData();
			
			String name = source.getQueryToken().getName();
			String schema = builder.getQueryModel().getSchema() == null ? source.getQueryToken().getSchema() : builder.getQueryModel().getSchema();
			String catalog = schema == null ? null : dbmetadata.getConnection().getCatalog();
			
			message.setText( I18n.getFormattedString("querybuilder.message.loading.relations","check {0}'s relations ", new Object[]{"" + table.getIdentifier()}));
			
			ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
			if(md!=null && !QueryBuilder.autoAlias){
				ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema, name);
				if(mtd!=null){
					join(mtd.getImportedKeys(),source,false);
					join(mtd.getExportedKeys(),source,true);
				}else{
					join(dbmetadata.getImportedKeys(catalog, schema, name) , source, false);
					join(dbmetadata.getExportedKeys(catalog, schema, name) , source, true);
				}
			}else{
				join(dbmetadata.getImportedKeys(catalog, schema, name) , source, false);
				join(dbmetadata.getExportedKeys(catalog, schema, name) , source, true);
			}
			
		}
	}
	
	private void join(ArrayList mtdKeys, DiagramEntity source,boolean ispk){
		for(Object mtdKey : mtdKeys){
			String[] row = (String[])mtdKey;
			joinInternal(row[1],row[2],row[3],row[5],row[6],row[7],row[11],row[15],source,ispk,row[14]);
		}
		
	}
	private void joinInternal(String pkschema,String pktable,String pkcolumn,
			String fkschema,String fktable,String fkcolumn,String fkname,
			String pktAlias,DiagramEntity source,boolean ispk,String joinType){
		if(builder.getQueryModel().getSchema()!=null)
			pkschema = fkschema = null;
		
		if(pkschema!=null) pkschema = pkschema.trim();
		if(fkschema!=null) fkschema = fkschema.trim();
		
		DiagramEntity itemP = ispk ? source : builder.diagram.getEntity(pkschema, pktable);
		DiagramEntity itemF = ispk ? builder.diagram.getEntity(fkschema, fktable) : source;
		
		if(itemP!=null && itemF!=null && !itemP.getQueryToken().toString().equalsIgnoreCase(itemF.getQueryToken().toString()))
		{
			pktAlias=pktAlias.length()==0?null:pktAlias;
			String tAlias = itemP.getQueryToken().getAlias();
			tAlias = tAlias!=null?tAlias.length()==0?null:tAlias:tAlias;
			boolean go = false;
			if(pktAlias == null && pktAlias == tAlias) { go = true;}
			if( pktAlias!=null && pktAlias.equals(tAlias)){ go  = true; }
			
			if( go ){
				DiagramField fP = itemP.getField(pkcolumn,true);
				DiagramField fF = itemF.getField(fkcolumn,true);
				if(null==fP){
					JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.noColumnExistsInTable","No column: "+pkcolumn+" exists in table: "+pktable+"!"), table.getIdentifier(), JOptionPane.WARNING_MESSAGE);
					return;
				}
				if(null ==fF){
					JOptionPane.showMessageDialog(this,I18n.getString("querybuilder.message.noColumnExistsInTable","No column: "+fkcolumn+" exists in table: "+fktable+"!"), table.getIdentifier(), JOptionPane.WARNING_MESSAGE);
					return;
				}
				builder.diagram.join(itemP,fP,itemF,fF,joinType);
				builder.diagram.getRelations()[builder.diagram.getRelationCount()-1].setName(fkname);
			}		
		}
	}
	
	private void join(ResultSet rs, DiagramEntity source, boolean ispk)
		throws SQLException
	{
		while(rs.next())
		{
			String pkschema = rs.getString(2);
			String pktable	= rs.getString(3).trim();
			String pkcolumn = rs.getString(4).trim();
			String fkschema = rs.getString(6);
			String fktable	= rs.getString(7).trim();
			String fkcolumn = rs.getString(8).trim();
			String fkname	= rs.getString(12);
			
			if(builder.getQueryModel().getSchema()!=null)
				pkschema = fkschema = null;
			
			if(pkschema!=null) pkschema = pkschema.trim();
			if(fkschema!=null) fkschema = fkschema.trim();
			
			DiagramEntity itemP = ispk ? source : builder.diagram.getEntity(pkschema, pktable);
			DiagramEntity itemF = ispk ? builder.diagram.getEntity(fkschema, fktable) : source;
			
			if(itemP!=null && itemF!=null && !itemP.getQueryToken().toString().equalsIgnoreCase(itemF.getQueryToken().toString()))
			{
				DiagramField fP = itemP.getField(pkcolumn,true);
				DiagramField fF = itemF.getField(fkcolumn,true);
					
				builder.diagram.join(itemP,fP,itemF,fF);
				builder.diagram.getRelations()[builder.diagram.getRelationCount()-1].setName(fkname);
			}
		}
		rs.close();
	}
}