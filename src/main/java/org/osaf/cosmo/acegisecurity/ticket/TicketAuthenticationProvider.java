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

import java.util.Calendar;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationException;
import net.sf.acegisecurity.AuthenticationServiceException;
import net.sf.acegisecurity.BadCredentialsException;
import net.sf.acegisecurity.providers.AuthenticationProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.dao.TicketDao;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.model.Ticket;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DataAccessException;

/**
 */
public class TicketAuthenticationProvider
    implements AuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(TicketAuthenticationProvider.class);

    private TicketDao ticketDao;

    // AuthenticationProvider methods

    /**
     */
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        if (! supports(authentication.getClass())) {
            return null;
        }

        TicketAuthenticationToken token =
            (TicketAuthenticationToken) authentication;
        try {
            if (log.isDebugEnabled()) {
                log.debug("authenticating ticket " + token.getId() +
                          " for resource at path " + token.getPath());
            }
            Ticket ticket = ticketDao.getTicket(token.getPath(), token.getId());
            if (ticket == null) {
                throw new BadCredentialsException("Ticket " + token.getId() +
                                                  " not found for resource " +
                                                  "at " + token.getPath());
            }

            try {
                checkTimeout(ticket);
            } catch (TicketTimeoutException e) {
                if (log.isDebugEnabled()) {
                    log.debug("removing timed out ticket " + token.getId());
                }
                ticketDao.removeTicket(token.getPath(), ticket);
                throw e;
            }

            token.setTicket(ticket);
            token.setAuthenticated(true);
            return token;
        } catch (DataRetrievalFailureException e) {
            throw new TicketedItemNotFoundException("Resource at " +
                                                    token.getPath() +
                                                    " not found");
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    /**
     */
    public boolean supports(Class authentication) {
        return
            (TicketAuthenticationToken.class.isAssignableFrom(authentication));
    }

    // our methods

    public void checkTimeout(Ticket ticket)
        throws TicketTimeoutException, AuthenticationServiceException {
        if (ticket.getTimeout().equals(CosmoDavConstants.VALUE_INFINITY)) {
            return;
        }

        Integer timeout = null;
        try {
            timeout = Integer.valueOf(ticket.getTimeout().substring(7));
        } catch (NumberFormatException e) {
            throw new AuthenticationServiceException("illegal ticket " +
                                                     "timeout: " +
                                                     ticket.getTimeout());
        }

        Calendar expiry = Calendar.getInstance();
        expiry.setTime(ticket.getCreated());
        expiry.add(Calendar.SECOND, timeout.intValue());

        if (Calendar.getInstance().after(expiry)) {
            throw new TicketTimeoutException("ticket timed out at " +
                                             expiry.getTime());
        }
    }

    /**
     */
    public TicketDao getTicketDao() {
        return ticketDao;
    }

    /**
     */
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }
}
