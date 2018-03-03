/* ********************************************************************
 * Copyright (C) 2010 Oz-DevWorX (Tim Gall)
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

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jdbwc.util.SQLField;
import com.jdbwc.util.SQLResProcessor;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.ObjectArray;

/**
 * Handles processing raw server results into ResultSets
 *
 * @author Tim Gall
 * @version 2010-05-07
 */
public class ResultsProcessor {

	public ResultsProcessor(){

	}

	/**
	 * For empty results (other than those in a batch)
	 * return an empty resultset. No resultset-metadata is returned from this method.
	 *
	 * @param connection WCConnection - parent connection.
	 * @param statement WCStatement - related to this rawResult.
	 * @param query String - the query/ies related to this rawResult.
	 * @param rawResult String - containing .csv-ish data. First row contains column names.
	 * @return a WCResultSet[] containing all resultsets from the processed data.
	 * @throws SQLException - if something goes wrong.
	 */
	protected WCResultSet[] processRawResultsNoMeta(final WCConnection connection, final WCStatement statement, final String query, final String rawResult) throws SQLException{
		final String[] webResultFiles = rawResult.trim().split("_EOF__");
		final int limit = webResultFiles.length;

		final WCResultSet[] results = new WCResultSet[limit];
		String[] batchQueries = {};
		final int caseSafety = connection.getCaseSensitivity();
		int resNum = 0;

//		System.err.println(rawResult);
//		System.err.println("-------------------");
//		System.err.println("-------------------");
//		System.err.println("-------------------");

		for(int fIdx = 0; fIdx < limit; fIdx++){
			String aQuery = "";
			/*
			 * Find instances of multiple reultsets.
			 * As resultsets are processed, we need to check the sql string
			 * and find queries that can return results,
			 * then attach each query to its resultset
			 * for later use with ResultSetMetaData.
			 */
			if(batchQueries.length==0 && limit > 1){
				batchQueries = query.split(";");
			}
			if(batchQueries.length == limit){
				aQuery = batchQueries[fIdx] + ";";
			}else{
				aQuery = query;
			}

//			System.err.println(webResultFiles[fIdx]);
//			System.err.println(webResultFiles[fIdx+1]);
//			System.err.println("-------------------");


//			webResultFiles[fIdx] = webResultFiles[fIdx].trim();

			if(!"".equals(webResultFiles[fIdx])){
				//empty batch result
				if("_".equals(webResultFiles[fIdx])){
					results[resNum] = new WCResultSet(connection);


				}else{
					if(statement==null){
						results[resNum] = new WCResultSet(connection, statement, "", Util.getCaseSafeHandler(caseSafety));
					}else{
						results[resNum] = new WCResultSet(connection, statement, aQuery, Util.getCaseSafeHandler(caseSafety));
					}

					//get data [if any]
					if(webResultFiles[fIdx].startsWith("__JDBWC-DATA___EOL__")){
						webResultFiles[fIdx] = webResultFiles[fIdx].substring("__JDBWC-DATA___EOL__".length());

//						System.err.println("webResultFiles[fIdx] = " + webResultFiles[fIdx]);

						results[resNum] = getResult(connection, results[resNum], webResultFiles[fIdx], caseSafety);
					}

//					System.err.println("results row size = " + results[rowNum].getFetchSize());
				}
			}
			resNum++;
		}
//		System.err.println("-------------------");
//		System.err.println("-------------------");
//		System.err.println("-------------------");

		return results;
	}

	/**
	 * For empty results (other than those in a batch)
	 * return an empty resultset. Where possible basic resultset-metadata is returned with each resultset.
	 *
	 * @param connection WCConnection - parent connection.
	 * @param statement WCStatement - related to this rawResult.
	 * @param query String - the query/ies related to this rawResult.
	 * @param rawResult String - containing .csv-ish data. First row contains column names.
	 * @return a WCResultSet[] containing all resultsets from the processed data.
	 * @throws SQLException - if something goes wrong.
	 */
	protected WCResultSet[] processRawResults(final WCConnection connection, final WCStatement statement, final String query, final String rawResult) throws SQLException{
		final String[] webResultFiles = rawResult.trim().split("_EOF__");
		final int limit = webResultFiles.length;

		List<WCResultSet> resultList = new ArrayList<WCResultSet>(limit);//limit is the largest possible size, not the final size

		String[] batchQueries = {};
		final int caseSafety = connection.getCaseSensitivity();
		int resNum = 0;
		WCResultSet res;

//		System.err.println("limit = " + limit);
//		System.err.println(rawResult);
//		System.err.println("-------------------");
//		System.err.println("-------------------");
//		System.err.println("-------------------");

		for(int fIdx = 0; fIdx < limit; fIdx++){
			String aQuery = "";
			/*
			 * Find instances of multiple reultsets.
			 * As resultsets are processed, we need to check the sql string
			 * and find queries that can return results,
			 * then attach each query to its resultset
			 * for later use with ResultSetMetaData.
			 */
			if(batchQueries.length==0 && limit > 1){
				batchQueries = query.split(";");
			}else{
				aQuery = query;
			}

			webResultFiles[fIdx] = webResultFiles[fIdx].trim();

			if(!"".equals(webResultFiles[fIdx])){

				if("_".equals(webResultFiles[fIdx])){
					//empty batch result
					resultList.add(new WCResultSet(connection));


				}else{
					if("".equals(aQuery))
						aQuery = batchQueries[resNum] + ";";

					res = new WCResultSet(connection, statement, aQuery, Util.getCaseSafeHandler(caseSafety));

					//get data [if any]
					if(webResultFiles[fIdx].startsWith("__JDBWC-DATA___EOL__")){
						webResultFiles[fIdx] = webResultFiles[fIdx].substring("__JDBWC-DATA___EOL__".length());

						res = getResult(connection, res, webResultFiles[fIdx], caseSafety);
					}

//					System.err.println("results row size = " + results[rowNum].getFetchSize());

					//get metadata. Only add metadata, not nulls.
					//Allows DB's that dont produce PHP metadata sets to fetch metadata using backup metadata classes/methods.
					WCResultSetMetaData metadata = getMetaData(connection, webResultFiles[fIdx+1]);
					if(metadata!=null){
						res.addMetaData(getMetaData(connection, webResultFiles[fIdx+1]));
					}

					resultList.add(res);
					fIdx++;// extra increment for the metadata
				}
			}
			resNum++;
		}

		return resultList.toArray(new WCResultSet[resultList.size()]);
	}

	private WCResultSetMetaData getMetaData(final WCConnection connection, String webResultFile) throws SQLException{
		WCResultSetMetaData metadata = null;

		if(webResultFile.trim().startsWith("__JDBWC-METADATA___EOL__")){
			webResultFile = webResultFile.substring("__JDBWC-METADATA___EOL__".length());

			// using reflection, get the correct class for the database type thats in use
			SQLResProcessor resProc = null;
			try {
				Class<?> metaClass = Class.forName(connection.getDbPackagePath() + "SQLResProcessorImp");

	            Constructor<?> ct = metaClass.getConstructor(new Class[]{});

	            resProc = (SQLResProcessor)ct.newInstance(new Object[]{});

			} catch (Throwable e) {
				throw new SQLException("Could not construct a SQLResProcessor Object", e);
			}

			final WCResultSet metaRes = getResult(connection, null, webResultFile, connection.getCaseSensitivity());
			final SQLField[] metaFields = resProc.getFields(connection.getCatalog(), metaRes);

			metaRes.close();
			if(metaFields.length > 0)
				metadata = new WCResultSetMetaData(metaFields);
		}

		return metadata;
	}

	/**
	 *
	 * @param connection WCConnection
	 * @param result WCResultSet use null for processing metadata
	 * @param webResultFile String
	 * @param caseSafety int
	 * @return WCResultSet populated with data from webResultFile or an empty WCResultSet
	 * if webResultFile has no data.
	 * @throws SQLException
	 */
	private WCResultSet getResult(final WCConnection connection, WCResultSet result, String webResultFile, final int caseSafety) {
		if(result==null)
			try {
				result = new WCResultSet(connection);
			} catch (SQLException e) {
				// shouldn't happen
				e.printStackTrace();
			}




		final String[] webResultSet = webResultFile.split("_EOL__");

//		System.err.println("webResultSet0 = " + webResultSet[0]);
//		System.err.println("webResultSet1 = " + webResultSet[1]);

		if(webResultSet.length==1)
			return result;



		String[] rowLength = webResultSet[0].split(",");
		String[] webResultCol = new String[rowLength.length];
		ObjectArray aResult;
		for(int i = 0; i < webResultSet.length; i++){
			aResult = Util.getCaseSafeHandler(caseSafety);

			final String[] webResultRow = webResultSet[i].split(",");


			for(int j = 0; j < webResultRow.length; j++){

				if(i==0){
					webResultCol[j] = webResultRow[j].trim();
				}else{
					if(webResultCol.length>=j){
						aResult.addData(webResultCol[j], Util.csvUnFormat(webResultRow[j]));
					}
				}
			}
			if(!aResult.isEmpty()){
				result.addRow(aResult);
			}
		}

		return result;
	}
}
