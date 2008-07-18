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
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.activation.MimeType;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.AbstractResponseContext;
import org.apache.abdera.protocol.server.context.EmptyResponseContext;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.EntityTag;
import org.apache.abdera.util.MimeTypeHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.InsufficientPrivilegesException;
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
import org.osaf.cosmo.calendar.EntityConverter;
import org.osaf.cosmo.calendar.util.CalendarUtils;
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
import org.osaf.cosmo.model.ItemSecurityException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.NoteOccurrence;
import org.osaf.cosmo.model.StampUtils;
import org.osaf.cosmo.model.UidInUseException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.filter.EventStampFilter;
import org.osaf.cosmo.model.filter.NoteItemFilter;
import org.osaf.cosmo.model.text.XhtmlCollectionFormat;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;

public class ItemCollectionAdapter extends BaseCollectionAdapter implements AtomConstants {
   
    private static final Log log = LogFactory.getLog(ItemCollectionAdapter.class);

    private ProcessorFactory processorFactory;
    private ContentService contentService;

    // Provider methods
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "GET", "HEAD", "PUT", "OPTIONS" };

    public ResponseContext postEntry(RequestContext request) {
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
                    return ProviderHelper.notsupported(request, "Content-type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
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

                return created(request, entry, item, locator);
            } else {
                return created(item, locator);
            }
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return ProviderHelper.badrequest(request, "Entry content type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
        } catch (ParseException e) {
            String reason = "Unparseable content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch (UidInUseException e) {
            return ProviderHelper.conflict(request, "Uid already in use");
        } catch (IcalUidInUseException e) {
            return conflict(request, new UidConflictException(e));
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem item = target.getItem();

        // handle case where item is an occurrence, return unknown
        if(item instanceof NoteOccurrence)
            return ProviderHelper.notfound(request, "Item not found");
        
        if (log.isDebugEnabled())
            log.debug("deleting entry for item " + item.getUid());

        try {
            String uuid = request.getParameter("uuid");
            if (! StringUtils.isBlank(uuid)) {
                Item collection = contentService.findItemByUid(uuid);
                if (collection == null)
                    return ProviderHelper.conflict(request, "Collection not found");
                if (! (collection instanceof CollectionItem))
                    return ProviderHelper.conflict(request, "Uuid does not specify a collection");
                contentService.
                    removeItemFromCollection(item, (CollectionItem)collection);
            } else {
                contentService.removeItem(item);
            }

            return deleted();
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }
  
    public ResponseContext putEntry(RequestContext request) {
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

            return updated(request, entry, item, locator, false);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return ProviderHelper.badrequest(request, "Entry content type must be one of " + StringUtils.join(processorFactory.getSupportedContentTypes(), ", "));
        } catch (ParseException e) {
            String reason = "Unparseable content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch (IcalUidInUseException e) {
            return conflict(request, new UidConflictException(e));
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }
  
    @Override
    public ResponseContext putMedia(RequestContext request) {
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
            item = (NoteItem) contentService.updateContent((ContentItem) item);

            return updated(item);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (UnsupportedContentTypeException e) {
            return ProviderHelper.notsupported(request, "Unsupported media type " + e.getContentType());
        } catch (ValidationException e) {
            String reason = "Invalid content: ";
            if (e.getCause() != null)
                reason = reason + e.getCause().getMessage();
            else
                reason = reason + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch (ProcessorException e) {
            String reason = "Unknown content processing error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }
    
    
    @Override
    public ResponseContext postMedia(RequestContext request) {
        if(isAddItemToCollectionRequest(request))
            return addItemToCollection(request);
            
        return super.postMedia(request);
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

            return ok(request, feed, collection);
        } catch (InvalidQueryException e) {
            return ProviderHelper.badrequest(request, e.getMessage());
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
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
            
            return ok(request, entry, item);
        } catch (UnsupportedProjectionException e) {
            String reason = "Projection " + target.getProjection() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (UnsupportedFormatException e) {
            String reason = "Format " + target.getFormat() + " not supported";
            return ProviderHelper.badrequest(request, reason);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    // ExtendedCollectionAdapter methods
  
    public ResponseContext postCollection(RequestContext request) {
        // This adapter supports POSTing an XHTML representation (
        // of a collection (no child items), POSTing an icalendar
        // representation of a collection (where child items will be
        // imported as well), or POSTing a URL that points to
        // an icalendar representation of a collection
        
        MimeType ct = request.getContentType();
        if (ct == null)
            return ProviderHelper.notsupported(request, "Content-Type is required");
        
        String mimeType = ct.toString();
        
        if(MimeTypeHelper.isMatch(MEDIA_TYPE_XHTML, mimeType))
            return createCollectionXHTML(request);
        else if(MimeTypeHelper.isMatch(MEDIA_TYPE_CALENDAR, mimeType))
            return createCollectionICSFromData(request);
        else if(MimeTypeHelper.isMatch(MEDIA_TYPE_URLENCODED, mimeType))
            return createCollectionICSFromURL(request);
        else
            return ProviderHelper.notsupported(request, "unsupported Content-Type");
    }
    
    protected ResponseContext createCollectionICSFromURL(RequestContext request) {

        try {
            String url = getNonEmptyParameter(request, "url");
            if (url == null)
                return ProviderHelper.badrequest(request, "url must be provided");
            URL calUrl = new URL(url);
            
            Calendar calendar = CalendarUtils.parseCalendar(calUrl.openConnection().getInputStream());
            if(calendar==null)
                return ProviderHelper.badrequest(request, "invalid icalendar");
            
            calendar.validate(true);
            
            return createCollectionICSCommon(request, calendar);
            
        } catch(MalformedURLException e) {
            return ProviderHelper.badrequest(request, "invalid URL");
        } catch(UnsupportedEncodingException e) {
            return ProviderHelper.badrequest(request, "displayName and icalendar must be provided");
        } catch (IOException e) {
            String reason = "Unable to read URL content: " + e.getMessage();
            return ProviderHelper.badrequest(request, reason);
        } catch(ParserException e) {
            return ProviderHelper.badrequest(request, "invalid icalendar: " + e.getMessage());
        } catch(net.fortuna.ical4j.model.ValidationException e) {
            return ProviderHelper.badrequest(request, "invalid icalendar: " + e.getMessage());
        }
    }
    
    protected ResponseContext createCollectionICSFromData(RequestContext request) {

        try {
            // parse and validate icalendar
            Calendar calendar = CalendarUtils.parseCalendar(request.getReader());
            if(calendar==null)
                return ProviderHelper.badrequest(request, "invalid icalendar");
            
            calendar.validate(true);
            return createCollectionICSCommon(request, calendar);
            
        } catch(UnsupportedEncodingException e) {
            return ProviderHelper.badrequest(request, "displayName and icalendar must be provided");
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch(ParserException e) {
            return ProviderHelper.badrequest(request, "invalid icalendar: " + e.getMessage());
        } catch(net.fortuna.ical4j.model.ValidationException e) {
            return ProviderHelper.badrequest(request, "invalid icalendar: " + e.getMessage());
        }
    }
    
    protected ResponseContext createCollectionICSCommon(RequestContext request, Calendar calendar) {
        
        NewCollectionTarget target = (NewCollectionTarget) request.getTarget();
        User user = target.getUser();
        HomeCollectionItem home = target.getHomeCollection();

        if (log.isDebugEnabled())
            log.debug("creating collection from icalendar in home collection of user '" + user.getUsername() + "'");

        try {
            String displayName = target.getDisplayName();
           
            if(displayName==null)
                return ProviderHelper.badrequest(request, "displayName must be provided");
            
            // create new collection
            CollectionItem collection = getEntityFactory().createCollection();
            collection.setDisplayName(displayName);
            collection.setOwner(user);
            CalendarCollectionStamp stamp = getEntityFactory()
                    .createCalendarCollectionStamp(collection);

            stamp.setDescription(collection.getDisplayName());
            // XXX set the calendar language from Content-Language
            collection.addStamp(stamp);
            
            // add child items by converting icalendar calendar
            Set<Item> children = new HashSet<Item>();
            
            for (ContentItem child : new EntityConverter(getEntityFactory())
                    .convertCalendar(calendar)) {
                child.setOwner(user);
                child.setLastModifiedBy(user.getEmail());
                children.add(child);
            }
            
            // use api that creates collection and children
            collection = getContentService().createCollection(home, collection,
                    children);

            ServiceLocator locator = createServiceLocator(request);

            return created(collection, locator);
            
        } catch(ModelValidationException e) {
            return ProviderHelper.badrequest(request, "invalid icalendar: " + e.getMessage());
        }
    }
    
    protected ResponseContext createCollectionXHTML(RequestContext request) {
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
            return ProviderHelper.servererror(request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid request content: " + e.getMessage();
            if (e.getCause() != null)
                msg += ": " + e.getCause().getMessage();
            return ProviderHelper.badrequest(request, msg);
        } catch (UidInUseException e) {
            return ProviderHelper.conflict(request, "Uid already in use");
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }

    @Override
    public ResponseContext extensionRequest(RequestContext request) {
        // POST to a NewCollectionTarget
        if(isCreateCollectionRequest(request))
            return postCollection(request);
        // POST to a CollectionTarget with media type application/x-www-form-urlencoded
        else if(isAddItemToCollectionRequest(request))
            return addItemToCollection(request);
        else
            return super.extensionRequest(request);
    }

    public ResponseContext putCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        if (collection instanceof HomeCollectionItem)
            return ProviderHelper.unauthorized(request, "Home collection is not modifiable");

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
            return ProviderHelper.servererror(request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid request content: " + e.getMessage();
            if (e.getCause() != null)
                msg += ": " + e.getCause().getMessage();
            return ProviderHelper.badrequest(request, msg);
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }

    public ResponseContext deleteCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        if (collection instanceof HomeCollectionItem)
            return ProviderHelper.unauthorized(request, "Home collection is not deleteable");

        if (log.isDebugEnabled())
            log.debug("deleting collection " + collection.getUid());

        try {
            contentService.removeCollection(collection);

            return deleted();
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
        }
    }
    
    // our methods
    @Override
    public ResponseContext headEntry(RequestContext request) {
        ItemTarget target = (ItemTarget) request.getTarget();
        NoteItem note = target.getItem();
        EmptyResponseContext rc = new EmptyResponseContext(200);
        rc.setEntityTag(new EntityTag(note.getEntityTag()));
        rc.setLastModified(note.getModifiedDate());
        return rc;
    }

    protected ResponseContext addItemToCollection(RequestContext request) {
        CollectionTarget target = (CollectionTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("adding item to collection " + collection.getUid());

        ResponseContext frc = checkAddItemToCollectionPreconditions(request);
        if (frc != null)
            return frc;

        try {
            String uuid = getNonEmptyParameter(request, "uuid");
            if (uuid == null)
                return ProviderHelper.badrequest(request, "Uuid must be provided");

            Item item = contentService.findItemByUid(uuid);
            if (item == null)
                return ProviderHelper.badrequest(request, "Item with uuid " + uuid + " not found");
            if (! (item instanceof NoteItem))
                return ProviderHelper.badrequest(request, "Item with uuid " + uuid + " is not a note");

            //
            // FIXME
            // for now can only check if item owner is collection owner
            
            // if item owner is collection owner, then item will be updateable,
            // otherwise it will be read-only for now, until we support
            // multiple tickets vs single ticket/single principal
            if(item.getOwner().equals(collection.getOwner()))
                contentService.addItemToCollection(item, collection);
            else {
                // return forbidden
                return ProviderHelper.forbidden(request, "unauthorized for item "
                        + item.getUid());
            }
               
            return createResponseContext(204);
        } catch (CollectionLockedException e) {
            return locked(request);
        } catch (CosmoSecurityException e) {
            if(e instanceof ItemSecurityException)
                return insufficientPrivileges(request, new InsufficientPrivilegesException((ItemSecurityException) e));
            else
                return ProviderHelper.forbidden(request, e.getMessage());
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
    
    private boolean isCreateCollectionRequest(RequestContext request) {
        if(!(request.getTarget() instanceof NewCollectionTarget))
            return false;
        
        return request.getMethod().equalsIgnoreCase("POST");
    }
    
    private boolean isAddItemToCollectionRequest(RequestContext request) {
        if(!(request.getTarget() instanceof CollectionTarget))
            return false;
        
        MimeType ct = request.getContentType();
        if (ct == null)
            return false;
        return MimeTypeHelper.isMatch(MEDIA_TYPE_URLENCODED, ct.toString());
    }

    private ResponseContext checkAddItemToCollectionPreconditions(RequestContext request) {
        int contentLength = Integer.valueOf(request.getProperty(RequestContext.Property.CONTENTLENGTH).toString());
        if (contentLength <= 0)
            return lengthrequired(request);

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
                       
                        // ignore modifications without event stamp
                        if(StampUtils.getEventExceptionStamp(mod)==null)
                            continue;
                        
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
                    item = (NoteItem) contentService.updateContent((ContentItem) item);
                }
            }
        } else {
            // use simple update
            item = (NoteItem) contentService.updateContent((ContentItem) item);
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
        org.apache.abdera.protocol.server.context.AbstractResponseContext rc = createResponseContext(201, "Created");

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
