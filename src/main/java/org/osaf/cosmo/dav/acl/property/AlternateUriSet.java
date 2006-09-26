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
 * Represents the DAV:alternate-URI-set property.
 *
 * This property is protected. The value contains three DAV:href
 * elements specifying the Atom, CMP and web URLs for the principal.
 */
public class AlternateUriSet extends AbstractDavProperty
    implements AclConstants {

    private DavHomeCollection home;

    /**
     */
    public AlternateUriSet(DavHomeCollection home) {
        super(ALTERNATEURISET, true);
        this.home = home;
    }

    /**
     * Returns a
     * <code>AlternateUriSet.AlternateUriSetInfo</code>
     * for this property.
     */
    public Object getValue() {
        return new AlternateUriSetInfo();
    }

    /**
     */
    public class AlternateUriSetInfo implements XmlSerializable {
  
        /**
         */
        public Element toXml(Document document) {
            Element atom =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(atom, home.getAtomLocator().getHref(false));

            Element cmp =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(cmp, home.getCmpLocator().getHref(false));

            Element web =
                DomUtil.createElement(document, XML_HREF, NAMESPACE);
            DomUtil.setText(web, home.getWebLocator().getHref(false));

            Element set =
                DomUtil.createElement(document,
                                      ELEMENT_ACL_ALTERNATE_URI_SET,
                                      NAMESPACE);
            set.appendChild(atom);
            set.appendChild(cmp);
            set.appendChild(web);

            return set;
        }
    }
}
