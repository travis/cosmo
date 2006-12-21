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

import org.osaf.cosmo.eim.DateTimeField;
import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.TimeStampField;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Stamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Translates item records to <code>ContentItem</code>s.
 * <p>
 * Implements the following schema:
 * <p>
 * TBD
 */
public class ItemTranslator extends EimSchemaTranslator {
    private static final Log log = LogFactory.getLog(ItemTranslator.class);

    /** */
    public static final String FIELD_TITLE = "title";
    /** */
    public static final int MAXLEN_TITLE = 256;
    /** */
    public static final String FIELD_TRIAGE_STATUS = "triageStatus";
    /** */
    public static final int MAXLEN_TRIAGE_STATUS = 256;
    /** */
    public static final String FIELD_TRIAGE_STATUS_CHANGED =
        "triageStatusChanged";
    /** */
    public static final int DIGITS_TRIAGE_STATUS_CHANGED = 11;
    /** */
    public static final int DEC_TRIAGE_STATUS_CHANGED = 2;
    /** */
    public static final String FIELD_LAST_MODIFIED_BY = "lastModifiedBy";
    /** */
    public static final int MAXLEN_LAST_MODIFIED_BY = 256;
    /** */
    public static final String FIELD_CREATED_ON = "createdOn";

    /** */
    public ItemTranslator() {
        super(PREFIX_ITEM, NS_ITEM);
    }

    /**
     * Deactivates the item associated with the record.
     */
    protected void applyDeletion(Item item)
        throws EimSchemaException {
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("Item is not a content item");
        ContentItem c = (ContentItem) item;

        c.setIsActive(false);
    }

    /**
     * Copies the data from the given record field into the item.
     *
     * @throws IllegalArgumentException if the item is not a content
     * item
     * @throws EimSchemaException if the field is improperly
     * constructed or cannot otherwise be applied to the item 
     */
    protected void applyField(EimRecordField field,
                              Item item)
        throws EimSchemaException {
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("Item is not a content item");
        ContentItem ci = (ContentItem) item;

        if (field.getName().equals(FIELD_TITLE)) {
            String value = validateText(field, MAXLEN_TITLE);
            ci.setDisplayName(value);
        } else if (field.getName().equals(FIELD_TRIAGE_STATUS)) {
            String value = validateText(field, MAXLEN_TRIAGE_STATUS);
            ci.setTriageStatus(value);
        } else if (field.getName().equals(FIELD_TRIAGE_STATUS_CHANGED)) {
            BigDecimal value =
                validateDecimal(field, DIGITS_TRIAGE_STATUS_CHANGED,
                                DEC_TRIAGE_STATUS_CHANGED);
            ci.setTriageStatusUpdated(value);
        } else if (field.getName().equals(FIELD_LAST_MODIFIED_BY)) {
            String value = validateText(field, MAXLEN_LAST_MODIFIED_BY);
            ci.setLastModifiedBy(value);
        } else if (field.getName().equals(FIELD_CREATED_ON)) {
            Date value = validateTimeStamp(field);
            ci.setCreationDate(value);
        } else {
            applyUnknownField(field, item);
        }
    }

    /**
     * Adds record fields for each applicable item property.
     *
     * @throws IllegalArgumentException if the item is not a content
     * item
     */
    protected void addFields(EimRecord record,
                             Item item) {
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("Item is not a content item");
        ContentItem ci = (ContentItem) item;

        record.addKeyField(new TextField(FIELD_UUID, item.getUid()));
        record.addField(new TextField(FIELD_TITLE, ci.getDisplayName()));
        record.addField(new TextField(FIELD_TRIAGE_STATUS,
                                      ci.getTriageStatus()));
        record.addField(new DecimalField(FIELD_TRIAGE_STATUS_CHANGED,
                                         ci.getTriageStatusUpdated(),
                                         DIGITS_TRIAGE_STATUS_CHANGED,
                                         DEC_TRIAGE_STATUS_CHANGED));
        record.addField(new TextField(FIELD_LAST_MODIFIED_BY,
                                      ci.getLastModifiedBy()));
        record.addField(new TimeStampField(FIELD_CREATED_ON,
                                           ci.getCreationDate()));
        addUnknownFields(record, item);
    }

    protected void addFields(EimRecord record,
                             Stamp stamp) {
        throw new RuntimeException("ItemTranslator does not translate stamps");
    }
}
