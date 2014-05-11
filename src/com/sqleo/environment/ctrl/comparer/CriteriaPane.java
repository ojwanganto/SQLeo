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

package com.sqleo.environment.ctrl.comparer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi._ConnectionListener;


public class CriteriaPane extends JPanel implements _Starter, _ConnectionListener, ItemListener, ActionListener
{
	private _Analyzer analyzer;
	private CommandButton cb;
	
	private JComboBox cbxConnection1;
	private JComboBox cbxSchema1;
	private JComboBox cbxConnection2;
	private JComboBox cbxSchema2;
	
	/* filters */
	private JTextField txtTables;
	private JComboBox cbxPattern;

	private JCheckBox chxViceVersa;
	private JCheckBox chxColumns;
	private JCheckBox chxIndeces;
	private JCheckBox chxPrimaryKeys;
	private JCheckBox chxExportedKeys;
	private JCheckBox chxImportedKeys;
	
	public CriteriaPane(_Analyzer analyzer)
	{
		Application.window.addListener(this);
		this.analyzer = analyzer;
		
		setBackground(Color.white);
		setBorder(LineBorder.createGrayLineBorder());
		
		initComponents();
	}
	
	private void initComponents()
	{
		JPanel pnlMaster = new JPanel(new GridLayout(4,1,0,0));
		pnlMaster.setOpaque(false);
		pnlMaster.add(new JLabel("1) Use connection:"));
		pnlMaster.add(cbxConnection1	= new JComboBox(ConnectionAssistant.getHandlers().toArray()));
		pnlMaster.add(new JLabel("1) Schema:"));
		pnlMaster.add(cbxSchema1 = new JComboBox());
		cbxConnection1.setSelectedItem(null);
		cbxSchema1.setEnabled(false);
		
		JPanel pnlSlave = new JPanel(new GridLayout(4,1,0,0));
		pnlSlave.setOpaque(false);
		pnlSlave.add(new JLabel("2) Use connection:"));
		pnlSlave.add(cbxConnection2	= new JComboBox(ConnectionAssistant.getHandlers().toArray()));
		pnlSlave.add(new JLabel("2) Schema:"));
		pnlSlave.add(cbxSchema2 = new JComboBox());
		cbxConnection2.setSelectedItem(null);
		cbxSchema2.setEnabled(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.fill		= GridBagConstraints.HORIZONTAL;
		gbc.weightx		= 1.0;
		gbc.anchor		= GridBagConstraints.WEST;
		
		gbc.insets = new Insets(10,5,0,5);
		gbl.setConstraints(pnlMaster,gbc);
		add(pnlMaster);
		
		gbl.setConstraints(pnlSlave,gbc);
		add(pnlSlave);
		
		chxViceVersa = new JCheckBox("Vice versa");
		chxViceVersa.setOpaque(false);
		gbc.insets = new Insets(10,5,0,5);
		gbl.setConstraints(chxViceVersa,gbc);
		add(chxViceVersa);
		
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		gbc.insets = new Insets(20,5,0,5);
		gbc.weightx	= 0.0;
		JLabel lbl = new JLabel("Table:");
		gbl.setConstraints(lbl,gbc);
		add(lbl);
        
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx	= 1.0;
		cbxPattern = new JComboBox(new String[]{"equals","contains","starts with","ends with"});
		gbl.setConstraints(cbxPattern,gbc);
		add(cbxPattern);
        
		gbc.insets	= new Insets(0,5,0,5);
		txtTables = new JTextField();
		gbl.setConstraints(txtTables,gbc);
		add(txtTables);
		
		cbxPattern.setPreferredSize(txtTables.getPreferredSize());
		
		chxColumns = new JCheckBox("Columns");
		chxColumns.setOpaque(false);
		gbc.insets = new Insets(10,5,0,5);
		gbl.setConstraints(chxColumns,gbc);
		add(chxColumns);
		
		chxPrimaryKeys = new JCheckBox("Primary keys");
		chxPrimaryKeys.setOpaque(false);
		gbc.insets	= new Insets(0,5,0,5);
		gbl.setConstraints(chxPrimaryKeys,gbc);
		add(chxPrimaryKeys);
		
		chxIndeces = new JCheckBox("Indices");
		chxIndeces.setOpaque(false);
		gbc.insets	= new Insets(0,5,0,5);
		gbl.setConstraints(chxIndeces,gbc);
		add(chxIndeces);
		
		chxExportedKeys = new JCheckBox("Exported keys");
		chxExportedKeys.setOpaque(false);
		gbc.insets	= new Insets(0,5,0,5);
		gbl.setConstraints(chxExportedKeys,gbc);
		add(chxExportedKeys);
		
		chxImportedKeys = new JCheckBox("Imported keys");
		chxImportedKeys.setOpaque(false);
		gbc.insets	= new Insets(0,5,0,5);
		gbl.setConstraints(chxImportedKeys,gbc);
		add(chxImportedKeys);
		
		gbc.fill		= GridBagConstraints.NONE;
		gbc.anchor		= GridBagConstraints.CENTER;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.insets		= new Insets(25,2,0,2);
		
		cb = new CommandButton("Start",this);
		gbl.setConstraints(cb,gbc);
		add(cb);
		
		/* blank */
		gbc = new GridBagConstraints();
		gbc.weighty	= 1.0;
		
		JPanel pnl = new JPanel();
		pnl.setOpaque(false);
		gbl.setConstraints(pnl,gbc);
		add(pnl);
		
		cbxConnection1.addItemListener(this);
		cbxConnection2.addItemListener(this);
	}
	
	public void onConnectionClosed(String keycah)
	{
		cbxConnection1.removeItem(keycah);
		cbxConnection2.removeItem(keycah);		
	}

	public void onConnectionOpened(String keycah)
	{
		cbxConnection1.addItem(keycah);
		cbxConnection2.addItem(keycah);
	}

	public void actionPerformed(ActionEvent ae)
	{
		boolean stop = cb.getText().equals("Stop");
		
		if(!stop && cbxConnection1.getSelectedIndex() == -1) return;
		if(!stop && cbxSchema1.getItemCount() > 0 && cbxSchema1.getSelectedIndex() == -1) return;
		if(!stop && cbxConnection2.getSelectedIndex() == -1) return;
		if(!stop && cbxSchema2.getItemCount() > 0 && cbxSchema2.getSelectedIndex() == -1) return;
		
		if(stop)
		{
			analyzer.onStopped();
			onFinished();
		}
		else
		{
			setEnabled(false);
			cb.setText("Stop");
			analyzer.perform(this);
		}
	}

	public void setEnabled(boolean b)
	{
		cbxConnection1.setEnabled(b);
		cbxSchema1.setEnabled(b && cbxSchema1.getItemCount()>0);
		cbxConnection2.setEnabled(b);
		cbxSchema2.setEnabled(b && cbxSchema2.getItemCount()>0);
		txtTables.setEnabled(b);
		cbxPattern.setEnabled(b);
		chxColumns.setEnabled(b);
		chxIndeces.setEnabled(b);
		chxPrimaryKeys.setEnabled(b);
		chxExportedKeys.setEnabled(b);
		chxImportedKeys.setEnabled(b);
		chxViceVersa.setEnabled(b);
	}

	public void onFinished()
	{
		setEnabled(true);
		cb.setText("Start");
	}
	
	public boolean isViceVersa()
	{
		return chxViceVersa.isSelected();
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		JComboBox cbxSchema = ie.getSource() == cbxConnection1 ? cbxSchema1 : cbxSchema2;

		ConnectionHandler ch = ConnectionAssistant.getHandler(ie.getItem().toString());
		if(ch == null)
		{
			cbxSchema.setModel(new DefaultComboBoxModel());
		}
		else
		{
			ArrayList schemas = (ArrayList)ch.getObject("$schema_names");
			cbxSchema.setModel(new DefaultComboBoxModel(schemas.toArray()));
		}
		
		cbxSchema.setEnabled(cbxSchema.getItemCount()>0);
	}
	
	public String getKeyHandler1()
	{
		return cbxConnection1.getSelectedItem().toString();
	}

	public String getSchema1()
	{
		return cbxSchema1.getSelectedIndex() != -1 ? cbxSchema1.getSelectedItem().toString() : null;
	}

	public String getKeyHandler2()
	{
		return cbxConnection2.getSelectedItem().toString();
	}

	public String getSchema2()
	{
		return cbxSchema2.getSelectedIndex() != -1 ? cbxSchema2.getSelectedItem().toString() : null;
	}

	public String getTablePattern()
	{
		String operator = cbxPattern.getSelectedItem().toString();
		String value = txtTables.getText();
		
		if(value==null || value.length()==0) return null;
		
		if(operator.equals("contains"))
		{
			if(!value.toString().startsWith("%")) value = "%" + value.toString();
			if(!value.toString().endsWith("%")) value = value.toString() + "%";
		}
		else if(operator.equals("starts with"))
		{
			if(!value.toString().endsWith("%")) value = value.toString() + "%";
		}
		else if(operator.equals("ends with"))
		{
			if(!value.toString().startsWith("%")) value = "%" + value.toString();
		}
		
		return value.toUpperCase();
	}

	public boolean checkColumns()
	{
		return chxColumns.isSelected();
	}

	public boolean checkIndeces()
	{
		return chxIndeces.isSelected();
	}

	public boolean checkPrimaryKeys()
	{
		return chxPrimaryKeys.isSelected();
	}

	public boolean checkExportedKeys()
	{
		return chxExportedKeys.isSelected();
	}

	public boolean checkImportedKeys()
	{
		return chxImportedKeys.isSelected();
	}
}
