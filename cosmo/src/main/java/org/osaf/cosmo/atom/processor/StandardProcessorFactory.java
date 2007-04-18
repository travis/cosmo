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

import org.osaf.cosmo.eim.eimml.EimmlConstants;
import org.osaf.cosmo.eim.json.JsonConstants;

/**
 * Standard implementation of {@link ProcessorFactory}.
 *
 * @see ContentProcessor
 */
public class StandardProcessorFactory
    implements ProcessorFactory, JsonConstants, EimmlConstants {
    private static final Log log =
        LogFactory.getLog(StandardProcessorFactory.class);

    // ProcessorFactory methods

    /**
     * Creates an instance of <code>ContentProcessor</code>. The type of
     * processor is chosen based on one of the following media types:
     * <dl>
     * <dt>{@link JsonConstants#MEDIA_TYPE_EIM_JSON}</dt>
     * <dd>{@link JsonProcessor}</dd>
     * <dt>{@link EimmlConstants#MEDIA_TYPE_EIMML}</dt>
     * <dd>{@link EimmlProcessor}</dd>
     * </dl>
     * <p>
     *
     * @param mediaType the media type of the content to process
     * @return the entry processor, or null if no processor is
     * supported for the named media type
     */
    public ContentProcessor createProcessor(String mediaType)
        throws UnsupportedMediaTypeException {
        if (mediaType != null) {
            if (mediaType.equals(MEDIA_TYPE_EIM_JSON))
                return new JsonProcessor();
            if (mediaType.equals(MEDIA_TYPE_EIMML))
                return new EimmlProcessor();
        }
        throw new UnsupportedMediaTypeException(mediaType);
    }
}
