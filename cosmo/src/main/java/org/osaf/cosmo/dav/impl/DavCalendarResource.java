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
package org.osaf.cosmo.dav.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResourceLocator;
import org.osaf.cosmo.dav.ProtectedPropertyModificationException;
import org.osaf.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.osaf.cosmo.dav.caldav.UidConflictException;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.caldav.report.MultigetReport;
import org.osaf.cosmo.dav.caldav.report.QueryReport;
import org.osaf.cosmo.dav.io.DavInputContext;
import org.osaf.cosmo.dav.property.ContentLength;
import org.osaf.cosmo.dav.property.ContentType;
import org.osaf.cosmo.dav.property.DavProperty;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.IcalUidInUseException;
import org.osaf.cosmo.model.NoteItem;

/**
 * Abstract calendar resource.
 */
public abstract class DavCalendarResource extends DavContentBase
    implements ICalendarConstants {
    private static final Log log =
        LogFactory.getLog(DavCalendarResource.class);
    private static final Set<ReportType> REPORT_TYPES =
        new HashSet<ReportType>();
    
    static {
        registerLiveProperty(DavPropertyName.GETCONTENTLENGTH);
        registerLiveProperty(DavPropertyName.GETCONTENTTYPE);

        REPORT_TYPES.add(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY);
        REPORT_TYPES.add(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET);
        REPORT_TYPES.add(QueryReport.REPORT_TYPE_CALDAV_QUERY);
    }

    public DavCalendarResource(ContentItem item,
                               DavResourceLocator locator,
                               DavResourceFactory factory,
                               EntityFactory entityFactory)
        throws DavException {
        super(item, locator, factory, entityFactory);
    }
       
    // DavResource methods

    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, POST, TRACE, PROPFIND, PROPPATCH, COPY, PUT, DELETE, MOVE, MKTICKET, DELTICKET, REPORT";
    }

    /** */
    public void move(DavResource destination)
        throws org.apache.jackrabbit.webdav.DavException {
        validateDestination((DavItemResource)destination);
        try {
            super.move(destination);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        }
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws org.apache.jackrabbit.webdav.DavException {
        validateDestination((DavItemResource)destination);
        try {
            super.copy(destination, shallow);
        } catch (IcalUidInUseException e) {
            throw new UidConflictException(e);
        }
    }

    // DavResourceBase methods

    @Override
    protected void populateItem(InputContext inputContext)
        throws DavException {
        super.populateItem(inputContext);

        DavInputContext dic = (DavInputContext) inputContext;
        Calendar calendar = dic.getCalendar();

        setCalendar(calendar);
    }

    // our methods

    /**
     * Returns true if this resource matches the given filter.
     */
    public boolean matches(CalendarFilter filter)
        throws DavException {
        return getCalendarQueryProcesor().filterQuery((NoteItem)getItem(), filter);
    }

    /**
     * Returns a VFREEBUSY component containing
     * the freebusy periods for the resource for the specified time range.
     * @param period time range for freebusy information
     * @return VFREEBUSY component containing FREEBUSY periods for
     *         specified timerange
     */
    public VFreeBusy generateFreeBusy(Period period) {
        return getCalendarQueryProcesor().
            freeBusyQuery((ICalendarItem)getItem(), period);
    }

    /**
     * Returns the calendar object associated with this resource.
     */
    public abstract Calendar getCalendar();
    
    /**
     * Set the calendar object associated with this resource.
     * @param calendar calendar object parsed from inputcontext
     */
    protected abstract void setCalendar(Calendar calendar)
        throws DavException;

    private void validateDestination(DavItemResource destination)
        throws DavException {
        if (log.isDebugEnabled())
            log.debug("validating destination " + destination.getResourcePath());

        // XXX: we should allow items to be moved/copied out of
        // calendar collections into regular collections, but they
        // need to be stripped of their calendar-ness
        if (! (destination.getParent() instanceof DavCalendarCollection))
            throw new InvalidCalendarLocationException("Destination collection must be a calendar collection");
    }

    public void writeTo(OutputContext outputContext)
        throws DavException, IOException {
        if (! exists())
            throw new IllegalStateException("cannot spool a nonexistent resource");

        if (log.isDebugEnabled())
            log.debug("spooling file " + getResourcePath());

        String contentType =
            IOUtil.buildContentType(ICALENDAR_MEDIA_TYPE, "UTF-8");
        outputContext.setContentType(contentType);
  
        // convert Calendar object to String, then to bytes (UTF-8)
        byte[] calendarBytes = getCalendar().toString().getBytes("UTF-8");
        outputContext.setContentLength(calendarBytes.length);
        outputContext.setModificationTime(getModificationTime());
        outputContext.setETag(getETag());
        
        if (! outputContext.hasStream())
            return;

        // spool calendar bytes
        ByteArrayInputStream bois = new ByteArrayInputStream(calendarBytes);
        IOUtil.spool(bois, outputContext.getOutputStream());
    }

    public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }

    /** */
    protected void loadLiveProperties(DavPropertySet properties) {
        super.loadLiveProperties(properties);

        try {
            byte[] calendarBytes = getCalendar().toString().getBytes("UTF-8");
            properties.add(new ContentLength(new Long(calendarBytes.length)));
        } catch (Exception e) {
            throw new RuntimeException("Can't convert calendar", e);
        }

        properties.add(new ContentType(ICALENDAR_MEDIA_TYPE, "UTF-8"));
    }

    /** */
    protected void setLiveProperty(DavProperty property)
        throws DavException {
        super.setLiveProperty(property);

        DavPropertyName name = property.getName();
        if (name.equals(DavPropertyName.GETCONTENTTYPE))
            throw new ProtectedPropertyModificationException(name);
    }
}
