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

import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Catalog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Class implements a default chain for importing calendar
 * collection resources. It adds the following commands:
 * <ul>
 * <li>{@link AddCalendarCollectionCommand}
 * </ul>
 */
public class ImportCalendarCollectionChain extends ChainBase {
    private static final Log log =
        LogFactory.getLog(ImportCalendarCollectionChain.class);

    /**
     * The default name of this chain:
     * <code>import-calendar-collection</code>
     */
    public static final String NAME = "import-calendar-collection";

    /**
     */
    public ImportCalendarCollectionChain() {
        super();
        addCommand(new AddCalendarCollectionCommand());
    }

    /**
     * Returns an import chain. It first tries to lookup the command
     * in the default catalog. If this failes, a new instance of this class
     * is returned.
     */
    public static Command getChain() {
        Catalog catalog = CatalogFactoryBase.getInstance().getCatalog();
        Command importChain = catalog.getCommand(NAME);
        if (importChain == null) {
            // generate default import chain
            importChain = new ImportCalendarCollectionChain();
        }
        return importChain;
    }
}
