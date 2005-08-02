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

import net.fortuna.ical4j.model.Calendar;

/**
 * Dao interface for calendar related activities.
 */
public interface CalendarDao extends DAO {

    /**
     * Creates a calendar underneath the item at the given path.
     *
     * @param path the repository path of the parent of the new
     * collection
     * @param name the name of the new collection
     */
    public void createCalendar(String path, String name);

    /**
     * Returns true if a calendar exists at the given path,
     * false otherwise
     *
     * @param path the repository path to test for existence
     */
    public boolean existsCalendar(String path);

    /**
     * Removes the calendar at the given path and its
     * entire subtree.
     *
     * @param path the repository path of the calendar to be deleted
     */
    public void deleteCalendar(String path);

    /**
     * Creates a calendar resource in the repository. A calendar
     * resource contains one or more calendar components.
     *
     * The only supported "top level" calendar component is
     * event. Events are typically associated with timezones and
     * alarms. Recurring events are represented as multiple calendar
     * components: one "master" event that defines the recurrence
     * rule, and zero or more "exception" events. All of these
     * components share a uid.
     *
     * Journal, todo and freebusy components are not supported. These
     * components will be ignored.
     *
     * @param path the repository path of the parent of the new
     * event resource
     * @param name the name of the new event resource
     * @param event the <code>Calendar</code> containing events,
     * timezones and alarms
     *
     * @throws {@link UnsupportedFeatureException} if the
     * <code>Calendar</code> does not contain an event.
     */
    public void createCalendarResource(String path,
                                       String name,
                                       Calendar calendar);
}
