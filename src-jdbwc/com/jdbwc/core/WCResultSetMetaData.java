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

import java.sql.SQLException;

import com.jdbwc.core.util.SQLField;
import com.jdbwc.core.util.SQLResultsParser;

public final class WCResultSetMetaData implements java.sql.ResultSetMetaData {

	private transient SQLField[] myFields = null;
	private transient WCConnection myConnection = null;

//	protected WCResultSetMetaData() {
//		super();
//		myFields = new SQLField[0];
//	}

	protected WCResultSetMetaData(String sql, WCConnection connection) throws SQLException {
		super();

		myConnection = connection;
		synchronized (myConnection) {
			SQLResultsParser parser = new SQLResultsParser(connection, sql);

			myFields = parser.getFields();
			if (this.getColumnCount() == 0) {
				throw new SQLException("ResultSetMetaData returned a null Object.\n" + "This indicates your ResultSet is most likely empty or closed.\n" + "Check your java sytax and logical conditions related to the resultSet that caused this error first.", "WCNOE");
			}
		}
	}

	public String getCatalogName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnClassName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getColumnCount() throws SQLException {
		return myFields.length;
	}

	public int getColumnDisplaySize(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getPrecision();
	}

	public String getColumnLabel(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getColumnAlias();
	}

	public String getColumnName(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getColumnName();
	}

	public int getColumnType(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getGenericSqlType();
	}

	public String getColumnTypeName(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}
//		System.err.println("===========" + myFields[column-1] + "===========");
		return myFields[column-1].getGenericSqlTypeName();
	}

	public int getPrecision(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getPrecision();
	}

	public int getScale(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getScale();
	}

	public String getSchemaName(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getSchema();
	}

	public String getTableName(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].getTable();
	}

	public boolean isAutoIncrement(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return !myFields[column-1].isAutoIndex();
	}

	public boolean isCaseSensitive(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCurrency(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDefinitelyWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public int isNullable(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return myFields[column-1].isNullable() ? 1 : 0;
	}

	public boolean isReadOnly(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSearchable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSigned(int column) throws SQLException {
		if(column-1 >= myFields.length){
			throw new SQLException(
					"Column Index Out Of Bounds Exception!",
					"S0022");
		}

		return !myFields[column-1].isUnsigned();
	}

	public boolean isWritable(int column) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
