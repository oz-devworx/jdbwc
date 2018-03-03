/* ********************************************************************
 * Copyright (C) 2008 Oz-DevWorX (Tim Gall)
 * ********************************************************************
 * This file is part of JDBWC.
 *
 * JDBWC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JDBWC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JDBWC.  If not, see <http://www.gnu.org/licenses/>.
 * ********************************************************************
 */
package com.jdbwc.core;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 * NOTE: This is only mostly just a skeleton and has not been fully implemented yet!!!
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-04-11
 */
public class WCCallableStatement extends WCPreparedStatement implements CallableStatement {

	private transient final ResultSet myCurrentResultSet = null;

	/** stores sql added via addBatch */
	private transient StringBuilder myBatchStatement = null;

	/** stores parepared statements that are being built */
	private transient DataHandler myCallStatement = null;

	/**
	 * Constructs a new instance of this.<br />
	 * This one gets called from WCConnection.
	 *
	 * @param connection An active WCConnection Object.
	 * @param stmntSkeleton A CallableStatements initialising SQL. Can contain ? param markers.
	 */
	protected WCCallableStatement(WCConnection connection, String stmntSkeleton) {
		super(connection);
		initialiseInstance(stmntSkeleton);
	}

	/**
	 * Construct a new WCCallableStatement using the connection
	 * parameter for this classes connection Object.<br />
	 * I borrowed this comment from the php-mySqli documentation on Prepared-Statements
	 * because I couldnt have put it any clearer myself.<br />
	 * <br />
	 * The query, as a string. It must consist of a single SQL statement.<br />
	 * You can include one or more parameter markers in the SQL statement by embedding question mark (?) characters at the appropriate positions.<br />
	 * <br />
	 * <b>Note 1:</b> You should not add a terminating semicolon or \g to the statement.<br />
	 * <b>Note 2:</b> The markers are legal only in certain places in SQL statements. For example, they are allowed in the VALUES() list of an INSERT statement (to specify column values for a row), or in a comparison with a column in a WHERE clause to specify a comparison value.<br />
	 * However, they are not allowed for identifiers (such as table or column names), in the select list that names the columns to be returned by a SELECT statement), or to specify both operands of a binary operator such as the = equal sign. The latter restriction is necessary because it would be impossible to determine the parameter type. In general, parameters are legal only in Data Manipulation Languange (DML) statements, and not in Data Defination Language (DDL) statements.
	 *
	 * @param stmntSkeleton The prepared statements sql skeleton (with ? param markers if any)
	 */
	private void initialiseInstance(String stmntSkeleton) {
		myBatchStatement = new StringBuilder();
		myCallStatement = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myCallStatement.addData("sql", stmntSkeleton);
	}




	/**
	 * @see java.sql.CallableStatement#getArray(int)
	 */
	public Array getArray(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getArray(java.lang.String)
	 */
	public Array getArray(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @deprecated
	 * @see java.sql.CallableStatement#getBigDecimal(int, int)
	 */
	@Deprecated
	public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBlob(int)
	 */
	public Blob getBlob(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBlob(java.lang.String)
	 */
	public Blob getBlob(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBoolean(int)
	 */
	public boolean getBoolean(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getByte(int)
	 */
	public byte getByte(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getByte(java.lang.String)
	 */
	public byte getByte(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBytes(int)
	 */
	public byte[] getBytes(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getBytes(java.lang.String)
	 */
	public byte[] getBytes(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getCharacterStream(int)
	 */
	public Reader getCharacterStream(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getCharacterStream(java.lang.String)
	 */
	public Reader getCharacterStream(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getClob(int)
	 */
	public Clob getClob(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getClob(java.lang.String)
	 */
	public Clob getClob(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDate(int)
	 */
	public Date getDate(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDate(java.lang.String)
	 */
	public Date getDate(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDate(int, java.util.Calendar)
	 */
	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDate(java.lang.String, java.util.Calendar)
	 */
	public Date getDate(String parameterName, Calendar cal) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDouble(int)
	 */
	public double getDouble(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getDouble(java.lang.String)
	 */
	public double getDouble(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getFloat(int)
	 */
	public float getFloat(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getFloat(java.lang.String)
	 */
	public float getFloat(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getInt(int)
	 */
	public int getInt(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getInt(java.lang.String)
	 */
	public int getInt(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getLong(int)
	 */
	public long getLong(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getLong(java.lang.String)
	 */
	public long getLong(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNCharacterStream(int)
	 */
	public Reader getNCharacterStream(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNCharacterStream(java.lang.String)
	 */
	public Reader getNCharacterStream(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNClob(int)
	 */
	public NClob getNClob(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNClob(java.lang.String)
	 */
	public NClob getNClob(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNString(int)
	 */
	public String getNString(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getNString(java.lang.String)
	 */
	public String getNString(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getObject(int)
	 */
	public Object getObject(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getObject(java.lang.String)
	 */
	public Object getObject(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getObject(int, java.util.Map)
	 */
	public Object getObject(int arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getObject(java.lang.String, java.util.Map)
	 */
	public Object getObject(String arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getRef(int)
	 */
	public Ref getRef(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getRef(java.lang.String)
	 */
	public Ref getRef(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getRowId(int)
	 */
	public RowId getRowId(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getRowId(java.lang.String)
	 */
	public RowId getRowId(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getSQLXML(int)
	 */
	public SQLXML getSQLXML(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getSQLXML(java.lang.String)
	 */
	public SQLXML getSQLXML(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getShort(int)
	 */
	public short getShort(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getShort(java.lang.String)
	 */
	public short getShort(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getString(int)
	 */
	public String getString(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getString(java.lang.String)
	 */
	public String getString(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTime(int)
	 */
	public Time getTime(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTime(java.lang.String)
	 */
	public Time getTime(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTime(int, java.util.Calendar)
	 */
	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTime(java.lang.String, java.util.Calendar)
	 */
	public Time getTime(String parameterName, Calendar cal) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(int, java.util.Calendar)
	 */
	public Timestamp getTimestamp(int parameterIndex, Calendar cal)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public Timestamp getTimestamp(String parameterName, Calendar cal)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getURL(int)
	 */
	public URL getURL(int parameterIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#getURL(java.lang.String)
	 */
	public URL getURL(String parameterName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int)
	 */
	public void registerOutParameter(int parameterIndex, int sqlType)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int)
	 */
	public void registerOutParameter(String parameterName, int sqlType)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int, int)
	 */
	public void registerOutParameter(int parameterIndex, int sqlType, int scale)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(int, int, java.lang.String)
	 */
	public void registerOutParameter(int parameterIndex, int sqlType,
			String typeName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int, int)
	 */
	public void registerOutParameter(String parameterName, int sqlType,
			int scale) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#registerOutParameter(java.lang.String, int, java.lang.String)
	 */
	public void registerOutParameter(String parameterName, int sqlType,
			String typeName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream)
	 */
	public void setAsciiStream(String parameterName, InputStream x)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	public void setAsciiStream(String parameterName, InputStream x, int length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	public void setAsciiStream(String parameterName, InputStream x, long length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void setBigDecimal(String parameterName, BigDecimal x)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream)
	 */
	public void setBinaryStream(String parameterName, InputStream x)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	public void setBinaryStream(String parameterName, InputStream x, long length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.sql.Blob)
	 */
	public void setBlob(String parameterName, Blob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.io.InputStream)
	 */
	public void setBlob(String parameterName, InputStream inputStream)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBlob(java.lang.String, java.io.InputStream, long)
	 */
	public void setBlob(String parameterName, InputStream inputStream,
			long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBoolean(java.lang.String, boolean)
	 */
	public void setBoolean(String parameterName, boolean x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setByte(java.lang.String, byte)
	 */
	public void setByte(String parameterName, byte x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setBytes(java.lang.String, byte[])
	 */
	public void setBytes(String parameterName, byte[] x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader)
	 */
	public void setCharacterStream(String parameterName, Reader reader)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	public void setCharacterStream(String parameterName, Reader reader,
			int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	public void setCharacterStream(String parameterName, Reader reader,
			long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.sql.Clob)
	 */
	public void setClob(String parameterName, Clob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.io.Reader)
	 */
	public void setClob(String parameterName, Reader reader)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setClob(java.lang.String, java.io.Reader, long)
	 */
	public void setClob(String parameterName, Reader reader, long length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date)
	 */
	public void setDate(String parameterName, Date x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setDate(java.lang.String, java.sql.Date, java.util.Calendar)
	 */
	public void setDate(String parameterName, Date x, Calendar cal)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setDouble(java.lang.String, double)
	 */
	public void setDouble(String parameterName, double x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setFloat(java.lang.String, float)
	 */
	public void setFloat(String parameterName, float x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setInt(java.lang.String, int)
	 */
	public void setInt(String parameterName, int x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setLong(java.lang.String, long)
	 */
	public void setLong(String parameterName, long x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNCharacterStream(java.lang.String, java.io.Reader)
	 */
	public void setNCharacterStream(String parameterName, Reader value)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	public void setNCharacterStream(String parameterName, Reader value,
			long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.sql.NClob)
	 */
	public void setNClob(String parameterName, NClob value) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.io.Reader)
	 */
	public void setNClob(String parameterName, Reader reader)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNClob(java.lang.String, java.io.Reader, long)
	 */
	public void setNClob(String parameterName, Reader reader, long length)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNString(java.lang.String, java.lang.String)
	 */
	public void setNString(String parameterName, String value)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int)
	 */
	public void setNull(String parameterName, int sqlType) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setNull(java.lang.String, int, java.lang.String)
	 */
	public void setNull(String parameterName, int sqlType, String typeName)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object)
	 */
	public void setObject(String parameterName, Object x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int)
	 */
	public void setObject(String parameterName, Object x, int targetSqlType)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setObject(java.lang.String, java.lang.Object, int, int)
	 */
	public void setObject(String parameterName, Object x, int targetSqlType,
			int scale) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setRowId(java.lang.String, java.sql.RowId)
	 */
	public void setRowId(String parameterName, RowId x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	public void setSQLXML(String parameterName, SQLXML xmlObject)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setShort(java.lang.String, short)
	 */
	public void setShort(String parameterName, short x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setString(java.lang.String, java.lang.String)
	 */
	public void setString(String parameterName, String x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time)
	 */
	public void setTime(String parameterName, Time x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setTime(java.lang.String, java.sql.Time, java.util.Calendar)
	 */
	public void setTime(String parameterName, Time x, Calendar cal)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void setTimestamp(String parameterName, Timestamp x)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setTimestamp(java.lang.String, java.sql.Timestamp, java.util.Calendar)
	 */
	public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
			throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#setURL(java.lang.String, java.net.URL)
	 */
	public void setURL(String parameterName, URL val) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.CallableStatement#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	@Override
	public void addBatch() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	@Override
	public void clearParameters() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#execute()
	 */
	@Override
	public boolean execute() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	@Override
	public ResultSet executeQuery() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	@Override
	public int executeUpdate() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	@Override
	public ParameterMetaData getParameterMetaData() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	@Override
	public void addBatch(String sql) throws SQLException {
		myBatchStatement.append(sql);
		if(!sql.trim().endsWith(";")){
			myBatchStatement.append(";");
		}
	}

	/**
	 * @see java.sql.Statement#cancel()
	 */
	@Override
	public void cancel() throws SQLException {
		super.cancel();
	}

	/**
	 * @see java.sql.Statement#clearBatch()
	 */
	@Override
	public void clearBatch() throws SQLException {
		myBatchStatement = new StringBuilder();
		myCurrentResultSet.close();
		myCallStatement = com.jdbwc.util.Util.getCaseSafeHandler(super.getConnection().getDbType());
	}

	/**
	 * @see java.sql.Statement#clearWarnings()
	 */
	@Override
	public void clearWarnings() throws SQLException {
		super.clearWarnings();
	}

	/**
	 * @see java.sql.Statement#close()
	 */
	@Override
	public void close() throws SQLException {
		super.close();
	}

	/**
	 * @see java.sql.Statement#executeBatch()
	 */
	@Override
	public int[] executeBatch() throws SQLException {
		return super.executeBatch();
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

}
