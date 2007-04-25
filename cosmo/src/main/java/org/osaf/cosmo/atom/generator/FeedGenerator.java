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
package org.osaf.cosmo.atom.generator;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Entry;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * An interface for classes that generate Atom feeds and entries
 * representing Cosmo collections and items.
 *
 * @see Entry
 * @see Feed
 * @see CollectionItem
 * @see NoteItem
 */
public interface FeedGenerator {

    /**
     * Generates an Atom feed containing entries for each child item
     * of the collection.
     *
     * @param collection the collection on which the feed is based
     * @throws GeneratorException
     */
    public Feed generateFeed(CollectionItem item)
        throws GeneratorException;

    /**
     * Generates an Atom entry representing the item.
     *
     * @param item the item which the entry describes
     * @throws GeneratorException
     */
    public Entry generateEntry(NoteItem item)
        throws GeneratorException;
}
