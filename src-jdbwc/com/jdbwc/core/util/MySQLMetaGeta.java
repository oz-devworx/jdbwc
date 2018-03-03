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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jdbwc.core.WCConnection;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 *
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public class MySQLMetaGeta {

	private transient WCConnection myConnection = null;
	private transient boolean USE_INFOSCHEMA = true;

	/**
	 * Constructs a new instance of this.
	 */
	protected MySQLMetaGeta(WCConnection connection) {
		myConnection = connection;
	}

	/**
	 *
	 * @param tableNames
	 * @param columns
	 * @param aliases
	 * @return array of SQLField MetaData
	 * @throws SQLException
	 */
	protected SQLField[] getResultSetMetaData(
			DataHandler tableNames,
			DataHandler columns,
			DataHandler aliases)
	throws SQLException{
		SQLField[] rsMd = new SQLField[0];
		USE_INFOSCHEMA = myConnection.versionMeetsMinimum(5, 0, 0);

		if(USE_INFOSCHEMA){
//			for(int i = 0; i < tableNames.length(); i++){
				rsMd = getRsMetaData2(rsMd, tableNames, columns, aliases);
//			}
		}else{
			rsMd = getRsMetaData(tableNames, columns, aliases);
		}
		return rsMd;
	}

	/**
	 *
	 * @param tableNames
	 * @param columns
	 * @param params
	 * @return array of SQLField MetaData
	 * @throws SQLException
	 */
	protected SQLField[] getParameterMetaData(
			DataHandler tableNames,
			DataHandler columns,
			DataHandler params)
	throws SQLException{
		SQLField[] paramMdFields = new SQLField[0];
		USE_INFOSCHEMA = myConnection.versionMeetsMinimum(5, 0, 0);

		for(int tIdx = 0; tIdx < tableNames.length(); tIdx++){
			if(USE_INFOSCHEMA){
				paramMdFields = getParamMetaData(paramMdFields, tableNames.getKey(tIdx), columns, params);
			}else{
				paramMdFields = getParamMetaData(paramMdFields, tableNames.getKey(tIdx), columns, params);
			}
		}

		return paramMdFields;
	}


	private SQLField[] getRsMetaData(DataHandler tables, DataHandler columns, DataHandler aliases) throws SQLException{
		SQLField[] paramMdFields = new SQLField[0];
		for(int i = 0; i < tables.length(); i++){
			DataHandler rsMetadata = getCourseRsMetaData(Util.getCaseSafeHandler(Util.CASE_MIXED), tables.getKey(i));
			paramMdFields = getFineRsMetaData(paramMdFields, rsMetadata, tables.getKey(i), columns, aliases);
		}
		return paramMdFields;
	}

	private DataHandler getCourseRsMetaData(DataHandler myDbMetadata, String tableName) throws SQLException{

		Statement statement = myConnection.createStatement();

		String sql = "";
		if(tableName.contains(".")){
			String[] dbNTable = SQLUtils.removeBlanks(tableName.trim().split("\\."));
			sql = "SHOW TABLE STATUS FROM " + dbNTable[0].trim() + " WHERE Name LIKE '" + dbNTable[1].trim() + "';";
		}else{
			sql = "SHOW TABLE STATUS LIKE '" + tableName + "';";
		}

		ResultSet resultSet = statement.executeQuery(sql);
		if(resultSet.next()){
			myDbMetadata.addData("Engine", resultSet.getString("Engine"));
			myDbMetadata.addData("Auto_increment", resultSet.getString("Auto_increment"));
			myDbMetadata.addData("Collation", resultSet.getString("Collation"));
		}
		statement.close();
		return myDbMetadata;
	}

	private SQLField[] getFineRsMetaData(SQLField[] fieldSet, DataHandler myDbMetadata, String tableName, DataHandler columns, DataHandler aliases) throws SQLException{

		final String[] keys = {"PRI","UNI","MUL"};
		final String autoInc = "auto_increment";
		final String nullable = "YES";

		/* these values are for the Field constructor */
		String columnName = "";
		String columnAlias = "";// AS - Field label goes here
		String mySqlTypeName = "";
		String valueDefault = "";

		boolean isAutoIndex = false;
		boolean isNullable = false;

		boolean isPrimaryKey = false;
		boolean isUniqueKey = false;
		boolean isIndex = false;

		String database = myConnection.getDatabase();
		String schema = database;
		String charsetName = "";
		String collation = myDbMetadata.getString("Collation");
		String engine = myDbMetadata.getString("Engine");

		int autoindexValue = 0;

		try {
			autoindexValue = myDbMetadata.getInt("Auto_increment");
		} catch (NumberFormatException ignored) {
			//ignored.printStackTrace();
		}

		int length = -1;

		ResultSet resultSet = null;

		/* the next queries get fine metadata relating to the result fields. */
		Statement statement = myConnection.createStatement();

		// We could probably use the mysqli MetaData function
		// on the scripting end for this next bit.
		// Work out which is the fastest & least labour intensive
		// remembering that we need to do one additional query before this
		// to get the collation, engine & auto_increment values.
		//
		String sql = "SHOW COLLATION LIKE '" + collation + "';";
		sql += "DESCRIBE " + tableName + ";";

		if(statement.execute(sql)){
			resultSet = statement.getResultSet();
			if(resultSet.next()){
				charsetName = resultSet.getString("Charset");
			}

			if(statement.getMoreResults()){
				resultSet = statement.getResultSet();

				while(resultSet.next()){
					columnName = resultSet.getString("Field");

					/* allows fields for tableName that are specifically defined OR wildcard entries */
					if(columns.length()==0 || columns.hasKey(tableName) && (columns.hasElement(tableName, columnName) || columns.hasElement(tableName, "*"))){

						/* if we have a specific db with the table name, we need to
						 * correct the column, schema and database names */
						if(tableName.contains(".")){
							database = tableName.substring(0, tableName.indexOf('.'));
							schema = database;
							tableName = tableName.substring(tableName.indexOf('.')+1);
						}

						columnAlias = (aliases.length()==0) ? columnName : aliases.getString(columnName);
						mySqlTypeName = resultSet.getString("Type");
						valueDefault = resultSet.getString("Default");

						isAutoIndex = resultSet.getString("Extra").equalsIgnoreCase(autoInc);
						isNullable = resultSet.getString("Null").equalsIgnoreCase(nullable);

						isPrimaryKey = resultSet.getString("Key").equalsIgnoreCase(keys[0]);
						isUniqueKey = resultSet.getString("Key").equalsIgnoreCase(keys[1]);
						isIndex = resultSet.getString("Key").equalsIgnoreCase(keys[2]);

						SQLField field = new SQLField(
								myConnection.getDbType(),
								columnName,
								columnAlias,
								tableName,
								database,
								collation,
								schema,
								engine,
								charsetName,
								mySqlTypeName,
								valueDefault,

								isAutoIndex,
								isNullable,
								isPrimaryKey,
								isUniqueKey,
								isIndex,

								length,
								autoindexValue);


						fieldSet = SQLUtils.rebuildFieldSet(field, fieldSet);
					}
				}
			}
		}
		statement.close();
		return fieldSet;
	}

	/**
	 * Gets ResultSetMetaData from the INFORMATION_SCHEMA database
	 * in a single query (per table).
	 * 
	 * @param fieldSet
	 * @param tableNames
	 * @param columns
	 * @param aliases
	 * @return an array of SQLField Objects
	 * @throws SQLException
	 */
	private SQLField[] getRsMetaData2(SQLField[] fieldSet, DataHandler tableNames, DataHandler columns, DataHandler aliases) throws SQLException{

		final String[] keys = {"PRI","UNI","MUL"};

		/* these values are for the Field constructor */
		String columnName = "";
		String columnAlias = "";// AS - Field label goes here
		String mySqlTypeName = "";
		String valueDefault = "";

		boolean isAutoIndex = false;
		boolean isNullable = false;

		boolean isPrimaryKey = false;
		boolean isUniqueKey = false;
		boolean isIndex = false;

		String database = myConnection.getDatabase();
		String schema = "";
		String charsetName = "";
		String collation = "";
		String engine = "";

		int autoindexValue = 0;
		int length = 0;

		/* get metadata relating to the result fields.
		 * Check for database names appended to the Table's name
		 * and build the query conditional part accordingly
		 */
		String sqlConditions = "";
		String sqlOr;
		for(int i = 0; i < tableNames.length(); i++){
			sqlOr = ((i>0) ? "OR " : "WHERE ");
			if(tableNames.getKey(i).contains(".")){
				String[] dbNTable = SQLUtils.removeBlanks(tableNames.getKey(i).trim().split("\\."));
				sqlConditions += new StringBuilder(sqlOr).append("(TABLE_SCHEMA LIKE '").append(dbNTable[0].trim()).append("' ")
					.append(" AND TABLE_NAME LIKE '").append(dbNTable[1].trim()).append("') ").toString();

			}else{
				sqlConditions += new StringBuilder(sqlOr).append("(TABLE_SCHEMA LIKE '").append(database).append("' ")
					.append(" AND TABLE_NAME LIKE '").append(tableNames.getKey(i)).append("') ").toString();
			}
		}
		/* TABLE META-FIELDS */
		StringBuilder sqlBuilder = new StringBuilder("SELECT ")
			.append("AUTO_INCREMENT, ")
			.append("ENGINE ")
			.append("FROM INFORMATION_SCHEMA.TABLES ")
			.append(sqlConditions)
			.append("ORDER BY TABLE_SCHEMA, TABLE_NAME;");

		/* COLUMN META-FIELDS */
		sqlBuilder.append("SELECT ")
			.append("CHARACTER_SET_NAME, ")
			.append("COLLATION_NAME, ")
			.append("COLUMN_NAME, ")
			.append("TABLE_NAME, ")
			.append("TABLE_SCHEMA, ")
			.append("COLUMN_DEFAULT, ")
			.append("IF(IS_NULLABLE='YES', 'TRUE', 'FALSE') AS NULLABLE, ")
			.append("COLUMN_TYPE, ")
			.append("COLUMN_KEY, ")
			.append("IF(EXTRA='auto_increment', 'TRUE', 'FALSE') AS IS_AUTOINC, ")
			.append("COLUMN_COMMENT ")
			.append("FROM INFORMATION_SCHEMA.COLUMNS ")
			.append(sqlConditions)
			.append("ORDER BY TABLE_SCHEMA, TABLE_NAME, COLUMN_TYPE, EXTRA;");


		ResultSet resultSet = null;
		Statement statement = myConnection.createStatement();

		/* check for results and build a metaData fieldSet accordingly.
		 * The results ratio is 1 MetaTable row to many MetaColumn rows */
		if(statement.execute(sqlBuilder.toString())){
			resultSet = statement.getResultSet();
			/* process the first ResultSet from INFORMATION_SCHEMA.TABLES */
			if(resultSet.next()){
				engine = resultSet.getString("ENGINE");
				String aidx = resultSet.getString("AUTO_INCREMENT");
				try {
					autoindexValue = Integer.parseInt(aidx);
				} catch (NumberFormatException e) {
					autoindexValue = 0;
				}
				resultSet.close();

				if(statement.getMoreResults()){
					resultSet = statement.getResultSet();
					/* process the second ResultSet from INFORMATION_SCHEMA.COLUMNS */
					while(resultSet.next()){

						columnName = resultSet.getString("COLUMN_NAME");
						String tableName = tableNames.getKey(0);
						boolean hasWildcard = columns.hasElement(tableName, "*");

						/* allows fields for tableName that are specifically defined OR wildcard entries */
						if((columns.hasKey(tableName) && (columns.hasElement(tableName, columnName)) || hasWildcard)){

							database = resultSet.getString("TABLE_SCHEMA");
							schema = database;
							tableName = resultSet.getString("TABLE_NAME");
							columnAlias = "";

							if(!hasWildcard){
								synchronized(tableNames){
									/* remove processed elements from DataHandlers */
									columnAlias = aliases.getString(columns.getIndexByElement(tableName, columnName));
									columns.removeByIndex(columns.getIndexByElement(tableName, columnName));
									aliases.removeByIndex(aliases.getIndexByElement(columnName, columnAlias));
//									System.err.println("1 column & 1 alias removed!");
									if(!columns.hasKey(tableName)){
										tableNames.removeByIndex(0);
//										System.err.println("1 table removed!");
									}

								}
							}
							tableName = resultSet.getString("TABLE_NAME");


							charsetName = resultSet.getString("CHARACTER_SET_NAME");
							collation = resultSet.getString("COLLATION_NAME");

							mySqlTypeName = resultSet.getString("COLUMN_TYPE");
							valueDefault = resultSet.getString("COLUMN_DEFAULT");

							isAutoIndex = resultSet.getBoolean("IS_AUTOINC");
							isNullable = resultSet.getBoolean("NULLABLE");

							String colKey = resultSet.getString("COLUMN_KEY");
							isPrimaryKey = colKey.equalsIgnoreCase(keys[0]);
							isUniqueKey = colKey.equalsIgnoreCase(keys[1]);
							isIndex = colKey.equalsIgnoreCase(keys[2]);

							SQLField field = new SQLField(
									myConnection.getDbType(),
									columnName,
									columnAlias,
									tableName,
									database,
									collation,
									schema,
									engine,
									charsetName,
									mySqlTypeName,
									valueDefault,

									isAutoIndex,
									isNullable,
									isPrimaryKey,
									isUniqueKey,
									isIndex,

									length,
									autoindexValue);


							fieldSet = SQLUtils.rebuildFieldSet(field, fieldSet);
						}
					}
					resultSet.close();
				}
			}
		}
		statement.close();
		return fieldSet;
	}













	/**
	 * 
	 * @param paramMdFields
	 * @param tableName
	 * @param columns
	 * @param params
	 * @return an array of SQLField Objects containing parameter metadata
	 * @throws SQLException
	 */
	private SQLField[] getParamMetaData(SQLField[] paramMdFields, String tableName, DataHandler columns, DataHandler params) throws SQLException{

//		SQLField[] paramMdFields = new SQLField[0];
		final String nullable = "YES";
		final String MY_MODE_UNKNOWN = "UNKNOWN";

		/* these values are for the Field constructor */
		String fieldName = "";
		String typeName = "";
		boolean isNullable = false;
		String mode = MY_MODE_UNKNOWN;

		/* the next queries get fine metadata relating to the result fields. */
		Statement statement = myConnection.createStatement();
		String sql = "DESCRIBE `" + tableName + "`;";
		ResultSet resultSet = statement.executeQuery(sql);

		while(!columns.isEmpty()){
			/* Cycle forward through the results */
			while(resultSet.next() && !columns.isEmpty()){
				fieldName = resultSet.getString("Field");

				/* allows fields for tableName that are specifically defined OR unknown entries */
				if((columns.hasKey(fieldName) && (columns.hasElement(fieldName, tableName)) || columns.hasElement("?", tableName))){
					if(columns.hasKey(fieldName)){
						columns.removeByKey(fieldName);
					}else if(columns.hasKey("?")){
						columns.removeByKey("?");
					}

					typeName = resultSet.getString("Type");
					isNullable = resultSet.getString("Null").equalsIgnoreCase(nullable);
					mode = params.getString(fieldName);

					SQLField field = new SQLField(
							myConnection.getDbType(),
							fieldName,
							typeName,
							isNullable,
							mode);

					paramMdFields = SQLUtils.rebuildFieldSet(field, paramMdFields);
				}
			}
			/* Cycle backward through the same results to pick up any we couldn't identify on the first pass. */
			while(!columns.isEmpty() && resultSet.previous()){
				fieldName = resultSet.getString("Field");

				/* allows fields for tableName that are specifically defined OR unknown entries */
				if((columns.hasKey(fieldName) && (columns.hasElement(fieldName, tableName)) || columns.hasElement("?", tableName))){
					if(columns.hasKey(fieldName)){
						columns.removeByKey(fieldName);
					}else if(columns.hasKey("?")){
						columns.removeByKey("?");
					}

					typeName = resultSet.getString("Type");
					isNullable = resultSet.getString("Null").equalsIgnoreCase(nullable);
					mode = params.getString(fieldName);

					SQLField field = new SQLField(
							myConnection.getDbType(),
							fieldName,
							typeName,
							isNullable,
							mode);
					
					paramMdFields = SQLUtils.rebuildFieldSet(field, paramMdFields);
				}
			}
		}
		statement.close();
		return paramMdFields;
	}

}
