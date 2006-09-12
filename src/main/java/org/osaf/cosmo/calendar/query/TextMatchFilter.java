/*
 * Copyright (c) 2006 SimDesk Technologies, Inc.  All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * SimDesk Technologies, Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with SimDesk Technologies.
 *
 * SIMDESK TECHNOLOGIES MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT
 * THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.  SIMDESK TECHNOLOGIES
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
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
