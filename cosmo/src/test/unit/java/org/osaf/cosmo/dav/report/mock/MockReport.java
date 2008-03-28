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
package org.osaf.cosmo.dav.report.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.report.ReportBase;

/**
 * <p>
 * Mock used for testing reports.
 * </p>
 * <p>
 * When the report is run, each resource that is queried is added to the
 * {@link calls} list so that a test case can verify they were called.
 * </p>
 *
 */
public class MockReport extends ReportBase { 
    private static final Log log = LogFactory.getLog(MockReport.class);

    // keep track of which resource were passed to doQuery()
    public List<String> calls = new ArrayList<String>();

    protected void output(DavServletResponse response)
        throws DavException {
        // XXX
    }

    protected void parseReport(ReportInfo info)
        throws DavException {
        // XXX
    }    

    /**
     * Adds the queried resource to {@link #calls}.
     */
    protected void doQuerySelf(DavResource resource)
        throws DavException {
        if (log.isDebugEnabled())
            log.debug("querying " + resource.getResourcePath());
        calls.add(resource.getDisplayName());
    }

    /**
     * Does nothing.
     */
     protected void doQueryChildren(DavCollection collection)
        throws DavException {
        if (log.isDebugEnabled())
            log.debug("querying children of " + collection.getResourcePath());
    }

    public ReportType getType() {
        // XXX
        return null;
    }

    public boolean isMultiStatusReport() {
        // XXX
        return false;
    }

}
