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
package org.osaf.cosmo.acegisecurity.providers.homedir;

import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.intercept.web.FilterInvocation;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.vote.AccessDecisionVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.util.PathUtil;

/**
 * Makes access control decisions based on whether or not a user is
 * operating on a resource within his home directory.
 * <p>
 * For feed and Web Console requests, the resource's client path is
 * found in the request URI's path info or in a request parameter:
 * <ul>
 * <li> For read operations, the request URI's path info segment must
 * begin with one of the paths specified by {@link #READ_PATHS}
 * immediately followed by the client path of the resource.</li>
 * <li> For write operations, the request URI's path info segment must
 * begin with one of the paths specified by {@link #WRITE_PATHS}, and 
 * the request parameter {@link #PARAM_CLIENT_PATH} must be the client
 * path of the resource.</li>
 * </ul>
 * <p>
 * (For the moment, read and write paths are hardcoded in this class,
 * but they may eventually be defined declaratively with
 * Spring. Similarly, the location of the resource's client path will
 * probably be unified in the request parameter for both types of
 * operation.)
 * <p>
 * If neither of the above conditions is true, the path info is
 * considered to specify the client path directly, which is the case
 * for WebDAV requests.
 * <p>
 * This is a temporary approach until a full ACL system is in place.
 *
 * @see AccessDecisionVoter
 */
public class HomedirVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(HomedirVoter.class);

    // XXX specify these paths declaratively

    /**
     * The list of paths for read operations
     */
    public static final String[] READ_PATHS = {
        "/home/browse",
        "/home/view",
        "/home/download",
        "/home/remove",
        "/home/ticket",
        "/atom/1.0",
    };

    /**
     * The list of paths for write operations
     */
    public static final String[] WRITE_PATHS = {
        "/home/ticket/grant",
    };

    /**
     * The request parameter identifying the client path of the
     * requested resource
     */
    public static final String PARAM_CLIENT_PATH = "path";

    /**
     * Votes affirmatively if the authenticated principal is a user
     * (not a ticket) and if the requested resource is within the
     * user's home directory. Denies access if the principal is a user
     * but the resource is not within the user's home
     * directory. Abstains if the <code>authentication</code> does not
     * represent a user.
     * <p>
     * A resource is considered to be within a user's home directory
     * if the resource's client path begins with "/" and the user's
     * username.
     * <p>
     * Note that a user's admin status is not considered but is left
     * to another (role-based) voter.
     *
     * @param authentication the token representing the authenticated
     * principal
     * @param object the <code>FilterIvocation</code> being invoked
     * @param config the configuration attributes associated with the
     * invoked method
     * @return either {@link #ACCESS_GRANTED}, {@link #ACCESS_DENIED},
     * or {@link #ACCESS_ABSTAIN}
     * @see UsernamePasswordAuthenticationToken
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

        // path is given to us in a form like /bcm/foo/bar - we only
        // grant access if the first path segment exactly matches the
        // given username

        String segment = PathUtil.getInitialSegment(path);
        if (segment == null)
            // path is /
            return ACCESS_ABSTAIN;

        return segment.equals(username) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    // XXX for read ops, look for client path in PARAM_CLIENT_PATH
    // request parameter - this requires url rewriting to happen
    // before security
    private String findResourcePath(FilterInvocation fi) {
        String path = fi.getHttpRequest().getPathInfo();
        if (path == null) {
            return null;
        }
        String resourcePath = null;
        for (int i=0; i<WRITE_PATHS.length; i++) {
            if (path.startsWith(WRITE_PATHS[i])) {
                String paramPath =
                    fi.getHttpRequest().getParameter(PARAM_CLIENT_PATH);
                if (paramPath != null) {
                    resourcePath = paramPath;
                    break;
                }
            }
        }
        if (resourcePath == null) {
            for (int i=0; i<READ_PATHS.length; i++) { 
                if (path.startsWith(READ_PATHS[i])) {
                    resourcePath = path.substring(READ_PATHS[i].length());
                    break;
                }
            }
        }
        if (resourcePath == null) {
            // probably a dav path
            resourcePath = path;
        }

        return resourcePath;
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
}
