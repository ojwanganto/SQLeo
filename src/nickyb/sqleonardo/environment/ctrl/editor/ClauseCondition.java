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

package nickyb.sqleonardo.environment.ctrl.editor;

import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import nickyb.sqleonardo.querybuilder.syntax.QueryTokens;

public class ClauseCondition extends BaseDynamicTable
{
	protected Vector querytokens;
	protected JComboBox cbxCols;
	
	public ClauseCondition(_ClauseOwner owner)
	{
		super(owner,4);
	}
	
	void initTableModel(int col)
	{
		super.initTableModel(col);
		
		String[] operation = new String[]{"=","<",">","<=",">=","<>","like","not like","is","is not","in","not in"};
		
		TableColumn tableColumn = this.getColumn(1);
		tableColumn.setCellEditor(new DefaultCellEditor(cbxCols = new JComboBox()));
		
		tableColumn = this.getColumn(2);
		tableColumn.setPreferredWidth(60);
		tableColumn.setMaxWidth(60);
		tableColumn.setResizable(false);
		tableColumn.setCellEditor(new DefaultCellEditor(new JComboBox(operation)));
		
		tableColumn = this.getColumn(4);
		tableColumn.setPreferredWidth(55);
		tableColumn.setMaxWidth(55);
		tableColumn.setResizable(false);
		tableColumn.setCellEditor(new DefaultCellEditor(new JComboBox(new String[]{"AND","OR"})));
		
		querytokens = new Vector();
		cbxCols.setEditable(true);
		
		this.getModel().addTableModelListener(new ChangeHandler());
	}
	
	public void addColumn(String text)
	{
		cbxCols.addItem(text);
	}
	
	public void removeColumn(String text)
	{
		cbxCols.removeItem(text);
	}
	
	private void onDelete(int row)
	{
		QueryTokens.Condition token = (QueryTokens.Condition)querytokens.elementAt(row);
		querytokens.removeElementAt(row);
		
		if(querytokens.size() > 0 && row < querytokens.size())
		{
			QueryTokens.Condition next = (QueryTokens.Condition)querytokens.get(row);
			next.setAppend(token.getAppend());
		}
	}
	
	private void onInsert(int row)
	{
		QueryTokens.Condition token = new QueryTokens.Condition(null,"=",null);
		querytokens.addElement(token);

		this.setValueAt("=",row,2);
		if(row>0)
		{
			Object append = this.getValueAt(row-1,4);
			if(append==null)
			{
				append = "AND";
				this.setValueAt(append,row-1,4);
			}
			token.setAppend(append.toString());
		}
	}
	
	private void onUpdate(int row, int col)
	{
		if(col > 0)
		{
			Object value = this.getValueAt(row,col);
			if(value == null) value = "";
			
			QueryTokens.Condition cond = (QueryTokens.Condition)querytokens.elementAt(row);
			if(col == 1)
				cond.setLeft(new QueryTokens.DefaultExpression(value.toString()));
			else if(col == 2)
				cond.setOperator(value.toString());
			else if(col == 3)
				cond.setRight(new QueryTokens.DefaultExpression(value.toString()));
			else if(col == 4 && ((row+2)<this.getRowCount()))
			{
				cond = (QueryTokens.Condition)querytokens.elementAt(row+1);
				cond.setAppend(value.toString());
			}
		}
	}
	
	class ChangeHandler implements TableModelListener
	{
		public void tableChanged(TableModelEvent tme)
		{
			switch(tme.getType())
			{
				case TableModelEvent.DELETE:
					onDelete(tme.getFirstRow());
					break;
				case TableModelEvent.INSERT:
					onInsert(tme.getFirstRow());
					break;
				case TableModelEvent.UPDATE:
					onUpdate(tme.getFirstRow(), tme.getColumn());
					break;
			}
			owner.fireQueryChanged();
		}
	}
}