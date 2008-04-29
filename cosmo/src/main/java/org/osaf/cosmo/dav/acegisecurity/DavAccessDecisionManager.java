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
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.acl.AclEvaluator;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.TicketAclEvaluator;
import org.osaf.cosmo.dav.acl.UserAclEvaluator;
import org.osaf.cosmo.http.Methods;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.UriTemplate;

/**
 * <p>
 * Makes access control decisions for users and user 
 * resources.  Allow service layer to handle authorization
 * for all other resources.
 * </p>
 */
public class DavAccessDecisionManager
    implements AccessDecisionManager, ExtendedDavConstants {
    private static final Log log =
        LogFactory.getLog(DavAccessDecisionManager.class);

    private UserService userService;
  
    // DavAccessDecisionManager methods

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
        AclEvaluator evaluator = null;
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails)
                authentication.getPrincipal();
            evaluator = new UserAclEvaluator(details.getUser());
        } else if (authentication instanceof TicketAuthenticationToken) {
            Ticket ticket = (Ticket) authentication.getPrincipal();
            evaluator = new TicketAclEvaluator(ticket);
        } else {
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }

        HttpServletRequest request =
            ((FilterInvocation)object).getHttpRequest();

        String path = request.getPathInfo();
        if (path == null)
            path = "/";
        // remove trailing slash that denotes a collection
        if (! path.equals("/") && path.endsWith("/"))
            path = path.substring(0, path.length()-1);

        try {
            match(path, request.getMethod(), evaluator);
        } catch (AclEvaluationException e) {
            throw new DavAccessDeniedException(request.getRequestURI(),
                                               e.getPrivilege());
        }
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

    // our methods

    protected void match(String path,
                         String method,
                         AclEvaluator evaluator)
        throws AclEvaluationException {
        if (log.isDebugEnabled())
            log.debug("matching resource " + path + " with method " + method);

        UriTemplate.Match match = null;

        match = TEMPLATE_USERS.match(false, path);
        if (match != null) {
            evaluateUserPrincipalCollection(match, method, evaluator);
            return;
        }

        match = TEMPLATE_USER.match(false, path);
        if (match != null) {
            evaluateUserPrincipal(match, method, evaluator);
            return;
        }
    }

    protected void evaluateUserPrincipalCollection(UriTemplate.Match match,
                                                   String method,
                                                   AclEvaluator evaluator)
        throws AclEvaluationException {
        if (evaluator instanceof TicketAclEvaluator)
            throw new IllegalStateException("A ticket may not be used to access the user principal collection");

        if (method.equals("PROPFIND")) {
            if (log.isDebugEnabled())
                log.debug("Allowing method " + method + " so provider can evaluate check access itself");
            return;            
        }

        UserAclEvaluator uae = (UserAclEvaluator) evaluator;
        DavPrivilege privilege = Methods.isReadMethod(method) ?
            DavPrivilege.READ : DavPrivilege.WRITE;
        if (! uae.evaluateUserPrincipalCollection(privilege)) {
            if (log.isDebugEnabled())
                log.debug("Principal does not have privilege " + privilege + "; denying access");
            throw new AclEvaluationException(null, privilege);
        }

        if (log.isDebugEnabled())
            log.debug("Principal has privilege " + privilege + "; allowing access");
    }

    protected void evaluateUserPrincipal(UriTemplate.Match match,
                                         String method,
                                         AclEvaluator evaluator)
        throws AclEvaluationException {
        if (evaluator instanceof TicketAclEvaluator)
            throw new IllegalStateException("A ticket may not be used to access the user principal collection");

        String username = match.get("username");
        User user = getUserService().getUser(username);
        if (user == null) {
            if (log.isDebugEnabled())
                log.debug("User " + username + " not found; allowing for 404");
            return;
        }    

        if (method.equals("PROPFIND")) {
            if (log.isDebugEnabled())
                log.debug("Allowing method " + method + " so provider can evaluate check access itself");
            return;            
        }

        UserAclEvaluator uae = (UserAclEvaluator) evaluator;
        DavPrivilege privilege = Methods.isReadMethod(method) ?
            DavPrivilege.READ : DavPrivilege.WRITE;
        if (! uae.evaluateUserPrincipal(user, privilege)) {
            if (log.isDebugEnabled())
                log.debug("Principal does not have privilege " + privilege + "; denying access");
            throw new AclEvaluationException(null, privilege);
        }

        if (log.isDebugEnabled())
            log.debug("Principal has privilege " + privilege + "; allowing access");
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    public UserService getUserService() {
        return userService;
    }

    public static class AclEvaluationException extends Exception {
        private Item item;
        private DavPrivilege privilege;

        public AclEvaluationException(Item item,
                                      DavPrivilege privilege) {
            this.item = item;
            this.privilege = privilege;
        }

        public Item getItem() {
            return item;
        }

        public DavPrivilege getPrivilege() {
            return privilege;
        }
    }
}
