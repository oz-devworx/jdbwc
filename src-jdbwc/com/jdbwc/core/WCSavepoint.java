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
import java.sql.Savepoint;

/**
 * java.sql.Savepoint implementation.
 *
 * @author Tim Gall
 * @version 2010-04-23
 */
public class WCSavepoint implements Savepoint {

	private int savepointId;
	private final String savepointName;


	/**
	 *
	 */
	public WCSavepoint() {
		generateId();
		savepointName = generateName();
	}

	/**
	 * @param name Name of the savepoint
	 * @throws SQLException
	 */
	public WCSavepoint(String name) throws SQLException {
		if(name==null || name.trim().isEmpty()){
			throw new SQLException("Savepoint name must contain a value.");
		}
		generateId();
		savepointName = name;
	}

	/**
	 * @see java.sql.Savepoint#getSavepointId()
	 */
	@Override
	public int getSavepointId() throws SQLException {
		return savepointId;
	}

	/**
	 * @see java.sql.Savepoint#getSavepointName()
	 */
	@Override
	public String getSavepointName() throws SQLException {
		return savepointName;
	}

	private String generateName(){
		return "SP_" + savepointId;
	}

	private void generateId(){
		savepointId = (int)java.lang.System.currentTimeMillis();
	}
}
