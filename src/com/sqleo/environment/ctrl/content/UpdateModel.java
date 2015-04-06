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

import java.util.Arrays;

import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class UpdateModel
{
	private QueryTokens.Table table = null;
	private QueryTokens.Column[] rowid = null;
	
	public boolean isUpdatable(QueryTokens._Expression e)
	{
		if(!(e instanceof QueryTokens.Column)) return false;
		QueryTokens.Column c = (QueryTokens.Column)e;
		String owner = c.getTable() != null ? c.getTable().getReference() : null;
		return owner == null || table.getReference().equalsIgnoreCase(owner);
	}	
	
	public QueryTokens.Table getTable()
	{
		return table;
	}

	public void setTable(QueryTokens.Table table)
	{
		this.table = table;
	}

	public QueryTokens.Column getRowIdentifier(int idx)
	{
		return rowid[idx];
	}
	
	public int getRowIdentifierCount()
	{
		return rowid.length;
	}
	
	public void setRowIdentifier(QueryTokens.Column[] where)
	{
		this.rowid = where;
	}	


	public String getDeleteSyntax(Object[] whereValues)
	{
		return "DELETE FROM " + table.getIdentifier() + SQLFormatter.SPACE + getWhereSyntax(whereValues);
	}
	
	public String getInsertSyntax(String[] columns)
	{
		Object[] fieldValues = new Object[columns.length];
		Arrays.fill(fieldValues,"?");
		
		return getInsertSyntax(columns,fieldValues);
	}
	
	public String getInsertSyntax(String[] columns, Object[] fieldValues)
	{
		return	"INSERT INTO " + table.getIdentifier() +
				" (" + SQLFormatter.concatCommaDelimited(columns,false,0) +
				") VALUES (" + SQLFormatter.concatCommaDelimited(fieldValues,false,0) + ")";
	}
	
	public String getUpdateSyntax(String[] columns, Object[] whereValues)
	{
		Object[] fieldValues = new Object[columns.length];
		Arrays.fill(fieldValues,"?");
		
		return getUpdateSyntax(columns, fieldValues, whereValues);
	}
	
	public String getUpdateSyntax(String[] columns, Object[] fieldValues, Object[] whereValues)
	{
		String changes = new String();
		for(int i=0; i<fieldValues.length; i++)
			changes += columns[i] + " = " + fieldValues[i] + ",";
		
		return	"UPDATE " + table.getIdentifier() +
				" SET " + changes.substring(0,changes.length()-1) +
				SQLFormatter.SPACE + getWhereSyntax(whereValues);
	}
	
	private String getWhereSyntax(Object[] params)
	{
		if(rowid == null || params == null) return new String();
		
		QueryTokens.Condition[] c = new QueryTokens.Condition[rowid.length];
		for(int i=0; i<c.length; i++)
			c[i] = new QueryTokens.Condition(SQLFormatter.AND,rowid[i],
					params[i].toString().equalsIgnoreCase("null")?"IS":"=",
					new QueryTokens.DefaultExpression(params[i].toString()));
		c[0].setAppend(null);
		
		return SQLFormatter.WHERE + SQLFormatter.SPACE + SQLFormatter.concat(c,false, 0);
	}
/*	
	public static void main(String[] args)
	{
		QueryTokens.Table t = new QueryTokens.Table(null,"Tabella1");
		QueryTokens.Column[] c = new QueryTokens.Column[2];
		c[0] = new QueryTokens.Column(t,"Colonna1");
		c[1] = new QueryTokens.Column(t,"Colonna2");
		
		UpdateModel um = new UpdateModel();
		um.setTable(t);
		um.setRowIdentifier(c);
		
		System.out.println(um.getInsertSyntax(new String[]{"Colonna1","Colonna2"}));
		System.out.println(um.getInsertSyntax(new String[]{"Colonna1","Colonna2"},new Object[]{new Integer(1),"bla bla bla"}));
		
		System.out.println(um.getDeleteSyntax(new Object[]{new Integer(1),new Integer(1)}));
	}
*/
}
