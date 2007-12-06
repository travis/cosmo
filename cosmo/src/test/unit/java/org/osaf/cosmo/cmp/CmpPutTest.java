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
package org.osaf.cosmo.cmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.Ticket;
import org.osaf.cosmo.model.User;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test Case for CMP <code>PUT</code> operations.
 */
public class CmpPutTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpPutTest.class);

    /**
     */
    public void testSignup() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1.setAdmin(Boolean.TRUE);
        u1.setLocked(Boolean.TRUE);

        MockHttpServletRequest request = createMockRequest("PUT", "/signup");
        sendXmlRequest(request, new UserContent(u1));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_CREATED,
                     response.getStatus());
        assertNotNull("null Content-Location",
                      response.getHeader("Content-Location"));
        assertNotNull("null ETag", response.getHeader("ETag"));

        User u2 = userService.getUser(u1.getUsername());
        assertFalse("User signed up as admin", u2.getAdmin());
        assertFalse("User signed up as locked", u2.isLocked());
    }

    /**
     */
    public void testBadlyFormattedSignup() throws Exception {
        Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();
        Element e = DomUtil.createElement(doc, "deadbeef", UserResource.NS_CMP);
        doc.appendChild(e);

        MockHttpServletRequest request = createMockRequest("PUT", "/signup");
        sendXmlRequest(request, doc);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testSignupDuplicateUsername() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        // duplicate u1's username
        User u2 = testHelper.makeDummyUser();
        u2.setUsername(u1.getUsername());

        MockHttpServletRequest request = createMockRequest("PUT", "/signup");
        sendXmlRequest(request, new UserContent(u2));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", CmpConstants.SC_USERNAME_IN_USE,
                     response.getStatus());
    }

    /**
     */
    public void testSignupDuplicateEmail() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        // duplicate u1's email address
        User u2 = testHelper.makeDummyUser();
        u2.setEmail(u1.getEmail());

        MockHttpServletRequest request = createMockRequest("PUT", "/signup");
        sendXmlRequest(request, new UserContent(u2));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", CmpConstants.SC_EMAIL_IN_USE,
                     response.getStatus());
    }

    public void testSignupInvalidUsersCase() throws Exception {

        String[] testStrings = { "a", "aaa/bbb/c", "j/b/c", "vvv/v/v" };

        for(int i=0; i < testStrings.length; i++) {
            User u1 = testHelper.makeDummyUser();
            u1.setUsername(testStrings[i]);

            MockHttpServletRequest request = createMockRequest("PUT", "/signup");
            sendXmlRequest(request, new UserContent(u1));

            MockHttpServletResponse response = new MockHttpServletResponse();
            servlet.service(request, response);

            assertEquals("incorrect status for " + testStrings[i],
                        MockHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        }
    }

    public void testSignupValidUsersCase() throws Exception {

        // Test all of the various characters that should work.
        String[] testStrings = { "abcdefghijklmnopqrstuvwxyz",
                                 "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                 "1234567890", "!@#$%^&*(){}\"?><",
                                 ",.\\;'[]=-",
        };


        for(int i=0; i < testStrings.length; i++) {
            User u1 = testHelper.makeDummyUser();

            u1.setUsername(testStrings[i]);

            MockHttpServletRequest request = createMockRequest("PUT", "/signup");
            sendXmlRequest(request, new UserContent(u1));

            MockHttpServletResponse response = new MockHttpServletResponse();
            servlet.service(request, response);

            assertEquals("incorrect status for " + testStrings[i],
                        MockHttpServletResponse.SC_CREATED, response.getStatus());
        }
    }

    /**
     */
    public void testSignupWithSubscription() throws Exception {
        User u1 = testHelper.makeDummyUser();
        User u2 = testHelper.makeDummyUser();
        
        CollectionItem collection =
            testHelper.makeDummyCollection(u2);
        Ticket t1 = 
            testHelper.makeDummyTicket(u2);
        contentService.createTicket(collection, t1);
        
        CollectionSubscription s1 = testHelper.makeDummySubscription(collection, t1);

        MockHttpServletRequest request = createMockRequest("PUT", "/signup");
        sendXmlRequest(request, new UserContent(u1, s1));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_CREATED,
                     response.getStatus());

        User u3 = userService.getUser(u1.getUsername());
        CollectionSubscription s2 = u3.getSubscription(s1.getDisplayName());
        assertTrue("subscription missing",  s2 != null);
        assertEquals("subscription display name wrong", 
                s1.getDisplayName(), s2.getDisplayName());
        assertEquals("subscription ticket key wrong", 
                s1.getTicketKey(), s2.getTicketKey());
        assertEquals("subscription collection uid wrong", 
                s1.getCollectionUid(), s2.getCollectionUid());
        Ticket t2 = contentService.getTicket(collection, s2.getTicketKey());
        assertTrue("ticket missing from collection",  t2 != null);
        assertEquals("ticket wrong", t1, t2);
    }

    /**
     */
    public void testAccountUpdate() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        logInUser(u1);

        // make a new user with all new values for updating the old
        // user, except use the same username cos end users cannot
        // change their own usernames
        User cmpUser = testHelper.makeDummyUser();
        cmpUser.setUsername(u1.getUsername());

        MockHttpServletRequest request = createMockRequest("PUT", "/account");
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
        assertNotNull("null ETag", response.getHeader("ETag"));

        User storedUser = userService.getUser(cmpUser.getUsername());
        assertNotNull("null updated user", storedUser);
        assertNotNull("null username", storedUser.getUsername());
        assertNotNull("null password", storedUser.getPassword());
        // don't bother to check the password of the stored user
        // against that of the cmp user since the stored user's
        // password is encrypted
        assertNotNull("null firstName", storedUser.getFirstName());
        assertEquals("updated firstName doesn't match",
                     storedUser.getFirstName(), cmpUser.getFirstName());
        assertNotNull("null lastName", storedUser.getLastName());
        assertEquals("updated lastName doesn't match", storedUser.getLastName(),
                     cmpUser.getLastName());
        assertNotNull("null email", storedUser.getEmail());
        assertEquals("updated email doesn't match", storedUser.getEmail(),
                     cmpUser.getEmail());
        
        // Make sure users can't make themselves administrators
        
        cmpUser.setAdmin(true);
        
        request = createMockRequest("PUT", "/account");
        sendXmlRequest(request, new UserContent(cmpUser));

        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        this.assertEquals("user was allowed to make himself admin", 
                MockHttpServletResponse.SC_FORBIDDEN ,response.getStatus());

    }

    /**
     */
    public void testAccountUpdateChangeUsername() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        logInUser(u1);

        // try to change the username (and all other attributes too)
        User cmpUser = testHelper.makeDummyUser();

        MockHttpServletRequest request = createMockRequest("PUT", "/account");
        sendXmlRequest(request, new UserContent(cmpUser));
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testUserCreate() throws Exception {
        User cmpUser = testHelper.makeDummyUser();
        // ensure that the user doesn't already exist
        User test = userService.getUser(cmpUser.getUsername());
        assertNull(test);

        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/" + cmpUser.getUsername());
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_CREATED,
                     response.getStatus());
        assertNotNull("null ETag", response.getHeader("ETag"));

        User storedUser = userService.getUser(cmpUser.getUsername());
        assertNotNull("null updated user", storedUser);
        assertNotNull("null username", storedUser.getUsername());
        assertNotNull("null password", storedUser.getPassword());
        // don't bother to check the password of the stored user
        // against that of the cmp user since the stored user's
        // password is encrypted
        assertNotNull("null firstName", storedUser.getFirstName());
        assertEquals("updated firstName doesn't match",
                     storedUser.getFirstName(), cmpUser.getFirstName());
        assertNotNull("null lastName", storedUser.getLastName());
        assertEquals("updated lastName doesn't match", storedUser.getLastName(),
                     cmpUser.getLastName());
        assertNotNull("null email", storedUser.getEmail());
        assertEquals("updated email doesn't match", storedUser.getEmail(),
                     cmpUser.getEmail());
    }

    /**
     */
    public void testUserCreateBadUsername() throws Exception {
        User cmpUser = testHelper.makeDummyUser();

        // cmp user's username won't match uri
        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/deadbeef");
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testUserCreateInvalidUser() throws Exception {
        User cmpUser = testHelper.makeDummyUser();
        // null out the email address
        cmpUser.setEmail(null);

        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/" + cmpUser.getUsername());
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testUserCreateInvalidAttribute() throws Exception {
        Document doc = BUILDER_FACTORY.newDocumentBuilder().newDocument();

        Element user = DomUtil.createElement(doc, UserResource.EL_USER,
                                             UserResource.NS_CMP);
        doc.appendChild(user);

        Element foobar = DomUtil.createElement(doc, "foo",
                                               UserResource.NS_CMP);
        user.appendChild(foobar);

        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/foobar");
        sendXmlRequest(request, doc);

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("User create with invalid attribute did not return Bad Request",
                     MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testUserUpdate() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);
        String originalUsername = u1.getUsername();

        // make a new user with all new values for updating the old
        // user, including username
        User cmpUser = testHelper.makeDummyUser();

        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/" + u1.getUsername());
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_NO_CONTENT,
                     response.getStatus());
        assertNotNull("null Content-Location",
                      response.getHeader("Content-Location"));
        assertNotNull("null ETag", response.getHeader("ETag"));

        User storedUser = userService.getUser(cmpUser.getUsername());
        assertNotNull("null updated user", storedUser);
        assertNotNull("null username", storedUser.getUsername());
        assertEquals("updated username doesn't match",
                     storedUser.getUsername(), cmpUser.getUsername());
        assertNotNull("null password", storedUser.getPassword());
        // don't bother to check the password of the stored user
        // against that of the cmp user since the stored user's
        // password is encrypted
        assertNotNull("null firstName", storedUser.getFirstName());
        assertEquals("updated firstName doesn't match",
                     storedUser.getFirstName(), cmpUser.getFirstName());
        assertNotNull("null lastName", storedUser.getLastName());
        assertEquals("updated lastName doesn't match", storedUser.getLastName(),
                     cmpUser.getLastName());
        assertNotNull("null email", storedUser.getEmail());
        assertEquals("updated email doesn't match", storedUser.getEmail(),
                     cmpUser.getEmail());

        User test = userService.getUser(originalUsername);
        assertNull(test);
    }

    /**
     */
    public void testOverlordUpdateChangeUsername() throws Exception {
        User overlord = userService.getUser(User.USERNAME_OVERLORD);

        // make a new user with all new values for updating the old
        // overlord, including username which may not be changed
        User cmpUser = testHelper.makeDummyUser();

        MockHttpServletRequest request =
            createMockRequest("PUT", "/user/" + overlord.getUsername());
        sendXmlRequest(request, new UserContent(cmpUser));

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status", MockHttpServletResponse.SC_BAD_REQUEST,
                     response.getStatus());
    }

    /**
     */
    public void testPutContentLengthPrecondition() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("PUT", "/deadbeef");
        // don't add any content, which will leave content length unset

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_LENGTH_REQUIRED,
                     response.getStatus());
    }

    /**
     */
    public void testPutContentTypePrecondition() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("PUT", "/deadbeef");
        // add content but with the wrong content type
        request.setContentType("application/octet-stream");
        request.setContent("deadbeef".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                     response.getStatus());
    }

    /**
     */
    public void testPutContentHeaderPrecondition() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("PUT", "/deadbeef");
        // add real content but also Content-Encoding header
        // that is not allowed
        request.setContentType("text/xml");
        request.setContent("deadbeef".getBytes());
        request.addHeader("Content-Encoding", "my-encoding");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_NOT_IMPLEMENTED,
                     response.getStatus());
    }

    /**
     */
    public void testPutBadCommand() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("PUT", "/deadbeef");
        // add some content so that put preconditions are met
        request.setContentType("text/xml");
        request.setContent("deadbeef".getBytes());

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals("incorrect status",
                     MockHttpServletResponse.SC_NOT_FOUND,
                     response.getStatus());
    }
}
