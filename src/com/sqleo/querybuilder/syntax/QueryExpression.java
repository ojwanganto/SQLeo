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

package com.sqleo.querybuilder.syntax;

public class QueryExpression implements Cloneable
{
	private QuerySpecification querySpecification;
	private QueryExpression queryUnion;
	
	public QueryExpression()
	{
		querySpecification = new QuerySpecification();
		queryUnion = null;
	}

	public Object clone() throws CloneNotSupportedException
	{
		QueryExpression qe = (QueryExpression)super.clone();
		qe.querySpecification = (QuerySpecification)querySpecification.clone();
		if(queryUnion!=null) qe.queryUnion = (QueryExpression)queryUnion.clone();
		return qe;
	}
	
	public QuerySpecification getQuerySpecification()
	{
		return querySpecification;
	}
	
	public void setQuerySpecification(QuerySpecification qs)
	{
		querySpecification = qs;
	}
	
	public QueryExpression getUnion()
	{
		return queryUnion;
	}
	
	public void setUnion(QueryExpression qe)
	{
		queryUnion = qe;
	}
	
	public String toString(boolean wrap,int offset)
	{
		String syntax = querySpecification.toString(wrap,offset);
		
		if(queryUnion!=null)
			syntax = syntax + 
			(wrap ? SQLFormatter.BREAK + SQLFormatter.indent(offset) : SQLFormatter.SPACE)
			 + _ReservedWords.UNION + 
			(wrap ? SQLFormatter.BREAK : SQLFormatter.SPACE) + queryUnion.toString(wrap, offset);
		
		return syntax;
	}
	
	public String toString()
	{
		return toString(false,0);
	}	
}