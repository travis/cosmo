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

import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.ResourceType;

import org.apache.log4j.Logger;

import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.acl.property.AlternateUriSet;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.dav.caldav.property.CalendarHomeSet;
import org.osaf.cosmo.model.HomeCollectionItem;
import org.osaf.cosmo.model.ModelValidationException;

/**
 * Extends <code>DavCollection</code> to adapt the Cosmo
 * <code>HomeCollectionItem</code> to the DAV resource model.
 *
 * This class defines the following live properties:
 *
 * <ul>
 * <li><code>DAV:calendar-home-set</code> (protected)</li>
 * <li><code>DAV:alternate-URI-set</code> (protected)</li>
 * </ul>
 *
 * @see DavCollection
 * @see CollectionItem
 */
public class DavHomeCollection extends DavCollection
    implements AclConstants, CaldavConstants {
    private static final Logger log =
        Logger.getLogger(DavHomeCollection.class);
    private static final int[] RESOURCE_TYPES;

    static {
        registerLiveProperty(CALENDARHOMESET);
        registerLiveProperty(ALTERNATEURISET);

        int p = ResourceType.registerResourceType(ELEMENT_ACL_PRINCIPAL,
                                                  NAMESPACE);
        RESOURCE_TYPES = new int[] { ResourceType.COLLECTION, p };
    }

    /** */
    public DavHomeCollection(HomeCollectionItem collection,
                             DavResourceLocator locator,
                             DavResourceFactory factory,
                             DavSession session) {
        super(collection, locator, factory, session);
    }

    // DavResource

    /** */
    public String getSupportedMethods() {
        return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, MKTICKET, DELTICKET, MKCOL, MKCALENDAR";
    }

    // DavResourceBase

    /** */
    protected int[] getResourceTypes() {
        return RESOURCE_TYPES;
    }

    /** */
    protected void loadLiveProperties() {
        super.loadLiveProperties();

        HomeCollectionItem hc = (HomeCollectionItem) getItem();
        if (hc == null)
            return;

        DavPropertySet properties = getProperties();

        properties.add(new CalendarHomeSet(this));
        properties.add(new AlternateUriSet(this));
    }

    /** */
    protected void setLiveProperty(DavProperty property) {
        super.setLiveProperty(property);

        HomeCollectionItem hc = (HomeCollectionItem) getItem();
        if (hc == null)
            return;

        DavPropertyName name = property.getName();

        if (name.equals(CALENDARHOMESET) ||
            name.equals(ALTERNATEURISET))
            throw new ModelValidationException("cannot set protected property " + name);
    }

    /** */
    protected void removeLiveProperty(DavPropertyName name) {
        super.removeLiveProperty(name);

        HomeCollectionItem hc = (HomeCollectionItem) getItem();
        if (hc == null)
            return;

        if (name.equals(CALENDARHOMESET) ||
            name.equals(ALTERNATEURISET))
            throw new ModelValidationException("cannot remove protected property " + name);
    }

    // our methods

    /**
     * Returns a locator that provides the Atom URL for the home
     * collection.
     */
    public DavResourceLocator getAtomLocator() {
        return ((StandardLocatorFactory)getLocator().getFactory()).
            createAtomLocator(getLocator().getPrefix(),
                              getLocator().getResourcePath());
    }

    /**
     * Returns a locator that provides the CMP URL for the home
     * collection.
     */
    public DavResourceLocator getCmpLocator() {
        return ((StandardLocatorFactory)getLocator().getFactory()).
            createCmpLocator(getLocator().getPrefix(),
                             getLocator().getResourcePath());
    }

    /**
     * Returns a locator that provides the web URL for the home
     * collection.
     */
    public DavResourceLocator getWebLocator() {
        return ((StandardLocatorFactory)getLocator().getFactory()).
            createWebLocator(getLocator().getPrefix(),
                             getLocator().getResourcePath());
    }
}
