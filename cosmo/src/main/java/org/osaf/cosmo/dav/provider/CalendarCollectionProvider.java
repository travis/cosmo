/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.provider;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResourceFactory;
import org.osaf.cosmo.dav.DavResponse;
import org.osaf.cosmo.dav.ExistsException;
import org.osaf.cosmo.dav.caldav.InvalidCalendarLocationException;
import org.osaf.cosmo.dav.caldav.MissingParentException;
import org.osaf.cosmo.dav.impl.DavCalendarCollection;
import org.osaf.cosmo.dav.impl.DavItemCollection;
import org.osaf.cosmo.model.EntityFactory;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements
 * access to <code>DavCalendarCollection</code> resources.
 * </p>
 *
 * @see DavProvider
 * @see DavCalendarCollection
 */
public class CalendarCollectionProvider extends CollectionProvider {
    private static final Log log =
        LogFactory.getLog(CalendarCollectionProvider.class);

    public CalendarCollectionProvider(DavResourceFactory resourceFactory,
            EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    public void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws DavException, IOException {
        if (collection.exists())
            throw new ExistsException();

        DavItemCollection parent = (DavItemCollection) collection.getParent();
        if (! parent.exists())
            throw new MissingParentException("One or more intermediate collections must be created");
        if (parent.isCalendarCollection())
            throw new InvalidCalendarLocationException("A calendar collection may not be created within a calendar collection");
        // XXX DAV:needs-privilege DAV:bind on parent collection

        if (log.isDebugEnabled())
            log.debug("MKCALENDAR at " + collection.getResourcePath());

        DavPropertySet properties = request.getMkCalendarSetProperties();
        MultiStatusResponse msr =
            collection.getParent().addCollection(collection, properties);

        if (properties.isEmpty() || ! msr.hasNonOk()) {
            response.setStatus(201);
            response.setHeader("Cache-control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            return;
        }

        MultiStatus ms = new MultiStatus();
        ms.addResponse(msr);
        response.sendMultiStatus(ms);
    }
}
