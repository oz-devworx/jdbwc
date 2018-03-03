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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.iface.ResultSet;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;
import com.ozdevworx.dtype.impl.IlegalNumberTypeException;

/**
 * Extended JDBC-API implementation for java.sql.ResultSet.<br />
 * See this packages ResultSet interface for extension method details.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public class WCResultSet extends WCResultSetUpdates implements ResultSet {

//	/** Log object for this class. */
//    private static final Log LOG = LogFactory.getLog("jdbwc.core.ResultSet");

	private transient final boolean useJdbcTzShift = true;// best left set to true for now

	protected WCResultSet(WCConnection connection) throws SQLException{
		super();
		myRows = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myRow = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myPointer = -1;
		mySQL = "";
		myDbType = connection.getDbType();

		myConnection = connection;
		myStatement = new WCStatement(connection);
	}

	protected WCResultSet(WCConnection connection, WCStatement statement) throws SQLException{
		super();
		myRows = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myRow = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myPointer = -1;
		mySQL = "";
		myDbType = connection.getDbType();

		myConnection = connection;
		myStatement = statement;
	}

	protected WCResultSet(WCConnection connection, WCStatement statement, String sql) throws SQLException{
		super();
		myRows = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myRow = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myPointer = -1;
		mySQL = sql;
		myDbType = connection.getDbType();

		myConnection = connection;
		myStatement = statement;
	}

	protected WCResultSet(WCConnection connection, WCStatement statement, String sql, DataHandler results) throws SQLException{
		super();
		myRows = results;
		myRow = Util.getCaseSafeHandler(connection.getCaseSensitivity());
		myPointer = -1;
		mySQL = sql;
		myDbType = connection.getDbType();

		myConnection = connection;
		myStatement = statement;
	}

	public void addRow(DataHandler row){
		myRows.addData(myRows.length()+"", row);
	}

	public boolean next() throws SQLException{
		isResultSetOpen();
		boolean hasNext = false;
		if(myRows.getData(myPointer+1) instanceof DataHandler){
			movePointerForward();
			hasNext = true;
		}

		return hasNext;
	}

	public boolean previous() throws SQLException{
		isResultSetOpen();
		boolean hasPrevious = false;
		if(myRows.getData(myPointer-1) instanceof DataHandler){
			movePointerBackward();
			hasPrevious = true;
		}

		return hasPrevious;
	}

	public boolean first() throws SQLException {
		isResultSetOpen();
		boolean hasFirst = false;
		movePointerToStart();
		if(myRows.getData(myPointer) instanceof DataHandler){
			hasFirst = true;
		}else{
			throw new SQLException(
					"First Element in the ResultSet is not a valid row!",
					"WCNOE");
		}
		return hasFirst;
	}

	public boolean last() throws SQLException {
		isResultSetOpen();
		boolean hasLast = false;
		movePointerToEnd();
		if(myRows.getData(myPointer) instanceof DataHandler){
			hasLast = true;
		}else{
			throw new SQLException(
					"Last Element in the ResultSet is not a valid row!",
					"WCNOE");
		}

		return hasLast;
	}

	public boolean isClosed() throws SQLException {
		boolean closed = false;
		if(myRows.length()==0){
			closed = true;
		}

		return closed;
	}

	public boolean isFirst() throws SQLException {
		isResultSetOpen();
		boolean first = false;
		if(myPointer==0){
			first = true;
		}

		return first;
	}

	public boolean isLast() throws SQLException {
		isResultSetOpen();
		boolean last = false;
		if(myPointer==myRows.length()-1){
			last = true;
		}

		return last;
	}

	public void close() throws SQLException {
		myRows.clearData();
		myRow.clearData();
		myPointer = -1;
		mySQL = "";
	}

	public int getFetchDirection() throws SQLException {
		return myStatement.myDirection;
	}

	/**
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	public int getFetchSize() throws SQLException {
		return myRows.length();
	}

	public void setFetchDirection(int direction) throws SQLException {
		switch (direction){
		case FETCH_REVERSE:
			myStatement.myDirection = FETCH_REVERSE;
			break;
		case FETCH_UNKNOWN:
			myStatement.myDirection = FETCH_UNKNOWN;
			break;
		case FETCH_FORWARD:
		default:
			myStatement.myDirection = FETCH_FORWARD;
			break;
		}
	}

	public int getRow() throws SQLException {
		isResultSetOpen();
		return myPointer;
	}

	public WCStatement getStatement() throws SQLException {
		return myStatement;
	}




	public int getInt(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		int val = 0;
		try {
			val = myRow.getInt(columnIndex-1);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Integer cast Exception. The value at column index " + (columnIndex-1) + " is not an Integer.",
					"22003");
		}
		return val;
	}

	public int getInt(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		int val = 0;
		try {
			val = myRow.getInt(columnLabel);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Integer cast Exception. The value at column '" + columnLabel + "' is not an Integer.",
					"22003");
		}
		return val;
	}

	public double getDouble(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		double val = 0;
		try {
			val = myRow.getDouble(columnIndex-1);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Double cast Exception. The value at column index " + (columnIndex-1) + " is not a Double.",
					"22003");
		}
		return val;
	}

	public double getDouble(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		double val = 0;
		try {
			val = myRow.getDouble(columnLabel);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Double cast Exception. The value at column '" + columnLabel + "' is not a Double.",
					"22003");
		}
		return val;
	}

	public float getFloat(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		float val = 0;
		try {
			val = myRow.getFloat(columnIndex-1);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Float cast Exception. The value at column index " + (columnIndex-1) + " is not a Float.",
					"22003");
		}
		return val;
	}

	public float getFloat(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		float val = 0;
		try {
			val = myRow.getFloat(columnLabel);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Float cast Exception. The value at column '" + columnLabel + "' is not a Float.",
					"22003");
		}
		return val;
	}

	public Object getObject(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		Object val = myRow.getObject(columnIndex-1);
		return val;
	}

	public Object getObject(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		Object val = myRow.getObject(columnLabel);
		return val;
	}

	public String getString(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		String val = myRow.getString(columnIndex-1);
		return val;
	}

	public String getString(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		String val = myRow.getString(columnLabel);
		return val;
	}

	public long getLong(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		long val = 0L;
		try {
			val = myRow.getLong(columnIndex-1);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Long cast Exception. The value at column index " + (columnIndex-1) + " is not a Long.",
					"22003");
		}
		return val;
	}

	public long getLong(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		long val = 0L;
		try {
			val = myRow.getLong(columnLabel);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Long cast Exception. The value at column '" + columnLabel + "' is not a Long.",
					"22003");
		}
		return val;
	}

	public short getShort(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		short val = 0;
		try {
			val = myRow.getShort(columnIndex-1);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Short cast Exception. The value at column index " + (columnIndex-1) + " is not a Short.",
					"22003");
		}
		return val;
	}

	public short getShort(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		short val = 0;
		try {
			val = myRow.getShort(columnLabel);
		} catch (IlegalNumberTypeException e) {
			throw new SQLException(
					"Short cast Exception. The value at column '" + columnLabel + "' is not a Short.",
					"22003");
		}
		return val;
	}






















	/**
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws SQLException {
		boolean isAbsolute = false;
		if( (myRows!=null) && (myRows.length() > 0) ){
			if(row < 0){
				myPointer = (myRows.length() - row)-1;
			}else if(row > 0){
				myPointer = row-1;
			}else{
				myPointer = 0;
			}

			if(myRows.getData(myPointer) instanceof DataHandler){
				getCurrentRow();
				isAbsolute = true;
			}
		}
		return isAbsolute;
	}

	/**
	 * @see java.sql.ResultSet#afterLast()
	 */
	public void afterLast() throws SQLException {
		myPointer = myRows.length();
		myRow.clearData();
	}

	/**
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws SQLException {
		myPointer = 0;
		getCurrentRow();
	}

	/**
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	public void cancelRowUpdates() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#deleteRow()
	 */
	public void deleteRow() throws SQLException {
		myRows.removeByIndex(myPointer);
	}

	/**
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	public int findColumn(String columnLabel) throws SQLException {
		int index = 0;
		if(myRow!=null && myRow.countMatches(columnLabel) > 0){
			index = myRow.getIndex(columnLabel)+1;
		}else{
			throw new SQLException(
					columnLabel + " not found in this ResultSet.",
					"S0022");
		}
		return index;
	}

	/**
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public Array getArray(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	public Array getArray(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		BigDecimal val = null;
		try {
			double dVal = Double.parseDouble(myRow.getString(columnIndex-1));
			val = BigDecimal.valueOf(dVal);
		} catch (ClassCastException e) {
			throw new SQLException(
					"BigDecimal cast Exception. The value at column index " + (columnIndex-1) + " is not a BigDecimal.",
					"22003");
		}
		return val;
	}

	/**
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		BigDecimal val = null;
		try {
			double dVal = Double.parseDouble(myRow.getString(columnLabel));
			val = BigDecimal.valueOf(dVal);
		} catch (ClassCastException e) {
			throw new SQLException(
					"BigDecimal cast Exception. The value at column '" + columnLabel + "' is not a BigDecimal.",
					"22003");
		}
		return val;
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		isIndexValid(columnIndex);
		BigDecimal val = null;
		try {
			double dVal = Double.parseDouble(myRow.getString(columnIndex-1));
			val = BigDecimal.valueOf(dVal);
			val.setScale(scale);
		} catch (ClassCastException e) {
			throw new SQLException(
					"BigDecimal cast Exception. The value at column index " + (columnIndex-1) + " is not a BigDecimal.",
					"22003");
		}
		return val;
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	@Deprecated
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		isIndexValid(columnLabel);
		BigDecimal val = null;
		try {
			double dVal = Double.parseDouble(myRow.getString(columnLabel));
			val = BigDecimal.valueOf(dVal);
			val.setScale(scale);
		} catch (ClassCastException e) {
			throw new SQLException(
					"BigDecimal cast Exception. The value at column '" + columnLabel + "' is not a BigDecimal.",
					"22003");
		}
		return val;
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	public Blob getBlob(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	public Blob getBlob(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * Values that are considered booleans by this method (cAsE insensitive).
	 * <ul>
	 * <li><code>TRUE</code> <i>or</i> <code>FALSE</code></li>
	 * <li><code>YES</code> <i>or</i> <code>NO</code></li>
	 * <li><code>ON</code> <i>or</i> <code>OFF</code></li>
	 * <li><code>1</code> <i>or</i> <code>0</code></li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		boolean val = false;

		String boolStr = myRow.getString(columnIndex-1);
		/* look for common true booleans */
		if(boolStr.equalsIgnoreCase("yes")
		|| boolStr.equalsIgnoreCase("on")
		|| boolStr.equalsIgnoreCase("1"))
		{
			val = true;

		/* look for common false booleans */
		}else if(boolStr.equalsIgnoreCase("no")
				|| boolStr.equalsIgnoreCase("off")
				|| boolStr.equalsIgnoreCase("0"))
		{
			val = false;

		}else{
			/* look for java boolean or throw an exception if none found */
			try {
				val = Boolean.parseBoolean(myRow.getString(columnIndex-1));
			} catch (ClassCastException e) {
				throw new SQLException(
						"Boolean cast Exception. The value at column index " + (columnIndex-1) + " is not equal to true, yes or 1",
						"S1000");
			}
		}
		return val;
	}

	/**
	 * Values that are considered booleans by this method (cAsE insensitive).
	 * <ul>
	 * <li><code>TRUE</code> <i>or</i> <code>FALSE</code></li>
	 * <li><code>YES</code> <i>or</i> <code>NO</code></li>
	 * <li><code>ON</code> <i>or</i> <code>OFF</code></li>
	 * <li><code>1</code> <i>or</i> <code>0</code></li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		boolean val = false;

		String boolStr = myRow.getString(columnLabel);
		/* look for common true booleans */
		if(boolStr.equalsIgnoreCase("yes")
		|| boolStr.equalsIgnoreCase("on")
		|| boolStr.equalsIgnoreCase("1"))
		{
			val = true;

		/* look for common false booleans */
		}else if(boolStr.equalsIgnoreCase("no")
				|| boolStr.equalsIgnoreCase("off")
				|| boolStr.equalsIgnoreCase("0"))
		{
			val = false;

		}else{
			/* look for java booleans or throw an exception if none found */
			try {
				val = Boolean.parseBoolean(boolStr);
			} catch (ClassCastException e) {
				throw new SQLException(
						"Boolean cast Exception. The value at column '" + columnLabel + "' is not equal to true, yes or 1",
						"S1000");
			}
		}
		return val;
	}

	/**
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		byte val = 0;
		try {
			val = Byte.parseByte(myRow.getString(columnIndex-1));
		} catch (ClassCastException e) {
			throw new SQLException(
					"Byte cast Exception. The value at column index " + (columnIndex-1) + " is not a Byte.",
					"S1000");
		}
		return val;
	}

	/**
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	public byte getByte(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		byte val = 0;
		try {
			val = Byte.parseByte(myRow.getString(columnLabel));
		} catch (ClassCastException e) {
			throw new SQLException(
					"Byte cast Exception. The value at column index " + columnLabel + " is not a Byte.",
					"S1000");
		}
		return val;
	}

	/**
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	public byte[] getBytes(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getClob(int)
	 */
	public Clob getClob(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	public Clob getClob(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	public int getConcurrency() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getCursorName()
	 */
	public String getCursorName() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * Dates are converted during transit from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getDate(int columnIndex, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null dates return <code>null</code> (as expected).</li>
	 * <li>Empty dates return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null dates correctly
		 * and return null for empty date fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getDateFromString(valStr, null);
	}

	/**
	 * Dates are converted during transit from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getDate(String columnLabel, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null dates return <code>null</code> (as expected).</li>
	 * <li>Empty dates return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null dates correctly
		 * and return null for empty date fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getDateFromString(valStr, null);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Date in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getDate(int columnIndex)</code>
	 * to have Dates automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null dates return <code>null</code> (as expected).</li>
	 * <li>Empty dates return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null dates correctly
		 * and return null for empty date fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getDateFromString(valStr, cal);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Date in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getDate(String columnLabel)</code>
	 * to have Dates automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null dates return <code>null</code> (as expected).</li>
	 * <li>Empty dates return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null dates correctly
		 * and return null for empty date fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getDateFromString(valStr, cal);
	}

	/**
	 * @see java.sql.ResultSet#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getMetaData()
	 */
	public java.sql.ResultSetMetaData getMetaData() throws SQLException {
		WCResultSetMetaData metaData = null;

		String queryString = mySQL.toLowerCase();
		boolean isValidSql = queryString.contains("select ") ? true : (queryString.contains("show ") ? true : false);

		if(myConnection!=null && isValidSql){
			metaData = new WCResultSetMetaData(mySQL, myConnection);
		}else{
			throw new SQLException(
					"You tried to create a ResultSetMetaData Object for a query that is not a SELECT, EXPLAIN, EXEC, DESCRIBE or SHOW Statement. Only SELECT, EXPLAIN, EXEC, DESCRIBE and SHOW Statements can produce a ResultSetMetaData Object.",
					"S1009");
		}

		return metaData;
	}

	/**
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	public NClob getNClob(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	public NClob getNClob(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getNString(int)
	 */
	public String getNString(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	public String getNString(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getRef(int)
	 */
	public Ref getRef(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	public Ref getRef(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	public RowId getRowId(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	public RowId getRowId(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * Times are converted during transit, from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTime(int columnIndex, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null Times return <code>null</code> (as expected).</li>
	 * <li>Empty Times return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null times correctly
		 * and return null for empty time fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimeFromString(valStr, null);
	}

	/**
	 * Times are converted during transit, from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTime(String columnLabel, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null Times return <code>null</code> (as expected).</li>
	 * <li>Empty Times return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null times correctly
		 * and return null for empty time fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimeFromString(valStr, null);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Time in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTime(int columnIndex)</code>
	 * to have Times automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null Times return <code>null</code> (as expected).</li>
	 * <li>Empty Times return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null times correctly
		 * and return null for empty time fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimeFromString(valStr, cal);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Time in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTime(columnLabel)</code>
	 * to have Times automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null Times return <code>null</code> (as expected).</li>
	 * <li>Empty Times return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null times correctly
		 * and return null for empty time fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimeFromString(valStr, cal);
	}

	/**
	 * Timestamps are converted during transit, from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTimestamp(int columnIndex, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null Timestamps return <code>null</code> (as expected).</li>
	 * <li>Empty Timestamps return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null Timestamps correctly
		 * and return null for empty Timestamp fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimestampFromString(valStr, null);
	}

	/**
	 * Timestamps are converted during transit, from the server
	 * TimeZone to the Hosts TimeZone.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTimestamp(String columnLabel, Calendar cal)</code>
	 * and specify the desired timezone in the Calendar Object.
	 * <ul>
	 * <li>Null Timestamps return <code>null</code> (as expected).</li>
	 * <li>Empty Timestamps return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null Timestamps correctly
		 * and return null for empty Timestamp fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimestampFromString(valStr, null);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Timestamp in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTimestamp(int columnIndex)</code>
	 * to have Timestamps automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null Timestamps return <code>null</code> (as expected).</li>
	 * <li>Empty Timestamps return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		isIndexValid(columnIndex);

		String valStr = myRow.getString(columnIndex-1);

		/* handle null Timestamps correctly
		 * and return null for empty Timestamp fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimestampFromString(valStr, cal);
	}

	/**
	 * Specify the desired TimeZone of the output java.sql.Timestamp in the Calendar param.<br /><br />
	 * NOTE:<br />
	 * If this behavior is not desired use <code>getTimestamp(String columnLabel)</code>
	 * to have Timestamps automagically converted from the server
	 * TimeZone to the Hosts TimeZone.
	 * <ul>
	 * <li>Null Timestamps return <code>null</code> (as expected).</li>
	 * <li>Empty Timestamps return <code>null</code> (to avoid altering the actual value).</li>
	 * </ul>
	 *
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		isIndexValid(columnLabel);

		String valStr = myRow.getString(columnLabel);

		/* handle null Timestamps correctly
		 * and return null for empty Timestamp fields
		 * to avoid altering the actual value in transit. */
		if(isNull(valStr) || isEmpty(valStr)){
			return null;
		}

		return getTimestampFromString(valStr, cal);
	}

	/**
	 * @see java.sql.ResultSet#getType()
	 */
	public int getType() throws SQLException {
		return ResultSet.TYPE_SCROLL_INSENSITIVE;
	}

	/**
	 * @see java.sql.ResultSet#getURL(int)
	 */
	public URL getURL(int columnIndex) throws SQLException {
		isIndexValid(columnIndex);
		URL url = null;
		try {
			url = new URL(myRow.getString(columnIndex-1));
		} catch (MalformedURLException e) {
			throw new SQLException(
					"URL cast Exception. The value at column index " + (columnIndex-1) + " is not a valid URL.",
					"S1000", e);
		}
		return url;
	}

	/**
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	public URL getURL(String columnLabel) throws SQLException {
		isIndexValid(columnLabel);
		URL url = null;
		try {
			url = new URL(myRow.getString(columnLabel));
		} catch (MalformedURLException e) {
			throw new SQLException(
					"URL cast Exception. The value at column " + columnLabel + " is not a valid URL.",
					"S1000", e);
		}
		return url;
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Deprecated
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Deprecated
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException{
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#insertRow()
	 */
	public void insertRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws SQLException {
		return myPointer==myRows.length();
	}

	/**
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws SQLException {
		return myPointer==0;
	}

	/**
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public void moveToCurrentRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	public void moveToInsertRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#refreshRow()
	 */
	public void refreshRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#relative(int)
	 */
	public boolean relative(int rows) throws SQLException {
		boolean isRelative = false;
		if(rows > 0){
			if(myRows.getData(myPointer+rows) instanceof DataHandler){
				synchronized(this){
					myPointer += rows;
					getCurrentRow();
					isRelative = true;
				}
			}
		}else if(rows < 0){
			if(myRows.getData(myPointer-rows) instanceof DataHandler){
				synchronized(this){
					myPointer -= rows;
					getCurrentRow();
					isRelative = true;
				}
			}
		}
		return isRelative;
	}

	/**
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	public boolean rowDeleted() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#rowInserted()
	 */
	public boolean rowInserted() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	public boolean rowUpdated() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	public void setFetchSize(int rows) throws SQLException{
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.ResultSet#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		boolean isNull = false;
		if(myRow != null && myRow.length()>0){
			isNull = getString(myPointer).equalsIgnoreCase("NULL");
		}
		return isNull;
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}






	/* *******************************
	 *********************************
	 ******** Private methods ********
	 *********************************
	 *********************************/


	private void movePointerForward(){
		synchronized(this){
			myPointer++;
			getCurrentRow();
		}
	}

	private void movePointerBackward(){
		synchronized(this){
			myPointer--;
			getCurrentRow();
		}
	}

	private void movePointerToStart(){
		synchronized(this){
			myPointer = 0;
			getCurrentRow();
		}
	}

	private void movePointerToEnd(){
		synchronized(this){
			myPointer = myRows.length();
			getCurrentRow();
		}
	}

	private void getCurrentRow(){
		myRow = (DataHandler)myRows.getData(myPointer);
	}

	private void isResultSetOpen() throws SQLException{
		if(myRows==null){
			throw new SQLException(
					"ResultSet returned a null Object.\n"
					+ "This indicates your ResultSet is most likely closed.\n"
					+ "Check your java sytax and logical conditions related to the ResultSet that caused this error first.",
					"WCNOE");
		}
	}

	private boolean isIndexValid(int columnIndex) throws SQLException{
		boolean isValid = false;
		if(myRow!=null && columnIndex <= myRow.length()){
			isValid = true;
		}else{
			throw new SQLException(
					"Column index " + columnIndex + " not found in this ResultSet.",
					"S0022");
		}
		return isValid;
	}

	private boolean isIndexValid(String columnLabel) throws SQLException{
		boolean isValid = false;
		if(myRow!=null && myRow.countMatches(columnLabel) > 0){
			isValid = true;
		}else{
			throw new SQLException(
					"Column label " + columnLabel + " not found in this ResultSet.",
					"S0022");
		}
		return isValid;
	}

	private boolean isNull(String valueToCheck){
		boolean valIsNull = false;

		if(valueToCheck == null || "null".equalsIgnoreCase(valueToCheck)){
			valIsNull = true;
		}

		return valIsNull;
	}

	private boolean isEmpty(String valueToCheck){
		boolean valIsEmpty = false;

		if(valueToCheck.length()==0){
			valIsEmpty = true;
		}

		return valIsEmpty;
	}

	/**
	 * Create a normalized java.sql.Date using the given variables
	 *
	 * @param cal A Calendar to use for building this Date. Can be null
	 * @param years int
	 * @param months int
	 * @param days int
	 * @return A normalized java.sql.Date
	 */
	private Date createDate(Calendar cal, int years, int months, int days) throws SQLException{
		Calendar srcCal;
		if(cal!=null){
			srcCal = cal;
		}else{
			srcCal = Calendar.getInstance(getMyLocalTimeZone(myConnection.myTimeZone));
		}
		srcCal.clear();

		srcCal.set(Calendar.YEAR, years);
		srcCal.set(Calendar.MONTH, months-1);
		srcCal.set(Calendar.DAY_OF_MONTH, days);

		// normalize the time
		srcCal.set(Calendar.HOUR_OF_DAY, 0);
		srcCal.set(Calendar.MINUTE, 0);
		srcCal.set(Calendar.SECOND, 0);
		srcCal.set(Calendar.MILLISECOND, 0);
		if(useJdbcTzShift){
			srcCal = jdbcTimeZoneShift(srcCal, cal);
		}
		return new Date(srcCal.getTimeInMillis());
	}

	private Time createTime(Calendar cal, int hours, int minutes, int seconds) throws SQLException{
		Calendar srcCal = Calendar.getInstance(getMyLocalTimeZone(myConnection.myTimeZone));
		srcCal.clear();

		// set 'date' to epoch of Jan 1, 1970
		srcCal.set(Calendar.YEAR, 1970);
		srcCal.set(Calendar.MONTH, 0);
		srcCal.set(Calendar.DAY_OF_MONTH, 1);

		srcCal.set(Calendar.HOUR_OF_DAY, hours);
		srcCal.set(Calendar.MINUTE, minutes);
		srcCal.set(Calendar.SECOND, seconds);
		srcCal.set(Calendar.MILLISECOND, 0);
		if(useJdbcTzShift){
			srcCal = jdbcTimeZoneShift(srcCal, cal);
		}
		return new Time(srcCal.getTimeInMillis());
	}

	private Timestamp createTimestamp(Calendar cal,
			int years, int months, int days,
			int hours, int minutes, int seconds, int milliseconds)
	throws SQLException{
		Calendar srcCal;
		if(cal!=null){
			srcCal = cal;
		}else{
			srcCal = Calendar.getInstance(getMyLocalTimeZone(myConnection.myTimeZone));
		}
		srcCal.clear();

		srcCal.set(Calendar.YEAR, years);
		srcCal.set(Calendar.MONTH, months-1);
		srcCal.set(Calendar.DAY_OF_MONTH, days);

		srcCal.set(Calendar.HOUR_OF_DAY, hours);
		srcCal.set(Calendar.MINUTE, minutes);
		srcCal.set(Calendar.SECOND, seconds);
		srcCal.set(Calendar.MILLISECOND, milliseconds);
		if(useJdbcTzShift){
			srcCal = jdbcTimeZoneShift(srcCal, cal);
		}
		return new Timestamp(srcCal.getTimeInMillis());
	}




	private TimeZone getMyLocalTimeZone(String timeZone) {
		TimeZone tz;
		if(timeZone==null){
			tz = TimeZone.getDefault();
		}else{
			tz = TimeZone.getTimeZone(timeZone);
		}
		return tz;
	}

	private Calendar jdbcTimeZoneShift(Calendar srcCal, Calendar targetCal) throws SQLException{
		if(srcCal==null && targetCal==null){
			throw new SQLException("BOTH CALENDARS ARE NULL.");
//			return null;

		}else if(targetCal==null){
			targetCal = Calendar.getInstance(getMyLocalTimeZone(null));
		}

		targetCal.clear();
		targetCal.set(Calendar.YEAR, srcCal.get(Calendar.YEAR));
		targetCal.set(Calendar.MONTH, srcCal.get(Calendar.MONTH));
		targetCal.set(Calendar.DAY_OF_MONTH, srcCal.get(Calendar.DAY_OF_MONTH));

		targetCal.set(Calendar.HOUR_OF_DAY, srcCal.get(Calendar.HOUR_OF_DAY));
		targetCal.set(Calendar.MINUTE, srcCal.get(Calendar.MINUTE));
		targetCal.set(Calendar.SECOND, srcCal.get(Calendar.SECOND));
		targetCal.set(Calendar.MILLISECOND, srcCal.get(Calendar.MILLISECOND));

		return targetCal;
	}





	/**
	 * A great deal of this method and any supporting methods it uses
	 * (including inline comments) were copied or derived from the
	 * My-Sql Connector-J Driver (By MySql/Sun/Oracle).
	 *
	 * @param dateAsString
	 * @param targetCal
	 * @return java.sql.Date
	 * @throws SQLException
	 */
	protected java.sql.Date getDateFromString(String dateAsString, Calendar targetCal) throws SQLException {
		int year = 0;
		int month = 0;
		int day = 0;

		if (dateAsString!=null){
			dateAsString = dateAsString.trim();
		}

		if (dateAsString==null
			|| dateAsString.equals("0")
			|| dateAsString.equals("0000-00-00")
			|| dateAsString.equals("0000-00-00 00:00:00")
			|| dateAsString.equals("00000000000000"))
		{
			// '0001-01-01'.
			return createDate(targetCal, 1, 1, 1);
		}


		Pattern tStampPtn = Pattern.compile("\\d+&&^-&&^:|-&&\\s&&:|-", Pattern.CASE_INSENSITIVE);
		Matcher lookFor = tStampPtn.matcher(dateAsString);

		if (lookFor.find() && dateAsString.length()>=2) {
			// Convert from TIMESTAMP
			switch (dateAsString.length()) {
			case 21:
			case 19: // java.sql.Timestamp format
				year = Integer.parseInt(dateAsString.substring(0, 4));
				month = Integer.parseInt(dateAsString.substring(5, 7));
				day = Integer.parseInt(dateAsString.substring(8, 10));

				return createDate(targetCal, year, month, day);

			case 14:
			case 8:
				year = Integer.parseInt(dateAsString.substring(0, 4));
				month = Integer.parseInt(dateAsString.substring(4, 6));
				day = Integer.parseInt(dateAsString.substring(6, 8));

				return createDate(targetCal, year, month, day);

			case 12:
			case 6:
				year = Integer.parseInt(dateAsString.substring(0, 2));

				if (year <= 69) {
					year = year + 100;
				}

				month = Integer.parseInt(dateAsString.substring(2, 4));
				day = Integer.parseInt(dateAsString.substring(4, 6));

				return createDate(targetCal, year + 1900, month, day);

			case 10:
				year = Integer.parseInt(dateAsString.substring(0, 4));
				month = Integer.parseInt(dateAsString.substring(5, 7));
				day = Integer.parseInt(dateAsString.substring(8, 10));

				return createDate(targetCal, year, month, day);

			case 4:
				year = Integer.parseInt(dateAsString.substring(0, 4));

				if (year <= 69) {
					year = year + 100;
				}

				month = Integer.parseInt(dateAsString.substring(2, 4));

				return createDate(targetCal, year + 1900, month, 1);

			case 2:
				year = Integer.parseInt(dateAsString.substring(0, 2));

				if (year <= 69) {
					year = year + 100;
				}

				return createDate(targetCal, year + 1900, 1, 1);

			default:
				return null;
//				throw new SQLException(Messages.getString(
//						"ResultSet.Bad_format_for_Date", new Object[] {
//						stringVal, Constants.integerValueOf(columnIndex) }),
//						"S1009");
			} /* endswitch */
		} else if (dateAsString.length()>0 && dateAsString.length()<=4) {

			if (dateAsString.length() == 2 || dateAsString.length() == 1) {
				year = Integer.parseInt(dateAsString);

				if (year <= 69) {
					year = year + 100;
				}

				year += 1900;
			} else {
				year = Integer.parseInt(dateAsString.substring(0, 4));
			}

			return createDate(targetCal, year, 1, 1);
		} else if (dateAsString.contains(":")) {
			return createDate(targetCal, 1970, 1, 1); // Return EPOCH
		} else {
			if (dateAsString.length() < 10) {
				if (dateAsString.length() == 8) {
					return createDate(targetCal, 1970, 1, 1); // Return EPOCH for TIME
				}

//				throw new SQLException(Messages.getString(
//						"ResultSet.Bad_format_for_Date", new Object[] {
//						stringVal, Constants.integerValueOf(columnIndex) }),
//						"S1009");
			}

			if (dateAsString.length() != 18) {
				year = Integer.parseInt(dateAsString.substring(0, 4));
				month = Integer.parseInt(dateAsString.substring(5, 7));
				day = Integer.parseInt(dateAsString.substring(8, 10));
			} else {
				StringTokenizer st = new StringTokenizer(dateAsString, "- ");

				year = Integer.parseInt(st.nextToken());
				month = Integer.parseInt(st.nextToken());
				day = Integer.parseInt(st.nextToken());
			}
			return createDate(targetCal, year, month, day);
		}
	}











	protected java.sql.Time getTimeFromString(String timeAsString, Calendar targetCal) throws SQLException {
		int hr = 0;
		int min = 0;
		int sec = 0;

		// JDK-6 doesn't like trailing whitespace
		//
		// Note this isn't a performance issue, other
		// than the iteration over the string, as String.trim()
		// will return a new string only if whitespace is present
		//
		if (timeAsString!=null){
			timeAsString = timeAsString.trim();
		}

		if (timeAsString==null
			|| timeAsString.equals("0")
			|| timeAsString.equals("0000-00-00")
			|| timeAsString.equals("0000-00-00 00:00:00")
			|| timeAsString.equals("00000000000000"))
		{
			// We're left with the case of 'round' to a time Java _can_
			// represent, which is '00:00:00'
			return createTime(targetCal, 0, 0, 0);
		}


		Pattern tStampPtn = Pattern.compile("\\d+&&^-&&^:|:", Pattern.CASE_INSENSITIVE);
		Matcher lookFor = tStampPtn.matcher(timeAsString);

		if (lookFor.find() && timeAsString.length()>=2) {
			// It's a timestamp
			int length = timeAsString.length();

			switch (length) {
			case 19: // YYYY-MM-DD hh:mm:ss
				hr = Integer.parseInt(timeAsString.substring(length-8, length-6));
				min = Integer.parseInt(timeAsString.substring(length-5, length-3));
				sec = Integer.parseInt(timeAsString.substring(length-2, length));
				break;

			case 14:
			case 12:
				hr = Integer.parseInt(timeAsString.substring(length - 6, length - 4));
				min = Integer.parseInt(timeAsString.substring(length - 4, length - 2));
				sec = Integer.parseInt(timeAsString.substring(length - 2, length));
				break;

			case 10:
				hr = Integer.parseInt(timeAsString.substring(6, 8));
				min = Integer.parseInt(timeAsString.substring(8, 10));
				sec = 0;
				break;

			case 8:
				hr = Integer.parseInt(timeAsString.substring(0, 2));
				min = Integer.parseInt(timeAsString.substring(3, 5));
				sec = Integer.parseInt(timeAsString.substring(6, 8));
				break;

			default:
//				throw new SQLException(
//						Messages.getString("ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257")
//						+ columnIndex + "(" + this.fields[columnIndex - 1] + ").",
//						"S1009");
			}/* end-switch */

		} else if (timeAsString.contains("-")) {
			return createTime(targetCal, 0, 0, 0); // midnight on the given date
		} else {
			// convert a String to a Time
			if ((timeAsString.length() != 5) && (timeAsString.length() != 8)) {
//				throw new SQLException(Messages
//						.getString("ResultSet.Bad_format_for_Time____267")
//						+ timeAsString
//						+ Messages.getString("ResultSet.___in_column__268")
//						+ columnIndex, "S1009");
			}else{
				hr = Integer.parseInt(timeAsString.substring(0, 2));
				min = Integer.parseInt(timeAsString.substring(3, 5));
				sec = (timeAsString.length() == 5) ? 0 : Integer.parseInt(timeAsString.substring(6));
			}
		}
		return createTime(targetCal, hr, min, sec);
	}














	protected java.sql.Timestamp getTimestampFromString(String timestampValue, Calendar targetCal) throws java.sql.SQLException {

		int length = 0;
		if (timestampValue!=null){
			timestampValue = timestampValue.trim();
			length = timestampValue.length();
		}

		if ((timestampValue==null)
			|| length > 0 && timestampValue.charAt(0) == '0'
			&& (timestampValue.equals("0000-00-00")
			|| timestampValue.equals("0000-00-00 00:00:00")
			|| timestampValue.equals("00000000000000")
			|| timestampValue.equals("0")) )
		{
			// '0001-01-01'.
			return createTimestamp(targetCal, 1, 1, 1, 0, 0, 0, 0);

		}

		Pattern tStampPtn = Pattern.compile("\\d+&&^-&&^:", Pattern.CASE_INSENSITIVE);
		Matcher lookFor = tStampPtn.matcher(timestampValue);

		if (timestampValue.length()>0 && timestampValue.length()<=4 && lookFor.find()) {
			return createTimestamp(targetCal, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0);

		} else {
			if (timestampValue.endsWith(".")) {
				timestampValue = timestampValue.substring(0, timestampValue.length() - 1);
			}

			// Convert from TIMESTAMP or DATE
			switch (length) {
			case 26:
			case 25:
			case 24:
			case 23:
			case 22:
			case 21:
			case 20:
			case 19: {
				int year = Integer.parseInt(timestampValue.substring(0, 4));
				int month = Integer.parseInt(timestampValue.substring(5, 7));
				int day = Integer.parseInt(timestampValue.substring(8, 10));
				int hour = Integer.parseInt(timestampValue.substring(11, 13));
				int minutes = Integer.parseInt(timestampValue.substring(14, 16));
				int seconds = Integer.parseInt(timestampValue.substring(17, 19));
				int nanos = 0;

				if (length > 19) {
					int decimalIndex = timestampValue.lastIndexOf('.');

					if (decimalIndex != -1) {
						if ((decimalIndex + 2) <= timestampValue.length()) {
							nanos = Integer.parseInt(timestampValue.substring(decimalIndex + 1));
						} else {
							throw new IllegalArgumentException();
							// re-thrown further down with a
							// much better error message
						}
					}
				}
				return createTimestamp(targetCal, year, month, day, hour, minutes, seconds, nanos);
			}

			case 14: {
				int year = Integer.parseInt(timestampValue.substring(0, 4));
				int month = Integer.parseInt(timestampValue.substring(4, 6));
				int day = Integer.parseInt(timestampValue.substring(6, 8));
				int hour = Integer.parseInt(timestampValue.substring(8, 10));
				int minutes = Integer.parseInt(timestampValue.substring(10, 12));
				int seconds = Integer.parseInt(timestampValue.substring(12, 14));

				return createTimestamp(targetCal, year, month, day, hour, minutes, seconds, 0);
			}

			case 12: {
				int year = Integer.parseInt(timestampValue.substring(0, 2));

				if (year <= 69) {
					year = (year + 100);
				}

				int month = Integer.parseInt(timestampValue.substring(2, 4));
				int day = Integer.parseInt(timestampValue.substring(4, 6));
				int hour = Integer.parseInt(timestampValue.substring(6, 8));
				int minutes = Integer.parseInt(timestampValue.substring(8, 10));
				int seconds = Integer.parseInt(timestampValue.substring(10, 12));

				return createTimestamp(targetCal, year + 1900, month, day, hour, minutes, seconds, 0);
			}

			case 10: {
				int year;
				int month;
				int day;
				int hour;
				int minutes;

				if (timestampValue.indexOf("-") != -1) {
					year = Integer.parseInt(timestampValue.substring(0, 4));
					month = Integer.parseInt(timestampValue.substring(5, 7));
					day = Integer.parseInt(timestampValue.substring(8, 10));
					hour = 0;
					minutes = 0;
				} else {
					year = Integer.parseInt(timestampValue.substring(0, 2));

					if (year <= 69) {
						year = (year + 100);
					}

					month = Integer.parseInt(timestampValue.substring(2, 4));
					day = Integer.parseInt(timestampValue.substring(4, 6));
					hour = Integer.parseInt(timestampValue.substring(6, 8));
					minutes = Integer.parseInt(timestampValue.substring(8, 10));

					year += 1900; // two-digit year
				}

				return createTimestamp(targetCal, year, month, day, hour, minutes, 0, 0);
			}

			case 8: {
				if (timestampValue.indexOf(":") != -1) {
					int hour = Integer.parseInt(timestampValue.substring(0, 2));
					int minutes = Integer.parseInt(timestampValue.substring(3, 5));
					int seconds = Integer.parseInt(timestampValue.substring(6, 8));

					return createTimestamp(targetCal, 1970, 1, 1, hour, minutes, seconds, 0);
				}

				int year = Integer.parseInt(timestampValue.substring(0, 4));
				int month = Integer.parseInt(timestampValue.substring(4, 6));
				int day = Integer.parseInt(timestampValue.substring(6, 8));

				return createTimestamp(targetCal, year - 1900, month - 1, day, 0, 0, 0, 0);
			}

			case 6: {
				int year = Integer.parseInt(timestampValue.substring(0, 2));

				if (year <= 69) {
					year = (year + 100);
				}

				int month = Integer.parseInt(timestampValue.substring(2, 4));
				int day = Integer.parseInt(timestampValue.substring(4, 6));

				return createTimestamp(targetCal, year + 1900, month, day, 0, 0, 0, 0);
			}

			case 4: {
				int year = Integer.parseInt(timestampValue.substring(0, 2));

				if (year <= 69) {
					year = (year + 100);
				}

				int month = Integer.parseInt(timestampValue.substring(2, 4));

				return createTimestamp(targetCal, year + 1900, month, 1, 0, 0, 0, 0);
			}

			case 2: {
				int year = Integer.parseInt(timestampValue.substring(0, 2));

				if (year <= 69) {
					year = (year + 100);
				}

				return createTimestamp(targetCal, year + 1900, 1, 1, 0, 0, 0, 0);
			}

			default:
				return null;
//				throw new SQLException(
//						"Bad format for Timestamp '" + timestampValue + "' in column " + columnIndex + ".",
//						"S1009");
			}
		}
	}

}