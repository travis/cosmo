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
package org.osaf.cosmo.service;

import java.util.Set;
import java.util.TimeZone;

import org.osaf.cosmo.dao.NoSuchResourceException;
import org.osaf.cosmo.model.CalendarCollectionResource;
import org.osaf.cosmo.model.CalendarResource;
import org.osaf.cosmo.model.Resource;
import org.osaf.cosmo.model.Ticket;

/**
 * Interface for services that manage access to user home
 * directories.
 */
public interface HomeDirectoryService extends Service {

    /**
     * Returns the resource at the specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public Resource getResource(String path);

    /**
     * Removes the resource at the specified client path.
     */
    public void removeResource(String path);

    /**
     * Creates a ticket on the resource at the specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public void grantTicket(String path, Ticket ticket);

    /**
     * Removes the identified ticket from the resource at the
     * specified client path.
     *
     * @throws NoSuchResourceException if a resource does not exist at
     * the specified path
     */
    public void revokeTicket(String path, String id);

    /**
     * Creates a calendar collection resource at the given path
     * 
     * @param displayName the human readable name
     * @param path the path where the collection should be created
     */
    public void createCalendarCollection(String displayName, String path);
    
    /**
     * Returns all the calendar collection resources within a given collection. 
     * 
     * @param path path to the collection
     * @param recurse will search recursively if is true, otherwise
     *        just returns the calendar collections directly within the given 
     *        collection
     * @return
     */
    public Set<CalendarCollectionResource> getCalendarCollectionResources(
            String path, boolean recurse);

    /**
     * Returns the CalendarResource which has the event with the given id within a particular
     * calendar collection 
     * 
     * @param pathToCalendarCollection the path to the calendar collection in which the desired 
     *                                 event is located
     * @param id
     * @return
     */
    public CalendarResource getCalendarResourceByEventId(String pathToCalendarCollection, String id);

    /**
     * Returns all the CalendarResources which contain events that fall within the specified date range.
     * 
     * @param pathToCalendarCollection the path to the calendar collection in which the desired 
     *                                 events are located
     * @param utcStartTime             The start time, in UTC
     * @param utcEndTime               The end time,   in UTC
     * @param timezone                 The timezone to use when resolving the UTC of timezone-less events
     * @return                       
     */
    public Set<CalendarResource> getCalendarResourcesInDateRange(
            String pathToCalendarCollection, long utcStartTime,
            long utcEndTime, TimeZone timezone);
}
