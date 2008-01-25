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
package org.osaf.cosmo.mc.acegisecurity;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.AccessDecisionManager;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.InsufficientAuthenticationException;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.acegisecurity.providers.ticket.TicketAuthenticationToken;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.UriTemplate;

/**
 * <p>
 * Makes access control decisions for morse code requests.
 * A morse code request supports write operations (PUT,POST,DELETE)
 * and a read operation (GET).  All operations are done on a 
 * collection resource.  When creating a new collection (PUT), the
 * principal must have write access to the home collection. For all
 * other operations, the principal must have read or write access 
 * to the collection itself.
 * </p>
 */
public class MorseCodeAccessDecisionManager
    implements AccessDecisionManager {
    private static final Log log =
        LogFactory.getLog(MorseCodeAccessDecisionManager.class);

    private UserService userService;
    private ContentService contentService;
    
    // only supported mc uri is collection
    private static final UriTemplate TEMPLATE_COLLECTION =
        new UriTemplate("/collection/{uid}/*");

    public MorseCodeAccessDecisionManager(UserService userService,
                                    ContentService contentService) {
        this.userService = userService;
        this.contentService = contentService;
    }

   
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

        if (!((authentication instanceof UsernamePasswordAuthenticationToken) || (authentication instanceof TicketAuthenticationToken)))
            throw new InsufficientAuthenticationException(
                    "Unrecognized authentication token");
        
        String path = request.getPathInfo();
        if (path == null)
            path = "/";
        // remove trailing slash that denotes a collection
        if (! path.equals("/") && path.endsWith("/"))
            path = path.substring(0, path.length()-1);

        try {
            match(path, request.getMethod(),authentication);
        } catch (AclEvaluationException e) {
            throw new MorseCodeAccessDeniedException(request.getRequestURI());
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
                         Authentication authentication)
        throws AclEvaluationException {
        if (log.isDebugEnabled())
            log.debug("matching resource " + path + " with method " + method);
        
        UriTemplate.Match match = null;

        match = TEMPLATE_COLLECTION.match(false, path);
        if (match != null) {
            evaluateCollection(match, method, authentication);
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Not a morse code URL; allowing for 404");
    }

    protected void evaluateCollection(UriTemplate.Match match,
                                      String method,
                                      Authentication authentication)
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

        // handle case of accessing a collection that doesn't exist
        if (collection == null &&
            ! method.equals("PUT")) {
            if (log.isDebugEnabled())
                log.debug("Collection " + uid + " not found; allowing for 404");
            return;
        }
        
        // Handle case of new collection, in which case we need to check
        // permissions of parent,  which is the home collection of the principal
        if(collection==null && method.equals("PUT")) {
            // ticketed principal can't create collections in morse code
            if(authentication instanceof TicketAuthenticationToken)
                throw new AclEvaluationException(null);
            
            // For now, only owner of home collection can create
            // new collections using morse code
            CosmoUserDetails details = (CosmoUserDetails) authentication
                    .getPrincipal();
            CollectionItem home = contentService.getRootItem(details.getUser());
            if (!hasWriteAccess(home, authentication))
                throw new AclEvaluationException(home);
            return;
        } 

        // otherwise evaluate collection permissions
        evaluateCollectionAcl(collection, method, authentication);
    }

    /**
     * <p>
     * Evaluates the ACL of the provided collection for the given method. Chooses
     * the privilege to evaluate based on the method.  For morse code the
     * supported methods are GET,PUT,POST,DELETE.  GET requires read access, and
     * the rest require write access.
     * </p>
     */
    protected void evaluateCollectionAcl(CollectionItem collection,
                                   String method,
                                   Authentication authentication)
        throws AclEvaluationException {
       
        boolean requiresWriteAccess = false;
        
        if (method.equals("DELETE") || method.equals("POST") 
                || method.equals("PUT")) {
            requiresWriteAccess = true;
        }

        if (collection == null)
            throw new IllegalStateException("No collection to evaluate");

        if (log.isDebugEnabled()) {
            if(requiresWriteAccess)
                log.debug("Deciding on collection '" + collection.getUid() + "' (requires write access)");
            else
                log.debug("Deciding on collection '" + collection.getUid() + "' (requires read access)");
        }
        
        if(requiresWriteAccess) {
            if(hasWriteAccess(collection, authentication))
                return;
        } else {
            if(hasReadAccess(collection, authentication))
                return;
        }
               
        if (log.isDebugEnabled())
            log.debug("Principal does not have sufficient privileges; denying " + method);
            
        throw new AclEvaluationException(collection);
    }

    public ContentService getContentService() {
        return contentService;
    }

    public UserService getUserService() {
        return userService;
    }
    
    private boolean hasReadAccess(CollectionItem collection,  Authentication authentication) {
        // A principal has read access if the principal is a user and the user
        // is the owner, or if the principal is a ticket that gives read access
        // to the collection, or the principal is an admin
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails)
                authentication.getPrincipal();
            return details.getUser().getAdmin().booleanValue() || details.getUser().equals(collection.getOwner());
        } else if (authentication instanceof TicketAuthenticationToken) {
            Ticket ticket = (Ticket) authentication.getPrincipal();
            return ticket.isGranted(collection) && (ticket.isReadOnly() || ticket.isReadWrite());
        } else {
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }
    }
    
    private boolean hasWriteAccess(CollectionItem collection,  Authentication authentication) {
        // A principal has write access if the principal is a user and the user
        // is the owner, or if the principal is a ticket that gives write access
        // to the collection, or the principal is an admin
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            CosmoUserDetails details = (CosmoUserDetails)
                authentication.getPrincipal();
            return details.getUser().getAdmin().booleanValue() || details.getUser().equals(collection.getOwner());
        } else if (authentication instanceof TicketAuthenticationToken) {
            Ticket ticket = (Ticket) authentication.getPrincipal();
            return ticket.isGranted(collection) && ticket.isReadWrite();
        } else {
            throw new InsufficientAuthenticationException("Unrecognized authentication token");
        }
    }

    public static class AclEvaluationException extends Exception {
        private Item item;
        
        public AclEvaluationException(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }
}
