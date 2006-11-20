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
package org.osaf.cosmo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Index;
import org.hibernate.validator.NotNull;

/**
 * Represents an index for a CalendarItem.
 * In order to query CalendarItems efficiently, there has to be some
 * way to index each item.  CalendarIndex represents a time-range
 * based index for a CalendarItem.  Many CalendarIndexes can
 * be associated with a single CalendarItem.
 */
@Entity
@Table(name="cal_timerange_index")
public class CalendarTimeRangeIndex extends BaseModelObject implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 532005810963150124L;

    // Fields
    private String startDate;

    private String endDate;

    private Boolean isRecurring;

    private Boolean isFloating;

    private String type;

    private Item item;
    
    private EventStamp eventStamp = null;

    // Constructors

    /** default constructor */
    public CalendarTimeRangeIndex() {
    }

    @Column(name = "enddate", length=16)
    @Index(name="idx_enddt")
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @Column(name = "isfloating")
    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    @Column(name = "isrecurring")
    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="itemid")
    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="eventstampid")
    public EventStamp getEventStamp() {
        return eventStamp;
    }

    public void setEventStamp(EventStamp eventStamp) {
        this.eventStamp = eventStamp;
    }

    @Column(name = "startdate", length=16)
    @Index(name="idx_startdt")
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Column(name = "componenttype", nullable = false, length=255)
    @NotNull
    @Index(name="idx_comptype")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    public CalendarTimeRangeIndex copy() {
        CalendarTimeRangeIndex index = new CalendarTimeRangeIndex();
        index.setEndDate(endDate);
        index.setIsFloating(isFloating);
        index.setIsRecurring(isRecurring);
        index.setStartDate(startDate);
        index.setType(type);
        return index;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("type", getType()).
            append("startDate", getStartDate()).
            append("endDate", getEndDate()).
            append("isFloating", getIsFloating()).
            append("isRecurring", getIsRecurring()).
            toString();
    }
}
