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

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import com.sqleo.common.gui.CustomLineBorder;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class BuildUpdate extends BuildBasePane implements _ClauseOwner
{
	private BuildBaseEntity entity;
	private ClauseCondition where;
	
	public BuildUpdate(_BuildOwner owner)
	{
		super(owner);
	}
	
	void initComponents()
	{
		entity = new BuildBaseEntity(this);
		entity.setBorder(new CustomLineBorder(true,true,false,false));
		
		where = new ClauseCondition(this);
		where.setBorder(new CustomLineBorder(true,true,false,false));
		
		JScrollPane scroll1 = new JScrollPane(entity);
		scroll1.setBorder(new TitledBorder(" set "));
		
		JScrollPane scroll2 = new JScrollPane(where);
		scroll2.setBorder(new TitledBorder(" where "));
		
		JPanel clauses = new JPanel(new GridLayout(2,1,5,5));
		clauses.add(scroll1);
		clauses.add(scroll2);
		setComponentCenter(clauses);
	}
	
	void add(QueryTokens.Column column)
	{
		String name = SQLFormatter.ensureQuotes(column.getName(),owner.getIdentifierQuoteString(),!QueryBuilder.useAlwaysQuote);
		entity.addField(name);
		where.addColumn(name);
	}	
	
	void clear()
	{
		entity.removeRows();
		where.removeRows();
	}
	
	public JComponent getComponent()
	{
		return this;
	}

	private String getWhere()
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
		}
	
		if(buffer.length()>0) buffer.insert(0," WHERE ");
	
		return buffer.toString();
	}

	public String getSyntax()
	{
		StringBuffer set = new StringBuffer();
		
		if(entity!=null && where!=null)
		{
			for(int i=0; i<entity.getRowCount(); i++)
			{
				if(entity.isCellEditable(i,2))
				{
					set.append(entity.getValueAt(i,1).toString() + " = ");
					set.append((entity.getValueAt(i,2)!=null?entity.getValueAt(i,2).toString():null) + ", ");
				}
			}
			
			if(set.length()>0) set.deleteCharAt(set.length()-2);
			
			return "UPDATE " + owner.getTable() + " SET " + set.toString().trim() + this.getWhere();
		}
		
		return "UPDATE";
	}
}
