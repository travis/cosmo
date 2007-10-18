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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
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

        StringWriter out = new StringWriter();
        JsonStreamWriter writer = new JsonStreamWriter(out);
        writer.writeRecordSet(recordset);
        writer.close();

        if (log.isDebugEnabled())
            log.debug("Printing a single record set\n" + out.toString());
    }
    
    public void testWriteMultipleRecordSets() throws Exception{
        String uuid = "12345-ABCD-12345";
        String noteNs = "http://bobby/note";
        String notePrefix = "note";
        
        EimRecord noteRecord = new EimRecord(notePrefix, noteNs);
        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(uuid);
        
        recordset.addRecord(noteRecord);
        
        StringWriter out = new StringWriter();
        JsonStreamWriter writer = new JsonStreamWriter(out);
        writer.writeContainer();
        writer.writeRecordSet(recordset);
        writer.writeRecordSet(recordset);
        writer.writeRecordSet(recordset);
        writer.close();

        if (log.isDebugEnabled())
            log.debug("Printing multiple record sets\n" + out.toString());
    }

    private ClobField makeClobField() {
        return new ClobField("body", new InputStreamReader(testHelper.getInputStream("eimml/jabberwocky.txt")));
    }

    public void testUnicodeClob() throws Exception {
        String unicode = "åß∂ƒ©˙∆˚¬…\u2028\u2029";
        String unicodeResult = "åß∂ƒ©˙∆˚¬…\\u2028\\u2029";
        log.error("unicode: " + unicode);
        StringReader reader = new StringReader(unicode);
        //log.error("reader: " + IOUtils.toString(reader));
        //log.error("json: " + JSONObject.valueToString(IOUtils.toString(reader)));
        ClobField field = new ClobField("unicode", reader);

        StringWriter out = new StringWriter();
        JsonStreamWriter writer = new JsonStreamWriter(out);
        writer.getActual().object();
        writer.writeField(field);
        writer.getActual().endObject();
        writer.close();

        log.error("UTF-8 JSON: " + out);
        
        String jsonString =  out.toString();
        String preamble = "{\"unicode\":[\"clob\",\"";
        //get rid of the json stuff, just want the clob
        jsonString = jsonString.substring(preamble.length());
        jsonString = jsonString.substring(0, jsonString.length() - 3);
        
        for (int x = 0; x < jsonString.length(); x++){
            Character j = new Character(jsonString.charAt(x));   
            Character s = new Character(unicodeResult.charAt(x));
            assertEquals(j, s);
        }
        assertEquals(unicodeResult, jsonString);
    }
}
