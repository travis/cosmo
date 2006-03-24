/*
 * Copyright 2005 Open Source Applications Foundation
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

import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AccessDecisionVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.security.CosmoUserDetails;
/**
 * Votes affirmatively if the authenticated principal is a user (not a
 * ticket) and if the requested resource is within the user's home
 * directory.
 *
 * This is a temporary approach until a full ACL system is in place.
 */
public class HomedirVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(HomedirVoter.class);

    private static final String[] HOMEDIR_PATHS = {
        "/home/browse",
        "/home/view",
        "/home/download",
        "/home/ticket",
        "/atom/1.0",
    };

    private static final String[] POST_PATHS = {
        "/home/ticket/grant",
    };

    /**
     */
    public int vote(Authentication authentication,
                    Object object,
                    ConfigAttributeDefinition config) {
        if (! (authentication instanceof UsernamePasswordAuthenticationToken)) {
            return ACCESS_ABSTAIN;
        }

        CosmoUserDetails details =
            (CosmoUserDetails) authentication.getPrincipal();
        String username = details.getUser().getUsername();

        FilterInvocation fi = (FilterInvocation) object;
        String path = findResourcePath(fi);
        if (path == null) {
            return ACCESS_DENIED;
        }

        return path.startsWith(username) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    private String findResourcePath(FilterInvocation fi) {
        String path = fi.getHttpRequest().getPathInfo();
        if (path == null) {
            return null;
        }
        String resourcePath = null;
        for (int i=0; i<POST_PATHS.length; i++) {
            if (path.startsWith(POST_PATHS[i])) {
                String paramPath = fi.getHttpRequest().getParameter("path");
                if (paramPath != null) {
                    resourcePath = paramPath;
                    break;
                }
            }
        }
        if (resourcePath == null) {
            for (int i=0; i<HOMEDIR_PATHS.length; i++) { 
                if (path.startsWith(HOMEDIR_PATHS[i])) {
                    resourcePath = path.substring(HOMEDIR_PATHS[i].length());
                    break;
                }
            }
        }
        if (resourcePath == null) {
            // probably a dav path
            resourcePath = path;
        }
        return resourcePath.substring(1);
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
     * {@link org.acegisecurity.intercept.web.FilterInvocation}
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }
}
