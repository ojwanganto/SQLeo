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

import java.util.ArrayList;
import java.util.HashMap;

import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QueryTokens;
import com.sqleo.querybuilder.syntax.SQLFormatter;
import com.sqleo.querybuilder.syntax._ReservedWords;


public class QueryModel implements Cloneable
{
	private QueryExpression queryExpression;
	private ArrayList orderClause;
	private HashMap extrasMap = new HashMap();
	
	private String schema;
	
	public QueryModel()
	{
		this(null);
	}
	
	public QueryModel(String schema)
	{
		this.schema = schema;
		
		queryExpression = new QueryExpression();
		orderClause = new ArrayList();
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		QueryModel qm = (QueryModel)super.clone();
		qm.queryExpression = (QueryExpression)queryExpression.clone();
		qm.orderClause = (ArrayList)orderClause.clone();
		return qm;
	}
	
	public String getSchema()
	{
		return schema == null ? null : new String(schema);
	}

	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	public void addOrderByClause(QueryTokens.Sort token)
	{
		orderClause.add(token);
	}
	
	public QueryTokens.Sort[] getOrderByClause()
	{
		QueryTokens.Sort[] a = new QueryTokens.Sort[orderClause.size()];
		return (QueryTokens.Sort[])orderClause.toArray(a);
	}
	
	public void removeOrderByClause(QueryTokens.Sort token)
	{
		orderClause.remove(token);
	}
	
	public void removeAllOrderByClauses(){
		orderClause.clear();
	}
	
	public QueryExpression getQueryExpression()
	{
		return queryExpression;
	}
		
	public void setQueryExpression(QueryExpression qe)
	{
		queryExpression = qe;
	}
	
	public String toString(boolean wrap)
	{
		String syntax = queryExpression.toString(wrap,0);
		
		if(orderClause.size() > 0)
			syntax = syntax + (wrap ? SQLFormatter.BREAK : SQLFormatter.SPACE) + _ReservedWords.ORDER_BY + (wrap ? SQLFormatter.BREAK : SQLFormatter.SPACE) + SQLFormatter.concat(this.getOrderByClause(),wrap, 0);
		
		return syntax;
	}
	
	public String toString()
	{
		return toString(false);
	}

	public void resetExtrasMap(HashMap newExtrasMap) {
		if(!newExtrasMap.isEmpty()){
			this.extrasMap.clear();
			this.extrasMap.putAll(newExtrasMap);
		}
	}

	public HashMap getExtrasMap() {
		return extrasMap;
	}
	
/*	public static void main(String[] args)
	{
		DerivedTable dt = new DerivedTable();
		dt.getQuerySpecification().addSelectList(new QueryTokens.DefaultExpression("hello"));
		
		QueryModel qm = new QueryModel();
		qm.getQueryExpression().getQuerySpecification().addFromClause(dt);
		
		System.out.println(qm);
	}*/
}