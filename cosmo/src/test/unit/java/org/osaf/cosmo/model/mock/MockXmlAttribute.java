/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.model.mock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osaf.cosmo.model.Attribute;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.ModelValidationException;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.XmlAttribute;
import org.w3c.dom.Element;

/**
 * Represents an attribute with an XML DOM Element value.
 */
public class MockXmlAttribute extends MockAttribute
    implements java.io.Serializable, XmlAttribute {
    private static final Log log = LogFactory.getLog(XmlAttribute.class);

    private Element value;

    public MockXmlAttribute() {
    }

    public MockXmlAttribute(QName qname,
                        Element value) {
        setQName(qname);
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceXmlAttribute#getValue()
     */
    public Element getValue() {
        return this.value;
    }

    public Attribute copy() {
        XmlAttribute attr = new MockXmlAttribute();
        attr.setQName(getQName().copy());
        Element clone = value != null ?
            (Element) value.cloneNode(true) : null;
        attr.setValue(clone);
        return attr;
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceXmlAttribute#setValue(org.w3c.dom.Element)
     */
    public void setValue(Element value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && ! (value instanceof Element))
            throw new ModelValidationException("attempted to set non-Element value");
        setValue((Element) value);
    }

    /**
     * Convienence method for returning a Element value on an XmlAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch XmlAttribute from
     * @param qname QName of attribute
     * @return Long value of XmlAttribute
     */
    public static Element getValue(Item item,
                                QName qname) {
        XmlAttribute xa = (XmlAttribute) item.getAttribute(qname);
        if (xa == null)
            return null;
        else
            return xa.getValue();
    }

    /**
     * Convienence method for setting a Elementvalue on an XmlAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch Xmlttribute from
     * @param qname QName of attribute
     * @param value value to set on XmlAttribute
     */
    public static void setValue(Item item,
                                QName qname,
                                Element value) {
        XmlAttribute attr = (XmlAttribute) item.getAttribute(qname);
        if (attr == null && value != null) {
            attr = new MockXmlAttribute(qname, value);
            item.addAttribute(attr);
            return;
        }
        if (value == null)
            item.removeAttribute(qname);
        else
            attr.setValue(value);
    }
}
