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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.json.JsonConstants;
import org.osaf.cosmo.model.EntityFactory;

/**
 * Standard implementation of {@link ProcessorFactory}.
 *
 * @see ContentProcessor
 */
public class StandardProcessorFactory
    implements ProcessorFactory, AtomConstants, JsonConstants, EimmlConstants {
    private static final Log log =
        LogFactory.getLog(StandardProcessorFactory.class);
    private static final String[] CONTENT_TYPES = {
        MEDIA_TYPE_EIM_JSON,
        MEDIA_TYPE_EIMML,
        MEDIA_TYPE_XHTML
    };

    private EntityFactory entityFactory = null;
    
    // ProcessorFactory methods

    /**
     * Returns an array of content types supported by this processor.
     */
    public String[] getSupportedContentTypes() {
        return CONTENT_TYPES;
    }

    /**
     * Creates an instance of <code>ContentProcessor</code>. The type of
     * processor is chosen based on one of the following media types:
     * <dl>
     * <dt>{@link JsonConstants#MEDIA_TYPE_EIM_JSON}</dt>
     * <dd>{@link JsonProcessor}</dd>
     * <dt>{@link EimmlConstants#MEDIA_TYPE_EIMML}</dt>
     * <dd>{@link EimmlProcessor}</dd>
     * <dt>{@link AtomConstants#MEDIA_TYPE_XHTML}</dt>
     * <dd>{@link HCalendarProcessor}</dt>
     * </dl>
     * <p>
     *
     * @param type the type of content to process
     * @return the content processor
     * @throws UnsupportedContentTypeException if the given type is not
     * supported
     */
    public ContentProcessor createProcessor(String type)
        throws UnsupportedContentTypeException {
        if (type != null) {
            if (type.equals(MEDIA_TYPE_EIM_JSON))
                return new JsonProcessor(entityFactory);
            if (type.equals(MEDIA_TYPE_EIMML))
                return new EimmlProcessor(entityFactory);
            if (type.equals(MEDIA_TYPE_XHTML))
                return new HCalendarProcessor(entityFactory);
        }
        throw new UnsupportedContentTypeException(type);
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    
}
