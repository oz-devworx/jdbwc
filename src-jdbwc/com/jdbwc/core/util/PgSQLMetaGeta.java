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
import java.util.ArrayList;
import java.util.List;

import com.jdbwc.core.WCConnection;
import com.jdbwc.core.WCResultSet;
import com.jdbwc.core.WCStatement;
import com.ozdevworx.dtype.DataHandler;

/**
 * Gets meta data from PostgreSQL databases.
 *
 * @author Tim Gall
 * @version 2008-05-29
 * @version 2010-05-20
 */
public class PgSQLMetaGeta {

	private transient WCConnection myConnection = null;


	/**
	 * Constructs a new instance of this.
	 */
	protected PgSQLMetaGeta(WCConnection connection) {
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

		if(myConnection.versionMeetsMinimum(7, 4, 0)){
			return getRsMetaData2(sql, columns);
		}else{
			throw new SQLException("PostgreSQL versions less than 7.4.0 are not supported in this release.");
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

		if(myConnection.versionMeetsMinimum(7, 4, 0)){

			SQLField[] paramMdFields = new SQLField[0];
			SQLField[] currentFields = new SQLField[0];

			for(int tIdx = 0; tIdx < tableNames.length(); tIdx++){
				currentFields = getParamMetaData(tableNames.getKey(tIdx), columns, params);
				if(currentFields.length > 0){
					if(paramMdFields.length > 0)
						System.arraycopy(currentFields, 0, paramMdFields, paramMdFields.length, currentFields.length);
					else
						paramMdFields = currentFields;
				}
			}

			return paramMdFields;

		}else{
			throw new SQLException("PostgreSQL versions less than 7.4.0 are not supported in this release.");
		}
	}

	/**
	 * Gets ResultSetMetaData from the INFORMATION_SCHEMA database
	 * in a single query (per table).
	 *
	 * @param sql
	 * @param columns
	 * @return an array of SQLField Objects
	 * @throws SQLException
	 */
	private SQLField[] getRsMetaData2(String sql, DataHandler columns) throws SQLException{

		if(sql.isEmpty())
			throw new SQLException("Could not determine the resultsets query to get metadata for.");


		final String[] keys = {"PRIMARY KEY","UNIQUE","FOREIGN KEY"};

		List<SQLField> fieldList = new ArrayList<SQLField>();

		/* these values are for the Field constructor */
		String catalogName = "";
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
		 * This saves a monumental amount of work but still requires
		 * we manually produce the database-name/s and original field-name/s.
		 *
		 * Other than that, we simply retrieve all other relevant meta info from the view
		 * then delete it. This is handled in batches on the server end to save time.
		 *
		 * One downfall of this approach is that keys are not recorded in the view
		 * as they are in the original tables.
		 */

		// generate a name thats not likly to conflict
		// but is easy to locate using LIKE
		String viewName = "wc_rsmeta_" + com.jdbwc.util.Security.getHash("md5", sql);

		if(!sql.endsWith(";"))
			sql += ";";

		WCStatement statement = myConnection.createInternalStatement();

		statement.addBatch("drop view if exists " + viewName + ";");
		statement.addBatch("create view " + viewName + " as " + sql);


		/* COLUMN META-FIELDS */
		StringBuilder sqlBuilder = new StringBuilder(250);
		sqlBuilder.append("select ")
			.append("c.table_catalog, ")
			.append("c.table_schema, ")
			.append("c.table_name, ")
			.append("c.column_name, ")
			.append("c.column_default, ")
			.append("case when c.is_nullable like 'yes' then 'TRUE' else 'FALSE' end as nullable, ")
			.append("c.udt_name as data_type, ")
			.append("c.data_type as column_type, ")
			.append("c.character_maximum_length, ")
			.append("c.numeric_precision, ")
			.append("c.numeric_scale, ")
			.append("c.collation_name, ")
			.append("tc.constraint_type as column_key, ")
			.append("case when c.column_default like 'nextval(%\\:\\:regclass)' then 'YES' else 'NO' end as is_autoinc ")

			.append("from information_schema.columns c ")
			.append("left join information_schema.key_column_usage kcu using (table_catalog, table_schema, table_name, column_name) ")
			.append("left join information_schema.table_constraints tc using (table_catalog, table_schema, table_name, constraint_name) ")

			.append("where c.table_catalog like '" + myConnection.getCatalog() + "' and c.table_name = '" + viewName + "' ")
			.append("order by c.table_schema, c.table_name;");

//		System.err.println("drop view if exists " + viewName + ";");
//		System.err.println("create view " + viewName + " as " + sql);
//		System.err.println(sqlBuilder.toString());
//		System.err.println("RSMD 1");


		statement.addBatch(sqlBuilder.toString());
		statement.addBatch("drop view " + viewName + ";");

		statement.executeBatch();

		// move to the first result
		statement.getMoreResults();
		statement.getMoreResults();


		if(statement.getMoreResults()){
			WCResultSet resultSet = statement.getResultSet();

//			System.err.println("RSMD 2");

			/* process the ResultSet from INFORMATION_SCHEMA.COLUMNS */
			while(resultSet.next()){

				catalogName = resultSet.getString("table_catalog");
				schemaName = resultSet.getString("table_schema");

				tableName = resultSet.getString("table_name");
				columnName = resultSet.getString("column_name");
				columnAlias = columnName;

				/* if the parser could split the query properly, get the real col name.
				 * Otherwise we use the alias
				 */
				if(columns.hasKey(columnAlias)){
					columnName = columns.getString(columnName);
				}

				columnDefault = resultSet.getString("column_default");
				isNullable = resultSet.getBoolean("nullable");
				sqlType = PgSQLTypes.pgsqlNameToPgsqlType(resultSet.getString("data_type"));


//				System.err.println("data_type = " + resultSet.getString("data_type"));
//				System.err.println("sqlType = " + sqlType);


				try {
					maxLength = resultSet.getInt("character_maximum_length");
				} catch (Exception e) {
					maxLength = 0;
				}
				try {
					maxPrecision = resultSet.getInt("numeric_precision");
				} catch (Exception e) {
					maxPrecision = 0;
				}
				try {
					maxScale = resultSet.getInt("numeric_scale");
				} catch (Exception e) {
					maxScale = 0;
				}

				collationName = resultSet.getString("collation_name");

				isAutoIndex = resultSet.getBoolean("is_autoinc");

				isUnsigned = isUnsigned(resultSet.getString("column_type"));

				String colKey = resultSet.getString("column_key");
				isPrimaryKey = keys[0].equalsIgnoreCase(colKey);
				isUniqueKey = keys[1].equalsIgnoreCase(colKey);
				isIndex = keys[2].equalsIgnoreCase(colKey);


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

//				System.err.println(field.toString());

				fieldList.add(field);
			}
			resultSet.close();
		}
		statement.close();

		return fieldList.toArray(new SQLField[fieldList.size()]);
	}

//	/**
//	 * Gets ResultSetMetaData from the INFORMATION_SCHEMA database
//	 * in a single query (per table).
//	 *
//	 * @param tableName
//	 * @throws SQLException
//	 */
//	private SQLField[] getRsMetaData(SQLField[] fieldSet, DataHandler columns) throws SQLException{
//
//		final String[] keys = {"PRIMARY KEY","UNIQUE","FOREIGN KEY"};
//
//		/* these values are for the Field constructor */
//		String columnName = "";
//		String columnAlias = "";
//		String pgSqlTypeName = "";
//		String valueDefault = "";
//
//		boolean isAutoIndex = false;
//		boolean isNullable = false;
//
//		boolean isPrimaryKey = false;
//		boolean isUniqueKey = false;
//		boolean isIndex = false;
//
//		String database = myConnection.getDatabase();
//		String schema = "";
//		String charsetName = "";
//		String collation = "";
//		String engine = "";
//
////		int autoindexValue = 0;
//		int length = 0;
//
//
//		String sqlCatalogKeys = "where (";
//		String sqlTableKeys = "and (";
//
//		String sqlRelIndex = "where (";
//
//		String sqlCatalogInfo = "where (";
//		String sqlTableInfo = "and (";
//
//		String[] dbNTable;
//		String sqlOr;
//		int tlen = 1;//tableNames.length();
//
//		for(int i = 0; i < tlen; i++){
//			/* get metadata relating to the result fields.
//			 * Check for database names appended to the Table's name
//			 * and build the query conditional part accordingly
//			 */
//			sqlOr = (i>0) ? "or " : "";
//			dbNTable = new String[2];
//
////			if(tableNames.getKey(i).contains(".")){
////				dbNTable = SQLUtils.removeBlanks(tableNames.getKey(i).trim().split("\\."));
////			}else{
////				dbNTable[0] = database;
////				dbNTable[1] = tableNames.getKey(i).trim();
////			}
//
//			if(!sqlCatalogInfo.contains("'"+dbNTable[0]+"'")){
//				sqlCatalogKeys += sqlOr + "kcu.table_catalog like '" + dbNTable[0] + "' ";
//				sqlCatalogInfo += sqlOr + "table_catalog like '" + dbNTable[0] + "' ";
//			}
//
//			sqlTableKeys += sqlOr + "kcu.table_name like '" + dbNTable[1] + "' ";
//			sqlRelIndex += sqlOr + "pc.relname='" + dbNTable[1] + "' ";
//			sqlTableInfo += sqlOr + "table_name like '" + dbNTable[1] + "' ";
//		}
//		sqlCatalogKeys += ") ";
//		sqlTableKeys += ") ";
//
//		sqlRelIndex += ") ";
//
//		sqlCatalogInfo += ") ";
//		sqlTableInfo += ") ";
//
//		/* keys */
//		StringBuilder sqlBuilder = new StringBuilder("select ")
//			.append("c.column_name, ")
//			.append("tc.constraint_type ")
//
//			.append("from information_schema.key_column_usage kcu, ")
//			.append("information_schema.table_constraints tc, ")
//			.append("information_schema.columns c ")
//
//			.append(sqlCatalogKeys)
//			.append(sqlTableKeys)
//			.append("and kcu.constraint_name=tc.constraint_name ")
//			.append("and kcu.column_name=c.column_name ")
//
//			.append("and (tc.constraint_type = '").append(keys[0]).append("' ")
//			.append("or tc.constraint_type = '").append(keys[1]).append("' ")
//			.append("or tc.constraint_type = '").append(keys[2]).append("') ")
//
//			.append("group by c.column_name, tc.constraint_type ")
//			.append("order by c.column_name, tc.constraint_type;");
//
////
////		sqlBuilder.append("\n");
////
//
//
//		/* indexes */
//		sqlBuilder.append("select ")
////			.append("pgi.tablename, ")
//			.append("pga.attname ")
//
//			.append("from pg_catalog.pg_class pgc, ")
//			.append("pg_catalog.pg_indexes pgi, ")
//			.append("pg_catalog.pg_attribute pga ")
//
//			.append("where pgc.relname=pgi.indexname ")
//			.append("and pgc.oid=pga.attrelid ")
//
//			.append("and pgc.oid in (")
//			.append(" select pi.indexrelid ")
//			.append(" from pg_catalog.pg_index pi, pg_catalog.pg_class pc ")
//			.append(sqlRelIndex)
//			.append(" and pc.oid=pi.indrelid ")
//			.append(" and pi.indisunique = 'f' ")
//			.append(" and pi.indisprimary = 'f' ")
//			.append(") ")
//			.append("order by pga.attname;");
//
////
////		sqlBuilder.append("\n");
////
//
//
//		/* column info */
//		sqlBuilder.append("select ")
//			.append("'postgre' as engine, ")
////			.append("'-1' as auto_increment, ")
//			.append("getdatabaseencoding() as character_set_name, ")
//			.append("getdatabaseencoding() as collation_name, ")
//			.append("column_name, ")
//			.append("table_name, ")
//			.append("table_schema, ")
//			.append("table_catalog, ")
//			.append("column_default, ")
//			.append("case when is_nullable like 'yes' then 'true' else 'false' end as nullable, ")
//
//			.append("udt_name, ")
//			.append("character_maximum_length, ")
//			.append("numeric_precision, ")
//			.append("numeric_scale, ")
//			.append("character_octet_length, ")
//
//			.append("case when column_default like 'nextval(%\\:\\:regclass)' then 'true' else 'false' end as extra, ")
//			.append("null as column_comment ")
//
//			.append("from information_schema.columns ")
//			.append(sqlCatalogInfo)
//			.append(sqlTableInfo)
//			.append("order by table_catalog, table_name, udt_name;");
//
////
////		System.err.println("sqlBuilder = " + sqlBuilder);
////
//
//
//		Statement statement = myConnection.createStatement();
//		if(statement.execute(sqlBuilder.toString())){
//
//			ResultSet res = statement.getResultSet();
//			ResultSet indexResults = null;
//			ResultSet keyResults = null;
//			ResultSet columnResults = null;
//
//			/* count the resultSets */
//			int resultsCnt = 1;
//			while(statement.getMoreResults()){
//				++resultsCnt;
//			}
//
//
//			/* move resultSet pointer to the first resultSet */
//			if(statement.getMoreResults(0)){
//				if(resultsCnt==1){
//					columnResults = res;
//					res.close();
////					System.err.println("SINGLE RESULTSET");
//				}else if(resultsCnt==2){
//					if(res.getFetchSize()==1){
//						keyResults = res;
//					}else{
//						indexResults = res;
//					}
//					res.close();
//
//					columnResults = statement.getResultSet();
////					System.err.println("DOUBLE RESULTSET");
//				}else if(resultsCnt==3){
//					keyResults = res;
//					res.close();
//
//					indexResults = statement.getResultSet();
//
//					statement.getMoreResults();
//					columnResults = statement.getResultSet();
////					System.err.println("TRIPLE RESULTSET");
//				}
//			}
//
//			while(columnResults.next()){
//
//				columnName = columnResults.getString("column_name");
//
//				String tableName = "";//tableNames.getKey(0);
//				boolean hasWildcard = columns.hasElement(tableName, "*");
//
//				/* allows fields for tableName that are specifically defined OR wildcard entries */
//				if((columns.hasKey(tableName) && (columns.hasElement(tableName, columnName)) || hasWildcard)){
//
//					engine = columnResults.getString("engine");
////					autoindexValue = columnResults.getInt("auto_increment");
//
//					database = columnResults.getString("table_catalog");
//					schema = columnResults.getString("table_schema");
//					columnAlias = "";
//
////					if(!hasWildcard){
////						synchronized(this){
////							/* remove processed elements from DataHandlers */
////							columnAlias = aliases.getString(columns.getIndexByElement(tableName, columnName));
////							columns.removeByIndex(columns.getIndexByElement(tableName, columnName));
////							aliases.removeByIndex(aliases.getIndexByElement(columnName, columnAlias));
//////							System.err.println("1 column, 1 alias removed!");
////							if(!columns.hasKey(tableName)){
////								tableNames.removeByIndex(0);
//////								System.err.println("1 table removed!");
////							}
////
////						}
////					}
//					tableName = columnResults.getString("table_name");
//
//					collation = columnResults.getString("collation_name");
//					charsetName = columnResults.getString("character_set_name");
//
//					pgSqlTypeName = columnResults.getString("udt_name");
//					String cmlStr = columnResults.getString("character_maximum_length");
//					String npStr = columnResults.getString("numeric_precision");
//					String colStr = columnResults.getString("character_octet_length");
//
//					if(cmlStr.length()>0 && !cmlStr.equals("NULL")){
//						pgSqlTypeName += "(" + cmlStr + ")";
//					} else if(npStr.length()>0 && !npStr.equals("NULL")){
//						pgSqlTypeName += "(" + npStr + "," + columnResults.getString("numeric_scale") + ")";
//					} else if(colStr.length()>0 && !colStr.equals("NULL")){
//						pgSqlTypeName += "(" + colStr + ")";
//					}
//
//					valueDefault = columnResults.getString("column_default");
//					isAutoIndex = columnResults.getBoolean("extra");
//					isNullable = columnResults.getBoolean("nullable");
//
//					if(indexResults!=null){
//						isIndex = false;
//						indexResults.beforeFirst();
//						while(indexResults.next()){
//							if(columnName.equals(indexResults.getString("attname"))){
//								isIndex = true;
//								break;
//							}
//						}
//					}
//					if(keyResults!=null){
//						isUniqueKey = false;
//						isPrimaryKey = false;
//						keyResults.beforeFirst();
//						while(keyResults.next()){
//
////							System.err.println(keyResults.getString("column_name") + " = " + keyResults.getString("constraint_type"));
//							if(columnName.equals(keyResults.getString("column_name"))){
//
//								String keyType = keyResults.getString("constraint_type");
//
//								if(keyType.equalsIgnoreCase(keys[0])){
//									isPrimaryKey = true;
//								}
//								if(keyType.equalsIgnoreCase(keys[1]) || keyType.equalsIgnoreCase(keys[2])){
//									isUniqueKey = true;
//								}
//							}
//						}
//					}
//
//
//					SQLField field = new SQLField(
//							myConnection.getDbType(),
//							columnName,
//							columnAlias,
//							tableName,
//							database,
//							collation,
//							schema,
//							engine,
//							charsetName,
//							pgSqlTypeName,
//							valueDefault,
//
//							isAutoIndex,
//							isNullable,
//							isPrimaryKey,
//							isUniqueKey,
//							isIndex,
//
//							length);
//
//
//					fieldSet = SQLUtils.rebuildFieldSet(field, fieldSet);
//				}
//			}
//		}
//		statement.close();
//		return fieldSet;
//	}














	private SQLField[] getParamMetaData(String tableName, DataHandler columns, DataHandler params) throws SQLException{

		SQLField[] paramMdFields = new SQLField[0];
		final String nullable = "TRUE";
		final String MY_MODE_UNKNOWN = "UNKNOWN";

		/* these values are for the Field constructor */
		String fieldName = "";
		String typeName = "";
		boolean isNullable = false;
		String mode = MY_MODE_UNKNOWN;

		/* the next queries get fine metadata relating to the result fields. */
		Statement statement = myConnection.createStatement();
		String sql =
//			"SELECT column_name as Field," +
//			"         data_type as Type," +
//			"         is_nullable as isnull" + //YES, NO
//			"    FROM information_schema.columns" +
//			"   WHERE table_name = '" + tableName + "'";

		/* via pg_ tables */
			"SELECT a.attname AS Field," +
			"        t.typname AS Type," +
			"        a.attnotnull AS isnull" + //TRUE, FALSE
			"    FROM pg_class c," +
			"         pg_attribute a," +
			"         pg_type t" +
			"   WHERE c.relname = '" + tableName + "'" +
			"     AND a.attnum > 0" +
			"     AND a.attrelid = c.oid" +
			"     AND a.atttypid = t.oid;";

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
					isNullable = resultSet.getString("isnull").equalsIgnoreCase(nullable);
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
					isNullable = resultSet.getString("isnull").equalsIgnoreCase(nullable);
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
