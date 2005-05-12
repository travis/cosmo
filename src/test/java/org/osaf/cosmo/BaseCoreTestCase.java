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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Base class for Cosmo TestCases.
 *
 * @author Brian Moseley
 */
public class BaseCoreTestCase extends TestCase {
    private static final Log log = LogFactory.getLog(BaseCoreTestCase.class);
    private ApplicationContext appCtx = null;

    /**
     */
    public BaseCoreTestCase(String name) {
        super(name);
        String[] paths = {
            "/applicationContext-test.xml",
            "/applicationContext-hibernate.xml",
            "/applicationContext-jcr.xml",
            "/applicationContext-provisioning.xml"
        };
        appCtx = new ClassPathXmlApplicationContext(paths);
    }

    /**
     */
    public ApplicationContext getAppContext() {
        return appCtx;
    }
}
