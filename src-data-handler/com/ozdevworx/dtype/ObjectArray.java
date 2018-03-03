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
 * @version 1.0.0-9
 */
public interface ObjectArray {

	static final String VERSION = "1.0.0-9";
	/**
	 * Adds Elements to this ObjectArray Object.<br />
	 * If the key is null or the equivalent of,
	 * the Element will not be added to this ObjectArray.<br /><br />
	 * NOTE:<br />
	 * Use <code>setData(String n, Object d)</code> to modify existing data by key<br />
	 * or <code>setData(int i, Object d)</code> to modify existing data by index.
	 *
	 * @param n String - key name
	 * @param d Object - data to add to this ObjectArray.
	 */
	void addData(String n, Object d);

	/**
	 * Resets this ObjectArray.
	 */
	void clearData();

	/**
	 * Counts the Elements with key names matching name n in this ObjectArray.
	 *
	 * @param n The keys name to seek
	 * @return the number of matching keys in this ObjectArray
	 */
	int countMatches(String n);

	/**
	 * Does this key exist in the set?
	 *
	 * @param value key to search the set for.
	 * @return true if the key is found, else false.
	 */
	boolean hasKey(String value);

	/**
	 * Does this key exist in the set?
	 *
	 * @param index key index to search the set for.
	 * @return true if the key is found, else false.
	 */
	boolean hasKey(int index);

	/**
	 * Does this ObjectArray Element exist in the set?
	 *
	 * @param key The Elements key String
	 * @param data The Elements data Object as a String
	 * @return true if this Element is found, else false.
	 */
	boolean hasElement(String key, String data);

	/**
	 * Get a keys index in this set from its name String
	 *
	 * @param n String - keys name
	 * @return The keys index.
	 */
	int getIndex(String n);

	/**
	 * Get the keys name at index i.
	 *
	 * @param i The columns index to seek.
	 * @return the keys name
	 */
	String getKey(int i);

	/**
	 * Count all Elements in this ObjectArray.
	 *
	 * @return the size of this ObjectArray
	 */
	int length();

	/**
	 * Get the Object at column index i as a String.
	 * @param i The columns index to seek.
	 * @return a data String based on its keys index
	 */
	String getString(int i);

	/**
	 * Get the Object at column name n as a String.
	 *
	 * @param n The columns name to seek.
	 * @return a data String based on its keys name
	 */
	String getString(String n);

	/**
	 * Get the Object at column index i.
	 *
	 * @param i The columns index to seek.
	 * @return Object located at column i.
	 */
	Object getObject(int i);

	/**
	 * Get the Object at column name n.
	 *
	 * @param n The columns name to seek.
	 * @return Object located at column name n.
	 */
	Object getObject(String n);

	/**
	 * Determines if this ObjectArray contains Elements.
	 *
	 * @return true if this ObjectArray does not contain Elements else returns false
	 */
	boolean isEmpty();

	/**
	 * Removes the Element at index and shrinks this ObjectArray
	 * so that Element index+1 will be positioned at index after
	 * the original Element at index is removed.
	 *
	 * @param i Element index to remove from this ObjectArray.
	 */
	void removeByIndex(int i);

	/**
	 * Removes the Element at name n and shrinks this ObjectArray by one Element.
	 *
	 * @param n Elements name to remove from this ObjectArray.
	 */
	void removeByKey(String n);

	/**
	 * Update the Element at name n with data d
	 * or add a new Element if key name n doesnt exist.<br /><br />
	 * USE:<br />
	 * <code>addData(String n, Object d)</code> to add new data or data with duplicate keys to this ObjectArray.
	 *
	 * @param n The Elements name.
	 * @param d the Data to update/add to this ObjectArray.
	 */
	void setData(String n, Object d);

	/**
	 * Update the Element at index i with data d.<br />
	 * No action is taken if index i is greater than this DataHandlers size.
	 *
	 * @param i The Elements index.
	 * @param d the Data to update index i with.
	 */
	void setData(int i, Object d);

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