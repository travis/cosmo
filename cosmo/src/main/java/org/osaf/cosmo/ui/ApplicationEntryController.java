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
package org.osaf.cosmo.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class ApplicationEntryController extends MultiActionController {

    private String loginView;
    private String defaultLoggedInRedirect; 
    private CosmoSecurityManager securityManager;
    private UserService userService;
    private String welcomePageUrl;

    public ModelAndView login(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (securityManager.getSecurityContext().getUser() == null){
            return new ModelAndView(loginView);
        } else {
            return new ModelAndView("error_loggedin");
        }
    }
    
    public ModelAndView logout(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.getSession().invalidate();

        return new ModelAndView(loginView);
    }
    
    public ModelAndView welcome(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        User user = securityManager.getSecurityContext().getUser();
        
        if (user == null){
            return new ModelAndView(loginView);
        } else {
            user =  userService.getUser(user.getUsername());

            Preference loginUrlPref =
                user.getPreference(UIConstants.PREF_KEY_LOGIN_URL);
            String redirectUrl = loginUrlPref != null ?
                loginUrlPref.getValue() : defaultLoggedInRedirect;

            return new ModelAndView("redirect:" + redirectUrl);
        }
    }
    
    public ModelAndView root(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView("redirect:" + welcomePageUrl);
    }

    public void setSecurityManager(CosmoSecurityManager securityManager) {
        this.securityManager = securityManager;
    }

    public void setLoginView(String loginView) {
        this.loginView = loginView;
    }

    public String getDefaultLoggedInRedirect() {
        return defaultLoggedInRedirect;
    }

    public void setDefaultLoggedInRedirect(String defaultLoginUrl) {
        this.defaultLoggedInRedirect = defaultLoginUrl;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String getWelcomePageUrl() {
        return welcomePageUrl;
    }

    public void setWelcomePageUrl(String welcomePageUrl) {
        this.welcomePageUrl = welcomePageUrl;
    }


}
