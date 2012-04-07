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

package com.sqleo.environment.io;

import java.io.IOException;
import java.util.Iterator;

import com.sqleo.common.util.Store;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class FileStreamLQY
{
	/* reader */
	public static QueryModel read(String filename)
		throws IOException, ClassNotFoundException
	{
		QueryModel model;
		
		Store qstore = new Store();
		qstore.load(filename);
		
		qstore.mount(Application.ENTRY_INFO);
		String version = qstore.jump("version").get(0).toString();
		if(version.length()>7) version = version.substring(0,7);
		
		if(Double.valueOf(version).doubleValue() < 2006.05)
		{
			model = new QueryModel();
			FileStreamOLD.convert(qstore,model);
		}
		else
		{
			qstore.mount("$QUERY");
			
			Object schema = null;
			if(qstore.canJump("schema")) schema = qstore.jump("schema").get(0);
			
			model = new QueryModel(schema == null ? null : schema.toString());
			model.setQueryExpression(read(qstore,0));
			
			qstore.home();
			qstore.jump("order_by_clause");
			for(Iterator i = qstore.jump().iterator(); i.hasNext();)
			{
				QueryTokens.Sort token = toOrder(i.next());
				model.addOrderByClause(token);
			}
		}
		
		return model;
	}
	
	private static QueryExpression read(Store qstore, int idx)
	{
		qstore.home();
		if(!qstore.canJump("query_expression_" + idx)) return null;
		
		QueryExpression qe = new QueryExpression();
		
		qstore.jump("query_expression_" + idx);
		qstore.jump("quantifier");
		qe.getQuerySpecification().setQuantifier( toShort(qstore.jump().get(0)).shortValue() );
				
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("is_asterisk");
		qe.getQuerySpecification().setAsterisk( toBoolean(qstore.jump().get(0)).booleanValue() );
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("select_list");
		for(Iterator i = qstore.jump().iterator(); i.hasNext();)
		{
			QueryTokens._Expression token = toExpression(i.next());
			qe.getQuerySpecification().addSelectList(token);
		}
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("from_clause");
		for(Iterator i = qstore.jump().iterator(); i.hasNext();)
		{
			QueryTokens._TableReference token = toTableReference(i.next());
			qe.getQuerySpecification().addFromClause(token);
		}
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("where_clause");
		for(Iterator i = qstore.jump().iterator(); i.hasNext();)
		{
			QueryTokens.Condition token = toCondition(i.next());
			qe.getQuerySpecification().addWhereClause(token);
		}
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("group_by_clause");
		for(Iterator i = qstore.jump().iterator(); i.hasNext();)
		{
			QueryTokens.Group token = toGroup(i.next());
			qe.getQuerySpecification().addGroupByClause(token);
		}
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("having_clause");
		for(Iterator i = qstore.jump().iterator(); i.hasNext();)
		{
			QueryTokens.Condition token = toCondition(i.next());
			qe.getQuerySpecification().addHavingClause(token);
		}
		
		qe.setUnion(read(qstore,idx+1));
		
		return qe;
	}
	
	private static Boolean toBoolean(Object o)
	{
		return o == null ? null : (Boolean)o;
	}
	
	private static Integer toInteger(Object o)
	{
		return o == null ? null : (Integer)o;
	}
	
	private static Short toShort(Object o)
	{
		return o == null ? null : (Short)o;
	}
	
	private static String toString(Object o)
	{
		return o == null ? null : (String)o;
	}
	
	private static QueryTokens._Expression toExpression(Object o)
	{
		if(o==null) return new QueryTokens.DefaultExpression(null);
		return o instanceof String ? (QueryTokens._Expression)new QueryTokens.DefaultExpression(o.toString()) : (QueryTokens._Expression)toColumn(o);
	}

	private static QueryTokens._TableReference toTableReference(Object o)
	{
		return ((Object[])o).length == 3 ? (QueryTokens._TableReference)toTable(o) : (QueryTokens._TableReference)toJoin(o);
	}

	private static QueryTokens.Column toColumn(Object o)
	{
		Object[] a = (Object[])o;
		
		QueryTokens.Table table = toTable(a[0]);
		QueryTokens.Column column = new QueryTokens.Column(table,toString(a[1]));
		column.setAlias( toString(a[2]) );
		
		return column;
	}

	private static QueryTokens.Table toTable(Object o)
	{
		Object[] a = (Object[])o;
		
		QueryTokens.Table table = new QueryTokens.Table(toString(a[0]),toString(a[1]));
		table.setAlias(toString(a[2]));

		return table;
	}

	private static QueryTokens.Join toJoin(Object o)
	{
		Object[] a = (Object[])o;
		
		QueryTokens.Condition condition = toCondition(a[1]);
		return new QueryTokens.Join( toInteger(a[0]).intValue(), (QueryTokens.Column)condition.getLeft(), condition.getOperator(), (QueryTokens.Column)condition.getRight());
	}
	
	private static QueryTokens.Condition toCondition(Object o)
	{
		Object[] a = (Object[])o;
		return new QueryTokens.Condition(toString(a[0]),toExpression(a[1]),toString(a[2]),toExpression(a[3]));
	}
	
	private static QueryTokens.Group toGroup(Object o)
	{
		return new QueryTokens.Group(toExpression(o));
	}
	
	private static QueryTokens.Sort toOrder(Object o)
	{
		Object[] a = (Object[])o;
		return new QueryTokens.Sort(toExpression(a[0]), toShort(a[1]).shortValue());
	}
	
	/* writer */
	public static void write(String filename, QueryModel model)
		throws IOException
	{
		Store qstore = new Store();
		
		qstore.mount(Application.ENTRY_INFO);
		qstore.jump("version").add(Application.getVersion());
		
		qstore.mount("$QUERY");
		qstore.jump("schema").add(model.getSchema());
		write(qstore,model.getQueryExpression(),0);
		
		qstore.home();
		qstore.jump("order_by_clause");
		write(qstore,model.getOrderByClause());
		
		qstore.save(filename);
	}

	private static void write(Store qstore, QueryExpression qe, int idx)
	{
		if(qe == null) return;
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("quantifier").add(new Short(qe.getQuerySpecification().getQuantifier()));
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("is_asterisk").add(new Boolean(qe.getQuerySpecification().isAsteriskSet()));
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("select_list");
		write(qstore,qe.getQuerySpecification().getSelectList());
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("from_clause");
		write(qstore,qe.getQuerySpecification().getFromClause());
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("where_clause");
		write(qstore,qe.getQuerySpecification().getWhereClause());
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("group_by_clause");
		write(qstore,qe.getQuerySpecification().getGroupByClause());
		
		qstore.home();
		qstore.jump("query_expression_" + idx);
		qstore.jump("having_clause");
		write(qstore,qe.getQuerySpecification().getHavingClause());
		
		write(qstore,qe.getUnion(),idx+1);
	}

	private static void write(Store qstore, Object[] tokens)
	{
		for(int i=0; i<tokens.length; i++)
		{
			Object row = null;
			
			if(tokens[i] instanceof QueryTokens._Expression)
				row = toArray((QueryTokens._Expression)tokens[i]);
			else if(tokens[i] instanceof QueryTokens.Condition)
				row = toArray((QueryTokens.Condition)tokens[i]);
			else if(tokens[i] instanceof QueryTokens.Join)
				row = toArray((QueryTokens.Join)tokens[i]);
			else if(tokens[i] instanceof QueryTokens.Sort)
				row = toArray((QueryTokens.Sort)tokens[i]);
			else if(tokens[i] instanceof QueryTokens.Group)
				row = toArray((QueryTokens.Group)tokens[i]);
			else if(tokens[i] instanceof QueryTokens.Table)
				row = toArray((QueryTokens.Table)tokens[i]);
			else
				row = tokens[i].toString();
			
			qstore.jump().add(row);
		}
	}
	
	private static Object toArray(QueryTokens._Expression token)
	{
		if(token == null) return null;
		return token instanceof QueryTokens.Column ? toArray((QueryTokens.Column)token) : token.toString();
	}
	
	private static Object toArray(QueryTokens.Column token)
	{
		return new Object[]{toArray(token.getTable()),token.getName(),token.getAlias()};
	}
	
	private static Object toArray(QueryTokens.Condition token)
	{
		return new Object[]{token.getAppend(),toArray(token.getLeft()),token.getOperator(),toArray(token.getRight())};
	}
	
	private static Object toArray(QueryTokens.Join token)
	{
		return new Object[]{new Integer(token.getType()), toArray(token.getCondition())};
	}

	private static Object toArray(QueryTokens.Table token)
	{
		return new Object[]{token.getSchema(),token.getName(),token.getAlias()};
	}

	private static Object toArray(QueryTokens.Group token)
	{
		Object expr = token.getExpression().toString();
		 
		if(token.getExpression() instanceof QueryTokens.Column)
			expr = toArray((QueryTokens.Column)token.getExpression());
		
		return expr;
	}
	
	private static Object toArray(QueryTokens.Sort token)
	{
		Object expr = token.getExpression().toString();
		 
		if(token.getExpression() instanceof QueryTokens.Column)
			expr = toArray((QueryTokens.Column)token.getExpression());
			 
		return new Object[]{expr, new Short(token.isAscending() ? QueryTokens.Sort.ASCENDING : QueryTokens.Sort.DESCENDING)};
	}
}