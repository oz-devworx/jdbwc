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
package com.jdbwc.exceptions;

import java.sql.SQLException;

public class DataException extends SQLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DataException() {
		super();
	}

	public DataException(String reason, String SQLState, int vendorCode) {
		super(reason, SQLState, vendorCode);
	}

	public DataException(String reason, String SQLState) {
		super(reason, SQLState);
	}

	public DataException(String reason) {
		super(reason);
	}
}
