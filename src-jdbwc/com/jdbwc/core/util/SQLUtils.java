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
package com.jdbwc.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jdbwc.util.Util;
import com.ozdevworx.dtype.DataHandler;

/**
 * 
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-04-10
 */
public final class SQLUtils {

//	private static Method CAST_METHOD;
//	
//	static{
//		try {
//			CAST_METHOD = Class.class.getMethod("cast", new Class[] { Object.class });
//		} catch (SecurityException e) {
//			CAST_METHOD = null;
//		} catch (NoSuchMethodException e) {
//			CAST_METHOD = null;
//		}
//	}
	
	/**
	 * Increases the size of this.fieldSet and adds a new Field to the array.
	 * 
	 * @param newField a new Field to add to the result sieldSet.
	 */
	protected static SQLField[] rebuildFieldSet(SQLField newField, SQLField[] myFieldSet){
		SQLField[] rebuiltSet = new SQLField[myFieldSet.length+1];
		
		System.arraycopy(myFieldSet, 0, rebuiltSet, 0, myFieldSet.length);
		rebuiltSet[myFieldSet.length] = newField;

		return rebuiltSet;
	}
	
	/**
	 * Increases the size of this.fieldSet and adds a new Field to the array.
	 * 
	 * @param newFields a new Field array to add to the rebuiltSet.
	 * @param myFieldSet
	 * @return array of SQLField MetaData
	 */
	protected static SQLField[] rebuildFieldSet(SQLField[] newFields, SQLField[] myFieldSet){
		SQLField[] rebuiltSet = new SQLField[myFieldSet.length+newFields.length];
		
		System.arraycopy(myFieldSet, 0, rebuiltSet, 0, myFieldSet.length);
		System.arraycopy(newFields, 0, rebuiltSet, myFieldSet.length, newFields.length);

		return rebuiltSet;
	}
	
	/**
	 * Remove empty entries from an array.<br />
	 * Designed specifically for cleaning up string arrays
	 * constructed using split(regex) methods.
	 * 
	 * @param sqlStringArray An array with potentially empty indexes.
	 * @return An array without empty indexes.
	 */
	protected static String[] removeBlanks(String[] sqlStringArray){
		DataHandler tempArray = Util.getCaseSafeHandler(Util.CASE_MIXED);
		for(int size = 0; size < sqlStringArray.length; size++){
			if(!"".equals(sqlStringArray[size].trim())){
				tempArray.addData(size+"", sqlStringArray[size]);
			}
		}
		
		String[] results = new String[tempArray.length()];
		for(int i = 0; i < results.length; i++){
			results[i] = tempArray.getString(i);
		}
		
		return results;
	}
	
	/**
	 * Strip out excess whitespace characters.<br />
	 * <b>ie:</b><br />
	 * newlines, carriage returns, tabs, and multiple consecutive spaces 
	 * are all converted to a single space.<br />
	 * The result should not contain any consecutive whitespace characters.
	 * 
	 * @param sqlString A String to clean
	 * @return A String without excess whitespace characters.
	 */
	protected static String stripWhiteSpace(String sqlString){
		return sqlString.replaceAll("\\s+", " ");
	}
	

	protected static boolean isNullOrEmpty(String value) {
		value = value.trim();
		return (value == null || value.length() == 0);
	}
	
	/*
	 * The "stripComments" methods were copied from the mySQL ConnectorJ driver.
	 */
	protected static String stripComments(String src){
		return stripComments(src, "", "");
	}
	
	public static String stripComments(String src, String stringOpens, String stringCloses) {
		return stripComments(src, stringOpens, stringCloses, true, true, true, true);
	}
	
	/**
	 * Returns the given string, with comments removed
	 * 
	 * @param src
	 *            the source string
	 * @param stringOpens
	 *            characters which delimit the "open" of a string
	 * @param stringCloses
	 *            characters which delimit the "close" of a string, in
	 *            counterpart order to <code>stringOpens</code>
	 * @param slashStarComments
	 *            strip slash-star type "C" style comments
	 * @param slashSlashComments
	 *            strip slash-slash C++ style comments to end-of-line
	 * @param hashComments
	 *            strip #-style comments to end-of-line
	 * @param dashDashComments
	 *            strip "--" style comments to end-of-line
	 * @return the input string with all comment-delimited data removed
	 */
	protected static String stripComments(String src, String stringOpens,
			String stringCloses, boolean slashStarComments,
			boolean slashSlashComments, boolean hashComments,
			boolean dashDashComments) {
		if (src == null) {
			return null;
		}

		StringBuffer buf = new StringBuffer(src.length());

		// It's just more natural to deal with this as a stream
		// when parsing..This code is currently only called when
		// parsing the kind of metadata that developers are strongly
		// recommended to cache anyways, so we're not worried
		// about the _1_ extra object allocation if it cleans
		// up the code

		StringReader sourceReader = new StringReader(src);

		int contextMarker = Character.MIN_VALUE;
		boolean escaped = false;
		int markerTypeFound = -1;

		int ind = 0;

		int currentChar = 0;

		try {
			while ((currentChar = sourceReader.read()) != -1) {

//				if (false && currentChar == '\\') {
//					escaped = !escaped;
//				} else 
				if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound)
						&& !escaped) {
					contextMarker = Character.MIN_VALUE;
					markerTypeFound = -1;
				} else if ((ind = stringOpens.indexOf(currentChar)) != -1
						&& !escaped && contextMarker == Character.MIN_VALUE) {
					markerTypeFound = ind;
					contextMarker = currentChar;
				}

				if (contextMarker == Character.MIN_VALUE && currentChar == '/'
						&& (slashSlashComments || slashStarComments)) {
					currentChar = sourceReader.read();
					if (currentChar == '*' && slashStarComments) {
						int prevChar = 0;
						while ((currentChar = sourceReader.read()) != '/'
								|| prevChar != '*') {
							if (currentChar == '\r') {

								currentChar = sourceReader.read();
								if (currentChar == '\n') {
									currentChar = sourceReader.read();
								}
							} else {
								if (currentChar == '\n') {

									currentChar = sourceReader.read();
								}
							}
							if (currentChar < 0)
								break;
							prevChar = currentChar;
						}
						continue;
					} else if (currentChar == '/' && slashSlashComments) {
						while ((currentChar = sourceReader.read()) != '\n'
								&& currentChar != '\r' && currentChar >= 0)
							;
					}
				} else if (contextMarker == Character.MIN_VALUE
						&& currentChar == '#' && hashComments) {
					// Slurp up everything until the newline
					while ((currentChar = sourceReader.read()) != '\n'
							&& currentChar != '\r' && currentChar >= 0)
						;
				} else if (contextMarker == Character.MIN_VALUE
						&& currentChar == '-' && dashDashComments) {
					currentChar = sourceReader.read();

					if (currentChar == -1 || currentChar != '-') {
						buf.append('-');

						if (currentChar != -1) {
							buf.append(currentChar);
						}

						continue;
					}

					// Slurp up everything until the newline

					while ((currentChar = sourceReader.read()) != '\n'
							&& currentChar != '\r' && currentChar >= 0)
						;
				}

				if (currentChar != -1) {
					buf.append((char) currentChar);
				}
			}
		} catch (IOException ioEx) {
			// we'll never see this from a StringReader
		}

		return buf.toString().trim();
	}
	
	public static final boolean isEmptyOrWhitespaceOnly(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		
		int length = str.length();
		
		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static int getNumberFromString(String input){
		int numericPart = 0;
		final Pattern numericChars = Pattern.compile("\\d+", Pattern.CASE_INSENSITIVE);
		final Pattern alphaChars = Pattern.compile("[^0-9]+", Pattern.CASE_INSENSITIVE);	
		final Matcher numericMatch = numericChars.matcher(input);
		final Matcher alphaMatch = alphaChars.matcher(input);
		String numericStr = input;
		if(alphaMatch.find()){
			numericStr = alphaMatch.replaceAll("");
		}	
		if(numericMatch.find()){
			try {
				// test if its an int
				int num = Integer.parseInt(numericStr);
				numericPart = num;
			} catch (NumberFormatException ignored) {
				// ignored
			}
		}
		return numericPart;
	}
	
//	/**
//	 * Reflexive access on JDK-1.5's Class.cast() method.<br />
//	 * Adapted from mySql ConnectorJ. Currently does nothing.
//	 * 
//	 * @param invokeOn
//	 * @param toCast
//	 * @return <T> Object
//	 */
//	public static Object cast(final Object invokeOn, final Object toCast) {
//		if (CAST_METHOD != null) {
//			try {
//				return CAST_METHOD.invoke(invokeOn, new Object[]{toCast});
//			} catch (Throwable t) {
//				return null;
//			}
//		}
//		return toCast;
//	}
}
