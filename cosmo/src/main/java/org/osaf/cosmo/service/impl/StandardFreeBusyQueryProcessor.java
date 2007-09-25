/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

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
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.osaf.cosmo.calendar.Instance;
import org.osaf.cosmo.calendar.InstanceList;
import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.calendar.query.ComponentFilter;
import org.osaf.cosmo.calendar.query.TimeRangeFilter;
import org.osaf.cosmo.dao.CalendarDao;
import org.osaf.cosmo.icalendar.ICalendarOutputter;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.service.freebusy.FreeBusyQueryProcessor;

/**
 * Default FreeBusyQueryProcessor implementation.
 */
public class StandardFreeBusyQueryProcessor implements FreeBusyQueryProcessor {

    private CalendarDao calendarDao = null;
    private static final VersionFourGenerator uuidGenerator =
        new VersionFourGenerator();
    
   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.service.freebusy.FreeBusyQueryProcessor#generateFreeBusy(org.osaf.cosmo.model.CollectionItem, net.fortuna.ical4j.model.Period)
     */
    public VFreeBusy generateFreeBusy(CollectionItem collection, Period period) { 
        
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        doQuery(busyPeriods, busyTentativePeriods, busyUnavailablePeriods,
                collection, period);

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

   
    /* (non-Javadoc)
     * @see org.osaf.cosmo.service.freebusy.FreeBusyQueryProcessor#generateFreeBusy(org.osaf.cosmo.model.ICalendarItem, net.fortuna.ical4j.model.Period)
     */
    public VFreeBusy generateFreeBusy(ICalendarItem item, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        Calendar calendar = ICalendarOutputter.getCalendar(item);
        
        // Add busy details from the calendar data
        addBusyPeriods(calendar, null, period, busyPeriods,
                busyTentativePeriods, busyUnavailablePeriods);
        
        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

    protected void doQuery(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            CollectionItem collection, Period period) {

        CalendarCollectionStamp ccs = CalendarCollectionStamp.getStamp(collection);
        if(ccs==null)
            return;
        
        HashSet<ContentItem> results = new HashSet<ContentItem>();
        TimeZone tz = ccs.getTimezone();
        
        // For the time being, use CalendarFilters to get relevant
        // items.
        CalendarFilter[] filters = createQueryFilters(collection, period);
        for(CalendarFilter filter: filters)
            results.addAll(calendarDao.findCalendarItems(collection, filter));
        
        for(ContentItem content: results) {
            Calendar calendar = ICalendarOutputter.getCalendar(content);
            if(calendar==null)
                continue;
            // Add busy details from the calendar data
            addBusyPeriods(calendar, tz, period, busyPeriods,
                    busyTentativePeriods, busyUnavailablePeriods);
        }
    }
    
    
    protected void addBusyPeriods(Calendar calendar, TimeZone timezone,
            Period freeBusyRange, PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods) {
        
        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();
        instances.setUTC(true);

        instances.setTimezone(timezone);

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList overrides = new ComponentList();
        for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
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
                PropertyList fbs = comp.getProperties().getProperties(
                        Property.FREEBUSY);
                for (Iterator j = fbs.iterator(); j.hasNext();) {
                    FreeBusy fb = (FreeBusy) j.next();
                    FbType fbt = (FbType) fb.getParameters().getParameter(
                            Parameter.FBTYPE);
                    if ((fbt == null) || FbType.BUSY.equals(fbt)) {
                        addRelevantPeriods(busyPeriods, fb.getPeriods(),
                                freeBusyRange);
                    } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                        addRelevantPeriods(busyTentativePeriods, fb
                                .getPeriods(), freeBusyRange);
                    } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                        addRelevantPeriods(busyUnavailablePeriods, fb
                                .getPeriods(), freeBusyRange);
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
            if (Status.VEVENT_CANCELLED.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                continue;
            }

            // Can only have DATE-TIME values in PERIODs
            if (instance.getStart() instanceof DateTime) {
                DateTime start = (DateTime) instance.getStart();
                DateTime end = (DateTime) instance.getEnd();
                Value sv = freeBusyRange.getStart() instanceof DateTime ? Value.DATE_TIME
                        : Value.DATE;
                Value se = freeBusyRange.getEnd() instanceof DateTime ? Value.DATE_TIME
                        : Value.DATE;

                if (start.compareTo(freeBusyRange.getStart()) < 0) {
                    start = (DateTime) Dates.getInstance(freeBusyRange
                            .getStart(), sv);
                }
                if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                    end = (DateTime) Dates.getInstance(freeBusyRange.getEnd(),
                            se);
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
    
    /**
     * Add all periods that intersect a given period to the result PeriodList.
     */
    private void addRelevantPeriods(PeriodList results, PeriodList periods,
            Period range) {

        for (Iterator<Period> it = periods.iterator(); it.hasNext();) {
            Period p = it.next();
            if (p.intersects(range))
                results.add(p);
        }
    }
    
    private CalendarFilter[] createQueryFilters(CollectionItem collection, Period period) {
        DateTime start = period.getStart();
        DateTime end = period.getEnd();
        CalendarFilter[] filters = new CalendarFilter[2];
        TimeZone tz = null;

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
        CalendarCollectionStamp ccs = CalendarCollectionStamp.getStamp(collection);
        if (ccs!=null) {
            tz = ccs.getTimezone();
        }

        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null)
            eventFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());

        ComponentFilter calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[0] = filter;

        ComponentFilter freebusyFilter = new ComponentFilter(
                Component.VFREEBUSY);
        freebusyFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null)
            freebusyFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());

        calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(freebusyFilter);

        filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[1] = filter;

        return filters;
    }
    
    private VFreeBusy createVFreeBusy(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            Period period) {
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();

        // Now create a VFREEBUSY
        VFreeBusy vfb = new VFreeBusy(period.getStart(), period.getEnd());
        String uid = uuidGenerator.nextIdentifier().toString();
        vfb.getProperties().add(new Uid(uid));

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

        return vfb;
    }

    public void setCalendarDao(CalendarDao calendarDao) {
        this.calendarDao = calendarDao;
    }
}
