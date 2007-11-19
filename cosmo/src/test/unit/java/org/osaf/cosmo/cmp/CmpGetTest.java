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
package org.osaf.cosmo.cmp;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibUser;
import org.osaf.cosmo.util.DateUtil;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test Case for CMP <code>GET</code> operations.
 */
public class CmpGetTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpGetTest.class);

    /**
     */
    public void testGetAccount() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        logInUser(u1);

        MockHttpServletRequest request = createMockRequest("GET", "/account");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(response.getStatus(), MockHttpServletResponse.SC_OK);
        assertNotNull(response.getHeader("ETag"));

        CmpUser user = createUserFromXml(readXmlResponse(response));
        assertNotNull("user null", user);
        assertNotNull("user has no username", user.getUsername());
        assertEquals("usernames don't match", user.getUsername(),
                     u1.getUsername());
        assertNotNull("user has no first name", user.getFirstName());
        assertEquals("first names don't match", user.getFirstName(),
                     u1.getFirstName());
        assertNotNull("user has no last name", user.getLastName());
        assertEquals("last names don't match", user.getLastName(),
                     u1.getLastName());
        assertNotNull("user has no email", user.getEmail());
        assertEquals("emails don't match", user.getEmail(), u1.getEmail());
        assertNotNull("user has no url", user.getUrl());
        assertNotNull("user has no homedir url", user.getHomedirUrl());
    }
    
    public void testGetUserCount() throws Exception {
        MockHttpServletRequest request = createMockRequest("GET", "/users/count");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        String count = response.getContentAsString();
        assertTrue(count + " not equal to 1.", count.equals("1"));
        
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);
        
        request = createMockRequest("GET", "/users/count");
        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        count = response.getContentAsString();
        assertTrue(count + " not equal to 2.", count.equals("2"));
        
        User u2 = testHelper.makeDummyUser();
        userService.createUser(u2);

        request = createMockRequest("GET", "/users/count");
        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        count = response.getContentAsString();
        assertTrue(count + " not equal to 3.", count.equals("3"));

        User u3 = testHelper.makeDummyUser();
        userService.createUser(u3);

        request = createMockRequest("GET", "/users/count");
        response = new MockHttpServletResponse();
        servlet.service(request, response);
        
        count = response.getContentAsString();
        assertTrue(count + " not equal to 4.", count.equals("4"));
    }

    /**
     */
    public void testGetUsers() throws Exception {
        User u1 = testHelper.makeDummyUser();
        userService.createUser(u1);
        User u2 = testHelper.makeDummyUser();
        userService.createUser(u2);
        User u3 = testHelper.makeDummyUser();
        userService.createUser(u3);

        MockHttpServletRequest request = createMockRequest("GET", "/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(response.getStatus(), MockHttpServletResponse.SC_OK);

        Set users = createUsersFromXml(readXmlResponse(response));
        assertTrue(users.size() == 4); // account for overlord
        // can't just blindly check users.contains(u1) cos users read
        // from the response don't have passwords
        assertTrue("User 1 not found in users", containsUser(users, u1));
        assertTrue("User 2 not found in users", containsUser(users, u2));
        assertTrue("User 3 not found in users", containsUser(users, u3));

        CmpUser regular = findUser(users, u1.getUsername());
        assertNotNull("regular user has no first name", regular.getFirstName());
        assertNotNull("regular user has no last name", regular.getLastName());
        assertNotNull("regular user has no email", regular.getEmail());
        assertNotNull("regular user has no url", regular.getUrl());
        assertNotNull("regular user has no homedir url",
                      regular.getHomedirUrl());

        CmpUser overlord = findUser(users, User.USERNAME_OVERLORD);
        assertNotNull("overlord has no first name", overlord.getFirstName());
        assertNotNull("overlord has no last name", overlord.getLastName());
        assertNotNull("overlord has no email", overlord.getEmail());
        assertNotNull("overlord has no url", overlord.getUrl());
        assertNull("overlord has a homedir url", overlord.getHomedirUrl());
    }

    /**
     */
    public void testGetUser() throws Exception {
        User u1 = testHelper.makeDummyUser();
        u1 = userService.createUser(u1);

        MockHttpServletRequest request =
            createMockRequest("GET", "/user/" + u1.getUsername());
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(response.getStatus(), MockHttpServletResponse.SC_OK);
        assertNotNull(response.getHeader("ETag"));

        CmpUser user = createUserFromXml(readXmlResponse(response));
        assertNotNull("user null", user);
        assertNotNull("user has no username", user.getUsername());
        assertEquals("usernames don't match", user.getUsername(),
                     u1.getUsername());
        assertNotNull("user has no first name", user.getFirstName());
        assertEquals("first names don't match", user.getFirstName(),
                     u1.getFirstName());
        assertNotNull("user has no last name", user.getLastName());
        assertEquals("last names don't match", user.getLastName(),
                     u1.getLastName());
        assertNotNull("user has no email", user.getEmail());
        assertEquals("emails don't match", user.getEmail(), u1.getEmail());
        assertNotNull("user has no creation date", user.getCreationDate());
        assertEquals("creation dates don't match", 
                     DateUtil.formatRfc3339Date(user.getCreationDate()), 
                     DateUtil.formatRfc3339Date(u1.getCreationDate()));
        assertNotNull("user has no modification date", user.getModifiedDate());
        assertEquals("modification dates don't match", 
                     DateUtil.formatRfc3339Date(user.getModifiedDate()), 
                     DateUtil.formatRfc3339Date(u1.getModifiedDate()));
        assertNotNull("user has no administrator boolean", user.getAdmin());
        assertEquals("administrator booleans don't match", user.getAdmin(), u1.getAdmin());
        assertNotNull("user has no url", user.getUrl());
        assertNotNull("user has no homedir url", user.getHomedirUrl());
    }

    /**
     */
    public void testGetOverlord() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/user/" + User.USERNAME_OVERLORD);
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(response.getStatus(), MockHttpServletResponse.SC_OK);
        assertNotNull(response.getHeader("ETag"));

        CmpUser overlord = createUserFromXml(readXmlResponse(response));
        assertNotNull("overlord null", overlord);
        assertNotNull("overlord has no username", overlord.getUsername());
        assertEquals("usernames don't match", overlord.getUsername(),
                     User.USERNAME_OVERLORD);
        assertNotNull("overlord has no first name", overlord.getFirstName());
        assertNotNull("overlord has no last name", overlord.getLastName());
        assertNotNull("overlord has no email", overlord.getEmail());
        assertNotNull("overlord has no url", overlord.getUrl());
        assertNull("overlord has a homedir url", overlord.getHomedirUrl());
    }

    /**
     */
    public void testGetNonExistentUser() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/user/deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NOT_FOUND);
    }

    /**
     */
    public void testGetUserWithoutUsername() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/user/");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NOT_FOUND);
    }

    /**
     */
    public void testGetServerStatus() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/server/status");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertEquals(MockHttpServletResponse.SC_OK, response.getStatus());
        assertEquals("text/plain", response.getContentType());
        assertEquals("UTF-8", response.getCharacterEncoding());

        BufferedReader content = new BufferedReader(new StringReader(response.getContentAsString()));
        String line = content.readLine();
        boolean found = false;
        while (line != null) {
            if (line.startsWith("jvm.memory.max=")) {
                found = true;
                break;
            }
            line = content.readLine();
        }
        assertTrue("did not find jvm.memory.max in status output", found);
    }

    /**
     */
    public void testGetBadCommand() throws Exception {
        MockHttpServletRequest request =
            createMockRequest("GET", "/deadbeef");
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);

        assertTrue(response.getStatus() ==
                   MockHttpServletResponse.SC_NOT_FOUND);
    }

    private Set createUsersFromXml(Document doc)
        throws Exception {
        HashSet users = new HashSet();
        if (doc == null) {
            return null;
        }

        ElementIterator i =
            DomUtil.getChildren(doc.getDocumentElement(), UserResource.EL_USER,
                                OutputsXml.NS_CMP);
        while (i.hasNext()) {
            CmpUser u = createUserFromXml(i.nextElement());
            users.add(u);
        }

        return users;
    }

    private CmpUser createUserFromXml(Document doc)
        throws Exception {
        if (doc == null) {
            return null;
        }
        return createUserFromXml(doc.getDocumentElement());
    }

    private CmpUser createUserFromXml(Element root)
        throws Exception {
        if (root == null) {
            return null;
        }

        CmpUser u = new CmpUser();

        u.setUsername(DomUtil.getChildTextTrim(root, UserResource.EL_USERNAME,
                                               OutputsXml.NS_CMP));
        u.setFirstName(DomUtil.getChildTextTrim(root, UserResource.EL_FIRSTNAME,
                                                OutputsXml.NS_CMP));
        u.setLastName(DomUtil.getChildTextTrim(root, UserResource.EL_LASTNAME,
                                               OutputsXml.NS_CMP));
        u.setEmail(DomUtil.getChildTextTrim(root, UserResource.EL_EMAIL,
                                            OutputsXml.NS_CMP));
        u.setUrl(DomUtil.getChildTextTrim(root, UserResource.EL_URL,
                                          OutputsXml.NS_CMP));
        u.setHomedirUrl(DomUtil.getChildTextTrim(root,
                                                 UserResource.EL_HOMEDIRURL,
                                                 OutputsXml.NS_CMP));
        
        u.setCreationDate(DateUtil.parseRfc3339Date(
                DomUtil.getChildTextTrim(root, 
                                         UserResource.EL_CREATED, 
                                         OutputsXml.NS_CMP)));
        
        u.setModifiedDate(DateUtil.parseRfc3339Date(
                DomUtil.getChildTextTrim(root, 
                                         UserResource.EL_MODIFIED, 
                                         OutputsXml.NS_CMP)));
        
        u.setAdmin(Boolean.parseBoolean(DomUtil.getChildTextTrim(root, 
                        UserResource.EL_ADMINISTRATOR, 
                        OutputsXml.NS_CMP)));
        
        return u;
    }

    private boolean containsUser(Set users, User test) {
        return findUser(users, test.getUsername()) != null;
    }

    private CmpUser findUser(Set users, String username) {
        for (Iterator i=users.iterator(); i.hasNext();) {
            CmpUser u = (CmpUser) i.next();
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public class CmpUser extends HibUser {
        private String url;
        private String homedirUrl;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHomedirUrl() {
            return homedirUrl;
        }

        public void setHomedirUrl(String homedirUrl) {
            this.homedirUrl = homedirUrl;
        }
    }
}
