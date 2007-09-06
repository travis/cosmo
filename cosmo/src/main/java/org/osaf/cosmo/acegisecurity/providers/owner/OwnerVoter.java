/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.acegisecurity.providers.owner;

import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AccessDecisionVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ItemNotFoundException;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.server.ItemPath;
import org.osaf.cosmo.server.UserPath;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.util.PathUtil;

/**
 * Allows access to an item if the authenticated user owns the item.
 *
 * @see AccessDecisionVoter
 * @see Item
 */
public class OwnerVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(OwnerVoter.class);

    private ContentService contentService;
    private UserService userService;
    private boolean indirectlyAddressable = false;

    /**
     * Votes affirmatively if the authenticated principal is a user
     * (not a ticket) and if the requested item is owned by the user.
     * Denies access if the principal is a user but the item is not
     * owned by the user. Abstains if the <code>authentication</code>
     * does not represent a user or if the requested item is not
     * found.
     *
     * @param authentication the token representing the authenticated
     * principal
     * @param object the <code>FilterInvocation</code> being invoked
     * @param config the configuration attributes associated with the
     * invoked method
     * @return either {@link #ACCESS_GRANTED}, {@link #ACCESS_DENIED},
     * or {@link #ACCESS_ABSTAIN}
     * @see UsernamePasswordAuthenticationToken
     */
    public int vote(Authentication authentication,
                    Object object,
                    ConfigAttributeDefinition config) {
        if (! (authentication instanceof UsernamePasswordAuthenticationToken))
            return ACCESS_ABSTAIN;

        String path =
            ((FilterInvocation)object).getHttpRequest().getPathInfo();
        if (path == null)
            return ACCESS_ABSTAIN;
        // remove trailing slash that denotes a collection
        if (! path.equals("/") && path.endsWith("/"))
            path = path.substring(0, path.length()-1);

        CosmoUserDetails details =
            (CosmoUserDetails) authentication.getPrincipal();

        // an item is said to be "directly addressed" if the path
        // addresses a collection or item but does not have additional
        // path info.
        //
        // a collection or item is said to be "indirectly addressed"
        // if the path addresses a collection and includes additional
        // path info addressing a descendent collection or item.
        //
        // the possible outcomes of path resolution are:
        //
        // 1) a directly addressed item exists - proceed to owner check
        // 2) a directly addressed item does not exist - granted, as
        //    the protocol layer will interpret this as a request to
        //    create the directly addressed item or will return a "not
        //    found" response
        // 3) an indirectly addressed item exists - proceed to owner check
        // 4) an indirectly addressed item does not exist, but its
        //    parent exists - proceed to owner check, as the protocol
        //    layer will interpret this as a request to create the
        //    indirectly addressed child item or will return a "not
        //    found" response
        // 5) an indirectly addressed item does not exist, and its
        //    parent does not exist - causes abstention, as we can't
        //    find enough information to make a decision

        Item item = null;

        CollectionPath cp = CollectionPath.parse(path, true);
        if (cp != null) {
            if (cp.getPathInfo() != null) {
                if (indirectlyAddressable) {
                    // find indirectly addressed item
                    try {
                        item = contentService.
                            findItemByPath(cp.getPathInfo(), cp.getUid());
                    } catch (ItemNotFoundException e) {
                        // parent does not exist - case 5
                        if (log.isDebugEnabled())
                            log.debug("Indirectly addressed item at " + path + " not found and parent not found; abstaining");
                        return ACCESS_ABSTAIN;
                    }
                    if (item == null) {
                        // find indirectly addressed item's parent
                        String parentPath =
                            PathUtil.getParentPath(cp.getPathInfo());
                        
                        // If the parent path is "/", then the parent will be
                        // the directly addressed collection
                        if(parentPath.equals("/"))
                            item = contentService.findItemByUid(cp.getUid());
                        else
                            item = contentService.
                                findItemByPath(parentPath, cp.getUid());
                        
                        if (item == null) {
                            // case 5
                            if (log.isDebugEnabled())
                                log.debug("Indirectly addressed item at " + path + " not found and parent not found; abstaining");
                            return ACCESS_ABSTAIN;
                        }
                        // case 4
                        if (log.isDebugEnabled())
                            log.debug("Indirectly addressed item at " + path + " not found but parent found; proceeding to owner check");
                        return checkOwnership(details.getUser(), item);
                    }
                    // case 3
                    if (log.isDebugEnabled())
                        log.debug("Indirectly addressed item at " + path + " found; proceeding to owner check");
                    return checkOwnership(details.getUser(), item);
                }
                if (log.isDebugEnabled())
                    log.debug("Ignoring extra path info found since indirect addressing is not allowed");
            }

            // find directly addressed collection
            item = contentService.findItemByUid(cp.getUid());
            if (item == null) {
                // case 2
                if (log.isDebugEnabled())
                    log.debug("Directly addressed item at " + path + " not found; allowing");
                return ACCESS_GRANTED;
            }
            // case 1
            if (log.isDebugEnabled())
                log.debug("Directly addressed item at " + path + " found; proceeding to owner check");
            return checkOwnership(details.getUser(), item);
        }

        ItemPath ip = ItemPath.parse(path, true);
        if (ip != null) {
            // find directly addressed item
            item = contentService.findItemByUid(ip.getUid());
            if (item == null) {
                // case 2
                if (log.isDebugEnabled())
                    log.debug("Directly addressed item at " + path + " not found; allowing");
                return ACCESS_GRANTED;
            }
            // case 1
            if (log.isDebugEnabled())
                log.debug("Directly addressed item at " + path + " found; proceeding to owner check");
            return checkOwnership(details.getUser(), item);
        }

        UserPath up = UserPath.parse(path, true);
        if (up != null) {
            User user = userService.getUser(up.getUsername());
            if (user == null) {
                if (log.isDebugEnabled())
                    log.debug("Directly addressed user " + up.getUsername() + " not found; denying");
                return ACCESS_DENIED;
            }
            if (log.isDebugEnabled())
                log.debug("Directly addressed user " + up.getUsername() + " found; proceeding to owner check");
            return checkOwner(details.getUser(), user);
        }

        // fall back to old-school hierarchical path resolution. these
        // paths are treated just like indirect addressing except that
        // items are looked up relative to the user's root collection.

        item = contentService.findItemByPath(path);
        if (item == null) {
            String parentPath = PathUtil.getParentPath(path);
            item = contentService.findItemByPath(parentPath);
            if (item == null) {
                // case 5
                if (log.isDebugEnabled())
                    log.debug("Hierarchically addressed item at " + path + " not found and parent not found; abstaining");
                return ACCESS_ABSTAIN;
            }
            // case 4
            if (log.isDebugEnabled())
                log.debug("Hierarchically addressed item at " + path + " not found but parent found; proceeding to owner check");
            return checkOwnership(details.getUser(), item);
        }
        // case 3
        if (log.isDebugEnabled())
            log.debug("Hierarchically addressed item at " + path + " found; proceeding to owner check");
        return checkOwnership(details.getUser(), item);
    }

    private int checkOwnership(User principal,
                               Item item) {
        if (log.isDebugEnabled())
            log.debug("Checking ownership of item " + item.getUid() +
                      " by user " + principal.getUsername());

        if (! item.getOwner().equals(principal)) {
            if (log.isDebugEnabled())
                log.debug("User not owner - access denied");
            return ACCESS_DENIED;
        }

        if (log.isDebugEnabled())
            log.debug("User is owner - access granted");
        return ACCESS_GRANTED;
    }

    private int checkOwner(User principal,
                           User user) {
        if (log.isDebugEnabled())
            log.debug("Checking access to user " + user.getUsername() +
                      " by user " + principal.getUsername());

        if (! user.equals(principal)) {
            if (log.isDebugEnabled())
                log.debug("User not owner - access denied");
            return ACCESS_DENIED;
        }

        if (log.isDebugEnabled())
            log.debug("User is owner - access granted");
        return ACCESS_GRANTED;
    }

    /**
     * Always returns true, since this voter does not examine any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    /**
     * Returns true if the secure object is a
     * {@link FilterInvocation}.
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }

    /** */
    public ContentService getContentService() {
        return contentService;
    }

    /** */
    public void setContentService(ContentService service) {
        contentService = service;
    }

    /** */
    public UserService getUserService() {
        return userService;
    }

    /** */
    public void setUserService(UserService service) {
        userService = service;
    }

    /** */
    public boolean isIndirectlyAddressable() {
        return indirectlyAddressable;
    }

    /** */
    public void setIndirectlyAddressable(boolean indirectlyAddressable) {
        this.indirectlyAddressable = indirectlyAddressable;
    }
}
