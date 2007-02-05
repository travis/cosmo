/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.osaf.cosmo.calendar.query;

import java.text.ParseException;

import net.fortuna.ical4j.model.component.VTimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

/**
 * Represents the CALDAV:text-match element. From sec 9.6.5:
 * 
 * Name: text-match
 * 
 * Namespace: urn:ietf:params:xml:ns:caldav
 * 
 * Purpose: Specifies a substring match on a property or parameter value.
 * 
 * Definition:
 * 
 * <!ELEMENT text-match (#PCDATA)> PCDATA value: string
 * 
 * <!ATTLIST text-match caseless (yes | no) #IMPLIED 
 * negate-condition (yes | no) "no">
 */
public class TextMatchFilter implements DavConstants, CaldavConstants {
    private boolean isNegateCondition = false;

    private boolean isCaseless = false;

    private String value = null;

    public TextMatchFilter(String value) {
        this.value = value;
    }
    
    /**
     * Construct a TextMatchFilter object from a DOM Element
     * @param element
     * @throws ParseException
     */
    public TextMatchFilter(Element element) throws ParseException {
        // Element data is string to match
        // TODO: do we need to do this replacing??
        value = DomUtil.getTextTrim(element).replaceAll("'", "''");
        
        // Check attribute for caseless
        String caseless =
            DomUtil.getAttribute(element, ATTR_CALDAV_CASELESS,null);
                    
        String negateCondition = 
            DomUtil.getAttribute(element, ATTR_CALDAV_NEGATE_CONDITION,null);
                            
        if ((caseless == null) || ! VALUE_YES.equals(caseless))
            isCaseless = false;
        else
            isCaseless = true;
        
        if((negateCondition == null) || !VALUE_YES.equals(negateCondition))
            isNegateCondition = false;
        else
            isNegateCondition = true;
    }

    public TextMatchFilter() {
    }

    public boolean isCaseless() {
        return isCaseless;
    }

    public void setCaseless(boolean isCaseless) {
        this.isCaseless = isCaseless;
    }

    public boolean isNegateCondition() {
        return isNegateCondition;
    }

    public void setNegateCondition(boolean isNegateCondition) {
        this.isNegateCondition = isNegateCondition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("value", value).
            append("isCaseless", isCaseless).
            append("isNegateCondition", isNegateCondition).
            toString();
    }
}
