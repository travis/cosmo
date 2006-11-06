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
package org.osaf.cosmo.spring.mvc.controllers;

import java.beans.PropertyEditorSupport;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.ContentService;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.CancellableFormController;


/**
 * Provides backing for ticket creation form.
 * 
 */
public class TicketFormController extends CancellableFormController {
    private static final Log log = LogFactory.getLog(TicketFormController.class);
    private CosmoSecurityManager securityManager;
    private ContentService contentService;

    protected Object formBackingObject(HttpServletRequest request)
    throws Exception {
        return new Ticket();
    }
    
    protected ModelAndView processFormSubmission(HttpServletRequest request, 
            HttpServletResponse response, 
            Object command, 
            BindException errors) throws Exception{
        
        if (log.isDebugEnabled()) {
            log.debug("granting ticket to item at " 
                    + request.getParameter("path"));
        }

        Ticket ticket = (Ticket) command;
        ticket.setOwner(getSecurityManager().getSecurityContext().getUser());
        
        contentService.createTicket(request.getParameter("path"), ticket);
        
        ModelAndView mav = super.processFormSubmission(request, response, command, errors);
        
        if (mav.getViewName().equals(this.getSuccessView())){
            mav.setViewName(mav.getViewName() + "?path=" + request.getParameter("path"));
        }
        return mav;
    }
    
    @Override
    protected void initBinder(HttpServletRequest req, ServletRequestDataBinder binder) throws Exception {
        // TODO Auto-generated method stub
        super.initBinder(req, binder);
        
        binder.registerCustomEditor(Set.class, "privileges", 
                new TicketPrivilegeEditor());
    }


    private class TicketPrivilegeEditor extends PropertyEditorSupport {

        
        public void setAsText(String text) {
            HashSet privs = new HashSet();
            if (text.equals("fb")) {
                privs.add(Ticket.PRIVILEGE_FREEBUSY);
                setValue(privs);
            }
            else {
                privs.add(Ticket.PRIVILEGE_READ);
                if (text.equals("rw")) {
                    privs.add(Ticket.PRIVILEGE_WRITE);
                    
                }
                setValue(privs);
            }
        }
    }
    /**
     */
    public CosmoSecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     */
    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }
    /**
     */
    public ContentService getContentService() {
        return contentService;
    }

    /**
     */
    public void setContentService(ContentService service) {
        this.contentService = service;
    }


}
