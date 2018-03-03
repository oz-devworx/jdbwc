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
package com.jdbwc.core.util;

/**
 * Simple class for getting condition keywords 'WHERE' and 'AND'
 * in the correct order. Allows for more flexibility with metadata query building.
 *
 * @author Tim Gall
 * @version 18/05/2010
 */
public class ConditionKeyWord {

	private boolean usedWhere;

	public ConditionKeyWord(){
		usedWhere = false;
	}

	/**
	 *
	 * @return SQL keyword 'WHERE' or 'AND' in the correct order and quantity.
	 */
	public String getKeyWord(){
		String ret = usedWhere ? "AND " : "WHERE ";
		usedWhere = true;
		return ret;
	}

}
