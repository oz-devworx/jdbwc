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
package com.jdbwc.core.mysql;

import java.sql.SQLException;

import com.jdbwc.core.WCResultSet;
import com.jdbwc.core.WCStatement;
import com.jdbwc.util.Util;

/**
 * Connection implementation helper for MySQL specific requirements.
 *
 * @author Tim Gall
 * @version 2010-05-31
 */
public class DBConnectionImp implements com.jdbwc.core.DBConnection {

	/**
	 *
	 */
	public DBConnectionImp() {
	}

	/**
	 * @see com.jdbwc.core.DBConnection#getAutoCommit(com.jdbwc.core.WCStatement)
	 */
	@Override
	public boolean getAutoCommit(final WCStatement statement) throws SQLException {
		boolean autocommit = true;

		WCResultSet results = null;
		results = statement.executeQuery("select @@autocommit;");
		if(results.next()){
			autocommit = results.getInt(1)==0 ? false:true;
		}
		results.close();
		statement.close();

		return autocommit;
	}

	/**
	 *
	 * @return int. One of:
	 * <ul>
	 * <li>-1 = lowercase</li>
	 * <li> 0 = mixed-case</li>
	 * <li> 1 = uppercase</li>
	 * </ul>
	 */
	@Override
	public final int getCaseSensitivity() {
		return Util.CASE_MIXED;
	}

	/**
	 * @see com.jdbwc.core.DBConnection#getTransactionIsolation(com.jdbwc.core.WCStatement)
	 */
	@Override
	public String getTransactionIsolation(final WCStatement statement) throws SQLException {
		String isolation = null;
		WCResultSet results = statement.executeQuery("SELECT @@SESSION.tx_isolation;");

		if(results.next()){
			isolation = results.getString(1);
		}
		results.close();
		statement.close();

		return isolation;
	}

	/**
	 * @see com.jdbwc.core.DBConnection#setAutoCommit(com.jdbwc.core.WCStatement, boolean)
	 */
	@Override
	public void setAutoCommit(final WCStatement statement, final boolean autoCommit) throws SQLException {
		statement.execute("SET autocommit=" + (autoCommit ? 1:0) + ";");
		statement.close();
	}

	/**
	 * @see com.jdbwc.core.DBConnection#setReadOnly(com.jdbwc.core.WCStatement, boolean)
	 */
	@Override
	public void setReadOnly(final WCStatement statement, final boolean readOnly) throws SQLException {
		statement.executeQuery("SET GLOBAL read_only="+readOnly+";");
		statement.close();
	}

	/**
	 * @see com.jdbwc.core.DBConnection#setTransactionIsolation(com.jdbwc.core.WCStatement, java.lang.String)
	 */
	@Override
	public void setTransactionIsolation(final WCStatement statement, final String levelName) throws SQLException {
		// TODO: need to check the table type, if myisam then notify user in an exception???
		// or just set the level anyway and leave it up to the user to know this???
		// For now, just set the level regardless of table type.
		statement.execute("SET SESSION TRANSACTION ISOLATION LEVEL " + levelName + ";");
		statement.close();
	}
}
