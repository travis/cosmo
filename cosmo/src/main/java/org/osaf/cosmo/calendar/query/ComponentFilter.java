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
package org.osaf.cosmo.calendar.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;

import org.w3c.dom.Element;

/**
 * Represents a component filter as defined in the CalDAV spec:
 * 
 * from Section 9.6.1:
 * 
 * CALDAV:comp-filter <!ELEMENT comp-filter (is-not-defined | (time-range?,
 * prop-filter*, comp-filter*))>
 * 
 * <!ATTLIST comp-filter name CDATA #REQUIRED>
 * 
 * name value: a calendar component name (e.g., "VEVENT")
 * 
 * Note: This object model does not validate. For example you can create a
 * ComponentFilter with a IsNotDefinedFilter and a TimeRangeFilter. It is the
 * responsibility of the business logic to enforce this. This may be changed
 * later.
 */
public class ComponentFilter implements CaldavConstants, ICalendarConstants {

    private static final Log log = LogFactory
            .getLog(ComponentFilter.class);

    private List componentFilters = new ArrayList();

    private List propFilters = new ArrayList();

    private IsNotDefinedFilter isNotDefinedFilter = null;

    private TimeRangeFilter timeRangeFilter = null;

    private String name = null;

    public ComponentFilter() {
    }

    public ComponentFilter(Element element) throws ParseException {
        this(element, null);
    }
    
    /**
     * Construct a ComponentFilter object from a DOM Element
     * @param element
     * @throws ParseException
     */
    public ComponentFilter(Element element,VTimeZone timezone) throws ParseException {
        // Name must be present
        name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);

        if (name == null) {
            throw new ParseException(
                    "CALDAV:comp-filter a calendar component name  (e.g., \"VEVENT\") is required",
                    -1);
        }

        if (! (name.equals(Calendar.VCALENDAR) ||
            CalendarUtils.isSupportedComponent(name) ||
            name.equals(Component.VALARM) ||
            name.equals(Component.VTIMEZONE)))
            throw new ParseException(name + " is not a supported iCalendar component", -1);

        ElementIterator i = DomUtil.getChildren(element);
        int childCount = 0;
        
        while (i.hasNext()) {
            Element child = i.nextElement();
            childCount++;

            // if is-not-defined is present, then nothing else can be present
            if (childCount > 1 && isNotDefinedFilter != null)
                throw new ParseException(
                        "CALDAV:is-not-defined cannnot be present with other child elements",
                        -1);

            if (ELEMENT_CALDAV_TIME_RANGE.equals(child.getLocalName())) {

                // Can only have one time-range element in a comp-filter
                if (timeRangeFilter != null) {
                    throw new ParseException(
                            "CALDAV:comp-filter only one time-range element permitted",
                            -1);
                }

                timeRangeFilter = new TimeRangeFilter(child, timezone);
            } else if (ELEMENT_CALDAV_COMP_FILTER.equals(child.getLocalName())) {

                // Add to list
                componentFilters.add(new ComponentFilter(child, timezone));

            } else if (ELEMENT_CALDAV_PROP_FILTER.equals(child.getLocalName())) {

                // Add to list
                propFilters.add(new PropertyFilter(child, timezone));

            } else if (ELEMENT_CALDAV_IS_NOT_DEFINED.equals(child
                    .getLocalName())) {
                if (childCount > 1)
                    throw new ParseException(
                            "CALDAV:is-not-defined cannnot be present with other child elements",
                            -1);
                isNotDefinedFilter = new IsNotDefinedFilter();
            } else if (ELEMENT_CALDAV_IS_DEFINED.equals(child
                    .getLocalName())) {
                // XXX provided for backwards compatibility with
                // Evolution 2.6, which does not implement
                // is-not-defined;
                if (childCount > 1)
                    throw new ParseException(
                            "CALDAV:is-defined cannnot be present with other child elements",
                            -1);
                log.warn("old style 'is-defined' ignored from (outdated) client!");
            } else
                throw new ParseException(
                        "CALDAV:comp-filter an invalid element name found", -1);

        }
    }

    /**
     * Create a new ComponentFilter with the specified component name.
     * 
     * @param name
     *            component name
     */
    public ComponentFilter(String name) {
        this.name = name;
    }

    public IsNotDefinedFilter getIsNotDefinedFilter() {
        return isNotDefinedFilter;
    }

    public void setIsNotDefinedFilter(IsNotDefinedFilter isNotDefinedFilter) {
        this.isNotDefinedFilter = isNotDefinedFilter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getComponentFilters() {
        return componentFilters;
    }

    public void setComponentFilters(List componentFilters) {
        this.componentFilters = componentFilters;
    }

    public TimeRangeFilter getTimeRangeFilter() {
        return timeRangeFilter;
    }

    public void setTimeRangeFilter(TimeRangeFilter timeRangeFilter) {
        this.timeRangeFilter = timeRangeFilter;
    }

    public List getPropFilters() {
        return propFilters;
    }

    public void setPropFilters(List propFilters) {
        this.propFilters = propFilters;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", name).
            append("isNotDefinedFilter", isNotDefinedFilter).
            append("timeRangeFilter", timeRangeFilter).
            append("componentFilters", componentFilters).
            append("propFilters", propFilters).
            toString();
    }
    
    public void validate() {
        for(Iterator<ComponentFilter> it= componentFilters.iterator(); it.hasNext();)
            it.next().validate();
        for(Iterator<PropertyFilter> it= propFilters.iterator(); it.hasNext();)
            it.next().validate();
    }
}
