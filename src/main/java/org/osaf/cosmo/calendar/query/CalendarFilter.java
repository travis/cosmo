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
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.osaf.cosmo.dav.caldav.CaldavConstants;
import org.w3c.dom.Element;

/**
 * Filter for querying calendar events. The structure of this filter is based on
 * the structure of the <CALDAV:filter> element.
 * 
 * See section 9.6 of the CalDAV spec
 */
public class CalendarFilter implements CaldavConstants {

    private ComponentFilter filter;

    public CalendarFilter() {
    }

    public CalendarFilter(Element element) throws ParseException {
        this(element, null);
    }
    
    /**
     * Construct a CalendarFilter object from a DOM Element
     * @param element
     * @throws ParseException
     */
    public CalendarFilter(Element element, VTimeZone timezone) throws ParseException {
        // Can only have a single comp-filter element
        ElementIterator i = DomUtil.getChildren(element,
                ELEMENT_CALDAV_COMP_FILTER, NAMESPACE_CALDAV);
        if (!i.hasNext()) {
            throw new ParseException(
                    "CALDAV:filter must contain a comp-filter", -1);
        }

        Element child = i.nextElement();

        if (i.hasNext()) {
            throw new ParseException(
                    "CALDAV:filter can contain only one comp-filter", -1);
        }

        // Create new component filter and have it parse the element
        filter = new ComponentFilter(child, timezone);
    }

    /**
     * A CalendarFilter has exactly one ComponentFilter
     * 
     * @return
     */
    public ComponentFilter getFilter() {
        return filter;
    }

    /**
     * @param filter
     */
    public void setFilter(ComponentFilter filter) {
        this.filter = filter;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("filter", filter).
            toString();
    }
}
