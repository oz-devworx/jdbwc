/* ********************************************************************
 * Copyright (C) 2010 Oz-DevWorX (Tim Gall)
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
package com.jdbwc.core.mysql;

import java.sql.SQLException;

import com.jdbwc.core.WCResultSet;
import com.jdbwc.util.SQLField;
import com.jdbwc.util.SQLResProcessor;
import com.jdbwc.util.SQLTypes;

/**
 * Process Internal ResultSetMetadata for MySQL databases.
 *
 * @author Tim Gall
 * @version 2010-06-06
 */
public class SQLResProcessorImp implements SQLResProcessor {

	/**
	 *
	 */
	public SQLResProcessorImp() {

	}

	/**
	 * @see com.jdbwc.util.SQLResProcessor#getFields(java.lang.String, com.jdbwc.core.WCResultSet)
	 */
	@Override
	public SQLField[] getFields(String catalog, WCResultSet metaRes) throws SQLException {
		final SQLField[] metaFields = new SQLField[metaRes.myRows.length()];
		int mrow = 0;
		boolean baseServer = metaRes.getConnection().isBaseServer();
		SQLTypes types = metaRes.getConnection().getTypes();
		while(metaRes.next()){

			SQLField field = null;
			if(baseServer){
				field = getFromMySql(catalog, metaRes, types);
			} else {
				field = getFromMySqli(catalog, metaRes);
			}

			if(field!=null)
				metaFields[mrow++] = field;

//			System.err.println("field = " + field.toString());

		}
		metaRes.close();

		return metaFields;
	}

	private SQLField getFromMySql(String catalog, WCResultSet metaRes, SQLTypes types) throws SQLException {
		/* MySQL
		==========================================
		name - column name
		table - name of the table the column belongs to
		def - default value of the column
		max_length - maximum length of the column
		not_null - 1 if the column cannot be NULL
		primary_key - 1 if the column is a primary key
		unique_key - 1 if the column is a unique key
		multiple_key - 1 if the column is a non-unique key
		numeric - 1 if the column is numeric
		blob - 1 if the column is a BLOB
		type - the type of the column
		unsigned - 1 if the column is unsigned
		zerofill - 1 if the column is zero-filled
		==========================================
		 */

		try {

//			System.err.println("--------------------");
//
//			System.err.println("name = " + metaRes.getString("name"));// - column name
//			System.err.println("table = " + metaRes.getString("table"));// - name of the table the column belongs to
//			System.err.println("def = " + metaRes.getString("def"));// - default value of the column
//			System.err.println("max_length = " + metaRes.getString("max_length"));// - maximum length of the column
//			System.err.println("not_null = " + metaRes.getString("not_null"));// - 1 if the column cannot be NULL
//			System.err.println("primary_key = " + metaRes.getString("primary_key"));// - 1 if the column is a primary key
//			System.err.println("unique_key = " + metaRes.getString("unique_key"));// - 1 if the column is a unique key
//			System.err.println("multiple_key = " + metaRes.getString("multiple_key"));// - 1 if the column is a non-unique key
//			System.err.println("numeric = " + metaRes.getString("numeric"));// - 1 if the column is numeric
//			System.err.println("blob = " + metaRes.getString("blob"));// - 1 if the column is a BLOB
//			System.err.println("type = " + metaRes.getString("type"));// - the type of the column
//			System.err.println("unsigned = " + metaRes.getString("unsigned"));// - 1 if the column is unsigned
//			System.err.println("zerofill = " + metaRes.getString("zerofill"));// - 1 if the column is zero-filled
//
//			System.err.println("--------------------");


			String collationName = null;//gets converted to collation name
			long maxLength = metaRes.getLong("max_length");
			long length = maxLength;//unknown
			long decimals = 0;//unknown

			// trim fields down to fit a Java.Integer
			maxLength = (maxLength > Integer.MAX_VALUE) ? Integer.MAX_VALUE : maxLength;
			length = (length > Integer.MAX_VALUE) ? Integer.MAX_VALUE : length;
			decimals = (decimals > Integer.MAX_VALUE) ? Integer.MAX_VALUE : decimals;


			boolean isNullable = (metaRes.getInt("not_null")==0);
			boolean isUnsigned = (metaRes.getInt("unsigned")==1);
			boolean isAutoIndex = false;//unknown

			boolean isPrimaryKey = (metaRes.getInt("primary_key")==1);
			boolean isUniqueKey = (metaRes.getInt("unique_key")==1);
			boolean isIndex = (metaRes.getInt("multiple_key")==1);

//			System.err.println("type = " + metaRes.getInt("type"));

			return new SQLField(
					catalog,
					null,
					metaRes.getString("table"),
					null,//unknown
					metaRes.getString("name"),
					metaRes.getString("def"),
					collationName,

					types.nativeNameToNativeType(metaRes.getString("type")),//native type gets converted by SQLField
					(int)maxLength,
					(int)length,
					(int)decimals,

					isNullable,
					isAutoIndex,
					isUnsigned,
					isPrimaryKey,
					isUniqueKey,
					isIndex);
		} catch (SQLException e) {
			return null;
		}
	}

	private SQLField getFromMySqli(String catalog, WCResultSet metaRes) {
		/* MySQLi
		==========================================
		name       = The name of the column
		orgname    = Original column name if an alias was specified
		table      = The name of the table this field belongs to (if not calculated)
		orgtable   = Original table name if an alias was specified
		def        = The default value for this field, represented as a string
		max_length = The maximum width of the field for the result set.
		length     = The width of the field, as specified in the table definition.
		charsetnr  = The character set number for the field.
		flags      = An integer representing the bit-flags for the field.
		type       = The data type used for this field
		decimals   = The number of decimals used (for integer fields)
		==========================================
		 */

		try {
			String collationName = com.jdbwc.core.mysql.MySQLCollations.getCollation(metaRes.getInt("charsetnr"));//gets converted to collation name
			long maxLength = metaRes.getLong("max_length");
			long length = metaRes.getLong("length");
			long decimals = metaRes.getLong("decimals");

			// trim fields down to fit a Java.Integer
			maxLength = (maxLength > Integer.MAX_VALUE) ? Integer.MAX_VALUE : maxLength;
			length = (length > Integer.MAX_VALUE) ? Integer.MAX_VALUE : length;
			decimals = (decimals > Integer.MAX_VALUE) ? Integer.MAX_VALUE : decimals;


			int bitFlags = metaRes.getInt("flags");
			/* MYSQLI BIT-FLAGS */
			boolean isNullable = !((bitFlags & 1) > 0);//MYSQLI_NOT_NULL_FLAG
			boolean isUnsigned = ((bitFlags & 32) > 0);//MYSQLI_UNSIGNED_FLAG
			boolean isAutoIndex = ((bitFlags & 512) > 0);//MYSQLI_AUTO_INCREMENT_FLAG

			boolean isPrimaryKey = ((bitFlags & 2) > 0);//MYSQLI_PRI_KEY_FLAG
			boolean isUniqueKey = ((bitFlags & 4) > 0);//MYSQLI_UNIQUE_KEY_FLAG
			boolean isIndex = ((bitFlags & 8) > 0);//MYSQLI_MULTIPLE_KEY_FLAG

//			System.err.println("type = " + metaRes.getInt("type"));

			return new SQLField(
					catalog,
					null,
					metaRes.getString("orgtable"),
					metaRes.getString("orgname"),
					metaRes.getString("name"),
					metaRes.getString("def"),
					collationName,

					metaRes.getInt("type"),//native type gets converted by SQLField
					(int)maxLength,
					(int)length,
					(int)decimals,

					isNullable,
					isAutoIndex,
					isUnsigned,
					isPrimaryKey,
					isUniqueKey,
					isIndex);
		} catch (SQLException e) {
			return null;
		}
	}

}
