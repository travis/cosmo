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
package org.osaf.cosmo.dao;

import java.util.Set;

import javax.jcr.Node;

import net.fortuna.ical4j.model.Calendar;

/**
 * Dao interface for calendar related activities.
 */
public interface CalendarDao extends Dao {

    /**
     * Creates a calendar collection in the repository.
     *
     * @param node the <code>Node</code> to which the calendar
     * collection will be attached.
     * @param name the name of the new collection
     */
    public void createCalendarCollection(Node node, String name);

    /**
     * Attaches a calendar object to a node in the repository, or
     * updates an existing one.

     * A calendar object contains one or more calendar components. The
     * only supported "top level" calendar component is event. Events
     * are typically associated with timezones and alarms. Recurring
     * events are represented as multiple calendar components: one
     * "master" event that defines the recurrence rule, and zero or
     * more "exception" events. All of these components share a uid.
     *
     * Journal, todo and freebusy components are not supported. These
     * components will be ignored.
     *
     * @param node the <code>Node</code> to which the calendar object
     * will be attached
     * @param event the <code>Calendar</code> containing events,
     * timezones and alarms
     *
     * @throws {@link UnsupportedFeatureException} if the
     * <code>Calendar</code> does not contain an event.
     * @throws {@link RecurrenceFeatureException} if a recurring event
     * is improperly specified (no master event, etc)
     */
    public void storeCalendarObject(Node node, Calendar calendar);

    /**
     */
    public Calendar getCalendarObject(Node node);
}
