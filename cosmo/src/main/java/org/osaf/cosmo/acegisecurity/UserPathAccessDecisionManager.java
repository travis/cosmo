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
package org.osaf.cosmo.acegisecurity;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.InsufficientAuthenticationException;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.osaf.cosmo.acegisecurity.providers.wsse.WsseAuthenticationToken;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.UserPath;

/**
 * <p>
 * Makes access control decisions for morse code requests.
 * Only verifies principal has access to user paths.  All other
 * paths are handled by the secure ContentService.
 * </p>
 */
public class UserPathAccessDecisionManager
    implements AccessDecisionManager {
    private static final Log log =
        LogFactory.getLog(UserPathAccessDecisionManager.class);

  
    /**
     * <p>
     * </p>
     *
     * @throws InsufficientAuthenticationException if
     * <code>Authentication</code> is not a
     * {@link UsernamePasswordAuthenticationToken} or a
     * {@link TicketAuthenticationToken}.
     */
    public void decide(Authentication authentication,
                       Object object,
                       ConfigAttributeDefinition config)
        throws AccessDeniedException, InsufficientAuthenticationException {

        HttpServletRequest request =
            ((FilterInvocation)object).getHttpRequest();

        if (!((authentication instanceof UsernamePasswordAuthenticationToken) || (authentication instanceof TicketAuthenticationToken)
                || (authentication instanceof WsseAuthenticationToken)))
            throw new InsufficientAuthenticationException(
                    "Unrecognized authentication token");
        
        // Only check user paths for now
        UserPath up = UserPath.parse(request.getPathInfo(), true);
        if(up!=null) {
            // Must be user
            if(! (authentication instanceof UsernamePasswordAuthenticationToken) &&
               ! (authentication instanceof WsseAuthenticationToken))
                throw new AccessDeniedException("principal cannot access resource");
            
            CosmoUserDetails details = (CosmoUserDetails) authentication.getPrincipal();
            User user = details.getUser();
            
            // User must be admin or the User that matches path
            if(user.getUsername().equalsIgnoreCase(up.getUsername()) || user.getAdmin().booleanValue())
                return;
            
            // otherwise request is unauthorized
            throw new AccessDeniedException("principal cannot access resource");
        }
     
        // Let all other authorization be handled by service layer
      
    }

    /**
     * Always returns true, as this manager does not support any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) { return true; }

    /**
     * Returns true if the secure object is a
     * {@link FilterInvocation}.
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }

    
}
