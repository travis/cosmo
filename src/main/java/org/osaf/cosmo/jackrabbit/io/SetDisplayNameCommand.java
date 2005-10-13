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

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ImportContext;

import org.osaf.cosmo.jcr.CosmoJcrConstants;
import org.osaf.cosmo.jcr.JCREscapist;

/**
 * An import command for setting the display name of a dav
 * resource. If the resource does not already have a display name, the
 * display name is set to be the same as the resource's name.
 */
public class SetDisplayNameCommand extends AbstractCommand {

    /**
     */
    public boolean execute(AbstractContext context) throws Exception {
        if (context instanceof ImportContext) {
            return execute((ImportContext) context);
        } else {
            return false;
        }
    }

    /**
     */
    public boolean execute(ImportContext context) throws Exception {
        Node node = context.getNode();
        if (node != null &&
            (node.isNodeType(CosmoJcrConstants.NT_DAV_COLLECTION) ||
             node.isNodeType(CosmoJcrConstants.NT_DAV_RESOURCE))) {
            if (! node.hasProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME)) {
                String name =
                    JCREscapist.hexUnescapeJCRNames(context.getSystemId());
                node.setProperty(CosmoJcrConstants.NP_DAV_DISPLAYNAME, name);
            }
        }
        return false;
    }
}
