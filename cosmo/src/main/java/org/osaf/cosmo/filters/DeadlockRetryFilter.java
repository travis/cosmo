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

import org.apache.abdera.protocol.server.servlet.RequestHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * Filter that detects database deadlocks 
 * (catches ConcurrencyFailureException) and retries
 * the request a number of times before failing.  The filter
 * only applies to "update" operations, that is PUT, POST, DELETE.
 * 
 * The filter can also be configured to catch other exception
 * types and be applied to other methods using filter init
 * parameters.
 */
public class DeadlockRetryFilter implements Filter {
    
    private static final Log log = LogFactory.getLog(DeadlockRetryFilter.class);
    private int maxRetries = 10;
    private int maxMemoryBuffer = 1024*256;
    private Class[] exceptions = new Class[] {ConcurrencyFailureException.class};
    private String[] methods = new String[] {"PUT", "POST", "DELETE", "MKCALENDAR" };
    
    private static final String PARAM_RETRIES = "retries";
    private static final String PARAM_METHODS = "methods";
    private static final String PARAM_EXCEPTIONS = "exceptions";
    private static final String PARAM_MAX_MEM_BUFFER = "maxMemoryBuffer";


    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();

        // only care about certain methods
        if(isFilterMethod(method)) {
            // wrap request so we can utilize buffered content
            request = new BufferedRequestWrapper((HttpServletRequest) request, maxMemoryBuffer);
            
            // Wrap response so we can veto a sendError() set by the
            // abdera RequestProcessor
            response = new ResponseErrorWrapper((HttpServletResponse) response);
            
            int attempts = 0;

            while(attempts <= maxRetries) {
                
                Exception ex = null;
                
                try {
                    chain.doFilter(request, response);
                } catch (Exception e) {
                    // Shouldn't happen but this filter supports
                    // catching the exception and looking for it
                    // in the request.
                    if(isFilterException(e)) {
                        ex = e;
                    } else {
                        log.error("the server encountered an unexpected error", e);
                        sendError((ResponseErrorWrapper) response);
                    }
                }
                
                // If we didn't catch it, then look for the exception
                // in the request
                if(ex==null)
                    ex = findFilterException(httpRequest);
                
                // If there was an exception that we were looking for
                // (either caught or found in the request, then prepare
                // to retry.
                if (ex != null) {
                    attempts++;
                    ((ResponseErrorWrapper) response).clearError();
                    log.warn("caught: " + ex.getMessage() + " : retrying request " + httpRequest.getMethod()
                        + " " + httpRequest.getRequestURI() + " "
                        + attempts);

                    // Fail after maxRetries attempts
                    if(attempts > maxRetries) {
                        log.error("reached maximum retries for "
                            + httpRequest.getMethod() + " "
                            + httpRequest.getRequestURI());
                        sendError((ResponseErrorWrapper) response);
                    }
                    // Otherwise, prepare to retry
                    else {
                        ((BufferedRequestWrapper) request).retryRequest();
                        Thread.yield();
                    }
                } 
                // Otherwise flush the error if necessary and
                // proceed as normal.
                else {
                    ((ResponseErrorWrapper) response).flushError();
                    return;
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

    private Exception findFilterException(HttpServletRequest request) {
        Exception e = (Exception)
            request.getAttribute(RequestHandler.ATTR_EXCEPTION);
        if (e == null)
            return null;
        if(isFilterException(e))
            return e;
        return null;
    }

    private void sendError(ResponseErrorWrapper response) throws IOException {
        
        // if error was already queued, flush it
        if(response.flushError())
            return;
        
        // otherwise send a generic error and flush
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "the server was unable to complete the request");
        response.flushError();
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

            // initialize memory buffer size
            param = config.getInitParameter(PARAM_MAX_MEM_BUFFER);
            if(param!=null)
                maxMemoryBuffer = Integer.parseInt(param);
            
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
