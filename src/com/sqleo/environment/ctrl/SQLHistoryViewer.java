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

package com.sqleo.environment.ctrl;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.sqleo.common.gui.ListView;
import com.sqleo.common.util.SQLHistoryData;
import com.sqleo.environment.Application;


public class SQLHistoryViewer extends ListView  implements MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SQLHistoryViewer()
	{
		addMouseListener(this);
		
		addColumn("Timestamp");
		addColumn("Connection");
		addColumn("Window");
		addColumn("Query");
		
		for(SQLHistoryData line : Application.session.getSQLHistoryData()){
			final Object[] rowdata = toRowData(line);
			addRow(rowdata);
		}
	}
	
	private Object[] toRowData(final SQLHistoryData line){
		final Object[] rowdata = new Object[4];
		try {
			rowdata[0] = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy").parse(line.getTimestamp());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		rowdata[1] = line.getConnection();
		rowdata[2] = line.getWindow();
		rowdata[3] = line.getQuery();
		return rowdata;
	}
	
	public void addRowAtFirst(final SQLHistoryData line) {
		final Object[] rowdata = toRowData(line);
		addRowAtFirst(rowdata);
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {	}
	@Override
	public void mouseEntered(MouseEvent arg0) {	}
	@Override
	public void mouseExited(MouseEvent arg0) { }
	@Override
	public void mousePressed(MouseEvent arg0) {	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if(SwingUtilities.isRightMouseButton(me))
		{
			int row = getJavaComponent().rowAtPoint(me.getPoint());
			getJavaComponent().setRowSelectionInterval(row,row);
			
			JPopupMenu popup = new JPopupMenu();
			popup.add(new ActionCopyQuery());
			popup.add(new ActionDeleteRow());
			popup.show(getJavaComponent(),me.getX(),me.getY());
		}
	}
	
	private class ActionCopyQuery extends AbstractAction
	{
		private ActionCopyQuery()
		{
			putValue(NAME,"Copy query");
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			copyValueAt(getSelectedRow(), 3);
		}
	}
	private class ActionDeleteRow extends AbstractAction
	{
		private ActionDeleteRow()
		{
			putValue(NAME,"Delete row");
		}
        
		public void actionPerformed(ActionEvent ae)
		{
			final int selectedRow = getSelectedRow();
			final Date timestamp = (Date) getValueAt(selectedRow, 0);
			Application.session.removeSQLFromHistory(timestamp.toString());
			removeRow(getSelectedRow());
		}
	}
}