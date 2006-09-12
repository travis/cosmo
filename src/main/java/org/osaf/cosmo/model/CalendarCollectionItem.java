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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.osaf.cosmo.CosmoConstants;
import org.osaf.cosmo.calendar.util.CalendarBuilderDispenser;

/**
 * Extends {@link CollectionItem} to represent a collection containing calendar
 * items
 */
public class CalendarCollectionItem extends CollectionItem {

    /**
     * 
     */
    private static final long serialVersionUID = -5482399651067090453L;
    
    // possible component types
    public static final String COMPONENT_VEVENT = "VEVENT";
    public static final String COMPONENT_VTODO = "VTODO";
    public static final String COMPONENT_VJOURNAL = "VJOURNAL";
    public static final String COMPONENT_FREEBUSY = "VFREEBUSY";

    // CalendarCollection specific attributes
    public static final String ATTR_CALENDAR_UID = "calendar:uid";
    public static final String ATTR_CALENDAR_DESCRIPTION = "calendar:description";
    public static final String ATTR_CALENDAR_LANGUAGE = "calendar:language";
    public static final String ATTR_CALENDAR_TIMEZONE = "calendar:timezone";
    public static final String ATTR_CALENDAR_SUPPORTED_COMPONENT_SET = "calendar:supportedComponentSet";

    // Default set of supported components used to intialize
    // calendar collection 
    protected static final String[] defaultSupportedComponents = 
        new String[] { COMPONENT_VEVENT };
    
    private Calendar calendar;
    private Calendar timezone;

    public CalendarCollectionItem() {
        // set default supported components
        this.setSupportedComponents(getDefaultSupportedComponentSet());
    }

    public String getDescription() {
        return (String) getAttributeValue(ATTR_CALENDAR_DESCRIPTION);
    }

    public void setDescription(String description) {
        addStringAttribute(ATTR_CALENDAR_DESCRIPTION, description);
    }

    public String getLanguage() {
        return (String) getAttributeValue(ATTR_CALENDAR_LANGUAGE);
    }

    public void setLanguage(String language) {
        addStringAttribute(ATTR_CALENDAR_LANGUAGE, language);
    }

    /**
     * Get set of supported calendar components.
     * 
     * @return immutable set of supported components
     */
    public Set getSupportedComponents() {
        return (Set) getAttributeValue(ATTR_CALENDAR_SUPPORTED_COMPONENT_SET);
    }

    /**
     * Set supported calendar components.
     * 
     * @param supportedComponents
     *            set of supported components
     */
    public void setSupportedComponents(Set<String> supportedComponents) {
        setAttribute(ATTR_CALENDAR_SUPPORTED_COMPONENT_SET, supportedComponents);
    }

    /**
     * Parse timezone definition for calendar and return as a Calendar object
     * 
     * @return calendar object representing timezone
     */
    public Calendar getTimezone() {
        if (timezone == null) {
            String strTimezone =
                (String) getAttributeValue(ATTR_CALENDAR_TIMEZONE);
            if (strTimezone == null)
                return null;
            try {
                CalendarBuilder builder =
                    CalendarBuilderDispenser.getCalendarBuilder();
                timezone = builder.build(new StringReader(strTimezone));
            } catch (IOException e) {
                throw new ModelConversionException("unable to build calendar object from timezone attribute", e);
            } catch (ParserException e) {
                throw new ModelConversionException("unable to parse timezone attribute", e);
            }
        }
        return timezone;
    }

    /**
     * Set timezone definition for calendar.
     * 
     * @param timezone
     *            timezone definition in ical format
     */
    public void setTimezone(String timezone) {
            addStringAttribute(ATTR_CALENDAR_TIMEZONE, timezone);
    }

    /**
     * Returns a <code>Calendar</code> representing all of the events
     * in this calendar collection. This method will only calculate
     * the aggregate calendar once; subsequent recalls will return the
     * same calendar unless it is dumped by another method.
     */
    public Calendar getCalendar()
        throws IOException, ParserException {
        if (calendar == null) {
            calendar = loadCalendar();
        }
        return calendar;
    }

    /**
     * Determines if the given <code>Calendar</code> contains at least
     * one component supported by this collection.
     */
    public boolean supportsCalendar(Calendar calendar) {
        for (Iterator<String> i=getSupportedComponents().iterator();
             i.hasNext();) {
            if (! calendar.getComponents().getComponents(i.next()).isEmpty())
                return true;
        }
        return false;
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
        for (Iterator<Item> i=getChildren().iterator(); i.hasNext();) {
            Item child = i.next();
            // XXX process other child items based on the collection's
            // supported calendar component set
            if (! (child instanceof CalendarEventItem)) {
                continue;
            }
            CalendarEventItem event = (CalendarEventItem) child;
            Calendar childCalendar = event.getCalendar();

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
    
    protected static Set<String> getDefaultSupportedComponentSet() {
        HashSet<String> defaultSet = new HashSet<String>();
        for(String comp: defaultSupportedComponents)
            defaultSet.add(comp);
        return defaultSet;
    }
}
