/*
 *
 * SQLeo Visual Query Builder :: java database frontend with join definitions
 * Copyright (C) 2013 anudeepgade@users.sourceforge.net
 *  
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
package com.sqleo.common.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

public class SQLHelper {
	
	public static String[] getColumns(final Connection connection, final String schema, final String table) {
		//First try uppercase table name (For ORACLE,mysql)
		String[] cols = getColumnsInternal(connection, schema, table.toUpperCase());
		if(cols.length == 0){
			// Then try lowercase table name (For postgres)
			cols = getColumnsInternal(connection, schema, table.toLowerCase());
			if(cols.length == 0){
				//Then try given name (For some mixed case datasources)
			    cols = getColumnsInternal(connection, schema, table);
			}
		}
		return cols;
	}

	private static String[] getColumnsInternal(final Connection connection, final String schema, final String table) {
		final Set<String> columns = new TreeSet<String>();
		try {
			String schemaFinal = schema;
			if (schema.endsWith("_USER")) {
				schemaFinal = schemaFinal + "xxx";
				schemaFinal = schemaFinal.split("_USER")[0];
			}
			final String catalog = schemaFinal == null ? null : connection.getCatalog();
			final ResultSet rs = connection.getMetaData().getColumns(catalog, schemaFinal, table, "%");
			if (rs != null) {
				while (rs.next()) {
					final String colName = rs.getString(4);
					if (colName != null) {
						columns.add(colName.toLowerCase());
					}
				}
				rs.close();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return columns.toArray(new String[columns.size()]);
	}

	public static String getSchemaFromUser(final String chKey) {
		String schemaPrefix = null;
		final String[] splitKey = chKey.split("@");
		if (splitKey.length >= 2) {
			schemaPrefix = splitKey[1];
			schemaPrefix = schemaPrefix.toUpperCase();
		}
		return schemaPrefix;
	}

}
