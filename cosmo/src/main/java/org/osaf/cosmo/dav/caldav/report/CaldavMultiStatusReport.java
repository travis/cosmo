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
package org.osaf.cosmo.dav.caldav.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.calendar.data.OutputFilter;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarData;
import org.osaf.cosmo.dav.impl.DavCalendarResource;
import org.osaf.cosmo.dav.report.MultiStatusReport;

import org.w3c.dom.Element;

/**
 * <p>
 * Extends <code>MultiStatusReport</code> to handle CalDAV report features.
 * </p>
 * <p>
 * A report request may contain the pseudo-property
 * <code>CALDAV:calendar-data</code>. If so, the calendar data for the resource
 * is to be returned in the response as the value of a property of the same
 * name. The value for the request property may specify
 * an <em>output filter</em> that restricts the components, properties and
 * parameters included in the calendar data in the response. Subclasses are
 * responsible for setting the output filter when parsing the report info.
 * </p>
 */
public abstract class CaldavMultiStatusReport extends MultiStatusReport
    implements CaldavConstants {
    private static final Log log =
        LogFactory.getLog(CaldavMultiStatusReport.class);

    private OutputFilter outputFilter;

    // ReportBase methods


    // MultiStatusReport methods

    /**
     * Removes <code>CALDAV:calendar-data</code> from the property spec
     * since it doesn't represent a real property.
     */
    protected DavPropertyNameSet createResultPropSpec() {
        DavPropertyNameSet spec = super.createResultPropSpec();
        spec.remove(CALENDARDATA);
        return spec;
    }

    /**
     * Includes the resource's calendar data in the response as the
     * <code>CALDAV:calendar-data</code> property if it was requested. The
     * calendar data is filtered if a filter was included in the request.
     */
    protected MultiStatusResponse
        buildMultiStatusResponse(DavResource resource,
                                 DavPropertyNameSet props)
        throws DavException {
        MultiStatusResponse msr =
            super.buildMultiStatusResponse(resource, props);

        DavCalendarResource dcr = (DavCalendarResource) resource;
        if (getPropFindProps().contains(CALENDARDATA))
            msr.add(new CalendarData(readCalendarData(dcr)));

        return msr;
    }

    // our methods

    public OutputFilter getOutputFilter() {
        return outputFilter;
    }

    public void setOutputFilter(OutputFilter outputFilter) {
        this.outputFilter = outputFilter;
    }

    /**
     * Parses an output filter out of the given report info.
     */
    protected static OutputFilter findOutputFilter(ReportInfo info)
        throws DavException {
        Element propdata =
            DomUtil.getChildElement(info.getReportElement(),
                                    XML_PROP, NAMESPACE);
        if (propdata == null)
            return null;

        Element cdata =
            DomUtil.getChildElement(propdata, ELEMENT_CALDAV_CALENDAR_DATA,
                                    NAMESPACE_CALDAV);
        if (cdata == null)
            return null;

        return CaldavOutputFilter.createFromXml(cdata);
    }

    private String readCalendarData(DavCalendarResource resource)
        throws DavException {
        if (! resource.exists())
            return null;
        StringBuffer buffer = new StringBuffer();
        if (outputFilter != null)
            outputFilter.filter(resource.getCalendar(), buffer);
        else
            buffer.append(resource.getCalendar().toString());
        return buffer.toString();
    }
}
