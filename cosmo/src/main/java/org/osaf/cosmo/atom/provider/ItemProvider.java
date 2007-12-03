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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.activation.MimeType;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.AbstractResponseContext;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;
import org.apache.abdera.util.MimeTypeHelper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.UidConflictException;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.ItemFeedGenerator;
import org.osaf.cosmo.atom.generator.UnsupportedFormatException;
import org.osaf.cosmo.atom.generator.UnsupportedProjectionException;
import org.osaf.cosmo.atom.processor.ContentProcessor;
import org.osaf.cosmo.atom.processor.ProcessorException;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.atom.processor.UnsupportedContentTypeException;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.BaseEventStamp;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionLockedException;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.EventExceptionStamp;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.IcalUidInUseException;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.text.XhtmlCollectionFormat;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;

public class ItemProvider extends BaseProvider implements AtomConstants {
    private static final Log log = LogFactory.getLog(ItemProvider.class);

    private ProcessorFactory processorFactory;
    private ContentService contentService;

    // Provider methods
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "GET", "HEAD", "PUT", "OPTIONS" };

    /**
     * Supports additional API methods:
     * <ul>
     * <li> POST to a collection - if the content type is
     * <code>application/x-www-form-urlencoded</code>, the request is
     * delegated to the method {@link #addItemToCollection(RequestContext)}.
     * </li>
     * </ul>
     */
    public ResponseContext request(RequestContext request) {
        String method = request.getMethod();
        TargetType type = request.getTarget().getType();

        if (method.equals("POST")) {
            if (type == TargetType.TYPE_COLLECTION) {
                if (isAddItemToCollectionRequest(request))
                    return addItemToCollection(request);
            }
        }

        return super.request(request);
    }

    public ResponseContext createEntry(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("creating entry in collection " + collection.getUid());

        ResponseContext frc = checkEntryWritePreconditions(request, false);
        if (frc != null)
            return frc;

        try {
            String mediaType = request.getContentType().toString();
            boolean isEntry =
                MimeTypeHelper.isMatch(Constants.ATOM_MEDIA_TYPE, mediaType);

            Entry entry = null;
            ContentProcessor processor = null;
            NoteItem item = null;
            if (isEntry) {
                // XXX: does abdera automatically resolve external content?
                entry = (Entry) request.getDocument().getRoot();
                processor = createContentProcessor(entry);
                item = processor.processCreation(entry.getContent(), collection);
            } else {
                try {
                    processor = createContentProcessor(mediaType);
                } catch (UnsupportedContentTypeException e) {
                    return notsupported(getAbdera(), request, "Content-type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
                }
                item = processor.processCreation(request.getReader(), collection);
            }

            item = (NoteItem) contentService.createContent(collection, item);

            ServiceLocator locator = createServiceLocator(request);

            if (isEntry) {
                ItemTarget itemTarget =
                    new ItemTarget(request, item, target.getProjection(),
                                   target.getFormat());
                ItemFeedGenerator generator =
                    createItemFeedGenerator(itemTarget, locator);
                entry = generator.generateEntry(item);

                return created(entry, item, locator);
            } else {
                return created(item, locator);
            }
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return badrequest(getAbdera(), request, "Entry content type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
        } catch (ParseException e) {
            String reason = "Unparseable content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (UidInUseException e) {
            return conflict(getAbdera(), request, "Uid already in use");
        } catch (IcalUidInUseException e) {
            return conflict(request, new UidConflictException(e));
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
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

            return deleted();
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext updateEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();
        if (log.isDebugEnabled())
            log.debug("updating entry for item " + item.getUid());

        ResponseContext frc = checkEntryWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            // XXX: does abdera automatically resolve external content?
            Entry entry = (Entry) request.getDocument().getRoot();
            ContentProcessor processor = createContentProcessor(entry);
            item = processEntryUpdate(processor, entry, item);

            target = new ItemTarget(request, item, target.getProjection(),
                                    target.getFormat());
            ServiceLocator locator = createServiceLocator(request);
            ItemFeedGenerator generator =
                createItemFeedGenerator(target, locator);
            entry = generator.generateEntry(item);

            return updated(entry, item, locator, false);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return badrequest(getAbdera(), request, "Entry content type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
        } catch (ParseException e) {
            String reason = "Unparseable content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
        } catch (IcalUidInUseException e) {
            return conflict(request, new UidConflictException(e));
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
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

        ResponseContext frc = checkMediaWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            ContentProcessor processor =
                createContentProcessor(request.getContentType().toString());
            processor.processContent(request.getReader(), item);
            item = (NoteItem) contentService.updateItem(item);

            return updated(item);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return notsupported(getAbdera(), request, "Unsupported media type " + e.getContentType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return badrequest(getAbdera(), request, reason);
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

            return ok(feed, collection);
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

            return ok(entry, item);
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
  
    public ResponseContext createCollection(RequestContext request) {
        NewCollectionTarget target = (NewCollectionTarget) request.getTarget();
        User user = target.getUser();
        HomeCollectionItem home = target.getHomeCollection();

        if (log.isDebugEnabled())
            log.debug("creating collection in home collection of user '" + user.getUsername() + "'");

        ResponseContext frc = checkCollectionWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            CollectionItem content = readCollection(request);

            CollectionItem collection = getEntityFactory().createCollection();
            collection.setUid(content.getUid());
            String name = content.getDisplayName().replaceAll("[\\/\\?\\#\\=\\;]", "_");
            collection.setName(collection.getUid());
            collection.setDisplayName(content.getDisplayName());
            collection.setOwner(user);

            CalendarCollectionStamp stamp = getEntityFactory()
                    .createCalendarCollectionStamp(collection);
               
            stamp.setDescription(collection.getDisplayName());
            // XXX set the calendar language from Content-Language
            collection.addStamp(stamp);

            collection = contentService.createCollection(home, collection);

            ServiceLocator locator = createServiceLocator(request);

            return created(collection, locator);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid request content: " + e.getMessage();
            if (e.getCause() != null)
                msg += ": " + e.getCause().getMessage();
            return badrequest(getAbdera(), request, msg);
        } catch (UidInUseException e) {
            return conflict(getAbdera(), request, "Uid already in use");
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }

    public ResponseContext updateCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        if (collection instanceof HomeCollectionItem)
            return unauthorized(getAbdera(), request, "Home collection is not modifiable");

        if (log.isDebugEnabled())
            log.debug("updating details for collection " + collection.getUid());

        ResponseContext frc = checkCollectionWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            CollectionItem content = readCollection(request);

            if (! content.getDisplayName().
                equals(collection.getDisplayName())) {
                if (log.isDebugEnabled())
                    log.debug("updating collection " + collection.getUid());
                collection.setDisplayName(content.getDisplayName());
                collection = contentService.updateCollection(collection);
            }

            return updated(collection);
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

    public ResponseContext deleteCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        if (collection instanceof HomeCollectionItem)
            return unauthorized(getAbdera(), request, "Home collection is not deleteable");

        if (log.isDebugEnabled())
            log.debug("deleting collection " + collection.getUid());

        try {
            contentService.removeCollection(collection);

            return deleted();
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
        }
    }

    // our methods

    public ResponseContext addItemToCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("adding item to collection " + collection.getUid());

        ResponseContext frc = checkAddItemToCollectionPreconditions(request);
        if (frc != null)
            return frc;

        try {
            String uuid = readUuid(request);
            if (uuid == null)
                return badrequest(getAbdera(), request, "Uuid must be provided");

            Item item = contentService.findItemByUid(uuid);
            if (item == null)
                return badrequest(getAbdera(), request, "Item with uuid " + uuid + " not found");
            if (! (item instanceof NoteItem))
                return badrequest(getAbdera(), request, "Item with uuid " + uuid + " is not a note");

            contentService.addItemToCollection(item, collection);

            return createResponseContext(204);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return servererror(getAbdera(), request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(getAbdera(), request);
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
        throws UnsupportedContentTypeException {
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

        throw new UnsupportedContentTypeException("indeterminate media type");
    }

    protected ContentProcessor createContentProcessor(String mediaType)
        throws UnsupportedContentTypeException {
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

    private boolean isAddItemToCollectionRequest(RequestContext request) {
        MimeType ct = request.getContentType();
        if (ct == null)
            return false;
        return MimeTypeHelper.isMatch(MEDIA_TYPE_URLENCODED, ct.toString());
    }

    private ResponseContext checkAddItemToCollectionPreconditions(RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(getAbdera(), request);

        return null;
    }

    private String readUuid(RequestContext request)
        throws IOException {
        BufferedReader in = (BufferedReader) request.getReader();
        try {
            for (String pair : in.readLine().split("\\&")) {
                String[] fields = pair.split("=");
                String name = URLDecoder.decode(fields[0], "UTF-8");
                if (name.equals("uuid"))
                    return URLDecoder.decode(fields[1], "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {}
        return null;
    }

    private NoteItem processEntryUpdate(ContentProcessor processor,
                                        Entry entry,
                                        NoteItem item)
        throws ValidationException, ProcessorException {
        EventStamp es = StampUtils.getEventStamp(item);
        Date oldstart = es != null && es.isRecurring() ?
            es.getStartDate() : null;

        processor.processContent(entry.getContent(), item);
        
        // oldStart will have a value if the item has an EventStamp
        // and the EventStamp is recurring
        if (oldstart != null) {
            es = StampUtils.getEventStamp(item);
            // Case 1: EventStamp was removed from recurring event, so we
            // have to remove all modifications (a modification doesn't make
            // sense if there is no recurring event)
            if(es==null) {
                LinkedHashSet<ContentItem> updates = new LinkedHashSet<ContentItem>();
                for(NoteItem mod: item.getModifications()) {
                    mod.setIsActive(false);
                    updates.add(mod);
                }
                updates.add(item);
                
                // Update item and remove modifications in one atomic service call
                contentService.updateContentItems(item.getParents(), updates);
            } 
            // Case 2: Start date may have changed on master event.
            else {
                Date newstart = es.getStartDate();
                
                // If changed, we have to update all the recurrenceIds
                // for any modifications
                if (newstart != null && ! newstart.equals(oldstart)) {
                    long delta = newstart.getTime() - oldstart.getTime();
                    if (log.isDebugEnabled())
                        log.debug("master event start date changed; " +
                                  "adjusting modifications by " + delta +
                                  " milliseconds");
    
                    LinkedHashSet<ContentItem> updates = new LinkedHashSet<ContentItem>();
                    HashSet<NoteItem> copies = new HashSet<NoteItem>();
                    HashSet<NoteItem> removals = new HashSet<NoteItem>();
    
                    // copy each modification and update the copy's uid
                    // with the new start date
                    Iterator<NoteItem> mi = item.getModifications().iterator();
                    while (mi.hasNext()) {
                        NoteItem mod = mi.next();
                        mod.setIsActive(false);
                        removals.add(mod);
    
                        NoteItem copy = (NoteItem) mod.copy();
                        copy.setModifies(item);
                        
                        EventExceptionStamp ees =
                            StampUtils.getEventExceptionStamp(copy);
                        
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
    
                        // If the modification's dtstart is missing, then
                        // we have to adjust dtstart to be equal to the
                        // recurrenceId.
                        if(isDtStartMissing(StampUtils.getBaseEventStamp(mod))) {
                            ees.setStartDate(ees.getRecurrenceId());
                        }
                        
                        copies.add(copy);
                    }
    
                    // add removals first
                    updates.addAll(removals);
                    // then additions
                    updates.addAll(copies);
                    // then updates
                    updates.add(item);
                    
                    // Update everything in one atomic service call
                    contentService.updateContentItems(item.getParents(), updates);
                } else {
                    // otherwise use simple update
                    item = (NoteItem) contentService.updateItem(item);
                }
            }
        } else {
            // use simple update
            item = (NoteItem) contentService.updateItem(item);
        }

        return item;
    }

    private CollectionItem readCollection(RequestContext request)
        throws IOException, ValidationException {
        try {
            Reader in = request.getReader();
            if (in == null)
                throw new ValidationException("An entity-body must be provided");
           
            XhtmlCollectionFormat formatter = new XhtmlCollectionFormat();
            CollectionItem collection = formatter.parse(IOUtils.toString(in), getEntityFactory());
            
            if (collection.getDisplayName() == null)
                throw new ValidationException("Display name is required");

            return collection;
        } catch (java.text.ParseException e) {
            throw new ValidationException("Error parsing XHTML content", e);
        }
    }
    
    /**
     * Determine if startDate is missing.  The startDate is missing
     * if the startDate is equal to the reucurreceId and the anyTime
     * parameter is inherited.
     * @param stamp BaseEventStamp to test
     * @return
     */
    private boolean isDtStartMissing(BaseEventStamp stamp) {
       
        if(stamp.getStartDate()==null || stamp.getRecurrenceId()==null)
            return false;
        
        // "missing" startDate is represented as startDate==recurrenceId
        if(!stamp.getStartDate().equals(stamp.getRecurrenceId()))
            return false;
        
        // "missing" anyTime is represented as null
        if(stamp.isAnyTime()!=null)
            return false;
        
        return true;
    }

    private ResponseContext created(NoteItem item,
                                    ServiceLocator locator) {
        AbstractResponseContext rc = createResponseContext(201, "Created");

        rc.setEntityTag(new EntityTag(item.getEntityTag()));
        rc.setLastModified(item.getModifiedDate());

        rc.setLocation(locator.getAtomItemUrl(item.getUid(), true));
        // don't set Content-Location since no content is included in the
        // response

        return rc;
    }

    private ResponseContext created(CollectionItem collection,
                                    ServiceLocator locator) {
        AbstractResponseContext rc = createResponseContext(201, "Created");

        rc.setEntityTag(new EntityTag(collection.getEntityTag()));
        rc.setLastModified(collection.getModifiedDate());

        String location =
            locator.getAtomCollectionUrl(collection.getUid(), true);
        rc.setLocation(location);
        // don't set Content-Location since no content is included in the
        // response

        return rc;
    }
}
