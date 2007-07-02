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
package org.osaf.cosmo.filters;

import java.io.IOException;
import java.lang.reflect.Proxy;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dav.impl.BufferedServletInputStream;
import org.springframework.dao.PessimisticLockingFailureException;

/**
 * Filter that detects database deadlocks 
 * (catches PessimisticLockingFailureException) and retries
 * the request a number of times before failing.  The filter
 * only applies to "update" operations, that is PUT, POST, DELETE.
 * TODO:  make filter configurable for retries, method and exception types.
 */
public class DeadlockRetryFilter implements Filter {
    private static final Log log = LogFactory.getLog(DeadlockRetryFilter.class);
    private int maxRetries = 5;
    
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        if("PUT".equals(method) ||
           "POST".equals(method) ||
           "DELETE".equals(method)) {
            
            // Buffer request content
            BufferedServletInputStream bis = new BufferedServletInputStream(request
                    .getInputStream());
            
            // Wrap request in proxy so we can utilize buffered content
            HttpServletRequestInvocationHandler handler = new HttpServletRequestInvocationHandler((HttpServletRequest) request, bis);
            request = (ServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {HttpServletRequest.class}, handler);
            int attempts = 0;
           
            while(attempts < maxRetries) {
                try {
                    chain.doFilter(request, response);
                    break;
                } catch (PessimisticLockingFailureException e) {
                    if(log.isDebugEnabled())
                        log.debug("retrying request " + attempts);
                    attempts++;
                    
                    // Retry and fail after maxRetries attempts
                    if(attempts > maxRetries)
                        throw e;
                    else
                        handler.retryRequest();
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

}
