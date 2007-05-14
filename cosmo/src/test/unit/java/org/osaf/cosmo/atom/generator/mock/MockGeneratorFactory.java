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
import org.osaf.cosmo.atom.generator.ServiceGenerator;
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
    private Set<String> projections;
    private Set<String> formats;
    private boolean failureMode;

    public MockGeneratorFactory(Abdera abdera) {
        this.abdera = abdera;
        this.projections = new HashSet<String>();
        this.formats = new HashSet<String>();
        this.failureMode = false;
    }

    // GeneratorFactory methods

    /**
     * Creates an instance of <code>Generator</code>.
     *
     * @param serviceLocator the service locator from which service
     * URLs are calculated
     * @return the service generator
     */
    public ServiceGenerator
        createServiceGenerator(ServiceLocator serviceLocator) {
        return new MockServiceGenerator(this, serviceLocator);
    }

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
        // projection is required
        if (projection == null || ! projections.contains(projection))
            throw new UnsupportedProjectionException(projection);
        if (format != null && ! formats.contains(format))
            throw new UnsupportedFormatException(format);
        return new MockFeedGenerator(this, projection, format, serviceLocator);
    }

    // our methods

    public Abdera getAbdera() {
        return abdera;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public Set<String> getProjections() {
        return projections;
    }

    public Set<String> getFormats() {
        return formats;
    }

    public boolean isFailureMode() {
        return failureMode;
    }

    public void setFailureMode(boolean mode) {
        failureMode = mode;
    }
}
