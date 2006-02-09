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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

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

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

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
        String repositoryPath = tokenPathToRepositoryPath(token.getPath());
        for (Iterator i=token.getIds().iterator(); i.hasNext();) {
            String id = (String) i.next();
            Ticket ticket = findTicket(repositoryPath, id);
            if (ticket != null) {
                token.setTicket(ticket);
                token.setAuthenticated(true);
                return token;
            }
        }
        throw new BadCredentialsException("No valid tickets found for" +
                                          " resource at " + repositoryPath);
    }

    /**
     */
    public boolean supports(Class authentication) {
        return
            (TicketAuthenticationToken.class.isAssignableFrom(authentication));
    }

    // our methods

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

    /**
     */
    protected Ticket findTicket(String repositoryPath,
                                String id) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("authenticating ticket " + id +
                          " for resource at path " + repositoryPath);
            }
            Ticket ticket = ticketDao.getTicket(repositoryPath, id);
            if (ticket == null) {
                return null;
            }

            if (ticket.hasTimedOut()) {
                if (log.isDebugEnabled()) {
                    log.debug("removing timed out ticket " + ticket.getId());
                }
                ticketDao.removeTicket(repositoryPath, ticket);
                return null;
            }

            return ticket;
        } catch (DataRetrievalFailureException e) {
            throw new TicketedItemNotFoundException("Resource at " +
                                                    repositoryPath + 
                                                    " not found");
        } catch (DataAccessException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    /**
     */
    private String tokenPathToRepositoryPath(String tokenPath) {
        try {
            return URLDecoder.decode(tokenPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported?!");
        }
    }
}
