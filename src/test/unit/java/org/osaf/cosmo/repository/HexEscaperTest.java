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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class HexEscaperTest extends TestCase {
    private static final Log log = LogFactory.getLog(HexEscaperTest.class);

    private static final char[] HEX_ESCAPE_CHARS =
        new char[] { '/', ':', '[', ']', '*', '"', '|', '\'' };

    public void testEscape() throws Exception {
        for (int i=0; i<HEX_ESCAPE_CHARS.length; i++) {
            char bad = HEX_ESCAPE_CHARS[i];
            String in = "My" + String.valueOf(bad) + "Documents";
            String out = HexEscaper.escape(in);
            assertEquals("My%" + Integer.toHexString(bad) + "Documents",
                         out);
        }
    }

    public void testUnescape() throws Exception {
        for (int i=0; i<HEX_ESCAPE_CHARS.length; i++) {
            char bad = HEX_ESCAPE_CHARS[i];
            String in = "My%" + Integer.toHexString(bad) + "Documents";
            String out = HexEscaper.unescape(in);
            assertEquals("My" + String.valueOf(bad) + "Documents", out);
        }
    }
}
