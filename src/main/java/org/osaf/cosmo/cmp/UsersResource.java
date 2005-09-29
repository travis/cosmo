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

import java.util.Iterator;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import org.osaf.cosmo.model.User;

/**
 * An interface for Cosmo API resources
 */
public class UsersResource implements CmpResource {
    /**
     */
    public static final String EL_USERS = "users";

    private Set users;
    private String urlBase;

    /**
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
     * {@link org.jdom.Document}.
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
    public Document toXml() {
        Element e = new Element(EL_USERS, NS_CMP);
        for (Iterator i=users.iterator(); i.hasNext();) {
            User user = (User) i.next();
            Element ue = (Element)
                (new UserResource(user, urlBase)).toXml().getContent(0).clone();
            e.addContent(ue);
        }
        return new Document(e);
    }
}
