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
package org.osaf.cosmo.model.mock;

import net.fortuna.ical4j.model.Calendar;

import org.osaf.cosmo.model.AvailabilityItem;
import org.osaf.cosmo.model.ICalendarItem;
import org.osaf.cosmo.model.Item;

/**
 * Extends {@link ICalendarItem} to represent a VAVAILABILITY item.
 */
public class MockAvailabilityItem extends MockICalendarItem implements AvailabilityItem {

  
    public MockAvailabilityItem() {
    }

    @Override
    public Item copy() {
        AvailabilityItem copy = new MockAvailabilityItem();
        copyToItem(copy);
        return copy;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAvailabilityItem#getAvailabilityCalendar()
     */
    public Calendar getAvailabilityCalendar() {
        return getCalendar();
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceAvailabilityItem#setAvailabilityCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void setAvailabilityCalendar(Calendar calendar) {
        setCalendar(calendar);
    }
    
}
