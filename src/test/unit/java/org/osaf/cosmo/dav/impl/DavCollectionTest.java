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

import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.property.ExcludeFreeBusyRollup;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCollection</code>.
 */
public class DavCollectionTest extends BaseDavResourceTestCase
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(DavCollectionTest.class);

    /** */
    public void testDeadFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(false);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

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
        home.setExcludeFreeBusyRollup(false);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not false", ! flag);
    }

    /** */
    public void testGetExcludeFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(true);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        DavProperty efbr = dc.getProperty(EXCLUDEFREEBUSYROLLUP);
        assertNotNull("exclude-free-busy-rollup property not found", efbr);

        boolean flag = ((Boolean) efbr.getValue()).booleanValue();
        assertTrue("exclude-free-busy-rollup property not true", flag);
    }

    /** */
    public void testSetIncludeFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(true);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        DavProperty efbr = new ExcludeFreeBusyRollup(false);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is true",
                   ! home.isExcludeFreeBusyRollup());
    }

    /** */
    public void testSetExcludeFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(false);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        DavProperty efbr = new ExcludeFreeBusyRollup(true);
        dc.setLiveProperty(efbr);

        assertTrue("set exclude-free-busy-rollup property is false",
                   home.isExcludeFreeBusyRollup());
    }

    /** */
    public void testBadSetExcludeFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(true);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        // ugh should be interpreted as false
        DavProperty efbr = new DefaultDavProperty(EXCLUDEFREEBUSYROLLUP, "ugh");
        dc.setLiveProperty(efbr);

        assertTrue("failed to interpret String exclude-free-busy-rollup property value as false",
                   ! home.isExcludeFreeBusyRollup());
    }

    /** */
    public void testRemoveExcludeFreeBusyRollupProperty() throws Exception {
        home.setExcludeFreeBusyRollup(true);
        DavCollection dc =
            new DavCollection(home, homeLocator, resourceFactory, session);

        dc.removeLiveProperty(EXCLUDEFREEBUSYROLLUP);

        assertTrue("removed exclude-free-busy-rollup property is true",
                   ! home.isExcludeFreeBusyRollup());
    }
}
