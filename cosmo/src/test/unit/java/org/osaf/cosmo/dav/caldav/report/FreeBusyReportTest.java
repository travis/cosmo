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

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.model.CollectionItem;

import org.w3c.dom.Document;

/**
 * Test case for <code>FreeBusyReport</code>.
 * <p>
 */
public class FreeBusyReportTest extends BaseDavTestCase
    implements DavConstants {
    private static final Log log = LogFactory.getLog(FreeBusyReportTest.class);

    /** */
    public void testIncludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = new FreeBusyReport();
        report.init(home, makeReportInfo("freebusy1.xml", DEPTH_1));

        report.runQuery();
    }

    /** */
    public void testExcludedCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavHomeCollection home = testHelper.initializeHomeResource();

        FreeBusyReport report = new FreeBusyReport();
        report.init(home, makeReportInfo("freebusy1.xml", DEPTH_1));

        try {
            report.runQuery();
            fail("free-busy report targeted at excluded collection should not have succeeded but did");
        } catch (DavException e) {
            assertEquals("free-busy report targeted at excluded collection did not return Forbidden", DavServletResponse.SC_FORBIDDEN, e.getErrorCode());
        }
    }

    /** */
    public void testExcludedParentCollection() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        CollectionItem coll = testHelper.
            makeAndStoreDummyCollection(testHelper.getHomeCollection());
        coll.setExcludeFreeBusyRollup(false);

        DavHomeCollection home = testHelper.initializeHomeResource();
        DavCollection dc =
            (DavCollection) testHelper.getMember(home, coll.getName());

        FreeBusyReport report = new FreeBusyReport();
        report.init(dc, makeReportInfo("freebusy1.xml", DEPTH_1));

        try {
            report.runQuery();
            fail("free-busy report targeted at collection with excluded parent should not have succeeded but did");
        } catch (DavException e) {
            assertEquals("free-busy report targeted at collection with excluded parent did not return Forbidden", DavServletResponse.SC_FORBIDDEN, e.getErrorCode());
        }
    }

    private ReportInfo makeReportInfo(String resource, int depth)
        throws Exception {
        Document doc = testHelper.loadXml(resource);
        return new ReportInfo(doc.getDocumentElement(), depth);
    }
}
