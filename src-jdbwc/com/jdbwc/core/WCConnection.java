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
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.iface.Connection;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;


/**
 * Extended JDBC-API implementation for <code>java.sql.Connection</code><br />
 * <br />
 * <b>NOTES:</b><br />
 * One of the major hurdles with communication across the internet is latency.
 * It causes otherwise predictable behaviour to become unpredicatable.
 * Ive found through using a combination of sql transactions and stored sql procedures
 * the latency issue can be gracefully handled.<br />
 * <br />
 * The retry handler this class implements will also help with some latency issues but not all.
 * Transactions are one of the safest ways to ensure data doesnt go missing in transit
 * and cannot duplicate itself in the remote database.
 * Even better are Stored-Procedures with nested Transactions;
 * this reduces bandwidth and increases reliability significantly.
 * Banks rely heavily on Stored-Procedures as part of thier
 * overall security and reliability strategies.<br />
 * <br />
 * Your applications logic should be capable of recovering from missing data
 * (EG: by resending or re-requesting without triggering duplications).<br />
 * <br />
 * Ultimately it will depend on what data your application is handling and
 * how critical the successfull transmission for each portion of your code is
 * as to what strategy your java interface and SQL queries implement.
 *
 *
 * @see com.jdbwc.iface.Connection interface for extension method details.
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-06-29
 * @version 2010-04-09
 */
public class WCConnection extends WCConnectionInfo implements Connection {

	/** Log object for this class. */
    private static final Log LOG = LogFactory.getLog("jdbwc.core.Connection");

	private static final int MY_TIME_OUT = 10000;
	private static final int MY_IDLE_CLOSE = 10000;
	private static final String MY_SCRIPT = "jdbwc/index.php";

	private transient int myTimeOut = MY_TIME_OUT;

	private transient boolean ourSessLimit = false;
	private transient boolean connectionClosed = true;

	private transient HttpClient myClient;
	private transient HttpConnectionManager myCMan;

	/** JDBC url. Required to start a connection and specify the database type */
	private transient String myFullJdbcUrl = "";
	/** full http/s url */
	private transient String myUrl = "";
	/** holder for encrypted username */
	private transient String myUser = "";
	/** holder for encrypted password */
	private transient String myPass = "";
	/** holder for encrypted database (name + user + password) */
	private transient String myCredentials = "";


	private transient int myPort = 0;//Expect to implement in the next major version
	private transient String myDomain = "";//Expect to implement in the next major version



	protected HttpMethodRetryHandler myretryhandler = new HttpMethodRetryHandler() {
	    public boolean retryMethod(final HttpMethod method, final IOException exception, int executionCount) {
	    	if (executionCount >= 7) {
	            // Do not retry if over max retry count
	            return false;
	        }
	        if (exception instanceof ConnectTimeoutException) {
	            // Retry if the server did not accept the connection
	            return true;
	        }
	        if (exception instanceof NoHttpResponseException) {
	            // Retry if the server dropped connection on us
	            return true;
	        }
	        if (!method.isRequestSent()) {
	            // Retry if the request has not been sent fully or
	            // if it's OK to retry methods that have been sent
	            return true;
	        }
	        // otherwise do not retry
	        return false;
	    }
	};

	/**
	 * For the delegate only.<br />
	 *
	 * See <code>WCConnection(String, String, int, String, String, String, String, int, String)</code>
	 * for the actual Connection initialiser routine.
	 */
	protected WCConnection(){
		super();
	}

	/**
	 * Initialises a jdbc connection.<br />
	 * Connection can use any standard http port. They are: 80 http, 443 https).<br />
	 * Custom ports are not supported in this release.
	 *
	 * @param fullJdbcUrl
	 * @param domain
	 * @param port
	 * @param url
	 * @param user
	 * @param password
	 * @param jdbwcCredentials
	 * @param dbType
	 * @param database
	 * @throws SQLException
	 */
	protected WCConnection(String fullJdbcUrl, String domain, int port, String url, String user, String password, String jdbwcCredentials, int dbType, String database) throws SQLException{
		super();
		synchronized(myFullJdbcUrl){
			/* initialise a live connection. */
			myDomain = domain; // domain part only EG: localhost, example.com, etc.
			myPort = port; // port number you will be connecting through

			myFullJdbcUrl = fullJdbcUrl;
			myUrl = url + MY_SCRIPT; // remote jdbwc-handler script EG: http://localhost/admin/

			//stored as random cipher hashes.
			myUser = com.jdbwc.util.Security.getSecureString(user); // serverside-api username
			myPass = com.jdbwc.util.Security.getSecureString(password); // serverside-api passsword
			myCredentials = com.jdbwc.util.Security.getSecureString(jdbwcCredentials); // database (name + user + password)

			myDbType = dbType;
			myActiveDatabase = database;
			myCaseSensitivity = (dbType==Util.ID_POSTGRESQL) ? -1 : 0;//lowercase for Postgres

			prepConnection();

			if(authorise()){
				/* populate the DB version vars for "versionMeetsMinimum(i,i2,i3)" */
				getDatabaseInfo();

				// this is a work-around for WCConnectionTransaction()
				super.setTransConnection(getConnection());

			}else{
				// we should never get to this exception.
				// It was implented early in the exception handling integration process.
				// Since then error handling for this constructor has been moved to authorise()
				throw new SQLException(
						"FAILSAFE: Database Credentials are not valid for the available databases!\n"
						+ "TIP: The database credentials should be appended to the JDBC request url as parameters or passed in as a Properties object EG:\n"
						+ "jdbc:jdbwc:[dbType]//http[s]://serversFullUrl.ext/[locationOfJDBWCFolderIfAny/]?port=443&db_database=xxxxx&db_user=xxxxx&db_password=xxxxx",
						"08004");
			}
		}
	}

	protected WCConnection getConnection(){
		return this;
	}

	protected String getScriptUrl(){
		return myUrl;
	}

	public String getCredentials(){
		return myCredentials;
	}

	public String getUrl(){
		return myFullJdbcUrl;
	}

	public String getUser(){
		return myUser;
	}

	public HttpClient getClient(){
		return myClient;
	}

	public int getTimeOut(){
		return myTimeOut;
	}

	public void setTimeOut(int timeOut){
		myTimeOut = timeOut;
	}

	public boolean getSessLimit(){
		return ourSessLimit;
	}

	public void setSessLimit(boolean sessLimit){
		ourSessLimit = sessLimit;
	}

	public Statement createStatement() throws SQLException {
		WCStatement stmnt = new WCStatement(this);
		// set all defaults
		stmnt.rsType = WCStatement.CLOSE_CURRENT_RESULT;
		stmnt.rsConcurrency = WCStatement.SUCCESS_NO_INFO;
		stmnt.rsHoldability = WCStatement.NO_GENERATED_KEYS;

		return stmnt;
	}

	public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		WCStatement stmnt = new WCStatement(this);
		// override Type & Concurrency defaults
		stmnt.rsType = resultSetType;
		stmnt.rsConcurrency = resultSetConcurrency;
		stmnt.rsHoldability = WCStatement.NO_GENERATED_KEYS;

		return stmnt;
	}

	public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		WCStatement stmnt = new WCStatement(this);
		// override all defaults
		stmnt.rsType = resultSetType;
		stmnt.rsConcurrency = resultSetConcurrency;
		stmnt.rsHoldability = resultSetHoldability;

		return stmnt;
	}

	/**
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		final PostMethod pmethod = new PostMethod(myUrl);
		pmethod.setDoAuthentication(true);
		pmethod.setFollowRedirects(false);
		pmethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, myretryhandler);
		//see com.jdbwc.util.Util class for an explanation of what OUR_UA_FIX does
		if(Util.OUR_UA_FIX) pmethod.getParams().setParameter(HttpMethodParams.USER_AGENT, Util.OUR_USER_AGENT);

		DataHandler nvpArray = Util.getCaseSafeHandler(Util.CASE_MIXED);
		nvpArray.addData(Util.OUR_AUTH, myCredentials);
		nvpArray.addData(Util.OUR_DBTYPE, myDbType);
		nvpArray.addData(Util.OUR_ACTION, "cleanup");
		pmethod.setRequestBody(Util.prepareForWeb(nvpArray));
		try {
			if (myClient.executeMethod(pmethod) != -1){
				final String contents = Util.parseResponse(pmethod.getResponseBodyAsStream());

				if(LOG.isDebugEnabled()){
					LOG.debug("\n" + contents);
					LOG.debug("Connection closed");
				}
			}
		} catch (HttpException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}finally{
			pmethod.releaseConnection();
			myCMan.closeIdleConnections(1L);
			connectionClosed = true;
		}
	}

	/**
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#createBlob()
	 */
	public Blob createBlob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#createClob()
	 */
	public Clob createClob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#createNClob()
	 */
	public NClob createNClob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#createSQLXML()
	 */
	public SQLXML createSQLXML() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		// TODO This needs to be updated to return the autoCommit state.
		// Generally the way we are accessing the databases
		// ensures this is more often true than not.
		return true;
	}

	/**
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getClientInfo()
	 */
	public Properties getClientInfo() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	public String getClientInfo(String name) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		DatabaseMetaData dbm = null;

		switch(myDbType){
		case Util.ID_POSTGRESQL:
			dbm = new PgSQLDatabaseMetaData(this);
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			dbm = new MySQLDatabaseMetaData(this);
			break;
		}

		return dbm;
	}

	/**
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return connectionClosed;//very simple implementation
	}

	/**
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#isValid(int)
	 */
	public boolean isValid(int timeout) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
//		if (sql == null) {
//			return null;
//		}
//
//		Object escapedSqlResult = EscapeProcessor.escapeSQL(
//				sql,
//				versionMeetsMinimum(4, 0, 2),
//				this);
//
//		if (escapedSqlResult instanceof String) {
//			return (String) escapedSqlResult;
//		}
//
//		return ((EscapeProcessorResult) escapedSqlResult).escapedSql;
		return null;
	}

	/**
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		// TODO implement me!
//		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		// TODO implement me!
//		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability(int holdability) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// This works for classes that aren't actually wrapping anything.
		return isClosed() ? false : iface.isInstance(this);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}


	//----------------------------------------------------------------private methods


	/**
	 * Prepares the httpClient connection objects for use.
	 *
	 * @throws SQLException
	 */
	private void prepConnection() throws SQLException{
		synchronized(new String()){

			//To counter potentially major security issues, authentication is now handled by the PHP Api.
			//All sensitive login data passed to the server Api uses a strong random-salt based hash.

			final HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
			connectionParams.setTcpNoDelay(true);
			connectionParams.setConnectionTimeout(myTimeOut);

			final HttpClientParams clientParams = new HttpClientParams();
			clientParams.setCookiePolicy(CookiePolicy.DEFAULT);

			myCMan = new SimpleHttpConnectionManager(false);
			myCMan.setParams(connectionParams);
			myCMan.closeIdleConnections(MY_IDLE_CLOSE);

			// create a singular HttpClient object
			myClient = new HttpClient(clientParams, myCMan);
		}
	}

	/**
	 * Authorise the connection.<br />
	 * Handles logging in to remote API, triggering cookie generation
	 * and selecting a database for use.<br />
	 * user, pass and dbCred values are never sent in plaintext.<br />
	 * <br />
	 * Exceptions will be thrown if the database can't be accessed,
	 * Triggers include a bad URL in the connection String/Properties;<br />
	 * or the web-server or database-server is offline or unreachable.<br />
	 * Exception messages indicate the best course of action to try and fix the problem.
	 *
	 * @return boolean True on success
	 * @throws SQLException
	 */
	private boolean authorise() throws SQLException{
		boolean authorised = false;

		final PostMethod pmethod = new PostMethod(myUrl);

		synchronized(new String()){
			//pmethod.setDoAuthentication(true);
			pmethod.setFollowRedirects(false);
			pmethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, myretryhandler);
			//see com.jdbwc.util.Util class for an explanation of what OUR_UA_FIX does
			if(Util.OUR_UA_FIX) pmethod.getParams().setParameter(HttpMethodParams.USER_AGENT, Util.OUR_USER_AGENT);

			DataHandler nvpArray = Util.getCaseSafeHandler(Util.CASE_MIXED);
			nvpArray.addData(Util.OUR_ACTION, Util.OUR_AUTH);

			//the next 3 items have been stored using a random-salt based hash for security
			//(never transmitted in plaintext)
			nvpArray.addData(Util.OUR_SEC_USER, myUser);
			nvpArray.addData(Util.OUR_SEC_PASS, myPass);
			nvpArray.addData(Util.OUR_AUTH, myCredentials);

			//trigger for serverside debugging based on user debug param.
			nvpArray.addData(Util.OUR_DEBUG_TAG, Util.OUR_DEBUG_MODE);


			nvpArray.addData(Util.OUR_DBTYPE, myDbType);
			pmethod.setRequestBody(Util.prepareForWeb(nvpArray));
		}
		try {
			//if this fails: we will end up in the IOException or HttpException in that order.
			// usually indicates a web server or network issue
			// (usually not a db issue.)
			if (myClient.executeMethod(pmethod) != -1) {

				/* Util.parseResponse locates errors from server-side */
				String contents = Util.parseResponse(pmethod.getResponseBodyAsStream());
//				System.err.println("contents = " + contents);

				WCResultSet versionData[] = WCStatement.prepareForJava(this, null, "", contents);
				// get the first ResultSet.
				if(versionData!=null && versionData[0].next()){
					String[] versionSet = versionData[0].getString("SERVER_VERSIONS").split("\\|");
					if(versionSet[0].contains(",")){
						myDatabaseVersion = versionSet[0].substring(0, versionSet[0].indexOf(','));
					}else{
						myDatabaseVersion = versionSet[0];
					}
					myScriptingVersion = versionSet[1];
					myJDBWCScriptVersion = versionSet[2];
					myTimeZone = versionSet[3];
					myServerProtocol = versionSet[4];

					ourSessLimit = false;
					authorised = true;
					connectionClosed = false;
				}else{

					if(LOG.isErrorEnabled()){
						LOG.error("AUTHENTICATED BY " + myScriptingVersion);
					}

					//usually the wrong web URL in the connection String/Properties
					throw new SQLException(
							"A connection could not be established with the database.\n" +
							"These are the most likely reasons:\n" +
							"\tYou may be using the wrong URL path in your connection String/Properties [I TRIED: " + myUrl + "]\n" +
							"\t\t(could be the protocol, domain name [or folder name if any]. Don't include the 'jdbwc' folder-name in the path; its added automatically).\n" +
							"\tIt could also be due to a non transparent Proxy server [if any] or your local firewall settings\n" +
							"\t\t(check your internet settings and firewall. If you suspect a Proxy, try bypassing it).\n" +
							"\t\tNOTE: if you're running a local firewall (its a good idea) you will need to\n" +
							"\t\tgive the driver permission to access the required network the first time it requests a connection.\n",
							"08001");
				}

				if(LOG.isDebugEnabled()){
					LOG.debug("AUTHENTICATED BY " + myScriptingVersion);
					LOG.debug("Server Host Name = " + myDomain);
					LOG.debug("Server Host Port = " + myPort);
					LOG.debug("Server Time Zone = " + myTimeZone);
					LOG.debug("Server Database  = " + myDatabaseVersion);
					LOG.debug("Server Scripting = " + myScriptingVersion);
					LOG.debug("Server Protocol  = " + myServerProtocol);
					LOG.debug("JDBWC Server Version = " + myJDBWCScriptVersion);
					LOG.debug("JDBWC Driver Version = " + getDriverName() + " " + getDriverVersion());
					LOG.debug("CORE DATA DRIVER = " + DataHandler.class + " " + DataHandler.VERSION);//interface
					LOG.debug("CORE DATA DRIVER = " + Util.DT_IMPL_VER);//implementation
				}

			}else{
				//database server unreachable
				throw new SQLException(
						"A connection could not be established with the database.\n" +
						"These are the most likely reasons:\n" +
						"\tThe database server is off-line or otherwise unreachable\n" +
						"\t\t(could be your ISP or an issue with the database or web server).\n" +
						"\tIt could also be due to a non transparent Proxy server [if any] or your local firewall settings\n" +
						"\t\t(check your internet settings and firewall. If you suspect a Proxy, try bypassing it).\n" +
						"\t\tNOTE: if you're running a local firewall (its a good idea) you will need to\n" +
						"\t\tgive the driver permission to access the required network the first time it requests a connection.\n",
						"08S01");
			}
		} catch (HttpException e) {
			//communication fault (guessing)
			throw new SQLException(
					"An unknown error has occurred trying to connect. The remote Database may be offline or you may not be connected to the network or the login details may be incorrect.\n"
					+ "It could also be due to your local firewall settings or a non transparent Proxy server.\n",
					"08004",
					e);
		} catch (IOException e) {
			//web server unreachable or connection issue
			throw new SQLException(
					"A connection could not be established with the database.\n" +
					"These are the most likely reasons:\n" +
					"\tThe web server is off-line or otherwise unreachable\n" +
					"\t\t(could be your ISP or an issue with the web server).\n" +
					"\tIt could also be due to a non transparent Proxy server [if any] or your local firewall settings\n" +
					"\t\t(check your internet settings and firewall. If you suspect a Proxy, try bypassing it).\n" +
					"\t\tNOTE: if you're running a local firewall (its a good idea) you will need to\n" +
					"\t\tgive the driver permission to access the required network the first time it requests a connection.\n",
					"08S01",
					e);

		} catch (java.lang.NullPointerException e) {
			//general error or database offline
			throw new SQLException(
					"A connection could not be established with the database.\n" +
					"These are the most likely reasons:\n" +
					"\tThe $_POST variables were damaged before arriving at the server.\n" +
					"\t\tThis seems to happen with shared IPs and HttpClient3.1. The reason is not clear at present.\n" +

					"\tThe database server is off-line or otherwise unreachable\n" +
					"\t\t(could be your ISP or an issue with the database or web server).\n" +
					"\tIt could also be due to a non transparent Proxy server [if any] or your local firewall settings\n" +
					"\t\t(check your internet settings and firewall. If you suspect a Proxy, try bypassing it).\n" +
					"\t\tNOTE: if you're running a local firewall (its a good idea) you will need to\n" +
					"\t\tgive the driver permission to access the required network the first time it requests a connection.\n",
					"08S01",
					e);

		}finally{
			pmethod.releaseConnection();
		}
		return authorised;
	}
}
