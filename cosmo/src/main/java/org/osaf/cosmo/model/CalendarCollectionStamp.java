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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.hibernate.validator.Timezone;
import org.osaf.cosmo.icalendar.ICalendarConstants;


/**
 * Represents a Calendar Collection.
 */
@Entity
@DiscriminatorValue("calendar")
@SecondaryTable(name="calendar_stamp", pkJoinColumns={
        @PrimaryKeyJoinColumn(name="stampid", referencedColumnName="id")})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CalendarCollectionStamp extends Stamp implements
        java.io.Serializable, ICalendarConstants {
    
    // CalendarCollection specific attributes
    public static final QName ATTR_CALENDAR_SUPPORTED_COMPONENT_SET = new QName(
            CalendarCollectionStamp.class, "supportedComponentSet");
    
    private Calendar calendar;
    private Calendar timezone;
    private String description;
    private String language;
 
    private static final long serialVersionUID = -6197756070431706553L;

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

    public Stamp copy(Item item) {
        Calendar tz = null;
        try {
            tz = new Calendar(timezone);
        } catch (Exception e) {
            throw new RuntimeException("Cannot copy timezone calendar", e);
        }

        CalendarCollectionStamp stamp = new CalendarCollectionStamp();
        stamp.language = language;
        stamp.description = description;
        stamp.timezone = tz;
        return stamp;
    }
    
    @Column(table="calendar_stamp", name="description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(table="calendar_stamp", name="language")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * @return calendar object representing timezone
     */
    @Column(table="calendar_stamp", name = "timezone", length=100000)
    @Type(type="calendar_clob")
    @Timezone
    public Calendar getTimezone() {
        return timezone;
    }

    /**
     * @return name of timezone if one is set
     */
    @Transient
    public String getTimezoneName() {
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
    public void setTimezone(Calendar timezone) {
        this.timezone = timezone;
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
