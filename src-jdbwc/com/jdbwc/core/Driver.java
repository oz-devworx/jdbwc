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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Required JDBC-API implementation for java.sql.Driver.<br />
 * <br />
 * Its advised to keep this class as small as possible
 * to reduce memory footprint size when the Driver is registered with the JRE (before use).<br />
 * Once the Driver is put into use it will use the methods in WCDriver
 * to determine suitability for a given URL and start a connection if its suitable.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-05-21 added implements keyword to class declaration
 */
public class Driver extends WCDriver implements java.sql.Driver {
	/* The static portion is required for any JDBC Driver implementation.
	 * It allows us to register ourselves with the DriverManager.
	 * This is required to construct a new driver instance.
	 */
	static {
		try {
			java.sql.DriverManager.registerDriver(new Driver());
		} catch (SQLException e) {
			throw new RuntimeException("Can't register driver!");
		}
	}

	/**
	 * Construct a new driver instance for Class.forName().newInstance()
	 *
	 * @throws SQLException if a database error occurs.
	 */
	public Driver() throws SQLException {
		super();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
