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

import java.util.Properties;

import javax.jcr.Node;

import org.apache.jackrabbit.server.io.ExportContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Extends {@link org.apache.jackrabbit.server.io.exportContext}
 * to make it aware of Spring's application context.
 */
public class ApplicationContextAwareExportContext extends ExportContext
    implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     */
    public ApplicationContextAwareExportContext(Node exportRoot) {
        super(exportRoot);
    }

    /**
     */
    public ApplicationContextAwareExportContext(Properties props,
                                                Node exportRoot) {
        super(props, exportRoot);
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
