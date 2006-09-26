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
 * Represents the DAV:principal-URL property.
 *
 * The property is protected. The value is a single DAV:href element
 * containing the URL of the home collection.
 */
public class PrincipalUrl extends AbstractDavProperty
    implements AclConstants {

    private DavHomeCollection home;

    /**
     */
    public PrincipalUrl(DavHomeCollection home) {
        super(PRINCIPALURL, true);
        this.home = home;
    }

    /**
     * Returns a
     * <code>PrincipalUrl.PrincipalUrlInfo</code>
     * for this property.
     */
    public Object getValue() {
        return new PrincipalUrlInfo();
    }

    /**
     */
    public class PrincipalUrlInfo implements XmlSerializable {
  
        /**
         */
        public Element toXml(Document document) {
            Element href =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(href, home.getLocator().getHref(true));

            Element url =
                DomUtil.createElement(document,
                                      ELEMENT_ACL_PRINCIPAL_URL,
                                      NAMESPACE);
            url.appendChild(href);

            return url;
        }
    }
}
