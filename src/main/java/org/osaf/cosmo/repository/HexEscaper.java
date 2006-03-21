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

/**
 * Implements escape and unescape routines to convert client view
 * path segments into repository view path segments and vice versa
 * with a "hex-escaping" scheme similar to the one used to escape
 * illegal characters in URLs.
 *
 * Illegal characters are escaped with the '%' character followed
 * by the two-digit hexadecimal representation of the character
 * (eg ':' becomes '%3a'). These characters are escaped: '/', ':',
 * '[', ']', '*', '"', '|', '''.
 *
 * Also note that the escaping scheme ignores the following cases
 * which are also illegal in JCR names:
 *
 * <ul>
 * <li> literal '%NN' sequences (if not escaped, these sequences
 * will be incorrectly unescaped when converting the repository
 * path back to a client path) </li>
 * <li> leading '.' </li>
 * <li> whitespace other than ' ' </li>
 * </ul>
 */
public class HexEscaper {
    private static final Log log = LogFactory.getLog(HexEscaper.class);

    /**
     */
    public static String escape(String name) {
        StringBuffer buf = null;
        int length = name.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            int ch = name.charAt(i);
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
                    buf.append(name.substring(pos, i));
                }
                pos = i + 1;
                break;
            default:
                continue;
            }
            buf.append("%").append(Integer.toHexString(ch));
        }
        
        if (buf == null) {
            return name;
        }

        if (pos < length) {
            buf.append(name.substring(pos));
        }
        return buf.toString();
    }

    /**
     */
    public static String unescape(String name) {
        StringBuffer buf = null;
        int length = name.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            int ch = name.charAt(i);
            switch (ch) {
            case '%':
                if (i+3 > length) {
                    continue;
                }
                if (buf == null) {
                    buf = new StringBuffer();
                }
                if (i > 0) {
                    buf.append(name.substring(pos, i));
                }
                pos = i + 1;
                break;
            default:
                continue;
            }
            if (ch == '%') {
                String hex = name.substring(i+1, i+3);
                if (hex.equals("2f") ||
                    hex.equals("3a") ||
                    hex.equals("5b") ||
                    hex.equals("5d") ||
                    hex.equals("2a") ||
                    hex.equals("22") ||
                    hex.equals("7c") ||
                    hex.equals("27")) {
                    buf.append((char) Integer.parseInt(hex, 16));
                }
                else {
                    continue;
                }
                i += 2;
                pos = i + 1;
            }
        }
        if (buf == null) {
            return name;
        }

        if (pos < length) {
            buf.append(name.substring(pos));
        }
        return buf.toString();
    }
}
