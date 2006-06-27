/*
 * Copyright 2005 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osaf.cosmo.dav.io;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Item;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.server.io.IOListener;
import org.apache.jackrabbit.server.io.ImportContextImpl;
import org.apache.jackrabbit.webdav.io.InputContext;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.ModelConversionException;

/**
 * Extends {@link org.apache.jackrabbit.server.io.ImportContextImpl}
 * to provide access to model objects (eg eents) contained within
 * imported content.
 */
public class CosmoImportContext extends ImportContextImpl
    implements ICalendarConstants {
    private static final Log log = LogFactory.getLog(CosmoImportContext.class);

    private Calendar calendar;

    /**
     */
    public CosmoImportContext(Item importRoot,
                              String systemId,
                              InputContext inputCtx)
        throws IOException {
        super(importRoot, systemId, inputCtx);
    }

    /**
     */
    public CosmoImportContext(Item importRoot,
                              String systemId,
                              InputStream in,
                              IOListener ioListener)
        throws IOException {
        super(importRoot, systemId, in, ioListener);
    }

    /**
     * Returns true if the imported content is generic calendar
     * content. This implementation returns true if the context
     * provides an input stream and the context's mime type indicates
     * that the content is a calendar stream.
     */
    public boolean isCalendarContent() {
        return getInputStream() != null && getMimeType().equals(CONTENT_TYPE);
    }

    /**
     * If the imported content is calendar content, parses the input
     * stream and returns a {@link net.fortuna.ical4j.model.Calendar}
     * representing the content. If not calendar content, returns
     * <code>null<code>.
     *
     * This method only parses the input stream once, caching the
     * <code>Calendar</code> until {@link #informCompleted} is
     * called.
     */
    public Calendar getCalendar()
        throws IOException {
        if (! isCalendarContent()) {
            return null;
        }
        if (calendar == null) {
            try {
                CalendarBuilder builder = new CalendarBuilder();
                calendar = builder.build(getInputStream());
            } catch (ParserException e) {
                throw new InvalidDataException("error parsing calendar stream",
                                               e);
            }
        }
        return calendar;
    }

    /** */
    public void informCompleted(boolean success) {
        super.informCompleted(success);
        calendar = null;
    }
}
