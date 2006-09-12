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
