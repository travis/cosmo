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

import java.util.Locale;

import javax.jcr.Node;

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ImportContext;

import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dao.jcr.JcrEscapist;

/**
 * An import command for adding the <code>caldav:collection</code>
 * mixin type and setting associated properties to an existing
 * <code>dav:collection</code> node.
 */
public class MakeCalendarCollectionCommand extends AbstractCommand
    implements JcrConstants {

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
        if (! (node == null || node.isNodeType(NT_DAV_COLLECTION))) {
            return false;
        }

        if (! node.isNodeType(NT_CALDAV_COLLECTION)) {
            node.addMixin(NT_CALDAV_COLLECTION);
        }

        String description =
            JcrEscapist.hexUnescapeJcrNames(context.getSystemId());
        node.setProperty(NP_CALDAV_CALENDARDESCRIPTION, description);
        node.setProperty(NP_XML_LANG, Locale.getDefault().toString());

        return false;
    }
}
