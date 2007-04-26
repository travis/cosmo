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

import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.AbstractProvider;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.BaseResponseContext;
import org.apache.abdera.protocol.server.provider.EmptyResponseContext;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.protocol.server.servlet.HttpServletRequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
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
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;

public class StandardProvider extends AbstractProvider
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(StandardProvider.class);

    private Abdera abdera;
    private GeneratorFactory generatorFactory;
    private ProcessorFactory processorFactory;
    private ContentService contentService;
    private ServiceLocatorFactory serviceLocatorFactory;

    // Provider methods

    public ResponseContext createEntry(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("creating entry in collection " + collection.getUid());

        // XXX: check write preconditions?

        NoteItem item = null;
        try {
            // XXX: does abdera automatically resolve external content?
            Entry entry = (Entry) request.getDocument().getRoot();
            ContentProcessor processor = createContentProcessor(entry);
            item = processor.processCreation(entry.getContent(), collection);
            item = (NoteItem) contentService.createContent(collection, item);

            ItemTarget itemTarget =
                new ItemTarget(request, item, PROJECTION_FULL, null);
            ServiceLocator locator = createServiceLocator(request);
            FeedGenerator generator = createFeedGenerator(itemTarget, locator);
            entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                new BaseResponseContext<Document<Element>>(entry.getDocument());
            rc.setStatus(201);
            rc.setStatusText("Created");
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            rc.setLocation(entry.getSelfLink().toString());
            rc.setContentLocation(entry.getSelfLink().toString());

            return rc;
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(abdera, request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(abdera, request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(abdera, request);
        } catch (UnsupportedProjectionException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (UnsupportedFormatException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("deleting entry for item " + item.getUid());

        contentService.removeItem(item);

        AbstractResponseContext rc = new EmptyResponseContext(204);
        return rc;
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext updateEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("updating entry for item " + item.getUid());

        // XXX: check write preconditions?

        try {
            // XXX: does abdera automatically resolve external content?
            Entry entry = (Entry) request.getDocument().getRoot();
            ContentProcessor processor = createContentProcessor(entry);
            processor.processContent(entry.getContent(), item);
            item = (NoteItem) contentService.updateItem(item);

            target = new ItemTarget(request, item, PROJECTION_FULL, null);
            ServiceLocator locator = createServiceLocator(request);
            FeedGenerator generator = createFeedGenerator(target, locator);
            entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                new BaseResponseContext<Document<Element>>(entry.getDocument());
            rc.setEntityTag(new EntityTag(item.getEntityTag()));

            return rc;
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(abdera, request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(abdera, request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(abdera, request);
        } catch (UnsupportedProjectionException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (UnsupportedFormatException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        }
    }
  
    public ResponseContext updateMedia(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("updating media for item " + item.getUid());

        // XXX: check write preconditions?

        try {
            ContentProcessor processor =
                createContentProcessor(request.getContentType().toString());
            processor.processContent(request.getReader(), item);
            item = (NoteItem) contentService.updateItem(item);

            AbstractResponseContext rc = new EmptyResponseContext(204);
            rc.setEntityTag(new EntityTag(item.getEntityTag()));

            return rc;
        } catch (MimeTypeParseException e) {
            return notsupported(abdera, request, "invalid content type");
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(abdera, request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(abdera, request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(abdera, request);
        }
    }
  
    public ResponseContext getService(RequestContext request) {
        return null;
    }
  
    public ResponseContext getFeed(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("getting feed for collection " + collection.getUid());

        try {
            ServiceLocator locator = createServiceLocator(request);
            FeedGenerator generator = createFeedGenerator(target, locator);
            Feed feed = generator.generateFeed(collection);

            AbstractResponseContext rc =
                new BaseResponseContext<Document<Element>>(feed.getDocument());
            rc.setEntityTag(new EntityTag(collection.getEntityTag()));
            return rc;
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return badrequest(abdera, request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return badrequest(abdera, request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        }
    }
  
    public ResponseContext getEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("getting feed for item " + item.getUid());

        try {
            ServiceLocator locator = createServiceLocator(request);
            FeedGenerator generator = createFeedGenerator(target, locator);
            Entry entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                new BaseResponseContext<Document<Element>>(entry.getDocument());
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            return rc;
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return badrequest(abdera, request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return badrequest(abdera, request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(abdera, request, reason, e);
        }
    }
  
    public ResponseContext getMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext entryPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    // AbstractProvider methods

    protected int getDefaultPageSize() {
        // XXX
        return 25;
    }

    // our methods

    public Abdera getAbdera() {
        return abdera;
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

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
        if (abdera == null)
            throw new IllegalStateException("abdera is required");
        if (generatorFactory == null)
            throw new IllegalStateException("generatorFactory is required");
        if (processorFactory == null)
            throw new IllegalStateException("processorFactory is required");
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (serviceLocatorFactory == null)
            throw new IllegalStateException("serviceLocatorFactory is required");
    }

    protected FeedGenerator createFeedGenerator(BaseItemTarget target,
                                                ServiceLocator locator)
        throws UnsupportedProjectionException, UnsupportedFormatException {
        return generatorFactory.createFeedGenerator(target.getProjection(),
                                                    target.getFormat(),
                                                    locator);
    }

    protected ContentProcessor createContentProcessor(Entry entry)
        throws UnsupportedMediaTypeException {
        String mediaType = entry.getContentType() != null ?
            entry.getContentType().toString() :
            entry.getContentMimeType().toString();
        return createContentProcessor(mediaType);
    }

    protected ContentProcessor createContentProcessor(String mediaType)
        throws UnsupportedMediaTypeException {
        return processorFactory.createProcessor(mediaType);
    }

    protected ServiceLocator createServiceLocator(RequestContext context) {
        HttpServletRequest request =
            ((HttpServletRequestContext)context).getRequest();
        return serviceLocatorFactory.createServiceLocator(request);
    }

    protected ResponseContext preconditionfailed(Abdera abdera,
                                                 RequestContext request,
                                                 String reason) {
        return returnBase(createErrorDocument(abdera, 412, reason, null),
                          412, null);
    }

    protected ResponseContext locked(Abdera abdera,
                                     RequestContext request) {
        return returnBase(createErrorDocument(abdera, 423, "Collection Locked",
                                              null),
                          423, null);
    }
}
