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
package org.osaf.cosmo.acegisecurity.providers.wsse;

import java.text.ParseException;
import java.util.Date;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.AuthenticationProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.DateUtil;
import org.osaf.cosmo.wsse.UsernameToken;
import org.osaf.cosmo.wsse.WsseUtils;

/**
 * AuthenticationProvider that authenticates WsseAuthenticationTokens
 * based on WSSE UsernameToken profile. 
 * 
 * <p>
 * see http://www.oasis-open.org/committees/wss/documents/WSS-Username-02-0223-merged.pdf
 * </p>
 *
 * <p>
 * A UsernameToken consists of a username, nonce, password digest, and 
 * creation timestamp.  The password digest is equal to: <br/>
 * Base64 \ (SHA1 (Nonce + CreationTimestamp + Password))
 * </p>
 *
 * <p>
 * The nonce is a cryptographically random string. The creation timestamp is
 * in W3DTF format.  The password is the MD5 hash of the clear text password.
 * This is a password equivalent and is required because the server only has
 * access to the MD5 hash.
 * </p>
 * 
 * <p>
 * The AuthenticationProvider ensures the hash is valid by re-constructing
 * from the given username/nonce/timestamp and md5 hash stored in the db.
 * If valid, the provider ensures the timestamp hasn't timed out based on 
 * the current time and a configurable token timeout period.  If everything
 * checks out, the provider authenticates the token, populating it with
 * a CosmoUserDetails for the authenticated user.
 * </p>
 */
public class WsseAuthenticationProvider
    implements AuthenticationProvider {
    private static final Log log =
        LogFactory.getLog(WsseAuthenticationProvider.class);
    
    // 2 hour timeout (in seconds)
    private static int DEFAULT_WSSE_TOKEN_TIMEOUT = 60*60*2;

    private UserDao userDao;
    
    // Used for testing purposes
    private Date currentTime = null;
    private int tokenTimeout = DEFAULT_WSSE_TOKEN_TIMEOUT;
    
    // AuthenticationProvider methods

    /** */
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        if (! supports(authentication.getClass()))
            return null;

        WsseAuthenticationToken token =
            (WsseAuthenticationToken) authentication;
        
        UsernameToken wsseToken = (UsernameToken) token.getCredentials();
        
        if(wsseToken.getNonce()==null || "".equals(wsseToken.getNonce())
                || wsseToken.getCreated()==null || "".equals(wsseToken.getCreated())
                || wsseToken.getPasswordDigest()==null || "".equals(wsseToken.getPasswordDigest()))
            throw new BadCredentialsException("WSSE token invalid : missing fields");
        
        Date created = null;
        
        try {
            created = DateUtil.parseRfc3339Calendar(wsseToken.getCreated()).getTime();
        } catch (ParseException e) {
            throw new BadCredentialsException("WSSE token invalid: invalid created timestamp format");
        }
        
        // Date must be before current date
        Date currentDate = currentTime == null ? new Date() : currentTime;
        
        if(created.after(currentDate))
            throw new BadCredentialsException("WSSE token invalid: created is in the future");
        
        if(currentDate.getTime() > (created.getTime() + (tokenTimeout*1000)))
            throw new BadCredentialsException("WSSE token invalid: token timed out");
        
        User user = userDao.getUser(wsseToken.getUsername());
        if(user==null)
            throw new BadCredentialsException("WSSE token invalid: invalid user");
        
        // TODO: cache nonces and reject nonces that have already been
        // used
        
        String passwordDigest = WsseUtils.calculatePasswordDigest(user
                .getPassword(), wsseToken.getNonce(), wsseToken.getCreated());
        
        if(passwordDigest.equals(wsseToken.getPasswordDigest())) {
            token.setUserDetails(new CosmoUserDetails(user));
            token.setAuthenticated(true);
            return token;
        }
        
       
        throw new BadCredentialsException("WSSE token invalid: invalid password hash");
    }

    /** */
    public boolean supports(Class authentication) {
        return WsseAuthenticationToken.class.
            isAssignableFrom(authentication);
    }

    // our methods

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
    
    public int getTokenTimeout() {
        return tokenTimeout;
    }

    public void setTokenTimeout(int tokenTimeout) {
        this.tokenTimeout = tokenTimeout;
    }

    protected void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }
}
