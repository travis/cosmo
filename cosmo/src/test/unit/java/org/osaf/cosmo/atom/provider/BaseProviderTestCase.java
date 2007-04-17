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

import org.osaf.cosmo.atom.generator.ContentFactory;
import org.osaf.cosmo.atom.generator.GeneratorFactory;
import org.osaf.cosmo.atom.processor.ProcessorFactory;
import org.osaf.cosmo.dao.mock.MockCalendarDao;
import org.osaf.cosmo.dao.mock.MockContentDao;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.impl.StandardContentService;
import org.osaf.cosmo.service.lock.SingleVMLockManager;

/**
 * Base class for for {@link StandardProvider} tests.
 */
public class BaseProviderTestCase extends TestCase {
    private static final Log log =
        LogFactory.getLog(BaseProviderTestCase.class);

    protected StandardProvider provider;

    protected void setUp() throws Exception {
        GeneratorFactory generatorFactory = new GeneratorFactory();
        generatorFactory.setAbdera(new Abdera());
        generatorFactory.setContentFactory(new ContentFactory());

        MockDaoStorage storage = new MockDaoStorage();
        StandardContentService contentService = new StandardContentService();
        contentService.setCalendarDao(new MockCalendarDao(storage));
        contentService.setContentDao(new MockContentDao(storage));
        contentService.setLockManager(new SingleVMLockManager());
        contentService.init();

        ServiceLocatorFactory serviceLocatorFactory =
            new ServiceLocatorFactory();
        serviceLocatorFactory.setAtomPrefix("/atom");
        serviceLocatorFactory.setCmpPrefix("/cmp");
        serviceLocatorFactory.setDavPrefix("/dav");
        serviceLocatorFactory.setDavPrincipalPrefix("/dav");
        serviceLocatorFactory.setDavCalendarHomePrefix("/dav");
        serviceLocatorFactory.setMorseCodePrefix("/mc");
        serviceLocatorFactory.setPimPrefix("/pim");
        serviceLocatorFactory.setWebcalPrefix("/webcal");
        serviceLocatorFactory.setSecurityManager(new MockSecurityManager());
        serviceLocatorFactory.init();

        provider = new StandardProvider();
        provider.setGeneratorFactory(generatorFactory);
        provider.setProcessorFactory(new ProcessorFactory());
        provider.setContentService(contentService);
        provider.setServiceLocatorFactory(serviceLocatorFactory);
        provider.init();
    }

    protected void tearDown() throws Exception {
    }
}
