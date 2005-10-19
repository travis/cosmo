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
 * Interface for DAOs that manage calendar resources.
 *
 * Calendar objects and components as defined by iCalendar are
 * represented by {@link net.fortuna.ical4j} objects. A "calendar
 * object" (VCALENDAR) is a container for "calendar components"
 * (VEVENT, VTIMEZONE, etc).
 *
 * A group of related calendar objects can be stored in the repository
 * as a "calendar collection". A calendar collection contains
 * "calendar resources". A calendar resource stored within a calendar
 * collection contains one or more instances of a "main component" and
 * zero or more "support components". When more than one instance of
 * the main component exists, the component is said to be
 * "recurring". The "master instance" of the main component is the one
 * that defines the recurrence rule. The "exception instances" are
 * those that define recurrence ids. All instances of a recurring
 * component share the same uid.
 *
 * A calendar resource stored in a calendar collections is further
 * distinguished by the type of its main component. At the moment,
 * Cosmo only supports event resources (eventually task, note and
 * other resource types will be added). Timezones and alarms are
 * support components, thus there are no timezone or alarm resources.
 *
 * Calendar resources can also be stored outside of calendar
 * collections. Such a resource can contain many main components (each
 * main component having a unique uid, each instance of a particular
 * main component sharing the same uid) and many support
 * components. This allows related calendar objects to be stored
 * within a single calendar resource. This can be thought of as an
 * entire calendar collection packed within a single calendar
 * resource.
 */
public interface CalendarDao extends Dao {

    /**
     * Attaches a calendar object to a calendar resource, or updates a
     * calendar object already attached to a resource.
     *
     * @param path the repository path of the resource to which
     * the calendar object is to be attached
     * @param calendar the calendar object to attach
     *
     * @throws DataRetrievalFailureException if the item at the given
     * path is not found
     * @throws UnsupportedCalendarObjectException if the
     * <code>Calendar</code> does not contain any supported components
     * @throws RecurrenceException if recurrence is
     * improperly specified (no master instance, differing uids, etc)
     */
    public void storeCalendarObject(String path, Calendar calendar);

    /**
     * Returns the calendar object attached to a calendar resource.
     *
     * @param path the repository path of the calendar resource
     *
     * @throws DataRetrievalFailureException if the item at the given
     * path is not found
     */
    public Calendar getCalendarObject(String path);
}
