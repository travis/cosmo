/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.schema.modifiedby;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecord;
import org.osaf.cosmo.eim.EimRecordField;
import org.osaf.cosmo.eim.schema.BaseItemApplicator;
import org.osaf.cosmo.eim.schema.EimFieldValidator;
import org.osaf.cosmo.eim.schema.EimSchemaException;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.Item;

/**
 * Applies modifiedBy records to content items.
 *
 * Modifiedby is a special type of record that is not processed in the
 * standard field-per-attribute fashion. It represents the fact that a
 * user made a change to an item at some point in the past and is used
 * to update a content item's lastModifiedBy property if the record
 * represents a change that was made subsequent to the last stored
 * change. In other words, if a modifiedby record shows that a change
 * was made at point X in time, but we have already stored updates
 * made after X in time, then there is no need to apply the record.
 *
 * Deleted modifiedby records are used by the client and may be sent
 * to the server. They are not useful to the server and are ignored.
 * 
 * @see ContentItem
 */
public class ModifiedByApplicator extends BaseItemApplicator
    implements ModifiedByConstants {
    private static final Log log =
        LogFactory.getLog(ModifiedByApplicator.class);

    /** */
    public ModifiedByApplicator(Item item) {
        super(PREFIX_MODIFIEDBY, NS_MODIFIEDBY, item);
        if (! (item instanceof ContentItem))
            throw new IllegalArgumentException("item " + item.getUid() + " not a content item");
    }

    /**
     * Override the superclass method to read the timestamp and userid
     * key fields and update the item's lastModifiedBy if the record
     * timestamp is more recent than the item's modifiedDate.
     */
    @Override
    public void applyRecord(EimRecord record)
        throws EimSchemaException {
        if (record.getNamespace() == null ||
            ! record.getNamespace().equals(NS_MODIFIEDBY))
            throw new IllegalArgumentException("Record namespace " + record.getNamespace() + " does not match " + NS_MODIFIEDBY);
            
        if (record.isDeleted()) {
            applyDeletion(record);
            return;
        }

        if (log.isDebugEnabled())
            log.debug("applying record " + NS_MODIFIEDBY);

        Date timestamp = null;
        String userid = null;
        Integer action = null;

        for (EimRecordField field : record.getKey().getFields()) {
            if (field.getName().equals(FIELD_UUID))
                ; // skip
            else if (field.getName().equals(FIELD_TIMESTAMP))
                timestamp = EimFieldValidator.validateTimeStamp(field);
            else if (field.getName().equals(FIELD_USERID))
                userid = EimFieldValidator.validateText(field, MAXLEN_USERID);
            else if (field.getName().equals(FIELD_ACTION)) {
                action = EimFieldValidator.validateInteger(field);
                if (! ContentItem.Action.validate(action))
                    throw new EimValidationException("invalid last modification action " + action);
            }
            else
                throw new EimSchemaException("Unknown key field " + field.getName());
        }

        if (timestamp == null)
            throw new EimValidationException(FIELD_TIMESTAMP + " field required for " + getNamespace() + " record");
        if (userid == null)
            throw new EimValidationException(FIELD_USERID + " field required for " + getNamespace() + " record");
        if (action == null)
            throw new EimValidationException(FIELD_ACTION + " field required for " + getNamespace() + " record");

        ContentItem contentItem = (ContentItem) getItem();
        //        log.debug("checking client timestamp " + timestamp + " against item client modified date " + contentItem.getClientModifiedDate());
        if (contentItem.getClientModifiedDate() == null ||
            timestamp.after(contentItem.getClientModifiedDate())) {
            //            log.debug("updating lastModifiedBy with " + userid);
            //            log.debug("updating clientModifiedDate with " + timestamp);
            contentItem.setLastModifiedBy(userid);
            contentItem.setClientModifiedDate(timestamp);
            contentItem.setLastModification(action);
        }
    }

    /**
     * Does nothing, since deleted records are ignored.
     */
    protected void applyDeletion(EimRecord record)
        throws EimSchemaException {}

    /**
     * Throws an exception if called, since modifiedBy records contain
     * only key fields.
     */
    protected void applyField(EimRecordField field)
        throws EimSchemaException {
        throw new EimSchemaException("Unknown field " + field.getName());
    }
}
