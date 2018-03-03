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
package com.jdbwc.core;

import java.sql.SQLException;
import java.util.TimeZone;

/**
 * Holds metaData about this connection object.
 *
 * @author Tim Gall
 * @version 2008-05-29
 */
public class WCConnectionInfo {

	// we make all version numbers related to the server
	// available here during authentication.
	//
	protected transient String currentDatabase = "";
	protected transient String databaseUser = "";
	protected transient String databasePass = "";

	protected transient String myDatabaseVersion = "";
	protected transient String myScriptingVersion = "";
	protected transient String myJDBWCScriptVersion = "";
	protected transient String myTimeZone = "";
	protected transient String myServerProtocol = "";
	protected transient boolean isBaseServer = false;

	/** The Database type this class is designed to work with */
	protected transient int myDbType = -1;
	protected transient String myDBName = "";
	protected transient String myDBVersion = "";

	protected transient String myfullJdbcUrl = "";
	protected transient String myUserName = "";

	/** -1 = lowercase, 0 = mixed-case, 1 = uppercase */
	protected transient int myCaseSensitivity = 0;

	protected WCConnectionInfo() {
		super();
	}

	protected void getDatabaseInfo() throws SQLException {
		String ver = getVersion(1);

		if(ver.length() > 0){
			if(ver.contains("-")){
				myDBVersion = ver.substring(ver.indexOf('-')+1);
				myDBName = ver.substring(0, ver.indexOf('-'));
			}else{
				myDBVersion = ver.substring(ver.indexOf(' ')+1);
				myDBName = ver.substring(0, ver.indexOf(' '));
			}
		}
	}

	protected int getDatabaseMicroVersion() throws SQLException {
		String minorStr = myDatabaseVersion.substring(myDatabaseVersion.lastIndexOf('.')+1);

		return com.jdbwc.core.util.SQLUtils.getNumberFromString(minorStr);
	}

	/**
	 *
	 * @return int. One of:
	 * <ul>
	 * <li>-1 = lowercase</li>
	 * <li> 0 = mixed-case</li>
	 * <li> 1 = uppercase</li>
	 * </ul>
	 */
	public int getCaseSensitivity() {
		return myCaseSensitivity;
	}

	public String getDatabase() {
		return currentDatabase;
	}

	public int getDatabaseMajorVersion() throws SQLException {
		String majorStr = myDBVersion.substring(0, myDBVersion.indexOf('.'));
		int majorVer;

		try {
			majorVer = Integer.parseInt(majorStr);
		} catch (NumberFormatException ignored) {
			majorVer = 0;
		}

		return majorVer;
	}

	public int getDatabaseMinorVersion() throws SQLException {
		String minorStr = myDBVersion.substring(myDBVersion.indexOf('.')+1, myDBVersion.lastIndexOf('.'));
		int minorVer;

		try {
			minorVer = Integer.parseInt(minorStr);
		} catch (NumberFormatException ignored) {
			minorVer = 0;
		}

		return minorVer;
	}

	public String getDatabaseProductName() throws SQLException {
		return myDBName;
	}

	public String getDatabaseProductVersion() throws SQLException {
		return myDBVersion;
	}

	public int getDbType() {
		return myDbType;
	}

	public int getDriverMajorVersion() {
		int majorVersion = 1;
		try {
			majorVersion = new Driver().getMajorVersion();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return majorVersion;
	}

	public int getDriverMinorVersion() {
		int minorVersion = 1;
		try {
			minorVersion = new Driver().getMinorVersion();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return minorVersion;
	}

	public String getDriverName() throws SQLException {
		return new Driver().getVersionName();
	}

	public String getDriverVersion() throws SQLException {
		return new Driver().getVersionString();
	}

	/**
	 *
	 * @param versionType - int, can be 0 (ALL), 1 (database), 2 (scripting engine) or 3 (jdbwc script bundle)
	 * @return The requested version String.
	 */
	public String getVersion(int versionType) {
		String ver = "";
		switch(versionType){
		case 0:
			ver = myDatabaseVersion + "/"
				+ myScriptingVersion + "/"
				+ myJDBWCScriptVersion;
			break;
		case 1:
			ver = myDatabaseVersion;
			break;
		case 2:
			ver = myScriptingVersion;
			break;
		case 3:
			ver = myJDBWCScriptVersion;
			break;
		}
		return ver;
	}

	/**
	 * @return the isBaseServer
	 */
	public final boolean isBaseServer() {
		return isBaseServer;
	}

	public boolean versionMeetsMinimum(int majorVersion, int minorVersion, int microVersion) throws SQLException {
		boolean meetsMin = false;

		if(getDatabaseMajorVersion() > majorVersion){
			// exceeds required version
			meetsMin = true;
		}else if(getDatabaseMajorVersion() == majorVersion){
			if(getDatabaseMinorVersion() > minorVersion){
				// exceeds required version
				meetsMin = true;
			}else if(getDatabaseMinorVersion() == minorVersion){
				if(getDatabaseMicroVersion() >= microVersion){
					// meets or exceeds required version
					meetsMin = true;
				}
			}
		}
		return meetsMin;
	}

	/**
	 * Gets the timezone as used by the server.
	 *
	 * @return the myTimeZone
	 */
	protected TimeZone getMyTimeZone() {
		TimeZone tz;
		if(myTimeZone==null){
			tz = TimeZone.getDefault();
		}else{
			tz = TimeZone.getTimeZone(myTimeZone);
		}
		return tz;
	}

}