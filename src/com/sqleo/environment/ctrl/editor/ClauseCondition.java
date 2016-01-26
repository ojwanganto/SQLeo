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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Column;
import com.sqleo.querybuilder.syntax.QueryTokens.Table;


public class ClauseCondition extends BaseDynamicTable
{
	protected Vector querytokens;
	protected JComboBox<String> cbxCols;
	protected JComboBox<String> cbxValues;
	private String handlerKey;
	private Table dbTable = new Table("", "");
	private Hashtable<String, String[]> columnDistinctValues = new Hashtable<String, String[]>();
	
	public String getHandlerKey() {
		return handlerKey;
	}

	public void setHandlerKey(String keycah) {
		this.handlerKey = keycah;
	}

	public ClauseCondition(_ClauseOwner owner)
	{
		super(owner,4);
	}

	/**
	 * Creates a ClauseCondition based on a unique table, to search for values in database
	 * @param owner
	 * @param defaultSchema
	 * @param defaultTableName
	 */
	public ClauseCondition(_ClauseOwner owner, String defaultSchema, String defaultTableName)
	{
		super(owner,4);
		this.dbTable = new Table(defaultSchema, defaultTableName);

	}

	/**
	 * It populates cbxValues according to the column/value in cbxCols.
	 * It's called from cbxCols.ItemListener and JTable.ListSelectionListener
	 * @param columnName Name of column to bring db values
	 * 
	 */
	private void populateValues(Object columnName){
		int selectedRow = getSelectedRow();

	    if (selectedRow > -1){
		    cbxValues.removeAllItems();
	    	Object selectedValue = columnName;
		    if (selectedValue != null 
		    	&& selectedValue.equals("") == false){
			    Column c = new Column(dbTable, selectedValue.toString());
			    String[] values = columnValues(c);
			    
			    if (values != null){
				    for (String value : values) {
					    cbxValues.addItem(value);
					}
			    }
		    }
    	}
	}
	
	protected void initTableModel(int col)
	{
		super.initTableModel(col);
		
		String[] operation = new String[]{"=","<",">","<=",">=","<>","like","not like","is","is not","in","not in"};
		
		TableColumn tableColumn = this.getColumn(1);

		cbxCols = new JComboBox();
		cbxValues = new JComboBox();

		// whenever the column changes, cbxValues can have different values
		ItemListener columnValuesListener = new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED){
					populateValues(e.getItem());
				}
			}
		};
		
		cbxCols.addItemListener(columnValuesListener);

		tableColumn.setCellEditor(new DefaultCellEditor(cbxCols));
		
		tableColumn = this.getColumn(2);
		tableColumn.setPreferredWidth(60);
		tableColumn.setMaxWidth(60);
		tableColumn.setResizable(false);
		tableColumn.setCellEditor(new DefaultCellEditor(new JComboBox(operation)));

		tableColumn = this.getColumn(3);
		tableColumn.setCellEditor(new DefaultCellEditor(cbxValues));

		tableColumn = this.getColumn(4);
		tableColumn.setPreferredWidth(55);
		tableColumn.setMaxWidth(55);
		tableColumn.setResizable(false);
		tableColumn.setCellEditor(new DefaultCellEditor(new JComboBox(new String[]{"AND","OR"})));
		
		querytokens = new Vector();
		cbxCols.setEditable(true);
		cbxValues.setEditable(true);
		

		// similar to ItemListener, but whenever a row is selected, cbxValues can be uptated
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectedRow = getSelectedRow();
				if (selectedRow > -1)
					populateValues(getValueAt(selectedRow, 1));
				
			}
		});

		this.getModel().addTableModelListener(new ChangeHandler());
	}

	/**
	 * Caches column values for each column
	 * @param columnName
	 * @return Values in a String[] array
	 */
	private String[] columnValues(Column columnName){
		
		String[] values = columnDistinctValues.get(columnName.getName());
		
		// it wont search if there is no table
		if (values == null && columnName.getTable().getName() != null && columnName.getTable().getName().equals("") == false){
			
			// TODO Add limit/top/rownumber/first/etc depending on RDBMS
			// see http://www.devmedia.com.br/select-top-em-varios-sgbds/25560
			String queryColumnValues = "SELECT DISTINCT(" + columnName.getName() + ") FROM " + columnName.getTable();
			values = searchColumnValues(queryColumnValues);
			
			columnDistinctValues.put(columnName.getName(), values);
			columnDistinctValues.put(columnName.getIdentifier(), values);
		}
		
		return values;
	}
	
	/**
	 * Search for db distinct values for each column in select.
	 * @param sql
	 * @return Values in a String[] array
	 */
	private String[] searchColumnValues(String sql){
		// TODO search database in background
		String[] values = null;
		
		try
		{
			ConnectionHandler ch = ConnectionAssistant.getHandler(handlerKey);
			Statement stmt = ch.get().createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			rs.last();
			values = new String[rs.getRow()]; 
			rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			boolean bStringValue = rsmd.getColumnClassName(1).equals("java.lang.String");
			
			while (rs.next()) {
				if (bStringValue)
					values[rs.getRow()-1] = "'" + rs.getString(1) + "'";
				else
					values[rs.getRow()-1] = rs.getString(1);
			}
			rs.close();
			stmt.close();
				
		}
		catch(SQLException sqle)
		{
			Application.println(sqle,true);
		}
				
		return values;
	}

	public void addColumn(String text)
	{
		cbxCols.addItem(text);
	}


	/**
	 * Add a column value for cbxCols and search db for values
	 * @param c
	 */
	public void addColumn(Column c)
	{
		columnValues(c);
		addColumn(c.getIdentifier());
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