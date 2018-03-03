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
package com.jdbwc.util;

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

	public transient String collation = "";

	private transient String dbEngine = "";
	private transient String defaultValue = "";

	public transient String jdbcSqlTypeName = "";
	public transient String genericSqlTypeName = "";

	public transient int jdbcSqlType = 0;
	public transient int genericSqlType = 0;

	private transient boolean isPrimaryKey = false;
	private transient boolean isUniqueKey = false;
	private transient boolean isIndex = false;
	public transient boolean isUnsigned = false;

	private transient boolean isAutoIndex = false;
	private transient boolean isNullable = false;
	public transient boolean isSearchable = true;
	public transient boolean isCurrency = false;
	public transient boolean isCaseSensitive = true;

	private transient int fieldLength = 0;
	private transient int precision = 0;
	private transient int scale = 0;
	public transient int precisionAdjustFactor = 0;

//	private transient int serverType = 0;

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
			final String fieldName,
			final String typeName,
			final boolean isNullable,
			final String mode
	) {
		this.fieldName = fieldName;

		this.genericSqlTypeName = typeName;
		this.jdbcSqlTypeName = typeName;

		this.isNullable = isNullable;
		this.isUnsigned = typeName.endsWith("unsigned");

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
	}

	/**
	 * Data is fetched for use from the Field Object
	 * using its public <i>get</i> methods.<br />
	 * Small portions of this constructor were derived from MySql-Connector/J
	 *
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
		return (this.columnAlias==null || this.columnAlias.isEmpty()) ? this.columnName : this.columnAlias;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return (this.columnName==null || this.columnName.isEmpty()) ? this.columnAlias : this.columnName;
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
		return isCaseSensitive;
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
}
