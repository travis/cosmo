/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.caldav;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

/**
 * Provides request functionality required by CalDAV.
 */
public interface CaldavRequest {

    /**
     * Return the list of 'set' entries in the MKCALENDAR request
     * body. The list is empty if the request body could not be parsed
     * or if the request body did not contain any 'set' elements.
     */
    public DavPropertySet getMkCalendarSetProperties();
}