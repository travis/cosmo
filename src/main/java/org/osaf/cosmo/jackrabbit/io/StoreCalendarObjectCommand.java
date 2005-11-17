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
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import org.apache.jackrabbit.server.io.AbstractCommand;
import org.apache.jackrabbit.server.io.AbstractContext;
import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.webdav.DavException;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dao.jcr.JcrConstants;
import org.osaf.cosmo.dao.jcr.JcrEscapist;
import org.osaf.cosmo.dao.UnsupportedCalendarObjectException;
import org.osaf.cosmo.dav.CosmoDavResponse;
import org.osaf.cosmo.icalendar.DuplicateUidException;
import org.osaf.cosmo.icalendar.ICalendarConstants;

/**
 * An import command for storing the calendar object attached to a
 * dav resource.
 */
public class StoreCalendarObjectCommand extends AbstractCommand
    implements JcrConstants, ICalendarConstants {
    private static final Logger log =
        Logger.getLogger(StoreCalendarObjectCommand.class);

    /**
     */
    public boolean execute(AbstractContext context)
        throws Exception {
        if (context instanceof ImportContext) {
            return execute((ImportContext) context);
        }
        else {
            return false;
        }
    }

    /**
     */
    public boolean execute(ImportContext context)
        throws Exception {
        Node resourceNode = context.getNode();
        if (resourceNode == null) {
            return false;
        }

        // if the node's parent is not a calendar collection, don't
        // bother storing, since we'll never query "webcal"
        // calendars.
        if (! resourceNode.getParent().isNodeType(NT_CALDAV_COLLECTION)) {
            return false;
        }

        // ensure that the resource is a dav resource and that either
        // it is of type text/calendar or its name ends with .ics
        if (! (resourceNode.isNodeType(NT_DAV_RESOURCE) &&
               (context.getContentType().startsWith(CONTENT_TYPE) ||
                resourceNode.getName().endsWith("." + FILE_EXTENSION)))) {
            return false;
        }

        // get a handle to the resource content
        Node content = resourceNode.getNode(NN_JCR_CONTENT);
        InputStream in = content.getProperty(NP_JCR_DATA).getStream();

        Calendar calendar = null;
        try {
            // parse the resource
            CalendarBuilder builder = new CalendarBuilder();
            calendar = builder.build(in);
        } catch (ParserException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error parsing calendar resource", e);
            }
            throw new DavException(CosmoDavResponse.SC_FORBIDDEN);
        }

        // make sure the object contains an event
        if (calendar.getComponents().getComponents(Component.VEVENT).
            isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Calendar object contains no supported components");
            }
            throw new DavException(CosmoDavResponse.SC_CONFLICT);
        }

        // make the node a caldav resource if it isn't already
        if (! resourceNode.isNodeType(NT_CALDAV_RESOURCE)) {
            resourceNode.addMixin(NT_CALDAV_RESOURCE);
        }

        // it's possible (tho pathological) that the client will
        // change the resource's uid on an update, so always
        // verify and set it
        Component event = (Component)
            calendar.getComponents().getComponents(Component.VEVENT).get(0);
        Property uid = (Property)
            event.getProperties().getProperty(Property.UID);
        if (! isUidUnique(resourceNode, uid.getValue())) {
            throw new DuplicateUidException(uid.getValue());
        }
        resourceNode.setProperty(NP_CALDAV_UID, uid.getValue());

        return false;
    }

    /**
     */
    protected boolean isUidUnique(Node node, String uid)
        throws RepositoryException {
        // look for nodes anywhere below the parent calendar
        // collection that have this same uid 
        StringBuffer stmt = new StringBuffer();
        stmt.append("/jcr:root");
        if (! node.getParent().getPath().equals("/")) {
            stmt.append(JcrEscapist.xmlEscapeJcrPath(node.getParent().
                                                     getPath()));
        }
        stmt.append("//element(*, ").
            append(NT_CALDAV_RESOURCE).
            append(")");
        stmt.append("[@").
            append(NP_CALDAV_UID).
            append(" = '").
            append(uid).
            append("']");

        QueryManager qm =
            node.getSession().getWorkspace().getQueryManager();
        QueryResult qr =
            qm.createQuery(stmt.toString(), Query.XPATH).execute();

        // if we are updating this node, then we expect it to show up
        // in the result, but nothing else
        for (NodeIterator i=qr.getNodes(); i.hasNext();) {
            Node n = (Node) i.next();
            if (! n.getPath().equals(node.getPath())) {
                return false;
            }
        }

        return true;
    }
}
