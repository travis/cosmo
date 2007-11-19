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
package org.osaf.cosmo.dav.report;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.impl.DavHomeCollection;
import org.osaf.cosmo.dav.report.mock.MockReport;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.hibernate.HibCalendarCollectionStamp;

import org.w3c.dom.Document;

/**
 * Test case for <code>ReportBase</code>.
 * <p>
 */
public class ReportBaseTest extends BaseDavTestCase
    implements DavConstants {
    private static final Log log = LogFactory.getLog(ReportBaseTest.class);

    public void testDepthInfinity() throws Exception {
        DavHomeCollection home = testHelper.initializeHomeResource();

        CollectionItem coll1 = testHelper.
            makeAndStoreDummyCollection(testHelper.getHomeCollection());
        coll1.addStamp(new HibCalendarCollectionStamp(coll1));
        
        CollectionItem coll2 = testHelper.
            makeAndStoreDummyCollection(testHelper.getHomeCollection());
        
        MockReport report = new MockReport();
        report.init(home, makeReportInfo("freebusy1.xml", DEPTH_INFINITY));

        report.runQuery();

        // Verify report is recursively called on all collections
        // should be 3 collections: home collection and two test
        // collections
        Assert.assertEquals(3, report.calls.size());
        Assert.assertTrue(report.calls.contains(testHelper.getHomeCollection().
                          getDisplayName()));
        Assert.assertTrue(report.calls.contains(coll1.getDisplayName()));
        Assert.assertTrue(report.calls.contains(coll2.getDisplayName()));
    }

    private ReportInfo makeReportInfo(String resource, int depth)
        throws Exception {
        Document doc = testHelper.loadXml(resource);
        return new ReportInfo(doc.getDocumentElement(), depth);
    }
}
