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

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sqleo.common.gui.AbstractDialogModal;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class DialogPreview extends AbstractDialogModal
{
	private JTextArea syntaxes;
	private ContentPane content;
	
	public DialogPreview(ContentPane content)
	{
		super(Application.window,"Changes");
		this.content = content;
			
		getContentPane().add(new JScrollPane(syntaxes=new JTextArea()));
		syntaxes.setEditable(false);
	}

	protected void onOpen()
	{
		ConnectionHandler ch = ConnectionAssistant.getHandler(content.getHandlerKey());
		for(int row=0; row<content.getView().getChanges().count(); row++)
		{
			Vector columns = new Vector();
			Vector values = new Vector();
			String sql = new String();
			
			ContentChanges.Handler handler = content.getView().getChanges().getHandlerAt(row);
			Object[] rowdata = content.getView().getValues(handler.rid);
			
			if(handler.type.equals(ContentChanges.INSERT))
			{
				for(int col=0; col<rowdata.length ; col++)
				{
					QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
					if(content.getUpdateModel().isUpdatable(e[col]))
					{						
						Object cell = rowdata[col];
						if(rowdata[col] instanceof Object[]) cell = ((Object[])rowdata[col])[0];
						values.addElement(toJdbcValue(cell,col));
							
						String cname = ((QueryTokens.Column)e[col]).getName();
						columns.addElement(SQLFormatter.ensureQuotes(cname,ch.getObject("$identifierQuoteString").toString(),true));
					}
				}
				sql = content.getUpdateModel().getInsertSyntax((String[])columns.toArray(new String[columns.size()]),values.toArray());
			}
			else
			{
				Boolean columnFound = true ;
				Vector whereValues = new Vector();
				for(int j=0; j<content.getUpdateModel().getRowIdentifierCount() ; j++)
				{
					int col = content.getView().getColumnIndex(content.getUpdateModel().getRowIdentifier(j).getReference());
					if(col == -1) col = content.getView().getColumnIndex(content.getUpdateModel().getRowIdentifier(j).getName());

					// Ticket #334 Content Window: java.lang.ArrayIndexOutOfBoundsException
					if(col == -1)
					{ 
						columnFound = false;
						Application.alert(Application.PROGRAM,"Column: \"" + content.getUpdateModel().getRowIdentifier(j).getName() + "\" not found in Grid" );
					}
					else
					{
						Object cell = rowdata[col];
						if(cell instanceof Object[]) cell = ((Object[])cell)[1];
						whereValues.addElement(toJdbcValue(cell,col));
					}


				}
				
				if(handler.type.equals(ContentChanges.DELETE) && columnFound)
				{
					sql = content.getUpdateModel().getDeleteSyntax(whereValues.toArray());
				}
				else if(handler.type.equals(ContentChanges.UPDATE)  && columnFound)
				{
					for(int col=0; col<rowdata.length ; col++)
					{
						if(rowdata[col] instanceof Object[])
						{
							QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
							if(content.getUpdateModel().isUpdatable(e[col]))
							{						
								Object cell = ((Object[])rowdata[col])[0];
								values.addElement(toJdbcValue(cell,col));
							
								String cname = ((QueryTokens.Column)e[col]).getName();
								columns.addElement(SQLFormatter.ensureQuotes(cname,ch.getObject("$identifierQuoteString").toString(),true));
							}
						}
					}
					sql = content.getUpdateModel().getUpdateSyntax((String[])columns.toArray(new String[columns.size()]),values.toArray(),whereValues.toArray());
				}
			}
			syntaxes.append(sql.trim() + ";\n");
		}
	}
	
	private String toJdbcValue(Object value, int col)
	{
		return SQLFormatter.toJdbcValue(value,content.getView().getColumnType(col));
	}
}
