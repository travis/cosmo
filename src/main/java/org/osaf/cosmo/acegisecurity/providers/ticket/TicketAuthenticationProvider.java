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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.service.ContentService;
import org.springframework.dao.DataAccessException;

/**
 */
public class TicketAuthenticationProvider
    implements AuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(TicketAuthenticationProvider.class);

    private ContentService contentService;

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
        String clientPath = tokenPathToRepositoryPath(token.getPath());
        for (Iterator i=token.getIds().iterator(); i.hasNext();) {
            String id = (String) i.next();
            Ticket ticket = findTicket(clientPath, id);
            if (ticket != null) {
                token.setTicket(ticket);
                token.setAuthenticated(true);
                return token;
            }
        }
        throw new BadCredentialsException("No valid tickets found for" +
                                          " resource at " + clientPath);
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
    public ContentService getContentService() {
        return contentService;
    }

    /**
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     */
    protected Ticket findTicket(String path,
                                String id) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("authenticating ticket " + id +
                          " for resource at path " + path);
            }

            // If the item exists, get it
            Item item = contentService.findItemByPath(path);
            
            // If the item doesn't exist (PUT request), then
            // get the parent
            if (item == null) {
                item = contentService.findItemParentByPath(path);
                // Parent must exist
                if(item==null)
                    throw new TicketedItemNotFoundException("Resource at " +
                            path + " not found");
            }

            // First look for ticket on item
            Ticket ticket = contentService.getTicket(item, id);
            
            // Look for ticket on each parent
            while (ticket == null && (item.getParent()!=null)) {
                item = item.getParent();
                ticket = contentService.getTicket(item, id);
            }
            
            // If we didn't find a ticket, then its not there
            if(ticket==null)
                return null;

            if (ticket.hasTimedOut()) {
                if (log.isDebugEnabled()) {
                    log.debug("removing timed out ticket " + ticket.getKey());
                }
                contentService.removeTicket(item, ticket);
                return null;
            }

            return ticket;
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
