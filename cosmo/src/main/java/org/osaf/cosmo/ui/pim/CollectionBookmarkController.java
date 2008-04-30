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
package org.osaf.cosmo.ui.pim;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityContext;
import org.osaf.cosmo.security.CosmoSecurityException;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * Action for browsing a user's home directory.
 */
public class CollectionBookmarkController extends AbstractController {
    private static final Log log = LogFactory
            .getLog(CollectionBookmarkController.class);
    
    private String pimView;
    private ContentService contentService;
    private CosmoSecurityManager securityManager;
    private ServiceLocatorFactory serviceLocatorFactory;
       
    /** 
     */
    public ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
       
        String path = request.getPathInfo();
        String collectionUid = path.substring(path.lastIndexOf('/') + 1);
        
        // Get the collection and make sure it's valid.
        CollectionItem collection = null;
        try{
            collection = 
                (CollectionItem) contentService.findItemByUid(collectionUid);
        } catch (ClassCastException e){
            // This isn't quite the right thing to do, but is a good idea for now.
            return new ModelAndView("error_notfound");     
        } catch(CosmoSecurityException e) {
            new ModelAndView("error_forbidden");
        }

        if (collection == null){
            return new ModelAndView("error_notfound");
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("collection", collection);
        
        CosmoSecurityContext csc = securityManager.getSecurityContext();
        Map<String, String> relationLinks;
        Ticket ticket = findTicket(csc);
        if (ticket != null)
            relationLinks = serviceLocatorFactory.createServiceLocator(request, ticket, false)
                .getCollectionUrls(collection);
        else relationLinks = serviceLocatorFactory.createServiceLocator(request, false)
                .getCollectionUrls(collection);
        model.put("relationLinks", relationLinks);
        

        if (ticket != null) {
            model.put("ticketKey", ticket.getKey());
            return new ModelAndView(pimView, model);
        } else {
            // If we can't find a ticket principal, use the current user.
            User authUser = csc.getUser();
            if (authUser != null) {
                return new ModelAndView(pimView, model);
            }
        }

        // when all else fails...
        return new ModelAndView("error_forbidden");
    }
    
    // First try to find a ticket principal
    private Ticket findTicket(CosmoSecurityContext csc){
        Set<Ticket> tickets = csc.getTickets();
        if (!tickets.isEmpty()) return (Ticket)tickets.toArray()[0];
        else return null;
    }

    public void setPimView(String pimView) {
        this.pimView = pimView;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void setServiceLocatorFactory(ServiceLocatorFactory serviceLocatorFactory) {
        this.serviceLocatorFactory = serviceLocatorFactory;
    }
}
