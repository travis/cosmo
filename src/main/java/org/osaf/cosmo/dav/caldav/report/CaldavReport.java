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
package org.osaf.cosmo.dav.caldav.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.filter.OutputFilter;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.jcr.JcrDavSession;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.calendar.query.CalendarFilter;
import org.osaf.cosmo.dav.ExtendedDavResource;
import org.osaf.cosmo.dav.impl.DavResourceBase;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CalendarEventItem;

import org.w3c.dom.Element;

/**
 * Base class for CalDAV reports.
 *
 * Based on code originally written by Cyrus Daboo.
 */
public abstract class CaldavReport
    implements Report, DavConstants, CaldavConstants {
    private static final Log log = LogFactory.getLog(CaldavReport.class);

    /** */
    public static final ReportType CALDAV_QUERY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_QUERY,
                            NAMESPACE_CALDAV, QueryReport.class);
    /** */
    public static final ReportType CALDAV_MULTIGET =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_MULTIGET,
                            NAMESPACE_CALDAV, MultigetReport.class);
    /** */
    public static final ReportType CALDAV_FREEBUSY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                            NAMESPACE_CALDAV, FreeBusyReport.class);

    private ExtendedDavResource resource;
    private ReportInfo info;
    private CalendarFilter queryFilter;
    private OutputFilter outputFilter;
    private List hrefs = new ArrayList();

    // Report methods

    /** */
    public void init(DavResource resource,
                     ReportInfo info)
        throws DavException {
        this.resource = (ExtendedDavResource) resource;
        this.info = info;
        parseReport(info);
    }

    /** */
    public void run(DavServletResponse response)
        throws IOException, DavException {
        runQuery();
        buildResponse();
        output(response);
    }

    /** */
    public ExtendedDavResource getResource() {
        return resource;
    }

    /** */
    public ReportInfo getInfo() {
        return info;
    }

    /** */
    public List getHrefs() {
        return hrefs;
    }

    /** */
    public void setHrefs(List hrefs) {
        this.hrefs = hrefs;
    }

    /** */
    public void addHrefs(List hrefs) {
        this.hrefs.addAll(hrefs);
    }

    // our methods

    /**
     * Parse information from the given report info needed to execute
     * the report. For example, a report request might include a query
     * filter that constrains the set of resources to be returned from
     * the report.
     */
    protected abstract void parseReport(ReportInfo info)
        throws DavException;

    /**
     * Executes the report query and sets the member resource hrefs
     * used to calculate the report response.
     *
     * If the report is specified with depth 0, no queries are run,
     * since collections themselves do not have associated calendar
     * data.
     *
     * If the report is specified with depth 1, only the calendar
     * resources within the collection are considered.
     *
     * If the report is specified with depth infinity, all descendent
     * calendar collections are considered as well as the requested
     * collection.
     *
     * @throws DavException if an error occurs while running the
     * report query
     */
    protected void runQuery()
        throws DavException {
        if (info.getDepth() == DEPTH_0) {
            return;
        }

        // run the query on this resource itself
        doQuery(resource);

        if (info.getDepth() == DEPTH_INFINITY) {
            doQueryForChildren(resource);
        }
    }

    /**
     * Process the list of calendar resource hrefs found by the report
     * query, creating whatever objects are needed to generate the
     * response output (filtering with the <code>OutputFilter</code>
     * if one is provided with the report info).
     */
    protected abstract void buildResponse()
        throws DavException;

    /**
     * Write output to the response.
     */
    protected abstract void output(DavServletResponse response)
        throws IOException;

    /** */
    protected OutputFilter getOutputFilter() {
        return outputFilter;
    }

    /**
     * Set a <code>OutputFilter</code> used to narrow the calendar
     * objects returned in the response, if one is provided by a
     * specific report.
     */
    protected void setOutputFilter(OutputFilter filter) {
        this.outputFilter = filter;
    }

    /** */
    protected CalendarFilter getQueryFilter() {
        return queryFilter;
    }

    /**
     * Set a <code>CaldavQueryFilter</code> used to constrain the JCR query,
     * if one is provided by a specific report.
     */
    protected void setQueryFilter(CalendarFilter filter) {
        this.queryFilter = filter;
    }

    /**
     * Return an <code>OutputFilter</code> representing the
     * <code>CALDAV:calendar-data</code> property in a report info, if
     * one is provided.
     */
    protected OutputFilter findOutputFilter(ReportInfo info)
        throws DavException {
        Element propdata = DomUtil.getChildElement(info.getReportElement(),
                                                   XML_PROP, NAMESPACE);
        if (propdata == null) {
            return null;
        }
        Element cdata = DomUtil.
            getChildElement(propdata, ELEMENT_CALDAV_CALENDAR_DATA,
                            NAMESPACE_CALDAV);
        if (cdata == null) {
            return null;
        }
        try {
            return CaldavOutputFilter.createFromXml(cdata);
        } catch (ParseException e) {
            log.error("error parsing CALDAV:calendar-data", e);
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "error parsing CALDAV:calendar-data: " + e.getMessage());
        }
    }

    /**
     * Read the calendar data from the given dav resource, filtering
     * it if an output filter has been set.
     */
    protected String readCalendarData(ExtendedDavResource resource)
        throws DavException {
        OutputContext ctx = new OutputContext() {
                // simple output context that ignores all setters,
                // simply used to collect the output content
                private ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

                public boolean hasStream() {
                    return true;
                }

                public OutputStream getOutputStream() {
                    return out;
                }

                public void setContentLanguage(String contentLanguage) {
                }

                public void setContentLength(long contentLength) {
                }

                public void setContentType(String contentType) {
                }

                public void setModificationTime(long modificationTime) {
                }

                public void setETag(String etag) {
                }

                public void setProperty(String propertyName,
                                        String propertyValue) {
                }
            };
        try {
            resource.spool(ctx);
        } catch (IOException e) {
            log.error("cannot read calendar data from resource " + resource.getResourcePath(), e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot read calendar data: " + e.getMessage());
        }

        String calendarData = ctx.getOutputStream().toString();

        if (outputFilter != null) {
            try {
                CalendarBuilder builder = new CalendarBuilder();
                Calendar calendar =
                    builder.build(new StringReader(calendarData));

                // filter the output
                StringWriter out = new StringWriter();
                CalendarOutputter outputter = new CalendarOutputter();
                outputter.output(calendar, out, outputFilter);
                calendarData = out.toString();
                out.close();

                // NB ical4j's outputter may generate \r\n line
                // ends but we need \n only
                calendarData = calendarData.replaceAll("\r", "");
            } catch (IOException e) {
                log.error("cannot read or filter calendar data for resource " + resource.getResourcePath(), e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot read or filter calendar data: " + e.getMessage());
            } catch (ParserException e) {
                log.error("cannot parse calendar data for resource " + resource.getResourcePath(), e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot parse calendar data: " + e.getMessage());
            } catch (ValidationException e) {
                log.error("invalid calendar data for resource " + resource.getResourcePath(), e);
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "invalid calendar data: " + e.getMessage());
            }
        }

        return calendarData;
    }

    // private methods

    private void doQuery(DavResource resource)
        throws DavException {
        try {
            // XXX
            //            DavCalendarCollection dcc = (DavCalendarCollection) resource;
            //            Collection events = dcc.findEvents(queryFilter);
            Collection events = new ArrayList();
            addHrefs(queryResultToHrefs(events));
        } catch (Exception e) {
            log.error("cannot run report query", e);
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, "cannot run report query: " + e.getMessage());
        }
    }

    private void doQueryForChildren(DavResource resource)
        throws DavException {
        // iterate through this this resource's child calendar
        // collections and run the query for each one
        for (DavResourceIterator i = resource.getMembers(); i.hasNext();) {
            ExtendedDavResource child = (ExtendedDavResource) i.nextResource();
            if (child.isCalendarCollection()) {
                doQuery(child);
                doQueryForChildren(child);
            }
        }
    }

    private List queryResultToHrefs(Collection events) {
        DavResourceLocator parentLocator = resource.getLocator();

        List hrefs = new ArrayList();
        for (Iterator i=events.iterator(); i.hasNext();) {
            CalendarEventItem event = (CalendarEventItem) i.next();
            String eventPath = parentLocator.getResourcePath() + "/" +
                event.getName();
            DavResourceLocator eventLocator = parentLocator.getFactory()
                .createResourceLocator(parentLocator.getPrefix(),
                                       parentLocator.getWorkspacePath(),
                                       eventPath, false);
            hrefs.add(eventLocator.getHref(true));
        }

        return hrefs;
    }
}
