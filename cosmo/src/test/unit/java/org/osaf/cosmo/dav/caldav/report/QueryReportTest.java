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

import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.BadRequestException;
import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavEvent;

import org.w3c.dom.Document;

/**
 * Test case for <code>QueryReport</code>.
 */
public class QueryReportTest extends BaseDavTestCase {
    private static final Log log =
        LogFactory.getLog(QueryReportTest.class);

    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("query");

        QueryReport report = new QueryReport();
        try {
            report.init(dcc, makeReportInfo("freebusy1.xml"));
            fail("Freebusy report initalized");
        } catch (DavException e) {}
    }

    private ReportInfo makeReportInfo(String resource)
        throws Exception {
        Document doc = testHelper.loadXml(resource);
        return new ReportInfo(doc.getDocumentElement(), DEPTH_1);
    }
}
