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
package com.jdbwc.exceptions;

/**
 * This should never appear. Its here because the server is capable of throwing this type of custom error
 * but would only occur if someone was attempting to use devious means to access the JDBWC server.
 * This driver obviously doesn't use devious means of access, thus should never trigger this exception.
 *
 * @author Tim Gall
 * @version 2010-04-17
 */
public class HackingException extends ServerSideException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public HackingException() {
		super();
	}

	public HackingException(String reason) {
		super(reason);
	}
}
