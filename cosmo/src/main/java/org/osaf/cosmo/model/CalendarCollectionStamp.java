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
package org.osaf.cosmo.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.hibernate.validator.Timezone;
import org.osaf.cosmo.icalendar.ICalendarConstants;


/**
 * Represents a Calendar Collection.
 */
@Entity
@DiscriminatorValue("calendar")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CalendarCollectionStamp extends Stamp implements
        java.io.Serializable, ICalendarConstants {
    
    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_SUPPORTED_COMPONENT_SET = new QName(
            CalendarCollectionStamp.class, "supportedComponentSet");
    
    public static final QName ATTR_CALENDAR_TIMEZONE = new QName(
            CalendarCollectionStamp.class, "timezone");
    
    public static final QName ATTR_CALENDAR_DESCRIPTION = new QName(
            CalendarCollectionStamp.class, "description");
    
    public static final QName ATTR_CALENDAR_LANGUAGE = new QName(
            CalendarCollectionStamp.class, "language");
    
    private Calendar calendar;
    
    /** default constructor */
    public CalendarCollectionStamp() {
    }
    
    @Transient
    public String getType() {
        return "calendar";
    }
    
    public CalendarCollectionStamp(CollectionItem collection) {
        this();
        setItem(collection);
    }

    public Stamp copy() {
        CalendarCollectionStamp stamp = new CalendarCollectionStamp();
        return stamp;
    }
    
    @Transient
    public String getDescription() {
        // description stored as StringAttribute on Item
        StringAttribute descAttr = (StringAttribute) getAttribute(ATTR_CALENDAR_DESCRIPTION);
        if(descAttr!=null)
            return descAttr.getValue();
        else
            return null;
    }

    public void setDescription(String description) {
        // description stored as StringAttribute on Item
        StringAttribute descAttr = (StringAttribute) getAttribute(ATTR_CALENDAR_DESCRIPTION);
        if(descAttr==null && description!=null) {
            descAttr = new StringAttribute(ATTR_CALENDAR_DESCRIPTION,description);
            addAttribute(descAttr);
            return;
        }
        if(description==null)
            removeAttribute(ATTR_CALENDAR_DESCRIPTION);
        else
            descAttr.setValue(description);
    }

    @Transient
    public String getLanguage() {
        // language stored as StringAttribute on Item
        StringAttribute langAttr = (StringAttribute) getAttribute(ATTR_CALENDAR_LANGUAGE);
        if(langAttr!=null)
            return langAttr.getValue();
        else
            return null;
    }

    public void setLanguage(String language) {
        // language stored as StringAttribute on Item
        StringAttribute langAttr = (StringAttribute) getAttribute(ATTR_CALENDAR_LANGUAGE);
        if(langAttr==null && language!=null) {
            langAttr = new StringAttribute(ATTR_CALENDAR_LANGUAGE,language);
            addAttribute(langAttr);
            return;
        }
        if(language==null)
            removeAttribute(ATTR_CALENDAR_LANGUAGE);
        else
            langAttr.setValue(language);
    }

    /**
     * @return calendar object representing timezone
     */
    @Timezone
    @Transient
    public Calendar getTimezoneCalendar() {
        // calendar stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_CALENDAR_TIMEZONE);
        if(calAttr!=null)
            return calAttr.getValue();
        else
            return null;
    }

    /**
     * @return timezone if present
     */
    @Transient
    public TimeZone getTimezone() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null)
            return null;
        VTimeZone vtz = (VTimeZone) timezone.getComponents().getComponent(Component.VTIMEZONE);
        return new TimeZone(vtz);
    }
    
    /**
     * @return name of timezone if one is set
     */
    @Transient
    public String getTimezoneName() {
        Calendar timezone = getTimezoneCalendar();
        if (timezone == null)
            return null;
        return timezone.getComponents().getComponent(Component.VTIMEZONE).
            getProperties().getProperty(Property.TZID).getValue();
    }

    /**
     * Set timezone definition for calendar.
     * 
     * @param timezone
     *            timezone definition in ical format
     */
    public void setTimezoneCalendar(Calendar timezone) {
        // timezone stored as ICalendarAttribute on Item
        ICalendarAttribute calAttr = (ICalendarAttribute) getAttribute(ATTR_CALENDAR_TIMEZONE);
        if(calAttr==null && timezone!=null) {
            calAttr = new ICalendarAttribute(ATTR_CALENDAR_TIMEZONE, timezone);
            addAttribute(calAttr);
        }
        
        if(timezone==null)
            removeAttribute(ATTR_CALENDAR_TIMEZONE);
        else
            calAttr.setValue(timezone);
    }

    /**
     * Returns a <code>Calendar</code> representing all of the events
     * in this calendar collection. This method will only calculate
     * the aggregate calendar once; subsequent recalls will return the
     * same calendar unless it is dumped by another method.
     */
    @Transient
    public Calendar getCalendar()
        throws IOException, ParserException {
        if (calendar == null) {
            calendar = loadCalendar();
        }
        return calendar;
    }

    private Calendar loadCalendar()
        throws IOException, ParserException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // extract the supported calendar components for each child item and
        // add them to the collection calendar object.
        // index the timezones by tzid so that we only include each tz
        // once. if for some reason different calendar items have
        // different tz definitions for a tzid, *shrug* last one wins
        // for this same reason, we use a single calendar builder/time
        // zone registry.
        HashMap tzIdx = new HashMap();
        Set<EventStamp> eventStamps = getEventStamps();
        for (EventStamp eventStamp : eventStamps) {
            
            Calendar childCalendar = eventStamp.getCalendar();

            for (Iterator j=childCalendar.getComponents().
                     getComponents(Component.VEVENT).iterator();
                 j.hasNext();) {
                calendar.getComponents().add((Component) j.next());
            }

            for (Iterator j=childCalendar.getComponents().
                     getComponents(Component.VTIMEZONE).iterator();
                 j.hasNext();) {
                Component tz = (Component) j.next();
                Property tzId = tz.getProperties().getProperty(Property.TZID);
                if (! tzIdx.containsKey(tzId.getValue())) {
                    tzIdx.put(tzId.getValue(), tz);
                }
            }
        }

        for (Iterator i=tzIdx.values().iterator(); i.hasNext();) {
            calendar.getComponents().add((Component) i.next());
        }

        return calendar;
    }
    
    /**
     * Return a set of all EventStamps for the collection's children.
     * @return set of EventStamps contained in children
     */
    @Transient
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
