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
package org.osaf.cosmo.acegisecurity.ticket;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.context.security.SecureContextUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet filter that populates the
 * {@link net.sf.acegisecurity.ContextHolder} with a
 * {@link TicketAuthenticationToken} if needed.
 *
 * @see http://www.sharemation.com/%7Emilele/public/dav/draft-ito-dav-ticket-00.txt
 */
public class TicketProcessingFilter implements Filter {
    private static final Log log =
        LogFactory.getLog(TicketProcessingFilter.class);

    /** the request parameter containing a ticket: <code>ticket</code> */
    public static final String PARAM_TICKET = "ticket";
    /** the request header containing a ticket: <code>Ticket</code> */
    public static final String HEADER_TICKET = "Ticket";

    private String resourcePathPrefix;

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    /**
     * Examines HTTP servlet requests for a ticket, creating a
     * {@link TicketAuthenticationToken} if one is found.
     *
     * Looks first for the request parameter named
     * by <code>PARAM_TICKET</code>. If one is not found, then looks
     * for the request header named by <code>HEADER_TICKET</code>.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        SecureContext sc = SecureContextUtils.getSecureContext();
        if (sc.getAuthentication() == null) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String id = httpRequest.getParameter(PARAM_TICKET);
                if (id == null || id.equals("")) {
                    id = httpRequest.getHeader(HEADER_TICKET);
                }
                if (id != null && ! id.equals("")) {
                    // fix up the path to remove the resource path
                    // prefix (often the servlet path but not always),
                    // to remove trailing slashes (used by webdav
                    // to indicate collections), and to use "/" for
                    // the root item
                    String path = httpRequest.getRequestURI();
                    if (resourcePathPrefix != null &&
                        path.startsWith(resourcePathPrefix)) {
                        path = path.substring(resourcePathPrefix.length());
                    }
                    if (path == null || path.equals("")) {
                        path = "/";
                    }
                    else if (path.endsWith("/")) {
                        path = path.substring(0, path.length()-1);
                    }
                    // cadaver for some reason appends a "/" to the
                    // ticket query param when doing "open
                    // http://localhost:8080/home/bcm/?ticket=deadbeef"
                    if (id.endsWith("/")) {
                        id = id.substring(0, id.length()-1);
                    }
                    Authentication token = createAuthentication(path, id);
                    sc.setAuthentication(token);
                    if (log.isDebugEnabled()) {
                        log.debug("Replaced ContextHolder with ticket token: " +
                                  sc.getAuthentication());
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

    // our methods

    /**
     * Returns a {@link TicketAuthenticationToken} for the given
     * ticket.
     */
    protected Authentication createAuthentication(String path, String id) {
        return new TicketAuthenticationToken(path, id);
    }

    /**
     */
    public String getResourcePathPrefix() {
        return resourcePathPrefix;
    }

    /**
     */
    public void setResourcePathPrefix(String resourcePathPrefix) {
        this.resourcePathPrefix = resourcePathPrefix;
    }
}
