/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.account;

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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * Controller for the user account view page.
 */
public class AccountViewController extends AbstractController {
    private static final Log log = LogFactory
            .getLog(AccountViewController.class);
    
    private String accountView;
    private ContentService contentService;
    private CosmoSecurityManager securityManager;
    private ServiceLocatorFactory serviceLocatorFactory;
        
    /** 
     */
    public ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
       
        User user = securityManager.getSecurityContext().getUser();
        
        CollectionItem collection =
                (CollectionItem) contentService.getRootItem(user);

        Map<String, Object> model = new HashMap<String, Object>();
        
        Map<String, String> relationLinks = serviceLocatorFactory.
            createServiceLocator(request).getCollectionUrls(collection);
        
        model.put("relationLinks", relationLinks);
        
        return new ModelAndView(accountView, model);
        
    }

    public void setAccountView(String accountView) {
        this.accountView = accountView;
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
