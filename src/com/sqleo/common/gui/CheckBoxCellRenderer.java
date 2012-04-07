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

package com.sqleo.common.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class CheckBoxCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ItemListener
{
	protected Border noFocusBorder;
	private JCheckBox checkBox;
	
	public CheckBoxCellRenderer()
	{
		super();
		
		if(noFocusBorder == null)
		{
			noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		}
		
		checkBox = new JCheckBox();
		checkBox.addItemListener(this);
		checkBox.setOpaque(true);
		checkBox.setBorder(noFocusBorder);
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		fireEditingStopped();
	}

	private Component getCell(JTable table, Object value, boolean isSelected, boolean hasFocus)
	{
		if(value==null) value = new Boolean(false);
		checkBox.setSelected(((Boolean)value).booleanValue());
		checkBox.setFont(table.getFont());
		
		if (isSelected)
		{
			checkBox.setBackground(table.getSelectionBackground());
			checkBox.setForeground(table.getSelectionForeground());
		}
		else
		{
			checkBox.setBackground(table.getBackground());
			checkBox.setForeground(table.getForeground());
		}
		
		checkBox.setBorder((hasFocus) ? UIManager.getBorder("Table.focusCellHighlightBorder") : noFocusBorder);

		return checkBox;
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		return getCell(table, value, isSelected, true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		return getCell(table, value, isSelected, true);
	}
	
	public Object getCellEditorValue()
	{
		return new Boolean(checkBox.isSelected());
	}
}
