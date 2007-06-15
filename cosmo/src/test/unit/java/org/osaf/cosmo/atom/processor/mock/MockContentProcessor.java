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
package org.osaf.cosmo.atom.processor.mock;

import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.processor.BaseContentProcessor;
import org.osaf.cosmo.atom.processor.ProcessorException;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * A mock implementation of {@link ContentProcessor}.
 */
public class MockContentProcessor extends BaseContentProcessor {
    private static final Log log =
        LogFactory.getLog(MockContentProcessor.class);

    private MockProcessorFactory factory;

    public MockContentProcessor(MockProcessorFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns a dummy item. The given content is used to populate the
     * item's name and UID.
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
        if (factory.isValidationErrorMode())
            throw new ValidationException("Validation error mode");
        if (factory.isFailureMode())
            throw new ProcessorException("Failure mode");

        try {
            String uid = IOUtils.toString(content);

            NoteItem child = new NoteItem();
            child.setUid(uid);
            child.setName(uid);
            child.setOwner(collection.getOwner());

            return child;
        } catch (Exception e) {
            throw new ProcessorException("Cannot create child item", e);
        }
    }

    /**
     * Does nothing.
     *
     * @param content the content
     * @param item the item which the content represents
     *
     * @throws ValidationException if the processor factor is in
     * validation error mode
     * @throws ProcessorException if the processor factory is in
     * failure mode
     */
    public void processContent(Reader content,
                               NoteItem item)
        throws ValidationException, ProcessorException {
        if (factory.isValidationErrorMode())
            throw new ValidationException("Validation error mode");
        if (factory.isFailureMode())
            throw new ProcessorException("Failure mode");

        try {
            String uid = IOUtils.toString(content);

            log.error("updating item " + item.getUid() + " with new uid " + uid);

            item.setUid(uid);
        } catch (Exception e) {
            throw new ProcessorException("Cannot create child item", e);
        }
    }
}
