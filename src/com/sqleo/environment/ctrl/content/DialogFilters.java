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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.environment.Application;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.environment.ctrl.editor.ClauseCondition;
import com.sqleo.environment.ctrl.editor._ClauseOwner;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Column;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class DialogFilters extends AbstractDialogConfirm implements _ClauseOwner
{
	private ClauseCondition where;
	private ContentPane content;
	private boolean modified;
	
	public DialogFilters(ContentPane content)
	{
		super(Application.window,"Filters");
		this.content = content;
		
		where = new ClauseCondition(this, content);
		where.setHandlerKey(content.getHandlerKey());
		getContentPane().add(new JScrollPane(where));
	}

	@Override
	protected boolean onConfirm()
	{
		Set<Integer> emptyRows = new HashSet<Integer>();
		int totalRows =where.getModel().getRowCount()-1; 
		for(int i=0; i<totalRows; i++)
		{
			Object[] rowdata = new Object[4];
			for(int j=0; j<4;j++) rowdata[j] = where.getModel().getValueAt(i,j+1);
			if (rowdata[0] == null && rowdata[2] == null){
				emptyRows.add(Integer.valueOf(i));
			}
		}
		
		if(modified)
		{
			QueryTokens.Condition[] qtokens = content.getQueryModel().getQueryExpression().getQuerySpecification().getWhereClause();
			for(int i=0; i<qtokens.length; i++)
			{
				content.getQueryModel().getQueryExpression().getQuerySpecification().removeWhereClause(qtokens[i]);
			}

			String append = null;
			for(int i=0; i<totalRows; i++)
			{
				if(emptyRows.contains(Integer.valueOf(i)))
					continue;
				Object[] rowdata = new Object[4];
				for(int j=0; j<4;j++) rowdata[j] = where.getModel().getValueAt(i,j+1);

				QueryTokens.DefaultExpression exprL = new QueryTokens.DefaultExpression((rowdata[0] == null ? null : rowdata[0].toString()));
				QueryTokens.DefaultExpression exprR = new QueryTokens.DefaultExpression((rowdata[2] == null ? null : rowdata[2].toString()));

				QueryTokens.Condition ctoken = new QueryTokens.Condition(append,exprL,rowdata[1].toString(),exprR);
				content.getQueryModel().getQueryExpression().getQuerySpecification().addWhereClause(ctoken);
				
				append = (rowdata[3]!=null ? rowdata[3].toString() : null);
			}
			if(content.getClientQB()!=null){
				content.getClientQB().getQueryBuilder().setQueryModel(content.getQueryModel());
			}
			content.getActionMap().get("task-go").actionPerformed(null);
		}
		
		return true;
	}

	@Override
	protected void onOpen()
	{
		QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
		for(int i=0; i<e.length; i++)
		{
			if(e[i] instanceof QueryTokens.Column)
			{
				Column c = ((QueryTokens.Column)e[i]);
				where.addColumn(c);
				
			}
		}
		
		QueryTokens.Condition[] qtokens = content.getQueryModel().getQueryExpression().getQuerySpecification().getWhereClause();
		for(int i=0; i<qtokens.length; i++)
		{
			int r = where.addRow();
			where.setValueAt(qtokens[i].getLeft().toString(),r,1);
			where.setValueAt(qtokens[i].getOperator(),r,2);
			where.setValueAt(qtokens[i].getRight().toString(),r,3);
			if(i+1<qtokens.length){
				final String append = qtokens[i+1].getAppend()!=null?qtokens[i+1].getAppend().toUpperCase():null;
				if(append!=null && (append.equals(_ReservedWords.OR) ||append.equals(_ReservedWords.AND))){
					where.setValueAt(append,r,4);
				}
			}
		}		
	}

	@Override
	public void fireQueryChanged()
	{
		modified = true;
	}
}
