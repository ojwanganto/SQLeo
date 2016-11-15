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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sqleo.common.util.I18n;
import com.sqleo.environment.Preferences;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class MaskJoin extends BaseMask
{
	private JCheckBox allLeft;
	private JCheckBox allRight;
	private JComboBox operator;
	
	private DiagramRelation relation;
	
	public MaskJoin(DiagramRelation relation,QueryBuilder builder)
	{
		super("join.edit",builder);
		this.relation = relation;
		
		JLabel primary = new JLabel(relation.primaryField.querytoken.getIdentifier(), JLabel.CENTER);
		JLabel foreign = new JLabel(relation.foreignField.querytoken.getIdentifier(), JLabel.CENTER);
		
		Border border = new CompoundBorder(LineBorder.createBlackLineBorder(), new EmptyBorder(3,4,3,4));
		primary.setBorder(border);
		primary.setOpaque(true);
		primary.setBackground(ViewDiagram.BGCOLOR_START_JOIN);
		foreign.setBorder(border);
		foreign.setOpaque(true);
		foreign.setBackground(ViewDiagram.BGCOLOR_JOINED);
		
		operator = new JComboBox(new String[]{"=","<",">","<=",">=","<>","!="});
		operator.setSelectedItem(relation.querytoken.getCondition().getOperator());
		
		GridBagLayout gbl = new GridBagLayout();
		JPanel pane = new JPanel(gbl);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets	= new Insets(0,0,3,0);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		
		gbc.weightx	= 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(primary, gbc);
		pane.add(primary);
		
		gbc.weightx	= 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(operator, gbc);
		pane.add(operator);
		
		gbc.weightx	= 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbl.setConstraints(foreign, gbc);
		pane.add(foreign);
		
		gbc.insets	= new Insets(8,0,0,0);
		allLeft = new JCheckBox(I18n.getFormattedString("querybuilder.checkbox.allRowsFrom","all rows from {0}", new Object[]{ ""+relation.primaryEntity.getHeaderMenu().getText()}) );
		allLeft.setSelected(relation.querytoken.getType() == QueryTokens.Join.LEFT_OUTER || relation.querytoken.getType() == QueryTokens.Join.FULL_OUTER);
		pane.add(allLeft);
		gbl.setConstraints(allLeft, gbc);
		
		gbc.insets	= new Insets(0,0,0,0);
		allRight = new JCheckBox(I18n.getFormattedString("querybuilder.checkbox.allRowsFrom","all rows from {0}", new Object[]{ "" + relation.foreignEntity.getHeaderMenu().getText()}) );
		allRight.setSelected(relation.querytoken.getType() == QueryTokens.Join.RIGHT_OUTER || relation.querytoken.getType() == QueryTokens.Join.FULL_OUTER);
		pane.add(allRight);
		gbl.setConstraints(allRight, gbc);

		add(pane);
	}
	
	public Dimension getPreferredSize()
	{
		return Preferences.getScaledDimension(350,170);		
	}	
	
	protected void onShow(){}
	protected boolean onConfirm()
	{
		int jointype = 0;
		
		jointype += allLeft.isSelected() ? QueryTokens.Join.LEFT_OUTER : 0;
		jointype += allRight.isSelected() ? QueryTokens.Join.RIGHT_OUTER : 0;
		
		relation.setValues(jointype,operator.getSelectedItem().toString());
		
		return true;
	}
}