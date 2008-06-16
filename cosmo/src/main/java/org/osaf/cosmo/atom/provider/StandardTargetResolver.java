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

import java.text.ParseException;

import org.apache.abdera.protocol.Request;
import org.apache.abdera.protocol.Resolver;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.servlet.ServletRequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomConstants;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModificationUid;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.UriTemplate;

/**
 * Resolves the request to a resource target.
 */
public class StandardTargetResolver
    implements Resolver<Target>, AtomConstants {
    private static final Log log =
        LogFactory.getLog(StandardTargetResolver.class);

    private ContentService contentService;
    private UserService userService;

    // Resolver methods

    /**
     * <p>
     * Returns a target representing the resource addressed by the
     * request.
     * </p>
     * <p>
     * Resolution involves matching the request's path info (ignoring
     * context and servlet path) against known URI templates. If a
     * match occurs, the corresponding model object is retrieved and a
     * target is returned.
     * </p>
     *
     * @param request the request
     * @return the target of the request, or <code>null</code>
     */
    public Target resolve(Request request) {
        ServletRequestContext context =
            (ServletRequestContext) request;
        String uri = context.getRequest().getPathInfo();
        if (log.isDebugEnabled())
            log.debug("resolving URI " + uri);

        UriTemplate.Match match = null;

        match = TEMPLATE_TICKETS.match(uri);
        if (match != null)
            return createTicketsTarget(context, match);

        match = TEMPLATE_TICKET.match(uri);
        if (match != null)
            return createTicketTarget(context, match);

        match = TEMPLATE_COLLECTION.match(uri);
        if (match != null)
            return createCollectionTarget(context, match);

        match = TEMPLATE_ITEM.match(uri);
        if (match != null)
            return createItemTarget(context, match);

        match = TEMPLATE_EXPANDED.match(uri);
        if (match != null)
            return createExpandedItemTarget(context, match);

        match = TEMPLATE_DETACHED.match(uri);
        if (match != null)
            return createDetachedItemTarget(context, match);

        match = TEMPLATE_SUBSCRIPTIONS.match(uri);
        if (match != null)
            return createSubscriptionsTarget(context, match);

        match = TEMPLATE_SUBSCRIPTION.match(uri);
        if (match != null)
            return createSubscriptionTarget(context, match);

        match = TEMPLATE_PREFERENCES.match(uri);
        if (match != null)
            return createPreferencesTarget(context, match);

        match = TEMPLATE_PREFERENCE.match(uri);
        if (match != null)
            return createPreferenceTarget(context, match);

        match = TEMPLATE_SERVICE.match(uri);
        if (match != null) {
            if (context.getMethod().equals("POST"))
                return createNewCollectionTarget(context, match);
            return createServiceTarget(context, match);
        }
        
        match = TEMPLATE_IMPORT_COLLECTION.match(uri);
        if (match != null)
            return createNewCollectionTarget(context, match);

        return null;
    }

    // our methods

    /**
     * Creates a target representing a collection. All targets are of
     * type {@ink TargetType.COLLECTION}.
     */
    protected Target createCollectionTarget(RequestContext context,
                                            UriTemplate.Match match) {
        Item item = contentService.findItemByUid(match.get("uid"));
        if (item == null)
            return null;
        if (! (item instanceof CollectionItem))
            return null;
        return new CollectionTarget(context, (CollectionItem) item,
                                    match.get("projection"),
                                    match.get("format"));
    }
    
    /**
     * Creates a target representing a collection of tickets
     */
    protected Target createTicketsTarget(RequestContext context,
                                         UriTemplate.Match match){
        Item item = contentService.findItemByUid(match.get("uid"));
        if (item == null)
            return null;
        if (! (item instanceof CollectionItem))
            return null;
        return new TicketsTarget(context, (CollectionItem) item);

    }
    
    /**
     * Creates a target representing a ticket
     */
    protected Target createTicketTarget(RequestContext context,
                                        UriTemplate.Match match){
        Item item = contentService.findItemByUid(match.get("uid"));
        if (item == null)
            return null;
        if (! (item instanceof CollectionItem))
            return null;
        Ticket ticket = contentService.getTicket(item, match.get("key"));
        if (ticket == null)
            return null;
        return new TicketTarget(context, (CollectionItem) item, ticket);
        
    }

    /**
     * Creates a target representing a non-collection item.
     */
    protected Target createItemTarget(RequestContext context,
                                      UriTemplate.Match match) {
        Item item = contentService.findItemByUid(match.get("uid"));
        if (item == null)
            return null;
        if (! (item instanceof NoteItem))
            return null;
        return new ItemTarget(context, (NoteItem) item, match.get("projection"),
                              match.get("format"));
    }

    /**
     * Creates a target representing an expanded non-collection item.
     */
    protected Target createExpandedItemTarget(RequestContext context,
                                              UriTemplate.Match match) {
        Item item = contentService.findItemByUid(match.get("uid"));
        if (item == null)
            return null;
        if (! (item instanceof NoteItem))
            return null;
        return new ExpandedItemTarget(context, (NoteItem) item,
                                      match.get("projection"),
                                      match.get("format"));
    }

    /**
     * Creates a target representing a detached recurring item occurrence.
     */
    protected Target createDetachedItemTarget(RequestContext context,
                                              UriTemplate.Match match) {
        Item master = contentService.findItemByUid(match.get("uid"));
        if (master == null)
            return null;
        if (! (master instanceof NoteItem))
            return null;

        ModificationUid occurrenceUid = null;
        try {
            occurrenceUid =
                new ModificationUid(master, match.get("occurrence"));
        } catch (ParseException e) {
            log.warn("Error parsing occurrence date: " + e.getMessage());
            return null;
        }
        Item occurrence =
            contentService.findItemByUid(occurrenceUid.toString());
        if (occurrence == null)
            return null;
        if (! (occurrence instanceof NoteItem))
            return null;

        return new DetachedItemTarget(context, (NoteItem) master,
                                      (NoteItem) occurrence,
                                      match.get("projection"),
                                      match.get("format"));
    }

    /**
     * Creates a target representing the subscriptions collection.
     */
    protected Target createSubscriptionsTarget(RequestContext context,
                                               UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        return new SubscriptionsTarget(context, user);
    }

    /**
     * Creates a target representing a collection subscription.
     */
    protected Target createSubscriptionTarget(RequestContext context,
                                              UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        CollectionSubscription sub = user.getSubscription(match.get("name"));
        if (sub == null)
            return null;
        return new SubscriptionTarget(context, user, sub);
    }

    /**
     * Creates a target representing the preferences collection.
     */
    protected Target createPreferencesTarget(RequestContext context,
                                             UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        return new PreferencesTarget(context, user);
    }

    /**
     * Creates a target representing a preference entry.
     */
    protected Target createPreferenceTarget(RequestContext context,
                                            UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        Preference pref = user.getPreference(match.get("name"));
        if (pref == null)
            return null;
        return new PreferenceTarget(context, user, pref);
    }

    /**
     */
    protected Target createNewCollectionTarget(RequestContext context,
                                               UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        HomeCollectionItem home = contentService.getRootItem(user);
        String displayName = match.get("displayName");
        return new NewCollectionTarget(context, user, home, displayName);
    }

    /**
     * Creates a target representing the user service.
     */
    protected Target createServiceTarget(RequestContext context,
                                         UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        return new UserTarget(context, user);
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void init() {
        if (contentService == null)
            throw new IllegalStateException("contentService is required");
        if (userService == null)
            throw new IllegalStateException("userService is required");
    }
}
