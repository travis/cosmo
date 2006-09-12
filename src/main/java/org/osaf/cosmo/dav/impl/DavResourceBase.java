/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.Namespace;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dav.ExtendedDavResource;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.CalendarCollectionItem;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.DuplicateItemNameException;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.ModelConversionException;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.util.PathUtil;

/**
 * Base class for implementations of <code>ExtendedDavResource</code>
 * which provides behavior common to all resources.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:getcreationdate</code></li>
 * <li><code>DAV:displayname</code></li>
 * <li><code>DAV:iscollection</code></li>
 * <li><code>DAV:resourcetype</code></li>
 * </ul>
 *
 * Note that all of these properties are protected and cannot be
 * modified or removed.
 *
 * This class does not define any resource types.
 *
 * @see org.apache.jackrabbit.webdav.DavResource
 * @see ExtendedDavResource
 */
public abstract class DavResourceBase implements ExtendedDavResource {
    private static final Logger log =
        Logger.getLogger(DavResourceBase.class);

    private static final Set<DavPropertyName> LIVE_PROPERTIES =
        new HashSet<DavPropertyName>();

    private DavResourceLocator locator;
    private DavResourceFactory factory;
    private DavSession session;
    private Item item;
    private DavPropertySet properties;
    private DavCollection parent;

    static {
        registerLiveProperty(DavPropertyName.CREATIONDATE);
        registerLiveProperty(DavPropertyName.DISPLAYNAME);
        registerLiveProperty(DavPropertyName.ISCOLLECTION);
        registerLiveProperty(DavPropertyName.RESOURCETYPE);
    }

    /** */
    public DavResourceBase(Item item,
                           DavResourceLocator locator,
                           DavResourceFactory factory,
                           DavSession session) {
        this.item = item;
        this.locator = locator;
        this.factory = factory;
        this.session = session;
        this.properties = new DavPropertySet();

        loadProperties();
    }

    // DavResource methods

    /** */
    public String getComplianceClass() {
        return ExtendedDavResource.COMPLIANCE_CLASS;
    }

    /** */
    public boolean exists() {
        return item != null && item.getUid() != null;
    }

    /** */
    public boolean isCollection() {
        return item instanceof CollectionItem;
    }

    /** */
    public String getDisplayName() {
        return item.getName();
    }

    /** */
    public DavResourceLocator getLocator() {
        return locator;
    }

    /** */
    public String getResourcePath() {
        return locator.getResourcePath();
    }

    /** */
    public String getHref() {
        return locator.getHref(isCollection());
    }

    /** */
    public DavPropertyName[] getPropertyNames() {
        return properties.getPropertyNames();
    }

    /** */
    public DavProperty getProperty(DavPropertyName name) {
        return properties.get(name);
    }

    /** */
    public DavPropertySet getProperties() {
        return properties;
    }

    /**
     * Sets the given DAV property on the resource.
     *
     * Attempts to interpret the property as a live property. If that
     * fails, then sets the property as a dead property.
     *
     * @param property the property to set
     *
     * @see setLiveProperty(DavProperty)
     */
    public void setProperty(DavProperty property)
        throws DavException {
        if (! exists())
            throw new DavException(DavServletResponse.SC_NOT_FOUND);

        try {
            setResourceProperty(property);
        } catch (ModelConversionException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        } catch (ModelValidationException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        }

        getContentService().updateItem(item);
    }

    /**
     * Removes the named DAV property from the resource.
     *
     * Attempts to interpret the property as a live property. If that
     * fails, then assumes the property is a dead property.
     *
     * @param property the property to set
     *
     * @see removeLiveProperty(DavPropertyName)
     */
    public void removeProperty(DavPropertyName propertyName)
        throws DavException {
        if (! exists())
            throw new DavException(DavServletResponse.SC_NOT_FOUND);

        try {
            removeResourceProperty(propertyName);
        } catch (ModelConversionException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        } catch (ModelValidationException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        }

        getContentService().updateItem(item);
    }

    /**
     * Sets and removes the specified DAV properties for the
     * resource.
     *
     * @param setProperties the properties to set
     * @param removeProperties the names of properties to remove
     *
     * @see setLiveProperty(DavProperty)
     * @see removeLiveProperty(DavPropertyName)
     */
    public MultiStatusResponse alterProperties(DavPropertySet setProperties,
                                               DavPropertyNameSet removePropertyNames)
        throws DavException {
        if (! exists())
            throw new DavException(DavServletResponse.SC_NOT_FOUND);

        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);

        for (DavPropertyIterator i=setProperties.iterator(); i.hasNext();) {
            DavProperty property = i.nextProperty();

            try {
                setResourceProperty(property);
                msr.add(property.getName(), DavServletResponse.SC_OK);
            } catch (ModelConversionException e) {
                msr.add(property.getName(), DavServletResponse.SC_CONFLICT);
            } catch (ModelValidationException e) {
                msr.add(property.getName(), DavServletResponse.SC_CONFLICT);
            }
        }

        for (DavPropertyNameIterator i=removePropertyNames.iterator();
             i.hasNext();) {
            DavPropertyName name = (DavPropertyName) i.next();

            try {
                removeResourceProperty(name);
                msr.add(name, DavServletResponse.SC_OK);
            } catch (ModelConversionException e) {
                msr.add(name, DavServletResponse.SC_CONFLICT);
            } catch (ModelValidationException e) {
                msr.add(name, DavServletResponse.SC_CONFLICT);
            }
        }

        getContentService().updateItem(item);

        return msr;
    }

    /** */
    public DavResource getCollection() {
        if (parent == null) {
            if (isHomeCollection())
                return null;

            if (log.isDebugEnabled())
                log.debug("getting parent collection for resource " +
                          getResourcePath());

            String parentPath = PathUtil.getParentPath(getResourcePath());
            DavResourceLocator parentLocator =
                getLocator().getFactory().
                createResourceLocator(getLocator().getPrefix(),
                                      getLocator().getWorkspacePath(),
                                      parentPath);

            try {
                parent = (DavCollection) getFactory().
                    createResource(parentLocator, getSession());
            } catch (DavException e) {
                log.error("could not instantiate parent resource " +
                          parentPath + " for resource " + getResourcePath());
                throw new RuntimeException("could not instantiate parent resource", e);
            }
        }
        return parent;
    }

    /** */
    public void move(DavResource destination)
        throws DavException {
        if (! exists())
            throw new DavException(DavServletResponse.SC_NOT_FOUND);

        if (log.isDebugEnabled())
            log.debug("moving resource " + getResourcePath() + " to " +
                      destination.getResourcePath());

        try {
            getContentService().moveItem(item, destination.getResourcePath());
        } catch (ItemNotFoundException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        } catch (DuplicateItemNameException e) {
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        }
    }

    /** */
    public void copy(DavResource destination,
                     boolean shallow)
        throws DavException {
        if (! exists())
            throw new DavException(DavServletResponse.SC_NOT_FOUND);

        if (log.isDebugEnabled())
            log.debug("copying resource " + getResourcePath() + " to " +
                      destination.getResourcePath());

        try {
            getContentService().copyItem(item, destination.getResourcePath(),
                                         ! shallow);
        } catch (ItemNotFoundException e) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        } catch (DuplicateItemNameException e) {
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        }
    }

    /** */
    public boolean isLockable(Type type,
                              Scope scope) {
        // nothing is lockable at the moment
        return false;
    }

    /** */
    public boolean hasLock(Type type,
                           Scope scope) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    /** */
    public ActiveLock getLock(Type type,
                              Scope scope) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    /** */
    public ActiveLock[] getLocks() {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    /** */
    public ActiveLock lock(LockInfo reqLockInfo)
        throws DavException {
        // nothing is lockable at the moment
        throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED,
                               "Resource not lockable");
    }

    /** */
    public ActiveLock refreshLock(LockInfo reqLockInfo,
                                  String lockToken)
        throws DavException {
        // nothing is lockable at the moment
        throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED,
                               "Resource not lockable");
    }

    /** */
    public void unlock(String lockToken)
        throws DavException {
        // nothing is lockable at the moment
        throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED,
                               "Resource not lockable");
    }

    /** */
    public void addLockManager(LockManager lockmgr) {
        // nothing is lockable at the moment
        throw new UnsupportedOperationException();
    }

    /** */
    public DavResourceFactory getFactory() {
        return factory;
    }

    /** */
    public DavSession getSession() {
        return session;
    }

    /** */
    public Report getReport(ReportInfo reportInfo)
        throws DavException {
        // XXX
        throw new UnsupportedOperationException();
    }

    // ExtendedDavResource methods

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isCalendarCollection() {
        return item instanceof CalendarCollectionItem;
    }

    /**
     * Returns true if this resource represents a calendar
     * collection.
     */
    public boolean isHomeCollection() {
        // home collections cannot be created through dav, so if the
        // item does not exist, then it's just a collection that
        // hasn't been saved yet.
        return (item instanceof HomeCollectionItem);
    }

    /**
     * Associates a ticket with this resource and saves it into
     * persistent storage.
     */
    public void saveTicket(Ticket ticket)
        throws DavException {
        if (ticket == null) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        }

        if (log.isDebugEnabled())
            log.debug("adding ticket for " + item.getName());

        item.getTickets().add(ticket);
        //        contentService.createTicket(item, ticket);
    }

    /**
     * Removes the association between the ticket and this resource
     * and deletes the ticket from persistent storage.
     */
    public void removeTicket(Ticket ticket)
        throws DavException {
        if (ticket == null || ticket.getKey() == null) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        }

        if (log.isDebugEnabled())
            log.debug("removing ticket " + ticket.getKey() + " on " +
                      item.getName());

        item.getTickets().remove(ticket);
        //        contentService.removeTicket(item, ticket);
    }

    /**
     * Returns the ticket with the given id on this resource.
     */
    public Ticket getTicket(String id) {
        if (id == null) {
            throw new IllegalArgumentException("no ticket id provided");
        }

        for (Iterator i=item.getTickets().iterator(); i.hasNext();) {
            Ticket t = (Ticket) i.next();
            if (t.getKey().equals(id))
                return t;
        }

        return null;
    }

    /**
     * Returns all visible tickets (those owned by the currently
     * authenticated user) on this resource, or an empty
     * <code>Set</code> if there are no visible tickets.
     */
    public Set getTickets() {
        // XXX: filter out visible tickets
        return item.getTickets();
    }

    // our methods

    /** */
    protected ContentService getContentService() {
        return ((StandardDavResourceFactory) factory).getContentService();
    }

    /** */
    protected CosmoSecurityManager getSecurityManager() {
        return ((StandardDavResourceFactory) factory).getSecurityManager();
    }

    /** */
    protected Item getItem() {
        return item;
    }

    /** */
    protected void setItem(Item item) {
        this.item = item;
        loadProperties();
    }

    /**
     * Returns the DAV resource type codes for this resource.
     *
     * @see ResourceType
     */
    protected abstract int[] getResourceTypes();

    /**
     * Sets the properties of the item backing this resource from the
     * given input context. 
     */
    protected void populateItem(InputContext inputContext) {
        if (log.isDebugEnabled())
            log.debug("populating item for " + getResourcePath());

        item.setName(PathUtil.getBasename(getResourcePath()));

        // if we don't know specifically who the user is, then the
        // owner of the resource becomes the person who issued the
        // ticket
        User owner = getSecurityManager().getSecurityContext().getUser();
        if (owner == null) {
            Ticket ticket = getSecurityManager().getSecurityContext().
                getTicket();
            owner = ticket.getOwner();
        }
        item.setOwner(owner);
    }

    /**
     * Sets the attributes the item backing this resource from the
     * given property set.
     */
    protected MultiStatusResponse populateAttributes(DavPropertySet properties) {
        if (log.isDebugEnabled())
            log.debug("populating attributes for " + getResourcePath());

        MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);

        if (properties == null)
            return msr;

        for (DavPropertyIterator i=properties.iterator(); i.hasNext();) {
            DavProperty property = i.nextProperty();

            try {
                setResourceProperty(property);
                msr.add(property.getName(), DavServletResponse.SC_OK);
            } catch (ModelConversionException e) {
                msr.add(property.getName(), DavServletResponse.SC_CONFLICT);
            } catch (ModelValidationException e) {
                msr.add(property.getName(), DavServletResponse.SC_CONFLICT);
            }
        }

        return msr;
    }

    /**
     * Registers the name of a live property.
     *
     * Typically used in subclass static initializers to add to the
     * list of live properties which are often exposed differently
     * in the model than dead properties.
     */
    protected static void registerLiveProperty(DavPropertyName name) {
        LIVE_PROPERTIES.add(name);
    }

    /**
     * Determines whether or not the given property name identifies a
     * live property.
     * 
     * If the server understands the semantic meaning of a property
     * (probably because the property is defined in a DAV-related
     * specification somewhere), then the property is defined as
     * "live". Live properties typically have their own
     * <code>Item</code> accessor methods with strong typing and often
     * particular semantics.
     *
     * If the server does not know anything specific about the
     * property (usually because it was defined by a particular
     * client), then it is known as a "dead" property. Dead properties
     * are stored as <code>Attribute</code>s with names of the form
     * <code>&lt;namespace prefix&gt;:&lt;namespace URI&gt;&lt;local name&gt;</code>.
     */
    protected boolean isLiveProperty(DavPropertyName name) {
        return LIVE_PROPERTIES.contains(name);
    }

    /**
     * Loads the live DAV properties for the resource.
     */
    protected abstract void loadLiveProperties();

    /**
     * Sets a live DAV property on the resource.
     *
     * If the given property is a live property, then the backing
     * <code>Item</code> is updated. If the property is dead, then the
     * <code>Item</code> is not updated. This method does not persist
     * the changes to the <code>Item</code>. That must be done by the
     * caller.
     *
     * @param property the property to set
     *
     * @throws ModelValidationException if the property is protected
     * or if a null value is specified for a property that does not
     * accept them
     */
    protected abstract void setLiveProperty(DavProperty property);

    /**
     * Removes a live DAV property from the resource.
     *
     * If the given property is a live property, then the backing
     * <code>Item</code> is updated. If the property is dead, then the
     * <code>Item</code> is not updated. This method does not persist
     * the changes to the <code>Item</code>. That must be done by the
     * caller.
     *
     * @param name the name of the property to remove
     *
     * @throws ModelValidationException if the property is protected
     */
    protected abstract void removeLiveProperty(DavPropertyName name);

    /**
     * Returns a list of names of <code>Attribute</code>s that should
     * not be exposed through DAV as dead properties.
     */
    protected abstract Set getDeadPropertyFilter();

    private void loadProperties() {
        if (! exists())
            return;

        if (log.isDebugEnabled())
            log.debug("loading properties for " + getResourcePath());

        // load base live properties
        long creationTime = item.getCreationDate() != null ?
            item.getCreationDate().getTime() :
            new Date().getTime();
        properties.add(new DefaultDavProperty(DavPropertyName.CREATIONDATE,
                                              IOUtil.getCreated(creationTime)));

        properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME,
                                              item.getName()));

        properties.add(new ResourceType(getResourceTypes()));

        // Windows XP support
        properties.add(new DefaultDavProperty(DavPropertyName.ISCOLLECTION,
                                              isCollection() ? "1" : "0"));

        // load subclass live properties
        loadLiveProperties();

        // load dead properties
        for (Iterator<Map.Entry<String,Attribute>>
                 i=item.getAttributes().entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,Attribute> entry = i.next();

            // skip attributes that are not meant to be shown as dead
            // properties
            if (getDeadPropertyFilter().contains(entry.getKey()))
                continue;

            DavPropertyName propName = attrNameToPropName(entry.getKey());

            // ignore live properties, as they'll be loaded separately
            if (isLiveProperty(propName))
                continue;

            properties.add(new DefaultDavProperty(propName,
                                                  entry.getValue().getValue()));
        }
    }

    private void setResourceProperty(DavProperty property) {
        String value = property.getValue() != null ?
            property.getValue().toString() :
            null;

        if (log.isDebugEnabled())
            log.debug("setting property " + property.getName() + " on " +
                      getResourcePath() + " to " + value);

        if (isLiveProperty(property.getName()))
            setLiveProperty(property);
        else
            item.addStringAttribute(propNameToAttrName(property.getName()),
                                    value);

        properties.add(property);
    }

    private void removeResourceProperty(DavPropertyName name) {
        if (log.isDebugEnabled())
            log.debug("removing property " + name + " on " +
                      getResourcePath());

        if (isLiveProperty(name))
            removeLiveProperty(name);
        else
            item.removeAttribute(propNameToAttrName(name));

        properties.remove(name);
    }

    private String propNameToAttrName(DavPropertyName name) {
        String prefix = name.getNamespace() != null ?
            name.getNamespace().getPrefix() : "";
        String uri = name.getNamespace() != null ?
            name.getNamespace().getURI() : "";
        return prefix + "@:@" + uri + "@:@" + name.getName();
    }

    private DavPropertyName attrNameToPropName(String name) {
        String[] chunks = name.split("@:@", 3);

        // no namespace at all
        if (chunks.length == 1)
            return DavPropertyName.create(name);

        String prefix = null;
        String uri = null;
        String value = null;

        if (chunks.length == 2) {
            uri = chunks[0];
            value = chunks[1];
        } else {
            prefix = chunks[0];
            uri = chunks[1];
            value = chunks[2];
        }

        Namespace ns = (prefix == null || prefix.equals("")) ?
            Namespace.getNamespace(uri) :
            Namespace.getNamespace(prefix, uri);

        return DavPropertyName.create(value, ns);
    }
}
