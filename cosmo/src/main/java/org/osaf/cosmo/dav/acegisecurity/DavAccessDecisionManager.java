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

import org.acegisecurity.AccessDecisionManager;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.InsufficientAuthenticationException;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AccessDecisionVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.dav.acl.DavPrivilege;
import org.osaf.cosmo.dav.acl.AclEvaluator;
import org.osaf.cosmo.dav.acl.UserAclEvaluator;
import org.osaf.cosmo.dav.acl.TicketAclEvaluator;
import org.osaf.cosmo.http.Methods;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.PathUtil;
import org.osaf.cosmo.util.UriTemplate;

/**
 * <p>
 * Makes access control decisions by examining the targeted resource's
 * access control list.
 * </p>
 */
public class DavAccessDecisionManager
    implements AccessDecisionManager, ExtendedDavConstants {
    private static final Log log =
        LogFactory.getLog(DavAccessDecisionManager.class);

    private UserService userService;
    private ContentService contentService;

    public DavAccessDecisionManager(UserService userService,
                                    ContentService contentService) {
        this.userService = userService;
        this.contentService = contentService;
    }

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

        match = TEMPLATE_COLLECTION.match(false, path);
        if (match != null) {
            evaluateCollection(match, method, evaluator);
            return;
        }

        match = TEMPLATE_ITEM.match(false, path);
        if (match != null) {
            evaluateItem(match, method, evaluator);
            return;
        }

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

        match = TEMPLATE_HOME.match(false, path);
        if (match != null) {
            evaluateHomeResource(match, method, evaluator);
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Not a dav URL; allowing for 404");
    }

    protected void evaluateCollection(UriTemplate.Match match,
                                      String method,
                                      AclEvaluator evaluator)
        throws AclEvaluationException {
        String uid = match.get("uid");
        CollectionItem collection = null;
        try {
            collection = (CollectionItem)
                getContentService().findItemByUid(uid);
        } catch (ClassCastException e) {
             if (log.isDebugEnabled())
                 log.debug("Item " + uid + " not not a collection; allowing for 403");
             return;
        }

        if (collection == null &&
            ! (method.equals("MKCOL") || method.equals("MKCALENDAR"))) {
            if (log.isDebugEnabled())
                log.debug("Collection " + uid + " not found; allowing for 404");
            return;
        }

        CollectionItem parent = null;
        
        // If collection doesn't exist, then the parent must be
        // the principal's home collection, otherwise use the parent
        // of the collection (there should only be one)
        if(collection==null)
            parent = findHomeCollection(evaluator.getPrincipal());
        else 
            parent = collection.getParents().iterator().next();
        
        evaluateItemAcl(collection, parent, method, evaluator);
    }

    protected void evaluateItem(UriTemplate.Match match,
                                String method,
                                AclEvaluator evaluator)
        throws AclEvaluationException {
        String uid = match.get("uid");
        Item item = getContentService().findItemByUid(uid);
        if (item == null && ! method.equals("PUT")) {
            if (log.isDebugEnabled())
                log.debug("Item " + uid + " not found; allowing for 404");
            return;
        }

        CollectionItem home = findHomeCollection(evaluator.getPrincipal());

        evaluateItemAcl(item, home, method, evaluator);
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

    protected void evaluateHomeResource(UriTemplate.Match match,
                                        String method,
                                        AclEvaluator evaluator)
        throws AclEvaluationException {
        String username = match.get("username");
        String path = match.get("*");

        if (path != null) {
            if (log.isDebugEnabled())
                log.debug("Looking up resource at '" + path + "' within home collection of " + username);
        } else {
            if (log.isDebugEnabled())
                log.debug("Looking up home collection of " + username);
        }

        User homeowner = userService.getUser(username);
        if (homeowner == null) {
            if (log.isDebugEnabled())
                log.debug("User " + username + " not found; allowing for 404");
            return;
        }

        CollectionItem home = contentService.getRootItem(homeowner);
        Item item = path != null ?
            contentService.findItemByPath(path, home.getUid()) :
            home;
        if (item == null) {
            // the targeted resource does not exist. that's okay for write
            // methods, but it's not ok for read methods.
            if (! (method.equals("PUT") || method.equals("MKCOL") ||
                   method.equals("MKCALENDAR"))) {
                // if the principal does not even have permission to read the
                // targeted home collection, they get a 403.
                if (! evaluator.evaluate(home, DavPrivilege.READ))
                    throw new AclEvaluationException(home, DavPrivilege.READ);
                // the principal has enough access to see a 404.
                if (log.isDebugEnabled())
                    log.debug("Item '" + path + "' within home collection for " + username + " not found; allowing for 404");
                return;
            }
        }

        CollectionItem parent = home;
        if (path != null && path != "/") {
            String parentPath = PathUtil.getParentPath(path);
            Item parentItem = null;
            if (parentPath != "/") {
                try {
                    parentItem = contentService.findItemByPath(parentPath, home.getUid());
                    if (parentItem == null) {
                        // the targeted resource's parent does not exist. that
                        // is a conflict condition.
                        // if the principal does not even have permission to
                        // read the targeted home collection, they get a 403.
                        if (! evaluator.evaluate(home, DavPrivilege.READ))
                            throw new AclEvaluationException(home, DavPrivilege.READ);
                        // the principal has enough access to see a 409.
                        if (log.isDebugEnabled())
                            log.debug("Parent item '" + parentPath + "' within home collection for " + username + " not found; allowing for 409");
                        return;
                    }
                    parent = (CollectionItem) parentItem;
                } catch (ClassCastException e) {    
                    // the targeted resource's parent is not a collection.
                    // that is a forbidden no matter how you slice it.
                    throw new AclEvaluationException(parentItem, DavPrivilege.READ);
                }
            }
        }

        evaluateItemAcl(item, parent, method, evaluator);
    }

    /**
     * <p>
     * Evaluates the ACL of the provided item for the given method. Chooses
     * the privilege to evaluate based on the method.
     * </p>
     * <p> For <code>MKCOL</code>, <code>MKCALENDAR</code>,
     * <code>DELETE</code>, and for <code>PUT</code> when the item does not
     * already exist, the privilege is actually evaluated against the parent
     * collection instead.
     * </p>
     * <p>
     * To account for re-publishing shared items, if a principal is
     * attempts to access an item he does not have the privilege for, the
     * parent collection of the item (as accessed through the request URL) is
     * also checked, unless the method is one of those listed above that was
     * checked against the parent collection to begin with.
     * </p>
     */
    protected void evaluateItemAcl(Item item,
                                   CollectionItem parent,
                                   String method,
                                   AclEvaluator evaluator)
        throws AclEvaluationException {
        DavPrivilege privilege = null;

        if (method.equals("REPORT") || method.equals("PROPFIND")) {
            if (log.isDebugEnabled())
                log.debug("Allowing method " + method + " so provider can evaluate check ticket access itself");
            return;
        } else if (method.equals("MKCOL") || method.equals("MKCALENDAR")) {
            item = parent;
            privilege = DavPrivilege.BIND;
        } else if (method.equals("DELETE")) {
            item = parent;
            privilege = DavPrivilege.UNBIND;
        } else if (method.equals("PROPPATCH")) {
            privilege = DavPrivilege.WRITE_PROPERTIES;
        } else if (method.equals("PUT")) {
            if (item == null) {
                item = parent;
                privilege = DavPrivilege.BIND;
            } else {
                privilege = DavPrivilege.WRITE_CONTENT;
            }
        } else if (method.equals("MKTICKET") || method.equals("DELTICKET")) {
            privilege = DavPrivilege.WRITE;
        } else {
            privilege = DavPrivilege.READ;
        }

        if (item == null)
            throw new IllegalStateException("No item to evaluate");

        if (log.isDebugEnabled())
            log.debug("Deciding on resource '" + item.getName() + "' for privilege " + privilege);

        if (evaluator.evaluate(item, privilege)) {
            if (log.isDebugEnabled())
                log.debug("Principal has privilege " + privilege + "; allowing " + method);
            return;
        }

        if (parent != null && item != parent) {
            if (log.isDebugEnabled())
                log.debug("Deciding on parent '" + parent.getName() + "' for privilege " + privilege);

            if (evaluator.evaluate(parent, privilege)) {
                if (log.isDebugEnabled())
                    log.debug("Principal has privilege " + privilege + " on parent; allowing " + method);
                return;
            }
        }

        if (log.isDebugEnabled())
            log.debug("Principal does not have privilege " + privilege + "; denying " + method);
            throw new AclEvaluationException(item, privilege);
    }

    public ContentService getContentService() {
        return contentService;
    }

    public UserService getUserService() {
        return userService;
    }

    private CollectionItem findHomeCollection(Object principal) {
        User user = principal instanceof Ticket ?
            ((Ticket) principal).getOwner() : (User) principal;

        return contentService.getRootItem(user);
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
