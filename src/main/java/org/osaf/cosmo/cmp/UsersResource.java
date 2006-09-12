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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jackrabbit.webdav.xml.DomUtil;

import org.osaf.cosmo.model.User;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface for Cosmo API resources
 */
public class UsersResource implements CmpResource {
    private static final Log log = LogFactory.getLog(UsersResource.class);

    /**
     */
    public static final String EL_USERS = "users";

    private Set users;
    private String urlBase;

    /**
     * Constructs a resource that represents the given {@link User}s.
     */
    public UsersResource(Set users, String urlBase) {
        this.users = users;
        this.urlBase = urlBase;
    }

    /**
     * Returns the <code>List</code> of <code>User</code>s that backs
     * this resource.
     */
    public Object getEntity() {
        return users;
    }

    /**
     * Returns an XML representation of the resource in the form of a
     * {@link org.w3c.dom.Element}.
     *
     * The XML is structured like so:
     *
     * <pre>
     * <users>
     *   <user>
     *     ...
     *   </user>
     * </users>
     * </pre>
     *
     * where the structure of the <code>user</code> element is defined
     * by {@link UserResource}.
     */
    public Element toXml(Document doc) {
        Element e = DomUtil.createElement(doc, EL_USERS, NS_CMP);
        for (Iterator i=users.iterator(); i.hasNext();) {
            User user = (User) i.next();
            UserResource ur = new UserResource(user, urlBase);
            e.appendChild(ur.toXml(doc));
        }
        return e;
    }

    // our methods

    /**
     */
    public Set getUsers() {
        return users;
    }
}
