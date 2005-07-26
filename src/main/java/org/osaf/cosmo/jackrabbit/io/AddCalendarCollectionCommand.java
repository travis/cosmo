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

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.jcr.CosmoJcrConstants;

/**
 * A jcr-server import command that extends
 * {@link org.apache.jackrabbit.server.io.AddNodeCommand} to provide
 * special handling for calendar collections.
 */
public class AddCalendarCollectionCommand extends AddNodeCommand {

    /**
     */
    public AddCalendarCollectionCommand() {
        super(CosmoJcrConstants.NT_CALDAV_COLLECTION);
    }

    /**
     * Adds a calendar collection node named by the import context's
     * system id to the current node of the import context and sets it
     * to be the current node. Also sets the properties of the
     * auto-created calendar child node.
     */
    public boolean execute(ImportContext context) throws Exception {
        Node parentNode = context.getNode();
        Node newNode = parentNode.addNode(context.getSystemId(), getNodeType());

        // add subnodes representing calendar properties to the
        // autocreated calendar node
        Node calendarNode = newNode.getNode(CosmoJcrConstants.NN_ICAL_CALENDAR);

        Node prodidNode =
            calendarNode.addNode(CosmoJcrConstants.NN_ICAL_PRODID);
        prodidNode.setProperty(CosmoJcrConstants.NP_ICAL_VALUE,
                               CosmoConstants.PRODUCT_ID);
        
        Node versionNode =
            calendarNode.addNode(CosmoJcrConstants.NN_ICAL_VERSION);
        versionNode.setProperty(CosmoJcrConstants.NP_ICAL_MAX_VERSION,
                                CosmoConstants.ICALENDAR_VERSION);

        // CALSCALE: we only support Gregorian, so as per RFC 2445
        // section 4.7.1, we don't need to set it

        // METHOD: only used for iTIP scheduling, not necessary for
        // provisioning

        context.setNode(newNode);
        return false;
    }
}
