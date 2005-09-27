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
package org.osaf.cosmo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Base class for Cosmo tests.
 */
public abstract class BaseCoreTestCase
    extends AbstractDependencyInjectionSpringContextTests {
    private static final Log log = LogFactory.getLog(BaseCoreTestCase.class);
    
    private static final String[] CONFIG_LOCATIONS = {
        "/applicationContext-test.xml",
        "/applicationContext-hibernate.xml",
        "/applicationContext-jcr.xml",
        "/applicationContext-provisioning.xml"
    };
    
    private TestSecurityManager securityManager;

    /**
     */
    protected String[] getConfigLocations() {
        return CONFIG_LOCATIONS;
    }

    /**
     */
    public TestSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(TestSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
