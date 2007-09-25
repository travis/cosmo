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
package org.osaf.cosmo.dav.caldav.report;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.calendar.FreeBusyUtils;
import org.osaf.cosmo.calendar.ICalendarUtils;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavCalendarResource;
import org.osaf.cosmo.dav.impl.DavItemCollection;
import org.osaf.cosmo.dav.report.SimpleReport;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>CALDAV:free-busy-query</code> report that
 * provides a mechanism for finding free-busy information. 
 * </p>
 */
public class FreeBusyReport extends SimpleReport implements CaldavConstants {
    private static final Log log = LogFactory.getLog(FreeBusyReport.class);
    
    private Period freeBusyRange;
    private ArrayList<VFreeBusy> freeBusyResults;

    public static final ReportType REPORT_TYPE_CALDAV_FREEBUSY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                            NAMESPACE_CALDAV, FreeBusyReport.class);

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_FREEBUSY;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting the time range.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info))
            throw new DavException("Report not of type " + getType());

        freeBusyRange = findFreeBusyRange(info);
    }

    /**
     * <p>
     * Runs the report query and generates a resulting <code>VFREEBUSY</code>.
     * </p>
     * <p>
     * The set of <code>VFREEBUSY</code> components that are returned by the
     * query are merged into a single component. It is wrapped in a calendar
     * object and converted using UTF-8 to bytes which are set as the report
     * stream. The report's content type and encoding are also set.
     * </p>
     *
     * @throws UnprocessableEntityException if the targeted resource is not a
     * collection and is not a calendar resource.
     * @throws ForbiddenException if the targeted resource or an ancestor
     * collection is excluded from free-busy rollups
     */
    protected void runQuery()
        throws DavException {
        if (! (getResource().isCollection() ||
               getResource() instanceof DavCalendarResource))
            throw new UnprocessableEntityException(getType() + " report not supported for non-calendar resources");

        // if the resource or any of its ancestors is excluded from
        // free busy rollups, deny the query
        DavItemCollection dc = getResource().isCollection() ?
            (DavItemCollection) getResource() :
            (DavItemCollection) getResource().getParent();
        while (dc != null) {
            if (dc.isExcludedFromFreeBusyRollups())
                throw new ForbiddenException("Targeted resource or ancestor collection does not participate in freebusy rollups");
            dc = (DavItemCollection) dc.getParent();
            if (! dc.exists())
                dc = null;
        }

        freeBusyResults = new ArrayList<VFreeBusy>();
        
        super.runQuery();

        VFreeBusy vfb =
            FreeBusyUtils.mergeComponents(freeBusyResults, freeBusyRange);
        Calendar calendar = ICalendarUtils.createBaseCalendar(vfb);
        String output = writeCalendar(calendar);

        setContentType("text/calendar");
        setEncoding("UTF-8"); 
        try {
            setStream(new ByteArrayInputStream(output.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new DavException(e);
        }
    }    

    protected void doQuerySelf(DavResource resource)
        throws DavException {
        if (! resource.isCollection()) {
            if (isExcluded(resource.getParent()))
                return;
            // runQuery() has already determined that this is a calendar
            // resource
            DavCalendarResource dcr = (DavCalendarResource) resource;
            VFreeBusy vfb = dcr.generateFreeBusy(freeBusyRange);
            if (vfb != null)
                freeBusyResults.add(vfb);
            return;
        }
        // collections themselves never match - only calendar resources
    }

    protected void doQueryChildren(DavCollection collection)
        throws DavException {
        if (isExcluded(collection))
            return;
        if (collection instanceof DavCalendarCollection) {
            DavCalendarCollection dcc = (DavCalendarCollection) collection;
            VFreeBusy vfb = dcc.generateFreeBusy(freeBusyRange);
            if(vfb!=null)
                freeBusyResults.add(vfb);
            return;
        }
        // if it's a regular collection, there won't be any calendar resources
        // within it to match the query
    }

    protected void doQueryDescendents(DavCollection collection)
        throws DavException {
        if (isExcluded(collection))
            return;
        super.doQueryDescendents(collection);
    }

    // our methods

    private static Period findFreeBusyRange(ReportInfo info)
        throws DavException {
        Element tre =
            info.getContentElement(ELEMENT_CALDAV_TIME_RANGE,
                                   NAMESPACE_CALDAV);
        if (tre == null)
            throw new BadRequestException("Expected " + QN_CALDAV_TIME_RANGE);

        DateTime sdt = null;
        try {
            String start = DomUtil.getAttribute(tre, ATTR_CALDAV_START, null);
            sdt = new DateTime(start);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_START + " not parseable: " + e.getMessage());
        }

        DateTime edt = null;
        try {
            String end = DomUtil.getAttribute(tre, ATTR_CALDAV_END, null);
            edt = new DateTime(end);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_END + " not parseable: " + e.getMessage());
        }

        return new Period(sdt, edt);
    }

    private static boolean isExcluded(DavCollection collection) {
        DavItemCollection dic = (DavItemCollection) collection;
        return dic.isExcludedFromFreeBusyRollups();
    }

    private VFreeBusy mergeFreeBusyResults() {
        // If there is only one result, return it
        if(freeBusyResults.size()==1)
            return freeBusyResults.get(0);
        
        // Otherwise, merge results into single VFREEBUSY
        return FreeBusyUtils.mergeComponents(freeBusyResults, freeBusyRange);
    }

    private static String writeCalendar(Calendar calendar)
        throws DavException {
        try {
            StringWriter out = new StringWriter();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
            String output = out.toString();
            out.close();

            // NB ical4j's outputter generates \r\n line ends but we
            // need only \n, so remove all \r's from the string
            output = output.replaceAll("\r", "");

            return output;
        } catch (Exception e) {
            throw new DavException(e);
        }
    }
}
