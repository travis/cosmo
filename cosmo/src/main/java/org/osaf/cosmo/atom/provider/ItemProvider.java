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

import net.fortuna.ical4j.model.TimeZone;

import java.io.InputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import javax.activation.MimeTypeParseException;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.provider.AbstractResponseContext;
import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.ResponseContext;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;

import org.apache.commons.io.IOUtils;
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
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.text.XhtmlCollectionFormat;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.util.MimeUtil;

public class ItemProvider extends BaseProvider implements AtomConstants {
    private static final Log log = LogFactory.getLog(ItemProvider.class);

    private ProcessorFactory processorFactory;
    private ContentService contentService;

    // Provider methods
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "POST", "PUT", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "GET", "HEAD", "PUT", "OPTIONS" };

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
            rc.setLastModified(item.getModifiedDate());

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
        } catch (UidInUseException e) {
            return conflict(getAbdera(), request, "Uid already in use");
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
            item = processEntryUpdate(processor, entry, item);

            target = new ItemTarget(request, item, PROJECTION_FULL, null);
            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setContentType(Constants.ATOM_MEDIA_TYPE);
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            rc.setLastModified(item.getModifiedDate());

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
        } catch (UidInUseException e) {
            return conflict(getAbdera(), request, "Uid already in use");
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
            rc.setLastModified(item.getModifiedDate());

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
        } catch (UidInUseException e) {
            return conflict(getAbdera(), request, "Uid already in use");
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
            generator.setFilter(createQueryFilter(request));

            Feed feed = generator.generateFeed(collection);

            AbstractResponseContext rc =
                createResponseContext(feed.getDocument());
            rc.setEntityTag(new EntityTag(collection.getEntityTag()));
            rc.setLastModified(collection.getModifiedDate());
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
            Entry entry = generator.generateEntry(item);

            AbstractResponseContext rc =
                createResponseContext(entry.getDocument());
            rc.setEntityTag(new EntityTag(item.getEntityTag()));
            rc.setLastModified(item.getModifiedDate());
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
        return methodnotallowed(getAbdera(), request, ALLOWED_ENTRY_METHODS);
    }
  
    public ResponseContext mediaPost(RequestContext request) {
        return methodnotallowed(getAbdera(), request, ALLOWED_ENTRY_METHODS);
    }

    // ExtendedProvider methods

    public ResponseContext updateCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        if (collection instanceof HomeCollectionItem)
            return unauthorized(getAbdera(), request, "Home collection is not modifiable");

        if (log.isDebugEnabled())
            log.debug("updating details for collection " + collection.getUid());

        try {
            if (! MimeUtil.isXhtml(request.getContentType()))
                return notsupported(getAbdera(), request, "Entity-body must be " + MimeUtil.MEDIA_TYPE_XHTML);

            CollectionItem content = readCollection(request);

            if (! content.getDisplayName().
                equals(collection.getDisplayName())) {
                if (log.isDebugEnabled())
                    log.debug("updating collection " + collection.getUid());
                collection.setDisplayName(content.getDisplayName());
                collection = contentService.updateCollection(collection);
            }

            AbstractResponseContext rc = createResponseContext(204);
            rc.setEntityTag(new EntityTag(collection.getEntityTag()));
            rc.setLastModified(collection.getModifiedDate());
            return rc;
        } catch (MimeTypeParseException e) {
            return notsupported(getAbdera(), request, "Invalid content type");
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid request content: " + e.getMessage();
            if (e.getCause() != null)
                msg += ": " + e.getCause().getMessage();
            return badrequest(getAbdera(), request, msg);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }

    // our methods

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

    protected NoteItemFilter createQueryFilter(RequestContext request)
        throws InvalidQueryException {
        boolean requiresFilter = false;
        EventStampFilter eventFilter = new EventStampFilter();

        try {
            java.util.Date start = getDateParameter(request, "start");
            java.util.Date end = getDateParameter(request, "end");

            if ((start == null && end != null) ||
                (start != null && end == null))
                throw new InvalidQueryException("Both start and end parameters must be provided for a time-range query");

            if (start != null && end != null) {
                requiresFilter = true;
                eventFilter.setTimeRange(start, end);
                eventFilter.setExpandRecurringEvents(true);
            }
        } catch (java.text.ParseException e) {
            throw new InvalidQueryException("Error parsing time-range parameter: " + e.getMessage(), e);
        }

        try {
            TimeZone tz = getTimeZoneParameter(request, "tz");
            if (tz != null) {
                requiresFilter = true;
                eventFilter.setTimezone(tz);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized time zone " + request.getParameter("tz") +
                     " ... falling back to system default time zone");
        }

        if (! requiresFilter)
            return null;

        NoteItemFilter itemFilter = new NoteItemFilter();
        itemFilter.getStampFilters().add(eventFilter);

        return itemFilter;
    }

    private NoteItem processEntryUpdate(ContentProcessor processor,
                                        Entry entry,
                                        NoteItem item)
        throws ValidationException, ProcessorException {
        EventStamp es = EventStamp.getStamp(item);
        Date oldstart = es != null && es.isRecurring() ?
            es.getStartDate() : null;

        processor.processContent(entry.getContent(), item);
        item = (NoteItem) contentService.updateItem(item);

        if (oldstart != null) {
            Date newstart = es.getStartDate();
            if (newstart != null && ! newstart.equals(oldstart)) {
                long delta = newstart.getTime() - oldstart.getTime();
                if (log.isDebugEnabled())
                    log.debug("master event start date changed; " +
                              "adjusting modifications by " + delta +
                              " milliseconds");

                HashSet<NoteItem> copies = new HashSet<NoteItem>();
                HashSet<NoteItem> removals = new HashSet<NoteItem>();

                // copy each modification and update the copy's uid
                // with the new start date
                Iterator<NoteItem> mi = item.getModifications().iterator();
                while (mi.hasNext()) {
                    NoteItem mod = mi.next();
                    removals.add(mod);

                    NoteItem copy = (NoteItem) mod.copy();

                    EventExceptionStamp ees =
                        EventExceptionStamp.getStamp(copy);
                    DateTime oldRid = (DateTime) ees.getRecurrenceId();
                    java.util.Date newRidTime =
                        new java.util.Date(oldRid.getTime() + delta);
                    DateTime newRid = (DateTime)
                        Dates.getInstance(newRidTime, Value.DATE_TIME);
                    if (oldRid.isUtc())
                        newRid.setUtc(true);
                    else
                        newRid.setTimeZone(oldRid.getTimeZone());
                        
                    copy.setUid(new ModificationUid(item, newRid).toString());
                    ees.setRecurrenceId(newRid);

                    copies.add(copy);
                }

                // remove the old mods from each of their collections,
                // leaving tombstones behind
                for (NoteItem removal : removals) {
                    for (CollectionItem parent : removal.getParents())
                        contentService.removeItemFromCollection(removal,
                                                                parent);
                }

                // add the replacement mods to each of the master's
                // collections
                for (NoteItem newmod : copies) {
                    newmod.setModifies(item);
                    item.getModifications().add(newmod);

                    Iterator<CollectionItem> pi = item.getParents().iterator();
                    contentService.createContent(pi.next(), newmod);
                    while (pi.hasNext())
                        contentService.addItemToCollection(newmod, pi.next());
                }
            }
        }

        return item;
    }

    private CollectionItem readCollection(RequestContext request)
        throws IOException, ValidationException {
        try {
            XhtmlCollectionFormat formatter = new XhtmlCollectionFormat();
            InputStream in = request.getInputStream();
            if (in == null)
                throw new ValidationException("An entity-body must be provided");
            CollectionItem collection = formatter.parse(IOUtils.toString(in));
            if (collection.getDisplayName() == null)
                collection.setDisplayName("");
            return collection;
        } catch (java.text.ParseException e) {
            throw new ValidationException("Error parsing XHTML content", e);
        }
    }
}
