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

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;

import com.jdbwc.exceptions.NotImplemented;

/**
 * NOTE: This is only a skeleton and has not been implemented yet!!!
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-06-09
 */
public class WCSQLData implements SQLData {

	/**
	 * Constructs a new instance of this.
	 */
	protected WCSQLData() {
		// TODO implement me!
	}

	/* (non-Javadoc)
	 * @see java.sql.SQLData#getSQLTypeName()
	 */
	public String getSQLTypeName() throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/* (non-Javadoc)
	 * @see java.sql.SQLData#readSQL(java.sql.SQLInput, java.lang.String)
	 */
	public void readSQL(SQLInput stream, String typeName) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/* (non-Javadoc)
	 * @see java.sql.SQLData#writeSQL(java.sql.SQLOutput)
	 */
	public void writeSQL(SQLOutput stream) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

}
