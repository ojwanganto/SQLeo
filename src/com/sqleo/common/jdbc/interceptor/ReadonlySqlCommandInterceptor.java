/*
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2016 edinhojorge@users.sourceforge.net
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

package com.sqleo.common.jdbc.interceptor;

import java.util.HashSet;
import java.util.Set;

public class ReadonlySqlCommandInterceptor implements SqlCommandInterceptor {

	
	private static final Set<String> permittedOperations = new HashSet<String>();
	
	static {
		permittedOperations.add("SELECT");
		permittedOperations.add("USE");
		permittedOperations.add("SHOW");
		permittedOperations.add("EXPLAIN");
	}
	
	public ReadonlySqlCommandInterceptor() {

	}
	
	@Override
	public boolean allowUpdate(String sql) {

		// Just intercept and validates real sql commands.
		// Any futher error will be catch by API/Vendor driver
		if (sql != null && sql.trim().length() > 0){
			String newSql = sql.replaceFirst("[\\t\\n\\r]+", " ");
			if (permittedOperations.contains(sql.toUpperCase().substring(0, newSql.indexOf(" ")))){
				return true;
			}
			return false;
		}
		return true;
		
	}

}
