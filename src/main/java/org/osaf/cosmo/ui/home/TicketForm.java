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
package org.osaf.cosmo.ui.home;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.validator.ValidatorForm;
import org.apache.struts.action.ActionMapping;

import org.osaf.cosmo.model.Ticket;

/**
 * Extends {@link org.apache.struts.validator.ValidatorForm} to
 * provide attributes for tickets.
 */
public class TicketForm extends ValidatorForm {
    private static final Log log = LogFactory.getLog(TicketForm.class);

    private String path;
    private Integer timeout;
    private String privileges;

    /**
     */
    public TicketForm() {
        initialize();
    }

    /**
     */
    public String getPath() {
        return path;
    }

    /**
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     */
    public String getPrivileges() {
        return privileges;
    }

    /**
     */
    public void setPrivileges(String privileges) {
        this.privileges = privileges;
    }

    /**
     */
    public Ticket getTicket() {
        Ticket ticket = new Ticket();
        if (timeout != null) {
            ticket.setTimeout(timeout);
        }
        else {
            ticket.setTimeout(Ticket.TIMEOUT_INFINITE);
        }
        if (privileges.equals("fb")) {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_FREEBUSY);
        }
        else {
            ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
            if (privileges.equals("rw")) {
                ticket.getPrivileges().add(Ticket.PRIVILEGE_WRITE);
            }
        }
        return ticket;
    }

    /**
     */
    public void reset(ActionMapping mapping,
                      HttpServletRequest request) {
        super.reset(mapping, request);
        initialize();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("path", path).
            append("timeout", timeout).
            append("privileges", privileges).
            toString();
    }

    /**
     */
    private void initialize() {
        path = null;
        timeout = null;
    }
}
