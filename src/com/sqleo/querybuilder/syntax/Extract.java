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

public class Extract implements QueryTokens._Expression
{
	private String word;
	private String expr;
	private String alias;
	
	public Extract()
	{
		word=new String(); expr=new String(); alias=new String();
	}

	public void setExtract(String w, String xpr, String a)
	{
		word=w; expr=xpr; alias=a;
	}

	public boolean isAliasSet()
	{
		return alias!=null && !alias.equals("");
	}
	
	public String toString()
	{
		if(isAliasSet())
			return "EXTRACT (" + word + SQLFormatter.SPACE + _ReservedWords.FROM + SQLFormatter.SPACE + expr +") " + alias;
		else 
			return "EXTRACT (" + word + SQLFormatter.SPACE + _ReservedWords.FROM + SQLFormatter.SPACE + expr +")";
	}
}
