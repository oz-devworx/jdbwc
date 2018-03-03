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
public class PgSQLMetaGeta {

	private transient WCConnection myConnection = null;
	private transient boolean USE_INFOSCHEMA = true;
	
	/**
	 * Constructs a new instance of this.
	 */
	protected PgSQLMetaGeta(WCConnection connection) {
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
		USE_INFOSCHEMA = myConnection.versionMeetsMinimum(7, 4, 0);
		
		if(USE_INFOSCHEMA){
			// 1 query per table
			rsMd = getRsMetaData2(rsMd, tableNames, columns, aliases);
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
		USE_INFOSCHEMA = myConnection.versionMeetsMinimum(7, 4, 0);
		
		for(int tIdx = 0; tIdx < tableNames.length(); tIdx++){
			if(USE_INFOSCHEMA){
				paramMdFields = getParamMetaData(tableNames.getKey(tIdx), columns, params);
			}else{
				paramMdFields = getParamMetaData(tableNames.getKey(tIdx), columns, params);
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
		
		final String[] keys = {"PRIMARY KEY","UNIQUE","MUL"};
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
	 * @param tableName
	 * @throws SQLException
	 */
	private SQLField[] getRsMetaData2(SQLField[] fieldSet, DataHandler tableNames, DataHandler columns, DataHandler aliases) throws SQLException{

		final String[] keys = {"PRIMARY KEY","UNIQUE","FOREIGN KEY"};
		
		/* these values are for the Field constructor */
		String columnName = "";
		String columnAlias = "";
		String pgSqlTypeName = "";
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
		
		
		String sqlCatalogKeys = "where (";
		String sqlTableKeys = "and (";
		
		String sqlRelIndex = "where (";
		
		String sqlCatalogInfo = "where (";
		String sqlTableInfo = "and (";
		
		String[] dbNTable;
		String sqlOr;
		int tlen = tableNames.length();
		
		for(int i = 0; i < tlen; i++){
			/* get metadata relating to the result fields. 
			 * Check for database names appended to the Table's name 
			 * and build the query conditional part accordingly
			 */
			sqlOr = (i>0) ? "or " : "";
			dbNTable = new String[2];
	
			if(tableNames.getKey(i).contains(".")){
				dbNTable = SQLUtils.removeBlanks(tableNames.getKey(i).trim().split("\\."));
			}else{
				dbNTable[0] = database;
				dbNTable[1] = tableNames.getKey(i).trim();
			}

			if(!sqlCatalogInfo.contains("'"+dbNTable[0]+"'")){
				sqlCatalogKeys += sqlOr + "kcu.table_catalog like '" + dbNTable[0] + "' ";
				sqlCatalogInfo += sqlOr + "table_catalog like '" + dbNTable[0] + "' ";
			}
			
			sqlTableKeys += sqlOr + "kcu.table_name like '" + dbNTable[1] + "' ";
			sqlRelIndex += sqlOr + "pc.relname='" + dbNTable[1] + "' ";
			sqlTableInfo += sqlOr + "table_name like '" + dbNTable[1] + "' ";
		}
		sqlCatalogKeys += ") ";
		sqlTableKeys += ") ";
		
		sqlRelIndex += ") ";
		
		sqlCatalogInfo += ") ";
		sqlTableInfo += ") ";
			
		/* keys */
		StringBuilder sqlBuilder = new StringBuilder("select ")
			.append("c.column_name, ")
			.append("tc.constraint_type ")
			
			.append("from information_schema.key_column_usage kcu, ")
			.append("information_schema.table_constraints tc, ")
			.append("information_schema.columns c ")
			
			.append(sqlCatalogKeys)
			.append(sqlTableKeys)
			.append("and kcu.constraint_name=tc.constraint_name ")
			.append("and kcu.column_name=c.column_name ")

			.append("and (tc.constraint_type = '").append(keys[0]).append("' ")
			.append("or tc.constraint_type = '").append(keys[1]).append("' ")
			.append("or tc.constraint_type = '").append(keys[2]).append("') ")

			.append("group by c.column_name, tc.constraint_type ")
			.append("order by c.column_name, tc.constraint_type;");
			
//		
//		sqlBuilder.append("\n");
//		

		
		/* indexes */
		sqlBuilder.append("select ")
//			.append("pgi.tablename, ")
			.append("pga.attname ")
			
			.append("from pg_catalog.pg_class pgc, ")
			.append("pg_catalog.pg_indexes pgi, ")
			.append("pg_catalog.pg_attribute pga ")
			
			.append("where pgc.relname=pgi.indexname ")
			.append("and pgc.oid=pga.attrelid ")
			
			.append("and pgc.oid in (")
			.append(" select pi.indexrelid ")
			.append(" from pg_catalog.pg_index pi, pg_catalog.pg_class pc ")
			.append(sqlRelIndex)
			.append(" and pc.oid=pi.indrelid ")
			.append(" and pi.indisunique = 'f' ")
			.append(" and pi.indisprimary = 'f' ")
			.append(") ")
			.append("order by pga.attname;");

//		
//		sqlBuilder.append("\n");
//		
		

		/* column info */
		sqlBuilder.append("select ")
			.append("'postgre' as engine, ")
			.append("'-1' as auto_increment, ")
			.append("getdatabaseencoding() as character_set_name, ")
			.append("getdatabaseencoding() as collation_name, ")
			.append("column_name, ")
			.append("table_name, ")
			.append("table_schema, ")
			.append("table_catalog, ")
			.append("column_default, ")
			.append("case when is_nullable like 'yes' then 'true' else 'false' end as nullable, ")
			
			.append("udt_name, ")
			.append("character_maximum_length, ")
			.append("numeric_precision, ")
			.append("numeric_scale, ")
			.append("character_octet_length, ")
			
			.append("case when column_default like 'nextval(%\\:\\:regclass)' then 'true' else 'false' end as extra, ")
			.append("null as column_comment ")

			.append("from information_schema.columns ")
			.append(sqlCatalogInfo)
			.append(sqlTableInfo)
			.append("order by table_catalog, table_name, udt_name;");

//		
//		System.err.println("sqlBuilder = " + sqlBuilder);
//

		
		Statement statement = myConnection.createStatement();
		if(statement.execute(sqlBuilder.toString())){
			
			ResultSet res = statement.getResultSet();
			ResultSet indexResults = null;
			ResultSet keyResults = null;
			ResultSet columnResults = null;
			
			/* count the resultSets */
			int resultsCnt = 1;
			while(statement.getMoreResults()){
				++resultsCnt;
			}
			
			
			/* move resultSet pointer to the first resultSet */
			if(statement.getMoreResults(0)){
				if(resultsCnt==1){
					columnResults = res;
					res.close();
//					System.err.println("SINGLE RESULTSET");
				}else if(resultsCnt==2){
					if(res.getFetchSize()==1){
						keyResults = res;
					}else{
						indexResults = res;
					}
					res.close();

					columnResults = statement.getResultSet();
//					System.err.println("DOUBLE RESULTSET");
				}else if(resultsCnt==3){
					keyResults = res;
					res.close();

					indexResults = statement.getResultSet();
					
					statement.getMoreResults();
					columnResults = statement.getResultSet();
//					System.err.println("TRIPLE RESULTSET");
				}
			}
			
			while(columnResults.next()){
				
				columnName = columnResults.getString("column_name");
				
				String tableName = tableNames.getKey(0);
				boolean hasWildcard = columns.hasElement(tableName, "*");

				/* allows fields for tableName that are specifically defined OR wildcard entries */
				if((columns.hasKey(tableName) && (columns.hasElement(tableName, columnName)) || hasWildcard)){
	
					engine = columnResults.getString("engine");
					autoindexValue = columnResults.getInt("auto_increment");
					
					database = columnResults.getString("table_catalog");
					schema = columnResults.getString("table_schema");
					columnAlias = "";
					
					if(!hasWildcard){
						synchronized(this){
							/* remove processed elements from DataHandlers */
							columnAlias = aliases.getString(columns.getIndexByElement(tableName, columnName));
							columns.removeByIndex(columns.getIndexByElement(tableName, columnName));
							aliases.removeByIndex(aliases.getIndexByElement(columnName, columnAlias));
//							System.err.println("1 column, 1 alias removed!");
							if(!columns.hasKey(tableName)){
								tableNames.removeByIndex(0);
//								System.err.println("1 table removed!");
							}
							
						}
					}
					tableName = columnResults.getString("table_name");

					collation = columnResults.getString("collation_name");
					charsetName = columnResults.getString("character_set_name");

					pgSqlTypeName = columnResults.getString("udt_name");
					String cmlStr = columnResults.getString("character_maximum_length");
					String npStr = columnResults.getString("numeric_precision");
					String colStr = columnResults.getString("character_octet_length");

					if(cmlStr.length()>0 && !cmlStr.equals("NULL")){
						pgSqlTypeName += "(" + cmlStr + ")";
					} else if(npStr.length()>0 && !npStr.equals("NULL")){
						pgSqlTypeName += "(" + npStr + "," + columnResults.getString("numeric_scale") + ")";
					} else if(colStr.length()>0 && !colStr.equals("NULL")){
						pgSqlTypeName += "(" + colStr + ")";
					}

					valueDefault = columnResults.getString("column_default");
					isAutoIndex = columnResults.getBoolean("extra");
					isNullable = columnResults.getBoolean("nullable");
					
					if(indexResults!=null){
						isIndex = false;
						indexResults.beforeFirst();
						while(indexResults.next()){
							if(columnName.equals(indexResults.getString("attname"))){
								isIndex = true;
								break;
							}
						}
					}
					if(keyResults!=null){
						isUniqueKey = false;
						isPrimaryKey = false;
						keyResults.beforeFirst();
						while(keyResults.next()){
							
//							System.err.println(keyResults.getString("column_name") + " = " + keyResults.getString("constraint_type"));
							if(columnName.equals(keyResults.getString("column_name"))){
								
								String keyType = keyResults.getString("constraint_type");
								
								if(keyType.equalsIgnoreCase(keys[0])){
									isPrimaryKey = true;
								}
								if(keyType.equalsIgnoreCase(keys[1]) || keyType.equalsIgnoreCase(keys[2])){
									isUniqueKey = true;
								}
							}
						}
					}
					
					
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
							pgSqlTypeName,
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
		statement.close();
		return fieldSet;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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

}
