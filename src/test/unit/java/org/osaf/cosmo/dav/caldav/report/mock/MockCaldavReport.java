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
package org.osaf.cosmo.dav.caldav.report.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.osaf.cosmo.dav.caldav.report.CaldavReport;

/**
 * Mock implementation used for testing.
 *
 */
public class MockCaldavReport extends CaldavReport {

    // keep track of which resource were passed to doQuery()
    public List<String> calls = new ArrayList<String>();
    
    @Override
    protected void doQuery(DavResource resource, boolean recurse) throws DavException {
        calls.add(resource.getDisplayName());
        super.doQuery(resource, recurse);
    }

    @Override
    protected void output(DavServletResponse response) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void parseReport(ReportInfo info) throws DavException {
        // TODO Auto-generated method stub

    }

    public ReportType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isMultiStatusReport() {
        // TODO Auto-generated method stub
        return false;
    }

}
