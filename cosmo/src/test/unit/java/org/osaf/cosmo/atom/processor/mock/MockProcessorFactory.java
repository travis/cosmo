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
import org.osaf.cosmo.atom.processor.UnsupportedContentTypeException;

/**
 * Mock implementation of {@link ProcessorFactory}.
 *
 * @see MockContentProcessor
 */
public class MockProcessorFactory implements ProcessorFactory {
    private static final Log log =
        LogFactory.getLog(MockProcessorFactory.class);

    private Set<String> contentTypes;
    private boolean failureMode;
    private boolean validationErrorMode;

    public MockProcessorFactory() {
        contentTypes = new HashSet<String>();
        failureMode = false;
        validationErrorMode = false;
    }

    // ProcessorFactory methods

    /**
     * Returns an array of content types supported by this processor.
     */
    public String[] getSupportedContentTypes() {
        return (String[]) contentTypes.toArray(new String[0]);
    }

    /**
     * Creates an instance of <code>MockContentProcessor</code>.
     *
     * @param type the type of content to process
     * @return the content processor
     * @throws UnsupportedContentTypeException if the given type is not
     * supported
     */
    public ContentProcessor createProcessor(String type)
        throws UnsupportedContentTypeException {
        if (type == null || ! contentTypes.contains(type))
            throw new UnsupportedContentTypeException(type);
        return new MockContentProcessor(this);
    }

    // our methods

    public Set<String> getContentTypes() {
        return contentTypes;
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
