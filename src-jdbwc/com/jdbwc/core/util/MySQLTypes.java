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
 * MySql types<br />
 * <br />
 * This deals with convering MySQL types to different forms.
 * Most input for type requests come from a MySQL type id (integer).
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-05-09
 */
public final class MySQLTypes {

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

	// MySQL type names.
	public static final String MYSQL_NAME_BIT = "BIT";
	public static final String MYSQL_NAME_BLOB = "BLOB";
	public static final String MYSQL_NAME_CHAR = "CHAR";
	public static final String MYSQL_NAME_DATE = "DATE";
	public static final String MYSQL_NAME_DATETIME = "DATETIME";
	public static final String MYSQL_NAME_DECIMAL = "DECIMAL";
	public static final String MYSQL_NAME_DOUBLE = "DOUBLE";
	public static final String MYSQL_NAME_ENUM = "ENUM";
	public static final String MYSQL_NAME_FLOAT = "FLOAT";
	public static final String MYSQL_NAME_GEOMETRY = "GEOMETRY";
	public static final String MYSQL_NAME_INT24 = "INT";
	public static final String MYSQL_NAME_INTERVAL = "ENUM";
	public static final String MYSQL_NAME_LONG = "LONG";
	public static final String MYSQL_NAME_LONG_BLOB = "LONGBLOB";
	public static final String MYSQL_NAME_LONGLONG = "LONGLONG";
	public static final String MYSQL_NAME_MEDIUM_BLOB = "MEDIUMBLOB";
	public static final String MYSQL_NAME_NEWDATE = "DATE";
	public static final String MYSQL_NAME_NEWDECIMAL = "DECIMAL";
	public static final String MYSQL_NAME_NULL = "NULL";
	public static final String MYSQL_NAME_SET = "SET";
	public static final String MYSQL_NAME_SHORT = "SHORT";
	public static final String MYSQL_NAME_STRING = "VARCHAR";
	public static final String MYSQL_NAME_TIME = "TIME";
	public static final String MYSQL_NAME_TIMESTAMP = "TIMESTAMP";
	public static final String MYSQL_NAME_TINY = "TINY";
	public static final String MYSQL_NAME_TINY_BLOB = "TINYBLOB";
	public static final String MYSQL_NAME_VAR_STRING = "LONGVARCHAR";
	public static final String MYSQL_NAME_YEAR = "YEAR";

	// MySQL types as per the mysqli PHP specs.
	public static final int MYSQL_TYPE_BIT = 16;
	public static final int MYSQL_TYPE_BLOB = 252;
	public static final int MYSQL_TYPE_CHAR = 1;
	public static final int MYSQL_TYPE_DATE = 10;
	public static final int MYSQL_TYPE_DATETIME = 12;
	public static final int MYSQL_TYPE_DECIMAL = 0;
	public static final int MYSQL_TYPE_DOUBLE = 5;
	public static final int MYSQL_TYPE_ENUM = 247;
	public static final int MYSQL_TYPE_FLOAT = 4;
	public static final int MYSQL_TYPE_GEOMETRY = 255;
	public static final int MYSQL_TYPE_INT24 = 9;
	public static final int MYSQL_TYPE_INTERVAL = 247;
	public static final int MYSQL_TYPE_LONG = 3;
	public static final int MYSQL_TYPE_LONG_BLOB = 251;
	public static final int MYSQL_TYPE_LONGLONG = 8;
	public static final int MYSQL_TYPE_MEDIUM_BLOB = 250;
	public static final int MYSQL_TYPE_NEWDATE = 14;
	public static final int MYSQL_TYPE_NEWDECIMAL = 246;
	public static final int MYSQL_TYPE_NULL = 6;
	public static final int MYSQL_TYPE_SET = 248;
	public static final int MYSQL_TYPE_SHORT = 2;
	public static final int MYSQL_TYPE_STRING = 254;
	public static final int MYSQL_TYPE_TIME = 11;
	public static final int MYSQL_TYPE_TIMESTAMP = 7;
	public static final int MYSQL_TYPE_TINY = 1;
	public static final int MYSQL_TYPE_TINY_BLOB = 249;
	public static final int MYSQL_TYPE_VAR_STRING = 253;
	public static final int MYSQL_TYPE_YEAR = 13;


	public static String mysqlNameToJdbcName(final String mysqlType) {
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
	 * Maps MySQL type name to Java.sql.types
	 */
	public static int mysqlNameToJdbcType(final String mysqlType) {

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
	 * Maps the given MySQL type to the equivalent java.sql.Types name.
	 */
	public static String mysqlTypeToJdbcName(final int mysqlType) {

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
	 * Maps the given MySQL type to the equivalent JDBC type.
	 */
	public static int mysqlTypeToJdbcType(final int mysqlType) {

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
	 * Maps MySQL type id's to thier type name.
	 *
	 * @param mysqlType int - MySQL type id
	 * @return String - the MySQL type name
	 */
	public static String mysqlTypeToMysqlName(final int mysqlType) {
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
	 * Maps MySQL type name to thier type id's.
	 *
	 * @param mysqlType String - MySQL type name
	 * @return int - the MySQL type id
	 */
	public static int mysqlNameToMysqlType(final String mysqlType) {

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
}
