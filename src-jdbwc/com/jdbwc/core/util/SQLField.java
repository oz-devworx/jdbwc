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
package com.jdbwc.core.util;

import java.sql.Types;

import com.jdbwc.util.MySQLTypes;
import com.jdbwc.util.PgSQLTypes;


public class SQLField {

	private transient String myColumnName = "";
	private transient String myColumnAlias = "";
	private transient String myTable = "";
	private transient String myDatabase = "";
	private transient String myCollation = "";
	private transient String mySchema = "";
	private transient String myEngine = "";
	private transient String myCharsetName = "";
	private transient String myValueDefault = "";

	private transient String myJdbcSqlTypeName = "";
	private transient String myGenericSqlTypeName = "";

	// for ParameterMetaData only
	private transient String myFieldName = "";
	private transient String myMode = "UNKNOWN";

	private transient boolean myIsAutoIndex = false;
	private transient boolean myIsNullable = false;
	private transient boolean myIsPrimaryKey = false;
	private transient boolean myIsUniqueKey = false;
	private transient boolean myIsIndex = false;
	private transient boolean myIsUnsigned = false;

	private transient int myAutoindexValue = 0;
	private transient int myPrecision = 0;
	private transient int myScale = 0;
	private transient int myPrecisionAdjustFactor = 0;

	private transient int myJdbcSqlType = 0;
	private transient int myGenericSqlType = 0;

	private transient int myServerType = 0;


	/**
	 * Constructs a new instance of a ParameterMetaData Field.<br />
	 * Create a new Field Object containing database metadata
	 * populated with the supplied parameters.<br />
	 * Data is fetched for use from the Field Object
	 * using its public <i>get</i> methods.<br />
	 * <span style="color:red;">Field Objects are not designed to have
	 * thier contents altered once instantiated.</span>
	 *
	 * @param fieldName String
	 * @param typeName String
	 * @param isNullable boolean
	 * @param mode String
	 */
	public SQLField(
			int serverType,
			String fieldName,
			String typeName,
			boolean isNullable,
			String mode
	) {
		myServerType = serverType;

		myFieldName = fieldName;

		myGenericSqlTypeName = typeName;
		myJdbcSqlTypeName = typeName;

		myIsNullable = isNullable;
		myIsUnsigned = !isUnsigned(typeName);

		myMode = mode;
		myPrecision = 0;


		if(typeName.contains("(")){
			myGenericSqlTypeName = typeName.substring(0, typeName.indexOf('('));

			String bracketContent = typeName.substring(typeName.indexOf('(')+1, typeName.indexOf(')'));
			if(bracketContent.contains(",")){
				String[] decimalSettings = bracketContent.split(",");
				try {
					myPrecision = Integer.parseInt(decimalSettings[0].trim());
					myScale = Integer.parseInt(decimalSettings[1].trim());
				} catch (NumberFormatException e) {
					myScale = 0;
				}

			}else{
				try {
					myPrecision = Integer.parseInt(bracketContent.trim());
				} catch (NumberFormatException e) {
					myPrecision = 0;
				}
				myScale = 0;
			}
		}


		if(myServerType==com.jdbwc.util.Util.ID_POSTGRESQL){
			// Map PgSql.Types to java.sql.Types
			myGenericSqlType = PgSQLTypes.pgsqlNameToType(myGenericSqlTypeName);
			myJdbcSqlType = PgSQLTypes.pgsqlToJavaType(myGenericSqlTypeName);
			myJdbcSqlTypeName = PgSQLTypes.pgsqlNameToJdbcName(myGenericSqlTypeName);

			if (myJdbcSqlType == Types.TINYINT && myPrecision == 1) {
				// Adjust for pseudo-boolean
				myJdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType() && !isNativeDateTimeType()) {
				//
				// Handle TEXT type (special case), Fix proposed by Peter McKeown
				//
				if ((myJdbcSqlType == java.sql.Types.LONGVARBINARY)) {
					myJdbcSqlType = java.sql.Types.LONGVARCHAR;
				} else if ((myJdbcSqlType == java.sql.Types.VARBINARY)) {
					myJdbcSqlType = java.sql.Types.VARCHAR;
				}
			}

			//
			// Handle odd values for 'M' for floating point/decimal numbers
			//
			myPrecisionAdjustFactor = 0;
			if (!isUnsigned(typeName)) {
				switch (myGenericSqlType) {
				case PgSQLTypes.NUMERIC:
					myPrecisionAdjustFactor = -1;

					break;
				case PgSQLTypes.MONEY:
				case PgSQLTypes.FLOAT4:
				case PgSQLTypes.FLOAT8:
					myPrecisionAdjustFactor = 1;

					break;
				}
			} else {
				switch (myGenericSqlType) {
				case PgSQLTypes.FLOAT4:
				case PgSQLTypes.FLOAT8:
					myPrecisionAdjustFactor = 1;

					break;
				}
			}
		}else{
			// Map MySql.Types to java.sql.Types
			myGenericSqlType = MySQLTypes.mysqlNameToType(myGenericSqlTypeName);
			myJdbcSqlType = MySQLTypes.mysqlToJavaType(myGenericSqlTypeName);
			myJdbcSqlTypeName = MySQLTypes.mysqlNameToJdbcName(myGenericSqlTypeName);

//			// Re-map to 'real' blob type, if we're a BLOB
//			if (myJdbcSqlType == MySQLTypes.FIELD_TYPE_BLOB) {
//
//			    if (true) {
//			        myJdbcSqlType = Types.VARCHAR;
//			        mySqlType = MySQLTypes.FIELD_TYPE_VARCHAR;
//				} else {
//					// *TEXT masquerading as blob
//					mySqlType = MySQLTypes.FIELD_TYPE_VAR_STRING;
//					myJdbcSqlType = Types.LONGVARCHAR;
//				}
//			}

			if (myJdbcSqlType == Types.TINYINT && myPrecision == 1) {
				// Adjust for pseudo-boolean
				myJdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType() && !isNativeDateTimeType()) {
				//
				// Handle TEXT type (special case), Fix proposed by Peter McKeown
				//
				if ((myJdbcSqlType == java.sql.Types.LONGVARBINARY)) {
					myJdbcSqlType = java.sql.Types.LONGVARCHAR;
				} else if ((myJdbcSqlType == java.sql.Types.VARBINARY)) {
					myJdbcSqlType = java.sql.Types.VARCHAR;
				}
			}

			//
			// Handle odd values for 'M' for floating point/decimal numbers
			//
			myPrecisionAdjustFactor = 0;
			if (!isUnsigned(typeName)) {
				switch (myGenericSqlType) {
				case MySQLTypes.FIELD_TYPE_DECIMAL:
				case MySQLTypes.FIELD_TYPE_NEW_DECIMAL:
					myPrecisionAdjustFactor = -1;

					break;
				case MySQLTypes.FIELD_TYPE_DOUBLE:
				case MySQLTypes.FIELD_TYPE_FLOAT:
					myPrecisionAdjustFactor = 1;

					break;
				}
			} else {
				switch (myGenericSqlType) {
				case MySQLTypes.FIELD_TYPE_DOUBLE:
				case MySQLTypes.FIELD_TYPE_FLOAT:
					myPrecisionAdjustFactor = 1;

					break;
				}
			}
		}
	}

	/**
	 * Constructs a new instance of a ResultSetMetaData Field.<br />
	 * Data is fetched for use from the Field Object
	 * using its public <i>get</i> methods.<br />
	 * <span style="color:red;">Field Objects are not designed to have
	 * thier contents altered once instantiated.</span><br />
	 * Small portions of this constructor were copied from MySql-Connector/J
	 *
	 * @param columnName String
	 * @param columnAlias String
	 * @param table String
	 * @param database String
	 * @param collation String
	 * @param schema String
	 * @param engine String
	 * @param charsetName String
	 * @param sqlTypeName String
	 * @param valueDefault String
	 *
	 * @param isAutoIndex boolean
	 * @param isNullable boolean
	 * @param isPrimaryKey boolean
	 * @param isUniqueKey boolean
	 * @param isIndex boolean
	 *
	 * @param length int
	 * @param autoindexValue int
	 */
	public SQLField(
		int serverType,
		String columnName,
		String columnAlias,
		String table,
		String database,
		String collation,
		String schema,
		String engine,
		String charsetName,
		String sqlTypeName,
		String valueDefault,
		boolean isAutoIndex,
		boolean isNullable,
		boolean isPrimaryKey,
		boolean isUniqueKey,
		boolean isIndex,
		int length,
		int autoindexValue
	) {
//		synchronized(this){
			myServerType = serverType;

			myColumnName = columnName;
			myColumnAlias = columnAlias.equals("") ? columnName : columnAlias;
			myTable = table;
			myDatabase = database;
			myCollation = collation;
			mySchema = schema;
			myEngine = engine;
			myValueDefault = valueDefault;
			myCharsetName = charsetName;

			myIsUnsigned = isUnsigned(sqlTypeName);
			myIsAutoIndex = isAutoIndex;
			myIsNullable = isNullable;

			myIsPrimaryKey = isPrimaryKey;
			myIsUniqueKey = isUniqueKey;
			myIsIndex = isIndex;

			myAutoindexValue = (isAutoIndex) ? autoindexValue : -1;
			myPrecisionAdjustFactor = 0;
//		}

			if(sqlTypeName.contains("(")){
				myGenericSqlTypeName = sqlTypeName.substring(0, sqlTypeName.indexOf('('));
				String bracketContent = sqlTypeName.substring(sqlTypeName.indexOf('(')+1, sqlTypeName.indexOf(')'));

				if(bracketContent.contains(",")){
					String[] decimalSettings = bracketContent.split(",");
					try {
						myPrecision = Integer.parseInt(decimalSettings[0]);
						myScale = Integer.parseInt(decimalSettings[1]);
					} catch (NumberFormatException e) {
						myPrecision = 0;
						myScale = 0;
					}
				}else{
					try {
						myPrecision = Integer.parseInt(bracketContent);
					} catch (NumberFormatException e) {
						myPrecision = 0;
					}
					myScale = 0;
				}
			}else{
				myGenericSqlTypeName = sqlTypeName;
			}


		/* NOTES:
		 * Following are some adjustments to some data types
		 */
		if(myServerType==com.jdbwc.util.Util.ID_POSTGRESQL){
			// Map native sql Types to java.sql Types
			myGenericSqlType = PgSQLTypes.pgsqlNameToType(myGenericSqlTypeName);
			myJdbcSqlType = PgSQLTypes.pgsqlToJavaType(myGenericSqlTypeName);
			myJdbcSqlTypeName = PgSQLTypes.pgsqlNameToJdbcName(myGenericSqlTypeName);

			if (myJdbcSqlType == Types.TINYINT && myPrecision == 1) {
				myJdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType() && !isNativeDateTimeType()) {
				if ((myJdbcSqlType == java.sql.Types.LONGVARBINARY)) {
					myJdbcSqlType = java.sql.Types.LONGVARCHAR;
				} else if ((myJdbcSqlType == java.sql.Types.VARBINARY)) {
					myJdbcSqlType = java.sql.Types.VARCHAR;
				}
			} else {
				myCharsetName = "US-ASCII";
			}

			myPrecisionAdjustFactor = 0;
		}else{
			// Map native sql to java.sql Types
			myGenericSqlType = MySQLTypes.mysqlNameToType(myGenericSqlTypeName);
			myJdbcSqlType = MySQLTypes.mysqlToJavaType(myGenericSqlTypeName);
			myJdbcSqlTypeName = MySQLTypes.mysqlNameToJdbcName(myGenericSqlTypeName);

//				// Re-map to 'real' blob type, if its a BLOB
//				if (myJdbcSqlType == MySQLTypes.FIELD_TYPE_BLOB) {
//
//				    if (true) {
//				        myJdbcSqlType = Types.VARCHAR;
//				        myGenericSqlType = MySQLTypes.FIELD_TYPE_VARCHAR;
//					} else {
//						// *TEXT masquerading as blob
//						myJdbcSqlType = Types.LONGVARCHAR;
//						myGenericSqlType = MySQLTypes.FIELD_TYPE_VAR_STRING;
//					}
//				}

			if (myJdbcSqlType == Types.TINYINT && myPrecision == 1) {
				// Adjust for pseudo-boolean
				myJdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType() && !isNativeDateTimeType()) {
				//
				// Handle TEXT type (special case), Fix proposed by Peter McKeown
				//
				if ((myJdbcSqlType == java.sql.Types.LONGVARBINARY)) {
					myJdbcSqlType = java.sql.Types.LONGVARCHAR;
				} else if ((myJdbcSqlType == java.sql.Types.VARBINARY)) {
					myJdbcSqlType = java.sql.Types.VARCHAR;
				}
			} else {
				myCharsetName = "US-ASCII";
			}

			this.myPrecisionAdjustFactor = 0;
			if (!isUnsigned(sqlTypeName)) {
				switch (myGenericSqlType) {
				case MySQLTypes.FIELD_TYPE_DECIMAL:
				case MySQLTypes.FIELD_TYPE_NEW_DECIMAL:
					myPrecisionAdjustFactor = -1;

					break;
				case MySQLTypes.FIELD_TYPE_DOUBLE:
				case MySQLTypes.FIELD_TYPE_FLOAT:
					myPrecisionAdjustFactor = 1;

					break;
				}
			} else {
				switch (myGenericSqlType) {
				case MySQLTypes.FIELD_TYPE_DOUBLE:
				case MySQLTypes.FIELD_TYPE_FLOAT:
					myPrecisionAdjustFactor = 1;

					break;
				}
			}
		}
	}

	/**
	 * @return the autoindexValue
	 */
	public int getAutoindexValue() {
		return myAutoindexValue;
	}

	/**
	 * @return the charsetName
	 */
	public String getCharsetName() {
		return myCharsetName;
	}

	/**
	 * @return the ClassName
	 */
	public String getClassName() {
		return "java.sql." + myJdbcSqlTypeName;
	}

	/**
	 * @return the collation
	 */
	public String getCollation() {
		return myCollation;
	}

	/**
	 * @return the columnAlias
	 */
	public String getColumnAlias() {
		return myColumnAlias;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return myColumnName;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return myDatabase;
	}

	/**
	 * @return the engine
	 */
	public String getEngine() {
		return myEngine;
	}

	/**
	 * @return the FieldName
	 */
	public String getFieldName() {
		return myFieldName;
	}

	/**
	 * @return the JdbcSqlType
	 */
	public int getJdbcSqlType() {
		return myJdbcSqlType;
	}

	/**
	 * @return the JdbcSqlTypeName
	 */
	public String getJdbcSqlTypeName() {
		return myJdbcSqlTypeName;
	}

	/**
	 * @return the Mode
	 */
	public String getMode() {
		return myMode;
	}

	/**
	 * @return the dataType
	 */
	public int getGenericSqlType() {
		return myGenericSqlType;
	}

	/**
	 * @return the mySqlTypeName
	 */
	public String getGenericSqlTypeName() {
		return myGenericSqlTypeName;
	}

	/**
	 * @return the Precision
	 */
	public int getPrecision() {
		return myPrecision;
	}

	/**
	 * @return the myPrecisionAdjustFactor
	 */
	public int getPrecisionAdjustFactor() {
		return myPrecisionAdjustFactor;
	}

	/**
	 * @return the Scale
	 */
	public int getScale() {
		return myScale;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return mySchema;
	}

	/**
	 * @return the myTable
	 */
	public String getTable() {
		return myTable;
	}

	/**
	 * @return the valueDefault
	 */
	public String getValueDefault() {
		return myValueDefault;
	}

	/**
	 * @return the isAutoIndex
	 */
	public boolean isAutoIndex() {
		return myIsAutoIndex;
	}

	/**
	 * @return the isIndex
	 */
	public boolean isIndex() {
		return myIsIndex;
	}

	/**
	 * @return the IsNullable
	 */
	public boolean isNullable() {
		return myIsNullable;
	}

	/**
	 * @return the IsSigned
	 */
	public boolean isSigned() {
		return !myIsUnsigned;
	}

	/**
	 * @return the isPrimaryKey
	 */
	public boolean isPrimaryKey() {
		return myIsPrimaryKey;
	}

	/**
	 * @return the isUniqueKey
	 */
	public boolean isUniqueKey() {
		return myIsUniqueKey;
	}

	public boolean isUnsigned() {
		return this.myIsUnsigned;
	}

	/**
	 * This is for debugging to make it easy to see whats in this SQLField.
	 *
	 * @return This SQLResultsField as a human readable String.
	 */
	@Override
	public String toString() {
		final String _NL_ = System.getProperty("line.separator");
		StringBuilder output = new StringBuilder();

		/* if myFieldName isEmpty the field is a ResultSetField */
		if(myFieldName.equals("")){
			output.append("SQLField[").append(_NL_);
			output.append("\tColumnName = ").append(getColumnName()).append(_NL_);
			output.append("\tColumnAlias = ").append(getColumnAlias()).append(_NL_);

			output.append("\tTable = ").append(getTable()).append(_NL_);
			output.append("\tDatabase = ").append(getDatabase()).append(_NL_);
			output.append("\tCollation = ").append(getCollation()).append(_NL_);
			output.append("\tSchema = ").append(getSchema()).append(_NL_);
			output.append("\tEngine = ").append(getEngine()).append(_NL_);
			output.append("\tCharsetName = ").append(getCharsetName()).append(_NL_);
			output.append("\tValueDefault = ").append(getValueDefault()).append(_NL_);

			output.append("\tIsAutoIndex = ").append(isAutoIndex()).append(_NL_);
			output.append("\tIsNullable = ").append(isNullable()).append(_NL_);
			output.append("\tIsPrimaryKey = ").append(isPrimaryKey()).append(_NL_);
			output.append("\tIsUniqueKey = ").append(isUniqueKey()).append(_NL_);
			output.append("\tIsIndex = ").append(isIndex()).append(_NL_);

			output.append("\tPrecision = ").append(getPrecision()).append(_NL_);
			output.append("\tScale = ").append(getScale()).append(_NL_);
			output.append("\tIsUnsigned = ").append(isUnsigned()).append(_NL_);

			output.append("\tAutoindexValue = ").append(getAutoindexValue()).append(_NL_);
			output.append("\tPrecisionAdjustFactor = ").append(getPrecisionAdjustFactor()).append(_NL_);

			output.append("\tJDBCSqlTypeName = ").append(getJdbcSqlTypeName()).append(_NL_);
			output.append("\tJdbcSqlType = ").append(getJdbcSqlType()).append(_NL_);
			output.append("\tGenericSqlTypeName = ").append(getGenericSqlTypeName()).append(_NL_);
			output.append("\tGenericSqlType = ").append(getGenericSqlType()).append(_NL_);

			output.append("]");
		/* Otherwise its a parameter field */
		}else{
			output.append("SQLField[").append(_NL_);
			output.append("\tFieldName = ").append(getFieldName()).append(_NL_);
			output.append("\tGenericSqlTypeName = ").append(getGenericSqlTypeName()).append(_NL_);
			output.append("\tClassName = ").append(getClassName()).append(_NL_);
			output.append("\tGenericSqlType = ").append(getGenericSqlType()).append(_NL_);
			output.append("\tJdbcSqlType = ").append(getJdbcSqlType()).append(_NL_);
			output.append("\tIsNullable = ").append(isNullable()).append(_NL_);
			output.append("\tIsSigned = ").append(isSigned()).append(_NL_);
			output.append("\tMode = ").append(getMode()).append(_NL_);
			output.append("\tPrecision = ").append(getPrecision()).append(_NL_);
			output.append("\tScale = ").append(getScale()).append(_NL_);
			output.append("]");
		}
		return output.toString();
	}



	private boolean isUnsigned(String typeName) {
		return typeName.endsWith("unsigned");
	}

	private boolean isNativeDateTimeType() {
		boolean nativeDateTime = false;
		if(myServerType==com.jdbwc.util.Util.ID_POSTGRESQL){
			nativeDateTime = (
					myGenericSqlType == PgSQLTypes.TIMETZ ||
					myGenericSqlType == PgSQLTypes.TIMESTAMPTZ ||
					myGenericSqlType == PgSQLTypes.DATE ||
					myGenericSqlType == PgSQLTypes.TIME ||
					myGenericSqlType == PgSQLTypes.TIMESTAMP);
		}else{
			nativeDateTime = (
					myGenericSqlType == MySQLTypes.FIELD_TYPE_DATE ||
					myGenericSqlType == MySQLTypes.FIELD_TYPE_NEWDATE ||
					myGenericSqlType == MySQLTypes.FIELD_TYPE_DATETIME ||
					myGenericSqlType == MySQLTypes.FIELD_TYPE_TIME ||
					myGenericSqlType == MySQLTypes.FIELD_TYPE_TIMESTAMP);
		}
		return nativeDateTime;
	}

	private boolean isNativeNumericType() {
		return isNativeNumericType(myGenericSqlType);
	}

	private boolean isNativeNumericType(int inputType) {
		boolean nativeNumeric = false;

		if(myServerType==com.jdbwc.util.Util.ID_POSTGRESQL){
			nativeNumeric = (
					inputType == PgSQLTypes.OID ||
					inputType == PgSQLTypes.INT2 ||
					inputType == PgSQLTypes.INT4 ||
					inputType == PgSQLTypes.INT8 ||
					inputType == PgSQLTypes.FLOAT4 ||
					inputType == PgSQLTypes.FLOAT8 ||
					inputType == PgSQLTypes.NUMERIC ||
					inputType == PgSQLTypes.MONEY);
		}else{
			nativeNumeric = (
					(inputType >= MySQLTypes.FIELD_TYPE_TINY &&
					inputType <= MySQLTypes.FIELD_TYPE_DOUBLE) ||
					inputType == MySQLTypes.FIELD_TYPE_LONGLONG ||
					inputType == MySQLTypes.FIELD_TYPE_YEAR);
		}
		return nativeNumeric;
	}

}
