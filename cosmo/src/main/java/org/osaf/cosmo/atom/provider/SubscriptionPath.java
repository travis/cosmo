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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.server.UserPath;

/**
 * <p>
 * This class represents a URL that addresses a user's subscriptions.
 * </p>
 * <p>
 * A subscription URL is simply a user URL with path info matching
 * one of these two possibilities:
 * </p>
 * <ul>
 * <li><code>/subscription</code></li>
 * <li><code>/subscription/&lt;display name&gt;</code></li>
 * </ul>
 *
 * @see UserPath
 */
public class SubscriptionPath extends UserPath {
    private static final Log log = LogFactory.getLog(SubscriptionPath.class);
    private static final Pattern PATTERN =
        Pattern.compile("^/subscription(/.*)?$");

    private String displayName;

    /**
     * Constructs a <code>SubscriptionPath</code> instance based on
     * the servlet-relative component of the url-path from a user
     * URL.
     *
     * @param urlPath the servlet-relative url-path
     *
     * @throws IllegalArgumentException if the given url-path is not
     * servlet-relative (starts with a "/")
     * @throws IllegalStateException if the given url-path does not
     * represent a subscription path
     */
    public SubscriptionPath(String urlPath) {
        super(urlPath);

        Matcher matcher = PATTERN.matcher(getPathInfo());
        if (! matcher.matches())
            throw new IllegalStateException("urlPath is not a subscription path");

        this.displayName = matcher.group(1);
    }

    /** */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses the given url-path, returning an instance of
     * <code>SubscriptionPath</code>.
     *
     * @param urlPath the servlet-relative url-path
     * @return an instance of <code>SubscriptionPath</code> if the
     * url-path represents a subscription path, or <code>null</code>
     * otherwise.
     *
     * @throws IllegalArgumentException if the given url-path is not
     * servlet-relative (starts with a "/")
     */
    public static SubscriptionPath parse(String urlPath) {
        if (urlPath == null)
            return null;
        try {
            return new SubscriptionPath(urlPath);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
