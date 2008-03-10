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
package org.osaf.cosmo.atom.processor;

import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.eim.EimRecordSet;
import org.osaf.cosmo.eim.schema.EimValidationException;
import org.osaf.cosmo.eim.schema.ItemTranslator;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;

/**
 * A base class for implementations of {@link ContentProcessor} that
 * work with EIM recordsets.
 *
 * @see EimRecordSet
 * @see NoteItem
 */
public abstract class BaseEimProcessor extends BaseContentProcessor {
    private static final Log log = LogFactory.getLog(BaseEimProcessor.class);

    private EntityFactory entityFactory = null;
    
    public BaseEimProcessor(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    // ContentProcessor methods

    /**
     * Process a content body describing an item to be added as a
     * child of the given collection.
     *
     * @param content the content
     * @param collection the parent of the new item
     * @throws ValidationException if the content is not a valid
     * representation of an item
     * @throws ProcessorException
     * @return the new item
     */
    public NoteItem processCreation(Reader content,
                                    CollectionItem collection)
        throws ValidationException, ProcessorException {
        EimRecordSet recordset = readRecordSet(content);

        try {
            NoteItem child = createChild(collection, recordset);
            ItemTranslator translator = new ItemTranslator(child);
            translator.applyRecords(recordset);
            return child;
        } catch (EimValidationException e) {
            throw new ValidationException("Invalid recordset", e);
        } catch (Exception e) {
            throw new ProcessorException("Unable to apply recordset", e);
        }
    }

    /**
     * Process an EIMML content body describing changes to an item.
     *
     * @param content the content
     * @param item the item which the content represents
     * @throws ValidationException if the content does not represent a
     * valid EIM record set or if the record set's data does not
     * validate against the EIM schemas
     * @throws ProcessorException
     */
    public void processContent(Reader content,
                               NoteItem item)
        throws ValidationException, ProcessorException {
        EimRecordSet recordset = readRecordSet(content);

        try {
            ItemTranslator translator = new ItemTranslator(item);
            translator.applyRecords(recordset);
        } catch (EimValidationException e) {
            throw new ValidationException("Invalid recordset", e);
        } catch (Exception e) {
            throw new ProcessorException("Unable to apply recordset", e);
        }
    }

    // our methods

    /**
     * Converts the content body into a valid EIM record set.
     *
     * @throws ValidationException if the content does not represent a
     * valid EIM record set
     * @throws ProcessorException
     */
    protected abstract EimRecordSet readRecordSet(Reader content)
        throws ValidationException, ProcessorException;

    private NoteItem createChild(CollectionItem collection,
                                 EimRecordSet recordset)
        throws ValidationException {
        NoteItem child = entityFactory.createNote();

        child.setUid(recordset.getUuid());
        child.setIcalUid(child.getUid());
        child.setOwner(collection.getOwner());

        // if the item is a modification, relate it to the item it
        // modifies
        if (child.getUid().
            indexOf(ModificationUid.RECURRENCEID_DELIMITER) > 0) {
            ModificationUid modUid = toModificationUid(child.getUid());
            String masterUid = modUid.getParentUid();

            // modification should inherit icaluid
            child.setIcalUid(null);
            
            NoteItem master  = null;
            for (Item sibling : collection.getChildren()) {
                if (sibling.getUid().equals(masterUid)) {
                    if (! (sibling instanceof NoteItem))
                        throw new ValidationException("Modification master item " + sibling.getUid() + " is not a note item");
                    master = (NoteItem) sibling;
                    break;
                }
            }

            if (master != null)
                child.setModifies(master);
            else
                throw new ValidationException("Master item not found for " + child.getUid());
        }     

        return child;
    }

    private ModificationUid toModificationUid(String uid)
        throws ValidationException {
        try {
            return new ModificationUid(uid);
        } catch (ModelValidationException e) {
            throw new ValidationException("Invalid modification uid " + uid);
        }
    }
}
