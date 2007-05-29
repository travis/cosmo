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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.server.UserPath;

/**
 * <p>
 * This class represents the path info portion of a user URL that
 * addresses a particular subscription.
 * </p>
 * <p>
 * A subscription path info matches the pattern
 * <code>/subscription/&lt;display name&gt;</code>.
 * </p>
 */
public class SubscriptionPathInfo {
    private static final Log log =
        LogFactory.getLog(SubscriptionPathInfo.class);
    private static final Pattern ONE_PATTERN =
        Pattern.compile("^/subscription/(.+)?$");

    private UserPath userPath;
    private String displayName;

    /**
     * Constructs a <code>SubscriptionPathInfo</code> instance based
     * on the path info of a <code>UserPath</code>.
     *
     * @param userPath the user path
     *
     * @throws IllegalStateException if the path info of the given
     * user path is null or does not represent a subscription path
     */
    public SubscriptionPathInfo(UserPath userPath) {
        if (userPath.getPathInfo() == null)
            throw new IllegalStateException("path info is null");

        this.userPath = userPath;

        Matcher matcher = ONE_PATTERN.matcher(userPath.getPathInfo());
        if (! matcher.matches())
            throw new IllegalStateException("not a subscription path info");

        try {
            this.displayName = URLDecoder.decode(matcher.group(1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Can't decode path info in UTF-8", e);
        }
    }

    /** */
    public String getDisplayName() {
        return displayName;
    }

    /** */
    public UserPath getUserPath() {
        return userPath;
    }

    /**
     * Returns an instance of <code>SubscriptionPath</code> based on
     * the provided <code>UserPath</code>.
     *
     * @param userPath the user path
     * @return an instance of <code>SubscriptionPath</code> if the
     * path info of the given user path represents a subscription
     * path, or <code>null</code> otherwise.
     */
    public static SubscriptionPathInfo parse(UserPath userPath) {
        try {
            return new SubscriptionPathInfo(userPath);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
