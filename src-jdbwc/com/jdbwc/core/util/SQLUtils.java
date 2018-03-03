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
 * Utility methods used by the core driver classes
 *
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-04-10
 */
public final class SQLUtils {

	private static final String MY_WS = "\\s+";
	private static final String MY_SELECT = "SELECT";
	private static final String MY_DEL_KEYWORD = "DELIMITER";
	private static final String MY_ROUTINES =
		MY_DEL_KEYWORD+MY_WS +
		"|CREATE"+MY_WS+"FUNCTION"+MY_WS +
		"|CREATE"+MY_WS+"PROCEDURE"+MY_WS +
		"|DROP"+MY_WS+"FUNCTION"+MY_WS +
		"|DROP"+MY_WS+"PROCEDURE"+MY_WS;



	private static final Pattern myRoutines = Pattern.compile(MY_ROUTINES, Pattern.CASE_INSENSITIVE);
	private static final Pattern myDelKeyword = Pattern.compile(MY_DEL_KEYWORD+MY_WS, Pattern.CASE_INSENSITIVE);
	private static final Pattern mySelect = Pattern.compile(MY_SELECT+MY_WS, Pattern.CASE_INSENSITIVE);

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
		return sqlString.replaceAll("\\s+", " ").trim();
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


	/**
	 * Check an SQL String for traces of Routine keywords
	 * used for creating and dropping functions and procedures
	 * from the database. In specific this method avoids us
	 * passing the keyword DELIMITER to the database as it produces
	 * unwanted results. Instead we process it in a specific
	 * way on the serverside end.
	 *
	 * @param sql SQL String to check for traces of an SQL Routine.
	 * @return true if sql contains any Routine keywords.
	 */
	public static boolean isSqlARoutine(String sql){
		boolean isRoutine = false;
		Matcher matcher = myRoutines.matcher(sql);
		if(matcher.find()){
			isRoutine = true;
		}
		return isRoutine;
	}

	/**
	 * Check an SQL String for traces of ResultType keywords.
	 *
	 * @param sql SQL String to check for traces of a SQL ResultType.
	 * @return true if sql contains any ResultType keywords.
	 */
	public static boolean isSqlAResultType(String sql){
		boolean isResultType = false;
		Matcher matcher = mySelect.matcher(sql);
		if(matcher.find()){
			isResultType = true;
		}
		return isResultType;
	}

	/**
	 * Find instances of the <i>DELIMITER</i> keyword, PHP doesnt support it anymore.<br />
	 * Contrary to some popular beliefs, mysql and mysqli extensions for php
	 * will both throw errors at the DELIMITER keyword.<br />
	 * Fortunately though, PHP doesnt require the delimiter to be changed.<br />
	 * Good'ol <b>;</b> is fine, even in complex stored routine syntax.<br />
	 * DELIMITER most likely conflicts with a php/apache keyword or variable or whatever
	 * which would explain why support has been omitted.<br />
	 * <br />
	 * HISTORICAL: I think it used to be supported in some earlier mysqli libraries,
	 * earlier than that I have no idea.
	 *
	 * @param sql query String which might contain the DELIMITER keyword.
	 * @return sql String suitable for running via PHP.
	 */
	public static String replaceCustomDelimiters(String sql){
		String cleanSQL = sql;

		/* find and remove instances of the DELIMITER keyword */
		Matcher delimKey = myDelKeyword.matcher(cleanSQL);
		if(delimKey.find()){
			String[] sqlBits = myDelKeyword.split(cleanSQL);
			if(sqlBits.length > 0){

				/* try to get the replacement delimiter sequence */
				String firstBit = sqlBits[0].trim();
				if(firstBit.length() == 0){
					firstBit = sqlBits[1].trim();
				}

				String delimiterSeq = "";
				for(int i = 0; i < firstBit.length(); i++){
					char singleChar = firstBit.charAt(i);
					if(';'==singleChar || '\n'==singleChar || '\r'==singleChar || ' '==singleChar){
						if(delimiterSeq.length() > 0){
							break;
						}
					}else{
						delimiterSeq += singleChar;
					}
				}
				cleanSQL = cleanSQL.replace(delimiterSeq, ";");

				/* remove instances of the DELIMITER keyword
				 * and replace the replacement delimiter with the standard mySql one.
				 * Finally we make sure theres no empty entries. EG: removing the opening
				 * delimiter declaration would have replaced it with ;
				 * there would most likely be at least one more empty at
				 * the end when the delimiter is (should be) reset to its default */
				delimKey = myDelKeyword.matcher(cleanSQL);
				cleanSQL = delimKey.replaceAll("");

				String finalPass[] = cleanSQL.split(";");
				if(finalPass.length > 0){
					cleanSQL = "";
					for(int i = 0; i < finalPass.length; i++){
						if(!"".equals(finalPass[i].trim())){
							cleanSQL += finalPass[i] + ";";
						}
					}
				}
			}
		}
		return cleanSQL;
	}

	public static String getRealString(String obj){
		if("NULL".equals(obj))
			obj = null;
		return obj;
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
