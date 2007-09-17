/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavItemCollection;
import org.osaf.cosmo.dav.report.SimpleReport;
import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>CALDAV:free-busy-query</code> report that
 * provides a mechanism for finding free-busy information. 
 * </p>
 */
public class FreeBusyReport extends SimpleReport implements CaldavConstants {
    private static final Log log = LogFactory.getLog(FreeBusyReport.class);
    private static final VersionFourGenerator uuidGenerator =
        new VersionFourGenerator();

    private Period freeBusyRange;
    private ArrayList<VFreeBusy> freeBusyResults;

    public static final ReportType REPORT_TYPE_CALDAV_FREEBUSY =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                            NAMESPACE_CALDAV, FreeBusyReport.class);

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_FREEBUSY;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting the time range and query filter.
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info))
            throw new DavException("Report not of type " + getType());

        freeBusyRange = findFreeBusyRange(info);
    }

    /**
     * Runs the query, extracts the set of periods for busy time for
     * each occurrence of each found calendar item, creates a
     * VFREEBUSY component, and sets the report's stream, content type
     * and character set.
     */
    protected void runQuery()
        throws DavException {
        if (! (getResource() instanceof DavItemCollection))
            throw new UnprocessableEntityException(getType() + " report not supported for non-collection resources");

        // if the collection or any of its parent is excluded from
        // free busy rollups, deny the query
        DavItemCollection dc = (DavItemCollection) getResource();
        while (dc != null) {
            if (dc.isExcludedFromFreeBusyRollups())
                throw new ForbiddenException("Targeted collection does not participate in freebusy rollups");
            dc = (DavItemCollection) dc.getParent();
            if (! dc.exists())
                dc = null;
        }
        
        // Results are stored as VFREEBUSY components that
        // get merged at the end
        freeBusyResults = new ArrayList<VFreeBusy>();
        
        super.runQuery();

        Calendar calendar = buildFreeBusy();
        String output = writeCalendar(calendar);

        setContentType("text/calendar");
        setEncoding("UTF-8"); 
        setStream(new ByteArrayInputStream(output.getBytes()));
    }    

    protected void doQuerySelf(DavResource resource)
        throws DavException {
        // runQuery already enforced that this is a collection, which never
        // matches a calendar report query
    }

    protected void doQueryChildren(DavCollection collection)
        throws DavException {
        if (isExcluded(collection))
            return;
        if (collection instanceof DavCalendarCollection) {
            DavCalendarCollection dcc = (DavCalendarCollection) collection;
            VFreeBusy vfb = dcc.generateFreeBusy(freeBusyRange);
            if(vfb!=null)
                freeBusyResults.add(vfb);
            return;
        }
        // if it's a regular collection, there won't be any calendar resources
        // within it to match the query
    }

    protected void doQueryDescendents(DavCollection collection)
        throws DavException {
        if (isExcluded(collection))
            return;
        super.doQueryDescendents(collection);
    }

    // our methods

    private static Period findFreeBusyRange(ReportInfo info)
        throws DavException {
        Element tre =
            info.getContentElement(ELEMENT_CALDAV_TIME_RANGE,
                                   NAMESPACE_CALDAV);
        if (tre == null)
            throw new BadRequestException("Expected " + QN_CALDAV_TIME_RANGE);

        DateTime sdt = null;
        try {
            String start = DomUtil.getAttribute(tre, ATTR_CALDAV_START, null);
            sdt = new DateTime(start);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_START + " not parseable: " + e.getMessage());
        }

        DateTime edt = null;
        try {
            String end = DomUtil.getAttribute(tre, ATTR_CALDAV_END, null);
            edt = new DateTime(end);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_END + " not parseable: " + e.getMessage());
        }

        return new Period(sdt, edt);
    }

    private static boolean isExcluded(DavCollection collection) {
        DavItemCollection dic = (DavItemCollection) collection;
        return dic.isExcludedFromFreeBusyRollups();
    }

    /**
     * Build VCALENDAR with single VFREEBUSY
     */
    private Calendar buildFreeBusy()
        throws DavException {

        VFreeBusy vfb = mergeFreeBusyResults();
        
        // Now add VFREEBUSY to a calendar
        Calendar calendar = new Calendar();
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getComponents().add(vfb);

        return calendar;
    }
    
    /**
     * Build single VFREEBUSY
     */
    private VFreeBusy mergeFreeBusyResults() {
        
        // If there is only one result, return it
        if(freeBusyResults.size()==1)
            return freeBusyResults.get(0);
        
        // Otherwise, merge results into single VFREEBUSY
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        for(VFreeBusy vfb: freeBusyResults) {
            PropertyList props = vfb.getProperties(Property.FREEBUSY);
            for(Iterator it = props.iterator();it.hasNext();) {
                FreeBusy fb = (FreeBusy) it.next();
                FbType fbt = (FbType)
                    fb.getParameters().getParameter(Parameter.FBTYPE);
                if ((fbt == null) || FbType.BUSY.equals(fbt)) {
                    busyPeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                    busyTentativePeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                    busyUnavailablePeriods.addAll(fb.getPeriods());
                }
            }
        }
        
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();
        
        // Construct new VFREEBUSY
        VFreeBusy vfb =
            new VFreeBusy(freeBusyRange.getStart(), freeBusyRange.getEnd());
        String uid = uuidGenerator.nextIdentifier().toString();
        vfb.getProperties().add(new Uid(uid));
       
        // Add all periods to the VFREEBUSY
        if (busyPeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyPeriods);
            vfb.getProperties().add(fb);
        }
        if (busyTentativePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyTentativePeriods);
            fb.getParameters().add(FbType.BUSY_TENTATIVE);
            vfb.getProperties().add(fb);
        }
        if (busyUnavailablePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyUnavailablePeriods);
            fb.getParameters().add(FbType.BUSY_UNAVAILABLE);
            vfb.getProperties().add(fb);
        }
        
        return vfb;
    }

    private static String writeCalendar(Calendar calendar)
        throws DavException {
        try {
            StringWriter out = new StringWriter();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
            String output = out.toString();
            out.close();

            // NB ical4j's outputter generates \r\n line ends but we
            // need only \n, so remove all \r's from the string
            output = output.replaceAll("\r", "");

            return output;
        } catch (Exception e) {
            throw new DavException(e);
        }
    }
}
