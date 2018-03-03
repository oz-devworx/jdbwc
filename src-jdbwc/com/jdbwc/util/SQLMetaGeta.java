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
package com.jdbwc.util;

import java.sql.SQLException;

import com.ozdevworx.dtype.ObjectArray;

/**
 * Provides access to the database specific meta organisers.<br />
 * For ResultSetMetaData this is largely a fallback system or used for
 * databases that are difficult to extract metadata from.<br />
 * For ParameterMetaData this is the primary access point.
 *
 * @author Tim Gall
 * @version 2010-05-31
 */
public interface SQLMetaGeta {

	/**
	 * @param sql String - The query that triggered this method call.
	 * @param columns ObjectArray - The columLabel to columnNames from the query.
	 * @return SQLField[] of MetaData
	 * @throws SQLException
	 */
	SQLField[] getResultSetMetaData(
			String sql,
			ObjectArray columns)
	throws SQLException;

	/**
	 *
	 * @param tableNames
	 * @param columns
	 * @param params
	 * @return array of SQLField MetaData
	 * @throws SQLException
	 */
	SQLField[] getParameterMetaData(
			ObjectArray tableNames,
			ObjectArray columns,
			ObjectArray params)
	throws SQLException;
}
