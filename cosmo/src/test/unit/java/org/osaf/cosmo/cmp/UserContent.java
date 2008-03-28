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
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.cmp.UserResource;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.CollectionSubscription;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple wrapper bean that converts a {@link User} to an XML
 * fragment suitable for sending as CMP request content. The
 * client-side test equivalent of {@link UserResource}.
 */
public class UserContent implements XmlSerializable {
    private static final Log log = LogFactory.getLog(UserContent.class);

    private User user;
    private CollectionSubscription subscription;

    /**
     */
    public UserContent(User user) {
        this.user = user;
    }

    public UserContent(User user, CollectionSubscription sub) {
        this.user = user;
        this.subscription = sub;
    }

    /**
     */
    public Element toXml(Document doc) {
        Element e = DomUtil.createElement(doc, UserResource.EL_USER,
                                          UserResource.NS_CMP);

        if (user.getUsername() != null) {
            Element username =
                DomUtil.createElement(doc, UserResource.EL_USERNAME,
                                      UserResource.NS_CMP);
            DomUtil.setText(username, user.getUsername());
                e.appendChild(username);
        }

        if (user.getPassword() != null) {
            Element password =
                DomUtil.createElement(doc, UserResource.EL_PASSWORD,
                                      UserResource.NS_CMP);
            DomUtil.setText(password, user.getPassword());
            e.appendChild(password);
        }

        if (user.getFirstName() != null) {
            Element firstName =
                DomUtil.createElement(doc, UserResource.EL_FIRSTNAME,
                                      UserResource.NS_CMP);
            DomUtil.setText(firstName, user.getFirstName());
            e.appendChild(firstName);
        }

        if (user.getLastName() != null) {
            Element lastName =
                DomUtil.createElement(doc, UserResource.EL_LASTNAME,
                                      UserResource.NS_CMP);
            DomUtil.setText(lastName, user.getLastName());
            e.appendChild(lastName);
        }

        if (user.getEmail() != null) {
            Element email =
                DomUtil.createElement(doc, UserResource.EL_EMAIL,
                                      UserResource.NS_CMP);
            DomUtil.setText(email, user.getEmail());
            e.appendChild(email);
        }

        if (user.getAdmin() != null) {
            Element admin =
                DomUtil.createElement(doc, UserResource.EL_ADMINISTRATOR,
                        UserResource.NS_CMP);
            DomUtil.setText(admin, user.getAdmin().toString());
            e.appendChild(admin);
        }

        if (user.isLocked() != null) {
            Element locked =
                DomUtil.createElement(doc, UserResource.EL_LOCKED,
                        UserResource.NS_CMP);
            DomUtil.setText(locked, user.isLocked().toString());
            e.appendChild(locked);
        }

        if (subscription != null){
            Element sub = 
                DomUtil.createElement(doc, 
                        UserResource.EL_SUBSCRIPTION,
                        UserResource.NS_CMP);
            sub.setAttribute(UserResource.ATTR_SUBSCRIPTION_NAME, 
                    subscription.getDisplayName());
            sub.setAttribute(UserResource.ATTR_SUBSCRIPTION_TICKET,
                    subscription.getTicketKey());
            DomUtil.setText(sub, subscription.getCollectionUid());
            e.appendChild(sub);
        }

        return e;
    }
}
