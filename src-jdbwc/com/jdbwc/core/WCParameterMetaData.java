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

import java.sql.ParameterMetaData;
import java.sql.SQLException;

import com.jdbwc.core.util.SQLField;
import com.jdbwc.core.util.SQLParamParser;
import com.jdbwc.exceptions.NotImplemented;
import com.ozdevworx.dtype.DataHandler;

/**
 * 
 * 
 * @author Tim Gall (Oz-DevWorX)
 * @version 2008-05-29
 * @version 2010-04-11
 */
public class WCParameterMetaData implements ParameterMetaData {

	private transient SQLField[] myFields = null;
	
	/**
	 * Constructs a new instance of this.<br />
	 */
	protected WCParameterMetaData(WCConnection connection, DataHandler prepStatement) throws SQLException {
		SQLParamParser prepStatment = new SQLParamParser(connection, prepStatement);
		myFields = prepStatment.getFields();
	}

	/**
	 * @see java.sql.ParameterMetaData#getParameterClassName(int)
	 */
	public String getParameterClassName(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].getClassName();
	}

	/**
	 * @see java.sql.ParameterMetaData#getParameterCount()
	 */
	public int getParameterCount() throws SQLException {
		if(myFields==null){
			throw new SQLException(
					"ParameterResultSet is closed or has not been properly constructed Exception!", 
					"S0022");
		}
		return myFields.length;
	}

	/**
	 * @see java.sql.ParameterMetaData#getParameterMode(int)
	 */
	public int getParameterMode(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		
		int paramMode = -1;
		
		String mode = myFields[param-1].getMode();
		if(mode.equals(SQLParamParser.MY_MODE_IN)){
			paramMode = ParameterMetaData.parameterModeIn;
		}else if(mode.equals(SQLParamParser.MY_MODE_OUT)){
			paramMode = ParameterMetaData.parameterModeOut;
		}else if(mode.equals(SQLParamParser.MY_MODE_INOUT)){
			paramMode = ParameterMetaData.parameterModeInOut;
		}else{
			paramMode = ParameterMetaData.parameterModeUnknown;
		}

		return paramMode;
	}

	/**
	 * @see java.sql.ParameterMetaData#getParameterType(int)
	 */
	public int getParameterType(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].getJdbcSqlType();
	}

	/**
	 * @see java.sql.ParameterMetaData#getParameterTypeName(int)
	 */
	public String getParameterTypeName(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].getGenericSqlTypeName();
	}

	/**
	 * @see java.sql.ParameterMetaData#getPrecision(int)
	 */
	public int getPrecision(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].getPrecision();
	}

	/**
	 * @see java.sql.ParameterMetaData#getScale(int)
	 */
	public int getScale(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].getScale();
	}

	/**
	 * @see java.sql.ParameterMetaData#isNullable(int)
	 */
	public int isNullable(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].isNullable() ? ParameterMetaData.parameterNullable : ParameterMetaData.parameterNoNulls;
	}

	/**
	 * @see java.sql.ParameterMetaData#isSigned(int)
	 */
	public boolean isSigned(int param) throws SQLException {
		if(param-1 >= myFields.length){
			throw new SQLException(
					"Paramater Index Out Of Bounds Exception!", 
					"S0022");
		}
		return myFields[param-1].isSigned();
	}

	/**
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

	/**
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO implement me!
		throw new NotImplemented();
	}

}
