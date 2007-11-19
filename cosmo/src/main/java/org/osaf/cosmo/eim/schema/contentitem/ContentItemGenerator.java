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
package org.osaf.cosmo.eim.schema.contentitem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.eim.DecimalField;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.IntegerField;
import org.osaf.cosmo.eim.TextField;
import org.osaf.cosmo.eim.schema.BaseItemGenerator;
import org.osaf.cosmo.eim.schema.text.TriageStatusFormat;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

/**
 * Generates EIM records from content items.
 *
 * @see ContentItem
 */
public class ContentItemGenerator extends BaseItemGenerator
    implements ContentItemConstants {
    private static final Log log =
        LogFactory.getLog(ContentItemGenerator.class);

    /** */
    public ContentItemGenerator(Item item) {
        super(PREFIX_ITEM, NS_ITEM, item);
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("item " + item.getUid() + " not a content item");
    }

    /**
     * Copies contentItem properties and attributes into a contentItem
     * record.
     */
    public List<EimRecord> generateRecords() {
        ContentItem contentItem = (ContentItem) getItem();

        EimRecord record = new EimRecord(getPrefix(), getNamespace());

        record.addKeyField(new TextField(FIELD_UUID, contentItem.getUid()));

        if(isMissingAttribute("displayName")) {
            record.addField(generateMissingField(new TextField(FIELD_TITLE, null)));
        } else {
            record.addField(new TextField(FIELD_TITLE,
                    contentItem.getDisplayName()));
        }

        
        String ts = TriageStatusFormat.getInstance(getItem().getFactory()).
            format(contentItem.getTriageStatus());
        
        // missing TriageStatus ends up as empty string instead of null
        if(isModification() && "".equals(ts)) {
            record.addField(generateMissingField(new TextField(FIELD_TRIAGE, null)));
        } else {
            record.addField(new TextField(FIELD_TRIAGE, ts));
        }
        
        if(isMissingAttribute("sent")) {
            record.addField(generateMissingField(new IntegerField(FIELD_HAS_BEEN_SENT, null)));
        } else {
            boolean sent = BooleanUtils.isTrue(contentItem.getSent());
            record.addField(new IntegerField(FIELD_HAS_BEEN_SENT, sent)); 
        }
        
        if(isMissingAttribute("needsReply")) {
            record.addField(generateMissingField(new IntegerField(FIELD_NEEDS_REPLY, null)));
        } else {
            boolean needsReply = BooleanUtils.isTrue(contentItem.getNeedsReply());
            record.addField(new IntegerField(FIELD_NEEDS_REPLY, needsReply));
        }
        
        Date d = contentItem.getClientCreationDate();
        BigDecimal createdOn = d != null ?
            new BigDecimal(d.getTime() / 1000) :
            null;
        record.addField(new DecimalField(FIELD_CREATED_ON, createdOn,
                                         DIGITS_TIMESTAMP, DEC_TIMESTAMP));

        record.addFields(generateUnknownFields());

        ArrayList<EimRecord> records = new ArrayList<EimRecord>();
        records.add(record);

        return records;
    }
}
