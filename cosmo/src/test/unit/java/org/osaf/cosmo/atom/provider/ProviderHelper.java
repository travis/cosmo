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
package org.osaf.cosmo.atom.provider;

import java.io.IOException;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.DefaultServiceContext;
import org.apache.abdera.protocol.server.provider.RequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.MockHelper;
import org.osaf.cosmo.atom.generator.ContentFactory;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.mock.MockGeneratorFactory;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.mock.MockProcessorFactory;
import org.osaf.cosmo.atom.provider.mock.MockCollectionRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockItemRequestContext;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;

/**
 * A utility class that provides the dependent objects required
 * by a {@link Provider} to execute its methods. These dependencies
 * are mock objects intended for use with unit tests. The helper also
 * provides convenience methods for setting up test data in the mock
 * storage layer.
 */
public class ProviderHelper extends MockHelper {
    private static final Log log = LogFactory.getLog(ProviderHelper.class);

    private Abdera abdera;
    private MockGeneratorFactory generatorFactory;
    private MockProcessorFactory processorFactory;
    private ServiceContext serviceContext;

    public ProviderHelper() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        abdera = new Abdera();

        generatorFactory = new MockGeneratorFactory(abdera);
        processorFactory = new MockProcessorFactory();

        serviceContext = new DefaultServiceContext();
        serviceContext.init(abdera, null);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public Abdera getAbdera() {
        return abdera;
    }

    public GeneratorFactory getGeneratorFactory() {
        return generatorFactory;
    }

    public ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    public RequestContext createFeedRequestContext(CollectionItem collection,
                                                   String method) {
        return new MockCollectionRequestContext(serviceContext, collection,
                                                method);
    }

    public RequestContext createFeedRequestContext(CollectionItem collection,
                                                   String method,
                                                   String projection,
                                                   String format) {
        return new MockCollectionRequestContext(serviceContext, collection,
                                                method, projection, format);
    }

    public RequestContext createFeedRequestContext(String uid,
                                                   String method) {
        return new MockCollectionRequestContext(serviceContext, uid, method);
    }

    public RequestContext createEntryRequestContext(NoteItem item,
                                                    String method)
        throws IOException {
        MockItemRequestContext rc =
            new MockItemRequestContext(serviceContext, item, method);
        if (method.equals("PUT"))
            rc.setEntryContent(item);
        return rc;
    }

    public RequestContext createEntryRequestContext(String uid,
                                                    String method)
        throws IOException {
        return new MockItemRequestContext(serviceContext, uid, method);
    }

    public RequestContext createMediaRequestContext(NoteItem item,
                                                    String method)
        throws IOException {
        MockItemRequestContext rc =
            new MockItemRequestContext(serviceContext, item, method, true);
        if (method.equals("PUT"))
            rc.setMediaContent(item);
        return rc;
    }

    public RequestContext createMediaRequestContext(String uid,
                                                    String method)
        throws IOException {
        return new MockItemRequestContext(serviceContext, uid, method, true);
    }

    public void rememberProjection(String projection) {
        generatorFactory.getProjections().add(projection);
    }

    public void rememberFormat(String format) {
        generatorFactory.getFormats().add(format);
    }

    public void enableGeneratorFailure() {
        generatorFactory.setFailureMode(true);
    }

    public void rememberMediaType(String mediaType) {
        processorFactory.getMediaTypes().add(mediaType);
    }

    public void enableProcessorFailure() {
        processorFactory.setFailureMode(true);
    }

    public void enableProcessorValidationError() {
        processorFactory.setValidationErrorMode(true);
    }
}
