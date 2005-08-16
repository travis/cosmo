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
package org.osaf.cosmo.dav.property;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;

import org.jdom.Element;
import org.jdom.Namespace;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.dav.CosmoDavConstants;
import org.osaf.cosmo.dav.property.CosmoDavPropertyName;

/**
 * Represents the CalDAV calendar-description property.
 */
public class CalendarDescription extends AbstractDavProperty {

    private String text;
    private String language;

    /**
     */
    public CalendarDescription(String text) {
        this(text, Locale.getDefault().getLanguage());
    }

    /**
     */
    public CalendarDescription(String text, String language) {
        super(CosmoDavPropertyName.CALENDARDESCRIPTION, true);
        this.text = text;
        this.language = language;
    }

    /**
     * Returns an <code>Element</code> representing this property.
     */
    public Element toXml() {
        Element element = getName().toXml();
        element.setText((String) getValue());
        element.setAttribute(CosmoDavConstants.ATTR_XML_LANG, language,
                             Namespace.XML_NAMESPACE);
        return element;
    }

    /**
     * Returns a <code>String</code> representing the property's
     * text.
     */
    public Object getValue() {
        return text;
    }
}
