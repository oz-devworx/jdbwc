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
package com.jdbwc.iface;

import com.ozdevworx.dtype.DataHandler;

/**
 * Vendor based extension.<br />
 * This interface is for any extension methods we require over the
 * standard <code>java.sql.ResultSet</code> interface to help make our driver work.<br />
 * <br />
 * <span style="color:red;">
 * This interface and its method/s are not designed for direct public consumption.</span><br />
 * The purpose of this interface is to expand functionality to assist the driver in doing its job internally.<br />
 * Declaring any additional public methods here makes it easier to work with the actual implementation.<br />
 * <i>EG: you can easily tell whats generic to <code>java.sql.ResultSet</code> and whats an extension.</i>
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 */
public abstract interface ResultSet extends java.sql.ResultSet {

	/**
	 * Adds a new data row to this ResultSet Object.
	 *
	 * @param row The new row to add to this ResultSet.
	 */
	public abstract void addRow(DataHandler row);
}
