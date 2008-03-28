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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.EimRecordKey;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;

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
    protected void checkClobField(EimRecordField field,
                                  String expectedName,
                                  String expectedValue) {
        assertTrue("not a clob field", field instanceof ClobField);
        ClobField cf = (ClobField) field;
        assertEquals("incorrect field name", expectedName, cf.getName());
        assertEquals("incorrect field value", expectedValue, read(cf.getClob()));
    }
    
    /** */
    protected void checkIntegerField(EimRecordField field,
                                  String expectedName,
                                  Integer expectedValue) {
        assertTrue("not an integer field", field instanceof IntegerField);
        IntegerField intF = (IntegerField) field;
        assertEquals("incorrect field name", expectedName, intF.getName());
        assertEquals("incorrect field value", expectedValue, intF.getInteger());
    }

    /** */
    protected void checkBooleanField(EimRecordField field,
                                     String expectedName,
                                     Boolean expectedValue) {
        Integer i = BooleanUtils.isTrue(expectedValue) ?
            new Integer(1) : new Integer(0);
        checkIntegerField(field, expectedName, i);
    }

    /** */
    protected void checkTimeStampField(EimRecordField field,
                                       String expectedName,
                                       Date expectedValue) {
        BigDecimal bd = new BigDecimal(expectedValue.getTime() / 1000);
        checkDecimalField(field, expectedName, bd, DIGITS_TIMESTAMP,
                          DEC_TIMESTAMP);
    }
    
    private String read(Reader reader) {
        if (reader == null)
            return null;
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(reader, writer);
        } catch (IOException e) {
            throw new RuntimeException("error reading stream");
        }
        return writer.toString();
    }
}
