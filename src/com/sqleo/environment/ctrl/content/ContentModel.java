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

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.content.ContentChanges.Handler;
import com.sqleo.environment.mdi.DialogPreferences;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class ContentModel implements TableModel
{
	public static int MAX_BLOCK_RECORDS = Preferences.getInteger(DialogPreferences.CONTENT_MAX_ROWS_FETCH_SIZE_KEY,100);
	
	private Vector columns	= new Vector();
	private Vector rows		= new Vector();
	
	private Hashtable content = new Hashtable();
	private ContentChanges changes = new ContentChanges();
	
	private long ridcounter = 0;
	private int block = 1;
	private boolean readOnly = false; 
	
	public ContentModel(boolean readOnly){
		MAX_BLOCK_RECORDS = Preferences.getInteger(DialogPreferences.CONTENT_MAX_ROWS_FETCH_SIZE_KEY,100);
		this.readOnly = readOnly; 
	}
	public ContentModel(){
		MAX_BLOCK_RECORDS = Preferences.getInteger(DialogPreferences.CONTENT_MAX_ROWS_FETCH_SIZE_KEY,100);
		this.readOnly = false; 
	}
	
	public ContentChanges getChanges()
	{
		return changes;
	}
	
	public int getBlockCount()
	{
		return (this.getFlatRowCount()/MAX_BLOCK_RECORDS) + (this.getFlatRowCount()%MAX_BLOCK_RECORDS == 0 ? 0:1);
	}
	
	public int getBlock()
	{
		return block;
	}
    
	public void setBlock(int idx)
	{
		block = idx;
	}

	public void addColumn(String text,int type)
	{
		columns.add(new Object[]{text,new Integer(type)});
	}
	
	public int getColumnCount()
	{
		return columns.size();
	}

	public Class getColumnClass(int idx)
	{
		return Object.class;
	}

	public String getColumnName(int idx)
	{
		return ((Object[])columns.elementAt(idx))[0].toString();
	}

	public int getColumnIndex(String name)
	{
		for(int idx=0; idx<columns.size(); idx++)
		{
			if(this.getColumnName(idx).equals(name)) return idx;
		}
		
		return -1;
	}

	public int getColumnType(int idx)
	{
		return ((Integer)((Object[])columns.elementAt(idx))[1]).intValue();
	}
	
	public void addRow(Object[] rowdata)
	{
		addRow(rowdata,false);
	}
	
	public void addRow(Object[] rowdata, boolean newrow)
	{
		Long rid = new Long(ridcounter++);
		
		rows.add(rid);
		content.put(rid,rowdata);
		
		if(newrow) changes.setInserted(rid);
	}
	
	public void deleteRow(int row)
	{
		Long rid = (Long)rows.elementAt(this.toFlatRow(row));
		
		final Handler handler = getHandlerAt(row);
		if(handler!=null && handler.type.equals(ContentChanges.INSERT)){
			rows.removeElement(rid);
		}
		
		changes.setDeleted(rid);
	}
	
	public void insertRow(int row)
	{
		Object[] rowdata = new Object[this.getColumnCount()];
		Long rid = new Long(ridcounter++);
		
		rows.insertElementAt(rid,this.toFlatRow(row));
		content.put(rid,rowdata);
		
		changes.setInserted(rid);
	}

	public boolean isCellEditable(int row, int col)
	{
		return !readOnly;
	}
	public boolean isReadOnly(){
		return readOnly;
	}

	public int getRowCount()
	{
		return rows.size();
	}
	
	public Object getValueAt(int row, int col)
	{
		return getFlatValueAt(this.toFlatRow(row),col);
	}
	
	public boolean isCellChanged(int row, int col)
	{
		return isFlatCellChanged(this.toFlatRow(row),col);
	}
	
	public void resetValueAt(int row, int col)
	{
		resetFlatValueAt(this.toFlatRow(row),col);
	}	
	
	public void setValueAt(Object aValue, int row, int col)
	{
		setFlatValueAt(aValue,this.toFlatRow(row),col);
	}

	public int getFlatRowCount()
	{
		return rows.size();
	}
	
	public Object getFlatValueAt(int row, int col)
	{
		Long rid = (Long)rows.elementAt(row);
		Object[] rowdata = getValues(rid);
		
		return rowdata[col] instanceof Object[] ? ((Object[])rowdata[col])[0]: rowdata[col];		
	}
	
	public Object[] getFlatValues(int row){
		Object[] rowdata = getValues(row);
		for(int i=0;i<rowdata.length;i++){
			if( rowdata[i] instanceof Object[] ){
				rowdata[i] =((Object[])rowdata[i])[0];
			} 
		}
		return rowdata;
	}
	
	public boolean isFlatCellChanged(int row, int col)
	{
		Long rid = (Long)rows.elementAt(row);
		return changes.exists(ContentChanges.INSERT,rid) || changes.exists(ContentChanges.DELETE,rid) ||(getValues(rid)[col] instanceof Object[]);
	}
	
	public Handler getHandlerAt(int row){
		Long rid = (Long)rows.elementAt(row);
		return changes.getHandlerAt(rid);
	}
	
	private boolean isFlatRowChanged(int row)
	{
		for(int i=0; i<columns.size(); i++)
			if(isFlatCellChanged(row,i)) return true;
		
		return false;
	}	
	
	public void resetFlatValueAt(int row, int col)
	{
		Long rid = (Long)rows.elementAt(row);
		Object[] rowdata = getValues(rid);
		
		if(rowdata[col] instanceof Object[])
			setFlatValueAt(((Object[])rowdata[col])[1], row, col);		
	}
	
	public void setFlatValueAt(Object aValue, int row, int col)
	{
		Long rid = (Long)rows.elementAt(row);

		Object[] rowdata = getValues(rid);
		if(changes.exists(ContentChanges.INSERT,rid))
		{
			rowdata[col] = aValue;
		}
		else
		{
			Object[] cell = {aValue,null};
			
			if(rowdata[col] instanceof Object[])
				cell[1] = ((Object[])rowdata[col])[1];
			else
				cell[1] = rowdata[col];
			
			if(cell[0] == null && cell[1] == null)
			{
				rowdata[col] = null;
				cell = null;
			}
			else if(cell[0] != null && cell[1] != null)
			{
				if(cell[0].toString().equals(cell[1].toString()))
				{
					rowdata[col] = aValue;
					cell = null;
				}
				else
					cell[0] = aValue;
			}
			else
				cell[0] = aValue;
	
			if(cell!=null)
			{
				rowdata[col] = cell;
				changes.setUpdated(rid);
			}
			else if(!isFlatRowChanged(row))
			{
				changes.aborted(rid);
			}
		}
	}

	Object[] getValues(int row)
	{
		Long rid = (Long)rows.elementAt(row);
		return getValues(rid);
	}

	Object[] getValues(Long rid)
	{
		return (Object[])content.get(rid);
	}

	int toFlatRow(int row)
	{
		int gap = (block-1)*MAX_BLOCK_RECORDS;
		
		return gap < 0 ? row : row+gap;
	}
	
	private int compare(int col, int row1, int row2, short type)
	{
		Object value1,value2;
		
		Long rid1 = (Long)rows.elementAt(row1);
		Long rid2 = (Long)rows.elementAt(row2);
		
		Object[] rowdata1 = getValues(rid1);
		Object[] rowdata2 = getValues(rid2);
		
		if(rowdata1[col] instanceof Object[])
			value1 = ((Object[])rowdata1[col])[0];
		else
			value1 = rowdata1[col];
		
		if(rowdata2[col] instanceof Object[])
			value2 = ((Object[])rowdata2[col])[0];
		else
			value2 = rowdata2[col];
		
		int cmp = 0;
		if(value1!=null && value2!=null)
		{
			if(TaskRetrieve.isNumberType(getColumnType(col)))
			{
				if(Float.valueOf(value1.toString()).floatValue() > Float.valueOf(value2.toString()).floatValue())
					cmp = 1;
				else if(Float.valueOf(value1.toString()).floatValue() < Float.valueOf(value2.toString()).floatValue())
					cmp = -1;
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
		
		return type == QueryTokens.Sort.ASCENDING ? cmp : cmp*(-1);
	}
	
	private void swap(int i, int j)
	{
		Long rid = (Long)rows.elementAt(i);
		rows.setElementAt(rows.elementAt(j),i);
		rows.setElementAt(rid,j);
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
	
	public void sort(int col, short type)
	{
		if(getFlatRowCount() < 2) return;
		
		int max = 0;
		for(int i=1; i < getFlatRowCount(); i++)
			if(compare(col,max,i,type) < 0) max = i;
		
		swap(getFlatRowCount()-1,max);
		sort(col,0,getFlatRowCount()-2,type);
	}
	
	public void addTableModelListener(TableModelListener l)
	{
	}	
	public void removeTableModelListener(TableModelListener l)
	{
	}
}