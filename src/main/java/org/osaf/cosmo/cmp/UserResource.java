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

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

/**
 * A resource view of a {@link User}.
 */
public class UserResource implements CmpResource {
    /**
     */
    public static final String EL_USER = "user";
    /**
     */
    public static final String EL_USERNAME = "username";
    /**
     */
    public static final String EL_PASSWORD = "password";
    /**
     */
    public static final String EL_FIRSTNAME = "firstName";
    /**
     */
    public static final String EL_LASTNAME = "lastName";
    /**
     */
    public static final String EL_EMAIL = "email";
    /**
     */
    public static final String EL_URL = "url";
    /**
     */
    public static final String EL_HOMEDIRURL = "homedirUrl";

    private User user;
    private String urlBase;
    private String userUrl;
    private String homedirUrl;

    /**
     */
    public UserResource(User user, String urlBase) {
        this.user = user;
        this.urlBase = urlBase;
        calculateUserUrl();
        calculateHomedirUrl();
    }

    /**
     */
    public UserResource(User user, String urlBase, Document doc) {
        this.user = user;
        this.urlBase = urlBase;
        setUserProperties(doc);
        calculateUserUrl();
        calculateHomedirUrl();
    }

    /**
     */
    public UserResource(String urlBase, Document doc) {
        this.user = new User();
        this.urlBase = urlBase;
        setUserProperties(doc);
        calculateUserUrl();
        calculateHomedirUrl();
    }

    // CmpResource methods

    /**
     * Returns the <code>User</code> that backs this resource.
     */
    public Object getEntity() {
        return user;
    }

    /**
     * Returns an XML representation of the resource in the form of a
     * {@link org.jdom.Document}.
     *
     * The XML is structured like so:
     *
     * <pre>
     * <user>
     *   <username>bcm</username>
     *   <firstName>Brian</firstName>
     *   <lastName>Moseley</firstName>
     *   <email>bcm@osafoundation.org</email>
     *   <url>http://localhost:8080/api/user/bcm</url>
     *   <homedirUrl>http://localhost:8080/home/bcm</homedirUrl>
     * </user>
     * </pre>
     *
     * The user's password is not included in the XML representation.
     */
    public Document toXml() {
        Element e = new Element(EL_USER, NS_CMP);

        Element username = new Element(EL_USERNAME, NS_CMP);
        username.addContent(user.getUsername());
        e.addContent(username);

        Element firstName = new Element(EL_FIRSTNAME, NS_CMP);
        firstName.addContent(user.getFirstName());
        e.addContent(firstName);

        Element lastName = new Element(EL_LASTNAME, NS_CMP);
        lastName.addContent(user.getLastName());
        e.addContent(lastName);

        Element email = new Element(EL_EMAIL, NS_CMP);
        email.addContent(user.getEmail());
        e.addContent(email);

        Element url = new Element(EL_URL, NS_CMP);
        url.addContent(userUrl);
        e.addContent(url);

        if (! user.isOverlord()) {
            Element hurl = new Element(EL_HOMEDIRURL, NS_CMP);
            hurl.addContent(homedirUrl);
            e.addContent(hurl);
        }

        return new Document(e);
    }

    // our methods

    /**
     * Just as {@link #getEntity}, except the returned object is cast
     * to <code>User</code>.
     */
    public User getUser() {
        return (User) getEntity();
    }

    /**
     */
    public String getUserUrl() {
        return userUrl;
    }

    /**
     */
    public String getHomedirUrl() {
        return homedirUrl;
    }

    /**
     */
    protected void setUserProperties(Document doc) {
        if (doc == null) {
            return;
        }

        Element root = doc.getRootElement();
        if (! root.getName().equals(EL_USER)) {
            throw new CmpException("root element not user");
        }
        if (! root.getNamespace().equals(NS_CMP)) {
            throw new CmpException("root element not in CMP namespace");
        }

        Element e = root.getChild(EL_USERNAME, NS_CMP);
        if (e != null) {
            if (user.isOverlord()) {
                throw new CmpException("root user's username may not " +
                                       "be changed");
            }
            user.setUsername(getTextContent(e));
        }

        e = root.getChild(EL_PASSWORD, NS_CMP);
        if (e != null) {
            user.setPassword(getTextContent(e));
        }

        e = root.getChild(EL_FIRSTNAME, NS_CMP);
        if (e != null) {
            if (user.isOverlord()) {
                throw new CmpException("root user's first name may not " +
                                       "be changed");
            }
            user.setFirstName(getTextContent(e));
        }

        e = root.getChild(EL_LASTNAME, NS_CMP);
        if (e != null) {
            if (user.isOverlord()) {
                throw new CmpException("root user's last name may not " +
                                       "be changed");
            }
            user.setLastName(getTextContent(e));
        }

        e = root.getChild(EL_EMAIL, NS_CMP);
        if (e != null) {
            user.setEmail(getTextContent(e));
        }
    }

    /**
     */
    protected void calculateUserUrl() {
        userUrl = urlBase + "/api/user/" + user.getUsername();
    }

    /**
     */
    protected void calculateHomedirUrl() {
        homedirUrl = urlBase + "/home/" + user.getUsername();
    }

    /**
     */
    protected String getTextContent(Element e) {
        if (e.getContentSize() != 1) {
            throw new CmpException(e.getName() + " must be single-valued");
        }
        Content c = e.getContent(0);
        if (! (c instanceof Text)) {
            throw new CmpException(e.getName() + " content not text");
        }
        return ((Text) c).getText();
    }
}
