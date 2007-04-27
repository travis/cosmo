/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import javax.activation.MimeTypeParseException;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * A class that generates basic Atom feeds whose content are formatted
 * in HTML/hCalendar.
 *
 * @see Feed
 * @see CollectionItem
 */
public class BasicFeedGenerator extends BaseFeedGenerator {
    private static final Log log = LogFactory.getLog(BasicFeedGenerator.class);

    /** */
    public BasicFeedGenerator(Factory abderaFactory,
                              ContentFactory contentFactory,
                              ServiceLocator seviceLocator) {
        super(abderaFactory, contentFactory, seviceLocator);
    }

    /**
     * Sets HTML entry content and plain text entry summary.
     */
    protected void setEntryContent(Entry entry,
                                   NoteItem item)
        throws GeneratorException {
        ContentBean content = null;
        try {
            content = getContentFactory().createContent(FORMAT_HTML, item);
            entry.setContent(content.getValue(), content.getMediaType());
        } catch (MimeTypeParseException e) {
            throw new GeneratorException("Attempted to set invalid content media type " + content.getMediaType(), e);
        }

        ContentBean summary =
            getContentFactory().createContent(FORMAT_TEXT, item);
        entry.setSummary(summary.getValue());
    }

    /**
     * Returns {@link AtomConstants#PROJECTION_BASIC}.
     */
    protected String getProjection() {
        return PROJECTION_BASIC;
    }

    /**
     * Returns the IRI of the given item without path info.
     *
     * @param item the item
     */
    protected String selfIri(Item item) {
        return selfIri(item, false);
    }
}
