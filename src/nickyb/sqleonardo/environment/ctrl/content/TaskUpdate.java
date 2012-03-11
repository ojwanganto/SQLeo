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

package nickyb.sqleonardo.environment.ctrl.content;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Vector;

import javax.swing.JOptionPane;

import nickyb.sqleonardo.common.jdbc.ConnectionAssistant;
import nickyb.sqleonardo.common.jdbc.ConnectionHandler;
import nickyb.sqleonardo.common.util.Text;
import nickyb.sqleonardo.environment.Application;
import nickyb.sqleonardo.environment.ctrl.ContentPane;
import nickyb.sqleonardo.querybuilder.syntax.QueryTokens;
import nickyb.sqleonardo.querybuilder.syntax.SQLFormatter;

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
				if(handler.type.equals(ContentChanges.DELETE))
				{
					sql = target.getUpdateModel().getDeleteSyntax();
				}
				else if(handler.type.equals(ContentChanges.UPDATE))
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
					sql = target.getUpdateModel().getUpdateSyntax((String[])columns.toArray(new String[columns.size()]));
				}
				
				for(int i=0; i<target.getUpdateModel().getRowIdentifierCount() ; i++)
				{
					int col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(i).getReference());
					if(col == -1) col = target.getView().getColumnIndex(target.getUpdateModel().getRowIdentifier(i).getName());

					Object cell = rowdata[col];
					if(cell instanceof Object[]) cell = ((Object[])cell)[1];
					params.addElement(new Object[]{cell, new Integer(target.getView().getColumnType(i))});
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
		message += "\ndo you want continue?";
		
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
			case Types.NUMERIC:
				pstmt.setLong(i+1, new Long(param[0].toString()).longValue());
				break;
			default:
				pstmt.setObject(i+1,param[0],((Integer)param[1]).intValue());
			}
		}
		pstmt.executeUpdate();
		this.close();
	}
}