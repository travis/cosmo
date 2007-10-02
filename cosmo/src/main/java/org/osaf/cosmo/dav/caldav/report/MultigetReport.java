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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.UnprocessableEntityException;
import org.osaf.cosmo.dav.impl.DavCalendarResource;

import org.w3c.dom.Element;

/**
 * <p>
 * Represents the <code>CALDAV:calendar-multiget</code> report that
 * provides a mechanism for retrieving in one request the properties
 * and filtered calendar data from the resources identifed by the
 * supplied <code>DAV:href</code> elements.
 * </p>
 */
public class MultigetReport extends CaldavMultiStatusReport {
    private static final Log log = LogFactory.getLog(MultigetReport.class);
    public static final ReportType REPORT_TYPE_CALDAV_MULTIGET =
        ReportType.register(ELEMENT_CALDAV_CALENDAR_MULTIGET,
                            NAMESPACE_CALDAV, MultigetReport.class);

    private Set<String> hrefs;

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_MULTIGET;
    }

    // ReportBase methods

    /**
     * <p>
     * Parses the report info, extracting the properties and output filter.
     * </p>
     * <pre>
     * <!ELEMENT calendar-multiget ((DAV:allprop |
     *                              DAV:propname |
     *                              DAV:prop)?, DAV:href+)>
     * </pre>
     *
     * @throws DavException if the report info is not of the correct type
     * @throws BadRequestException if the report info is invalid
     */
    protected void parseReport(ReportInfo info)
        throws DavException {
        if (! getType().isRequestedReportType(info))
            throw new DavException("Report not of type " + getType().getReportName());

        setPropFindProps(info.getPropertyNameSet());
        if (info.containsContentElement(XML_ALLPROP, NAMESPACE)) {
            setPropFindType(PROPFIND_ALL_PROP);
        } else if (info.containsContentElement(XML_PROPNAME, NAMESPACE)) {
            setPropFindType(PROPFIND_PROPERTY_NAMES);
        } else {
            setPropFindType(PROPFIND_BY_PROPERTY);
            setOutputFilter(findOutputFilter(info));
        }

        List<Element> hrefElements =
            info.getContentElements(XML_HREF, NAMESPACE);
        if (hrefElements.size() == 0) 
            throw new BadRequestException("Expected at least one " + QN_HREF);
        if (getResource() instanceof DavContent && hrefElements.size() > 1)
            throw new BadRequestException("Expected at most one " + QN_HREF);

        URL resourceUrl = ((DavResource)getResource()). getResourceLocator().
            getUrl(true, getResource().isCollection());

        hrefs = new HashSet<String>();
        for (Element element : hrefElements) {
            String href = DomUtil.getTextTrim(element);
            // validate and absolutize submitted href
            URL memberUrl = normalizeHref(resourceUrl, href);

            // check if the href refers to the targeted resource (or to a
            // descendent if the target is a collection)
            if (getResource() instanceof DavCollection) {
                if (! isDescendentOrEqual(resourceUrl, memberUrl))
                    throw new BadRequestException("Href " + href + " does not refer to the requested collection " + resourceUrl + " or a descendent");
            } else {
                if (! memberUrl.equals(resourceUrl))
                    throw new BadRequestException("Href " + href + " does not refer to the requested resource " + resourceUrl);
            }

            // use the absolute path of our normalized URL as the href
            hrefs.add(memberUrl.getPath());
        }
    }

    protected void doQuerySelf(DavResource resource)
        throws DavException {}

    protected void doQueryChildren(DavCollection collection)
        throws DavException {}

    /**
     * Resolves the hrefs provided in the report info to resources.
     */
    protected void runQuery()
        throws DavException {
        DavPropertyNameSet propspec = createResultPropSpec();

        if (getResource() instanceof DavCollection) {
            DavCollection collection = (DavCollection) getResource();
            for (String href : hrefs) {
                DavResource target = collection.findMember(href);
                if (target != null)
                    getMultiStatus().addResponse(buildMultiStatusResponse(target, propspec));
                else
                    getMultiStatus().addResponse(new MultiStatusResponse(href,404));
            }
            return;
        }

        if (getResource() instanceof DavCalendarResource) {
            getMultiStatus().addResponse(buildMultiStatusResponse(getResource(), propspec));
            return;
        }

        throw new UnprocessableEntityException(getType() + " report not supported for non-calendar resources");
    }

    private static URL normalizeHref(URL context,
                                     String href)
        throws DavException {
        URL url = null;
        try {
            url = new URL(context, href);
            // check that the URL is escaped. it's questionable whether or
            // not we should all unescaped URLs, but at least as of
            // 10/02/2007, iCal 3.0 generates them
            url.toURI();
            return url;
        } catch (URISyntaxException e) {
            try {
                URI escaped =
                    new URI(url.getProtocol(), url.getAuthority(), url.getPath(),
                            url.getQuery(), url.getRef());
                return new URL(escaped.toASCIIString());
            } catch (Exception e2) {
                throw new BadRequestException("Malformed unescaped href " + href + ": " + e.getMessage());
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Malformed href " + href + ": " + e.getMessage());
        }
    }

    private static boolean isDescendentOrEqual(URL collection,
                                               URL test) {
        if (collection.equals(test))
            return true;
        return test.getPath().startsWith(collection.getPath());
    }
}
