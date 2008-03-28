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
package org.osaf.cosmo.security.mock;

import org.osaf.cosmo.security.BaseSecurityContext;

import java.security.Principal;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;

/**
 * A mock implementation of {@link CosmoSecurityContext} that provides
 * dummy instances for use with unit mocks.
 */
public class MockSecurityContext extends BaseSecurityContext {
    private static final Log log =
        LogFactory.getLog(MockSecurityContext.class);

    /**
     */
    public MockSecurityContext(Principal principal) {
        super(principal, null);
    }
    
    /**
     */
    public MockSecurityContext(Principal principal, Set<Ticket> tickets) {
        super(principal, tickets);
    }

    protected void processPrincipal() {
        if (getPrincipal() instanceof MockAnonymousPrincipal) {
            setAnonymous(true);
        }
        else if (getPrincipal() instanceof MockUserPrincipal) {
            User user = ((MockUserPrincipal) getPrincipal()).getUser();
            setUser(user);
            setAdmin(user.getAdmin().booleanValue());
        }
        else if (getPrincipal() instanceof MockTicketPrincipal) {
            setTicket(((MockTicketPrincipal) getPrincipal()).getTicket());
        }
        else {
            throw new RuntimeException("unknown principal type " + getPrincipal().getClass().getName());
        }
    }
}
