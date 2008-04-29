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
package org.osaf.cosmo.acegisecurity.providers.wsse;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.wsse.UsernameToken;
import org.osaf.cosmo.wsse.WsseUtils;

/**
 * Servlet filter that populates the
 * {@link org.springframework.security.ContextHolder} with a
 * {@link WsseAuthenticationToken} if needed.
 */
public class WsseTokenProcessingFilter implements Filter {
    
    public static final String WSSE_HEADER = "X-WSSE";
    private static final Log log =
        LogFactory.getLog(WsseTokenProcessingFilter.class);

    // Filter methods

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    /**
     * Examines HTTP servlet requests for WSSE Username token, creating a
     * {@link WsseAuthenticationToken} if any are found.
     *
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() == null) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                
                UsernameToken userToken = parseWsseToken(httpRequest);
                
                if(userToken!=null) {
                    Authentication token = new WsseAuthenticationToken(userToken);
                    sc.setAuthentication(token);
                    if (log.isDebugEnabled()) {
                        log.debug("Replaced ContextHolder with wsse token: " +
                                  sc.getAuthentication());
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    private UsernameToken parseWsseToken(HttpServletRequest request) {
        String wsseToken = request.getHeader(WSSE_HEADER);
        
        if(wsseToken==null)
            return null;
        
        wsseToken = wsseToken.trim();
        
        return WsseUtils.parseWsseToken(wsseToken);
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

    // our methods
}
