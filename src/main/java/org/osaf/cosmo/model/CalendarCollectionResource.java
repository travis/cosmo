/*
 * Copyright 2005 Open Source Applications Foundation
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
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.osaf.cosmo.CosmoConstants;

/**
 * Extends {@link CollectionResource} to represent a collection of
 * calendar resources.
 */
public class CalendarCollectionResource extends CollectionResource {

    private String description;
    private String language;
    private Calendar calendar;

    /**
     */
    public CalendarCollectionResource() {
        super();
    }

    /**
     */
    public String getDescription() {
        return description;
    }

    /**
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     */
    public String getLanguage() {
        return language;
    }

    /**
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Returns a {@link net.fortuna.ical4j.model.Calendar}
     * representing the content of the calendar resources contained
     * within this collection. This method will
     * only parse the resources once, returning the same calendar
     * instance on subsequent invocations.
     */
    public Calendar getCalendar()
        throws IOException, ParserException {
        if (calendar == null) {
            calendar = loadCalendarResources();
        }
        return calendar;
    }

    /**
     */
    public boolean equals(Object o) {
        if (! (o instanceof CalendarCollectionResource)) {
            return false;
        }
        CalendarCollectionResource it = (CalendarCollectionResource) o;
        return new EqualsBuilder().
            appendSuper(super.equals(o)).
            append(description, it.description).
            append(language, it.language).
            isEquals();
    }

    /**
     */
    public int hashCode() {
        return new HashCodeBuilder(13, 15).
            appendSuper(super.hashCode()).
            append(description).
            append(language).
            toHashCode();
    }

    /**
     */
    public String toString() {
        return new ToStringBuilder(this).
            appendSuper(super.toString()).
            append("description", description).
            append("language", language).
            toString();
    }

    private Calendar loadCalendarResources()
        throws IOException, ParserException {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // extract the events and timezones for each child event and
        // add them to the collection calendar object
        // index the timezones by tzid so that we only include each tz
        // once. if for some reason different event resources have
        // different tz definitions for a tzid, *shrug* last one wins
        // for this same reason, we use a single calendar builder/time
        // zone registry
        HashMap tzIdx = new HashMap();
        Resource resource = null;
        EventResource event = null;
        Calendar childCalendar = null;
        Component tz = null;
        Property tzId = null;
        for (Iterator i=getResources().iterator(); i.hasNext();) {
            resource = (Resource) i.next();
            if (! (resource instanceof EventResource)) {
                continue;
            }
            event = (EventResource) resource;
            childCalendar = event.getCalendar();

            for (Iterator j=childCalendar.getComponents().
                     getComponents(Component.VEVENT).iterator();
                 j.hasNext();) {
                calendar.getComponents().add((Component) j.next());
            }

            for (Iterator j=childCalendar.getComponents().
                     getComponents(Component.VTIMEZONE).iterator();
                 j.hasNext();) {
                tz = (Component) j.next();
                tzId = tz.getProperties().getProperty(Property.TZID);
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
}
