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
import java.util.ArrayList;
import java.util.List;

import com.jdbwc.core.WCConnection;
import com.jdbwc.core.WCResultSet;
import com.jdbwc.core.WCStatement;
import com.ozdevworx.dtype.DataHandler;

/**
 * Gets meta data from MySQL databases.
 *
 * @author Tim Gall
 * @version 2008-05-29
 * @version 2010-04-27
 */
public class MySQLMetaGeta {

	private transient WCConnection myConnection = null;


	/**
	 * Constructs a new instance of this.
	 */
	protected MySQLMetaGeta(WCConnection connection) {
		myConnection = connection;
	}

	/**
	 * @param sql
	 * @param columns
	 * @return array of SQLField MetaData
	 * @throws SQLException
	 */
	protected SQLField[] getResultSetMetaData(
			String sql,
			DataHandler columns)
	throws SQLException{

		if(myConnection.versionMeetsMinimum(5, 0, 0)){
			return getRsMetaData(sql, columns);
		}else{
			throw new SQLException("MySQL versions less than 5.0.0 are not supported in this release.");
		}
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

		if(myConnection.versionMeetsMinimum(5, 0, 0)){
			for(int tIdx = 0; tIdx < tableNames.length(); tIdx++){
				//new results get appended to existing
				paramMdFields = getParamMetaData(paramMdFields, tableNames.getKey(tIdx), columns, params);
			}
		}else{
			throw new SQLException("MySQL versions less than 5.0.0 are not supported in this release.");
		}

		return paramMdFields;
	}


	/**
	 * Gets ResultSetMetaData from the INFORMATION_SCHEMA database.<br />
	 * This method gets bypassed for MySQL.
	 * It was originally the second official version for harvesting ResultSetMetaData.
	 * Now MySQL metadata is harvested in the same pass as the queries ResultSet.
	 *
	 * @param sql
	 * @param columns
	 * @return an array of SQLField Objects
	 * @throws SQLException
	 */
	private SQLField[] getRsMetaData(String sql, DataHandler columns) throws SQLException{

		if(sql.isEmpty())
			throw new SQLException("Could not determine the resultsets query to get metadata for.");


		final String[] keys = {"PRI","UNI","MUL"};

		List<SQLField> fieldList = new ArrayList<SQLField>();

		/* these values are for the Field constructor */
		String catalogName;
		String schemaName = "";
		String tableName;
		String columnName;
		String columnAlias;

		String columnDefault;
		boolean isNullable;
		int sqlType;

		int maxLength;
		int maxPrecision;
		int maxScale;

		String collationName;
		boolean isAutoIndex;
		boolean isUnsigned;
		boolean isPrimaryKey;
		boolean isUniqueKey;
		boolean isIndex;


		/*
		 * build a temporary sql view of the resultset query.
		 * This saves a a monumental amount of work but still requires
		 * we manually produce the database-name/s and original field-name/s.
		 *
		 * Other than that, we simply retrieve all other relevant meta info from the view
		 * then delete it. This is handled in batches on the server end to save time.
		 */

		// generate a name thats not likly to conflict
		// but is easy to locate using LIKE
		String viewName = "wc_rsmeta_" + com.jdbwc.util.Security.getHash("md5", sql);

		if(!sql.endsWith(";"))
			sql += ";";

		WCStatement statement = myConnection.createInternalStatement();

		statement.addBatch("DROP VIEW IF EXISTS " + viewName + ";");
		statement.addBatch("CREATE VIEW " + viewName + " AS " + sql);


		/* COLUMN META-FIELDS */
		StringBuilder sqlBuilder = new StringBuilder(200);
		sqlBuilder.append("SELECT ")
			.append("TABLE_SCHEMA, ")
			.append("TABLE_NAME, ")
			.append("COLUMN_NAME, ")
			.append("COLUMN_DEFAULT, ")

			.append("IF(IS_NULLABLE='YES', 'TRUE', 'FALSE') AS NULLABLE, ")
			.append("DATA_TYPE, ")
			.append("COLUMN_TYPE, ")

			.append("CHARACTER_MAXIMUM_LENGTH, ")
			.append("NUMERIC_PRECISION, ")
			.append("NUMERIC_SCALE, ")

			.append("COLLATION_NAME, ")

			.append("COLUMN_KEY, ")
			.append("IF(EXTRA LIKE '%auto_increment%', 'TRUE', 'FALSE') AS IS_AUTOINC ")


			.append("FROM INFORMATION_SCHEMA.COLUMNS ")
			.append("WHERE TABLE_SCHEMA LIKE '" + myConnection.getCatalog() + "' AND TABLE_NAME = '" + viewName + "' ")
			.append("ORDER BY TABLE_SCHEMA, TABLE_NAME;");

		statement.addBatch(sqlBuilder.toString());
		statement.addBatch("DROP VIEW " + viewName + ";");

		statement.executeBatch();

		// move to the first result
		statement.getMoreResults();
		statement.getMoreResults();


		if(statement.getMoreResults()){
			WCResultSet resultSet = statement.getResultSet();

			/* process the second ResultSet from INFORMATION_SCHEMA.COLUMNS */
			while(resultSet.next()){

				catalogName = resultSet.getString("TABLE_SCHEMA");

				tableName = resultSet.getString("TABLE_NAME");
				columnName = resultSet.getString("COLUMN_NAME");
				columnAlias = columnName;

				/* if the parser could split the query properly, get the real col name.
				 * Otherwise we use the alias
				 */
				if(columns.hasKey(columnAlias)){
					columnName = columns.getString(columnName);
				}

				columnDefault = resultSet.getString("COLUMN_DEFAULT");
				isNullable = resultSet.getBoolean("NULLABLE");
				sqlType = MySQLTypes.mysqlNameToMysqlType(resultSet.getString("DATA_TYPE"));

				try {
					maxLength = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
				} catch (Exception e) {
					maxLength = 0;
				}
				try {
					maxPrecision = resultSet.getInt("NUMERIC_PRECISION");
				} catch (Exception e) {
					maxPrecision = 0;
				}
				try {
					maxScale = resultSet.getInt("NUMERIC_SCALE");
				} catch (Exception e) {
					maxScale = 0;
				}

				collationName = resultSet.getString("COLLATION_NAME");

				isAutoIndex = resultSet.getBoolean("IS_AUTOINC");

				isUnsigned = isUnsigned(resultSet.getString("COLUMN_TYPE"));

				String colKey = resultSet.getString("COLUMN_KEY");
				isPrimaryKey = colKey.equalsIgnoreCase(keys[0]);
				isUniqueKey = colKey.equalsIgnoreCase(keys[1]);
				isIndex = colKey.equalsIgnoreCase(keys[2]);


				SQLField field = new SQLField(
						myConnection.getDbType(),
						catalogName,
						schemaName,
						tableName,
						columnName,
						columnAlias,
						columnDefault,
						collationName,

						sqlType,
						maxLength,
						maxPrecision,
						maxScale,

						isNullable,
						isAutoIndex,
						isUnsigned,
						isPrimaryKey,
						isUniqueKey,
						isIndex);

				fieldList.add(field);
			}
			resultSet.close();
		}
		statement.close();

		return fieldList.toArray(new SQLField[fieldList.size()]);
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
		WCStatement statement = myConnection.createInternalStatement();
		String sql = "DESCRIBE `" + tableName + "`;";
		WCResultSet resultSet = statement.executeQuery(sql);

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

	private boolean isUnsigned(final String typeName) {
		return typeName.toLowerCase().endsWith("unsigned");
	}

}
