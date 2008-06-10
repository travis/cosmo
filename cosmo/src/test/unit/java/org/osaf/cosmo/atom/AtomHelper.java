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
import java.util.Date;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.MockHelper;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.mock.MockGeneratorFactory;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.mock.MockProcessorFactory;
import org.osaf.cosmo.atom.provider.mock.BaseMockRequestContext;
import org.osaf.cosmo.atom.provider.mock.MockCollectionAdapter;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.mock.MockEntityFactory;
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
    private MockCollectionAdapter collectionAdapter;
    private EntityFactory entityFactory;

    public AtomHelper() {
        super();

        abdera = new Abdera();
        collectionAdapter = new MockCollectionAdapter();
        generatorFactory = new MockGeneratorFactory(abdera);
        processorFactory = new MockProcessorFactory();
        entityFactory = new MockEntityFactory();

        
    }

    public Abdera getAbdera() {
        return abdera;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public GeneratorFactory getGeneratorFactory() {
        return generatorFactory;
    }

    public ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public MockCollectionAdapter getCollectionAdapter(RequestContext request) {
        return collectionAdapter;
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

    public void setIfModifiedSince(RequestContext context,
                                   Date date) {
        ((BaseMockRequestContext)context).getMockRequest().
            addHeader("If-Modified-Since", date);
    }

    public void setIfUnmodifiedSince(RequestContext context,
                                     Date date) {
        ((BaseMockRequestContext)context).getMockRequest().
            addHeader("If-Unmodified-Since", date);
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

    public void rememberContentType(String type) {
        processorFactory.getContentTypes().add(type);
    }

    public void forgetContentTypes() {
        processorFactory.getContentTypes().clear();
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
