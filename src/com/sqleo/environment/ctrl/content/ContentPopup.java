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

import java.awt.Cursor;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import com.sqleo.environment.Application;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;


public class ContentPopup extends JPopupMenu implements MouseListener
{
	private ContentView view;
	
	private boolean bAllCols; // row-popoup
	private boolean bAllRows; // col-popoup
	
	ContentPopup(ContentView view)
	{
		this.view = view;
		
		add(new ActionCopySpecial());
		addSeparator();
		add(new ActionCopy());
		add(new ActionPaste());
		addSeparator();
		add(new ActionSetNull());
		add(new ActionSetToday());
		add(new ActionResetOldValue());
		add(new ActionTrim());
				
		addSeparator(); // 9
		add(new ActionClone());
		JMenuItem iRi = add(view.getControl().getActionMap().get("record-insert"));
		JMenuItem iRd = add(view.getControl().getActionMap().get("record-delete"));
		addSeparator(); // 13
		add("save record").setEnabled(false);
		
		addSeparator(); // 15
		add(new ActionJump());
		
		addSeparator(); // 17
		add(new ActionSortAsc());
		add(new ActionSortDesc());
		
		iRi.setToolTipText(null);
		iRi.setIcon(null);
		
		iRd.setToolTipText(null);
		iRd.setIcon(null);
	}
	
	protected MenuElement getSubElementsAt(int index)
	{
		return this.getSubElements()[index];
	}
	
	public void mouseEntered(MouseEvent me){}
	public void mouseExited(MouseEvent me){}	
	public void mouseClicked(MouseEvent me){}

	public void mousePressed(MouseEvent me)
	{		
		if(me.getSource() instanceof JTableHeader)
		{
			JTableHeader header = (JTableHeader)me.getSource();
			
			int row = view.getRow() == -1 && view.getRowCount() > 0 ? 0 : view.getRow();
			int col = header.columnAtPoint(me.getPoint());
			
			if(row==-1 || col==-1) return;
			view.setSelectedCell(row,col);
		}
	}

	public void mouseReleased(MouseEvent me)
	{
		if(view.getControl().isBusy()) return;
		
		bAllCols = false;
		bAllRows = false;
		
		if(SwingUtilities.isRightMouseButton(me))
		{
			int row = -1;
			int col = -1;
			
			if(me.getSource() instanceof JTable)
			{
				JTable jtable = (JTable)me.getSource();
				row = jtable.rowAtPoint(me.getPoint());
				col = jtable.columnAtPoint(me.getPoint());
				
				bAllCols = jtable instanceof LineNumberView;
			}
			else if(me.getSource() instanceof JTableHeader)
			{
				JTableHeader header = (JTableHeader)me.getSource();
				row = view.getRow();
				col = header.columnAtPoint(me.getPoint());
				
				bAllRows = true;
			}
			else
			{
				row = view.getRow();
				col = view.getColumn();
			}
			
			if(row==-1 || col==-1) return;
			view.setSelectedCell(row,col);
			
			// record
			getComponent(9).setVisible(bAllCols);
			getComponent(10).setVisible(bAllCols);
			getComponent(11).setVisible(bAllCols);
			getComponent(12).setVisible(bAllCols);
			getComponent(13).setVisible(bAllCols);
			getComponent(14).setVisible(bAllCols);
			
			// jump
			getComponent(15).setVisible(!bAllCols && !bAllRows);
			getComponent(16).setVisible(!bAllCols && !bAllRows);
			
			// sort
			getComponent(17).setVisible(bAllRows);
			getComponent(18).setVisible(bAllRows);
			getComponent(19).setVisible(bAllRows);
			
			show((JComponent)me.getSource(),me.getX(),me.getY());
		}
	}

	private class ActionCopySpecial extends AbstractAction
	{
		ActionCopySpecial()
		{
			this.putValue(NAME,"copy special");
		}

		public void actionPerformed(ActionEvent ae)
		{
			int col = ContentPopup.this.view.getColumn();
			int row = ContentPopup.this.view.getRow();
				
			Object value = this.getValueAt(row,col);
			
			if(bAllCols)
			{
				String fields = new String();
				String values = new String();
				
				for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
				{
					fields = i==0 ? ContentPopup.this.view.getColumnName(i) : fields + "," + ContentPopup.this.view.getColumnName(i);
					values = i==0 ? this.getValueAt(row,i) : values + "," + this.getValueAt(row,i);
				}
				
				value = "INSERT INTO <tablename> (" + fields + ") VALUES (" + values + ");";
			}
			else if(bAllRows)
			{
				for(int i=0; i<ContentPopup.this.view.getFlatRowCount(); i++)
				{
					Object cell = this.getFlatValueAt(i,col);
					value = i==0 ? cell : value + "," + cell;
				}
			}
			
			if(value == null) return;
			
			Clipboard cb = ContentPopup.this.getToolkit().getSystemClipboard();
			StringSelection contents = new StringSelection(value.toString());
			cb.setContents(contents, Application.defaultClipboardOwner);
		}
		
		String getValueAt(int row, int col)
		{
			Object cell = ContentPopup.this.view.getValueAt(row,col);
			return toJdbcValue(cell,col);
		}
		
		String getFlatValueAt(int row, int col)
		{
			Object cell = ContentPopup.this.view.getFlatValueAt(row,col);
			return toJdbcValue(cell,col);
		}
		
		private String toJdbcValue(Object value, int col)
		{
			return SQLFormatter.toJdbcValue(value,ContentPopup.this.view.getColumnType(col));
		}
	}

	private class ActionCopy extends AbstractAction
	{
		ActionCopy(){super("copy");}
		
		public void actionPerformed(ActionEvent ae)
		{
			int col = ContentPopup.this.view.getColumn();
			int row = ContentPopup.this.view.getRow();
				
			Object value = ContentPopup.this.view.getValueAt(row,col);
			
			if(bAllCols)
			{
				for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
				{
					Object cell = ContentPopup.this.view.getValueAt(row,i);
					value = i==0 ? cell : value + "\t" + cell;
				}
			}
			else if(bAllRows)
			{
				for(int i=0; i<ContentPopup.this.view.getFlatRowCount(); i++)
				{
					Object cell = ContentPopup.this.view.getFlatValueAt(i,col);
					value = i==0 ? cell : value + "\n" + cell;
				}
			}
			
			if(value == null) return;
			
			Clipboard cb = ContentPopup.this.getToolkit().getSystemClipboard();
			StringSelection contents = new StringSelection(value.toString());
			cb.setContents(contents, Application.defaultClipboardOwner);
		}
	}
	
	private class ActionPaste extends AbstractActionSet
	{
		ActionPaste()
		{
			super("paste");
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}
		
		Object getValue()
		{
			try
			{
				Clipboard cb = ContentPopup.this.getToolkit().getSystemClipboard();
				return cb.getContents(null).getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException ufe)
			{
				Application.println(ufe,false);
			}
			catch (IOException ioe)
			{
				Application.println(ioe,false);
			}
			
			return null;
		}
	}
		
	private class ActionClone extends AbstractAction
	{
		ActionClone()
		{
			this.putValue(NAME,"clone record");
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}

		public void actionPerformed(ActionEvent ae)
		{
			int row = ContentPopup.this.view.getRow();
			ContentPopup.this.view.insertRow(++row);
			
			for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
			{
				Object source = ContentPopup.this.view.getValueAt(row-1,i);
				Object clone = source == null ? null : new String(source.toString());
				
				ContentPopup.this.view.setValueAt(clone,row,i);
			}
		}
	}
	
	private class ActionTrim extends AbstractAction
	{
		ActionTrim()
		{
			this.putValue(NAME,"trim trailing spaces");
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}

		public void actionPerformed(ActionEvent ae)
		{
			int col = ContentPopup.this.view.getColumn();
			int row = ContentPopup.this.view.getRow();
				
			if(bAllCols)
			{
				for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
				{
					Object cell = ContentPopup.this.view.getValueAt(row,i);
					cell = cell == null ? null : cell.toString().trim();
					
					ContentPopup.this.view.setValueAt(cell,row,i);
				}
			}
			else if(bAllRows)
			{
				for(int i=0; i<ContentPopup.this.view.getFlatRowCount(); i++)
				{
					Object cell = ContentPopup.this.view.getFlatValueAt(i,col);
					cell = cell == null ? null : cell.toString().trim();
					
					ContentPopup.this.view.setFlatValueAt(cell,i,col);
				}
			}
			else
			{
				Object cell = ContentPopup.this.view.getCellValue();
				cell = cell == null ? null : cell.toString().trim();
					
				ContentPopup.this.view.setCellValue(cell);
			}
			
			if(bAllCols || bAllRows)
			{
				ContentPopup.this.view.onTableChanged(true);
				ContentPopup.this.view.setSelectedCell(row,col);
			}
		}
	}
	
	private abstract class AbstractActionSet extends AbstractAction
	{
		AbstractActionSet(String name)
		{
			this.putValue(NAME,name);
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}
		
		abstract Object getValue();
		
		public void actionPerformed(ActionEvent ae)
		{
			int col = ContentPopup.this.view.getColumn();
			int row = ContentPopup.this.view.getRow();
				
			if(bAllCols)
			{
				for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
				{
					ContentPopup.this.view.setValueAt(this.getValue(),row,i);
				}
			}
			else if(bAllRows)
			{
				for(int i=0; i<ContentPopup.this.view.getFlatRowCount(); i++)
				{
					ContentPopup.this.view.setFlatValueAt(this.getValue(),i,col);
				}
			}
			else
			{
				ContentPopup.this.view.setCellValue(this.getValue());
			}
			
			if(bAllCols || bAllRows)
			{
				ContentPopup.this.view.onTableChanged(true);
				ContentPopup.this.view.setSelectedCell(row,col);
			}
		}		
	}
	
	private class ActionSetNull extends AbstractActionSet
	{
		ActionSetNull(){super("set <null>");}
		Object getValue(){return null;}
	}
	
	private class ActionSetToday extends AbstractActionSet
	{
		ActionSetToday(){super("set <today>");}
		Object getValue(){return new Date(System.currentTimeMillis());}
	}
	
	private class ActionResetOldValue extends AbstractAction
	{
		ActionResetOldValue()
		{
			this.putValue(NAME,"reset <old-value>");
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}

		public void actionPerformed(ActionEvent ae)
		{
			int col = ContentPopup.this.view.getColumn();
			int row = ContentPopup.this.view.getRow();
				
			if(bAllCols)
			{
				for(int i=0; i<ContentPopup.this.view.getColumnCount(); i++)
				{
					ContentPopup.this.view.resetFlatValueAt(row,i);
				}
			}
			else if(bAllRows)
			{
				for(int i=0; i<ContentPopup.this.view.getFlatRowCount(); i++)
				{
					ContentPopup.this.view.resetFlatValueAt(i,col);
				}
			}
			else
			{
				ContentPopup.this.view.resetCellValue();
			}
			
			if(bAllCols || bAllRows)
			{
				ContentPopup.this.view.onTableChanged(true);
				ContentPopup.this.view.setSelectedCell(row,col);
			}
		}
	}
	
	private abstract class AbstractActionSort extends AbstractAction
	{
		AbstractActionSort(String name)
		{
			this.putValue(NAME,name);
		}
		
		abstract short getType();
		
		public void actionPerformed(ActionEvent ae)
		{
			ContentPopup.this.view.getControl().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			ContentPopup.this.view.sort(ContentPopup.this.view.getColumn(),this.getType());
			ContentPopup.this.view.onTableChanged(true);
					
			ContentPopup.this.view.getControl().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	private class ActionSortAsc extends AbstractActionSort
	{
		ActionSortAsc(){super("sort ascending");}
		short getType(){return QueryTokens.Sort.ASCENDING;}
	}
	
	private class ActionSortDesc extends AbstractActionSort
	{
		ActionSortDesc(){super("sort descending");}
		short getType(){return QueryTokens.Sort.DESCENDING;}
	}
	
	private class ActionJump extends AbstractAction
	{
		ActionJump()
		{
			this.putValue(NAME,"jump...");
//			setEnabled(!ContentPopup.this.view.getControl().isReadOnly());
		}

		public void actionPerformed(ActionEvent ae)
		{
			JumpManager.perform(ContentPopup.this.view.getControl());
		}
	}	
}