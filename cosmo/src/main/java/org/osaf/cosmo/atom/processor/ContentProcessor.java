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

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * An interface for classes that process Atom content representing
 * Cosmo items.
 *
 * @see NoteItem
 */
public interface ContentProcessor {

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
        throws ValidationException, ProcessorException;

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
    public NoteItem processCreation(String content,
                                    CollectionItem collection)
        throws ValidationException, ProcessorException;

    /**
     * Process a content body describing changes to an item.
     *
     * @param content the content
     * @param item the item which the content represents
     * @throws ValidationException if the content is not a valid
     * representation of an item
     * @throws ProcessorException
     */
    public void processContent(Reader content,
                               NoteItem item)
        throws ValidationException, ProcessorException;

    /**
     * Process a content body describing changes to an item.
     *
     * @param content the content
     * @param item the item which the content represents
     * @throws ValidationException if the content is not a valid
     * representation of an item
     * @throws ProcessorException
     */
    public void processContent(String content,
                               NoteItem item)
        throws ValidationException, ProcessorException;
}
