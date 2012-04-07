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

package com.sqleo.environment.ctrl.editor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class MaskBrowser extends JPanel implements ItemListener
{
	private JList objects;
	private JComboBox schemas;
	private JComboBox types;

	private ConnectionHandler ch;

	MaskBrowser()
	{
		super(new BorderLayout());
		initComponents();
	}

	private void initComponents()
	{
		JPanel pnlNorth = new JPanel(new GridLayout(0,2));
		pnlNorth.add(schemas = new JComboBox());
		pnlNorth.add(types = new JComboBox());
		
		add(pnlNorth,BorderLayout.NORTH);
		add(new JScrollPane(objects = new JList()),BorderLayout.CENTER);
	}
	
	void setConnection(ConnectionHandler ch) throws SQLException
	{
		if(this.ch!=null) return;
		
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		this.ch = ch;
		
		objects.setListData(new Vector());
		
		schemas.removeItemListener(this);
		types.removeItemListener(this);
		
		schemas.removeAllItems();
		types.removeAllItems();
		
		schemas.setEnabled(false);
		types.setEnabled(false);
		
		if(ch.get()!=null)
		{
			DatabaseMetaData dbmd = ch.get().getMetaData();
			ResultSet rsTypes = dbmd.getTableTypes();
			while(rsTypes.next())
			{
				String type = rsTypes.getString(1).trim();
				boolean added = false;
				
				for(int i=0; !added && i<types.getItemCount(); i++)
					added = type.equals(types.getItemAt(i).toString());
				
				if(!added)
					types.addItem(type);
			}
			rsTypes.close();
			types.addItem("ALL");
			
			if( ((Boolean)ch.getObject("$supportsSchema")).booleanValue() )
			{
				ResultSet rsSchemas = dbmd.getSchemas();
				while(rsSchemas.next())
					schemas.addItem(rsSchemas.getString(1).trim());
				rsSchemas.close();
			}
			
			schemas.setSelectedItem(null);
			types.setSelectedItem(null);
		}

		schemas.setEnabled(schemas.getItemCount()>0);
		types.setEnabled(types.getItemCount()>0);

		schemas.addItemListener(this);
		types.addItemListener(this);
		
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		Object schema = schemas.getSelectedItem();
		Object type = types.getSelectedItem();
		
		if(type!=null && (schema!=null || !schemas.isEnabled()))
		{
			this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			Vector vObjects = new Vector();
			
			String[] tableType;
			if(type.toString().equals("ALL"))
				tableType = null;
			else
				tableType = new String[]{type.toString()};
			
			try
			{
				DatabaseMetaData dbmd = ch.get().getMetaData();
				String catalog = schema == null ? null : dbmd.getConnection().getCatalog();
				
				ResultSet rsTables = dbmd.getTables(catalog,(schema == null ? null : schema.toString()),"%", tableType);
				if(rsTables!=null)
				{
					while(rsTables.next())
						vObjects.addElement(rsTables.getString(3).trim());
					rsTables.close();
				}
			}
			catch (SQLException sqle)
			{
				sqle.printStackTrace();
			}
			finally
			{
				objects.setListData(vObjects);
				this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	QueryTokens.Table getSelectedItem()
	{
		if(objects.getSelectedIndex() == -1) return null;
		
		Object schema = schemas.getSelectedItem();
		String table = objects.getSelectedValue().toString();
		
		return new QueryTokens.Table((schema == null ? null : schema.toString()),table);		
	}	
}
