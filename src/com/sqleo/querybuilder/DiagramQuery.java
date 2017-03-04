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
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import com.sqleo.environment.Application;
import com.sqleo.querybuilder.syntax.DerivedTable;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class DiagramQuery extends DiagramAbstractEntity
{
	private DerivedTable subquery;
	private QueryTokens.Table reference;
	private static ImageIcon icon;

	DiagramQuery(QueryBuilder builder,DerivedTable subquery)
	{
		super(builder);
		
		if (icon == null) icon = Application.resources.getIcon(Application.ICON_DIAG_QUERY);
		getHeaderMenu().setIcon(icon);
		
		setQueryToken(subquery);
	}

	void onCreate()
	{
		builder.browser.addFromClause(subquery);
		doFlush();		
	}
	
	void onDestroy()
	{
		fireDeselectAll();
		builder.diagram.removeAllRelation(this);
		builder.browser.removeFromClause(subquery);
	}
	
	protected Color getDefaultBackground()
	{
		return Color.red;
	}
	
	DiagramField addField(String label)
	{
		DiagramField df = new DiagramField(this,label);
		
		QueryTokens.Column ctoken = new QueryTokens.Column(this.getQueryToken(),label);
		df.setQueryToken(ctoken);
		
		addField(df);
		return df;
	}
	
	void removeField(String label)
	{
		DiagramField df = getField(label);
		if(df==null){
			return;
		}
		removeField(df);
		
		queryItem.getQueryExpression().getQuerySpecification().removeSelectList(df.getQueryToken());
		
		((BrowserItems.DefaultTreeItem)queryItem.getFirstChild()).removeChild(df.getQueryToken());
		builder.browser.reload(queryItem.getFirstChild());
		
		BrowserItems.FromTreeItem fromItem = (BrowserItems.FromTreeItem)queryItem.getChildAt(1);
		Component[] diagramObjects = fromItem.getDiagramObjects();
		if(diagramObjects!=null)
		{
			ArrayList al = new ArrayList();
			for(int i=0; i<diagramObjects.length; i++)
			{
				if(diagramObjects[i] instanceof DiagramRelation)
				{
					DiagramRelation rel = (DiagramRelation)diagramObjects[i];
					if(rel.primaryField == df || rel.foreignField == df)
					{
						rel.primaryField.unjoined();
						rel.foreignField.unjoined();
						
						queryItem.getQueryExpression().getQuerySpecification().removeFromClause(rel.getQueryToken());
						
						for(int s=0; s<2; s++)
						{
							QueryTokens.Table qtoken = s==0 ? rel.getQueryToken().getPrimary().getTable() : rel.getQueryToken().getForeign().getTable();
							BrowserItems.DefaultTreeItem child = fromItem.findChild(qtoken);
							if(child!=null)
							{
								((BrowserItems.JoinHandler)child).unjoined();
								if(!((BrowserItems.JoinHandler)child).isJoined())
								{
									child.setUserObject(qtoken);
									if(child instanceof BrowserItems.TableTreeItem)
										queryItem.getQueryExpression().getQuerySpecification().addFromClause(qtoken);
								}
							}
						}
						
						continue;
					}
				}
				al.add(diagramObjects[i]);
			}
			
			fromItem.setDiagramObjects(al.size()>0 ? (Component[])al.toArray(new Component[al.size()]) : null);
		}		
	}
	
	public QueryTokens.Table getQueryToken()
	{
		return reference;
	}

	public void setQueryToken(DerivedTable subquery)
	{
		this.subquery = subquery;
		getHeaderMenu().setText(subquery.getAlias());
		
		reference = new QueryTokens.Table(null,null);
		reference.setAlias(subquery.getAlias());
		
		pack();
	}	
}