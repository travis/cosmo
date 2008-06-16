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
package org.osaf.cosmo.atom.provider;

import java.io.FileInputStream;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.mock.MockImportCollectiontContext;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;

/**
 * Test class for {@link ItemCollectionAdapterr#postCollection()} tests.
 */
public class ImportCollectionTest extends BaseItemCollectionAdapterTestCase
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(ImportCollectionTest.class);

    protected String baseDir = "src/test/unit/resources/testdata/atom/provider/";
    
    public void testCreateICSCollection() throws Exception {
        
        Calendar calendar = getCalendar("bigcalendar.ics");
        
        RequestContext req = createCalendarRequestContext("test", calendar.toString());

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 201, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertNotNull("Null Location header", res.getLocation());
        assertNull("Non-null Content-Location header", res.getContentLocation());
        
        Set<Item> children = helper.getHomeCollection().getChildren();
        assertTrue("Collection not stored", children.size()==1);
        CollectionItem stored = (CollectionItem) children.iterator().next();
        
        
        assertEquals("Incorrect display name", "test", stored.getDisplayName());
        assertEquals("Incorrect number children", 8, stored.getChildren().size());
    }   
    
    public void testCreateICSCollectionNoCalendar() throws Exception {
        
        RequestContext req = createCalendarRequestContext("test", null);

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }
    
    public void testCreateICSCollectionBadCalendar() throws Exception {
        
        RequestContext req = createCalendarRequestContext("test", "foo");

        ResponseContext res = adapter.postCollection(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public RequestContext createCalendarRequestContext(String displayName,
            String icalendar) throws Exception {
        MockImportCollectiontContext rc = new MockImportCollectiontContext(provider,
                helper.getUser(), helper.getHomeCollection(), displayName);

        rc.setContentAsCalendar(icalendar);
        return rc;
    }
    
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
}
