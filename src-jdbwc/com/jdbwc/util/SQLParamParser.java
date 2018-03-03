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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jdbwc.core.WCConnection;
import com.ozdevworx.dtype.ObjectArray;

/**
 * This class is designed to parse a variety of SQL Strings into table and parameter variables suitable for building ParameterMetaData from.<br />
 * At the moment its a bit limited but should be ok for most traditional sql.<br />
 * Some SQL99 is also translated correctly.<br />
 * If we cant produce accurate metadata here we usually
 * throw an exception to ParameterMetaData.<br />
 * <br />
 * This class can handle table JOINS.<br />
 * Nested functions will result in the first valid String parameter of
 * a multi-parameter function being used as the actual column name.
 * This will be improved later or replaced with something more efficient.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 1.0.0.2
 */
public class SQLParamParser {

	/** Log object for this class. */
    private static final Log LOG = LogFactory.getLog("jdbwc.util.ParamParser");

	/* parameter direction */
	public static final String MY_MODE_IN = "IN";
	public static final String MY_MODE_OUT = "OUT";
	public static final String MY_MODE_INOUT = "INOUT";


	/* query type */
	private static final int MY_TYPE_SELECT = 100;
	private static final int MY_TYPE_DELETE = 200;
	private static final int MY_TYPE_INSERT = 300;
	private static final int MY_TYPE_UPDATE = 400;

	/* Regex Strings suitable for using in Regex Patterns */
	private static final String MY_WS = "\\s+";
	// private static final String MY_NON_WS = "\\S+";

	/* main types we can currently handle */
	private static final String MY_SELECT = "SELECT";
	private static final String MY_DELETE = "DELETE";
	private static final String MY_INSERT = "INSERT";
	private static final String MY_UPDATE = "UPDATE";
//	private static final String MY_SET = "SET";

	/* join conditions */
	private static final String MY_ON = "ON";
	private static final String MY_USING = "USING";

	/* general common keywords */
	private static final String MY_AS = "AS";
	private static final String MY_ISVAL = MY_WS+"LIKE"+MY_WS+"|<|>|=|<=|>=|<>";
	private static final String MY_VALUES = "VALUES";
	private static final String MY_DISTINCT = "DISTINCT|NOT";
//	private static final String MY_INTO = "INTO|"+MY_SET;

	/* common internal keywords by order of importance */
	private static final String MY_FROM = "FROM";
	private static final String MY_OR = "OR";
	private static final String MY_AND = "AND";
	private static final String MY_WHERE = "WHERE";
	private static final String MY_ORDER = "ORDER"+MY_WS+"BY";
	private static final String MY_GROUP = "GROUP"+MY_WS+"BY";
	private static final String MY_LIMIT = "LIMIT";
	private static final String MY_UNION = "UNION";
	private static final String MY_DELIM = ";";

	/** deals with the field portion of a select statement */
	public static final char OUR_PARAM_MARKER = '?';
	private static final char MY_SEPERATOR = ',';

	/** deals with the end of the tables portion of a select statement */
	private static final String MY_QUERY_END =
		MY_WS+MY_WHERE+MY_WS +
		"|"+MY_WS+MY_AND+MY_WS +
		"|"+MY_WS+MY_OR+MY_WS +
		"|"+MY_WS+MY_ORDER+MY_WS +
		"|"+MY_WS+MY_GROUP+MY_WS +
		"|"+MY_WS+MY_LIMIT+MY_WS +
		"|"+MY_DELIM +
		"|"+MY_WS+MY_UNION+MY_WS;

	private static final String MY_STRING_CLOSERS =
		"\\\"" +
		"|\"" +
		"|'" +
		"|`";

	private static final String[] MY_JOINS  = {
		MY_WS+"LEFT"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"RIGHT"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"STRAIGHT_JOIN"+MY_WS,
		MY_WS+"INNER"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"CROSS"+MY_WS+"JOIN"+MY_WS,
		MY_WS+"JOIN"+MY_WS};


	/* pre-compiled Regex Patterns */
	private static final Pattern myAlias = Pattern.compile(MY_WS+MY_AS+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myJoinConditions = Pattern.compile(MY_WS+"&&"+MY_ON+"|"+MY_USING, Pattern.CASE_INSENSITIVE);
	private static final Pattern myStringWrappers = Pattern.compile(MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myWrappedString = Pattern.compile(MY_STRING_CLOSERS+"&&.&&"+MY_STRING_CLOSERS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myDistinct = Pattern.compile(MY_DISTINCT+"&&"+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myFieldSeperator = Pattern.compile(MY_SEPERATOR+"", Pattern.CASE_INSENSITIVE);
	private static final Pattern myValSplitter = Pattern.compile(MY_ISVAL, Pattern.CASE_INSENSITIVE);
//	private static final Pattern myIntoAndSet = Pattern.compile(MY_INTO+"&&"+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myParamFinder = Pattern.compile(OUR_PARAM_MARKER+"", Pattern.LITERAL);

	private static final Pattern myQueryEnd = Pattern.compile(MY_QUERY_END, Pattern.CASE_INSENSITIVE);

	private static final Pattern mySelect = Pattern.compile(MY_SELECT+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern mySelectFrom = Pattern.compile(MY_WS+MY_FROM+MY_WS, Pattern.CASE_INSENSITIVE);

	private static final Pattern myDelete = Pattern.compile(MY_DELETE+MY_WS+"FROM"+MY_WS, Pattern.CASE_INSENSITIVE);

	private static final Pattern myInsert = Pattern.compile(MY_INSERT+MY_WS+"INTO"+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myInsertValues = Pattern.compile(MY_VALUES, Pattern.CASE_INSENSITIVE);

	private static final Pattern myUpdate = Pattern.compile(MY_UPDATE+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern myUpdateSet = Pattern.compile(MY_WS+"SET"+MY_WS, Pattern.CASE_INSENSITIVE);

	private static Pattern[] myJoins = null;
	static{
		myJoins = new Pattern[MY_JOINS.length];
		for(int i = 0; i < MY_JOINS.length; i++){
			myJoins[i] = Pattern.compile(MY_JOINS[i], Pattern.CASE_INSENSITIVE);
		}
	}



	public static int countParams(String input){
		return countParams(input, OUR_PARAM_MARKER);
	}

	public static int countParams(String input, char delimiter){
		int paramCount = 0;
		if(input!=null){
			for(int i = 0; i < input.length(); i++){
				if(input.charAt(i)==delimiter){
					paramCount++;
				}
			}
		}
		return paramCount;
	}

	public static String populateParams(ObjectArray prepStatement) throws SQLException{
		String sql = prepStatement.getString(0);
		StringBuilder rebuiltSql = new StringBuilder();
		int paramCount = countParams(sql);
		int storedParamCount = prepStatement.length()-1;

		if(storedParamCount!=paramCount){
			String message;
			if(storedParamCount>paramCount){
				int diff = storedParamCount-paramCount;
				message = (diff>1 ? diff + " to many Parameters " : "one too many Parameters ");
			}else{
				int diff = paramCount-storedParamCount;
				message = (diff>1 ? diff + " to few Parameters that should be " : "one missing Parameter that should be ");
			}

			throw new SQLException(
					"Parameter mismatch. There appears to be " + message + "set using the setXXX() methods.",
					"07001");
		}

		if(paramCount>0){
			String[] splitQuery = myParamFinder.split(sql);
			for(int i = 0; i < splitQuery.length; i++){
				rebuiltSql.append(splitQuery[i]).append(prepStatement.getString(i+1));
			}
		}

		return rebuiltSql.toString();
	}



	private transient SQLField[] myFieldSet = new SQLField[0];
	private transient ObjectArray myColumns = null;
	private transient ObjectArray myAliases = null;
	private transient ObjectArray myTables = null;

	private transient ObjectArray myResultsOrder = null;
	private transient ObjectArray myParams = null;

	/** number of Parameter markers in SQL statement */
	private transient int myParamCount = 0;
	/** the type of query thats being examined */
	private transient int myQueryType = 0;

//	private transient int myDbType = 0;

	/* Somewhere to store the sql parts once thier split.
	 * Out vars will be handled differently;
	 * as they are usually syntactically different than IN params
	 * we should have minimal trouble finding them.
	 * By rights they should also be limited to SELECT statements
	 * since we dont support subqueries at the moment.
	 */
	/** we want to put the table portions here */
	private transient String myPartTables = "";
	/** we want to put the Parameters portion here */
	private transient String myPartINParams = "";


	/**
	 * Constructs a new SQLParamParser ready to return PreparedStatement ParameterMetaData
	 * for the sqlString parameter.
	 *
	 * @param connection Database Connection
	 * @param prepStatement A ObjectArray containing a valid SQL String at the first index,
	 * parameters in the remaining indexes.
	 * @throws SQLException if the connection or sqlString are not valid for this Constructor.
	 */
	public SQLParamParser(WCConnection connection, ObjectArray prepStatement) throws SQLException {
		processPreparedStatement(prepStatement.getString(0));
		buildMetaData(connection);
	}

	/**
	 * Constructs a new SQLParamParser ready to return CallableStatement ParameterMetaData
	 * for the sqlString parameter.
	 *
	 * @param connection Database Connection
	 * @param callableStatement A ObjectArray containing a valid SQL CALL String at the first index,
	 * parameters in the remaining indexes.
	 * @param isCallable boolean to signify the fieldSet should be built for a CallableStatement.
	 * @throws SQLException if the connection or sqlString are not valid for this Constructor.
	 */
	public SQLParamParser(WCConnection connection, ObjectArray callableStatement, boolean isCallable) throws SQLException {
		processCallableStatement(callableStatement.getString(0));
		buildMetaData(connection);
	}

	public SQLField[] getFields(){
		return myFieldSet;
	}

	/**
	 * Iterate through the tables in the PreparedStatement
	 * and fetch metaData from each as required for the parameter markers
	 * in the PreparedStatement.
	 *
	 * @throws SQLException If I have trouble fetching
	 * the metaData info from the database
	 */
	private void buildMetaData(WCConnection connection) throws SQLException{

		// using reflection, get the correct class for the database type thats in use
		try {
			Class<?> metaClass = Class.forName(connection.getDbPackagePath() + "SQLMetaGetaImp");

            Constructor<?> ct = metaClass.getConstructor(new Class[]{connection.getClass()});

            SQLMetaGeta metaG = (SQLMetaGeta)ct.newInstance(new Object[]{connection});
            myFieldSet = metaG.getParameterMetaData(myTables, myColumns, myParams);

		} catch (Throwable e) {
			throw new SQLException("Could not construct a SQLMetaGeta Object", e);
		}

//		for(int tIdx = 0; tIdx < myTables.length(); tIdx++){
//			getMetaData(myTables.getKey(tIdx));
//		}

		/* reorder the fields to match the original request order */
		reorderFieldSet();
	}

	private void buildParamHolder(){
		myParams = Util.getCaseSafeHandler(Util.CASE_MIXED);
		for(int i = 0; i < myParamCount; i++){
			myParams.addData(i+"", '?');
		}
	}

	/**
	 * Trim a String and remove the trailing <code>MY_SEPERATOR</code> char if found.
	 *
	 * @param input The String to attempt removal of <code>MY_SEPERATOR</code> from.
	 * @return A trimmed String with the trailing char removed if it matches <code>MY_SEPERATOR</code>.
	 */
	private String cleanString(String input){
		return cleanString(input, MY_SEPERATOR);
	}

	/**
	 * Trim a String and remove the trailing <code>delimiter</code> char if found.
	 *
	 * @param input The String to attempt removal of <code>delimiter</code> from.
	 * @param delimiter The delimiter character to look for on the end of <code>input</code>
	 * @return A trimmed String with the trailing char removed if it matches <code>delimiter</code>.
	 */
	private String cleanString(String input, char delimiter){
		String output = input.trim();
		if(output.endsWith(delimiter+"")){
			output = output.substring(0, output.length()-1).trim();
		}
		return output;
	}

	private String cleanUpSql(String sqlString){

		String sql = SQLUtils.stripComments(sqlString);

		// TODO: verify its ok to do such a thorough whitespace cleanup
		// before finalisation.
		//
		String workArea = SQLUtils.stripWhiteSpace(sql);
		//String workArea = sql.trim();

		// Remove instances of the DISTINCT keyword.
		Matcher matcher = myDistinct.matcher(workArea);
		if(matcher.find()){
			workArea = matcher.replaceAll("");
		}
		return workArea;
	}

	private ObjectArray findParamNamesInString(String entries){
		String paramName = "";
		String workArea = entries;
		ObjectArray results = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String[] myFields = SQLUtils.removeBlanks(myFieldSeperator.split(SQLUtils.stripWhiteSpace(workArea)));

		for(int mf = 0; mf < myFields.length; mf++){
			if(countParams(myFields[mf]) > 0){
				String[] paramNameNAlias = myValSplitter.split(myFields[mf]);
				paramName = paramNameNAlias[0].trim();

				ObjectArray colVals = getColumnName(paramName);
				results.addData(colVals.getKey(0), colVals.getString(0));
			}
		}
		return results;
	}

	/**
	 * Find out what type of query it is we are parsing.
	 *
	 * @param input The sql statement to inspect.
	 * @return The sql type int as defined in this classes final variables.
	 */
	private int findQueryType(String input){
		int queryType = -1;

		Matcher mSelect = mySelect.matcher(input);
		if(mSelect.find()){
			if(MY_SELECT.equalsIgnoreCase(input.substring(0, MY_SELECT.length()))){
				return MY_TYPE_SELECT;
			}
		}

		Matcher mDelete = myDelete.matcher(input);
		if(mDelete.find()){
			if(MY_DELETE.equalsIgnoreCase(input.substring(0, MY_DELETE.length()))){
				return MY_TYPE_DELETE;
			}
		}

		Matcher mInsert = myInsert.matcher(input);
		if(mInsert.find()){
			if(MY_INSERT.equalsIgnoreCase(input.substring(0, MY_INSERT.length()))){
				return MY_TYPE_INSERT;
			}
		}

		Matcher mUpdate = myUpdate.matcher(input);
		if(mUpdate.find()){
			if(MY_UPDATE.equalsIgnoreCase(input.substring(0, MY_UPDATE.length()))){
				return MY_TYPE_UPDATE;
			}
		}

		return queryType;
	}

	private ObjectArray findTablesInString(String entries){

		/* find joins and make the table names visible to the rest of this method */
		String workingArea = entries;
		for(int i = 0; i < myJoins.length; i++){
			Matcher matcher = myJoins[i].matcher(workingArea);
			if(matcher.find()){
				workingArea = matcher.replaceAll(MY_SEPERATOR+"");

//				System.err.println("OK: Found a JOIN!");
			}
		}


		ObjectArray results = Util.getCaseSafeHandler(Util.CASE_MIXED);
		Matcher matcher = myFieldSeperator.matcher(workingArea);
		String[] myFields = {};
		if(matcher.find()){
			myFields = SQLUtils.removeBlanks(myFieldSeperator.split(workingArea));
		}else{
			myFields = new String[1];
			myFields[0] = workingArea;
		}
//		System.err.println("workingArea = " + workingArea);
//		System.err.println("Found " + myFields.length + " table entries");

		for(int mf = 0; mf < myFields.length; mf++){

			workingArea = myFields[mf].trim();
			String tableName = workingArea;
			String tableAlias = "";

			/* if any joins were removed, the join conditions would still be in place
			 * so we check for the existance of them and remove where necessary.
			 *
			 * This is a bit of a blind punt at the moment.
			 */
			Matcher joinConditions = myJoinConditions.matcher(workingArea);
			if(joinConditions.find()){
				tableName = workingArea.substring(0, joinConditions.start());
//				System.err.println("OK: The Joins conditional part was found and removed!");
			}

			ObjectArray nameNAlias = getTableName(tableName);
			tableName = nameNAlias.getKey(0);
			tableAlias = nameNAlias.getString(0);

			results.addData(tableName, tableAlias);
		}
		return results;
	}

	private ObjectArray getColumnName(String name){
		ObjectArray cleanName = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String workArea = name.trim();
		String fieldName;
		String tableAlias = "";

		Matcher matcher = myWrappedString.matcher(workArea);
		if(matcher.find()){
			String[] nameBits = SQLUtils.removeBlanks(myStringWrappers.split(workArea));
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

	private ObjectArray getTableName(String name){
		ObjectArray cleanName = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String workArea = name.trim();
		String tableName = workArea;
		String tableAlias = "";

		Matcher matcher = myWrappedString.matcher(workArea);
		if(matcher.find()){
			String[] nameBits = SQLUtils.removeBlanks(myStringWrappers.split(workArea));
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

	/**
	 * Organise the data weve compiled so far into a coherent state so we can
	 * fetch some metaData for our param markers.
	 *
	 * @param tableSet ObjectArray of tables in the query.
	 * @param columnSet ObjectArray of parameter-column names in the query.
	 * @throws SQLException
	 */
	private void organiseSqlParts(ObjectArray tableSet, ObjectArray paramSet) throws SQLException{
		myTables = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myColumns = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myAliases = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myParams = Util.getCaseSafeHandler(Util.CASE_MIXED);
		myResultsOrder = Util.getCaseSafeHandler(Util.CASE_MIXED);


		int paramSetCount = paramSet.length();
		for(int pc = 0; pc < paramSetCount; pc++){
			myParams.addData(paramSet.getKey(pc), "IN");
			myResultsOrder.addData(paramSet.getKey(pc), "IN");
		}

		for(int tIdx = 0; tIdx < tableSet.length(); tIdx++){
			String tableName = tableSet.getKey(tIdx).trim();
			String tableAlias = tableSet.getString(tIdx).trim();
//			if(!tableName.equals("")){
				myTables.addData(tableName, tableAlias);
				for(int psc = 0; psc < paramSetCount; psc++){
					String paramName = paramSet.getKey(psc);
					String paramAlias = "";
					String paramTableAlias = paramSet.getString(psc);

					String[] columnNameNAlias = SQLUtils.removeBlanks(myAlias.split(paramName));
					if(columnNameNAlias.length > 1){
						paramName = columnNameNAlias[0].trim();
						paramAlias = columnNameNAlias[1].trim();
					}

					/* only add columns that use this tables alias (if there is one) */
					if(myParams.hasKey(paramName) && paramTableAlias.equals(tableAlias)){
						myColumns.addData(paramName, tableName);
						myAliases.addData(paramName, paramAlias);
					}
				}
//			}
		}
	}

	/**
	 * Split an SQL statement into parsable portions.<br />
	 * The split portions need to be assembled according to the query type
	 * so we can reasonably accurately find the Parameter types for each ? marker.
	 *
	 * @param input
	 * @throws SQLException
	 */
	private void processCommonSql(String input) throws SQLException{
		ObjectArray tableSet = Util.getCaseSafeHandler(Util.CASE_MIXED);
		ObjectArray paramSet = Util.getCaseSafeHandler(Util.CASE_MIXED);

		String partTables;
		String partColumns;
		String partINParams;

		myPartTables = "";
//		myPartColumns = "";
		myPartINParams = "";

		/* seperate queries if neccessary by looking for statement end ';' markers */
		String[] splitQuery = input.split(MY_DELIM);
		int numOfQueries = splitQuery.length;

		/* Start stage 1 of 2
		 *
		 * cycle through the queries and split them into 3 parts.
		 * Tables, columns, IN param prospects.
		 *
		 * This loop could be reviewed as I beleive we
		 * only need to allow for a single query at a time.
		 * TODO: re-read the ParameterMetaData specifications
		 * and the specs of interfaces that can return ParameterMetaData.
		 */
		for(int i = 0; i < numOfQueries; i++){
			/* these params need resetting after each statement iteration
			 * so they are best placed here to avoid using
			 * carry over values from the last iteration.
			 */
			partTables = "";
			partColumns = "";
			partINParams = "";

			int paramsInStart = 0;
			int paramsInMid = 0;
			int paramsInEnd = 0;

			boolean columnsExtracted = false;
			boolean tablesExtracted = false;


			switch(myQueryType){

			/* XXX: SELECT has:
			 * 1) columns (can be a wildcard *)
			 * 2) tables (can be a wildcard * which would be very rare and probably not worth implementing)
			 * 3) parameters
			 *
			 * Look out for INTO, SET and @ keySymbols as they
			 * should contain OUT Parameters. */
			case MY_TYPE_SELECT:
//				if(LOG.isDebugEnabled()){
//					LOG.debug("------- START\nPROCESSING Select Query;");
//				}

				// remove select keyWord and any wrapping whitespace
				String querySelect = splitQuery[i].replaceFirst(mySelect.pattern(), "").trim();

				String[] splitAtFrom = SQLUtils.removeBlanks(mySelectFrom.split(querySelect));
//				if(LOG.isDebugEnabled()){
//					LOG.debug("splitAtFrom.length = " + splitAtFrom.length);
//				}

				for(int j = 0; j < splitAtFrom.length; j++){

					// columns first
					if(!columnsExtracted){
						paramsInStart = countParams(splitAtFrom[j]);
						partColumns = splitAtFrom[j];// columns
						columnsExtracted = true;

						/* OUT parameters could be located within the columns part. */
//						if(LOG.isDebugEnabled()){
//							LOG.debug("FOUND " + paramsInStart + " paramsInStart portion.");
//	//						LOG.debug("COLUMNS: " + partColumns);
//						}

					// tables next, then possible IN params
					}else{
						String[] lastBits = myQueryEnd.split(splitAtFrom[j]);
						paramsInMid = countParams(lastBits[0]);
						partTables = lastBits[0];// tables

//						if(LOG.isDebugEnabled()){
//							LOG.debug("FOUND " + paramsInMid + " paramsInMid portion.");
//	//						LOG.debug("TABLES: " + partTables);
//						}

						/* There are potential IN Parameters after the table name/s
						 * so we need to do some extra processing for this.
						 * All IN parameters would usually be after the table declaration
						 * in a select query.
						 * NOTE: OUT parameters could also be located within the columns part.
						 */
						paramsInEnd = myParamCount - (paramsInStart + paramsInMid);
						for(int lb = 1; lb < lastBits.length; lb++){
							partINParams += lastBits[lb] + ",";
						}
						partINParams = cleanString(partINParams);

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInEnd + " paramsInEnd portion.");
	//						LOG.debug("IN PARAMETER PROSPECTS: " + partINParams);
						}
					}
				}
				myPartTables += partTables;
//				myPartColumns += partColumns;
				myPartINParams += partINParams;
				break;







			/* XXX: DELETE has:
			 * 1) tables
			 * 2) -nothing-
			 * 3) parameters
			 *
			 * No OUT Parameters. */
			case MY_TYPE_DELETE:

				if(LOG.isDebugEnabled()){
					LOG.debug("------- START\nPROCESSING Delete Query;");
				}

				// remove delete keyWord and any wrapping whitespace
				String queryDelete = splitQuery[i].replaceFirst(myDelete.pattern(), "").trim();

				String[] tablesAndINParams = SQLUtils.removeBlanks(myQueryEnd.split(queryDelete));
				if(LOG.isDebugEnabled()){
					LOG.debug("tablesAndINParams.length = " + tablesAndINParams.length);
				}


				partColumns = "*";

				if(LOG.isDebugEnabled()){
					LOG.debug("FOUND 0 paramsInStart portion.");
	//				LOG.debug("COLUMNS: " + partColumns);
				}

				paramsInMid = countParams(tablesAndINParams[0]);
				partTables = tablesAndINParams[0];

				if(LOG.isDebugEnabled()){
					LOG.debug("FOUND " + paramsInMid + " paramsInMid portion.");
	//				LOG.debug("TABLES: " + partTables);
				}

				// There are potential Parameters after the table name/s
				// so we need to do some extra processing for this.
				// All parameters would usually be after the table declaration
				// in a select query.
				//
				paramsInEnd = myParamCount - (paramsInStart + paramsInMid);
				for(int lb = 1; lb < tablesAndINParams.length; lb++){
					partINParams += tablesAndINParams[lb] + ",";
				}
				partINParams = cleanString(partINParams);

				if(LOG.isDebugEnabled()){
					LOG.debug("FOUND " + paramsInEnd + " paramsInEnd portion.");
	//				LOG.debug("IN PARAMETER PROSPECTS: " + partINParams);
				}

				myPartTables += partTables;
//				myPartColumns += partColumns;
				myPartINParams += partINParams;
				break;







			/* XXX: INSERT INTO has:
			 * 1) tables
			 * 2) columns (if any)
			 * 3) parameters
			 *
			 * No OUT Parameters.
			 * Attempting to parse Insert statements is full on.
			 * Be very carefull making changes here. */
			case MY_TYPE_INSERT:
				if(LOG.isDebugEnabled()){
					LOG.debug("------- START\nPROCESSING Insert Query;");
				}

				boolean hasColumnNames = false;

				// remove insert keyWord and any wrapping whitespace
				String queryInsert = splitQuery[i].replaceFirst(myInsert.pattern(), "").trim();

				String[] splitAtValues = SQLUtils.removeBlanks(myInsertValues.split(queryInsert));
				if(LOG.isDebugEnabled()){
					LOG.debug("splitAtValues.length = " + splitAtValues.length);
				}

				for(int j = 0; j < splitAtValues.length; j++){
					// tables are first
					if(!tablesExtracted){
						int tablesEnd = 0;
						splitAtValues[j] = splitAtValues[j].trim();

						if(splitAtValues[j].length() > 0 || splitAtValues[j].contains("(")){
							tablesEnd = splitAtValues[j].indexOf("(");
							if(tablesEnd<=0){
								tablesEnd = splitAtValues[j].length();
								partColumns = "*";

								if(LOG.isDebugEnabled()){
									LOG.debug("FOUND 0 paramsInMid portion.");
	//								LOG.debug("COLUMNS: " + partColumns);
								}
							}else{
								hasColumnNames = true;
							}
						}

						/* Throw an Exception if we cant find any tables.
						 * We cant do much without at least one table name. */
						if(tablesEnd == 0){
							throw new SQLException(
									"I couldnt find any tables in your sql query. This could be a deficiency on my behalf, but just incase you should check your SQL sytax first or try using a simpler form of SQL.",
									"WCDE");
						}

						String tablesPart = splitAtValues[j].substring(0, tablesEnd).trim();
						paramsInStart = countParams(tablesPart);
						partTables = tablesPart;
						tablesExtracted = true;

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInStart + " paramsInStart portion.");
	//						LOG.debug("TABLES: " + partTables);
						}
					}


					// fields are second (if any) and should be before the VALUES keyword.
					if(hasColumnNames && !columnsExtracted){
						int colStart = splitAtValues[j].indexOf('(')+1;
						int colEnd = splitAtValues[j].indexOf(')');
						String colsPart = splitAtValues[j].substring(colStart, colEnd).trim();
						paramsInMid = countParams(colsPart);
						partColumns = colsPart;
						columnsExtracted = true;

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInMid + " paramsInMid portion.");
	//						LOG.debug("COLUMNS: " + partColumns);
						}

					// IN Parameters should be last
					}else{
						/* if there were no columns we need to
						 * manually jump to the next item in the array
						 */
						if(!hasColumnNames){
							j = j+1;
							if(LOG.isDebugEnabled()){
								LOG.debug("FORCED array increment to: " + j);
							}
						}

						String[] lastBits = SQLUtils.removeBlanks(myQueryEnd.split(splitAtValues[j]));
						paramsInEnd = myParamCount - (paramsInStart + paramsInMid);
						for(int lb = 0; lb < lastBits.length; lb++){
							partINParams += lastBits[lb] + ",";
						}

						/* we are looking for multiple sets of insert values
						 * and attempting to allocate column names to parameter markers
						 * as well as a general cleanup of the partINParams String
						 * if it contains brackets.
						 */
						partINParams = partINParams.trim();
						if(partINParams.startsWith("(")){
							String[] valuesStr = SQLUtils.removeBlanks(partINParams.split("\\("));
							/* we are going to rebuild the string
							 * and pair up column names with param markers
							 * if there is any, so we need to clear the paramString
							 * ready for the new content.
							 */
							partINParams = "";
							int vsCount = valuesStr.length;
							int paramsNamed = 0;

							for(int vs = 0; vs < vsCount; vs++){
								valuesStr[vs] = cleanString(valuesStr[vs]);
								valuesStr[vs] = cleanString(valuesStr[vs], ')');

								String[] paramVals = SQLUtils.removeBlanks(myFieldSeperator.split(valuesStr[vs]));
								if(hasColumnNames && !"*".equals(partColumns)){
									/* we need to split the columns String by seperator
									 * so we can attempt to allocate column names to parameter markers.
									 */
									String[] colNames = SQLUtils.removeBlanks(myFieldSeperator.split(partColumns));

									for(int cn = 0; cn < colNames.length; cn++){
										if(paramsInEnd > paramsNamed){
											paramVals[paramsNamed] = paramVals[paramsNamed].trim();
											if(countParams(paramVals[paramsNamed])>0){
												partINParams += colNames[cn] + "=" + paramVals[paramsNamed] + MY_SEPERATOR;
												paramsNamed++;
											}else{
												partINParams += paramVals[paramsNamed] + MY_SEPERATOR;
												paramsNamed++;
											}
										}else{
											break;
										}
									}
								}else{
									for(int pv = 0; pv < paramVals.length; pv++){
										partINParams += paramVals[pv] + MY_SEPERATOR;
									}
								}
							}
						}

						partINParams = partINParams.trim();
						if(partINParams.endsWith(MY_SEPERATOR+"")){
							partINParams = partINParams.substring(0, partINParams.length()-1).trim();
						}

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInEnd + " paramsInEnd portion.");
	//						LOG.debug("IN PARAMETER PROSPECTS: " + partINParams);
						}
					}
				}
				myPartTables += partTables;
//				myPartColumns += partColumns;
				myPartINParams += partINParams;
				break;







			/* XXX: UPDATE has:
			 * 1) tables
			 * 2) columns => parameters
			 * 3) -nothing-
			 *
			 * No OUT Parameters. */
			case MY_TYPE_UPDATE:
				if(LOG.isDebugEnabled()){
					LOG.debug("------- START\nPROCESSING Update Query;");
				}

				// remove delete keyWord and any wrapping whitespace
				String queryUpdate = splitQuery[i].replaceFirst(myUpdate.pattern(), "").trim();

				String[] splitAtSet = SQLUtils.removeBlanks(myUpdateSet.split(queryUpdate));

				if(LOG.isDebugEnabled()){
					LOG.debug("splitAtSet.length = " + splitAtSet.length);
				}

				for(int j = 0; j < splitAtSet.length; j++){

					// tables should be first
					if(!tablesExtracted){
						partTables = splitAtSet[j].trim();
						paramsInStart = countParams(partTables);
						tablesExtracted = true;

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInStart + " paramsInStart portion.");
	//						LOG.debug("TABLES: " + partTables);
						}


					// columns and parameter markers are next
					}else{
						String[] lastBits = SQLUtils.removeBlanks(myQueryEnd.split(splitAtSet[j]));
						String colpart = lastBits[0].trim();
						paramsInMid = countParams(colpart);

						String[] cols = SQLUtils.removeBlanks(myFieldSeperator.split(colpart));
						for(int cidx = 0; cidx < cols.length; cidx++){
							String[] colNameNVal = cols[cidx].split("=");
							partColumns += colNameNVal[0].trim() + MY_SEPERATOR;
							if(countParams(colNameNVal[1])>0){
								partINParams += colNameNVal[0] + "=" + colNameNVal[1] + ",";
							}
						}
						partColumns = cleanString(partColumns);

						/* There are potential IN Parameters after the table name/s
						 * so we need to do some extra processing for this.
						 * All IN parameters would usually be after the table declaration
						 * in a select query.
						 * NOTE: OUT parameters could also be located within the columns part.
						 */
						paramsInEnd = myParamCount - (paramsInStart + paramsInMid);
						for(int lb = 1; lb < lastBits.length; lb++){
							partINParams += lastBits[lb] + ",";
						}

						if(LOG.isDebugEnabled()){
							LOG.debug("FOUND " + paramsInMid + " paramsInMid portion.");
							LOG.debug("FOUND " + paramsInEnd + " paramsInEnd portion.");
	//						LOG.debug("IN PARAMETER PROSPECTS: " + partINParams);
						}
					}
				}
				myPartTables += partTables;
//				myPartColumns += partColumns;
				myPartINParams += partINParams;
				break;
			}
		}// finshed stage 1 of 2

		if(LOG.isDebugEnabled()){
			LOG.debug("TABLES: " + myPartTables);
			LOG.debug("IN PARAMETER PROSPECTS: " + myPartINParams);
		}


		/* Start stage 2 of 2 */
		tableSet = findTablesInString(cleanString(myPartTables));
		paramSet = findParamNamesInString(cleanString(myPartINParams));




		/* Nearly there now. Before we finish here,
		 * we need to put some metaData togeather for
		 * each paramater marker in the sql query,
		 * based on our table and field sets.
		 */
		organiseSqlParts(tableSet, paramSet);


//		if(LOG.isDebugEnabled()){
//			for(int i = 0; i < myTables.length(); i++){
//				LOG.debug("TABLE: " + myTables.getKey(i) + " = " + myTables.getString(i));
//			}
//			for(int i = 0; i < myColumns.length(); i++){
//				LOG.debug("PARAM: " + myColumns.getKey(i) + " = " + myColumns.getString(i));
//			}
//
//			// finshed stage 2 of 2
//			LOG.debug("--------- END");
//		}
	}

	/**
	 * Attempt to find out what columns the parameter markers relate to
	 * and fetch some metaData about the markers (if there are markers).<br />
	 * <br />
	 * If I cant understand the sql syntax I wont be able to provide any metaData about
	 * the parameters but should still be able to process PreparedStatements successfully
	 * with my public static methods.<br />
	 * <br />
	 * If the metaData cant be calculated correctly i'll chuck an error back to ParameterMetaData
	 * emphasising the fact that the exception could be due to deficiencies in my existing logic
	 * as well as a message advising you to check your sql syntax incase it contains errors.
	 */
	private void processPreparedStatement(String sqlString) throws SQLException{

		String workArea = cleanUpSql(sqlString);

		/* setup some basic parameter marker info */
		myParamCount = countParams(workArea);
		buildParamHolder();

		/* find out what type of query we are processing */
		myQueryType = findQueryType(sqlString);

		/* make sure we can get metaData for this type of statement */
		validateQueryByType(sqlString);

		/* attempt to find out what columns the parameter markers relate to
		 * so we can fetch some metaData about the markers (if ther are markers) */
		processCommonSql(sqlString);


	}

	/**
	 * Attempt to find out what columns the parameter markers relate to
	 * and fetch some metaData about the markers (if there are markers).<br />
	 * <br />
	 * If I cant understand the sql syntax I wont be able to provide any metaData about
	 * the parameters but should still be able to process PreparedStatements successfully
	 * with my public static methods.<br />
	 * <br />
	 * If the metaData cant be calculated correctly i'll chuck an error back to ParameterMetaData
	 * emphasising the fact that the exception could be due to deficiencies in my existing logic
	 * as well as a message advising you to check your sql syntax incase it contains errors.
	 */
	private void processCallableStatement(String sqlString) throws SQLException{

		String workArea = cleanUpSql(sqlString);

		/* setup some basic parameter marker info */
		myParamCount = countParams(workArea);
		buildParamHolder();

		/* find out what type of query we are processing */
		myQueryType = findQueryType(sqlString);

		/* make sure we can get metaData for this type of statement */
		validateQueryByType(sqlString);

		/* attempt to find out what columns the parameter markers relate to
		 * so we can fetch some metaData about the markers (if ther are markers) */
		processCommonSql(sqlString);


	}

	/**
	 * When compiling the list of params, some or all param indexes often
	 * end up out of order (in relation to the original statements order);
	 * so we do a quick shuffle to put the param indexes back in the correct order.<br />
	 * <br />
	 * If the params are from an insert statement that did not specify any column names
	 * the param-indexes will all-ready  be in the correct order so the
	 * myFieldSet will remain unchanged.
	 */
	private void reorderFieldSet(){
		int limit = myFieldSet.length;
		SQLField[] reorderedSet = new SQLField[limit];
		int unnamedParams = myResultsOrder.countMatches("?");

		// this 'if' is a bit of a punt but seems to work reliably
		if(unnamedParams > 0 && unnamedParams==limit){
			reorderedSet = myFieldSet;
		}else{
			if(limit==myResultsOrder.length()){
				for(int i = 0; i < limit; i++){
					String key = myResultsOrder.getKey(i).trim();
					for(int w = 0; w < limit; w++){
						String fname = myFieldSet[w].getFieldName();
						boolean matchFound = key.equals(fname);
						if(matchFound){
//							System.err.println("-OK- item " + w + " repositioned at " + i);
							reorderedSet[i] = myFieldSet[w];
							// NOTE: we dont break out to ensure we allow for duplicate param columns
						}
					}
				}
				myFieldSet = reorderedSet;
//			}else{
//				System.err.println("ERROR# Size mismatch encountered when trying to reorder the fieldSet array!");
			}
		}
	}

	/**
	 * Do a quick validation to make sure we can handle the sql query type.<br />
	 * If we dont support it we chuck an exception at the requesting method.
	 */
	private void validateQueryByType(String sqlString) throws SQLException{
		switch(myQueryType){
		case MY_TYPE_SELECT:
		case MY_TYPE_DELETE:
		case MY_TYPE_INSERT:
		case MY_TYPE_UPDATE:
			break;
		default:
			throw new SQLException(
					new StringBuilder("FATAL ERROR: ParameterMetaData - Unsupported query syntax. I dont know how to read your query so I cant get its metaData right now.")
					.append(Util.WC_NL).append("At the moment I can handle simple to mid complexity sql of types:")
					.append(MY_TYPE_SELECT).append(", ")
					.append(MY_TYPE_DELETE).append(", ")
					.append(MY_TYPE_INSERT).append(" and ")
					.append(MY_TYPE_UPDATE).append(".")
					.append(Util.WC_NL).append("This will be improved upon in later releases.")
					.toString(),
					"S1C00");
		}
	}
}
