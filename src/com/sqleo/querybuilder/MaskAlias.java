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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sqleo.querybuilder.syntax.DerivedTable;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Table;


public class MaskAlias extends BaseMask
{
	private QueryTokens.AbstractDatabaseObject querytoken;
	private DerivedTable derivedTable;
	private JTextField value;
	
	public MaskAlias(DerivedTable token,QueryBuilder builder)
	{
		super("database object.edit",builder);
		maskAliasInternal(null,token,builder);
	}
	
	public MaskAlias(QueryTokens.AbstractDatabaseObject token,QueryBuilder builder)
	{
		super("database object.edit",builder);
		maskAliasInternal(token,null,builder);
	}
	
	private void maskAliasInternal(QueryTokens.AbstractDatabaseObject token,DerivedTable derivedTable,QueryBuilder builder)
	{
		querytoken = token;
		String identifier = querytoken!=null ? querytoken.getIdentifier() : null;
		
		this.derivedTable = derivedTable;
		identifier = this.derivedTable!=null ? this.derivedTable.getAlias() : null;
		
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel pnl = new JPanel();
		pnl.setLayout(gbl);
		
		gbc.anchor		= GridBagConstraints.WEST;
		gbc.gridwidth	= GridBagConstraints.REMAINDER;
		gbc.fill		= GridBagConstraints.HORIZONTAL;
		gbc.weightx		= 1.0;

		gbc.insets = new Insets(0,6,0,6);
		JLabel lbl = new JLabel("identifier:");
		gbl.setConstraints(lbl, gbc);
		pnl.add(lbl);
		JTextField txt = new JTextField(identifier);
		txt.setEditable(false);
		gbl.setConstraints(txt,gbc);
		pnl.add(txt);
		
		gbc.insets = new Insets(5,6,0,6);
		lbl = new JLabel("alias:");
		gbl.setConstraints(lbl, gbc);
		pnl.add(lbl);
		gbc.insets = new Insets(0,6,0,6);
		value = new JTextField();
		gbl.setConstraints(value,gbc);
		pnl.add(value);
		
		setComponentCenter(pnl);
	}
	
	public Dimension getPreferredSize()
	{
		return new Dimension(300,100);
	}

	protected boolean onConfirm()
	{
		if(querytoken instanceof QueryTokens.Table)
		{
			DiagramEntity entity = builder.diagram.getEntity((QueryTokens.Table)querytoken);
			querytoken.setAlias(value.getText());
			entity.setQueryToken((QueryTokens.Table)querytoken);
		}
		else
		{
			if(builder.browser.getQueryItem() instanceof BrowserItems.DiagramQueryTreeItem)
			{
				if(value.getText() == null || value.getText().trim().length() == 0)
				{
					String message = "Please, set a valid alias.";
					
					if(SwingUtilities.getWindowAncestor(builder) instanceof Frame)
						JOptionPane.showMessageDialog((Frame)SwingUtilities.getWindowAncestor(builder),message,"SQLeonardo",JOptionPane.WARNING_MESSAGE);
					else if(SwingUtilities.getWindowAncestor(builder) instanceof Dialog)
						JOptionPane.showMessageDialog((Dialog)SwingUtilities.getWindowAncestor(builder),message,"SQLeonardo",JOptionPane.WARNING_MESSAGE);
								
					return false;
				}
				
				BrowserItems.DiagramQueryTreeItem dqti = (BrowserItems.DiagramQueryTreeItem)builder.browser.getQueryItem();
				if(querytoken!=null){
					final String fieldName = querytoken.getAlias()!=null ? querytoken.getAlias() : querytoken.getName();
					if(dqti.getDiagramObject()!=null){
						final DiagramField field = dqti.getDiagramObject().getField(fieldName);
						if(field!=null){
							field.getLabelComponent().setText(value.getText());
							field.getQueryToken().setName(value.getText());
							field.setName(value.getText());
							dqti.getDiagramObject().pack();
							builder.browser.reload(dqti.getParent().getParent().getChildAt(0));
						}
					}
				}else if (derivedTable!=null){
					final DiagramQuery diagQuery = dqti.getDiagramObject();
					derivedTable.setAlias(value.getText());
					if(diagQuery!=null){
						diagQuery.setQueryToken(derivedTable);
					}
					dqti.setUserObject(value.getText());
					builder.browser.reload(dqti.getParent().getParent().getChildAt(0));
				}
			}
			if(querytoken!=null) {		
				querytoken.setAlias(value.getText());
			}
		}
		
		return true;
	}

	protected void onShow()
	{
		if(querytoken!=null && querytoken.isAliasSet())
			value.setText(querytoken.getAlias());
		else if(derivedTable!=null && derivedTable.isAliasSet())
			value.setText(derivedTable.getAlias());
		else
			value.setText("");
	}
}