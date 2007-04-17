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
package org.osaf.cosmo.eim.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.eimml.EimmlStreamChecker;

/**
 * Test Case for {@link JsonStreamWriter}.
 */
public class JsonStreamWriterTest extends TestCase
    implements EimmlConstants, JsonConstants {
    private static final Log log =
        LogFactory.getLog(JsonStreamWriterTest.class);

    private TestHelper testHelper;

    /** */
    protected void setUp() {
        testHelper = new TestHelper();
    }

    /** */
    public void testBasicWrite() throws Exception {
        // XXX disable until json-rpc is gone
        if (true)
            return;

        String uuid = "12345-ABCD-12345";
        String eventNs = "http://bobby/event";
        String eventPrefix = "event";
        String noteNs = "http://bobby/note";
        String notePrefix = "note";
        
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(uuid);

        EimRecord eventRecord = new EimRecord(eventPrefix, eventNs);
        eventRecord.addKeyField(new TextField("uuid", "12345-ABCD-12345"));
        eventRecord.addField(new TextField("dtstart", ";VALUE=DATE-TIME:20070212T074500"));
        eventRecord.addField(new TextField("duration", "PT1H"));
        eventRecord.addField(new TextField("location", null));
        eventRecord.addField(new TextField("status", "confirmed"));
        recordset.addRecord(eventRecord);

        EimRecord noteRecord = new EimRecord(notePrefix, noteNs);
        noteRecord.addField(makeClobField());
        recordset.addRecord(noteRecord);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JsonStreamWriter writer =
            new JsonStreamWriter(out);
        writer.writeRecordSet(recordset);
        writer.close();

        System.out.print(new String(out.toByteArray()));

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      
    }

    private ClobField makeClobField() {
        return new ClobField("body", new InputStreamReader(testHelper.getInputStream("eimml/jabberwocky.txt")));
    }
}
