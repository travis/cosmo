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

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 */
public class JcrTestHelper implements JcrConstants {
    static int nseq = 0;
    static int pseq = 0;

    private JcrTestHelper() {
    }

    /**
     */
    public static Node addNode(Session session)
        throws RepositoryException {
        String serial = new Integer(++nseq).toString();
        String name = "dummy" + serial;

        Node node = session.getRootNode().addNode(name);
        session.save();

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
    public static Calendar makeAndStoreDummyCalendar(Node node)
        throws RepositoryException {
        Calendar calendar = TestHelper.makeDummyCalendarWithEvent();

        JcrCalendarMapper.calendarToNode(calendar, node);
        node.save();

        return calendar;
    }

    /**
     */
    public static Calendar findDummyCalendar(Node node)
        throws RepositoryException {
        return JcrCalendarMapper.nodeToCalendar(node);
    }
}
