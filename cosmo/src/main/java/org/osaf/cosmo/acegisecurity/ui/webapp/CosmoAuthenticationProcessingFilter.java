/* 
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.acegisecurity.ui.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.acegisecurity.Authentication;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.acegisecurity.context.SecurityContextHolder;

import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.model.Preference;
import org.osaf.cosmo.ui.UIConstants;

import org.springframework.util.Assert;

/**
 * Custom authentication filter to allow users to set a preferred
 * login redirect url and to return the redirect url in the message
 * body instead of issueing an actual redirect to allow for dynamic
 * client side processing of authentication results.
 * 
 * Note: this class overrides the values of 
 * <code>AbstractProcessingFilter.defaultTargetUrl</code>
 * and
 * <code>AbstractProcessingFilter.alwaysUseDefaultTargetUrl</code>
 * 
 * @author travis
 *
 */
public class CosmoAuthenticationProcessingFilter extends
        AuthenticationProcessingFilter {
    
    private static final String MEDIA_TYPE_PLAIN_TEXT = "text/plain";
    private Boolean alwaysUseUserPreferredUrl = false;
    private String cosmoDefaultLoginUrl;
    
    /* A place to put authentication so it will be available to
     * sendRedirect 
     */
    private Authentication currentAuthentication;

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.hasLength(getFilterProcessesUrl(), "filterProcessesUrl must be specified");
        Assert.hasLength(getAuthenticationFailureUrl(), "authenticationFailureUrl must be specified");
        Assert.notNull(getAuthenticationManager(), "authenticationManager must be specified");
        Assert.notNull(getRememberMeServices());
        
        // Ensure sendRedirect will always be called with url = true on successful auth.
        this.setAlwaysUseDefaultTargetUrl(true);
    }

    /*
     * This method will be called on success or failure. In the failure case, a url will be
     * passed. In the success case, null will be passed.
     * 
     * On succesful authentication:
     * 1) First, try to find a url the user was attempting to visit
     * 2) If that does not exist, try to find the user's preferred login url
     * 3) If that does not exist, get the default redirect url for this filter
     * 
     * Finally, return the url in the message body.
     */
    @Override
    protected void sendRedirect(HttpServletRequest request, 
                                HttpServletResponse response, 
                                String targetUrl) throws IOException {
        
        // If failure
        if (getAuthenticationFailureUrl().equals(targetUrl)){
            this.sendResponse(request, response, 
                    getRelativeUrl(request, getAuthenticationFailureUrl()));
            return;
        } else {
            targetUrl = alwaysUseUserPreferredUrl ? 
                    null : obtainFullRequestUrl(request);
        } 
        
        if (targetUrl == null) {
            Preference loginUrlPref =
                ((CosmoUserDetails) currentAuthentication.getPrincipal()).
                getUser().getPreference(UIConstants.PREF_KEY_LOGIN_URL);
            if (loginUrlPref != null)
                targetUrl = getRelativeUrl(request, loginUrlPref.getValue());
        }

        if (targetUrl == null) {
            targetUrl = getRelativeUrl(request, cosmoDefaultLoginUrl);
        }
        
        this.sendResponse(request, response, targetUrl);
    }

    private void sendResponse(HttpServletRequest req, 
            HttpServletResponse resp, String redirectUrl) throws IOException{
        resp.setContentType(MEDIA_TYPE_PLAIN_TEXT);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(redirectUrl.length());
        resp.getWriter().write(redirectUrl);
    }

    private String getRelativeUrl(HttpServletRequest request, String path){
        if (path != null){
            return request.getContextPath() + path;
        }
        else return null;
    }

    public Boolean getAlwaysUseUserPreferredUrl() {
        return alwaysUseUserPreferredUrl;
    }

    public void setAlwaysUseUserPreferredUrl(Boolean alwaysUseUserPreferredUrl) {
        this.alwaysUseUserPreferredUrl = alwaysUseUserPreferredUrl;
    }

    public String getCosmoDefaultLoginUrl() {
        return cosmoDefaultLoginUrl;
    }

    public void setCosmoDefaultLoginUrl(String cosmoDefaultTargetUrl) {
        this.cosmoDefaultLoginUrl = cosmoDefaultTargetUrl;
    }

    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        // TODO Auto-generated method stub
        super.onSuccessfulAuthentication(request, response, authResult);
        this.currentAuthentication = authResult;
    }

}
