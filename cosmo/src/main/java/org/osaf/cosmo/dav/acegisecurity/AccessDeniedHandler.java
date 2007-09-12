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
package org.osaf.cosmo.dav.acegisecurity;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.AccessDeniedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dav.ForbiddenException;
import org.osaf.cosmo.dav.impl.StandardDavResponse;

/**
 * <p>
 * Handles <code>AccessDeniedException</code> by sending a 403 response
 * enclosing an XML body describing the error.
 * </p>
 */
public class AccessDeniedHandler
    implements org.acegisecurity.ui.AccessDeniedHandler {
    private static final Log log =
        LogFactory.getLog(AccessDeniedHandler.class);

    public void handle(ServletRequest request,
                       ServletResponse response,
                       AccessDeniedException exception)
        throws IOException, ServletException {
        StandardDavResponse sdr =
            new StandardDavResponse((HttpServletResponse)response);
        sdr.sendDavError(new ForbiddenException(exception.getMessage()));
    }
}
