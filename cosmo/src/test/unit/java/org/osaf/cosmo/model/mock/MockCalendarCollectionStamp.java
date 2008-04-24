/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;

import org.osaf.cosmo.hibernate.validator.Timezone;
import org.osaf.cosmo.icalendar.ICalendarConstants;
import org.osaf.cosmo.model.CalendarCollectionStamp;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.EventStamp;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.QName;
import org.osaf.cosmo.model.Stamp;


/**
 * Represents a Calendar Collection.
 */
public class MockCalendarCollectionStamp extends MockStamp implements
        java.io.Serializable, ICalendarConstants, CalendarCollectionStamp {
    
    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_TIMEZONE = new MockQName(
            CalendarCollectionStamp.class, "timezone");
    
    public static final QName ATTR_CALENDAR_DESCRIPTION = new MockQName(
            CalendarCollectionStamp.class, "description");
    
    public static final QName ATTR_CALENDAR_LANGUAGE = new MockQName(
            CalendarCollectionStamp.class, "language");
    
    private transient Calendar calendar;
    
    /** default constructor */
    public MockCalendarCollectionStamp() {
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getType()
     */
    public String getType() {
        return "calendar";
    }
    
    public MockCalendarCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }

    public Stamp copy() {
        CalendarCollectionStamp stamp = new MockCalendarCollectionStamp();
        return stamp;
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getDescription()
     */
    public String getDescription() {
        // description stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        // description stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_DESCRIPTION, description);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getLanguage()
     */
    public String getLanguage() {
        // language stored as StringAttribute on Item
        return MockStringAttribute.getValue(getItem(), ATTR_CALENDAR_LANGUAGE);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#setLanguage(java.lang.String)
     */
    public void setLanguage(String language) {
        // language stored as StringAttribute on Item
        MockStringAttribute.setValue(getItem(), ATTR_CALENDAR_LANGUAGE, language);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezoneCalendar()
     */
    @Timezone
    public Calendar getTimezoneCalendar() {
        // calendar stored as ICalendarAttribute on Item
        return MockICalendarAttribute.getValue(getItem(), ATTR_CALENDAR_TIMEZONE);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezone()
     */
    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null)
            return null;
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }
    
    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#getTimezoneName()
     */
    public String getTimezoneName() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null)
            return null;
        return timezone.getComponents().getComponent(Component.VTIMEZONE).
            getProperties().getProperty(Property.TZID).getValue();
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.copy.InterfaceCalendarCollectionStamp#setTimezoneCalendar(net.fortuna.ical4j.model.Calendar)
     */
    public void setTimezoneCalendar(Calendar timezone) {
        // timezone stored as ICalendarAttribute on Item
        MockICalendarAttribute.setValue(getItem(), ATTR_CALENDAR_TIMEZONE, timezone);
    }

    /* (non-Javadoc)
     * @see org.osaf.cosmo.model.CalendarCollectionStamp#getEventStamps()
     */
    public Set<EventStamp> getEventStamps() {
        Set<EventStamp> events = new HashSet<EventStamp>();
        for (Iterator<Item> i= ((CollectionItem) getItem()).getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            Stamp stamp = child.getStamp(EventStamp.class);
            if(stamp!=null)
                events.add((EventStamp) stamp);
        }
        return events;
    }
    
    /**
     * Return CalendarCollectionStamp from Item
     * @param item
     * @return CalendarCollectionStamp from Item
     */
    public static CalendarCollectionStamp getStamp(Item item) {
        return (CalendarCollectionStamp) item.getStamp(CalendarCollectionStamp.class);
    }
}
