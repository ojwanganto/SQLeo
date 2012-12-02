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

public interface _ReservedWords
{
	public static final String ALL		= "ALL";
	public static final String DISTINCT	= "DISTINCT";
	
	public static final String WITH		= "WITH";
	public static final String SELECT	= "SELECT";
	public static final String FROM		= "FROM";
	public static final String WHERE	= "WHERE";
	public static final String GROUP_BY	= "GROUP BY";
	public static final String HAVING	= "HAVING";
	public static final String UNION	= "UNION";
	public static final String ORDER_BY	= "ORDER BY";
	
	public static final String BETWEEN	= "BETWEEN";
	public static final String AS		= "AS";
	public static final String AND		= "AND";
	public static final String OR		= "OR";
	public static final String ON		= "ON";
	
	public static final String INNER_JOIN       = "INNER JOIN";
	public static final String LEFT_OUTER_JOIN  = "LEFT OUTER JOIN";
	public static final String RIGHT_OUTER_JOIN = "RIGHT OUTER JOIN";
	public static final String FULL_OUTER_JOIN  = "FULL OUTER JOIN";
}
