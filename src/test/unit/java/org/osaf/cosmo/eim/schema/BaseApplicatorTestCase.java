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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.TimeStampField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

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
    protected void checkTextValue(EimRecordField field,
                                  String value) {
        String expected = ((TextField)field).getText();
        assertEquals("incorrect text value", expected, value);
    }

    /** */
    protected void checkTimeStampValue(EimRecordField field,
                                       Date value) {
        Date expected = ((TimeStampField)field).getTimeStamp();
        assertEquals("incorrect timestamp value", expected, value);
    }

    /** */
    protected void checkUnknownValue(EimRecordField field,
                                     Item item) {
        QName qname = new QName(field.getRecord().getNamespace(),
                                field.getName());
        Attribute attr = item.getAttribute(qname);
        assertNotNull("attribute " + qname + " not found", attr);
        assertEquals("incorrect attribute value", field.getValue(),
                     attr.getValue());
    }
}
