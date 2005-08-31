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

import java.io.InputStream;

import javax.jcr.Node;

import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.server.io.FileImportCommand;

import org.osaf.cosmo.jcr.CosmoJcrConstants;

/**
 * Extends {@link org.apache.jackrabbit.server.io.FileImportCommand}
 * to provide logic for importing WebDAV resources.
 */
public class DavResourceImportCommand extends FileImportCommand {

    /**
     */
    public DavResourceImportCommand() {
        super();
        setNodeType(CosmoJcrConstants.NT_DAV_RESOURCE);
    }

    /**
     */
    public boolean importResource(ImportContext ctx,
                                  Node parentNode,
                                  InputStream in)
        throws Exception {
        super.importResource(ctx, parentNode, in);

        parentNode.setProperty(CosmoJcrConstants.NP_DAV_CONTENTLANGUAGE,
                               ctx.getContentLanguage());

        return true;
    }
}
