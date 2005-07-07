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

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.intercept.web.FilterInvocation;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

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
        String path = fi.getHttpRequest().getPathInfo();
        if (path == null) {
            path = "/";
        }
        if (! path.equals("/") && path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }

        return path.startsWith("/" + username) ? ACCESS_GRANTED : ACCESS_DENIED;
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
     * {@link net.sf.acegisecurity.intercept.web.FilterInvocation}
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }
}
