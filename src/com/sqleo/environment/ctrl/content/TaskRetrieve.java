/*
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

package com.sqleo.environment.ctrl.content;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.JdbcUtils;
import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;


public class TaskRetrieve implements Runnable
{
	private int limit;
	private ContentPane target = null;
	
	private Statement stmt = null;
	private ResultSet rs = null;
	private int currentRow = 0;
	private boolean rowsToFetchExists = true;

	public TaskRetrieve(ContentPane target)
	{
		this(target,0);
	}
	
	public TaskRetrieve(ContentPane target, int limit)
	{
		this.target = target;
		this.limit = limit;
		this.currentRow = 0;
		this.rowsToFetchExists = true;
	}
	
	public boolean areAllRowsFetched(){
		return !rowsToFetchExists;
	}
	
	public void close() throws Exception
	{
		JdbcUtils.cancelAndCloseStatement(stmt);
		stmt = null;
		rs = null;
	}
	
	public void run()
	{
		try
		{
			String syntax = target.getQuery();
			if(target.getHandlerKey()!=null)
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(target.getHandlerKey());
				stmt = ch.get().createStatement();
				stmt.setMaxRows(limit);

				// reset timeout for #351 Content Window: SQLexception: Statement was canceled or the session timed out 
				stmt.setQueryTimeout(0);

				// ticket #375 prevent OutOfMemory errors with Big MySQL tables
				if( ch.getDatabaseProductName() == "MySQL")
				{
					stmt.setMaxRows(1000);
				} else {
					stmt.setFetchSize(ContentModel.MAX_BLOCK_RECORDS);
				}

				syntax = SQLHelper.getSQLeoFunctionQuery(syntax,target.getHandlerKey());
				
				// test #329 Query builder / Command editor: avoid PostgreSQL ERROR: current transaction is aborted
				rs = JdbcUtils.executeQuery(ch, syntax, stmt);
				
				for(int i=1; i<=this.getColumnCount(); i++)
				{
					String l = this.getColumnLabel(i);
					target.getView().addColumn(l,this.getColumnType(i));
				}
				target.getView().onTableChanged(false);

				for(int i=1; i<=this.getColumnCount(); i++)
				{
					String t = this.getColumnLabel(i) + " : " + this.getColumnTypeName(i) + " " + this.getColumnNullable(i);
					target.getView().setToolTipText(i-1,t);
				}

				for(currentRow=1;target.isBusy() && (rowsToFetchExists=rs.next());currentRow++)
				{
					Object[] rowdata = new Object[this.getColumnCount()];
					for(int i=1; i<=this.getColumnCount(); i++)
					{
						rowdata[i-1] =  SQLHelper.getRowValue(rs, i);
					}
					target.getView().addRow(rowdata,false);
					
					if(currentRow == ContentModel.MAX_BLOCK_RECORDS){
						break;
					}
				}
			}
		}
		catch(SQLException sqle)
		{
			Application.println(sqle,true);
		} 
		finally
		{
			target.getView().onTableChanged(true);
			if(rowsToFetchExists){
				target.doSuspend();
			}else{
				target.doStop();
			}
			target.doRefreshStatus(!rowsToFetchExists);
		}
	}
	
	public void setNextResultSet(){
		try
		{
			if(null == rs){
				return;
			}
			int lastRow = (currentRow-1) + ContentModel.MAX_BLOCK_RECORDS;
			while(target.isBusy() && (rowsToFetchExists=rs.next()))
			{
				Object[] rowdata = new Object[this.getColumnCount()];
				for(int i=1; i<=this.getColumnCount(); i++)
				{
					rowdata[i-1] =  SQLHelper.getRowValue(rs, i);
				}
				target.getView().addRow(rowdata,false);
				
				if(currentRow == lastRow){
					break;
				}
				currentRow++;
			}
		}
		catch (SQLException sqle){
			Application.println(sqle,true);
		}finally{
			target.getView().onTableChanged(true);
			if(rowsToFetchExists){
				target.doSuspend();
			}else{
				target.doStop();
			}
			target.doRefreshStatus(!rowsToFetchExists);
		}
	}
	
	private int getColumnCount() throws SQLException
	{
		return rs.getMetaData().getColumnCount();
	}

	private String getColumnLabel(int index) throws SQLException
	{
		return rs.getMetaData().getColumnLabel(index);
	}
/*	
	private String getColumnSize(int index) throws SQLException
	{
		String size;
		if(isNumberType(getColumnType(index)))
		{
			int p = rs.getMetaData().getPrecision(index);
			int s = rs.getMetaData().getScale(index);
			
			size = s > 0 ? p + "," + s : Integer.toString(p);
		}
		else
			size = Integer.toString(rs.getMetaData().getColumnDisplaySize(index));
		
		return "(" + size + ")";
	}
*/	
	private String getColumnNullable(int index) throws SQLException
	{
		return rs.getMetaData().isNullable(index) == ResultSetMetaData.columnNullable ? "(null)" : "(not null)";
	}
	
	private int getColumnType(int index) throws SQLException
	{
		return rs.getMetaData().getColumnType(index);
	}

	private String getColumnTypeName(int index) throws SQLException
	{
		return rs.getMetaData().getColumnTypeName(index).toLowerCase();
	}

	public static boolean isNumberType(int type)
	{
		return type == Types.BIGINT || type == Types.BIT || type == Types.DECIMAL
			|| type == Types.DOUBLE || type == Types.FLOAT || type == Types.INTEGER
			|| type == Types.NUMERIC || type == Types.REAL || type == Types.SMALLINT;
	}	
}