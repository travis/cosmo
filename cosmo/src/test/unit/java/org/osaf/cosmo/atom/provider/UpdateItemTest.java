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

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Recur;

import org.apache.abdera.model.Content;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.provider.mock.MockItemRequestContext;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.hibernate.HibEventExceptionStamp;
import org.osaf.cosmo.model.hibernate.HibEventStamp;

/**
 * Test class for {@link ItemProvider#updateEntry()} tests.
 */
public class UpdateItemTest extends BaseItemCollectionAdapterTestCase
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(UpdateItemTest.class);

    public void testUpdateEntry() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setDisplayName("this is a new name");
        RequestContext req = createRequestContext(item, copy);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
    }

    public void testUnsupportedContentType() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setDisplayName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.forgetContentTypes();

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testInvalidContent() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setDisplayName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorValidationError();

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 400, res.getStatus());
    }

    public void testProcessingError() throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        NoteItem copy = (NoteItem) item.copy();
        copy.setUid(item.getUid());
        copy.setDisplayName("this is a new name");
        RequestContext req = createRequestContext(item, copy);
        helper.enableProcessorFailure();

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 500, res.getStatus());
    }

    public void testUpdateEntryChangeRecurrenceStart() throws Exception {
        NoteItem master = makeAndStoreRecurringEvent("20070601T120000");
        NoteItem mod = makeAndStoreModification(master, "20070603T120000");
        String oldModUid = mod.getUid();

        String newModUid = master.getUid() + ":20070603T140000";
        NoteItem copy =
            updateRecurringEventStartDate(master, "20070601T140000");
        RequestContext req = createRequestContext(master, copy);

        ResponseContext res = adapter.putEntry(req);
        assertNotNull("Null response context", res);
        assertEquals("Incorrect response status", 200, res.getStatus());
        assertNotNull("Null etag", res.getEntityTag());
        assertNotNull("Null last modified", res.getLastModified());
        assertNull("Original modification still stored",
                   helper.findItem(oldModUid));
        assertNotNull("Replacement modification not stored",
                      helper.findItem(newModUid));
        assertNull("Original modification still known to master",
                   findModification(master, oldModUid));

        NoteItem replacement = findModification(master, newModUid);
        assertNotNull("Replacement modification not known to master",
                      replacement);
        assertEquals("Master not known to replacement modification", master,
                     replacement.getModifies());
    }

    protected void setUp() throws Exception {
        super.setUp();

        helper.rememberContentType(Content.Type.TEXT.toString());
        helper.rememberProjection(PROJECTION_FULL);
    }

    private RequestContext createRequestContext(NoteItem original,
                                                NoteItem update)
        throws Exception {
        MockItemRequestContext rc =
            new MockItemRequestContext(provider, original,
                                       "PUT");
        rc.setPropertiesAsEntry(serialize(update));
        return rc;
    }

    private NoteItem makeAndStoreRecurringEvent(String start)
        throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        EventStamp stamp = new HibEventStamp();
        item.addStamp(stamp);
        stamp.createCalendar();

        stamp.setStartDate(new DateTime(start));
        stamp.setDuration(new Dur("PT1H"));
        stamp.setRecurrenceRule(new Recur("FREQ=DAILY;COUNT=5"));

        return item;
    }

    private NoteItem makeAndStoreModification(NoteItem master,
                                              String recurrenceId)
        throws Exception {
        NoteItem item = helper.makeAndStoreDummyItem();
        master.addModification(item);
        item.setModifies(master);

        EventExceptionStamp stamp = new HibEventExceptionStamp();
        item.addStamp(stamp);
        stamp.createCalendar();

        stamp.setRecurrenceId(new DateTime(recurrenceId));
        stamp.setSummary("this is an exception");

        return item;
    }

    private NoteItem updateRecurringEventStartDate(NoteItem master,
                                                   String start)
        throws Exception {
        NoteItem copy = (NoteItem) master.copy();
        copy.setUid(master.getUid());

        EventStamp stamp = StampUtils.getEventStamp(copy);
        stamp.setStartDate(new DateTime(start));

        return copy;
    }

    private NoteItem findModification(NoteItem master,
                                      String uid) {
        for (NoteItem mod : master.getModifications()) {
            if (mod.getUid().equals(uid))
                return mod;
        }
        return null;
    }
}
