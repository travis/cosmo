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
package org.osaf.cosmo.server;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents the portion of a collection URL that
 * identifies the collection by UID as well as any metadata that
 * provides extra information to the service providing access to the
 * collection.
 * <p>
 * Collections in the Cosmo database are addressed similarly
 * regardless of which service is used to access the data. A
 * collection URL includes the service mount URL, the literal path
 * component <code>collection</code>, and the UID of the collection.
 * <p>
 * For example, the URL
 * <code>http://localhost:8080/cosmo/dav/collection/8501de14-1dc9-40d4-a7d4-f289feff8214</code>
 * is the WebDAV service address for the collection with the specified
 * UID.
 *
 * <h2>Path Selectors</h2>
 *
 * Collection URLs may contain <em>path selectors</em> which indicate
 * particular (usually protocol- or data format-specific)
 * semantics. Each component of the URL trailing the collection's UID
 * is interpreted as a separate path selector.
 * <p>
 * For example, including the selector <code>xcal</code> in a
 * collection URL (e.g. one with the path
 * <code>/collection/&lt;uid&gt;/xcal&gt;</code>) might indicate that
 * the client wishes to receive the WebDAV response data in the
 * xCalendar data format.
 * <p>
 * This class does not attempt to interpret path selectors; it simply
 * determines what selectors are included in the path.
 */
public class CollectionPath {
    private static final Log log = LogFactory.getLog(CollectionPath.class);

    private static final Pattern PATTERN_COLLECTION_UID =
        Pattern.compile("^/collection/([^/]+)(/.*)?$");

    private String urlPath;
    private String uid;
    private HashSet<String> selectors;

    /**
     * Constructs a <code>CollectionPath</code> instance based on the
     * servlet-relative component of the url-path from a collection
     * URL.
     *
     * @param urlPath the servlet-relative url-path
     *
     * @throws IllegalArgumentException if the given url-path is not
     * servlet-relative (starts with a "/")
     * @throws IllegalStateException if the given url-path does not
     * represent a collection path
     */
    public CollectionPath(String urlPath) {
        if (! urlPath.startsWith("/"))
            throw new IllegalArgumentException("urlPath must start with /");

        this.urlPath = urlPath;

        Matcher collectionMatcher = PATTERN_COLLECTION_UID.matcher(urlPath);
        if (! collectionMatcher.matches())
            throw new IllegalStateException("urlPath is not a collection path");
        this.uid = collectionMatcher.group(1);

        this.selectors = new HashSet<String>();
        String extra = collectionMatcher.group(2);
        if (extra != null && extra != "/") {
            String [] chunks = extra.split("/");
            for (String chunk : chunks)
                selectors.add(chunk);
        }
    }

    /** */
    public String getUrlPath() {
        return urlPath;
    }

    /** */
    public String getUid() {
        return uid;
    }

    /** */
    public Set<String> getSelectors() {
        return selectors;
    }

    /**
     * Determines whether or not the named path selector is present in
     * the url.
     *
     * @return <code>true</code> if the url address an item by uid and
     * the path selector is present in the url, <code>false</code>
     * otherwise
     */
    public boolean getSelector(String name) {
        return selectors.contains(name);
    }

    /**
     * Parses the given url-path, returning an instance of
     * <code>CollectionPath</code>. Path selectors are disallowed.
     *
     * @param urlPath the servlet-relative url-path
     * @return an instance of <code>CollectionPath</code> if the
     * url-path represents a collection path, or <code>null</code>
     * otherwise.
     *
     * @throws IllegalArgumentException if the given url-path is not
     * servlet-relative (starts with a "/") or if disallowed path
     * selectors are present
     */
    public static CollectionPath parse(String urlPath) {
        return parse(urlPath, false);
    }

    /**
     * Parses the given url-path, returning an instance of
     * <code>CollectionPath</code>.
     *
     * @param urlPath the servlet-relative url-path
     * @param allowSelectors determines whether or not path selectors
     * are allowed in the path
     * @return an instance of <code>CollectionPath</code> if the
     * url-path represents a collection path, or <code>null</code>
     * otherwise.
     *
     * @throws IllegalArgumentException if the given url-path is not
     * servlet-relative (starts with a "/")
     */
    public static CollectionPath parse(String urlPath,
                                       boolean allowSelectors) {
        try {
            CollectionPath cp = new CollectionPath(urlPath);
            if (! allowSelectors && ! cp.getSelectors().isEmpty())
                return null;
            return cp;
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
