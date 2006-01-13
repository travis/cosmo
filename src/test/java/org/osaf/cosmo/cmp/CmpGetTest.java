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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

import org.osaf.cosmo.cmp.CmpServlet;
import org.osaf.cosmo.model.User;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test Case for CMP <code>GET</code> operations.
 */
public class CmpGetTest extends BaseCmpServletTestCase {
    private static final Log log = LogFactory.getLog(CmpGetTest.class);

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
