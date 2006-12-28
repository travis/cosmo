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
package org.osaf.cosmo.eim.schema.collection;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.EimSchemaConstants;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.QName;

/**
 * Test Case for {@link CollectionApplicator}.
 */
public class CollectionApplicatorTest extends TestCase
    implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(CollectionApplicatorTest.class);

    public void testApplyField() throws Exception {
        CollectionItem collection = new CollectionItem();

        EimRecordField field = makeTestRecordField();

        CollectionApplicator applicator = new CollectionApplicator(collection);
        applicator.applyField(field);

        QName qname = new QName(NS_COLLECTION, field.getName());
        Attribute attr = collection.getAttribute(qname);
        assertNotNull("attribute " + qname + " not found", attr);
        assertEquals("incorrect attribute value", field.getValue(),
                     attr.getValue());
    }

    private EimRecordField makeTestRecordField() {
        String n = "deadbeef";
        String v = "The quick brown fox etc and so forth.";
        TextField field = new TextField(n, v);

        EimRecord record = new EimRecord(PREFIX_COLLECTION, NS_COLLECTION);
        record.addField(field);

        return field;
    }
}
