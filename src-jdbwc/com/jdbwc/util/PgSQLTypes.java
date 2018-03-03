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
 * PostgtreSQL types.
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public class PgSQLTypes {
	
	public static final int UNSPECIFIED = 0;
    public static final int INT2 = 21;
    public static final int INT2_ARRAY = 1005;
    public static final int INT4 = 23;
    public static final int INT4_ARRAY = 1007;
    public static final int INT8 = 20;
    public static final int INT8_ARRAY = 1016;
    public static final int TEXT = 25;
    public static final int TEXT_ARRAY = 1009;
    public static final int NUMERIC = 1700;
    public static final int NUMERIC_ARRAY = 1231;
    public static final int FLOAT4 = 700;
    public static final int FLOAT4_ARRAY = 1021;
    public static final int FLOAT8 = 701;
    public static final int FLOAT8_ARRAY = 1022;
    public static final int BOOL = 16;
    public static final int BOOL_ARRAY = 1000;
    public static final int DATE = 1082;
    public static final int DATE_ARRAY = 1182;
    public static final int TIME = 1083;
    public static final int TIME_ARRAY = 1183;
    public static final int TIMETZ = 1266;
    public static final int TIMETZ_ARRAY = 1270;
    public static final int TIMESTAMP = 1114;
    public static final int TIMESTAMP_ARRAY = 1115;
    public static final int TIMESTAMPTZ = 1184;
    public static final int TIMESTAMPTZ_ARRAY = 1185;
    public static final int BYTEA = 17;
    public static final int BYTEA_ARRAY = 1001;
    public static final int VARCHAR = 1043;
    public static final int VARCHAR_ARRAY = 1015;
    public static final int OID = 26;
    public static final int OID_ARRAY = 1028;
    public static final int BPCHAR = 1042;
    public static final int BPCHAR_ARRAY = 1014;
    public static final int MONEY = 790;
    public static final int MONEY_ARRAY = 791;
    public static final int NAME = 19;
    public static final int NAME_ARRAY = 1003;
    public static final int BIT = 1560;
    public static final int BIT_ARRAY = 1561;
    public static final int VOID = 2278;
    public static final int INTERVAL = 1186;
    public static final int INTERVAL_ARRAY = 1187;
    public static final int CHAR = 18; // This is not char(N), this is "char" a single byte type.
    public static final int CHAR_ARRAY = 1002;
    public static final int VARBIT = 1562;
    public static final int VARBIT_ARRAY = 1563;

	
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

//	private static Map<String, Integer> pgsqlToJdbcTypesMap = new HashMap<String, Integer>();
//	
//	static {
//		pgsqlToJdbcTypesMap.put("BIT", Constants.integerValueOf(pgsqlToJavaType(INT2)));
//		pgsqlToJdbcTypesMap.put("TINYINT", Constants.integerValueOf(pgsqlToJavaType(INT2)));
//		pgsqlToJdbcTypesMap.put("SMALLINT", Constants.integerValueOf(pgsqlToJavaType(INT4)));
//		pgsqlToJdbcTypesMap.put("MEDIUMINT", Constants.integerValueOf(pgsqlToJavaType(INT8)));
//		pgsqlToJdbcTypesMap.put("INT", Constants.integerValueOf(pgsqlToJavaType(INT8)));
//		pgsqlToJdbcTypesMap.put("INTEGER", Constants.integerValueOf(pgsqlToJavaType(INT8)));
//		pgsqlToJdbcTypesMap.put("BIGINT", Constants.integerValueOf(pgsqlToJavaType(INT8)));
//		pgsqlToJdbcTypesMap.put("INT24", Constants.integerValueOf(pgsqlToJavaType(INT8)));
//		pgsqlToJdbcTypesMap.put("REAL", Constants.integerValueOf(pgsqlToJavaType(FLOAT8)));
//		pgsqlToJdbcTypesMap.put("FLOAT", Constants.integerValueOf(pgsqlToJavaType(FLOAT8)));
//		pgsqlToJdbcTypesMap.put("DECIMAL", Constants.integerValueOf(pgsqlToJavaType(FLOAT8)));
//		pgsqlToJdbcTypesMap.put("NUMERIC", Constants.integerValueOf(pgsqlToJavaType(NUMERIC)));
//		pgsqlToJdbcTypesMap.put("DOUBLE", Constants.integerValueOf(pgsqlToJavaType(FLOAT8)));
//		pgsqlToJdbcTypesMap.put("CHAR", Constants.integerValueOf(pgsqlToJavaType(VARCHAR)));
//		pgsqlToJdbcTypesMap.put("VARCHAR", Constants.integerValueOf(pgsqlToJavaType(VARCHAR)));
//		pgsqlToJdbcTypesMap.put("DATE", Constants.integerValueOf(pgsqlToJavaType(DATE)));
//		pgsqlToJdbcTypesMap.put("TIME", Constants.integerValueOf(pgsqlToJavaType(TIME)));
//		pgsqlToJdbcTypesMap.put("YEAR", Constants.integerValueOf(pgsqlToJavaType(INT4)));
//		pgsqlToJdbcTypesMap.put("TIMESTAMP", Constants.integerValueOf(pgsqlToJavaType(TIMESTAMP)));
//		pgsqlToJdbcTypesMap.put("DATETIME", Constants.integerValueOf(pgsqlToJavaType(TIMESTAMP)));
//		pgsqlToJdbcTypesMap.put("TINYBLOB", Constants.integerValueOf(java.sql.Types.BINARY));
//		pgsqlToJdbcTypesMap.put("BLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		pgsqlToJdbcTypesMap.put("MEDIUMBLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		pgsqlToJdbcTypesMap.put("LONGBLOB", Constants.integerValueOf(java.sql.Types.LONGVARBINARY));
//		pgsqlToJdbcTypesMap.put("TINYTEXT", Constants.integerValueOf(java.sql.Types.VARCHAR));
//		pgsqlToJdbcTypesMap.put("TEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		pgsqlToJdbcTypesMap.put("MEDIUMTEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		pgsqlToJdbcTypesMap.put("LONGTEXT", Constants.integerValueOf(java.sql.Types.LONGVARCHAR));
//		pgsqlToJdbcTypesMap.put("ENUM", Constants.integerValueOf(pgsqlToJavaType(UNSPECIFIED)));
//		pgsqlToJdbcTypesMap.put("SET", Constants.integerValueOf(pgsqlToJavaType(UNSPECIFIED)));
//		pgsqlToJdbcTypesMap.put("GEOMETRY", Constants.integerValueOf(pgsqlToJavaType(UNSPECIFIED)));
//	}
	
	public static String pgsqlNameToJdbcName(String pgsqlType) {
		if (pgsqlType.toLowerCase().endsWith("_array")) {
			return "ARRAY";
		}else if (pgsqlType.equalsIgnoreCase("UNSPECIFIED")) {
			return "OTHER";
		} else if (pgsqlType.equalsIgnoreCase("INT2")) {
			return "TINYINT";
		} else if (pgsqlType.equalsIgnoreCase("INT4")) {
			return "SMALLINT";
		} else if (pgsqlType.equalsIgnoreCase("INT8")) {
			return "INTEGER";
		} else if (pgsqlType.equalsIgnoreCase("TEXT")) {
			return "VARCHAR";
		} else if (pgsqlType.equalsIgnoreCase("NUMERIC")) {
			return "NUMERIC";
		} else if (pgsqlType.equalsIgnoreCase("FLOAT4")) {
			return "FLOAT";
		} else if (pgsqlType.equalsIgnoreCase("FLOAT8")) {
			return "FLOAT";
		} else if (pgsqlType.equalsIgnoreCase("BOOL")) {
			return "BOOLEAN";
		} else if (pgsqlType.equalsIgnoreCase("DATE")) {
			return "DATE";
		} else if (pgsqlType.equalsIgnoreCase("TIME")) {
			return "TIME";
		} else if (pgsqlType.equalsIgnoreCase("TIMETZ")) {
			return "TIME";
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMP")) {
			return "TIMESTAMP";
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMPTZ")) {
			return "TIMESTAMP";
		} else if (pgsqlType.equalsIgnoreCase("BYTEA")) {
			return "OTHER";// not sure what BYTEA is???
		} else if (pgsqlType.equalsIgnoreCase("VARCHAR")) {
			return "VARCHAR";
		} else if (pgsqlType.equalsIgnoreCase("OID")) {
			return "INTEGER";
		} else if (pgsqlType.equalsIgnoreCase("BPCHAR")) {
			return "OTHER";// not sure what BPCHAR is???
		} else if (pgsqlType.equalsIgnoreCase("MONEY")) {
			return "DOUBLE";
		} else if (pgsqlType.equalsIgnoreCase("NAME")) {
			return "VARCHAR";
		} else if (pgsqlType.equalsIgnoreCase("BIT")) {
			return "BIT";
		} else if (pgsqlType.equalsIgnoreCase("VOID")) {
			return "NULL";
		} else if (pgsqlType.equalsIgnoreCase("INTERVAL")) {
			return "OTHER";// not sure what INTERVAL is???
		} else if (pgsqlType.equalsIgnoreCase("CHAR")) {
			return "CHAR";
		} else if (pgsqlType.equalsIgnoreCase("VARBIT")) {
			return "VARBINARY";
		} else{
			// Punt
			return "OTHER";
		}
	}
	
	/**
	 * Maps the given PgSQL type name to the correct PgSQL type.
	 */
	public static int pgsqlNameToType(String pgsqlType) {
		int nameAsType = VARCHAR;
		
		if (pgsqlType.equalsIgnoreCase("INT2")) {
			nameAsType = INT2;
		} else if (pgsqlType.equalsIgnoreCase("INT2_ARRAY")) {
			nameAsType = INT2_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("INT4")) {
			nameAsType = INT4;
		} else if (pgsqlType.equalsIgnoreCase("INT4_ARRAY")) {
			nameAsType = INT4_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("INT8")) {
			nameAsType = INT8;
		} else if (pgsqlType.equalsIgnoreCase("INT8_ARRAY")) {
			nameAsType = INT8_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("TEXT")) {
			nameAsType = TEXT;
		} else if (pgsqlType.equalsIgnoreCase("TEXT_ARRAY")) {
			nameAsType = TEXT_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("NUMERIC")) {
			nameAsType = NUMERIC;
		} else if (pgsqlType.equalsIgnoreCase("NUMERIC_ARRAY")) {
			nameAsType = NUMERIC_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT4")) {
			nameAsType = FLOAT4;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT4_ARRAY")) {
			nameAsType = FLOAT4_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT8")) {
			nameAsType = FLOAT8;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT8_ARRAY")) {
			nameAsType = FLOAT8_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("BOOL")) {
			nameAsType = BOOL;
		} else if (pgsqlType.equalsIgnoreCase("BOOL_ARRAY")) {
			nameAsType = BOOL_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("DATE")) {
			nameAsType = DATE;
		} else if (pgsqlType.equalsIgnoreCase("DATE_ARRAY")) {
			nameAsType = DATE_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("TIME")) {
			nameAsType = TIME;
		} else if (pgsqlType.equalsIgnoreCase("TIME_ARRAY")) {
			nameAsType = TIME_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("TIMETZ")) {
			nameAsType = TIMETZ;
		} else if (pgsqlType.equalsIgnoreCase("TIMETZ_ARRAY")) {
			nameAsType = TIMETZ_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMP")) {
			nameAsType = TIMESTAMP;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMP_ARRAY")) {
			nameAsType = TIMESTAMP_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMPTZ")) {
			nameAsType = TIMESTAMPTZ;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMPTZ_ARRAY")) {
			nameAsType = TIMESTAMPTZ_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("BYTEA")) {
			nameAsType = BYTEA;
		} else if (pgsqlType.equalsIgnoreCase("BYTEA_ARRAY")) {
			nameAsType = BYTEA_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("VARCHAR")) {
			nameAsType = VARCHAR;
		} else if (pgsqlType.equalsIgnoreCase("VARCHAR_ARRAY")) {
			nameAsType = VARCHAR_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("OID")) {
			nameAsType = OID;
		} else if (pgsqlType.equalsIgnoreCase("OID_ARRAY")) {
			nameAsType = OID_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("BPCHAR")) {
			nameAsType = BPCHAR;
		} else if (pgsqlType.equalsIgnoreCase("BPCHAR_ARRAY")) {
			nameAsType = BPCHAR_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("MONEY")) {
			nameAsType = MONEY;
		} else if (pgsqlType.equalsIgnoreCase("MONEY_ARRAY")) {
			nameAsType = MONEY_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("NAME")) {
			nameAsType = NAME;
		} else if (pgsqlType.equalsIgnoreCase("NAME_ARRAY")) {
			nameAsType = NAME_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("BIT")) {
			nameAsType = BIT;
		} else if (pgsqlType.equalsIgnoreCase("BIT_ARRAY")) {
			nameAsType = BIT_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("VOID")) {
			nameAsType = VOID;
		} else if (pgsqlType.equalsIgnoreCase("INTERVAL")) {
			nameAsType = INTERVAL;
		} else if (pgsqlType.equalsIgnoreCase("INTERVAL_ARRAY")) {
			nameAsType = INTERVAL_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("CHAR")) {
			nameAsType = CHAR;
		} else if (pgsqlType.equalsIgnoreCase("CHAR_ARRAY")) {
			nameAsType = CHAR_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("VARBIT")) {
			nameAsType = VARBIT;
		} else if (pgsqlType.equalsIgnoreCase("VARBIT_ARRAY")) {
			nameAsType = VARBIT_ARRAY;
		} else if (pgsqlType.equalsIgnoreCase("UNSPECIFIED")) {
			nameAsType = UNSPECIFIED;
		}

		return nameAsType;
	}

	/**
	 * Maps the given PgSQL type to the correct JDBC type.
	 */
	public static int pgsqlToJavaType(int pgsqlType) {
		return pgsqlToJavaType(typeToName(pgsqlType));
	}

	/**
	 * Maps the given MySQL type to the correct JDBC type.
	 */
	public static int pgsqlToJavaType(String pgsqlType) {
		
		if (pgsqlType.toLowerCase().endsWith("_array")) {
			return java.sql.Types.ARRAY;
		}else if (pgsqlType.equalsIgnoreCase("UNSPECIFIED")) {
			return java.sql.Types.OTHER;
		} else if (pgsqlType.equalsIgnoreCase("INT2")) {
			return java.sql.Types.TINYINT;
		} else if (pgsqlType.equalsIgnoreCase("INT4")) {
			return java.sql.Types.SMALLINT;
		} else if (pgsqlType.equalsIgnoreCase("INT8")) {
			return java.sql.Types.INTEGER;
		} else if (pgsqlType.equalsIgnoreCase("TEXT")) {
			return java.sql.Types.VARCHAR;
		} else if (pgsqlType.equalsIgnoreCase("NUMERIC")) {
			return java.sql.Types.NUMERIC;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT4")) {
			return java.sql.Types.FLOAT;
		} else if (pgsqlType.equalsIgnoreCase("FLOAT8")) {
			return java.sql.Types.FLOAT;
		} else if (pgsqlType.equalsIgnoreCase("BOOL")) {
			return java.sql.Types.BOOLEAN;
		} else if (pgsqlType.equalsIgnoreCase("DATE")) {
			return java.sql.Types.DATE;
		} else if (pgsqlType.equalsIgnoreCase("TIME")) {
			return java.sql.Types.TIME;
		} else if (pgsqlType.equalsIgnoreCase("TIMETZ")) {
			return java.sql.Types.TIME;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMP")) {
			return java.sql.Types.TIMESTAMP;
		} else if (pgsqlType.equalsIgnoreCase("TIMESTAMPTZ")) {
			return java.sql.Types.TIMESTAMP;
		} else if (pgsqlType.equalsIgnoreCase("BYTEA")) {
			return java.sql.Types.OTHER;// not sure what BYTEA is???
		} else if (pgsqlType.equalsIgnoreCase("VARCHAR")) {
			return java.sql.Types.VARCHAR;
		} else if (pgsqlType.equalsIgnoreCase("OID")) {
			return java.sql.Types.INTEGER;
		} else if (pgsqlType.equalsIgnoreCase("BPCHAR")) {
			return java.sql.Types.OTHER;// not sure what BPCHAR is???
		} else if (pgsqlType.equalsIgnoreCase("MONEY")) {
			return java.sql.Types.DOUBLE;
		} else if (pgsqlType.equalsIgnoreCase("NAME")) {
			return java.sql.Types.VARCHAR;
		} else if (pgsqlType.equalsIgnoreCase("BIT")) {
			return java.sql.Types.BIT;
		} else if (pgsqlType.equalsIgnoreCase("VOID")) {
			return java.sql.Types.NULL;
		} else if (pgsqlType.equalsIgnoreCase("INTERVAL")) {
			return java.sql.Types.OTHER;// not sure what INTERVAL is???
		} else if (pgsqlType.equalsIgnoreCase("CHAR")) {
			return java.sql.Types.CHAR;
		} else if (pgsqlType.equalsIgnoreCase("VARBIT")) {
			return java.sql.Types.VARBINARY;
		}

		// Punt
		return java.sql.Types.OTHER;
	}

	/**
	 * @param pgsqlType numeric type index
	 * @return String - the types name
	 */
	public static String typeToName(int pgsqlType) {
		switch (pgsqlType) {

	    case INT2: return "INT2";
	    case INT2_ARRAY: return "INT2_ARRAY";
	    case INT4: return "INT4";
	    case INT4_ARRAY: return "INT4_ARRAY";
	    case INT8: return "INT8";
	    case INT8_ARRAY: return "INT8_ARRAY";
	    case TEXT: return "TEXT";
	    case TEXT_ARRAY: return "TEXT_ARRAY";
	    case NUMERIC: return "NUMERIC";
	    case NUMERIC_ARRAY: return "NUMERIC_ARRAY";
	    case FLOAT4: return "FLOAT4";
	    case FLOAT4_ARRAY: return "FLOAT4_ARRAY";
	    case FLOAT8: return "FLOAT8";
	    case FLOAT8_ARRAY: return "FLOAT8_ARRAY";
	    case BOOL: return "BOOL";
	    case BOOL_ARRAY: return "BOOL_ARRAY";
	    case DATE: return "DATE";
	    case DATE_ARRAY: return "DATE_ARRAY";
	    case TIME: return "TIME";
	    case TIME_ARRAY: return "TIME_ARRAY";
	    case TIMETZ: return "TIMETZ";
	    case TIMETZ_ARRAY: return "TIMETZ_ARRAY";
	    case TIMESTAMP: return "TIMESTAMP";
	    case TIMESTAMP_ARRAY: return "TIMESTAMP_ARRAY";
	    case TIMESTAMPTZ: return "TIMESTAMPTZ";
	    case TIMESTAMPTZ_ARRAY: return "TIMESTAMPTZ_ARRAY";
	    case BYTEA: return "BYTEA";
	    case BYTEA_ARRAY: return "BYTEA_ARRAY";
	    case VARCHAR: return "VARCHAR";
	    case VARCHAR_ARRAY: return "VARCHAR_ARRAY";
	    case OID: return "OID";
	    case OID_ARRAY: return "OID_ARRAY";
	    case BPCHAR: return "BPCHAR";
	    case BPCHAR_ARRAY: return "BPCHAR_ARRAY";
	    case MONEY: return "MONEY";
	    case MONEY_ARRAY: return "MONEY_ARRAY";
	    case NAME: return "NAME";
	    case NAME_ARRAY: return "NAME_ARRAY";
	    case BIT: return "BIT";
	    case BIT_ARRAY: return "BIT_ARRAY";
	    case VOID: return "VOID";
	    case INTERVAL: return "INTERVAL";
	    case INTERVAL_ARRAY: return "INTERVAL_ARRAY";
	    case CHAR: return "CHAR";
	    case CHAR_ARRAY: return "CHAR_ARRAY";
	    case VARBIT: return "VARBIT";
		case VARBIT_ARRAY: return "VARBIT_ARRAY";

		default:
			return "UNSPECIFIED";
		}
	}
	
	
	/**
	 * @param pgsqlType numeric type index
	 * @return java Type name
	 */
	public static String pgSqlTypeToJavaTypeName(int pgsqlType) {
		
//UNSPECIFIED = 0;
//INT2 = 21;
//INT2_ARRAY = 1005;
//INT4 = 23;
//INT4_ARRAY = 1007;
//INT8 = 20;
//INT8_ARRAY = 1016;
//TEXT = 25;
//TEXT_ARRAY = 1009;
//NUMERIC = 1700;
//NUMERIC_ARRAY = 1231;
//FLOAT4 = 700;
//FLOAT4_ARRAY = 1021;
//FLOAT8 = 701;
//FLOAT8_ARRAY = 1022;
//BOOL = 16;
//BOOL_ARRAY = 1000;
//DATE = 1082;
//DATE_ARRAY = 1182;
//TIME = 1083;
//TIME_ARRAY = 1183;
//TIMETZ = 1266;
//TIMETZ_ARRAY = 1270;
//TIMESTAMP = 1114;
//TIMESTAMP_ARRAY = 1115;
//TIMESTAMPTZ = 1184;
//TIMESTAMPTZ_ARRAY = 1185;
//BYTEA = 17;
//BYTEA_ARRAY = 1001;
//VARCHAR = 1043;
//VARCHAR_ARRAY = 1015;
//OID = 26;
//OID_ARRAY = 1028;
//BPCHAR = 1042;
//BPCHAR_ARRAY = 1014;
//MONEY = 790;
//MONEY_ARRAY = 791;
//NAME = 19;
//NAME_ARRAY = 1003;
//BIT = 1560;
//BIT_ARRAY = 1561;
//VOID = 2278;
//INTERVAL = 1186;
//INTERVAL_ARRAY = 1187;
//CHAR = 18; // This is not char(N), this is "char" a single byte type.
//CHAR_ARRAY = 1002;
//VARBIT = 1562;
//VARBIT_ARRAY = 1563;
		
		
//		switch (pgsqlType) {
//		case PgSQLTypes.FIELD_TYPE_DECIMAL:
//			return "java.lang.Double";
//
//		case PgSQLTypes.FIELD_TYPE_TINY:
//			return "java.lang.Short";
//
//		case PgSQLTypes.FIELD_TYPE_SHORT:
//			return "java.lang.Short";
//
//		case PgSQLTypes.FIELD_TYPE_LONG:
//			return "java.lang.Long";
//
//		case PgSQLTypes.FIELD_TYPE_FLOAT:
//			return "java.lang.Float";
//
//		case PgSQLTypes.FIELD_TYPE_DOUBLE:
//			return "java.lang.Double";
//
//		case PgSQLTypes.FIELD_TYPE_NULL:
//			return "java.lang.Object";
//
//		case PgSQLTypes.FIELD_TYPE_TIMESTAMP:
//			return "java.sql.Timestamp";
//
//		case PgSQLTypes.FIELD_TYPE_LONGLONG:
//			return "java.lang.Long";
//
//		case PgSQLTypes.FIELD_TYPE_INT24:
//			return "java.lang.Integer";
//
//		case PgSQLTypes.FIELD_TYPE_DATE:
//			return "java.sql.Date";
//
//		case PgSQLTypes.FIELD_TYPE_TIME:
//			return "java.sql.Time";
//
//		case PgSQLTypes.FIELD_TYPE_DATETIME:
//			return "java.sql.Date";
//
//		case PgSQLTypes.FIELD_TYPE_YEAR:
//			return "java.sql.Date";
//
//		case PgSQLTypes.FIELD_TYPE_NEWDATE:
//			return "java.sql.Date";
//
//		case PgSQLTypes.FIELD_TYPE_ENUM:
//			return "java.lang.Object";
//
//		case PgSQLTypes.FIELD_TYPE_SET:
//			return "java.lang.Object";
//
//		case PgSQLTypes.FIELD_TYPE_TINY_BLOB:
//			return "java.sql.Blob";
//
//		case PgSQLTypes.FIELD_TYPE_MEDIUM_BLOB:
//			return "java.sql.Blob";
//
//		case PgSQLTypes.FIELD_TYPE_LONG_BLOB:
//			return "java.sql.Blob";
//
//		case PgSQLTypes.FIELD_TYPE_BLOB:
//			return "java.sql.Blob";
//
//		case PgSQLTypes.FIELD_TYPE_VAR_STRING:
//			return "java.lang.String";
//
//		case PgSQLTypes.FIELD_TYPE_STRING:
//			return "java.lang.String";
//
//		case PgSQLTypes.FIELD_TYPE_VARCHAR:
//			return "java.lang.String";
//
//		case PgSQLTypes.FIELD_TYPE_GEOMETRY:
//			return "java.lang.Object";
//
//		default:
//			return "java.lang.Object";
//		}
		return "java.lang.Object";
	}
}
