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
import org.apache.jackrabbit.webdav.xml.ElementIterator;

import org.osaf.cosmo.atom.processor.ValidationException;
import org.osaf.cosmo.model.CollectionSubscription;
import org.osaf.cosmo.model.EntityFactory;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.util.DateUtil;
import org.osaf.cosmo.util.UriTemplate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A resource view of a {@link User}.
 */
public class UserResource implements CmpResource, OutputsXml {
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
    /**
     */
    public static final String EL_CREATED = "created";
    /**
     */
    public static final String EL_MODIFIED = "modified";
    /**
     */
    public static final String EL_ADMINISTRATOR = "administrator";
    /**
     */
    public static final String EL_UNACTIVATED = "unactivated";
    /**
     */
    public static final String EL_LOCKED = "locked";
    /**
     */
    public static final String EL_SUBSCRIPTION = "subscription";
    /**
     */
    public static final String ATTR_SUBSCRIPTION_TICKET = "ticket";
    /**
     */
    public static final String ATTR_SUBSCRIPTION_NAME = "name";

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
    public UserResource(User user, String urlBase, Document doc, EntityFactory factory) {
        this.user = user;
        this.urlBase = urlBase;
        setUser(doc, factory);
        calculateUserUrl();
        calculateHomedirUrl();
    }

    /**
     * Constructs a resource that represents a {@link User}
     * with properties as per the given {@link org.w3c.dom.Document}.
     */
    public UserResource(String urlBase, Document doc, EntityFactory factory) {
        this.urlBase = urlBase;
        this.user = factory.createUser();
        setUser(doc, factory);
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

    // OutputsXml methods

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
     *   <created>2006-10-05T17:41:02-0700</created>
     *   <modified>2006-10-05T17:41:02-0700</modified>
     *   <administrator>true</administrator>
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

        Element created = DomUtil.createElement(doc, EL_CREATED, NS_CMP);
        DomUtil.setText(created, DateUtil.formatRfc3339Date(user.getCreationDate()));
        e.appendChild(created);

        Element modified = DomUtil.createElement(doc, EL_MODIFIED, NS_CMP);
        DomUtil.setText(modified, DateUtil.formatRfc3339Date(user.getModifiedDate()));
        e.appendChild(modified);


        Element admin = DomUtil.createElement(doc, EL_ADMINISTRATOR, NS_CMP);
        DomUtil.setText(admin, user.getAdmin().toString());
        e.appendChild(admin);

        Element url = DomUtil.createElement(doc, EL_URL, NS_CMP);
        DomUtil.setText(url, userUrl);
        e.appendChild(url);

        if (!user.isActivated()){
            Element unactivated = DomUtil.createElement(doc, EL_UNACTIVATED, NS_CMP);
            e.appendChild(unactivated);
        }

        Element locked = DomUtil.createElement(doc, EL_LOCKED, NS_CMP);
        DomUtil.setText(locked, user.isLocked().toString());
        e.appendChild(locked);

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
            user.getModifiedDate().getTime() + "\"";
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
    protected void setUser(Document doc, EntityFactory factory) {
        if (doc == null) {
            return;
        }

        Element root = doc.getDocumentElement();
        if (! DomUtil.matches(root, EL_USER, NS_CMP)) {
            throw new CmpException("root element not user");
        }

        for (ElementIterator i=DomUtil.getChildren(root); i.hasNext();) {
            Element e = i.nextElement();

            if (DomUtil.matches(e, EL_USERNAME, NS_CMP)) {
                if (user.getUsername() != null &&
                    user.getUsername().equals(User.USERNAME_OVERLORD)) {
                    throw new CmpException("root user's username may not " +
                                           "be changed");
                }
                user.setUsername(DomUtil.getTextTrim(e));
            }
            else if (DomUtil.matches(e, EL_PASSWORD, NS_CMP)) {
                user.setPassword(DomUtil.getTextTrim(e));
            }
            else if (DomUtil.matches(e, EL_FIRSTNAME, NS_CMP)) {
                if (user.getUsername() != null &&
                    user.getUsername().equals(User.USERNAME_OVERLORD)) {
                    throw new CmpException("root user's first name may not " +
                                           "be changed");
                }
                user.setFirstName(DomUtil.getTextTrim(e));
            }
            else if (DomUtil.matches(e, EL_LASTNAME, NS_CMP)) {
                if (user.getUsername() != null &&
                        user.getUsername().equals(User.USERNAME_OVERLORD)) {
                        throw new CmpException("root user's last name may not " +
                                               "be changed");
                    }
                    user.setLastName(DomUtil.getTextTrim(e));
            }
            else if (DomUtil.matches(e, EL_EMAIL, NS_CMP)) {
                user.setEmail(DomUtil.getTextTrim(e));
            }
            else if (DomUtil.matches(e, EL_ADMINISTRATOR, NS_CMP)){
                if (user.getUsername() != null &&
                    user.getUsername().equals(User.USERNAME_OVERLORD) &&
                    !DomUtil.getTextTrim(e).toLowerCase().equals("true")) {
                        throw new CmpException("root user's admin status " +
                                               "must be true");
                }
                user.setAdmin(Boolean.parseBoolean(DomUtil.getTextTrim(e)));
            }
            else if (DomUtil.matches(e, EL_LOCKED, NS_CMP)){
                if (user.isOverlord())
                    throw new CmpException("root user cannot be locked");
                user.setLocked(Boolean.parseBoolean(DomUtil.getTextTrim(e)));
            }
            else if (DomUtil.matches(e, EL_SUBSCRIPTION, NS_CMP)){
                String uuid = DomUtil.getTextTrim(e);
                String ticketKey = e.getAttribute(ATTR_SUBSCRIPTION_TICKET);
                String displayName = e.getAttribute(ATTR_SUBSCRIPTION_NAME);
                if (displayName == null)
                    throw new CmpException("Subscription requires a display name");
                if (uuid == null)
                    throw new CmpException("Subscription requires a collection uuid");
                if (ticketKey == null)
                    throw new CmpException("Subscription requires a ticket key");
                if (user.getSubscription(displayName) != null)
                    throw new CmpException("Subscription with this name already exists");
                CollectionSubscription subscription = 
                    factory.createCollectionSubscription();
                subscription.setDisplayName(displayName);
                subscription.setCollectionUid(uuid);
                subscription.setTicketKey(ticketKey);
                user.addSubscription(subscription);
            }
            else {
                throw new CmpException("unknown user attribute element " +
                                       e.getTagName());
            }
        }
    }

    /**
     */
    protected void calculateUserUrl() {
        userUrl = urlBase + "/cmp/user/" + UriTemplate.escapeSegment(user.getUsername());
    }

    /**
     */
    protected void calculateHomedirUrl() {
        homedirUrl = urlBase + "/dav/" + UriTemplate.escapeSegment(user.getUsername());
    }
}
