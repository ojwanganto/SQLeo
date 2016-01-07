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
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
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
import java.util.Map;

import com.sqleo.common.jdbc.interceptor.SqlCommandInterceptor;

public class CallableStatementWrapper extends AbstractWrapper implements CallableStatement {

	private CallableStatement originalCallableStatement;
	
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return originalCallableStatement.unwrap(iface);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		// checking to avoid any DML getting in as a query
		isUpdatable(sql);
		return originalCallableStatement.executeQuery(sql);
	}

	public ResultSet executeQuery() throws SQLException {
		return originalCallableStatement.executeQuery();
	}

	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException {
		originalCallableStatement.registerOutParameter(parameterIndex, sqlType);
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return originalCallableStatement.isWrapperFor(iface);
	}

	public int executeUpdate(String sql) throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.executeUpdate(sql);
	}

	public int executeUpdate() throws SQLException {
		// treat this on ConnectionWrapper.prepareCall
		return originalCallableStatement.executeUpdate();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		originalCallableStatement.setNull(parameterIndex, sqlType);
	}

	public void close() throws SQLException {
		originalCallableStatement.close();
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException {
		originalCallableStatement.registerOutParameter(parameterIndex, sqlType,
				scale);
	}

	public int getMaxFieldSize() throws SQLException {
		return originalCallableStatement.getMaxFieldSize();
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		originalCallableStatement.setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		originalCallableStatement.setByte(parameterIndex, x);
	}

	public void setMaxFieldSize(int max) throws SQLException {
		originalCallableStatement.setMaxFieldSize(max);
	}

	public boolean wasNull() throws SQLException {
		return originalCallableStatement.wasNull();
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		originalCallableStatement.setShort(parameterIndex, x);
	}

	public String getString(int parameterIndex) throws SQLException {
		return originalCallableStatement.getString(parameterIndex);
	}

	public int getMaxRows() throws SQLException {
		return originalCallableStatement.getMaxRows();
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		originalCallableStatement.setInt(parameterIndex, x);
	}

	public void setMaxRows(int max) throws SQLException {
		originalCallableStatement.setMaxRows(max);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		originalCallableStatement.setLong(parameterIndex, x);
	}

	public boolean getBoolean(int parameterIndex) throws SQLException {
		return originalCallableStatement.getBoolean(parameterIndex);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		originalCallableStatement.setEscapeProcessing(enable);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		originalCallableStatement.setFloat(parameterIndex, x);
	}

	public byte getByte(int parameterIndex) throws SQLException {
		return originalCallableStatement.getByte(parameterIndex);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		originalCallableStatement.setDouble(parameterIndex, x);
	}

	public int getQueryTimeout() throws SQLException {
		return originalCallableStatement.getQueryTimeout();
	}

	public short getShort(int parameterIndex) throws SQLException {
		return originalCallableStatement.getShort(parameterIndex);
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		originalCallableStatement.setQueryTimeout(seconds);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x)
			throws SQLException {
		originalCallableStatement.setBigDecimal(parameterIndex, x);
	}

	public int getInt(int parameterIndex) throws SQLException {
		return originalCallableStatement.getInt(parameterIndex);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		originalCallableStatement.setString(parameterIndex, x);
	}

	public long getLong(int parameterIndex) throws SQLException {
		return originalCallableStatement.getLong(parameterIndex);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		originalCallableStatement.setBytes(parameterIndex, x);
	}

	public float getFloat(int parameterIndex) throws SQLException {
		return originalCallableStatement.getFloat(parameterIndex);
	}

	public void cancel() throws SQLException {
		originalCallableStatement.cancel();
	}

	public SQLWarning getWarnings() throws SQLException {
		return originalCallableStatement.getWarnings();
	}

	public double getDouble(int parameterIndex) throws SQLException {
		return originalCallableStatement.getDouble(parameterIndex);
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		originalCallableStatement.setDate(parameterIndex, x);
	}

	public BigDecimal getBigDecimal(int parameterIndex, int scale)
			throws SQLException {
		return originalCallableStatement.getBigDecimal(parameterIndex, scale);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		originalCallableStatement.setTime(parameterIndex, x);
	}

	public void clearWarnings() throws SQLException {
		originalCallableStatement.clearWarnings();
	}

	public void setCursorName(String name) throws SQLException {
		originalCallableStatement.setCursorName(name);
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws SQLException {
		originalCallableStatement.setTimestamp(parameterIndex, x);
	}

	public byte[] getBytes(int parameterIndex) throws SQLException {
		return originalCallableStatement.getBytes(parameterIndex);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterIndex, x, length);
	}

	public Date getDate(int parameterIndex) throws SQLException {
		return originalCallableStatement.getDate(parameterIndex);
	}

	public boolean execute(String sql) throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.execute(sql);
	}

	public Time getTime(int parameterIndex) throws SQLException {
		return originalCallableStatement.getTime(parameterIndex);
	}

	public void setUnicodeStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalCallableStatement.setUnicodeStream(parameterIndex, x, length);
	}

	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		return originalCallableStatement.getTimestamp(parameterIndex);
	}

	public Object getObject(int parameterIndex) throws SQLException {
		return originalCallableStatement.getObject(parameterIndex);
	}

	public ResultSet getResultSet() throws SQLException {
		return originalCallableStatement.getResultSet();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterIndex, x, length);
	}

	public int getUpdateCount() throws SQLException {
		return originalCallableStatement.getUpdateCount();
	}

	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		return originalCallableStatement.getBigDecimal(parameterIndex);
	}

	public boolean getMoreResults() throws SQLException {
		return originalCallableStatement.getMoreResults();
	}

	public void clearParameters() throws SQLException {
		originalCallableStatement.clearParameters();
	}

	public Object getObject(int parameterIndex, Map<String, Class<?>> map)
			throws SQLException {
		return originalCallableStatement.getObject(parameterIndex, map);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException {
		originalCallableStatement.setObject(parameterIndex, x, targetSqlType);
	}

	public void setFetchDirection(int direction) throws SQLException {
		originalCallableStatement.setFetchDirection(direction);
	}

	public Ref getRef(int parameterIndex) throws SQLException {
		return originalCallableStatement.getRef(parameterIndex);
	}

	public int getFetchDirection() throws SQLException {
		return originalCallableStatement.getFetchDirection();
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		originalCallableStatement.setObject(parameterIndex, x);
	}

	public Blob getBlob(int parameterIndex) throws SQLException {
		return originalCallableStatement.getBlob(parameterIndex);
	}

	public void setFetchSize(int rows) throws SQLException {
		originalCallableStatement.setFetchSize(rows);
	}

	public Clob getClob(int parameterIndex) throws SQLException {
		return originalCallableStatement.getClob(parameterIndex);
	}

	public int getFetchSize() throws SQLException {
		return originalCallableStatement.getFetchSize();
	}

	public int getResultSetConcurrency() throws SQLException {
		return originalCallableStatement.getResultSetConcurrency();
	}

	public Array getArray(int parameterIndex) throws SQLException {
		return originalCallableStatement.getArray(parameterIndex);
	}

	public boolean execute() throws SQLException {
		// treat this on ConnectionWrapper.prepCall
		return originalCallableStatement.execute();
	}

	public int getResultSetType() throws SQLException {
		return originalCallableStatement.getResultSetType();
	}

	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		return originalCallableStatement.getDate(parameterIndex, cal);
	}

	public void addBatch(String sql) throws SQLException {
		originalCallableStatement.addBatch(sql);
	}

	public void clearBatch() throws SQLException {
		originalCallableStatement.clearBatch();
	}

	public void addBatch() throws SQLException {
		originalCallableStatement.addBatch();
	}

	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		return originalCallableStatement.getTime(parameterIndex, cal);
	}

	public int[] executeBatch() throws SQLException {
		return originalCallableStatement.executeBatch();
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws SQLException {
		originalCallableStatement.setCharacterStream(parameterIndex, reader,
				length);
	}

	public Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException {
		return originalCallableStatement.getTimestamp(parameterIndex, cal);
	}

	public void setRef(int parameterIndex, Ref x) throws SQLException {
		originalCallableStatement.setRef(parameterIndex, x);
	}

	public void registerOutParameter(int parameterIndex, int sqlType,
			String typeName) throws SQLException {
		originalCallableStatement.registerOutParameter(parameterIndex, sqlType,
				typeName);
	}

	public void setBlob(int parameterIndex, Blob x) throws SQLException {
		originalCallableStatement.setBlob(parameterIndex, x);
	}

	public void setClob(int parameterIndex, Clob x) throws SQLException {
		originalCallableStatement.setClob(parameterIndex, x);
	}

	public Connection getConnection() throws SQLException {
		return originalCallableStatement.getConnection();
	}

	public void setArray(int parameterIndex, Array x) throws SQLException {
		originalCallableStatement.setArray(parameterIndex, x);
	}

	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException {
		originalCallableStatement.registerOutParameter(parameterName, sqlType);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return originalCallableStatement.getMetaData();
	}

	public boolean getMoreResults(int current) throws SQLException {
		return originalCallableStatement.getMoreResults(current);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setDate(parameterIndex, x, cal);
	}

	public void registerOutParameter(String parameterName, int sqlType,
			int scale) throws SQLException {
		originalCallableStatement.registerOutParameter(parameterName, sqlType,
				scale);
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return originalCallableStatement.getGeneratedKeys();
	}

	public void setTime(int parameterIndex, Time x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setTime(parameterIndex, x, cal);
	}

	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.executeUpdate(sql, autoGeneratedKeys);
	}

	public void registerOutParameter(String parameterName, int sqlType,
			String typeName) throws SQLException {
		originalCallableStatement.registerOutParameter(parameterName, sqlType,
				typeName);
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setTimestamp(parameterIndex, x, cal);
	}

	public void setNull(int parameterIndex, int sqlType, String typeName)
			throws SQLException {
		originalCallableStatement.setNull(parameterIndex, sqlType, typeName);
	}

	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.executeUpdate(sql, columnIndexes);
	}

	public URL getURL(int parameterIndex) throws SQLException {
		return originalCallableStatement.getURL(parameterIndex);
	}

	public void setURL(String parameterName, URL val) throws SQLException {
		originalCallableStatement.setURL(parameterName, val);
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		originalCallableStatement.setURL(parameterIndex, x);
	}

	public void setNull(String parameterName, int sqlType) throws SQLException {
		originalCallableStatement.setNull(parameterName, sqlType);
	}

	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.executeUpdate(sql, columnNames);
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		return originalCallableStatement.getParameterMetaData();
	}

	public void setBoolean(String parameterName, boolean x) throws SQLException {
		originalCallableStatement.setBoolean(parameterName, x);
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		originalCallableStatement.setRowId(parameterIndex, x);
	}

	public void setByte(String parameterName, byte x) throws SQLException {
		originalCallableStatement.setByte(parameterName, x);
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException {
		originalCallableStatement.setNString(parameterIndex, value);
	}

	public void setShort(String parameterName, short x) throws SQLException {
		originalCallableStatement.setShort(parameterName, x);
	}

	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.execute(sql, autoGeneratedKeys);
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		originalCallableStatement.setNCharacterStream(parameterIndex, value,
				length);
	}

	public void setInt(String parameterName, int x) throws SQLException {
		originalCallableStatement.setInt(parameterName, x);
	}

	public void setLong(String parameterName, long x) throws SQLException {
		originalCallableStatement.setLong(parameterName, x);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		originalCallableStatement.setNClob(parameterIndex, value);
	}

	public void setFloat(String parameterName, float x) throws SQLException {
		originalCallableStatement.setFloat(parameterName, x);
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		originalCallableStatement.setClob(parameterIndex, reader, length);
	}

	public void setDouble(String parameterName, double x) throws SQLException {
		originalCallableStatement.setDouble(parameterName, x);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.execute(sql, columnIndexes);
	}

	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException {
		originalCallableStatement.setBigDecimal(parameterName, x);
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException {
		originalCallableStatement.setBlob(parameterIndex, inputStream, length);
	}

	public void setString(String parameterName, String x) throws SQLException {
		originalCallableStatement.setString(parameterName, x);
	}

	public void setBytes(String parameterName, byte[] x) throws SQLException {
		originalCallableStatement.setBytes(parameterName, x);
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException {
		originalCallableStatement.setNClob(parameterIndex, reader, length);
	}

	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		isUpdatable(sql);
		return originalCallableStatement.execute(sql, columnNames);
	}

	public void setDate(String parameterName, Date x) throws SQLException {
		originalCallableStatement.setDate(parameterName, x);
	}

	public void setTime(String parameterName, Time x) throws SQLException {
		originalCallableStatement.setTime(parameterName, x);
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException {
		originalCallableStatement.setSQLXML(parameterIndex, xmlObject);
	}

	public void setTimestamp(String parameterName, Timestamp x)
			throws SQLException {
		originalCallableStatement.setTimestamp(parameterName, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType,
			int scaleOrLength) throws SQLException {
		originalCallableStatement.setObject(parameterIndex, x, targetSqlType,
				scaleOrLength);
	}

	public int getResultSetHoldability() throws SQLException {
		return originalCallableStatement.getResultSetHoldability();
	}

	public void setAsciiStream(String parameterName, InputStream x, int length)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterName, x, length);
	}

	public boolean isClosed() throws SQLException {
		return originalCallableStatement.isClosed();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		originalCallableStatement.setPoolable(poolable);
	}

	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterName, x, length);
	}

	public boolean isPoolable() throws SQLException {
		return originalCallableStatement.isPoolable();
	}

	public void setObject(String parameterName, Object x, int targetSqlType,
			int scale) throws SQLException {
		originalCallableStatement.setObject(parameterName, x, targetSqlType,
				scale);
	}

	public void closeOnCompletion() throws SQLException {
		originalCallableStatement.closeOnCompletion();
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterIndex, x, length);
	}

	public boolean isCloseOnCompletion() throws SQLException {
		return originalCallableStatement.isCloseOnCompletion();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterIndex, x, length);
	}

	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException {
		originalCallableStatement.setObject(parameterName, x, targetSqlType);
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException {
		originalCallableStatement.setCharacterStream(parameterIndex, reader,
				length);
	}

	public void setObject(String parameterName, Object x) throws SQLException {
		originalCallableStatement.setObject(parameterName, x);
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterIndex, x);
	}

	public void setCharacterStream(String parameterName, Reader reader,
			int length) throws SQLException {
		originalCallableStatement.setCharacterStream(parameterName, reader,
				length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException {
		originalCallableStatement.setCharacterStream(parameterIndex, reader);
	}

	public void setDate(String parameterName, Date x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setDate(parameterName, x, cal);
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException {
		originalCallableStatement.setNCharacterStream(parameterIndex, value);
	}

	public void setTime(String parameterName, Time x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setTime(parameterName, x, cal);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		originalCallableStatement.setClob(parameterIndex, reader);
	}

	public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
			throws SQLException {
		originalCallableStatement.setTimestamp(parameterName, x, cal);
	}

	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException {
		originalCallableStatement.setNull(parameterName, sqlType, typeName);
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException {
		originalCallableStatement.setBlob(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		originalCallableStatement.setNClob(parameterIndex, reader);
	}

	public String getString(String parameterName) throws SQLException {
		return originalCallableStatement.getString(parameterName);
	}

	public boolean getBoolean(String parameterName) throws SQLException {
		return originalCallableStatement.getBoolean(parameterName);
	}

	public byte getByte(String parameterName) throws SQLException {
		return originalCallableStatement.getByte(parameterName);
	}

	public short getShort(String parameterName) throws SQLException {
		return originalCallableStatement.getShort(parameterName);
	}

	public int getInt(String parameterName) throws SQLException {
		return originalCallableStatement.getInt(parameterName);
	}

	public long getLong(String parameterName) throws SQLException {
		return originalCallableStatement.getLong(parameterName);
	}

	public float getFloat(String parameterName) throws SQLException {
		return originalCallableStatement.getFloat(parameterName);
	}

	public double getDouble(String parameterName) throws SQLException {
		return originalCallableStatement.getDouble(parameterName);
	}

	public byte[] getBytes(String parameterName) throws SQLException {
		return originalCallableStatement.getBytes(parameterName);
	}

	public Date getDate(String parameterName) throws SQLException {
		return originalCallableStatement.getDate(parameterName);
	}

	public Time getTime(String parameterName) throws SQLException {
		return originalCallableStatement.getTime(parameterName);
	}

	public Timestamp getTimestamp(String parameterName) throws SQLException {
		return originalCallableStatement.getTimestamp(parameterName);
	}

	public Object getObject(String parameterName) throws SQLException {
		return originalCallableStatement.getObject(parameterName);
	}

	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		return originalCallableStatement.getBigDecimal(parameterName);
	}

	public Object getObject(String parameterName, Map<String, Class<?>> map)
			throws SQLException {
		return originalCallableStatement.getObject(parameterName, map);
	}

	public Ref getRef(String parameterName) throws SQLException {
		return originalCallableStatement.getRef(parameterName);
	}

	public Blob getBlob(String parameterName) throws SQLException {
		return originalCallableStatement.getBlob(parameterName);
	}

	public Clob getClob(String parameterName) throws SQLException {
		return originalCallableStatement.getClob(parameterName);
	}

	public Array getArray(String parameterName) throws SQLException {
		return originalCallableStatement.getArray(parameterName);
	}

	public Date getDate(String parameterName, Calendar cal) throws SQLException {
		return originalCallableStatement.getDate(parameterName, cal);
	}

	public Time getTime(String parameterName, Calendar cal) throws SQLException {
		return originalCallableStatement.getTime(parameterName, cal);
	}

	public Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException {
		return originalCallableStatement.getTimestamp(parameterName, cal);
	}

	public URL getURL(String parameterName) throws SQLException {
		return originalCallableStatement.getURL(parameterName);
	}

	public RowId getRowId(int parameterIndex) throws SQLException {
		return originalCallableStatement.getRowId(parameterIndex);
	}

	public RowId getRowId(String parameterName) throws SQLException {
		return originalCallableStatement.getRowId(parameterName);
	}

	public void setRowId(String parameterName, RowId x) throws SQLException {
		originalCallableStatement.setRowId(parameterName, x);
	}

	public void setNString(String parameterName, String value)
			throws SQLException {
		originalCallableStatement.setNString(parameterName, value);
	}

	public void setNCharacterStream(String parameterName, Reader value,
			long length) throws SQLException {
		originalCallableStatement.setNCharacterStream(parameterName, value,
				length);
	}

	public void setNClob(String parameterName, NClob value) throws SQLException {
		originalCallableStatement.setNClob(parameterName, value);
	}

	public void setClob(String parameterName, Reader reader, long length)
			throws SQLException {
		originalCallableStatement.setClob(parameterName, reader, length);
	}

	public void setBlob(String parameterName, InputStream inputStream,
			long length) throws SQLException {
		originalCallableStatement.setBlob(parameterName, inputStream, length);
	}

	public void setNClob(String parameterName, Reader reader, long length)
			throws SQLException {
		originalCallableStatement.setNClob(parameterName, reader, length);
	}

	public NClob getNClob(int parameterIndex) throws SQLException {
		return originalCallableStatement.getNClob(parameterIndex);
	}

	public NClob getNClob(String parameterName) throws SQLException {
		return originalCallableStatement.getNClob(parameterName);
	}

	public void setSQLXML(String parameterName, SQLXML xmlObject)
			throws SQLException {
		originalCallableStatement.setSQLXML(parameterName, xmlObject);
	}

	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		return originalCallableStatement.getSQLXML(parameterIndex);
	}

	public SQLXML getSQLXML(String parameterName) throws SQLException {
		return originalCallableStatement.getSQLXML(parameterName);
	}

	public String getNString(int parameterIndex) throws SQLException {
		return originalCallableStatement.getNString(parameterIndex);
	}

	public String getNString(String parameterName) throws SQLException {
		return originalCallableStatement.getNString(parameterName);
	}

	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		return originalCallableStatement.getNCharacterStream(parameterIndex);
	}

	public Reader getNCharacterStream(String parameterName) throws SQLException {
		return originalCallableStatement.getNCharacterStream(parameterName);
	}

	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		return originalCallableStatement.getCharacterStream(parameterIndex);
	}

	public Reader getCharacterStream(String parameterName) throws SQLException {
		return originalCallableStatement.getCharacterStream(parameterName);
	}

	public void setBlob(String parameterName, Blob x) throws SQLException {
		originalCallableStatement.setBlob(parameterName, x);
	}

	public void setClob(String parameterName, Clob x) throws SQLException {
		originalCallableStatement.setClob(parameterName, x);
	}

	public void setAsciiStream(String parameterName, InputStream x, long length)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterName, x, length);
	}

	public void setBinaryStream(String parameterName, InputStream x, long length)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterName, x, length);
	}

	public void setCharacterStream(String parameterName, Reader reader,
			long length) throws SQLException {
		originalCallableStatement.setCharacterStream(parameterName, reader,
				length);
	}

	public void setAsciiStream(String parameterName, InputStream x)
			throws SQLException {
		originalCallableStatement.setAsciiStream(parameterName, x);
	}

	public void setBinaryStream(String parameterName, InputStream x)
			throws SQLException {
		originalCallableStatement.setBinaryStream(parameterName, x);
	}

	public void setCharacterStream(String parameterName, Reader reader)
			throws SQLException {
		originalCallableStatement.setCharacterStream(parameterName, reader);
	}

	public void setNCharacterStream(String parameterName, Reader value)
			throws SQLException {
		originalCallableStatement.setNCharacterStream(parameterName, value);
	}

	public void setClob(String parameterName, Reader reader)
			throws SQLException {
		originalCallableStatement.setClob(parameterName, reader);
	}

	public void setBlob(String parameterName, InputStream inputStream)
			throws SQLException {
		originalCallableStatement.setBlob(parameterName, inputStream);
	}

	public void setNClob(String parameterName, Reader reader)
			throws SQLException {
		originalCallableStatement.setNClob(parameterName, reader);
	}

	public <T> T getObject(int parameterIndex, Class<T> type)
			throws SQLException {
		return originalCallableStatement.getObject(parameterIndex, type);
	}

	public <T> T getObject(String parameterName, Class<T> type)
			throws SQLException {
		return originalCallableStatement.getObject(parameterName, type);
	}

	public CallableStatementWrapper(CallableStatement callableStatement, ArrayList<SqlCommandInterceptor> sqlCommandInterceptorList) {
		super(sqlCommandInterceptorList);
		this.originalCallableStatement = callableStatement;
	}

}
