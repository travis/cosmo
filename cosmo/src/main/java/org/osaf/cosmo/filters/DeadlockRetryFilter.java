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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.PessimisticLockingFailureException;

/**
 * Filter that detects database deadlocks 
 * (catches PessimisticLockingFailureException) and retries
 * the request a number of times before failing.  The filter
 * only applies to "update" operations, that is PUT, POST, DELETE.
 * TODO:  make filter configurable for method and exception types.
 */
public class DeadlockRetryFilter implements Filter {
    private static final Log log = LogFactory.getLog(DeadlockRetryFilter.class);
    private int maxRetries = 10;
    private Class[] exceptions = new Class[] {PessimisticLockingFailureException.class};
    private String[] methods = new String[] {"PUT", "POST", "DELETE", "MKCALENDAR" };
    private static final String PARAM_RETRIES = "retries";
    private static final String PARAM_METHODS = "methods";
    private static final String PARAM_EXCEPTIONS = "exceptions";
    
    
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        
        // only care about certain methods
        if(isFilterMethod(method)) {
            // Wrap request so we can utilize buffered content
            request = new BufferedRequestWrapper((HttpServletRequest) request);
            int attempts = 0;
           
            while(attempts <= maxRetries) {
                try {
                    chain.doFilter(request, response);
                    break;
                } catch (Exception e) {
                    if(isFilterException(e)) {
                        attempts++;
                        log.warn("caught: " + e.getMessage() + " : retrying request " + httpRequest.getMethod()
                                + " " + httpRequest.getRequestURI() + " "
                                + attempts);
                        
                        // Retry and fail after maxRetries attempts
                        if(attempts > maxRetries) {
                            log.error("reached maximum retries for "
                                    + httpRequest.getMethod() + " "
                                    + httpRequest.getRequestURI());
                            sendError((HttpServletResponse) response);
                        }
                        else {
                            ((BufferedRequestWrapper) request).retryRequest();
                            Thread.yield();
                        }
                    } else {
                        log.error("the server encountered an unexpexted error", e);
                        sendError((HttpServletResponse) response);
                        return;
                    }
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }
    
    private boolean isFilterMethod(String method) {
        for(String filterMethod: methods)
            if(filterMethod.equalsIgnoreCase(method))
                return true;
        
        return false;
    }
    
    private boolean isFilterException(Exception e) {
        for(Class exception: exceptions)
            if(exception.isInstance(e))
                return true;
        return false;
    }
    
    private void sendError(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "the server was unable to complete the request");
    }

    public void init(FilterConfig config) throws ServletException {
        try {
            // initialize maxRetries from config parameter
            String param = config.getInitParameter(PARAM_RETRIES);
            if(param!=null)
                maxRetries = Integer.parseInt(param);
            
            // initialize methods to filter
            param = config.getInitParameter(PARAM_METHODS);
            if(param!=null) {
                methods = param.split(",");
            }
            
            // initialize exceptions to catch and retry
            param = config.getInitParameter(PARAM_EXCEPTIONS);
            if(param!=null) {
                String[] classes = param.split(","); 
                exceptions = new Class[classes.length];
                for(int i=0;i<classes.length;i++)
                    exceptions[i] = Class.forName(classes[i]);
            }
        } catch (Exception e) {
            log.error("error configuring filter", e);
            throw new ServletException(e);
        }
    }

}
