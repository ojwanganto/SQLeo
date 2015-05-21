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

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import com.sqleo.common.util.Appearance;
import com.sqleo.environment.Preferences;


public class LineNumberView extends JTable
{
	private int block = 1;
	private int rows = 0;

	public LineNumberView()
	{
		super(0,1);
		setAutoscrolls(false);
		setAutoCreateColumnsFromModel(false);
			
		getColumnModel().getColumn(0).setPreferredWidth(50);
		getColumnModel().getColumn(0).setCellRenderer(new InternalCellRenderer());
			
		setPreferredScrollableViewportSize(getPreferredSize());
		
        final int oldRowHeight = getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			setRowHeight(newRowHeight);
		}
	}
 
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
		
	public Class getColumnClass(int idx)
	{
		return Object.class;
	}
		
	public int getColumnCount()
	{
		return 1;
	}
 
	public String getColumnName(int idx)
	{
		return "#";
	}

	public int getRowCount()
	{
		return rows;
	}

	public void setRowCount(int rows)
	{
		this.rows = rows;
	}
 
	void setBlock(int idx)
	{
		block = idx;
	}
	
	public Object getValueAt(int row, int column)
	{
		return new Integer((ContentModel.MAX_BLOCK_RECORDS * (block-1)) + (row+1));
	}

	private class InternalCellRenderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(table != null)
			{
				JTableHeader header = table.getTableHeader();
				if (header != null)
				{
					setForeground(header.getForeground());
					setBackground(header.getBackground());
				}
			}
	
			setHorizontalAlignment(JLabel.CENTER);
			setText((value == null) ? "" : value.toString());
			setFont(isSelected ? Appearance.fontBOLD : Appearance.fontPLAIN);
			
			return this;
		}
	}
}