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
package com.ozdevworx.dtype;

import com.ozdevworx.dtype.impl.IlegalNumberTypeException;

/**
 * <b style="color:red;">IMPORTANT NOTE:</b> implementations should be synchronised.<br /><br />
 * This is a Data-Type interface for an indexed and labelled array
 * of Objects similar to the PHP array concept.<br />
 * Data should be stored with a String label.<br />
 * <br />
 * Originally designed for handling resultSet data and query building in database intensive desktop applications.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 1.0.0-7
 * @version 1.0.0-8
 */
public interface DataHandler extends ObjectArray {

	static final String VERSION = "1.0.0-8";

	/**
	 *
	 * @param key The keys name to locate
	 * @param data The data to find a match for
	 * @return The data Object at key location (or null if none found)
	 */
	Object getObjectByElement(final String key, final String data);

	/**
	 *
	 * @param key The keys name to locate
	 * @param data The data to find a match for
	 * @return The Objects index at key location (or -1 if none found)
	 */
	int getIndexByElement(final String key, final String data);

	/**
	 * Alias function for getObject(int i)
	 *
	 * @param i int - keys index
	 * @return a data Object based on its key index i
	 */
	Object getData(int i);

	/**
	 * Alias function for getObject(String n)
	 *
	 * @param n String - key name
	 * @return a data Object based on its keys name n
	 */
	Object getData(String n);

	/**
	 * Get all key indexes in this set from a key name String.
	 *
	 * @param n String - key name
	 * @return int[] with the matching key-indexes in this DataHandler matching n (keyName)
	 */
	int[] getIndexes(String n);

	/**
	 * Get the value at index i as a Double or throw an error if the data is not valid for this java Type.
	 *
	 * @param i The columns index to seek.
	 * @return Double value located at column index i.
	 * @throws IlegalNumberTypeException if value at i is not a Double.
	 */
	double getDouble(int i) throws IlegalNumberTypeException;

	/**
	 * Get the value at name n as a Double or throw an error if the data is not valid for this java Type.
	 *
	 * @param n The columns name to seek.
	 * @return Double value located at column name n.
	 * @throws IlegalNumberTypeException if value at n is not a Double.
	 */
	double getDouble(String n) throws IlegalNumberTypeException;

	/**
	 * Get the value at index i as an Integer or throw an error if the data is not valid for this java Type.
	 *
	 * @param i The columns index to seek.
	 * @return Integer value located at column index i.
	 * @throws IlegalNumberTypeException if value at i is not an Integer.
	 */
	int getInt(int i) throws IlegalNumberTypeException;

	/**
	 * Get the value at name n as an Integer or throw an error if the data is not valid for this java Type.
	 *
	 * @param n The columns name to seek.
	 * @return Integer value located at column name n.
	 * @throws IlegalNumberTypeException if value at n is not an Integer.
	 */
	int getInt(String n) throws IlegalNumberTypeException;

	/**
	 * Get the value at index i as a Float or throw an error if the data is not valid for this java Type.
	 *
	 * @param i The columns index to seek.
	 * @return Float value located at column i.
	 * @throws IlegalNumberTypeException if value at i is not a Float.
	 */
	float getFloat(int i) throws IlegalNumberTypeException;

	/**
	 * Get the value at name n as a Float or throw an error if the data is not valid for this java Type.
	 *
	 * @param n The columns name to seek.
	 * @return Float value located at column name n.
	 * @throws IlegalNumberTypeException if value at n is not a Float.
	 */
	float getFloat(String n) throws IlegalNumberTypeException;

	/**
	 * Get the value at index i as a Long or throw an error if the data is not valid for this java Type.
	 *
	 * @param i The columns index to seek.
	 * @return Long value located at column i.
	 * @throws IlegalNumberTypeException if value at i is not a Long.
	 */
	long getLong(int i) throws IlegalNumberTypeException;

	/**
	 * Get the value at name n as a Long or throw an error if the data is not valid for this java Type.
	 *
	 * @param n The columns name to seek.
	 * @return Long value located at column name n.
	 * @throws IlegalNumberTypeException if value at i is not a Long.
	 */
	long getLong(String n) throws IlegalNumberTypeException;

	/**
	 * Get the value at index i as a Short or throw an error if the data is not valid for this java Type.
	 *
	 * @param i The columns index to seek.
	 * @return Short value located at column i.
	 * @throws IlegalNumberTypeException if value at i is not a Short.
	 */
	Short getShort(int i) throws IlegalNumberTypeException;

	/**
	 * Get the value at name n as a Short or throw an error if the data is not valid for this java Type.
	 *
	 * @param n The columns name to seek.
	 * @return Short value located at column name n.
	 * @throws IlegalNumberTypeException if value at n is not a Short.
	 */
	Short getShort(String n) throws IlegalNumberTypeException;

	/**
	 * Update the Element at name key with a new key and new data
	 * or add a new Element if name key doesnt exist.<br /><br />
	 * NOTE:<br />
	 * Use <code>addData(String n, Object d)</code> to add new data or data with duplicate keys to this DataHandler.
	 *
	 * @param key The first Element with name matching key.
	 * @param newKey the new key for the Element.
	 * @param newObj the Data to update the Element with.
	 */
	void setData(String key, String newKey, Object newObj);

	/**
	 * Update the Element at index with a new key and new data.<br />
	 * No action is taken if index is greater than this DataHandlers size.
	 *
	 * @param index The Element at index we want to update.
	 * @param newKey the new key for the Element.
	 * @param newObj the Data to update the Element with.
	 */
	void setData(int index, String newKey, Object newObj);

}