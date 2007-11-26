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

import java.util.Date;

import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;

import org.apache.abdera.util.EntityTag;

import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.caldav.CaldavRequest;
import org.osaf.cosmo.dav.report.ReportRequest;
import org.osaf.cosmo.dav.ticket.TicketDavRequest;

/**
 * A marker interface that collects the functionality defined by
 * the various WebDAV extensions implemented by the DAV service.
 */
public interface DavRequest
    extends WebdavRequest, CaldavRequest, ReportRequest, TicketDavRequest {

    public EntityTag[] getIfMatch();

    public Date getIfModifiedSince();

    public EntityTag[] getIfNoneMatch();

    public Date getIfUnmodifiedSince();

    public int getPropFindType()
        throws DavException;

    public DavPropertyNameSet getPropFindProperties()
        throws DavException;
        
    public DavPropertySet getProppatchSetProperties()
        throws DavException;

    public DavPropertyNameSet getProppatchRemoveProperties()
        throws DavException;

    public DavResourceLocator getResourceLocator();

    public DavResourceLocator getDestinationResourceLocator()
        throws DavException;
}
