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
package org.osaf.cosmo.io;

import org.apache.jackrabbit.server.io.DefaultIOManager;

/**
 * Extends {@link org.apache.jackrabbit.server.io.DefaultIOManager}
 * to provide a set of custom IO handlers:
 *
 * <ol>
 * <li> {@link DavCollectionHandler} </li>
 * <li> {@link DavResourceHandler} </li>
 * </ol>
 */
public class CosmoIOManager extends DefaultIOManager {

    /**
     * Overwrites the handler list to include the following handlers:
     *
     * <ol>
     * <li> {@link DavCollectionHandler}</li>
     * <li> {@link CosmoHandler}</li>
     * </ol>
     */
    protected void init() {
        addIOHandler(new DavCollectionHandler(this));
        addIOHandler(new DavResourceHandler(this));
    }
}
