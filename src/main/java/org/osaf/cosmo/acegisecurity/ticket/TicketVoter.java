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
package org.osaf.cosmo.acegisecurity.ticket;

import java.util.HashSet;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.intercept.web.FilterInvocation;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.DavMethods;

import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.CosmoDavMethods;
import org.osaf.cosmo.model.Ticket;

/**
 * Votes affirmatively if the authenticated principal is a ticket and
 * the ticket has the privilege required by the requested WebDAV
 * method.
 *
 * This is a temporary approach until a full ACL system is in place.
 */
public class TicketVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(TicketVoter.class);

    // XXX when implementing WebDAV ACL, move these to DavMethods class

    private static final HashSet readMethods = new HashSet();
    private static final HashSet writeMethods = new HashSet();

    static {
        readMethods.add(DavMethods.METHOD_OPTIONS);
        readMethods.add(DavMethods.METHOD_GET);
        readMethods.add(DavMethods.METHOD_HEAD);
        readMethods.add(DavMethods.METHOD_PROPFIND);

        writeMethods.add(DavMethods.METHOD_POST);
        writeMethods.add(DavMethods.METHOD_PUT);
        writeMethods.add(DavMethods.METHOD_DELETE);
        writeMethods.add(DavMethods.METHOD_PROPPATCH);
        writeMethods.add(DavMethods.METHOD_MKCOL);
        writeMethods.add(DavMethods.METHOD_COPY);
        writeMethods.add(DavMethods.METHOD_MOVE);
        writeMethods.add(DavMethods.METHOD_LOCK);
        writeMethods.add(DavMethods.METHOD_UNLOCK);

        writeMethods.add(CosmoDavMethods.METHOD_MKTICKET);
        writeMethods.add(CosmoDavMethods.METHOD_DELTICKET);
        writeMethods.add(CosmoDavMethods.METHOD_MKCALENDAR);
    }

    /**
     */
    public int vote(Authentication authentication,
                    Object object,
                    ConfigAttributeDefinition config) {
        if (! (authentication instanceof TicketAuthenticationToken)) {
            return ACCESS_ABSTAIN;
        }

        Ticket ticket = (Ticket) authentication.getPrincipal();

        FilterInvocation fi = (FilterInvocation) object;
        String method = fi.getHttpRequest().getMethod();

        if (readMethods.contains(method)) {
            return ticket.getPrivileges().
                contains(CosmoDavConstants.PRIVILEGE_READ) ?
                ACCESS_GRANTED :
                ACCESS_DENIED;
        }

        if (writeMethods.contains(method)) {
            return ticket.getPrivileges().
                contains(CosmoDavConstants.PRIVILEGE_WRITE) ?
                ACCESS_GRANTED :
                ACCESS_DENIED;
        }

        return ACCESS_ABSTAIN;
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
