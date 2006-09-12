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

package org.osaf.cosmo.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Represents an index for a CalendarItem.
 * In order to query CalendarItems efficiently, there has to be some
 * way to index each item.  CalendarIndex represents a time-range
 * based index for a CalendarItem.  Many CalendarIndexes can
 * be associated with a single CalendarItem.
 */
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

    // Constructors

    /** default constructor */
    public CalendarTimeRangeIndex() {
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsFloating() {
        return isFloating;
    }

    public void setIsFloating(Boolean isFloating) {
        this.isFloating = isFloating;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

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
