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

import java.sql.Types;

/**
 * The original file this class was derived from was copied from MySql-Connector/J.
 * This class has been modified from the original and includes one additional method
 * mySqlTypeToJavaTypeName() which is specifically for the JDBWC Drivers Statement extensions 
 * WCPreparedStatement() and WCCallableStatement().<br />
 * Many thanks to the MySql team for allowing developers access to the sources.<br />
 * This files origin did not have any comment from the original author but is an
 * important part of the MySql-Connector/J Driver none the less. You should
 * have received a copy of the original MySql-Connector/J Driver this file 
 * was originally copied from (the original filename is the same as this files name).
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public class MySQLTypes {
	public static final int FIELD_TYPE_BIT = 16;

	public static final int FIELD_TYPE_BLOB = 252;

	public static final int FIELD_TYPE_DATE = 10;

	public static final int FIELD_TYPE_DATETIME = 12;

	public static final int FIELD_TYPE_DECIMAL = 0;

	public static final int FIELD_TYPE_DOUBLE = 5;

	public static final int FIELD_TYPE_ENUM = 247;

	public static final int FIELD_TYPE_FLOAT = 4;

	public static final int FIELD_TYPE_GEOMETRY = 255;

	public static final int FIELD_TYPE_INT24 = 9;

	public static final int FIELD_TYPE_LONG = 3;

	public static final int FIELD_TYPE_LONG_BLOB = 251;

	public static final int FIELD_TYPE_LONGLONG = 8;

	public static final int FIELD_TYPE_MEDIUM_BLOB = 250;

	public static final int FIELD_TYPE_NEW_DECIMAL = 246;

	public static final int FIELD_TYPE_NEWDATE = 14;

	public static final int FIELD_TYPE_NULL = 6;

	public static final int FIELD_TYPE_SET = 248;

	public static final int FIELD_TYPE_SHORT = 2;

	public static final int FIELD_TYPE_STRING = 254;

	public static final int FIELD_TYPE_TIME = 11;

	public static final int FIELD_TYPE_TIMESTAMP = 7;

	public static final int FIELD_TYPE_TINY = 1;

	public static final int FIELD_TYPE_TINY_BLOB = 249;

	public static final int FIELD_TYPE_VAR_STRING = 253;

	public static final int FIELD_TYPE_VARCHAR = 15;

	public static final int FIELD_TYPE_YEAR = 13;

	
	protected static final long LENGTH_BLOB = 65535;

	protected static final long LENGTH_LONGBLOB = 4294967295L;

	protected static final long LENGTH_MEDIUMBLOB = 16777215;

	protected static final long LENGTH_TINYBLOB = 255;

	protected static final int MAX_ROWS = 50000000; // Limitation from the MySQL FAQ

	/**
	 * Used to indicate that the server sent no field-level character set
	 * information, so the driver should use the connection-level character
	 * encoding instead.
	 */
	protected static final int NO_CHARSET_INFO = -1;

	protected static final byte OPEN_CURSOR_FLAG = 1;

//	private static Map<String, Integer> mysqlToJdbcTypesMap = new HashMap<String, Integer>();
//	
//	static {
//		mysqlToJdbcTypesMap.put("BIT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_BIT)));
//		mysqlToJdbcTypesMap.put("TINYINT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_TINY)));
//		mysqlToJdbcTypesMap.put("SMALLINT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_SHORT)));
//		mysqlToJdbcTypesMap.put("MEDIUMINT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_INT24)));
//		mysqlToJdbcTypesMap.put("INT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_LONG)));
//		mysqlToJdbcTypesMap.put("INTEGER", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_LONG)));
//		mysqlToJdbcTypesMap.put("BIGINT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_LONGLONG)));
//		mysqlToJdbcTypesMap.put("INT24", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_INT24)));
//		mysqlToJdbcTypesMap.put("REAL", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DOUBLE)));
//		mysqlToJdbcTypesMap.put("FLOAT", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_FLOAT)));
//		mysqlToJdbcTypesMap.put("DECIMAL", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DECIMAL)));
//		mysqlToJdbcTypesMap.put("NUMERIC", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DECIMAL)));
//		mysqlToJdbcTypesMap.put("DOUBLE", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DOUBLE)));
//		mysqlToJdbcTypesMap.put("CHAR", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_STRING)));
//		mysqlToJdbcTypesMap.put("VARCHAR", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_VAR_STRING)));
//		mysqlToJdbcTypesMap.put("DATE", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DATE)));
//		mysqlToJdbcTypesMap.put("TIME", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_TIME)));
//		mysqlToJdbcTypesMap.put("YEAR", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_YEAR)));
//		mysqlToJdbcTypesMap.put("TIMESTAMP", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_TIMESTAMP)));
//		mysqlToJdbcTypesMap.put("DATETIME", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_DATETIME)));
//		mysqlToJdbcTypesMap.put("TINYBLOB", Constants.integerValueOf(java.sql.Types.BINARY));
//		mysqlToJdbcTypesMap.put("BLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		mysqlToJdbcTypesMap.put("MEDIUMBLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		mysqlToJdbcTypesMap.put("LONGBLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		mysqlToJdbcTypesMap.put("TINYTEXT", Constants.integerValueOf(java.sql.Types.VARCHAR));
//		mysqlToJdbcTypesMap.put("TEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		mysqlToJdbcTypesMap.put("MEDIUMTEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		mysqlToJdbcTypesMap.put("LONGTEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		mysqlToJdbcTypesMap.put("ENUM", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_ENUM)));
//		mysqlToJdbcTypesMap.put("SET", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_SET)));
//		mysqlToJdbcTypesMap.put("GEOMETRY", Constants.integerValueOf(mysqlToJavaType(FIELD_TYPE_GEOMETRY)));
//	}
	
	public static String mysqlNameToJdbcName(String mysqlType) {
		if (mysqlType.equalsIgnoreCase("BIT")) {
			return "BIT";
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
			return "INTEGER";
		} else if (mysqlType.equalsIgnoreCase("REAL")) {
			return "REAL";
		} else if (mysqlType.equalsIgnoreCase("FLOAT")) {
			return "FLOAT";
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
			return "DATE";
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
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("ENUM")) {
			return "VARCHAR";
		} else if (mysqlType.equalsIgnoreCase("SET")) {
			return "CHAR";
		} else if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
			return "BINARY";
		} else if (mysqlType.equalsIgnoreCase("BINARY")) {
			return "BINARY";
		} else if (mysqlType.equalsIgnoreCase("VARBINARY")) {
			return "VARBINARY";
		} else if (mysqlType.equalsIgnoreCase("BIT")) {
			return "BIT";
		}else{
			return "VARCHAR";
		}
	}
	
	/**
	 * Maps the given MySQL type name to its generic type.
	 */
	public static int mysqlNameToType(String mysqlType) {
		if (mysqlType.equalsIgnoreCase("BIT")) {
			return FIELD_TYPE_BIT;
		} else if (mysqlType.equalsIgnoreCase("TINYINT")) {
			return FIELD_TYPE_TINY;
		} else if (mysqlType.equalsIgnoreCase("SMALLINT")) {
			return FIELD_TYPE_SHORT;
		} else if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
			return FIELD_TYPE_INT24;
		} else if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) {
			return FIELD_TYPE_LONG;
		} else if (mysqlType.equalsIgnoreCase("BIGINT")) {
			return FIELD_TYPE_LONGLONG;
		} else if (mysqlType.equalsIgnoreCase("INT24")) {
			return FIELD_TYPE_INT24;
		} else if (mysqlType.equalsIgnoreCase("REAL")) {
			return FIELD_TYPE_DOUBLE;
		} else if (mysqlType.equalsIgnoreCase("FLOAT")) {
			return FIELD_TYPE_FLOAT;
		} else if (mysqlType.equalsIgnoreCase("DECIMAL")) {
			return FIELD_TYPE_DECIMAL;
		} else if (mysqlType.equalsIgnoreCase("NUMERIC")) {
			return FIELD_TYPE_DECIMAL;
		} else if (mysqlType.equalsIgnoreCase("DOUBLE")) {
			return FIELD_TYPE_DOUBLE;
		} else if (mysqlType.equalsIgnoreCase("CHAR")) {
			return FIELD_TYPE_STRING;
		} else if (mysqlType.equalsIgnoreCase("VARCHAR")) {
			return FIELD_TYPE_VAR_STRING;
		} else if (mysqlType.equalsIgnoreCase("DATE")) {
			return FIELD_TYPE_DATE;
		} else if (mysqlType.equalsIgnoreCase("TIME")) {
			return FIELD_TYPE_TIME;
		} else if (mysqlType.equalsIgnoreCase("YEAR")) {
			return FIELD_TYPE_YEAR;
		} else if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
			return FIELD_TYPE_TIMESTAMP;
		} else if (mysqlType.equalsIgnoreCase("DATETIME")) {
			return FIELD_TYPE_DATETIME;
		} else if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
			return FIELD_TYPE_TINY_BLOB;
		} else if (mysqlType.equalsIgnoreCase("BLOB")) {
			return FIELD_TYPE_BLOB;
		} else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
			return FIELD_TYPE_MEDIUM_BLOB;
		} else if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
			return FIELD_TYPE_LONG_BLOB;
		} else if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
			return FIELD_TYPE_VARCHAR;
		} else if (mysqlType.equalsIgnoreCase("TEXT")) {
			return FIELD_TYPE_VARCHAR;
		} else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
			return FIELD_TYPE_VARCHAR;
		} else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
			return FIELD_TYPE_VARCHAR;
		} else if (mysqlType.equalsIgnoreCase("ENUM")) {
			return FIELD_TYPE_ENUM;
		} else if (mysqlType.equalsIgnoreCase("SET")) {
			return FIELD_TYPE_SET;
		} else if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
			return FIELD_TYPE_GEOMETRY;
		} else if (mysqlType.equalsIgnoreCase("BINARY")) {
			return Types.BINARY; // no concrete type on the wire
		} else if (mysqlType.equalsIgnoreCase("VARBINARY")) {
			return Types.VARBINARY; // no concrete type on the wire
		} else if (mysqlType.equalsIgnoreCase("BIT")) {
			return FIELD_TYPE_BIT;
		}

		// Punt
		return java.sql.Types.OTHER;
	}

	/**
	 * Maps the given MySQL type to the equivalent JDBC type.
	 */
	public static int mysqlToJavaType(int mysqlType) {
		int jdbcType;

		switch (mysqlType) {
		case MySQLTypes.FIELD_TYPE_NEW_DECIMAL:
		case MySQLTypes.FIELD_TYPE_DECIMAL:
			jdbcType = Types.DECIMAL;
			break;
		case MySQLTypes.FIELD_TYPE_TINY:
			jdbcType = Types.TINYINT;
			break;
		case MySQLTypes.FIELD_TYPE_SHORT:
			jdbcType = Types.SMALLINT;
			break;
		case MySQLTypes.FIELD_TYPE_LONG:
			jdbcType = Types.INTEGER;
			break;
		case MySQLTypes.FIELD_TYPE_FLOAT:
			jdbcType = Types.REAL;
			break;
		case MySQLTypes.FIELD_TYPE_DOUBLE:
			jdbcType = Types.DOUBLE;
			break;
		case MySQLTypes.FIELD_TYPE_NULL:
			jdbcType = Types.NULL;
			break;
		case MySQLTypes.FIELD_TYPE_TIMESTAMP:
			jdbcType = Types.TIMESTAMP;
			break;
		case MySQLTypes.FIELD_TYPE_LONGLONG:
			jdbcType = Types.BIGINT;
			break;
		case MySQLTypes.FIELD_TYPE_INT24:
			jdbcType = Types.INTEGER;
			break;
		case MySQLTypes.FIELD_TYPE_DATE:
			jdbcType = Types.DATE;
			break;
		case MySQLTypes.FIELD_TYPE_TIME:
			jdbcType = Types.TIME;
			break;
		case MySQLTypes.FIELD_TYPE_DATETIME:
			jdbcType = Types.TIMESTAMP;
			break;
		case MySQLTypes.FIELD_TYPE_YEAR:
			jdbcType = Types.DATE;
			break;
		case MySQLTypes.FIELD_TYPE_NEWDATE:
			jdbcType = Types.DATE;
			break;
		case MySQLTypes.FIELD_TYPE_ENUM:
			jdbcType = Types.CHAR;
			break;
		case MySQLTypes.FIELD_TYPE_SET:
			jdbcType = Types.CHAR;
			break;
		case MySQLTypes.FIELD_TYPE_TINY_BLOB:
			jdbcType = Types.VARBINARY;
			break;
		case MySQLTypes.FIELD_TYPE_MEDIUM_BLOB:
			jdbcType = Types.LONGVARBINARY;
			break;
		case MySQLTypes.FIELD_TYPE_LONG_BLOB:
			jdbcType = Types.LONGVARBINARY;
			break;
		case MySQLTypes.FIELD_TYPE_BLOB:
			jdbcType = Types.LONGVARBINARY;
			break;
		case MySQLTypes.FIELD_TYPE_VAR_STRING:
		case MySQLTypes.FIELD_TYPE_VARCHAR:
			jdbcType = Types.VARCHAR;
			break;
		case MySQLTypes.FIELD_TYPE_STRING:
			jdbcType = Types.CHAR;
			break;
		case MySQLTypes.FIELD_TYPE_GEOMETRY:
			jdbcType = Types.BINARY;
			break;
		case MySQLTypes.FIELD_TYPE_BIT:
			jdbcType = Types.BIT;
			break;
		default:
			jdbcType = Types.VARCHAR;
		}

		return jdbcType;
	}

	/**
	 * Maps the given MySQL type to the correct JDBC type.
	 */
	public static int mysqlToJavaType(String mysqlType) {
		if (mysqlType.equalsIgnoreCase("BIT")) {
			return mysqlToJavaType(FIELD_TYPE_BIT);
		} else if (mysqlType.equalsIgnoreCase("TINYINT")) {
			return mysqlToJavaType(FIELD_TYPE_TINY);
		} else if (mysqlType.equalsIgnoreCase("SMALLINT")) {
			return mysqlToJavaType(FIELD_TYPE_SHORT);
		} else if (mysqlType.equalsIgnoreCase("MEDIUMINT")) {
			return mysqlToJavaType(FIELD_TYPE_INT24);
		} else if (mysqlType.equalsIgnoreCase("INT") || mysqlType.equalsIgnoreCase("INTEGER")) { //$NON-NLS-2$
			return mysqlToJavaType(FIELD_TYPE_LONG);
		} else if (mysqlType.equalsIgnoreCase("BIGINT")) {
			return mysqlToJavaType(FIELD_TYPE_LONGLONG);
		} else if (mysqlType.equalsIgnoreCase("INT24")) {
			return mysqlToJavaType(FIELD_TYPE_INT24);
		} else if (mysqlType.equalsIgnoreCase("REAL")) {
			return mysqlToJavaType(FIELD_TYPE_DOUBLE);
		} else if (mysqlType.equalsIgnoreCase("FLOAT")) {
			return mysqlToJavaType(FIELD_TYPE_FLOAT);
		} else if (mysqlType.equalsIgnoreCase("DECIMAL")) {
			return mysqlToJavaType(FIELD_TYPE_DECIMAL);
		} else if (mysqlType.equalsIgnoreCase("NUMERIC")) {
			return mysqlToJavaType(FIELD_TYPE_DECIMAL);
		} else if (mysqlType.equalsIgnoreCase("DOUBLE")) {
			return mysqlToJavaType(FIELD_TYPE_DOUBLE);
		} else if (mysqlType.equalsIgnoreCase("CHAR")) {
			return mysqlToJavaType(FIELD_TYPE_STRING);
		} else if (mysqlType.equalsIgnoreCase("VARCHAR")) {
			return mysqlToJavaType(FIELD_TYPE_VAR_STRING);
		} else if (mysqlType.equalsIgnoreCase("DATE")) {
			return mysqlToJavaType(FIELD_TYPE_DATE);
		} else if (mysqlType.equalsIgnoreCase("TIME")) {
			return mysqlToJavaType(FIELD_TYPE_TIME);
		} else if (mysqlType.equalsIgnoreCase("YEAR")) {
			return mysqlToJavaType(FIELD_TYPE_YEAR);
		} else if (mysqlType.equalsIgnoreCase("TIMESTAMP")) {
			return mysqlToJavaType(FIELD_TYPE_TIMESTAMP);
		} else if (mysqlType.equalsIgnoreCase("DATETIME")) {
			return mysqlToJavaType(FIELD_TYPE_DATETIME);
		} else if (mysqlType.equalsIgnoreCase("TINYBLOB")) {
			return java.sql.Types.BINARY;
		} else if (mysqlType.equalsIgnoreCase("BLOB")) {
			return java.sql.Types.LONGVARBINARY;
		} else if (mysqlType.equalsIgnoreCase("MEDIUMBLOB")) {
			return java.sql.Types.LONGVARBINARY;
		} else if (mysqlType.equalsIgnoreCase("LONGBLOB")) {
			return java.sql.Types.LONGVARBINARY;
		} else if (mysqlType.equalsIgnoreCase("TINYTEXT")) {
			return java.sql.Types.VARCHAR;
		} else if (mysqlType.equalsIgnoreCase("TEXT")) {
			return java.sql.Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase("MEDIUMTEXT")) {
			return java.sql.Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase("LONGTEXT")) {
			return java.sql.Types.LONGVARCHAR;
		} else if (mysqlType.equalsIgnoreCase("ENUM")) {
			return mysqlToJavaType(FIELD_TYPE_ENUM);
		} else if (mysqlType.equalsIgnoreCase("SET")) {
			return mysqlToJavaType(FIELD_TYPE_SET);
		} else if (mysqlType.equalsIgnoreCase("GEOMETRY")) {
			return mysqlToJavaType(FIELD_TYPE_GEOMETRY);
		} else if (mysqlType.equalsIgnoreCase("BINARY")) {
			return Types.BINARY; // no concrete type on the wire
		} else if (mysqlType.equalsIgnoreCase("VARBINARY")) {
			return Types.VARBINARY; // no concrete type on the wire
		} else if (mysqlType.equalsIgnoreCase("BIT")) {
			return mysqlToJavaType(FIELD_TYPE_BIT);
		}

		// Punt
		return java.sql.Types.OTHER;
	}

	/**
	 * @param mysqlType
	 * @return String - the types name
	 */
	public static String typeToName(int mysqlType) {
		switch (mysqlType) {
			case MySQLTypes.FIELD_TYPE_DECIMAL: return "FIELD_TYPE_DECIMAL";
			case MySQLTypes.FIELD_TYPE_TINY: return "FIELD_TYPE_TINY";
			case MySQLTypes.FIELD_TYPE_SHORT: return "FIELD_TYPE_SHORT";
			case MySQLTypes.FIELD_TYPE_LONG: return "FIELD_TYPE_LONG";
			case MySQLTypes.FIELD_TYPE_FLOAT: return "FIELD_TYPE_FLOAT";
			case MySQLTypes.FIELD_TYPE_DOUBLE: return "FIELD_TYPE_DOUBLE";
			case MySQLTypes.FIELD_TYPE_NULL: return "FIELD_TYPE_NULL";
			case MySQLTypes.FIELD_TYPE_TIMESTAMP: return "FIELD_TYPE_TIMESTAMP";
			case MySQLTypes.FIELD_TYPE_LONGLONG: return "FIELD_TYPE_LONGLONG";
			case MySQLTypes.FIELD_TYPE_INT24: return "FIELD_TYPE_INT24";
			case MySQLTypes.FIELD_TYPE_DATE: return "FIELD_TYPE_DATE";
			case MySQLTypes.FIELD_TYPE_TIME: return "FIELD_TYPE_TIME";
			case MySQLTypes.FIELD_TYPE_DATETIME: return "FIELD_TYPE_DATETIME";
			case MySQLTypes.FIELD_TYPE_YEAR: return "FIELD_TYPE_YEAR";
			case MySQLTypes.FIELD_TYPE_NEWDATE: return "FIELD_TYPE_NEWDATE";
			case MySQLTypes.FIELD_TYPE_ENUM: return "FIELD_TYPE_ENUM";
			case MySQLTypes.FIELD_TYPE_SET: return "FIELD_TYPE_SET";
			case MySQLTypes.FIELD_TYPE_TINY_BLOB: return "FIELD_TYPE_TINY_BLOB";
			case MySQLTypes.FIELD_TYPE_MEDIUM_BLOB: return "FIELD_TYPE_MEDIUM_BLOB";
			case MySQLTypes.FIELD_TYPE_LONG_BLOB: return "FIELD_TYPE_LONG_BLOB";
			case MySQLTypes.FIELD_TYPE_BLOB: return "FIELD_TYPE_BLOB";
			case MySQLTypes.FIELD_TYPE_VAR_STRING: return "FIELD_TYPE_VAR_STRING";
			case MySQLTypes.FIELD_TYPE_STRING: return "FIELD_TYPE_STRING";
			case MySQLTypes.FIELD_TYPE_VARCHAR: return "FIELD_TYPE_VARCHAR";
			case MySQLTypes.FIELD_TYPE_GEOMETRY: return "FIELD_TYPE_GEOMETRY";
			default: return " Unknown MySQL Type # " + mysqlType;
		}
	}
	
	
//	/**
//	 * @param mysqlType numeric type index
//	 * @return java Type name
//	 */
//	public static String mySqlTypeToJavaTypeName(int mysqlType) {
//		switch (mysqlType) {
//		case MySQLTypes.FIELD_TYPE_DECIMAL:
//			return "java.lang.Double";
//
//		case MySQLTypes.FIELD_TYPE_TINY:
//			return "java.lang.Short";
//
//		case MySQLTypes.FIELD_TYPE_SHORT:
//			return "java.lang.Short";
//
//		case MySQLTypes.FIELD_TYPE_LONG:
//			return "java.lang.Long";
//
//		case MySQLTypes.FIELD_TYPE_FLOAT:
//			return "java.lang.Float";
//
//		case MySQLTypes.FIELD_TYPE_DOUBLE:
//			return "java.lang.Double";
//
//		case MySQLTypes.FIELD_TYPE_NULL:
//			return "java.lang.Object";
//
//		case MySQLTypes.FIELD_TYPE_TIMESTAMP:
//			return "java.sql.Timestamp";
//
//		case MySQLTypes.FIELD_TYPE_LONGLONG:
//			return "java.lang.Long";
//
//		case MySQLTypes.FIELD_TYPE_INT24:
//			return "java.lang.Integer";
//
//		case MySQLTypes.FIELD_TYPE_DATE:
//			return "java.sql.Date";
//
//		case MySQLTypes.FIELD_TYPE_TIME:
//			return "java.sql.Time";
//
//		case MySQLTypes.FIELD_TYPE_DATETIME:
//			return "java.sql.Date";
//
//		case MySQLTypes.FIELD_TYPE_YEAR:
//			return "java.sql.Date";
//
//		case MySQLTypes.FIELD_TYPE_NEWDATE:
//			return "java.sql.Date";
//
//		case MySQLTypes.FIELD_TYPE_ENUM:
//			return "java.lang.Object";
//
//		case MySQLTypes.FIELD_TYPE_SET:
//			return "java.lang.Object";
//
//		case MySQLTypes.FIELD_TYPE_TINY_BLOB:
//			return "java.sql.Blob";
//
//		case MySQLTypes.FIELD_TYPE_MEDIUM_BLOB:
//			return "java.sql.Blob";
//
//		case MySQLTypes.FIELD_TYPE_LONG_BLOB:
//			return "java.sql.Blob";
//
//		case MySQLTypes.FIELD_TYPE_BLOB:
//			return "java.sql.Blob";
//
//		case MySQLTypes.FIELD_TYPE_VAR_STRING:
//			return "java.lang.String";
//
//		case MySQLTypes.FIELD_TYPE_STRING:
//			return "java.lang.String";
//
//		case MySQLTypes.FIELD_TYPE_VARCHAR:
//			return "java.lang.String";
//
//		case MySQLTypes.FIELD_TYPE_GEOMETRY:
//			return "java.lang.Object";
//
//		default:
//			return "java.lang.Object";
//		}
//	}
}
