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
package org.osaf.cosmo.dao.jcr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 */
public class JcrTestHelper implements ICalendarConstants, JcrConstants {
    static int nseq = 0;
    static int pseq = 0;

    private JcrTestHelper() {
    }

    /**
     */
    public static Node addNode(Session session)
        throws RepositoryException {
        return addNode(session.getRootNode());
    }

    /**
     */
    public static Node addNode(Node parent)
        throws RepositoryException {
        String serial = new Integer(++nseq).toString();
        String name = "dummy" + serial;

        Node node = parent.addNode(name);
        parent.save();

        return node;
    }

    /**
     */
    public static Node addFileNode(Session session,
                                   InputStream data,
                                   String mimetype,
                                   String charset)
        throws RepositoryException {
        return addFileNode(session.getRootNode(), data, mimetype, charset);
    }

    /**
     */
    public static Node addFileNode(Node parent,
                                   InputStream data,
                                   String mimetype,
                                   String charset)
        throws RepositoryException {
        String serial = new Integer(++nseq).toString();
        String name = "dummy" + serial;

        Node node = parent.addNode(name, NT_FILE);
        Node content = node.addNode(NN_JCR_CONTENT, NT_RESOURCE);
        content.setProperty(NP_JCR_DATA, data);
        content.setProperty(NP_JCR_MIMETYPE, mimetype);
        content.setProperty(NP_JCR_ENCODING, charset);
        content.setProperty(NP_JCR_LASTMODIFIED,
                            java.util.Calendar.getInstance());
        parent.save();

        return node;
    }

    /**
     */
    public static Property addProperty(Node node)
        throws RepositoryException {
        String serial = new Integer(++pseq).toString();
        String name = "dummy" + serial;

        Property property = node.setProperty(name, name);
        node.save();

        return property;
    }

    /**
     */
    public static User makeAndStoreDummyUser(Session session)
        throws RepositoryException {
        User user = TestHelper.makeDummyUser();

        Node node = session.getRootNode().addNode(user.getUsername());
        node.addMixin(NT_USER);
        JcrUserMapper.userToNode(user, node);
        session.getRootNode().save();

        return user;
    }

    /**
     */
    public static User findDummyUser(Session session, String username)
        throws RepositoryException {
        return session.getRootNode().hasNode(username) ?
            JcrUserMapper.nodeToUser(session.getRootNode().getNode(username)) :
            null;
    }

    /**
     */
    public static void removeDummyUser(Session session, User user)
        throws RepositoryException {
        session.getRootNode().getNode(user.getUsername()).remove();
        session.save();
    }

    /**
     */
    public static Node addTicketableNode(Session session)
        throws RepositoryException {
        Node node = addNode(session);

        node.addMixin(NT_TICKETABLE);
        session.getRootNode().save();

        return node;
    }

    /**
     */
    public static Ticket makeAndStoreDummyTicket(Node node,
                                                 User user)
        throws RepositoryException {
        Ticket ticket = TestHelper.makeDummyTicket(user);

        Node ticketNode = node.addNode(NN_TICKET, NT_TICKET);
        JcrTicketMapper.ticketToNode(ticket, ticketNode);
        node.save();

        return ticket;
    }

    /**
     */
    public static Ticket findDummyTicket(Node node, String id)
        throws RepositoryException {
        for (NodeIterator i = node.getNodes(NN_TICKET); i.hasNext();) {
            Node child = i.nextNode();
            if (child.getProperty(NP_TICKET_ID).getString().equals(id)) {
                return JcrTicketMapper.nodeToTicket(child);
            }
        }
        return null;
    }

    /**
     */
    public static Node addCalendarCollectionNode(Session session)
        throws RepositoryException {
        Node node = addNode(session);

        node.addMixin(NT_CALDAV_COLLECTION);
        node.setProperty(NP_CALDAV_CALENDARDESCRIPTION, node.getName());
        node.setProperty(NP_XML_LANG, Locale.getDefault().toString());
        session.getRootNode().save();

        return node;
    }

    /**
     */
    public static Node addDavResourceNode(Session session,
                                          InputStream data,
                                          String mimetype,
                                          String charset)
        throws RepositoryException {
        return addDavResourceNode(session.getRootNode(), data, mimetype,
                                  charset);
    }

    /**
     */
    public static Node addDavResourceNode(Node parent,
                                          InputStream data,
                                          String mimetype,
                                          String charset)
        throws RepositoryException {
        Node node = addFileNode(parent, data, mimetype, charset);

        node.addMixin(NT_DAV_RESOURCE);
        node.addMixin(NT_TICKETABLE);
        node.setProperty(NP_DAV_DISPLAYNAME, node.getName());
        node.save();

        return node;
    }

    /**
     */
    public static Node addCalendarResourceNode(Session session,
                                               Calendar calendar)
        throws RepositoryException {
        return addCalendarResourceNode(session.getRootNode(), calendar);
    }

    /**
     */
    public static Node addCalendarResourceNode(Node node,
                                               Calendar calendar)
        throws RepositoryException {
        try {
            InputStream data =
                new ByteArrayInputStream(calendar.toString().getBytes("utf8"));
            return addDavResourceNode(node, data, CONTENT_TYPE, "utf8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("utf8 not supported?", e);
        }
    }

    /**
     */
    public static Calendar makeAndStoreDummyCalendar(Node node)
        throws RepositoryException {
        Calendar calendar = TestHelper.makeDummyCalendarWithEvent();

        JcrCalendarMapper.calendarToNode(calendar, node);
        node.save();

        return calendar;
    }

    /**
     */
    public static Calendar makeAndStoreDummyCaldavCalendar(Node collection)
        throws RepositoryException {
        Calendar calendar = TestHelper.makeDummyCalendarWithEvent();

        Node resource = addCalendarResourceNode(collection, calendar);
        JcrCalendarMapper.calendarToNode(calendar, resource);
        collection.save();

        return calendar;
    }

    /**
     */
    public static Calendar findDummyCalendar(Node node)
        throws RepositoryException {
        return JcrCalendarMapper.nodeToCalendar(node);
    }

    /**
     */
    public static Calendar loadCalendar(String name)
        throws Exception {
        InputStream in =
            JcrTestHelper.class.getClassLoader().getResourceAsStream(name);
        return new CalendarBuilder().build(in);
    }
}
