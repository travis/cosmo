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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.CollectionItem;

/**
 * An interface for classes that generate a collection's ticket feed.
 *
 * @see Entry
 * @see Feed
 * @see CollectionItem
 */
public interface TicketsFeedGenerator {

    /**
     * Generates an Atom feed containing entries for a collection's
     * tickets.
     *
     * @param collection the collection
     * @throws GeneratorException
     */
    public Feed generateFeed(CollectionItem collection)
        throws GeneratorException;

    /**
     * Generates an Atom entry representing a specific 
     * ticket.
     *
     * @param ticket the ticket
     * @throws GeneratorException
     */
    public Entry generateEntry(CollectionItem collection, Ticket ticket)
        throws GeneratorException;
}
