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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.common.util.Text;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class TaskUpdate implements Runnable
{
	private ContentPane target = null;
	private PreparedStatement pstmt = null;

	public TaskUpdate(ContentPane target)
	{
		this.target = target;		
	}
	
	public void run()
	{
		ConnectionHandler ch = ConnectionAssistant.getHandler(target.getHandlerKey());
		for(int exceptions = 0; target.getView().getChanges().count() > exceptions ;)
		{
			Vector columns = new Vector();
			Vector params = new Vector();
			String sql = new String();
			
			ContentChanges.Handler handler = target.getView().getChanges().getHandlerAt(exceptions);
			Object[] rowdata = target.getView().getValues(handler.rid);
			
			if(handler.type.equals(ContentChanges.INSERT))
			{
				for(int i=0; i<rowdata.length ; i++)
				{
					QueryTokens._Expression[] e = target.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
					if(target.getUpdateModel().isUpdatable(e[i]))
					{						
						Object cell = rowdata[i];
						if(rowdata[i] instanceof Object[]) cell = ((Object[])rowdata[i])[0];
						params.addElement(new Object[]{cell, new Integer(target.getView().getColumnType(i))});
						
						String cname = ((QueryTokens.Column)e[i]).getName();
						columns.addElement(SQLFormatter.ensureQuotes(cname,ch.getObject("$identifierQuoteString").toString(),true));
					}
				}
				sql = target.getUpdateModel().getInsertSyntax((String[])columns.toArray(new String[columns.size()]));
			}
			else
			{
				Boolean columnFound = true ;	
				Vector whereValues = new Vector();	
				for(int j=0; j<target.getUpdateModel().getRowIdentifierCount() ; j++)
				{
					int col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(j).getReference());
					if(col == -1) col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(j).getName());

					// Ticket #334 Content Window: java.lang.ArrayIndexOutOfBoundsException
					if(col == -1) 
					{
						columnFound = false;
						Application.alert(Application.PROGRAM,"Column: \"" + target.getUpdateModel().getRowIdentifier(j).getName() + "\" not found in Grid" );
					}
					else
					{
						Object cell = rowdata[col];
						if(cell instanceof Object[]) cell = ((Object[])cell)[1];
						if(null == cell){
							whereValues.addElement("null");
						}else{
							whereValues.addElement("?");
						}
					}

				}
				if(handler.type.equals(ContentChanges.DELETE) && columnFound)
				{
					sql = target.getUpdateModel().getDeleteSyntax(whereValues.toArray());
				}
				else if(handler.type.equals(ContentChanges.UPDATE) && columnFound)
				{
					for(int i=0; i<rowdata.length ; i++)
					{
						if(rowdata[i] instanceof Object[])
						{
							QueryTokens._Expression[] e = target.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
							if(target.getUpdateModel().isUpdatable(e[i]))
							{						
								Object cell = ((Object[])rowdata[i])[0];
								params.addElement(new Object[]{cell, new Integer(target.getView().getColumnType(i))});
							
								String cname = ((QueryTokens.Column)e[i]).getName();
								columns.addElement(SQLFormatter.ensureQuotes(cname,ch.getObject("$identifierQuoteString").toString(),true));
							}
						}
					}
					sql = target.getUpdateModel().getUpdateSyntax((String[])columns.toArray(new String[columns.size()]),whereValues.toArray());
				}
				
				for(int i=0; i<target.getUpdateModel().getRowIdentifierCount() ; i++)
				{
					int col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(i).getReference());
					if(col == -1) col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(i).getName());

					Object cell = rowdata[col];
					if(cell instanceof Object[]) cell = ((Object[])cell)[1];
					if(cell!=null){
						params.addElement(new Object[]{cell, new Integer(target.getView().getColumnType(col))});
					}
				}
			}
			
			try
			{
				this.close();
			}
			catch(Exception e)
			{
				Application.println(e,true);
			}
				
			try
			{
				execute(sql.toString(),params);
					
				for(int i=0; i<rowdata.length; i++)
				{
					if(rowdata[i] instanceof Object[])
					{
						Object cell = ((Object[])rowdata[i])[0];
						rowdata[i] = cell;
					}
				}
		
				target.getView().getChanges().removeHandlerAt(exceptions);
			}
			catch(Exception e)
			{
				if(!alert(e)) break;
				exceptions++;
			}			
		}
		target.getView().onTableChanged(true);
		target.doStop();
	}
	
	private boolean alert(Exception e)
	{
		String title = e.getClass().getName();
		String message = Text.wrap(e.toString(),75);
		message += "\nDo you want continue?";
		
		return JOptionPane.showConfirmDialog(Application.window,message,title,JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;		
	}
	
	private void close() throws Exception
	{
		if(pstmt!=null)
		{
			pstmt.close();
			pstmt = null;
		}		
	}
	
	private void execute(String sql, Vector values) throws Exception
	{
		ConnectionHandler ch = ConnectionAssistant.getHandler(target.getHandlerKey());
		pstmt = ch.get().prepareStatement(sql);
				for(int i=0; i<values.size(); i++)
		{
			Object[] param = (Object[])values.elementAt(i);
						if(param[0] == null)
			{
				pstmt.setNull(i+1,((Integer)param[1]).intValue());
				continue;
			}
			
			switch( ((Integer)param[1]).intValue() )
			{
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
			case Types.LONGNVARCHAR:
			case Types.NCHAR:
			case Types.NVARCHAR:
				pstmt.setString(i+1,(String)param[0]);
				break;
			case Types.DECIMAL:
			case Types.DOUBLE:
				pstmt.setDouble(i+1, new Double(param[0].toString()).doubleValue());
				break;
			case Types.FLOAT:
				pstmt.setFloat(i+1, new Float(param[0].toString()).floatValue());
				break;
			case Types.INTEGER:
				pstmt.setInt(i+1, new Integer(param[0].toString()).intValue());
				break;
			case Types.NUMERIC:
				pstmt.setBigDecimal(i+1, new BigDecimal(param[0].toString()));
				break;
			default:
				pstmt.setObject(i+1,param[0],((Integer)param[1]).intValue());
			}
		}
		// Ticket #337 - savepoint enable/disable preferences
		boolean hasSavepoint = Preferences.getBoolean("application.autoSavePoint", false);
		if (hasSavepoint)
		{
			// test #329 Query builder / Command editor: avoid PostgreSQL ERROR: current transaction is aborted
			// rs = stmt.executeQuery(syntax);
			String savepointName = "AutoSavepoint";
			Savepoint savepoint= ch.get().setSavepoint(savepointName);
			try {
					pstmt.executeUpdate();
			} catch (SQLException sqle) {
						ch.get().rollback(savepoint);
						Application.println(sqle, true);
			}
			// end test #329
		} else {
			try {
				pstmt.executeUpdate();
			} catch (SQLException sqle) {
						Application.println(sqle, true);
			}
		}
		this.close();
	}
}