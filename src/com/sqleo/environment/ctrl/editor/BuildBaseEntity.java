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

import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.sqleo.environment.Preferences;

public class BuildBaseEntity extends JTable
{
	private BuildBasePane builder;
	
	public BuildBaseEntity(BuildBasePane builder)
	{
		this.builder = builder;

		DefaultTableModel model = new DefaultTableModel(0,3)
		{
			public Class getColumnClass(int column)
			{
				if(column == 0) return Boolean.class;
				return String.class;
			}
			
			public boolean isCellEditable(int row, int column)
			{
				switch(column)
				{
					case 0: return true;
					case 2: return ((Boolean)getValueAt(row,0)).booleanValue();
					default : return false;
				}
			}
		};
		setModel(model);
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(false);
		
		getTableHeader().setPreferredSize(new Dimension(0,0));
		getTableHeader().setVisible(false);
		
		TableColumn tableColumn = this.getColumn(this.getColumnName(0));
		tableColumn.setPreferredWidth(20);
		tableColumn.setMaxWidth(20);
		tableColumn.setResizable(false);
		
		final int oldRowHeight = getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			setRowHeight(newRowHeight);
		}
	}

	void addField(String name)
	{
		DefaultTableModel model = (DefaultTableModel)this.getModel();
		model.addRow(new Object[]{new Boolean(true),name,null});
	}
	
	void removeRows()
	{
		((DefaultTableModel)this.getModel()).setNumRows(0);
		((DefaultTableModel)this.getModel()).setRowCount(0);
	}
	
	public final void tableChanged(TableModelEvent e)
	{
		super.tableChanged(e);
		
		if(builder!=null)
			builder.fireQueryChanged();
	}
}
