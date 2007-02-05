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
package org.osaf.cosmo.eim.schema;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.TimeStampField;

/**
 * Base class for record generator tests.
 */
public class BaseGeneratorTestCase extends TestCase
    implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(BaseGeneratorTestCase.class);

    /** */
    protected void checkNamespace(EimRecord record,
                                  String prefix,
                                  String namespace) {
        assertEquals("incorrect record prefix", prefix, record.getPrefix());
        assertEquals("incorrect record namespace", namespace,
                     record.getNamespace());
    }

    /** */
    protected void checkDeleted(EimRecord record) {
        assertTrue("record should be deleted", record.isDeleted());
    }

    /** */
    protected void checkNotDeleted(EimRecord record) {
        assertFalse("record incorrectly deleted", record.isDeleted());
    }

    /** */
    protected void checkUuidKey(EimRecordKey key,
                                String uuid) {
        assertNotNull("null key", key);

        List<EimRecordField> keyFields = key.getFields();
        assertEquals("unexpected number of key fields", 1, keyFields.size());

        EimRecordField keyField = keyFields.get(0);
        assertEquals("incorrect key field name", FIELD_UUID,
                     keyField.getName());
        assertEquals("incorrect key field value", uuid, keyField.getValue());
    }

    /** */
    protected void checkDecimalField(EimRecordField field,
                                     String expectedName,
                                     BigDecimal expectedValue,
                                     int expectedDigits,
                                     int expectedDecimalPlaces) {
        assertTrue("not a decimal field", field instanceof DecimalField);
        DecimalField df = (DecimalField) field;
        assertEquals("incorrect field name", expectedName, df.getName());
        assertEquals("incorrect field value", expectedValue, df.getDecimal());
        assertEquals("incorrect digits", expectedDigits, df.getDigits());
        assertEquals("incorrect decimal places", expectedDecimalPlaces,
                     df.getDecimalPlaces());
    }

    /** */
    protected void checkTextField(EimRecordField field,
                                  String expectedName,
                                  String expectedValue) {
        assertTrue("not a text field", field instanceof TextField);
        TextField tf = (TextField) field;
        assertEquals("incorrect field name", expectedName, tf.getName());
        assertEquals("incorrect field value", expectedValue, tf.getText());
    }

    /** */
    protected void checkTimeStampField(EimRecordField field,
                                       String expectedName,
                                       Date expectedValue) {
        assertTrue("not a timestamp field", field instanceof TimeStampField);
        TimeStampField tsf = (TimeStampField) field;
        assertEquals("incorrect field name", expectedName, tsf.getName());
        assertEquals("incorrect field value", expectedValue,
                     tsf.getTimeStamp());
    }
}
