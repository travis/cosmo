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
package org.osaf.cosmo.dav.caldav;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavResource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for CalDAV reports that return multistatus
 * responses.
 *
 * Based on code originally written by Cyrus Daboo.
 */
public abstract class CaldavMultiStatusReport extends CaldavReport {
    private static final Log log =
        LogFactory.getLog(CaldavMultiStatusReport.class);

    private MultiStatus multistatus;
    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;

    // Report methods

    /**
     * Returns true.
     */
    public boolean isMultiStatusReport() {
        return true;
    }

    // our methods

    /**
     * Process the list of calendar resource hrefs found by the report
     * query and create a multistatus response.
     */
    protected void buildResponse()
        throws DavException {
        // Get parent's href as we use this a bit
        String parentHref = getResource().getHref();

        // Get the host portion of the href as we need to check for relative
        // hrefs.
        String host = parentHref.substring(0, parentHref.indexOf("/", 8));

        // Iterate over each href (making sure it is a child of the root)
        // and return a response for each
        multistatus = new MultiStatus();
        for (Iterator i=getHrefs().iterator(); i.hasNext();) {
            // Note that the href sent by the client may be relative, so
            // we need to convert to absolute for subsequent comprisons
            String href = (String) i.next();
            if (! href.startsWith("http")) {
                href = host + href;
            }

            // Check it is a child or the same
            if (!Text.isDescendantOrEqual(parentHref, href)) {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST, "CALDAV:" + getInfo().getReportName() + " href element " + href + " is not a child or equal to request href.");
            }

            // Get resource for this href
            CosmoDavResource child = (CosmoDavResource)
                getResource().getMember(href);
            if (child == null) {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST, "CALDAV:" + getInfo().getReportName() + " href element " + href + " could not be resolved.");
            }

            multistatus.addResponse(buildMultiStatusResponse(child));
        }
    }

    /**
     * Write output to the response.
     */
    protected void output(DavServletResponse response)
        throws IOException {
        response.sendXmlResponse(multistatus,
                                 DavServletResponse.SC_MULTI_STATUS);
    }

    /** */
    public int getPropFindType() {
        return propfindType;
    }

    /** */
    public void setPropFindType(int type) {
        this.propfindType = type;
    }

    /** */
    public DavPropertyNameSet getPropFindProps() {
        return propfindProps;
    }

    /** */
    public void setPropFindProps(DavPropertyNameSet props) {
        this.propfindProps = props;
    }

    // private methods

    private CaldavMultiStatusResponse
        buildMultiStatusResponse(CosmoDavResource resource)
        throws DavException {
        CaldavMultiStatusResponse response =
            new CaldavMultiStatusResponse(resource, propfindProps,
                                          propfindType);

        if (resource.exists()) {
            if (getInfo().getPropertyNameSet().
                contains(CosmoDavConstants.CALENDARDATA)) {
                String calendarData = readCalendarData(resource);

                if (getOutputFilter() != null) {
                    try {
                        CalendarBuilder builder = new CalendarBuilder();
                        Calendar calendar =
                            builder.build(new StringReader(calendarData));

                        // filter the output
                        StringWriter out = new StringWriter();
                        CalendarOutputter outputter = new CalendarOutputter();
                        outputter.output(calendar, out, getOutputFilter());
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
                response.setCalendarData(calendarData);
            }
        }

        return response;
    }
}
