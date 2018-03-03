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
package com.jdbwc.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.ozdevworx.dtype.ObjectArray;
import com.ozdevworx.dtype.impl.ObjectList;


/**
 * General utility class to assist the JDBWC Driver.
 *
 * @author Tim Gall
 * @version 2008-05-29
 * @version 2010-04-10
 * @version 2010-04-17
 * @version 2010-04-28 changed the ObjectArray implementation
 */
public final class Util {

	/** systems line break property */
	public static final String WC_NL = System.getProperty("line.separator");
	//post method keys
	public static final String TAG_SQL = "sql";
	public static final String TAG_ACTION = "jdbwcAction";
	public static final String TAG_AUTH = "auth";
	public static final String TAG_USER = "su";
	public static final String TAG_PASS = "sp";
	public static final String TAG_DBTYPE = "dbType";
	public static final String TAG_DEBUG = "debug";


	/** ObjectArray implementation used by this driver */
	public static final String DT_IMPL_VER = ObjectList.class + " " + ObjectList.VERSION;

	/** Dummy User-Agent. Rarely needed */
	public static final String CUSTOM_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3 (.NET CLR 3.5.30729)";

	public static final int CASE_LOWER = -1;
	public static final int CASE_UPPER = 1;
	public static final int CASE_MIXED = 0;

	/** Indetifier for caught errors being sent from the server API */
	public static final String WC_ERROR_TAG = "ERROR# ";

	//specific error idetifiers so we can throw specific exceptions
	private static final String WC_ERROR_HACK_STR = "ERROR-HACK: ";//we should never see this one on the JDBC end
	private static final String WC_ERROR_PATH_STR = "ERROR-PATH: ";
	private static final String WC_ERROR_CREDS_STR = "ERROR-BAD-CREDENTIALS: ";
	private static final String WC_ERROR_SESS_STR = "ERROR-SESSION: ";
	private static final String WC_ERROR_UNKNOWN_STR = "ERROR-Unknown: ";
	private static final String WC_PHP_STR = "PHP ";
	private static final String WC_ERROR_DB_CON_STR = "ERROR-DB-CONNECTION: ";
	private static final String WC_ERROR_DB_QRY_STR = "ERROR-DB-QUERY: ";
	/** For detecting non-recoverable-Fatal-Errors on the serverside. */
	public static final String[] WC_ERROR_SCRIPT =
		{"Catchable Fatal Error",
	    "Fatal error",
	    "Parsing Error",
	    "Parse error",
	    "Error",
	    "Core Error",
	    "Core Warning",
	    "Compile Error",
	    "Compile Warning",
	    "Runtime Notice"};


	public static String stripTags(final String input){
		final Pattern tagDetector = Pattern.compile("<.>|</.>|<./>|<./\\s+>", Pattern.CASE_INSENSITIVE);
		final Pattern innerTagDetector = Pattern.compile("<.+>", Pattern.CASE_INSENSITIVE);
		final Pattern myBreakTag = Pattern.compile("<br>|<br\\s+/>|<br/>", Pattern.CASE_INSENSITIVE);
		String plainText = input.trim();

		//System.err.println("plainText BEFORE = " + plainText);

		/* convert breaks to newlines so we retain the overall formatting */
		final Matcher br2nl = myBreakTag.matcher(plainText);
		if(br2nl.find()){
			plainText = br2nl.replaceAll("\n").trim();
			//System.err.println("plainText DURING = " + plainText);
		}
		/* strip out any remaining html tags */
		Matcher htmlStripper = tagDetector.matcher(plainText);
		if(htmlStripper.find()){
			plainText = htmlStripper.replaceAll("").trim();
			htmlStripper = innerTagDetector.matcher(plainText);
			if(htmlStripper.find()){
				plainText = htmlStripper.replaceAll("").trim();
			}
			//System.err.println("plainText AFTER = " + plainText);
		}
		return plainText;
	}

	/**
	 * Parse the servers http response into a string if no errors are encountered.<br />
	 * The http reponse is checked for error headers, then checked for exception triggers delivered from the web server.<br />
	 * Http response errors would only be thrown from here if the files were moved, deleted or made not readable
	 * while actively in use.
	 *
	 * @param response The http response from the web server
	 * @return The response from the web server as a String if no errors are encountered.
	 * @throws SQLException if the results include an error trigger from the web server
	 * @throws IOException if errors are encountered converting the http entity to a string
	 */
	public static String parseResponse(final HttpResponse response) throws SQLException, IOException{
		String responseBody = "";

		synchronized (responseBody) {
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					responseBody = EntityUtils.toString(entity);
				}
			} else {
				//failsafe incase the server-side files are tampered with while in use (deleted, moved, etc.)
				throw new com.jdbwc.exceptions.ServerSideException(
						"Error accessing the server. Server Error code: "
						+ response.getStatusLine().getStatusCode()
						+ "Reason: "
						+ response.getStatusLine().getReasonPhrase()
						+ ", HTTP Protocol: "
						+ response.getStatusLine().getProtocolVersion());
			}
			checkForExceptions(responseBody);
		}
		return responseBody;
	}

	/**
	 * Look for errors from the server.<br />
	 * Chuck a wobbly (alias: throw exception) back to the
	 * requesting method with a few details about the problem if one [or more] are found.<br />
	 * <br />
	 * NOTE: This release will only throw one exception for the first found
	 * (instead of java's usual exception trail) but will include details for all in the message.
	 *
	 * @param responseBody The Server response String.
	 * @return false if no errors are found.
	 * @throws SQLException if any errors are detected.
	 */
	public static boolean checkForExceptions(final String responseBody) throws SQLException{
		boolean hasExceptions = false;

		if(responseBody.startsWith(WC_ERROR_TAG)){
			hasExceptions = true;
			String message = stripTags(responseBody.substring(WC_ERROR_TAG.length()));

//			System.err.println("checkForExceptions message = " + responseBody + "\n\n");

			//SQL exceptions
			if(message.startsWith(WC_ERROR_DB_QRY_STR) || message.startsWith(WC_ERROR_DB_CON_STR)){
				message = message.substring(message.indexOf(':')+1);

				int vendorCode;
				try {
					vendorCode = Integer.parseInt( (message.substring(0, message.indexOf(" - ")).trim()) );
				} catch (NumberFormatException e) {
					vendorCode = 0;
				}

				throw new SQLException(message, "S1000", vendorCode);//general error state

			//Server API exceptions
			}else if(message.startsWith(WC_ERROR_HACK_STR)){
				throw new com.jdbwc.exceptions.HackingException(message);

			}else if(message.startsWith(WC_ERROR_PATH_STR)){
				throw new com.jdbwc.exceptions.InvalidServerPathException(message);

			}else if(message.startsWith(WC_ERROR_CREDS_STR)){

				throw new com.jdbwc.exceptions.InvalidAuthorizationException(message);

			}else if(message.startsWith(WC_ERROR_SESS_STR)){
				throw new com.jdbwc.exceptions.SessionException(message);

			}else if(message.startsWith(WC_ERROR_UNKNOWN_STR)){
				throw new com.jdbwc.exceptions.ServerSideException(message);

			//PHP exceptions
			}else if(message.startsWith(WC_PHP_STR)){
				throw new com.jdbwc.exceptions.PHPException(message);

			//unknown errors
			}else{
				if(message.contains(WC_ERROR_TAG)){
					//recurse for nested messages
					message = message.substring(message.indexOf(WC_ERROR_TAG));
					return checkForExceptions(message);
				}else{
					throw new SQLException("Unknown error: " + message, "S1000");//general error state
				}
			}
		}
		return hasExceptions;
	}

	/**
	 * @param input ObjectArray
	 * @return A UrlEncodedFormEntity of NameValuePair's representing the input param
	 */
	public static HttpEntity prepareForWeb(final ObjectArray input){

		List <NameValuePair> nvpData = new ArrayList <NameValuePair>();

		final int inputSize = input.length();
		for(int i = 0; i < inputSize; i++){
			if(TAG_SQL.equals(input.getKey(i))){
				nvpData.add(new BasicNameValuePair(input.getKey(i), input.getString(i).trim()));
			}else{
				nvpData.add(new BasicNameValuePair(input.getKey(i), csvFormat(input.getString(i).trim())));
			}
		}
		HttpEntity entity;
		try {
			entity = new UrlEncodedFormEntity(nvpData, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			try {
				entity = new UrlEncodedFormEntity(nvpData);
			} catch (UnsupportedEncodingException e1) {
				//we should never arrive here
				entity = null;
			}
		}

		return entity;
	}

	/**
	 * ObjectArray Factory method.<br />
	 * <br />
	 * Get a ObjectArray object with keys matching the required caseType
	 * to satisfy various database collation requirements.<br />
	 * <br />
	 * All ObjectArray objects used by this driver come from this method,
	 * making the use of different ObjectArray implementations very simple.
	 *
	 * @param caseType One of <code>Util.CASE_LOWER, Util.CASE_UPPER, Util.CASE_MIXED</code>
	 * @return A new ObjectArray object with keys forced to caseType
	 */
	public static ObjectArray getCaseSafeHandler(final int caseType){
		ObjectArray output;
		switch(caseType){
		case CASE_LOWER:
			output = new ObjectList(true);
			break;
		case CASE_UPPER:
			output = new ObjectList(false);
			break;
		case CASE_MIXED:
		default:
			output = new ObjectList();
		}

		return output;
	}



	public static String csvFormat(final String input){
		final String pass1 = input.replace(",", "#_002C");

		final String pass2 = pass1.replace("\r\n", "#_CN");//must be first line-break type
		final String pass3 = pass2.replace("\r", "#_CR");
		final String pass4 = pass3.replace("\n", "#_NL");

		final String pass5 = pass4.replace("\f", "#_FF");

		final String pass6 = pass5.replace("\"", "#_0022");
		final String pass7 = pass6.replace("'", "#_0027");

//		String pass8 = pass7.replace("#_040", "");// not required when sending to server.

		return pass7;
	}



	public static String csvUnFormat(final String input){
		final String pass1 = input.replace("#_002C", ",");

		final String pass2 = pass1.replace("#_CN", "\r\n");//must be first line-break type
		final String pass3 = pass2.replace("#_CR", "\r");
		final String pass4 = pass3.replace("#_NL", "\n");

		final String pass5 = pass4.replace("#_FF", "\f");

		final String pass6 = pass5.replace("#_0022", "\"");
		final String pass7 = pass6.replace("#_0027", "'");

		final String pass8 = pass7.replace("#_040", "");//!important. Restores empty values

		return pass8;
	}

	/**
	 * Stops this class being initialised.<br />
	 * As its a purely static class, it shouldn't be constructed.
	 */
	private Util(){}

//	private static String getFromStream(final InputStream ins) throws IOException{
//		final int BLEN = 8192; // byte length
//		final StringBuilder contents = new StringBuilder();
//
//		final BufferedInputStream bis = new BufferedInputStream(ins);
//		final byte[] bytes = new byte[BLEN];
//		int count = bis.read(bytes);
//		while(count != -1 && count <= BLEN) {
//			contents.append(new String(bytes, 0, count));
//			count = bis.read(bytes);
//		}
//		if(count != -1) {
//			contents.append(new String(bytes, 0, count));
//		}
//		bis.close();
//		ins.close();
//
////		System.err.println(contents.toString());
//
//		return contents.toString();
//	}
}
