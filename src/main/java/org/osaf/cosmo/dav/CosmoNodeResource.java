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
package org.osaf.cosmo.dav;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.webdav.simple.ChainBasedNodeResource;
import org.apache.jackrabbit.webdav.simple.DavResourceImpl;

import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;

import org.osaf.cosmo.jackrabbit.io.ApplicationContextAwareExportContext;

/**
 * Extends
 *  {@link org.apache.jackrabbit.webdav.simple.ChainBasedNodeResource}
 * to provide Cosmo-specific logic.
 */
public class CosmoNodeResource extends ChainBasedNodeResource {
    private static final Logger log = Logger.getLogger(CosmoNodeResource.class);

    private ApplicationContext applicationContext;

    /**
     */
    public CosmoNodeResource(ApplicationContext applicationContext) {
        super();
        this.applicationContext = applicationContext;
    }

    /**
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Creates an <code>ApplicationContextAwareExportContext</code> to
     * be used when executing the export chain.
     */
    protected ExportContext createExportContext(DavResourceImpl davResource,
                                                Node node) {
        ApplicationContextAwareExportContext ctx =
            new ApplicationContextAwareExportContext(node);
        ctx.setApplicationContext(getApplicationContext());
        return ctx;
    }
}
