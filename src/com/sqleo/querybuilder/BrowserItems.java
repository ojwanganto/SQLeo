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

import java.awt.Component;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sqleo.querybuilder.syntax.DerivedTable;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;
import com.sqleo.querybuilder.syntax.SubQuery;
import com.sqleo.querybuilder.syntax._ReservedWords;


public abstract class BrowserItems
{
	static class DefaultTreeItem extends DefaultMutableTreeNode
	{
		DefaultTreeItem(Object userObject){super(userObject);}

		DefaultTreeItem addChild(Object userObject)
		{
			DefaultTreeItem child = new DefaultTreeItem(userObject);
			this.add(child);
			
			return child;
		}
		
		DefaultTreeItem findChild(Object userObject)
		{
			for(int i=0; i<getChildCount(); i++)
			{
				DefaultTreeItem child = (DefaultTreeItem)getChildAt(i);
				if(child.getUserObject().toString().equals(userObject.toString())) return child;
			}
			
			return null;
		}
		
		boolean isQueryToken()
		{
			return getUserObject() instanceof QueryTokens._Base;
		}

		boolean hasChild(Object userObject)
		{
			for(int i=0; i<getChildCount(); i++)
			{
				if(findChild(userObject)!=null) return true;
			}
			
			return false;
		}
		
		boolean hasChild(DefaultTreeItem item)
		{
			return hasChild(item.getUserObject());
		}
		
		void removeChild(Object userObject)
		{
			DefaultTreeItem child = findChild(userObject);
			if(child!=null)
				this.remove(child);
		}
	}
	
	static abstract class AbstractQueryTreeItem extends DefaultTreeItem
	{
		private QueryExpression qe;
		
		AbstractQueryTreeItem(String label, QueryExpression qe)
		{
			super(label);
			this.qe = qe;
			
			if(qe.getQuerySpecification().getQuantifier() == QuerySpecification.DISTINCT)
				add(new ClauseTreeItem(_ReservedWords.SELECT + " " + _ReservedWords.DISTINCT));
			else
				add(new ClauseTreeItem(_ReservedWords.SELECT));
			
			add(new FromTreeItem());
			add(new ClauseTreeItem(_ReservedWords.WHERE));
			add(new ClauseTreeItem(_ReservedWords.GROUP_BY));
			add(new ClauseTreeItem(_ReservedWords.HAVING));
		}
		
		QueryExpression getQueryExpression()
		{
			return qe;
		}
	}

	static class QueryTreeItem extends AbstractQueryTreeItem
	{
		QueryTreeItem(String label, QueryExpression qe){super(label, qe);}
	}
	
	static class DiagramQueryTreeItem extends AbstractQueryTreeItem implements JoinHandler
	{
		private DiagramQuery entity;
		private int joins;
		
		DiagramQueryTreeItem(DerivedTable token){super(token.getAlias(),token);}
		
		DiagramQuery getDiagramObject()
		{
			return entity;
		}
		
		void setDiagramObject(DiagramQuery entity)
		{
			this.entity = entity;
		}
		
		public boolean isJoined()
		{
			return joins>0;
		}
	
		public void joined()
		{
			joins++;
		}

		public void unjoined()
		{
			joins--;
		}
		
		public String toString()
		{
			return isJoined() ? "{ " + super.toString() + " }" : super.toString();
		}		
	}
	
	static class UnionQueryTreeItem extends AbstractQueryTreeItem
	{
		UnionQueryTreeItem(QueryExpression qe){super( _ReservedWords.UNION, qe);}
	}
	
	static class ConditionQueryTreeItem extends AbstractQueryTreeItem
	{
		private QueryTokens.Condition token;
		
		ConditionQueryTreeItem(QueryTokens.Condition token)
		{
            super("SUBQUERY", (SubQuery)token.getRight());
			this.token = token;
		}
		
		boolean isQueryToken()
		{
			return true;
		}
		
		QueryTokens.Condition getCondition()
		{
			return token;
		}
		
		public String toString()
		{
			String label = token.getAppend() == null ? new String() : token.getAppend() + SQLFormatter.SPACE;
			label = label + (token.getLeft() == null ? new String() : token.getLeft().toString() + SQLFormatter.SPACE);
			label = label + token.getOperator() + SQLFormatter.SPACE;
			label = "" + label + " (SUBQUERY)";
			
			return label;
		}
	}
	
	static class TableTreeItem extends DefaultTreeItem implements JoinHandler
	{
		private int joins;
		
		TableTreeItem(Object userObject){super(userObject);}
		
		public boolean isJoined()
		{
			return joins>0;
		}
	
		public void joined()
		{
			joins++;
		}

		public void unjoined()
		{
			joins--;
		}
		
		public String toString()
		{
			return isJoined() ? "{ " + super.toString() + " }" : super.toString();
		}
	}

	static class FromTreeItem extends DefaultTreeItem
	{
		private Component[] diagramObjects = null;
		
		FromTreeItem(){super(_ReservedWords.FROM);}
		
		Component[] getDiagramObjects()
		{
			return diagramObjects;
		}
		
		void setDiagramObjects(Component[] c)
		{
			diagramObjects = c;
		}
		
		void setSelected(boolean b)
		{
			String label = _ReservedWords.FROM;
			if(b) label = "[ " + label + " ]";
			
			setUserObject(label);
		}
	}
	
	static class ClauseTreeItem extends DefaultTreeItem
	{
		ClauseTreeItem(String label){super(label);}
		
		void onDropPerformed(QueryBuilder builder)
		{
			if(this.toString().indexOf(_ReservedWords.SELECT)!=-1)
				refreshSelectList(builder);
			else if(this.toString().indexOf(_ReservedWords.WHERE)!=-1)
				refreshWhereClause(builder);
			else if(this.toString().indexOf(_ReservedWords.GROUP_BY)!=-1)
				refreshGroupByClause(builder);
			else if(this.toString().indexOf(_ReservedWords.HAVING)!=-1)
				refreshHavingClause(builder);
			else if(this.toString().indexOf(_ReservedWords.ORDER_BY)!=-1)
				refreshOrderByClause(builder);
		}
			
		private void refreshSelectList(QueryBuilder builder)
		{
			QueryTokens._Expression[] tokens = builder.browser.getQuerySpecification().getSelectList();
			for(int i=0; i<tokens.length; i++)
			{
				builder.browser.getQuerySpecification().removeSelectList(tokens[i]);
			}
			
			for(int i=0; i<this.getChildCount(); i++)
			{
				QueryTokens._Expression token;
				
				DefaultTreeItem item = (DefaultTreeItem)this.getChildAt(i);
				if(item instanceof AbstractQueryTreeItem)
					token = (SubQuery)((AbstractQueryTreeItem)item).getQueryExpression();
				else
					token = (QueryTokens._Expression)item.getUserObject();
					
				builder.browser.getQuerySpecification().addSelectList(token);
			}
		}
	
		private void refreshWhereClause(QueryBuilder builder)
		{
			QueryTokens.Condition[] tokens = builder.browser.getQuerySpecification().getWhereClause();
			for(int i=0; i<tokens.length; i++)
			{
				builder.browser.getQuerySpecification().removeWhereClause(tokens[i]);
			}
			
			for(int i=0; i<this.getChildCount(); i++)
			{
				DefaultTreeItem item = (DefaultTreeItem)this.getChildAt(i);
				QueryTokens.Condition token = item instanceof ConditionQueryTreeItem ? ((ConditionQueryTreeItem)item).getCondition() : (QueryTokens.Condition)item.getUserObject();

				builder.browser.getQuerySpecification().addWhereClause(token);
				
				if(i==0)
					token.setAppend(null);
				else if(token.getAppend() == null)
					token.setAppend(_ReservedWords.AND);
			}
		}
	
		private void refreshGroupByClause(QueryBuilder builder)
		{
			QueryTokens.Group[] tokens = builder.browser.getQuerySpecification().getGroupByClause();
			for(int i=0; i<tokens.length; i++)
			{
				builder.browser.getQuerySpecification().removeGroupByClause(tokens[i]);
			}
			
			for(int i=0; i<this.getChildCount(); i++)
			{
				DefaultTreeItem item = (DefaultTreeItem)this.getChildAt(i);
				QueryTokens.Group token = (QueryTokens.Group)item.getUserObject();
				builder.browser.getQuerySpecification().addGroupByClause(token);
			}
		}
		
		private void refreshHavingClause(QueryBuilder builder)
		{
			QueryTokens.Condition[] tokens = builder.browser.getQuerySpecification().getHavingClause();
			for(int i=0; i<tokens.length; i++)
			{
				builder.browser.getQuerySpecification().removeHavingClause(tokens[i]);
			}
			
			for(int i=0; i<this.getChildCount(); i++)
			{
				DefaultTreeItem item = (DefaultTreeItem)this.getChildAt(i);
				QueryTokens.Condition token = (QueryTokens.Condition)item.getUserObject();
				builder.browser.getQuerySpecification().addHavingClause(token);
				
				if(i==0)
					token.setAppend(null);
				else if(token.getAppend() == null)
					token.setAppend(_ReservedWords.AND);
			}
		}
	
		private void refreshOrderByClause(QueryBuilder builder)
		{
			QueryTokens.Sort[] tokens = builder.getQueryModel().getOrderByClause();
			for(int i=0; i<tokens.length; i++)
			{
				builder.getQueryModel().removeOrderByClause(tokens[i]);
			}
			
			for(int i=0; i<this.getChildCount(); i++)
			{
				DefaultTreeItem item = (DefaultTreeItem)this.getChildAt(i);
				QueryTokens.Sort token = (QueryTokens.Sort)item.getUserObject();
				builder.getQueryModel().addOrderByClause(token);
			}
		}
	}
	
	protected interface JoinHandler
	{
		public boolean isJoined();
		public void joined();
		public void unjoined();
	}
}