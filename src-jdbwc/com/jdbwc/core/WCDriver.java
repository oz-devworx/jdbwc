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


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.jdbwc.util.Util;

/**
 * Required JDBC-API implementation for java.sql.Driver.<br />
 * <br />
 * This class provides the functionality for com.jdbwc.core.Driver<br />
 *
 * <br />
 * <i>NOTE: The methods in this class were migrated out of com.jdbwc.core.Driver
 * class to reduce the standby memory footprint size of Driver.</i>
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-04-09 version 1.0.0-4beta
 * @version 2010-05-21 removed implements keyword from class declaration
 */
public class WCDriver {

	//--------------------------------------------------------- fields

	/* driver version info */
	private static final int WC_MAJOR_VER = 1;
	private static final int WC_MINOR_VER = 0;
	private static final int WC_SUBMINOR_VER = 1;
	private static final String WC_STATUS_VER = "-1";
	private static final String WC_STATUS_NAME = "rc";
	private static final String WC_VER_NAME = "JDBWC";

	private static final String WC_SERVER = "jdbwc/index.php";

	private static final boolean JDBC_COMPLIANT = false;

//	/* Acceptible URL prefixes */
//	private static final String KEY_VID_DEFAULT = "jdbc:jdbwc://";
//	private static final String KEY_VID_MYSQL = "jdbc:jdbwc:mysql//";
//	private static final String KEY_VID_POSTGRESQL = "jdbc:jdbwc:postgresql//";

	/* URL prefix parts */
	private static final String KEY_VID_DEFAULT = "jdbc:jdbwc:";
	/* databases. name must match package folder name */
	protected static final String KEY_VID_MYSQL = "mysql";
	protected static final String KEY_VID_POSTGRESQL = "postgresql";

	/* Acceptible URL keys */
	protected static final String KEY_URL = "url";
	protected static final String KEY_USER = "user";
	protected static final String KEY_PASS = "password";

	public static final String KEY_DB_VID = "dbType";
	protected static final String KEY_DB_NAME = "databaseName";
	protected static final String KEY_DB_USER = "databaseUser";
	protected static final String KEY_DB_PASS = "databasePassword";

	protected static final String KEY_PROXY_URL = "proxyUrl";

	protected static final String KEY_NV_SSL = "nonVerifiedSSL";
	protected static final String KEY_TIMEOUT = "timeout";
	protected static final String KEY_USE_UA = "useDummyAgent";

	protected static final String KEY_DEBUG = Util.TAG_DEBUG;
	protected static final String KEY_DEBUG_LOG = "debugLogger";
	protected static final String KEY_DEBUG_LEVEL = "debugLevel";

	//--------------------------------------------------------- constructors

	/**
	 * Required constructor for Class.forName().newInstance()
	 */
	public WCDriver(){}

	//--------------------------------------------------------- public methods

	/**
	 * Check if this driver supports the given url.<br />
	 * This method is usually called by DriverManager before calling connect.
	 *
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	public boolean acceptsURL(String url) throws SQLException {
		boolean accept = false;

		if(url.startsWith(KEY_VID_DEFAULT.concat("//"))){
			accept = true;
		}else if(url.startsWith(KEY_VID_DEFAULT.concat(KEY_VID_MYSQL).concat("//"))){
			accept = true;
		}else if(url.startsWith(KEY_VID_DEFAULT.concat(KEY_VID_POSTGRESQL).concat("//"))){
			accept = true;
		}
		return accept;
	}

	/**
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	public java.sql.Connection connect(String url, Properties info) throws SQLException {
		WCConnection connection = null;

		if(url != null && acceptsURL(url)){

			//get the dbType
			int dbType = Util.ID_DEFAULT;
			if(url.startsWith(KEY_VID_DEFAULT.concat(KEY_VID_MYSQL))){
				dbType = Util.ID_MYSQL;
			}else if(url.startsWith(KEY_VID_DEFAULT.concat(KEY_VID_POSTGRESQL))){
				dbType = Util.ID_POSTGRESQL;
			}

			connection = getConnection(dbType, url, info);
		}

		return connection;
	}

	/**
	 * @see java.sql.Driver#getMajorVersion()
	 */
	public final int getMajorVersion() {
		return WC_MAJOR_VER;
	}

	/**
	 * @see java.sql.Driver#getMinorVersion()
	 */
	public final int getMinorVersion() {
		return WC_MINOR_VER;
	}

	/**
	 * All info related to this Driver comes from WCDriverPropertiesInfo
	 *
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {

		if (info == null) {
			info = new Properties();
		}
		info = getPropsFromUrl(url, info);

		return (new WCDriverPropertiesInfo(null)).getDriverInfoProps(info);
	}

	/**
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	public final boolean jdbcCompliant() {
		return JDBC_COMPLIANT;
	}


	/* *****************************************************
	 * Extensions we need over the java.sql.Driver interface.
	 ***************************************************** */
	public final String getMinorBuild() {
		return WC_SUBMINOR_VER + WC_STATUS_VER + WC_STATUS_NAME;
	}

	public final String getVersionName() {
		return WC_VER_NAME;
	}

	public final String getVersionString() {
		return getStaticVersion();
	}

	//--------------------------------------------------------- private methods

	private int getDbType(String typeName){
		//get the dbType
		int dbType = Util.ID_DEFAULT;
		if(typeName.compareToIgnoreCase(KEY_VID_MYSQL) == 0){
			dbType = Util.ID_MYSQL;
		}else if(typeName.compareToIgnoreCase(KEY_VID_POSTGRESQL) == 0){
			dbType = Util.ID_POSTGRESQL;
		}

		return dbType;
	}

	private WCConnection getConnection(int dbType, String url, Properties info) throws SQLException{
		WCConnection connection = null;

		// get required and optional parameters
		synchronized(url){
			// if the props are part of the url, we convert them to a Properties object
			Properties urlProps = getPropsFromUrl(url, info);

			/* get the servers absolute url */
			String hostUrl = urlProps.getProperty(KEY_URL);

			if(hostUrl==null || hostUrl.isEmpty()){
				throw new SQLException(
						KEY_URL + " must contain a value. Subfolders are optional. EG: https://myserver.ext:443/[myfolder/myotherfolder/]"
						, "42000");
			}

			/* get the servers domain */
			String hostDomain = getDomainString(hostUrl);

			String hostUser = urlProps.getProperty(KEY_USER);
			String hostPass = urlProps.getProperty(KEY_PASS);

			/* support for getPropertyInfo */
			String dbVid = urlProps.getProperty(KEY_DB_VID);
			if(dbVid!=null && !dbVid.isEmpty())
				dbType = getDbType(dbVid);

			String dbName = urlProps.getProperty(KEY_DB_NAME);
			String dbUser = urlProps.getProperty(KEY_DB_USER);
			String dbPass = urlProps.getProperty(KEY_DB_PASS);


			String hostScheme;
			hostUrl = hostUrl + WC_SERVER; // remote jdbwc-handler script EG: http://localhost/admin/
			if(hostUrl.startsWith("https"))
				hostScheme = "https";
			else
				hostScheme = "http";
			// path part of URL
			String hostPath = hostUrl.replace(hostScheme + "://" + hostDomain, "");


			int hostPort;
			try {
				hostPort = Integer.parseInt(hostDomain.substring(hostDomain.indexOf(":")+1));
			} catch (NumberFormatException e) {
				//makes the port portion of the host url optional
				if(hostUrl.startsWith("https"))
					hostPort = 443;
				else
					hostPort = 80;
			}

			// remove any port references from the domain name. If needed WCConnection will add them later
			if(hostDomain.contains(":")) hostDomain = hostDomain.substring(0, hostDomain.indexOf(":"));


			/* OPTIONAL PARAMETERS */
			int proxyPort = 0;
			String proxyUrl = urlProps.getProperty(KEY_PROXY_URL);
			String proxyScheme = null;
			String proxyDomain = null;

			if(proxyUrl!=null && !proxyUrl.isEmpty()){
				proxyDomain = getDomainString(proxyUrl);

				if(proxyUrl.startsWith("https"))
					proxyScheme = "https";
				else
					proxyScheme = "http";

				try {
					proxyPort = Integer.parseInt(proxyDomain.substring(proxyDomain.indexOf(":")+1));
				} catch (NumberFormatException e1) {
					//proxy's are rarely on standard ports so we can't safely recover from this one
					throw new SQLException("The proxy server port must be included in the proxyUrl using standard port notation. EG: http://myproxy.ext:800", "08S01", e1);
				}

				if(proxyDomain.contains(":")) proxyDomain = proxyDomain.substring(0, proxyDomain.indexOf(":"));
			}


			int timeout;
			try {
				timeout = Integer.parseInt(urlProps.getProperty(KEY_TIMEOUT));
			} catch (NumberFormatException e1) {
				timeout=0;
			}

			boolean nonVerifiedSSL = "true".equals(urlProps.getProperty(KEY_NV_SSL));
			boolean dummyUA = "true".equals(urlProps.getProperty(KEY_USE_UA));
			boolean debug = "true".equals(urlProps.getProperty(KEY_DEBUG));

			// logging. Mainly for debugging
			if(debug){
				setupLogging(
						urlProps.getProperty(KEY_DEBUG_LOG)
						, urlProps.getProperty(KEY_DEBUG_LEVEL));
			}

			/* Fire-up a database connection with a database-server. */
			connection = new WCConnection(
					url,

					hostScheme,
					hostDomain,
					hostPort,
					hostPath,

					hostUser,
					hostPass,

					dbType,
					dbName,
					dbUser,
					dbPass,

					nonVerifiedSSL,
					timeout,
					debug,
					dummyUA,

					proxyScheme,
					proxyDomain,
					proxyPort
					);
		}
		return connection;
	}

	private final static String getStaticVersion(){
		return new StringBuilder()
			.append(WC_MAJOR_VER)
			.append('.').append(WC_MINOR_VER)
			.append('.').append(WC_SUBMINOR_VER)
			.append(WC_STATUS_VER)
			.append(WC_STATUS_NAME)
			.toString();
	}

	/**
	 * Get a lowercase domain from a standard url.
	 *
	 * @param urlString - a url consisting of prefix, subdomain[optional], domain and folder portions.<br />
	 * EG: <i>https://subdomain.domain.ext/admin/</i>
	 * @return the domain with extension portion of this url
	 */
	private String getDomainString(String urlString){

		/* get start of domain part */
		int domainStart = urlString.indexOf("//")+2;
		String domainString = urlString.substring(domainStart);
		domainString = domainString.toLowerCase();

		if(domainString.contains("/")){
			/* get start of folder part */
			int domainEnd = domainString.indexOf('/');
			/* remove folder portion from domain */
			domainString = domainString.substring(0, domainEnd);
		}

		return domainString;
	}

	private Properties getParametersProp(Properties info, String paramString){
		Properties urlProps = (info != null) ? new Properties(info) : new Properties();

		/* parse paramString into WCConnection compatible params */
		StringTokenizer queryParams = new StringTokenizer(paramString, "&");

		if (queryParams.countTokens() > 0) {

			while (queryParams.hasMoreTokens()) {
				String parameterValuePair = queryParams.nextToken();

				int indexOfEquals = parameterValuePair.indexOf("=");

				String parameter = null;
				String value = null;

				if (indexOfEquals != -1) {
					parameter = parameterValuePair.substring(0, indexOfEquals);

					if (indexOfEquals + 1 < parameterValuePair.length()) {
						value = parameterValuePair.substring(indexOfEquals + 1);
					}
				}

				if ((value != null && value.length() > 0) && (parameter != null && parameter.length() > 0)) {
					try {
						urlProps.put(parameter, URLDecoder.decode(value, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						new SQLException("Your system doesn't appear to support UTF-8. You should make sure your systems region settings are not corrupted.", e);
					} catch (NoSuchMethodError e) {
						new SQLException("Can't find java.net.URLDecoder in your JRE. Please check you have java installed correctly or try updating your JRE to a newer version.", e);
					}
				}
			}
		}
		return urlProps;
	}

	private void setupLogging(String debugLogger, String debugLevel){

			int requestedLevel;
			try {
				requestedLevel = Integer.parseInt(debugLevel);
			} catch (NumberFormatException e) {
				requestedLevel = 0;//default if not specified
			}

			if("SimpleLog".equals(debugLogger)){
				//general setup
				System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
				System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");

				//JDBWC informative/debugging
				if(requestedLevel >= 0){
					System.setProperty("org.apache.commons.logging.simplelog.log.jdbwc.core", "DEBUG");
				}
				//JDBWC debugging
				if(requestedLevel >= 1){
					System.setProperty("org.apache.commons.logging.simplelog.log.jdbwc.util", "DEBUG");
				}
				//Enable header wire + context logging - Best for Debugging
				if(requestedLevel == 2){
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
				}
				//Enable full wire + context logging
				if(requestedLevel == 3){
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
				}
				//Enable context logging for connection management
				if(requestedLevel == 4){
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn", "DEBUG");
				}
				//Enable context logging for connection management / request execution
				if(requestedLevel == 5){
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.conn", "DEBUG");
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl.client", "DEBUG");
					System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.client", "DEBUG");
				}

			}else if("Jdk14Logger".equals(debugLogger)){

//				System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
//
//				Properties loggerProps = new Properties();
//				loggerProps.put(".level", "INFO");
//				loggerProps.put("handlers", "java.util.logging.ConsoleHandler");
//				loggerProps.put("java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter");
//
//				//informative debugging
//				if(requestedLevel >= 0){
//					loggerProps.put("com.jdbwc.core.level", "FINEST");
//				}
//
//				//basic debugging
//				if(requestedLevel >= 1){
//					loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
//				}
//
//				//good for connection debugging
//				if(requestedLevel == 2){
//					loggerProps.put("httpclient.wire.header.level", "FINEST");
//					loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
//				}
//
//				//overkill. Lots of output
//				if(requestedLevel == 3){
//					loggerProps.put("httpclient.wire.level", "FINEST");
//					loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
//				}
//
////				System.err.println(loggerProps.toString());
////				java.util.logging.Logger log = java.util.logging.Logger.getLogger("Driver");
//				java.util.logging.LogManager logger = java.util.logging.LogManager.getLogManager();
//
//				String loggerStr = loggerProps.toString().replace(", ", "\n");
//
//				loggerStr = loggerStr.substring(1, loggerStr.length()-1);
////				System.err.println(loggerStr);
//
//				try {
//					InputStream ins = new ByteArrayInputStream(loggerStr.getBytes("UTF-8"));
//					logger.readConfiguration(ins);
//
////					log.
//				} catch (UnsupportedEncodingException e) {
//					throw new SQLException("Could not configure java.util.logging.", e);
//				} catch (SecurityException e) {
//					throw new SQLException("Could not configure java.util.logging.", e);
//				} catch (IOException e) {
//					throw new SQLException("Could not configure java.util.logging.", e);
//				}




			}else if("Log4JLogger".equals(debugLogger)){
//				System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");


			}
	}

	private Properties getPropsFromUrl(String url, Properties info){
		Properties urlProps;
		if(url.contains("?")){
			int slashesEnd = url.indexOf("//")+2;
			int paramStart = url.indexOf('?')+1;

			/* get the servers absolute url */
			String scriptUrl = url.substring(slashesEnd, paramStart-1);
			info.put(KEY_URL, scriptUrl);
			/* get the paramaters from the jdbc url */
			String paramString = url.substring(paramStart);
			urlProps = getParametersProp(info, paramString);
		}else{
			urlProps = info;
		}
		return urlProps;
	}
}
