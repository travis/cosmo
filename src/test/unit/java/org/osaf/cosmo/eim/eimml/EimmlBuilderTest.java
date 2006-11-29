/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.eim.eimml;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.eim.EimRecord;

/**
 * Test Case for {@link EimmlBuilder}.
 */
public class EimmlBuilderTest extends TestCase {
    private static final Log log =
        LogFactory.getLog(EimmlBuilderTest.class);

    private static final TestHelper helper = new TestHelper();

    /** */
    public void testBuild() throws Exception {
        InputStream in = helper.getInputStream("eimml/1.xml");

        EimmlBuilder builder = new EimmlBuilder();
        List<EimRecord> records = builder.build(in);
    }
}
