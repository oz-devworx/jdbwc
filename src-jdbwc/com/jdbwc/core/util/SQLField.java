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


/**
 * An SQLField is designed to hold metadata for the metadata core classes.
 *
 * @author Tim Gall
 * @version 2008-05-29
 * @version 2010-04-26
 * @version 2010-05-05
 */
public class SQLField {

	//------------------------------------------------------- fields

	private transient String catalog = "";
	private transient String schema = "";
	private transient String tableName = "";

	private transient String columnName = "";
	private transient String columnAlias = "";

	private transient String collation = "";

	private transient String dbEngine = "";
	private transient String defaultValue = "";

	private transient String jdbcSqlTypeName = "";
	private transient String genericSqlTypeName = "";

	private transient int jdbcSqlType = 0;
	private transient int genericSqlType = 0;

	private transient boolean isPrimaryKey = false;
	private transient boolean isUniqueKey = false;
	private transient boolean isIndex = false;
	private transient boolean isUnsigned = false;

	private transient boolean isAutoIndex = false;
	private transient boolean isNullable = false;
	private transient boolean isSearchable = true;
	private transient boolean isCurrency = false;

	private transient int fieldLength = 0;
	private transient int precision = 0;
	private transient int scale = 0;
	private transient int precisionAdjustFactor = 0;

	private transient int serverType = 0;

	// for ParameterMetaData only
	private transient String fieldName = "";
	private transient String mode = "UNKNOWN";

	//------------------------------------------------------- constructors

	/**
	 * Constructs a new instance of a ParameterMetaData Field.<br />
	 * Create a new Field Object containing database metadata
	 * populated with the supplied parameters.<br />
	 * Data is fetched for use from the Field Object
	 * using its public <i>get</i> methods.<br />
	 * <span style="color:red;">SQLField Objects are not designed to have
	 * thier contents altered once instantiated.</span>
	 *
	 * @param fieldName String
	 * @param typeName String
	 * @param isNullable boolean
	 * @param mode String
	 */
	public SQLField(
			final int serverType,
			final String fieldName,
			final String typeName,
			final boolean isNullable,
			final String mode
	) {
		this.serverType = serverType;

		this.fieldName = fieldName;

		this.genericSqlTypeName = typeName;
		this.jdbcSqlTypeName = typeName;

		this.isNullable = isNullable;
		this.isUnsigned = !isUnsigned(typeName);

		this.mode = mode;
		this.precision = 0;


		if(typeName.contains("(")){
			this.genericSqlTypeName = typeName.substring(0, typeName.indexOf('('));

			final String bracketContent = typeName.substring(typeName.indexOf('(')+1, typeName.indexOf(')'));
			if(bracketContent.contains(",")){
				final String[] decimalSettings = bracketContent.split(",");
				try {
					this.precision = Integer.parseInt(decimalSettings[0].trim());
					this.scale = Integer.parseInt(decimalSettings[1].trim());
				} catch (final NumberFormatException e) {
					this.scale = 0;
				}

			}else{
				try {
					this.precision = Integer.parseInt(bracketContent.trim());
				} catch (final NumberFormatException e) {
					this.precision = 0;
				}
				this.scale = 0;
			}
		}


		if(this.serverType==com.jdbwc.util.Util.ID_POSTGRESQL){
			// Map PgSql.Types to java.sql.Types
			this.genericSqlType = PgSQLTypes.pgsqlNameToPgsqlType(this.genericSqlTypeName);
			this.jdbcSqlType = PgSQLTypes.pgsqlNameToJdbcType(this.genericSqlTypeName);
			this.jdbcSqlTypeName = PgSQLTypes.pgsqlNameToJdbcName(this.genericSqlTypeName);

			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
				// Adjust for pseudo-boolean
				this.jdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
				//
				// Handle TEXT type (special case), Fix proposed by Peter McKeown
				//
				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
					this.jdbcSqlType = Types.LONGVARCHAR;
				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
					this.jdbcSqlType = Types.VARCHAR;
				}
			}

			this.isSearchable = true;//TODO: add postgres value
			this.isCurrency = (this.genericSqlType==PgSQLTypes.MONEY || this.genericSqlType==PgSQLTypes.MONEY_ARRAY);

			//
			// Handle odd values for 'M' for floating point/decimal numbers
			//
			this.precisionAdjustFactor = 0;
			if (!isUnsigned(typeName)) {
				switch (this.genericSqlType) {
				case PgSQLTypes.NUMERIC:
					this.precisionAdjustFactor = -1;

					break;
				case PgSQLTypes.MONEY:
				case PgSQLTypes.FLOAT4:
				case PgSQLTypes.FLOAT8:
					this.precisionAdjustFactor = 1;

					break;
				}
			} else {
				switch (this.genericSqlType) {
				case PgSQLTypes.FLOAT4:
				case PgSQLTypes.FLOAT8:
					this.precisionAdjustFactor = 1;

					break;
				}
			}
		}else{
			// Map MySql.Types to Types
			this.genericSqlType = MySQLTypes.mysqlNameToJdbcType(this.genericSqlTypeName);
			this.jdbcSqlType = MySQLTypes.mysqlTypeToJdbcType(this.genericSqlType);
			this.jdbcSqlTypeName = MySQLTypes.mysqlNameToJdbcName(this.genericSqlTypeName);

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

			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
				// Adjust for pseudo-boolean
				this.jdbcSqlType = Types.BIT;
			}

			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
				//
				// Handle TEXT type (special case), Fix proposed by Peter McKeown
				//
				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
					this.jdbcSqlType = Types.LONGVARCHAR;
				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
					this.jdbcSqlType = Types.VARCHAR;
				}
			}

			this.isSearchable = true;//all MySQL columns are searchable as far as I know
			this.isCurrency = false;// MySQL doen't have a currency datatype I know of

			this.precisionAdjustFactor = 0;
			switch (this.genericSqlType) {
			case MySQLTypes.MYSQL_TYPE_DECIMAL:
			case MySQLTypes.MYSQL_TYPE_NEWDECIMAL:

				if (!this.isUnsigned) this.precisionAdjustFactor = -1;

				break;
			case MySQLTypes.MYSQL_TYPE_DOUBLE:
			case MySQLTypes.MYSQL_TYPE_FLOAT:
				this.precisionAdjustFactor = 1;

				break;
			}
		}
	}

//	/**
//	 * Constructs a new instance of a ResultSetMetaData Field.<br />
//	 * Data is fetched for use from the Field Object
//	 * using its public <i>get</i> methods.<br />
//	 * <span style="color:red;">Field Objects are not designed to have
//	 * thier contents altered once instantiated.</span><br />
//	 * Small portions of this constructor were copied from MySql-Connector/J
//	 *
//	 * @param serverType
//	 * @param columnName
//	 * @param columnAlias
//	 * @param table
//	 * @param database
//	 * @param collation
//	 * @param schema
//	 * @param engine
//	 * @param charsetName
//	 * @param sqlTypeName
//	 * @param valueDefault
//	 * @param isAutoIndex
//	 * @param isNullable
//	 * @param isPrimaryKey
//	 * @param isUniqueKey
//	 * @param isIndex
//	 * @param length
//	 */
//	public SQLField(
//			final int serverType,
//			final String columnName,
//			final String columnAlias,
//			final String table,
//			final String database,
//			final String collation,
//			final String schema,
//			final String engine,
//			final String charsetName,
//			final String sqlTypeName,
//			final String valueDefault,
//			final boolean isAutoIndex,
//			final boolean isNullable,
//			final boolean isPrimaryKey,
//			final boolean isUniqueKey,
//			final boolean isIndex,
//			final int length
//	) {
//		this.serverType = serverType;
//
//		this.columnName = columnName;
//		this.columnAlias = columnAlias;
//
//		this.tableName = table;
//		this.catalog = database;
//		this.collation = collation;
//		this.schema = schema;
//		this.dbEngine = engine;
//		this.defaultValue = valueDefault;
////		this.charsetName = charsetName;
//
//		this.isUnsigned = isUnsigned(sqlTypeName);
//		this.isAutoIndex = isAutoIndex;
//		this.isNullable = isNullable;
//
//		this.isPrimaryKey = isPrimaryKey;
//		this.isUniqueKey = isUniqueKey;
//		this.isIndex = isIndex;
//
//		this.precisionAdjustFactor = 0;
//		this.precision = length;
//		this.scale = 0;
//
//		this.fieldLength = length;
//
//
//		/* NOTES:
//		 * Following are some adjustments to some data types
//		 */
//		if(this.serverType==com.jdbwc.util.Util.ID_POSTGRESQL){
//			// Map native sql Types to java.sql Types
//			this.genericSqlType = PgSQLTypes.pgsqlNameToPgsqlType(this.genericSqlTypeName);
//			this.jdbcSqlType = PgSQLTypes.pgsqlNameToJdbcType(this.genericSqlTypeName);
//			this.jdbcSqlTypeName = PgSQLTypes.pgsqlNameToJdbcName(this.genericSqlTypeName);
//
//			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
//				this.jdbcSqlType = Types.BIT;
//			}
//
//			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
//				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
//					this.jdbcSqlType = Types.LONGVARCHAR;
//				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
//					this.jdbcSqlType = Types.VARCHAR;
//				}
////			} else {
////				this.charsetName = "US-ASCII";
//			}
//
//			this.precisionAdjustFactor = 0;
//			this.isSearchable = true;//TODO: add postgres value
//			this.isCurrency = (this.genericSqlType==PgSQLTypes.MONEY || this.genericSqlType==PgSQLTypes.MONEY_ARRAY);
//		}else{
//			this.schema = null;
//			// Map native sql to java.sql Types
//			this.genericSqlType = MySQLTypes.mysqlNameToJdbcType(this.genericSqlTypeName);
//			this.jdbcSqlType = MySQLTypes.mysqlTypeToJdbcType(this.genericSqlType);
//			this.jdbcSqlTypeName = MySQLTypes.mysqlNameToJdbcName(this.genericSqlTypeName);
//
//			//				// Re-map to 'real' blob type, if its a BLOB
//			//				if (myJdbcSqlType == MySQLTypes.FIELD_TYPE_BLOB) {
//			//
//			//				    if (true) {
//			//				        myJdbcSqlType = Types.VARCHAR;
//			//				        myGenericSqlType = MySQLTypes.FIELD_TYPE_VARCHAR;
//			//					} else {
//			//						// *TEXT masquerading as blob
//			//						myJdbcSqlType = Types.LONGVARCHAR;
//			//						myGenericSqlType = MySQLTypes.FIELD_TYPE_VAR_STRING;
//			//					}
//			//				}
//
//			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
//				// Adjust for pseudo-boolean
//				this.jdbcSqlType = Types.BIT;
//			}
//
//			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
//				//
//				// Handle TEXT type (special case), Fix proposed by Peter McKeown
//				//
//				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
//					this.jdbcSqlType = Types.LONGVARCHAR;
//				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
//					this.jdbcSqlType = Types.VARCHAR;
//				}
//			}
//
//			this.isSearchable = true;//all MySQL columns are searchable as far as I know
//			this.isCurrency = false;// MySQL doen't have a currency datatype I know of
//
//			this.precisionAdjustFactor = 0;
//			switch (this.genericSqlType) {
//			case MySQLTypes.MYSQL_TYPE_DECIMAL:
//			case MySQLTypes.MYSQL_TYPE_NEWDECIMAL:
//
//				if (!this.isUnsigned) this.precisionAdjustFactor = -1;
//
//				break;
//			case MySQLTypes.MYSQL_TYPE_DOUBLE:
//			case MySQLTypes.MYSQL_TYPE_FLOAT:
//				this.precisionAdjustFactor = 1;
//
//				break;
//			}
//		}
//	}

	/**
	 * Data is fetched for use from the Field Object
	 * using its public <i>get</i> methods.<br />
	 * Small portions of this constructor were derived from MySql-Connector/J
	 *
	 * @param serverType int - driver specific dbType
	 * @param catalogName String - Catalog name
	 * @param schemaName String - Shema name
	 * @param tableName String - Table name
	 * @param columnName String - Column name
	 * @param columnAlias String - Column label
	 * @param columnDefault String - Column default value
	 * @param collationName String - charset/collation name
	 *
	 * @param sqlType int - Generic database type
	 * @param maxLength int - Column length
	 * @param maxPrecision int - Column numeric precision
	 * @param maxScale int - Column numeric decimal
	 *
	 * @param isNullable boolean -
	 * @param isAutoIndex boolean -
	 * @param isUnsigned boolean -
	 * @param isPrimaryKey boolean -
	 * @param isUniqueKey boolean -
	 * @param isIndex boolean -
	 */
	public SQLField(
			final int serverType,

			final String catalogName,
			final String schemaName,
			final String tableName,
			final String columnName,
			final String columnAlias,
			final String columnDefault,
			final String collationName,

			final int sqlType,
			final int maxLength,
			final int maxPrecision,
			final int maxScale,

			final boolean isNullable,
			final boolean isAutoIndex,
			final boolean isUnsigned,
			final boolean isPrimaryKey,
			final boolean isUniqueKey,
			final boolean isIndex
	) {
		this.serverType = serverType;

		this.catalog = catalogName;
		this.schema = schemaName;

		this.tableName = tableName;
		this.columnName = columnName;
		this.columnAlias = columnAlias;
		this.defaultValue = columnDefault;

		this.genericSqlType = sqlType;
		this.fieldLength = maxLength;
		this.precision = maxPrecision;
		this.scale = maxScale;

		this.collation = (collationName==null || collationName.isEmpty()) ? "ascii_general_ci" : collationName;

		this.isNullable = isNullable;
		this.isAutoIndex = isAutoIndex;
		this.isUnsigned = isUnsigned;
		this.isPrimaryKey = isPrimaryKey;
		this.isUniqueKey = isUniqueKey;
		this.isIndex = isIndex;



		/* finish setting up values */
		if(this.serverType==com.jdbwc.util.Util.ID_POSTGRESQL){
			// Map native sql Types to java.sql Types
			this.jdbcSqlType = PgSQLTypes.pgsqlTypeToJdbcType(this.genericSqlType);
			this.jdbcSqlTypeName = PgSQLTypes.pgsqlTypeToJdbcName(this.genericSqlType);
			this.genericSqlTypeName = PgSQLTypes.pgsqlTypeToPgsqlName(this.genericSqlType);

//			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
//				this.jdbcSqlType = Types.BIT;
//			}
//
//			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
//				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
//					this.jdbcSqlType = Types.LONGVARCHAR;
//				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
//					this.jdbcSqlType = Types.VARCHAR;
//				}
//			}

			this.precisionAdjustFactor = 0;
			this.isSearchable = true;//TODO: add real postgres value
			this.isCurrency = (this.genericSqlType==PgSQLTypes.MONEY || this.genericSqlType==PgSQLTypes.MONEY_ARRAY);
		}else{
			// Map native sql to java.sql Types

			this.jdbcSqlType = MySQLTypes.mysqlTypeToJdbcType(this.genericSqlType);
			this.jdbcSqlTypeName = MySQLTypes.mysqlTypeToJdbcName(this.genericSqlType);
			this.genericSqlTypeName = MySQLTypes.mysqlTypeToMysqlName(this.genericSqlType);


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
//
//			if (this.jdbcSqlType == Types.TINYINT && this.precision == 1) {
//				// Adjust for pseudo-boolean
//				this.jdbcSqlType = Types.BIT;
//				this.collation = "ascii_general_ci";
//			}
//
//			if (!isNativeNumericType(this.genericSqlType) && !isNativeDateTimeType(this.genericSqlType)) {
//				//
//				// Handle TEXT type (special case), Fix proposed by Peter McKeown
//				//
//				if ((this.jdbcSqlType == Types.LONGVARBINARY)) {
//					this.jdbcSqlType = Types.LONGVARCHAR;
//				} else if ((this.jdbcSqlType == Types.VARBINARY)) {
//					this.jdbcSqlType = Types.VARCHAR;
//				}
//			}else{
//				this.collation = "ascii_general_ci";
//			}

			this.isSearchable = true;//all MySQL columns are searchable as far as I know
			this.isCurrency = false;// MySQL doen't have a currency datatype I know of

			this.precisionAdjustFactor = 0;
			switch (this.genericSqlType) {
			case MySQLTypes.MYSQL_TYPE_DECIMAL:
			case MySQLTypes.MYSQL_TYPE_NEWDECIMAL:

				if (!this.isUnsigned) this.precisionAdjustFactor = -1;

				break;
			case MySQLTypes.MYSQL_TYPE_DOUBLE:
			case MySQLTypes.MYSQL_TYPE_FLOAT:
				this.precisionAdjustFactor = 1;

				break;
			}
		}
	}
	/**
	 * Short ResultSetMetaData constructor for metadata about metadata methods.
	 *
	 * @param table String - columns table name
	 * @param columnName String - columns name
	 * @param javaType int - java.sql type
	 * @param length int - field length/precision
	 * @param scale int - decimal scale
	 */
	public SQLField(
			final String table,
			final String columnName,
			final int javaType,
			final int length,
			final int scale
	) {
		this.tableName = table;
		this.columnName = columnName;

		this.jdbcSqlType = javaType;
		this.precision = length;
		this.scale = scale;

		this.isNullable = true;
	}

	//------------------------------------------------------- public methods

	/**
	 * @return the ClassName
	 */
	public String getClassName() {
		return "java.sql." + this.jdbcSqlTypeName;
	}

	/**
	 * @return the columnAlias
	 */
	public String getColumnAlias() {
		return (this.columnAlias==null || "".equals(this.columnAlias)) ? this.columnName : this.columnAlias;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return (this.columnName==null || "".equals(this.columnName)) ? this.columnAlias : this.columnName;
	}

	/**
	 * @return the database
	 */
	public String getCatalog() {
		return this.catalog;
	}

	/**
	 * @return the engine
	 */
	public String getEngine() {
		return this.dbEngine;
	}

	/**
	 * @return the FieldName
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * @return the dataType
	 */
	public int getGenericSqlType() {
		return this.genericSqlType;
	}

	/**
	 * @return the mySqlTypeName
	 */
	public String getGenericSqlTypeName() {
		return this.genericSqlTypeName;
	}

	/**
	 * @return the JdbcSqlType
	 */
	public int getJdbcSqlType() {
		return this.jdbcSqlType;
	}

	/**
	 * @return the JdbcSqlTypeName
	 */
	public String getJdbcSqlTypeName() {
		return this.jdbcSqlTypeName;
	}

	/**
	 * @return the field Length
	 */
	public int getLength() {
		if(this.fieldLength==0 && this.precision>0)
			this.fieldLength = this.precision;

		return this.fieldLength;
	}

	/**
	 * @return the Mode
	 */
	public String getMode() {
		return this.mode;
	}

	/**
	 * @return the Precision
	 */
	public int getPrecision() {
		return this.precision + this.precisionAdjustFactor;
	}

	/**
	 * @return the Scale
	 */
	public int getScale() {
		return this.scale;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return this.schema;
	}

	/**
	 * @return the myTable
	 */
	public String getTable() {
		return this.tableName;
	}

	/**
	 * @return column defaultValue
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * @return the isAutoIndex
	 */
	public boolean isAutoIndex() {
		return this.isAutoIndex;
	}

	/**
	 * @return the isCurrency
	 */
	public boolean isCurrency() {
		return this.isCurrency;
	}

	/**
	 * @return the isIndex
	 */
	public boolean isIndex() {
		return this.isIndex;
	}

	public boolean isCaseSensetive(){
		if(isNativeDateTimeType(this.genericSqlType))
			return false;
		if(isNativeNumericType(this.genericSqlType))
			return false;
		if(this.collation.endsWith("_ci"))
			return false;

		return true;
	}

	/**
	 * @return the IsNullable
	 */
	public boolean isNullable() {
		return this.isNullable;
	}

	/**
	 * @return the isPrimaryKey
	 */
	public boolean isPrimaryKey() {
		return this.isPrimaryKey;
	}

	/**
	 * @return the isSearchable
	 */
	public boolean isSearchable() {
		return this.isSearchable;
	}

	/**
	 * @return the IsSigned
	 */
	public boolean isSigned() {
		return !this.isUnsigned;
	}

	/**
	 * @return the isUniqueKey
	 */
	public boolean isUniqueKey() {
		return this.isUniqueKey;
	}

	public boolean isUnsigned() {
		return this.isUnsigned;
	}

	/**
	 * This is for debugging to make it easy to see whats in this SQLField.
	 *
	 * @return This SQLResultsField as a human readable String.
	 */
	@Override
	public String toString() {
		final String _NL_ = System.getProperty("line.separator");
		final StringBuilder output = new StringBuilder();

		/* if myFieldName isEmpty the field is a ResultSetField */
		if("".equals(this.fieldName)){
			output.append("SQLField[").append(_NL_);
			output.append("\tColumnName = ").append(getColumnName()).append(_NL_);
			output.append("\tColumnAlias = ").append(getColumnAlias()).append(_NL_);

			output.append("\tTable = ").append(getTable()).append(_NL_);
			output.append("\tDatabase = ").append(getCatalog()).append(_NL_);
			output.append("\tSchema = ").append(getSchema()).append(_NL_);
			output.append("\tEngine = ").append(getEngine()).append(_NL_);
			output.append("\tValueDefault = ").append(getDefaultValue()).append(_NL_);

			output.append("\tIsAutoIndex = ").append(isAutoIndex()).append(_NL_);
			output.append("\tIsNullable = ").append(isNullable()).append(_NL_);
			output.append("\tIsPrimaryKey = ").append(isPrimaryKey()).append(_NL_);
			output.append("\tIsUniqueKey = ").append(isUniqueKey()).append(_NL_);
			output.append("\tIsIndex = ").append(isIndex()).append(_NL_);

			output.append("\tPrecision = ").append(getPrecision()).append(_NL_);
			output.append("\tScale = ").append(getScale()).append(_NL_);
			output.append("\tIsUnsigned = ").append(isUnsigned()).append(_NL_);

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

	//------------------------------------------------------- private methods

	private boolean isUnsigned(final String typeName) {
		return typeName.endsWith("unsigned");
	}

	private boolean isNativeDateTimeType(final int inputType) {
		boolean nativeDateTime = false;
		if(this.serverType==com.jdbwc.util.Util.ID_POSTGRESQL){
			nativeDateTime = (
					inputType == PgSQLTypes.TIMETZ ||
					inputType == PgSQLTypes.TIMESTAMPTZ ||
					inputType == PgSQLTypes.DATE ||
					inputType == PgSQLTypes.TIME ||
					inputType == PgSQLTypes.TIMESTAMP);
		}else{
			nativeDateTime = (
					inputType == MySQLTypes.MYSQL_TYPE_DATE ||
					inputType == MySQLTypes.MYSQL_TYPE_NEWDATE ||
					inputType == MySQLTypes.MYSQL_TYPE_DATETIME ||
					inputType == MySQLTypes.MYSQL_TYPE_TIME ||
					inputType == MySQLTypes.MYSQL_TYPE_TIMESTAMP);
		}
		return nativeDateTime;
	}


	private boolean isNativeNumericType(final int inputType) {
		boolean nativeNumeric = false;

		if(this.serverType==com.jdbwc.util.Util.ID_POSTGRESQL){
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
					(inputType >= MySQLTypes.MYSQL_TYPE_TINY &&
							inputType <= MySQLTypes.MYSQL_TYPE_DOUBLE) ||
							inputType == MySQLTypes.MYSQL_TYPE_LONGLONG ||
							inputType == MySQLTypes.MYSQL_TYPE_YEAR);
		}
		return nativeNumeric;
	}
}
