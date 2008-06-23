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
import java.text.ParseException;

import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.atom.generator.GeneratorException;
import org.osaf.cosmo.atom.generator.TicketsFeedGenerator;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.text.XhtmlTicketFormat;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.ContentService;

public class TicketsCollectionAdapter extends BaseCollectionAdapter
    implements AtomConstants {
    private static final Log log = LogFactory.getLog(TicketsCollectionAdapter.class);
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "POST", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "GET", "HEAD", "OPTIONS" };

    private ContentService contentService;
    private CosmoSecurityManager securityManager;

    // Provider methods

    public ResponseContext postEntry(RequestContext request) {
        TicketsTarget target = (TicketsTarget) request.getTarget();
        CollectionItem collection = target.getCollection();

        ResponseContext frc = checkEntryWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            Ticket ticket = readTicket(request);
            ticket.setOwner(securityManager.getSecurityContext().getUser());
            if (contentService.getTicket(collection, ticket.getKey()) != null)
                return ProviderHelper.conflict(request, "Ticket exists on " + 
                                collection.getDisplayName());

            if (log.isDebugEnabled())
                log.debug("creating ticket " + ticket.getKey() +
                          " for collection " + collection.getDisplayName());

            contentService.createTicket(collection, ticket);
            collection = contentService.updateCollection(collection);
            

            ServiceLocator locator = createServiceLocator(request);
            TicketsFeedGenerator generator =
                createTicketsFeedGenerator(locator);
            Entry entry = generator.generateEntry(collection, ticket);
            
            return created(request, entry, ticket, locator);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid entry: " + e.getMessage();
            if (e.getCause() != null)
                msg += e.getCause().getMessage();
            return ProviderHelper.badrequest(request, msg);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        TicketTarget target = (TicketTarget) request.getTarget();
        CollectionItem collection= target.getCollection();
        Ticket ticket = target.getTicket();
        if (log.isDebugEnabled())
            log.debug("deleting entry for ticket " + ticket.getKey() +
                      " for collection " + collection.getDisplayName());
        contentService.removeTicket(collection, ticket);
        contentService.updateCollection(collection);

        return deleted();
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext putEntry(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getService(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getFeed(RequestContext request) {
        TicketsTarget target = (TicketsTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        if (log.isDebugEnabled())
            log.debug("getting tickets feed for collection " +
                      collection.getDisplayName());

        try {
            ServiceLocator locator = createServiceLocator(request);
            TicketsFeedGenerator generator =
                createTicketsFeedGenerator(locator);
            Feed feed = generator.generateFeed(collection);

            return ok(request, feed);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    public ResponseContext getEntry(RequestContext request) {
        TicketTarget target = (TicketTarget) request.getTarget();
        CollectionItem collection = target.getCollection();
        Ticket ticket = target.getTicket();
        if (log.isDebugEnabled())
            log.debug("getting entry for ticket " +
                      ticket.getKey() + " for collection " + collection.getDisplayName());

        try {
            ServiceLocator locator = createServiceLocator(request);
            TicketsFeedGenerator generator =
                createTicketsFeedGenerator(locator);
            Entry entry = generator.generateEntry(collection, ticket);

            return ok(request, entry, ticket);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }
  
    public ResponseContext getMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getCategories(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    
    // ExtendedProvider methods

    public ResponseContext putCollection(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_COLL_METHODS);
    }

    public ResponseContext postCollection(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_COLL_METHODS);
    }

    public ResponseContext deleteCollection(RequestContext request) {
        return ProviderHelper.notallowed(request, ALLOWED_COLL_METHODS);
    }

    // our methods

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
    
    public void init() {
        super.init();
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (securityManager == null)
            throw new IllegalStateException("securityManager is required");
    }

    protected TicketsFeedGenerator
        createTicketsFeedGenerator(ServiceLocator locator) {
        return getGeneratorFactory().
            createTicketsFeedGenerator(locator);
    }

    private Ticket readTicket(RequestContext request)
        throws IOException, ValidationException {
        Entry entry = (Entry) request.getDocument().getRoot();
        if (entry.getContentType() == null ||
            ! entry.getContentType().equals(Content.Type.XHTML))
            throw new ValidationException("Content must be XHTML");

        try {
            XhtmlTicketFormat formatter = new XhtmlTicketFormat();
            Ticket ticket = formatter.parse(entry.getContent(), getEntityFactory());
            if (ticket.getKey() == null)
                throw new ValidationException("Ticket requires a key");
            if (ticket.getType() == null)
                throw new ValidationException("Ticket requires a type");
            return ticket;
        } catch (ParseException e) {
            throw new ValidationException("Error parsing XHTML content", e);
        }
    }

    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
