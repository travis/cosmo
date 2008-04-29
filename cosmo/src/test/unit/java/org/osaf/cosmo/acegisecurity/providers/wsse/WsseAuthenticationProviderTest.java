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

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.springframework.security.BadCredentialsException;
import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.osaf.cosmo.dao.mock.MockDaoStorage;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.DateUtil;
import org.osaf.cosmo.wsse.UsernameToken;
import org.osaf.cosmo.wsse.WsseUtils;

/**
 * Test WsseAuthenticationProvider
 */
public class WsseAuthenticationProviderTest extends TestCase {
    
    private TestHelper testHelper;
    private MockUserDao userDao;
    private MockDaoStorage storage;
    
    /** */
    protected void setUp() throws Exception {
        testHelper = new TestHelper();
        storage = new MockDaoStorage();
        userDao = new MockUserDao(storage);
    }
    
    public void testValidToken() throws Exception {
        User user = testHelper.makeDummyUser("user", "password");
        userDao.createUser(user);
       
        Date currentTime = DateUtil.parseRfc3339Calendar("2008-01-22T14:43:07Z").getTime();
        WsseAuthenticationProvider provider = new WsseAuthenticationProvider();
        provider.setUserDao(userDao);
        provider.setCurrentTime(currentTime);
        
        String passwordDigest = WsseUtils.calculatePasswordDigest("password", "nonce", "2008-01-22T13:43:07Z");
        
        UsernameToken token = new UsernameToken("user", "nonce", passwordDigest, "2008-01-22T13:43:07Z");
        WsseAuthenticationToken authToken = new WsseAuthenticationToken(token);
        
        
        provider.authenticate(authToken);
        
        Assert.assertTrue(authToken.isAuthenticated());
        Object principal = authToken.getPrincipal();
        Assert.assertNotNull(principal);
        Assert.assertTrue(principal instanceof CosmoUserDetails);
        
        CosmoUserDetails cud = (CosmoUserDetails) principal;
        Assert.assertEquals("user", cud.getUser().getUsername());
        Assert.assertEquals(1, authToken.getAuthorities().length);
        Assert.assertEquals("ROLE_USER", authToken.getAuthorities()[0].toString());
    }
    
    public void testValidTokenTimestampExpired() throws Exception {
        User user = testHelper.makeDummyUser("user", "password");
        userDao.createUser(user);
       
        Date currentTime = DateUtil.parseRfc3339Calendar("2008-01-22T14:43:07Z").getTime();
        WsseAuthenticationProvider provider = new WsseAuthenticationProvider();
        provider.setUserDao(userDao);
        provider.setCurrentTime(currentTime);
        
        String passwordDigest = WsseUtils.calculatePasswordDigest("password", "nonce", "2008-01-22T13:43:07Z");
        
        UsernameToken token = new UsernameToken("user", "nonce", passwordDigest, "2008-01-21T13:43:07Z");
        WsseAuthenticationToken authToken = new WsseAuthenticationToken(token);
        
        
        try {
            provider.authenticate(authToken);
            Assert.fail("should have thrown bad creds exception!");
        } catch (BadCredentialsException e) {
          
        }
    }
    
    public void testValidTokenNoUser() throws Exception {
       
        Date currentTime = DateUtil.parseRfc3339Calendar("2008-01-22T14:43:07Z").getTime();
        WsseAuthenticationProvider provider = new WsseAuthenticationProvider();
        provider.setUserDao(userDao);
        provider.setCurrentTime(currentTime);
        
        String passwordDigest = WsseUtils.calculatePasswordDigest("password", "nonce", "2008-01-22T13:43:07Z");
        
        UsernameToken token = new UsernameToken("user", "nonce", passwordDigest, "2008-01-22T13:43:07Z");
        WsseAuthenticationToken authToken = new WsseAuthenticationToken(token);
        
        
        try {
            provider.authenticate(authToken);
            Assert.fail("should have thrown bad creds exception!");
        } catch (BadCredentialsException e) { 
        }
    }
    
    public void testInvalidToken() throws Exception {
        User user = testHelper.makeDummyUser("user", "password");
        userDao.createUser(user);
       
        Date currentTime = DateUtil.parseRfc3339Calendar("2008-01-22T14:43:07Z").getTime();
        WsseAuthenticationProvider provider = new WsseAuthenticationProvider();
        provider.setUserDao(userDao);
        provider.setCurrentTime(currentTime);
        
        String passwordDigest = WsseUtils.calculatePasswordDigest("password", "nonce", "2008-01-22T13:43:07Z");
        
        // nonce is changed
        UsernameToken token = new UsernameToken("user", "nonce_bad", passwordDigest, "2008-01-22T13:43:07Z");
        WsseAuthenticationToken authToken = new WsseAuthenticationToken(token);
        
        
        try {
            provider.authenticate(authToken);
            Assert.fail("should have thrown bad creds exception!");
        } catch (BadCredentialsException e) { 
        }
        
        // digest is wrong
        token = new UsernameToken("user", "nonce", "bad", "2008-01-22T13:43:07Z");
        authToken = new WsseAuthenticationToken(token);
        
        
        try {
            provider.authenticate(authToken);
            Assert.fail("should have thrown bad creds exception!");
        } catch (BadCredentialsException e) { 
        }
        
        // timestamp is wrong
        token = new UsernameToken("user", "nonce", passwordDigest, "2008-01-22T13:44:07Z");
        authToken = new WsseAuthenticationToken(token);
        
        
        try {
            provider.authenticate(authToken);
            Assert.fail("should have thrown bad creds exception!");
        } catch (BadCredentialsException e) { 
        }
    }
}
