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
public class JcrTestHelper extends TestHelper
    implements ICalendarConstants, JcrConstants {
    static int nseq = 0;
    static int pseq = 0;

    private Session session;

    public JcrTestHelper(Session session) {
        super();
        this.session = session;
    }

    /**
     */
    public Session getSession() {
        return session;
    }

    /**
     */
    public Node addNode()
        throws RepositoryException {
        return addNode(session.getRootNode());
    }

    /**
     */
    public Node addNode(Node parent)
        throws RepositoryException {
        String serial = new Integer(++nseq).toString();
        String name = "dummy" + serial;

        return parent.addNode(name);
    }

    /**
     */
    public Node addFileNode(InputStream data,
                            String mimetype,
                            String charset)
        throws RepositoryException {
        return addFileNode(session.getRootNode(), data, mimetype, charset);
    }

    /**
     */
    public Node addFileNode(Node parent,
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

        return node;
    }

    /**
     */
    public Property addProperty(Node node)
        throws RepositoryException {
        String serial = new Integer(++pseq).toString();
        String name = "dummy" + serial;

        return node.setProperty(name, name);
    }

    /**
     */
    public User makeAndStoreDummyUser()
        throws RepositoryException {
        User user = makeDummyUser();

        Node node = session.getRootNode().addNode(user.getUsername());
        node.addMixin(NT_USER);
        JcrUserMapper.userToNode(user, node);

        return user;
    }

    /**
     */
    public User findDummyUser(String username)
        throws RepositoryException {
        return session.getRootNode().hasNode(username) ?
            JcrUserMapper.nodeToUser(session.getRootNode().getNode(username)) :
            null;
    }

    /**
     */
    public void removeDummyUser(User user)
        throws RepositoryException {
        if (! session.getRootNode().hasNode(user.getUsername())) {
            return;
        }
        session.getRootNode().getNode(user.getUsername()).remove();
    }

    /**
     */
    public Node addTicketableNode()
        throws RepositoryException {
        Node node = addNode();

        node.addMixin(NT_TICKETABLE);

        return node;
    }

    /**
     */
    public Ticket makeAndStoreDummyTicket(Node node,
                                          User user)
        throws RepositoryException {
        Ticket ticket = makeDummyTicket(user);

        Node ticketNode = node.addNode(NN_TICKET, NT_TICKET);
        JcrTicketMapper.ticketToNode(ticket, ticketNode);

        return ticket;
    }

    /**
     */
    public Ticket findDummyTicket(Node node,
                                  String id)
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
    public Node addCalendarCollectionNode()
        throws RepositoryException {
        Node node = addNode();

        node.addMixin(NT_CALENDAR_COLLECTION);
        node.setProperty(NP_CALENDAR_DESCRIPTION, node.getName());
        node.setProperty(NP_XML_LANG, Locale.getDefault().toString());

        return node;
    }

    /**
     */
    public Node addDavResourceNode(InputStream data,
                                   String mimetype,
                                   String charset)
        throws RepositoryException {
        return addDavResourceNode(session.getRootNode(), data, mimetype,
                                  charset);
    }

    /**
     */
    public Node addDavResourceNode(Node parent,
                                   InputStream data,
                                   String mimetype,
                                   String charset)
        throws RepositoryException {
        Node node = addFileNode(parent, data, mimetype, charset);

        node.addMixin(NT_DAV_RESOURCE);
        node.addMixin(NT_TICKETABLE);
        node.setProperty(NP_DAV_DISPLAYNAME, node.getName());

        return node;
    }

    /**
     */
    public Node addCalendarResourceNode(Calendar calendar)
        throws RepositoryException {
        return addCalendarResourceNode(session.getRootNode(), calendar);
    }

    /**
     */
    public Node addCalendarResourceNode(Node node,
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
    public Calendar loadCalendar(String name)
        throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        Calendar calendar = new CalendarBuilder().build(in);
        in.close();
        return calendar;
    }
}
