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
package org.osaf.cosmo.eim.eimml;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.TextField;

/**
 * Test Case for {@link EimmlStreamWriter}.
 */
public class EimmlStreamWriterTest extends TestCase
    implements EimmlConstants {
    private static final Log log =
        LogFactory.getLog(EimmlStreamWriterTest.class);

    private TestHelper testHelper;

    /** */
    protected void setUp() {
        testHelper = new TestHelper();
    }

    /** */
    public void testBasicWrite() throws Exception {
        String collUuid = "cafebebe";
        String collName = "Test Collection";
        Long colHue = new Long(1);
        String uuid = "deadbeef";
        String ns1 = "cosmo:test:ns1";
        String prefix1 = "ns1";
        String ns2 = "cosmo:test:ns2";
        String prefix2 = "ns2";

        EimRecordSet recordset = new EimRecordSet();
        recordset.setUuid(uuid);

        EimRecord record1 = new EimRecord(prefix1, ns1);
        record1.addKeyField(makeClobField());
        recordset.addRecord(record1);

        EimRecord record2 = new EimRecord(prefix2, ns2);
        record2.addKeyField(new TextField("deadbeef", "http://deadbeef.maz.org/"));
        recordset.addRecord(record2);

        EimRecord record3 = new EimRecord(prefix2, ns2);
        record3.setDeleted(true);
        record3.addKeyField(new TextField("cosmo", "http://cosmo.osafoundation.org/"));
        recordset.addRecord(record3);

        StringWriter out = new StringWriter();
        EimmlStreamWriter writer = new EimmlStreamWriter(out);
        writer.writeStartDocument();
        writer.writeCollection(collUuid, collName, colHue);
        writer.writeRecordSet(recordset);
        writer.close();

        //System.out.print(out.toString());

        StringReader in = new StringReader(out.toString());
        EimmlStreamChecker checker = new EimmlStreamChecker(in);

        checker.nextTag();
        if (! checker.checkStartElement(QN_COLLECTION))
            fail("Expected element " + QN_COLLECTION);
        if (! (checker.checkNamespaceCount(1) &&
               checker.checkNamespace(0, PRE_CORE, NS_CORE)))
            fail("Expected namespace mapping " + PRE_CORE + "=" + NS_CORE);
        if (! checker.checkAttributeCount(3))
            fail("Expected three attributes on " + QN_COLLECTION);
        if (!checker.checkAttribute(0, ATTR_UUID, collUuid))
            fail("Expected attribute " + ATTR_UUID + "=" + collUuid);
        if (!checker.checkAttribute(1, ATTR_NAME, collName))
            fail("Expected attribute " + ATTR_NAME + "=" + collName);
        if (!checker.checkAttribute(2, ATTR_HUE, colHue.toString()))
            fail("Expected attribute " + ATTR_HUE + "=" + colHue);

        checker.nextTag();
        if (! checker.checkStartElement(QN_RECORDSET))
            fail("Expected element " + QN_RECORDSET);
        if (! (checker.checkAttributeCount(1) &&
               checker.checkAttribute(0, ATTR_UUID, uuid)))
            fail("Expected attribute " + ATTR_UUID + "=" + uuid);

        checker.nextTag();
        if (! checker.checkStartElement(ns1, EL_RECORD))
            fail("Expected element " + ns1 + ":" + EL_RECORD);
        if (! (checker.checkNamespaceCount(1) &&
               checker.checkNamespace(0, prefix1, ns1)))
            fail("Expected namespace mapping " + prefix1+ "=" + ns1);
    }

    private ClobField makeClobField() {
        return new ClobField("jabberwocky", new InputStreamReader(testHelper.getInputStream("eimml/jabberwocky.txt")));
    }
}
