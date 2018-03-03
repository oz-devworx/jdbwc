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
package com.jdbwc.iface;

import org.apache.commons.httpclient.HttpClient;


/**
 * Vendor based extension.<br />
 * This interface is for any extension methods we require over the
 * standard <code>java.sql.Connection</code> interface to help make our driver work.<br />
 * <br />
 * <span style="color:red;">
 * This interface and its method/s are not designed for direct public consumption.</span><br />
 * The purpose of this interface is to expand functionality to assist the driver in doing its job internally.<br />
 * Declaring any additional public methods here makes it easier to work with the actual implementation.<br />
 * <i>EG: you can easily tell whats generic to <code>java.sql.Connection</code> and whats an extension.</i>
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public abstract interface Connection extends java.sql.Connection {

	/**
	 * Get the database credentials required to gain access to the remote database server.
	 *
	 * @return A one way hash that includes this Connections database login details
	 * as supplied when this Connection was created.
	 */
	public abstract String getCredentials();

	/**
	 * Returns the database's numeric type as defined in this Connections Driver class.<br />
	 * The numeric type is set based on the request url used to create this Connection.
	 *
	 * @return The numeric typeName for this Connection.
	 */
	public abstract int getDbType();

	/**
	 * Gets the remote URL this Connection is connected to.
	 *
	 * @return The URL this Connection instance is using.
	 */
	public abstract String getUrl();

	/**
	 *
	 * @return this HttpClient
	 */
	public abstract HttpClient getClient();

	/**
	 *
	 * @return get the current timeout in ms
	 */
	public abstract int getTimeOut();

	/**
	 *
	 * @param timeOut the timeout period in ms
	 */
	public abstract void setTimeOut(int timeOut);

	/**
	 *
	 * @return current Session limit
	 */
	public abstract boolean getSessLimit();

	/**
	 *
	 * @param sessLimit
	 */
	public abstract void setSessLimit(boolean sessLimit);

	/**
	 * Returns the name of the requested database that was used when this Connection was created.
	 *
	 * @return the active database's name that this Connection Object is using.
	 */
	public abstract String getDatabase();
}
