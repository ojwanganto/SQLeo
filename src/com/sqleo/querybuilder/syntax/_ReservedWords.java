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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface _ReservedWords
{
	public static final String ALL		= "ALL";
	public static final String DISTINCT	= "DISTINCT";
	
	public static final String WITH		= "WITH";
	public static final String SHOW		= "SHOW";
	public static final String UPDATE	= "UPDATE";
	public static final String SET		= "SET";
	public static final String SELECT	= "SELECT";
	public static final String FROM		= "FROM";
	public static final String WHERE	= "WHERE";
	public static final String GROUP_BY	= "GROUP BY";
	public static final String HAVING	= "HAVING";
	public static final String UNION	= "UNION";
	public static final String ORDER_BY	= "ORDER BY";
	public static final String LIMIT	= "LIMIT";
	
	public static final String BETWEEN	= "BETWEEN";
	public static final String AS		= "AS";
	public static final String AND		= "AND";
	public static final String OR		= "OR";
	public static final String ON		= "ON";
	
	public static final String INNER_JOIN       = "INNER JOIN";
	public static final String LEFT_OUTER_JOIN  = "LEFT OUTER JOIN";
	public static final String RIGHT_OUTER_JOIN = "RIGHT OUTER JOIN";
	public static final String FULL_OUTER_JOIN  = "FULL OUTER JOIN";

	public static final String JOIN       = "JOIN";
	public static final String LEFT_JOIN  = "LEFT JOIN";
	public static final String RIGHT_JOIN = "RIGHT JOIN";
	public static final String FULL_JOIN  = "FULL JOIN";
	public static final String CROSS_JOIN  = "CROSS JOIN";
	public static final String EXTRACT    = "EXTRACT";
	public static final String IN    = "IN";
	public static final String EXISTS = "EXISTS";

	static List<String> SQL_RESERVED_WORDS = Arrays.asList(ALL, DISTINCT, WITH, SHOW, SELECT, FROM, WHERE, GROUP_BY,
			HAVING, UNION, ORDER_BY,EXISTS, BETWEEN, AS, AND, OR, ON, INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN,
			FULL_OUTER_JOIN, JOIN, INNER_JOIN, LEFT_JOIN, RIGHT_JOIN, FULL_JOIN, CROSS_JOIN, EXTRACT, IN
			,//SQL commands below 
				"DELETE","INSERT","INTO","VALUES","UPDATE","SET","ADD","ALTER","CREATE","DROP","CONSTRAINT","REFERENCES",
				"PRIMARY KEY","FOREIGN KEY","COLUMN","INDEX","TABLE","VIEW");
	
	public static List<String> ALL_RESERVED_WORDS = new ArrayList<String>(SQL_RESERVED_WORDS);
				

}
