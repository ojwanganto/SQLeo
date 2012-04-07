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
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sqleo.common.gui.BorderLayoutPanel;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class MaskExpression extends BaseMask
{
	private QueryTokens.DefaultExpression querytoken;
	private JTextArea value;
	private JTextField alias;
	
	public MaskExpression(QueryTokens.DefaultExpression token,QueryBuilder builder)
	{
		super("expression.edit",builder);
		querytoken = token;
		
		BorderLayoutPanel north = new BorderLayoutPanel(2,2);
		north.setComponentWest(new JLabel("Alias:"));
		north.setComponentCenter(alias = new JTextField());
		setComponentNorth(north);
		
		setComponentCenter(new JScrollPane(value = new JTextArea()));
		value.setWrapStyleWord(true);
		value.setLineWrap(true);
		value.setColumns(45);
		value.setRows(10);
	}

	protected boolean onConfirm()
	{
		if(builder.browser.getQueryItem() instanceof BrowserItems.DiagramQueryTreeItem
		&&(alias.getText() == null || alias.getText().trim().length() == 0))
		{
			String message = "Please, set a valid alias.";
			
			if(SwingUtilities.getWindowAncestor(builder) instanceof Frame)
				JOptionPane.showMessageDialog((Frame)SwingUtilities.getWindowAncestor(builder),message,"SQLeonardo",JOptionPane.WARNING_MESSAGE);
			else if(SwingUtilities.getWindowAncestor(builder) instanceof Dialog)
				JOptionPane.showMessageDialog((Dialog)SwingUtilities.getWindowAncestor(builder),message,"SQLeonardo",JOptionPane.WARNING_MESSAGE);
						
			return false;
		}
		
		querytoken.setValue(value.getText());
		querytoken.setAlias(alias.getText());
		return true;
	}

	protected void onShow()
	{
		if(!querytoken.isEmpty()) value.setText(querytoken.getValue());
		if(querytoken.isAliasSet()) alias.setText(querytoken.getAlias());
	}
}