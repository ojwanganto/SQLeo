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
import javax.swing.tree.TreeNode;

import com.sqleo.environment.Preferences;
import com.sqleo.environment._Version;
import com.sqleo.querybuilder.BrowserItems.DefaultTreeItem;
import com.sqleo.querybuilder.BrowserItems.DiagramQueryTreeItem;
import com.sqleo.querybuilder.syntax.DerivedTable;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.QueryTokens.Condition;
import com.sqleo.querybuilder.syntax.QueryTokens.DefaultExpression;
import com.sqleo.querybuilder.syntax.SQLFormatter;


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
		return Preferences.getScaledDimension(300,100);
	}
	
	private String getUpdatedTokenWithAlias(final String schema, final String tableName,
			final String token, final String aliasBefore, final String aliasAfter){
		if(token.lastIndexOf(SQLFormatter.DOT)!=-1){
			final String[] split = token.split("\\"+SQLFormatter.DOT);
			final String tabNameOfToken = schema!=null? split[1] : split[0];
			if(null == aliasBefore){
				if(tableName.equals(tabNameOfToken)){
					return token.replaceFirst(tableName, aliasAfter);
				}
			}else {
				if(aliasBefore.equals(tabNameOfToken)){
					return token.replaceFirst(aliasBefore, aliasAfter);
				}
			}
		}
		return null;
	}
	
	private Condition updateConditionTokenAlias(final Condition condition,final String schema,final String tableName,
			final String aliasBefore,final String aliasAfter){
		if(condition.getLeft() instanceof DefaultExpression){
			final DefaultExpression exp = (DefaultExpression) condition.getLeft();
			final String updatedToken = getUpdatedTokenWithAlias(schema, tableName, exp.toString(), aliasBefore, aliasAfter);
			if(updatedToken!=null){
				exp.setValue(updatedToken);
			}
		}
		return condition;
	}
	
	private void updateQueryTokensRelatedToTableAlias(String schema, String tableName, String aliasBefore, String aliasAfter){
		QuerySpecification qs = builder.browser.getQueryItem().getQueryExpression().getQuerySpecification();
		for(int i = 0 ; i < qs.getWhereClause().length ; i++){
			updateConditionTokenAlias(qs.getWhereClause()[i], schema, tableName, aliasBefore, aliasAfter );
		}
		for(int i = 0; i < qs.getHavingClause().length; i++){
			updateConditionTokenAlias(qs.getHavingClause()[i], schema, tableName, aliasBefore, aliasAfter );
		}
	}

	protected boolean onConfirm()
	{
		if(querytoken instanceof QueryTokens.Table)
		{
			updateQueryTokensRelatedToTableAlias(((QueryTokens.Table) querytoken).getSchema(), querytoken.getName(), querytoken.getAlias(), value.getText());
			
			DiagramEntity entity = builder.diagram.getEntity((QueryTokens.Table)querytoken);
			querytoken.setAlias(value.getText());
			entity.setQueryToken((QueryTokens.Table)querytoken);
			
			final TreeNode parent = builder.browser.getQueryItem().getParent();
			if(parent!=null){
				final TreeNode selected = builder.browser.getSelectedNode();
				builder.browser.reload(parent.getChildAt(0));
				builder.browser.setSelectedItem((DefaultTreeItem) selected);
			}
		}
		else
		{
			if(builder.browser.getQueryItem() instanceof BrowserItems.DiagramQueryTreeItem)
			{
				if(value.getText() == null || value.getText().trim().length() == 0)
				{
					String message = "Please, set a valid alias.";
					
					if(SwingUtilities.getWindowAncestor(builder) instanceof Frame)
						JOptionPane.showMessageDialog((Frame)SwingUtilities.getWindowAncestor(builder),message,_Version.PROGRAM,JOptionPane.WARNING_MESSAGE);
					else if(SwingUtilities.getWindowAncestor(builder) instanceof Dialog)
						JOptionPane.showMessageDialog((Dialog)SwingUtilities.getWindowAncestor(builder),message,_Version.PROGRAM,JOptionPane.WARNING_MESSAGE);
								
					return false;
				}
				
				BrowserItems.DiagramQueryTreeItem dqti = (BrowserItems.DiagramQueryTreeItem)builder.browser.getQueryItem();
				if(querytoken!=null){
					final String fieldName = querytoken.getAlias()!=null ? querytoken.getAlias() : querytoken.getName();
					reloadParentWithAlias(dqti,fieldName, value.getText());
				}else if (derivedTable!=null){
					final DiagramQuery diagQuery = dqti.getDiagramObject();
					final String oldAlias = derivedTable.getAlias();
					derivedTable.setAlias(value.getText());
					if(diagQuery!=null){
						diagQuery.setQueryToken(derivedTable);
					}
					dqti.setUserObject(value.getText());
					reloadParentWithAlias(dqti,oldAlias, value.getText());
				}
			}
			if(querytoken!=null) {		
				querytoken.setAlias(value.getText());
			}
		}
		
		return true;
	}
	
	private void reloadParentWithAlias(final BrowserItems.DiagramQueryTreeItem dqti,final String fieldName, final String text) {
		if(dqti!=null){
			if(dqti.getDiagramObject()!=null){
				if (derivedTable!=null){
					dqti.getDiagramObject().setFieldsWithAlias(fieldName, text);
				}else {
					DiagramField field = dqti.getDiagramObject().getField(fieldName);
					if(field!=null){
						field.getLabelComponent().setText(text);
						field.getQueryToken().setName(text);
						field.setName(text);
						dqti.getDiagramObject().pack();
					}
				}
				final TreeNode parent = dqti.getParent().getParent();
				builder.browser.reload(parent.getChildAt(0));
				if(parent instanceof BrowserItems.DiagramQueryTreeItem){
					reloadParentWithAlias((DiagramQueryTreeItem) parent, fieldName, text);
				}
			}
		}
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