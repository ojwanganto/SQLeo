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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.I18n;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.io.ManualDBMetaData;
import com.sqleo.environment.io.ManualTableMetaData;
import com.sqleo.querybuilder.syntax.QueryTokens;



public class MaskReferences extends BaseMask
{
	private DiagramEntity item;
	
	private JList foreignTables;
	private JList primaryTables;
	
	public MaskReferences(DiagramEntity item,QueryBuilder builder)
	{
		super(I18n.getFormattedString("querybuilder.message.references","{0} references", new Object[]{ "" +item.getQueryToken().getReference() }),builder);
        this.item = item;

		foreignTables = new JList();
		primaryTables = new JList();
		
		BorderLayoutPanel contentL = new BorderLayoutPanel();
		contentL.setComponentNorth(new JLabel(I18n.getString("querybuilder.menu.foreignTables","foreign tables")));
		contentL.setComponentCenter(new JScrollPane(foreignTables));
		
		BorderLayoutPanel contentR = new BorderLayoutPanel();
		contentR.setComponentNorth(new JLabel(I18n.getString("querybuilder.menu.primaryTables","primary tables")));
		contentR.setComponentCenter(new JScrollPane(primaryTables));
		
		JPanel pnlCenter = new JPanel(new GridLayout(1,2,2,2));
		pnlCenter.add(contentL);
		pnlCenter.add(contentR);		
		setComponentCenter(pnlCenter);
	}
	
	public Dimension getPreferredSize()
	{
		return Preferences.getScaledDimension(400,220);		
	}

	private void addToSource(Object[] tables)
	{
		for(int i=0; i<tables.length; i++)
		{
			String schema = null;
			String table = tables[i].toString();
			
			if(table.indexOf('.')!=-1)
			{
				schema = table.substring(0,table.indexOf('.'));
				table = table.substring(table.indexOf('.')+1);
			}
			
			QueryTokens.Table token = new QueryTokens.Table(schema,table);
			DiagramLoader.run(DiagramLoader.DEFAULT,item.builder,token,true);
		}
	}

	protected boolean onConfirm()
	{
		addToSource(foreignTables.getSelectedValues());
		addToSource(primaryTables.getSelectedValues());
		
		return true;
	}
	
	protected void onShow()
	{
		try
		{
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			loadExportedKeys();
			loadImportedKeys();
		}
		catch(SQLException sqle)
		{
			System.out.println("[ MaskReferences::onShowing ]\n" + sqle);
		}
		finally
		{
			this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));			
		}
	}
	
	private void loadImportedKeysInternal(String pkschema,String pktable,DefaultListModel model){
		if(item.builder.getQueryModel().getSchema()!=null) pkschema = null;
		if(pkschema!=null) pkschema = pkschema.trim();
		
		if(!pktable.equals(item.getQueryToken().getName()))
		{
			String pkElement = pkschema == null ? pktable : pkschema + "." + pktable;
			if(!model.contains(pkElement)) model.addElement(pkElement);
		}
	}

	private void loadImportedKeysManual(ArrayList impKeys,DefaultListModel model,String catalog,String schema){
		for(Object impKey : impKeys){
			String[] row = (String[])impKey;
			String pkschema = row[1];
			String pktable	= row[2].trim();
			loadImportedKeysInternal(pkschema,pktable,model);
		}
	}

	private void loadImportedKeysAuto(DefaultListModel model,DatabaseMetaData dbmd,String catalog,String schema)
	throws SQLException{
		ResultSet rs = dbmd.getImportedKeys(catalog, schema, item.getQueryToken().getName());		
		while(rs.next())
		{
			String pkschema = rs.getString(2);
			String pktable	= rs.getString(3).trim();
			loadImportedKeysInternal(pkschema,pktable,model);
		}
		rs.close();
	}
	
	private void loadImportedKeys()
		throws SQLException
	{
		DefaultListModel model = new DefaultListModel();
		DatabaseMetaData dbmd = item.builder.getConnection().getMetaData();
		
		String schema = item.builder.getQueryModel().getSchema() == null ? item.getQueryToken().getSchema() : item.builder.getQueryModel().getSchema();
		String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
		ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
		if(md!=null){
			ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema,item.getQueryToken().getName());
			if(mtd!=null){
				loadImportedKeysManual(mtd.getImportedKeys(),model,catalog,schema);
			}else{
				loadImportedKeysAuto(model,dbmd,catalog,schema);
			}
		}else{
			loadImportedKeysAuto(model,dbmd,catalog,schema);
		}
		primaryTables.setModel(model);
	}
	
	
	private void loadExportedKeysInternal(String fkschema,String fktable,DefaultListModel model){
		if(item.builder.getQueryModel().getSchema()!=null) fkschema = null;
		if(fkschema!=null) fkschema = fkschema.trim();

		if(!fktable.equals(item.getQueryToken().getName()))
		{
			String fkElement = fkschema == null ? fktable : fkschema + "." + fktable;
			if(!model.contains(fkElement)) model.addElement(fkElement);
		}
	}
	private void loadExportedKeysManual(ArrayList expKeys,DefaultListModel model,String catalog,String schema){
		for(Object expKey : expKeys){
			String[] row = (String[])expKey;
			String fkschema = row[5];
			String fktable = row[6].trim();
			loadExportedKeysInternal(fkschema,fktable,model);
		}
		
	}
	private void loadExportedKeysAuto(DefaultListModel model,DatabaseMetaData dbmd,String catalog,String schema)
	throws SQLException{
		ResultSet rs = dbmd.getExportedKeys(catalog, schema, item.getQueryToken().getName());		
		while(rs.next())
		{
			String fkschema = rs.getString(6);
			String fktable	= rs.getString(7).trim();
			loadExportedKeysInternal(fkschema,fktable,model);
		}
		rs.close();
	}
	private void loadExportedKeys()
		throws SQLException
	{
		DefaultListModel model = new DefaultListModel();
		DatabaseMetaData dbmd = item.builder.getConnection().getMetaData();
		
		String schema = item.builder.getQueryModel().getSchema() == null ? item.getQueryToken().getSchema() : item.builder.getQueryModel().getSchema();
		String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
		
		ManualDBMetaData md = ConnectionAssistant.getManualDBMetaData(builder.getConnectionHandlerKey());
		if(md!=null){
			ManualTableMetaData mtd = md.getManualTableMetaData(catalog, schema,item.getQueryToken().getName());
			if(mtd!=null){
				loadExportedKeysManual(mtd.getExportedKeys(),model,catalog,schema);
			}else{
				loadExportedKeysAuto(model,dbmd,catalog,schema);
			}
		}else{
			loadExportedKeysAuto(model,dbmd,catalog,schema);
		}
		foreignTables.setModel(model);
	}
}