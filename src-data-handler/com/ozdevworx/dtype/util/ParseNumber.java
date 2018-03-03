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
package com.ozdevworx.dtype.util;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.ozdevworx.dtype.impl.IlegalNumberTypeException;

/**
 * Persistant number casting class.<br />
 * Throws a NumberFormatException wrapped in an SQLException if a value cannot be parsed into a valid number.
 * 
 * @author Tim Gall, 2008-07-01
 * @author Tim Gall, 2010-04-10
 * @version 1.0.0.1
 */
public class ParseNumber {

	public static BigDecimal getBigDecimal(Object value) throws IlegalNumberTypeException {
		return getBigDecimal(String.valueOf(value));
	}

	public static BigDecimal getBigDecimal(String textValue) throws IlegalNumberTypeException {
		BigDecimal val;
		try {
			val = BigDecimal.valueOf(Double.parseDouble(textValue));
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static BigInteger getBigInteger(Object value) throws IlegalNumberTypeException {
		return getBigInteger(String.valueOf(value));
	}

	public static BigInteger getBigInteger(String textValue) throws IlegalNumberTypeException {
		BigInteger val;
		try {
			val = BigInteger.valueOf(Long.parseLong(textValue));
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static double getDouble(Object value) throws IlegalNumberTypeException {
		return getDouble(String.valueOf(value));
	}

	public static double getDouble(String textValue) throws IlegalNumberTypeException {
		double val;

		try {
			val = Double.parseDouble(textValue.trim());
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static float getFloat(Object value) throws IlegalNumberTypeException {
		return getFloat(String.valueOf(value));
	}

	public static float getFloat(String textValue) throws IlegalNumberTypeException {
		float val;
		try {
			val = Float.parseFloat(textValue.trim());
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static int getInt(Object value) throws IlegalNumberTypeException {
		return getInt(String.valueOf(value));
	}

	public static int getInt(String textValue) throws IlegalNumberTypeException {
		int val;

		try {
			val = Integer.parseInt(textValue.trim());
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static Long getLong(Object value) throws IlegalNumberTypeException {
		return getLong(String.valueOf(value));
	}

	public static Long getLong(String textValue) throws IlegalNumberTypeException {
		Long val;

		try {
			val = Long.parseLong(textValue.trim());
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}

	public static Short getShort(Object value) throws IlegalNumberTypeException {
		return getShort(String.valueOf(value));
	}

	public static Short getShort(String textValue) throws IlegalNumberTypeException {
		Short val;

		try {
			val = Short.parseShort(textValue.trim());
		} catch (NumberFormatException e) {
			throw new IlegalNumberTypeException("Encountered a NumberFormatException parsing " + textValue, e);
		}

		return val;
	}
}
