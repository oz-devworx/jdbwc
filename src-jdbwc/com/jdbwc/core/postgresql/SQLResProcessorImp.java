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
package com.jdbwc.core.postgresql;

import java.sql.SQLException;

import com.jdbwc.core.WCResultSet;
import com.jdbwc.util.SQLField;

/**
 * Process Internal ResultSetMetadata for PostgreSQL databases.
 *
 * @author Tim Gall
 * @version 2010-06-06
 */
public class SQLResProcessorImp implements com.jdbwc.util.SQLResProcessor {

	/**
	 *
	 */
	public SQLResProcessorImp() {

	}

	/**
	 * @see com.jdbwc.util.SQLResProcessor#getFields(java.lang.String, com.jdbwc.core.WCResultSet)
	 */
	@Override
	public SQLField[] getFields(String catalog, WCResultSet metaRes) throws SQLException {
		final SQLField[] metaFields = new SQLField[metaRes.myRows.length()];

		//This release does not support this type of metadata.
		//An alternative system is used to retrieve PostgreSQL metadata at the moment.

		metaRes.close();

		return metaFields;
	}

}
