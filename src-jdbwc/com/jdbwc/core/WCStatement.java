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

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;

import com.jdbwc.core.util.SQLUtils;
import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.iface.Statement;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 * Extended JDBC-API implementation for java.sql.Statement.<br />
 * See this packages Statement interface for extension method details.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-05-13
 */
public class WCStatement implements Statement{

	//----------------------------------------------------------fields

//	/** Log object for this class. */
//    private static final Log LOG = LogFactory.getLog("jdbwc.core.Statement");

	protected transient WCConnection myConnection = null;

	protected SQLWarning warnings = null;

	protected transient int rsType = 0;
	protected transient int rsConcurrency = 0;
	protected transient int rsHoldability = 0;
	protected transient int rsAutoGenKeys = 0;

	protected transient int myDirection = WCResultSet.FETCH_FORWARD;
	protected transient int mySize = -1;
	protected transient int myMaxSize = -1;
	protected transient String myCatalog = null;

	/**
	 * A common SQL action:<br />
	 * <ul>
	 * <li>insert</li>
	 * <li>update</li>
	 * <li>delete</li>
	 * <li>execute</li>
	 * <li>savepoint</li>
	 * <li>release savepoint</li>
	 * </ul>
	 */
	private static final String SQL_QUERY = "query";
	private static final String SQL_BATCH = "batch";
	private static final String SQL_ROUTINE = "routine";
	private static final String SQL_RESULTS = "resultset";
//	private static final String SQL_TRANSAC = "transaction";

	private static final int RETURN_ROWS_AFFECTED = 1;
	private static final int RETURN_GENERATED_KEYS = 1;
	private static final String MY_ROWS_AFFECTED = "getRows";
	private static final String MY_GENERATED_KEYS = "getKeys";

	private transient StringBuilder myBatchStatement;
	private transient String myProcessedBatch = "";

	private transient int myRowAffected = 0;
	private transient int[] myRowsAffected = null;
	private transient WCResultSet myGeneratedKeys = null;
	private transient WCResultSet[] myExtraResults = null;
	private transient boolean useExtraResults = false;
	private transient int eResultsPointer = -1;
	private transient boolean hasClosed;

	//----------------------------------------------------------constructors

	protected WCStatement(final WCConnection connection, String catalog){
		rsType = CLOSE_CURRENT_RESULT;
		rsConcurrency = SUCCESS_NO_INFO;
		rsHoldability = NO_GENERATED_KEYS;
		hasClosed = false;
		myConnection = connection;
		myCatalog = catalog;
		myBatchStatement = new StringBuilder();
	}

	//----------------------------------------------------------public methods

	/**
	 * @see java.sql.Statement#addBatch(java.lang.String)
	 */
	public void addBatch(String sql) throws SQLException {
		if(!sql.trim().endsWith(";")){
			myBatchStatement.append(sql).append(';');
		}else{
			myBatchStatement.append(sql);
		}
	}

	/**
	 * @see java.sql.Statement#cancel()
	 */
	public void cancel() throws SQLException {
		resetStatement();
	}

	/**
	 * @see java.sql.Statement#clearBatch()
	 */
	public void clearBatch() throws SQLException {
		myBatchStatement = new StringBuilder();
		myProcessedBatch = "";

		myExtraResults = new WCResultSet[0];
		useExtraResults = false;
		eResultsPointer = -1;
	}

	/**
	 * @see java.sql.Statement#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		this.warnings = null;
	}

	/**
	 * @see java.sql.Statement#close()
	 */
	public void close() throws SQLException {
		resetStatement();
		hasClosed = true;
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String)
	 */
	public boolean execute(String sql) throws SQLException {

//		System.err.println("sql="+sql);

		boolean executed = false;
		if(SQLUtils.isSqlARoutine(sql)){
			executeStoredRoutine(sql);
//			System.err.println("executeStoredRoutine");
		}else if(SQLUtils.isSqlAResultType(sql)){
			if(isTransientBatch(sql)){
				executeTransientBatch(sql);
//				System.err.println("executeTransientBatch1");
			}else{
				executeWithResults(sql);
//				System.err.println("executeWithResults");
			}
		}else{
			executeTransientBatch(sql);
//			System.err.println("executeTransientBatch2");
		}
		if(getMoreResults()){
			executed = true;
		}
		return executed;
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, int)
	 */
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {

		switch(autoGeneratedKeys){
			case RETURN_GENERATED_KEYS:
				rsHoldability = RETURN_GENERATED_KEYS;
				break;
			case NO_GENERATED_KEYS:
			default:
				rsHoldability = NO_GENERATED_KEYS;
				break;
		}

		return execute(sql);
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, int[])
	 */
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		// TODO Auto-generated method stub
		throw new NotImplemented("execute(String sql, int[] columnIndexes)");
	}

	/**
	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
	 */
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		// TODO Auto-generated method stub
		throw new NotImplemented("execute(String sql, String[] columnNames)");
	}

	/**
	 * @see java.sql.Statement#executeBatch()
	 */
	public int[] executeBatch() throws SQLException {
		return dbQueryBatch();
	}

	/**
	 * @see java.sql.Statement#executeQuery(java.lang.String)
	 */
	public WCResultSet executeQuery(String sql) throws SQLException{
		return dbQueryResults(sql);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String)
	 */
	public int executeUpdate(String sql) throws SQLException{
		return dbQueryUpdate(sql);
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
	 */
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("executeUpdate(String sql, int autoGeneratedKeys)");
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
	 */
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("executeUpdate(String sql, int[] columnIndexes)");
	}

	/**
	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
	 */
	public int executeUpdate(String sql, String[] columnNames) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("executeUpdate(String sql, String[] columnNames)");
	}

	/**
	 * @see java.sql.Statement#getConnection()
	 */
	public WCConnection getConnection() throws SQLException{
		return myConnection.getConnection();
	}

	/**
	 * @see java.sql.Statement#getFetchDirection()
	 */
	public int getFetchDirection() throws SQLException{
		return myDirection;
	}

	/**
	 * @see java.sql.Statement#getFetchSize()
	 */
	public int getFetchSize() throws SQLException{
		return mySize;
	}

	/**
	 * @see java.sql.Statement#getGeneratedKeys()
	 */
	public WCResultSet getGeneratedKeys() throws SQLException{
		return myGeneratedKeys;
	}

	public int getRowAffected() throws SQLException{
		return myRowAffected;
	}

	public int[] getRowsAffected() throws SQLException{
		return myRowsAffected;
	}

	/**
	 * @see java.sql.Statement#getMaxFieldSize()
	 */
	public int getMaxFieldSize() throws SQLException{
		return 65535;// TODO: check this value is ok for Postgresql.
	}

	/**
	 * @see java.sql.Statement#getMaxRows()
	 */
	public int getMaxRows() throws SQLException{
		return myMaxSize;
	}

	/**
	 * @see java.sql.Statement#getMoreResults()
	 */
	public boolean getMoreResults() throws SQLException{
		boolean hasMore = false;
		if(myExtraResults!=null
		&& myExtraResults.length > 0
		&& myExtraResults.length > eResultsPointer+1
		&& myExtraResults[eResultsPointer+1]!=null
		){
			useExtraResults = true;
			eResultsPointer++;
			hasMore = true;
		}

		return hasMore;
	}

	/**
	 * @see java.sql.Statement#getMoreResults(int)
	 */
	public boolean getMoreResults(int current) throws SQLException{
		boolean hasMore = false;
		if(myExtraResults!=null
		&& myExtraResults.length > 0
		&& myExtraResults.length > current+1
		&& myExtraResults[current+1]!=null
		){
			useExtraResults = true;
			eResultsPointer = current+1;
			hasMore = true;
		}

		return hasMore;
	}

	/**
	 * @see java.sql.Statement#getQueryTimeout()
	 */
	public int getQueryTimeout() throws SQLException{
		return myConnection.getTimeOut()/1000; // myTimeOut is in millseconds
	}

	/**
	 * @see java.sql.Statement#getResultSet()
	 */
	public WCResultSet getResultSet() throws SQLException{
		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		WCResultSet resSet = null;
		if(useExtraResults && myExtraResults!=null){
			useExtraResults = false;
			resSet = myExtraResults[eResultsPointer];
		}else{
			resSet = new WCResultSet(localConnection, this);
		}

		checkResultSetsState(resSet);

		return resSet;
	}

	/**
	 * @see java.sql.Statement#getResultSetConcurrency()
	 */
	public int getResultSetConcurrency() throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("getResultSetConcurrency()");
	}

	/**
	 * @see java.sql.Statement#getResultSetHoldability()
	 */
	public int getResultSetHoldability() throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("getResultSetHoldability()");
	}

	/**
	 * JDBWC resultsets are generally always scroll insensitive
	 * by default.
	 *
	 * @see java.sql.Statement#getResultSetType()
	 */
	public int getResultSetType() throws SQLException{
		return WCResultSet.TYPE_SCROLL_INSENSITIVE;
//		throw new NotImplemented("getResultSetType()");
	}

	/**
	 * @see java.sql.Statement#getUpdateCount()
	 */
	public int getUpdateCount() throws SQLException{
		return myRowAffected;
	}

	/**
	 * @see java.sql.Statement#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException{
		return this.warnings;
	}

	/**
	 * @see java.sql.Statement#isClosed()
	 */
	public boolean isClosed() throws SQLException{
		return hasClosed;
	}

	/**
	 * @see java.sql.Statement#isPoolable()
	 */
	public boolean isPoolable() throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("isPoolable()");
	}

	/**
	 * @see java.sql.Statement#setCursorName(java.lang.String)
	 */
	public void setCursorName(String name) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("setCursorName(String name)");
	}

	/**
	 * @see java.sql.Statement#setEscapeProcessing(boolean)
	 */
	public void setEscapeProcessing(boolean enable) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("setEscapeProcessing(boolean enable)");
	}

	/**
	 * @see java.sql.Statement#setFetchDirection(int)
	 */
	public void setFetchDirection(int direction) throws SQLException{
		myDirection = direction;
		switch (direction){
		case WCResultSet.FETCH_FORWARD:
			myDirection = WCResultSet.FETCH_FORWARD;
			break;
		case WCResultSet.FETCH_REVERSE:
			myDirection = WCResultSet.FETCH_REVERSE;
			break;
		case WCResultSet.FETCH_UNKNOWN:
			myDirection = WCResultSet.FETCH_UNKNOWN;
			break;
		default:
			myDirection = WCResultSet.FETCH_FORWARD;
			break;
		}
	}

	/**
	 * @see java.sql.Statement#setFetchSize(int)
	 */
	public void setFetchSize(int rows) throws SQLException{
		mySize = rows;
	}

	/**
	 * @see java.sql.Statement#setMaxFieldSize(int)
	 */
	public void setMaxFieldSize(int max) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("setMaxFieldSize(int max)");
	}

	/**
	 * @see java.sql.Statement#setMaxRows(int)
	 */
	public void setMaxRows(int max) throws SQLException{
		myMaxSize = max;
	}

	/**
	 * @see java.sql.Statement#setPoolable(boolean)
	 */
	public void setPoolable(boolean poolable) throws SQLException{
		// TODO Auto-generated method stub
		throw new NotImplemented("setPoolable(boolean poolable)");
	}

	/**
	 * @see java.sql.Statement#setQueryTimeout(int)
	 */
	public void setQueryTimeout(int seconds) throws SQLException{
		myConnection.setTimeOut(seconds*1000); // output needs to be in milliseconds
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.cast(this);
	}

	//----------------------------------------------------------protected methods

	protected WCStatement getStatement(){
		return this;
	}

	/**
	 * PHP has trouble with custom DELIMITERS.
	 * In particular the DELIMITER keyword, so rather than have developers
	 * tear thier hair out over this, we silently reformat
	 * the sql to suit PHP and avoid much frustration in the process.<br />
	 * From reading, I get the impression this may apply to all PHP db interfaces (mySql, postgresSql).
	 *
	 * @param sqlRoutine
	 * @return true on success
	 * @throws SQLException
	 */
	protected boolean executeStoredRoutine(String sqlRoutine) throws SQLException {

		String cleanRoutine = SQLUtils.replaceCustomDelimiters(sqlRoutine);

		return dbQueryRoutine(cleanRoutine);
	}

	/**
	 * ResultSets are stored in the extra results holder.<br />
	 * If ResultSets were found this method returns true,
	 * otherwise it returns false.
	 *
	 * @param sqlRoutine
	 * @return true if resultsets were returned, else false.
	 * @throws SQLException
	 */
	protected boolean executeWithResults(String sqlRoutine) throws SQLException {
		boolean hasResults = false;
		eResultsPointer = -1;

		//System.err.println("ResultsType query Detected!");

		WCResultSet res = dbQueryResults(sqlRoutine);
		if(res!=null){
			if(myExtraResults!=null && myExtraResults.length > 0){
				WCResultSet[] extraRes = myExtraResults;
				WCResultSet[] allRes = new WCResultSet[extraRes.length + 1];
				allRes[0] = res;
				System.arraycopy(allRes, 1, extraRes, 0, extraRes.length);
				myExtraResults = allRes;
			}else{
				myExtraResults = new WCResultSet[1];
				myExtraResults[0] = res;
			}
			hasResults = true;
		}
		return hasResults;
	}

	//----------------------------------------------------------private methods

	/**
	 * perform an SQL query
	 *
	 * @param query String SQL query to process
	 * @return results int - the number of affected rows
	 */
	private void dbQuery(final String query) throws SQLException{
		dbQuery(query, NO_GENERATED_KEYS);
	}

	/**
	 * perform an SQL query
	 *
	 * @param query String SQL query to process
	 * @return results int - the number of affected rows
	 */
	private void dbQuery(final String query, final int getGeneratedKeys) throws SQLException{
		int results = 0;
		boolean getKeys = (getGeneratedKeys==RETURN_GENERATED_KEYS) ? true : false;
		HttpPost pmethod = initPostMethod(SQL_QUERY, query, getKeys, false);

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				String contents = Util.parseResponse(response);
				//System.err.println(contents);

				String contentArray[] = contents.split("_EOL__");
				results = getIntValueFromResult(contentArray);
				myRowAffected = results;

				if(getKeys){
					storeKeys();
				}
			}
		} catch (ClientProtocolException e) {
			pmethod.abort();
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			pmethod.abort();
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}
	}

	private boolean storeKeys(){
		boolean keysStored = false;
		if(myExtraResults!=null && myExtraResults.length > 0){
			int oldSize = myExtraResults.length;
			// store the keys in thier correct location
			myGeneratedKeys = myExtraResults[0];

			// remove the key results from the
			// extra results area and resize the array
			if(oldSize > 1){
				int newSize = oldSize - 1;
				WCResultSet[] newSet = new WCResultSet[newSize];
				System.arraycopy(myExtraResults, 1, newSet, 0, newSize);

				myExtraResults = newSet;
			}else{
				myExtraResults = null;
			}

			keysStored = true;
		}

		return keysStored;
	}

	/**
	 * perform an SQL query
	 *
	 * @param query String SQL query to process
	 * @return results int - the number of affected rows or 0 (zero)
	 */
	private int dbQueryUpdate(final String query) throws SQLException {
		int results = 0;
		HttpPost pmethod = initPostMethod(SQL_QUERY, query);

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				String contents = Util.parseResponse(response);

				String contentArray[] = contents.split("_EOL__");
				results = getIntValueFromResult(contentArray);
			}
		} catch (ClientProtocolException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}finally{
			pmethod.abort();
		}
		return results;
	}

	/**
	 * Get a WCResultSet from an SQL query.<br />
	 * A WCResultSet is an extended <i>java.sql.ResultSet</i><br />
	 * Gets the first result as the default ResultSet.<br />
	 * Additional results may be ignored or accessed via the <i>getNextResultSet()</i> method.
	 *
	 * @param query - String. SQL query
	 * @return sqlResultSet - LabeledArray.
	 */
	private WCResultSet dbQueryResults(final String query) throws SQLException {

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		WCResultSet sqlResultSet = new WCResultSet(localConnection, this, query);
		HttpPost pmethod = initPostMethod(SQL_RESULTS, query);
//		System.err.println("query = " + query);
		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				String contents = Util.parseResponse(response);

//				System.err.println("contents = " + contents);

				WCResultSet excessResultSets[] = new ResultsProcessor().processRawResults(localConnection, this, query, contents);
				int sizeOfResults = excessResultSets.length;
				// get the first result as the default ResultSet.
				// additional results may be ignored or accessed via getNextResultSet()
				sqlResultSet = excessResultSets[0];
				if(sizeOfResults > 1){
					myExtraResults = new WCResultSet[sizeOfResults-1];
					System.arraycopy(excessResultSets, 1, myExtraResults, 0, sizeOfResults-1);
				}
			}
		} catch (ArrayStoreException e){
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IndexOutOfBoundsException e){
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.getMessage());
		} catch (ClientProtocolException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}finally{
			pmethod.abort();
		}
		return sqlResultSet;
	}

	/**
	 * Designed for use with <i>executeBatch()</i>.<br />
	 * Perform a batch of previously added SQL queries.<br />
	 *
	 * @return results int[] - the number of affected rows per query
	 */
	private int[] dbQueryBatch() throws SQLException{
		int results[] = {};
		myProcessedBatch = myBatchStatement.toString();
		myBatchStatement = new StringBuilder();
		HttpPost pmethod = initPostMethod(SQL_BATCH, myProcessedBatch);

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				String contents = Util.parseResponse(response);
				//System.err.println("contents = " + contents);
				results = processBatchData(contents);
			}
		} catch (ClientProtocolException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}finally{
			pmethod.abort();
		}
		return results;
	}

	/**
	 * Designed for <i>execute()<i> calls that contain
	 * multiple statements.<br />
	 * Perform a batch of SQL queries.
	 *
	 * @param batchQuery String SQL queries to process
	 * @return results int - the number of affected rows
	 */
	private int[] dbQueryBatch(String batchQuery) throws SQLException{
		int results[] = {};
		myProcessedBatch = batchQuery;
		HttpPost pmethod = initPostMethod(SQL_BATCH, myProcessedBatch);

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				String contents = Util.parseResponse(response);
//				System.err.println("contents = " + contents);
				results = processBatchData(contents);
			}
		} catch (ClientProtocolException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}finally{
			pmethod.abort();
		}
		return results;
	}

	/**
	 * Designed for stored functions and procedures that
	 * are being created or dropped.
	 *
	 * @param routineQuery
	 * @return true on success
	 * @throws SQLException if something goes wrong
	 */
	private boolean dbQueryRoutine(String routineQuery) throws SQLException{
		boolean success = false;
		HttpPost pmethod = initPostMethod(SQL_ROUTINE, routineQuery);

		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		try {
			HttpResponse response = localConnection.getHttpResponse(pmethod);
			if (response != null) {
				Util.parseResponse(response);
//				System.err.println("RESULTS FOR: db_queryRoutine =\n" + contents + "\n---------");
				success = true;
			}
		} catch (ClientProtocolException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		} catch (IOException e) {
			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
		}finally{
			pmethod.abort();
		}
		return success;
	}

	private int[] processBatchData(String results) throws SQLException{
		int[] rowCounts = {};

		/* first line should be a comma separated list
		 * of Integers reflecting the rows affected by each query in the batch.
		 * These results should match the order the batch statements were executed in.
		 */

		//System.err.println(results);


		boolean multiLine = results.contains("_EOF__");
		String rowsAffected = results;
		if(multiLine){
			rowsAffected = results.substring(0, results.indexOf("_EOF__")).trim();
		}
		if(rowsAffected.indexOf(',') > -1){
			String batchAffects[] = rowsAffected.split(",");
			rowCounts = getIntValuesFromResult(batchAffects);
		}

		/* Any results that were produced by the batch will be stored
		 * for retrieval using the getMoreResults() methods */
		if(multiLine){
			String resultSetsCsv = results.substring(results.indexOf("_EOF__")+6).trim();
			myExtraResults = new ResultsProcessor().processRawResults(myConnection, this, myProcessedBatch, resultSetsCsv);
			eResultsPointer = -1;
			useExtraResults = true;
		}
		return rowCounts;
	}

//	/**
//	 * perform an SQL transaction
//	 *
//	 * @param query String SQL query to process
//	 * @return results int - the number of affected rows
//	 */
//	private int db_queryTransaction(final String query) throws SQLException {
//		int results = 0;
//		PostMethod pmethod = initPostMethod(MY_TRANSAC, query);
//
//		try {
//			if (myConnection.myClient.executeMethod(pmethod) != -1) {
//				String contents = Util.parseResponse(pmethod.getResponseBodyAsStream());
//
//				String contentArray[] = contents.split("\n");
//				results = getIntValueFromResult(contentArray);
//			}
//		} catch (HttpException e) {
//			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
////			System.err.println(e);
//			//e.printStackTrace();
//		} catch (IOException e) {
//			Util.checkForExceptions(Util.WC_ERROR_TAG + e.toString());
////			System.err.println(e);
//			//e.printStackTrace();
//		}finally{
//			pmethod.releaseConnection();
//		}
//		return results;
//	}

	/**
	 * Get a single integer from the given String array[]
	 *
	 * @param resultArray
	 * @return The first valid single integer from the resultsArray or 0 (zero)
	 */
	private int getIntValueFromResult(String[] resultArray){
		int result = 0;
		for(int i = 0; i < resultArray.length; i++){
			try {
				result = Integer.parseInt(Util.csvUnFormat(resultArray[i]).trim());
				break;
			} catch (NumberFormatException e) {
				result = 0;
			}
		}
		return result;
	}

	/**
	 * Get an int array[] from the given String array[]
	 *
	 * @param resultArray
	 * @return All valid single integers from the resultsArray or 0 (zero) if value isnt valid. Empty values are ignored.
	 */
	private int[] getIntValuesFromResult(String[] resultArray){
		int[] result = new int[resultArray.length];
		for(int i = 0; i < resultArray.length; i++){
			if(!"".equals(resultArray[i].trim())){
				try {
					result[i] = Integer.parseInt(resultArray[i].trim());
				} catch (NumberFormatException e) {
					result[i] = 0;
				}
			}
		}
		return result;
	}

	private void resetStatement(){
		rsType = 0;
		rsConcurrency = 0;
		rsHoldability = 0;

		myDirection = WCResultSet.FETCH_FORWARD;
		mySize = -1;
		myMaxSize = -1;

		myBatchStatement = new StringBuilder();
		myProcessedBatch = "";

		myRowAffected = 0;
		myRowsAffected = new int[0];
		myGeneratedKeys = null;
		myExtraResults = new WCResultSet[0];
		useExtraResults = false;
		eResultsPointer = -1;
	}

	/**
	 * Executes a transient query batch.<br />
	 * For the purpose of explaining this further, a transient query batch is a pre-built
	 * batch of queries.<br />
	 * The batch is usually built programatically and not through using addBatch(),
	 * thus the transient description as the query only exists in short term memory
	 * and is not designed to be shared before its been processed.
	 *
	 * @param sql A batch of sql statements in a single String.
	 * @throws SQLException
	 */
	private void executeTransientBatch(String sql) throws SQLException{
		if(isTransientBatch(sql)){
			myRowsAffected = dbQueryBatch(sql);
		}else{
			dbQuery(sql);
		}
	}

	/**
	 * Checks an sql query to determine if its a batch of statements or a single statement.
	 *
	 * @param sql A query to be examined for the existence of multiple statements.
	 * @return boolean - true if the query is a batch of statements.
	 * @throws SQLException
	 */
	private boolean isTransientBatch(String sql) throws SQLException{
		boolean isBatch = false;
		if(sql.trim().split(";").length > 1){
			isBatch = true;
		}
		return isBatch;
	}

	private void checkResultSetsState(WCResultSet resSet) throws SQLException{
		if(resSet==null){
			throw new SQLException(
					"ResultSet returned a null Object. This indicates your ResultSet is most likely closed.\n"
					+ "Check your java sytax and logical conditions related to the ResultSet that caused this error first.",
					"WCNOE");
		}
	}

	/**
	 * Initialises a new HttpPost and populates it with data.<br />
	 * The returned HttpPost is ready to be executed.
	 *
	 * @param actionType This value should be selected from one of this classes static final ACTION_TYPES
	 * @param sqlQuery A plaintext SQL query ready for executing on the remote webserver.
	 * @return A complete HttpPost object that can be executed without further changes.
	 */
	private HttpPost initPostMethod(final String actionType, final String sqlQuery) throws SQLException{
		return initPostMethod(actionType, sqlQuery, false, false);
	}

	/**
	 * Initialises a new HttpPost and populates it with data.<br />
	 * The returned HttpPost is ready to be executed.<br />
	 * If both boolean values are set to true, both value types will be stored until the next Statement is executed or released.
	 *
	 * @param actionType This value should be selected from one of this classes static final ACTION_TYPES
	 * @param sqlQuery A plaintext SQL query ready for executing on the remote webserver.
	 * @param getGeneratedKeys boolean, if true the any autoincrement values generated by the sql query are requested
	 * @param getRowsAffected boolean, if true the number of rows affected by the sql query are requested
	 * @return A complete HttpPost object that can be executed without further changes.
	 */
	private HttpPost initPostMethod(final String actionType, String sqlQuery, final boolean getGeneratedKeys, boolean getRowsAffected) throws SQLException{
		WCConnection localConnection = myConnection;

		if(!localConnection.getCatalog().equals(myCatalog))
			localConnection.setCatalog(myCatalog);

		HttpPost pmethod = localConnection.getHttpPost();

		synchronized(pmethod){
			DataHandler nvpArray = Util.getCaseSafeHandler(0);
			nvpArray.addData(Util.TAG_AUTH, localConnection.getCredentials());
			nvpArray.addData(Util.TAG_DBTYPE, localConnection.getDbType());
			nvpArray.addData(Util.TAG_ACTION, actionType);
			if(sqlQuery!=null){
				sqlQuery = SQLUtils.stripComments(sqlQuery, "'", "'");
				nvpArray.addData(Util.TAG_SQL, sqlQuery);
			}

			if(getGeneratedKeys){
				nvpArray.addData(MY_GENERATED_KEYS, RETURN_GENERATED_KEYS);
			}
			if(getRowsAffected){
				nvpArray.addData(MY_ROWS_AFFECTED, RETURN_ROWS_AFFECTED);
			}

	        pmethod.setEntity(Util.prepareForWeb(nvpArray));
		}

		return pmethod;
	}
}
