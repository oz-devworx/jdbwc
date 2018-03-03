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
package com.jdbwc.core;

import java.sql.SQLException;

/**
 * Connection implementation helper for database specific requirements.<br />
 *
 * @author Tim Gall
 * @version 31/05/2010
 */
public interface DBConnection {

	boolean getAutoCommit(WCStatement statement) throws SQLException;

	/**
	 *
	 * @return int. One of:
	 * <ul>
	 * <li>-1 = lowercase</li>
	 * <li> 0 = mixed-case</li>
	 * <li> 1 = uppercase</li>
	 * </ul>
	 */
	int getCaseSensitivity();

	String getTransactionIsolation(WCStatement statement) throws SQLException;

	void setAutoCommit(WCStatement statement, boolean autoCommit) throws SQLException;

	void setReadOnly(WCStatement statement, boolean readOnly) throws SQLException;

	void setTransactionIsolation(WCStatement statement, String levelName) throws SQLException;
}
