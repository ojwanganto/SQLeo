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

package com.sqleo.common.jdbc;

import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

public class ConnectionHandler
{
    private Connection connection;
    private Hashtable metacache;
    private boolean uncommittedTransactionExists = false;
    
    public ConnectionHandler(Connection connection)
    {
    	try {
    		if(connection!=null){
    			connection.setAutoCommit(ConnectionAssistant.getAutoCommitPrefered());
    		}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
        this.connection = connection;
		
		metacache = new Hashtable();
		try
		{
			metacache.put("$maxColumnNameLength",new Integer(connection.getMetaData().getMaxColumnNameLength()));
			metacache.put("$identifierQuoteString",connection.getMetaData().getIdentifierQuoteString());
			
	//		String term = connection.getMetaData().getSchemaTerm();
	//		metacache.put("$supportsSchema", new Boolean(term!=null && term.length()>0) );
	
			metacache.put("$supportsSchema", new Boolean(connection.getMetaData().supportsSchemasInTableDefinitions()) );
		}
		catch(Exception e)
		{ /* do_nothing	*/ }
		
		loadSchemas();
		loadTableTypes();
		loadConnectionInfos();
    }
    
	private boolean supportsSchema()
	{
		return ((Boolean)metacache.get("$supportsSchema")).booleanValue();
	}
    
	private void loadSchemas()
	{
		ArrayList schemas = new ArrayList();
		metacache.put("$schema_names",schemas);
		
		try
		{
			if(this.supportsSchema())
			{
				ResultSet rs = connection.getMetaData().getSchemas();
				while(rs.next())
				{
					String name = rs.getString(1).trim();
					if(!schemas.contains(name)) schemas.add(name);
				}
				rs.close();
			}
		}
		catch(Exception e)
		{ /* do_nothing	*/ }
	}
    
	private void loadTableTypes()
	{
		ArrayList tableTypes = new ArrayList();
		metacache.put("$table_types",tableTypes);
		
		try
		{
			ResultSet rs = connection.getMetaData().getTableTypes();
			while(rs.next())
			{
				String type = rs.getString(1).trim();
				if(!tableTypes.contains(type)) tableTypes.add(type);
			}
			rs.close();
		}
		catch(Exception e)
		{ /* do_nothing	*/ }
	}
	
	private void loadConnectionInfos()
	{
		ArrayList infos = new ArrayList();
		metacache.put("$connection_infos",infos);
		
		try
		{
			String[] info = new String[2];
			info[0] = "database product name";
			info[1] = connection.getMetaData().getDatabaseProductName();
			infos.add(info);
			
			info = new String[2];
			info[0] = "database product version";
			info[1] = connection.getMetaData().getDatabaseProductVersion();
			infos.add(info);
			
			info = new String[2];
			info[0] = "driver name";
			info[1] = connection.getMetaData().getDriverName();
			infos.add(info);
			
			info = new String[2];
			info[0] = "driver version";
			info[1] = connection.getMetaData().getDriverVersion();
			infos.add(info);
		}
		catch(Exception e)
		{ /* do_nothing	*/ }
	}
    
    /* bad */
    public Connection get()
    {
    	return connection;
    }
    
	public void close() throws SQLException
	{
		connection.close();
	}
	
	public ArrayList getArrayList(String key)
	{
		return (ArrayList)metacache.get(key);
	}
	
	public Object getObject(String key)
	{
		return metacache.get(key);
	}

	public void setUncommittedTransactionExists(boolean uncommittedTransactionExists) {
		this.uncommittedTransactionExists = uncommittedTransactionExists;
	}

	public boolean isUncommittedTransactionExists() {
		return uncommittedTransactionExists;
	}
}