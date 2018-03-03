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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.ozdevworx.dtype.DataHandler;
import com.ozdevworx.dtype.util.ParseNumber;


/**
 * A List based array. Access DataHandlers Data via a String label or a numeric index.<br />
 * A DataHandler is an Object itself so support for multidimensional DataHandler Objects is generic to this implementation.<br /><br />
 * <i>This DataHandler implementation uses synchronisation as specified by the DataHandler interface.</i><br /><br />
 * The core abstract implementation for this DataHandler implementation is ArrayList.<br />
 * We are using dual synchronised ArrayList's that work in strict unison to maintain data integrity
 * and maximise the flexibility and dynamics in this Data Type.<br />
 * ArrayList is one of the better performing List implementations.
 * The synchronisation abilities of the ArrayList is
 * handled by this class as ArrayList is not a synchronised Object by default.
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 1.0.0.7
 */
public class KeyedList implements DataHandler {

	/** static version number of this class */
	public static final String VERSION = "1.0.0-7";
	/**
	 * int myInc default List increment size.<br />
	 * This value can be over-ridden when constructing a new DataHandler.
	 */
	private transient int myInc = 10;

	/**
	 * Synchronised List that contains the data Objects.<br />
	 * myData and myKey operate in unison.
	 */
	private transient List<Object> myData;
	/**
	 * Synchronised List that contains the key Strings
	 */
	private transient List<String> myKey;

	private transient boolean myCaseSensitive = false;
	private transient boolean myLowerCaseKeys = false;

	/**
	 * Create a <b>case sensitive</b> DataHandler of initial capacity 10.<br />
	 * <b>Initial capacity is 10
	 * with an expansion rate of 10</b> when the DataHandler is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>length()</b> returns the actual size and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the DataHandler.<br />
	 * etc.</i>
	 */
	public KeyedList() {
		super();
		init();
	}

	/**
	 * Create a <b>case insensitive</b> DataHandler of initial capacity incSize.<br />
	 * <b>Initial capacity is 10
	 * with an expansion rate of 10</b> when the DataHandler is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the DataHandler.<br />
	 * etc.</i>
	 *
	 * @param lowerCaseKeys boolean. If true, keys all use lower case,
	 * if false keys all use upper case.
	 */
	public KeyedList(final boolean lowerCaseKeys) {
		super();
		myCaseSensitive = true;
		myLowerCaseKeys = lowerCaseKeys;
		init();
	}

	/**
	 * Create a <b>case sensitive</b> DataHandler of initial capacity incSize.<br />
	 * <b>Initial capacity is incSize
	 * with an expansion rate of incSize</b> when the DataHandler is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the DataHandler.<br />
	 * etc.</i>
	 *
	 * @param incSize int. This value affects the initial size
	 * and the expansion rate when the DataHandler is growing.
	 */
	public KeyedList(final int incSize) {
		super();
		myInc = incSize;
		init();
	}

	/**
	 * Create a <b>case insensitive</b> DataHandler of initial capacity incSize.<br />
	 * <b>Initial capacity is incSize
	 * with an expansion rate of incSize</b> when the DataHandler is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the DataHandler.<br />
	 * etc.</i>
	 *
	 * @param incSize int. This value affects the initial size
	 * and the expansion rate when the DataHandler is growing.
	 * @param lowerCaseKeys boolean. If true, keys all use lower case,
	 * if false keys all use upper case.
	 */
	public KeyedList(final int incSize, final boolean lowerCaseKeys) {
		super();
		myCaseSensitive = true;
		myLowerCaseKeys = lowerCaseKeys;
		myInc = incSize;
		init();
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#addData(java.lang.String, java.lang.Object)
	 */
	public void addData(final String n, final Object d) {
		try{
			if(n!=null){
				synchronized(myKey) {
					myKey.add(fixCase(n));
					myData.add(d);
				}
			}
		} catch (final NullPointerException ignored){
			//nothing todo here.
			//We can't store null keys.
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#clearData()
	 */
	public void clearData(){
		synchronized(myKey) {
			myKey.clear();
			myData.clear();
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#countMatches(java.lang.String)
	 */
	public int countMatches(final String n) {
		int m = 0;
		for (int i = 0; i < length(); i++){
			if (getKey(i).equals(fixCase(n)))
				m++;

		}
		return m;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getData(int)
	 */
	public Object getData(final int i) {
		return getObject(i);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getData(java.lang.String)
	 */
	public Object getData(final String n) {
		return getObject(n);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getDouble(int)
	 */
	public double getDouble(final int i) {
		try {
			return ParseNumber.getDouble(getDouble(getString(i)));
		} catch (IlegalNumberTypeException e) {
			return 0D;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getDouble(java.lang.String)
	 */
	public double getDouble(final String n) {
		try {
			return ParseNumber.getDouble(getString(n));
		} catch (IlegalNumberTypeException e) {
			return 0D;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getFloat(int)
	 */
	public float getFloat(final int i) {
		try {
			return ParseNumber.getFloat(getString(i));
		} catch (IlegalNumberTypeException e) {
			return 0F;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getFloat(java.lang.String)
	 */
	public float getFloat(final String n) {
		try {
			return ParseNumber.getFloat(getString(n));
		} catch (IlegalNumberTypeException e) {
			return 0F;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getIndex(java.lang.String)
	 */
	public int getIndex(final String n) {
		return myKey.indexOf(fixCase(n));
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getIndexByElement(java.lang.String, java.lang.String)
	 */
	public int getIndexByElement(final String key, final String data){
		int found = -1;
		for(int i = 0; i < myKey.size(); i++){
			if(myKey.get(i).equalsIgnoreCase(key) && myData.get(i).equals(data)){
				found = i;
				break;
			}
		}
		return found;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getIndexes(java.lang.String)
	 */
	public int[] getIndexes(final String n) {
		final int m = countMatches(n);
		final int idx[] = new int[m];
		int idxI = 0;
		for (int i = 0; i < length(); i++){
			if (getKey(i).equals(fixCase(n)))
				idx[idxI++] = i;

		}
		return idx;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getInt(int)
	 */
	public int getInt(final int i) {
		try {
			return ParseNumber.getInt(getString(i));
		} catch (IlegalNumberTypeException e) {
			return 0;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getInt(java.lang.String)
	 */
	public int getInt(final String n) {
		try {
			return ParseNumber.getInt(getString(n));
		} catch (IlegalNumberTypeException e) {
			return 0;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getKey(int)
	 */
	public String getKey(final int i) {
		String key = "";
		try {
			if(i < length())
				key = myKey.get(i);

		} catch (final IndexOutOfBoundsException e){
		} catch(final NullPointerException e){
		}

		return key;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getLong(int)
	 */
	public long getLong(final int i) {
		try {
			return ParseNumber.getLong(getString(i));
		} catch (IlegalNumberTypeException e) {
			return 0L;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getLong(java.lang.String)
	 */
	public long getLong(final String n) {
		try {
			return ParseNumber.getLong(getString(n));
		} catch (IlegalNumberTypeException e) {
			return 0L;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getObject(int)
	 */
	public Object getObject(final int i) {
		return getItem(i, false);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getObject(java.lang.String)
	 */
	public Object getObject(final String n) {
		return getItem(myKey.indexOf(fixCase(n)), false);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getObjectByElement(java.lang.String, java.lang.String)
	 */
	public Object getObjectByElement(final String key, final String data){
		Object found = null;
		for(int i = 0; i < myKey.size(); i++){
			if(myKey.get(i).equalsIgnoreCase(key) && myData.get(i).equals(data)){
				found = myData.get(i);
				break;
			}
		}
		return found;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getShort(int)
	 */
	public Short getShort(final int i) {
		try {
			return ParseNumber.getShort(getString(i));
		} catch (IlegalNumberTypeException e) {
			return 0;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getShort(java.lang.String)
	 */
	public Short getShort(final String n) {
		try {
			return ParseNumber.getShort(getString(n));
		} catch (IlegalNumberTypeException e) {
			return 0;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getString(int)
	 */
	public String getString(final int i) {
		return String.valueOf(getItem(i, true));
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#getString(java.lang.String)
	 */
	public String getString(final String n) {
		return String.valueOf(getItem(myKey.indexOf(fixCase(n)), true));
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#hasElement(java.lang.String, java.lang.String)
	 */
	public boolean hasElement(final String key, final String data){
		boolean found = false;
		for(int i = 0; i < myKey.size(); i++){
			if(myKey.get(i).equalsIgnoreCase(key) && myData.get(i).equals(data)){
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#hasKey(int)
	 */
	public boolean hasKey(final int index){
		return myKey.size() > index;
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#hasKey(java.lang.String)
	 */
	public boolean hasKey(final String value){
		return myKey.contains(fixCase(value));
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#isEmpty()
	 */
	public boolean isEmpty() {
		return myKey.isEmpty();
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#length()
	 */
	public int length() {
		return myKey.size();
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#removeByIndex(int)
	 */
	public void removeByIndex(final int i){
		synchronized(myKey) {
			myKey.remove(i);
			myData.remove(i);
		}

	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#removeByKey(java.lang.String)
	 */
	public void removeByKey(final String n){
		final int index = myKey.indexOf(fixCase(n));
		if(index > -1)
			removeByIndex(index);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#setData(int, java.lang.Object)
	 */
	public void setData(final int i, final Object d) {
		if(length() >= i)
			myData.set(i, d);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#setData(int, java.lang.String, java.lang.Object)
	 */
	public void setData(final int index, final String newKey, final Object newObj){
		if(length() >= index){
			synchronized(myKey){
				myData.set(index, newObj);
				myKey.set(index, newKey);
			}
		}
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#setData(java.lang.String, java.lang.Object)
	 */
	public void setData(final String n, final Object d) {
		final int i = myKey.indexOf(fixCase(n));
		if (i > -1)
			myData.set(i, d);
		else
			addData(n, d);
	}

	/**
	 * @see com.ozdevworx.dtype.DataHandler#setData(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void setData(final String key, final String newKey, final Object newObj){
		final int i = myKey.indexOf(fixCase(key));
		if (i > -1){
			synchronized(myKey){
				myData.set(i, newObj);
				myKey.set(i, newKey);
			}
		}else
			addData(key, newObj);
	}


	/**
	 * Correct the CaSe for DataHandler keys.<br />
	 * NOTE: we use <code>Locale.ENGLISH</code> as recommended
	 * by the <code>java.util.Locale</code> documentation
	 * to preserve the programatic sense of the keys name.
	 * Particularly String wrappers and special characters.
	 *
	 * @param n A DataHandler key of unknown character CaSe.
	 * @return A key matching our Database case restrictions (if any).
	 */
	protected String fixCase(final String n){
		String output = n;
		if(myCaseSensitive){
			if(myLowerCaseKeys)
				output = output.toLowerCase(Locale.ENGLISH);
			else
				output = output.toUpperCase(Locale.ENGLISH);
		}
		return output;
	}

	/**
	 * Gets Elements from any List implementation based on the Elements index i.<br />
	 * Allows missing keys and missing indexes to be handled gracefully and discreetly.
	 *
	 * @param i int - List-index
	 * @param notNull - boolean
	 * @return Object or empty-string if no index in list
	 */
	protected Object getItem(final int i, final boolean notNull) {
		Object o = null;
		if(notNull){
			o = "";
		}
		try {
			if(i < length())
				o = myData.get(i);

		} catch (final IndexOutOfBoundsException e){
		} catch(final NullPointerException e){
		}
		return o;
	}

	/**
	 * Initialises 2 synchronised ArrayList's,
	 * one for Key-Strings and one for Data-Objects.<br />
	 * The methods that maintain the synchronised ArrayList's
	 * ensure the key and data locations are always valid.
	 */
	private void init() {
		myKey = Collections.synchronizedList(new ArrayList<String>(myInc));
		myData = Collections.synchronizedList(new ArrayList<Object>(myInc));
	}
}
