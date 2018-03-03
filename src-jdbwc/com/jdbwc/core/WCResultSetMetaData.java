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
import com.jdbwc.exceptions.NotImplemented;

/**
 * Looks after the supply of ResultSetMetaData for ResultSets.
 *
 * @author Tim Gall
 * @version 2008-06
 * @version 2010-04-17
 */
public final class WCResultSetMetaData implements java.sql.ResultSetMetaData {

	//---------------------------------------------------- fields

	private transient SQLField[] myFields = null;

	//---------------------------------------------------- constructors

	/**
	 * Default constructor for generating static metadata for the
	 * java.sql metadata implementations
	 */
	protected WCResultSetMetaData(SQLField[] fields){
		this.myFields = fields;
	}

	/**
	 * Default constructor for retriving dynamic meta data from unknown queries
	 *
	 * @param sql query to generate metadata for
	 * @param connection The connection to use for generating metadata
	 * @throws SQLException if the returned metadata does not contain any data
	 */
	protected WCResultSetMetaData(String sql, WCConnection connection) throws SQLException {
		super();

		SQLResultsParser parser = new SQLResultsParser(connection, sql);

		myFields = parser.getFields();
		if (this.getColumnCount() == 0) {
			throw new SQLException(
					"ResultSetMetaData returned null.\n" +
					"This indicates the ResultSet is most likely empty or closed."
					, "S1009");
		}
	}

	//---------------------------------------------------- public methods

	/**
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public String getCatalogName(int column) throws SQLException {
		return getColumnField(column).getCatalog();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	public String getColumnClassName(int column) throws SQLException {
		return getColumnField(column).getClassName();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() throws SQLException {
		return myFields.length;
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	public int getColumnDisplaySize(int column) throws SQLException {
		return getColumnField(column).getLength();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int column) throws SQLException {
		return getColumnField(column).getColumnAlias();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int column) throws SQLException {
		return getColumnField(column).getColumnName();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int column) throws SQLException {
		return getColumnField(column).getJdbcSqlType();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int column) throws SQLException {
		return getColumnField(column).getJdbcSqlTypeName();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int column) throws SQLException {
		return getColumnField(column).getPrecision();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	public int getScale(int column) throws SQLException {
		return getColumnField(column).getScale();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	public String getSchemaName(int column) throws SQLException {
		return getColumnField(column).getSchema();
	}

	/**
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	public String getTableName(int column) throws SQLException {
		return getColumnField(column).getTable();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
	 */
	public boolean isAutoIncrement(int column) throws SQLException {
		return getColumnField(column).isAutoIndex();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	public boolean isCaseSensitive(int column) throws SQLException {
		return getColumnField(column).isCaseSensetive();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	public boolean isCurrency(int column) throws SQLException {
		return getColumnField(column).isCurrency();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
	 */
	public boolean isDefinitelyWritable(int column) throws SQLException {
		//TODO: check table and column perms
		throw new NotImplemented("isDefinitelyWritable(int column)");
	}

	/**
	 * @see java.sql.ResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int column) throws SQLException {
		return getColumnField(column).isNullable() ? 1 : 0;
	}

	/**
	 * @see java.sql.ResultSetMetaData#isReadOnly(int)
	 */
	public boolean isReadOnly(int column) throws SQLException {
		//TODO: check table and column perms
		return getColumnField(column).isAutoIndex();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isSearchable(int)
	 */
	public boolean isSearchable(int column) throws SQLException {
		return getColumnField(column).isSearchable();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	public boolean isSigned(int column) throws SQLException {
		return !getColumnField(column).isUnsigned();
	}

	/**
	 * @see java.sql.ResultSetMetaData#isWritable(int)
	 */
	public boolean isWritable(int column) throws SQLException {
		return !getColumnField(column).isAutoIndex();
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.cast(this);
	}

	//---------------------------------------------------- private methods

	/**
	 * Verify the column exists then return the sqlField related to this column.
	 * Throw an exception if the column is not found.
	 *
	 * @param column int - a column index
	 * @return one SQLField if the column index is valid
	 * @throws SQLException if the column index does not exist
	 */
	private SQLField getColumnField(int column) throws SQLException{
		if(column > 0 && column <= myFields.length){
			return myFields[column-1];
		}else{
			throw new SQLException(
					"Column Index "+column+" not found in this ResultSetMetaData.",
					"S0022");
		}
	}

}
