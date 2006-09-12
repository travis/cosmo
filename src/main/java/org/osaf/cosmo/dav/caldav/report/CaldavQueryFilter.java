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
package org.osaf.cosmo.dav.caldav.report;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.util.Dates;
import net.fortuna.ical4j.util.TimeZones;

import org.apache.log4j.Logger;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.caldav.CaldavConstants;

import net.fortuna.ical4j.model.Calendar;

import org.w3c.dom.Element;

/**
 * This class represents an object model for the calendar-query
 * report's <filter> element. It parses the element out and then
 * provides methods to generate XPATH queries as needed for JCR.
 *
 * Based on code written by Cyrus Daboo.
 */

public class CaldavQueryFilter
    implements DavConstants, CaldavConstants {
    private static final Logger log =
        Logger.getLogger(CaldavQueryFilter.class);

    private static final String TIME_RANGE_FIELD_SUFFIX = "--TIMERANGE";
    private static final String TIME_RANGE_FIELD_SUFFIX_LOWERCASE =
        TIME_RANGE_FIELD_SUFFIX.toLowerCase();

    protected compfilter filter;
    protected VTimeZone timezone;

    public CaldavQueryFilter() {
    }

    /** */
    public void setTimezone(VTimeZone timezone) {
        this.timezone = timezone;
    }

    /** */
    public void createFromXml(Element element)
        throws ParseException {
        // Can only have a single comp-filter element
        ElementIterator i =
            DomUtil.getChildren(element, ELEMENT_CALDAV_COMP_FILTER,
                                NAMESPACE_CALDAV);
        if (! i.hasNext()) {
            throw new ParseException("CALDAV:filter must contain a comp-filter", -1);
        }

        Element child = i.nextElement();

        if (i.hasNext()) {
            throw new ParseException("CALDAV:filter can contain only one comp-filter", -1);
        }

        // Create new component filter and have it parse the element
        filter = new compfilter();

        filter.createFromXml(child);
    }

    /**
     * Convert filter into XPath string for use with JCR queries.
     * 
     * @return the XPath query as a string.
     */
    public String toXPath() {
        // Look at only calendar resources
        String path = "/element(*, calendar:resource)";

        if (CosmoConstants.INDEX_VIRTUAL_PROPERTIES) {
            path += "/jcr:content";
        }

        // Generate a list of terms to use in the XPath expression
        String tests = filter.generateTests("");

        if (tests != null && ! tests.equals("")) {
            path += "[";
            path += tests;
            path += "]";
        }

        return path;
    }

    /**
     */
    protected String generatePeriods(Period period) {
        // Get fixed start/end time
        DateTime dstart = period.getStart();
        DateTime dend = period.getEnd();

        // Get float start/end
        DateTime fstart = (DateTime) Dates.getInstance(dstart, dstart);
        DateTime fend = (DateTime) Dates.getInstance(dend, dend);

        //if the timezone is null then default system timezone is used
        fstart.setTimeZone((timezone != null) ? new TimeZone(timezone) : null);

        //if the timezone is null then default system timezone is used
        fend.setTimeZone((timezone != null) ? new TimeZone(timezone) : null);

        return dstart.toString() + '/' + dend.toString() + ','
            + fstart.toString() + '/' + fend.toString();
    }

    private abstract class filter {

        abstract protected String generateTests(String prefix);

        protected String generateOrList(List items,
                                        String prefix,
                                        String result) {
            if (items.size() != 0) {
                boolean andExpression = (result.length() != 0);
                if (andExpression) {
                    result = result + " and ";
                    if (items.size() > 1)
                        result += '(';
                }

                boolean first = true;
                for (Iterator iter1 = items.iterator(); iter1.hasNext();) {
                    if (first)
                        first = false;
                    else
                        result += " or ";

                    result += ((filter) iter1.next()).generateTests(prefix);
                }

                if (andExpression && (items.size() > 1))
                    result += ')';
            }

            return result;
        }
    }

    /**
     */
    private class compfilter extends filter {

        protected String name;
        protected boolean useTimeRange;
        protected Period timeRange;

        protected List compFilters;
        protected List propFilters;

        public compfilter() {
            useTimeRange = false;
        }

        /** */
        public void createFromXml(Element element)
        throws ParseException {
            // Name must be present
            name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);

            if (name == null) {
                throw new ParseException("CALDAV:comp-filter a calendar component name  (e.g., \"VEVENT\") is required", -1);
            }

            // Look at each child component
            boolean got_one = false;

            ElementIterator i = DomUtil.getChildren(element);
            while (i.hasNext()) {
                Element child = i.nextElement();

                if (ELEMENT_CALDAV_TIME_RANGE.equals(child.getLocalName())) {

                    // Can only have one time-range element in a comp-filter
                    if (got_one) {
                        throw new ParseException("CALDAV:comp-filter only one time-range element permitted", -1);
                    }

                    got_one = true;

                    // Mark the value
                    useTimeRange = true;

                    // Get start (must be present)
                    String start =
                        DomUtil.getAttribute(child, ATTR_CALDAV_START, null);
                    if (start == null) {
                        throw new ParseException("CALDAV:comp-filter time-range requires a start time", -1);
                    }

                    DateTime trstart = new DateTime(start);
                    if (! trstart.isUtc()) {
                        throw new ParseException("CALDAV:param-filter timerange start must be UTC", -1);
                    }

                    
                    // Get end (must be present)
                    String end =
                        DomUtil.getAttribute(child, ATTR_CALDAV_END, null);
                    if (end == null) {
                        throw new ParseException("CALDAV:comp-filter time-range requires an end time", -1); 
                    }

                    DateTime trend = new DateTime(end);
                    if (! trend.isUtc()) {
                        throw new ParseException("CALDAV:param-filter timerange end must be UTC", -1);
                    }

                    timeRange = new Period(trstart, trend);
                } else if (ELEMENT_CALDAV_COMP_FILTER.
                           equals(child.getLocalName())) {

                    // Create new list if needed
                    if (compFilters == null)
                        compFilters = new Vector();

                    // Create new component filter
                    compfilter cfilter = new compfilter();

                    cfilter.createFromXml(child);

                    // Add to list
                    compFilters.add(cfilter);

                } else if (ELEMENT_CALDAV_PROP_FILTER.
                           equals(child.getLocalName())) {

                    // Create new list if needed
                    if (propFilters == null)
                        propFilters = new Vector();

                    // Create new prop filter
                    propfilter pfilter = new propfilter();

                    pfilter.createFromXml(child);

                    // Add to list
                    propFilters.add(pfilter);
                }
            }
        }

        /** */
        public String generateTests(String prefix) {
            String result = new String();

            // If this is the top-level item, then prepend the icalendar
            // namespace to the prefix
            String myprefix;
            if (prefix.length() == 0)
                myprefix = "icalendar:" + name.toLowerCase();
            else
                myprefix = prefix + "-" + name.toLowerCase();

            int compSize = (compFilters != null) ? compFilters.size() : 0;
            int propSize = (propFilters != null) ? propFilters.size() : 0;

            if (useTimeRange) {
                String key = myprefix + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
                String value = generatePeriods(timeRange);
                result = "jcr:timerange(@" + key + ", '" + value + "')";
            }

            // For each sub-component and property test, generate more tests
            if (compSize != 0) {
                result = generateOrList(compFilters, myprefix, result);
            }

            if (propSize != 0) {
                result = generateOrList(propFilters, myprefix, result);
            }

            if (! (useTimeRange || compSize > 0 || propSize > 0)) {
                //If a timerange is not specified and there are
                //no additional propfilters or compfilters then
                //check to see if the calendar resource contains this component
                result = "@" + myprefix;
            }

            return result;
        }
    }

    /**
     */
    private class propfilter extends filter {

        protected String name;
        protected boolean useTimeRange;
        protected Period timeRange;
        protected boolean useTextMatch;
        protected String textMatch;
        protected boolean isCaseless;

        protected List paramFilters;

        public propfilter() {
            useTimeRange = false;
            useTextMatch = false;
        }

        /** */
        public void createFromXml(Element element)
            throws ParseException {

            // Name must be present
            name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);
            if (name == null) {
                throw new ParseException("CALDAV:prop-filter a calendar property name (e.g., \"ATTENDEE\") is required", -1);
            }

            // Look at each child component
            boolean got_one = false;

            ElementIterator i = DomUtil.getChildren(element);
            while (i.hasNext()) {
                Element child = i.nextElement();

                if (ELEMENT_CALDAV_TIME_RANGE.
                    equals(child.getLocalName())) {

                    // Can only have one time-range or text-match
                    if (got_one)
                        throw new ParseException("CALDAV:prop-filter only one time-range or text-match element permitted", -1);

                    got_one = true;

                    // Get value
                    useTimeRange = true;

                    // Get start (must be present)
                    String start =
                        DomUtil.getAttribute(child, ATTR_CALDAV_START, null);
                    if (start == null) {
                        throw new ParseException("CALDAV:prop-filter time-range requires a start time", -1);
                    }

                    DateTime trstart = new DateTime(start);
                    if (! trstart.isUtc()) {
                        throw new ParseException("CALDAV:param-filter timerange start must be UTC", -1);
                    }

                    // Get end (must be present)
                    String end =
                        DomUtil.getAttribute(child, ATTR_CALDAV_END, null);
                    if (end == null) {
                        throw new ParseException("CALDAV:prop-filter time-range requires an end time", -1);
                    }

                    DateTime trend = new DateTime(end);
                    if (! trend.isUtc()) {
                        throw new ParseException("CALDAV:param-filter timerange end must be UTC", -1);
                    }

                    timeRange = new Period(trstart, trend);
                } else if (ELEMENT_CALDAV_TEXT_MATCH.
                           equals(child.getLocalName())) {

                    // Can only have one time-range or text-match
                    if (got_one) {
                        throw new ParseException("CALDAV:prop-filter only one time-range or text-match element permitted", -1);
                    }

                    got_one = true;

                    // Get value
                    useTextMatch = true;

                    // Element data is string to match
                    textMatch = DomUtil.getTextTrim(child).replaceAll("'", "''");

                    // Check attribute for caseless
                    String caseless =
                        DomUtil.getAttribute(child, ATTR_CALDAV_CASELESS,
                                             null);
                    if ((caseless == null) || ! VALUE_YES.equals(caseless))
                        isCaseless = false;
                    else
                        isCaseless = true;

                } else if (ELEMENT_CALDAV_PARAM_FILTER.
                           equals(child.getLocalName())) {

                    // Create new list if needed
                    if (paramFilters == null)
                        paramFilters = new Vector();

                    // Create new param filter
                    paramfilter pfilter = new paramfilter();

                    pfilter.createFromXml(child);

                    // Add to list
                    paramFilters.add(pfilter);
                }
            }
        }

        /** */
        public String generateTests(String prefix) {

            String result = new String();

            String myprefix = prefix + "_" + name.toLowerCase();

            int paramSize = (paramFilters != null) ? paramFilters.size() : 0;

            if (useTimeRange) {
                String key = myprefix + TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
                String value = generatePeriods(timeRange);
                result = "jcr:timerange(@" + key + ", '" + value + "')";
            } else if (useTextMatch) {
                result = "jcr:like(@" + myprefix + ", '%" + textMatch
                        + "%')";
            }

            if (paramSize != 0) {
                result = generateOrList(paramFilters, myprefix, result);
            }

            if (! (useTimeRange || useTextMatch || paramSize > 0)) {
                //If a timerange or textmatch is not specified and there are
                //no additional parameters to the filter query then
                //check to see if the calendar resource contains this property
                result = "@" + myprefix;
            }

            return result;
        }
    }

    /** */
    private class paramfilter extends filter {

        protected String name;
        protected boolean useTextMatch;
        protected String textMatch;
        protected boolean isCaseless;

        /** */
        public paramfilter() {
            useTextMatch = false;
        }

        /** */
        public void createFromXml(Element element)
            throws ParseException {
            // Get name which must be present
            name = DomUtil.getAttribute(element, ATTR_CALDAV_NAME, null);

            if (name == null) {
                throw new ParseException("CALDAV:param-filter a property parameter name (e.g., \"PARTSTAT\") is required", -1);
            }

            // Can only have a single ext-match element
            ElementIterator i = DomUtil.getChildren(element);
            if (! i.hasNext()) {
                throw new ParseException("CALDAV:param-filter only a single text-match element is allowed", -1);
            }

            Element child = i.nextElement();

            if (i.hasNext()) {
                throw new ParseException("CALDAV:param-filter only a single text-match element is allowed", -1);
            }

            if (ELEMENT_CALDAV_TEXT_MATCH.equals(child.getLocalName())) {

                useTextMatch = true;

                // Element data is string to match
                textMatch = DomUtil.getTextTrim(child).replaceAll("'", "''");

                // Check attribute for caseless
                String caseless =
                    DomUtil.getAttribute(child, ATTR_CALDAV_CASELESS, null);

                if ((caseless == null) || ! VALUE_YES.equals(caseless))
                    isCaseless = false;
                else
                    isCaseless = true;
            } else
                throw new ParseException("CALDAV:prop-filter an invalid element name found", -1);
        }

        /** */
        public String generateTests(String prefix) {

            String result = new String();

            String myprefix = prefix + "_" + name.toLowerCase();

            if (useTextMatch) {
                // TODO Figure out how to do caseless matching
                result = "jcr:like(@" + myprefix + ", '%" + textMatch
                        + "%')";
            }
            return result;
        }
    }
}
