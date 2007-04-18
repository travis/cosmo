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
package org.osaf.cosmo.atom.generator.mock;

import java.util.HashSet;
import java.util.Set;

import org.apache.abdera.Abdera;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.FeedGenerator;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.UnsupportedFormatException;
import org.osaf.cosmo.atom.generator.UnsupportedProjectionException;
import org.osaf.cosmo.server.ServiceLocator;

/**
 * Mock implementation of <code>GeneratorFactory</code>.
 *
 * @see GeneratorFactory
 * @see FeedGenerator
 */
public class MockGeneratorFactory implements GeneratorFactory {
    private static final Log log =
        LogFactory.getLog(MockGeneratorFactory.class);

    private Abdera abdera;
    private Set projections;
    private Set formats;

    public MockGeneratorFactory(Abdera abdera) {
        this.abdera = abdera;
        this.projections = new HashSet();
        this.formats = new HashSet();
    }

    // GeneratorFactory methods

    /**
     * Creates an instance of <code>MockFeedGenerator</code> based on
     * the given projection and format. Projection is mandatory but
     * format is optional. The projection, and format if given, must
     * already be known by the factory.
     *
     * @param projection the projection name
     * @param format the format name
     * @param serviceLocator the service locator from which feed URLs
     * are calculated
     * @return the feed generator
     */
    public FeedGenerator createFeedGenerator(String projection,
                                             String format,
                                             ServiceLocator serviceLocator)
        throws UnsupportedProjectionException, UnsupportedFormatException {
        // projection is requried
        if (projection == null || ! projections.contains(projection))
            throw new UnsupportedProjectionException(projection);
        if (format != null && ! formats.contains(format))
            throw new UnsupportedFormatException(format);
        return new MockFeedGenerator(abdera, projection, format,
                                     serviceLocator);
    }

    // our methods

    public Set getProjections() {
        return projections;
    }

    public Set getFormats() {
        return formats;
    }
}
