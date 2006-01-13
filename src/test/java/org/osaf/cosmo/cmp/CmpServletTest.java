/*
 * Copyright 2005 Open Source Applications Foundation
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

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.id.random.SessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

import org.osaf.cosmo.TestHelper;
import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.dao.mock.MockUserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;
import org.osaf.cosmo.security.mock.MockSecurityManager;
import org.osaf.cosmo.service.UserService;
import org.osaf.cosmo.service.impl.StandardUserService;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for {@link CmpServlet}.
 */
public class CmpServletTest extends TestCase {
    private static final Log log = LogFactory.getLog(CmpServletTest.class);

    private static final String SERVLET_PATH = "/cmp";

    private TestHelper testHelper;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private MockServletContext servletContext;
    private CmpServlet servlet;

    /**
     */
    protected void setUp() throws Exception {
        userService = createMockUserService();
        userService.init();

        securityManager = new MockSecurityManager();
        // XXX: initiate a security context

        testHelper = new TestHelper();

        servletContext = new MockServletContext();

        servlet = new CmpServlet();
        servlet.setUserService(userService);
        servlet.setSecurityManager(securityManager);
        servlet.init(new MockServletConfig(servletContext));
   }

    private UserService createMockUserService() {
        StandardUserService svc = new StandardUserService();
        svc.setUserDao(new MockUserDao());
        svc.setPasswordGenerator(new SessionIdGenerator());
        return svc;
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

        Document xmldoc = readXmlResponse(response);
        Set users = createUsersFromXml(xmldoc);

        assertTrue(users.size() == 4); // account for overlord
        // can't just blindly check users.contains(u1) cos users read
        // from the response don't have passwords
        assertTrue("User 1 not found in users", containsUser(users, u1));
        assertTrue("User 2 not found in users", containsUser(users, u2));
        assertTrue("User 3 not found in users", containsUser(users, u3));
    }

    private MockHttpServletRequest createMockRequest(String method,
                                                     String cmpPath) {
        MockHttpServletRequest request =
            new MockHttpServletRequest(servletContext, method,
                                       SERVLET_PATH + cmpPath);
        request.setServletPath(SERVLET_PATH);
        request.setPathInfo(cmpPath);
        return request;
    }

    private Document readXmlResponse(MockHttpServletResponse response)
        throws Exception {
        ByteArrayInputStream in =
            new ByteArrayInputStream(response.getContentAsByteArray());
        SAXBuilder builder = new SAXBuilder(false);
        return builder.build(in);
    }

    private Set createUsersFromXml(Document doc)
        throws Exception {
        HashSet users = new HashSet();

        if (doc == null) {
            return users;
        }

        Element root = doc.getRootElement();
        for (Iterator i=root.getChildren(UserResource.EL_USER,
                                         CmpResource.NS_CMP).iterator();
             i.hasNext();) {
            Element e = (Element) i.next();
            User u = createUserFromXml(e);
            users.add(u);
        }

        return users;
    }

    private User createUserFromXml(Document doc)
        throws Exception {
        if (doc == null) {
            return null;
        }
        return createUserFromXml(doc.getRootElement());
    }

    private User createUserFromXml(Element root)
        throws Exception {
        if (root == null) {
            return null;
        }

        User u = new User();

        Element e = root.getChild(UserResource.EL_USERNAME, CmpResource.NS_CMP);
        u.setUsername(getTextContent(e));

        e = root.getChild(UserResource.EL_FIRSTNAME, CmpResource.NS_CMP);
        u.setFirstName(getTextContent(e));

        e = root.getChild(UserResource.EL_LASTNAME, CmpResource.NS_CMP);
        u.setLastName(getTextContent(e));

        e = root.getChild(UserResource.EL_EMAIL, CmpResource.NS_CMP);
        u.setEmail(getTextContent(e));

        return u;
    }

    private String getTextContent(Element e) {
        return ((Text) e.getContent(0)).getText();
    }

    private boolean containsUser(Set users, User test) {
        for (Iterator i=users.iterator(); i.hasNext();) {
            User u = (User) i.next();
            if (u.getUsername().equals(test.getUsername())) {
                return true;
            }
        }
        return false;
    }
}
