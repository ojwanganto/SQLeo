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

public class SubQuery extends QueryExpression implements QueryTokens._Expression, QueryTokens._Alias
{
	public String alias;
	public boolean AS;
	
	public SubQuery()
	{
		super();
		alias=null; AS=false;
	}

	public boolean isAsSet()
	{
		return AS;
	}

	public void setAs(boolean val)
	{
		AS=val;
	}

	public boolean isAliasSet()
	{
		return alias!=null && !alias.equals("");
	}
	
	public String getAlias()
	{
		return alias;
	}
	
	public void setAlias(String alias)
	{
		if(alias!=null)
		{
			alias = alias.replace(' ','_');
			alias = alias.replace('.','_');
		}
		this.alias = alias;
	}
	
	public String toString(boolean wrap,int offset)
	{
		if (this instanceof DerivedTable)
			return "(" + super.toString(wrap,offset) + ")";
		else if(!isAsSet())
			return "(" + super.toString(wrap,offset) + ")" + (isAliasSet() ? SQLFormatter.SPACE + this.getAlias() : "");
		else if(isAsSet())
			return "(" + super.toString(wrap,offset) + ")" + (isAliasSet() ? SQLFormatter.SPACE + _ReservedWords.AS + SQLFormatter.SPACE + this.getAlias() : "");
		else
			return ""; //for future use
	}
	
	public String toString()
	{
		return toString(false,0);
	}
}
