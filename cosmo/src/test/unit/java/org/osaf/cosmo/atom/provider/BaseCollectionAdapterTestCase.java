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
package org.osaf.cosmo.atom.provider;

import junit.framework.TestCase;

import org.apache.abdera.Abdera;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.atom.AtomHelper;

/**
 * Base class for for provider tests.
 */
public abstract class BaseCollectionAdapterTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseCollectionAdapterTestCase.class);

    protected BaseCollectionAdapter adapter;
    protected StandardProvider provider;
    protected AtomHelper helper;

    protected void setUp() throws Exception {
        helper = new AtomHelper();
        helper.setUp();

        provider = new StandardProvider();
        provider.setWorkspaceManager(new StandardWorkspaceManager());
        provider.setGeneratorFactory(helper.getGeneratorFactory());
        provider.setServiceLocatorFactory(helper.getServiceLocatorFactory());
        provider.init(new Abdera(), null);
        
        adapter = createAdapter();
        adapter.setGeneratorFactory(helper.getGeneratorFactory());
        adapter.setServiceLocatorFactory(helper.getServiceLocatorFactory());
        adapter.setEntityFactory(helper.getEntityFactory());
        adapter.init();

        helper.logIn();
    }

    protected void tearDown() throws Exception {
    }

    protected abstract BaseCollectionAdapter createAdapter();
}
