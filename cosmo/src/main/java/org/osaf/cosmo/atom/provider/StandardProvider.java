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

import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.BaseResponseContext;
import org.apache.abdera.protocol.server.provider.EmptyResponseContext;
import org.apache.abdera.protocol.server.provider.Provider;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.protocol.server.servlet.HttpServletRequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.generator.FeedGenerator;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.UnsupportedFormatException;
import org.osaf.cosmo.atom.generator.UnsupportedProjectionException;
import org.osaf.cosmo.atom.processor.ContentProcessor;
import org.osaf.cosmo.atom.processor.ProcessorException;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.UnsupportedMediaTypeException;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.server.ServerUtils;
import org.osaf.cosmo.service.ContentService;

public class StandardProvider implements Provider {
    private static final Log log = LogFactory.getLog(StandardProvider.class);

    private GeneratorFactory generatorFactory;
    private ProcessorFactory processorFactory;
    private ContentService contentService;
    private ServiceLocatorFactory serviceLocatorFactory;

    // Provider methods

    public ResponseContext createEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext deleteEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext updateEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        if (log.isDebugEnabled())
            log.debug("updating entry for item " + target.getUid());

        Item item = contentService.findItemByUid(target.getUid());
        if (item == null)
            return errorResponse(404, "Requested item not found");
        if (! (item instanceof NoteItem))
            return errorResponse(403, "Requested item is not a note");
        NoteItem note = (NoteItem) item;

        // XXX If-Match

        // XXX: check write preconditions?

        try {
            // XXX: does abdera automatically resolve external content?
            Entry entry = (Entry) request.getDocument().getRoot();
            ContentProcessor processor = createContentProcessor(entry);
            processor.processContent(entry.getContent(), note);
        } catch (UnsupportedMediaTypeException e) {
            return errorResponse(415);
        } catch (ValidationException e) {
            String msg = "Invalid content";
            if (e.getCause() != null)
                msg = msg + ": " + e.getCause().getMessage();
            return errorResponse(400, msg);
        } catch (IOException e) {
            log.error("Unable to read request content", e);
            return errorResponse(500, "Unable to read request content: " + e.getMessage());
        } catch (ProcessorException e) {
            log.error("Unknown content processing error", e);
            return errorResponse(500, "Unknown content processing error: " + e.getMessage());
        }

        contentService.updateItem(note);

        AbstractResponseContext rc = new EmptyResponseContext(204);
        rc.setEntityTag(new EntityTag(ServerUtils.calculateEtag(note)));
        return rc;
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext getService(RequestContext request) {
        return null;
    }
  
    public ResponseContext getFeed(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        if (log.isDebugEnabled())
            log.debug("getting feed for collection " + target.getUid());

        Item item = contentService.findItemByUid(target.getUid());
        if (item == null)
            return errorResponse(404, "Requested item not found");
        if (! (item instanceof CollectionItem))
            return errorResponse(403, "Requested item is not a collection");
        CollectionItem collection = (CollectionItem) item;

        // XXX If-None-Match

        try {
            ServiceLocator locator = createServiceLocator(request);
            FeedGenerator generator = createFeedGenerator(target, locator);
            Feed feed = generator.generateFeed(collection);

            AbstractResponseContext rc =
                new BaseResponseContext<Document<Element>>(feed.getDocument());
            rc.setEntityTag(new EntityTag(ServerUtils.calculateEtag(collection)));
            return rc;
        } catch (UnsupportedProjectionException e) {
            return errorResponse(404, "Projection " + target.getProjection() + " not supported");
        } catch (UnsupportedFormatException e) {
            return errorResponse(404, "Format " + target.getFormat() + " not supported");
        } catch (GeneratorException e) {
            log.error("Unknown feed generation error", e);
            return errorResponse(500, "Unknown feed generation error: " + e.getMessage());
        }
    }
  
    public ResponseContext getEntry(RequestContext request) {
        return null;
    }
  
    public ResponseContext getMedia(RequestContext request) {
        return null;
    }
  
    public ResponseContext getCategories(RequestContext request) {
        return null;
    }
  
    public ResponseContext entryPost(RequestContext request) {
        return null;
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        return null;
    }

    // our methods

    public GeneratorFactory getGeneratorFactory() {
        return generatorFactory;
    }

    public void setGeneratorFactory(GeneratorFactory factory) {
        generatorFactory = factory;
    }

    public ProcessorFactory getProcessorFactory() {
        return processorFactory;
    }

    public void setProcessorFactory(ProcessorFactory factory) {
        processorFactory = factory;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public ServiceLocatorFactory getServiceLocatorFactory() {
        return serviceLocatorFactory;
    }

    public void setServiceLocatorFactory(ServiceLocatorFactory factory) {
        serviceLocatorFactory = factory;
    }

    public void init() {
        if (generatorFactory == null)
            throw new IllegalStateException("generatorFactory is required");
        if (processorFactory == null)
            throw new IllegalStateException("processorFactory is required");
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (serviceLocatorFactory == null)
            throw new IllegalStateException("serviceLocatorFactory is required");
    }

    protected FeedGenerator createFeedGenerator(CollectionTarget target,
                                                ServiceLocator locator)
        throws UnsupportedProjectionException, UnsupportedFormatException {
        return generatorFactory.createFeedGenerator(target.getProjection(),
                                                    target.getFormat(),
                                                    locator);
    }

    protected ContentProcessor createContentProcessor(Entry entry)
        throws UnsupportedMediaTypeException {
        String type = entry.getContentType() != null ?
            entry.getContentType().toString() :
            entry.getContentMimeType().toString();
        return processorFactory.createProcessor(type);
    }

    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((HttpServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    protected AbstractResponseContext errorResponse(int status) {
        return errorResponse(status, null);
    }

    protected AbstractResponseContext errorResponse(int status,
                                                    String message) {
        AbstractResponseContext rc = new EmptyResponseContext(status);
        if (message != null)
            rc.setStatusText(message);
        return rc;
    }
}
