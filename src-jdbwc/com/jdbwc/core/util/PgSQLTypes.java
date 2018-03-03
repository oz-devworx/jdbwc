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
 * PostgtreSQL types.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-05-20
 */
public final class PgSQLTypes {

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



	/**
	 * Maps PostgtreSQL type name to Java.sql.types
	 */
	public static int pgsqlNameToJdbcType(final String pgsqlType) {
		if (pgsqlType.toLowerCase().endsWith("_array")) {
			return java.sql.Types.ARRAY;
		}else if (pgsqlType.equalsIgnoreCase("UNSPECIFIED")) {
			return java.sql.Types.OTHER;
		} else if (pgsqlType.equalsIgnoreCase("INT2")) {
			return java.sql.Types.INTEGER;
		} else if (pgsqlType.equalsIgnoreCase("INT4")) {
			return java.sql.Types.INTEGER;
		} else if (pgsqlType.equalsIgnoreCase("INT8")) {
			return java.sql.Types.BIGINT;
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

		// Dont know. Guess
		return java.sql.Types.VARCHAR;
	}

	/**
	 * Maps the given PostgtreSQL type to the equivalent java.sql.Types name.
	 */
	public static String pgsqlTypeToJdbcName(final int pgsqlType) {
		switch (pgsqlType) {

		    case INT2_ARRAY:
		    case INT4_ARRAY:
		    case NUMERIC_ARRAY:
		    case INT8_ARRAY:
		    case TEXT_ARRAY:
		    case FLOAT4_ARRAY:
		    case FLOAT8_ARRAY:
		    case BOOL_ARRAY:
		    case DATE_ARRAY:
		    case TIME_ARRAY:
			case TIMETZ_ARRAY:
			case TIMESTAMP_ARRAY:
			case TIMESTAMPTZ_ARRAY:
		    case BYTEA_ARRAY:
			case VARCHAR_ARRAY:
			case OID_ARRAY:
			case BPCHAR_ARRAY:
			case MONEY_ARRAY:
			case NAME_ARRAY:
			case BIT_ARRAY:
			case INTERVAL_ARRAY:
			case CHAR_ARRAY:
			case VARBIT_ARRAY:

				return "ARRAY";

			case UNSPECIFIED: return "OTHER";
		    case INT2: return "INTEGER";
		    case INT4: return "INTEGER";
		    case INT8: return "BIGINT";
		    case TEXT: return "LONGVARCHAR";
		    case NUMERIC: return "NUMERIC";
		    case FLOAT4: return "FLOAT";
		    case FLOAT8: return "DOUBLE";
		    case BOOL: return "BOOLEAN";
		    case DATE: return "TIMESTAMP";
		    case TIME: return "TIME";
		    case TIMETZ: return "TIMESTAMP";
		    case TIMESTAMP: return "TIMESTAMP";
		    case TIMESTAMPTZ: return "TIMESTAMP";
		    case BYTEA: return "CHAR";
		    case VARCHAR: return "VARCHAR";
		    case OID: return "INTEGER";
		    case BPCHAR: return "CHAR";
		    case MONEY: return "DOUBLE";
		    case NAME: return "VARCHAR";
		    case BIT: return "BIT";
		    case VOID: return "NULL";
		    case INTERVAL: return "CHAR";
		    case CHAR: return "CHAR";
		    case VARBIT: return "VARCHAR";

			default: return "VARCHAR";// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * Maps the given PostgtreSQL type to the equivalent JDBC type.
	 */
	public static int pgsqlTypeToJdbcType(final int pgsqlType) {

		switch (pgsqlType) {

		    case INT2_ARRAY:
		    case INT4_ARRAY:
		    case NUMERIC_ARRAY:
		    case INT8_ARRAY:
		    case TEXT_ARRAY:
		    case FLOAT4_ARRAY:
		    case FLOAT8_ARRAY:
		    case BOOL_ARRAY:
		    case DATE_ARRAY:
		    case TIME_ARRAY:
			case TIMETZ_ARRAY:
			case TIMESTAMP_ARRAY:
			case TIMESTAMPTZ_ARRAY:
		    case BYTEA_ARRAY:
			case VARCHAR_ARRAY:
			case OID_ARRAY:
			case BPCHAR_ARRAY:
			case MONEY_ARRAY:
			case NAME_ARRAY:
			case BIT_ARRAY:
			case INTERVAL_ARRAY:
			case CHAR_ARRAY:
			case VARBIT_ARRAY:

				return Types.ARRAY;

			case UNSPECIFIED: return Types.OTHER;
		    case INT2: return Types.INTEGER;
		    case INT4: return Types.INTEGER;
		    case INT8: return Types.BIGINT;
		    case TEXT: return Types.LONGVARCHAR;
		    case NUMERIC: return Types.NUMERIC;
		    case FLOAT4: return Types.FLOAT;
		    case FLOAT8: return Types.DOUBLE;
		    case BOOL: return Types.BOOLEAN;
		    case DATE: return Types.TIMESTAMP;
		    case TIME: return Types.TIME;
		    case TIMETZ: return Types.TIMESTAMP;
		    case TIMESTAMP: return Types.TIMESTAMP;
		    case TIMESTAMPTZ: return Types.TIMESTAMP;
		    case BYTEA: return Types.CHAR;
		    case VARCHAR: return Types.VARCHAR;
		    case OID: return Types.INTEGER;
		    case BPCHAR: return Types.CHAR;
		    case MONEY: return Types.DOUBLE;
		    case NAME: return Types.VARCHAR;
		    case BIT: return Types.BIT;
		    case VOID: return Types.NULL;
		    case INTERVAL: return Types.CHAR;
		    case CHAR: return Types.CHAR;
		    case VARBIT: return Types.VARCHAR;

			default: return Types.VARCHAR;// Guess? Return varchar for max compatibility
		}
	}


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
			return "VARCHAR";// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * Maps PostgtreSQL type id's to thier type name.
	 *
	 * @param pgsqlType int - PostgtreSQL type id
	 * @return String - the PostgtreSQL type name
	 */
	public static String pgsqlTypeToPgsqlName(final int pgsqlType) {
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

			default: return "VARCHAR";// Guess? Return varchar for max compatibility
		}
	}

	/**
	 * Maps the given PgSQL type name to its PgSQL type id.
	 *
	 * @param pgsqlType String - the PostgtreSQL type name
	 * @return int - PostgtreSQL type id
	 */
	public static int pgsqlNameToPgsqlType(String pgsqlType) {
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
}
