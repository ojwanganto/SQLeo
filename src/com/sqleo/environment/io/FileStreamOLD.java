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

import java.util.Iterator;

import com.sqleo.common.util.Store;
import com.sqleo.querybuilder.QueryModel;
import com.sqleo.querybuilder.syntax.QueryExpression;
import com.sqleo.querybuilder.syntax.QuerySpecification;
import com.sqleo.querybuilder.syntax.QueryTokens;


public class FileStreamOLD
{
	public static void convert(Store qstore,QueryModel model)
	{
		QueryExpression qe = model.getQueryExpression();
		
		qstore.mount("$QUERY");
		for(int i=0; qstore.canJump("qry" + i); i++)
		{
			if(i == 0)
			{
				qstore.home();
				qstore.jump("qry" + i);
				for(Iterator it = qstore.jump("order").iterator(); it.hasNext();)
				{
					Object[] row = (Object[])it.next();
				
					QueryTokens.Sort qtoken = new QueryTokens.Sort(new QueryTokens.DefaultExpression(row[0].toString()),((Boolean)row[1]).booleanValue());
					model.addOrderByClause(qtoken);
				}
			}
			else
				qe.setUnion(qe = new QueryExpression());
			
			load("qry"+i, qstore, qe.getQuerySpecification());
			qstore.home();
		}
	}

	private static void load(String sub, Store qstore, QuerySpecification qs)
	{
		qstore.home();
		if(sub!=null) qstore.jump(sub);
		boolean distinct = ((Boolean)qstore.jump("distinct").get(0)).booleanValue();
		qs.setQuantifier(distinct ? QuerySpecification.DISTINCT : QuerySpecification.NONE);

		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("tables").iterator(); i.hasNext();)
		{
			Object[] row = (Object[])i.next();
				
			QueryTokens.Table table = new QueryTokens.Table( (row[0]==null?null:row[0].toString()) ,row[1].toString());
			if(row[2]!=null) table.setAlias(row[2].toString());
			
			qs.addFromClause(table);
		}

		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("expressions").iterator(); i.hasNext();)
		{
			Object row = i.next();
			if(row instanceof Object[])
			{
				Object[] rowdata = (Object[])row;
				QueryTokens.Table table = new QueryTokens.Table( (rowdata[0]==null?null:rowdata[0].toString()) ,rowdata[1].toString());
					
				String column = rowdata[2].toString();
				if(rowdata[3]!=null) table.setAlias(rowdata[3].toString());
				
				qs.addSelectList(new QueryTokens.Column(table,column));
			}
			else
			{
				qs.addSelectList(new QueryTokens.DefaultExpression(row.toString()));
			}
		}
		
		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("joins").iterator(); i.hasNext();)
		{
			Object[] row = (Object[])i.next();
				
			int idx = 0;
			int type = ((Integer)row[idx]).intValue();
				
			String lschema = row[++idx] == null ? null : row[idx].toString();
			String ltable = row[++idx].toString();
			String lalias = null;
			if(row.length == 10 && row[++idx] != null)
				lalias = row[idx].toString();
			String lname = row[++idx].toString();
				
			String operator = row[++idx].toString();
				
			String rschema = row[++idx] == null ? null : row[idx].toString();
			String rtable = row[++idx].toString();
			String ralias = null;
			if(row.length == 10 && row[++idx] != null)
				ralias = row[idx].toString();				
			String rname = row[++idx].toString();
				
			QueryTokens.Table ltoken = new QueryTokens.Table(lschema,ltable);
			if(lalias != null) ltoken.setAlias(lalias);
			QueryTokens.Column lcolumn = new QueryTokens.Column(ltoken,lname);
				
			QueryTokens.Table rtoken = new QueryTokens.Table(rschema,rtable);
			if(ralias != null) rtoken.setAlias(ralias);
			QueryTokens.Column rcolumn = new QueryTokens.Column(rtoken,rname);
				
			qs.addFromClause(new QueryTokens.Join(type,lcolumn,operator,rcolumn));
		}
		
		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("where").iterator(); i.hasNext();)
		{
			Object[] row = (Object[])i.next();
			
			QueryTokens.Condition qtoken = new QueryTokens.Condition( (row[0]==null?null:row[0].toString()), new QueryTokens.DefaultExpression(row[1].toString()),row[2].toString(),new QueryTokens.DefaultExpression(row[3].toString()));
			qs.addWhereClause(qtoken);
		}

		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("group").iterator(); i.hasNext();)
		{
			qs.addGroupByClause(new QueryTokens.Group(i.next().toString()));
		}
			
		qstore.home();
		if(sub!=null) qstore.jump(sub);
		for(Iterator i = qstore.jump("having").iterator(); i.hasNext();)
		{
			Object[] row = (Object[])i.next();
				
			QueryTokens.Condition qtoken = new QueryTokens.Condition( (row[0]==null?null:row[0].toString()), new QueryTokens.DefaultExpression(row[1].toString()),row[2].toString(),new QueryTokens.DefaultExpression(row[3].toString()));
			qs.addHavingClause(qtoken);
		}
	}
}