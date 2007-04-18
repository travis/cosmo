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

/**
 * An interface for factories that creates {@link ContentProcessor}
 * instances.
 */
public interface ProcessorFactory {

    /**
     * Creates an instance of <code>ContentProcessor</code> based on
     * the given media type.
     *
     * @param mediaType the media type of the content to process
     * @return the entry processor, or null if no processor is
     * supported for the named media type
     */
    public ContentProcessor createProcessor(String mediaType)
        throws UnsupportedMediaTypeException;
}
