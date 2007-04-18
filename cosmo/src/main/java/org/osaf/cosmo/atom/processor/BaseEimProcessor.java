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

    /**
     * Converts the content body into a valid EIM record set.
     *
     * @throws ValidationException if the content does not represent a
     * valid EIM record set
     * @throws ProcessorException
     */
    protected abstract EimRecordSet readRecordSet(Reader content)
        throws ValidationException, ProcessorException;
}
