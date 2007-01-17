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
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;

/**
 * Custom authentication filter to allow users to set a preferred
 * login redirect url and to return the redirect url in the message
 * body instead of issueing an actual redirect to allow for dynamic
 * client side processing of authentication results.
 * 
 * @author travis
 *
 */
public class CosmoAuthenticationProcessingFilter extends
        AuthenticationProcessingFilter {
    
    private static final String MEDIA_TYPE_PLAIN_TEXT = null;
    private Boolean alwaysUseUserPreferredUrl = false;
    private String loginUrlKey;

    /**
     * On succesful authentication:
     * 1) First, try to find a url the user was attempting to visit
     * 2) If that does not exist, try to find the user's preferred login url
     * 3) If that does not exist, get the default redirect url for this filter
     * 
     * Finally, return the url in the message body.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, 
            HttpServletResponse response, 
            Authentication authResult) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult.toString());
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);

        if (logger.isDebugEnabled()) {
            logger.debug("Updated SecurityContextHolder to contain the " +
                        "following Authentication: '" + authResult + "'");
        }
        
        try {
            CosmoUserDetails userDetails = (CosmoUserDetails) authResult.getPrincipal();

            // Don't attempt to obtain the url from the saved request if alwaysUserPreferredUrl is set
            String targetUrl = alwaysUseUserPreferredUrl ? null : obtainFullRequestUrl(request);
            
            if (targetUrl == null){
                targetUrl = getRelativeUrl(request, userDetails.getUser().getPreferences().get(
                        this.loginUrlKey));
            }

            if (targetUrl == null) {
                targetUrl = getRelativeUrl(request, getDefaultTargetUrl());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Returning desired redirect url:" + targetUrl);
            }

            onSuccessfulAuthentication(request, response, authResult);

            getRememberMeServices().loginSuccess(request, response, authResult);

            // Fire event
            if (this.eventPublisher != null) {
                eventPublisher.publishEvent(
                        new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
            }

            sendResponse(request, response, targetUrl);

        } catch (ClassCastException e) {
            sendResponse(request, response, getRelativeUrl(request, getDefaultTargetUrl()));
        }
    }
    
    /**
     * On unsuccessful auth, send the url {serverContext}/loginfailed
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException failed) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Updated SecurityContextHolder to contain null Authentication");
        }

        String failureUrl = getExceptionMappings().getProperty(failed.getClass().getName(), getAuthenticationFailureUrl());

        if (logger.isDebugEnabled()) {
            logger.debug("Authentication request failed: " + failed.toString());
        }

        try {
            request.getSession().setAttribute(ACEGI_SECURITY_LAST_EXCEPTION_KEY, failed);
        } catch (Exception ignored) {}

        onUnsuccessfulAuthentication(request, response, failed);

        getRememberMeServices().loginFail(request, response);

        this.sendResponse(request, response, getRelativeUrl(request, failureUrl));

    }

    private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String redirectUrl) throws IOException{
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



    public String getLoginUrlKey() {
        return loginUrlKey;
    }



    public void setLoginUrlKey(String preferredUrlPreferenceKey) {
        this.loginUrlKey = preferredUrlPreferenceKey;
    }


}
