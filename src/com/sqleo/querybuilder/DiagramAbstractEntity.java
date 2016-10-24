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

package com.sqleo.querybuilder;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.sqleo.common.util.I18n;
import com.sqleo.querybuilder.syntax.QueryTokens;


public abstract class DiagramAbstractEntity extends JInternalFrame
{
	BrowserItems.AbstractQueryTreeItem queryItem;
	QueryBuilder builder;
	
	private JMenu header;
	private JPanel fields;
	
	private Vector filterdFields = new Vector();

	DiagramAbstractEntity(QueryBuilder builder)
	{
		super("DiagramAbstractEntity",false,true);
		this.queryItem = builder.browser.getQueryItem();
		this.builder = builder;
		
		setLayer(JDesktopPane.PALETTE_LAYER);
		putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
		
		getContentPane().add(fields = new JPanel(new GridLayout(0,1,0,0)));		
		
		this.setJMenuBar(new JMenuBar());
		this.getJMenuBar().add(header = new JMenu());
		
		header.add(new MenuItemSortByName());
		header.add(new MenuItemPack());
		header.addSeparator();
		header.add(new ActionSelectAll());
		header.add(new ActionDeselectAll());
	}
	
	abstract void onCreate();
	abstract void onDestroy();
	
	public abstract QueryTokens.Table getQueryToken();
	
	JMenu getHeaderMenu()
	{
		return header;
	}
	
	public void setFontColorAndToolTip(final Color color,final String toolTip){
		header.setForeground(color);
		header.setToolTipText(toolTip);
	} 
	
	void addField(DiagramField field)
	{
		// fix #78 do not autoalias fields in subqueries	
		// if(QueryBuilder.autoAlias || queryItem instanceof BrowserItems.DiagramQueryTreeItem)
		if(QueryBuilder.autoAlias )
		{
			String alias = this.getQueryToken().getReference() + "." + field.querytoken.getName();
			if (alias.length() > QueryBuilder.maxColumnNameLength && QueryBuilder.maxColumnNameLength > 0)
				alias = alias.substring(0, QueryBuilder.maxColumnNameLength);
			field.querytoken.setAlias(alias);
		}
		fields.add(field);
	}
	
	void removeField(DiagramField field)
	{
		if(fields!=null)
			fields.remove(field);
	}
	
	private int findField(String label, boolean unpack)
	{
		if(unpack)
		{
			for(int i=0; i<filterdFields.size(); i++)
			{
				DiagramField field = (DiagramField)filterdFields.get(i);
				if(field.getLabel().equalsIgnoreCase(label))
				{
					fields.add(field);
					filterdFields.remove(field);
					
					doSort();
					pack();
					break;
				}
			}
		}
		
		for(int i=0; i<fields.getComponentCount(); i++)
		{
			DiagramField field = (DiagramField)fields.getComponent(i);
			if(field.getLabel().equalsIgnoreCase(label)) return i;
			if(field.getQueryToken().toString().equalsIgnoreCase(label)) return i;
			//for where tokens without alias (select t.col as x where t.col = 1), here t.col === x 
			if(field.getQueryToken().toStringWithoutAlias().equalsIgnoreCase(label)) return i;
		}
		
		return -1;		
	}
	
	public void setFieldsWithAlias(String oldAlias,String newAlias)
	{
		for(int i=0; i<fields.getComponentCount(); i++)
		{
			DiagramField field = (DiagramField)fields.getComponent(i);
			if(field.getQueryToken() instanceof QueryTokens.Column){
				final QueryTokens.Column column = (QueryTokens.Column) field.getQueryToken();
				if(oldAlias.equals(column.getTable().getAlias())){
					column.getTable().setAlias(newAlias);
				}
			}
		}
		
	}

    /**
     * Returns a DiagramField for the given name or null is no matching field is found...
     */
	public DiagramField getField(String label)
	{
		return getField(label,false);
	}
	
	public JPanel getFields()
	{
	   return fields;
	}
	
	// argument : unpack = true -> remove field from filter
	DiagramField getField(String label, boolean unpack)
	{
		int index = findField(label,unpack);
		return index!=-1?(DiagramField)fields.getComponent(index):null;
	}
	
	void setDragAndDropEnabled(boolean b)
	{
		for(int i=0; i<fields.getComponentCount(); i++)
		{
			DiagramField field = (DiagramField)fields.getComponent(i);
			field.setDragAndDropEnabled(b);
		}		
	}	
	
	void onSelectionChanged(DiagramField field)
	{
		if(field.isSelected())
			builder.browser.addSelectList(field.querytoken);
		else
			builder.browser.removeSelectList(field.querytoken);
		
		doPack();
		
		if(queryItem instanceof BrowserItems.DiagramQueryTreeItem)
		{
			DiagramQuery entityUp = ((BrowserItems.DiagramQueryTreeItem)queryItem).getDiagramObject();
			if(null == entityUp)
				return;
			if(field.isSelected())
			{
				// fix #78 do not autoalias fields in subqueries
				// entityUp.addField(field.querytoken.getAlias());
				final DiagramField existinField = entityUp.getField(field.querytoken.getAlias()!=null?field.querytoken.getAlias():field.querytoken.getName());
				if(null == existinField) {
					// dont add again fields as we add them while reversing subquery
					if(field.querytoken.getAlias()==null)
						entityUp.addField(field.querytoken.getName());
					else
						entityUp.addField(field.querytoken.getAlias());
				}

			}
			else
			{
				if(field.querytoken.getAlias()==null)
					entityUp.removeField(field.querytoken.getName());
				else
					entityUp.removeField(field.querytoken.getAlias());

			}
			entityUp.pack();
		}
	}
	
	void fireDeselectAll()
	{
		((JMenuItem)getHeaderMenu().getMenuComponent(4)).getAction().actionPerformed(null);
	}
	
	void setColumnSelections(boolean selected)
	{
		this.setPack(false);
			
		for(int i=0; i<fields.getComponentCount(); i++)
			((DiagramField)fields.getComponent(i)).setSelected(selected);		
	}
	
	protected Color getDefaultBackground()
	{
		return ViewDiagram.BGCOLOR_DEFAULT;
	}

	void doFlush()
	{
		for(int i=0; i<fields.getComponentCount(); i++)
		{
			DiagramField field = (DiagramField)fields.getComponent(i);
			field.setBackground(field.isJoined() ? ViewDiagram.BGCOLOR_JOINED : ViewDiagram.BGCOLOR_DEFAULT);
		}
	}

	void doSort()
	{
		JCheckBoxMenuItem mItem = (JCheckBoxMenuItem)header.getMenuComponent(0);
		if(mItem.isSelected())
		{
			for(int i=0; i<fields.getComponentCount()-1; i++)
			{
				String master = ((DiagramField)fields.getComponent(i)).getName();
				for(int j=i+1; j<fields.getComponentCount(); j++)
				{
					String slave = ((DiagramField)fields.getComponent(j)).getName();
					if(master.compareTo(slave) > 0)
					{
						DiagramField field = (DiagramField)fields.getComponent(j);
						fields.remove(field);
						fields.add(field,i);
							
						master = slave;
					}
				}
			}
		}
		else
		{
			for(int i=0; i<fields.getComponentCount()-1; i++)
			{
				int master = ((DiagramField)fields.getComponent(i)).position;
				for(int j=i+1; j<fields.getComponentCount(); j++)
				{
					int slave = ((DiagramField)fields.getComponent(j)).position;
					if(master > slave)
					{
						DiagramField field = (DiagramField)fields.getComponent(j);
						fields.remove(field);
						fields.add(field,i);
							
						master = slave;
					}
				}
			}
		}
		
		builder.diagram.repaint();
		builder.diagram.validate();
		builder.diagram.doResize();
	}
	
	void setSort(boolean b)
	{
		JCheckBoxMenuItem mItem = (JCheckBoxMenuItem)header.getMenuComponent(0);
		mItem.setSelected(b);
		
		doSort();
	}

	void doPack()
	{
		boolean changed = false;		
		
		if(this.isPack())
		{
			for(int i=0; i<fields.getComponentCount(); i++)
			{
				DiagramField field = (DiagramField)fields.getComponent(i);
				if(!field.isSelected() && !field.isJoined() &&!field.isInWhereClause())
				{
					filterdFields.add(field);
					fields.remove(field);
					i--;
						
					changed = true;
				}
			}
		}
		else
		{
			while(filterdFields.size()>0)
			{
				fields.add((DiagramField)filterdFields.get(0));
				filterdFields.remove(0);
					
				changed = true;
			}
			doSort();
		}
			
		if(changed)
		{
			pack();
			builder.diagram.doResize();
		}
	}
	
	boolean isPack()
	{
		JCheckBoxMenuItem mItem = (JCheckBoxMenuItem)header.getMenuComponent(1);
		return mItem.isSelected();
	}
	
	void setPack(boolean b)
	{
		JCheckBoxMenuItem mItem = (JCheckBoxMenuItem)header.getMenuComponent(1);
		mItem.setSelected(b);
		
		doPack();
	}
	
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);
		header.setEnabled(b);
	}

//	/////////////////////////////////////////////////////////////////////////////
//	Menu Actions
//	/////////////////////////////////////////////////////////////////////////////
	private class MenuItemSortByName extends JCheckBoxMenuItem implements ActionListener
	{
		private MenuItemSortByName()
		{
			super(I18n.getString("querybuilder.menu.sortByName","sort by name"));
			addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			DiagramAbstractEntity.this.doSort();
		}
	}

	private class MenuItemPack extends JCheckBoxMenuItem implements ActionListener
	{
		private MenuItemPack()
		{
			super(I18n.getString("querybuilder.menu.pack","pack"));
			addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			DiagramAbstractEntity.this.doPack();
		}
	}
	
	private class ActionSelectAll extends AbstractAction
	{
		private ActionSelectAll()
		{
			super(I18n.getString("querybuilder.menu.selectAll","select all"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			DiagramAbstractEntity.this.setColumnSelections(true);
		}
	}
	
	private class ActionDeselectAll extends AbstractAction
	{
		private ActionDeselectAll()
		{
			super(I18n.getString("querybuilder.menu.deselectAll","deselect all"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			DiagramAbstractEntity.this.setColumnSelections(false);
		}
	}	
}
