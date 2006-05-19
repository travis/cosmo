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
package org.osaf.cosmo.dav.caldav;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Instance;
import net.fortuna.ical4j.model.InstanceList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.Dates;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the <code>CALDAV:free-busy-query</code> report that
 * provides a mechanism for finding free-busy information. It should
 * be supported by all CalDAV resources. <p/> CalDAV specifies the
 * following required format for the request body:
 *
 * <pre>
 *                         &lt;!ELEMENT free-busy-query (time-range)&gt;
 * </pre>
 */
public class FreeBusyReport extends CaldavSingleResourceReport {
    private static final Log log = LogFactory.getLog(FreeBusyReport.class);

    Period freeBusyRange;

    // Report methods

    /** */
    public ReportType getType() {
        return CALDAV_FREEBUSY;
    }

    // CaldavReport methods

    /**
     * Parse information from the given report info needed to execute
     * the report. Sets a query filter based on the included time
     * range information.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info)) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "report not of type " + getType());
        }

        Element tre =
            info.getContentElement(CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE,
                                   CosmoDavConstants.NAMESPACE_CALDAV);
        if (tre == null) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "CALDAV:free-busy-query must contain one time-range element");
        }

        String start =
            DomUtil.getAttribute(tre, CosmoDavConstants.ATTR_CALDAV_START,
                                 null);
        String end =
            DomUtil.getAttribute(tre, CosmoDavConstants.ATTR_CALDAV_END,
                                 null);
        setQueryFilter(createQueryFilter(tre.getOwnerDocument(), start, end));

        try {
            freeBusyRange = new Period(new DateTime(start), new DateTime(end));
        } catch (ParseException e) {
            log.error("cannot parse CALDAV:time-range", e);
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "cannot parse CALDAV:time-range: " + e.getMessage());
        }
    }

    // CaldavSingleResourceReport

    /**
     * Iterate over each calendar resource found by the report query,
     * extract the set of periods for busy time for each instance,
     * then create a VFREEBUSY component with those periods, and set
     * the report's stream, content type and character set.
     */
    protected void buildResponse()
        throws DavException {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        // Create recurrence instances within the time-range
        for (Iterator i = getHrefs().iterator(); i.hasNext();) {
            String href = (String) i.next();

            // Get resource for this href
            DavResource child = getResource().getMember(href);
            if (child == null) {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST, "href " + href + " could not be resolved.");
            }

            // Read in calendar data
            String calendarData = readCalendarData((CosmoDavResource) child);

            try {
                // Parse the calendar data into an iCalendar object
                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar =
                    builder.build(new StringReader(calendarData));

                // Add busy details from the calendar data
                addBusyPeriods(calendar, busyPeriods, busyTentativePeriods,
                               busyUnavailablePeriods);
            } catch (IOException e) {
                log.error("cannot read calendar for resource " + child.getResourcePath(), e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,  "cannot read calendar data: " + e.getMessage());
            } catch (ParserException e) {
                log.error("cannot parse calendar for resource " + child.getResourcePath(), e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,  "cannot parse calendar data: " + e.getMessage());
            }
        }

        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();

        // Now create a VFREEBUSY in a calendar
        Calendar calendar = new Calendar();
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));

        VFreeBusy vfb =
            new VFreeBusy(freeBusyRange.getStart(), freeBusyRange.getEnd());
        calendar.getComponents().add(vfb);

        // Add all periods to the VFREEBUSY
        if (busyPeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyPeriods);
            vfb.getProperties().add(fb);
        }
        if (busyTentativePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyTentativePeriods);
            fb.getParameters().add(FbType.BUSY_TENTATIVE);
            vfb.getProperties().add(fb);
        }
        if (busyUnavailablePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyUnavailablePeriods);
            fb.getParameters().add(FbType.BUSY_UNAVAILABLE);
            vfb.getProperties().add(fb);
        }

        // Write the calendar object out
        StringWriter out = new StringWriter();
        String output = null;
        try {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
            output = out.toString();
            out.close();
        } catch (IOException e) {
            log.error("cannot generate freebusy", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot generate freebusy: " + e.getMessage());
        } catch (ValidationException e) {
            log.error("invalid freebusy generated", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "invalid freebusy generated: " + e.getMessage());
        }

        // NB ical4j's outputter generates \r\n line ends but we
        // need only \n, so remove all \r's from the string
        output = output.replaceAll("\r", "");

        setContentType("text/calendar");
        setEncoding("UTF-8"); 
        setStream(new ByteArrayInputStream(output.getBytes()));
    }

    // private methods

    QueryFilter createQueryFilter(Document doc,
                                  String start,
                                  String end)
        throws DavException {
        // Create a fake calendar-filter element designed to match
        // VEVENTs/VFREEBUSYs within the specified time range.
        //
        // <C:filter>
        // <C:comp-filter name="VCALENDAR">
        // <C:comp-filter name="VEVENT">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // <C:comp-filter name="VFREEBUSY">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // </C:comp-filter>
        // </C:filter>

        Element calendarFilter =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_FILTER,
                                  CosmoDavConstants.NAMESPACE_CALDAV);

        Element compFilterVCalendar =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                                  CosmoDavConstants.NAMESPACE_CALDAV);
        compFilterVCalendar.
            setAttribute(CosmoDavConstants.ATTR_CALDAV_NAME, "VCALENDAR");
        calendarFilter.appendChild(compFilterVCalendar);

        Element compFilterVEvent =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                                  CosmoDavConstants.NAMESPACE_CALDAV);
        compFilterVEvent.
            setAttribute(CosmoDavConstants.ATTR_CALDAV_NAME, "VEVENT");
        compFilterVCalendar.appendChild(compFilterVEvent);

        Element timeRange =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE,
                                  CosmoDavConstants.NAMESPACE_CALDAV);
        timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_START, start);
        timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_END, end);
        compFilterVEvent.appendChild(timeRange);

        Element compFilterVFreeBusy =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                                  CosmoDavConstants.NAMESPACE_CALDAV);
        compFilterVFreeBusy.
            setAttribute(CosmoDavConstants.ATTR_CALDAV_NAME, "VFREEBUSY");
        compFilterVCalendar.appendChild(compFilterVFreeBusy);

        timeRange =
            DomUtil.createElement(doc,
                                  CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE,
                                  CosmoDavConstants.NAMESPACE_CALDAV);
        timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_START, start);
        timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_END, end);
        compFilterVFreeBusy.appendChild(timeRange);

        // Parse out fake filter element
        QueryFilter filter = new QueryFilter();
        try {
            filter.createFromXml(calendarFilter);
        } catch (ParseException e) {
            log.error("error parsing pseudo freebusy filter", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "error parsing pseudo freebusy filter");
        }
        
        return filter;
    }

    private void addBusyPeriods(Calendar calendar,
                                PeriodList busyPeriods,
                                PeriodList busyTentativePeriods,
                                PeriodList busyUnavailablePeriods) {
        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList overrides = new ComponentList();
        for (Iterator i=calendar.getComponents().iterator(); i.hasNext();) {
            Component comp = (Component) i.next();
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                // See if this is the master instance
                if (vcomp.getReccurrenceId() == null) {
                    instances.addComponent(vcomp, freeBusyRange.getStart(),
                                           freeBusyRange.getEnd());
                } else {
                    overrides.add(vcomp);
                }
            } else if (comp instanceof VFreeBusy) {
                // Add all FREEBUSY BUSY/BUSY-TENTATIVE/BUSY-UNAVAILABLE to the
                // periods
                PropertyList fbs =
                    comp.getProperties().getProperties(Property.FREEBUSY);
                for (Iterator j = fbs.iterator(); j.hasNext();) {
                    FreeBusy fb = (FreeBusy) j.next();
                    FbType fbt = (FbType)
                        fb.getParameters().getParameter(Parameter.FBTYPE);
                    if ((fbt == null) || FbType.BUSY.equals(fbt)) {
                        busyPeriods.add(fb.getPeriods());
                    } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                        busyTentativePeriods.add(fb.getPeriods());
                    } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                        busyUnavailablePeriods.add(fb.getPeriods());
                    }
                }
            }
        }

        for (Iterator i = overrides.iterator(); i.hasNext();) {
            Component comp = (Component) i.next();
            instances.addComponent(comp, null, null);
        }

        // See if there is nothing to do (should not really happen)
        if (instances.size() == 0) {
            return;
        }

        // Add start/end period for each instance
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        for (Iterator i = sortedKeys.iterator(); i.hasNext();) {
            String ikey = (String) i.next();
            Instance instance = (Instance) instances.get(ikey);

            // Check that the VEVENT has the proper busy status
            if (Transp.TRANSPARENT.equals(instance.getComp().getProperties()
                                          .getProperty(Property.TRANSP))) {
                continue;
            }
            if (Status.VEVENT_CANCELLED.equals(instance.getComp().
                                               getProperties().
                                               getProperty(Property.STATUS))) {
                continue;
            }

            // Can only have DATE-TIME values in PERIODs
            if (instance.getStart() instanceof DateTime) {
                DateTime start = (DateTime) instance.getStart();
                DateTime end = (DateTime) instance.getEnd();

                if (start.compareTo(freeBusyRange.getStart()) < 0) {
                    start = (DateTime)
                        Dates.getInstance(freeBusyRange.getStart(),
                                          freeBusyRange.getStart());
                }
                if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                    end = (DateTime)
                        Dates.getInstance(freeBusyRange.getEnd(),
                                          freeBusyRange.getEnd());
                }
                if (Status.VEVENT_TENTATIVE.equals(instance.getComp()
                                                   .getProperties().
                                                   getProperty(Property.STATUS))) {
                    busyTentativePeriods.add(new Period(start, end));
                } else {
                    busyPeriods.add(new Period(start, end));
                }
            }
        }
    }
}
