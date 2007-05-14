/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;

/**
 * Abstract calendar resource.
 */
public abstract class DavCalendarResource extends DavFile {
    
    private static final Log log =
        LogFactory.getLog(DavCalendarResource.class);
  
    public DavCalendarResource(NoteItem item, DavResourceLocator locator,
            DavResourceFactory factory, DavSession session) {
        super(item, locator, factory, session);
    }
       
    // DavResource methods

    /** */
    public void move(DavResource destination)
        throws DavException {
        validateDestination(destination);
        super.move(destination);
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws DavException {
        validateDestination(destination);
        super.copy(destination, shallow);
    }

    @Override
    protected void populateItem(InputContext inputContext) throws DavException {
        super.populateItem(inputContext);
        NoteItem content = (NoteItem) getItem();
        Calendar calendar = null;
        
        // CALDAV:valid-calendar-data
        try {
            // forces the event's content to be parsed
            calendar = CalendarUtils.parseCalendar(content.getContentInputStream());
            calendar.validate(true);
        } catch (IOException e) {
            log.error("Cannot read resource content", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot read resource content: " + e.getMessage());
        }catch (ValidationException e) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Invalid calendar object: " + e.getMessage());
        } catch (ParserException e) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Invalid calendar object: " + e.getMessage());
        } 
        
        setCalendar(calendar);
        
        try {
            // set the contentLength on ContentItem
            content.setContentLength((long) calendar.toString().getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // no need to store content twice (already stored as event stamp)
        content.clearContent();
    }
    
    // our methods

    /**
     * Returns the calendar object associated with this resource.
     */
    public abstract Calendar getCalendar();
    
    /**
     * Set the calendar object associated with this resource.
     * @param calendar calendar object parsed from inputcontext
     */
    protected abstract void setCalendar(Calendar calendar);

    private void validateDestination(DavResource destination)
        throws DavException {
        DavResource destinationCollection = destination.getCollection();

        // XXX: we should allow items to be moved/copied out of
        // calendar collections into regular collections, but they
        // need to be stripped of their calendar-ness
        if (! (destinationCollection instanceof DavCalendarCollection))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Destination collection not a calendar collection");

        CollectionItem collection = (CollectionItem)
            ((DavResourceBase) destinationCollection).getItem();
        CalendarCollectionStamp calendar = CalendarCollectionStamp.getStamp(collection);
        
        NoteItem item = (NoteItem) getItem();
        EventStamp event = EventStamp.getStamp(item);

        if (log.isDebugEnabled())
            log.debug("validating destination " +
                      destination.getResourcePath());

        // CALDAV:supported-calendar-data
        // had to be of appropriate media type to be stored in the
        // original calendar collection

        // CALDAV:valid-calendar-data
        // same

        // CALDAV:valid-calendar-object-resource
        // same

        // CALDAV:supported-calendar-component
        // destination collection may support different component set
        if (! calendar.supportsCalendar(event.getCalendar()))
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Calendar object does not contain at least one supported component");

        // CALDAV:calendar-collection-location-ok not required here

        // CALDAV:max-resource-size was already taken care of when
        // DavCollection.addMember called DavResourceBase.populateItem
        // on the item, though it returned a 409 rather than a 412

        // XXX CALDAV:min-date-time

        // XXX CALDAV:max-date-time

        // XXX CALDAV:max-instances

        // XXX CALDAV:max-attendees-per-instance
    }
    
    @Override
    /** */
    public void spool(OutputContext outputContext)
        throws IOException {
        if (! exists())
            throw new IllegalStateException("cannot spool a nonexistent resource");

        if (log.isDebugEnabled())
            log.debug("spooling file " + getResourcePath());

        ContentItem content = (ContentItem) getItem();

        String contentType =
            IOUtil.buildContentType(content.getContentType(),
                                    content.getContentEncoding());
        outputContext.setContentType(contentType);

        if (content.getContentLanguage() != null)
            outputContext.setContentLanguage(content.getContentLanguage());
        
        // convert Calendar object to String, then to bytes (UTF-8)
        byte[] calendarBytes = getCalendar().toString().getBytes("UTF-8");
        outputContext.setContentLength(calendarBytes.length);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());
        
        // track mismatches
        long storedLength = content.getContentLength() == null ? 0 : content
                .getContentLength().longValue();
        if(calendarBytes.length != storedLength)
            log.warn("contentLength: " + content.getContentLength() + 
                    " does not match content: " + calendarBytes.length);

        if (! outputContext.hasStream())
            return;

        // spool calendar bytes
        ByteArrayInputStream bois = new ByteArrayInputStream(calendarBytes);
        IOUtil.spool(bois, outputContext.getOutputStream());
    }
}
