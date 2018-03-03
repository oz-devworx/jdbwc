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
 * @author Tim Gall (Oz-DevWorX)
 * @version 1.0.0.0
 */
public class SQLResultsParser {

	private transient SQLField[] myFieldSet = new SQLField[0];

	private transient DataHandler myColumns = null;
	private transient DataHandler myAliases = null;
	private transient DataHandler myTables = null;
	private transient DataHandler myResultsOrder = null;

	/* Regex Strings suitable for using in Regex Patterns */
	private static final String MY_WS = "\\s+";
	//private static final String MY_NON_WS = "\\S+";

	private static final String MY_SELECT = "SELECT";
	private static final String MY_FROM   = "FROM";

	private static final String MY_ON    = "ON";
	private static final String MY_USING = "USING";

	private static final String MY_AS = "AS";
	private static final String MY_DEL_KEYWORD = "DELIMITER";
	private static final String MY_SEPERATOR = ",";
	private static final String MY_STRING_CLOSERS = "\\\"|\"|'|`";
	private static final String MY_DISTINCT = "DISTINCT";
	private static final String MY_ROUTINES =
		MY_DEL_KEYWORD+MY_WS +
		"|CREATE"+MY_WS+"FUNCTION"+MY_WS +
		"|CREATE"+MY_WS+"PROCEDURE"+MY_WS +
		"|DROP"+MY_WS+"FUNCTION"+MY_WS +
		"|DROP"+MY_WS+"PROCEDURE"+MY_WS;


	private static final String[] MY_JOINS  = {
		MY_WS+"LEFT"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"RIGHT"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"STRAIGHT_JOIN"+MY_WS,
		MY_WS+"INNER"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"CROSS"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"JOIN"+MY_WS};

	private static final String[] MY_FROM_TRIM  = {
		MY_WS+"UNION"+MY_WS,
		MY_WS+"WHERE"+MY_WS,
		MY_WS+"GROUP"+MY_WS+"BY"+MY_WS,
		MY_WS+"ORDER"+MY_WS+"BY"+MY_WS,
		MY_WS+"LIMIT"+MY_WS,
		";"};


	/* pre-compiled Regex Patterns */
	private static final Pattern mySelect = Pattern.compile(MY_SELECT+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myFrom = Pattern.compile(MY_WS+MY_FROM+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myFieldSeperator = Pattern.compile(MY_SEPERATOR, Pattern.CASE_INSENSITIVE);
	private static final Pattern myAlias = Pattern.compile(MY_WS+MY_AS+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myJoinConditions = Pattern.compile(MY_WS+"&&"+MY_ON+"|"+MY_USING, Pattern.CASE_INSENSITIVE);
	private static final Pattern myStringWrappers = Pattern.compile(MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myWrappedString = Pattern.compile(MY_STRING_CLOSERS+"&&.&&"+MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myDistinct = Pattern.compile(MY_DISTINCT+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myRoutines = Pattern.compile(MY_ROUTINES, Pattern.CASE_INSENSITIVE);
	private static final Pattern myDelKeyword = Pattern.compile(MY_DEL_KEYWORD+MY_WS, Pattern.CASE_INSENSITIVE);


	private static Pattern[] myFromTrim = null;
	private static Pattern[] myJoins = null;
	static{
		myJoins = new Pattern[MY_JOINS.length];
		for(int i = 0; i < MY_JOINS.length; i++){
			myJoins[i] = Pattern.compile(MY_JOINS[i], Pattern.CASE_INSENSITIVE);
		}

		myFromTrim = new Pattern[MY_FROM_TRIM.length];
		for(int i = 0; i < MY_FROM_TRIM.length; i++){
			myFromTrim[i] = Pattern.compile(MY_FROM_TRIM[i], Pattern.CASE_INSENSITIVE);
		}
	}

	//----------------------------------------------------------------public methods

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
		String sql = SQLUtils.stripComments(sqlString);
		sql = SQLUtils.stripWhiteSpace(sql);
		processSQL(sql);

		switch(connection.getDbType()){
			case Util.ID_POSTGRESQL:
				PgSQLMetaGeta pgMg = new PgSQLMetaGeta(connection);
				myFieldSet = pgMg.getResultSetMetaData(myTables, myColumns, myAliases);
				break;

			case Util.ID_MYSQL:
			case Util.ID_DEFAULT:
				MySQLMetaGeta myMg = new MySQLMetaGeta(connection);
				myFieldSet = myMg.getResultSetMetaData(myTables, myColumns, myAliases);

				break;
		}

		/* reorder the fields to match the original request order */
		reorderFieldSet();
	}

	public SQLField[] getFields() throws SQLException{

		// TODO: remove this debugging stuff before releasing
//		if(Util.JDBWC_DEBUG){
//			System.err.println("~~~~~~~~~~~~~~~ START");
//			System.err.println("ResultSetMetaData.fieldSet.length = " + myFieldSet.length);
//			for(int i = 0; i < myFieldSet.length; i++){
//				System.err.println(myFieldSet[i].toString());
//			}
//			System.err.println("~~~~~~~~~~~~~~~~~ END");
//		}

		return myFieldSet;
	}
	
	/**
	 * Check an SQL String for traces of Routine keywords
	 * used for creating and dropping functions and procedures
	 * from the database. In specific this method avoids us
	 * passing the keyword DELIMITER to the database as it produces
	 * unwanted results. Instead we process it in a specific
	 * way on the serverside end.
	 *
	 * @param sql SQL String to check for traces of an SQL Routine.
	 * @return true if sql contains any Routine keywords.
	 */
	public static boolean isSqlARoutine(String sql){
		boolean isRoutine = false;
		Matcher matcher = myRoutines.matcher(sql);
		if(matcher.find()){
			isRoutine = true;
		}
		return isRoutine;
	}

	/**
	 * Check an SQL String for traces of ResultType keywords.
	 *
	 * @param sql SQL String to check for traces of a SQL ResultType.
	 * @return true if sql contains any ResultType keywords.
	 */
	public static boolean isSqlAResultType(String sql){
		boolean isResultType = false;
		Matcher matcher = mySelect.matcher(sql);
		if(matcher.find()){
			isResultType = true;
		}
		return isResultType;
	}

	/**
	 * Find instances of the <i>DELIMITER</i> keyword, PHP doesnt support it anymore.<br />
	 * Contrary to some popular beliefs, mysql and mysqli extensions for php
	 * will both throw errors at the DELIMITER keyword.<br />
	 * Fortunately though, PHP doesnt require the delimiter to be changed.<br />
	 * Good'ol <b>;</b> is fine, even in complex stored routine syntax.<br />
	 * DELIMITER most likely conflicts with a php/apache keyword or variable or whatever
	 * which would explain why support has been omitted.<br />
	 * <br />
	 * HISTORICAL: I think it used to be supported in some earlier mysqli libraries,
	 * earlier than that I have no idea.
	 *
	 * @param sql query String which might contain the DELIMITER keyword.
	 * @return sql String suitable for running via PHP.
	 */
	public static String replaceCustomDelimiters(String sql){
		String cleanSQL = sql;

		/* find and remove instances of the DELIMITER keyword */
		Matcher delimKey = myDelKeyword.matcher(cleanSQL);
		if(delimKey.find()){
			String[] sqlBits = myDelKeyword.split(cleanSQL);
			if(sqlBits.length > 0){

				/* try to get the replacement delimiter sequence */
				String firstBit = sqlBits[0].trim();
				if(firstBit.length() == 0){
					firstBit = sqlBits[1].trim();
				}

				String delimiterSeq = "";
				for(int i = 0; i < firstBit.length(); i++){
					char singleChar = firstBit.charAt(i);
					if(';'==singleChar || '\n'==singleChar || '\r'==singleChar || ' '==singleChar){
						if(delimiterSeq.length() > 0){
							break;
						}
					}else{
						delimiterSeq += singleChar;
					}
				}
				cleanSQL = cleanSQL.replace(delimiterSeq, ";");

				/* remove instances of the DELIMITER keyword
				 * and replace the replacement delimiter with the standard mySql one.
				 * Finally we make sure theres no empty entries. EG: removing the opening
				 * delimiter declaration would have replaced it with ;
				 * there would most likely be at least one more empty at
				 * the end when the delimiter is (should be) reset to its default */
				delimKey = myDelKeyword.matcher(cleanSQL);
				cleanSQL = delimKey.replaceAll("");

				String finalPass[] = cleanSQL.split(";");
				if(finalPass.length > 0){
					cleanSQL = "";
					for(int i = 0; i < finalPass.length; i++){
						if(!"".equals(finalPass[i].trim())){
							cleanSQL += finalPass[i] + ";";
						}
					}
				}
			}
		}
		return cleanSQL;
	}
	
	//----------------------------------------------------------------private methods

	private void processSQL(String sqlString) throws SQLException{
		DataHandler tableSet = Util.getCaseSafeHandler(Util.CASE_MIXED);
		DataHandler columnSet = Util.getCaseSafeHandler(Util.CASE_MIXED);
		String workArea = sqlString.trim();

		// Remove instances of the DISTINCT keyword.
		Matcher matcher = myDistinct.matcher(workArea);
		if(matcher.find()){
			workArea = matcher.replaceAll("");
		}

		// split string into single select statements
		String[] parts = SQLUtils.removeBlanks(mySelect.split(workArea));
		int selectsInQuery = parts.length;
		if(selectsInQuery > 0){

			for(int i = 0; i < selectsInQuery; i++){
				boolean firstSelect = false;
				String[] parts2 = SQLUtils.removeBlanks(myFrom.split(parts[i]));
				for(int j = 0; j < parts2.length; j++){

					if(!firstSelect){
						firstSelect = true;
						columnSet = findFieldsInString(parts2[j]);
					}else{
						tableSet = findTablesInString(parts2[j]);
					}

				}
				organiseSqlParts(tableSet, columnSet);
			}
		}
	}

	private DataHandler findTablesInString(String inputStr){
		DataHandler tableSet = Util.getCaseSafeHandler(Util.CASE_MIXED);

		outer:
			for(int i = 0; i < myFromTrim.length; i++){
				Matcher matcher = myFromTrim[i].matcher(inputStr);
				if(matcher.find()){
					String[] thisPartSplit = SQLUtils.removeBlanks(myFromTrim[i].split(inputStr));
					for (String element : thisPartSplit) {
						tableSet = seperateTableEntries(element);
						break outer;
					}
				}
			}

		return tableSet;
	}

	private DataHandler findFieldsInString(String inputStr){
		return seperateFieldEntries(inputStr);
	}

	private void organiseSqlParts(DataHandler tableSet, DataHandler columnSet) throws SQLException{
		myTables = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myColumns = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myAliases = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myResultsOrder = Util.getCaseSafeHandler(Util.CASE_MIXED);

		for(int tIdx = 0; tIdx < tableSet.length(); tIdx++){
			String tableName = tableSet.getKey(tIdx).trim();
			String tableAlias = tableSet.getString(tIdx).trim();

			myTables.addData(tableName, tableAlias);
			columnSet = processTableCols(columnSet, tableName, tableAlias, (tIdx==0));
		}
	}

	private DataHandler processTableCols(DataHandler columnSet, String tableName, String tableAlias, boolean isFirst){

		if(isFirst){
			for(int cIdx = 0; cIdx < columnSet.length(); cIdx++){
				String colName = columnSet.getKey(cIdx);
				String colsTableAlias = columnSet.getString(cIdx);

				String[] columnNameNAlias = SQLUtils.removeBlanks(myAlias.split(colName));
				if(columnNameNAlias.length > 1){
					colName = columnNameNAlias[1].trim();
				}

				myResultsOrder.addData(colName, colsTableAlias);

//				System.err.println("ADDED: " + colName + ", " + colsTableAlias);
			}
			isFirst = false;
		}

		for(int cIdx = 0; cIdx < columnSet.length(); cIdx++){

			synchronized (columnSet) {
				String colName = columnSet.getKey(cIdx);
				String colAlias = colName;
				String colsTableAlias = columnSet.getString(cIdx);
				String[] columnNameNAlias = SQLUtils.removeBlanks(myAlias.split(colName));
				if (columnNameNAlias.length > 1) {
					colName = columnNameNAlias[0].trim();
					colAlias = columnNameNAlias[1].trim();
				}
				//				System.err.println("columnSet.getString(cIdx) = " + columnSet.getString(cIdx));
				/* only add columns that use this tables alias (if there is one) */
				if (colsTableAlias.equals(tableAlias)) {
					myColumns.addData(tableName, colName);
					myAliases.addData(colName, colAlias);

					columnSet.removeByIndex(cIdx);
					columnSet = processTableCols(columnSet, tableName, tableAlias, false);
				}
			}

		}
		return columnSet;
	}

	private DataHandler seperateTableEntries(String entries){
		/* find joins and make the table names visible to the rest of this method */
		String workingArea = entries;
		for(int i = 0; i < myJoins.length; i++){
			Matcher matcher = myJoins[i].matcher(workingArea);
			if(matcher.find()){
				workingArea = matcher.replaceAll(MY_SEPERATOR);
			}
		}

		DataHandler results = Util.getCaseSafeHandler(Util.CASE_MIXED);
		String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(workingArea));

		for(int mf = 0; mf < myFields.length; mf++){

			workingArea = myFields[mf].trim();
			String tableName = workingArea;
			String tableAlias = "";

			/* if any joins were removed, the join conditions would still be in place
			 * so we check for the existence of them and remove where necessary.
			 *
			 * This is a bit of a blind punt at the moment.
			 */
			Matcher joinConditions = myJoinConditions.matcher(workingArea);
			if(joinConditions.find()){
				tableName = workingArea.substring(0, joinConditions.start());
//				System.err.println("OK: The Joins conditional part was found and removed!");
			}

			DataHandler nameNAlias = getTableName(tableName);
			tableName = nameNAlias.getKey(0);
			tableAlias = nameNAlias.getString(0);

			results.addData(tableName, tableAlias);
		}
		return results;
	}

	/**
	 * TODO: This function needs some work.
	 *
	 * @param entries
	 * @return entries seperated and stored in a DataHandler.
	 */
	private DataHandler seperateFieldEntries(String entries){

		String fieldName = "";
		String tableAlias = "";
		String workArea = entries;

		DataHandler results = Util.getCaseSafeHandler(Util.CASE_MIXED);
		String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(SQLUtils.stripWhiteSpace(workArea)));
		String humptyDumpty = "";

		int openBracketCount = 0;
		boolean insideFunction = false;
		myResultsOrder = Util.getCaseSafeHandler(Util.CASE_MIXED);

		for(int mf = 0; mf < myFields.length; mf++){
			fieldName = myFields[mf];

			/* Check for broken functions.
			 * The function will be broken if it contained a comma
			 * so we need to piece it togeather again like Humpty-Dumpty :-D
			 *
			 * The tricky bit is we dont know what type of function it is
			 * or if the function has nested functions so we need to do a
			 * bit of extra magic to get around this
			 * as gracefully as possible using some intuitive guess work.
			 * EG: couting opening and closing baces.
			 */
			if(insideFunction || fieldName.contains("(")){
				openBracketCount++;
				insideFunction = true;
				if(fieldName.contains(")") && openBracketCount==1){
					/* function is a single parameter type so we salvage humptyDumpty from the contents */
					humptyDumpty = fieldName.substring(fieldName.indexOf('(')+1, fieldName.indexOf(')'));
					humptyDumpty += fieldName.substring(fieldName.indexOf(')')+1);
					openBracketCount--;
				}else{
					/* function is a multi parameter type so we put humptyDumpty's pieces back togeather */
					humptyDumpty += fieldName + MY_SEPERATOR;
				}

			}else if(insideFunction && fieldName.contains(")")){
				openBracketCount--;
				humptyDumpty += fieldName;
				if(openBracketCount==0){
					/* mmm... To arrive here it means the humptyDumpty function
					 * has been completely rebuilt.
					 * Now we need to work out which bit contains the fieldName.
					 * If we cant find a definitive fieldName or wildcard
					 *
					 * TODO: we will eventually use the name UnknownColumnInsideFunction
					 * which will be handled gracefully by the Field class
					 * in preference to throwing an exception.
					 * The first parameter that is not numeric will be used.
					 * If its later found to not actually be a column name
					 * it will revert to the UnknownColumnInsideFunction name.
					 *
					 *
					 * That will allow for a touch more usability
					 * until this class matures and gets smarter.
					 */

					// discard any wrapping functions
					while(humptyDumpty.contains("(") && humptyDumpty.contains(")")){
						humptyDumpty = humptyDumpty.substring(humptyDumpty.indexOf('(')+1, humptyDumpty.lastIndexOf(')')).trim();
					}
					String humptiesRealName = humptyDumpty;
					String[] humptiesInsides = myFieldSeperator.split(humptiesRealName);
					for(int hIdx = 0; hIdx < humptiesInsides.length; hIdx++){
						try {
							Integer.parseInt(humptiesInsides[hIdx]);
						} catch (NumberFormatException e) {
							try {
								Double.parseDouble(humptiesInsides[hIdx]);
							} catch (NumberFormatException e1) {
								humptiesRealName = humptiesInsides[hIdx];
								break;
							}
						}
					}
					humptyDumpty = humptiesRealName;
				}



			}else{
				humptyDumpty = fieldName;
			}

			if(insideFunction && openBracketCount==0){
				insideFunction = false;
			}

			if(!insideFunction){
				DataHandler fieldNameNAlias = getFieldName(humptyDumpty);
				fieldName = fieldNameNAlias.getKey(0).trim();
				tableAlias = fieldNameNAlias.getString(0).trim();

				results.addData(fieldName, tableAlias);
			}
		}
		return results;
	}

	/**
	 * During processing the field sets often get out of order.<br />
	 * This method reorders the set to its original request order.
	 */
	private void reorderFieldSet(){
		int limit = myFieldSet.length;
		SQLField[] reorderedSet = new SQLField[limit];

		if(limit==myResultsOrder.length()){
			String key1;
			synchronized(myFieldSet){
				for(int i = 0; i < limit; i++){
					key1 = myResultsOrder.getKey(i).trim();
					for(int w = 0; w < limit; w++){
						boolean matchFound = key1.equals(myFieldSet[w].getColumnAlias());
						if(matchFound){
							reorderedSet[i] = myFieldSet[w];
//							System.err.println("-OK- item " + w + " repositioned at " + i);
						}
					}
				}
				myFieldSet = reorderedSet;
			}
		}
	}

	private DataHandler getTableName(String name){
		DataHandler cleanName = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String workArea = name.trim();
		String tableName = workArea;
		String tableAlias = "";

		Matcher matcher = myWrappedString.matcher(workArea);
		if(matcher.find()){
			String[] nameBits = myStringWrappers.split(workArea);
			workArea = nameBits[0];
			// check for a table alias and append if exists
			if(nameBits.length>1){
				workArea += nameBits[1];
			}
		}

		if(workArea.indexOf(' ') > -1){
			tableName = workArea.substring(0, workArea.indexOf(' ')).trim();
			tableAlias = workArea.substring(workArea.indexOf(' ')+1).trim();
		}

		cleanName.addData(tableName, tableAlias);

		return cleanName;
	}

	private DataHandler getFieldName(String name){
		DataHandler cleanName = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String workArea = name.trim();
		String fieldName;
		String tableAlias = "";

		Matcher matcher = myWrappedString.matcher(workArea);
		if(matcher.find()){
			String[] nameBits = myStringWrappers.split(workArea);
			workArea = nameBits[0];
			// check for a table alias and append if exists
			if(nameBits.length>1){
				workArea += nameBits[1];
			}
		}

		if(workArea.indexOf('.') > -1){
			fieldName = workArea.substring(workArea.indexOf('.')+1);
			tableAlias = workArea.substring(0, workArea.indexOf('.'));
		}else{
			fieldName = workArea;
		}

		cleanName.addData(fieldName, tableAlias);

		return cleanName;
	}

}
