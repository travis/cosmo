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

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.EntityTag;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.util.Constants;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.generator.ItemFeedGenerator;
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
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;

public class ItemProvider extends BaseProvider implements AtomConstants {
    private static final Log log = LogFactory.getLog(ItemProvider.class);

    private ProcessorFactory processorFactory;
    private ContentService contentService;

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
            ItemFeedGenerator generator =
                createItemFeedGenerator(itemTarget, locator);
            entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument(), 201, "Created");
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            rc.setEntityTag(new EntityTag(item.getEntityTag()));

            try {
                rc.setLocation(entry.getSelfLink().getHref().toString());
                rc.setContentLocation(entry.getSelfLink().getHref().toString());
            } catch (Exception e) {
                throw new RuntimeException("Error parsing self link href", e);
            }

            return rc;
        } catch (ParseException e) {
            return badrequest(getAbdera(), request, "Unable to parse entry: " + e.getMessage());
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(getAbdera(), request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        } catch (UnsupportedProjectionException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (UnsupportedFormatException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();

        if (log.isDebugEnabled())
            log.debug("deleting entry for item " + item.getUid());

        try {
            String uuid = request.getParameter("uuid");
            if (! StringUtils.isBlank(uuid)) {
                Item collection = contentService.findItemByUid(uuid);
                if (collection == null)
                    return conflict(getAbdera(), request, "Collection not found");
                if (! (collection instanceof CollectionItem))
                    return conflict(getAbdera(), request, "Uuid does not specify a collection");
                contentService.
                    removeItemFromCollection(item, (CollectionItem)collection);
            } else {
                contentService.removeItem(item);
            }
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }

        return createResponseContext(204);
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
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            rc.setEntityTag(new EntityTag(item.getEntityTag()));

            return rc;
        } catch (ParseException e) {
            return badrequest(getAbdera(), request, "Unable to parse entry: " + e.getMessage());
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(getAbdera(), request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        } catch (UnsupportedProjectionException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (UnsupportedFormatException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
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

            AbstractResponseContext rc = createResponseContext(204);
            rc.setEntityTag(new EntityTag(item.getEntityTag()));

            return rc;
        } catch (MimeTypeParseException e) {
            return notsupported(getAbdera(), request, "invalid content type");
        } catch (UnsupportedMediaTypeException e) {
            return notsupported(getAbdera(), request, "invalid content type " + e.getMediaType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }
  
    public ResponseContext getService(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getFeed(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("getting feed for collection " + collection.getUid());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            generator.setFilter(QueryBuilder.buildFilter(request));
            Feed feed = generator.generateFeed(collection);

            AbstractResponseContext rc =
                createResponseContext(feed.getDocument());
            rc.setEntityTag(new EntityTag(collection.getEntityTag()));
            return rc;
        } catch (InvalidQueryException e) {
            return badrequest(getAbdera(), request, e.getMessage());
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
    }

    public ResponseContext getEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("getting entry for item " + item.getUid());

        try {
            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            generator.setFilter(QueryBuilder.buildFilter(request));
            Entry entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            // override Abdera which sets content type to include the
            // type attribute because IE chokes on it
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            return rc;
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (InvalidQueryException e) {
            return badrequest(getAbdera(), request, e.getMessage());
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
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

    // ExtendedProvider methods

    public ResponseContext updateCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("updating details for collection " + collection.getUid());

        try {
            boolean dirty = false;
            for (String param : request.getParameterNames()) {
                if (param.equals("name")) {
                    String name = request.getParameter("name");
                    if (! StringUtils.isBlank(name)) {
                        collection.setName(name);
                        collection.setDisplayName(name);
                        dirty = true;
                    }
                } else {
                    log.warn("skipping unknown collection details parameter " +
                             param);
                }
            }
            if (dirty)
                collection = contentService.updateCollection(collection);

            AbstractResponseContext rc = createResponseContext(204);
            rc.setEntityTag(new EntityTag(collection.getEntityTag()));

            return rc;
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }

    // our methods

    public ResponseContext getExpandedItemFeed(RequestContext request) {
        ExpandedItemTarget target = (ExpandedItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("getting expanded feed for item " + item.getUid());

        try {
            NoteItemFilter filter = QueryBuilder.buildFilter(request);
            if (filter == null)
                return badrequest(getAbdera(), request, "Time range query parameters not included");

            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            generator.setFilter(filter);
            Feed feed = generator.generateFeed(item);

            AbstractResponseContext rc =
                createResponseContext(feed.getDocument());
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            return rc;
        } catch (InvalidQueryException e) {
            return badrequest(getAbdera(), request, e.getMessage());
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return badrequest(getAbdera(), request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        }
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

    public void init() {
        super.init();
        if (processorFactory == null)
            throw new IllegalStateException("processorFactory is required");
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
    }

    protected ItemFeedGenerator createItemFeedGenerator(BaseItemTarget target,
                                                        ServiceLocator locator)
        throws UnsupportedProjectionException, UnsupportedFormatException {
        return getGeneratorFactory().
            createItemFeedGenerator(target.getProjection(), target.getFormat(),
                                    locator);
    }

    protected ContentProcessor createContentProcessor(Entry entry)
        throws UnsupportedMediaTypeException {
        String mediaType = null;

        // if the entry is text, HTML, XHTML or XML, we can use the
        // content type itself to find a processor.
        if (entry.getContentType() != null &&
            ! entry.getContentType().equals(Content.Type.MEDIA))
            mediaType = entry.getContentType().toString();
 
        // if it's media, then we we want to use the content's mime
        // type directly.
        if (mediaType == null &&
            entry.getContentMimeType() != null)
            mediaType = entry.getContentMimeType().toString();

        if (mediaType != null)
            return createContentProcessor(mediaType);

        throw new UnsupportedMediaTypeException("indeterminate media type");
    }

    protected ContentProcessor createContentProcessor(String mediaType)
        throws UnsupportedMediaTypeException {
        return processorFactory.createProcessor(mediaType);
    }
}
