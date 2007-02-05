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
package org.osaf.cosmo.dav.acl.property;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.acl.AclConstants;
import org.osaf.cosmo.dav.impl.DavHomeCollection;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 * Represents the DAV:group-membership property.
 *
 * The property is protected. The value is a list of hrefs indicating
 * the groups in which the principal is directly a member. The list
 * will always contain 0 elements since groups are not yet supported.
 */
public class GroupMembership extends AbstractDavProperty
    implements AclConstants {

    /**
     */
    public GroupMembership() {
        super(GROUPMEMBERSHIP, true);
    }

    /**
     * Returns a
     * <code>GroupMembership.GroupMembershipInfo</code>
     * for this property.
     */
    public Object getValue() {
        return new GroupMembershipInfo();
    }

    /**
     */
    public class GroupMembershipInfo implements XmlSerializable {
  
        /**
         */
        public Element toXml(Document document) {
            Element groups =
                DomUtil.createElement(document,
                                      ELEMENT_ACL_GROUP_MEMBERSHIP,
                                      NAMESPACE);

            return groups;
        }
    }
}
