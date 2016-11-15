/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

package com.sqleo.environment.ctrl.comparer.data;

import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import com.sqleo.common.util.SQLHelper;
import com.sqleo.environment.ctrl.editor.BaseDynamicTable;


public class DataComparerDialogTable extends BaseDynamicTable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String[] operation = new String[]{"=","<",">","<=",">=","<>","!=","like","not like","is","is not","in","not in"};
	private static final String[] aggregates = SQLHelper.SQL_AGGREGATES;
	public static enum DATA_TYPE { COLUMNS, AGGREGATES , FILTERS};
	
	private Vector<String> querytokens;
	private JComboBox cbxCols;
	private DATA_TYPE dataType;
		
	public DataComparerDialogTable(final DATA_TYPE dataType){
		this.dataType = dataType;
		initTableModelInternal();
	}
	
	public DATA_TYPE getDataType(){
		return this.dataType;
	}
	
	public String getQueryTokensAsString(){
		String separator = "";
		switch(dataType){
		case COLUMNS : 
		case AGGREGATES:
			separator = ",";
			break;
		case FILTERS:
			separator = "\n";
			break;
		}
		final StringBuilder builder = new StringBuilder();
		final int size = querytokens.size(); 
		for(int i=0; i<size; i++){
			builder.append(querytokens.get(i));
			if(i!=size-1){
				builder.append(separator);
			}
		}
		return builder.toString();
	}
	
	private void initTableModelInternal(){
		int col = 0;
		switch(dataType){
		case COLUMNS : 
			col = 1;
			break;
		case AGGREGATES:
			col = 2; 
			break;
		case FILTERS:
			col = 4;
			break;
		}
		
		super.initTableModel(col);
		TableColumn tableColumn = null;
		
		switch(dataType){
		case COLUMNS : 
			tableColumn = this.getColumn(1);
			tableColumn.setCellEditor(new DefaultCellEditor(cbxCols = new JComboBox()));
			break;
		case AGGREGATES: 
			tableColumn = this.getColumn(1);
			tableColumn.setPreferredWidth(60);
			tableColumn.setMaxWidth(60);
			tableColumn.setResizable(false);
			tableColumn.setCellEditor(new DefaultCellEditor(new JComboBox(aggregates)));
			
			tableColumn = this.getColumn(2);
			tableColumn.setCellEditor(new DefaultCellEditor(cbxCols = new JComboBox()));
			break;
		case FILTERS:
			tableColumn = this.getColumn(1);
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
			break;
		}
		
		querytokens = new Vector();
		cbxCols.setEditable(true);
		
		this.getModel().addTableModelListener(new ChangeHandler());
	}
	
	public void addColumns(String[] columns){
		cbxCols.removeAllItems();
		for(String text : columns)
			addColumn(text);
	}
	
	public void addColumn(String text){
		cbxCols.addItem(text);
	}
	
	public void removeColumn(String text){
		cbxCols.removeItem(text);
	}
	
	private void onDelete(int row){
		querytokens.removeElementAt(row);
	}
	
	private String getTokenValue(int row){
		switch (dataType) {
		case COLUMNS:
			return (String) this.getValueAt(row, 1);
		case AGGREGATES:
			final String aggregate = (String) this.getValueAt(row, 1);
			final String column = (String) this.getValueAt(row, 2);
			if(aggregate!=null && column!=null){
				return aggregate + "(" + column+ ")";
			}else{
				return "";
			}
		case FILTERS:
			final String columnForFilter = (String) this.getValueAt(row, 1);
			final String operator = (String) this.getValueAt(row, 2);
			final String value = (String) this.getValueAt(row, 3);
			final String condition = (String) this.getValueAt(row, 4);
			final StringBuilder token = new StringBuilder();
			if(columnForFilter!=null && operator!=null && value!=null){
				token.append(columnForFilter).append(operator).append(value);
				if(condition!=null){
					token.append(" ").append(condition).append(" ");
				}else if(row!=getRowCount()-2){
					token.append(" AND ");
					this.setValueAt("AND",row,4);
				}
			}
			return token.toString();
		}
		return "";
	}
	
	private void onInsert(int row){
		querytokens.add(getTokenValue(row));
	}
	
	private void onUpdate(int row, int col){
		if(col > 0){
			final Object value = this.getValueAt(row,col);
			if(null == value){
				querytokens.set(row, "");
			}else{
				querytokens.set(row, getTokenValue(row));	
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
		}
	}
}