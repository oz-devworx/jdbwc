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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 * This MetaData class is designed for MySql implementations.<br />
 * <br />
 * This implementation has had its query logic migrated to a separate class
 * MySQLDbMetaDataFromInfoSchema.<br />
 * <br />
 * <b>NOTE:</b> Portions of this class were originally derived from MySql-Connector/J. 
 * Namely the (Strings) function names, (Strings) keywords.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public final class MySQLDatabaseMetaData extends MySQLDBMDFromInfoSchema implements java.sql.DatabaseMetaData {

	/* DEVELOPER NOTE:
	 * ***************
	 * We try to keep the bulk of our keywords at the start of this class.
	 * The purpose is to make it easier to update this information as we need to.
	 * It also makes other database implementations easier to manage.
	 */

	/** System functions supported by this Driver */
	private static final String mySystemFuncs =
		"DATABASE,USER,SYSTEM_USER,SESSION_USER,PASSWORD,ENCRYPT,LAST_INSERT_ID,VERSION";

	/** Numeric functions supported by this Driver */
	private static final String myNumericFuncs =
		"ABS,ACOS,ASIN,ATAN,ATAN2,BIT_COUNT,CEILING,COS,"
		+ "COT,DEGREES,EXP,FLOOR,LOG,LOG10,MAX,MIN,MOD,PI,POW,"
		+ "POWER,RADIANS,RAND,ROUND,SIN,SQRT,TAN,TRUNCATE";

	/** TimeDate functions supported by this Driver */
	private static final String myTimeDateFuncs =
		"DAYOFWEEK,WEEKDAY,DAYOFMONTH,DAYOFYEAR,MONTH,DAYNAME,"
		+ "MONTHNAME,QUARTER,WEEK,YEAR,HOUR,MINUTE,SECOND,PERIOD_ADD,"
		+ "PERIOD_DIFF,TO_DAYS,FROM_DAYS,DATE_FORMAT,TIME_FORMAT,"
		+ "CURDATE,CURRENT_DATE,CURTIME,CURRENT_TIME,NOW,SYSDATE,"
		+ "CURRENT_TIMESTAMP,UNIX_TIMESTAMP,FROM_UNIXTIME,"
		+ "SEC_TO_TIME,TIME_TO_SEC";

	/** String functions supported by this Driver */
	private static final String myStringFuncs =
		"ASCII,BIN,BIT_LENGTH,CHAR,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT,"
		+ "CONCAT_WS,CONV,ELT,EXPORT_SET,FIELD,FIND_IN_SET,HEX,INSERT,"
		+ "INSTR,LCASE,LEFT,LENGTH,LOAD_FILE,LOCATE,LOCATE,LOWER,LPAD,"
		+ "LTRIM,MAKE_SET,MATCH,MID,OCT,OCTET_LENGTH,ORD,POSITION,"
		+ "QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX,"
		+ "SPACE,STRCMP,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING,"
		+ "SUBSTRING_INDEX,TRIM,UCASE,UPPER";

	/** SQL Keywords specific to this Driver */
	private static String mysqlKeywordsThatArentSQL92;
	static {
		// Current as-of MySQL-5.1.16
		String[] allMySQLKeywords = new String[] { "ACCESSIBLE", "ADD", "ALL",
				"ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE",
				"BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL",
				"CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
				"COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT",
				"CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE",
				"CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
				"DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND",
				"DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE",
				"DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE",
				"DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE",
				"DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED",
				"ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH",
				"FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM",
				"FULLTEXT", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY",
				"HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF",
				"IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT",
				"INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4",
				"INT8", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN",
				"KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE",
				"LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME",
				"LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT",
				"LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT",
				"MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND",
				"MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT",
				"NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE",
				"OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER",
				"OUTFILE", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE",
				"RANGE", "READ", "READS", "READ_ONLY", "READ_WRITE", "REAL",
				"REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT",
				"REPLACE", "REQUIRE", "RESTRICT", "RETURN", "REVOKE", "RIGHT",
				"RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT",
				"SENSITIVE", "SEPARATOR", "SET", "SHOW", "SMALLINT", "SPATIAL",
				"SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
				"SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT",
				"SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED",
				"THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING",
				"TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK",
				"UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE",
				"UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR",
				"VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH",
				"WRITE", "X509", "XOR", "YEAR_MONTH", "ZEROFILL" };

		String[] sql92Keywords = new String[] { "ABSOLUTE", "EXEC", "OVERLAPS",
				"ACTION", "EXECUTE", "PAD", "ADA", "EXISTS", "PARTIAL", "ADD",
				"EXTERNAL", "PASCAL", "ALL", "EXTRACT", "POSITION", "ALLOCATE",
				"FALSE", "PRECISION", "ALTER", "FETCH", "PREPARE", "AND",
				"FIRST", "PRESERVE", "ANY", "FLOAT", "PRIMARY", "ARE", "FOR",
				"PRIOR", "AS", "FOREIGN", "PRIVILEGES", "ASC", "FORTRAN",
				"PROCEDURE", "ASSERTION", "FOUND", "PUBLIC", "AT", "FROM",
				"READ", "AUTHORIZATION", "FULL", "REAL", "AVG", "GET",
				"REFERENCES", "BEGIN", "GLOBAL", "RELATIVE", "BETWEEN", "GO",
				"RESTRICT", "BIT", "GOTO", "REVOKE", "BIT_LENGTH", "GRANT",
				"RIGHT", "BOTH", "GROUP", "ROLLBACK", "BY", "HAVING", "ROWS",
				"CASCADE", "HOUR", "SCHEMA", "CASCADED", "IDENTITY", "SCROLL",
				"CASE", "IMMEDIATE", "SECOND", "CAST", "IN", "SECTION",
				"CATALOG", "INCLUDE", "SELECT", "CHAR", "INDEX", "SESSION",
				"CHAR_LENGTH", "INDICATOR", "SESSION_USER", "CHARACTER",
				"INITIALLY", "SET", "CHARACTER_LENGTH", "INNER", "SIZE",
				"CHECK", "INPUT", "SMALLINT", "CLOSE", "INSENSITIVE", "SOME",
				"COALESCE", "INSERT", "SPACE", "COLLATE", "INT", "SQL",
				"COLLATION", "INTEGER", "SQLCA", "COLUMN", "INTERSECT",
				"SQLCODE", "COMMIT", "INTERVAL", "SQLERROR", "CONNECT", "INTO",
				"SQLSTATE", "CONNECTION", "IS", "SQLWARNING", "CONSTRAINT",
				"ISOLATION", "SUBSTRING", "CONSTRAINTS", "JOIN", "SUM",
				"CONTINUE", "KEY", "SYSTEM_USER", "CONVERT", "LANGUAGE",
				"TABLE", "CORRESPONDING", "LAST", "TEMPORARY", "COUNT",
				"LEADING", "THEN", "CREATE", "LEFT", "TIME", "CROSS", "LEVEL",
				"TIMESTAMP", "CURRENT", "LIKE", "TIMEZONE_HOUR",
				"CURRENT_DATE", "LOCAL", "TIMEZONE_MINUTE", "CURRENT_TIME",
				"LOWER", "TO", "CURRENT_TIMESTAMP", "MATCH", "TRAILING",
				"CURRENT_USER", "MAX", "TRANSACTION", "CURSOR", "MIN",
				"TRANSLATE", "DATE", "MINUTE", "TRANSLATION", "DAY", "MODULE",
				"TRIM", "DEALLOCATE", "MONTH", "TRUE", "DEC", "NAMES", "UNION",
				"DECIMAL", "NATIONAL", "UNIQUE", "DECLARE", "NATURAL",
				"UNKNOWN", "DEFAULT", "NCHAR", "UPDATE", "DEFERRABLE", "NEXT",
				"UPPER", "DEFERRED", "NO", "USAGE", "DELETE", "NONE", "USER",
				"DESC", "NOT", "USING", "DESCRIBE", "NULL", "VALUE",
				"DESCRIPTOR", "NULLIF", "VALUES", "DIAGNOSTICS", "NUMERIC",
				"VARCHAR", "DISCONNECT", "OCTET_LENGTH", "VARYING", "DISTINCT",
				"OF", "VIEW", "DOMAIN", "ON", "WHEN", "DOUBLE", "ONLY",
				"WHENEVER", "DROP", "OPEN", "WHERE", "ELSE", "OPTION", "WITH",
				"END", "OR", "WORK", "END-EXEC", "ORDER", "WRITE", "ESCAPE",
				"OUTER", "YEAR", "EXCEPT", "OUTPUT", "ZONE", "EXCEPTION" };

		TreeMap<String, Object> mySQLKeywordMap = new TreeMap<String, Object>();

		for (int i = 0; i < allMySQLKeywords.length; i++) {
			mySQLKeywordMap.put(allMySQLKeywords[i], null);
		}

		HashMap<String, Object> sql92KeywordMap = new HashMap<String, Object>(sql92Keywords.length);

		for (int i = 0; i < sql92Keywords.length; i++) {
			sql92KeywordMap.put(sql92Keywords[i], null);
		}

		Iterator<String> it = sql92KeywordMap.keySet().iterator();

		while (it.hasNext()) {
			mySQLKeywordMap.remove(it.next());
		}

		StringBuffer keywordBuf = new StringBuffer();

		it = mySQLKeywordMap.keySet().iterator();

		if (it.hasNext()) {
			keywordBuf.append(it.next().toString());
		}

		while (it.hasNext()) {
			keywordBuf.append(",");
			keywordBuf.append(it.next().toString());
		}

		mysqlKeywordsThatArentSQL92 = keywordBuf.toString();
	}


	protected MySQLDatabaseMetaData(WCConnection connection) throws SQLException {
		super(connection);
	}

	public boolean allProceduresAreCallable() throws SQLException {
		return false;
	}

	public boolean allTablesAreSelectable() throws SQLException {
		return true;
	}

	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		return true;
	}

	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return false;
	}

	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		return false;
	}

	public boolean deletesAreDetected(int type) throws SQLException {
		return true;
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return true;
	}

	public String getCatalogSeparator() throws SQLException {
		return ".";
	}

	public String getCatalogTerm() throws SQLException {
		return "database";
	}

	public Connection getConnection() throws SQLException {
		return myConnection;
	}

	public int getDatabaseMajorVersion() throws SQLException {
		return myConnection.getDatabaseMajorVersion();
	}

	public int getDatabaseMinorVersion() throws SQLException {
		return myConnection.getDatabaseMinorVersion();
	}

	public String getDatabaseProductName() throws SQLException {
		return myConnection.getDatabaseProductName();
	}

	public String getDatabaseProductVersion() throws SQLException {
		return myConnection.getDatabaseProductVersion();
	}

	public int getDriverMajorVersion() {
		return myConnection.getDriverMajorVersion();
	}

	public int getDriverMinorVersion() {
		return myConnection.getDriverMinorVersion();
	}

	public String getDriverName() throws SQLException {
		return myConnection.getDriverName();
	}

	public String getDriverVersion() throws SQLException {
		return myConnection.getDriverVersion();
	}

	public String getExtraNameCharacters() throws SQLException {
		return "#@";
	}

	public String getIdentifierQuoteString() throws SQLException {
		return "`";
		//return "\"";
		//return " ";
	}

	/** 
	 * JDBC 3 is the conformance level we have aimed/aiming for
	 * with this Driver but some of the major areas
	 * (PreparedStatement, CallableStatement and ParameterMetaData)
	 * and newer datatypes are incomplete or unsupported.
	 * This will be rectified at some point.<br />
	 * Therefore the official JDBC standard is really 2
	 *
	 * MySQL DatabaseMetaData info was derived from
	 * MySQL Connector/J (JDBC 2, 3 and 4 implementations)
	 *
	 * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()
	 */
	public int getJDBCMajorVersion() throws SQLException {
		return 3;
	}

	/**
	 * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()
	 */
	public int getJDBCMinorVersion() throws SQLException {
		return 0;
	}

	public int getMaxBinaryLiteralLength() throws SQLException {
		return 16777208;
	}

	public int getMaxCatalogNameLength() throws SQLException {
		return 32;
	}

	public int getMaxCharLiteralLength() throws SQLException {
		return 16777208;
	}

	public int getMaxColumnNameLength() throws SQLException {
		return 64;
	}

	public int getMaxColumnsInGroupBy() throws SQLException {
		return 64;
	}

	public int getMaxColumnsInIndex() throws SQLException {
		return 16;
	}

	public int getMaxColumnsInOrderBy() throws SQLException {
		return 64;
	}

	public int getMaxColumnsInSelect() throws SQLException {
		return 256;
	}

	public int getMaxColumnsInTable() throws SQLException {
		return 512;
	}

	public int getMaxConnections() throws SQLException {
		return 0;
	}

	public int getMaxCursorNameLength() throws SQLException {
		return 64;
	}

	public int getMaxIndexLength() throws SQLException {
		return 256;
	}

	public int getMaxProcedureNameLength() throws SQLException {
		return 0;
	}

	public int getMaxRowSize() throws SQLException {
		return Integer.MAX_VALUE - 8; // Max buffer size - HEADER
	}

	public int getMaxSchemaNameLength() throws SQLException {
		return 0;
	}

	public int getMaxStatementLength() throws SQLException {
		return 65535;
	}

	public int getMaxStatements() throws SQLException {
		return 0;
	}

	public int getMaxTableNameLength() throws SQLException {
		return 64;
	}

	public int getMaxTablesInSelect() throws SQLException {
		return 256;
	}

	public int getMaxUserNameLength() throws SQLException {
		return 16;
	}

	public String getNumericFunctions() throws SQLException {
		return myNumericFuncs;
	}

	public int getResultSetHoldability() throws SQLException {
		return ResultSet.HOLD_CURSORS_OVER_COMMIT;
	}

	public RowIdLifetime getRowIdLifetime() throws SQLException {
		// TODO implement me!?
		return null;
	}

	public String getSQLKeywords() throws SQLException {
		return mysqlKeywordsThatArentSQL92;
	}

	/**
	 * @see java.sql.DatabaseMetaData#getSQLStateType()
	 */
	public int getSQLStateType() throws SQLException {
		int retVal = java.sql.DatabaseMetaData.sqlStateXOpen;
		if (myConnection.versionMeetsMinimum(4, 1, 0)) {
			retVal = java.sql.DatabaseMetaData.sqlStateSQL99;
		}
		return retVal;
	}

	public String getSchemaTerm() throws SQLException {
		return "";
	}

	/**
	 * @see java.sql.DatabaseMetaData#getSearchStringEscape()
	 */
	public String getSearchStringEscape() throws SQLException {
		return "\\";
	}

	public String getStringFunctions() throws SQLException {
		return myStringFuncs;
	}

	public ResultSet getSuperTables(String catalog, String schemaPattern,
			String tableNamePattern) throws SQLException {
		// TODO implement me!?
		return null;
	}

	public ResultSet getSuperTypes(String catalog, String schemaPattern,
			String typeNamePattern) throws SQLException {
		// TODO implement me!?
		return null;
	}

	public String getSystemFunctions() throws SQLException {
		return mySystemFuncs;
	}

	public ResultSet getTableTypes() throws SQLException {
		WCResultSet res = new WCResultSet(myConnection);
		String[] typesSplit = myTabletypes.split(",");

		for(int i = 0; i < typesSplit.length; i++){
			DataHandler aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
			aRow.addData("TABLE_TYPE", typesSplit[i]);
			res.addRow(aRow);
		}

		return res;
	}

	public String getTimeDateFunctions() throws SQLException {
		return myTimeDateFuncs;
	}

	/**
	 * The data content and inline comments for this method was
	 * derived from the MySQL-Connector/J Driver.<br />
	 * However the structure and concepts vary significantly from Connector/J.
	 *
	 * @see java.sql.DatabaseMetaData#getTypeInfo()
	 */
	public ResultSet getTypeInfo() throws SQLException {

		final String NAME = "TYPE_NAME";
		final String TYPE = "DATA_TYPE";
		final String PRECISION = "PRECISION";
		final String PREFIX = "LITERAL_PREFIX";
		final String SUFFIX = "LITERAL_SUFFIX";
		final String CREATE = "CREATE_PARAMS";
		final String NULLABLE = "NULLABLE";
		final String SENSITIVE = "CASE_SENSITIVE";
		final String SEARCHABLE = "SEARCHABLE";
		final String ATTRIBUTE = "UNSIGNED_ATTRIBUTE";
		final String PREC_SCALE = "FIXED_PREC_SCALE";
		final String INCREMENT = "AUTO_INCREMENT";
		final String L_TYPE_NAME = "LOCAL_TYPE_NAME";
		final String MIN_SCALE = "MINIMUM_SCALE";
		final String MAX_SCALE = "MAXIMUM_SCALE";
		final String SQL_DATATYPE = "SQL_DATA_TYPE";
		final String SQL_DT_SUB = "SQL_DATETIME_SUB";
		final String RADIX = "NUM_PREC_RADIX";

		/*
		 * The following are ordered by java.sql.Types, and then by how closely
		 * the MySQL type matches the JDBC Type (per spec)
		 */
		WCResultSet res = new WCResultSet(myConnection);

		/*
		 * MySQL Type: BIT (silently converted to TINYINT(1)) JDBC Type: BIT
		 */
		DataHandler aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BIT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.BIT));
		aRow.addData(PRECISION, "1");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "BIT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: BOOL (silently converted to TINYINT(1)) JDBC Type: BIT
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BOOL");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.BIT));
		aRow.addData(PRECISION, "1");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "BOOL");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TINYINT JDBC Type: TINYINT
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TINYINT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.TINYINT));
		aRow.addData(PRECISION, "3");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "TINYINT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TINYINT UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.TINYINT));
		aRow.addData(PRECISION, "3");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "TINYINT UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: BIGINT JDBC Type: BIGINT
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BIGINT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.BIGINT));
		aRow.addData(PRECISION, "19");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "BIGINT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BIGINT UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.BIGINT));
		aRow.addData(PRECISION, "20");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "BIGINT UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: LONG VARBINARY JDBC Type: LONGVARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "LONG VARBINARY");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARBINARY));
		aRow.addData(PRECISION, "16777215");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "LONG VARBINARY");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: MEDIUMBLOB JDBC Type: LONGVARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "MEDIUMBLOB");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARBINARY));
		aRow.addData(PRECISION, "16777215");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "MEDIUMBLOB");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: LONGBLOB JDBC Type: LONGVARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "LONGBLOB");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARBINARY));
		aRow.addData(PRECISION, Integer.toString(Integer.MAX_VALUE));
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "LONGBLOB");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: BLOB JDBC Type: LONGVARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BLOB");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARBINARY));
		aRow.addData(PRECISION, "65535");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "BLOB");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TINYBLOB JDBC Type: LONGVARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TINYBLOB");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARBINARY));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "TINYBLOB");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: VARBINARY (sliently converted to VARCHAR(M) BINARY) JDBC
		 * Type: VARBINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "VARBINARY");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.VARBINARY));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "(M)");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "VARBINARY");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: BINARY (silently converted to CHAR(M) BINARY) JDBC Type:
		 * BINARY
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "BINARY");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.BINARY));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "(M)");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "true");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "BINARY");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: LONG VARCHAR JDBC Type: LONGVARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "LONG VARCHAR");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARCHAR));
		aRow.addData(PRECISION, "16777215");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "LONG VARCHAR");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: MEDIUMTEXT JDBC Type: LONGVARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "MEDIUMTEXT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARCHAR));
		aRow.addData(PRECISION, "16777215");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "MEDIUMTEXT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: LONGTEXT JDBC Type: LONGVARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "LONGTEXT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARCHAR));
		aRow.addData(PRECISION, Integer.toString(Integer.MAX_VALUE));
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "LONGTEXT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TEXT JDBC Type: LONGVARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TEXT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARCHAR));
		aRow.addData(PRECISION, "65535");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "TEXT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TINYTEXT JDBC Type: LONGVARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TINYTEXT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.LONGVARCHAR));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "TINYTEXT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: CHAR JDBC Type: CHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "CHAR");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.CHAR));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "(M)");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "CHAR");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		// The maximum number of digits for DECIMAL or NUMERIC is 65 (64 from MySQL 5.0.3 to 5.0.5).
		int decimalPrecision = 254;
		if (myConnection.versionMeetsMinimum(5,0,3)) {
			if (myConnection.versionMeetsMinimum(5, 0, 6)) {
				decimalPrecision = 65;
			} else {
				decimalPrecision = 64;
			}
		}

		/*
		 * MySQL Type: NUMERIC (silently converted to DECIMAL) JDBC Type:
		 * NUMERIC
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "NUMERIC");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.NUMERIC));
		aRow.addData(PRECISION, String.valueOf(decimalPrecision));
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M[,D])] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "NUMERIC");
		aRow.addData(MIN_SCALE, "-308");
		aRow.addData(MAX_SCALE, "308");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: DECIMAL JDBC Type: DECIMAL
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "DECIMAL");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.DECIMAL));
		aRow.addData(PRECISION, String.valueOf(decimalPrecision));
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M[,D])] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "DECIMAL");
		aRow.addData(MIN_SCALE, "-308");
		aRow.addData(MAX_SCALE, "308");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: INTEGER JDBC Type: INTEGER
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "INTEGER");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "10");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "INTEGER");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "INTEGER UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "10");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "INTEGER UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: INT JDBC Type: INTEGER
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "INT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "10");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "INT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "INT UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "10");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "INT UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: MEDIUMINT JDBC Type: INTEGER
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "MEDIUMINT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "7");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "MEDIUMINT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "MEDIUMINT UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.INTEGER));
		aRow.addData(PRECISION, "8");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "MEDIUMINT UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: SMALLINT JDBC Type: SMALLINT
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "SMALLINT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.SMALLINT));
		aRow.addData(PRECISION, "5");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [UNSIGNED] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "SMALLINT");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "SMALLINT UNSIGNED");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.SMALLINT));
		aRow.addData(PRECISION, "5");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "true");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "SMALLINT UNSIGNED");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: FLOAT JDBC Type: REAL (this is the SINGLE PERCISION
		 * floating point type)
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "FLOAT");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.REAL));
		aRow.addData(PRECISION, "10");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M,D)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "FLOAT");
		aRow.addData(MIN_SCALE, "-38");
		aRow.addData(MAX_SCALE, "38");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: DOUBLE JDBC Type: DOUBLE
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "DOUBLE");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.DOUBLE));
		aRow.addData(PRECISION, "17");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M,D)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "DOUBLE");
		aRow.addData(MIN_SCALE, "-308");
		aRow.addData(MAX_SCALE, "308");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: DOUBLE PRECISION JDBC Type: DOUBLE
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "DOUBLE PRECISION");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.DOUBLE));
		aRow.addData(PRECISION, "17");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M,D)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "DOUBLE PRECISION");
		aRow.addData(MIN_SCALE, "-308");
		aRow.addData(MAX_SCALE, "308");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: REAL (does not map to Types.REAL) JDBC Type: DOUBLE
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "REAL");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.DOUBLE));
		aRow.addData(PRECISION, "17");
		aRow.addData(PREFIX, "");
		aRow.addData(SUFFIX, "");
		aRow.addData(CREATE, "[(M,D)] [ZEROFILL]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "true");
		aRow.addData(L_TYPE_NAME, "REAL");
		aRow.addData(MIN_SCALE, "-308");
		aRow.addData(MAX_SCALE, "308");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: VARCHAR JDBC Type: VARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "VARCHAR");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.VARCHAR));
		aRow.addData(PRECISION, "255");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "(M)");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "VARCHAR");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: ENUM JDBC Type: VARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "ENUM");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.VARCHAR));
		aRow.addData(PRECISION, "65535");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "ENUM");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: SET JDBC Type: VARCHAR
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "SET");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.VARCHAR));
		aRow.addData(PRECISION, "64");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "SET");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: DATE JDBC Type: DATE
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "DATE");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.DATE));
		aRow.addData(PRECISION, "0");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "DATE");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TIME JDBC Type: TIME
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TIME");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.TIME));
		aRow.addData(PRECISION, "0");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "TIME");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: DATETIME JDBC Type: TIMESTAMP
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "DATETIME");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.TIMESTAMP));
		aRow.addData(PRECISION, "0");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "DATETIME");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		/*
		 * MySQL Type: TIMESTAMP JDBC Type: TIMESTAMP
		 */
		aRow = Util.getCaseSafeHandler(Util.CASE_MIXED);
		aRow.addData(NAME, "TIMESTAMP");
		aRow.addData(TYPE, Integer.toString(java.sql.Types.TIMESTAMP));
		aRow.addData(PRECISION, "0");
		aRow.addData(PREFIX, "'");
		aRow.addData(SUFFIX, "'");
		aRow.addData(CREATE, "[(M)]");
		aRow.addData(NULLABLE, Integer.toString(java.sql.DatabaseMetaData.typeNullable));
		aRow.addData(SENSITIVE, "false");
		aRow.addData(SEARCHABLE, Integer.toString(java.sql.DatabaseMetaData.typeSearchable));
		aRow.addData(ATTRIBUTE, "false");
		aRow.addData(PREC_SCALE, "false");
		aRow.addData(INCREMENT, "false");
		aRow.addData(L_TYPE_NAME, "TIMESTAMP");
		aRow.addData(MIN_SCALE, "0");
		aRow.addData(MAX_SCALE, "0");
		aRow.addData(SQL_DATATYPE, "0");
		aRow.addData(SQL_DT_SUB, "0");
		aRow.addData(RADIX, "10");
		res.addRow(aRow);


		return res;
	}

	public String getURL() throws SQLException {
		return myConnection.getUrl();
	}

	public String getUserName() throws SQLException {
		return myConnection.getUser();
	}

	public boolean insertsAreDetected(int type) throws SQLException {
		return false;
	}

	public boolean isCatalogAtStart() throws SQLException {
		return true;
	}

	public boolean isReadOnly() throws SQLException {
		return false;
	}

	public boolean locatorsUpdateCopy() throws SQLException {
		return false;
	}

	public boolean nullPlusNonNullIsNull() throws SQLException {
		return true;
	}

	public boolean nullsAreSortedAtEnd() throws SQLException {
		return false;
	}

	public boolean nullsAreSortedAtStart() throws SQLException {
		return false;
	}

	public boolean nullsAreSortedHigh() throws SQLException {
		return false;
	}

	public boolean nullsAreSortedLow() throws SQLException {
		return !nullsAreSortedHigh();
	}

	public boolean othersDeletesAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean othersInsertsAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean ownDeletesAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean ownInsertsAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		return false;
	}

	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return false;
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return false;
	}

	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return !storesLowerCaseIdentifiers();
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return !storesLowerCaseQuotedIdentifiers();
	}

	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
	 */
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return true;
	}

	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return true;
	}

	public boolean supportsANSI92FullSQL() throws SQLException {
		return false;
	}

	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return false;
	}

	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		return true;
	}

	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		return true;
	}

	public boolean supportsBatchUpdates() throws SQLException {
		return true;
	}

	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return myConnection.versionMeetsMinimum(3, 22, 0);
	}

	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return myConnection.versionMeetsMinimum(3, 22, 0);
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return myConnection.versionMeetsMinimum(3, 22, 0);
	}

	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return myConnection.versionMeetsMinimum(3, 22, 0);
	}

	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return myConnection.versionMeetsMinimum(3, 22, 0);
	}

	public boolean supportsColumnAliasing() throws SQLException {
		return true;
	}

	public boolean supportsConvert() throws SQLException {
		return false;
	}

	/**
	 *
	 * @see java.sql.DatabaseMetaData#supportsConvert(int, int)
	 */
	public boolean supportsConvert(int fromType, int toType) throws SQLException {
		switch (fromType) {
		/*
		 * The char/binary types can be converted to pretty much anything.
		 */
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.BINARY:
		case java.sql.Types.VARBINARY:
		case java.sql.Types.LONGVARBINARY:

			switch (toType) {
			case java.sql.Types.DECIMAL:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.REAL:
			case java.sql.Types.TINYINT:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.BIGINT:
			case java.sql.Types.FLOAT:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
			case java.sql.Types.OTHER:
			case java.sql.Types.DATE:
			case java.sql.Types.TIME:
			case java.sql.Types.TIMESTAMP:
				return true;

			default:
				return false;
			}

		/*
		 * We don't handle the BIT type yet.
		 */
		case java.sql.Types.BIT:
			return false;

		/*
		 * The numeric types. Basically they can convert among themselves, and
		 * with char/binary types.
		 */
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.BIGINT:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:

			switch (toType) {
			case java.sql.Types.DECIMAL:
			case java.sql.Types.NUMERIC:
			case java.sql.Types.REAL:
			case java.sql.Types.TINYINT:
			case java.sql.Types.SMALLINT:
			case java.sql.Types.INTEGER:
			case java.sql.Types.BIGINT:
			case java.sql.Types.FLOAT:
			case java.sql.Types.DOUBLE:
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
				return true;

			default:
				return false;
			}

		/* MySQL doesn't support a NULL type. */
		case java.sql.Types.NULL:
			return false;

		/*
		 * With this driver, this will always be a serialized object, so the
		 * char/binary types will work.
		 */
		case java.sql.Types.OTHER:

			switch (toType) {
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
				return true;

			default:
				return false;
			}

		/* Dates can be converted to char/binary types. */
		case java.sql.Types.DATE:

			switch (toType) {
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
				return true;

			default:
				return false;
			}

		/* Time can be converted to char/binary types */
		case java.sql.Types.TIME:

			switch (toType) {
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
				return true;

			default:
				return false;
			}

		/*
		 * Timestamp can be converted to char/binary types and date/time types
		 * (with loss of precision).
		 */
		case java.sql.Types.TIMESTAMP:

			switch (toType) {
			case java.sql.Types.CHAR:
			case java.sql.Types.VARCHAR:
			case java.sql.Types.LONGVARCHAR:
			case java.sql.Types.BINARY:
			case java.sql.Types.VARBINARY:
			case java.sql.Types.LONGVARBINARY:
			case java.sql.Types.TIME:
			case java.sql.Types.DATE:
				return true;

			default:
				return false;
			}

		/* We shouldn't get here! */
		default:
			return false; // not sure
		}
	}

	public boolean supportsCoreSQLGrammar() throws SQLException {
		return true;
	}

	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return false;
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
		return false;
	}

	public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
		return false;
	}

	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return true;
	}

	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return true;
	}

	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return false;
	}

	public boolean supportsFullOuterJoins() throws SQLException {
		return false;
	}

	public boolean supportsGetGeneratedKeys() throws SQLException {
		return true;
	}

	public boolean supportsGroupBy() throws SQLException {
		return true;
	}

	public boolean supportsGroupByBeyondSelect() throws SQLException {
		return true;
	}

	public boolean supportsGroupByUnrelated() throws SQLException {
		return true;
	}

	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		// XXX: Off-hand I dont think IntegrityEnhancementFacility will be implemented.
		// Need to find out more about what it does and
		// what MySQL engines it applies to at some point.
		// Could well be an InnoDB thing.
		return false;
	}

	public boolean supportsLikeEscapeClause() throws SQLException {
		return true;
	}

	public boolean supportsLimitedOuterJoins() throws SQLException {
		return true;
	}

	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return true;
	}

	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return !storesLowerCaseIdentifiers();
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return !storesLowerCaseQuotedIdentifiers();
	}

	public boolean supportsMultipleOpenResults() throws SQLException {
		return true;
	}

	public boolean supportsMultipleResultSets() throws SQLException {
		return false;
	}

	public boolean supportsMultipleTransactions() throws SQLException {
		return true;
	}

	public boolean supportsNamedParameters() throws SQLException {
		return false;
	}

	public boolean supportsNonNullableColumns() throws SQLException {
		return true;
	}

	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		return false;
	}

	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		return false;
	}

	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		return false;
	}

	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		return false;
	}

	public boolean supportsOrderByUnrelated() throws SQLException {
		return false;
	}

	public boolean supportsOuterJoins() throws SQLException {
		return true;
	}

	public boolean supportsPositionedDelete() throws SQLException {
		return false;
	}

	public boolean supportsPositionedUpdate() throws SQLException {
		return false;
	}

	/**
	 * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
	 */
	public boolean supportsResultSetConcurrency(int type, int concurrency)
	throws SQLException {
		switch (type) {
		case ResultSet.TYPE_SCROLL_INSENSITIVE:
			if ((concurrency == ResultSet.CONCUR_READ_ONLY)
					|| (concurrency == ResultSet.CONCUR_UPDATABLE)) {
				return true;
			} else {
				throw new SQLException(
						"Illegal arguments to supportsResultSetConcurrency()",
						"S1009");
			}
		case ResultSet.TYPE_FORWARD_ONLY:
			if ((concurrency == ResultSet.CONCUR_READ_ONLY)
					|| (concurrency == ResultSet.CONCUR_UPDATABLE)) {
				return true;
			} else {
				throw new SQLException(
						"Illegal arguments to supportsResultSetConcurrency()",
						"S1009");
			}
		case ResultSet.TYPE_SCROLL_SENSITIVE:
			return false;
		default:
			throw new SQLException(
					"Illegal arguments to supportsResultSetConcurrency()",
					"S1009");
		}

	}

	public boolean supportsResultSetHoldability(int holdability) throws SQLException {
		return (holdability == ResultSet.HOLD_CURSORS_OVER_COMMIT);
	}

	public boolean supportsResultSetType(int type) throws SQLException {
		return (type == ResultSet.TYPE_SCROLL_INSENSITIVE);
	}

	public boolean supportsSavepoints() throws SQLException {
		// XXX: I think this should be based on each tables engine type unless Im mistaken,
		// which poses a few problems in working this out accurately.
		//
		// We can limit this to the database that was logged into originally.
		// Unless the database is entirely InnoDB tables (or other transactional engine),
		// we should return false (I think ???).
		//
		// IMPORTANT NOTE:
		// MySQL-Connector/J seem to set this according the the MySQL server version.
		// Oddly enough they check for two version numbers >= 4.0.14 & >= 4.1.1
		// Not sure why they do that. I would assume the lower of the numbers would be enough.
		// We may as well do the same although technically the result
		// isnt necessarily always correct unless all table engines fully support transactions.
		// MyISAM only provides partial transaction support,
		// although this can be increased slightly through using stored procedures
		// with custom transaction rollback and commit handlers.
		//
		// Still, I dont think MyISAM supports savePoints but cant entirely verify this as yet.
		//
		// EG:
		// MyISAM should be false,
		// InnoDB should be true,
		// etc.
		// For now, false is the safest bet as MyISAM is the
		// most common of the MySQL engines.
		//return false;
		return myConnection.versionMeetsMinimum(4, 0, 14);
	}

	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return false;
	}

	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return false;
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return false;
	}

	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return false;
	}

	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return false;
	}

	public boolean supportsSelectForUpdate() throws SQLException {
		// XXX: If possible and if necessary???.
		// Returning true from this method only applies to InnoDB
		// from what I can tell. MyISAM should return false.
		// Not sure about the other MySQL storage engines yet.
		//return false;
		return myConnection.versionMeetsMinimum(4, 0, 0);
	}

	public boolean supportsStatementPooling() throws SQLException {
		// XXX: Pooling could be potentially useful
		// and should be invesitgated further at some point.
		// Due to the unique construction of this Driver
		// it may be achievable with minimal change.
		//
		// Maybee pooling isnt so applicable
		// to this Driver so a basic feasability study should
		// be done before any actual implementations are considered.
		return false;
	}

	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return true;
	}

	public boolean supportsStoredProcedures() throws SQLException {
		return true;
	}

	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return false;
	}

	public boolean supportsSubqueriesInExists() throws SQLException {
		return false;
	}

	public boolean supportsSubqueriesInIns() throws SQLException {
		return false;
	}

	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		return false;
	}

	public boolean supportsTableCorrelationNames() throws SQLException {
		return true;
	}

	public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
		// TODO implement me!
		return false;
	}

	public boolean supportsTransactions() throws SQLException {
		return true;
	}

	public boolean supportsUnion() throws SQLException {
		return false;
	}

	public boolean supportsUnionAll() throws SQLException {
		return false;
	}

	public boolean updatesAreDetected(int type) throws SQLException {
		// TODO: make sure the return value is correct for this Driver.
		return true;
	}

	public boolean usesLocalFilePerTable() throws SQLException {
		return false;
	}

	public boolean usesLocalFiles() throws SQLException {
		return false;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO do something with the wrappers.
		// this applies to all clasees that have wrapper methods.
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO do something with the wrappers.
		// this applies to all clasees that have wrapper methods.
		return null;
	}

}
