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
package org.osaf.cosmo.acegisecurity.providers.ticket;

import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.vote.AccessDecisionVoter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.http.Methods;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.TicketType;

/**
 * Votes affirmatively if the authenticated principal is a ticket and
 * the ticket has the privilege required by the requested WebDAV
 * method.
 *
 * This is a temporary approach until a full ACL system is in place.
 */
public class TicketVoter implements AccessDecisionVoter {
    private static final Log log = LogFactory.getLog(TicketVoter.class);

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

        // freebusy reports and certain propfinds have their own rules, and
        // since we haven't parsed the request body yet, we have to defer
        // authorization to the servlet layer
        if (method.equals("REPORT") ||
            method.equals("PROPFIND")) {
            return ACCESS_GRANTED;
        }

        // you can't make or delete a ticket with another ticket
        if (method.equals("MKTICKET") ||
            method.equals("DELTICKET"))
            return ACCESS_DENIED;

        if (Methods.isReadMethod(method)) {
            return ticket.getPrivileges().contains(TicketType.PRIVILEGE_READ) ?
                ACCESS_GRANTED :
                ACCESS_DENIED;
        }

        if (Methods.isWriteMethod(method)) {
            return ticket.getPrivileges().contains(TicketType.PRIVILEGE_WRITE) ?
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
     * {@link org.springframework.security.intercept.web.FilterInvocation}
     */
    public boolean supports(Class clazz) {
        return (FilterInvocation.class.isAssignableFrom(clazz));
    }
}
