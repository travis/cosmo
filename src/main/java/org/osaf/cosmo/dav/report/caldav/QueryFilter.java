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
import org.jdom.Element;
import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.jackrabbit.query.TextCalendarTextFilter;

/**
 * @author cyrusdaboo
 * 
 * This class represents an object model for the calendar-query report's
 * <filter> element. It parses the element out and then provides methods to
 * generate XPATH queries as needed for JCR.
 * 
 * TODO Right now this class simply returns null/false on parse errors. Ideally
 * it should throw exceptions with more information about exactly what went
 * wrong to aid debugging clients.
 * 
 */

public class QueryFilter implements JcrConstants {
    private static final Logger log =
        Logger.getLogger(QueryFilter.class);
    
    /**
     * The parsed top-level filter object.
     */
    protected compfilter filter;
    protected VTimeZone timezone;

    public QueryFilter() {
    }

    /**
     * @param tz
     *            The tz to set.
     */
    public void setTimezone(VTimeZone timezone) {
        this.timezone = timezone;
    }

    /**
     * Parse the <filter> XML element.
     * 
     * @param element
     */
    public boolean parseElement(Element element) {

        // Can only have a single comp-filter element
        List childrenList = element.getChildren(
                CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER,
                CosmoDavConstants.NAMESPACE_CALDAV);
        if (childrenList.size() != 1)
            return false;

        Element child = (Element) childrenList.get(0);

        // Create new component filter and have it parse the element
        filter = new compfilter();
        return filter.parseElement(child);
    }

    /**
     * Convert filter into XPath string for use with JCR queries.
     * 
     * @return the XPath query as a string.
     */
    public String toXPath() {

        // Look at elements that are event resources and look in the content
        // node
        // of those resources for indexed data
        String path = "/element(*, " + NT_CALENDAR_RESOURCE + ")/"
                + NN_JCR_CONTENT + "[";

        // Generate a list of terms to use in the XPath expression
        path += filter.generateTests("");
        path += "]";

        return path;
    }

    /**
     * Generate fixed and floating time periods as a string.
     * 
     * @param period
     *            UTC period to convert
     * @return string representing both sets of periods
     */
    protected String generatePeriods(Period period) {

        // Get fixed start/end time

        DateTime dstart = period.getStart();
        DateTime dend = period.getEnd();

        // Get float start/end
        DateTime fstart = (DateTime) Dates.getInstance(dstart, dstart);
        DateTime fend = (DateTime) Dates.getInstance(dend, dend);
        
        if (timezone != null){
            fstart.setTimeZone(new TimeZone(timezone));
            fend.setTimeZone(new TimeZone(timezone));
        } else {
            fstart.setUtc(true);
            fend.setUtc(true);
        }

        return dstart.toString() + '/' + dend.toString() + ','
                + fstart.toString() + '/' + fend.toString();
    }

    private abstract class filter {

        abstract protected String generateTests(String prefix);

        protected String generateOrList(List items, String prefix, String result) {

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
     * @author cyrusdaboo
     * 
     * Object that models the <comp-filter> element.
     * 
     */
    private class compfilter extends filter {

        protected String name;
        protected boolean useTimeRange;
        protected Period timeRange;

        /**
         * List of embedded <comp-filter> elements. NULL if none are present.
         */
        protected List compFilters;

        /**
         * List of embedded <prop-filter> elements. NULL if none are present.
         */
        protected List propFilters;

        public compfilter() {
            useTimeRange = false;
        }

        /**
         * Parse the <comp-filter> XML element.
         * 
         * @param element
         *            <comp-filter> element to parse
         * @return true on successful parse, false otherwise
         */
        public boolean parseElement(Element element) {

            // Name must be present
            name = element
                    .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
            if (name == null)
                return false;

            // Look at each child component
            boolean got_one = false;
            for (Iterator iter = element.getChildren().iterator(); iter
                    .hasNext();) {
                Element child = (Element) iter.next();

                // TODO is-defined has been removed in the -09 draft. We will
                // continue to support it for older clients for now, but
                // eventually this should be removed.
                if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                        .getName())) {

                    // Can only have one of is-defined, time-range
                    if (got_one)
                        return false;
                    got_one = true;

                } else if (CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE
                        .equals(child.getName())) {

                    // Can only have one of is-defined, time-range
                    if (got_one)
                        return false;
                    got_one = true;

                    // Mark the value
                    useTimeRange = true;

                    try {
                        // Get start (must be present)
                        String start = child
                                .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_START);
                        if (start == null)
                            return false;
                        DateTime trstart;
                        trstart = new DateTime(start);

                        // Get end (must be present)
                        String end = child
                                .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_END);
                        if (end == null)
                            return false;
                        DateTime trend = new DateTime(end);

                        timeRange = new Period(trstart, trend);

                    } catch (ParseException e) {

                        return false;
                    }

                } else if (CosmoDavConstants.ELEMENT_CALDAV_COMP_FILTER
                        .equals(child.getName())) {

                    // Create new list if needed
                    if (compFilters == null)
                        compFilters = new Vector();

                    // Create new component filter
                    compfilter cfilter = new compfilter();

                    // Parse it and make sure it is valid
                    if (!cfilter.parseElement(child))
                        return false;

                    // Add to list
                    compFilters.add(cfilter);

                } else if (CosmoDavConstants.ELEMENT_CALDAV_PROP_FILTER
                        .equals(child.getName())) {

                    // Create new list if needed
                    if (propFilters == null)
                        propFilters = new Vector();

                    // Create new prop filter
                    propfilter pfilter = new propfilter();

                    // Parse it and make sure it is valid
                    if (!pfilter.parseElement(child))
                        return false;

                    // Add to list
                    propFilters.add(pfilter);
                }
            }

            return true;
        }

        /**
         * Generate an XPath element for testing components.
         * 
         * @param prefix
         *            the prefix string used for the flat iCal namespace
         * @return the XPath element
         */
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
                String key = myprefix
                        + TextCalendarTextFilter.TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
                String value = generatePeriods(timeRange);
                result = "jcr:timerange(@" + key + ", '" + value + "')";
            } else { // is-defined
                if ((compSize == 0) && (propSize == 0)) {
                    // If there are no component or property filters then we
                    // must explicitly test for this component
                    result = "jcr:contains(@" + myprefix + ", 'begin')";
                }
            }

            // For each sub-component and property test, generate more tests
            if (compSize != 0) {
                result = generateOrList(compFilters, myprefix, result);
            }

            if (propSize != 0) {
                result = generateOrList(propFilters, myprefix, result);
            }

            return result;
        }
    }

    /**
     * @author cyrusdaboo
     * 
     * Object that models the <prop-filter> element.
     * 
     */
    private class propfilter extends filter {

        protected String name;
        protected boolean useTimeRange;
        protected Period timeRange;
        protected boolean useTextMatch;
        protected String textMatch;
        protected boolean isCaseless;

        /**
         * List of embedded <param-filter> elements. NULL if none are present.
         */
        protected List paramFilters;

        public propfilter() {
            useTimeRange = false;
            useTextMatch = false;
        }

        /**
         * Parse the <param-filter> XML element.
         * 
         * @param element
         */
        public boolean parseElement(Element element) {

            // Name must be present
            name = element
                    .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
            if (name == null)
                return false;

            // Look at each child component
            boolean got_one = false;
            for (Iterator iter = element.getChildren().iterator(); iter
                    .hasNext();) {
                Element child = (Element) iter.next();

                // TODO is-defined has been removed in the -09 draft. We will
                // continue to support it for older clients for now, but
                // eventually this should be removed.
                if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                        .getName())) {

                    // Can only have one of is-defined, time-range, text-match
                    if (got_one)
                        return false;
                    got_one = true;

                } else if (CosmoDavConstants.ELEMENT_CALDAV_TIME_RANGE
                        .equals(child.getName())) {

                    // Can only have one of is-defined, time-range, text-match
                    if (got_one)
                        return false;
                    got_one = true;

                    // Get value
                    useTimeRange = true;

                    try {
                        // Get start (must be present)
                        String start = child
                                .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_START);
                        if (start == null)
                            return false;
                        DateTime trstart;
                        trstart = new DateTime(start);

                        // Get end (must be present)
                        String end = child
                                .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_END);
                        if (end == null)
                            return false;
                        DateTime trend = new DateTime(end);

                        timeRange = new Period(trstart, trend);

                    } catch (ParseException e) {

                        return false;
                    }

                } else if (CosmoDavConstants.ELEMENT_CALDAV_TEXT_MATCH
                        .equals(child.getName())) {

                    // Can only have one of is-defined, time-range, text-match
                    if (got_one)
                        return false;
                    got_one = true;

                    // Get value
                    useTextMatch = true;

                    // Element data is string to match
                    textMatch = child.getValue();

                    // Check attribute for caseless
                    String caseless = child
                            .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_CASELESS);
                    if ((caseless == null)
                            || !CosmoDavConstants.VALUE_YES.equals(caseless))
                        isCaseless = false;
                    else
                        isCaseless = true;

                } else if (CosmoDavConstants.ELEMENT_CALDAV_PARAM_FILTER
                        .equals(child.getName())) {

                    // Create new list if needed
                    if (paramFilters == null)
                        paramFilters = new Vector();

                    // Create new param filter
                    paramfilter pfilter = new paramfilter();

                    // Parse it and make sure it is valid
                    if (!pfilter.parseElement(child))
                        return false;

                    // Add to list
                    paramFilters.add(pfilter);
                }
            }

            return true;
        }

        /**
         * Generate an XPath element for testing components.
         * 
         * @param prefix
         *            the prefix string used for the flat iCal namespace
         * @return the XPath element
         */
        public String generateTests(String prefix) {

            String result = new String();

            String myprefix = prefix + "_" + name.toLowerCase();

            int paramSize = (paramFilters != null) ? paramFilters.size() : 0;

            if (useTimeRange) {
                String key = myprefix
                        + TextCalendarTextFilter.TIME_RANGE_FIELD_SUFFIX_LOWERCASE;
                String value = generatePeriods(timeRange);
                result = "jcr:timerange(@" + key + ", '" + value + "')";
            } else if (useTextMatch) {
                result = "jcr:contains(@" + myprefix + ", '*" + textMatch
                        + "*')";
            } else { // is-defined
                if (paramSize == 0) {
                    // If there are no parameter filters then we must
                    // explicitly test for this component
                    result = "@" + myprefix;
                }
            }

            if (paramSize != 0) {
                result = generateOrList(paramFilters, myprefix, result);
            }

            return result;
        }
    }

    /**
     * @author cyrusdaboo
     * 
     * Object that models the <param-filter> element.
     * 
     */
    private class paramfilter extends filter {

        protected String name;
        protected boolean useTextMatch;
        protected String textMatch;
        protected boolean isCaseless;

        /**
         * 
         */
        public paramfilter() {

            useTextMatch = false;
        }

        /**
         * Parse the <param-filter> XML element.
         * 
         * @param element
         */
        public boolean parseElement(Element element) {

            // Get name which must be present
            name = element
                    .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
            if (name == null)
                return false;

            // Can only have a single is-defined or text-match element
            List childrenList = element.getChildren();
            if (childrenList.size() != 1)
                return false;

            Element child = (Element) childrenList.get(0);

            // TODO is-defined has been removed in the -09 draft. We will
            // continue to support it for older clients for now, but
            // eventually this should be removed.
            if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                    .getName())) {

                return true;

            } else if (CosmoDavConstants.ELEMENT_CALDAV_TEXT_MATCH.equals(child
                    .getName())) {

                useTextMatch = true;

                // Element data is string to match
                textMatch = child.getValue();

                // Check attribute for caseless
                String caseless = child
                        .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_CASELESS);
                if ((caseless == null)
                        || !CosmoDavConstants.VALUE_YES.equals(caseless))
                    isCaseless = false;
                else
                    isCaseless = true;

                return true;
            } else
                return false;
        }

        /**
         * Generate an XPath element for testing parameters.
         * 
         * @param prefix
         *            the prefix string used for the flat iCal namespace
         * @return the XPath element
         */
        public String generateTests(String prefix) {

            String result = new String();

            String myprefix = prefix + "_" + name.toLowerCase();

            if (useTextMatch) {
                // TODO Figure out how to do caseless matching
                result = "jcr:contains(@" + myprefix + ", '*" + textMatch
                        + "*')";
            } else { // is-defined
                result = "@" + myprefix;
            }

            return result;
        }
    }
}
