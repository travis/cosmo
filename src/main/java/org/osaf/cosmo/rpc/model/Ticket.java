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
package org.osaf.cosmo.rpc.model;

import java.util.Set;

/**
 * This class encapsulates the necessary information 
 * @author bobbyrullo
 */
public class Ticket {
    private String ticketId;
    private Set priveleges;
    
    public Set getPrivileges() {
        return priveleges;
    }
    
    public void setPrivileges(Set priveleges) {
        this.priveleges = priveleges;
    }
    
    public String getTicketKey() {
        return ticketId;
    }
    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
 }