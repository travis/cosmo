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

/**
 * Dao interface for calendar related activities.
 *
 * A calendar collection is a container for calendars. A calendar
 * collection may contain any number of calendars but may not contain
 * other calendar collections.
 */
public interface CalendarDao extends DAO {

    /**
     * Creates a calendar collection within which calendars may be
     * created.
     *
     * @param path the repository path of the parent node of the new
     * collection
     * @param name the name of the new collection
     */
    public void createCalendarCollection(String path, String name);

    /**
     * Returns true if a calendar collection exists at the given path,
     * false otherwise
     *
     * @param path the repository path to test for existence
     */
    public boolean existsCalendarCollection(String path);

    /**
     * Removes the calendar collection at the given path and its
     * entire subtree.
     *
     * @param path the repository path of the calendar collection to
     * remove
     */
    public void deleteCalendarCollection(String path);

}
