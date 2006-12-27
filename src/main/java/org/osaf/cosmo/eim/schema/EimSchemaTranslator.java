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
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TextAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for schema translators.
 *
 * A schema translator implements a concrete schema (event, task,
 * note) with a namespace and a set of typed fields (including maximum
 * lengths, precision, etc).
 *
 * Translators can copy data from an EIM record into an item, changing
 * its properties, attributes, and stamps. They can also create EIM
 * records representing the state of an item.
 *
 * Occasionally a record will contain a field that is not part of the
 * matching schema. This could happen if the schema changed in an
 * updated version of the client, but the server has not yet
 * incorporated the schema change. In this event, the translator
 * stores the field value as an item attribute in the schema's
 * namespace.
 */
public abstract class EimSchemaTranslator implements EimSchemaConstants {
    private static final Log log =
        LogFactory.getLog(EimSchemaTranslator.class);

    private String prefix;
    private String namespace;

    /**
     * This class should not be instantiated directly.
     */
    protected EimSchemaTranslator(String prefix,
                                  String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    /**
     * Copies the data from an EIM record into an item.
     * <p>
     * If the record is marked deleted, then
     * {@link #applyDeletion(EimRecord, Item)} is called.
     * <p>
     * If the record is not marked deleted, then
     * {@link #applyField(EimRecordField, Item)} is called for each
     * non-key record field.
     * 
     * @throws IllegalArgumentException if the record's namespace does
     * not match this translator's namespace
     * @throws EimSchemaException if the record is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    public void applyRecord(EimRecord record,
                            Item item)
        throws EimSchemaException {
        if (record.getNamespace() != namespace)
            throw new IllegalArgumentException("Record namespace " + record.getNamespace() + " does not match " + namespace);

        if (record.isDeleted()) {
            applyDeletion(record, item);
            return;
        }

        for (EimRecordField field : record.getFields()) {
            applyField(field, item);
        }
    }

    /**
     * Deletes the data associated with the record.
     *
     * @throws EimSchemaException if deletion is not allowed for this
     * record type or if deletion cannot otherwise be processed.
     */
    protected abstract void applyDeletion(EimRecord record,
                                          Item item)
        throws EimSchemaException;

    /**
     * Copies the data from the given record field into the item.
     *
     * If the field is not part of the subclass' schema, the field
     * should be handled with
     * {@link applyUnknownField(EimRecordField, Item)}.
     *
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected abstract void applyField(EimRecordField field,
                                       Item item)
        throws EimSchemaException;

    /**
     * Copies the field into an attribute of the item in the record's
     * namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * cannot be applied to the item 
     */
    protected void applyUnknownField(EimRecordField field,
                                     Item item)
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
            item.addAttribute(new DateAttribute(qn, value.getTime()));
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

    /**
     * Adds a record field for each item attribute with the same
     * namespace as the record.
     */
    protected void addUnknownFields(EimRecord record,
                                    Item item) {
        Map<String, Attribute> attrs =
            item.getAttributes(record.getNamespace());
        for (Attribute attr : attrs.values()) {
            if (attr instanceof BinaryAttribute) {
                InputStream value = ((BinaryAttribute)attr).getInputStream();
                record.addField(new BlobField(attr.getName(), value));
            } else if (attr instanceof DateAttribute) {
                Date value = ((DateAttribute)attr).getValue();
                record.addField(new DateTimeField(attr.getName(), value));
            } else if (attr instanceof DecimalAttribute) {
                BigDecimal value = ((DecimalAttribute)attr).getValue();
                record.addField(new DecimalField(attr.getName(), value));
            } else if (attr instanceof IntegerAttribute) {
                Long value = ((IntegerAttribute)attr).getValue();
                record.addField(new IntegerField(attr.getName(), new Integer(value.intValue())));
            } else if (attr instanceof StringAttribute) {
                String value = ((StringAttribute)attr).getValue();
                record.addField(new TextField(attr.getName(), value));
            } else if (attr instanceof TextAttribute) {
                Reader value = ((TextAttribute)attr).getReader();
                record.addField(new ClobField(attr.getName(), value));
            } else {
                log.warn("Skipping attribute " + attr.getQName() + " of unknown type " + attr.getClass().getName());
            }
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

    /**
     * Validates and returns a clob field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected Reader validateClob(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof ClobField))
            throw new EimValidationException("Field " + field.getName() + " is not a clob field");
        Reader value = ((ClobField)field).getClob();
        return value;
    }

    /**
     * Validates and returns a datetime field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected Calendar validateDateTime(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof DateTimeField))
            throw new EimValidationException("Field " + field.getName() + " is not a datetime field");
        Calendar value = ((DateTimeField)field).getCalendar();
        return value;
    }

    /**
     * Validates and returns a decimal field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected BigDecimal validateDecimal(EimRecordField field,
                                         int numDigits,
                                         int numDecimalPlaces)
        throws EimValidationException {
        if (! (field instanceof DecimalField))
            throw new EimValidationException("Field " + field.getName() + " is not a decimal field");
        BigDecimal value = ((DecimalField)field).getDecimal();
        if (numDigits > 0) {
            if (value.precision() != numDigits)
                throw new EimValidationException("Field " + field.getName() + " decimal value has " + value.precision() + " digits which is not the same as the specified number " + numDigits);
        }
        if (numDecimalPlaces > 0) {
            if (value.scale() != numDecimalPlaces)
                throw new EimValidationException("Field " + field.getName() + " decimal value has " + value.scale() + " decimal places which is not the same as the specified number " + numDecimalPlaces);
        }
        return value;
    }

    /**
     * Validates and returns a text field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected String validateText(EimRecordField field,
                                 int maxLength)
        throws EimValidationException {
        if (! (field instanceof TextField))
            throw new EimValidationException("Field " + field.getName() + " is not a text field");
        String value = ((TextField)field).getText();
        if (maxLength > 0) {
            try {
                int len = value.getBytes("UTF-8").length;
                if (len > maxLength)
                    throw new EimValidationException("Field " + field.getName() + " text value is " + len + " bytes which is greater than the allowable length of " + len + " bytes");
            } catch (UnsupportedEncodingException e) {
                // will never happen
            }
        }
        return value;
    }

    /**
     * Validates and returns a timestamp field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected Date validateTimeStamp(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof TimeStampField))
            throw new EimValidationException("Field " + field.getName() + " is not a timestamp field");
        Date value = ((TimeStampField)field).getTimeStamp();
        return value;
    }

    /**
     * Validates and returns a integer field value.
     *
     * @throws EimValidationException if the value is invalid
     */
    protected Integer validateInteger(EimRecordField field)
        throws EimValidationException {
        if (! (field instanceof IntegerField))
            throw new EimValidationException("Field " + field.getName() + " is not a integer field");
        Integer value = ((IntegerField)field).getInteger();
        return value;
    }
}
