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
package org.osaf.cosmo.acegisecurity.vote;

import javax.servlet.http.HttpServletRequest;

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
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;

/**
 * <p>
 * A base class for voters that control access by users to HTTP resources.
 * </p>
 *
 * @see AccessDecisionVoter
 */
public abstract class UserResourceVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(UserResourceVoter.class);

    private ContentService contentService;
    private UserService userService;

    /**
     * <p>
     * Votes affirmatively if the authenticated user principal has
     * access to the resource at the given URI path for the given method.
     * The URI path is specified relative to the servlet path. URI paths
     * representing collection resources do not have trailing slashes.
     * </p>
     */
    public abstract int vote(User user,
                             String path,
                             String method);

    /**
     * <p>
     * Ensures that the authenticated principal represents a Cosmo
     * user account and then delegates to
     * {@link vote(CosmoUserDetails, String, String)}. Abstains if
     * <code>authentication</code> is not an instance of
     * {@link UsernamePasswordAuthenticationToken} or if the URI path
     * can not be calculated.
     * </p>
     */
    public int vote(Authentication authentication,
                    Object object,
                    ConfigAttributeDefinition config) {
        if (! (authentication instanceof UsernamePasswordAuthenticationToken))
            return ACCESS_ABSTAIN;

        HttpServletRequest request =
            ((FilterInvocation)object).getHttpRequest();

        String path = request.getPathInfo();
        if (path == null)
            return ACCESS_ABSTAIN;
        // remove trailing slash that denotes a collection
        if (! path.equals("/") && path.endsWith("/"))
            path = path.substring(0, path.length()-1);

        CosmoUserDetails details =
            (CosmoUserDetails) authentication.getPrincipal();

        return vote(details.getUser(), path, request.getMethod());
    }

    /**
     * Grants access if the principal is the owner of the item, and denies
     * access otherwise.
     */
    protected int voteIsOwner(User principal,
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

    /**
     * Grants access if the principal represents the given user account, and
     * denies access otherwise.
     */
    protected int voteIsUser(User principal,
                             User user) {
        if (log.isDebugEnabled())
            log.debug("Checking sameness of principal " + user.getUsername() +
                      " and user " + principal.getUsername());

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
     * Returns true if the secure object is a
     * {@link FilterInvocation}.
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService service) {
        contentService = service;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService service) {
        userService = service;
    }
}
