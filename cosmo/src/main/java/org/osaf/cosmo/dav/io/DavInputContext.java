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
import org.osaf.cosmo.dav.PreconditionFailedException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.UnsupportedMediaTypeException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;

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
        if (! IOUtil.getMimeType(getContentType()).equals(CT_ICALENDAR))
            throw new UnsupportedMediaTypeException("Content-type for calendar data must be " + CT_ICALENDAR);

        try {
            Calendar c = CalendarUtils.parseCalendar(getInputStream());
            c.validate(true);

            // CALDAV:valid-calendar-object-resource
            if (CalendarUtils.hasMultipleComponentTypes(c))
                throw new PreconditionFailedException("Calendar object contains more than one type of component");
            if (c.getProperties().getProperty(Property.METHOD) != null)
                throw new PreconditionFailedException("Calendar object contains METHOD property");

            calendar = c;
        } catch (IOException e) {
            throw new DavException(e);
        // CALDAV:valid-calendar-data
        } catch (ParserException e) {
            throw new BadRequestException("Failed to parse calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new UnprocessableEntityException("Invalid calendar object: " + e.getMessage());
        }

        return calendar;
    }
}
