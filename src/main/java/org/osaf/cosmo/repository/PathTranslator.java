/*
 * Copyright 2005 Open Source Applications Foundation
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
package org.osaf.cosmo.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.util.HexEscaper;
import org.apache.jackrabbit.util.ISO9075;

/**
 * Converts paths between the formats used by the various views of the
 * Cosmo repository.
 *
 * The "client view", which the majority of server classes work
 * with, provides the external, simple addressing of repository
 * items (eg /bcm/Brian's Calendar/deadbeef.ics)
 *
 * The "repository view", used by data access objects that
 * directly use the JCR API, in which items are addressed by valid JCR
 * paths and where storage requirements often dictate a structure that
 * is not exposed to clients
 * (eg /b/bc/bcm/Brian's Calendar/deadbeef.ics)
 */
public class PathTranslator {
    private static final Log log = LogFactory.getLog(PathTranslator.class);

    /**
     * Converts a standard repository path into one that is suitable
     * for use in JCR queries using the XPath syntax. The path is
     * escaped using the ISO 9075 scheme for converting arbitrary
     * strings into valid XML element names. This is necessary since
     * XPath queries are executed against the logical XML document
     * view of the repository.
     *
     * @see {@link org.apache.jackrabbit.util.ISO9075}
     */
    public static String toQueryableRepositoryPath(String repositoryPath) {
        if (repositoryPath.equals("/")) {
            return repositoryPath;
        }

        StringBuffer buf = new StringBuffer();

        String[] segments = repositoryPath.split("/");
        for (int i=0; i<segments.length; i++) {
            buf.append(ISO9075.encode(segments[i]));
            if (i < segments.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    /**
     * Converts a client path into a repository path, escaping
     * characters that are illegal in JCR names.
     *
     * If the path starts with a '/', it is interpreted as an absolute
     * path (one ore more path segments separated by '/'). In this
     * case, path segments cannot contain the '/' character, for
     * obvious reasons. Note that additional path segments may be
     * inserted into the path to account for the repository schema's
     * internal storage structure.
     *
     * If the path does not start with '/', it is assumed to be a
     * simple name, and the '/' character is escaped.
     *
     * @see {@link HexEscaper}
     */
    public static String toRepositoryPath(String clientPath) {
        if (clientPath.equals("/")) {
            return clientPath;
        }
        if (! clientPath.startsWith("/")) {
            return HexEscaper.escape(clientPath);
        }

        // remove the leading / that signifies the root node and add
        // it to the repo path
        clientPath = clientPath.substring(1);
        StringBuffer repositoryPath = new StringBuffer("/");

        // the first segment of the remaining client path is the
        // username. add two layers of structural segments and then
        // the username segment to the repo path
        String[] segments = clientPath.split("/");
        String username = segments[0];
        repositoryPath.
            append(username.substring(0, 1)).
            append("/").
            append(username.substring(0, 2)).
            append("/").
            append(username).
            append("/");

        // add the rest of the segments to the repo path
        for (int i=1; i<segments.length; i++) {
            repositoryPath.append(HexEscaper.escape(segments[i]));
            if (i < segments.length-1) {
                repositoryPath.append("/");
            }
        }
        
        return repositoryPath.toString();
    }

    /**
     * Converts a repository path into a client path, converting
     * escape sequences back into the original JCR-illegal
     * characters.
     *
     * If the path starts with a '/', it is interpreted as an absolute
     * path, and any segments representing internal storage nodes are
     * removed.
     *
     * @see {@link HexEscaper}
     */
    public static String toClientPath(String repositoryPath) {
        if (repositoryPath.equals("/")) {
            return repositoryPath;
        }
        if (! repositoryPath.startsWith("/")) {
            return HexEscaper.unescape(repositoryPath);
        }

        // remove the leading / that signifies the root node and add
        // it to the client path
        repositoryPath = repositoryPath.substring(1);
        StringBuffer clientPath = new StringBuffer("/");

        // the first two segments of the remaining client path are
        // structural segments that are not added to the client path
        String[] segments = repositoryPath.split("/");
        for (int i=2; i<segments.length; i++) {
            clientPath.append(HexEscaper.unescape(segments[i]));
            if (i < segments.length-1) {
                clientPath.append("/");
            }
        }

        return clientPath.toString();
    }
}
