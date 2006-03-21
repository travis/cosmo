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
 * (eg /b/bc/bcm/Brian's Calendar/d/de/deadbeef.ics)
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
    public static String toQueryableRepositoryPath(String path) {
        if (path.equals("/")) {
            return path;
        }

        StringBuffer buf = new StringBuffer();

        String[] names = path.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(ISO9075.encode(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    /**
     * Converts a client path into a repository path, taking into
     * account the internal storage structure of the repository schema
     * and escaping characters that are illegal in JCR names.
     *
     * Note that individual path segments cannot contain the '/'
     * character, as this character is used as the path delimiter for
     * both the client and repository views.
     *
     * @see {@link HexEscaper}
     */
    public static String toRepositoryPath(String clientPath) {
        if (clientPath.equals("/")) {
            return clientPath;
        }

        StringBuffer buf = new StringBuffer();

        String[] names = clientPath.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(HexEscaper.escape(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    /**
     * Converts a repository path into a client path, removing nodes
     * that are part of the internal storage structure of the
     * repository schema and hex-unescaping path segments.
     *
     * @see {@link HexEscaper}
     */
    public static String toClientPath(String repositoryPath) {
        if (repositoryPath.equals("/")) {
            return repositoryPath;
        }

        StringBuffer buf = new StringBuffer();

        String[] names = repositoryPath.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(HexEscaper.unescape(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }
}
