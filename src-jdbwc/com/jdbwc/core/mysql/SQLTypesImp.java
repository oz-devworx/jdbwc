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
package com.jdbwc.core.mysql;

import java.sql.Types;

import com.jdbwc.util.SQLField;


/**
 * MySql types<br />
 * <br />
 * This deals with convering MySQL types to different forms.
 * Most input for type requests come from a MySQL type id (integer).
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-05-09
 */
public final class SQLTypesImp implements com.jdbwc.util.SQLTypes {

//	CHAR
//	VARCHAR
//	TINYTEXT
//	TEXT
//	BLOB
//	MEDIUMTEXT
//	MEDIUMBLOB
//	LONGTEXT
//	LONGBLOB
//	TINYINT
//	SMALLINT
//	MEDIUMINT
//	INT
//	BIGINT
//	FLOAT
//	DOUBLE
//	DECIMAL
//	DATE
//	DATETIME
//	TIMESTAMP
//	TIME
//	ENUM
//	SET

	//-------------------------------------------------------- fields

	// MySQL type names.
	private static final String MYSQL_NAME_BIT = "BIT";
	private static final String MYSQL_NAME_BLOB = "BLOB";
	private static final String MYSQL_NAME_CHAR = "CHAR";
	private static final String MYSQL_NAME_DATE = "DATE";
	private static final String MYSQL_NAME_DATETIME = "DATETIME";
	private static final String MYSQL_NAME_DECIMAL = "DECIMAL";
	private static final String MYSQL_NAME_DOUBLE = "DOUBLE";
	private static final String MYSQL_NAME_ENUM = "ENUM";
	private static final String MYSQL_NAME_FLOAT = "FLOAT";
	private static final String MYSQL_NAME_GEOMETRY = "GEOMETRY";
	private static final String MYSQL_NAME_INT24 = "INT";
	private static final String MYSQL_NAME_INTERVAL = "ENUM";
	private static final String MYSQL_NAME_LONG = "LONG";
	private static final String MYSQL_NAME_LONG_BLOB = "LONGBLOB";
	private static final String MYSQL_NAME_LONGLONG = "LONGLONG";
	private static final String MYSQL_NAME_MEDIUM_BLOB = "MEDIUMBLOB";
	private static final String MYSQL_NAME_NEWDATE = "DATE";
	private static final String MYSQL_NAME_NEWDECIMAL = "DECIMAL";
	private static final String MYSQL_NAME_NULL = "NULL";
	private static final String MYSQL_NAME_SET = "SET";
	private static final String MYSQL_NAME_SHORT = "SHORT";
	private static final String MYSQL_NAME_STRING = "VARCHAR";
	private static final String MYSQL_NAME_TIME = "TIME";
	private static final String MYSQL_NAME_TIMESTAMP = "TIMESTAMP";
	private static final String MYSQL_NAME_TINY = "TINY";
	private static final String MYSQL_NAME_TINY_BLOB = "TINYBLOB";
	private static final String MYSQL_NAME_VAR_STRING = "LONGVARCHAR";
	private static final String MYSQL_NAME_YEAR = "YEAR";

	// MySQL types as per the mysqli PHP specs.
	private static final int MYSQL_TYPE_BIT = 16;
	private static final int MYSQL_TYPE_BLOB = 252;
	private static final int MYSQL_TYPE_CHAR = 1;
	private static final int MYSQL_TYPE_DATE = 10;
	private static final int MYSQL_TYPE_DATETIME = 12;
	private static final int MYSQL_TYPE_DECIMAL = 0;
	private static final int MYSQL_TYPE_DOUBLE = 5;
	private static final int MYSQL_TYPE_ENUM = 247;
	private static final int MYSQL_TYPE_FLOAT = 4;
	private static final int MYSQL_TYPE_GEOMETRY = 255;
	private static final int MYSQL_TYPE_INT24 = 9;
//	private static final int MYSQL_TYPE_INTERVAL = 247;//TODO: implement me!
	private static final int MYSQL_TYPE_LONG = 3;
	private static final int MYSQL_TYPE_LONG_BLOB = 251;
	private static final int MYSQL_TYPE_LONGLONG = 8;
	private static final int MYSQL_TYPE_MEDIUM_BLOB = 250;
	private static final int MYSQL_TYPE_NEWDATE = 14;
	private static final int MYSQL_TYPE_NEWDECIMAL = 246;
	private static final int MYSQL_TYPE_NULL = 6;
	private static final int MYSQL_TYPE_SET = 248;
	private static final int MYSQL_TYPE_SHORT = 2;
	private static final int MYSQL_TYPE_STRING = 254;
	private static final int MYSQL_TYPE_TIME = 11;
	private static final int MYSQL_TYPE_TIMESTAMP = 7;
	private static final int MYSQL_TYPE_TINY = 1;
	private static final int MYSQL_TYPE_TINY_BLOB = 249;
	private static final int MYSQL_TYPE_VAR_STRING = 253;
	private static final int MYSQL_TYPE_YEAR = 13;

	//-------------------------------------------------------- constructors

	public SQLTypesImp(){

    }

	//-------------------------------------------------------- public methods

	/**
	 * @see com.jdbwc.util.SQLTypes#nativeNameToJdbcName(java.lang.String)
	 */
	@Override
	public String nativeNameToJdbcName(final String mysqlType) {
		if (mysqlType.equalsIgnoreCase("BIT")) {
			return "TINYINT";
		} else if (mysqlType.equalsIgnoreCase("BOOL")) {
			return "TINYINT";
		} else if (mysqlType.equalsIgnoreCase("TINYINT")) {
			return "TINYINT";
		} else if (mysqlType.equalsIgnoreCase("SMALLINT")) {
			return "SMALLINT";
		} else if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
			return "MEDIUMINT";
		} else if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) {
			return "INTEGER";
		} else if (mysqlType.equalsIgnoreCase("BIGINT")) {
			return "BIGINT";
		} else if (mysqlType.equalsIgnoreCase("INT24")) {
			return "BIGINT";
		} else if (mysqlType.equalsIgnoreCase("REAL")) {
			return "DOUBLE";
		} else if (mysqlType.equalsIgnoreCase("FLOAT")) {
			return "DOUBLE";
		} else if (mysqlType.equalsIgnoreCase("DECIMAL")) {
			return "DECIMAL";
		} else if (mysqlType.equalsIgnoreCase("NUMERIC")) {
			return "NUMERIC";
		} else if (mysqlType.equalsIgnoreCase("DOUBLE")) {
			return "DOUBLE";
		} else if (mysqlType.equalsIgnoreCase("CHAR")) {
			return "CHAR";
		} else if (mysqlType.equalsIgnoreCase("VARCHAR")) {
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("DATE")) {
			return "DATE";
		} else if (mysqlType.equalsIgnoreCase("TIME")) {
			return "TIME";
		} else if (mysqlType.equalsIgnoreCase("YEAR")) {
			return "DATE";
		} else if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
			return "TIMESTAMP";
		} else if (mysqlType.equalsIgnoreCase("DATETIME")) {
			return "TIMESTAMP";
		} else if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
			return "BLOB";
		} else if (mysqlType.equalsIgnoreCase("BLOB")) {
			return "BLOB";
		} else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
			return "BLOB";
		} else if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
			return "BLOB";
		} else if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("TEXT")) {
			return "LONGVARCHAR";
		} else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
			return "LONGVARCHAR";
		} else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
			return "LONGVARCHAR";
		} else if (mysqlType.equalsIgnoreCase("ENUM")) {
			return "CHAR";
		} else if (mysqlType.equalsIgnoreCase("SET")) {
			return "CHAR";
		} else if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
			return "BINARY";
		} else if (mysqlType.equalsIgnoreCase("BINARY")) {
			return "BINARY";
		} else if (mysqlType.equalsIgnoreCase("VARBINARY")) {
			return "VARBINARY";
		} else if (mysqlType.equalsIgnoreCase("BIT")) {
			return "CHAR";
		}else{
			return "VARCHAR";// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * @see com.jdbwc.util.SQLTypes#nativeNameToJdbcType(java.lang.String)
	 */
	@Override
	public int nativeNameToJdbcType(final String mysqlType) {

		if (mysqlType.equalsIgnoreCase(MYSQL_NAME_DECIMAL)){
			return Types.DECIMAL;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_BIT)){
			return Types.CHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_BLOB)){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_CHAR)){
			return Types.CHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_DATE)){
			return Types.DATE;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_DATETIME)){
			return Types.TIMESTAMP;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_DOUBLE)){
			return Types.DOUBLE;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_ENUM)){
			return Types.CHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_FLOAT)){
			return Types.DOUBLE;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_GEOMETRY)){
			return Types.BINARY;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_INT24)){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_INTERVAL)){
			return Types.CHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_LONG)){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_LONG_BLOB)){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_LONGLONG)){
			return Types.BIGINT;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_MEDIUM_BLOB)){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_NEWDATE)){
			return Types.DATE;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_NEWDECIMAL)){
			return Types.DECIMAL;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_NULL)){
			return Types.NULL;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_SET)){
			return Types.CHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_SHORT)){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_STRING)){
			return Types.VARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_TIME)){
			return Types.TIME;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_TIMESTAMP)){
			return Types.TIMESTAMP;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_TINY)){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_TINY_BLOB)){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_VAR_STRING)){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase(MYSQL_NAME_YEAR)){
			return Types.DATE;

		} else if (mysqlType.equalsIgnoreCase("TEXT")){
			return Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase("INTEGER")){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase("INT")){
			return Types.INTEGER;
		} else if (mysqlType.equalsIgnoreCase("TINYINT")){
			return Types.TINYINT;
		} else if (mysqlType.equalsIgnoreCase("SMALLINT")){
			return Types.SMALLINT;

		} else{
			return Types.VARCHAR;// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * @see com.jdbwc.util.SQLTypes#nativeNameToNativeType(java.lang.String)
	 */
	@Override
	public int nativeNameToNativeType(final String mysqlType) {

			if(MYSQL_NAME_DECIMAL.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_DECIMAL;
			else if(MYSQL_NAME_BIT.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_BIT;
			else if(MYSQL_NAME_BLOB.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_BLOB;
			else if(MYSQL_NAME_CHAR.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_CHAR;
			else if(MYSQL_NAME_DATE.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_DATE;
			else if(MYSQL_NAME_DATETIME.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_DATETIME;
			else if(MYSQL_NAME_DOUBLE.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_DOUBLE;
			else if(MYSQL_NAME_ENUM.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_ENUM;
			else if(MYSQL_NAME_FLOAT.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_FLOAT;
			else if(MYSQL_NAME_GEOMETRY.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_GEOMETRY;
			else if(MYSQL_NAME_INT24.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_INT24;
			//case MYSQL_TYPE_INTERVAL: return MYSQL_NAME_INTERVAL;
			else if(MYSQL_NAME_LONG.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_LONG;
			else if(MYSQL_NAME_LONG_BLOB.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_LONG_BLOB;
			else if(MYSQL_NAME_LONGLONG.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_LONGLONG;
			else if(MYSQL_NAME_MEDIUM_BLOB.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_MEDIUM_BLOB;
			else if(MYSQL_NAME_NEWDATE.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_NEWDATE;
			else if(MYSQL_NAME_NEWDECIMAL.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_NEWDECIMAL;
			else if(MYSQL_NAME_NULL.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_NULL;
			else if(MYSQL_NAME_SET.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_SET;
			else if(MYSQL_NAME_SHORT.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_SHORT;
			else if(MYSQL_NAME_STRING.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_STRING;
			else if(MYSQL_NAME_TIME.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_TIME;
			else if(MYSQL_NAME_TIMESTAMP.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_TIMESTAMP;
			//case MYSQL_TYPE_TINY: return MYSQL_NAME_TINY;
			else if(MYSQL_NAME_TINY_BLOB.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_TINY_BLOB;
			else if(MYSQL_NAME_VAR_STRING.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_VAR_STRING;
			else if(MYSQL_NAME_YEAR.equalsIgnoreCase(mysqlType)) return MYSQL_TYPE_YEAR;

			else return MYSQL_TYPE_STRING;// Guess? Return varchar for max compatibility
	}


	/**
	 * @see com.jdbwc.util.SQLTypes#nativeTypeToJdbcName(int)
	 */
	@Override
	public String nativeTypeToJdbcName(final int mysqlType) {

		switch (mysqlType) {

			case MYSQL_TYPE_DECIMAL: return "DECIMAL";
			case MYSQL_TYPE_BIT: return "BIT";
			case MYSQL_TYPE_BLOB: return "LONGVARCHAR";
			case MYSQL_TYPE_CHAR: return "CHAR";
			case MYSQL_TYPE_DATE: return "DATE";
			case MYSQL_TYPE_DATETIME: return "TIMESTAMP";
			case MYSQL_TYPE_DOUBLE: return "DOUBLE";
			case MYSQL_TYPE_ENUM: return "CHAR";
			case MYSQL_TYPE_FLOAT: return "DOUBLE";
			case MYSQL_TYPE_GEOMETRY: return "BINARY";
			case MYSQL_TYPE_INT24: return "INTEGER";
			//case MYSQL_TYPE_INTERVAL: return MYSQL_NAME_INTERVAL;
			case MYSQL_TYPE_LONG: return "INTEGER";
			case MYSQL_TYPE_LONG_BLOB: return "LONGVARBINARY";
			case MYSQL_TYPE_LONGLONG: return "BIGINT";
			case MYSQL_TYPE_MEDIUM_BLOB: return "LONGVARBINARY";
			case MYSQL_TYPE_NEWDATE: return "DATE";
			case MYSQL_TYPE_NEWDECIMAL: return "DECIMAL";
			case MYSQL_TYPE_NULL: return "NULL";
			case MYSQL_TYPE_SET: return "CHAR";
			case MYSQL_TYPE_SHORT: return "INTEGER";
			case MYSQL_TYPE_STRING: return "VARCHAR";
			case MYSQL_TYPE_TIME: return "TIME";
			case MYSQL_TYPE_TIMESTAMP: return "TIMESTAMP";
			//case MYSQL_TYPE_TINY: return MYSQL_NAME_TINY";
			case MYSQL_TYPE_TINY_BLOB: return "LONGVARBINARY";
			case MYSQL_TYPE_VAR_STRING: return "LONGVARCHAR";
			case MYSQL_TYPE_YEAR: return "DATE";
			default: return "VARCHAR";// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * @see com.jdbwc.util.SQLTypes#nativeTypeToJdbcType(int)
	 */
	@Override
	public int nativeTypeToJdbcType(final int mysqlType) {

		switch (mysqlType) {

			case MYSQL_TYPE_DECIMAL: return Types.DECIMAL;
			case MYSQL_TYPE_BIT: return Types.CHAR;
			case MYSQL_TYPE_BLOB: return Types.LONGVARCHAR;
			case MYSQL_TYPE_CHAR: return Types.CHAR;
			case MYSQL_TYPE_DATE: return Types.DATE;
			case MYSQL_TYPE_DATETIME: return Types.TIMESTAMP;
			case MYSQL_TYPE_DOUBLE: return Types.DOUBLE;
			case MYSQL_TYPE_ENUM: return Types.CHAR;
			case MYSQL_TYPE_FLOAT: return Types.DOUBLE;
			case MYSQL_TYPE_GEOMETRY: return Types.BINARY;
			case MYSQL_TYPE_INT24: return Types.INTEGER;
			//case MYSQL_TYPE_INTERVAL: return MYSQL_NAME_INTERVAL;
			case MYSQL_TYPE_LONG: return Types.INTEGER;
			case MYSQL_TYPE_LONG_BLOB: return Types.LONGVARCHAR;
			case MYSQL_TYPE_LONGLONG: return Types.BIGINT;
			case MYSQL_TYPE_MEDIUM_BLOB: return Types.LONGVARCHAR;
			case MYSQL_TYPE_NEWDATE: return Types.DATE;
			case MYSQL_TYPE_NEWDECIMAL: return Types.DECIMAL;
			case MYSQL_TYPE_NULL: return Types.NULL;
			case MYSQL_TYPE_SET: return Types.CHAR;
			case MYSQL_TYPE_SHORT: return Types.INTEGER;
			case MYSQL_TYPE_STRING: return Types.VARCHAR;
			case MYSQL_TYPE_TIME: return Types.TIME;
			case MYSQL_TYPE_TIMESTAMP: return Types.TIMESTAMP;
			//case MYSQL_TYPE_TINY: return MYSQL_NAME_TINY;
			case MYSQL_TYPE_TINY_BLOB: return Types.LONGVARCHAR;
			case MYSQL_TYPE_VAR_STRING: return Types.LONGVARCHAR;
			case MYSQL_TYPE_YEAR: return Types.DATE;
			default: return Types.VARCHAR;// Guess? Return varchar for max compatibility
		}
	}


	/**
	 * @see com.jdbwc.util.SQLTypes#nativeTypeToNativeName(int)
	 */
	@Override
	public String nativeTypeToNativeName(final int mysqlType) {
		switch (mysqlType) {

			case MYSQL_TYPE_DECIMAL: return MYSQL_NAME_DECIMAL;
			case MYSQL_TYPE_BIT: return MYSQL_NAME_BIT;
			case MYSQL_TYPE_BLOB: return MYSQL_NAME_BLOB;
			case MYSQL_TYPE_CHAR: return MYSQL_NAME_CHAR;
			case MYSQL_TYPE_DATE: return MYSQL_NAME_DATE;
			case MYSQL_TYPE_DATETIME: return MYSQL_NAME_DATETIME;
			case MYSQL_TYPE_DOUBLE: return MYSQL_NAME_DOUBLE;
			case MYSQL_TYPE_ENUM: return MYSQL_NAME_ENUM;
			case MYSQL_TYPE_FLOAT: return MYSQL_NAME_FLOAT;
			case MYSQL_TYPE_GEOMETRY: return MYSQL_NAME_GEOMETRY;
			case MYSQL_TYPE_INT24: return MYSQL_NAME_INT24;
			//case MYSQL_TYPE_INTERVAL: return MYSQL_NAME_INTERVAL;
			case MYSQL_TYPE_LONG: return MYSQL_NAME_LONG;
			case MYSQL_TYPE_LONG_BLOB: return MYSQL_NAME_LONG_BLOB;
			case MYSQL_TYPE_LONGLONG: return MYSQL_NAME_LONGLONG;
			case MYSQL_TYPE_MEDIUM_BLOB: return MYSQL_NAME_MEDIUM_BLOB;
			case MYSQL_TYPE_NEWDATE: return MYSQL_NAME_NEWDATE;
			case MYSQL_TYPE_NEWDECIMAL: return MYSQL_NAME_NEWDECIMAL;
			case MYSQL_TYPE_NULL: return MYSQL_NAME_NULL;
			case MYSQL_TYPE_SET: return MYSQL_NAME_SET;
			case MYSQL_TYPE_SHORT: return MYSQL_NAME_SHORT;
			case MYSQL_TYPE_STRING: return MYSQL_NAME_STRING;
			case MYSQL_TYPE_TIME: return MYSQL_NAME_TIME;
			case MYSQL_TYPE_TIMESTAMP: return MYSQL_NAME_TIMESTAMP;
			//case MYSQL_TYPE_TINY: return MYSQL_NAME_TINY;
			case MYSQL_TYPE_TINY_BLOB: return MYSQL_NAME_TINY_BLOB;
			case MYSQL_TYPE_VAR_STRING: return MYSQL_NAME_VAR_STRING;
			case MYSQL_TYPE_YEAR: return MYSQL_NAME_YEAR;

			default: return MYSQL_NAME_STRING;// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * @see com.jdbwc.util.SQLTypes#updateField(com.jdbwc.util.SQLField)
	 */
	@Override
	public SQLField updateField(final SQLField field) {

		// Map native sql to java.sql Types
		if(field.getFieldName().isEmpty()){
			field.jdbcSqlType = nativeTypeToJdbcType(field.genericSqlType);
			field.jdbcSqlTypeName = nativeTypeToJdbcName(field.genericSqlType);
			field.genericSqlTypeName = nativeTypeToNativeName(field.genericSqlType);
		}else{
			field.jdbcSqlType = nativeNameToJdbcType(field.genericSqlTypeName);
			field.jdbcSqlTypeName = nativeNameToJdbcName(field.genericSqlTypeName);
			field.genericSqlType = nativeNameToNativeType(field.genericSqlTypeName);
		}

		field.isSearchable = true;//all MySQL columns are searchable as far as I know
		field.isCurrency = false;// MySQL doen't have a currency datatype I know of

		field.precisionAdjustFactor = 0;
		switch (field.genericSqlType) {
			case MYSQL_TYPE_DECIMAL:
			case MYSQL_TYPE_NEWDECIMAL:

				if (!field.isUnsigned) field.precisionAdjustFactor = -1;

				break;
			case MYSQL_TYPE_DOUBLE:
			case MYSQL_TYPE_FLOAT:
				field.precisionAdjustFactor = 1;

				break;
		}

		if(isNativeDateTimeType(field.genericSqlType))
			field.isCaseSensitive = false;
		if(isNativeNumericType(field.genericSqlType))
			field.isCaseSensitive = false;
		if(field.collation.endsWith("_ci"))
			field.isCaseSensitive = false;

		return field;
	}

	//-------------------------------------------------------- private methods

	/**
	 *
	 */
	private boolean isNativeDateTimeType(final int inputType) {
		final boolean nativeDateTime = (
					inputType == MYSQL_TYPE_DATE ||
					inputType == MYSQL_TYPE_NEWDATE ||
					inputType == MYSQL_TYPE_DATETIME ||
					inputType == MYSQL_TYPE_TIME ||
					inputType == MYSQL_TYPE_TIMESTAMP);

		return nativeDateTime;
	}

	/**
	 *
	 */
	private boolean isNativeNumericType(final int inputType) {
		final boolean nativeNumeric = (
			(inputType >= MYSQL_TYPE_TINY &&
					inputType <= MYSQL_TYPE_DOUBLE) ||
					inputType == MYSQL_TYPE_LONGLONG ||
					inputType == MYSQL_TYPE_YEAR);

		return nativeNumeric;
	}
}
