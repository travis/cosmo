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
package org.osaf.cosmo.dav.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.property.DavProperty;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.caldav.report.FreeBusyReport;
import org.osaf.cosmo.dav.caldav.report.MultigetReport;
import org.osaf.cosmo.dav.caldav.report.QueryReport;
import org.osaf.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCollectionBase</code>.
 */
public class DavCollectionBaseTest extends BaseDavTestCase
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(DavCollectionBaseTest.class);

    /** */
    public void testDeadFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        boolean found = false;
        for (String name : dc.getDeadPropertyFilter()) {
            if (name.equals(CollectionItem.class.getName())) {
                found = true;
                break;
            }
        }
        assertTrue("exclude-free-busy-rollup not found in dead property filter",
                   found);
    }

    /** */
    public void testGetIncludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not false", ! flag);
    }

    /** */
    public void testGetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not true", flag);
    }

    /** */
    public void testSetIncludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        ExcludeFreeBusyRollup efbr = new ExcludeFreeBusyRollup(false);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /** */
    public void testSetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        ExcludeFreeBusyRollup efbr = new ExcludeFreeBusyRollup(true);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is false",
                   testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /** */
    public void testRemoveExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollectionBase dc = (DavCollectionBase) testHelper.initializeHomeResource();

        dc.removeLiveProperty(EXCLUDEFREEBUSYROLLUP);

        assertTrue("removed exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    public void testCaldavReportTypes() throws Exception {
        DavCollectionBase test = new DavCollectionBase(null, null, testHelper.getEntityFactory());

        assert(test.getReportTypes().contains(FreeBusyReport.REPORT_TYPE_CALDAV_FREEBUSY));
        assert(test.getReportTypes().contains(MultigetReport.REPORT_TYPE_CALDAV_MULTIGET));
        assert(test.getReportTypes().contains(QueryReport.REPORT_TYPE_CALDAV_QUERY));
    }

    protected void setUp() throws Exception {
        super.setUp();

        testHelper.logIn();
    }
}
