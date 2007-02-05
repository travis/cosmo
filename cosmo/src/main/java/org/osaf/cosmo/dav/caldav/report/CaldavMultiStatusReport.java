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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.osaf.cosmo.dav.caldav.property.CalendarData;
import org.osaf.cosmo.dav.impl.DavCalendarResource;

/**
 * Base class for CalDAV reports that return multistatus
 * responses.
 *
 * Based on code originally written by Cyrus Daboo.
 */
public abstract class CaldavMultiStatusReport extends CaldavReport {
    private static final Log log =
        LogFactory.getLog(CaldavMultiStatusReport.class);

    private MultiStatus multistatus = new MultiStatus();
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
     * Runs the query and builds a multistatus response.
     */
    protected void runQuery()
        throws DavException {
        super.runQuery();

        for (DavCalendarResource member : getResults()) {
            multistatus.addResponse(buildMultiStatusResponse(member));
        }
    }

    /** */
    protected MultiStatus getMultiStatus() {
        return multistatus;
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

    /** */
    protected MultiStatusResponse
        buildMultiStatusResponse(DavCalendarResource resource)
        throws DavException {
        // clone the incoming property name set and remove
        // calendar-data since we generate it manually
        DavPropertyNameSet resourceProps =
            new DavPropertyNameSet(propfindProps);
        resourceProps.remove(CALENDARDATA);

        MultiStatusResponse response =
            new MultiStatusResponse(resource, resourceProps, propfindType);

        if (propfindProps.contains(CALENDARDATA)) {
            String calendarData = null;
            if (resource.exists()) {
                calendarData = readCalendarData(resource);
            }
            response.add(new CalendarData(calendarData));
        }

        return response;
    }
}
