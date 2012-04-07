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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import com.sqleo.common.util.Appearance;


public class HeaderCellRenderer extends DefaultTableCellRenderer
{
	public HeaderCellRenderer()
	{
		setFont(Appearance.fontPLAIN);
		setHorizontalAlignment(JLabel.CENTER);
	}
		
	public void setSelected(boolean b)
	{
		setFont(b ? Appearance.fontBOLD : Appearance.fontPLAIN);
	}
		
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(table!=null)
		{
			JTableHeader header = table.getTableHeader();
			if (header!=null)
			{
				setForeground(header.getForeground());
				setBackground(header.getBackground());
			}
		}
				
		setText((value == null) ? "" : value.toString());
		setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		
		return this;
	}
}