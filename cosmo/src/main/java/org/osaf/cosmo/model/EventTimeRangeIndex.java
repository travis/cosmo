package org.osaf.cosmo.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.Index;

@Embeddable
public class EventTimeRangeIndex {
    
    private String startDate = null;
    private String endDate = null;
    private Boolean isFloating = null;
    private Boolean isRecurring = null;
    
    /**
     * The end date of the event.  If the event is recurring, the
     * value is the earliest start date for the recurring series.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format is one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * @return start date of the event
     */
    @Column(table="event_stamp", name = "enddate", length=16)
    @Index(name="idx_enddt")
    public String getDateEnd() {
        return endDate;
    }

  
    /**
     * The end date of the event.  If the event is recurring, the
     * value is the latest end date for the recurring series.
     * If the recurring event is infinite, the value will be a 
     * String that represents infinity.
     * If the date has a timezone, the date will be converted
     * to UTC.  The format must be one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * Z-TIME-INFINITY<br/>
     * @param endDate end date of the event
     */
    public void setDateEnd(String endDate) {
        this.endDate = endDate;
    }
    
    /**
     * The start date of the event.  If the event is recurring, the
     * value is the earliest start date for the recurring series. 
     * If the date has a timezone, the date will be converted
     * to UTC.  The format is one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * 
     * @return start date of the event
     */
    @Column(table="event_stamp", name = "startdate", length=16)
    @Index(name="idx_startdt")
    public String getDateStart() {
        return startDate;
    }

    /**
     * The start date of the event.  If the event is recurring, the
     * value is the earliest start date for the recurring series.  
     * If the date has a timezone, the date will be converted
     * to UTC.  The format must be one of:
     * <p>
     * 20070101<br/>
     * 20070101T100000<br/>
     * 20070101T100000Z<br/>
     * @param startDate start date of the event
     */
    public void setDateStart(String startDate) {
        this.startDate = startDate;
    }

    @Column(table="event_stamp", name = "isfloating")
    @Index(name="idx_floating")
    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }
    
    @Column(table="event_stamp", name = "isrecurring")
    @Index(name="idx_recurring")
    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
}
