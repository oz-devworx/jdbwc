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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jdbwc.core.util.MySQLTypes;
import com.jdbwc.core.util.SQLField;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

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
					fIdx++;
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

			final WCResultSet metaRes = getResult(connection, null, webResultFile, connection.getCaseSensitivity());
			final SQLField[] metaFields = new SQLField[metaRes.myRows.length()];
			final int dbtype = connection.getDbType();
			boolean baseServer = connection.isBaseServer();

			int mrow = 0;

			while(metaRes.next()){
				switch (dbtype){
				case Util.ID_POSTGRESQL:
					//for now Postgres gets its metadata via alternate means
					//so we return null from this method to trigger
					//the alternate metadata generation.
					break;

				case Util.ID_MYSQL:
				case Util.ID_DEFAULT:

					SQLField field = null;
					if(baseServer){
						field = getFromMySql(connection.getCatalog(), metaRes, dbtype);
					} else {
						field = getFromMySqli(connection.getCatalog(), metaRes, dbtype);
					}

					if(field!=null)
						metaFields[mrow++] = field;

					break;
				}
			}

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
		DataHandler aResult;
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

	private SQLField getFromMySql(String catalog, WCResultSet metaRes, int dbtype) throws SQLException {
		/* MySQL
		==========================================
		name - column name
		table - name of the table the column belongs to
		def - default value of the column
		max_length - maximum length of the column
		not_null - 1 if the column cannot be NULL
		primary_key - 1 if the column is a primary key
		unique_key - 1 if the column is a unique key
		multiple_key - 1 if the column is a non-unique key
		numeric - 1 if the column is numeric
		blob - 1 if the column is a BLOB
		type - the type of the column
		unsigned - 1 if the column is unsigned
		zerofill - 1 if the column is zero-filled
		==========================================
		 */

		try {

//			System.err.println("--------------------");
//
//			System.err.println("name = " + metaRes.getString("name"));// - column name
//			System.err.println("table = " + metaRes.getString("table"));// - name of the table the column belongs to
//			System.err.println("def = " + metaRes.getString("def"));// - default value of the column
//			System.err.println("max_length = " + metaRes.getString("max_length"));// - maximum length of the column
//			System.err.println("not_null = " + metaRes.getString("not_null"));// - 1 if the column cannot be NULL
//			System.err.println("primary_key = " + metaRes.getString("primary_key"));// - 1 if the column is a primary key
//			System.err.println("unique_key = " + metaRes.getString("unique_key"));// - 1 if the column is a unique key
//			System.err.println("multiple_key = " + metaRes.getString("multiple_key"));// - 1 if the column is a non-unique key
//			System.err.println("numeric = " + metaRes.getString("numeric"));// - 1 if the column is numeric
//			System.err.println("blob = " + metaRes.getString("blob"));// - 1 if the column is a BLOB
//			System.err.println("type = " + metaRes.getString("type"));// - the type of the column
//			System.err.println("unsigned = " + metaRes.getString("unsigned"));// - 1 if the column is unsigned
//			System.err.println("zerofill = " + metaRes.getString("zerofill"));// - 1 if the column is zero-filled
//
//			System.err.println("--------------------");


			String collationName = null;//gets converted to collation name
			long maxLength = metaRes.getLong("max_length");
			long length = maxLength;//unknown
			long decimals = 0;//unknown

			// trim fields down to fit a Java.Integer
			maxLength = (maxLength > Integer.MAX_VALUE) ? Integer.MAX_VALUE : maxLength;
			length = (length > Integer.MAX_VALUE) ? Integer.MAX_VALUE : length;
			decimals = (decimals > Integer.MAX_VALUE) ? Integer.MAX_VALUE : decimals;


			boolean isNullable = (metaRes.getInt("not_null")==0);
			boolean isUnsigned = (metaRes.getInt("unsigned")==1);
			boolean isAutoIndex = false;//unknown

			boolean isPrimaryKey = (metaRes.getInt("primary_key")==1);
			boolean isUniqueKey = (metaRes.getInt("unique_key")==1);
			boolean isIndex = (metaRes.getInt("multiple_key")==1);

//			System.err.println("type = " + metaRes.getInt("type"));

			return new SQLField(
					dbtype,
					catalog,
					null,
					metaRes.getString("table"),
					null,//unknown
					metaRes.getString("name"),
					metaRes.getString("def"),
					collationName,

					MySQLTypes.mysqlNameToMysqlType(metaRes.getString("type")),//native type gets converted by SQLField
					(int)maxLength,
					(int)length,
					(int)decimals,

					isNullable,
					isAutoIndex,
					isUnsigned,
					isPrimaryKey,
					isUniqueKey,
					isIndex);
		} catch (SQLException e) {
			return null;
		}
	}

	private SQLField getFromMySqli(String catalog, WCResultSet metaRes, int dbtype) {
		/* MySQLi
		==========================================
		name       = The name of the column
		orgname    = Original column name if an alias was specified
		table      = The name of the table this field belongs to (if not calculated)
		orgtable   = Original table name if an alias was specified
		def        = The default value for this field, represented as a string
		max_length = The maximum width of the field for the result set.
		length     = The width of the field, as specified in the table definition.
		charsetnr  = The character set number for the field.
		flags      = An integer representing the bit-flags for the field.
		type       = The data type used for this field
		decimals   = The number of decimals used (for integer fields)
		==========================================
		 */

		try {
			String collationName = MySQLCollations.getCollation(metaRes.getInt("charsetnr"));//gets converted to collation name
			long maxLength = metaRes.getLong("max_length");
			long length = metaRes.getLong("length");
			long decimals = metaRes.getLong("decimals");

			// trim fields down to fit a Java.Integer
			maxLength = (maxLength > Integer.MAX_VALUE) ? Integer.MAX_VALUE : maxLength;
			length = (length > Integer.MAX_VALUE) ? Integer.MAX_VALUE : length;
			decimals = (decimals > Integer.MAX_VALUE) ? Integer.MAX_VALUE : decimals;


			int bitFlags = metaRes.getInt("flags");
			/* MYSQLI BIT-FLAGS */
			boolean isNullable = !((bitFlags & 1) > 0);//MYSQLI_NOT_NULL_FLAG
			boolean isUnsigned = ((bitFlags & 32) > 0);//MYSQLI_UNSIGNED_FLAG
			boolean isAutoIndex = ((bitFlags & 512) > 0);//MYSQLI_AUTO_INCREMENT_FLAG

			boolean isPrimaryKey = ((bitFlags & 2) > 0);//MYSQLI_PRI_KEY_FLAG
			boolean isUniqueKey = ((bitFlags & 4) > 0);//MYSQLI_UNIQUE_KEY_FLAG
			boolean isIndex = ((bitFlags & 8) > 0);//MYSQLI_MULTIPLE_KEY_FLAG

//			System.err.println("type = " + metaRes.getInt("type"));

			return new SQLField(
					dbtype,
					catalog,
					null,
					metaRes.getString("orgtable"),
					metaRes.getString("orgname"),
					metaRes.getString("name"),
					metaRes.getString("def"),
					collationName,

					metaRes.getInt("type"),//native type gets converted by SQLField
					(int)maxLength,
					(int)length,
					(int)decimals,

					isNullable,
					isAutoIndex,
					isUnsigned,
					isPrimaryKey,
					isUniqueKey,
					isIndex);
		} catch (SQLException e) {
			return null;
		}
	}
}
