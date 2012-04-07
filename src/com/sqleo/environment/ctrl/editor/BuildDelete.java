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

package com.sqleo.environment.ctrl.editor;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import com.sqleo.common.gui.CustomLineBorder;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class BuildDelete extends BuildBasePane implements _ClauseOwner
{
	private ClauseCondition where;
	
	public BuildDelete(_BuildOwner owner)
	{
		super(owner);
	}

	void initComponents()
	{
		where = new ClauseCondition(this);
		where.setBorder(new CustomLineBorder(true,true,false,false));
		
		JScrollPane scroll = new JScrollPane(where);
		scroll.setBorder(new TitledBorder(" where "));
		setComponentCenter(scroll);
	}
	
	void add(QueryTokens.Column column)
	{
		where.addColumn(SQLFormatter.ensureQuotes(column.getName(),owner.getIdentifierQuoteString(),!QueryBuilder.useAlwaysQuote));
	}
	
	void clear()
	{
		where.removeRows();
	}
	
	public JComponent getComponent()
	{
		return this;
	}

	public String getSyntax()
	{
		StringBuffer buffer = new StringBuffer();
		
		if(where!=null)
		{
			String append = null;
			for(int i=0; i<where.getModel().getRowCount()-1; i++)
			{
				Object[] rowdata = new Object[4];
				for(int j=0; j<4;j++) rowdata[j] = where.getModel().getValueAt(i,j+1);

				QueryTokens.DefaultExpression exprL = new QueryTokens.DefaultExpression((rowdata[0] == null ? null : rowdata[0].toString()));
				QueryTokens.DefaultExpression exprR = new QueryTokens.DefaultExpression((rowdata[2] == null ? null : rowdata[2].toString()));

				buffer.append(new QueryTokens.Condition(append,exprL,rowdata[1].toString(),exprR));
				
				append = (rowdata[3]!=null ? " " + rowdata[3].toString() : null);
			}
			
			if(buffer.length()>0) buffer.insert(0," WHERE ");
			
			return "DELETE FROM " + owner.getTable() + buffer;
		}
		
		return "DELETE FROM";
	}
}
