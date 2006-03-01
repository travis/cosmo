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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.model.User;
import org.osaf.cosmo.security.CosmoSecurityManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A resource view of a {@link User}.
 */
public class UserResource implements CmpResource {
    private static final Log log = LogFactory.getLog(UserResource.class);

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
     * Constructs a resource that represents the given {@link User}.
     */
    public UserResource(User user, String urlBase) {
        this.user = user;
        this.urlBase = urlBase;
        calculateUserUrl();
        calculateHomedirUrl();
    }

    /**
     * Constructs a resource that represents the given {@link User}
     * and updates its properties as per the given
     * {@link org.w3c.dom.Document}.
     */
    public UserResource(User user, String urlBase, Document doc) {
        this.user = user;
        this.urlBase = urlBase;
        setUser(doc);
        calculateUserUrl();
        calculateHomedirUrl();
    }

    /**
     * Constructs a resource that represents a {@link User}
     * with properties as per the given {@link org.w3c.dom.Document}.
     */
    public UserResource(String urlBase, Document doc) {
        this.urlBase = urlBase;
        this.user = new User();
        setUser(doc);
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
     * {@link org.w3c.dom.Element}.
     *
     * The XML is structured like so:
     *
     * <pre>
     * <user>
     *   <username>bcm</username>
     *   <firstName>Brian</firstName>
     *   <lastName>Moseley</firstName>
     *   <email>bcm@osafoundation.org</email>
     *   <url>http://localhost:8080/cmp/user/bcm</url>
     *   <homedirUrl>http://localhost:8080/home/bcm</homedirUrl>
     * </user>
     * </pre>
     *
     * The user's password is not included in the XML representation.
     */
    public Element toXml(Document doc) {
        Element e =  DomUtil.createElement(doc, EL_USER, NS_CMP);

        Element username = DomUtil.createElement(doc, EL_USERNAME, NS_CMP);
        DomUtil.setText(username, user.getUsername());
        e.appendChild(username);

        Element firstName = DomUtil.createElement(doc, EL_FIRSTNAME, NS_CMP);
        DomUtil.setText(firstName, user.getFirstName());
        e.appendChild(firstName);

        Element lastName = DomUtil.createElement(doc, EL_LASTNAME, NS_CMP);
        DomUtil.setText(lastName, user.getLastName());
        e.appendChild(lastName);

        Element email = DomUtil.createElement(doc, EL_EMAIL, NS_CMP);
        DomUtil.setText(email, user.getEmail());
        e.appendChild(email);

        Element url = DomUtil.createElement(doc, EL_URL, NS_CMP);
        DomUtil.setText(url, userUrl);
        e.appendChild(url);

        if (! user.getUsername().equals(User.USERNAME_OVERLORD)) {
            Element hurl = DomUtil.createElement(doc, EL_HOMEDIRURL, NS_CMP);
            DomUtil.setText(hurl, homedirUrl);
            e.appendChild(hurl);
        }

        return e;
    }

    // our methods

    /**
     * Returns an entity tag as defined in RFC 2616 that uniqely
     * identifies the state of the <code>User</code> backing this
     * resource.
     */
    public String getEntityTag() {
        if (user == null) {
            return "";
        }
        return "\"" + user.hashCode() + "-" +
            user.getDateModified().getTime() + "\"";
    }

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
    protected void setUser(Document doc) {
        if (doc == null) {
            return;
        }

        Element root = doc.getDocumentElement();
        if (! DomUtil.matches(root, EL_USER, NS_CMP)) {
            throw new CmpException("root element not user");
        }

        Element e = DomUtil.getChildElement(root, EL_USERNAME, NS_CMP);
        if (e != null) {
            if (user.getUsername() != null &&
                user.getUsername().equals(User.USERNAME_OVERLORD)) {
                throw new CmpException("root user's username may not " +
                                       "be changed");
            }
            user.setUsername(DomUtil.getTextTrim(e));
        }

        e = DomUtil.getChildElement(root, EL_PASSWORD, NS_CMP);
        if (e != null) {
            user.setPassword(DomUtil.getTextTrim(e));
        }

        e = DomUtil.getChildElement(root, EL_FIRSTNAME, NS_CMP);
        if (e != null) {
            if (user.getUsername() != null &&
                user.getUsername().equals(User.USERNAME_OVERLORD)) {
                throw new CmpException("root user's first name may not " +
                                       "be changed");
            }
            user.setFirstName(DomUtil.getTextTrim(e));
        }

        e = DomUtil.getChildElement(root, EL_LASTNAME, NS_CMP);
        if (e != null) {
            if (user.getUsername() != null &&
                user.getUsername().equals(User.USERNAME_OVERLORD)) {
                throw new CmpException("root user's last name may not " +
                                       "be changed");
            }
            user.setLastName(DomUtil.getTextTrim(e));
        }

        e = DomUtil.getChildElement(root, EL_EMAIL, NS_CMP);
        if (e != null) {
            user.setEmail(DomUtil.getTextTrim(e));
        }
    }

    /**
     */
    protected void calculateUserUrl() {
        userUrl = urlBase + "/cmp/user/" + user.getUsername();
    }

    /**
     */
    protected void calculateHomedirUrl() {
        homedirUrl = urlBase + "/home/" + user.getUsername();
    }
}
