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
import org.osaf.cosmo.atom.generator.SubscriptionFeedGenerator;
import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.text.XhtmlSubscriptionFormat;
import org.osaf.cosmo.server.ServiceLocator;
import org.osaf.cosmo.service.UserService;

public class SubscriptionCollectionAdapter extends BaseCollectionAdapter
    implements AtomConstants {
    private static final Log log =
        LogFactory.getLog(SubscriptionCollectionAdapter.class);
    private static final String[] ALLOWED_COLL_METHODS =
        new String[] { "GET", "HEAD", "POST", "OPTIONS" };
    private static final String[] ALLOWED_ENTRY_METHODS =
        new String[] { "GET", "HEAD", "PUT", "OPTIONS" };

    private UserService userService;

    // Provider methods

    public ResponseContext postEntry(RequestContext request) {
        SubscriptionsTarget target = (SubscriptionsTarget) request.getTarget();
        User user = target.getUser();

        ResponseContext frc = checkEntryWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            CollectionSubscription sub = readSubscription(request);

            if (user.getSubscription(sub.getDisplayName()) != null)
                return ProviderHelper.conflict(request,
                                "Named subscription exists");

            if (log.isDebugEnabled())
                log.debug("creating subscription " + sub.getDisplayName() +
                          " for user " + user.getUsername());

            user.addSubscription(sub);
            user = userService.updateUser(user);

            ServiceLocator locator = createServiceLocator(request);
            SubscriptionFeedGenerator generator =
                createSubscriptionFeedGenerator(locator);
            Entry entry = generator.generateEntry(sub);

            return created(request, entry, sub, locator);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid entry: " + e.getMessage();
            if (e.getCause() != null)
                msg += ": " + e.getCause().getMessage();
            return ProviderHelper.badrequest(request, msg);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    public ResponseContext deleteEntry(RequestContext request) {
        SubscriptionTarget target = (SubscriptionTarget) request.getTarget();
        User user = target.getUser();
        CollectionSubscription sub = target.getSubscription();
        if (log.isDebugEnabled())
            log.debug("deleting entry for subscription " +
                      sub.getDisplayName() + " for user " + user.getUsername());

        user.removeSubscription(sub);
        userService.updateUser(user);

        return deleted();
    }
  
    public ResponseContext deleteMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext putEntry(RequestContext request) {
        SubscriptionTarget target = (SubscriptionTarget) request.getTarget();
        User user = target.getUser();
        CollectionSubscription sub = target.getSubscription();
        if (log.isDebugEnabled())
            log.debug("upudating subscription " + sub.getDisplayName() +
                      " for user " + user.getUsername());

        ResponseContext frc = checkEntryWritePreconditions(request);
        if (frc != null)
            return frc;

        try {
            CollectionSubscription newsub = readSubscription(request);

            if (user.getSubscription(newsub.getDisplayName()) != null &&
                ! user.getSubscription(newsub.getDisplayName()).equals(sub))
                return ProviderHelper.conflict(request,
                                "Named subscription exists");

            String oldName = sub.getDisplayName();
            sub.setDisplayName(newsub.getDisplayName());
            sub.setCollectionUid(newsub.getCollectionUid());
            sub.setTicketKey(newsub.getTicketKey());

            user = userService.updateUser(user);

            ServiceLocator locator = createServiceLocator(request);
            SubscriptionFeedGenerator generator =
                createSubscriptionFeedGenerator(locator);
            Entry entry = generator.generateEntry(sub);

            boolean locationChanged = ! oldName.equals(sub.getDisplayName());
            return updated(request, entry, sub, locator, locationChanged);
        } catch (IOException e) {
            String reason = "Unable to read request content: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        } catch (ValidationException e) {
            String msg = "Invalid entry: " + e.getMessage();
            if (e.getCause() != null)
                log.error(msg, e.getCause());
            return ProviderHelper.badrequest(request, msg);
        } catch (GeneratorException e) {
            String reason = "Unknown entry generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }
  
    public ResponseContext putMedia(RequestContext request) {
        throw new UnsupportedOperationException();
    }
  
    public ResponseContext getService(RequestContext request) {
        throw new UnsupportedOperationException();
    }

    public ResponseContext getFeed(RequestContext request) {
        SubscriptionsTarget target = (SubscriptionsTarget) request.getTarget();
        User user = target.getUser();
        if (log.isDebugEnabled())
            log.debug("getting subscriptions feed for user " +
                      user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            SubscriptionFeedGenerator generator =
                createSubscriptionFeedGenerator(locator);
            Feed feed = generator.generateFeed(user);

            return ok(request, feed);
        } catch (GeneratorException e) {
            String reason = "Unknown feed generation error: " + e.getMessage();
            log.error(reason, e);
            return ProviderHelper.servererror(request, reason, e);
        }
    }

    public ResponseContext getEntry(RequestContext request) {
        SubscriptionTarget target = (SubscriptionTarget) request.getTarget();
        User user = target.getUser();
        CollectionSubscription sub = target.getSubscription();
        if (log.isDebugEnabled())
            log.debug("getting entry for subscription " +
                      sub.getDisplayName() + " for user " + user.getUsername());

        try {
            ServiceLocator locator = createServiceLocator(request);
            SubscriptionFeedGenerator generator =
                createSubscriptionFeedGenerator(locator);
            Entry entry = generator.generateEntry(sub);

            return ok(request, entry, sub);
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

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void init() {
        super.init();
        if (userService == null)
            throw new IllegalStateException("userService is required");
    }

    protected SubscriptionFeedGenerator
        createSubscriptionFeedGenerator(ServiceLocator locator) {
        return getGeneratorFactory().
            createSubscriptionFeedGenerator(locator);
    }

    private CollectionSubscription readSubscription(RequestContext request)
        throws IOException, ValidationException {
        Entry entry = (Entry) request.getDocument().getRoot();
        if (entry.getContentType() == null ||
            ! entry.getContentType().equals(Content.Type.XHTML))
            throw new ValidationException("Content must be XHTML");

        try {
            XhtmlSubscriptionFormat formatter = new XhtmlSubscriptionFormat();
            CollectionSubscription sub = formatter.parse(entry.getContent(), getEntityFactory());
            if (sub.getDisplayName() == null)
                throw new ValidationException("Subscription requires a display name");
            if (sub.getCollectionUid() == null)
                throw new ValidationException("Subscription requires a collection uuid");
            if (sub.getTicketKey() == null)
                throw new ValidationException("Subscription requires a ticket key");
            return sub;
        } catch (ParseException e) {
            throw new ValidationException("Error parsing XHTML content", e);
        }
    }
}
