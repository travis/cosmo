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

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.osaf.cosmo.eim.BlobField;
import org.osaf.cosmo.eim.BytesField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.CalendarAttribute;
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TextAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for classes that apply EIM records to items and stamps.
 *
 * An applicator uses a fixed record type schema (event, task, note)
 * to validate EIM record data and map it to item or stamp properties
 * and attributes.
 */
public abstract class BaseApplicator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(BaseApplicator.class);

    private String prefix;
    private String namespace;
    private Item item;

    /**
     * This class should not be instantiated directly.
     */
    protected BaseApplicator(String prefix,
                             String namespace,
                             Item item) {
        this.prefix = prefix;
        this.namespace = namespace;
        this.item = item;
    }

    /**
     * Copies the field into an item attribute in the record's
     * namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * cannot be applied to the item 
     */
    protected void applyUnknownField(EimRecordField field)
        throws EimSchemaException {
        QName qn = new QName(field.getRecord().getNamespace(), field.getName());
        if (field instanceof BlobField) {
            InputStream value = ((BlobField)field).getBlob();
            item.addAttribute(new BinaryAttribute(qn, value));
        } else if (field instanceof BytesField) {
            byte[] value = ((BytesField)field).getBytes();
            item.addAttribute(new BinaryAttribute(qn, value));
        } else if (field instanceof ClobField) {
            Reader value = ((ClobField)field).getClob();
            item.addAttribute(new TextAttribute(qn, value));
        } else if (field instanceof DateTimeField) {
            Calendar value = ((DateTimeField)field).getCalendar();
            item.addAttribute(new CalendarAttribute(qn, value));
        } else if (field instanceof DecimalField) {
            BigDecimal value = ((DecimalField)field).getDecimal();
            item.addAttribute(new DecimalAttribute(qn, value));
        } else if (field instanceof IntegerField) {
            Integer value = ((IntegerField)field).getInteger();
            item.addAttribute(new IntegerAttribute(qn, new Long(value.longValue())));
        } else if (field instanceof TextField) {
            String value = ((TextField)field).getText();
            item.addAttribute(new StringAttribute(qn, value));
        } else {
            throw new EimSchemaException("Field " + field.getName() + " is of unknown type " + field.getClass().getName());
        }
    }


    /** */
    public String getPrefix() {
        return prefix;
    }

    /** */
    public String getNamespace() {
        return namespace;
    }

    /** */
    public Item getItem() {
        return item;
    }
}
