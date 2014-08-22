/*
 *
 * Modified by SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2012 anudeepgade@users.sourceforge.net
 * 
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

import java.util.ArrayList;

public class QuerySpecification implements Cloneable
{
	public static final short ALL		= 1;
	public static final short DISTINCT	= 2;
	public static final short NONE		= 0;
	
	private short quantifier = NONE;
	private boolean asterisk = false;
	
	private ArrayList selectList;
	private ArrayList fromClause;
	private ArrayList whereClause;
	private ArrayList groupClause;
	private ArrayList havingClause;
	
	public QuerySpecification()
	{
		selectList = new ArrayList();
		fromClause = new ArrayList();
		whereClause = new ArrayList();
		groupClause = new ArrayList();
		havingClause = new ArrayList();
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		QuerySpecification qs = (QuerySpecification)super.clone();
		qs.selectList = (ArrayList)selectList.clone();
		qs.fromClause = (ArrayList)fromClause.clone();
		qs.whereClause = (ArrayList)whereClause.clone();
		qs.groupClause = (ArrayList)groupClause.clone();
		qs.havingClause = (ArrayList)havingClause.clone();
		return qs;
	}
	
	public void addSelectList(QueryTokens._Expression token)
	{
		asterisk = false;
		selectList.add(token);
	}
	
	public void addFromClause(QueryTokens._TableReference token)
	{
		fromClause.add(token);
	}
	
	public void addWhereClause(QueryTokens.Condition token)
	{
		whereClause.add(token);
	}
	
	public void addGroupByClause(QueryTokens.Group token)
	{
		groupClause.add(token);
	}
	
	public void addHavingClause(QueryTokens.Condition token)
	{
		havingClause.add(token);
	}
	
	public QueryTokens._Expression[] getSelectList()
	{
		QueryTokens._Expression[] a = new QueryTokens._Expression[selectList.size()];
		return (QueryTokens._Expression[])selectList.toArray(a);
	}
	
	public QueryTokens._TableReference[] getFromClause()
	{
		QueryTokens._TableReference[] a = new QueryTokens._TableReference[fromClause.size()];
		
		/* necessario, problemi con subqueries */
		return (QueryTokens._TableReference[])fromClause.toArray(a);
		/*
		for(int i=0; i<a.length;i++)
		{
			a[i] = (QueryTokens._TableReference)fromClause.get(i);
		}
		return a;
		*/
	}
	
	public QueryTokens.Condition[] getWhereClause()
	{
		QueryTokens.Condition[] a = new QueryTokens.Condition[whereClause.size()];
		return (QueryTokens.Condition[])whereClause.toArray(a);
	}
	
	public QueryTokens.Group[] getGroupByClause()
	{
		QueryTokens.Group[] a = new QueryTokens.Group[groupClause.size()];
		return (QueryTokens.Group[])groupClause.toArray(a);
	}
	
	public QueryTokens.Condition[] getHavingClause()
	{
		QueryTokens.Condition[] a = new QueryTokens.Condition[havingClause.size()];
		return (QueryTokens.Condition[])havingClause.toArray(a);
	}
	
	public void removeSelectList(QueryTokens._Expression token)
	{
		selectList.remove(token);
	}
	
	public void removeFromClause(QueryTokens._TableReference token)
	{
		// fromClause.remove(token); It seems to not work as hashcodes are different for same objects
		//	System.out.println(fromClause.get(i).hashCode());
		//	System.out.println(token.hashCode());
		//	System.out.println(fromClause.get(i).toString().equals(token.toString()));
		for(int i=0; i < fromClause.size(); i++){
			if(fromClause.get(i).toString().equals(token.toString())){
				fromClause.remove(i);
				break;
			}
		}
		
	}
	public void removeFromClause(final int index)
	{
		fromClause.remove(index);
	}

	public void removeWhereClause(QueryTokens.Condition token)
	{
		whereClause.remove(token);
	}
	
	public void removeGroupByClause(QueryTokens.Group token)
	{
		groupClause.remove(token);
	}
	
	public void removeHavingClause(QueryTokens.Condition token)
	{
		havingClause.remove(token);
	}

	public void setSelectList(int idx,QueryTokens._Expression token)
	{
		asterisk = false;
		selectList.set(idx,token);
	}
	
	public void setFromClause(int idx,QueryTokens._TableReference token)
	{
		fromClause.set(idx,token);
	}
	
	public void setWhereClause(int idx,QueryTokens.Condition token)
	{
		whereClause.set(idx,token);
	}
	
	public void setGroupByClause(int idx,QueryTokens.Group token)
	{
		groupClause.set(idx,token);
	}
	
	public void setHavingClause(int idx,QueryTokens.Condition token)
	{
		havingClause.set(idx,token);
	}
	
	public boolean isAsteriskSet()
	{
		return asterisk;
	}
	 
	public void setAsterisk(boolean b)
	{
		asterisk = b;
	}

	public short getQuantifier()
	{
		return quantifier;
	} 
		
	public void setQuantifier(short q)
	{
		quantifier = q;
	}
	
	public String toString(boolean wrap,int offset)
	{
		String concat = (wrap ? String.valueOf(SQLFormatter.BREAK) : String.valueOf(SQLFormatter.SPACE));
		String syntax = _ReservedWords.SELECT;
		//String syntax = wrap ? (offset>0?concat:"") + SQLFormatter.indent(offset) +_ReservedWords.SELECT : _ReservedWords.SELECT;
		
		if(quantifier == ALL)
			syntax = syntax + SQLFormatter.SPACE + _ReservedWords.ALL;
		else if(quantifier == DISTINCT)
			syntax = syntax + SQLFormatter.SPACE + _ReservedWords.DISTINCT;
		
		if(selectList.size() > 0)
			syntax = syntax + concat + SQLFormatter.concat(this.getSelectList(),wrap,offset);
		else if(isAsteriskSet())
			syntax = syntax + SQLFormatter.SPACE + "*";
		
		String indentPrefix = SQLFormatter.indent(offset) + (offset>0? SQLFormatter.SPACE : "");
		if(fromClause.size() > 0)
			syntax = syntax + concat + 
				(wrap ? indentPrefix + _ReservedWords.FROM : _ReservedWords.FROM)
				+ concat + SQLFormatter.concat(this.getFromClause(),wrap, offset,  this);

		if(whereClause.size() > 0)
			syntax = syntax + concat + 
				(wrap ? indentPrefix + _ReservedWords.WHERE : _ReservedWords.WHERE)
					+ concat + SQLFormatter.concat(this.getWhereClause(),wrap, offset);

		if(groupClause.size() > 0)
			syntax = syntax + concat + 
				(wrap ? indentPrefix + _ReservedWords.GROUP_BY : _ReservedWords.GROUP_BY)
					+ concat + SQLFormatter.concat(this.getGroupByClause(),wrap, offset);

		if(havingClause.size() > 0)
			syntax = syntax + concat + 
				(wrap ? indentPrefix + _ReservedWords.HAVING : _ReservedWords.HAVING)
				   	+ concat + SQLFormatter.concat(this.getHavingClause(),wrap, offset);
		
		return syntax;
	}
	
	public String toString()
	{
		return toString(false,0);
	}	
}