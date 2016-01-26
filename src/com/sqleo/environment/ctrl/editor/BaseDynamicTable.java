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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.sqleo.environment.Preferences;

public class BaseDynamicTable extends JTable
{
	_ClauseOwner owner;
	
	BaseDynamicTable(_ClauseOwner owner, int col)
	{
		this.owner = owner;
		initTableModel(col);
	}
	
	protected BaseDynamicTable()
	{
		
	}
	
	protected void initTableModel(int col)
	{
		DefaultTableModel model = new DefaultTableModel(1,col+1)
		{
			public boolean isCellEditable(int row, int column)
			{
				return row<(getRowCount()-1) || column == 0;
			}
		};
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		setDefaultRenderer(Object.class, new InternalCellRenderer());
		getTableHeader().setPreferredSize(new Dimension(0,0));
		getTableHeader().setVisible(false);
		
		TableColumn tableColumn = this.getColumn(0);
		tableColumn.setCellEditor(new ButtonCell());
		tableColumn.setCellRenderer(new ButtonCell());
		tableColumn.setPreferredWidth(15);
		tableColumn.setMaxWidth(15);
		tableColumn.setResizable(false);
		
		final int oldRowHeight = getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			setRowHeight(newRowHeight);
		}
		
		if(col>1){
			DefaultCellEditor singleclick = new DefaultCellEditor(new JTextField());
			singleclick.setClickCountToStart(1);
			this.getColumn(col-1).setCellEditor(singleclick);
		}
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
	
	protected TableColumn getColumn(int index)
	{
		return this.getColumn(this.getColumnName(index));
	}
	
	public int addRow()
	{
		int row = this.getRowCount()-1;
		
		this.editingStopped(new ChangeEvent(this));
		((DefaultTableModel)this.getModel()).insertRow(row, new Object[this.getColumnCount()]);
		
		return row;
	}
	
	public void removeRow()
	{
		this.editingCanceled(new ChangeEvent(this));
		((DefaultTableModel)this.getModel()).removeRow(this.getSelectedRow());
	}

	public void removeRows()
	{
		this.editingStopped(new ChangeEvent(this));
		for(int i=0; this.getRowCount()>1; i++)
			((DefaultTableModel)this.getModel()).removeRow(0);
	}
	
	public final void tableChanged(TableModelEvent e)
	{
		super.tableChanged(e);
		
		if(owner!=null)
			owner.fireQueryChanged();
	}
	
	private class InternalCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(table.isCellEditable(row,column))
				super.setBackground(UIManager.getColor("Table.background"));
			else
				super.setBackground(UIManager.getColor("TableHeader.background"));
			
			setFont(table.getFont());
			setValue(value);

			return this;
		}
	}
	
	private class ButtonCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener
	{
		private JButton button = new JButton();
		
		private ButtonCell()
		{
			button.addActionListener(this);
			button.setBorder(null);
		}

		public void actionPerformed(ActionEvent e)
		{
			if(button.getText().equals("+"))
			{
				BaseDynamicTable.this.addRow();
				this.setMinus();
			}
			else
			{
				BaseDynamicTable.this.removeRow();
			}
		}
		
		private void setMinus()
		{
			button.setToolTipText("remove");
			button.setText("-");
		}
		
		private void setPlus()
		{
			button.setToolTipText("add");
			button.setText("+");
		}
		
		private Component getCell(JTable table, int row)
		{
			if(row == table.getRowCount()-1)
				this.setPlus();
			else
				this.setMinus();
			
			return button;
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			return getCell(table,row);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return getCell(table,row);
		}
		
		public Object getCellEditorValue()
		{
			return button.getText();
		}
	}
}
