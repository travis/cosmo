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
package org.osaf.cosmo.dao.jcr;

import org.apache.xerces.util.XMLChar;

/**
 * Class providing some character escape methods. The escape schemata are
 * defined in chapter 6.4.3 (for JCR names) and (6.4.4 for JCR values) in the
 * JCR specification.
 *
 * The original code in this class was copied from the Apache
 * Jackrabbit class {@link org.apache.jackrabbit.test.api.EscapeJCRUtil}.
 */
public class JcrEscapist {

    private static final String utf16esc =
        "_x[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]_";

    private static char[] Utf16Padding = {'0', '0', '0'};

    private static int utf16length = 4;

    public static String xmlEscapeJcrValues(String str) {
        return xmlEscapeValues(str);
    }

    public static String xmlEscapeJcrNames(String str) {
        return xmlEscapeNames(str);
    }

    public static String xmlEscapeJcrPath(String str) {
        StringBuffer buf = new StringBuffer();

        String[] names = str.split("/");
        for (int i=0; i<names.length; i++) {
            buf.append(xmlEscapeNames(names[i]));
            if (i < names.length-1) {
                buf.append("/");
            }
        }

        return buf.toString();
    }

    public static String hexEscapeJcrNames(String str) {
        return hexEscape(str);
    }

    public static String hexEscapeJcrPath(String str) {
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

    private static String xmlEscapeNames(String str) {
        String[] split = str.split(":");
        if (split.length == 2) {
            // prefix should yet be a valid xml name
            String localname = xmlEscape(split[1]);
            String name = split[0] + ":" + localname;
            return name;
        } else {
            String localname = xmlEscape(split[0]);
            return localname;
        }
    }

    private static String xmlEscapeValues(String str) {
        char[] chars = str.toCharArray();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\u0020'
                    || c == '\u0009'
                    || c == '\n'
                    || c == '\r') {
                buf.append(xmlEscapeChar(c));
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    // Check if a substring can be misinterpreted as an xml escape
    // sequence.
    private static boolean canMisinterpret(String str) {
        boolean misinterprete = false;
        // check if is like "_xXXXX_"
        if (str.length() >= 7) {
            String sub16 = str.substring(0, 7);
            if (sub16.matches(utf16esc)) {
                misinterprete = true;
            }
        }
        return misinterprete;
    }

    // Escapes a single (invalid xml) character.
    private static String xmlEscapeChar(char c) {
        String unicodeRepr = Integer.toHexString(c);
        StringBuffer escaped = new StringBuffer();
        escaped.append("_x");
        escaped.append(Utf16Padding, 0, utf16length - unicodeRepr.length());
        escaped.append(unicodeRepr);
        escaped.append("_");
        return escaped.toString();
    }

    // Escapes a string containing invalid xml character(s).
    private static String xmlEscape(String str) {
        char[] chars = str.toCharArray();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // handle start character
            if (i == 0) {
                if (!XMLChar.isNameStart(c)) {
                    String escaped = xmlEscapeChar(c);
                    buf.append(escaped);
                } else {
                    String substr = str.substring(i, str.length());
                    if (canMisinterpret(substr)) {
                        buf.append(xmlEscapeChar(c));
                    } else {
                        buf.append(c);
                    }
                }
            } else {
                if (!XMLChar.isName(c)) {
                    buf.append(xmlEscapeChar(c));
                } else {
                    String substr = str.substring(i, str.length());
                    if (canMisinterpret(substr)) {
                        buf.append(xmlEscapeChar(c));
                    } else {
                        buf.append(c);
                    }
                }
            }
        }
        return buf.toString();
    }

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
                if (i+3 >= length) {
                    continue;
                }
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
