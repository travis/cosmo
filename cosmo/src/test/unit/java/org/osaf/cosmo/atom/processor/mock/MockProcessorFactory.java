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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.processor.ContentProcessor;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.UnsupportedMediaTypeException;

/**
 * Mock implementation of {@link ProcessorFactory}.
 *
 * @see MockContentProcessor
 */
public class MockProcessorFactory implements ProcessorFactory {
    private static final Log log =
        LogFactory.getLog(MockProcessorFactory.class);

    private Set<String> mediaTypes;
    private boolean failureMode;
    private boolean validationErrorMode;

    public MockProcessorFactory() {
        mediaTypes = new HashSet<String>();
        failureMode = false;
        validationErrorMode = false;
    }

    // ProcessorFactory methods

    /**
     * Creates an instance of <code>MockContentProcessor</code>.
     *
     * @param mediaType the media type of the content to process
     * @return the entry processor, or null if no processor is
     * supported for the named media type
     */
    public ContentProcessor createProcessor(String mediaType)
        throws UnsupportedMediaTypeException {
        if (mediaType == null || ! mediaTypes.contains(mediaType))
            throw new UnsupportedMediaTypeException(mediaType);
        return new MockContentProcessor(this);
    }

    // our methods

    public Set<String> getMediaTypes() {
        return mediaTypes;
    }

    public boolean isFailureMode() {
        return failureMode;
    }

    public void setFailureMode(boolean mode) {
        failureMode = mode;
    }

    public boolean isValidationErrorMode() {
        return validationErrorMode;
    }

    public void setValidationErrorMode(boolean mode) {
        validationErrorMode = mode;
    }
}
