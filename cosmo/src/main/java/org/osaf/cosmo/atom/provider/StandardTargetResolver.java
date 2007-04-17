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

import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;

/**
 * Resolves the request to a resource target. The request URI can
 * identify either a collection or an item.
 */
public class StandardTargetResolver implements TargetResolver {
    private static final Log log =
        LogFactory.getLog(StandardTargetResolver.class);

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

        ItemPath ip = ItemPath.parse(uri);
        if (ip != null)
            return createItemTarget(context, ip);

        return null;
    }

    /**
     * Not actually used for anything.
     * @throws UnsupportedOperationException
     */
    public void setContextPath(String contextPath) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a target representing a collection. All targets are of
     * type {@ink TargetType.COLLECTION}.
     */
    protected Target createCollectionTarget(RequestContext context,
                                            CollectionPath path) {
        String projection = null;
        String format = null;
        if (path.getPathInfo() != null &&
            path.getPathInfo() != "/") {
            String[] segments = path.getPathInfo().substring(1).split("/");
            projection = segments[0];
            if (segments.length > 1)
                format = segments[1];
        }
        return new CollectionTarget(context, path.getUid(), projection, format);
    }

    /**
     * Creates a target representing a non-collection item. All
     * targets are of type {@link TargetType.ENTRY}.
     */
    protected Target createItemTarget(RequestContext context,
                                      ItemPath path) {
        // XXX check content type - could be media
        return new ItemTarget(TargetType.TYPE_ENTRY, context, path.getUid());
    }
}
