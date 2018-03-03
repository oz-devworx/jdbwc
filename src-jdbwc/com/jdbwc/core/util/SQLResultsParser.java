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

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jdbwc.core.WCConnection;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 * This class is designed to parse an SQL <b>SELECT</b> String into table and field variables suitable for building ResultSetMetaData from.<br />
 * At the moment its a bit limited but should be ok for most traditional sql.<br />
 * Some SQL99 is also translated correctly.<br />
 * If we cant produce accurate metadata here we usually
 * throw an exception to ResultSetMetaData.<br />
 * <br />
 * This class can handle a variety of FUNCTIONS including nested functions and table JOINS.<br />
 * Nested functions will result in the first valid String parameter of
 * a multi-parameter function being used as the actual column name.<br />
 * The values alias (if any) will remain unchanged.
 *
 * @author Tim Gall
 * @version 2008-06
 * @version 2010-04-26
 */
public class SQLResultsParser {

	private transient SQLField[] fieldSet = new SQLField[0];

//	private transient final DataHandler table2Alias = null;
	private transient DataHandler table2Column = null;
//	private transient DataHandler column2Alias = null;

//	private transient final DataHandler myResultsOrder = null;

	/* Regex Strings suitable for using in Regex Patterns */
	private static final String MY_WS = "\\s+";
	//private static final String MY_NON_WS = "\\S+";

	private static final String MY_SELECT = "SELECT";
	private static final String MY_FROM   = "FROM";

//	private static final String MY_ON    = "ON";
//	private static final String MY_USING = "USING";

	private static final String MY_AS = "AS";

	private static final String MY_SEPERATOR = ",";
	private static final String MY_STRING_CLOSERS = "\\\"|\"|'|`";
	private static final String MY_DISTINCT = "DISTINCT";



//	private static final String[] MY_JOINS  = {
//		MY_WS+"LEFT"+MY_WS+"JOIN"+MY_WS,
//		MY_WS+"RIGHT"+MY_WS+"JOIN"+MY_WS,
//		MY_WS+"STRAIGHT_JOIN"+MY_WS,
//		MY_WS+"INNER"+MY_WS+"JOIN"+MY_WS,
//		MY_WS+"CROSS"+MY_WS+"JOIN"+MY_WS,
//		MY_WS+"JOIN"+MY_WS};

//	private static final String[] MY_FROM_TRIM  = {
//		MY_WS+"UNION"+MY_WS,//FIXME: not sure about unions being here
//		MY_WS+"WHERE"+MY_WS,
//		MY_WS+"GROUP"+MY_WS+"BY"+MY_WS,
//		MY_WS+"ORDER"+MY_WS+"BY"+MY_WS,
//		MY_WS+"LIMIT"+MY_WS,
//		";"};


	/* pre-compiled Regex Patterns */
	private static final Pattern mySelect = Pattern.compile(MY_SELECT+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myFrom = Pattern.compile(MY_WS+MY_FROM+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myFieldSeperator = Pattern.compile(MY_SEPERATOR, Pattern.CASE_INSENSITIVE);
	private static final Pattern myAlias = Pattern.compile(MY_WS+MY_AS+MY_WS, Pattern.CASE_INSENSITIVE);
//	private static final Pattern myJoinConditions = Pattern.compile(MY_WS+"&&"+MY_ON+"|"+MY_USING, Pattern.CASE_INSENSITIVE);
	private static final Pattern myStringWrappers = Pattern.compile(MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
//	private static final Pattern myWrappedString = Pattern.compile(MY_STRING_CLOSERS+"&&.&&"+MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myDistinct = Pattern.compile(MY_DISTINCT+MY_WS, Pattern.CASE_INSENSITIVE);



//	private static Pattern[] myFromTrim = null;
//	private static Pattern[] myJoins = null;
//	static{
//		myJoins = new Pattern[MY_JOINS.length];
//		for(int i = 0; i < MY_JOINS.length; i++){
//			myJoins[i] = Pattern.compile(MY_JOINS[i], Pattern.CASE_INSENSITIVE);
//		}
//
//		myFromTrim = new Pattern[MY_FROM_TRIM.length];
//		for(int i = 0; i < MY_FROM_TRIM.length; i++){
//			myFromTrim[i] = Pattern.compile(MY_FROM_TRIM[i], Pattern.CASE_INSENSITIVE);
//		}
//	}


	//---------------------------------------------------------------- constructors


	public SQLResultsParser(){
		//delegate for accessing static methods
	}


	/**
	 * Constructs a new SQLParser ready to return the ResultSetMetaData
	 * for the sqlString parameter.<br />
	 * <br />
	 * If the MySQL server is version 5.0.0 or greater
	 * INFORMATION_SCHEMA metaDataTables will be used to gather metaData in a single pass.<br />
	 * INFORMATION_SCHEMA metaDataTables will provide better performance.<br />
	 * <br />
	 * Otherwise, built-in MySQL query functions will be used to gather metaData in 2 passes.<br />
	 *
	 * @param connection Database Connection
	 * @param sqlString A valid SQL String
	 * @throws SQLException if the connection or sqlString
	 * are not valid for this Constructor.
	 */
	public SQLResultsParser(WCConnection connection, String sqlString) throws SQLException {

//		System.err.println("sql-1 = " + sqlString);

		String sql = SQLUtils.stripComments(sqlString);
		sql = SQLUtils.stripWhiteSpace(sql);
		processSQL(sql);

//		System.err.println("sql-2 = " + sql);

		Matcher matches = mySelect.matcher(sql);

		if(!matches.find()){
			throw new SQLException(
					"ResultSetMetaData only works with SELECT queries. SQL: " + sql
					, "S1009");
		}

		switch(connection.getDbType()){
			case Util.ID_POSTGRESQL:
				PgSQLMetaGeta pgMg = new PgSQLMetaGeta(connection);
				fieldSet = pgMg.getResultSetMetaData(sql, table2Column);
				break;

			case Util.ID_MYSQL:
			case Util.ID_DEFAULT:
				MySQLMetaGeta myMg = new MySQLMetaGeta(connection);
				fieldSet = myMg.getResultSetMetaData(sql, table2Column);
				break;
		}
	}


	//----------------------------------------------------------------public methods


	public SQLField[] getFields() throws SQLException{

		// TODO: do something with this debugging stuff before releasing
////		if(Util.JDBWC_DEBUG){
//			System.err.println("~~~~~~~~~~~~~~~ START");
//			System.err.println("ResultSetMetaData.fieldSet.length = " + fieldSet.length);
//			for(int i = 0; i < fieldSet.length; i++){
//				System.err.println(fieldSet[i].toString());
//			}
//			System.err.println("~~~~~~~~~~~~~~~~~ END");
////		}

		return fieldSet;
	}




	//----------------------------------------------------------------private methods


	private void processSQL(String sqlString) throws SQLException{
//		table2Alias = Util.getCaseSafeHandler(Util.CASE_MIXED);
		table2Column = Util.getCaseSafeHandler(Util.CASE_MIXED);
//		column2Alias = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String workArea = sqlString.trim();

		// Remove instances of the DISTINCT keyword.
		Matcher matcher = myDistinct.matcher(workArea);
		if(matcher.find()){
			workArea = matcher.replaceAll("");
		}

//		System.err.println("workArea = " + workArea);

		// FIXME: this should only detect the outer select. Inner selects are considered a field name
		// split string into single select statements.
		String[] parts = SQLUtils.removeBlanks(mySelect.split(workArea));
		int selectsInQuery = parts.length;

		for(int i = 0; i < selectsInQuery; i++){
			boolean firstSelect = false;
			String[] parts2 = SQLUtils.removeBlanks(myFrom.split(parts[i]));
			for(int j = 0; j < parts2.length; j++){

				if(!firstSelect){
					firstSelect = true;
					table2Column = seperateFieldEntries(parts2[j]);
//				}else{
//					table2Alias = findTablesInString(parts2[j]);
				}

			}
//			organiseSqlParts(tableSet, columnSet);
		}
	}

//	private DataHandler findTablesInString(String inputStr){
//		DataHandler tableSet = Util.getCaseSafeHandler(Util.CASE_MIXED);
//
//		outer:
//			for(int i = 0; i < myFromTrim.length; i++){
//				Matcher matcher = myFromTrim[i].matcher(inputStr);
//				if(matcher.find()){
//					String[] thisPartSplit = SQLUtils.removeBlanks(myFromTrim[i].split(inputStr));
//					for (String element : thisPartSplit) {
//						tableSet = seperateTableEntries(element);
//
////						System.err.println("element="+element);
//						System.err.println("tableSet="+tableSet.getKey(0) + ", " + tableSet.getString(0));
//						break outer;
//					}
//				}
//			}
//
//		return tableSet;
//	}

//	private DataHandler seperateTableEntries(String entries){
//		/* find joins and make the table names visible to the rest of this method */
//		String workingArea = entries;
//		for(int i = 0; i < myJoins.length; i++){
//			Matcher matcher = myJoins[i].matcher(workingArea);
//			if(matcher.find()){
//				workingArea = matcher.replaceAll(MY_SEPERATOR);
//			}
//		}
//
//		DataHandler results = Util.getCaseSafeHandler(Util.CASE_MIXED);
//		String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(workingArea));
//
//		for(int mf = 0; mf < myFields.length; mf++){
//
//			workingArea = myFields[mf].trim();
//			String tableName = workingArea;
//			String tableAlias = "";
//
//			/* if any joins were removed, the join conditions would still be in place
//			 * so we check for the existence of them and remove where necessary.
//			 *
//			 * This is a punt at the moment.
//			 */
//			Matcher joinConditions = myJoinConditions.matcher(workingArea);
//			if(joinConditions.find()){
//				tableName = workingArea.substring(0, joinConditions.start());
////				System.err.println("OK: The Joins conditional part was found and removed!");
//			}
//
//			String[] nameNAlias = getTableName(tableName);
//			tableName = nameNAlias[0];
//			tableAlias = nameNAlias[1];
//
//			//key=name, value=alias
//			results.addData(tableName, tableAlias);
//
//		}
//		return results;
//	}

	/**
	 *
	 *
	 * @param entries
	 * @return entries separated and stored in a DataHandler.
	 */
	private DataHandler seperateFieldEntries(String entries){

		String fieldName = "";

		DataHandler results = Util.getCaseSafeHandler(Util.CASE_MIXED);
		String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(SQLUtils.stripWhiteSpace(entries)));
		String wholeField = "";

		for(int mf = 0; mf < myFields.length; mf++){
			fieldName = myFields[mf];

			/* Check for broken functions.
			 * The function will be broken if it contained a comma
			 * so we need to piece it together again.
			 *
			 * The tricky bit is we don't know what type of function it is
			 * or if the function has nested functions so we need to do a
			 * bit of extra magic to get around this
			 * as gracefully as possible using some intuitive guess work.
			 */
			if(fieldName.contains("(")){
				wholeField = fieldName;
				rebuilt:
					for(mf += 1; mf < myFields.length; mf++){
						wholeField += "," + myFields[mf];
						if(myFields[mf].contains(")"))
								break rebuilt;
					}


			}else{
				wholeField = fieldName;
			}

//			System.err.println("wholeField="+wholeField);

			String[] nameNAlias = getFieldName(wholeField);
			//key=alias, value=name
			results.addData(nameNAlias[0], nameNAlias[1]);

		}
		return results;
	}

//	/**
//	 * 0=name, 1=alias
//	 */
//	private String[] getTableName(String name){
//
//		String[] cleanName = new String[2];
//
//		String tableName = name.trim();;
//		String tableAlias = "";
//
//		Matcher matches = myStringWrappers.matcher(tableName);
//
//		if(matches.find()){
//			tableName = matches.replaceAll("");
//		}
//
//		if(tableName.indexOf(' ') > -1){
//			//TODO: don't forget to check for . in this condition before storing tableName
//			tableAlias = tableName.substring(tableName.indexOf(' ')+1).trim();//must be first
//			tableName = tableName.substring(0, tableName.indexOf(' ')).trim();
//		}else{
//			tableAlias = tableName.substring(tableName.indexOf('.')+1).trim();//must be first
//			tableName = tableName.substring(0, tableName.indexOf('.')).trim();
//		}
//
//		cleanName[0] = tableName;
//		cleanName[1] = tableAlias;
//
//		return cleanName;
//	}

	/**
	 * 0=alias, 1=name.
	 * Alias should always be unique if there is one
	 */
	private String[] getFieldName(String name){
		String[] cleanName = new String[2];

//		System.err.println("name="+name);

		String fieldName = name.trim();
		String fieldAlias = "";

		Matcher matches = myStringWrappers.matcher(fieldName);

		if(matches.find()){
			fieldName = matches.replaceAll("");
		}

		if(fieldName.indexOf('.') > -1){
			fieldName = fieldName.substring(fieldName.indexOf('.')+1);
		}


		Matcher fieldNAlias = myAlias.matcher(fieldName);
		if(fieldNAlias.find()){
			String[] splitName = myAlias.split(fieldName);
			fieldName = splitName[0];
			fieldAlias = splitName[1];
		}else{
			fieldAlias = fieldName;
		}

		cleanName[0] = fieldAlias.trim();
		cleanName[1] = fieldName.trim();
//		System.err.println("fieldName="+fieldName+", fieldAlias="+fieldAlias);

		return cleanName;
	}

}
