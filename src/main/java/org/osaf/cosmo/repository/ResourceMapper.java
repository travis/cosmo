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
package org.osaf.cosmo.repository;

import java.util.Calendar;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CalendarCollectionResource;
import org.osaf.cosmo.model.CalendarResource;
import org.osaf.cosmo.model.CollectionResource;
import org.osaf.cosmo.model.EventResource;
import org.osaf.cosmo.model.FileResource;
import org.osaf.cosmo.model.HomeCollectionResource;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.model.ResourceProperty;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * Utility class that converts between {@link Resource}s and
 * {@link javax.jcr.Node}s.
 *
 * Maps {@link FileResource} to a <code>nt:file</code> node with the
 * <code>dav:resource</code> and <code>ticket:ticketable</code> mixin
 * types and a <code>jcr:content</code> child node with type
 * <code>nt:resource</code>.
 *
 * Maps {@link CollectionResource} to a <code>nt:folder</code> node
 * with the <code>dav:collection</code> and
 * <code>ticket:ticketable</code> mixin types.
 */
public class ResourceMapper implements SchemaConstants {
    private static final Log log = LogFactory.getLog(ResourceMapper.class);

    /**
     * Returns a new instance of <code>Resource</code> populated from a
     * resource node. If the resource is a collection, include its
     * child resources to the indicated depth (0 meaning no
     * children).
     */
    public static Resource nodeToResource(Node node, int depth)
        throws RepositoryException {
        if (node.getPath().equals("/")) {
            return nodeToRootCollection(node, depth);
        }
        if (node.isNodeType(NT_HOME_COLLECTION)) {
            return nodeToHomeCollection(node, depth);
        }
        if (node.isNodeType(NT_CALENDAR_COLLECTION)) {
            return nodeToCalendarCollection(node, depth);
        }
        if (node.isNodeType(NT_DAV_COLLECTION)) {
            return nodeToCollection(node, depth);
        }
        if (node.isNodeType(NT_EVENT_RESOURCE)) {
            return nodeToEvent(node);
        }
        return nodeToFile(node);
    }

    /**
     * Returns a new instance of <code>Resource</code> populated from a
     * resource node. If the resource is a collection, include its no
     * children but no further descendents.
     */
    public static Resource nodeToResource(Node node)
        throws RepositoryException {
        return nodeToResource(node, 1);
    }

    /**
     * Copies the properties of a <code>Resource</code> into a resource
     * node. The resource node is found by converting the client view
     * name into a repository view name and using the result to find
     * the appropriate child node of the given parent node, creating a
     * new one if necessary. The resource node itself is returned.
     */
    public static Node resourceToNode(Resource resource,
                                      Node parentNode)
        throws RepositoryException {
        String nodeType = null;
        if (resource instanceof HomeCollectionResource) {
            nodeType = NT_HOME_COLLECTION;
        }
        else if (resource instanceof CollectionResource) {
            nodeType = NT_DAV_COLLECTION;
        }
        else {
            nodeType = NT_DAV_RESOURCE;
        }

        String name =
            PathTranslator.toRepositoryPath(resource.getDisplayName());
        Node resourceNode = null;
        if (parentNode.hasNode(name)) {
            resourceNode = parentNode.getNode(name);
        } else {
            resourceNode = parentNode.addNode(name, nodeType);
            Calendar now = Calendar.getInstance();
            resourceNode.setProperty(NP_DAV_CREATED, now);
            resource.setDateModified(now.getTime());
        }

        setCommonResourceProperties(resource, resourceNode);

        if (resource instanceof CollectionResource) {
            collectionToNode(((CollectionResource)resource), resourceNode);
        }

        if (resource instanceof HomeCollectionResource) {
            homeCollectionToNode(((HomeCollectionResource)resource),
                                 resourceNode);
        }
        else if (resource instanceof CalendarCollectionResource) {
            calendarCollectionToNode(((CalendarCollectionResource)resource),
                                     resourceNode);
        }

        return resourceNode;
    }

    /**
     * Creates the named home collection in the repository
     * representing the given <code>HomeCollectionResource</code>,
     * returning the resulting node and setting the path on the
     * resource.
     *
     * In the client view of the schema, all home collections are
     * created in the root of the workspace. Thus, the client path of
     * a home collection (the one returned by {@link
     * Resource.getPath()} becomes <code>/&lgt;name&gt;</code> once
     * the collection is created. Any path set on the collection
     * before its creation is ignored.
     *
     * In the repository view, the home collection is created beneath
     * two layers of structural nodes. The 'grandparent' node is named
     * for the first character of the resource's name, and the parent
     * node is named for the first two characters of the name. For
     * this reason, home collection names must be at least 3
     * characters long. This layering creates buckets into which
     * individual home collections are allocated.
     */
    public static Node createHomeCollection(HomeCollectionResource resource,
                                            String name,
                                            Session session)
        throws RepositoryException {
        if (name == null || name.length() < 3) {
            throw new IllegalArgumentException("home collection name must be least 3 characters");
        }

        // create bucket nodes if necessary
        String n1 = PathTranslator.toRepositoryPath(name.substring(0, 1));
        Node l1 = session.getRootNode().hasNode(n1) ?
            session.getRootNode().getNode(n1) :
            session.getRootNode().addNode(n1, NT_UNSTRUCTURED);

        String n2 = PathTranslator.toRepositoryPath(name.substring(0, 2));
        Node l2 = l1.hasNode(n2) ?
            l1.getNode(n2) :
            l1.addNode(n2, NT_UNSTRUCTURED);

        Node hc = resourceToNode(resource, l2);
        resource.setPath(PathTranslator.toClientPath(hc.getPath()));

        return hc;
    }

    private static void setCommonResourceProperties(Resource resource,
                                                    Node node)
        throws RepositoryException {
        node.setProperty(NP_DAV_DISPLAYNAME, resource.getDisplayName());
        Calendar now = Calendar.getInstance();
        node.setProperty(NP_DAV_LASTMODIFIED, now);
        resource.setDateModified(now.getTime());

        for (Iterator i=resource.getProperties().iterator(); i.hasNext();) {
            ResourceProperty rp = (ResourceProperty) i.next();
            node.setProperty(rp.getName(), rp.getValue());
        }
    }

    /**
     */
    public static void
        calendarCollectionToNode(CalendarCollectionResource resource,
                                 Node node)
        throws RepositoryException {
        if (! node.isNodeType(NT_CALENDAR_COLLECTION)) {
            node.addMixin(NT_CALENDAR_COLLECTION);
        }
        node.setProperty(NP_CALENDAR_DESCRIPTION, resource.getDescription());
        node.setProperty(NP_CALENDAR_LANGUAGE, resource.getLanguage());

        if (resource.hasTimezone())
            node.setProperty(NP_CALENDAR_TIMEZONE, resource.getTimezone().toString());


        if (resource.hasSupportedComponentSet()) {
            Set cs = resource.getSupportedComponentSet();
            Iterator i = cs.iterator();
            String[] comps = new String[cs.size()];
            int pos = 0;

            while (i.hasNext())
                comps[pos++] = i.next().toString();

            node.setProperty(NP_CALENDAR_SUPPORTED_COMPONENT_SET, comps);
        }
    }

    /**
     */
    public static void homeCollectionToNode(HomeCollectionResource resource,
                                            Node node)
        throws RepositoryException {
        if (! node.isNodeType(NT_HOME_COLLECTION)) {
            node.addMixin(NT_HOME_COLLECTION);
        }
    }

    /**
     */
    public static void collectionToNode(CollectionResource resource,
                                        Node node)
        throws RepositoryException {
        if (! node.isNodeType(NT_TICKETABLE)) {
            node.addMixin(NT_TICKETABLE);
        }
    }

    private static void setCommonResourceAttributes(Resource resource,
                                                    Node node)
        throws RepositoryException {
        resource.setPath(PathTranslator.toClientPath(node.getPath()));
        resource.setDisplayName(node.getProperty(NP_DAV_DISPLAYNAME).
                                getString());
        resource.setDateCreated(node.getProperty(NP_DAV_CREATED).
                                getDate().getTime());
        resource.setDateModified(node.getProperty(NP_DAV_LASTMODIFIED).
                                 getDate().getTime());

        for (PropertyIterator i=node.getProperties(); i.hasNext();) {
            Property p = i.nextProperty();
            if (p.getName().startsWith("cosmo:") ||
                p.getName().startsWith("jcr:") ||
                p.getName().startsWith("dav:") ||
                p.getName().startsWith("calendar:") ||
                p.getName().startsWith("icalendar:") ||
                p.getName().startsWith("xml")) {
                continue;
            }
            resource.getProperties().add(propToResourceProperty(p));
        }

        HashSet timedOutTicketNodes = new HashSet();
        for (NodeIterator i=node.getNodes(NN_TICKET); i.hasNext();) {
            Node child = i.nextNode();
            Ticket ticket = TicketMapper.nodeToTicket(child);
            if (ticket.hasTimedOut()) {
                timedOutTicketNodes.add(child);
                continue;
            }
            resource.getTickets().add(ticket);
        }
        if (! timedOutTicketNodes.isEmpty()) {
            for (Iterator i=timedOutTicketNodes.iterator(); i.hasNext();) {
                Node child = (Node) i.next();
                if (log.isDebugEnabled()) {
                    log.debug("removing timed out ticket " +
                              child.getProperty(NP_TICKET_ID).getString());
                }
                child.remove();
            }
            node.save();
        }

        resource.setOwner(findOwner(node));
    }

    private static User findOwner(Node node)
        throws RepositoryException {
        if (node.getPath().equals("/")) {
            return null;
        }
        if (node.isNodeType(NT_USER)) {
            return UserMapper.nodeToUser(node);
        }
        Node parent = node.getParent();
        if (parent == null) {
            return null;
        }
        return findOwner(parent);
    }

    private static EventResource nodeToEvent(Node node)
        throws RepositoryException {
        EventResource event = new EventResource();
        nodeToCalendarObject(node, event);

        return event;
    }

    private static void nodeToCalendarObject(Node node,
                                             CalendarResource resource)
        throws RepositoryException {
        nodeToFile(node, resource);
        resource.setUid(node.getProperty(NP_CALENDAR_UID).getString());
    }

    private static FileResource nodeToFile(Node node)
        throws RepositoryException {
        FileResource resource = new FileResource();
        nodeToFile(node, resource);
        return resource;
    }

    private static void nodeToFile(Node node,
                                   FileResource resource)
        throws RepositoryException {
        setCommonResourceAttributes(resource, node);

        Node contentNode = node.getNode(NN_JCR_CONTENT);
        resource.setDateModified(contentNode.getProperty(NP_JCR_LASTMODIFIED).
                                 getDate().getTime());
        resource.setContentType(contentNode.getProperty(NP_JCR_MIMETYPE).
                                getString());
        if (contentNode.hasProperty(NP_JCR_ENCODING)) {
            resource.setContentEncoding(contentNode.
                                        getProperty(NP_JCR_ENCODING).
                                        getString());
        }
        if (contentNode.hasProperty(NP_DAV_CONTENTLANGUAGE)) {
            resource.setContentLanguage(contentNode.
                                        getProperty(NP_DAV_CONTENTLANGUAGE).
                                        getString());
        }
        Property content = contentNode.getProperty(NP_JCR_DATA);
        resource.setContentLength(new Long(content.getLength()));
        resource.setContent(content.getStream());
    }

    private static CollectionResource nodeToRootCollection(Node node,
                                                           int depth)
        throws RepositoryException {
        CollectionResource collection = new CollectionResource();

        collection.setDisplayName("/");
        collection.setPath("/");

        for (NodeIterator i=node.getNodes(NN_TICKET); i.hasNext();) {
            Node child = i.nextNode();
            collection.getTickets().add(TicketMapper.nodeToTicket(child));
        }

        // JCR 1.0 does not define a standard node type for the
        // root node, so we have no way of knowing what it's
        // creation date was or whether it has extra properties

        if (depth > 0) {
            // the next resource nodes are two levels deep
            for (NodeIterator i=node.getNodes(); i.hasNext();) {
                Node l1 = i.nextNode();
                for (NodeIterator j=l1.getNodes(); j.hasNext();) {
                    Node l2 = j.nextNode();
                    for (NodeIterator k=l2.getNodes(); k.hasNext();) {
                        Node child = k.nextNode();
                        if (child.isNodeType(NT_DAV_COLLECTION) ||
                            child.isNodeType(NT_DAV_RESOURCE)) {
                            collection.addResource(nodeToResource(child,
                                                                  depth-1));
                        }
                    }
                }
            }
        }

        return collection;
    }

    private static HomeCollectionResource nodeToHomeCollection(Node node,
                                                               int depth)
        throws RepositoryException {
        HomeCollectionResource collection = new HomeCollectionResource();

        setCommonResourceAttributes(collection, node);

        if (depth > 0) {
            for (NodeIterator i=node.getNodes(); i.hasNext();) {
                Node child = i.nextNode();
                if (child.isNodeType(NT_DAV_COLLECTION) ||
                    child.isNodeType(NT_DAV_RESOURCE)) {
                    collection.addResource(nodeToResource(child, depth-1));
                }
            }
        }

        return collection;
    }

    private static CalendarCollectionResource
        nodeToCalendarCollection(Node node,
                                 int depth)
        throws RepositoryException {
        CalendarCollectionResource collection =
            new CalendarCollectionResource();

        setCommonResourceAttributes(collection, node);

        if (node.hasProperty(NP_CALENDAR_DESCRIPTION)) {
            collection.setDescription(node.
                                      getProperty(NP_CALENDAR_DESCRIPTION).
                                      getString());
        }
        if (node.hasProperty(NP_CALENDAR_LANGUAGE)) {
            collection.setLanguage(node.getProperty(NP_CALENDAR_LANGUAGE).getString());
        }
        if (node.hasProperty(NP_CALENDAR_TIMEZONE)) {
            try {
            collection.setTimezone(node.getProperty(NP_CALENDAR_TIMEZONE).getString());
            } catch (Exception e) {
                log.warn("Unable to set calendar timezone", e);
            }
        }
        if (node.hasProperty(NP_CALENDAR_SUPPORTED_COMPONENT_SET)) {
            try {
                Value[] vals = node.getProperty(NP_CALENDAR_SUPPORTED_COMPONENT_SET).getValues();
                HashSet comps = new HashSet();

                for (int i = 0; i < vals.length; i++)
                    comps.add(vals[i]);

                collection.setSupportedComponentSet(comps);

            } catch (ValueFormatException e) {
                log.warn("Unable to set calendar supported component set", e);
            }
        }

        if (depth > 0) {
            for (NodeIterator i=node.getNodes(); i.hasNext();) {
                Node child = i.nextNode();
                if (child.isNodeType(NT_EVENT_RESOURCE) ||
                    child.isNodeType(NT_DAV_COLLECTION) ||
                    child.isNodeType(NT_DAV_RESOURCE)) {
                    collection.addResource(nodeToResource(child, depth-1));
                }
            }
        }

        return collection;
    }

    private static CollectionResource nodeToCollection(Node node,
                                                       int depth)
        throws RepositoryException {
        CollectionResource collection = new CollectionResource();

        setCommonResourceAttributes(collection, node);

        if (depth > 0) {
            for (NodeIterator i=node.getNodes(); i.hasNext();) {
                Node child = i.nextNode();
                if (child.isNodeType(NT_DAV_COLLECTION) ||
                    child.isNodeType(NT_DAV_RESOURCE)) {
                    collection.addResource(nodeToResource(child, depth-1));
                }
            }
        }

        return collection;
    }

    private static ResourceProperty propToResourceProperty(Property property)
        throws RepositoryException {
        ResourceProperty rp = new ResourceProperty();
        rp.setName(property.getName());
        rp.setValue(property.getString());
        return rp;
    }
}
