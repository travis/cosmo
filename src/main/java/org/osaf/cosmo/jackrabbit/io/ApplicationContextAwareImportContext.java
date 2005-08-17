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
package org.osaf.cosmo.jackrabbit.io;

import javax.jcr.Node;

import org.apache.jackrabbit.server.io.ImportContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extends {@link org.apache.jackrabbit.server.io.ImportContext}
 * to make it aware of Spring's application context.
 */
public class ApplicationContextAwareImportContext extends ImportContext
    implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     */
    public ApplicationContextAwareImportContext(Node importRoot) {
        super(importRoot);
    }

    /**
     */
    public ApplicationContextAwareImportContext(ImportContext base,
                                                Node importRoot) {
        super(base, importRoot);
    }

    // ApplicationContextAware methods

    /**
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // our methods

    /**
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
