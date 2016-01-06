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
package com.sqleo.common.jdbc.wrapper;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 
 * @author eder
 * Wrapper class for Connection from JDBC API to add some
 * interceptor functionalities.
 */
public class ConnectionWrapper extends AbstractWrapper implements Connection {

	private Connection originalConnection;
	
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return originalConnection.isWrapperFor(iface);
	}

	public Statement createStatement() throws SQLException {
		
		Statement statement = originalConnection.createStatement();
		return new StatementWrapper(statement, getSqlCommandInterceptorList());
	}


	public PreparedStatement prepareStatement(String sql) throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public CallableStatement prepareCall(String sql) throws SQLException {
		isUpdatable(sql);
		CallableStatement callableStatement = originalConnection.prepareCall(sql);
		return new CallableStatementWrapper(callableStatement, getSqlCommandInterceptorList());
	}


	public String nativeSQL(String sql) throws SQLException {
		return originalConnection.nativeSQL(sql);
	}


	public void setAutoCommit(boolean autoCommit) throws SQLException {
		originalConnection.setAutoCommit(autoCommit);
	}


	public boolean getAutoCommit() throws SQLException {
		return originalConnection.getAutoCommit();
	}


	public void commit() throws SQLException {
		originalConnection.commit();
	}


	public void rollback() throws SQLException {
		originalConnection.rollback();
	}


	public void close() throws SQLException {
		originalConnection.close();
	}


	public boolean isClosed() throws SQLException {
		return originalConnection.isClosed();
	}


	public DatabaseMetaData getMetaData() throws SQLException {
		return originalConnection.getMetaData();
	}


	public void setReadOnly(boolean readOnly) throws SQLException {
		originalConnection.setReadOnly(readOnly);
	}


	public boolean isReadOnly() throws SQLException {
		return originalConnection.isReadOnly();
	}


	public void setCatalog(String catalog) throws SQLException {
		originalConnection.setCatalog(catalog);
	}


	public String getCatalog() throws SQLException {
		return originalConnection.getCatalog();
	}


	public void setTransactionIsolation(int level) throws SQLException {
		originalConnection.setTransactionIsolation(level);
	}


	public int getTransactionIsolation() throws SQLException {
		return originalConnection.getTransactionIsolation();
	}


	public SQLWarning getWarnings() throws SQLException {
		return originalConnection.getWarnings();
	}


	public void clearWarnings() throws SQLException {
		originalConnection.clearWarnings();
	}


	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		
		Statement statement = originalConnection.createStatement(resultSetType,
				resultSetConcurrency);
		return new StatementWrapper(statement, getSqlCommandInterceptorList());

	}


	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		isUpdatable(sql);
		CallableStatement callableStatement = originalConnection.prepareCall(sql, resultSetType,
				resultSetConcurrency);
		return new CallableStatementWrapper(callableStatement, getSqlCommandInterceptorList());
	}


	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return originalConnection.getTypeMap();
	}


	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		originalConnection.setTypeMap(map);
	}


	public void setHoldability(int holdability) throws SQLException {
		originalConnection.setHoldability(holdability);
	}


	public int getHoldability() throws SQLException {
		return originalConnection.getHoldability();
	}


	public Savepoint setSavepoint() throws SQLException {
		return originalConnection.setSavepoint();
	}


	public Savepoint setSavepoint(String name) throws SQLException {
		return originalConnection.setSavepoint(name);
	}


	public void rollback(Savepoint savepoint) throws SQLException {
		originalConnection.rollback(savepoint);
	}


	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		originalConnection.releaseSavepoint(savepoint);
	}


	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		
		Statement statement = originalConnection.createStatement(resultSetType,
				resultSetConcurrency, resultSetHoldability);
		return new StatementWrapper(statement, getSqlCommandInterceptorList());

	}


	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		isUpdatable(sql);
		CallableStatement callableStatement = originalConnection.prepareCall(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
		return new CallableStatementWrapper(callableStatement, getSqlCommandInterceptorList());
	}


	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql, autoGeneratedKeys);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql, columnIndexes);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		isUpdatable(sql);
		PreparedStatement preparedStatement = originalConnection.prepareStatement(sql, columnNames);
		return new PreparedStatementWrapper(preparedStatement, getSqlCommandInterceptorList());
	}


	public Clob createClob() throws SQLException {
		return originalConnection.createClob();
	}


	public Blob createBlob() throws SQLException {
		return originalConnection.createBlob();
	}


	public NClob createNClob() throws SQLException {
		return originalConnection.createNClob();
	}


	public SQLXML createSQLXML() throws SQLException {
		return originalConnection.createSQLXML();
	}


	public boolean isValid(int timeout) throws SQLException {
		return originalConnection.isValid(timeout);
	}


	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		originalConnection.setClientInfo(name, value);
	}


	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		originalConnection.setClientInfo(properties);
	}


	public String getClientInfo(String name) throws SQLException {
		return originalConnection.getClientInfo(name);
	}


	public Properties getClientInfo() throws SQLException {
		return originalConnection.getClientInfo();
	}


	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return originalConnection.createArrayOf(typeName, elements);
	}


	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return originalConnection.createStruct(typeName, attributes);
	}


	public void setSchema(String schema) throws SQLException {
		originalConnection.setSchema(schema);
	}


	public String getSchema() throws SQLException {
		return originalConnection.getSchema();
	}


	public void abort(Executor executor) throws SQLException {
		originalConnection.abort(executor);
	}


	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		originalConnection.setNetworkTimeout(executor, milliseconds);
	}


	public int getNetworkTimeout() throws SQLException {
		return originalConnection.getNetworkTimeout();
	}


	public ConnectionWrapper(Connection conn) {
		this.originalConnection = conn;
	}
	
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return originalConnection.unwrap(iface);
	}


}
