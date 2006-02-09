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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet filter that populates the
 * {@link org.acegisecurity.ContextHolder} with a
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

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    /**
     * Examines HTTP servlet requests for ticket ids, creating a
     * {@link TicketAuthenticationToken} if any are found.
     *
     * This method consults {@link #findTicketIds(HttpServletRequest)}
     * to find the set of ticket ids present in the request.
     *
     * Tokens are created with the
     * {@link #createAuthentication(String, Set)} method.
     *
     * A token's path is the path info of the request URI less any
     * trailing "/", or "/" if the URI represents the root resource.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                Set ids = findTicketIds(httpRequest);
                if (! ids.isEmpty()) {
                    String path = httpRequest.getPathInfo();
                    if (path == null || path.equals("")) {
                        path = "/";
                    }
                    if (! path.equals("/") && path.endsWith("/")) {
                        path = path.substring(0, path.length()-1);
                    }

                    Authentication token = createAuthentication(path, ids);
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
     * Returns a {@link java.util.Set} of all ticket ids found in the
     * request, both in the {@link #HEADER_TICKET} header and the
     * {@link #PARAM_TICKET} parameter.
     */
    protected Set findTicketIds(HttpServletRequest request) {
        HashSet ids = new HashSet();
        Enumeration headerValues = request.getHeaders(HEADER_TICKET);
        if (headerValues != null) {
            while (headerValues.hasMoreElements()) {
                String value = (String) headerValues.nextElement();
                String[] atoms = value.split(", ");
                for (int i=0; i<atoms.length; i++) {
                    ids.add(atoms[i]);
                }
            }
        }
        String[] paramValues = request.getParameterValues(PARAM_TICKET);
        if (paramValues != null) {
            for (int i=0; i<paramValues.length; i++) {
                ids.add(paramValues[i]);
            }
        }
        return ids;
    }

    /**
     * Returns a {@link TicketAuthenticationToken} for the given
     * path and ticket ids
     */
    protected Authentication createAuthentication(String path,
                                                  Set ids) {
        return new TicketAuthenticationToken(path, ids);
    }
}
