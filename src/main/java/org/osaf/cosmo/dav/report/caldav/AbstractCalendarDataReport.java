/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.filter.OutputFilter;

import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.jdom.Document;
import org.jdom.Element;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.impl.CosmoDavResourceImpl;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;

/**
 * @author cyrusdaboo
 * 
 * <code>AbstractCalendarDataReport</code> is a base class for CalDAV reports
 * that use properties and calendar-data elements.
 * 
 */
public abstract class AbstractCalendarDataReport implements Report,
        DavConstants {

    protected DavResource resource;

    protected ReportInfo info;

    protected int propfindType = PROPFIND_ALL_PROP;

    protected DavPropertyNameSet propfindProps;

    protected List hrefs;

    protected boolean hasOldStyleCalendarData;

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
     * 
     * @return Xml <code>Document</code> as defined by <a
     *         href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>
     * @throws DavException
     * @see Report#toXml()
     */
    public Document toXml() throws DavException {
        if (info == null || resource == null) {
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error while running CALDAV:"
                            + info.getReportElement().getName() + " report");
        }

        // Get parent's href as we use this a bit
        String parentHref = resource.getHref();

        // Get the host portion of the href as we need to check for relative
        // hrefs.
        String host = parentHref.substring(0, parentHref.indexOf("/", 8));

        // Look for and parse any filter items here
        if (calendarDataElement.getChildren().size() != 0) {

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
                                + info.getReportElement().getName()
                                + " href element is not a child or equal to request href.");
            }

            // Get resource for this href
            DavResource hrefResource = ((CosmoDavResourceImpl) resource)
                    .getChildHref(href, info.getSession());

            // Check it is a child or the same
            if (hrefResource == null) {
                throw new IllegalArgumentException("CALDAV:"
                        + info.getReportElement().getName()
                        + " href element could not be resolved.");
            }

            buildMultiStatus(hrefResource, ms);
        }
        return ms.toXml();
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
    protected void buildMultiStatus(DavResource res, MultiStatus ms)
            throws DavException {

        // Generate response
        CalDAVMultiStatusResponse response = new CalDAVMultiStatusResponse(res,
                propfindProps, propfindType);

        // Add calendar data if required
        if (res.exists()) {

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
                    throw new DavException(
                            DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error while running CALDAV:"
                                    + info.getReportElement().getName()
                                    + " report");
                } catch (ParserException e) {
                    throw new DavException(
                            DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error while running CALDAV:"
                                    + info.getReportElement().getName()
                                    + " report");
                } catch (ValidationException e) {
                    throw new DavException(
                            DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error while running CALDAV:"
                                    + info.getReportElement().getName()
                                    + " report");
                }
            }

            // TODO We handle the old-style calendar-data format where that
            // element is placed outside the propstat element. Eventually this
            // will go away when old-style clients are updated.

            // Add the calendar data if we got any
            if (calendarData != null) {
                response.setCalendarData(calendarData, hasOldStyleCalendarData);
            } else {
                throw new DavException(
                        DavServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Error while running CALDAV:"
                                + info.getReportElement().getName() + " report");
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

        // Generate calendar data string from resource
        String calendarData = null;

        // spool content in case of 'GET' request
        InputStream in = res.getStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (in != null) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
                buffer = null;
                out.flush();
            }
        } catch (IOException e) {

            // Just let finally handle it
        } finally {
            // also close stream if not sending content
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (out != null) {
                try {
                    out.flush();
                    calendarData = out.toString();
                    out.close();
                } catch (IOException e) {
                    // ignore
                }

            }
        }

        return calendarData;
    }

    private OutputFilter parseCalendarData(Element cdata) {

        OutputFilter result = null;

        // Look at each child element of calendar-data
        for (Iterator iter = cdata.getChildren().iterator(); iter.hasNext();) {

            Element child = (Element) iter.next();
            if (CosmoDavConstants.ELEMENT_CALDAV_COMP.equals(child.getName())) {

                // At the top-level of calendar-data there should only be one
                // <comp> element as VCALENDAR components are the only top-level
                // components allowed in iCalendar data
                if (result != null)
                    return null;

                // Get required name attribute and verify it is VCALENDAR
                String name = child
                        .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
                if ((name == null) || !Calendar.VCALENDAR.equals(name))
                    return null;

                // Now parse filter item
                result = parseCalendarDataComp(child);

            } else if (CosmoDavConstants.ELEMENT_CALDAV_EXPAND_RECURRENCE_SET
                    .equals(child.getName())) {
                // TODO Handle this element
            } else if (CosmoDavConstants.ELEMENT_CALDAV_LIMIT_RECURRENCE_SET
                    .equals(child.getName())) {
                // TODO Handle this element
            }
        }

        return result;
    }

    private OutputFilter parseCalendarDataComp(Element comp) {

        // Get required name attribute
        String name = comp
                .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
        if (name == null)
            return null;

        // Now create filter item
        OutputFilter result = new OutputFilter(name);

        // Look at each child element
        for (Iterator iter = comp.getChildren().iterator(); iter.hasNext();) {
            Element child = (Element) iter.next();
            if (CosmoDavConstants.ELEMENT_CALDAV_ALLCOMP
                    .equals(child.getName())) {
                // Validity check
                if (result.hasSubComponentFilters()) {
                    result = null;
                    return null;
                }
                result.setAllSubComponents();

            } else if (CosmoDavConstants.ELEMENT_CALDAV_ALLPROP.equals(child
                    .getName())) {
                // Validity check
                if (result.hasPropertyFilters()) {
                    result = null;
                    return null;
                }
                result.setAllProperties();

            } else if (CosmoDavConstants.ELEMENT_CALDAV_COMP.equals(child
                    .getName())) {
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
                    .getName())) {
                // Validity check
                if (result.isAllProperties()) {
                    result = null;
                    return null;
                }

                // Get required name attribute
                String propname = child
                        .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NAME);
                if (propname == null) {
                    result = null;
                    return null;
                }

                // Get optional novalue attribute
                boolean novalue = false;
                String novaluetxt = child
                        .getAttributeValue(CosmoDavConstants.ATTR_CALDAV_NOVALUE);
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
}