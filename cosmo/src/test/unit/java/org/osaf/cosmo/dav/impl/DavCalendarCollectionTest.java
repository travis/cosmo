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
package org.osaf.cosmo.dav.impl;

import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.SupportedCollationSet;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test case for <code>DavCalendarCollection</code>.
 */
public class DavCalendarCollectionTest extends BaseDavTestCase
    implements ExtendedDavConstants,CaldavConstants  {
    private static final Log log = LogFactory.getLog(DavCalendarCollectionTest.class);

    public void testSupportedCollationSetProperty() throws Exception {
        CollectionItem col = testHelper.makeAndStoreDummyCalendarCollection();
        
        DavHomeCollection home = testHelper.initializeHomeResource();

        DavCollection dcc =
            (DavCalendarCollection) testHelper.findMember(home, col.getName());
        
        SupportedCollationSet prop = 
            (SupportedCollationSet) dcc.getProperty(SUPPORTEDCOLLATIONSET);
        
        Assert.assertNotNull(prop);
        Assert.assertTrue(prop.isProtected());
        Set<String> collations = prop.getCollations();
        Assert.assertNotNull(collations);
        Assert.assertTrue(collations.size()==2);
        for(String c: collations)
            Assert.assertTrue(CalendarUtils.isSupportedCollation(c));
    }

    protected void setUp() throws Exception {
        super.setUp();

        testHelper.logIn();
    }
}
