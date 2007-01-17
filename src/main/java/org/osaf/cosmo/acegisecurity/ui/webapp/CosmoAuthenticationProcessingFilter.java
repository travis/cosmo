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

public class CosmoAuthenticationProcessingFilter extends
        AuthenticationProcessingFilter {
    
    private static final String MEDIA_TYPE_PLAIN_TEXT = null;
    private Boolean alwaysUseUserPreferredUrl = false;
    private String preferredUrlPreferenceKey;

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
                targetUrl = userDetails.getUser().getPreferences().get(
                        this.preferredUrlPreferenceKey);
            }

            if (targetUrl == null) {
                targetUrl = getDefaultTargetUrl();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Returning desired redirect url:" + targetUrl);
            }

            onSuccessfulAuthentication(request, response, authResult);

            getRememberMeServices().loginSuccess(request, response, authResult);

            // Fire event
            if (this.eventPublisher != null) {
                eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
            }

            sendResponse(request, response, targetUrl);

        } catch (ClassCastException e) {
            sendResponse(request, response, getDefaultTargetUrl());
        }
    }
    
    

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

        this.sendResponse(request, response, failureUrl);

    }

    private void sendResponse(HttpServletRequest req, HttpServletResponse resp, String targetUrl) throws IOException{
        String redirectUrl = req.getContextPath() + targetUrl;
        resp.setContentType(MEDIA_TYPE_PLAIN_TEXT);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(redirectUrl.length());
        resp.getWriter().write(redirectUrl);
    }



    public Boolean getAlwaysUseUserPreferredUrl() {
        return alwaysUseUserPreferredUrl;
    }



    public void setAlwaysUseUserPreferredUrl(Boolean alwaysUseUserPreferredUrl) {
        this.alwaysUseUserPreferredUrl = alwaysUseUserPreferredUrl;
    }



    public String getPreferredUrlPreferenceKey() {
        return preferredUrlPreferenceKey;
    }



    public void setPreferredUrlPreferenceKey(String preferredUrlPreferenceKey) {
        this.preferredUrlPreferenceKey = preferredUrlPreferenceKey;
    }


}
