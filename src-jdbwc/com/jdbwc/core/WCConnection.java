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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.jdbwc.exceptions.NotImplemented;
import com.jdbwc.iface.Connection;
import com.jdbwc.util.GzipStreamReader;
import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;


/**
 * Extended JDBC-API implementation for <code>java.sql.Connection</code><br />
 * <br />
 * <b>NOTES:</b><br />
 * One of the major hurdles with communication across the Internet is latency.
 * It causes otherwise predictable behaviour to become unpredictable.
 * I've found through using a combination of sql transactions and stored sql procedures
 * the latency issue can be gracefully handled.<br />
 * <br />
 * Transactions are one of the safest ways to ensure data doesn't go missing in transit
 * and cannot duplicate itself in the remote database.
 * Even better are Stored-Procedures with nested Transactions;
 * this reduces bandwidth and increases reliability significantly.<br />
 * <br />
 * Ultimately it will depend on what data your application is handling and
 * how critical the successful transmission for each portion of your code is
 * as to what strategy your java interface and SQL queries implement.
 *
 *
 * @see com.jdbwc.iface.Connection interface for extension method details.
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-06-29
 * @version 2010-04-09
 * @version 2010-04-21
 * @version 2012-03-12 Updated for httpclient 4.1.3 (GA) and jre 7 compilation
 */
public class WCConnection extends WCConnectionInfo implements Connection {

	//--------------------------------------------------------- fields

	/** Log object for this class. */
    private static final Log LOG = LogFactory.getLog("jdbwc.core.Connection");

    /** default connection timeout value */
	private static final int MY_TIME_OUT = 60000;//milliseconds
	private static final long MY_IDLE_CLOSE = 100L;//seconds

	/** actual connection timeout value. Can be over-ridden by the user in the connection params. Must be > 10ms */
	private transient int myTimeOut;
	private transient boolean isReadOnly = false;

	private transient boolean sessLimit = false;
	private transient boolean connectionClosed = true;

	private transient HttpClient myClient;
	private transient ThreadSafeClientConnManager myCMan;
	private transient HttpHost host;

	/** full http/s url */
	private transient String hostUrl;
	/** holder for encrypted username */
	private transient String hostUser;
	/** holder for encrypted password */
	private transient String hostPass;
	/** holder for encrypted database (name + user + password) */
	private transient String dbCredentials;

	private transient int hostPort;
	private transient String hostDomain;
	private transient String hostScheme;
	private transient String hostPath;

	private transient int proxyPort;
	private transient String proxyDomain;
	private transient String proxyScheme;

	private transient boolean useNonVerifiedSSL;
	private transient boolean useDummyUA;
	private transient boolean useDebug;
	private transient boolean useProxy;

	private transient Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>();

	protected SQLWarning warnings = null;



//	protected HttpMethodRetryHandler myretryhandler = new HttpMethodRetryHandler() {
//	    public boolean retryMethod(final HttpMethod method, final IOException exception, int executionCount) {
//	    	if (executionCount >= 7) {
//	            // Do not retry if over max retry count
//	            return false;
//	        }
//	        if (exception instanceof ConnectTimeoutException) {
//	            // Retry if the server did not accept the connection
//	            return true;
//	        }
//	        if (exception instanceof NoHttpResponseException) {
//	            // Retry if the server dropped connection on us
//	            return true;
//	        }
//	        if (!method.isRequestSent()) {
//	            // Retry if the request has not been sent fully or
//	            // if it's OK to retry methods that have been sent
//	            return true;
//	        }
//	        // otherwise do not retry
//	        return false;
//	    }
//	};

	//--------------------------------------------------------- constructors

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
	 * Custom ports are fully supported in this release.<br />
	 * Connection can use any valid port in the range [1-0xfffe]<br />
	 *
	 *
	 * @param fullJdbcUrl The full jdbwc URL as passed in by the user.
	 * @param hostScheme Host protocol scheme. Can be http or https
	 * @param hostDomain Servers domain or IP
	 * @param hostPort Servers port. Can be any valid port in the range [1-0xfffe]
	 * @param hostPath Path portion of hostUrl.
	 * @param hostUser Server username as set in the JDBWC configure.php file
	 * @param hostPass Server password as set in the JDBWC configure.php file
	 * @param dbType Database type. Must be one of the types in the Util class.
	 * @param dbName Database name
	 * @param dbUser Database username
	 * @param dbPass Database password
	 * @param nonVerifiedSSL If true, the driver will attempt to use unverified or self signed SSL on the server.
	 * @param timeout Server timeout in milliseconds.
	 * @param debug Turns debugging on or off. Its off by default
	 * @param dummyUA Replace the HttpClient User-Agent with a built in UA that mimicks a web browser.
	 * @param proxyScheme Proxy protocol scheme. Can be http or https
	 * @param proxyDomain Proxy domain or IP
	 * @param proxyPort Proxy port
	 * @throws SQLException
	 */
	protected WCConnection(
			String fullJdbcUrl,

			String hostScheme,
			String hostDomain,
			int hostPort,
			String hostPath,

			String hostUser,
			String hostPass,

			int dbType,
			String dbName,
			String dbUser,
			String dbPass,

			boolean nonVerifiedSSL,
			int timeout,
			boolean debug,
			boolean dummyUA,

			String proxyScheme,
			String proxyDomain,
			int proxyPort

			) throws SQLException{
		super();

		this.myfullJdbcUrl = fullJdbcUrl;
		this.myUserName = hostUser;
		synchronized(this.myfullJdbcUrl){

			this.hostScheme = hostScheme;
			this.hostDomain = hostDomain;// domain part only EG: localhost, example.com, etc.
			this.hostPort = hostPort;
			this.hostUrl = hostScheme + "://" + hostDomain + ":" + hostPort + hostPath;
			this.hostPath = hostPath;

			//stored as random cipher hashes.
			this.hostUser = com.jdbwc.util.Security.getSecureString(hostUser); // serverside-api username
			this.hostPass = com.jdbwc.util.Security.getSecureString(hostPass); // serverside-api passsword
			this.dbCredentials = com.jdbwc.util.Security.getSecureString(dbName + dbUser + dbPass); // database (name + user + password)

			this.myDbType = dbType;
			this.myCaseSensitivity = (dbType==Util.ID_POSTGRESQL) ? -1 : 0;//lowercase for Postgres
			this.currentDatabase = dbName;
			this.databaseUser = dbUser;
			this.databasePass = dbPass;

			this.useNonVerifiedSSL = nonVerifiedSSL;
			this.myTimeOut = (timeout==0) ? MY_TIME_OUT : timeout;
			this.useDebug = debug;
			this.useDummyUA = dummyUA;

			this.useProxy = (proxyScheme!=null && proxyDomain!=null && proxyPort>0);
			this.proxyScheme = proxyScheme;
			this.proxyDomain = proxyDomain;
			this.proxyPort = proxyPort;

			/*
			 * prepare sockets, schemes, params and start a new http connection with a manager.
			 */
			prepConnection();

			if(authorise()){
				/* populate the DB version vars for "versionMeetsMinimum(i,i2,i3)" */
				getDatabaseInfo();


			}else{
				// we should never get to this exception.
				throw new SQLException(
						"Connection Failed.",
						"08004");
			}
		}
	}


	//--------------------------------------------------------- public methods

	public String getCredentials(){
		return dbCredentials;
	}

	public String getUrl(){
		return myfullJdbcUrl;
	}

	public String getUser(){
		return myUserName;
	}

	public HttpClient getClient(){
		return myClient;
	}

	public int getTimeOut(){
		return myTimeOut;
	}

	public void setTimeOut(int timeout){
		myTimeOut = timeout;
	}

	public boolean getSessLimit(){
		return sessLimit;
	}

	public void setSessLimit(boolean sessLimit){
		this.sessLimit = sessLimit;
	}

	public WCStatement createInternalStatement() throws SQLException {
		WCStatement stmnt = new WCStatement(this, this.currentDatabase);
		// set all defaults
		stmnt.rsType = WCStatement.CLOSE_CURRENT_RESULT;
		stmnt.rsConcurrency = WCStatement.SUCCESS_NO_INFO;
		stmnt.rsHoldability = WCStatement.NO_GENERATED_KEYS;

		return stmnt;
	}

	public Statement createStatement() throws SQLException {
		WCStatement stmnt = new WCStatement(this, this.currentDatabase);
		// set all defaults
		stmnt.rsType = WCStatement.CLOSE_CURRENT_RESULT;
		stmnt.rsConcurrency = WCStatement.SUCCESS_NO_INFO;
		stmnt.rsHoldability = WCStatement.NO_GENERATED_KEYS;

		return stmnt;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		WCStatement stmnt = new WCStatement(this, this.currentDatabase);
		// override Type & Concurrency defaults
		stmnt.rsType = resultSetType;
		stmnt.rsConcurrency = resultSetConcurrency;
		stmnt.rsHoldability = WCStatement.NO_GENERATED_KEYS;

		return stmnt;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		WCStatement stmnt = new WCStatement(this, this.currentDatabase);
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
		this.warnings = null;
	}

	/**
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		final HttpPost pmethod = getHttpPost();

		DataHandler nvpArray = Util.getCaseSafeHandler(Util.CASE_MIXED);
		nvpArray.addData(Util.TAG_DBTYPE, myDbType);
		nvpArray.addData(Util.TAG_ACTION, "cleanup");
		pmethod.setEntity(Util.prepareForWeb(nvpArray));

		try {
			HttpResponse response = getHttpResponse(pmethod);
			if (response != null) {
				final String contents = Util.parseResponse(response);

				if(LOG.isDebugEnabled()){
					LOG.debug("\n" + contents);
					LOG.debug("Connection closed");
				}
			}
		} catch (ClientProtocolException e) {
			//ignore
		} catch (IOException e) {
			//ignore
		}finally{
			pmethod.abort();
			myCMan.closeIdleConnections(MY_IDLE_CLOSE, TimeUnit.SECONDS);
			myClient.getConnectionManager().shutdown();
			connectionClosed = true;
		}
	}

	/**
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createArrayOf(String typeName, Object[] elements)");
	}

	/**
	 * @see java.sql.Connection#createBlob()
	 */
	public Blob createBlob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createBlob()");
	}

	/**
	 * @see java.sql.Connection#createClob()
	 */
	public Clob createClob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createClob()");
	}

	/**
	 * @see java.sql.Connection#createNClob()
	 */
	public NClob createNClob() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createNClob()");
	}

	/**
	 * @see java.sql.Connection#createSQLXML()
	 */
	public SQLXML createSQLXML() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createSQLXML()");
	}

	/**
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("createStruct(String typeName, Object[] attributes)");
	}

	/**
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		Statement stmnt = new WCStatement(this, this.currentDatabase);
		boolean autocommit = true;

		java.sql.ResultSet results = null;
		switch (this.myDbType){
		case Util.ID_POSTGRESQL:
			results = stmnt.executeQuery("show autocommit;");
			if(results.next()){
				autocommit = "on".equals(results.getString(1)) ? true:false;
			}
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			results = stmnt.executeQuery("select @@autocommit;");
			if(results.next()){
				autocommit = results.getInt(1)==0 ? false:true;
			}
			break;
		}

		results.close();
		stmnt.close();

		return autocommit;
	}

	/**
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return this.currentDatabase;
	}

	/**
	 * @see java.sql.Connection#getClientInfo()
	 */
	public Properties getClientInfo() throws SQLException {
		WCDriverPropertiesInfo clientInfoProps = new WCDriverPropertiesInfo(this);

		return clientInfoProps.getClientInfoProps(null);
	}

	/**
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	public String getClientInfo(String name) throws SQLException {
		Properties props = getClientInfo();

		return props.getProperty(name);
	}

	/**
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		// TODO implement me!
		throw new NotImplemented("getHoldability()");
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
		return this.typeMap;
	}

	/**
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return this.warnings;
	}

	/**
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		if(!connectionClosed){
			return !isValid(1000);
		}

		return connectionClosed;
	}

	/**
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return this.isReadOnly;
	}

	/**
	 * @see java.sql.Connection#isValid(int)
	 */
	public boolean isValid(int timeout) throws SQLException {
		boolean isValid = false;
		setTimeOut(timeout);
		final HttpPost pmethod = getHttpPost();

		DataHandler nvpArray = Util.getCaseSafeHandler(Util.CASE_MIXED);
		nvpArray.addData(Util.TAG_AUTH, dbCredentials);
		nvpArray.addData(Util.TAG_DBTYPE, myDbType);
		nvpArray.addData(Util.TAG_ACTION, "ping");

		pmethod.setEntity(Util.prepareForWeb(nvpArray));

		try {
			HttpResponse response = getHttpResponse(pmethod);
			if (response != null) {
				final String contents = Util.parseResponse(response);

//				System.out.println("isValid = "+contents);

				isValid = Boolean.parseBoolean(contents);
			}
		} catch (ClientProtocolException e) {
			isValid = false;
		} catch (IOException e) {
			isValid = false;
		}finally{
			pmethod.abort();
			if(!isValid){
				myCMan.closeIdleConnections(MY_IDLE_CLOSE, TimeUnit.SECONDS);
				myClient.getConnectionManager().shutdown();
				connectionClosed = true;
			}else{
				connectionClosed = false;
			}
		}

		return isValid;
	}

	/**
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL(String sql) throws SQLException {
		if (sql == null) {
			return null;
		}

		// TODO implement me!
		throw new NotImplemented("nativeSQL(String sql)");
	}

	/**
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		WCStatement stmnt = new WCStatement(this, this.currentDatabase);

		switch(myDbType){
		case Util.ID_POSTGRESQL:
			stmnt.execute("SET autocommit='" + (autoCommit ? "on":"off") + "';");
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			stmnt.execute("SET autocommit=" + (autoCommit ? 1:0) + ";");
		}

		stmnt.close();
	}

	/**
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog(String catalog) throws SQLException {
		//the server will change databases (if its already authorised)
		//when the next query is run.
		this.currentDatabase = catalog;
		this.dbCredentials = com.jdbwc.util.Security.getSecureString(currentDatabase + databaseUser + databasePass); // database (name + user + password)
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
		throw new NotImplemented("setHoldability(int holdability)");
	}

	/**
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		WCStatement stmnt = this.createInternalStatement();
		switch(myDbType){
		case Util.ID_POSTGRESQL:
			if (versionMeetsMinimum(7, 4, 0) && readOnly != this.isReadOnly){
	            stmnt.execute("SET SESSION CHARACTERISTICS AS TRANSACTION " + (readOnly ? "READ ONLY" : "READ WRITE"));
	        }
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			stmnt.executeQuery("SET GLOBAL read_only="+readOnly+";");
		}
		stmnt.close();

		this.isReadOnly = readOnly;
	}

	/**
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap(Map<String, Class<?>> typeMap) throws SQLException {
		this.typeMap = typeMap;
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return isClosed() ? false : iface.isInstance(this);
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.cast(this);
	}


	/* ****************************************************
	 * transaction methods
	 * **************************************************** */

	/**
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		Statement stmnt = new WCStatement(this, this.currentDatabase);
		stmnt.execute("COMMIT;");
		stmnt.close();
	}

	/**
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		int isolationType = 0;
		String isolation = "";
		Statement stmnt = new WCStatement(this, this.currentDatabase);

		java.sql.ResultSet results = null;
		switch(myDbType){
		case Util.ID_POSTGRESQL:
			results = stmnt.executeQuery("show transaction isolation level;");
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			results = stmnt.executeQuery("SELECT @@SESSION.tx_isolation;");
		}

		if(results.next()){
			isolation = results.getString(1);
		}
		results.close();
		stmnt.close();

		if(isolation==null)
			return Connection.TRANSACTION_NONE;

		if("READ-COMMITTED".equalsIgnoreCase(isolation) || "READ COMMITTED".equalsIgnoreCase(isolation))
			isolationType = Connection.TRANSACTION_READ_COMMITTED;

		else if("READ-UNCOMMITTED".equalsIgnoreCase(isolation) || "READ UNCOMMITTED".equalsIgnoreCase(isolation))
			isolationType = Connection.TRANSACTION_READ_UNCOMMITTED;

		else if("REPEATABLE-READ".equalsIgnoreCase(isolation) || "REPEATABLE READ".equalsIgnoreCase(isolation))
			isolationType = Connection.TRANSACTION_REPEATABLE_READ;

		else if("SERIALIZABLE".equalsIgnoreCase(isolation))
			isolationType = Connection.TRANSACTION_SERIALIZABLE;

		else
			isolationType = Connection.TRANSACTION_NONE;


		return isolationType;
	}

	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("prepareCall(...)");
	}

	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("prepareCall(...)");
	}

	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("prepareCall(...)");
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new WCPreparedStatement(this, sql);
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		WCPreparedStatement pstmnt = new WCPreparedStatement(this, sql);
		pstmnt.rsAutoGenKeys = autoGeneratedKeys;

		return pstmnt;
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		WCPreparedStatement pstmnt = new WCPreparedStatement(this, sql);
		pstmnt.rsType = resultSetType;
		pstmnt.rsConcurrency = resultSetConcurrency;

		return pstmnt;
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		WCPreparedStatement pstmnt = new WCPreparedStatement(this, sql);
		pstmnt.rsType = resultSetType;
		pstmnt.rsConcurrency = resultSetConcurrency;
		pstmnt.rsHoldability = resultSetHoldability;

		return pstmnt;
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("prepareStatement(String sql, int[] columnIndexes)");
	}

	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		// TODO implement me!
		throw new NotImplemented("prepareStatement(String sql, String[] columnNames)");
	}

	/**
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		Statement stmnt = new WCStatement(this, this.currentDatabase);
		stmnt.execute("RELEASE SAVEPOINT " + savepoint.getSavepointName() + ";");
		stmnt.close();
	}

	/**
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		rollback(null);
	}

	/**
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		Statement stmnt = new WCStatement(this, this.currentDatabase);
		if(savepoint!=null)
			stmnt.execute("ROLLBACK " + savepoint.getSavepointName() + ";");
		else
			stmnt.execute("ROLLBACK;");
		stmnt.close();
	}

	/**
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		WCSavepoint savepoint = new WCSavepoint();
		Statement stmnt = new WCStatement(this, this.currentDatabase);
		stmnt.execute("SAVEPOINT " + savepoint.getSavepointName() + ";");
		stmnt.close();

		return savepoint;
	}

	/**
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		if(name==null || name.isEmpty()){
			throw new SQLException("Savepoint name must contain a value");
		}

		Statement stmnt = new WCStatement(this, this.currentDatabase);
		stmnt.execute("SAVEPOINT " + name + ";");
		stmnt.close();

		return new WCSavepoint(name);
	}

	/**
	 * TODO: could do with some checks for open transactions before attempting to change
	 * the level.
	 *
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		String levelName = "";
		switch(level){
		case Connection.TRANSACTION_NONE:
			levelName = "";
			// TODO: need to check the table type, if myisam then notify user in an exception???
			// or just set the level anyway and leave it up to the user to know this???
			break;
		case Connection.TRANSACTION_READ_COMMITTED:
			levelName = "READ COMMITTED";
			break;
		case Connection.TRANSACTION_READ_UNCOMMITTED:
			levelName = "READ UNCOMMITTED";
			break;
		case Connection.TRANSACTION_REPEATABLE_READ:
			levelName = "REPEATABLE READ";
			break;
		case Connection.TRANSACTION_SERIALIZABLE:
			levelName = "SERIALIZABLE";
			break;
		}

		if(levelName.isEmpty()){
			throw new SQLException("Isolation Level " + level + " not supported.");
		}
		Statement stmnt = new WCStatement(this, this.currentDatabase);

		switch(myDbType){
		case Util.ID_POSTGRESQL:
			stmnt.execute("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL " + levelName + ";");
			break;

		case Util.ID_MYSQL:
		case Util.ID_DEFAULT:
			stmnt.execute("SET SESSION TRANSACTION ISOLATION LEVEL " + levelName + ";");
		}

		stmnt.close();
	}

	/**
	 * Required for jre >= 7 compilation
	 */
	@Override
	public void setSchema(String schema) throws SQLException {
		// TODO implement me! Required for jre >= 7
		throw new NotImplemented("setSchema(String schema)");
	}

	/**
	 * Required for jre >= 7 compilation
	 */
	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub. Required for jre >= 7
		return null;
	}

	/**
	 * Required for jre >= 7 compilation
	 */
	@Override
	public void abort(Executor executor) throws SQLException {
		// TODO implement me! Required for jre >= 7
		throw new NotImplemented("abort(Executor executor)");
	}

	/**
	 * Required for jre >= 7 compilation
	 */
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		// TODO implement me! Required for jre >= 7
		throw new NotImplemented("setNetworkTimeout(Executor executor, int milliseconds)");
	}

	/**
	 * Required for jre >= 7 compilation
	 */
	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub. Required for jre >= 7
		return 0;
	}




	//---------------------------------------------------------------- protected methods

	/**
	 * @return this WCConnection object
	 */
	protected WCConnection getConnection(){
		return this;
	}
	/**
	 * @return the url this connection is connected to.
	 */
	protected String getScriptUrl(){
		return hostUrl;
	}



	/**
	 * Get a HttpPost to use for communication. If the hostPort is standard 80 or 443
	 * we use the full URL, otherwise only the URL path is used.<br />
	 * This method works in unison with getHttpResponse() which uses a HttpHost
	 * for non standard ports and no proxy.
	 *
	 * @return a HttpPost object customised for best performance.
	 */
	protected HttpPost getHttpPost(){
		final HttpPost pmethod = new HttpPost((!useProxy && (hostPort==80 || hostPort==443)) ? hostUrl:hostPath);
		pmethod.getParams().setParameter("http.connection.stalecheck", false);
		pmethod.getParams().setParameter("http.protocol.expect-continue", false);
		pmethod.getParams().setParameter("http.tcp.nodelay", true);
		pmethod.getParams().setParameter("http.connection.timeout", myTimeOut);

		return pmethod;
	}

	/**
	 * Uses a HttpHost for non standard ports and no proxy (to increase performance).<br />
	 * This method works in unison with getHttpPost() which uses a URL path
	 * for non standard ports and no proxy.
	 *
	 * @param post a HttpPost request to execute
	 * @return A HttpResponse from executing a HttpPost request.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	protected HttpResponse getHttpResponse(HttpPost post) throws ClientProtocolException, IOException {
		if(!useProxy && hostPort==80 || hostPort==443)
			return getClient().execute(post);//saves up to 7ms per request
		else
			return getClient().execute(getHttpHost(), post);
	}



	//---------------------------------------------------------------- private methods



	/**
	 *
	 * @return a DefaultHttpClient with transparent gzip compression support
	 */
	private DefaultHttpClient getHttpClient(ClientConnectionManager manager, HttpParams params){
		DefaultHttpClient httpclient = new DefaultHttpClient(manager, params);

      httpclient.addRequestInterceptor(new HttpRequestInterceptor() {

          public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
              if (!request.containsHeader("Accept-Encoding")) {
                  request.addHeader("Accept-Encoding", "gzip");
              }
              if(useDummyUA) request.setHeader("User-Agent", Util.CUSTOM_AGENT);
          }
      });

      httpclient.addResponseInterceptor(new HttpResponseInterceptor() {

          public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
              HttpEntity entity = response.getEntity();
              Header ceheader = entity.getContentEncoding();
              if (ceheader != null) {
                  HeaderElement[] codecs = ceheader.getElements();
                  for (int i = 0; i < codecs.length; i++) {
                      if ("gzip".equalsIgnoreCase(codecs[i].getName())) {
                          response.setEntity(new GzipStreamReader(response.getEntity()));
                          return;
                      }
                  }

              }
          }
      });

      return httpclient;
	}

	/**
	 * Get the HttpHost to use when communicating with the JDBWC server
	 *
	 * @return the host we are connecting to
	 */
	private HttpHost getHttpHost(){
		return host;
	}

	private SchemeRegistry getSchemeRegistry(SchemeRegistry schemes, String serverScheme, int serverPort) throws SQLException{

		if(schemes==null)
			schemes = new SchemeRegistry();

		if(serverScheme.startsWith("https")){

			try {
				SSLContext context;
				if(useNonVerifiedSSL){
					TrustManager sslTrustMan = new X509TrustManager() {
						@Override
						public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
							// don't perform checking on unvalidated or self-signed certs as they always fail
						}

						@Override
						public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
							// don't perform checking on unvalidated or self-signed certs as they always fail
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[]{};
						}
					};
					context = SSLContext.getInstance("SSL");
					context.init(null, new TrustManager[]{sslTrustMan}, null);

				}else{
					context = SSLContext.getDefault();
				}

				SSLSocketFactory sslSocket;

				if(!useNonVerifiedSSL)
					sslSocket = new SSLSocketFactory(context, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
				else
					sslSocket = new SSLSocketFactory(context);

				schemes.register(new Scheme("https", serverPort, sslSocket));

			} catch (KeyManagementException e) {
				throw new SQLException("SSL key exception. Could be due to Java KeyManagement. See the exception cause for more info.", "28000", e);
			} catch (NoSuchAlgorithmException e) {
				throw new SQLException("No Such Algorithm. Your Java runtime may not support SSL. See the exception cause for more info.", "S1000", e);
			}


		}else{
			schemes.register(new Scheme("http", serverPort, PlainSocketFactory.getSocketFactory()));
		}

		return schemes;
	}

	/**
	 * Prepares the httpClient connection objects for use.
	 * This only gets run once per connection so most of the work gets offloaded here to reduce
	 * data exchange times.
	 *
	 * @throws SQLException
	 */
	private void prepConnection() throws SQLException{

		if(myCMan==null || myClient==null){
			synchronized(hostUrl){

				HttpParams params = new BasicHttpParams();
				params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, myTimeOut);
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		        HttpProtocolParams.setContentCharset(params, "UTF-8");
		        HttpProtocolParams.setUseExpectContinue(params, false);
		        if(useDummyUA) HttpProtocolParams.setUserAgent(params, Util.CUSTOM_AGENT);

		        SchemeRegistry schemes = getSchemeRegistry(null, hostScheme, hostPort);
		        if(useProxy && !hostScheme.equals(proxyScheme))
		        	schemes = getSchemeRegistry(schemes, proxyScheme, proxyPort);


				myCMan = new ThreadSafeClientConnManager(schemes);
				// Increase max total connections
				myCMan.setMaxTotal(50);
				// Increase default max connections
				myCMan.setDefaultMaxPerRoute(10);


				myClient = getHttpClient(myCMan, params);
				host = new HttpHost(hostDomain, hostPort, hostScheme);

				if(useProxy){
			        HttpHost proxy = new HttpHost(proxyDomain, proxyPort, proxyScheme);
			        myClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
				}

			}
		}
	}

	/**
	 * Authorise the connection.<br />
	 * Handles logging in to remote API, triggering cookie generation
	 * and selecting a database for use.<br />
	 * user, pass and dbCred values are never sent in plaintext.<br />
	 * <br />
	 * Exception messages indicate the best course of action to try and fix the problem.<br />
	 * <br />
	 * Database can't be accessed Triggers:
	 * <ul>
	 *   <li>Web-server is unreachable.</li>
	 *   <li>Database-server is unreachable (gets caught in the Util.parseResponse() method).</li>
	 *   <li>Bad URL in the connection Properties (the users web-path).</li>
	 * </ul>
	 * <br />
	 * Login errors and server errors:
	 * <ul>
	 *   <li>Wrong username or password.</li>
	 *   <li>Wrong database credentials.</li>
	 *   <li>Server session errors.</li>
	 *   <li>PHP errors.</li>
	 * </ul>
	 *
	 *
	 * @return boolean True on success
	 * @throws SQLException
	 */
	private boolean authorise() throws SQLException{
		boolean authorised = false;

		final HttpPost pmethod = getHttpPost();

		synchronized(pmethod){
			DataHandler nvpArray = Util.getCaseSafeHandler(Util.CASE_MIXED);

			//the next 3 items have been stored using a random-salt based hash for security
			//(never transmitted in plaintext)
			nvpArray.addData(Util.TAG_USER, hostUser);
			nvpArray.addData(Util.TAG_PASS, hostPass);
			nvpArray.addData(Util.TAG_AUTH, dbCredentials);

			//trigger for serverside debugging based on user debug param.
			nvpArray.addData(Util.TAG_DEBUG, useDebug);
			nvpArray.addData(Util.TAG_DBTYPE, myDbType);

			pmethod.setEntity(Util.prepareForWeb(nvpArray));
		}
		try {
			//if this fails: we will end up in the IOException or HttpException in that order.
			// usually indicates a web server, network or routing issue
			// (usually not a db issue.)
			HttpResponse response = getHttpResponse(pmethod);

			if (response != null && response.getStatusLine().getStatusCode()==200) {
				String contents = Util.parseResponse(response);

				/* Test once during each connection for fatal errors.
				 * We do this here instead of in the usual check for
				 * exceptions to avoid impacting driver performance.
				 *
				 * TODO: ?? add a connection param to choose between pedantic
				 * fatal error checking and once-only. For pedantic, this block will
				 * need to migrate (or be copied) into the Util class.
				 */
				String errorTest;
				if(!(errorTest = Util.stripTags(contents)).isEmpty()){

					for (String element : Util.WC_ERROR_SCRIPT) {
						if(errorTest.length() >= element.length()
						&& errorTest.substring(0, element.length()).equalsIgnoreCase(element)
						){
							throw new com.jdbwc.exceptions.ServerSideException(
									errorTest + "\n",
									"08S01");
						}
					}
				}
//				System.err.println("contents = " + contents);


				//this should never happen. Its here for paranoia's sake.
				//this type of error should always be detected by Util.checkForExceptions()
				if(contents.isEmpty()){
					pmethod.abort();

					if(LOG.isErrorEnabled()){
						LOG.error("Looks like the DATABASE SERVER is unreachable");
					}
					throw new com.jdbwc.exceptions.ServerSideException(
							"A connection could not be established with the database.\n" +
							"These are the most likely reasons:\n" +
							"\tThe DATABASE SERVER is off-line or otherwise unreachable\n" +
							"\t\t(could be your ISP, network or an issue with the database server).\n",
							"08S01");
				}


				// get the results
				if(contents!=null){
					String[] versionSet = contents.split("\\|");
					if(versionSet[0].contains(",")){
						myDatabaseVersion = versionSet[0].substring(0, versionSet[0].indexOf(','));
					}else{
						myDatabaseVersion = versionSet[0];
					}
					myScriptingVersion = versionSet[1];
					myJDBWCScriptVersion = versionSet[2];
					myTimeZone = versionSet[3];
					myServerProtocol = versionSet[4];
					isBaseServer = "0".equals(versionSet[5]);

					sessLimit = false;
					authorised = true;
					connectionClosed = false;
				}

				if(LOG.isDebugEnabled()){
					LOG.debug("AUTHENTICATED BY " + myScriptingVersion);
					LOG.debug("Server Host Name = " + hostDomain);
					LOG.debug("Server Host Port = " + hostPort);
					LOG.debug("Server Protocol  = " + hostScheme);
					LOG.debug("Server Host Path = " + hostPath);

					if(useProxy){
						LOG.debug("Proxy Name      = " + proxyDomain);
						LOG.debug("Proxy Port      = " + proxyPort);
						LOG.debug("Proxy Protocol  = " + proxyScheme);
					}

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
				pmethod.abort();

				if(LOG.isErrorEnabled()){
					LOG.error("Wrong URL in connection Properties [TRIED: " + hostUrl + "] a server did respond but not JDBWC.");
				}
				throw new com.jdbwc.exceptions.InvalidServerPathException(
						"A connection could not be established with the database.\n" +
						"These are the most likely reasons:\n" +
						"\tWrong URL in connection Properties [TRIED: " + hostUrl + "]\n" +
						"\t\tA web-server responded but JDBWC was not found.\n",
						"08S01");
			}

		} catch (ClientProtocolException e) {
			pmethod.abort();

			if(LOG.isErrorEnabled()){
				LOG.error("An unknown error has occurred trying to connect");
			}
			throw new SQLException(
					"An unknown error has occurred trying to connect. The remote Database may be offline or you may not be connected to the network or the login details may be incorrect.\n"
					+ "It could also be due to your local firewall settings or a non transparent Proxy server.\n",
					"08S01",
					e);

		} catch (IOException e) {
			pmethod.abort();

			if(LOG.isErrorEnabled()){
				LOG.error("Looks like the WEB SERVER is unreachable");
			}
			throw new SQLException(
					"A connection could not be established with the database.\n" +
					"These are the most likely reasons:\n" +
					"\tThe WEB SERVER is off-line or otherwise unreachable\n" +
					"\t\t(could be your ISP, network or an issue with the web server).\n" +
					"\t\tNOTE: if you're running a local firewall you will need to give the driver permission\n" +
					"\t\tto access the required network.\n",
					"08S01",
					e);

		}finally{
			pmethod.abort();
		}
		return authorised;
	}

}
