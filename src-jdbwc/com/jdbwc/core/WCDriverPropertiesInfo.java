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
package com.jdbwc.core;

import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jdbwc.util.Util;
import com.ozdevworx.dtype.ObjectArray;

/**
 * Handles explaining parameters and thier defaults
 * that are used by the Driver.
 *
 * @author Tim Gall
 * @version 2010-05-13
 * @version 2010-05-20
 */
public class WCDriverPropertiesInfo {

	private class DriverProperty{
		private transient String name;
		private transient int maxLength;
		private transient String defaultValue;
		private transient String description;
		private transient boolean required;
		private transient String[] choices;

		/**
		 *
		 */
		private DriverProperty(String name, int maxLength, boolean required, String defaultValue, String[] choices, String description) {
			this.name = name;
			this.maxLength = maxLength;
			this.defaultValue = defaultValue;
			this.description = description;
			this.required = required;
			this.choices = choices;
		}

		/**
		 * @return the name
		 */
		private final String getName() {
			return name;
		}

		/**
		 * @return the maxLength
		 */
		private final int getMaxLength() {
			return maxLength;
		}

		/**
		 * @return isRequired boolean
		 */
		private final boolean getRequired() {
			return required;
		}

		/**
		 * @return the defaultValue
		 */
		private final String getDefaultValue() {
			return defaultValue;
		}

		/**
		 * @return the description
		 */
		private final String getDescription() {
			return description;
		}

		/**
		 * @return DriverPropertyInfo choices[] or null if not applicable.
		 */
		private final String[] getChoices() {
			return choices;
		}
	}


	private transient WCConnection connection;
	private transient DriverProperty[] clientProperties;


	/**
	 *
	 * @param connection WCConnection - can be null when not requesting a ResultSet Object
	 */
	public WCDriverPropertiesInfo(WCConnection connection){
		this.connection = connection;
		buildProps();
	}

	/**
	 * For DatabaseMetaData
	 *
	 * @return ResultSet for getClientInfoProperties()
	 * @throws SQLException if the connection is closed or a new ResultSet cannot be created
	 */
	public WCResultSet getClientInfoResultSet() throws SQLException {
		WCResultSet res = new WCResultSet(this.connection, WCStaticMetaData.getClientInfoProperties());
		ObjectArray row = Util.getCaseSafeHandler(this.connection.myCaseSensitivity);

		for(DriverProperty element : clientProperties){
			row.clearData();
			row.addData("NAME", element.getName());
			row.addData("MAX_LEN", element.getMaxLength());
			row.addData("DEFAULT_VALUE", element.getDefaultValue());
			row.addData("DESCRIPTION", element.getDescription());

			res.addRow(row);
		}

		return res;
	}

	/**
	 * For Connection
	 *
	 * @param info Properties - can be null.
	 * @return Properties Object for getClientInfo(...)
	 */
	public Properties getClientInfoProps(Properties info){
		Properties props = new Properties();
		for(DriverProperty element : clientProperties){
			String infoData = info.getProperty(element.getName());

			if(infoData != null && !infoData.isEmpty())
				props.setProperty(element.getName(), infoData);
			else
				props.setProperty(element.getName(), element.getDefaultValue());

		}

		return props;
	}

	/**
	 * For Connection
	 *
	 * @return Properties Object for getClientInfo(...)
	 */
	public DriverPropertyInfo[] getDriverInfoProps(final Properties driverInfo){
		List<DriverPropertyInfo> dpi = new ArrayList<DriverPropertyInfo>(20);

		DriverPropertyInfo dbPassProp;
		for(DriverProperty element : clientProperties){

			String propVal = driverInfo.getProperty(element.getName());
			if(propVal==null || propVal.isEmpty()){
				propVal = element.getDefaultValue();
			}

			dbPassProp = new DriverPropertyInfo(element.getName(), propVal);
			dbPassProp.required = element.getRequired();
			dbPassProp.description = element.getDescription();
			dbPassProp.choices = element.getChoices();

			dpi.add(dbPassProp);
		}

		return dpi.toArray(new DriverPropertyInfo[dpi.size()]);
	}

	/**
	 * All of the Driver params are kept in one location to avoid
	 * code duplication and potential resulting errors.
	 */
	private final void buildProps(){
		List<DriverProperty> properties = new ArrayList<DriverProperty>();

		properties.add(new DriverProperty(WCDriver.KEY_URL, 255, true, "", null,"The URL of the remote server. The protocol, domain and path are required. The port is optional. Path must end with trailing / EG: https://myserver.ext[:443]/myfolder/myotherfolder/"));
		properties.add(new DriverProperty(WCDriver.KEY_USER, 255, true, "", null, "Server username"));
		properties.add(new DriverProperty(WCDriver.KEY_PASS, 255, true, "", null, "Server password"));

		properties.add(new DriverProperty(WCDriver.KEY_DB_VID, 255, true, WCDriver.KEY_VID_MYSQL, new String[]{WCDriver.KEY_VID_MYSQL,WCDriver.KEY_VID_POSTGRESQL}, "Database Type. Can be empty if specified on the jdbc part of the JDBC URL. EG: jdbc:jdbwc:[DatabaseType]//"));
		properties.add(new DriverProperty(WCDriver.KEY_DB_NAME, 255, true, "", null, "Database name"));
		properties.add(new DriverProperty(WCDriver.KEY_DB_USER, 255, true, "", null, "Database user name"));
		properties.add(new DriverProperty(WCDriver.KEY_DB_PASS, 255, true, "", null, "Database password"));

		properties.add(new DriverProperty(WCDriver.KEY_PROXY_URL, 255, false, "", null, "Proxy Server URL. The protocol, domain and port are required. EG: http://myproxyserver.ext:8085"));

		properties.add(new DriverProperty(WCDriver.KEY_NV_SSL, 5, false, "false", new String[]{"true","false"}, "Use non validated SSL connections? No validation on the SSL certificate will be performed if this value is true."));
		properties.add(new DriverProperty(WCDriver.KEY_TIMEOUT, 15, false, "60000", null, "Timeout in milliseconds. Zero indicates no limit."));
		properties.add(new DriverProperty(WCDriver.KEY_USE_UA, 5, false, "false", new String[]{"true","false"}, "If true, a dummy User-Agent will be used in place of the default apache httpclient agent."));

		properties.add(new DriverProperty(WCDriver.KEY_DEBUG, 5, false, "false", new String[]{"true","false"}, "If true, debugging will be enabled."));
		properties.add(new DriverProperty(WCDriver.KEY_DEBUG_LEVEL, 5, false, "0", new String[]{"0","1","2","3","4","5"}, "Represents the debugging level. Can be 0 to 5, zero being the lowest level."));
		properties.add(new DriverProperty(WCDriver.KEY_DEBUG_LOG, 32, false, "SimpleLog", new String[]{"SimpleLog"}, "The type of logger to use for debugging output."));

		clientProperties = properties.toArray(new DriverProperty[properties.size()]);
	}
}
