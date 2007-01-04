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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.osaf.cosmo.eim.BlobField;
import org.osaf.cosmo.eim.BytesField;
import org.osaf.cosmo.eim.ClobField;
import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.TimeStampField;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.BinaryAttribute;
import org.osaf.cosmo.model.CalendarAttribute;
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TextAttribute;
import org.osaf.cosmo.model.Item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for classes that generate EIM records from items and stamps.
 *
 * A generator uses a fixed record type schema (event, task, note)
 * to convert or stamp properties and attributes into EIM field values.
 */
public abstract class BaseGenerator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(BaseGenerator.class);

    private String prefix;
    private String namespace;
    private Item item;

    /**
     * This class should not be instantiated directly.
     */
    protected BaseGenerator(String prefix,
                            String namespace,
                            Item item) {
        this.prefix = prefix;
        this.namespace = namespace;
        this.item = item;
    }

    /**
     * Returns a list of record fields for each item attribute in the
     * generator's namespace.
     */
    protected List<EimRecordField> generateUnknownFields() {
        return generateUnknownFields(namespace);
    }

    /**
     * Returns a list of record fields for each item attribute in the
     * given namespace.
     */
    protected List<EimRecordField> generateUnknownFields(String namespace) {
        Map<String, Attribute> attrs = item.getAttributes(namespace);
        ArrayList<EimRecordField> fields = new ArrayList<EimRecordField>();
        for (Attribute attr : attrs.values()) {
            if (attr instanceof BinaryAttribute) {
                InputStream value = ((BinaryAttribute)attr).getInputStream();
                fields.add(new BlobField(attr.getName(), value));
            } else if (attr instanceof CalendarAttribute) {
                Calendar value = ((CalendarAttribute)attr).getValue();
                fields.add(new DateTimeField(attr.getName(), value));
            } else if (attr instanceof DateAttribute) {
                Date value = ((DateAttribute)attr).getValue();
                fields.add(new TimeStampField(attr.getName(), value));
            } else if (attr instanceof DecimalAttribute) {
                BigDecimal value = ((DecimalAttribute)attr).getValue();
                fields.add(new DecimalField(attr.getName(), value));
            } else if (attr instanceof IntegerAttribute) {
                Long value = ((IntegerAttribute)attr).getValue();
                fields.add(new IntegerField(attr.getName(), new Integer(value.intValue())));
            } else if (attr instanceof StringAttribute) {
                String value = ((StringAttribute)attr).getValue();
                fields.add(new TextField(attr.getName(), value));
            } else if (attr instanceof TextAttribute) {
                Reader value = ((TextAttribute)attr).getReader();
                fields.add(new ClobField(attr.getName(), value));
            } else {
                log.warn("Skipping attribute " + attr.getQName() + " of unknown type " + attr.getClass().getName());
            }
        }
        return fields;
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
