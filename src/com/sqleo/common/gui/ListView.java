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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.sqleo.environment.Preferences;

public class ListView extends BorderLayoutPanel
{
	public final static short SORT_ASCENDING = 0;
	public final static short SORT_DESCENDING = 1;
	
	private int lastIdxSorted = -1;
	private short lastSortType = SORT_ASCENDING;
	
	private static ClipboardOwner defaultClipboardOwner = new ClipboardObserver();
	private JTable table;
    
	public ListView()
    {
    	super(2,2);
	    JScrollPane scroll = new JScrollPane(table = new JTable());
		scroll.getViewport().setBackground(Color.white);
	    setComponentCenter(scroll);
		
	    table.setShowGrid(false);
	    table.setIntercellSpacing(new Dimension(0,0));
		table.getTableHeader().addMouseListener(new SortListener());
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reset();
		
		table.getActionMap().put("Copy",new ActionCopyCell());
		table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C,KeyEvent.CTRL_MASK),"Copy");
		
		final int oldRowHeight = table.getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			table.setRowHeight(newRowHeight);
		}
    }
	
	public void setBackgroundColor(final Color color){
		table.setFillsViewportHeight(true);
		table.setBackground(color);
	}
    
	protected JTable getJavaComponent()
	{
		return table;
	}    
    
	public void addListSelectionListener(ListSelectionListener l)
	{
		table.getSelectionModel().addListSelectionListener(l);
	}
	
	public synchronized void addMouseListener(MouseListener l)
	{
		table.addMouseListener(l);
	}
    
	public void addColumn(String text)
	{
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.addColumn(text);
	}
    
	public void setColumnWidth(String text,int size)
	{
		TableColumn tableColumn = table.getColumn(text);
		tableColumn.setPreferredWidth(size);
		tableColumn.setWidth(size);
	}
	
	public void setHeaderVisible(boolean aFlag)
	{
		table.getTableHeader().setPreferredSize(new Dimension(0,0));
		table.getTableHeader().setVisible(aFlag);
	}
    
	public void addRow(Object[] rowdata)
	{
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.addRow(rowdata);
	}
	
	public void addRowAtFirst(Object[] rowdata)
	{
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.insertRow(0,rowdata);
	}
	
	public void removeLastRow(){
		removeRow(getRowCount()-1);
	}
	
	public boolean isSelectionEmpty()
	{
		return table.getSelectedRowCount() < 1;
	}	

	public String getColumnName(int col)
	{
		return table.getColumnName(col);
	}
	
    public int getColumnCount()
    {
        return table.getColumnCount();
    }
    
    public int getRowCount()
    {
        return table.getRowCount();
    }
    
    public int getSelectedRow()
    {
    	return table.getSelectedRow();
    }
    
	public Object getValueAt(int row,int col)
	{
		return table.getValueAt(row,col);
	}
    
	public void setValueAt(Object value,int row,int col)
	{
		table.setValueAt(value,row,col);
	}
    
	public void removeRow(int row)
	{
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.removeRow(row);
	}
	
	public void removeSelectedRow()
	{
		removeRow(getSelectedRow());
	}
	
	public void removeAllRows()
	{
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.setRowCount(0);
	}
	
	public void copyAllRows()
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<table.getRowCount(); i++)
		{
			for(int j=0; j<table.getColumnCount(); j++)
			{
				String cell = table.getValueAt(i,j) == null ? null : table.getValueAt(i,j).toString();
				if(j>0) sb.append("\t");
				sb.append(cell);
			}
			sb.append("\n");
		}

		Clipboard cb = this.getToolkit().getSystemClipboard();
		StringSelection contents = new StringSelection(sb.toString());
		cb.setContents(contents, defaultClipboardOwner);
	}
	
	public void copyValueAt(int row,int col){
		final String value = (String) getValueAt(row,col);
		Clipboard cb = this.getToolkit().getSystemClipboard();
		StringSelection contents = new StringSelection(value);
		cb.setContents(contents, defaultClipboardOwner);
	}
	
	public void reset()
	{
		lastSortType = SORT_ASCENDING;
		lastIdxSorted = -1;
		
		DefaultTableModel model = new DefaultTableModel()
		{
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		table.setModel(model);
	}
	
	public void tableDataChanged()
	{
		table.tableChanged(new TableModelEvent(table.getModel()));
	}
	
	private int compare(int col, int row1, int row2, short type)
	{
		Object value1 = getValueAt(row1,col);
		Object value2 = getValueAt(row2,col);

		int cmp = 0;
		if(value1!=null && value2!=null)
		{
			if(value1 instanceof Number)
			{
				double d1 = ((Number)value1).doubleValue();
				double d2 = ((Number)value2).doubleValue();
				
				if(d1 > d2)
					cmp = 1;
				else if(d1 < d2)
					cmp = -1;
			}
			else if(value1 instanceof Date && value2 instanceof Date)
			{
				cmp = ((Date)value1).compareTo((Date)value2);
			}
			else
			{
				cmp = value1.toString().toLowerCase().compareTo(value2.toString().toLowerCase());
			}
		}
		else if(value1!=null)
			cmp = 1;
		else if(value2!=null)
			cmp = -1;

		return type == SORT_ASCENDING ? cmp : cmp*(-1);
	}
	
	private void swap(int i, int j)
	{
		Vector data = ((DefaultTableModel)table.getModel()).getDataVector();
		
		Object appo = data.elementAt(i);
		data.setElementAt(data.elementAt(j),i);
		data.setElementAt(appo,j);
	}
	
	private void sort(int col, int first, int last, short type)
	{
		int lower = first + 1;
		int upper = last;
		
		swap(first,(first+last)/2);
		
		while(lower <= upper)
		{
			while(compare(col,lower,first,type) < 0) lower++;
			while(compare(col,first,upper,type) < 0) upper--;
			
			if(lower < upper)
				swap(lower++,upper--);
			else
				lower++;
		}
		
		swap(upper,first);
		if(first < upper-1)
			sort(col,first,upper-1,type);
		if(upper+1 < last)
			sort(col,upper+1,last,type);
	}
	
	private void sort(int col, short type)
	{
		int rows = getRowCount();
		if(rows < 2) return;
		
		int max = 0;
		for(int i=1; i < rows; i++)
			if(compare(col,max,i,type) < 0) max = i;
		
		swap(rows-1,max);
		sort(col,0,rows-2,type);
		
		table.tableChanged(new TableModelEvent(table.getModel()));
	}	
	
	private class SortListener extends MouseAdapter
	{
		public void mouseClicked(MouseEvent me)
		{
			if(lastIdxSorted != -1)
			{
				TableColumn ltc = ListView.this.table.getColumnModel().getColumn(lastIdxSorted);
				HeaderCellRenderer lhcr = ltc.getHeaderRenderer() == null ? new HeaderCellRenderer() : (HeaderCellRenderer)ltc.getHeaderRenderer();
				lhcr.setSelected(false);
				ltc.setHeaderRenderer(lhcr);			
			}
			
			int idx = ListView.this.table.getTableHeader().columnAtPoint(me.getPoint());
						
			TableColumn tc = ListView.this.table.getColumnModel().getColumn(idx);			
			HeaderCellRenderer hcr = tc.getHeaderRenderer() == null ? new HeaderCellRenderer() : (HeaderCellRenderer)tc.getHeaderRenderer();
			hcr.setSelected(true);
			tc.setHeaderRenderer(hcr);
			
			ListView.this.table.getTableHeader().repaint();
			
			if(idx == lastIdxSorted)
				lastSortType = lastSortType == SORT_ASCENDING ? SORT_DESCENDING : SORT_ASCENDING;
			else
				lastIdxSorted = idx;

			ListView.this.sort(lastIdxSorted,lastSortType);
		}
	}
	
	private class ActionCopyCell extends AbstractAction
	{
		public void actionPerformed(ActionEvent ae)
		{
			int col = ListView.this.table.getSelectedColumn();
			int row = ListView.this.table.getSelectedRow();
			
			if(row!=-1 && col!=-1)
			{
				if(ListView.this.table.getValueAt(row,col)==null) return;
				
				Clipboard cb = ListView.this.getToolkit().getSystemClipboard();
				
				String value = ListView.this.table.getValueAt(row,col).toString();
				StringSelection contents = new StringSelection(value);
				cb.setContents(contents, defaultClipboardOwner);
			}
		}
	}
	
	static class ClipboardObserver implements ClipboardOwner
	{
		public void lostOwnership(Clipboard clipboard, Transferable contents)
		{
		}
	}
}
