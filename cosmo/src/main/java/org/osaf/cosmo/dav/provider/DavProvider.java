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

import org.osaf.cosmo.dav.DavCollection;
import org.osaf.cosmo.dav.DavContent;
import org.osaf.cosmo.dav.DavException;
import org.osaf.cosmo.dav.DavRequest;
import org.osaf.cosmo.dav.DavResource;
import org.osaf.cosmo.dav.DavResponse;

/**
 * <p>
 * An interface for components that execute requests against WebDAV resources.
 * </p>
 */
public interface DavProvider {

    public void get(DavRequest request,
                    DavResponse response,
                    DavResource resource)
        throws DavException, IOException;

    public void head(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException;

    public void propfind(DavRequest request,
                         DavResponse response,
                         DavResource resource)
        throws DavException, IOException;
    
    public void proppatch(DavRequest request,
                          DavResponse response,
                          DavResource resource)
        throws DavException, IOException;

    public void put(DavRequest request,
                    DavResponse response,
                    DavContent content)
        throws DavException, IOException;

    public void delete(DavRequest request,
                       DavResponse response,
                       DavResource resource)
        throws DavException, IOException;

    public void copy(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException;

    public void move(DavRequest request,
                     DavResponse response,
                     DavResource resource)
        throws DavException, IOException;

    public void mkcol(DavRequest request,
                      DavResponse response,
                      DavCollection collection)
        throws DavException, IOException;

    public void report(DavRequest request,
                       DavResponse response,
                       DavResource resource)
        throws DavException, IOException;

    public void mkcalendar(DavRequest request,
                           DavResponse response,
                           DavCollection collection)
        throws DavException, IOException;

    public void mkticket(DavRequest request,
                         DavResponse response,
                         DavResource resource)
        throws DavException, IOException;

    public void delticket(DavRequest request,
                          DavResponse response,
                          DavResource resource)
        throws DavException, IOException;

    public void acl(DavRequest request,
                    DavResponse response,
                    DavResource resource)
        throws DavException, IOException;
}
