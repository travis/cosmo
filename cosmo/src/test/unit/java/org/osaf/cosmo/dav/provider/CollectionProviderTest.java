/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.BaseDavTestCase;
import org.osaf.cosmo.dav.DavTestContext;
import org.osaf.cosmo.dav.ExistsException;
import org.osaf.cosmo.dav.impl.DavCollectionBase;
import org.osaf.cosmo.model.CollectionItem;

/**
 * Test case for <code>CollectionProvider</code>.
 */
public class CollectionProviderTest extends BaseDavTestCase {
    private static final Log log =
        LogFactory.getLog(CollectionProviderTest.class);

    /**
      * <blockquote>
     * If the Request-URI is already mapped to a resource, then the MKCOL
     * MUST fail.
     * </blockquote>
     */
    public void testCollectionExists() throws Exception {
        DavCollectionBase home = (DavCollectionBase)
            testHelper.initializeHomeResource();
        DavCollectionBase dummy =
            new DavCollectionBase((CollectionItem) home.getItem(),
                                  home.getResourceLocator(),
                                  testHelper.getResourceFactory());

        DavTestContext ctx = testHelper.createTestContext();
        CollectionProvider provider =
            new CollectionProvider(testHelper.getResourceFactory());

        try {
            provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), dummy);
            fail("mkcol succeeded for existing collection");
        } catch (ExistsException e) {}
    }
}
