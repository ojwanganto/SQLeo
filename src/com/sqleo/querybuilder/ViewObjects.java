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

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.common.util.I18n;
import com.sqleo.querybuilder.beans.Entity;
import com.sqleo.querybuilder.beans.Tag;
import com.sqleo.querybuilder.dnd.EntityTransferHandler;
import com.sqleo.querybuilder.syntax.QueryTokens;



public class ViewObjects extends BorderLayoutPanel implements ItemListener
{
	public final static String ALL_TABLE_TYPES = "All";
	private QueryBuilder builder;

	private JList jListObjects;
	public JComboBox jComboBoxSchemas;
	private JComboBox jComboBoxTypes;

	ViewObjects(QueryBuilder builder)
	{
		this.builder = builder;
		initComponents();
	}

	private void initComponents()
	{
		jListObjects = new JList();
		jListObjects.setDragEnabled(true);

		jListObjects.setTransferHandler(new EntityTransferHandler());

		jListObjects.setCellRenderer(new ObjectsListCellRenderer());
		jListObjects.addMouseListener(new ClickHandler());

		JPanel pnlNorth = new JPanel(new GridLayout(0, 2));
		pnlNorth.add(jComboBoxSchemas = new JComboBox());
		jComboBoxSchemas.setToolTipText(I18n.getString("querybuilder.tooltip.schemaFilter", "schema filter"));
		pnlNorth.add(jComboBoxTypes = new JComboBox());
		jComboBoxTypes.setToolTipText(I18n.getString("querybuilder.tooltip.typeFilter", "type filter"));

		setComponentNorth(pnlNorth);
		setComponentCenter(new JScrollPane(jListObjects));
	}

	void onConnectionChanged() throws SQLException
	{
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

		jListObjects.setListData(new Vector());

		jComboBoxSchemas.removeItemListener(this);
		jComboBoxTypes.removeItemListener(this);

		jComboBoxSchemas.removeAllItems();
		jComboBoxTypes.removeAllItems();

		jComboBoxSchemas.setEnabled(false);
		jComboBoxTypes.setEnabled(false);

		if (builder.getConnection() != null)
		{
			DatabaseMetaData dbmd = builder.getConnection().getMetaData();
			ResultSet rsTypes = dbmd.getTableTypes();
			while (rsTypes.next())
			{
				String type = rsTypes.getString(1).trim();
				boolean added = false;

				for (int i = 0; !added && i < jComboBoxTypes.getItemCount(); i++)
				{
					Tag t = (Tag)jComboBoxTypes.getItemAt(i);
					added = type.equals(t.getValue().toString());
				}
				if (!added)
				{
					jComboBoxTypes.addItem(new Tag(type, I18n.getString("querybuilder.objetctype." + type, "" + type)));
				}
			}
			rsTypes.close();
			jComboBoxTypes.addItem(new Tag(ALL_TABLE_TYPES, I18n.getString("querybuilder.objetctype.all", "All object types")));

			if (ViewObjects.jdbcUseSchema(dbmd))
			{
				ResultSet rsSchemas = dbmd.getSchemas();
				while (rsSchemas.next())
					jComboBoxSchemas.addItem(rsSchemas.getString(1).trim());
				rsSchemas.close();
			}

			jComboBoxSchemas.setSelectedItem(null);
			jComboBoxTypes.setSelectedItem(null);
		}

		jComboBoxSchemas.setEnabled(jComboBoxSchemas.getItemCount() > 0);
		jComboBoxTypes.setEnabled(jComboBoxTypes.getItemCount() > 0);

		jComboBoxSchemas.addItemListener(this);
		jComboBoxTypes.addItemListener(this);

//		if (jComboBoxSchemas.getItemCount() > 0 && QueryBuilder.loadObjectsAtOnce)
//			jComboBoxSchemas.setSelectedIndex(0);

		if (jComboBoxTypes.getItemCount() > 0 && QueryBuilder.loadObjectsAtOnce)
		{
			Tag t = new Tag("TABLE", I18n.getString("querybuilder.objetctype.TABLE", "TABLE"));
			for(int i=0; i<jComboBoxTypes.getItemCount(); i++)
			{
				if(jComboBoxTypes.getItemAt(i).toString().equals(t.toString()))
				{
					jComboBoxTypes.setSelectedIndex(i);
					break;
				}
			}
		}
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void onModelChanged()
	{
		String schema = builder.getQueryModel().getSchema();
		jComboBoxSchemas.setEnabled(jComboBoxSchemas.isEnabled() && schema == null);
		jComboBoxSchemas.setSelectedItem(schema);
	}

	public void itemStateChanged(ItemEvent ie)
	{
		Object schema = jComboBoxSchemas.getSelectedItem();
		Tag t = (Tag)jComboBoxTypes.getSelectedItem();
		
		Object type = null;
		if (t != null)
			type = t.getValue();

		if (type != null && (schema != null || !jComboBoxSchemas.isEnabled()))
		{
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Vector vObjects = new Vector();

			String[] tableType;
			if (type.toString().equals(ALL_TABLE_TYPES))
				tableType = null;
			else
				tableType = new String[] { type.toString()};

			try
			{
				DatabaseMetaData dbmd = builder.getConnection().getMetaData();
				String catalog = schema == null ? null : dbmd.getConnection().getCatalog();

				ResultSet rsTables = dbmd.getTables(catalog, (schema == null ? null : schema.toString()), "%", tableType);
				if (rsTables != null)
				{
					/*
					 * 06/02/2007 (Nicky)
					 * if schema is as model level don't display into diagram-entity 
					 */
					if(builder.getQueryModel().getSchema()!=null) schema = null;
					
					while (rsTables.next())
					{
						Entity entity = new Entity((schema == null ? null : schema.toString()), rsTables.getString(3).trim());
						vObjects.addElement(entity);
					}
					rsTables.close();
				}
			}
			catch (SQLException sqle)
			{
				sqle.printStackTrace();
			}
			finally
			{
				jListObjects.setListData(vObjects);
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public static boolean jdbcUseSchema(DatabaseMetaData dbmd) throws SQLException
	{
// #293 Query builder: support adding csvjdbc tables without schema 
//		String term = dbmd.getSchemaTerm();
//		return term != null && term.length() > 0;
		Boolean Schem = dbmd.supportsSchemasInTableDefinitions();
		return Schem ;
	}

	private class ClickHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				Entity selectedItem = (Entity)jListObjects.getSelectedValue();
				QueryTokens.Table token = new QueryTokens.Table(selectedItem.getSchema(), selectedItem.getEntityName());
				DiagramLoader.run(DiagramLoader.DEFAULT, builder, token, true);
			}
		}
	}
}