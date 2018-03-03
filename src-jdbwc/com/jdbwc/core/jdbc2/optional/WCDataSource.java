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
package com.jdbwc.core.jdbc2.optional;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.jdbwc.core.Driver;
import com.jdbwc.core.WCDriver;
import com.jdbwc.core.WCDriverPropertiesInfo;

/**
 * A DataSource implementation for setting and creating
 * new java.sql.Connections using the JDBWC Driver.
 *
 * @author Tim Gall
 * @version 2010-05-26
 */
public class WCDataSource implements DataSource {

	// Standard Data Source Properties
////	databaseName

////	dataSourceName
////	description

//	networkProtocol
//	portNumber


//	roleName
//	serverName

//	user
//	password

	private transient PrintWriter logWriter = null;
	private transient String dataSourceName = "";// unique DataSource ID
	private transient String description = "";// DataSource description

	/*
	 *
	 *
	 * TODO:
	 * redo variables and methods to closely match the new Driver properties
	 *
	 *
	 */
	private transient String hostUrl = "http://localhost:80/";
	private transient String hostUser = "";
	private transient String hostPass = "";

	private transient String databaseName = "";
	private transient String databaseUser = "";
	private transient String databasePass = "";

	private transient String databaseType = null;
	private transient String jdbcUrl = "jdbc:jdbwc://";

	private transient String proxyServer = "";
	private transient int timeOut = 10;
	private boolean dummyUserAgent = false;
	private boolean nvSSL = false;

	private boolean debug = false;
	private int debugLevel = 0;
	private String debugger = "";

	private transient String fullUrl = jdbcUrl + databaseType + "://" + hostUrl;
	private transient boolean useCachedUrl = false;

	/** The driver to create connections with */
	private static Driver driver = null;


	static {
		try {
			driver = (Driver) Class.forName(Driver.class.getName()).newInstance();
		} catch (final Exception E) {
			throw new RuntimeException(
					"Can't load Driver class " + Driver.class.getName());
		}
	}

	/**
	 *
	 */
	public WCDataSource() {

	}

	/**
	 * @see javax.sql.DataSource#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		Properties props = new Properties();

		props.put(WCDriver.KEY_URL, getHostUrl());
		props.put(WCDriver.KEY_USER, getHostUser());
		props.put(WCDriver.KEY_PASS, this.hostPass);

		props.put(WCDriver.KEY_PROXY_URL, getProxyServer());

		props.put(WCDriver.KEY_DB_NAME, getDatabaseName());
		props.put(WCDriver.KEY_DB_USER, getDatabaseUser());
		props.put(WCDriver.KEY_DB_PASS, this.databasePass);

		props.put(WCDriver.KEY_DEBUG, String.valueOf(getDebug()));
		props.put(WCDriver.KEY_DEBUG_LEVEL, String.valueOf(getDebugLevel()));
		props.put(WCDriver.KEY_DEBUG_LOG, getDebugger());

		props.put(WCDriver.KEY_TIMEOUT, String.valueOf(getLoginTimeout() * 1000));
		props.put(WCDriver.KEY_USE_UA, String.valueOf(getDummyUserAgent()));
		props.put(WCDriver.KEY_NV_SSL, String.valueOf(getNvSSL()));


		props = (new WCDriverPropertiesInfo(null)).getClientInfoProps(props);

		return driver.connect(getFullUrl(), props);
	}

	/**
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection getConnection(final String username, final String password) throws SQLException {
		setHostUser(username);
		setHostPass(password);

		return getConnection();
	}

	/**
	 * @return the databaseName
	 */
	public final String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @return the databaseType
	 */
	public final String getDatabaseType() {
		return databaseType;
	}

	/**
	 * @return the databaseUser
	 */
	public final String getDatabaseUser() {
		return databaseUser;
	}

	/**
	 * @return the dataSourceName
	 */
	public final String getDataSourceName() {
		return dataSourceName;
	}

	/**
	 * @return the debug
	 */
	public final boolean getDebug() {
		return debug;
	}

	/**
	 * @return the debugger
	 */
	public final String getDebugger() {
		return debugger;
	}

	/**
	 * @return the debugLevel
	 */
	public final int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @return the dummyUserAgent
	 */
	public final boolean getDummyUserAgent() {
		return dummyUserAgent;
	}

	/**
	 * @return the fullUrl
	 */
	public final String getFullUrl() {
		if (!this.useCachedUrl) {
			final StringBuilder fullUrlBuild = new StringBuilder(64);
			fullUrlBuild.append("jdbc:jdbwc:").append(getDatabaseType()).append("//");

			fullUrlBuild.append(getHostUrl())
				.append("?")
				.append(WCDriver.KEY_USER).append("=")
				.append(getHostUser()).append("&")

				.append(WCDriver.KEY_PASS).append("=")
				.append(this.hostPass).append("&")

				.append(WCDriver.KEY_DB_NAME).append("=")
				.append(getDatabaseName()).append("&")

				.append(WCDriver.KEY_DB_USER).append("=")
				.append(getDatabaseUser()).append("&")

				.append(WCDriver.KEY_DB_PASS).append("=")
				.append(this.databasePass);

			return fullUrlBuild.toString();
		}

		return this.fullUrl;
	}

	/**
	 * @return the hostUrl
	 */
	public final String getHostUrl() {
		return hostUrl;
	}

	/**
	 * @return the hostUser
	 */
	public final String getHostUser() {
		return hostUser;
	}

	/**
	 * @return the jdbcUrl
	 */
	public final String getJdbcUrl() {
		return "jdbc:jdbwc:" + getDatabaseType() + "//";
	}

	/**
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		return this.timeOut;
	}

	/**
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.logWriter;
	}

	/**
	 * @return the nvSSL
	 */
	public final boolean getNvSSL() {
		return nvSSL;
	}

	/**
	 * @return the proxyServer
	 */
	public final String getProxyServer() {
		return proxyServer;
	}

//	/**
//	 *
//	 * @return a javax.naming.Reference Object
//	 * @throws NamingException
//	 */
//	public Reference getReference() throws NamingException {
//		final Reference ref = new Reference(getClass().getName(), WCDataSourceFactory.class.getName(), null);
//
//		ref.add(new StringRefAddr(WCDriver.KEY_DB_USER, getHostUser()));
//		ref.add(new StringRefAddr(WCDriver.KEY_DB_PASS, this.hostPass));
//		ref.add(new StringRefAddr(WCDriver.KEY_URL, getHostUrl()));
//		ref.add(new StringRefAddr(WCDriver.KEY_DB_NAME, getDatabaseName()));
//		ref.add(new StringRefAddr(WCDriver.KEY_DB_USER, getDatabaseUser()));
//		ref.add(new StringRefAddr(WCDriver.KEY_DB_PASS, this.databasePass));
//		ref.add(new StringRefAddr("fullUrl", getFullUrl()));
//
//		return ref;
//	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		return iface.isInstance(this);
	}

	/**
	 * @param databaseName the databaseName to set
	 */
	public final void setDatabaseName(final String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @param databasePass the databasePass to set
	 */
	public final void setDatabasePass(final String databasePass) {
		this.databasePass = databasePass;
	}

	/**
	 * @param databaseType the databaseType to set
	 */
	public final void setDatabaseType(final String databaseType) {
		this.databaseType = databaseType;
	}

	/**
	 * @param databaseUser the databaseUser to set
	 */
	public final void setDatabaseUser(final String databaseUser) {
		this.databaseUser = databaseUser;
	}

	/**
	 * @param dataSourceName the dataSourceName to set
	 */
	public final void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/**
	 * @param debug the debug to set
	 */
	public final void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * @param debugger the debugger to set
	 */
	public final void setDebugger(String debugger) {
		this.debugger = debugger;
	}

	/**
	 * @param debugLevel the debugLevel to set
	 */
	public final void setDebugLevel(int debugLevel) {
		this.debugLevel = debugLevel;
	}

	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param dummyUserAgent the dummyUserAgent to set
	 */
	public final void setDummyUserAgent(boolean dummyUserAgent) {
		this.dummyUserAgent = dummyUserAgent;
	}

	/**
	 * @param fullUrl the fullUrl to set
	 */
	public final void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
		this.useCachedUrl = true;
	}

	/**
	 * @param hostPass the hostPass to set
	 */
	public final void setHostPass(final String hostPass) {
		this.hostPass = hostPass;
	}

	/**
	 * @param hostUrl the hostUrl to set
	 */
	public final void setHostUrl(final String hostUrl) {
		this.hostUrl = hostUrl;
	}

	/**
	 * @param hostUser the hostUser to set
	 */
	public final void setHostUser(final String hostUser) {
		this.hostUser = hostUser;
	}

	/**
	 * @param jdbcUrl the jdbcUrl to set
	 */
	public final void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	/**
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	@Override
	public void setLoginTimeout(final int seconds) throws SQLException {
		this.timeOut = seconds;
	}

	/**
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setLogWriter(final PrintWriter out) throws SQLException {
		this.logWriter = out;
	}

	/**
	 * @param nvSSL the nvSSL to set
	 */
	public final void setNvSSL(boolean nvSSL) {
		this.nvSSL = nvSSL;
	}

	/**
	 * @param proxyServer the proxyServer to set
	 */
	public final void setProxyServer(String proxyServer) {
		this.proxyServer = proxyServer;
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(final Class<T> iface) throws SQLException {
		return iface.cast(this);
	}

}
