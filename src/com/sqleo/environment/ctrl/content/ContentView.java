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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Types;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.sqleo.common.gui.CustomLineBorder;
import com.sqleo.common.gui.HeaderCellRenderer;
import com.sqleo.common.util.Appearance;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class ContentView extends JPanel implements ListSelectionListener
{
	private JTable data;
	private JScrollBar jsb;
	private LineNumberView lines;
	
	private ContentModel model;
	private ContentPopup popup;
	private ContentPane control;
	private Vector<Integer> columnwidths;
    
	public ContentView(final ContentPane control)
	{
		super(new GridLayout(1,1));
	    this.control = control;
	    columnwidths = new Vector<Integer>();
	    
		data = new JTable();
		data.setModel(model = new ContentModel(getControl().getQueryModel()==null));
		data.setColumnModel(new ContentColumnModel(columnwidths));
		data.addMouseListener(popup = new ContentPopup(this));
		data.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent key)
			{
				if(key.getKeyCode() == KeyEvent.VK_DOWN || key.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
				{
					if((ContentView.this.data.getSelectedRow() == ContentView.this.data.getRowCount()-1) && !control.areAllRowsFetched())
					{
							int col = ContentView.this.data.getSelectedColumn();
							int row = ContentView.this.data.getRowCount()-1;
							ContentView.this.data.scrollRectToVisible(ContentView.this.data.getCellRect(row,col,true));
							ContentView.this.data.setRowSelectionInterval(row+1,row+1);
							ContentView.this.data.scrollRectToVisible(ContentView.this.data.getCellRect(row+1,col,true));
							key.consume();
					}
				}
			}
		});
		
		final int oldRowHeight = data.getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			data.setRowHeight(newRowHeight);
		}
		
		JScrollPane scroll = new JScrollPane(data);
		scroll.getViewport().setBackground(UIManager.getDefaults().getColor("Table.background"));
		add(scroll);
		jsb = scroll.getVerticalScrollBar();
		jsb.addAdjustmentListener(new ListenerScrollBar());

		
		data.setRowSelectionAllowed(false);
		data.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		data.setDefaultRenderer(Object.class,new InternalCellRenderer());
		data.getTableHeader().addMouseListener(popup);
		data.getTableHeader().setReorderingAllowed(false);
		
		lines = new LineNumberView();
		lines.addMouseListener(popup);
		lines.setSelectionModel(data.getSelectionModel());
		scroll.setRowHeaderView(lines);
		
		JLabel cUL = new JLabel("#",JLabel.CENTER);
		cUL.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		cUL.setFont(UIManager.getFont("TableHeader.font"));
		scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER,cUL);
		
		JLabel cLL = new JLabel();
		cLL.setBorder(new CustomLineBorder(true,false,false,false));
		scroll.setCorner(JScrollPane.LOWER_LEFT_CORNER,cLL);
		
		data.getColumnModel().getSelectionModel().addListSelectionListener(this);
		
			data.getActionMap().put("Copy", ((JMenuItem)popup.getSubElementsAt(1)).getAction());
			data.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_MASK),"Copy");
			
			data.getActionMap().put("Paste", ((JMenuItem)popup.getSubElementsAt(2)).getAction());
			data.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V,InputEvent.CTRL_MASK),"Paste");
					
			data.getActionMap().put("Set-null", ((JMenuItem)popup.getSubElementsAt(3)).getAction());
			data.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),"Set-null");

		
			MouseAdapter ma = new MouseAdapter()
			{
				public void mousePressed(MouseEvent me)
				{
					ContentView.this.data.setColumnSelectionAllowed(me.getSource() == ContentView.this.data.getTableHeader());
					ContentView.this.data.setRowSelectionAllowed(me.getSource() == ContentView.this.lines);
				}
			};
			
			data.getTableHeader().addMouseListener(ma);
			data.addMouseListener(ma);
			lines.addMouseListener(ma);
	}
	
	public boolean isReadOnly(){
		return model.isReadOnly();
	}

	ContentPane getControl()
	{
		return control;
	}	
	
	public void addRow(Object[] rowdata, boolean newrow)
	{
		model.addRow(rowdata,newrow);
		lines.setRowCount(this.getRowCount());
	}
	
	public void addColumn(String text)
	{
		addColumn(text,Types.CHAR);
	}
    
	public void addColumn(String text,int type)
	{
		model.addColumn(text,type);
	}
    
	public void deleteRow(int row)
	{
		model.deleteRow(row);		
		lines.setRowCount(this.getRowCount());
		
		onTableChanged(true);
	}
	
	public void insertRow(int row)
	{
		model.insertRow(row);		
		lines.setRowCount(this.getRowCount());
		
		onTableChanged(true);
	}
	
	public int getRow()
	{
		return data.getSelectedRow();
	}
	
	public int getColumn()
	{
		return data.getSelectedColumn();
	}
	
	private HeaderCellRenderer getHeaderRenderer(int idx)
	{
		TableColumn tc = data.getColumnModel().getColumn(idx);
		if(tc.getHeaderRenderer()!=null) return (HeaderCellRenderer)tc.getHeaderRenderer();
		
		HeaderCellRenderer hcr = new HeaderCellRenderer();
		tc.setHeaderRenderer(hcr);

		return hcr;
	}

	public void setToolTipText(int i,String text)
	{
		this.getHeaderRenderer(i).setToolTipText(text);
	}

	public int getBlockCount()
	{
		return model.getBlockCount();
	}
    
	public int getBlock()
	{
		return model.getBlock();
	}
    
	public void setBlock(int idx)
	{
		model.setBlock(idx);
		lines.setBlock(idx);
		lines.setRowCount(this.getRowCount());
		
		onTableChanged(true);
	}
	
	public ContentChanges getChanges()
	{
		return model.getChanges();
	}
	
	public int getColumnCount()
	{
		return model.getColumnCount();
	}
	
	public int getColumnIndex(String name)
	{
		return model.getColumnIndex(name);
	}
	
	public String getColumnName(int idx)
	{
		return model.getColumnName(idx);
	}	
    
	public int getColumnType(int idx)
	{
		return model.getColumnType(idx);
	}	
    
	public int getRowCount()
	{
		return model.getRowCount();
	}

	public Object getLineAt(int row)
	{
		return lines.getValueAt(row,0);
	}
	
	Object[] getValues(int row)
	{
		return model.getValues(row);
	}
	
	public Object[] getFlatValues(int row){
		return model.getFlatValues(row);
	}

	Object[] getValues(Long rid)
	{
		return model.getValues(rid);
	}
	
	public Object getValueAt(int row, int col)
	{
		return model.getValueAt(row, col);
	}

	public void setValueAt(Object aValue, int row, int col)
	{
		model.setValueAt(aValue,row,col);
	}
	
	public int getFlatRow()
	{
		return model.toFlatRow(getRow());
	}	

	public int getFlatRowCount()
	{
		return model.getFlatRowCount();
	}
	
	public Object getFlatValueAt(int row, int col)
	{
		return model.getFlatValueAt(row,col);
	}

	public void resetFlatValueAt(int row, int col)
	{
		model.resetFlatValueAt(row,col);
	}
	
	public void setFlatValueAt(Object aValue, int row, int col)
	{
		model.setFlatValueAt(aValue,row,col);
	}

	public void reset()
	{
		cacheColumnWidths();
		data.setModel(model = new ContentModel());
		lines.setRowCount(0);
		lines.setBlock(1);
	}
	
	public void cacheColumnWidths(){
		if(!getControl().isBusy()){
			columnwidths.clear();
			for(int i=0; i<data.getColumnModel().getColumnCount(); i++)
			{
				TableColumn tc = data.getColumnModel().getColumn(i);
				columnwidths.add(tc.getPreferredWidth());
			}
			setColumnWidths(columnwidths);
		}
	}
	
	public Vector<Integer> getColumnWidths(){
		return columnwidths;
	}
	
	public void setColumnWidths(Vector<Integer> columnwidths){
		this.columnwidths = columnwidths;
		ContentColumnModel columnModel = (ContentColumnModel) data.getColumnModel();
		columnModel.setColumnWidths(columnwidths);
	}
	
	
	public Object getCellValue()
	{
		return data.getValueAt(data.getSelectedRow(),data.getSelectedColumn());
	}
	
	public void resetCellValue()
	{
		model.resetValueAt(data.getSelectedRow(),data.getSelectedColumn());
		data.tableChanged(new TableModelEvent(data.getModel(),data.getSelectedRow()));
	}
	
	public void setCellValue(Object value)
	{
		data.setValueAt(value, data.getSelectedRow(),data.getSelectedColumn());
		data.tableChanged(new TableModelEvent(data.getModel(),data.getSelectedRow()));
	}
	
	public void setSelectedCell(int row, int col)
	{
		if(row==-1 || col==-1) return;
		
		data.setRowSelectionInterval(row,row);
		data.setColumnSelectionInterval(col,col);
		data.scrollRectToVisible(data.getCellRect(row,col,true));
	}

	private ContentFlag flag;
	public void mark(ContentFlag flag)
	{
		int row = this.getRow();
		int col = this.getColumn();
		
		if((this.flag = flag) != null)
		{
			row = flag.row;
			col = flag.col;
		}
		else
		{
			if(row!=-1)	data.removeRowSelectionInterval(row,row);
			if(col!=-1)	data.removeColumnSelectionInterval(col,col);
		}
		
		setSelectedCell(row,col);
	}
	
	public void sort(int col,short type)
	{
		control.doStop();
		QueryTokens.Sort token = new QueryTokens.Sort((QueryTokens._Expression)new QueryTokens.DefaultExpression(""+(col+1)),type);
		if(control.getQueryModel()!=null){
			control.getQueryModel().removeAllOrderByClauses();
			control.getQueryModel().addOrderByClause(token);
		}else {
			//when launched from syntax view of query builder, query model is null
			String query = control.getQuery();
			if(query!=null){
				String orderByKeyword = null;
				if(query.contains("ORDER BY")){
					orderByKeyword = "ORDER BY";
				}else if(query.contains("order by")){
					orderByKeyword = "order by";
				}
				if(orderByKeyword!=null){
					int lastOrderByindex = query.lastIndexOf(orderByKeyword);
					int lastclosingindex = query.lastIndexOf(')');
					String prefix;
					if(lastOrderByindex>lastclosingindex){
						// derived table 
						prefix = query.substring(0, lastOrderByindex-1);
					}else {
						prefix = query;
					}
					control.setQuery(prefix+ "\n"+orderByKeyword+" " + token.toString());
				}else {
					control.setQuery(query+ "\nORDER BY " + token.toString());
				}
				
			}
		}
		control.relaunchQuery();
	}
	
	public void onTableChanged(boolean onlyData)
	{
		data.tableChanged(onlyData ? new TableModelEvent(model) : null);
		lines.tableChanged(null);
	}

	public void valueChanged(ListSelectionEvent lse)
	{
		if(lse.getValueIsAdjusting()) return;
		
		for(int i=0; i<data.getColumnModel().getColumnCount(); i++)
		{
			this.getHeaderRenderer(i).setSelected(this.getColumn()==i);
		}
		data.getTableHeader().repaint();
	}

	private class InternalCellRenderer extends DefaultTableCellRenderer
	{
		private void setBackgroundColor(final Color color){
			final Color backColor = getControl().getBackgroundColor();
			super.setBackground(backColor!=null ? backColor : color);
		}
		
		public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int col)
		{
			super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
			setBackgroundColor(UIManager.getDefaults().getColor("Table.background"));
			super.setFont(Appearance.fontPLAIN);
			super.setOpaque(true);

			if(isSelected && !hasFocus)
				setBackgroundColor(data.getSelectionBackground());

			if(ContentView.this.flag != null)
			{
				if(ContentView.this.getBlock() == ContentView.this.flag.block
				&& col == ContentView.this.flag.col	&& row == ContentView.this.flag.row)
					setBackgroundColor(ContentView.this.flag.bgcolor);
			}
			
			if(value==null)
			{
				super.setText("<null>");
				if(ContentView.this.model.isCellChanged(row,col)){
					final ContentChanges.Handler handler = ContentView.this.model.getHandlerAt(row);
					if(handler.type.equals(ContentChanges.INSERT)){
						super.setForeground(Color.green);
					}else if(handler.type.equals(ContentChanges.DELETE)){
						super.setForeground(Color.red);
					}else if(handler.type.equals(ContentChanges.UPDATE)){
						super.setForeground(Color.blue);
					}
				}
				else
					super.setForeground(Color.lightGray);
			}
			else
			{
				if(ContentView.this.model.isCellChanged(row,col)){
					final ContentChanges.Handler handler = ContentView.this.model.getHandlerAt(row);
					if(handler.type.equals(ContentChanges.INSERT)){
						super.setForeground(Color.green);
					}else if(handler.type.equals(ContentChanges.DELETE)){
						super.setForeground(Color.red);
					}else if(handler.type.equals(ContentChanges.UPDATE)){
						super.setForeground(Color.blue);
					}
				}
				else{
					super.setForeground(UIManager.getDefaults().getColor("Table.foreground"));
				}
			}
			
			if(TaskRetrieve.isNumberType(ContentView.this.getColumnType(col)))
				super.setHorizontalAlignment(RIGHT);
			else
				super.setHorizontalAlignment(LEFT);
		
			return this;
		}
	}
	
	private class ListenerScrollBar implements AdjustmentListener
	{
		public void adjustmentValueChanged(AdjustmentEvent e)
		{
			if(e.getValueIsAdjusting() || getRowCount()==0){
				return;
			}
			if (jsb.getMaximum() != 0 && e.getValue()>0)
			{
				if ((jsb.getMaximum() - jsb.getVisibleAmount()) <= e.getValue())
				{
					 control.fetchNextRecords();
				}
			}
		}
	}

}