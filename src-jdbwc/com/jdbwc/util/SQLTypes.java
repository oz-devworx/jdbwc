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


/**
 * Provides access to the database specific Type jugglers.
 *
 * @author Tim Gall
 * @version 2010-05-31
 */
public interface SQLTypes {

	String nativeNameToJdbcName(String nativeName);

	int nativeNameToJdbcType(final String nativeName);

	/**
	 * Maps the given Native SQL type name to its Native SQL type id.
	 *
	 * @param nativeName String - the Native SQL type name
	 * @return int - Native SQL type id
	 */
	int nativeNameToNativeType(String nativeName);

	/**
	 * Maps the given Native SQL type to the equivalent java.sql.Types name.
	 */
	String nativeTypeToJdbcName(final int nativeType);

	/**
	 * Maps the given Native SQL type to the equivalent JDBC type.
	 */
	int nativeTypeToJdbcType(final int nativeType);

	/**
	 * Maps Native SQL type id's to thier type name.
	 *
	 * @param nativeType int - Native SQL type id
	 * @return String - the Native SQL type name
	 */
	String nativeTypeToNativeName(final int nativeType);

	SQLField updateField(SQLField field);
}
