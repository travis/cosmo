/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
import org.osaf.cosmo.server.CollectionPath;
import org.osaf.cosmo.service.ContentService;

/**
 * Allows access to an item if the authenticated user owns the item.
 *
 * @see AccessDecisionVoter
 * @see Item
 */
public class OwnerVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(OwnerVoter.class);

    private ContentService contentService;

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

        CosmoUserDetails details =
            (CosmoUserDetails) authentication.getPrincipal();

        Item item = null;
        CollectionPath cp = CollectionPath.parse(path);
        if (cp != null) {
            item = contentService.findItemByUid(cp.getUid());
            if (item == null) {
                log.warn("Item with uid " + cp.getUid() +
                         " not found; abstaining");
                return ACCESS_ABSTAIN;
            }
            if (log.isDebugEnabled())
                log.debug("Checking ownership of item " + cp.getUid() +
                          " by user " + details.getUser().getUsername());
        } else {
            item = contentService.findItemByPath(path);
            if (item == null) {
                log.warn("Item at path " + path + " not found; abstaining");
                return ACCESS_ABSTAIN;
            }
            if (log.isDebugEnabled())
                log.debug("Checking ownership of item at path " + path +
                          " by user " + details.getUser().getUsername());
        }


        if (! item.getOwner().equals(details.getUser())) {
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
}
