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
public class PathTranslatorTest extends TestCase {
    private static final Log log = LogFactory.getLog(PathTranslatorTest.class);

    public void testEscapedQueryableRepositoryName()
        throws Exception {
        String in = "My Documents";
        String out = PathTranslator.toQueryableRepositoryPath(in);
        assertEquals("My_x0020_Documents", out);
    }

    public void testEscapedQueryableRepositoryPath()
        throws Exception {
        String in = "/home/bcm/My Documents";
        String out = PathTranslator.toQueryableRepositoryPath(in);
        assertEquals("/home/bcm/My_x0020_Documents", out);
    }

    public void testUnescapedQueryableRepositoryName()
        throws Exception {
        String in = "My_Documents";
        String out = PathTranslator.toQueryableRepositoryPath(in);
        assertEquals(in, out);
    }

    public void testUnescapedQueryableRepositoryPath()
        throws Exception {
        String in = "/home/bcm/My_Documents";
        String out = PathTranslator.toQueryableRepositoryPath(in);
        assertEquals(in, out);
    }

    public void testEscapeLiteralUnderscoreQueryableRepositoryName()
        throws Exception {
        String in = "My_x0020_Documents";
        String out = PathTranslator.toQueryableRepositoryPath(in);
        assertEquals("My_x005f_x0020_Documents", out);
    }
}
