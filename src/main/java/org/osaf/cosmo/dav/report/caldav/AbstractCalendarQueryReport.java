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

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.jcr.JcrDavSession;

import org.osaf.cosmo.jackrabbit.query.XPathTimeRangeQueryBuilder;

/**
 * @author cyrusdaboo
 * 
 * <code>AbstractCalendarQueryReport</code> is a base class for CalDAV reports
 * that use queries to determine matching items.
 * 
 */
public abstract class AbstractCalendarQueryReport
    extends AbstractCalendarDataReport {
    private static final Log log =
        LogFactory.getLog(AbstractCalendarDataReport.class);

    protected QueryFilter filter;

    /**
     * Generate a JCR query for the caldav filter items.
     * 
     * @return
     * @throws RepositoryException
     */
    protected Query getQuery()
        throws DavException, RepositoryException {

        // Create the XPath expression
        String statement = "/jcr:root" +
            resource.getLocator().getRepositoryPath() + filter.toXPath();

        if (log.isDebugEnabled()) {
            log.debug("executing JCR query " + statement);
        }

        // Now create an XPath query
        Session repSession =
            JcrDavSession.getRepositorySession(info.getSession());
        QueryManager qMgr = repSession.getWorkspace().getQueryManager();
        Query result = qMgr.createQuery(statement,
                XPathTimeRangeQueryBuilder.XPATH_TIMERANGE);

        return result;
    }

    /**
     * Turns a query result into a list of hrefs. This is pretty much copied
     * vebatim from org.apache.jackrabbit.webdav.jcr.search.SearchResourceImpl
     * 
     * NB Because currently Jackrabbit does not support the node axis in
     * predicates the paths we get are the paths to the end nodes/parameters.
     * However we need to extract the path to the actual .ics resources, which
     * means truncating the ones we get at the appropriate place.
     * 
     * @param qResult
     * @throws RepositoryException
     */
    protected void queryResultToHrefs(QueryResult qResult)
        throws RepositoryException {

        DavResourceLocator locator = resource.getLocator();

        // Get the JCR path segment of the root. We will use this to help
        // truncate the results up to the .ics resources.
        String root = resource.getLocator().getRepositoryPath();
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
