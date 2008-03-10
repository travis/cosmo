/*
 * Copyright 2008 Open Source Applications Foundation
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.dao.ContentDao;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.security.CosmoSecurityManager;

/**
 * Servlet filter that examines request for additional
 * ticket keys to include in the security context.
 */
public class ExtraTicketProcessingFilter implements Filter {
    private static final Log log =
        LogFactory.getLog(ExtraTicketProcessingFilter.class);

    public static final String TICKET_HEADER= "X-Cosmo-Ticket";
    public static final String MORSE_CODE_TICKET_HEADER= "X-MorseCode-Ticket";
    public static final String PARAM_TICKET = "ticket";
    
    private ContentDao contentDao = null;
    private CosmoSecurityManager securityManager = null;

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void init(FilterConfig filterConfig)
        throws ServletException {
    }

    /**
     * Examines HTTP servlet requests for extra ticket keys,
     * and register them with the security manager.
     */
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if(log.isDebugEnabled())
            log.debug("looking for tickets in request headers");
        
        Set<Ticket> tickets = new HashSet<Ticket>();
        
        // Look for tickets in header in the format:
        // X-Cosmo-Ticket: slkdfjsdf, slkdjfsdf, sdlfkjsfsdf
        Enumeration<String> ticketKeys = httpRequest.getHeaders(TICKET_HEADER);
        while(ticketKeys.hasMoreElements()) {
            String ticketKeyValue = ticketKeys.nextElement();
            for(String ticketKey: ticketKeyValue.split(",")) {
                Ticket ticket = contentDao.findTicket(ticketKey.trim());
                if(ticket!=null)
                    tickets.add(ticket);
            }
        }
        
        // Look for tickets in header in the format:
        // X-MorseCode-Ticket: slkdfjsdf, slkdjfsdf, sdlfkjsfsdf
        ticketKeys = httpRequest.getHeaders(MORSE_CODE_TICKET_HEADER);
        while(ticketKeys.hasMoreElements()) {
            String ticketKeyValue = ticketKeys.nextElement();
            for(String ticketKey: ticketKeyValue.split(",")) {
                Ticket ticket = contentDao.findTicket(ticketKey.trim());
                if(ticket!=null)
                    tickets.add(ticket);
            }
        }
        
        // look for tickets in request parameters
        String[] paramTicketKeys = httpRequest.getParameterValues(PARAM_TICKET);
        if(paramTicketKeys!=null) {
            for(String ticketKey: paramTicketKeys) {
                Ticket ticket = contentDao.findTicket(ticketKey);
                if(ticket!=null)
                    tickets.add(ticket);
            }
        }
        
        try {
            // register tickets
            securityManager.registerTickets(tickets);
            chain.doFilter(request, response);
        } finally{
            // clear tickets
            securityManager.unregisterTickets();
        }
    }

    /**
     * Does nothing - we use IoC lifecycle methods instead
     */
    public void destroy() {
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
}
