/**
 * 
 */
package org.osaf.cosmo.dav.report.caldav;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.jdom.Element;
import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;

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
 * TODO Right now Jackrabbit only supports the attribute axis in XPath
 * predicates. This is a paion because with the way the JCR layout is done ion
 * Cosmo for iCalendar, we reallyh need to be able to test for the existense or
 * content of specific nodes, whilst returning the root .ics node.
 * 
 * i.e. I would like to be able to do:
 * 
 * xpath = "jcr:root/calendar/element(*,
 * NT_CALDAV_EVENT_RESOURCE)/[NN_ICAL_REVENT]/[jcr:contains("SUMMARY", "Test")]
 * 
 * Right now we have to do:
 * 
 * xpath = "jcr:root/calendar/element(*,
 * NT_CALDAV_EVENT_RESOURCE)/NN_ICAL_REVENT/[jcr:contains("SUMMARY", "Test")]
 * 
 * This returns the node inside of the one we want so we have to manipulate the
 * query result to step back a few path levels to get to the resource path we
 * actually want.
 */
public class QueryFilter {

    private static HashMap prop2jcr = new HashMap();
    private static HashMap param2jcr = new HashMap();

    /**
     * The parsed top-level filter object.
     */
    protected compfilter filter;

    public QueryFilter() {
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
        return "/" + filter.toXPath();
    }

    private String mapPropToJCRNodeName(String name) {

        // Make sure map is initialised
        if (prop2jcr.size() == 0)
            prop2jcrCreate();

        return (String) prop2jcr.get(name.toUpperCase());
    }

    private void prop2jcrCreate() {

        prop2jcr.put("PRODID", JcrConstants.NN_ICAL_PRODID);
        prop2jcr.put("VERSION", JcrConstants.NN_ICAL_VERSION);
        prop2jcr.put("CALSCALE", JcrConstants.NN_ICAL_CALSCALE);
        prop2jcr.put("METHOD", JcrConstants.NN_ICAL_METHOD);
        prop2jcr.put("ACTION", JcrConstants.NN_ICAL_ACTION);
        prop2jcr.put("ATTACH", JcrConstants.NN_ICAL_ATTACH);
        prop2jcr.put("ATTENDEE", JcrConstants.NN_ICAL_ATTENDEE);
        prop2jcr.put("CATEGORIES", JcrConstants.NN_ICAL_CATEGORIES);
        prop2jcr.put("CLASS", JcrConstants.NN_ICAL_CLASS);
        prop2jcr.put("COMMENT", JcrConstants.NN_ICAL_COMMENT);
        prop2jcr.put("COMPLETED", JcrConstants.NN_ICAL_COMPLETED);
        prop2jcr.put("CONTACT", JcrConstants.NN_ICAL_CONTACT);
        prop2jcr.put("CREATED", JcrConstants.NN_ICAL_CREATED);
        prop2jcr.put("DESCRIPTION", JcrConstants.NN_ICAL_DESCRIPTION);
        prop2jcr.put("DTEND", JcrConstants.NN_ICAL_DTEND);
        prop2jcr.put("DTSTAMP", JcrConstants.NN_ICAL_DTSTAMP);
        prop2jcr.put("DTSTART", JcrConstants.NN_ICAL_DTSTART);
        prop2jcr.put("DUE", JcrConstants.NN_ICAL_DUE);
        prop2jcr.put("DUR", JcrConstants.NN_ICAL_DUR);
        prop2jcr.put("DURATION", JcrConstants.NN_ICAL_DURATION);
        prop2jcr.put("EXDATE", JcrConstants.NN_ICAL_EXDATE);
        prop2jcr.put("EXRULE", JcrConstants.NN_ICAL_EXRULE);
        prop2jcr.put("FREEBUSY", JcrConstants.NN_ICAL_FREEBUSY);
        prop2jcr.put("GEO", JcrConstants.NN_ICAL_GEO);
        prop2jcr.put("LAST-MODIFIED", JcrConstants.NN_ICAL_LASTMODIFIED);
        prop2jcr.put("LOCATION", JcrConstants.NN_ICAL_LOCATION);
        prop2jcr.put("ORGANIZER", JcrConstants.NN_ICAL_ORGANIZER);
        prop2jcr.put("PERCENT-COMPLETE", JcrConstants.NN_ICAL_PERCENT_COMPLETE);
        prop2jcr.put("PERIOD", JcrConstants.NN_ICAL_PERIOD);
        prop2jcr.put("PRIORITY", JcrConstants.NN_ICAL_PRIORITY);
        prop2jcr.put("RANGE", JcrConstants.NN_ICAL_RANGE);
        prop2jcr.put("RDATE", JcrConstants.NN_ICAL_RDATE);
        prop2jcr.put("RECUR", JcrConstants.NN_ICAL_RECUR);
        prop2jcr.put("RECURRENCE-ID", JcrConstants.NN_ICAL_RECURRENCEID);
        prop2jcr.put("RELATED-TO", JcrConstants.NN_ICAL_RELATEDTO);
        prop2jcr.put("REPEAT", JcrConstants.NN_ICAL_REPEAT);
        prop2jcr.put("REQUEST-STATUS", JcrConstants.NN_ICAL_REQUESTSTATUS);
        prop2jcr.put("RESOURCES", JcrConstants.NN_ICAL_RESOURCES);
        prop2jcr.put("RRULE", JcrConstants.NN_ICAL_RRULE);
        prop2jcr.put("SEQUENCE", JcrConstants.NN_ICAL_SEQ);
        prop2jcr.put("STATUS", JcrConstants.NN_ICAL_STATUS);
        prop2jcr.put("SUMMARY", JcrConstants.NN_ICAL_SUMMARY);
        prop2jcr.put("TRANSP", JcrConstants.NN_ICAL_TRANSP);
        prop2jcr.put("TRIGGER", JcrConstants.NN_ICAL_TRIGGER);
        prop2jcr.put("TZID", JcrConstants.NN_ICAL_TZID);
        prop2jcr.put("TZNAME", JcrConstants.NN_ICAL_TZNAME);
        prop2jcr.put("TZOFFSETFROM", JcrConstants.NN_ICAL_TZOFFSETFROM);
        prop2jcr.put("TZOFFSETTO", JcrConstants.NN_ICAL_TZOFFSETTO);
        prop2jcr.put("TZURL", JcrConstants.NN_ICAL_TZURL);
        prop2jcr.put("UID", JcrConstants.NN_ICAL_UID);
        prop2jcr.put("URL", JcrConstants.NN_ICAL_URL);

    }

    private String mapParamToJCRAttribute(String name) {

        // Make sure map is initialised
        if (param2jcr.size() == 0)
            param2jcrCreate();

        return (String) param2jcr.get(name.toUpperCase());
    }

    private void param2jcrCreate() {

        param2jcr.put("ALTREP", JcrConstants.NP_ICAL_ALTREP);
        param2jcr.put("CN", JcrConstants.NP_ICAL_CN);
        param2jcr.put("CUTYPE", JcrConstants.NP_ICAL_CUTYPE);
        param2jcr.put("DELEGATED-FROM", JcrConstants.NP_ICAL_DELFROM);
        param2jcr.put("DELEGATED-TO", JcrConstants.NP_ICAL_DELTO);
        param2jcr.put("DIR", JcrConstants.NP_ICAL_DIR);
        param2jcr.put("ENCODING", JcrConstants.NP_ICAL_ENCODING);
        param2jcr.put("FBTYPE", JcrConstants.NP_ICAL_FBTYPE);
        param2jcr.put("FMTTYPE", JcrConstants.NP_ICAL_FMTTYPE);
        param2jcr.put("LANGUAGE", JcrConstants.NP_ICAL_LANGUAGE);
        param2jcr.put("MEMBER", JcrConstants.NP_ICAL_MEMBER);
        param2jcr.put("PARTSTAT", JcrConstants.NP_ICAL_PARTSTAT);
        param2jcr.put("RANGE", JcrConstants.NP_ICAL_RANGE);
        param2jcr.put("RELATED", JcrConstants.NP_ICAL_RELATED);
        param2jcr.put("RELTYPE", JcrConstants.NP_ICAL_RELTYPE);
        param2jcr.put("ROLE", JcrConstants.NP_ICAL_ROLE);
        param2jcr.put("RSVP", JcrConstants.NP_ICAL_RSVP);
        param2jcr.put("SENT-BY", JcrConstants.NP_ICAL_SENTBY);
        param2jcr.put("TZID", JcrConstants.NP_ICAL_TZID);
        param2jcr.put("VALUE", JcrConstants.NP_ICAL_VALUE);
    }

    /**
     * @author cyrusdaboo
     * 
     * Object that models the <comp-filter> element.
     * 
     */
    private class compfilter {

        protected String name;
        protected boolean isDefined;
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
            isDefined = false;
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
                if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                        .getName())) {

                    // Can only have one of is-defined, time-range
                    if (got_one)
                        return false;
                    got_one = true;

                    // Mark the value
                    isDefined = true;

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

            // Must have one of is-defined or time-range
            if (!got_one) {
                return false;
            }

            return true;
        }

        /**
         * Convert property filter into XPath query element for use with JCR
         * queries.
         * 
         * @return the XPath query element as a string.
         */
        public String toXPath() {

            // iCalendar properties are represented in JCR as nodes so
            // we need an XPath that tests the node in the
            // appropriate fashion for the type of comparison being done

            // TODO Not sure about X-props right now

            String statement = null;

            // Map the iCalendar component name to a JCR node name or type. If
            // no
            // mapping is found then return empty string for XPath
            // element.
            String prop = null;
            if (name.equals("VCALENDAR"))
                prop = "element(*, " + JcrConstants.NT_CALDAV_EVENT_RESOURCE
                        + ")";
            else if (Component.VEVENT.equals(name))
                prop = JcrConstants.NN_ICAL_REVENT;
            else if (Component.VTIMEZONE.equals(name))
                prop = JcrConstants.NN_ICAL_TIMEZONE;
            if (prop == null)
                return "";

            if (isDefined) {
                statement = prop;
            } else if (useTimeRange) {

                // TODO - Need to figure out how to do time range tests in
                // JCR. For now just ignore.
                statement = "";

            }

            // Add any component or property filters
            int compSize = (compFilters != null) ? compFilters.size() : 0;
            int propSize = (propFilters != null) ? propFilters.size() : 0;
            if (compSize + propSize > 0) {

                // Shortcut simple cases
                if ((compSize == 1) && (propSize == 0)) {
                    statement += "/"
                            + ((compfilter) compFilters.get(0)).toXPath();
                } else if ((compSize == 0) && (propSize == 1)) {
                    statement += "/"
                            + ((propfilter) propFilters.get(0)).toXPath();
                } else {

                    // Add each test as a compound statement
                    statement += "/(";
                    boolean first = true;
                    if (compSize != 0) {
                        for (Iterator iter = compFilters.iterator(); iter
                                .hasNext();) {
                            // Multiple items must all occur together (and)
                            if (!first)
                                statement += " and ";
                            else
                                first = false;

                            statement += ((compfilter) iter.next()).toXPath();
                        }
                    }

                    if (propSize != 0) {
                        for (Iterator iter = propFilters.iterator(); iter
                                .hasNext();) {
                            // Multiple items must all occur together (and)
                            if (!first)
                                statement += " and ";
                            else
                                first = false;

                            statement += ((propfilter) iter.next()).toXPath();
                        }
                    }

                    statement += ")";
                }
            }

            return statement;
        }
    }

    /**
     * @author cyrusdaboo
     * 
     * Object that models the <prop-filter> element.
     * 
     */
    private class propfilter {

        protected String name;
        protected boolean isDefined;
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
            isDefined = false;
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
                if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                        .getName())) {

                    // Can only have one of is-defined, time-range, text-match
                    if (got_one)
                        return false;
                    got_one = true;

                    // Mark the value
                    isDefined = true;

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

            // Must have one of is-defined, time-range or text-match
            if (!got_one) {
                return false;
            }

            return true;
        }

        /**
         * Convert property filter into XPath query element for use with JCR
         * queries.
         * 
         * @return the XPath query element as a string.
         */
        public String toXPath() {

            // iCalendar properties are represented in JCR as nodes so
            // we need an XPath that tests the node in the
            // appropriate fashion for the type of comparison being done

            // TODO Not sure about X-props right now

            String statement = null;

            // Map the iCalendar parameter name to a JCR attribute name. If no
            // mapping is found (X-param ??) then return empty string for XPath
            // element.
            String prop = mapPropToJCRNodeName(name);
            if (prop == null)
                return "";

            // This will contain a list of node parameter checks that all need
            // to be done
            Vector paramChecks = new Vector();

            if (isDefined) {
                statement = prop;
            } else if (useTimeRange) {

                // TODO - Need to figure out how to do time range tests in
                // JCR. For now just ignore.
                statement = "";

            } else if (useTextMatch) {

                // TODO Figure out how to do case-sensitive/insensitive text
                // comparisons in JCR queries. For now just ignore caseless
                // value.

                statement = prop;

                // This is the XPath segment for a text match on a iCal property
                // value which is actually a JCR node parameter.
                // NB the jcr:contains function matches tokens, so we need to
                // surround
                // our text with *'s to get substring style test.
                paramChecks.add("jcr:contains(@"
                        + JcrConstants.NP_ICAL_PROPVALUE + ", '*" + textMatch
                        + "*')");
            }

            // Add any parameter filters
            if (paramFilters != null) {
                for (Iterator iter = paramFilters.iterator(); iter.hasNext();) {
                    paramChecks.add(((paramfilter) iter.next()).toXPath());
                }
            }

            // Now form the actual parameter check XPath segment if needed
            if (paramChecks.size() != 0) {
                statement += "[";
                boolean first = true;
                for (Iterator iter = paramChecks.iterator(); iter.hasNext();) {

                    // Multiple items must all occur together (and)
                    if (!first)
                        statement += " and ";
                    else
                        first = false;

                    statement += (String) iter.next();
                }
                statement += "]";
            }

            return statement;
        }
    }

    /**
     * @author cyrusdaboo
     * 
     * Object that models the <param-filter> element.
     * 
     */
    private class paramfilter {

        protected String name;
        protected boolean isDefined;
        protected boolean useTextMatch;
        protected String textMatch;
        protected boolean isCaseless;

        /**
         * 
         */
        public paramfilter() {

            isDefined = false;
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
            if (CosmoDavConstants.ELEMENT_CALDAV_IS_DEFINED.equals(child
                    .getName())) {

                isDefined = true;
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
         * Convert parameter filter into XPath query element for use with JCR
         * queries.
         * 
         * @return the XPath query element as a string.
         */
        public String toXPath() {

            // iCalendar parameters are represented in JCR as node attributes so
            // we need an XPath predicate that tests the node attribute in the
            // appropriate fashion for the type of comparison being done

            // TODO Not sure about X-params right now

            String statement = null;

            // Map the iCalendar parameter name to a JCR attribute name. If no
            // mapping is found (X-param ??) then return empty string for XPath
            // element.
            String attr = mapParamToJCRAttribute(name);
            if (attr == null)
                return "";

            if (isDefined) {
                statement = "@" + attr;
            } else if (useTextMatch) {

                // TODO Figure out how to do case-sensitive/insensitive text
                // comparisons in JCR queries. For now just ignore caseless
                // value.

                // NB the jcr:contains function matches tokens, so we need to
                // surround
                // our text with *'s to get subsstring style test.
                statement = "jcr:contains(@" + attr + ", '*" + textMatch
                        + "*')";
            }

            return statement;
        }
    }
}
