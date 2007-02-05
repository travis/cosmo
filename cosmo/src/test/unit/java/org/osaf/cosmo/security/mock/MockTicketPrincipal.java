/*
 * Copyright 2006 Open Source Applications Foundation
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

import java.security.Principal;

import org.osaf.cosmo.model.Ticket;

/**
 */
public class MockTicketPrincipal implements Principal {
    private Ticket ticket;

    public MockTicketPrincipal(Ticket ticket) {
        this.ticket = ticket;
    }

    public boolean equals(Object another) {
        if (!(another instanceof MockTicketPrincipal)) {
            return false;
        }
        return ticket.equals(((MockTicketPrincipal)another).getTicket());
    }

    public String toString() {
        return ticket.toString();
    }

    public int hashCode() {
        return ticket.hashCode();
    }

    public String getName() {
        return ticket.getKey();
    }

    public Ticket getTicket() {
        return ticket;
    }
}
