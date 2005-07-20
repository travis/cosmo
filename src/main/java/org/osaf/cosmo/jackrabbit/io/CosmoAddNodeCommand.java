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

import org.apache.jackrabbit.server.io.AddNodeCommand;
import org.apache.jackrabbit.server.io.ImportContext;

/**
 * A jcr-server import command that extends
 * {@link org.apache.jackrabbit.server.io.AddNodeCommand} to provide
 * special handling for specific Cosmo node types.
 *
 * <code>AddNodeCommand</code> is configured with a default node type
 * that will be used for all new nodes. There is no way to provide
 * logic that chooses a node type at runtime based on properties of
 * the chain context.
 *
 * This class provides special handling for calendar collection
 * nodes.
 */
public class CosmoAddNodeCommand extends AddNodeCommand {

    /**
     */
    public boolean execute(ImportContext context) throws Exception {
	return false;
    }
}
