/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.osaf.cosmo.acegisecurity.providers.ticket;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.context.SecurityContextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet filter that detects if a ticket is associated with
 * the current context and clears the context.
 */
public class TicketAuthenticationClearingFilter implements Filter {
    private static final Log log =
        LogFactory.getLog(TicketAuthenticationClearingFilter.class);

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    /**
     * Detects if a ticket is associated with
     * the current context and clears the context.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        
        
        SecurityContext sc = SecurityContextHolder.getContext();
        if(sc.getAuthentication()!=null && sc.getAuthentication() instanceof TicketAuthenticationToken) {
            if(log.isDebugEnabled())
                log.debug("found ticket authentication clearing...");
            SecurityContextHolder.setContext(new SecurityContextImpl());
        }
        
        chain.doFilter(request, response);
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

}
