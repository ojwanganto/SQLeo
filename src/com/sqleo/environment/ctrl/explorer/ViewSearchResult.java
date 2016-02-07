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

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi.MDIClient;
import com.sqleo.querybuilder.QueryBuilder;


public class ViewSearchResult extends AbstractViewObjects
{
	private String keycah = null;
	private boolean tableSearch = true;
	
	protected String getHandlerKey()
	{
		if(ConnectionAssistant.hasHandler(keycah))
		{
			QueryBuilder.identifierQuoteString = ConnectionAssistant.getHandler(keycah).getObject("$identifierQuoteString").toString();
			QueryBuilder.maxColumnNameLength = ((Integer)ConnectionAssistant.getHandler(keycah).getObject("$maxColumnNameLength")).intValue();
		}
		
		return keycah;
	}
	
	protected String getTableType()
	{
		if(tableSearch) return super.getTableType();
		
		try
		{
			String catalog	= getConnection().getCatalog();
			String schema	= getTableSchema();
			String table	= getTableName();
			String type		= null;
			
			ResultSet rs = getConnection().getMetaData().getTables(catalog,schema,table,null);
			if(rs.next())
			{
				type = rs.getString(4);
			}
			rs.close();
			return type;
		}
		catch (SQLException e)
		{
			Application.println(e,false);
		}
				
		return null;
	}
	
    protected void list(String keycah,String schema,String table,String column,String[] tableTypes) throws SQLException
    {
		this.reset();
    	this.keycah = keycah;
    	
		if(tableSearch = (column==null || column.length()==0))
			listTables(schema,table,tableTypes);
		else
			listColumns(schema,table,column);
		
		final Color backgroundColor = MDIClient.getConnectionBackgroundColor(keycah);
		if (backgroundColor!=null) {
			setBackgroundColor(backgroundColor);
		}
    }
    
    private void list(ResultSet rs) throws SQLException
    {
		ResultSetMetaData rsmd = rs.getMetaData();
		
		for(int i=1; i<=4; i++)
		{
			addColumn(rsmd.getColumnName(i));
		}
		
		while(rs.next())
		{
			String[] rowdata = new String[4];
			for(int i=1; i<=4; i++)
			{
				rowdata[i-1] =  SQLHelper.getRowValue(rs, i);;
			}
			addRow(rowdata);
		}
		rs.close();
    }
    
    private void listTables(String schema,String table,String []tableTypes) throws SQLException
    {
    	String catalog = schema == null ? null : this.getConnection().getCatalog();
    	list(this.getConnection().getMetaData().getTables(catalog,schema,table,tableTypes));
    }
    
    private void listColumns(String schema,String table,String column) throws SQLException
    {
		String catalog = schema == null ? null : this.getConnection().getCatalog();
		list(this.getConnection().getMetaData().getColumns(catalog,schema,table,column));
    }
}