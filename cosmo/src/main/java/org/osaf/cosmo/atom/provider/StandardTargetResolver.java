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

import org.apache.abdera.protocol.server.provider.RequestContext;
import org.apache.abdera.protocol.server.provider.TargetResolver;
import org.apache.abdera.protocol.server.provider.Target;
import org.apache.abdera.protocol.server.provider.TargetType;
import org.apache.abdera.protocol.server.servlet.HttpServletRequestContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;
import org.osaf.cosmo.server.UserPath;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.UriTemplate;

/**
 * Resolves the request to a resource target.
 */
public class StandardTargetResolver implements TargetResolver {
    private static final Log log =
        LogFactory.getLog(StandardTargetResolver.class);

    private static final UriTemplate TEMPLATE_COLLECTION =
        new UriTemplate("/collection/{uid}/{projection}?/{format}?");
    private static final UriTemplate TEMPLATE_ITEM =
        new UriTemplate("/item/{uid}/{projection}?/{format}?");
    private static final UriTemplate TEMPLATE_SUBSCRIBED =
        new UriTemplate("/user/{username}/subscribed");
    private static final UriTemplate TEMPLATE_SUBSCRIPTION =
        new UriTemplate("/user/{username}/subscription/{name}");
    private static final UriTemplate TEMPLATE_PREFERENCES =
        new UriTemplate("/user/{username}/preferences");
    private static final UriTemplate TEMPLATE_PREFERENCE =
        new UriTemplate("/user/{username}/preference/{name}");
    private static final UriTemplate TEMPLATE_SERVICE =
        new UriTemplate("/user/{username}");

    private ContentService contentService;
    private UserService userService;

    // TargetResolver methods

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
     * @param context the request context
     * @return the target of the request, or <code>null</code>
     */
    public Target resolve(RequestContext context) {
        String uri =
            ((HttpServletRequestContext) context).getRequest().getPathInfo();
        if (log.isDebugEnabled())
            log.debug("resolving URI " + uri);

        UriTemplate.Match match = null;

        match = TEMPLATE_COLLECTION.match(uri);
        if (match != null)
            return createCollectionTarget(context, match);

        match = TEMPLATE_ITEM.match(uri);
        if (match != null)
            return createItemTarget(context, match);

        match = TEMPLATE_SUBSCRIBED.match(uri);
        if (match != null)
            return createSubscribedTarget(context, match);

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
        if (match != null)
            return createServiceTarget(context, match);

        return null;
    }

    /**
     * Not actually used for anything.
     * @throws UnsupportedOperationException
     */
    public void setContextPath(String contextPath) {
        throw new UnsupportedOperationException();
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
     * Creates a target representing the subscribed collection.
     */
    protected Target createSubscribedTarget(RequestContext context,
                                            UriTemplate.Match match) {
        User user = userService.getUser(match.get("username"));
        if (user == null)
            return null;
        return new SubscribedTarget(context, user);
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
