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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

import com.jdbwc.exceptions.NotImplemented;
import com.ozdevworx.dtype.DataHandler;

/**
 * NOTE: This class has not been implemented yet!!!<br />
 * Partial implementation is in place for basic types, but
 * updateRow(), deleteRow() and insertRow() are not fully functional
 * in this release.
 *
 * @author Tim Gall
 * @version 2008-05-29
 * @version 2010-04-25 moved in some update methods from WCResultSet
 * @version 2010-05-22 covered some basic implementation ground-work
 */
public class WCResultSetUpdates {

	//---------------------------------------------------------- fields

	protected transient int myPointer = -1;
	protected transient int myDbType = 0;

	protected transient DataHandler myRows;
	protected transient DataHandler myRow;
//	protected transient int myRowLength = 0;
	protected transient WCStatement myStatement = null;
	protected transient WCConnection myConnection = null;

	//---------------------------------------------------------- constructors

	protected WCResultSetUpdates() {
		super();
	}

	//---------------------------------------------------------- public methods

	/**
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	public synchronized void cancelRowUpdates() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("cancelRowUpdates()");
	}

	/**
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public synchronized void moveToCurrentRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("moveToCurrentRow()");
	}

	/**
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	public synchronized void moveToInsertRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("moveToInsertRow()");
	}

	/**
	 * @see java.sql.ResultSet#insertRow()
	 */
	public synchronized void insertRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("insertRow()");
	}

	/**
	 * @see java.sql.ResultSet#updateRow()
	 */
	public synchronized void updateRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateRow()");
	}


	/**
	 * @see java.sql.ResultSet#deleteRow()
	 */
	public synchronized void deleteRow() throws SQLException {
		myRows.removeByIndex(myPointer);
	}

	/**
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	public boolean rowDeleted() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("rowDeleted()");
	}

	/**
	 * @see java.sql.ResultSet#rowInserted()
	 */
	public boolean rowInserted() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("rowInserted()");
	}

	/**
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	public boolean rowUpdated() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("rowUpdated()");
	}

	/**
	 * @see java.sql.ResultSet#refreshRow()
	 */
	public void refreshRow() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("refreshRow()");
	}

	public synchronized void updateArray(int columnIndex, Array x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateArray(int columnIndex, Array x)");
	}

	public synchronized void updateArray(String columnLabel, Array x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateArray(String columnLabel, Array x)");
	}

	public synchronized void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateAsciiStream(...)");
	}

	public synchronized void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBigDecimal(int columnIndex, BigDecimal x)");
	}

	public synchronized void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBigDecimal(String columnLabel, BigDecimal x)");
	}

	public synchronized void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBinaryStream(...)");
	}

	public synchronized void updateBlob(int columnIndex, Blob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBlob(String columnLabel, Blob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBlob(...)");
	}

	public synchronized void updateBoolean(int columnIndex, boolean x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBoolean(int columnIndex, boolean x)");
	}

	public synchronized void updateBoolean(String columnLabel, boolean x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBoolean(String columnLabel, boolean x)");
	}

	public synchronized void updateByte(int columnIndex, byte x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateByte(int columnIndex, byte x)");
	}

	public synchronized void updateByte(String columnLabel, byte x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateByte(String columnLabel, byte x)");
	}

	public synchronized void updateBytes(int columnIndex, byte[] x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBytes(int columnIndex, byte[] x)");
	}

	public synchronized void updateBytes(String columnLabel, byte[] x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateBytes(String columnLabel, byte[] x)");
	}

	public synchronized void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateCharacterStream(...)");
	}

	public synchronized void updateClob(int columnIndex, Clob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateClob(String columnLabel, Clob x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateClob(int columnIndex, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateClob(String columnLabel, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateClob(...)");
	}

	public synchronized void updateDate(int columnIndex, Date x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateDate(int columnIndex, Date x)");
	}

	public synchronized void updateDate(String columnLabel, Date x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateDate(String columnLabel, Date x)");
	}

	public synchronized void updateDouble(int columnIndex, double x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateDouble(String columnLabel, double x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateFloat(int columnIndex, float x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateFloat(String columnLabel, float x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateInt(int columnIndex, int x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateInt(String columnLabel, int x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateLong(int columnIndex, long x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateLong(String columnLabel, long x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNCharacterStream(int columnIndex, Reader x)");
	}

	public synchronized void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNCharacterStream(String columnLabel, Reader reader)");
	}

	public synchronized void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNCharacterStream(int columnIndex, Reader x, long length)");
	}

	public synchronized void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNCharacterStream(String columnLabel, Reader reader, long length)");
	}

	public synchronized void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(int columnIndex, NClob nClob)");
	}

	public synchronized void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(String columnLabel, NClob nClob)");
	}

	public synchronized void updateNClob(int columnIndex, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(int columnIndex, Reader reader)");
	}

	public synchronized void updateNClob(String columnLabel, Reader reader) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(String columnLabel, Reader reader)");
	}

	public synchronized void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(int columnIndex, Reader reader, long length)");
	}

	public synchronized void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNClob(String columnLabel, Reader reader, long length)");
	}

	public synchronized void updateNString(int columnIndex, String nString) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNString(int columnIndex, String nString)");
	}

	public synchronized void updateNString(String columnLabel, String nString) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateNString(String columnLabel, String nString)");
	}

	public synchronized void updateNull(int columnIndex) throws SQLException {
		myRow.setData(columnIndex, "NULL");
	}

	public synchronized void updateNull(String columnLabel) throws SQLException {
		myRow.setData(columnLabel, "NULL");
	}

	public synchronized void updateObject(int columnIndex, Object x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateObject(String columnLabel, Object x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateObject(String columnLabel, Object x, int scaleOrLength)");
	}

	public synchronized void updateRef(int columnIndex, Ref x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateRef(int columnIndex, Ref x)");
	}

	public synchronized void updateRef(String columnLabel, Ref x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateRef(String columnLabel, Ref x)");
	}

	public synchronized void updateRowId(int columnIndex, RowId x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateRowId(int columnIndex, RowId x)");
	}

	public synchronized void updateRowId(String columnLabel, RowId x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateRowId(String columnLabel, RowId x)");
	}

	public synchronized void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateSQLXML(int columnIndex, SQLXML xmlObject)");
	}

	public synchronized void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateSQLXML(String columnLabel, SQLXML xmlObject)");
	}

	public synchronized void updateShort(int columnIndex, short x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateShort(String columnLabel, short x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateString(int columnIndex, String x) throws SQLException {
		myRow.setData(columnIndex, x);
	}

	public synchronized void updateString(String columnLabel, String x) throws SQLException {
		myRow.setData(columnLabel, x);
	}

	public synchronized void updateTime(int columnIndex, Time x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateTime(int columnIndex, Time x)");
	}

	public synchronized void updateTime(String columnLabel, Time x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateTime(String columnLabel, Time x)");
	}

	public synchronized void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateTimestamp(int columnIndex, Timestamp x)");
	}

	public synchronized void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("updateTimestamp(String columnLabel, Timestamp x)");
	}

}