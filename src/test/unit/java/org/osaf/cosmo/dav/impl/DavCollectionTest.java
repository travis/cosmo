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
package org.osaf.cosmo.dav.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCollection</code>.
 */
public class DavCollectionTest extends BaseDavTestCase
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(DavCollectionTest.class);

    /** */
    public void testDeadFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollection dc = testHelper.initializeHomeResource();

        boolean found = false;
        for (String name : dc.getDeadPropertyFilter()) {
            if (name.equals(CollectionItem.ATTR_EXCLUDE_FREE_BUSY_ROLLUP)) {
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
        DavCollection dc = testHelper.initializeHomeResource();

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not false", ! flag);
    }

    /** */
    public void testGetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollection dc = testHelper.initializeHomeResource();

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not true", flag);
    }

    /** */
    public void testSetIncludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollection dc = testHelper.initializeHomeResource();

        DavProperty efbr = new ExcludeFreeBusyRollup(false);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /** */
    public void testSetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(false);
        DavCollection dc = testHelper.initializeHomeResource();

        DavProperty efbr = new ExcludeFreeBusyRollup(true);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is false",
                   testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /** */
    public void testBadSetExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollection dc = testHelper.initializeHomeResource();

        // ugh should be interpreted as false
        DavProperty efbr = new DefaultDavProperty(EXCLUDEFREEBUSYROLLUP, "ugh");
        dc.setLiveProperty(efbr);

        assertTrue("failed to interpret String exclude-free-busy-rollup property value as false",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }

    /** */
    public void testRemoveExcludeFreeBusyRollupProperty() throws Exception {
        testHelper.getHomeCollection().setExcludeFreeBusyRollup(true);
        DavCollection dc = testHelper.initializeHomeResource();

        dc.removeLiveProperty(EXCLUDEFREEBUSYROLLUP);

        assertTrue("removed exclude-free-busy-rollup property is true",
                   ! testHelper.getHomeCollection().isExcludeFreeBusyRollup());
    }
}
