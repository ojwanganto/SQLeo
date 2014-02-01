package com.sqleo.environment.ctrl.content;

import java.util.Vector;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ContentColumnModel extends DefaultTableColumnModel implements 
		TableColumnModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Vector<Integer> columnwidths;
	private int colCount;
	
	public ContentColumnModel(Vector<Integer> columnwidths){
		super();
		setColumnWidths(columnwidths);
	}
	
	public void setColumnWidths(Vector<Integer> columnwidths){
		this.columnwidths = columnwidths;
		this.colCount = columnwidths.size();
	}
	
	@Override
	public void addColumn(TableColumn column) {
		super.addColumn(column);
		if(this.colCount>0 && column.getModelIndex() < this.colCount){
			column.setPreferredWidth(this.columnwidths.elementAt(column.getModelIndex()));
		}
	}

}
