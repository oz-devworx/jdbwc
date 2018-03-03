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
package com.jdbwc.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * Handles reading a gzipped Input stream.<br />
 *
 * @author Tim Gall
 * @version 2010-04-15
 */
public final class GzipStreamReader extends HttpEntityWrapper {

        public GzipStreamReader(final HttpEntity entity) {
            super(entity);
        }

        /**
         * @see org.apache.http.entity.HttpEntityWrapper#getContent()
         */
        @Override
        public InputStream getContent() throws IOException, IllegalStateException {

            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream(wrappedin);
        }
        /**
         * @see org.apache.http.entity.HttpEntityWrapper#getContentLength()
         */
        @Override
        public long getContentLength() {
            return -1;//length is unknown
        }

    }