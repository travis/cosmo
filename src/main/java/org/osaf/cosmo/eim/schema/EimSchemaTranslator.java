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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;

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
import org.osaf.cosmo.model.DateAttribute;
import org.osaf.cosmo.model.DecimalAttribute;
import org.osaf.cosmo.model.IntegerAttribute;
import org.osaf.cosmo.model.StringAttribute;
import org.osaf.cosmo.model.TextAttribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.lang.StringUtils;
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
     * If the record is marked deleted, the item is set inactive.
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
            item.setIsActive(false);
            return;
        }

        for (EimRecordField field : record.getFields()) {
            applyField(field, item);
        }
    }

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
     * Copies the field into an attribute of the item with this
     * translator's namespace.
     *
     * @throws EimSchemaException if the field is improperly
     * cannot be applied to the item 
     */
    protected void applyUnknownField(EimRecordField field,
                                     Item item)
        throws EimSchemaException {
        QName qn = new QName(namespace, field.getName());
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
     * Creates an EIM record containing the data from the given item.
     * <p>
     * Sets the record's prefix and namespace.
     * <p>
     * If the item is inactive, the record is marked deleted.
     * <p>
     * If the item is active, then {@link #addFields(EimRecord, Item)}
     * and {@link #addUnknownFields(EimRecord, Item)} are called.
     */
    public EimRecord createRecord(Item item) {
        EimRecord record = new EimRecord(prefix, namespace);

        if (! item.getIsActive()) {
            record.setDeleted(true);
        } else {
            addFields(record, item);
            addUnknownFields(record, item);
        }

        return record;
    }

    /**
     * Creates an EIM record containing the data from the given stamp.
     * <p>
     * Sets the record's prefix and namespace.
     * <p>
     * Calls {@link #addFields(EimRecord, Stamp)}
     * and {@link #addUnknownFields(EimRecord, Stamp)}.
     */
    public EimRecord createRecord(Stamp stamp) {
        EimRecord record = new EimRecord(prefix, namespace);

        addFields(record, stamp);
        addUnknownFields(record, stamp.getItem());

        return record;
    }

    /**
     * Adds record fields for each applicable item property.
     *
     * @throws EimSchemaException if fields cannot be added to
     * the record for some reason
     */
    protected abstract void addFields(EimRecord record,
                                      Item item);

    /**
     * Adds record fields for each applicable stamp property.
     */
    protected abstract void addFields(EimRecord record,
                                      Stamp stamp);

    /**
     * Adds a record field for each item attribute with the same
     * namespace as this translator.
     */
    protected void addUnknownFields(EimRecord record,
                                    Item item) {
        Map<String, Attribute> attrs = item.getAttributes(namespace);
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

    /** */
    public List<Recur> parseICalRecurs(String text)
        throws EimValidationException {
        ArrayList<Recur> recurs = new ArrayList<Recur>();
        for (String value : text.split(","))
            recurs.add(parseICalRecur(value));
        return recurs;
    }

    /** */
    public DateList parseICalDates(String text)
        throws EimValidationException {
        DateList dates = new DateList(Value.DATE_TIME);
        for (String value : text.split(","))
            dates.add(parseICalDate(value));
        return dates;
    }

    /** */
    public Recur parseICalRecur(String text)
        throws EimValidationException {
        try {
            return new Recur(text);
        } catch (ParseException e) {
            throw new EimValidationException("Invalid iCalendar recur " + text);
        }
    }

    /** */
    public DateTime parseICalDate(String text)
        throws EimValidationException {
        try {
            return new DateTime(text);
        } catch (ParseException e) {
            throw new EimValidationException("Invalid iCalendar datetime " + text);
        }
    }

    /** */
    public static String formatRecurs(List<Recur> recurs) {
        if (! recurs.iterator().hasNext())
            return null;
        return StringUtils.join(recurs.iterator(), ",");
    }

    /** */
    public static String formatICalDates(DateList dates) {
        if (! dates.iterator().hasNext())
            return null;
        return dates.toString();
    }

    /** */
    public static String formatICalDate(net.fortuna.ical4j.model.Date date) {
        if (date == null)
            return null;
        return date.toString();
    }
}
