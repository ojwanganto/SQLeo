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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import com.sqleo.common.jdbc.interceptor.SqlCommandInterceptor;

public class PreparedStatementWrapper extends AbstractWrapper implements PreparedStatement {

	private PreparedStatement originalPreparedStatement;
	
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return originalPreparedStatement.unwrap(iface);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		// checking to avoid any DML getting in as a query
		isUpdatable(sql);
		return originalPreparedStatement.executeQuery(sql);
	}

	public ResultSet executeQuery() throws SQLException {
		return originalPreparedStatement.executeQuery();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return originalPreparedStatement.isWrapperFor(iface);
	}

	public int executeUpdate(String sql) throws SQLException {
		return originalPreparedStatement.executeUpdate(sql);
	}

	public int executeUpdate() throws SQLException {
		return originalPreparedStatement.executeUpdate();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		originalPreparedStatement.setNull(parameterIndex, sqlType);
	}

	public void close() throws SQLException {
		originalPreparedStatement.close();
	}

	public int getMaxFieldSize() throws SQLException {
		return originalPreparedStatement.getMaxFieldSize();
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		originalPreparedStatement.setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		originalPreparedStatement.setByte(parameterIndex, x);
	}

	public void setMaxFieldSize(int max) throws SQLException {
		originalPreparedStatement.setMaxFieldSize(max);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		originalPreparedStatement.setShort(parameterIndex, x);
	}

	public int getMaxRows() throws SQLException {
		return originalPreparedStatement.getMaxRows();
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		originalPreparedStatement.setInt(parameterIndex, x);
	}

	public void setMaxRows(int max) throws SQLException {
		originalPreparedStatement.setMaxRows(max);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		originalPreparedStatement.setLong(parameterIndex, x);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		originalPreparedStatement.setEscapeProcessing(enable);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		originalPreparedStatement.setFloat(parameterIndex, x);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		originalPreparedStatement.setDouble(parameterIndex, x);
	}

	public int getQueryTimeout() throws SQLException {
		return originalPreparedStatement.getQueryTimeout();
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		originalPreparedStatement.setQueryTimeout(seconds);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		originalPreparedStatement.setBigDecimal(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		originalPreparedStatement.setString(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		originalPreparedStatement.setBytes(parameterIndex, x);
	}

	public void cancel() throws SQLException {
		originalPreparedStatement.cancel();
	}

	public SQLWarning getWarnings() throws SQLException {
		return originalPreparedStatement.getWarnings();
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		originalPreparedStatement.setDate(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		originalPreparedStatement.setTime(parameterIndex, x);
	}

	public void clearWarnings() throws SQLException {
		originalPreparedStatement.clearWarnings();
	}

	public void setCursorName(String name) throws SQLException {
		originalPreparedStatement.setCursorName(name);
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		originalPreparedStatement.setTimestamp(parameterIndex, x);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalPreparedStatement.setAsciiStream(parameterIndex, x, length);
	}

	public boolean execute(String sql) throws SQLException {
		return originalPreparedStatement.execute(sql);
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalPreparedStatement.setUnicodeStream(parameterIndex, x, length);
	}

	public ResultSet getResultSet() throws SQLException {
		return originalPreparedStatement.getResultSet();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalPreparedStatement.setBinaryStream(parameterIndex, x, length);
	}

	public int getUpdateCount() throws SQLException {
		return originalPreparedStatement.getUpdateCount();
	}

	public boolean getMoreResults() throws SQLException {
		return originalPreparedStatement.getMoreResults();
	}

	public void clearParameters() throws SQLException {
		originalPreparedStatement.clearParameters();
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		originalPreparedStatement.setObject(parameterIndex, x, targetSqlType);
	}

	public void setFetchDirection(int direction) throws SQLException {
		originalPreparedStatement.setFetchDirection(direction);
	}

	public int getFetchDirection() throws SQLException {
		return originalPreparedStatement.getFetchDirection();
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		originalPreparedStatement.setObject(parameterIndex, x);
	}

	public void setFetchSize(int rows) throws SQLException {
		originalPreparedStatement.setFetchSize(rows);
	}

	public int getFetchSize() throws SQLException {
		return originalPreparedStatement.getFetchSize();
	}

	public int getResultSetConcurrency() throws SQLException {
		return originalPreparedStatement.getResultSetConcurrency();
	}

	public boolean execute() throws SQLException {
		return originalPreparedStatement.execute();
	}

	public int getResultSetType() throws SQLException {
		return originalPreparedStatement.getResultSetType();
	}

	public void addBatch(String sql) throws SQLException {
		originalPreparedStatement.addBatch(sql);
	}

	public void clearBatch() throws SQLException {
		originalPreparedStatement.clearBatch();
	}

	public void addBatch() throws SQLException {
		originalPreparedStatement.addBatch();
	}

	public int[] executeBatch() throws SQLException {
		return originalPreparedStatement.executeBatch();
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		originalPreparedStatement.setCharacterStream(parameterIndex, reader,
				length);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		originalPreparedStatement.setRef(parameterIndex, x);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		originalPreparedStatement.setBlob(parameterIndex, x);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		originalPreparedStatement.setClob(parameterIndex, x);
	}

	public Connection getConnection() throws SQLException {
		return originalPreparedStatement.getConnection();
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		originalPreparedStatement.setArray(parameterIndex, x);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return originalPreparedStatement.getMetaData();
	}

	public boolean getMoreResults(int current) throws SQLException {
		return originalPreparedStatement.getMoreResults(current);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		originalPreparedStatement.setDate(parameterIndex, x, cal);
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return originalPreparedStatement.getGeneratedKeys();
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		originalPreparedStatement.setTime(parameterIndex, x, cal);
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		isUpdatable(sql);
		return originalPreparedStatement.executeUpdate(sql, autoGeneratedKeys);
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		originalPreparedStatement.setTimestamp(parameterIndex, x, cal);
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		originalPreparedStatement.setNull(parameterIndex, sqlType, typeName);
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		return originalPreparedStatement.executeUpdate(sql, columnIndexes);
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		originalPreparedStatement.setURL(parameterIndex, x);
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		isUpdatable(sql);
		return originalPreparedStatement.executeUpdate(sql, columnNames);
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		return originalPreparedStatement.getParameterMetaData();
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		originalPreparedStatement.setRowId(parameterIndex, x);
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		originalPreparedStatement.setNString(parameterIndex, value);
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		isUpdatable(sql);
		return originalPreparedStatement.execute(sql, autoGeneratedKeys);
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		originalPreparedStatement.setNCharacterStream(parameterIndex, value,
				length);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		originalPreparedStatement.setNClob(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		originalPreparedStatement.setClob(parameterIndex, reader, length);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		isUpdatable(sql);
		return originalPreparedStatement.execute(sql, columnIndexes);
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		originalPreparedStatement.setBlob(parameterIndex, inputStream, length);
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		originalPreparedStatement.setNClob(parameterIndex, reader, length);
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		isUpdatable(sql);
		return originalPreparedStatement.execute(sql, columnNames);
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		originalPreparedStatement.setSQLXML(parameterIndex, xmlObject);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		originalPreparedStatement.setObject(parameterIndex, x, targetSqlType,
				scaleOrLength);
	}

	public int getResultSetHoldability() throws SQLException {
		return originalPreparedStatement.getResultSetHoldability();
	}

	public boolean isClosed() throws SQLException {
		return originalPreparedStatement.isClosed();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		originalPreparedStatement.setPoolable(poolable);
	}

	public boolean isPoolable() throws SQLException {
		return originalPreparedStatement.isPoolable();
	}

	public void closeOnCompletion() throws SQLException {
		originalPreparedStatement.closeOnCompletion();
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		originalPreparedStatement.setAsciiStream(parameterIndex, x, length);
	}

	public boolean isCloseOnCompletion() throws SQLException {
		return originalPreparedStatement.isCloseOnCompletion();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		originalPreparedStatement.setBinaryStream(parameterIndex, x, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		originalPreparedStatement.setCharacterStream(parameterIndex, reader,
				length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		originalPreparedStatement.setAsciiStream(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		originalPreparedStatement.setBinaryStream(parameterIndex, x);
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		originalPreparedStatement.setCharacterStream(parameterIndex, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		originalPreparedStatement.setNCharacterStream(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		originalPreparedStatement.setClob(parameterIndex, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		originalPreparedStatement.setBlob(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		originalPreparedStatement.setNClob(parameterIndex, reader);
	}

	public PreparedStatementWrapper(PreparedStatement statement, ArrayList<SqlCommandInterceptor> sqlCommandInterceptorList) {
		super(sqlCommandInterceptorList);
		this.originalPreparedStatement = statement;
	}

}
