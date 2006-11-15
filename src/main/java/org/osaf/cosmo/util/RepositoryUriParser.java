/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.util;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper that understands the Cosmo uid- and path-based URI schemes
 * for addressing items in the repository.
 * <p>
 * This class is stateful. It allows the caller to ask sequences of
 * questions like "Is this a collection URI?" and "What is the
 * collection uid?" without having to re-parse the URI to answer each
 * question.
 * </p>
 * <p>
 * The parser operates on servlet-relative URIs. These URI schemes are
 * understood:
 * </p>
 * <dl>
 * <dt> Collection </dt>
 * <dd> <code>/collection/&lt;uid&gt;</code> </dd>
 * <dt> Path </dt>
 * <dd> anything that does not match one of the above schemes
 * </dl>
 * <p>
 * URIs that address items by UID may contain <em>path selectors</em>
 * which indicate particular (usually protocol- or data
 * format-specific) semantics. For example, including the selector
 * <code>xcal</code> in a collection URI (eg
 * <code>/collection/&lt;uid&gt;/xcal&gt;</code> might indicate that
 * the client wishes to receive the response data in the xCalendar
 * data format. This class does not attempt to interpret path
 * selectors; it simply determines what selectors are included in the
 * URI.
 * </p>
 */
public class RepositoryUriParser {
    private static final Log log =
        LogFactory.getLog(RepositoryUriParser.class);

    private static final Pattern PATTERN_COLLECTION_UID =
        Pattern.compile("^/collection/([^/]+)(/.*)?$");

    private String uri;
    private Matcher collectionMatcher;
    private HashSet pathSelectors;

    /**
     * Constructs a parser instance and parses the given uri.
     *
     * @param uri the servlet-relative uri
     *
     * @throws IllegalArgumentException if the given uri is not
     * servlet-relative (starts with a "/")
     */
    public RepositoryUriParser(String uri) {
        if (! uri.startsWith("/"))
            throw new IllegalArgumentException("uri must be servlet-relative");

        this.uri = uri;

        this.collectionMatcher = PATTERN_COLLECTION_UID.matcher(uri);

        this.pathSelectors = new HashSet();
        String extra = null;
        if (collectionMatcher.matches())
            extra = collectionMatcher.group(2);
        if (extra != null && extra != "/") {
            String [] chunks = extra.split("/");
            for (String chunk : chunks)
                pathSelectors.add(chunk);
        }
    }

    /**
     * Determines whether or not the uri addresses a collection by
     * uid.
     *
     * @param allowSelectors if <code>false</code>, the presence of
     * path selectors causes the method to return <code>false</code>
     * even if it otherwise would return <code>true</code>
     *
     * @return <code>true</code> if it is a collection uri,
     * <code>false</code> otherwise
     */
    public boolean isCollectionUri(boolean allowSelectors) {
        if (! collectionMatcher.matches())
            return false;
        if (! allowSelectors && ! pathSelectors.isEmpty())
            return false;
        return true;
    }

    /**
     * Determines whether or not the uri addresses a collection by
     * uid. Does not allow path selectors.
     *
     * @return <code>true</code> if it is a collection uri,
     * <code>false</code> otherwise
     */
    public boolean isCollectionUri() {
        return isCollectionUri(false);
    }

    /**
     * Returns the collection uid if the uri addresses a collection by
     * uid.
     *
     * @param allowSelectors if <code>false</code>, the presence of
     * path selectors causes the method to return <code>null</code>
     * even if it otherwise would return a uri.
     *
     * @return the collection uid, or <code>null</code> if it is not a
     * collection uri
     */
    public String getCollectionUid(boolean allowSelectors) {
        return isCollectionUri(allowSelectors) ?
            collectionMatcher.group(1) :
            null;
    }

    /**
     * Returns the collection uid if the uri addresses a collection by
     * uid. Does not allow path selectors.
     *
     * @return the collection uid, or <code>null</code> if it is not a
     * collection uri
     */
    public String getCollectionUid() {
        return getCollectionUid(false);
    }

    /**
     * Determines whether or not the uri addresses a collection by
     * path.
     *
     * @return <code>true</code> if it is a path uri,
     * <code>false</code> otherwise
     */
    public boolean isPathUri() {
        return ! collectionMatcher.matches();
    }

    /**
     * Returns the item path if the uri addresses an item by path.
     *
     * @return the item path, or <code>null</code> if it is not a
     * path uri
     */
    public String getItemPath() {
        return ! collectionMatcher.matches() ? uri : null;
    }

    /**
     * Determines whether or not the named path selector is present in
     * the uri.
     *
     * @return <code>true</code> if the uri address an item by uid and
     * the path selector is present in the uri, <code>false</code>
     * otherwise
     */
    public boolean getPathSelector(String name) {
        if (! collectionMatcher.matches())
            return false;
        return pathSelectors.contains(name);
    }
}
