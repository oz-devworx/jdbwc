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
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.util.SQLParamParser;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.ObjectArray;

/**
 * Handles PreparedStatement's for this driver.
 *
 * @author Tim Gall
 * @version 2008-05-29
 */
public class WCPreparedStatement extends WCStatement implements PreparedStatement {

	private transient ResultSet myCurrentResultSet = null;

	/** stores sql added via addBatch */
	private transient StringBuilder myBatchStatement = null;

	/** stores prepared statements that are being built */
	private transient ObjectArray myPrepStatement = null;

	/**
	 * Constructs a new instance of this.<br />
	 * This one gets called from WCConnection.
	 *
	 * @param connection An active WCConnection Object.
	 * @param stmntSkeleton A PreparedStatements initialising SQL. Can contain ? param markers.
	 */
	protected WCPreparedStatement(WCConnection connection, String stmntSkeleton) throws SQLException{
		super(connection, connection.getCatalog());
		initialiseInstance(stmntSkeleton);
	}

	/**
	 * Constructs a new instance of this.<br />
	 * This one gets called from WCCallableStatement (sub-class).
	 *
	 * @param connection An active WCConnection Object.
	 */
	protected WCPreparedStatement(WCConnection connection) throws SQLException{
		super(connection, connection.getCatalog());
	}

	/**
	 * Prepare a String for inputing to SQL.
	 *
	 * @param stringVal A String to prepare for an SQL statement.
	 * @return A prepared String ready for use in an SQL statement.
	 */
	protected String wrapString(String stringVal){
		return "'" + stringVal + "'";
	}



	/**
	 * Construct a new WCPreparedStatement using the connection
	 * parameter for this classes connection Object.<br />
	 * I borrowed this comment from the php-mySqli documentation on Prepared-Statements
	 * because I couldnt have put it any clearer myself.<br />
	 * <br />
	 * The query, as a string. It must consist of a single SQL statement.<br />
	 * You can include one or more parameter markers in the SQL statement by embedding question mark (?) characters at the appropriate positions.<br />
	 * <br />
	 * <b>Note 1:</b> The markers are legal only in certain places in SQL statements. For example, they are allowed in the VALUES() list of an INSERT statement (to specify column values for a row), or in a comparison with a column in a WHERE clause to specify a comparison value.<br />
	 * However, they are not allowed for identifiers (such as table or column names), in the select list that names the columns to be returned by a SELECT statement), or to specify both operands of a binary operator such as the = equal sign. The latter restriction is necessary because it would be impossible to determine the parameter type. In general, parameters are legal only in Data Manipulation Languange (DML) statements, and not in Data Defination Language (DDL) statements.
	 *
	 * @param stmntSkeleton The prepared statements sql skeleton (with ? param markers if any)
	 */
	private void initialiseInstance(String stmntSkeleton) {
		myBatchStatement = new StringBuilder();
		myPrepStatement = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myPrepStatement.addData("sql", stmntSkeleton);
	}

	/**
	 * @see java.sql.PreparedStatement#addBatch()
	 */
	public void addBatch() throws SQLException {
		if(!myPrepStatement.isEmpty()){
			String newBatchPart = SQLParamParser.populateParams(myPrepStatement);

			myBatchStatement.append(newBatchPart);

			//System.err.println("```````````````````````` START\n" + newBatchPart + "\n`````````````````````````` END");
		}
	}

	/**
	 * @see java.sql.PreparedStatement#clearParameters()
	 */
	public void clearParameters() throws SQLException {
		synchronized (myPrepStatement) {
			String sql = myPrepStatement.getString("sql");
			myPrepStatement.clearData();
			myPrepStatement.addData("sql", sql);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#execute()
	 */
	public boolean execute() throws SQLException {
		boolean hasResults = super.execute(SQLParamParser.populateParams(myPrepStatement));
		if(hasResults){
			myCurrentResultSet = super.getResultSet();
		}

		return hasResults;
	}

	/**
	 * @see java.sql.PreparedStatement#executeQuery()
	 */
	public ResultSet executeQuery() throws SQLException {
		myCurrentResultSet = super.executeQuery(SQLParamParser.populateParams(myPrepStatement));
		return myCurrentResultSet;
	}

	/**
	 * @see java.sql.PreparedStatement#executeUpdate()
	 */
	public int executeUpdate() throws SQLException {
		executeQuery();
		return myCurrentResultSet.getFetchSize();
	}

	/**
	 * @see java.sql.PreparedStatement#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		ResultSetMetaData rsmd = null;

		if(myCurrentResultSet!=null && !myCurrentResultSet.isClosed()){
			rsmd = myCurrentResultSet.getMetaData();
		}else{
			String sql = SQLParamParser.populateParams(myPrepStatement);
			rsmd = new WCResultSetMetaData(sql, myConnection);
		}
		return rsmd;
	}

	/**
	 * @see java.sql.PreparedStatement#getParameterMetaData()
	 */
	public ParameterMetaData getParameterMetaData() throws SQLException {
		return new WCParameterMetaData(myConnection, myPrepStatement);
	}

	/**
	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
	 */
	public void setArray(int parameterIndex, Array x) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
	 */
	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
	 */
	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
	 */
	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
	 */
	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
	 */
	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
	 */
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
	 */
	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
	 */
	public void setBlob(int parameterIndex, Blob x) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
	 */
	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
	 */
	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
//		TODO: implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
	 */
	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "boolean", x);
		}else{
			myPrepStatement.addData("boolean", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setByte(int, byte)
	 */
	public void setByte(int parameterIndex, byte x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "byte", x);
		}else{
			myPrepStatement.addData("byte", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
	 */
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
	 */
	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
	 */
	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
	 */
	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
	 */
	public void setClob(int parameterIndex, Clob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
	 */
	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
	 */
	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
	 */
	public void setDate(int parameterIndex, Date x) throws SQLException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		setString(parameterIndex, formatter.format(x));
	}

	/**
	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
	 */
	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		/* FIXME: not sure if I interpretted the functionality of this one correctly.
		 * Needs to be tested and re-evaluated at some point
		 * concentrating on the timezone and locale.
		 *
		 * ???maybee try using??? TimeZone tz = cal.getTimeZone();
		 */
		cal.setTime(x);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String calendarDate = formatter.format(cal.getTime());
		setString(parameterIndex, calendarDate);
	}

	/**
	 * @see java.sql.PreparedStatement#setDouble(int, double)
	 */
	public void setDouble(int parameterIndex, double x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "double", x);
		}else{
			myPrepStatement.addData("double", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setFloat(int, float)
	 */
	public void setFloat(int parameterIndex, float x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "float", x);
		}else{
			myPrepStatement.addData("float", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setInt(int, int)
	 */
	public void setInt(int parameterIndex, int x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "int", x);
		}else{
			myPrepStatement.addData("int", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setLong(int, long)
	 */
	public void setLong(int parameterIndex, long x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "long", x);
		}else{
			myPrepStatement.addData("long", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
	 */
	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
	 */
	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
	 */
	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
	 */
	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
	 */
	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
	 */
	public void setNString(int parameterIndex, String value) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setNull(int, int)
	 */
	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "long", sqlType);
		}else{
			myPrepStatement.addData("long", sqlType);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
	 */
	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
	 */
	public void setObject(int parameterIndex, Object x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "Object", x);
		}else{
			myPrepStatement.addData("Object", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
	 */
	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
	 */
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
	 */
	public void setRef(int parameterIndex, Ref x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
	 */
	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
	 */
	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.PreparedStatement#setShort(int, short)
	 */
	public void setShort(int parameterIndex, short x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "short", x);
		}else{
			myPrepStatement.addData("short", x);
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
	 */
	public void setString(int parameterIndex, String x) throws SQLException {
		if(myPrepStatement.hasKey(parameterIndex)){
			myPrepStatement.setData(parameterIndex, "String", wrapString(x));
		}else{
			myPrepStatement.addData("String", wrapString(x));
		}
	}

	/**
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
	 */
	public void setTime(int parameterIndex, Time x) throws SQLException {
		SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss", Locale.US);
		setString(parameterIndex, formatter.format(x));
	}

	/**
	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
	 */
	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		/* FIXME: not sure if I interpretted the functionality of this one correctly.
		 * Needs to be tested and re-evaluated at some point
		 * concentrating on the timezone and locale.
		 */
		cal.setTime(x);

		SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss", Locale.US);
		setString(parameterIndex, formatter.format(cal.getTime()));
	}

	/**
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss yyyy-MM-dd", Locale.US);
		setString(parameterIndex, formatter.format(x));
	}

	/**
	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
	 */
	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		/* FIXME: not sure if I interpretted the functionality of this one correctly.
		 * Needs to be tested and re-evaluated at some point
		 * concentrating on the timezone and locale.
		 */
		cal.setTime(x);

		SimpleDateFormat formatter = new SimpleDateFormat("HH-mm-ss yyyy-MM-dd", Locale.US);
		setString(parameterIndex, formatter.format(cal.getTime()));
	}

	/**
	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
	 */
	public void setURL(int parameterIndex, URL x) throws SQLException {
		setString(parameterIndex, x.toString());
	}

	/**
	 * @deprecated
	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
	 */
	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
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
		super.clearBatch();
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
		super.clearBatch();
		super.addBatch(myBatchStatement.toString());
		return super.executeBatch();
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}
}