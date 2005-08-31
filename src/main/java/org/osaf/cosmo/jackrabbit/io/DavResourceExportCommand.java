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
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.FileExportCommand;

import org.osaf.cosmo.jcr.CosmoJcrConstants;

/**
 * Extends {@link org.apache.jackrabbit.server.io.FileExportCommand}
 * to provide logic for exporting the webdav properties of a
 * dav:resource node.
 */
public class DavResourceExportCommand extends FileExportCommand {

    /**
     */
    public boolean exportNode(ExportContext context,
                              Node content)
        throws Exception {
        super.exportNode(context, content);

        Node parentNode = content.getParent();
        if (parentNode.hasProperty(CosmoJcrConstants.NP_DAV_CONTENTLANGUAGE)) {
            Property p = parentNode.
                getProperty(CosmoJcrConstants.NP_DAV_CONTENTLANGUAGE);
            context.setContentLanguage(p.getString());
        }

        return true;
    }

    /**
     */
    public boolean canHandle(Node node) {
        try {
            return node.isNodeType(CosmoJcrConstants.NT_DAV_RESOURCE);
        } catch (RepositoryException e) {
            return false;
        }
    }
}
