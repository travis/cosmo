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

import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dao.jcr.JcrEscapist;

/**
 * Extends {@link org.apache.jackrabbit.server.io.FileImportCommand}
 * to provide logic for importing WebDAV resources.
 */
public class DavResourceImportCommand extends FileImportCommand
    implements JcrConstants {

    /**
     */
    public boolean importResource(ImportContext context,
                                  Node node,
                                  InputStream in)
        throws Exception {
        super.importResource(context, node, in);

        if (! node.isNodeType(NT_DAV_RESOURCE)) {
            node.addMixin(NT_DAV_RESOURCE);
        }
        if (! node.isNodeType(NT_TICKETABLE)) {
            node.addMixin(NT_TICKETABLE);
        }
        String name = JcrEscapist.hexUnescapeJcrNames(context.getSystemId());
        node.setProperty(NP_DAV_DISPLAYNAME, name);
        node.setProperty(NP_DAV_CONTENTLANGUAGE, context.getContentLanguage());

        // XXX: remove this when springmodules is integrated and the
        // dao is running in the same session as the dav server
        node.getParent().save();

        return true;
    }
}
