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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.jdom.Document;
import org.jdom.Element;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.report.Report;
import org.osaf.cosmo.dav.report.ReportInfo;
import org.osaf.cosmo.dav.report.ReportType;

/**
 * @author cyrusdaboo
 * 
 * <code>QueryReport</code> encapsulates the CALDAV:calendar-query report,
 * that provides a mechanism for finding calendar resources matching specified
 * criteria. It should be supported by all CalDAV resources. <p/> CalDAV
 * specifies the following required format for the request body:
 * 
 * <pre>
 *    &lt;!ELEMENT calendar-query (DAV:allprop | DAV:propname | DAV:prop)?
 *                  filter&gt;
 * </pre>
 * 
 */
public class QueryReport extends AbstractCalendarDataReport {

    protected QueryFilter filter;

    /**
     * Returns {@link ReportType#CALDAV_QUERY}.
     * 
     * @return
     * @see Report#getType()
     */
    public ReportType getType() {
        return ReportType.CALDAV_QUERY;
    }

    /**
     * Set the <code>ReportInfo</code>.
     * 
     * @param info
     * @throws IllegalArgumentException
     *             if the given <code>ReportInfo</code> does not contain a
     *             DAV:expand-property element.
     * @see Report#setInfo(ReportInfo)
     */
    public void setInfo(ReportInfo info)
        throws IllegalArgumentException {
        if (info == null
                || !CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_QUERY.equals(info
                        .getReportElement().getName())) {
            throw new IllegalArgumentException(
                    "CALDAV:calendar-query element expected.");
        }
        this.info = info;

        // Parse the report element.
        // calendar-query is basically a PROPFIND request but with a filter item
        // added in.
        // The code here is pretty much copied from
        // WebdavRequestImpl.parsePropFindRequest.

        propfindProps = new DavPropertyNameSet();
        hasOldStyleCalendarData = false;
        boolean gotPropType = false;

        List childList = info.getReportElement().getChildren();
        for (int i = 0; i < childList.size(); i++) {
            Element child = (Element) childList.get(i);
            String nodeName = child.getName();
            if (XML_PROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_BY_PROPERTY;
                propfindProps = new DavPropertyNameSet(child);
                gotPropType = true;

                // Look for CALDAV:calendar-data element as a property
                Iterator iter = propfindProps.iterator();
                while (iter.hasNext()) {
                    DavPropertyName name = (DavPropertyName) iter.next();
                    if (CosmoDavConstants.CALENDARDATA.equals(name)) {
                        // Remove it from the property list that the report will
                        // return as we will handle this one ourselves
                        propfindProps.remove(name);

                        // Now find the calendar-data element inside the prop
                        // element and cache that
                        calendarDataElement = child.getChild(
                                CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                                CosmoDavConstants.NAMESPACE_CALDAV);
                    }
                }
            } else if (XML_PROPNAME.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_PROPERTY_NAMES;
                gotPropType = true;
            } else if (XML_ALLPROP.equals(nodeName)) {
                if (gotPropType) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one prop/propname/allprop element.");
                }
                propfindType = PROPFIND_ALL_PROP;
                gotPropType = true;
            } else if (CosmoDavConstants.ELEMENT_CALDAV_FILTER.equals(nodeName)) {

                // Can only have one filter
                if (filter != null) {
                    throw new IllegalArgumentException(
                            "CALDAV:calendar-query must contain only one filter element.");
                }

                // Parse out filter element
                filter = new QueryFilter();
                filter.parseElement(child);

            } else if (CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA
                    .equals(nodeName)) {
                // TODO this is the old-style calendar-data location. We need to
                // change calendar-data to being a property.
                hasOldStyleCalendarData = true;
                calendarDataElement = child;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osaf.cosmo.dav.report.caldav.AbstractCalendarDataReport#toXml()
     */
    public Document toXml()
        throws DavException {
        // This is the meaty part of the query report. What wse do here is
        // execute the query and generate the list of hrefs that match. We then
        // hand that off to the base class to complete the actual multi-status
        // output.

        // Initially empty list of matching hrefs
        hrefs = new Vector();

        try {
            // Create a query and run it
            Query q = getQuery();
            QueryResult qR = q.execute();

            // Convert the query results into hrefs for each result
            queryResultToHrefs(qR);

        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Hand off to parent with list of matching hrefs now complete
        return super.toXml();
    }

    /**
     * Generate a JCR query for the caldav filter items.
     * 
     * @return
     * @throws RepositoryException
     */
    private Query getQuery()
        throws RepositoryException {

        // Create the XPath expression
        String statement = "/jcr:root" + resource.getLocator().getJcrPath();
        statement += filter.toXPath();

        // Now create an XPath query
        QueryManager qMgr = info.getSession().getRepositorySession()
                .getWorkspace().getQueryManager();
        Query result = qMgr.createQuery(statement, Query.XPATH);

        return result;
    }

    /**
     * Turns a query result into a list of hrefs. This is pretty much copied
     * vebatim from org.apache.jackrabbit.webdav.jcr.search.SearchResourceImpl
     * 
     * NB Because currently Jackrabbit does not support the node axis in
     * predicates the paths we get are the paths to the end nodes/parameters.
     * However we need to extract the path to the actual .ics resources, which
     * means trunctaing the ones we get at the appropriate place.
     * 
     * @param qResult
     * @throws RepositoryException
     */
    private void queryResultToHrefs(QueryResult qResult)
        throws RepositoryException {

        DavResourceLocator locator = resource.getLocator();

        // Get the JCR path segment of the root. We will use this to help
        // truncate the results up to the .ics resources.
        String root = resource.getLocator().getJcrPath();
        int rootLength = root.length();

        RowIterator rowIter = qResult.getRows();
        while (rowIter.hasNext()) {
            Row row = rowIter.nextRow();

            // get the jcr:path column indicating the node path and build
            // a webdav compliant resource path of it.
            String itemPath = row.getValue(JcrConstants.JCR_PATH).getString();

            // Truncate to .ics resource
            if (itemPath.length() > rootLength) {
                int pathLen = itemPath.indexOf("/", rootLength + 1);
                if (pathLen > 0)
                    itemPath = itemPath.substring(0, pathLen);
            }

            // create a new ms-response for this row of the result set
            DavResourceLocator loc = locator.getFactory()
                    .createResourceLocator(locator.getPrefix(),
                            locator.getWorkspacePath(), itemPath, false);
            String href = loc.getHref(true);

            hrefs.add(href);
        }
    }
}