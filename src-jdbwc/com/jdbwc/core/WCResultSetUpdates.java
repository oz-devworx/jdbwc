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

import com.ozdevworx.dtype.DataHandler;

/**
 * NOTE: This is only a skeleton and has not been implemented yet!!!
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public class WCResultSetUpdates {

	protected int myPointer = -1;
	protected int myDbType = 0;
	protected DataHandler myRows;
	protected DataHandler myRow;
	protected WCStatement myStatement = null;
	protected WCConnection myConnection = null;
	protected String mySQL = null;
	
	protected WCResultSetUpdates() {
		super();
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateArray(String columnLabel, Array x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateByte(String columnLabel, byte x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(String columnLabel, Clob x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateDate(String columnLabel, Date x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateDouble(String columnLabel, double x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateFloat(String columnLabel, float x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateInt(String columnLabel, int x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateLong(String columnLabel, long x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNString(int columnIndex, String nString) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNString(String columnLabel, String nString) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNull(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateNull(String columnLabel) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateObject(String columnLabel, Object x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateRef(String columnLabel, Ref x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateRow() throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateShort(String columnLabel, short x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateString(String columnLabel, String x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateTime(String columnLabel, Time x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		// TODO Auto-generated method stub
	
	}

}