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

import org.apache.xerces.util.XMLChar;

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

    // XXX: replace hex* apis with ones that convert client paths to
    // and from repository paths without describing the encoding
    // technique. perhaps use the strategy pattern to allow either hex
    // or ISO 9075 encoding to be used.

    public static String hexEscapeJcrNames(String str) {
        return hexEscape(str);
    }

    public static String hexEscapeJcrPath(String str) {
        if (str.equals("/")) {
            return str;
        }

        StringBuffer buf = new StringBuffer();

        String[] names = str.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(hexEscape(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    public static String hexUnescapeJcrNames(String str) {
        return hexUnescape(str);
    }

    public static String hexUnescapeJcrPath(String str) {
        if (str.equals("/")) {
            return str;
        }

        StringBuffer buf = new StringBuffer();

        String[] names = str.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(hexUnescape(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    // private methods

    private static String hexEscape(String str) {
        StringBuffer buf = null;
        int length = str.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            int ch = str.charAt(i);
            switch (ch) {
            case '/':
            case ':':
            case '[':
            case ']':
            case '*':
            case '"':
            case '|':
            case '\'':
                if (buf == null) {
                    buf = new StringBuffer();
                }
                if (i > 0) {
                    buf.append(str.substring(pos, i));
                }
                pos = i + 1;
                break;
            default:
                continue;
            }
            buf.append("%").append(Integer.toHexString(ch));
        }
        
        if (buf == null) {
            return str;
        }

        if (pos < length) {
            buf.append(str.substring(pos));
        }
        return buf.toString();
    }

    private static String hexUnescape(String str) {
        StringBuffer buf = null;
        int length = str.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            int ch = str.charAt(i);
            switch (ch) {
            case '%':
                if (i+3 > length) {
                    continue;
                }
                if (buf == null) {
                    buf = new StringBuffer();
                }
                if (i > 0) {
                    buf.append(str.substring(pos, i));
                }
                pos = i + 1;
                break;
            default:
                continue;
            }
            if (ch == '%') {
                String hex = str.substring(i+1, i+3);
                if (hex.equals("2f")) {
                    buf.append("/");
                }
                else if (hex.equals("3a")) {
                    buf.append(":");
                }
                else if (hex.equals("5b")) {
                    buf.append("[");
                }
                else if (hex.equals("5d")) {
                    buf.append("]");
                }
                else if (hex.equals("2a")) {
                    buf.append("*");
                }
                else if (hex.equals("22")) {
                    buf.append("\"");
                }
                else if (hex.equals("7c")) {
                    buf.append("|");
                }
                else if (hex.equals("27")) {
                    buf.append("'");
                }
                else {
                    continue;
                }
                i += 2;
                pos = i + 1;
            }
        }
        if (buf == null) {
            return str;
        }

        if (pos < length) {
            buf.append(str.substring(pos));
        }
        return buf.toString();
    }
}
