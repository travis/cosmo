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
public class CalendarPropertyIndex extends BaseModelObject implements
        java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5482828614822186436L;
    private String name;
    private String value;
    private Item item;

    // Constructors

    /** default constructor */
    public CalendarPropertyIndex() {
    }

    
    
    public Item getItem() {
        return item;
    }



    public void setItem(Item item) {
        this.item = item;
    }



    public String getName() {
        return name;
    }



    public void setName(String name) {
        this.name = name;
    }



    public String getValue() {
        return value;
    }



    public void setValue(String value) {
        this.value = value;
    }



    public CalendarPropertyIndex copy() {
        CalendarPropertyIndex index = new CalendarPropertyIndex();
        index.setName(getName());
        index.setValue(getValue());
        return index;
    }

    /** */
    public String toString() {
        return new ToStringBuilder(this).
            append("name", getName()).
            append("value", getValue()).
            toString();
    }
}
