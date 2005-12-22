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
package org.osaf.cosmo.acegisecurity;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import net.sf.acegisecurity.context.SecurityContext;
import net.sf.acegisecurity.context.SecurityContextHolder;
import net.sf.acegisecurity.context.SecurityContextImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Associates a fresh <code>SecurityContext</code> with an HTTP
 * request and clears it when the response has been sent. This is
 * useful for stateless protocols like WebDAV that do not understand
 * HTTP sessions.
 *
 * The created <code>SecurityContext</code> is an instance of the
 * class defined by the {@link #setContext(Class)} method (which
 * defaults to {@link
 * net.sf.acegisecurity.context.SecurityContextImpl}.
 *
 * This filter will only execute once per request, to resolve servlet
 * container (specifically Weblogic) incompatibilities.
 *
 * This filter MUST be executed BEFORE any authentication processing
 * mechanisms (eg BASIC, CAS processing filters etc), which expect the
 * <code>SecurityContextHolder</code> to contain a valid
 * <code>SecurityContext</code> by the time they execute.
 */
public class HttpRequestContextIntegrationFilter
    implements InitializingBean, Filter {
    private static final Log log =
        LogFactory.getLog(HttpRequestContextIntegrationFilter.class);
    private static final String FILTER_APPLIED =
        "__acegi_request_integration_filter_applied";

    private Class context = SecurityContextImpl.class;

    public void setContext(Class secureContext) {
        this.context = secureContext;
    }

    public Class getContext() {
        return context;
    }

    // InitializingBean methods

    public void afterPropertiesSet() throws Exception {
        if (context == null ||
            ! SecurityContext.class.isAssignableFrom(context)) {
            throw new IllegalArgumentException("context must be defined and implement SecurityContext (typically use net.sf.acegisecurity.context.SecurityContextImpl; existing class is " + context + ")");
        }
    }

    // Filter methods

    /**
     * Does nothing. We use IoC container lifecycle services instead.
     */
    public void destroy() {
    }

    /**
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is applied only once per request
            chain.doFilter(request, response);
            return;
        }

        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);

        if (log.isDebugEnabled()) {
            log.debug("New SecurityContext instance associated with SecurityContextHolder");
        }
        SecurityContextHolder.setContext(generateNewContext());

        try {
            chain.doFilter(request, response);
        } catch (IOException ioe) {
            throw ioe;
        } catch (ServletException se) {
            throw se;
        } finally {
            // do clean up, even if there was an exception
            SecurityContextHolder.setContext(generateNewContext());
            if (log.isDebugEnabled()) {
                log.debug("SecurityContextHolder refreshed, as request processing completed");
            }
        }
    }

    /**
     * Does nothing. We use IoC container lifecycle services instead.
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    // our methods

    /**
     */
    public SecurityContext generateNewContext() throws ServletException {
        try {
            return (SecurityContext) context.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
