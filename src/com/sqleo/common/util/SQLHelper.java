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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.StyleContext.SmallAttributeSet;

import com.sqleo.common.jdbc.ConnectionAssistant;
import com.sqleo.common.jdbc.ConnectionHandler;
import com.sqleo.environment.Application;
import com.sqleo.querybuilder.QueryBuilder;
import com.sqleo.querybuilder.syntax.QueryTokens.Column;
import com.sqleo.querybuilder.syntax.QueryTokens.Table;

public class SQLHelper {
	
	private static final String JOINCOLUMNS = "joincolumns";
	private static final String COLUMNS = "columns";

	public static String[] getColumns(final String chKey, final String schema, final String table) {
		if(null == chKey || null == table || table.isEmpty()){
			return new String[0];
		}
		final Connection connection = ConnectionAssistant.getHandler(chKey).get();
		return getColumns(chKey, connection, schema, table);
	}
	public static String[] getColumns(final String chKey,final Connection connection, final String schema, final String table) {
		final String schemaFinal = getFinalSchema(schema);
		final String cacheKey = getCacheKey(chKey, schemaFinal, table, COLUMNS);
		final Object cached = getColumnCache(cacheKey);
		if(cached!=null){
			return (String[]) cached;
		}
		//First try uppercase table name (For ORACLE,mysql)
		String[] cols = getColumnsInternal(connection, schemaFinal, table.toUpperCase());
		if(cols.length == 0){
			// Then try lowercase table name (For postgres)
			cols = getColumnsInternal(connection, schemaFinal, table.toLowerCase());
			if(cols.length == 0){
				//Then try given name (For some mixed case datasources)
			    cols = getColumnsInternal(connection, schemaFinal, table);
			}
		}
		if(cols.length !=0) {
			putColumnCache(cacheKey, cols);
		}
		return cols;
	}

	private static String[] getColumnsInternal(final Connection connection, final String schemaFinal, final String table) {
		final Set<String> columns = new TreeSet<String>();
		try {
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
	
	public static List<List<String>> getJoinColumns(final String chKey,final Connection connection, final String schema,
			final String fkTable, final String pkTable) {
		final String schemaFinal = getFinalSchema(schema);
		final String cacheKey = getCacheKey(chKey, schemaFinal, fkTable, pkTable+","+JOINCOLUMNS);
		final Object cached = getColumnCache(cacheKey);
		if(cached!=null){
			return  (List<List<String>>) cached;
		}
		//First try uppercase table name (For ORACLE,mysql)
		List<List<String>> joinColumns = getJoinColumnsInternal(connection, schemaFinal, fkTable.toUpperCase(),pkTable);
		if(joinColumns.isEmpty()){
			// Then try lowercase table name (For postgres)
			joinColumns = getJoinColumnsInternal(connection, schemaFinal, fkTable.toLowerCase(),pkTable);
			if(joinColumns.isEmpty()){
				//Then try given name (For some mixed case datasources)
				joinColumns = getJoinColumnsInternal(connection, schemaFinal, fkTable,pkTable);
			}
		}
		if(!joinColumns.isEmpty()){
			putColumnCache(cacheKey, joinColumns);
		}
		return joinColumns;
	}
	
	private static void putColumnCache(final String key, final Object content){
		Application.session.putColumnCache(key, content);
	}
	
	private static Object getColumnCache(final String key){
		return Application.session.getColumnCache(key);
	}
	
	private static String getCacheKey(final String chKey,final String schema,final String tableName,final String keyHeader){
		return chKey + "," + schema + "," + tableName.toLowerCase()+","+keyHeader;
	}
	
	
	
	private static List<List<String>> getJoinColumnsInternal(final Connection connection, final String schemaFinal,
			final String fkTable, final String pkTable) {
		final List<List<String>> joinColumns = new ArrayList<List<String>>();
		try {
			final String catalog = schemaFinal == null ? null : connection.getCatalog();
			final ResultSet rs = connection.getMetaData().getImportedKeys(catalog, schemaFinal, fkTable);
			if (rs != null) {
				while (rs.next()) {
					final String pkTableName = rs.getString(3);
					if (pkTableName != null && pkTableName.compareToIgnoreCase(pkTable)==0) {
						final String pkColName = rs.getString(4);
						final String fkColName = rs.getString(8);
						final List<String> temp = Arrays.asList(
								pkColName.toLowerCase(),
								fkColName.toLowerCase());
						joinColumns.add(temp);
					}
					
				}
				rs.close();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return joinColumns;
	}
	
	public static String[] getExportedColumns(final Connection connection, final String schema, final String table) {
		final Set<String> columns = new TreeSet<String>();
		try {
			final String schemaFinal = getFinalSchema(schema);
			final String catalog = schemaFinal == null ? null : connection.getCatalog();
			final ResultSet rs = connection.getMetaData().getExportedKeys(catalog, schemaFinal, table);
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

	private static String getFinalSchema(final String schema) {
		String schemaFinal = schema;
		if (schema!=null && schema.endsWith("_USER")) {
			schemaFinal = schemaFinal + "xxx";
			schemaFinal = schemaFinal.split("_USER")[0];
		}
		return schemaFinal;
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

	public static String getRowValue(final ResultSet rs, final int index) throws SQLException {
		final String value = rs.getString(index);
		if (null == value) {
			// try with object (blob,clob etc...types)
			final Object obj = rs.getObject(index);
			return obj != null ? obj.toString() : null;
		}
		return value;
	}
	
	public static String getSQLeoFunctionQuery(final String query,final String keych) throws SQLException {

		final StringBuffer result = new StringBuffer();
		final ConnectionHandler ch = ConnectionAssistant.getHandler(keych);
		final Statement stmt = ch.get().createStatement();

		// ********* SQLeoPivot **************
		// to do:
		// - take table name from syntax not from group(1) --> (remove table as first param)
		// - use syntax where clause to retrieve pivot values

		final Pattern p = Pattern.compile("SQLeoPivot\\((.*?),(.*?),(.*?),(.*?)\\)");
		final Matcher m = p.matcher(query);
		
		// Parameters : 
		// 1. [schema.]table
		// 2. Pivot Column (can be a concat results like col1||'-'||col2)
		// 3. Aggregate function in COUNT,AVG,SUM,MIN,MAX
		// 4. Aggregated Column

		Integer NbPivotValues = 0;
//		try{
			while (m.find()) {
				System.out.println("PivotSQL params: " + m.group(1) + " / "+ m.group(2)+" / "+ m.group(3)+" / "+ m.group(4));
				ResultSet pivots = stmt.executeQuery("SELECT distinct " + m.group(2) + " FROM " + m.group(1));
				StringBuilder val = new StringBuilder();
				while (pivots.next()) {
					val.append( m.group(3) + "(case " + m.group(2) + " when '" + pivots.getString(1) + "' then "+ m.group(4) +" else null end) " 
							+ "as \"" + pivots.getString(1)  + "\",");
					NbPivotValues++;
				}
				if(val.length() > 0) 
					val=val.deleteCharAt(val.length()-1); 
				else
					val=val.append(" null "); 

				m.appendReplacement(result, val.toString());
				System.out.println("PivotSQL text: " + val.toString() );
				System.out.println("PivotSQL length: " + val.length() );
			}
//		}finally{
//			stmt.close();
//		}
		m.appendTail(result);

		// ********* SQLeoGroupConcat **************
		// to do:
		// - take table name from syntax not from group(1) --> (remove table as first param)
		// - use syntax where clause to retrieve pivot values



		final Pattern p1 = Pattern.compile("SQLeoGroupConcat\\((.*?),(.*?),\\s*(\'.*?\')\\s*\\)");
		final Matcher m1 = p1.matcher(result);
		
		// Parameters : 
		// 1. [schema.]table
		// 2. Grouping Column 
		// 3. Separator char

		final StringBuffer result1 = new StringBuffer();
//		final ConnectionHandler ch = ConnectionAssistant.getHandler(keych);
//		final Statement stmt = ch.get().createStatement();
//		try{
			while (m1.find()) {
				System.out.println("GroupConcat params: " + m1.group(1) + " / "+ m1.group(2)+" / "+ m1.group(3));
				ResultSet pivots = stmt.executeQuery("SELECT distinct " + m1.group(2) + " FROM " + m1.group(1));
				StringBuilder val = new StringBuilder();
				while (pivots.next()) {
					val.append( "MAX(case " + m1.group(2) + " when '" + pivots.getString(1) + "' then "+ m1.group(2) + "||" + m1.group(3) +" else '' end) ||");
				}
				if(val.length() > 0) 
					for (int i=0;i <  2; i++) {val=val.deleteCharAt(val.length()-1);} 
				else
					val=val.append(" null "); 

				// RTRIM is not database independent : not deployed  
				// val.insert(0, "RTRIM(").append("," + m1.group(3) + ")");
				m1.appendReplacement(result1, val.toString());
				System.out.println("GroupConcatSQL: " + val.toString() );
				System.out.println("GroupConcatSQL length: " + val.length() );
			}
//		}finally{
			stmt.close();
//		}
		m1.appendTail(result1);

		final String pivotQuery = result1.toString().replace("NbPivotValues",NbPivotValues.toString());
		//System.out.println("Pivot SQL: " + pivotQuery ); 
		return pivotQuery.isEmpty()?query:pivotQuery;
	}
	
	/**
	 * Generates a query statement with distinct and limit depending on dbms
	 * 
	 * Default option is this one
	 * SQL Server & HSQLDB
	 * Maybe sybase (http://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc00801.1510/html/iqrefso/X315771.htm)
	 * 
	 * SELECT TOP [limit] [col] FROM [table]
	 * 
	 * 
	 * MySQL & PostgreSQL & MariaDB
	 * 
	 * SELECT [col] FROM [table] LIMIT [limit]
	 * 
	 * 
	 * Oracle
	 * 
	 * SELECT [col] FROM [table] WHERE ROWNUM < [limit]
	 * 
	 * 
	 * Firebird
	 * SELECT FIRST [limit] [col] FROM [table]
	 * 
	 * @param t
	 * @param c
	 * @param limit
	 * @return
	 */
	public static String createDistinctWithLimitQueryByDBMS(String handlerKey, Table t, Column c, int limit, String whereClause) 
			throws SQLException {
		StringBuilder query = new StringBuilder();
		
		String databaseName = ConnectionAssistant.getHandler(handlerKey).get().getMetaData().getDatabaseProductName().toLowerCase();
		
		String position1 = "";
		String position2 = "";
		String position3 = "";
		String position4 = "";

		if (databaseName.matches("mysql*|mariadb*|postgre*")){
			position3 = "LIMIT";
			position4 = String.valueOf(limit);
		} else if (databaseName.matches("oracle*")){
			if (whereClause == null || whereClause.trim().equals(""))
				position3 = " WHERE ROWNUM <";
			else
				position3 = " AND ROWNUM <";
			position4 = String.valueOf(limit);
		} else if (databaseName.matches("firebird*")){
			position1 = "FIRST";
			position2 = String.valueOf(limit);
		} else {
			position1 = "TOP";
			position2 = String.valueOf(limit);
		}

		query.append("SELECT ");
		query.append(position1);
		query.append(" ");
		query.append(position2);
		query.append(" DISTINCT ");
		query.append(c.getName());
		query.append(" FROM ");
		query.append(t.getName());
		if (whereClause != null && whereClause.trim().equals("") == false){
			query.append(" WHERE ");
			query.append(whereClause);
		}
		query.append(" ");
		query.append(position3);
		query.append(" ");
		query.append(position4);

		return query.toString();
	}
	
	/**
	 * @see #createDistinctWithLimitQueryByDBMS(Table, Column, int, String)
	 */
	public static String createDistinctWithLimitQueryByDBMS(String handlerKey, Table t, Column c, int limit)
			throws SQLException {
		return createDistinctWithLimitQueryByDBMS(handlerKey, t, c, limit, null);
	}	
	
	/**
	 * Close jdbc objects when not null
	 * @param c
	 * @param s
	 * @param rs
	 */
	public static void closeObjects(Connection c, Statement s, ResultSet rs){

		
		if (rs != null){
			try {
				rs.close();
			} catch (SQLException e) {
				Application.println(e, false);
			}
		}
		if (s != null){
			try {
				s.close();
			} catch (SQLException e) {
				Application.println(e, false);
			}
		}
		
		if (c != null){
			try {
				c.close();
			} catch (SQLException e) {
				Application.println(e, false);
			}
		}
	}

}






















