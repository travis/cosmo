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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.dav.report.BaseReportTestCase;
import org.osaf.cosmo.model.CollectionItem;

import org.w3c.dom.Document;

/**
 * Test case for <code>FreeBusyReport</code>.
 */
public class FreeBusyReportTest extends BaseReportTestCase {
    private static final Log log =
        LogFactory.getLog(FreeBusyReportTest.class);

    public void testWrongType() throws Exception {
        DavCalendarCollection dcc =
            testHelper.initializeDavCalendarCollection("freebusy");

        FreeBusyReport report = new FreeBusyReport();
        try {
            report.init(dcc, makeReportInfo("multiget1.xml", DEPTH_1));
            fail("Non-freebusy report info initalized");
        } catch (Exception e) {}
    }

    public void testIncludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, home);

        report.runQuery();
    }

    public void testExcludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, home);

        try {
            report.runQuery();
            fail("free-busy report targeted at excluded collection should not have succeeded but did");
        } catch (DavException e) {
            assertEquals("free-busy report targeted at excluded collection did not return 403", 403, e.getErrorCode());
        }
    }

    public void testExcludedParentCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        CollectionItem coll = testHelper.
            makeAndStoreDummyCollection(testHelper.getHomeCollection());
        coll.setExcludeFreeBusyRollup(false);

        DavHomeCollection home = testHelper.initializeHomeResource();
        DavCollection dc =
            (DavCollection) testHelper.findMember(home, coll.getName());

        FreeBusyReport report = makeReport("freebusy1.xml", DEPTH_1, dc);

        try {
            report.runQuery();
            fail("free-busy report targeted at collection with excluded parent should not have succeeded but did");
        } catch (DavException e) {
            assertEquals("free-busy report targeted at collection with excluded parent did not return 403", 403, e.getErrorCode());
        }
    }

    private FreeBusyReport makeReport(String reportXml,
                                      int depth,
                                      DavResource target)
        throws Exception {
        return (FreeBusyReport)
            super.makeReport(FreeBusyReport.class, reportXml, depth, target);
    }
}
