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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.filter.OutputFilter;

import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.impl.CosmoDavResourceImpl;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author cyrusdaboo
 * 
 * <code>AbstractCalendarDataReport</code> is a base class for CalDAV reports
 * that use properties and calendar-data elements.
 * 
 */
public abstract class AbstractCalendarDataReport
    implements Report, DavConstants {
    private static final Log log =
        LogFactory.getLog(AbstractCalendarDataReport.class);

    protected DavResource resource;
    protected ReportInfo info;
    protected int propfindType = PROPFIND_ALL_PROP;
    protected DavPropertyNameSet propfindProps;
    protected List hrefs;
    protected Element calendarDataElement;
    protected OutputFilter outputFilter;

    /**
     * Set the target resource.
     * 
     * @param resource
     * @throws IllegalArgumentException
     *             if the specified resource is <code>null</code>
     * @see Report#setResource(org.apache.jackrabbit.webdav.version.DavResource)
     */
    public void setResource(DavResource resource)
        throws IllegalArgumentException {
        if (resource == null) {
            throw new IllegalArgumentException(
                    "The resource specified must not be null.");
        }
        this.resource = resource;
    }

    /**
     * Run the report
     */
    public Element toXml(Document document) {
        // Get parent's href as we use this a bit
        String parentHref = resource.getHref();

        // Get the host portion of the href as we need to check for relative
        // hrefs.
        String host = parentHref.substring(0, parentHref.indexOf("/", 8));

        // Look for and parse any filter items here
        if (calendarDataElement != null &&
            DomUtil.getChildren(calendarDataElement).hasNext()) {

            // Grab an ical4j calendar data output filter
            outputFilter = parseCalendarData(calendarDataElement);
        }

        // Iterator over each href (making sure it is a child of the root)
        // and return a response for each
        MultiStatus ms = new MultiStatus();
        Iterator it = hrefs.iterator();
        while (it.hasNext()) {

            // Note that the href sent by the client may be relative, so
            // we need to convert to absolute for subsequent comprisons
            String href = (String) it.next();
            if (!href.startsWith("http"))
                href = host + href;

            // Check it is a child or the same
            if (!Text.isDescendantOrEqual(parentHref, href)) {
                throw new IllegalArgumentException(
                        "CALDAV:"
                                + info.getReportElement().getNodeName()
                                + " href element is not a child or equal to request href.");
            }

            // Get resource for this href
            DavResource hrefResource = ((CosmoDavResourceImpl) resource)
                    .getChildHref(href, info.getSession());

            // Check it is a child or the same
            if (hrefResource == null) {
                throw new IllegalArgumentException("CALDAV:"
                        + info.getReportElement().getNodeName()
                        + " href element could not be resolved.");
            }

            buildMultiStatus(hrefResource, ms);
        }
        return ms.toXml(document);
    }

    /**
     * Fills the specified <code>MultiStatus</code> object by generating a
     * <code>MultiStatusResponse</code> for the given resource.
     * 
     * @param res
     * @param ms
     * @throws DavException
     * @see #getResponse(DavResource, List)
     */
    protected void buildMultiStatus(DavResource res, MultiStatus ms) {
        // Generate response
        CalDAVMultiStatusResponse response = new CalDAVMultiStatusResponse(res,
                propfindProps, propfindType);

        if (res.exists()) {
            if (calendarDataElement != null) {
                // Generate calendar data string from resource
                String calendarData = readCalendarData(res);

                if (outputFilter != null) {
                    try {
                        // Parse the calendar data into an iCalendar object
                        CalendarBuilder builder = new CalendarBuilder();
                        Calendar calendar = builder.build(new StringReader(
                                                    calendarData));

                        // Write the calendar object back using the filter
                        StringWriter out = new StringWriter();
                        CalendarOutputter outputer = new CalendarOutputter();
                        outputer.output(calendar, out, outputFilter);
                        calendarData = out.toString();
                        out.close();

                        // NB ical4j's outputter generates \r\n line ends but we
                        // need
                        // only \n, so remove all \r's from the string
                        calendarData = calendarData.replaceAll("\r", "");

                    } catch (IOException e) {
                        String msg = "Error while running CALDAV:" +
                                      info.getReportElement().getNodeName() + " report";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (ParserException e) {
                        String msg = "Error while running CALDAV:" +
                                       info.getReportElement().getNodeName() + " report";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    } catch (ValidationException e) {
                        String msg = "Error while running CALDAV:" +
                                      info.getReportElement().getNodeName() + " report";
                        log.error(msg, e);
                        throw new RuntimeException(msg, e);
                    }
                }
                response.setCalendarData(calendarData);
            }
        }

        // Add to multistatus
        ms.addResponse(response);
    }

    /**
     * Read all calendar data from a resource. NB No attempt is made to validate
     * the actual data.
     * 
     * @param res
     *            DavResource from which to read data.
     * @return the resource data as a string.
     */
    protected String readCalendarData(DavResource res) {
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
            res.spool(ctx);
        } catch (IOException e) {
            log.error("unable to read calendar data", e);
            return null;
        }
        return ctx.getOutputStream().toString();
    }

    private OutputFilter parseCalendarData(Element cdata) {
        OutputFilter result = null;
        Period expand = null;
        Period limit = null;
        Period limitfb = null;

        // Look at each child element of calendar-data
        for (ElementIterator iter = DomUtil.getChildren(cdata);
             iter.hasNext();) {

            Element child = iter.nextElement();
            if (CosmoDavConstants.ELEMENT_CALDAV_COMP.equals(child.getLocalName())) {

                // At the top-level of calendar-data there should only be one
                // <comp> element as VCALENDAR components are the only top-level
                // components allowed in iCalendar data
                if (result != null)
                    return null;

                // Get required name attribute and verify it is VCALENDAR
                String name = 
                    DomUtil.getAttribute(child,
                                         CosmoDavConstants.ATTR_CALDAV_NAME,
                                         null);
                if ((name == null) || !Calendar.VCALENDAR.equals(name))
                    return null;

                // Now parse filter item
                result = parseCalendarDataComp(child);

            } else if (CosmoDavConstants.ELEMENT_CALDAV_EXPAND.equals(child
                            .getLocalName())) {
                expand = parsePeriod(child, true);
            } else if (CosmoDavConstants.ELEMENT_CALDAV_LIMIT_RECURRENCE_SET
                    .equals(child.getLocalName())) {
                limit = parsePeriod(child, true);
            } else if (CosmoDavConstants.ELEMENT_CALDAV_LIMIT_FREEBUSY_SET
                    .equals(child.getLocalName())) {
                limitfb = parsePeriod(child, true);
            } else {
                throw new IllegalArgumentException("Invalid child element: "  +
                                                   child.getLocalName() +
                                                   " found in calendar-data element");
            }
        }

        // Now add any limit/expand options, creating a filter if one is not
        // already present
        if ((result == null)
                && ((expand != null) || (limit != null) || (limitfb != null))) {
            result = new OutputFilter("VCALENDAR");
            result.setAllSubComponents();
            result.setAllProperties();
        }
        if (expand != null) {
            result.setExpand(expand);
        }
        if (limit != null) {
            result.setLimit(limit);
        }
        if (limitfb != null) {
            result.setLimitfb(limitfb);
        }

        return result;
    }

    private OutputFilter parseCalendarDataComp(Element comp) {
        // Get required name attribute
        String name =
            DomUtil.getAttribute(comp, CosmoDavConstants.ATTR_CALDAV_NAME,
                                 null);
        if (name == null)
            return null;

        // Now create filter item
        OutputFilter result = new OutputFilter(name);

        // Look at each child element
        ElementIterator i = DomUtil.getChildren(comp);
        while (i.hasNext()) {
            Element child = i.nextElement();
            if (CosmoDavConstants.ELEMENT_CALDAV_ALLCOMP
                    .equals(child.getLocalName())) {
                // Validity check
                if (result.hasSubComponentFilters()) {
                    result = null;
                    return null;
                }
                result.setAllSubComponents();

            } else if (CosmoDavConstants.ELEMENT_CALDAV_ALLPROP.equals(child
                    .getLocalName())) {
                // Validity check
                if (result.hasPropertyFilters()) {
                    result = null;
                    return null;
                }
                result.setAllProperties();

            } else if (CosmoDavConstants.ELEMENT_CALDAV_COMP.equals(child
                    .getLocalName())) {
                // Validity check
                if (result.isAllSubComponents()) {
                    result = null;
                    return null;
                }
                OutputFilter subfilter = parseCalendarDataComp(child);
                if (subfilter == null) {
                    result = null;
                    return null;
                } else
                    result.addSubComponent(subfilter);
            } else if (CosmoDavConstants.ELEMENT_CALDAV_PROP.equals(child
                    .getLocalName())) {
                // Validity check
                if (result.isAllProperties()) {
                    result = null;
                    return null;
                }

                // Get required name attribute
                String propname =
                    DomUtil.getAttribute(child,
                                         CosmoDavConstants.ATTR_CALDAV_NAME,
                                         null);
                if (propname == null) {
                    result = null;
                    return null;
                }

                // Get optional novalue attribute
                boolean novalue = false;
                String novaluetxt =
                    DomUtil.getAttribute(child,
                                         CosmoDavConstants.ATTR_CALDAV_NOVALUE,
                                         null);
                if (novaluetxt != null) {
                    if (CosmoDavConstants.VALUE_YES.equals(novaluetxt))
                        novalue = true;
                    else if (CosmoDavConstants.VALUE_NO.equals(novaluetxt))
                        novalue = false;
                    else {
                        result = null;
                        return null;
                    }
                }

                // Now add property item
                result.addProperty(propname, novalue);
            }
        }

        return result;
    }

    private Period parsePeriod(Element node) {
        return parsePeriod(node, false);
    }

    private Period parsePeriod(Element node, boolean utc) {
        try {
            // Get start (must be present)
            String start =
                DomUtil.getAttribute(node,
                                     CosmoDavConstants.ATTR_CALDAV_START, null);

            if (start == null)
                return null;

            DateTime trstart = new DateTime(start);

            if (utc && !trstart.isUtc()) {
                throw new IllegalArgumentException("CALDAV:timerange error " +
                                                   "start must be UTC");
            }


            // Get end (must be present)
            String end =
                DomUtil.getAttribute(node,
                                     CosmoDavConstants.ATTR_CALDAV_END, null);
            if (end == null)
                return null;

            DateTime trend = new DateTime(end);

            if (utc && !trend.isUtc()) {
                throw new IllegalArgumentException("CALDAV:timerange error " +
                                                   "end must be UTC");
            }

            return new Period(trstart, trend);

        } catch (ParseException e) {
            return null;
        }
    }
}
