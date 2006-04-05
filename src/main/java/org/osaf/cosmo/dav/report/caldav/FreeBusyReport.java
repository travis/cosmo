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
package org.osaf.cosmo.dav.report.caldav;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

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
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.impl.CosmoDavResourceImpl;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author cyrusdaboo
 * 
 * <code>FreeBusyReport</code> encapsulates the CALDAV:free-busy-query report,
 * that provides a mechanism for finding free-busy information. It should be
 * supported by all CalDAV resources. <p/> CalDAV specifies the following
 * required format for the request body:
 * 
 * <pre>
 *                         &lt;!ELEMENT free-busy-query (time-range)&gt;
 * </pre>
 * 
 */
public class FreeBusyReport extends AbstractCalendarQueryReport {
    private static final Log log = LogFactory.getLog(FreeBusyReport.class);

    Period freeBusyRange;

    /**
     * Returns {@link ReportType#CALDAV_FREEBUSY}.
     * 
     * @return
     * @see Report#getType()
     */
    public ReportType getType() {
        return ReportType.CALDAV_FREEBUSY;
    }

    /**
     * Set the <code>ReportInfo</code>.
     * 
     * @param info
     * @throws IllegalArgumentException
     *             if the given <code>ReportInfo</code> does not contain a
     *             DAV:expand-property element.
     * @see Report#setInfo(ReportInfo)
     */
    public void setInfo(ReportInfo info)
        throws IllegalArgumentException {
        if (info == null
                || !CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_FREEBUSY
                        .equals(info.getReportElement().getLocalName())) {
            throw new IllegalArgumentException(
                    "CALDAV:free-busy-query element expected.");
        }
        this.info = info;

        // Parse the report element.

        ElementIterator i = DomUtil.getChildren(info.getReportElement());
        while (i.hasNext()) {
            Element child = i.nextElement();
            String nodeName = child.getLocalName();
            if (CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE.equals(nodeName)) {

                // Can only have one filter
                if (filter != null) {
                    throw new IllegalArgumentException(
                            "CALDAV:free-busy-query must contain only one time-range element.");
                }

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

                Document doc = info.getReportElement().getOwnerDocument();
                Element calendarFilter =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_FILTER,
                        CosmoDavConstants.NAMESPACE_CALDAV);

                Element compFilterVCalendar =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                        CosmoDavConstants.NAMESPACE_CALDAV);
                compFilterVCalendar.setAttribute(
                        CosmoDavConstants.ATTR_CALDAV_NAME, "VCALENDAR");
                calendarFilter.appendChild(compFilterVCalendar);

                Element compFilterVEvent =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                        CosmoDavConstants.NAMESPACE_CALDAV);
                compFilterVEvent.setAttribute(
                        CosmoDavConstants.ATTR_CALDAV_NAME, "VEVENT");
                compFilterVCalendar.appendChild(compFilterVEvent);

                Element timeRange =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE,
                        CosmoDavConstants.NAMESPACE_CALDAV);
                timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_START,
                        DomUtil.getAttribute(child,
                            CosmoDavConstants.ATTR_CALDAV_START, null));
                timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_END,
                        DomUtil.getAttribute(child,
                            CosmoDavConstants.ATTR_CALDAV_END, null));
                compFilterVEvent.appendChild(timeRange);

                Element compFilterVFreeBusy =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                        CosmoDavConstants.NAMESPACE_CALDAV);
                compFilterVFreeBusy.setAttribute(
                        CosmoDavConstants.ATTR_CALDAV_NAME, "VFREEBUSY");
                compFilterVCalendar.appendChild(compFilterVFreeBusy);

                timeRange =
                    DomUtil.createElement(doc,
                        CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE,
                        CosmoDavConstants.NAMESPACE_CALDAV);
                timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_START,
                        DomUtil.getAttribute(child,
                            CosmoDavConstants.ATTR_CALDAV_START, null));
                timeRange.setAttribute(CosmoDavConstants.ATTR_CALDAV_END,
                        DomUtil.getAttribute(child,
                            CosmoDavConstants.ATTR_CALDAV_END, null));
                compFilterVFreeBusy.appendChild(timeRange);

                // Parse out fake filter element
                filter = new QueryFilter();
                filter.parseElement(calendarFilter);

                try {
                    String s =
                        DomUtil.getAttribute(child,
                                             CosmoDavConstants.ATTR_CALDAV_START,
                                             null);
                    DateTime start = new DateTime(s);
                    String e =
                        DomUtil.getAttribute(child,
                                             CosmoDavConstants.ATTR_CALDAV_END,
                                             null);
                    DateTime end = new DateTime(e);
                    freeBusyRange = new Period(start, end);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(
                            "CALDAV:free-busy-query cannot parse time-range element.");
                }

                // Now do fake prop find behaviour so that the calendar-data
                // element gets returned in propstat in the response
                propfindType = PROPFIND_BY_PROPERTY;
                propfindProps = new DavPropertyNameSet();
                propfindProps.add(CosmoDavConstants.CALENDARDATA);
            }
        }
    }

    /*
     */
    public Element toXml(Document doc) {
        // This is the meaty part of the query report. What we do here is
        // execute the query and generate the list of hrefs that match. Then we
        // extract the VFREEBUSY info for each matching event.

        // Initially empty list of matching hrefs
        hrefs = new Vector();

        try {
            // Create a query and run it
            Query q = getQuery();
            QueryResult qR = q.execute();

            // Convert the query results into hrefs for each result
            queryResultToHrefs(qR);

        } catch (RepositoryException e) {
            String msg = "Error while running CALDAV:" +
                info.getReportElement().getLocalName() + " report";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        String calendarData = generateFreeBusy();
        if (calendarData == null) {
            String msg = "no calendar data for CALDAV:" +
                info.getReportElement().getLocalName() + " report";
            log.error(msg);
            throw new RuntimeException(msg);
        }

        // Generate multi-status response for this data
        MultiStatus ms = new MultiStatus();
        CalDAVMultiStatusResponse response = new CalDAVMultiStatusResponse(
                resource, propfindProps, propfindType);
        response.setCalendarData(calendarData);
        ms.addResponse(response);

        return ms.toXml(doc);
    }

    /**
     * Generate the rolled-up free-busy component from all the matching hrefs.
     * 
     * @return the iCalendar data
     * @throws DavException
     */
    private String generateFreeBusy() {
        // Policy: iterate over each href and extract the set of periods for
        // busy
        // time for each instance within the free-busy time-range request. Then
        // create a VFREEBUSY component with those periods and write it out to
        // a string.

        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        // Create recurrence instances within the time-range
        for (Iterator iter = hrefs.iterator(); iter.hasNext();) {
            String href = (String) iter.next();

            // Get resource for this href
            DavResource hrefResource = ((CosmoDavResourceImpl) resource)
                    .getChildHref(href, info.getSession());

            // Check it is a child or the same
            if (hrefResource == null) {
                throw new IllegalArgumentException("CALDAV:"
                        + info.getReportElement().getLocalName()
                        + " href element could not be resolved.");
            }

            // Read in calendar data
            String calendarData = readCalendarData(hrefResource);

            try {
                // Parse the calendar data into an iCalendar object
                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar = builder
                        .build(new StringReader(calendarData));

                // Add busy details from the calendar data
                addBusyPeriods(calendar, busyPeriods, busyTentativePeriods,
                        busyUnavailablePeriods);

            } catch (IOException e) {
                String msg = "Error while running CALDAV:" +
                    info.getReportElement().getLocalName() + " report";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            } catch (ParserException e) {
                String msg = "Error while running CALDAV:" +
                    info.getReportElement().getLocalName() + " report";
                log.error(msg, e);
                throw new RuntimeException(msg, e);
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

        VFreeBusy vfb = new VFreeBusy(freeBusyRange.getStart(), freeBusyRange
                .getEnd());
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
        String result = null;
        try {
            CalendarOutputter outputer = new CalendarOutputter();
            outputer.output(calendar, out);
            result = out.toString();
            out.close();
        } catch (IOException e) {
            String msg = "Error while running CALDAV:" +
                info.getReportElement().getLocalName() + " report";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        } catch (ValidationException e) {
            String msg = "Error while running CALDAV:" +
                info.getReportElement().getLocalName() + " report";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        // NB ical4j's outputter generates \r\n line ends but we
        // need only \n, so remove all \r's from the string
        result = result.replaceAll("\r", "");

        return result;
    }

    /**
     * Determine busy information from the VEVENT & VFREEBUSY components in the
     * calendar taking into account the different types of busy time supported
     * by iCalendar. Recurrences are expanded.
     * 
     * @param calendar
     *            the calendar contains components
     * @param busyPeriods
     * @param busyTentativePeriods
     * @param busyUnavailablePeriods
     */
    private void addBusyPeriods(Calendar calendar,
                                PeriodList busyPeriods,
                                PeriodList busyTentativePeriods,
                                PeriodList busyUnavailablePeriods) {

        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList overrides = new ComponentList();
        for (Iterator iter = calendar.getComponents().iterator(); iter
                .hasNext();) {
            Component comp = (Component) iter.next();
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
                PropertyList fbs = comp.getProperties().getProperties(
                        Property.FREEBUSY);
                for (Iterator iterator = fbs.iterator(); iterator.hasNext();) {
                    FreeBusy fb = (FreeBusy) iterator.next();
                    FbType fbt = (FbType) fb.getParameters().getParameter(
                            Parameter.FBTYPE);
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

        for (Iterator iterator = overrides.iterator(); iterator.hasNext();) {
            Component comp = (Component) iterator.next();
            instances.addComponent(comp, null, null);
        }

        // See if there is nothing to do (should not really happen)
        if (instances.size() == 0) {
            return;
        }

        // Add start/end period for each instance
        TreeSet sortedKeys = new TreeSet(instances.keySet());
        for (Iterator iter = sortedKeys.iterator(); iter.hasNext();) {
            String ikey = (String) iter.next();
            Instance instance = (Instance) instances.get(ikey);

            // Check that the VEVENT has the proper busy status
            if (Transp.TRANSPARENT.equals(instance.getComp().getProperties()
                    .getProperty(Property.TRANSP))) {
                continue;
            }
            if (Status.VEVENT_CANCELLED.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                continue;
            }

            // Can only have DATE-TIME values in PERIODs
            if (instance.getStart() instanceof DateTime) {
                DateTime start = (DateTime) instance.getStart();
                DateTime end = (DateTime) instance.getEnd();

                if (start.compareTo(freeBusyRange.getStart()) < 0) {
                    start = (DateTime) Dates.getInstance(freeBusyRange
                            .getStart(), freeBusyRange.getStart());
                }
                if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                    end = (DateTime) Dates.getInstance(freeBusyRange.getEnd(),
                            freeBusyRange.getEnd());
                }
                if (Status.VEVENT_TENTATIVE.equals(instance.getComp()
                        .getProperties().getProperty(Property.STATUS))) {
                    busyTentativePeriods.add(new Period(start, end));
                } else {
                    busyPeriods.add(new Period(start, end));
                }
            }
        }
    }
}
