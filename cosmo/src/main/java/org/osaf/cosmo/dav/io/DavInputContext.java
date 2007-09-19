/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import javax.servlet.http.HttpServletRequest;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.io.InputContextImpl;
import org.apache.jackrabbit.server.io.IOUtil;

import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.InvalidCalendarDataException;
import org.osaf.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.osaf.cosmo.dav.caldav.SupportedCalendarComponentException;
import org.osaf.cosmo.dav.caldav.UnsupportedCalendarDataException;

/**
 * An <code>InputContext</code> that supports the semantics of DAV
 * extensions like CalDAV.
 *
 * @see org.apache.jackrabbit.webdav.io.InputContext
 */
public class DavInputContext extends InputContextImpl
    implements CaldavConstants {
    private static final Log log = LogFactory.getLog(DavInputContext.class);

    private String contentType;
    private Calendar calendar;

    /**
     * If the HTTP request method is MKCALENDAR, sets the context's
     * content type to indicate calendar collection.
     */
    public DavInputContext(HttpServletRequest request,
                           InputStream in) {
        super(request, in);
        if (request.getMethod().equals("MKCALENDAR"))
            contentType = CONTENT_TYPE_CALENDAR_COLLECTION;
    }
    
    // InputContext methods

    /**
     * If the content type has been explicitly set, return
     * that. Otherwise, defer to the superclass method.
     */
    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        return super.getContentType();
    }

    // our methods

    /**
     * Parses the input stream into a calendar object.
     */
    public Calendar getCalendar()
        throws DavException {
        if (calendar != null)
            return calendar;

        if (! hasStream())
            return null;

        if (getContentType() == null)
            throw new BadRequestException("No media type specified");
        String mediaType = IOUtil.getMimeType(getContentType());
        if (! IOUtil.getMimeType(mediaType).equals(CT_ICALENDAR))
            throw new UnsupportedCalendarDataException(mediaType);

        try {
            Calendar c = CalendarUtils.parseCalendar(getInputStream());
            c.validate(true);

            if (CalendarUtils.hasMultipleComponentTypes(c))
                throw new InvalidCalendarResourceException("Calendar object contains more than one type of component");
            if (c.getProperties().getProperty(Property.METHOD) != null)
                throw new InvalidCalendarResourceException("Calendar object contains METHOD property");
            if (! CalendarUtils.hasSupportedComponent(c))
                throw new SupportedCalendarComponentException();

            calendar = c;
        } catch (IOException e) {
            throw new DavException(e);
        } catch (ParserException e) {
            throw new InvalidCalendarDataException("Failed to parse calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new InvalidCalendarDataException("Invalid calendar object: " + e.getMessage());
        }

        return calendar;
    }
}
