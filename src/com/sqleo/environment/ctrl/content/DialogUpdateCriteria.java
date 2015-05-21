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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.gui.CheckBoxCellRenderer;
import com.sqleo.environment.Application;
import com.sqleo.environment.Preferences;
import com.sqleo.environment.ctrl.ContentPane;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class DialogUpdateCriteria extends AbstractDialogConfirm implements ItemListener
{
	private ContentPane content;
	private JComboBox tables;
	private JTable columns;
	
	private int expressionIndices[];

	public DialogUpdateCriteria(ContentPane content)
	{
		super(Application.window,"Define update criteria",350,275);
		this.content = content;
		
		DefaultTableModel model = new DefaultTableModel(0,2)
		{
			public boolean isCellEditable(int row, int column)
			{
				return column == 0;
			}
		};

		JScrollPane scroll = new JScrollPane(columns = new JTable(model));
		scroll.getViewport().setBackground(UIManager.getDefaults().getColor("Table.background"));
		
		BorderLayoutPanel mask = new BorderLayoutPanel();
		mask.setComponentNorth(tables = new JComboBox());
		mask.setComponentCenter(scroll);
		
		TableColumn tableColumn = columns.getColumn(columns.getColumnName(0));
		tableColumn.setCellEditor(new CheckBoxCellRenderer());
		tableColumn.setCellRenderer(new CheckBoxCellRenderer());
		tableColumn.setPreferredWidth(15);
		tableColumn.setMaxWidth(15);
		tableColumn.setResizable(false);
		
		columns.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		columns.setIntercellSpacing(new Dimension(0,0));
		columns.setShowGrid(false);
		columns.setColumnSelectionAllowed(false);
		columns.setDefaultRenderer(Boolean.class, new CheckBoxCellRenderer());
		columns.getTableHeader().setPreferredSize(new Dimension(0,0));
		columns.getTableHeader().setVisible(false);
		
		final int oldRowHeight = columns.getRowHeight();
		final int newRowHeight = Preferences.getScaledRowHeight(oldRowHeight);
		if(newRowHeight != oldRowHeight){
			columns.setRowHeight(newRowHeight);
		}
		
		getContentPane().add(mask);		
	}
	
	protected boolean onConfirm()
	{
		if(tables.getSelectedItem() == null) return false;
		
		UpdateModel um = new UpdateModel();
		
		QueryTokens._TableReference[] r = content.getQueryModel().getQueryExpression().getQuerySpecification().getFromClause();
		for(int i=0; i<r.length; i++)
		{
			if(r[i] instanceof QueryTokens.Table)
			{
				if(((QueryTokens.Table)r[i]).getReference().equalsIgnoreCase(tables.getSelectedItem().toString()))
				{
					um.setTable((QueryTokens.Table)r[i]);
					break;
				}
			}
			else
			{
				if(((QueryTokens.Join)r[i]).getPrimary().getTable().getReference().equalsIgnoreCase(tables.getSelectedItem().toString()))
				{
					um.setTable(((QueryTokens.Join)r[i]).getPrimary().getTable());
					break;
				}

				if(((QueryTokens.Join)r[i]).getForeign().getTable().getReference().equalsIgnoreCase(tables.getSelectedItem().toString()))
				{
					um.setTable(((QueryTokens.Join)r[i]).getForeign().getTable());
					break;
				}
			}
		}
		
		Vector rowid = new Vector();
		for(int i=0; i<columns.getRowCount(); i++)
		{
			if(((Boolean)columns.getValueAt(i,0)).booleanValue())
			{
				QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
				QueryTokens.Column c = (QueryTokens.Column)e[expressionIndices[i]];
				rowid.addElement(c);
			}
		}
		
		if(rowid.size() == 0) return false;
		
		QueryTokens.Column[] c = new QueryTokens.Column[rowid.size()];
		um.setRowIdentifier((QueryTokens.Column[])rowid.toArray(c));
		
		content.setUpdateModel(um);
		return true;
	}

	protected void onOpen()
	{
		QueryTokens._TableReference[] r = content.getQueryModel().getQueryExpression().getQuerySpecification().getFromClause();
		for(int i=0; i<r.length; i++)
		{
			if(r[i] instanceof QueryTokens.Table)
			{
				((DefaultComboBoxModel)tables.getModel()).addElement(((QueryTokens.Table)r[i]).getReference());
			}
			else
			{
				((DefaultComboBoxModel)tables.getModel()).addElement(((QueryTokens.Join)r[i]).getPrimary().getTable().getReference());
				((DefaultComboBoxModel)tables.getModel()).addElement(((QueryTokens.Join)r[i]).getForeign().getTable().getReference());
			}
		}

		tables.setSelectedItem(null);
		tables.addItemListener(this);
		
		if(!content.isReadOnly() && content.getUpdateModel().getTable() != null)
		{
			String reference = content.getUpdateModel().getTable().getReference();
			tables.setSelectedItem(reference);
			
			for(int i=0; i<content.getUpdateModel().getRowIdentifierCount(); i++)
			{
				QueryTokens.Column cpk = content.getUpdateModel().getRowIdentifier(i);
				for(int j=0; j<columns.getRowCount(); j++)
				{
					QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
					QueryTokens.Column c = (QueryTokens.Column)e[expressionIndices[j]];
					
					if(cpk.getName().equalsIgnoreCase(c.getName()))
					{
						columns.setValueAt(new Boolean(true),j,0);
					}
				}				
			}
		}
	}	
	
	public void itemStateChanged(ItemEvent ie)
	{
		if(ie.getSource() instanceof JComboBox)
		{
			((DefaultTableModel)columns.getModel()).setRowCount(0);
			
			String reference = tables.getSelectedItem().toString();
			QueryTokens._Expression[] e = content.getQueryModel().getQueryExpression().getQuerySpecification().getSelectList();
			
			expressionIndices = new int[e.length];
			for(int i=0,j=0; i<e.length; i++)
			{
				if(e[i] instanceof QueryTokens.Column)
				{
					QueryTokens.Column c = (QueryTokens.Column)e[i];
					String headerLabel = c.isAliasSet() ? c.getAlias() : c.getName();
					if(tables.getItemCount() == 1 || c.getTable().getReference().equalsIgnoreCase(reference))
					{
						((DefaultTableModel)columns.getModel()).addRow(new Object[]{new Boolean(false),headerLabel});
						expressionIndices[j++] = i;
					}
				}
			}
		}
	}	
}