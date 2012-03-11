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

import javax.swing.JScrollPane;

import nickyb.sqleonardo.common.gui.AbstractDialogConfirm;
import nickyb.sqleonardo.environment.Application;
import nickyb.sqleonardo.environment.ctrl.ContentPane;
import nickyb.sqleonardo.environment.ctrl.editor.ClauseCondition;
import nickyb.sqleonardo.environment.ctrl.editor._ClauseOwner;
import nickyb.sqleonardo.querybuilder.syntax.QueryTokens;

public class DialogFilters extends AbstractDialogConfirm implements _ClauseOwner
{
	private ClauseCondition where;
	private ContentPane content;
	private boolean modified;
	
	public DialogFilters(ContentPane content)
	{
		super(Application.window,"filters");
		this.content = content;
		
		where = new ClauseCondition(this);
		getContentPane().add(new JScrollPane(where));
	}

	protected boolean onConfirm()
	{
		if(modified)
		{
			QueryTokens.Condition[] qtokens = content.getQueryModel().getQueryExpression().getQuerySpecification().getWhereClause();
			for(int i=0; i<qtokens.length; i++)
			{
				content.getQueryModel().getQueryExpression().getQuerySpecification().removeWhereClause(qtokens[i]);
			}

			String append = null;
			for(int i=0; i<where.getModel().getRowCount()-1; i++)
			{
				Object[] rowdata = new Object[4];
				for(int j=0; j<4;j++) rowdata[j] = where.getModel().getValueAt(i,j+1);

				QueryTokens.DefaultExpression exprL = new QueryTokens.DefaultExpression((rowdata[0] == null ? null : rowdata[0].toString()));
				QueryTokens.DefaultExpression exprR = new QueryTokens.DefaultExpression((rowdata[2] == null ? null : rowdata[2].toString()));

				QueryTokens.Condition ctoken = new QueryTokens.Condition(append,exprL,rowdata[1].toString(),exprR);
				content.getQueryModel().getQueryExpression().getQuerySpecification().addWhereClause(ctoken);
				
				append = (rowdata[3]!=null ? rowdata[3].toString() : null);
			}
			
			content.getActionMap().get("task-go").actionPerformed(null);
		}
		
		return true;
	}

	protected void onOpen()
	{
		QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
		for(int i=0; i<e.length; i++)
		{
			if(e[i] instanceof QueryTokens.Column)
			{
				where.addColumn(((QueryTokens.Column)e[i]).getIdentifier());
			}
		}
		
		QueryTokens.Condition[] qtokens = content.getQueryModel().getQueryExpression().getQuerySpecification().getWhereClause();
		for(int i=0; i<qtokens.length; i++)
		{
			int r = where.addRow();
			where.setValueAt(qtokens[i].getLeft().toString(),r,1);
			where.setValueAt(qtokens[i].getOperator(),r,2);
			where.setValueAt(qtokens[i].getRight().toString(),r,3);
			where.setValueAt(qtokens[i].getAppend(),r,4);
		}		
	}

	public void fireQueryChanged()
	{
		modified = true;
	}
}
