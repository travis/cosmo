/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.ui.account.activation;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * Controller for account activation.
 */
public class AccountActivationController extends AbstractController {
    private static final Log log = LogFactory
            .getLog(AccountActivationController.class);
    
    private String accountActivationView;
    private String notFoundView;
    private UserService userService;
    
    /** 
     */
    public ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String activationId = request.getParameter("id");
        
        User user = userService.getUserByActivationId(activationId);
        
        if (user != null){
            user.activate();
            userService.updateUser(user);
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("user", user);
            return new ModelAndView(accountActivationView, model);    
        } else {
            return new ModelAndView(notFoundView);
        }
        
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setAccountActivationView(String accountActivationView) {
        this.accountActivationView = accountActivationView;
    }

    public void setNotFoundView(String notFoundView) {
        this.notFoundView = notFoundView;
    }


}
