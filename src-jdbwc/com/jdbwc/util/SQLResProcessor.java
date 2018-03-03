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

import com.jdbwc.core.WCResultSet;

/**
 * Process Internal ResultSetMetadata.
 *
 * @author Tim Gall
 * @version 2010-06-06
 */
public interface SQLResProcessor {

	/**
	 * Process metadata to suit the current database type.
	 * The metadata to be processed is usually retreved in the same exchange as the ResultSet data.
	 *
	 * @param catalog String - Current catalog name used by the parent connection.
	 * @param metaRes WCResultSet - A ResultSet conatining metadata. Usually retreved with a data resultset.
	 * @return SQLField[] containing internal metadata
	 * @throws SQLException if an error occurs while accessing metaRes
	 */
	SQLField[] getFields(String catalog, WCResultSet metaRes) throws SQLException;
}
