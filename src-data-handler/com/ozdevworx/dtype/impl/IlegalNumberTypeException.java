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
package com.ozdevworx.dtype.impl;

import java.sql.SQLException;

/**
 * @author Tim Gall
 * @version 2010-04-10
 */
public class IlegalNumberTypeException extends SQLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param reason A message describing the error
	 */
	public IlegalNumberTypeException(String reason) {
		super(reason);
		
	}
	/**
	 * @param reason A message describing the error
	 * @param cause The cause
	 */
	public IlegalNumberTypeException(String reason, Throwable cause) {
    	super(reason,cause);
    }
}
