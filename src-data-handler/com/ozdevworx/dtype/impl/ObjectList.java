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
import java.util.List;
import java.util.Locale;

import com.ozdevworx.dtype.ObjectArray;


/**
 * A List based array. Access DataHandlers Data via a String label or a numeric index.<br />
 * A ObjectArray is an Object itself so support for multidimensional
 * ObjectArray Objects is generic to this implementation.<br /><br />
 * <i>This implementation uses synchronization as specified by the ObjectArray interface.</i><br /><br />
 * The synchronization abilities for ArrayList is
 * handled by this class as ArrayList is not a synchronized Object by default.<br />
 * <br />
 * Handles sql NULL and java null better than previous ObjectArray implementations.
 *
 * @author Tim Gall
 * @version 2010-04-26 added sql NULL to java null conversions
 * @version 2010-04-28 added numeric conversion for values of Java null to 0.
 * @version 2010-05-07 improved synchronization.
 * @version 2010-05-26 stripped out non essential methods
 */
public class ObjectList implements ObjectArray {

	// ---------------------------------------------------- fields

	/** static version number of this class */
	public static final String VERSION = "1.0.0";
	/**
	 * int myInc default List increment size.<br />
	 * This value can be over-ridden when constructing a new ObjectArray.
	 */
	private transient int myInc = 25;
	/**
	 * List of data Objects.<br />
	 * myData and myKey operate in unison.
	 */
	private transient List<Object> myData;
	/**
	 * List of key Strings
	 * myKey and myData operate in unison.
	 */
	private transient List<String> myKey;

	private transient boolean myCaseSensitive = false;
	private transient boolean myLowerCaseKeys = false;
	/**
	 * Synchronization mutex
	 */
	private transient final Object mutex;

	// ---------------------------------------------------- constructors

	/**
	 * Create a <b>case sensitive</b> ObjectArray of initial capacity 10.<br />
	 * <b>Initial capacity is 10
	 * with an expansion rate of 10</b> when the ObjectArray is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>length()</b> returns the actual size and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the ObjectArray.<br />
	 * etc.</i>
	 */
	public ObjectList() {
		super();
		init();
		mutex = this;
	}

	/**
	 * Create a <b>case insensitive</b> ObjectArray of initial capacity incSize.<br />
	 * <b>Initial capacity is 10
	 * with an expansion rate of 10</b> when the ObjectArray is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the ObjectArray.<br />
	 * etc.</i>
	 *
	 * @param lowerCaseKeys boolean. If true, keys all use lower case,
	 * if false keys all use upper case.
	 */
	public ObjectList(final boolean lowerCaseKeys) {
		super();
		myCaseSensitive = true;
		myLowerCaseKeys = lowerCaseKeys;
		init();
		mutex = this;
	}

	/**
	 * Create a <b>case sensitive</b> ObjectArray of initial capacity incSize.<br />
	 * <b>Initial capacity is incSize
	 * with an expansion rate of incSize</b> when the ObjectArray is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the ObjectArray.<br />
	 * etc.</i>
	 *
	 * @param incSize int. This value affects the initial size
	 * and the expansion rate when the ObjectArray is growing.
	 */
	public ObjectList(final int incSize) {
		super();
		myInc = incSize;
		init();
		mutex = this;
	}

	/**
	 * Create a <b>case insensitive</b> ObjectArray of initial capacity incSize.<br />
	 * <b>Initial capacity is incSize
	 * with an expansion rate of incSize</b> when the ObjectArray is growing.<br />
	 * Empty place holders are ignored when manipulating and querying data.<br /><br />
	 * EG:<br /><i>
	 * <b>getSize()</b> returns the actual Element count and does not include placeholders related to this DataHandlers growth rate.<br />
	 * <b>isEmpty()</b> does not count placeholders, only actual Elements that were added to the ObjectArray.<br />
	 * etc.</i>
	 *
	 * @param incSize int. This value affects the initial size
	 * and the expansion rate when the ObjectArray is growing.
	 * @param lowerCaseKeys boolean. If true, keys all use lower case,
	 * if false keys all use upper case.
	 */
	public ObjectList(final int incSize, final boolean lowerCaseKeys) {
		super();
		myCaseSensitive = true;
		myLowerCaseKeys = lowerCaseKeys;
		myInc = incSize;
		init();
		mutex = this;
	}

	// ---------------------------------------------------- public methods

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#addData(java.lang.String, java.lang.Object)
	 */
	public void addData(final String n, final Object d) {
		try{
			if(n!=null){
				synchronized(mutex) {
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
	 * @see com.ozdevworx.dtype.ObjectArray#clearData()
	 */
	public void clearData(){
		synchronized(mutex) {
			myKey.clear();
			myData.clear();
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#countMatches(java.lang.String)
	 */
	public int countMatches(final String n) {
		int m = 0;
		for (int i = 0; i < length(); i++){
			if (fixCase(n).equals(getKey(i)))
				m++;
		}
		return m;
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getIndex(java.lang.String)
	 */
	public int getIndex(final String n) {
		synchronized (mutex) {
			return myKey.indexOf(fixCase(n));
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getKey(int)
	 */
	public String getKey(final int i) {
		String key = "";
		try {
			if(i < length()) {
				synchronized (mutex) {
					key = myKey.get(i);
				}
			}

		} catch (final IndexOutOfBoundsException e){
		} catch(final NullPointerException e){
		}

		return key;
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getObject(int)
	 */
	public Object getObject(final int i) {
		return getItem(i, false);
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getObject(java.lang.String)
	 */
	public Object getObject(final String n) {
		synchronized (mutex) {
			return getItem(myKey.indexOf(fixCase(n)), false);
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getString(int)
	 */
	public String getString(final int i) {
		final String o = String.valueOf(getItem(i, true));
		return "null".equalsIgnoreCase(o) ? null : o;
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#getString(java.lang.String)
	 */
	public String getString(final String n) {
		String o;
		synchronized (mutex) {
			o = String.valueOf(getItem(myKey.indexOf(fixCase(n)), true));
		}
		return "null".equalsIgnoreCase(o) ? null : o;
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#hasElement(java.lang.String, java.lang.String)
	 */
	public boolean hasElement(final String key, final String data){
		boolean found = false;
		synchronized (mutex) {
			for (int i = 0; i < myKey.size(); i++) {
				if (myKey.get(i).equalsIgnoreCase(key) && myData.get(i).equals(data)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#hasKey(int)
	 */
	public boolean hasKey(final int index){
		synchronized (mutex) {
			return index >= 0 && myKey.size() > index;
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#hasKey(java.lang.String)
	 */
	public boolean hasKey(final String value){
		synchronized (mutex) {
			return myKey.contains(fixCase(value));
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#isEmpty()
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			return myKey.isEmpty();
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#length()
	 */
	public int length() {
		synchronized (mutex) {
			return myKey.size();
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#removeByIndex(int)
	 */
	public void removeByIndex(final int i){
		synchronized(mutex) {
			myKey.remove(i);
			myData.remove(i);
		}

	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#removeByKey(java.lang.String)
	 */
	public void removeByKey(final String n){
		final int index;
		synchronized (mutex) {
			index = myKey.indexOf(fixCase(n));
		}
		if(index > -1)
			removeByIndex(index);
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#setData(int, java.lang.Object)
	 */
	public void setData(final int i, final Object d) {
		if(length() >= i) {
			synchronized (mutex) {
				myData.set(i, d);
			}
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#setData(int, java.lang.String, java.lang.Object)
	 */
	public void setData(final int index, final String newKey, final Object newObj){
		if(length() >= index){
			synchronized(mutex){
				myData.set(index, newObj);
				myKey.set(index, newKey);
			}
		}
	}

	/**
	 * @see com.ozdevworx.dtype.ObjectArray#setData(java.lang.String, java.lang.Object)
	 */
	public void setData(final String n, final Object d) {
		synchronized (mutex) {
			final int i = myKey.indexOf(fixCase(n));
			if (i > -1)
				myData.set(i, d);
			else
				addData(n, d);
		}
	}

	// ---------------------------------------------------- private methods

	/**
	 * Correct the CaSe for ObjectArray keys.<br />
	 * NOTE: we use <code>Locale.ENGLISH</code> as recommended
	 * by the <code>java.util.Locale</code> documentation
	 * to preserve the programatic sense of the keys name.
	 * Particularly String wrappers and special characters.
	 *
	 * @param n A ObjectArray key of unknown character CaSe.
	 * @return A key matching our Database case restrictions (if any).
	 */
	private String fixCase(final String n){
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
	private Object getItem(final int i, final boolean notNull) {
		Object o = null;
		if(notNull){
			o = "";
		}
		try {
			if(i < length()) {
				synchronized (mutex) {
					o = ("NULL".equalsIgnoreCase(String.valueOf(myData.get(i)))) ? null : myData.get(i);
				}
			}

		} catch (final IndexOutOfBoundsException e){
		} catch(final NullPointerException e){
		}
		return o;
	}

	/**
	 * Initialises 2 ArrayList's,
	 * one for Key-Strings and one for Data-Objects.<br />
	 * The methods that maintain the ArrayList's
	 * ensure the key and data locations are always valid.
	 */
	private void init() {
		myKey = new ArrayList<String>(myInc);
		myData = new ArrayList<Object>(myInc);
	}
}
