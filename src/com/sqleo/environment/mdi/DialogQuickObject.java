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

package com.sqleo.environment.mdi;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.sqleo.common.gui.AbstractDialogConfirm;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.common.util.I18n;


public class DialogQuickObject extends AbstractDialogConfirm implements ItemListener
{
	private boolean terminated = false;
	
	private JComboBox cbxConnections;
	private JComboBox cbxSchemas;
	private JTextField txtName;

	private DialogQuickObject(String title)
	{
		super(Application.window,title,350,230);
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		cbxConnections = new JComboBox(ConnectionAssistant.getHandlers().toArray());
		cbxSchemas = new JComboBox();
		txtName = new JTextField();
		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel pnl = new JPanel();
		pnl.setLayout(gbl);
		
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.fill		= GridBagConstraints.HORIZONTAL;
		gbc.weightx		= 1.0;

		gbc.insets = new Insets(0,8,0,8);
		JLabel lbl = new JLabel(I18n.getString("application.message.useConnection","Use connection:"));
		gbl.setConstraints(lbl, gbc);
		pnl.add(lbl);
		gbl.setConstraints(cbxConnections,gbc);
		pnl.add(cbxConnections);
		
		gbc.insets = new Insets(5,8,0,8);
		lbl = new JLabel(I18n.getString("application.message.schema","schema:"));
		gbl.setConstraints(lbl, gbc);
		pnl.add(lbl);
		gbc.insets = new Insets(0,8,0,8);
		gbl.setConstraints(cbxSchemas,gbc);
		pnl.add(cbxSchemas);
		
		gbc.insets = new Insets(5,8,0,8);
		lbl = new JLabel(I18n.getString("metadataexplorer.Table","table:"));
		gbl.setConstraints(lbl, gbc);
		pnl.add(lbl);
		gbc.insets = new Insets(0,8,0,8);
		gbl.setConstraints(txtName,gbc);
		pnl.add(txtName);
		
		getContentPane().add(new JScrollPane(pnl));
	}

	public static Object[] show(String title)
	{
		DialogQuickObject dlg = new DialogQuickObject(title);
		dlg.setVisible(true);
		
		return onDispose(dlg);
	}
	
	private static Object[] onDispose(DialogQuickObject dlg)
	{
		Object[] ret = null;
		
		if(dlg.terminated)
		{
			String keycah = dlg.cbxConnections.getSelectedItem().toString();
			String name = dlg.txtName.getText().trim();
			
			try
			{
				ConnectionHandler ch = ConnectionAssistant.getHandler(keycah);
				DatabaseMetaData dbmetadata = ch.get().getMetaData();
				
				if(dbmetadata.storesLowerCaseIdentifiers())
					name = name.toLowerCase();
				else if(dbmetadata.storesUpperCaseIdentifiers())
					name = name.toUpperCase();
			}
			catch(SQLException sql)
			{
				Application.println(sql,true);
			}
			
			ret = new Object[]{keycah,dlg.cbxSchemas.getSelectedItem(),name};
		}
		
		dlg.dispose();
		return ret;
	}

	protected boolean onConfirm()
	{
		if(cbxConnections.getSelectedItem() == null) return false;
		if(cbxSchemas.isEnabled() && cbxSchemas.getSelectedItem() == null) return false;
		if(txtName.getText().trim().length() == 0) return false;
		
		return terminated = true;
	}

	protected void onOpen()
	{
		cbxConnections.addItemListener(this);
		cbxConnections.setSelectedItem(null);
		
		cbxSchemas.setSelectedItem(null);
		cbxSchemas.setEnabled(false);
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		ConnectionHandler ch = ConnectionAssistant.getHandler(ie.getItem().toString());
		if(ch == null)
		{
			cbxSchemas.setModel(new DefaultComboBoxModel());
		}
		else
		{
			ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
			cbxSchemas.setModel(new DefaultComboBoxModel(schemas.toArray()));
		}
		
		cbxSchemas.setEnabled(cbxSchemas.getItemCount()>0);
	}
}
