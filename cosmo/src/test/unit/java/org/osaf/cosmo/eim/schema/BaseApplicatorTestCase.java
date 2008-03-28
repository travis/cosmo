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

import junit.framework.TestCase;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.mock.MockQName;

/**
 * Base class for record applicator tests.
 */
public class BaseApplicatorTestCase extends TestCase
    implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(BaseApplicatorTestCase.class);

    /** */
    protected void checkDecimalValue(EimRecordField field,
                                     BigDecimal value) {
        BigDecimal expected = ((DecimalField)field).getDecimal();
        assertEquals("incorrect decimal value", expected, value);
    }

    /** */
    protected void checkIntegerValue(EimRecordField field,
                                     Integer value) {
        Integer expected = ((IntegerField)field).getInteger();
        assertEquals("incorrect integer value", expected, value);
    }

    /** */
    protected void checkBooleanValue(EimRecordField field,
                                     Boolean value) {
        Integer v = BooleanUtils.isTrue(value) ?
            new Integer(1) : new Integer(0);
        checkIntegerValue(field, v);
    }

    /** */
    protected void checkTextValue(EimRecordField field,
                                  String value) {
        String expected = ((TextField)field).getText();
        assertEquals("incorrect text value", expected, value);
    }

    /** */
    protected void checkTimeStampValue(EimRecordField field,
                                       Date value) {
        Date expected =
            new Date(((DecimalField)field).getDecimal().longValue() * 1000);
        assertEquals("incorrect timestamp value", expected, value);
    }

    /** */
    protected void checkUnknownValue(EimRecordField field,
                                     Item item) {
        QName qname = new MockQName(field.getRecord().getNamespace(),
                                field.getName());
        Attribute attr = item.getAttribute(qname);
        assertNotNull("attribute " + qname + " not found", attr);
        assertEquals("incorrect attribute value", field.getValue(),
                     attr.getValue());
    }
    
    protected void addMissingTextField(String fieldName, EimRecord record) {
        TextField tf = new TextField(fieldName, null);
        tf.setMissing(true);
        record.addField(tf);
    }
    
    protected void addMissingClobField(String fieldName, EimRecord record) {
        ClobField cf = new ClobField(fieldName, null);
        cf.setMissing(true);
        record.addField(cf);
    }
    
    protected void addMissingIntegerField(String fieldName, EimRecord record) {
        IntegerField intF = new IntegerField(fieldName, 0);
        intF.setMissing(true);
        record.addField(intF);
    }
}
