/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

package com.sqleo.environment.ctrl.explorer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import com.sqleo.common.gui.CommandButton;
import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.environment.Application;
import com.sqleo.environment.mdi._ConnectionListener;
import com.sqleo.common.util.I18n;


public class SideSearchCriteria extends JPanel implements ActionListener, _ConnectionListener
{
    private String[] operators = {
		I18n.getString("metadataexplorer.action.Contains","contains"),
		I18n.getString("metadataexplorer.action.Equals","equals"),
		I18n.getString("metadataexplorer.action.StartWith","starts with"),
		I18n.getString("metadataexplorer.action.EndWith","ends with")};
    private ViewSearchResult rView;
    
	private JComboBox cbxConnections;
	private JComboBox cbxTableTypes;

	private CommandButton cbStart;
	private CommandButton cbReset;
	
	private JComboBox[] cbx;
	private JTextField[] txt;
    
    public SideSearchCriteria()
    {
		Application.window.addListener(this);
		
        setBackground(Color.white);
        setBorder(LineBorder.createGrayLineBorder());
        
        GridBagLayout gbl = new GridBagLayout();
        setLayout(gbl);
        
		cbx = new JComboBox[3];
		txt = new JTextField[3];
		addTableTypes(gbl);
	
        addCriteria(0,gbl,I18n.getString("application.message.schema","schema:"),5);
        addCriteria(1,gbl,I18n.getString("metadataexplorer.Table","table:"),5);
        addCriteria(2,gbl,I18n.getString("metadataexplorer.Column","columns:"),5);

		addConnection(gbl);
		
		cbxConnections.addItemListener( new OnSelectConnectionListener());
		
		addButtons(gbl);
        
        rView = new ViewSearchResult();
    }
    
    private class OnSelectConnectionListener implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent evt) {
			// Get the affected item
	        String connection = (String)evt.getItem();
	        if (evt.getStateChange() == ItemEvent.SELECTED && connection!=null) {
	        	cbxTableTypes.removeAllItems();
				cbxTableTypes.addItem("ALL");
				ArrayList<String> types =  ConnectionAssistant.getHandler(connection).getArrayList("$table_types");
					for(String s : types){
						cbxTableTypes.addItem(s);
					}
	        } 
		}
    }
    
    
    private void addCriteria(int idx, GridBagLayout gbl, String where, int top_gap)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(top_gap,5,1,5);
        
        JLabel lbl = new JLabel(where);
        gbl.setConstraints(lbl,gbc);
        add(lbl);
        
        gbc.gridwidth	= GridBagConstraints.REMAINDER;
        gbc.fill		= GridBagConstraints.HORIZONTAL;
        gbc.weightx		= 1.0;
        
        cbx[idx] = new JComboBox(operators);
        gbl.setConstraints(cbx[idx],gbc);
        add(cbx[idx]);
        
        gbc.insets	= new Insets(0,5,0,5);
        
        txt[idx] = new JTextField();
		txt[idx].addActionListener(this);
        gbl.setConstraints(txt[idx],gbc);
        add(txt[idx]);
        
        cbx[idx].setPreferredSize(txt[idx].getPreferredSize());
    }
    
    private void addConnection(GridBagLayout gbl)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth	= GridBagConstraints.REMAINDER;
        gbc.anchor		= GridBagConstraints.WEST;
        gbc.insets		= new Insets(5,5,1,5);
        
        JLabel lbl = new JLabel(I18n.getString("application.message.useConnection","Use connection:"));
        gbl.setConstraints(lbl,gbc);
        add(lbl);
        
        gbc.fill	= GridBagConstraints.BOTH;
        gbc.insets	= new Insets(0,5,8,5);
        
		cbxConnections = new JComboBox();
        gbl.setConstraints(cbxConnections,gbc);
        add(cbxConnections);
    }
    private void addTableTypes(GridBagLayout gbl)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth	= GridBagConstraints.REMAINDER;
        gbc.anchor		= GridBagConstraints.WEST;
        gbc.insets		= new Insets(5,5,1,5);
        
        JLabel lbl = new JLabel(I18n.getString("metadataexplorer.TableType","table type:"));
        gbl.setConstraints(lbl,gbc);
        add(lbl);
        
        gbc.fill	= GridBagConstraints.BOTH;
        gbc.insets	= new Insets(0,5,8,5);
        
		cbxTableTypes = new JComboBox();
        gbl.setConstraints(cbxTableTypes,gbc);
        add(cbxTableTypes);
    }

	private void addButtons(GridBagLayout gbl)
	{
		JPanel pnl = new JPanel();
		pnl.add(cbStart = new CommandButton(I18n.getString("metadataexplorer.action.Start","Start"),this));
		pnl.add(cbReset = new CommandButton(I18n.getString("metadataexplorer.action.Reset","Reset"),this));
		pnl.setOpaque(false);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor	= GridBagConstraints.NORTH;
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
        
		gbl.setConstraints(pnl,gbc);
		add(pnl);
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == cbReset)
		{
			rView.reset();
			rView.setInfo("...");
			
			cbxConnections.setSelectedItem(null);
			cbxTableTypes.setSelectedItem(null);
			for(int i=0; i<cbx.length; i++)
			{
				cbx[i].setSelectedIndex(0);
				txt[i].setText(null);
			}
		}
		else
		{
			if(cbxConnections.getSelectedIndex() == -1) return;
			
			String schema	= getSearchString(0);
			String table	= getSearchString(1);
			String column	= getSearchString(2);
			String[] tableTypes = cbxTableTypes.getSelectedIndex() == 0 ? null : new String[]{cbxTableTypes.getSelectedItem().toString()};
			try
			{
				cbStart.setEnabled(false);
				cbReset.setEnabled(false);
				
				rView.list(cbxConnections.getSelectedItem().toString(),schema,table,column,tableTypes);
				rView.setInfo("schema:" + schema + " table:" + table + " column:" + column + " on " + cbxConnections.getSelectedItem().toString());
			}
			catch(SQLException e)
			{
				Application.println(e,true);
			}
			finally
			{
				cbStart.setEnabled(true);
				cbReset.setEnabled(true);				
			}
		}
	}
	
	private String getSearchString(int idx)
	{
		String operator = cbx[idx].getSelectedItem().toString();
		String value	= txt[idx].getText();
		
		if(value==null || value.length()==0) return null;
		
		if(operator.toString().equals(operators[0]))
		{
			if(!value.toString().startsWith("%")) value = "%" + value.toString();
			if(!value.toString().endsWith("%")) value = value.toString() + "%";
		}
		else if(operator.toString().equals(operators[2]))
		{
			if(!value.toString().endsWith("%")) value = value.toString() + "%";
		}
		else if(operator.toString().equals(operators[3]))
		{
			if(!value.toString().startsWith("%")) value = "%" + value.toString();
		}
		
// #364 Metadata explorer: search is Upper cased, ko with lower case object names
//		return value.toString().toUpperCase();
		return value.toString();
	}
	
    public JComponent getRightView()
    {
        return rView;
    }

	public void setPreferences()
	{
	}
	
	public void onConnectionClosed(String keycah)
	{
		cbxConnections.removeItem(keycah);
	}
	
	public void onConnectionOpened(String keycah)
	{
		cbxConnections.addItem(keycah);
	}
}