/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dav.caldav.property;

import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.osaf.cosmo.calendar.util.CalendarUtils;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents the CalDAV supported-collation-set
 * property.
 */
public class SupportedCollationSet extends AbstractDavProperty
    implements CaldavConstants, ICalendarConstants {

    private String[] collations;

    public SupportedCollationSet() {
        this(SUPPORTED_COLLATIONS);
    }

    public SupportedCollationSet(Set<String> collations) {
        this((String[]) collations.toArray(new String[0]));
    }

    public SupportedCollationSet(String[] collations) {
        super(SUPPORTEDCOLLATIONSET, true);
        for (String collation :collations) {
            if (! CalendarUtils.isSupportedCollation(collation)) {
                throw new IllegalArgumentException("Invalid collation '" +
                                                   collation + "'.");
            }
        }
        this.collations = collations;
    }

    /**
     * (Returns a <code>Set</code> of
     * <code>SupportedCollationsSet.SupportedCollationInfo</code>s
     * for this property.
     */
    public Object getValue() {
        Set infos = new HashSet();
        for (String collation : collations)
            infos.add(new SupportedCollationInfo(collation));
        return infos;
    }

    /**
     * Returns the collations for this property.
     */
    public String[] getCollations() {
        return collations;
    }

    /**
     */
    public class SupportedCollationInfo implements XmlSerializable {
        private String collation;

        /**
         */
        public SupportedCollationInfo(String collation) {
            this.collation = collation;
        }

        public String toString() {
            return collation;
        }

        /**
         */
        public Element toXml(Document document) {
            Element elem =
                DomUtil.createElement(document, ELEMENT_CALDAV_SUPPORTEDCOLLATION,
                                      NAMESPACE_CALDAV);
            DomUtil.setText(elem, collation);
            
            return elem;
        }
    }
}
