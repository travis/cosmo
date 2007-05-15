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
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;
import org.osaf.cosmo.server.UserPath;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;

/**
 * Resolves the request to a resource target. The request URI can
 * identify either a collection or an item.
 */
public class StandardTargetResolver implements TargetResolver {
    private static final Log log =
        LogFactory.getLog(StandardTargetResolver.class);

    private ContentService contentService;
    private UserService userService;

    // TargetResolver methods

    /**
     * Returns a target representing the type of item addressed by the
     * request URI.
     * <p>
     * If the path info parses into a {@link CollectionPath}, then a
     * {@link CollectionTarget} is returned.
     * <p>
     * If the path info parses into a {@link ItemPath}, then a
     * {@link ItemTarget} is returned.
     * <p>
     * Otherwise, <code>null</code> is returned.
     * <p>
     * The context path is not considered during resolution.
     */
    public Target resolve(RequestContext context) {
        String uri =
            ((HttpServletRequestContext) context).getRequest().getPathInfo();
        if (log.isDebugEnabled())
            log.debug("resolving URI " + uri);

        CollectionPath cp = CollectionPath.parse(uri, true);
        if (cp != null)
            return createCollectionTarget(context, cp);

        ItemPath ip = ItemPath.parse(uri, true);
        if (ip != null)
            return createItemTarget(context, ip);

        SubscriptionPath sp = SubscriptionPath.parse(uri);
        if (sp != null)
            return createSubscriptionTarget(context, sp);

        UserPath up = UserPath.parse(uri);
        if (up != null)
            return createUserTarget(context, up);

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
                                            CollectionPath path) {
        Item item = contentService.findItemByUid(path.getUid());
        if (item == null)
            return null;
        if (! (item instanceof CollectionItem))
            return null;

        TargetPathInfo info = new TargetPathInfo(path.getPathInfo());

        return new CollectionTarget(context, (CollectionItem) item,
                                    info.getProjection(), info.getFormat());
    }

    /**
     * Creates a target representing a non-collection item.
     */
    protected Target createItemTarget(RequestContext context,
                                      ItemPath path) {
        Item item = contentService.findItemByUid(path.getUid());
        if (item == null)
            return null;
        if (! (item instanceof NoteItem))
            return null;

        TargetPathInfo info = new TargetPathInfo(path.getPathInfo());

        return new ItemTarget(context, (NoteItem) item,
                              info.getProjection(), info.getFormat());
    }

    /**
     * Creates a target representing one or all of a user's
     * subscriptions.
     */
    protected Target createSubscriptionTarget(RequestContext context,
                                              SubscriptionPath path) {
        User user = userService.getUser(path.getUsername());
        if (user == null)
            return null;

        if (path.getDisplayName() != null) {
            CollectionSubscription sub =
                user.getSubscription(path.getDisplayName());
            if (sub == null)
                return null;
            return new SubscriptionTarget(context, user, sub);
        }

        return new SubscriptionTarget(context, user);
    }

    /**
     * Creates a target representing a user.
     */
    protected Target createUserTarget(RequestContext context,
                                      UserPath path) {
        User user = userService.getUser(path.getUsername());
        if (user == null)
            return null;
        HomeCollectionItem home = contentService.getRootItem(user);

        return new UserTarget(context, user, home);
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

    private class TargetPathInfo {
        private String projection;
        private String format;
        
        public TargetPathInfo(String pathInfo) {
            if (pathInfo != null && pathInfo != "/") {
                String[] segments = pathInfo.substring(1).split("/");
                projection = segments[0];
                if (segments.length > 1)
                    format = segments[1];
            }
        }

        public String getProjection() {
            return projection;
        }

        public String getFormat() {
            return format;
        }
    }
}
