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

import org.acegisecurity.ConfigAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.acegisecurity.vote.UserResourceVoter;
import org.osaf.cosmo.dav.ExtendedDavConstants;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.UriTemplate;

/**
 * <p>
 * An <code>AccessDecisionVoter</code> that implements specific access
 * control policies for the WebDAV protocol handler.
 * </p>
 */
public class UserVoter extends UserResourceVoter
    implements ExtendedDavConstants {
    private static final Log log = LogFactory.getLog(UserVoter.class);

    public int vote(User authenticated,
                    String path,
                    String method) {
        if (log.isDebugEnabled())
            log.debug("voting on user " + authenticated.getUsername() + " for resource " + path + " with method " + method);

        // for now, we ignore method, since a regular (non-admin) user has
        // either all privileges or none

        UriTemplate.Match match = null;

        match = TEMPLATE_COLLECTION.match(path);
        if (match != null)
            return voteOnCollection(match, authenticated);

        match = TEMPLATE_ITEM.match(path);
        if (match != null)
            return voteOnItemResource(match, authenticated);

        match = TEMPLATE_USERS.match(path);
        if (match != null)
            return voteOnUserPrincipalCollection(match, authenticated);

        match = TEMPLATE_USER.match(path);
        if (match != null)
            return voteOnUserPrincipalResource(match, authenticated);

        match = TEMPLATE_HOME.match(path);
        if (match != null)
            return voteOnHomeResource(match, authenticated);

        if (log.isDebugEnabled())
            log.debug(path + " is not a dav path; allowing");
        return ACCESS_GRANTED;
    }

    private int voteOnCollection(UriTemplate.Match match,
                                 User authenticated) {
        // the authenticated user and the user who owns the collection
        // represented by the URI need to match

        String uid = match.get("uid");
        Item item = getContentService().findItemByUid(uid);
        if (item == null) {
            // allow so that the dav provider can return 404
            if (log.isDebugEnabled())
                log.debug(match.getPath() + " does not identify a known item; allowing");
            return ACCESS_GRANTED;
        }

        return voteIsOwner(authenticated, item);
    }

    private int voteOnItemResource(UriTemplate.Match match,
                                   User authenticated) {
        // the authenticated user and the user who owns the item
        // represented by the URI need to match
        
        String uid = match.get("uid");
        Item item = getContentService().findItemByUid(uid);
        if (item == null) {
            // allow so that the dav provider can return 404
            if (log.isDebugEnabled())
                log.debug(match.getPath() + " does not identify a known item; allowing");
            return ACCESS_GRANTED;
        }

        return voteIsOwner(authenticated, item);
    }

    private int voteOnUserPrincipalCollection(UriTemplate.Match match,
                                              User authenticated) {
        // allow anybody to access this principal collection
        return ACCESS_GRANTED;
    }

    private int voteOnUserPrincipalResource(UriTemplate.Match match,
                                            User authenticated) {
        // the authenticated user and the user represented by the
        // principal URL need to match

        User principal = getUserService().getUser(match.get("username"));
        if (principal == null) {
            // allow so that the dav provider can return 404
            if (log.isDebugEnabled())
                log.debug(match.getPath() + " does not identify a known user principal; allowing");
            return ACCESS_GRANTED;
        }

        return voteIsUser(authenticated, principal);
    }

    private int voteOnHomeResource(UriTemplate.Match match,
                                   User authenticated) {
        // the authenticated user and the user who owns the home collection
        // represented by the URI need to match

        User owner = getUserService().getUser(match.get("username"));
        if (owner == null) {
            // allow so that the dav provider can return 404
            if (log.isDebugEnabled())
                log.debug(match.getPath() + " does not identify a known home collection; allowing");
            return ACCESS_GRANTED;
        }

        return voteIsUser(authenticated, owner);
    }

    /**
     * Always returns true, since this voter does not examine any
     * config attributes.
     */
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }
}
