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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.jdbwc.iface.Connection;
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
 * @version 2010-04-09
 */
public class WCDriver implements java.sql.Driver {

	private static final int WC_MAJOR_VER = 1;
	private static final int WC_MINOR_VER = 0;
	private static final String WC_MINOR_BUILD = "0-3 beta";
	private static final String WC_VER_NAME = "JDBWC";

	private static final boolean JDBC_COMPLIANT = false;

	/* Acceptible URL prefixes */
	private static final String KEY_VID_DEFAULT = "jdbc:jdbwc://";
	private static final String KEY_VID_MYSQL = "jdbc:jdbwc:mysql//";
	private static final String KEY_VID_POSTGRESQL = "jdbc:jdbwc:postgresql//";

	/* Acceptible URL keys */
	private static final String KEY_URL = "url";
	private static final String KEY_PORT = "port";
	private static final String KEY_USER = "user";
	private static final String KEY_PASS = "password";

	private static final String KEY_DB_NAME = "databaseName";
	private static final String KEY_DB_USER = "databaseUser";
	private static final String KEY_DB_PASS = "databasePassword";


	private transient int myConnectionType = -1;



	/**
	 * Required constructor for Class.forName().newInstance()
	 */
	public WCDriver(){}

	/**
	 * Check if this driver supports the given url.
	 *
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	public boolean acceptsURL(String url) throws SQLException {
		boolean accept = false;

		if(url.regionMatches(true, 0, KEY_VID_DEFAULT, 0, KEY_VID_DEFAULT.length())){
			myConnectionType = Util.ID_DEFAULT;
			accept = true;
		}else if(url.regionMatches(true, 0, KEY_VID_MYSQL, 0, KEY_VID_MYSQL.length())){
			myConnectionType = Util.ID_MYSQL;
			accept = true;
		}else if(url.regionMatches(true, 0, KEY_VID_POSTGRESQL, 0, KEY_VID_POSTGRESQL.length())){
			myConnectionType = Util.ID_POSTGRESQL;
			accept = true;
		}
		return accept;
	}

	/**
	 *
	 *
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	public java.sql.Connection connect(String url, Properties info) throws SQLException {
		Connection connection = null;

		if(url != null && acceptsURL(url)){


			String fullJdbcUrl = url;
			synchronized(fullJdbcUrl){
				// if the props are part of the url, we convert them to a Properties object
				Properties urlProps = getPropsFromUrl(url, info);

				/* get the servers absolute url */
				String scriptUrl = urlProps.getProperty(KEY_URL);
				/* get the servers domain */
				String domainName = getDomainString(urlProps.getProperty(KEY_URL));

				String serverUser = urlProps.getProperty(KEY_USER);
				String serverPass = urlProps.getProperty(KEY_PASS);

				String wcDataBase = urlProps.getProperty(KEY_DB_NAME);
				String wcUser = urlProps.getProperty(KEY_DB_USER);
				String wcPass = urlProps.getProperty(KEY_DB_PASS);
				String wcCreds = (wcDataBase + wcUser + wcPass);

				int portNumber = 443;// fallback value for standard SSL connections
				try {
					portNumber = Integer.parseInt(urlProps.getProperty(KEY_PORT));
				} catch (NumberFormatException e) {
					System.err.println("Using fallback port " + portNumber + " for this Connection.");
					System.err.println("Please check the \"port\" is a whole number (EG: 80 or 443 or whateverPortYourServerUses)");
				}

				/* OPTIONAL PARAMETERS */
				Util.OUR_UA_FIX = "true".equals(urlProps.getProperty("useDummyAgent"));
				Util.OUR_DEBUG_MODE = "true".equals(urlProps.getProperty("debug"));

				if(Util.OUR_DEBUG_MODE){
					String debugLogger = urlProps.getProperty("debugLogger");
					int debugLevel;
					try {
						debugLevel = Integer.parseInt(urlProps.getProperty("debugLevel"));
					} catch (NumberFormatException e) {
						debugLevel = 0;//default if not specified
					}

					if("SimpleLog".equals(debugLogger)){
						//general setup
						System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
						System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");

						//informative debugging
						if(debugLevel >= 0){
							System.setProperty("org.apache.commons.logging.simplelog.log.jdbwc.core", "debug");
						}
						//basic debugging
						if(debugLevel >= 1){
							System.setProperty("org.apache.commons.logging.simplelog.log.jdbwc.util", "debug");
						}
						//good for connection debugging
						if(debugLevel == 2){
							System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
							System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
						}
						//overkill. Lots of output
						if(debugLevel == 3){
							System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
							System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
						}

					}else if("Jdk14Logger".equals(debugLogger)){

						System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");

						Properties loggerProps = new Properties();
						loggerProps.put(".level", "INFO");
						loggerProps.put("handlers", "java.util.logging.ConsoleHandler");
						loggerProps.put("java.util.logging.ConsoleHandler.formatter", "java.util.logging.SimpleFormatter");

						//informative debugging
						if(debugLevel >= 0){
							loggerProps.put("com.jdbwc.core.level", "FINEST");
						}

						//basic debugging
						if(debugLevel >= 1){
							loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
						}

						//good for connection debugging
						if(debugLevel == 2){
							loggerProps.put("httpclient.wire.header.level", "FINEST");
							loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
						}

						//overkill. Lots of output
						if(debugLevel == 3){
							loggerProps.put("httpclient.wire.level", "FINEST");
							loggerProps.put("org.apache.commons.httpclient.level", "FINEST");
						}

//						System.err.println(loggerProps.toString());
//						java.util.logging.Logger log = java.util.logging.Logger.getLogger("Driver");
						java.util.logging.LogManager logger = java.util.logging.LogManager.getLogManager();

						String loggerStr = loggerProps.toString().replace(", ", "\n");

						loggerStr = loggerStr.substring(1, loggerStr.length()-1);
//						System.err.println(loggerStr);

						try {
							InputStream ins = new ByteArrayInputStream(loggerStr.getBytes("UTF-8"));
							logger.readConfiguration(ins);

//							log.
						} catch (UnsupportedEncodingException e) {
							throw new SQLException("Could not configure java.util.logging.", e);
						} catch (SecurityException e) {
							throw new SQLException("Could not configure java.util.logging.", e);
						} catch (IOException e) {
							throw new SQLException("Could not configure java.util.logging.", e);
						}




					}else if("Log4JLogger".equals(debugLogger)){
						System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Log4JLogger");


					}
				}


				/* Fire-up a database connection with a database-server. */
				connection = new WCConnection(
						fullJdbcUrl,
						domainName,
						portNumber,
						scriptUrl,
						serverUser,
						serverPass,
						wcCreds,
						myConnectionType,
						wcDataBase);
			}
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
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String, java.util.Properties)
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {

		if (info == null) {
			info = new Properties();
		}
		info = getPropsFromUrl(url, info);


		DriverPropertyInfo urlProp = new DriverPropertyInfo(KEY_URL, info.getProperty(KEY_URL));
		urlProp.required = true;
		urlProp.description = "The absolute URL of the remote server (including http:// or https:// parts).";

		DriverPropertyInfo portProp = new DriverPropertyInfo(KEY_PORT, info.getProperty(KEY_PORT, "443"));
		portProp.required = true;
		portProp.description = "Server port number to connect to. Should be either the servers http or https port; Standard values are 80 and 443 respectively.";

		DriverPropertyInfo userProp = new DriverPropertyInfo(KEY_USER, info.getProperty(KEY_USER));
		userProp.required = true;
		userProp.description = "Server username";

		DriverPropertyInfo passProp = new DriverPropertyInfo(KEY_PASS, info.getProperty(KEY_PASS));
		passProp.required = true;
		passProp.description = "Server password";

		DriverPropertyInfo dbNameProp = new DriverPropertyInfo(KEY_DB_NAME, info.getProperty(KEY_DB_NAME));
		dbNameProp.required = true;
		dbNameProp.description = "Database name";

		DriverPropertyInfo dbUserProp = new DriverPropertyInfo(KEY_DB_USER, info.getProperty(KEY_DB_USER));
		dbUserProp.required = true;
		dbUserProp.description = "Database user name";

		DriverPropertyInfo dbPassProp = new DriverPropertyInfo(KEY_DB_PASS, info.getProperty(KEY_DB_PASS));
		dbPassProp.required = true;
		dbPassProp.description = "Database password";

		DriverPropertyInfo[] dpiProps = new DriverPropertyInfo[7];

		dpiProps[0] = urlProp;
		dpiProps[1] = portProp;
		dpiProps[2] = userProp;
		dpiProps[3] = passProp;
		dpiProps[4] = dbNameProp;
		dpiProps[5] = dbUserProp;
		dpiProps[6] = dbPassProp;

		return dpiProps;
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
		return WC_MINOR_BUILD;
	}

	public final String getVersionName() {
		return WC_VER_NAME;
	}

	public final String getVersionString() {
		return getStaticVersion();
	}


	/* *****************************************************
	 * Private methods used by this class
	 ***************************************************** */
	private final static String getStaticVersion(){
		return new StringBuilder()
			.append(WC_MAJOR_VER)
			.append('.').append(WC_MINOR_VER)
			.append('.').append(WC_MINOR_BUILD)
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
		/* remove unwanted subdomain parts.
		 * Specific subdomains are OK.
		 * EG: stuff.domain.ext */
		domainString = domainString.toLowerCase();
		domainString = domainString.replaceFirst("www.", "");
		/* get start of folder part */
		int domainEnd = domainString.indexOf('/');
		/* remove folder portion from domain */
		domainString = domainString.substring(0, domainEnd);

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
