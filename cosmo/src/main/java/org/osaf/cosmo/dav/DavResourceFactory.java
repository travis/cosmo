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
package org.osaf.cosmo.dav;

import org.osaf.cosmo.calendar.query.CalendarQueryProcessor;
import org.osaf.cosmo.icalendar.ICalendarClientFilterManager;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;

/**
 * Interface for components that create dav resources to represent
 * persisted items.
 *
 * @see DavResource
 * @see Item
 */
public interface DavResourceFactory {

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link DavResource}.
     * </p>
     * <p>
     * If the identified resource does not exist and the request method
     * indicates that one is to be created, returns a resource backed by a 
     * newly-instantiated item that has not been persisted or assigned a UID.
     * Otherwise, if the resource does not exists, then a
     * {@link NotFoundException} is thrown.
     * </p>
     */
    public DavResource resolve(DavResourceLocator locator,
                               DavRequest request)
        throws DavException;

    /**
     * <p>
     * Resolves a {@link DavResourceLocator} into a {@link DavResource}.
     * </p>
     * <p>
     * If the identified resource does not exists, returns <code>null</code>.
     * </p>
     */
    public DavResource resolve(DavResourceLocator locator)
        throws DavException;

    /**
     * <p>
     * Instantiates a <code>DavResource</code> representing the
     * <code>Item</code> located by the given <code>DavResourceLocator</code>.
     * </p>
     */
    public DavResource createResource(DavResourceLocator locator,
                                      Item item)
        throws DavException;

    public ContentService getContentService();
    
    public ICalendarClientFilterManager getClientFilterManager();

    public CalendarQueryProcessor getCalendarQueryProcessor();
    
    public UserService getUserService();

    public CosmoSecurityManager getSecurityManager();
}
