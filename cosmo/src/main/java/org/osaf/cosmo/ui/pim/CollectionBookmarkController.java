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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.server.ServiceLocatorFactory;
import org.osaf.cosmo.service.ContentService;
import org.osaf.cosmo.service.UserService;

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
    private UserService userService;
        
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
        }

        if (collection == null){
            return new ModelAndView("error_notfound");
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("collection", collection);
        
        
        // First try to find a ticket principal
        String ticketKey = request.getParameter("ticket");

        if (ticketKey != null) {
            Ticket ticket = contentService.getTicket(collection, ticketKey);
            if (ticket != null) {
                Map<String, String> relationLinks = serviceLocatorFactory.
                    createServiceLocator(request, ticket, false).
                    getCollectionUrls(collection);
                model.put("relationLinks", relationLinks);
                model.put("ticketKey", ticketKey);
                return new ModelAndView(pimView, model);
            } else {
                return new ModelAndView("error_forbidden");
            }
        } else {
            // If we can't find a ticket principal, use the current user.
            User authUser = securityManager.getSecurityContext().getUser();
            if (authUser != null) {
                User currentUser =
                    userService.getUser(authUser.getUsername());
                if (collection.getOwner().equals(currentUser) ||
                    currentUser.isSubscribedTo(collection))
                return new ModelAndView(pimView, model);
            }
        }

        // when all else fails...
        return new ModelAndView("error_forbidden");
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

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
