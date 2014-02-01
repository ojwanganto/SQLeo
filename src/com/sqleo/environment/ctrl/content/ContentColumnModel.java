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
	
	public ContentColumnModel(Vector<Integer> columnwidths){
		super();
		this.columnwidths = columnwidths;
	}
	
	@Override
	public void addColumn(TableColumn column) {
		super.addColumn(column);
		if(!columnwidths.isEmpty()){
			column.setPreferredWidth(columnwidths.elementAt(column.getModelIndex()));
		}
	}

}
