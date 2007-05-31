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
package org.osaf.cosmo.atom;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.ServiceContext;
import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.MockHelper;
import org.osaf.cosmo.atom.generator.ContentFactory;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.mock.MockGeneratorFactory;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.mock.MockProcessorFactory;
import org.osaf.cosmo.atom.provider.StandardTargetResolver;
import org.osaf.cosmo.atom.provider.mock.BaseMockRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockCollectionRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockItemRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockProviderManager;
import org.osaf.cosmo.atom.provider.mock.MockServiceRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockSubscribedRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockSubscriptionRequestContext;
import org.osaf.cosmo.atom.servlet.StandardRequestHandlerManager;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * A utility class that provides the dependent objects required
 * by framework classes. These dependencies are mock objects intended
 * for use with unit tests. The helper also provides convenience
 * methods for setting up test data in the mock storage layer.
 */
public class AtomHelper extends MockHelper {
    private static final Log log = LogFactory.getLog(AtomHelper.class);

    private Abdera abdera;
    private MockGeneratorFactory generatorFactory;
    private MockProcessorFactory processorFactory;
    private StandardServiceContext serviceContext;

    public AtomHelper() {
        super();

        abdera = new Abdera();

        generatorFactory = new MockGeneratorFactory(abdera);
        processorFactory = new MockProcessorFactory();

        serviceContext = new StandardServiceContext();
        serviceContext.setAbdera(abdera);
        serviceContext.setProviderManager(new MockProviderManager());
        // XXX mock these up
        serviceContext.setRequestHandlerManager(new StandardRequestHandlerManager());
        serviceContext.setTargetResolver(new StandardTargetResolver());
        serviceContext.init();
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

    public Provider getProvider() {
        return serviceContext.getProviderManager().getProvider();
    }

    public RequestContext createServiceRequestContext()
        throws IOException {
        return new MockServiceRequestContext(serviceContext, getUser());
    }

    public RequestContext createFeedRequestContext(CollectionItem collection,
                                                   String method)
        throws IOException {
        MockCollectionRequestContext rc =
            new MockCollectionRequestContext(serviceContext, collection,
                                             method);
        if (method.equals("POST")) {
            NoteItem item = makeDummyItem(collection.getOwner());
            rc.setEntryContent(item.getName());
        }
        return rc;
    }

    public RequestContext createFeedRequestContext(CollectionItem collection,
                                                   String method,
                                                   String projection,
                                                   String format)
        throws IOException {
        MockCollectionRequestContext rc =
            new MockCollectionRequestContext(serviceContext, collection,
                                             method, projection, format);
        if (method.equals("POST")) {
            NoteItem item = makeDummyItem(collection.getOwner());
            rc.setEntryContent(item.getName());            
        }
        return rc;
    }

    public RequestContext createFeedRequestContext(String uid,
                                                   String method) {
        return new MockCollectionRequestContext(serviceContext, uid, method);
    }

    public RequestContext createSubscribedRequestContext()
        throws IOException {
        MockSubscribedRequestContext rc =
            new MockSubscribedRequestContext(serviceContext, getUser());
        return rc;
    }

    public RequestContext
        createSubscriptionRequestContext(CollectionSubscription sub)
        throws IOException {
        return createSubscriptionRequestContext(sub, "GET");
    }

    public RequestContext
        createSubscriptionRequestContext(CollectionSubscription sub,
                                         String method)
        throws IOException {
        MockSubscriptionRequestContext rc =
            new MockSubscriptionRequestContext(serviceContext, getUser(), sub,
                                               method);
        return rc;
    }

    public RequestContext createUpdateRequestContext(CollectionItem collection)
        throws IOException {
        MockCollectionRequestContext rc =
            new MockCollectionRequestContext(serviceContext, collection,
                                             "POST");
        return rc;
    }

    public RequestContext createEntryRequestContext(NoteItem item,
                                                    String method)
        throws IOException {
        MockItemRequestContext rc =
            new MockItemRequestContext(serviceContext, item, method, null);
        if (method.equals("PUT"))
            rc.setEntryContent(item);
        return rc;
    }

    public RequestContext createEntryRequestContext(NoteItem item,
                                                    String method,
                                                    String projection,
                                                    String format)
        throws IOException {
        MockItemRequestContext rc =
            new MockItemRequestContext(serviceContext, item, method, null,
                                       projection, format);
        if (method.equals("PUT"))
            rc.setEntryContent(item);
        return rc;
    }

    public RequestContext createEntryRequestContext(String uid,
                                                    String method)
        throws IOException {
        return new MockItemRequestContext(serviceContext, uid, method, null);
    }

    public RequestContext createMediaRequestContext(NoteItem item,
                                                    String method)
        throws IOException {
        MockItemRequestContext rc =
            new MockItemRequestContext(serviceContext, item, method,
                                       "text/plain");
        if (method.equals("PUT"))
            rc.setTextContent(item);
        return rc;
    }

    public RequestContext createMediaRequestContext(String uid,
                                                    String method)
        throws IOException {
        return new MockItemRequestContext(serviceContext, uid, method,
                                          "text/plain");
    }

    public void setIfMatch(RequestContext context,
                           Item item) {
        setIfMatch(context, item.getEntityTag());
    }

    public void setIfMatch(RequestContext context,
                           String etag) {
        if (! etag.equals("*") &&
            ! etag.startsWith("\""))
            etag = "\"" + etag + "\"";
        ((MockHttpServletRequest)
         ((BaseMockRequestContext)context).getRequest()).
            addHeader("If-Match", etag);
    }

    public void setIfNoneMatch(RequestContext context,
                               Item item) {
        setIfNoneMatch(context, item.getEntityTag());
    }

    public void setIfNoneMatch(RequestContext context,
                               String etag) {
        if (! etag.equals("*") &&
            ! etag.startsWith("\""))
            etag = "\"" + etag + "\"";
        ((MockHttpServletRequest)
         ((BaseMockRequestContext)context).getRequest()).
            addHeader("If-None-Match", etag);
    }

    public void setContentType(RequestContext context,
                               String contentType) {
        ((BaseMockRequestContext)context).getMockRequest().
            setContentType(contentType);
        ((BaseMockRequestContext)context).getMockRequest().
            addHeader("Content-Type", contentType);
    }

    public void addParameter(RequestContext context,
                             String param,
                             String value) {
        ((BaseMockRequestContext)context).getMockRequest().
            addParameter(param, value);
    }

    public void rememberProjection(String projection) {
        generatorFactory.getProjections().add(projection);
    }

    public void forgetProjections() {
        generatorFactory.getProjections().clear();
    }

    public void rememberFormat(String format) {
        generatorFactory.getFormats().add(format);
    }

    public void forgetFormats() {
        generatorFactory.getFormats().clear();
    }

    public void enableGeneratorFailure() {
        generatorFactory.setFailureMode(true);
    }

    public void rememberMediaType(String mediaType) {
        processorFactory.getMediaTypes().add(mediaType);
    }

    public void forgetMediaTypes() {
        processorFactory.getMediaTypes().clear();
    }

    public void enableProcessorFailure() {
        processorFactory.setFailureMode(true);
    }

    public void enableProcessorValidationError() {
        processorFactory.setValidationErrorMode(true);
    }

    public String getContent(ResponseContext response) {
        if (! response.hasEntity())
            return null;
        try {
            StringWriter writer = new StringWriter();
            response.writeTo(writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Can't output response content");
        }
    }
}
