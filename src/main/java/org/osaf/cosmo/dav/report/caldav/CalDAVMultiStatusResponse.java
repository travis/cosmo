/**
 * 
 */
package org.osaf.cosmo.dav.report.caldav;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.jdom.Element;
import org.osaf.cosmo.dav.CosmoDavConstants;

/**
 * @author cyrusdaboo
 * 
 * This class extends the jackrabbit MultiStatusResponse by adding the ability
 * to return a calendar-data elements as needed by some CalDAV reports.
 * 
 * TODO Update to latest CalDAV draft approach of including calendar-data inside
 * of propstat.
 */
public class CalDAVMultiStatusResponse extends MultiStatusResponse {

    /**
     * The calendar data as text.
     */
    private String calendarData;

    /**
     * Indicates whether to use the old-style calendar-data element placement
     * 
     * TODO Remove once old-style clients are updated
     */
    private boolean oldStyle;

    public CalDAVMultiStatusResponse(DavResource resource,
            DavPropertyNameSet propNameSet, int propFindType) {
        super(resource, propNameSet, propFindType);
    }

    /**
     * @param calendarData
     *            The calendarData to set.
     */
    public void setCalendarData(String calendarData, boolean oldStyle) {
        this.calendarData = calendarData;
        this.oldStyle = oldStyle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jackrabbit.webdav.MultiStatusResponse#toXml()
     */
    public Element toXml() {

        // Generate calendar-data element if required
        Element cdata = null;
        if (calendarData != null) {
            cdata = new Element(CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                    CosmoDavConstants.NAMESPACE_CALDAV);
            cdata.setText(calendarData);
        }

        // TODO We currently support the old-style and new-style of
        // calendar-data placement. Eventually we should remove the old-style
        // when clients are updated.

        if ((cdata != null) && !oldStyle) {

            // Create DavProperty for this data
            CalendarDataProperty prop = new CalendarDataProperty(cdata);
            add(prop);
        }

        // Get standard multistatus response from superclass
        Element response = super.toXml();

        // Now add the calendar-data element if required
        if ((cdata != null) && oldStyle) {
            response.addContent(cdata);
        }

        return response;
    }

    private class CalendarDataProperty extends AbstractDavProperty {

        private Element calendarData;

        CalendarDataProperty(Element calendarData) {
            super(DavPropertyName.create(
                    CosmoDavConstants.ELEMENT_CALDAV_CALENDAR_DATA,
                    CosmoDavConstants.NAMESPACE_CALDAV), true);
            this.calendarData = calendarData;
        }

        public Object getValue() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.jackrabbit.webdav.property.AbstractDavProperty#toXml()
         */
        public Element toXml() {
            return calendarData;
        }
    }
}
