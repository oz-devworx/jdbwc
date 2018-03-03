/* ********************************************************************
 * Copyright (C) 2008 Tim Gall (Oz-DevWorX)
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
package jdbwctest;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;

/**
 * This class is designed for JDBWC-1.0.0-3beta.<br />
 * You will need the jdbwc-lib folder (which includes the jdbwc.jar and its dependencies).<br />
 * If you dont have them, get a copy here:
 * <a href="http://jdbwc.sourceforge.net">http://jdbwc.sourceforge.net</a><br />
 * <br />
 * To run this class call its main method. Params will be ignored.<br />
 * <br />
 * This class contains testing methods to demonstrate basic JDBWC use.<br />
 * <br />
 * We are only using Strings for output in this demo but a JDBWC ResultSet
 * can currently handle outputing Objects, Strings, all number classes,
 * date related classes, booleans and bytes (although not byte arrays yet).<br />
 * There may also be a few ive missed.<br />
 * Most other data types are not fully implemented.<br />
 * The inline comments in this class's source-file explain more.<br />
 * <br />
 * <b>SUMMARY:</b><br />
 * 1) Create a database (server-side) and give it a user. Be sure to enter the database name in the class variable <code>databaseName</code>.<br />
 * 2) Insert the supplied sql to suit your database type (pg.sql OR my.sql). This will insert the testing tables.<br />
 * 3) Have a brief look at the test tables to verify they are ok. They are very basic.<br />
 * 4) Add the <code>jdbwc.jar</code> to your classpath.<br />
 * 5) Update the credentials with your details and URL in the <code>connect()</code> method in <code>jdbwctest.CoreTest.java</code><br />
 * 6) Compile and run this test class. Make sure you run the class via a console so you can see the output.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2010-04-11
 */
public class CoreTest {

	/** A <code>java.sql.Connection</code> */
	Connection connection = null;
	/** The name of the database you  are using for testing EG: <code>jdbwctest</code>, <code>username_jdbwctest</code>, etc. */
	String databaseName = "jdbwctest";//you can also set this in the connect method for convenience

	/**
	 * Contructor for this test class.<br />
	 * Runs all enabled test methods in this class.<br />
	 * All errors are acknowledged from here, all connection checking is done from here.<br />
	 * This saves having to write the same exception handling and checks for each method.
	 * In production conditions you would usually have exception handling in each method.
	 */
	protected CoreTest() {
		connection = connect();

		try {
			if(connection!=null){

				/* NOTES about DatabaseMetaData stuff.
				 *
				 * Almost (but not all) java.sql.DatabaseMetaData methods have been implemented
				 * for both MySQL and PostgreSQL.
				 * The JDBWC PostgreSQL implementation is a little bit behind
				 * in the information schema area until the next release.
				 *
				 * The next 2 test methods will run most of the
				 * currently implemented DatabaseMetaData methods.
				 *
				 * The method names make them fairly self explanatory.
				 * JDBWC breaks MetaData into 2 categories;
				 * 1) Data that can be compiled by the Driver (static stuff)
				 * 2) Data that requires input from the DB to compile
				 * <ul>
				 * <li>testDatabaseMetaData uses methods that DONT perform DB access</li>
				 * <li>testDatabaseMetaDataFromInfoSchema uses methods that ONLY perform DB access</li>
				 * </ul>
				 */
				testDatabaseMetaData();
				testDatabaseMetaDataFromInfoSchema();

				/* NOTES:
				 * The next 1 test method will run some of the JDBWC java.sql.Statement
				 * implementations to give you some idea of what works.
				 *
				 * The JDBWC java.sql.ResultSetMetaData implementation is demonstrated
				 * at a basic level in testStatement() and testPreparedStatement();
				 * The JDBWC java.sql.ResultSet implementation is used in all "test" methods.
				 *
				 * ResultSet can handle a range of simpler data types at the moment.
				 * It should be safe to use Strings, Objects, booleans, number classes
				 * and the date, time, timestamp classes.
				 *
				 * You should verify any date related results before treating them as
				 * correct. The date types were only recently
				 * added to JDBWC and havent had a huge amount of testing yet.
				 * Dates are not straight forward and because JDBWC
				 * primarily connects to remote network databases,
				 * timezones and regional formats become of major importance
				 * to maintain accuracy and coherency in the remote database.
				 */
				testStatement();

				/* NOTES:
				 * PreparedStatement isn't finished yet but will function
				 * for very simple stuff (emphasis on very).
				 * See the testing method below
				 * for a clearer explanation of what works now.
				 *
				 * You can run database specific queries via the Statement implementation
				 * to get around it.
				 * EG: setup some stored procedures on your database server
				 * and execute them like you would any other query string.
				 * The database servers sql sytax and rules will apply.
				 */
				testPreparedStatement();// basic functionality only


				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * <b>Valid Vendor IDs:</b><br />
	 * <pre>
	 * USE DEFAULT DB : jdbc:jdbwc://
	 * USE MYSQL DB   : jdbc:jdbwc:mysql//
	 * USE POSTGRESQL : jdbc:jdbwc:postgresql//
	 * MySql is the default type.</pre>
	 *
	 * If you want debugging ON, <br />
	 * see the inline description under OPTIONAL PARAMETERS in this method.<br />
	 * <br />
	 * <b>Connection Property keys:</b>
	 * <pre>
	 * url               (website url EG: https://somedomain.com/myfolder/
	 *                     [In this case, the "myfolder" folder
	 *                     at https://somedomain.com
	 *                     should contain the 'web' folder "jdbwc"])
	 * port              (server port number. https [usually 443], http [usually 80])
	 * user              (login username)
	 * password          (login password)
	 * databaseName      (Database name)
	 * databaseUser      (Database username)
	 * databasePassword  (Database password)
	 * </pre>
	 * OPTIONAL PARAMETERS:
	 * <pre>
	 * debug             OPTIONS: true, false (or not used. defaults to false)
	 * debugLogger       OPTIONS: SimpleLog, Jdk14Logger, Log4JLogger
	 * debugLevel        OPTIONS: [0-3]

	 * useDummyAgent     OPTIONS: true, false (or not used. defaults to false)
	 * </pre>
	 *
	 * @return An active java.sql.Connection
	 */
	private java.sql.Connection connect() {
		try {
			/* register the JDBWC Driver with the Java DriverManager */
			Class.forName("com.jdbwc.core.Driver");


			//remote
//			databaseName = "uname_jdbwctest";
//			final String wcDataBase = databaseName;
//			final String wcUser = "xxxxxxxxxxxxxxx";
//			final String wcPass = "xxxxxxxxxxxxxxx";
//
//			final int port = 443;
//			final String httUrlStr = "https://example.org/testing/";
//			final String user = "xxxxxxxxxxxxxxx";
//			final String pass = "xxxxxxxxxxxxxxx";

			//local
			databaseName = "uname_jdbwctest";
			final String wcDataBase = databaseName;
			final String wcUser = "xxxxxxxxxxxxxxx";
			final String wcPass = "xxxxxxxxxxxxxxx";

			final int port = 80;
			final String httUrlStr = "http://localhost/";
			final String user = "xxxxxxxxxxxxxxx";
			final String pass = "xxxxxxxxxxxxxxx";


			/* *********************************
		     * Starting a connection via a url *
		     * *********************************/
//			final String jdbwcUrlStr = "jdbc:jdbwc:postgresql//"
//			final String jdbwcUrlStr = "jdbc:jdbwc:mysql//"
//				+ httUrlStr
//				+ "?port=" + port
//				+ "&databaseName=" + wcDataBase
//				+ "&databaseUser=" + wcUser
//				+ "&databasePassword=" + wcPass;
//
//			/* initialise a new JDBC Connection using the JDBWC Driver package */
//			connection = DriverManager.getConnection(jdbwcUrlStr, user, pass);


		    /* ****************************************
		     * Using a Propeties object (recommended) *
		     * ****************************************/
//			final String jdbwcUrlStr = "jdbc:jdbwc:postgresql//";
			final String jdbwcUrlStr = "jdbc:jdbwc:mysql//";


		    Properties props = new Properties();
		    props.put("url", httUrlStr);
		    props.put("port", String.valueOf(port));
		    props.put("user", user);
		    props.put("password", pass);
		    props.put("databaseName", wcDataBase);
		    props.put("databaseUser", wcUser);
		    props.put("databasePassword", wcPass);

		    /* *****************************
		     * OPTIONAL PARAMETERS:
		     * *****************************/

		    /*
		     * This release only supports the SimpleLog option (out of the box).
		     * Next release will aim to support all options as standard.
		     * commons.logging has built in wrappers for
		     * SimpleLog, java.util.logging, Log4J, Avalon and more.
		     *
		     * At the moment, in order to implement java.util.logging, Log4J, Avalon, etc.
		     * use debug=false and see: http://hc.apache.org/httpclient-3.x/logging.html
		     */
		    props.put("debug", "true");//OPTIONS: true, false (or not used. defaults to false)
		    props.put("debugLogger", "SimpleLog");//OPTIONS: SimpleLog, Jdk14Logger, Log4JLogger
		    props.put("debugLevel", "0");//OPTIONS: [0-3] 0=informative,2=good for connection debugging

		    /* Dummy User-Agent (only required in rare situations).
		     * Only required if your host blocks the default Apache HttpClient User-Agent
		     */
		    props.put("useDummyAgent", "false");//OPTIONS: true, false (or not used. defaults to false)

		    connection = DriverManager.getConnection(jdbwcUrlStr, props);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return connection;
	}










	private void testDatabaseMetaData() throws SQLException, Exception{

		System.out.println();
		System.out.println("DatabaseMetaData methods:");
		System.out.println("=========================");
		System.out.println("=========================");

		DatabaseMetaData dbmd = connection.getMetaData();
		ResultSet results;
		int columnIndex;

		System.out.println("CALLED: getDriverMajorVersion() = " + dbmd.getDriverMajorVersion());
		System.out.println("CALLED: getDriverMinorVersion() = " + dbmd.getDriverMinorVersion());
		System.out.println("CALLED: getDriverName()         = " + dbmd.getDriverName());
		System.out.println("CALLED: getDriverVersion()      = " + dbmd.getDriverVersion());
		System.out.println("---");

		System.out.println("CALLED: getDatabaseMajorVersion()   = " + dbmd.getDatabaseMajorVersion());
		System.out.println("CALLED: getDatabaseMinorVersion()   = " + dbmd.getDatabaseMinorVersion());
		System.out.println("CALLED: getDatabaseProductName()    = " + dbmd.getDatabaseProductName());
		System.out.println("CALLED: getDatabaseProductVersion() = " + dbmd.getDatabaseProductVersion());
		System.out.println("---");

		System.out.println("CALLED: getJDBCMajorVersion() = " + dbmd.getJDBCMajorVersion());
		System.out.println("CALLED: getJDBCMinorVersion() = " + dbmd.getJDBCMinorVersion());
		System.out.println("---");

		System.out.println("CALLED: getCatalogSeparator()      = " + dbmd.getCatalogSeparator());
		System.out.println("CALLED: getCatalogTerm()           = " + dbmd.getCatalogTerm());
		System.out.println("CALLED: getProcedureTerm()         = " + dbmd.getProcedureTerm());
		System.out.println("CALLED: getExtraNameCharacters()   = " + dbmd.getExtraNameCharacters());
		System.out.println("CALLED: getIdentifierQuoteString() = " + dbmd.getIdentifierQuoteString());
		System.out.println("---");

		System.out.println("CALLED: getNumericFunctions() = " + dbmd.getNumericFunctions());
		System.out.println("CALLED: getStringFunctions()  = " + dbmd.getStringFunctions());
		System.out.println("CALLED: getSystemFunctions()  = " + dbmd.getSystemFunctions());
		System.out.println("---");



		results = dbmd.getTableTypes();
		System.out.println("CALLED: getTableTypes()");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getTableTypes " + columnIndex + ") TABLE_TYPE = " + results.getString("TABLE_TYPE"));

			columnIndex++;
		}
		System.out.println("---");
		results.close();



		results = dbmd.getTypeInfo();
		System.out.println("CALLED: getTypeInfo()");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getTypeInfo " + columnIndex + ") TYPE_NAME = " + results.getString("TYPE_NAME"));
			System.out.println("getTypeInfo " + columnIndex + ") DATA_TYPE = " + results.getString("DATA_TYPE"));
			System.out.println("getTypeInfo " + columnIndex + ") PRECISION = " + results.getString("PRECISION"));
			System.out.println("getTypeInfo " + columnIndex + ") LITERAL_PREFIX = " + results.getString("LITERAL_PREFIX"));
			System.out.println("getTypeInfo " + columnIndex + ") LITERAL_SUFFIX = " + results.getString("LITERAL_SUFFIX"));
			System.out.println("getTypeInfo " + columnIndex + ") CREATE_PARAMS = " + results.getString("CREATE_PARAMS"));
			System.out.println("getTypeInfo " + columnIndex + ") NULLABLE = " + results.getString("NULLABLE"));
			System.out.println("getTypeInfo " + columnIndex + ") CASE_SENSITIVE = " + results.getString("CASE_SENSITIVE"));
			System.out.println("getTypeInfo " + columnIndex + ") SEARCHABLE = " + results.getString("SEARCHABLE"));
			System.out.println("getTypeInfo " + columnIndex + ") UNSIGNED_ATTRIBUTE = " + results.getString("UNSIGNED_ATTRIBUTE"));
			System.out.println("getTypeInfo " + columnIndex + ") FIXED_PREC_SCALE = " + results.getString("FIXED_PREC_SCALE"));
			System.out.println("getTypeInfo " + columnIndex + ") AUTO_INCREMENT = " + results.getString("AUTO_INCREMENT"));
			System.out.println("getTypeInfo " + columnIndex + ") LOCAL_TYPE_NAME = " + results.getString("LOCAL_TYPE_NAME"));
			System.out.println("getTypeInfo " + columnIndex + ") MINIMUM_SCALE = " + results.getString("MINIMUM_SCALE"));
			System.out.println("getTypeInfo " + columnIndex + ") MAXIMUM_SCALE = " + results.getString("MAXIMUM_SCALE"));
			System.out.println("getTypeInfo " + columnIndex + ") SQL_DATA_TYPE = " + results.getString("SQL_DATA_TYPE"));
			System.out.println("getTypeInfo " + columnIndex + ") SQL_DATETIME_SUB = " + results.getString("SQL_DATETIME_SUB"));
			System.out.println("getTypeInfo " + columnIndex + ") NUM_PREC_RADIX = " + results.getString("NUM_PREC_RADIX"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();

	}



	private void testDatabaseMetaDataFromInfoSchema() throws SQLException, Exception{

		System.out.println();
		System.out.println("DatabaseMetaDataFromInfoSchema methods:");
		System.out.println("=======================================");
		System.out.println("=======================================");

		DatabaseMetaData dbmd = connection.getMetaData();
		ResultSet results;
		int columnIndex;


		results = dbmd.getProcedures(null, null, "sptest02");
		System.out.println("CALLED: getProcedures(null, null, \"sptest02\"); (RESULTS = " + results.getFetchSize() + ")");
		columnIndex = 1;
		if(results.getFetchSize() > 0){
			while(results.next()){
				System.out.println("getProcedures " + columnIndex + ") PROCEDURE_CAT = " + results.getString("PROCEDURE_CAT"));
				System.out.println("getProcedures " + columnIndex + ") PROCEDURE_SCHEM = " + results.getString("PROCEDURE_SCHEM"));
				System.out.println("getProcedures " + columnIndex + ") PROCEDURE_NAME = " + results.getString("PROCEDURE_NAME"));
				System.out.println("getProcedures " + columnIndex + ") REMARKS = " + results.getString("REMARKS"));
				System.out.println("getProcedures " + columnIndex + ") PROCEDURE_TYPE = " + results.getString("PROCEDURE_TYPE"));
				System.out.println("getProcedures " + columnIndex + ") SPECIFIC_NAME = " + results.getString("SPECIFIC_NAME"));

				System.out.println("---");
				columnIndex++;
			}
		}
		results.close();



		results = dbmd.getCatalogs();
		System.out.println("CALLED: getCatalogs()");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getCatalogs() " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumnPrivileges(null, null, "test02", "valkey");
		System.out.println("CALLED: getColumnPrivileges(null, null, \"test02\", \"valkey\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTOR = " + results.getString("GRANTOR"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTEE = " + results.getString("GRANTEE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") PRIVILEGE = " + results.getString("PRIVILEGE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") IS_GRANTABLE = " + results.getString("IS_GRANTABLE"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumns(null, null, "test02", "valkey");
		System.out.println("CALLED: getColumns(null, null, \"test02\", \"valkey\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumns " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumns " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumns " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumns " + columnIndex + ") DATA_TYPE = " + results.getString("DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") TYPE_NAME = " + results.getString("TYPE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_SIZE = " + results.getString("COLUMN_SIZE"));
			System.out.println("getColumns " + columnIndex + ") BUFFER_LENGTH = " + results.getString("BUFFER_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") DECIMAL_DIGITS = " + results.getString("DECIMAL_DIGITS"));
			System.out.println("getColumns " + columnIndex + ") NUM_PREC_RADIX = " + results.getString("NUM_PREC_RADIX"));
			System.out.println("getColumns " + columnIndex + ") NULLABLE = " + results.getString("NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") REMARKS = " + results.getString("REMARKS"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_DEF = " + results.getString("COLUMN_DEF"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATA_TYPE = " + results.getString("SQL_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATETIME_SUB = " + results.getString("SQL_DATETIME_SUB"));
			System.out.println("getColumns " + columnIndex + ") CHAR_OCTET_LENGTH = " + results.getString("CHAR_OCTET_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") ORDINAL_POSITION = " + results.getString("ORDINAL_POSITION"));
			System.out.println("getColumns " + columnIndex + ") IS_NULLABLE = " + results.getString("IS_NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_CATLOG = " + results.getString("SCOPE_CATLOG"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_SCHEMA = " + results.getString("SCOPE_SCHEMA"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_TABLE = " + results.getString("SCOPE_TABLE"));
			System.out.println("getColumns " + columnIndex + ") SOURCE_DATA_TYPE = " + results.getString("SOURCE_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") IS_AUTOINCREMENT = " + results.getString("IS_AUTOINCREMENT"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumnPrivileges(null, null, "test02", "test02_id");
		System.out.println("CALLED: getColumnPrivileges(null, null, \"test02\", \"test02_id\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTOR = " + results.getString("GRANTOR"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTEE = " + results.getString("GRANTEE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") PRIVILEGE = " + results.getString("PRIVILEGE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") IS_GRANTABLE = " + results.getString("IS_GRANTABLE"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumns(null, null, "test02", "test02_id");
		System.out.println("CALLED: getColumns(null, null, \"test02\", \"test02_id\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumns " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumns " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumns " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumns " + columnIndex + ") DATA_TYPE = " + results.getString("DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") TYPE_NAME = " + results.getString("TYPE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_SIZE = " + results.getString("COLUMN_SIZE"));
			System.out.println("getColumns " + columnIndex + ") BUFFER_LENGTH = " + results.getString("BUFFER_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") DECIMAL_DIGITS = " + results.getString("DECIMAL_DIGITS"));
			System.out.println("getColumns " + columnIndex + ") NUM_PREC_RADIX = " + results.getString("NUM_PREC_RADIX"));
			System.out.println("getColumns " + columnIndex + ") NULLABLE = " + results.getString("NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") REMARKS = " + results.getString("REMARKS"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_DEF = " + results.getString("COLUMN_DEF"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATA_TYPE = " + results.getString("SQL_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATETIME_SUB = " + results.getString("SQL_DATETIME_SUB"));
			System.out.println("getColumns " + columnIndex + ") CHAR_OCTET_LENGTH = " + results.getString("CHAR_OCTET_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") ORDINAL_POSITION = " + results.getString("ORDINAL_POSITION"));
			System.out.println("getColumns " + columnIndex + ") IS_NULLABLE = " + results.getString("IS_NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_CATLOG = " + results.getString("SCOPE_CATLOG"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_SCHEMA = " + results.getString("SCOPE_SCHEMA"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_TABLE = " + results.getString("SCOPE_TABLE"));
			System.out.println("getColumns " + columnIndex + ") SOURCE_DATA_TYPE = " + results.getString("SOURCE_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") IS_AUTOINCREMENT = " + results.getString("IS_AUTOINCREMENT"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumnPrivileges(null, null, "test01", "value");
		System.out.println("CALLED: getColumnPrivileges(null, null, \"test01\", \"value\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumnPrivileges " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTOR = " + results.getString("GRANTOR"));
			System.out.println("getColumnPrivileges " + columnIndex + ") GRANTEE = " + results.getString("GRANTEE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") PRIVILEGE = " + results.getString("PRIVILEGE"));
			System.out.println("getColumnPrivileges " + columnIndex + ") IS_GRANTABLE = " + results.getString("IS_GRANTABLE"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getColumns(null, null, "test01", "value");
		System.out.println("CALLED: getColumns(null, null, \"test01\", \"value\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getColumns " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getColumns " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getColumns " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getColumns " + columnIndex + ") DATA_TYPE = " + results.getString("DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") TYPE_NAME = " + results.getString("TYPE_NAME"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_SIZE = " + results.getString("COLUMN_SIZE"));
			System.out.println("getColumns " + columnIndex + ") BUFFER_LENGTH = " + results.getString("BUFFER_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") DECIMAL_DIGITS = " + results.getString("DECIMAL_DIGITS"));
			System.out.println("getColumns " + columnIndex + ") NUM_PREC_RADIX = " + results.getString("NUM_PREC_RADIX"));
			System.out.println("getColumns " + columnIndex + ") NULLABLE = " + results.getString("NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") REMARKS = " + results.getString("REMARKS"));
			System.out.println("getColumns " + columnIndex + ") COLUMN_DEF = " + results.getString("COLUMN_DEF"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATA_TYPE = " + results.getString("SQL_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") SQL_DATETIME_SUB = " + results.getString("SQL_DATETIME_SUB"));
			System.out.println("getColumns " + columnIndex + ") CHAR_OCTET_LENGTH = " + results.getString("CHAR_OCTET_LENGTH"));
			System.out.println("getColumns " + columnIndex + ") ORDINAL_POSITION = " + results.getString("ORDINAL_POSITION"));
			System.out.println("getColumns " + columnIndex + ") IS_NULLABLE = " + results.getString("IS_NULLABLE"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_CATLOG = " + results.getString("SCOPE_CATLOG"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_SCHEMA = " + results.getString("SCOPE_SCHEMA"));
			System.out.println("getColumns " + columnIndex + ") SCOPE_TABLE = " + results.getString("SCOPE_TABLE"));
			System.out.println("getColumns " + columnIndex + ") SOURCE_DATA_TYPE = " + results.getString("SOURCE_DATA_TYPE"));
			System.out.println("getColumns " + columnIndex + ") IS_AUTOINCREMENT = " + results.getString("IS_AUTOINCREMENT"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getTablePrivileges(null, null, "columns");
		System.out.println("CALLED: getTablePrivileges(null, null, \"test\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getTablePrivileges " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getTablePrivileges " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getTablePrivileges " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getTablePrivileges " + columnIndex + ") GRANTOR = " + results.getString("GRANTOR"));
			System.out.println("getTablePrivileges " + columnIndex + ") GRANTEE = " + results.getString("GRANTEE"));
			System.out.println("getTablePrivileges " + columnIndex + ") PRIVILEGE = " + results.getString("PRIVILEGE"));
			System.out.println("getTablePrivileges " + columnIndex + ") IS_GRANTABLE = " + results.getString("IS_GRANTABLE"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getTables(databaseName, null, "test", null);
		System.out.println("CALLED: getTables(\""+databaseName+"\", null, \"test\", null)");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getTables " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getTables " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getTables " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getTables " + columnIndex + ") TABLE_TYPE = " + results.getString("TABLE_TYPE"));
			System.out.println("getTables " + columnIndex + ") REMARKS = " + results.getString("REMARKS"));
			System.out.println("getTables " + columnIndex + ") TYPE_CAT = " + results.getString("TYPE_CAT"));
			System.out.println("getTables " + columnIndex + ") TYPE_SCHEM = " + results.getString("TYPE_SCHEM"));
			System.out.println("getTables " + columnIndex + ") TYPE_NAME = " + results.getString("TYPE_NAME"));
			System.out.println("getTables " + columnIndex + ") SELF_REFERENCING_COL_NAME = " + results.getString("SELF_REFERENCING_COL_NAME"));
			System.out.println("getTables " + columnIndex + ") REF_GENERATION = " + results.getString("REF_GENERATION"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getPrimaryKeys(null, null, "test01");
		System.out.println("CALLED: getPrimaryKeys(null, null, \"test01\")");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getPrimaryKeys " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getPrimaryKeys " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getPrimaryKeys " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getPrimaryKeys " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getPrimaryKeys " + columnIndex + ") KEY_SEQ = " + results.getString("KEY_SEQ"));
			System.out.println("getPrimaryKeys " + columnIndex + ") PK_NAME = " + results.getString("PK_NAME"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getSchemas();
		System.out.println("CALLED: getSchemas()");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getSchemas " + columnIndex + ") TABLE_CATALOG = " + results.getString("TABLE_CATALOG"));
			System.out.println("getSchemas " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getSchemas(databaseName, null);
		System.out.println("CALLED: getSchemas(\""+databaseName+"\", null)");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getSchemas " + columnIndex + ") TABLE_CATALOG = " + results.getString("TABLE_CATALOG"));
			System.out.println("getSchemas " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = dbmd.getIndexInfo(null, databaseName, "test01", true, true);
		System.out.println("CALLED: getIndexInfo(null, \""+databaseName+"\", \"test01\", true, true)");
		columnIndex = 1;
		while(results.next()){
			System.out.println("getIndexInfo " + columnIndex + ") TABLE_CAT = " + results.getString("TABLE_CAT"));
			System.out.println("getIndexInfo " + columnIndex + ") TABLE_SCHEM = " + results.getString("TABLE_SCHEM"));
			System.out.println("getIndexInfo " + columnIndex + ") TABLE_NAME = " + results.getString("TABLE_NAME"));
			System.out.println("getIndexInfo " + columnIndex + ") NON_UNIQUE = " + results.getString("NON_UNIQUE"));
			System.out.println("getIndexInfo " + columnIndex + ") INDEX_QUALIFIER = " + results.getString("INDEX_QUALIFIER"));
			System.out.println("getIndexInfo " + columnIndex + ") INDEX_NAME = " + results.getString("INDEX_NAME"));
			System.out.println("getIndexInfo " + columnIndex + ") TYPE = " + results.getString("TYPE"));
			System.out.println("getIndexInfo " + columnIndex + ") ORDINAL_POSITION = " + results.getString("ORDINAL_POSITION"));
			System.out.println("getIndexInfo " + columnIndex + ") COLUMN_NAME = " + results.getString("COLUMN_NAME"));
			System.out.println("getIndexInfo " + columnIndex + ") ASC_OR_DESC = " + results.getString("ASC_OR_DESC"));
			System.out.println("getIndexInfo " + columnIndex + ") CARDINALITY = " + results.getString("CARDINALITY"));
			System.out.println("getIndexInfo " + columnIndex + ") PAGES = " + results.getString("PAGES"));
			System.out.println("getIndexInfo " + columnIndex + ") FILTER_CONDITION = " + results.getString("FILTER_CONDITION"));

			System.out.println("---");
			columnIndex++;
		}
		results.close();


		/* Commented out due to limitations in the PostgreSQL implementation.
		 * MySQL should be OK with getImportedKeys and getExportedKeys
		 */
//		results = dbmd.getImportedKeys(null, databaseName, "test02");
//		System.out.println("CALLED: getImportedKeys(null, \""+databaseName+"\", \"test02\")");
//		columnIndex = 1;
//		while(results.next()){
//			System.out.println("getImportedKeys " + columnIndex + ") PKTABLE_CAT = " + results.getString("PKTABLE_CAT"));
//			System.out.println("getImportedKeys " + columnIndex + ") PKTABLE_SCHEM = " + results.getString("PKTABLE_SCHEM"));
//			System.out.println("getImportedKeys " + columnIndex + ") PKTABLE_NAME = " + results.getString("PKTABLE_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") PKCOLUMN_NAME = " + results.getString("PKCOLUMN_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") FKTABLE_CAT = " + results.getString("FKTABLE_CAT"));
//			System.out.println("getImportedKeys " + columnIndex + ") FKTABLE_SCHEM = " + results.getString("FKTABLE_SCHEM"));
//			System.out.println("getImportedKeys " + columnIndex + ") FKTABLE_NAME = " + results.getString("FKTABLE_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") FKCOLUMN_NAME = " + results.getString("FKCOLUMN_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") KEY_SEQ = " + results.getString("KEY_SEQ"));
//			System.out.println("getImportedKeys " + columnIndex + ") UPDATE_RULE = " + results.getString("UPDATE_RULE"));
//			System.out.println("getImportedKeys " + columnIndex + ") DELETE_RULE = " + results.getString("DELETE_RULE"));
//			System.out.println("getImportedKeys " + columnIndex + ") FK_NAME = " + results.getString("FK_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") PK_NAME = " + results.getString("PK_NAME"));
//			System.out.println("getImportedKeys " + columnIndex + ") DEFERRABILITY = " + results.getString("DEFERRABILITY"));
//
//			System.out.println("---");
//			columnIndex++;
//		}
//		results.close();
//
//
//
//		results = dbmd.getExportedKeys(null, databaseName, "test02");
//		System.out.println("CALLED: getExportedKeys(null, \""+databaseName+"\", \"test02\")");
//		columnIndex = 1;
//		while(results.next()){
//			System.out.println("getExportedKeys " + columnIndex + ") PKTABLE_CAT = " + results.getString("PKTABLE_CAT"));
//			System.out.println("getExportedKeys " + columnIndex + ") PKTABLE_SCHEM = " + results.getString("PKTABLE_SCHEM"));
//			System.out.println("getExportedKeys " + columnIndex + ") PKTABLE_NAME = " + results.getString("PKTABLE_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") PKCOLUMN_NAME = " + results.getString("PKCOLUMN_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") FKTABLE_CAT = " + results.getString("FKTABLE_CAT"));
//			System.out.println("getExportedKeys " + columnIndex + ") FKTABLE_SCHEM = " + results.getString("FKTABLE_SCHEM"));
//			System.out.println("getExportedKeys " + columnIndex + ") FKTABLE_NAME = " + results.getString("FKTABLE_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") FKCOLUMN_NAME = " + results.getString("FKCOLUMN_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") KEY_SEQ = " + results.getString("KEY_SEQ"));
//			System.out.println("getExportedKeys " + columnIndex + ") UPDATE_RULE = " + results.getString("UPDATE_RULE"));
//			System.out.println("getExportedKeys " + columnIndex + ") DELETE_RULE = " + results.getString("DELETE_RULE"));
//			System.out.println("getExportedKeys " + columnIndex + ") FK_NAME = " + results.getString("FK_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") PK_NAME = " + results.getString("PK_NAME"));
//			System.out.println("getExportedKeys " + columnIndex + ") DEFERRABILITY = " + results.getString("DEFERRABILITY"));
//
//			System.out.println("---");
//			columnIndex++;
//		}
//		results.close();




	}




	private void testStatement() throws SQLException, Exception{

		System.out.println();
		System.out.println("Statement methods:");
		System.out.println("==================");
		System.out.println("==================");

		boolean executed = false;
		Statement statement = connection.createStatement();
		ResultSet results = null;
		ResultSetMetaData rsMetaData = null;
		int columnIndex = 1;

		/* cleanup first incase an exception was triggered
		 * when this method was last run
		 */
		statement.execute("DELETE FROM test01;");
		statement.execute("DELETE FROM test02;");


		String sql1 = "INSERT INTO test01 (valkey, expiry, value) VALUES ('key01', '20081113', 'Insert value 1'), ('key02', '20081114', 'value 2');";
		executed = statement.execute(sql1);
		System.out.println("INSERT 1: Statement.execute("+sql1+") has ResultSets = " + executed);

		String sql2 = "INSERT INTO test01 (valkey, expiry, value) VALUES ('key03', '20081113', 'Insert value 3'), ('key04', '20081114', 'value 4');";
		executed = statement.execute(sql2);
		System.out.println("INSERT 2: Statement.execute("+sql2+") has ResultSets = " + executed);

		String sql3 = "INSERT INTO test01 (valkey, expiry, value) VALUES ('key05', '20081113', 'Insert value 5'), ('key06', '20081114', 'value 6');";
		executed = statement.execute(sql3);
		System.out.println("INSERT 3: Statement.execute("+sql3+") has ResultSets = " + executed);

		String sql4 = "INSERT INTO test01 (valkey, expiry, value) VALUES ('key07', '20081113', 'Insert value 7'), ('key08', '20081114', 'value 8');";
		executed = statement.execute(sql4);
		System.out.println("INSERT 4: Statement.execute("+sql4+") has ResultSets = " + executed);

		String sql5 = "UPDATE test01 SET expiry='20081213', value='new value from update 05' WHERE valkey LIKE 'key05';";
		executed = statement.execute(sql5);
		System.out.println("UPDATE 1: Statement.execute("+sql5+") has ResultSets = " + executed);


		String sqla1 = "INSERT INTO test02 (valkey, accessed) VALUES ('key01', '20081110'), ('key02', '20081111');";
		executed = statement.execute(sqla1);
		System.out.println("INSERT 5: Statement.execute("+sqla1+") has ResultSets = " + executed);

		String sqla2 = "INSERT INTO test02 (valkey, accessed) VALUES ('key03', '20081112'), ('key04', '20081113');";
		executed = statement.execute(sqla2);
		System.out.println("INSERT 6: Statement.execute("+sqla2+") has ResultSets = " + executed);

		String sqla3 = "INSERT INTO test02 (valkey, accessed) VALUES ('key05', '20081114'), ('key06', '20081115'), ('key07', '20081116');";
		executed = statement.execute(sqla3);
		System.out.println("INSERT 7: Statement.execute("+sqla3+") has ResultSets = " + executed);


//		String sql6 = "DELETE FROM test01 WHERE valkey LIKE 'key02' OR valkey LIKE 'key04' OR valkey LIKE 'key07' OR valkey LIKE 'key08';";
//		executed = statement.execute(sql6);
//		System.out.println("DELETE 1: Statement.execute("+sql6+") has ResultSets = " + executed);
//
//		String sql7 = "-- this is a comment\nSELECT t.myidx,\n\n\t t.valkey, t.expiry AS exp, t.value FROM test01 t;";
		String sql7 =
			"-- This is a comment which will be discarded by the Driver before sending to help save bandwidth.\n"+
			"# The query contains some newlines and tabs, column aliases, a JOIN etc.\n" +
			"# to see how our ResultSetMetaData implementation handles them.\n"+
			"-- The rest of the query will be handled by the sql engine of your choice with minimal interference in between,\n" +
			"-- so the syntax should be written appropriately for the database your connecting to\n" +
			"#  or universally (EG: traditional SQL)\n"+
			"#\n"+
			"SELECT t1.myidx, t1.valkey, t1.value, t1.expiry as sumTotal, t2.accessed as acd, t2.test02_id AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey);";


		results = statement.executeQuery(sql7);
		System.out.println("Statement.executeQuery("+sql7+") contains " + results.getFetchSize() + " row/s");


		rsMetaData = results.getMetaData();
		System.out.println("-----");

		columnIndex = 1;
		while(results.next()){

			for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
				System.out.println(columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(i));
			}

			System.out.println("---");
			columnIndex++;
		}
		results.close();


		String sql8 = "SELECT DISTINCT t1.myidx, t1.valkey, t1.value, MAX(t1.expiry) as sumTotal, t2.accessed as acd, t2.test02_id AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) GROUP BY t1.valkey, t1.expiry, t1.myidx, t1.value, t2.accessed, t2.test02_id;";
		statement.addBatch(sql8);
		System.out.println("Entry 1 added to batch.");

		String sql9 = "SELECT t.myidx, t.valkey, t.expiry AS exp, t.value FROM test01 t WHERE 1=1;";
		statement.addBatch(sql9);
		System.out.println("Entry 2 added to batch.");

		statement.addBatch(sql7);
		System.out.println("Entry 3 added to batch.");

		statement.addBatch("SELECT * FROM test01;");
		System.out.println("Entry 4 added to batch.");

		int[] batchResults = statement.executeBatch();
		System.out.println("Statement.executeBatch()");
		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		while(statement.getMoreResults()){
			columnIndex = 1;

			results = statement.getResultSet();

			rsMetaData = results.getMetaData();
			while(results.next()){

				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("executeBatch " + columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(i));
				}

				System.out.println("---");
				columnIndex++;
			}
		}
		results.close();

//		//This bit is for MySQL Only.
//		String procSql =
//			"DELIMITER $$;\n" +
//
//			"DROP FUNCTION IF EXISTS sptest01$$\n" +
//			"CREATE OR REPLACE FUNCTION sptest01(IN rowKey varchar(32), IN expires INT(11), IN rowValue TEXT, OUT lastId INT)\n" +
//			"BEGIN\n" +
//			"	INSERT INTO test01 (valkey, expiry, value) VALUES (rowKey, expires, rowValue);\n" +
//			"	SET lastId=LAST_INSERT_ID();\n" +
//			"END$$\n" +
//
//			"DROP FUNCTION IF EXISTS sptest02$$\n" +
//			"CREATE OR REPLACE FUNCTION sptest02(IN rowKey varchar(32), IN expires INT(11), IN rowValue TEXT, OUT lastId INT)\n" +
//			"BEGIN\n" +
//			"	INSERT INTO test01 (valkey, expiry, value) VALUES (rowKey, expires, rowValue);\n" +
//			"	SET lastId=LAST_INSERT_ID();\n" +
//			"END$$\n" +
//
//			"delimiter ;$$\n";
//
//		executed = statement.execute(procSql);
//		System.out.println("CREATE FUNCTION'S has ResultSets = " + executed);



		results = statement.executeQuery("SELECT * FROM test01;");
		rsMetaData = results.getMetaData();
		columnIndex = 1;
		while(results.next()){

			for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
				System.out.println("a) executeQuery " + columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(rsMetaData.getColumnLabel(i)));
			}

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = statement.executeQuery("SELECT * FROM test02;");
		rsMetaData = results.getMetaData();
		columnIndex = 1;
		while(results.next()){

			for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
				System.out.println("b) executeQuery " + columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(rsMetaData.getColumnLabel(i)));
			}

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = statement.executeQuery("SELECT * FROM test01 LEFT JOIN test02 USING(valkey);");
		rsMetaData = results.getMetaData();
		columnIndex = 1;
		while(results.next()){

			for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
				System.out.println("ab1) executeQuery " + columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(rsMetaData.getColumnLabel(i)));
			}

			System.out.println("---");
			columnIndex++;
		}
		results.close();



		results = statement.executeQuery("SELECT t1.myidx, t1.expiry, t1.value, t1.valkey as vk1, t2.accessed, t2.test02_id, t2.valkey AS vk2 FROM test01 t1, test02 t2 WHERE t1.valkey=t2.valkey;");
		rsMetaData = results.getMetaData();
		columnIndex = 1;
		while(results.next()){

			for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
				System.out.println("ab2) executeQuery " + columnIndex + ") " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") = " + results.getString(i));
			}

			System.out.println("---");
			columnIndex++;
		}
		results.close();


//		String sql999 = "DELETE FROM test01;";
//		executed = statement.execute(sql999);
//		System.out.println("DELETE all executed = " + executed);

		statement.close();
	}



	/**
	 *
	 *
	 */
	private void testPreparedStatement() throws SQLException, Exception{

		System.out.println();
		System.out.println("PreparedStatement methods:");
		System.out.println("==========================");
		System.out.println("Using batched PreparedStatement's, ParameterMetadata and ResultSetMetaData");
		System.out.println("==========================");
		System.out.println("==========================");

		int[] batchResults = null;
		ResultSet results = null;
		ResultSetMetaData rsMetaData = null;
		PreparedStatement prepStmnt = null;
		ParameterMetaData pmd = null;


		String procStmntA = "SELECT * FROM test01 WHERE valkey LIKE ? AND myidx > ?;";

		String procStmntB = "SELECT myidx, valkey, expiry FROM test01 WHERE valkey LIKE ? AND myidx > ?;";
//		String procStmntB = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE 'key05' AND t2.`test02_id`>1 ORDER BY t1.expiry;";

//		String procStmntB = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";
		String procStmntG = "DELETE FROM test01 WHERE valkey LIKE ? AND expiry > ?;";
//		String procStmntH = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";
//		String procStmntI = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";
//		String procStmntK = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";

//		String procStmntK = "INSERT INTO test01 (valkey, expiry, value) VALUES (?, ?, ?);";
//		String procStmntL = "INSERT INTO test01 (valkey, expiry, value) VALUES (?, 20071228, ?);";
		String procStmntM = "INSERT INTO test02 (valkey, accessed) VALUES (?, ?);";
		String procStmntN = "INSERT INTO test02 (valkey, accessed) VALUES (?, ?);";
//		String procStmntO = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";

		String procStmntP  = "UPDATE test01 SET valkey=?, expiry=?, value=? WHERE valkey LIKE ? AND myidx > ?;";
//		String procStmntQ = "UPDATE test02 (valkey, expiry, value) VALUES (?, ?, ?);";
//		String procStmntR = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";
//		String procStmntS = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";
//		String procStmntT = "SELECT t1.myidx, t1.valkey, t1.expiry AS exp, t1.value, t2.accessed as acd, t2.`test02_id` AS id FROM test01 t1 LEFT JOIN test02 t2 USING(valkey) WHERE t1.`valkey` LIKE ? AND t2.`test02_id`>? ORDER BY t1.expiry;";


		System.out.println("QUERY: " + procStmntA);
		prepStmnt = connection.prepareStatement(procStmntA);
		pmd = prepStmnt.getParameterMetaData();


		prepStmnt.setString(1, "key03");
		prepStmnt.setLong(2, 5);
		prepStmnt.addBatch();


		prepStmnt.setString(1, "key04");
		prepStmnt.setLong(2, 0);
		prepStmnt.addBatch();

		System.out.println("RETURNED FROM: prepStmnt.executeBatch()");
		batchResults = prepStmnt.executeBatch();
		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		int columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");


		/* ***********************************************
		 *************************************************
		 *************************************************/

		System.out.println("QUERY: " + procStmntB);
		prepStmnt = connection.prepareStatement(procStmntB);
		pmd = prepStmnt.getParameterMetaData();

		prepStmnt.setString(1, "key10");
		prepStmnt.setLong(2, 5);
		prepStmnt.addBatch();

		prepStmnt.setString(1, "key01");
		prepStmnt.setLong(2, 0);
		prepStmnt.addBatch();

		batchResults = prepStmnt.executeBatch();

		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");

		/* ***********************************************
		 *************************************************
		 *************************************************/

		System.out.println("QUERY: " + procStmntG);
		prepStmnt = connection.prepareStatement(procStmntG);
		pmd = prepStmnt.getParameterMetaData();

		prepStmnt.setString(1, "key10");
		prepStmnt.setLong(2, 20030105);
		prepStmnt.addBatch();

		prepStmnt.setString(1, "key01");
		prepStmnt.setLong(2, 20080105);
		prepStmnt.addBatch();

		batchResults = prepStmnt.executeBatch();
		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");

		/* ***********************************************
		 *************************************************
		 *************************************************/

		System.out.println("QUERY: " + procStmntM);
		prepStmnt = connection.prepareStatement(procStmntM);
		pmd = prepStmnt.getParameterMetaData();

//		prepStmnt.setInt(1, 0);
		prepStmnt.setString(1, "key10");
		prepStmnt.setString(2, "2008-04-25 16:20:57");
		prepStmnt.addBatch();

//		prepStmnt.setInt(1, 0);
		prepStmnt.setString(1, "key11");
		prepStmnt.setString(2, "2008-05-12 17:55:56");
		prepStmnt.addBatch();

		batchResults = prepStmnt.executeBatch();

		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");

		/* ***********************************************
		 *************************************************
		 *************************************************/

		System.out.println("QUERY: " + procStmntN);
		prepStmnt = connection.prepareStatement(procStmntN);
		pmd = prepStmnt.getParameterMetaData();

		prepStmnt.setString(1, "key12");
		prepStmnt.setString(2, "2008-04-25 16:20:57");
		prepStmnt.addBatch();

		prepStmnt.setString(1, "key13");
		prepStmnt.setString(2, "2008-05-12 17:55:56");
		prepStmnt.addBatch();

		batchResults = prepStmnt.executeBatch();

		for(int i = 0; i < batchResults.length; i++){
			System.out.println("Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");

		/* ***********************************************
		 *************************************************
		 *************************************************/

		System.out.println("QUERY: " + procStmntP);
		prepStmnt = connection.prepareStatement(procStmntP);
		pmd = prepStmnt.getParameterMetaData();

		prepStmnt.setString(1, "key18");
		prepStmnt.setLong(2, 20080425L);
		prepStmnt.setString(3, "Updated to key18 from key02");
		prepStmnt.setString(4, "key02");
		prepStmnt.setInt(5, 1);
		prepStmnt.addBatch();

		prepStmnt.setString(1, "key19");
		prepStmnt.setLong(2, 20080507L);
		prepStmnt.setString(3, "Updated to key19 from key05");
		prepStmnt.setString(4, "key05");
		prepStmnt.setInt(5, 3);
		prepStmnt.addBatch();

		batchResults = prepStmnt.executeBatch();

		for(int i = 0; i < batchResults.length; i++){
			System.out.println("P) Entry " + (i+1) + " affected " + batchResults[i] + " rows.");
		}

		System.out.println("ParameterMetadata:");
		System.out.println("ParameterCount = " + pmd.getParameterCount());
		for(int i = 1; i-1<pmd.getParameterCount(); i++){
			System.out.println("Param " + i + " = TypeName: " + pmd.getParameterTypeName(i) + ", SqlType: " + pmd.getParameterType(i) + ", JavaClassName: " + pmd.getParameterClassName(i) + ", Nullable: " + pmd.isNullable(i));
		}


		columnIndex = 1;
		while(prepStmnt.getMoreResults()){

			if(columnIndex == 1) System.out.println("RESULTS WITH ResultSetMetaData:");

			results = prepStmnt.getResultSet();
			rsMetaData = prepStmnt.getMetaData();

			while(results.next()){
				for(int i = 1; i <= rsMetaData.getColumnCount(); i++){
					System.out.println("PreparedStatement.getMetaData() " + columnIndex + ": Column: " + rsMetaData.getColumnName(i) + " (" + rsMetaData.getColumnLabel(i) + ") \tValue: (" + rsMetaData.getColumnTypeName(i) + ") " + results.getString(rsMetaData.getColumnName(i)) + " \tSchema: " + rsMetaData.getSchemaName(i));
				}
				System.out.println("---");
			}
			columnIndex++;
		}
		prepStmnt.close();
		System.out.println("===");
	}





	/**
	 * Running this method will output a heap of stuff to your system console,
	 * so you should launch it from the command line.<br />
	 * <br />
	 * Try something like:
	 * <br />
	 * <code>java -jar CoreTest.jar</code> for compiled jar files (<i>classpath is added via the supplied test-manifest</i>)<br />
	 * <code>java CoreTest.class</code> for compiled class files (<i>don't forget to update your classpath first. See the bundled manifest for details.</i>).<br />
	 * <br />
	 * JUnit testing may be implemented in a future release. At the moment it isn't the highest priority.
	 *
	 * @param args are ignored
	 */
	public static void main(String[] args) {
		//basic testing
		new CoreTest();

		//load and reliability testing. Exceptions will abort this loop.
//		for(int i = 0; i < 100; i++){
//			new CoreTest();
//		}

	}
}
