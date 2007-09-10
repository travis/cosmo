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
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
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
import org.osaf.cosmo.model.ModelConversionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>CALDAV:free-busy-query</code> report that
 * provides a mechanism for finding free-busy information. 
 * </p>
 */
public class FreeBusyReport extends SimpleReport implements CaldavConstants {
    private static final Log log = LogFactory.getLog(FreeBusyReport.class);
    private static final VersionFourGenerator uuidGenerator =
        new VersionFourGenerator();

    private VTimeZone tz = null;
    private Period freeBusyRange;
    private CalendarFilter[] queryFilters;

    public static final ReportType REPORT_TYPE_CALDAV_FREEBUSY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                            NAMESPACE_CALDAV, FreeBusyReport.class);

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_FREEBUSY;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting the time range and query filter.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info))
            throw new DavException("Report not of type " + getType());

        freeBusyRange = findFreeBusyRange(info);
        queryFilters =
            createQueryFilter(info.getReportElement().getOwnerDocument(),
                              freeBusyRange);
    }

    /**
     * Runs the query, extracts the set of periods for busy time for
     * each occurrence of each found calendar item, creates a
     * VFREEBUSY component, and sets the report's stream, content type
     * and character set.
     */
    protected void runQuery()
        throws DavException {
        if (! (getResource() instanceof DavItemCollection))
            throw new UnprocessableEntityException(getType() + " report not supported for non-collection resources");

        // if the collection or any of its parent is excluded from
        // free busy rollups, deny the query
        DavItemCollection dc = (DavItemCollection) getResource();
        while (dc != null) {
            if (dc.isExcludedFromFreeBusyRollups())
                throw new ForbiddenException("Targeted collection does not participate in freebusy rollups");
            dc = (DavItemCollection) dc.getCollection();
        }

        super.runQuery();

        Calendar calendar = buildFreeBusy(getResults(), tz, freeBusyRange);
        String output = writeCalendar(calendar);

        setContentType("text/calendar");
        setEncoding("UTF-8"); 
        setStream(new ByteArrayInputStream(output.getBytes()));
    }    

    protected void doQuerySelf(DavResource resource)
        throws DavException {
        // runQuery already enforced that this is a collection, which never
        // matches a calendar report query
    }

    protected void doQueryChildren(DavCollection collection)
        throws DavException {
        if (isExcluded(collection))
            return;
        if (collection instanceof DavCalendarCollection) {
            DavCalendarCollection dcc = (DavCalendarCollection) collection;
            for(CalendarFilter filter: getQueryFilters())
                getResults().addAll(dcc.findMembers(filter));
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

    public CalendarFilter[] getQueryFilters() {
        return queryFilters;
    }

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

    CalendarFilter[] createQueryFilter(Document doc,
                                     Period period) {
        DateTime start = period.getStart();
        DateTime end = period.getEnd();
        CalendarFilter[] filters = new CalendarFilter[2];

        // Create calendar-filter elements designed to match
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

        // If the calendar collection has a timezone attribute,
        // then use that to convert floating date/times to UTC
        if (getResource() instanceof DavCalendarCollection) {
            tz = ((DavCalendarCollection) getResource()).getTimeZone();
        }
     
        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        eventFilter.getTimeRangeFilter().setTimezone(tz);
        
        ComponentFilter calFilter =
            new ComponentFilter(net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);
        

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[0] = filter;
        
        ComponentFilter freebusyFilter =
            new ComponentFilter(Component.VFREEBUSY);
        freebusyFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        freebusyFilter.getTimeRangeFilter().setTimezone(tz);
        
        calFilter =
            new ComponentFilter(net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);
        

        filter = new CalendarFilter();
        filter.setFilter(calFilter);
        
        filters[1] = filter;
        
        return filters;
    }

    private static boolean isExcluded(DavCollection collection) {
        DavItemCollection dic = (DavItemCollection) collection;
        return dic.isExcludedFromFreeBusyRollups();
    }

    private static Calendar buildFreeBusy(Set<DavResource> resources,
                                          VTimeZone tz,
                                          Period freeBusyRange)
        throws DavException {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();

        // Create recurrence instances within the time-range
        for (DavResource resource : resources) {
            try {
                Calendar calendar = ((DavCalendarResource)resource).getCalendar();

                // Add busy details from the calendar data
                addBusyPeriods(calendar, tz, freeBusyRange, busyPeriods,
                               busyTentativePeriods, busyUnavailablePeriods);
            } catch (ModelConversionException e) {
                throw new DavException(e);
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
        String uid = uuidGenerator.nextIdentifier().toString();
        vfb.getProperties().add(new Uid(uid));
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

        return calendar;
    }

    private static void addBusyPeriods(Calendar calendar,
                                       VTimeZone timezone,
                                       Period freeBusyRange,
                                       PeriodList busyPeriods,
                                       PeriodList busyTentativePeriods,
                                       PeriodList busyUnavailablePeriods) {
        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        
        if(timezone!=null)
            instances.setTimezone(new TimeZone(timezone));
        

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList overrides = new ComponentList();
        for (Iterator i=calendar.getComponents().iterator(); i.hasNext();) {
            Component comp = (Component) i.next();
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                // See if this is the master instance
                if (vcomp.getRecurrenceId() == null) {
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
            instances.addComponent(comp, freeBusyRange.getStart(),
                    freeBusyRange.getEnd());
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
                Value sv = freeBusyRange.getStart() instanceof DateTime ?
                    Value.DATE_TIME : Value.DATE;
                Value se = freeBusyRange.getEnd() instanceof DateTime ?
                    Value.DATE_TIME : Value.DATE;

                if (start.compareTo(freeBusyRange.getStart()) < 0) {
                    start = (DateTime)
                        Dates.getInstance(freeBusyRange.getStart(), sv);
                }
                if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                    end = (DateTime)
                        Dates.getInstance(freeBusyRange.getEnd(), se);
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
