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
package org.osaf.cosmo.rpc;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osaf.cosmo.rpc.model.Calendar;
import org.osaf.cosmo.rpc.model.CosmoDate;
import org.osaf.cosmo.rpc.model.Event;
import org.osaf.cosmo.rpc.model.RecurrenceRule;

/**
 * This is the interface that is exposed to web-clients via json-rpc or other 
 * rpc protocols. 
 * 
 * Notice that none of these method signatures specify the username - the implementation 
 * must have a way of knowing what user is requesting the calendar - either by using a
 * ThreadLocal variable, having a differnet ScoobyService for each user or by some 
 * other method.
 * 
 * @author bobbyrullo
 */
public interface RPCService {

    /**
     * Returns all the events between two given dates sorted by start date ascending.
     * 
     * @param calendarPath the path to the calendar to search
     * @param utcStartTime returned events must have start dates greater than or equal to
     *        this date
     * @param utcEndTime returned events must have start dates that are less than or equal
     *        to this date
     * @return the array of events between the specified dates. If there are no events 
     *         found an empty array is returned
     * @throws RPCException
     */
    public Event[] getEvents(String collectionUid, long utcStartTime, long utcEndTime) 
        throws RPCException;

    public Event[] getEvents(String collectionUid, long utcStartTime, long utcEndTime, String ticket) 
        throws RPCException;

    /**
     * Persists an event to the calendar. If there is no id specified in the given Event
     * a new Event object will be persisted and the id will be returned. If there is an
     * id, the event with the same id in the same calendar will be updated. If there is an 
     * id but there is no matching event, a ScoobyService exception will be thrown
     * 
     * @param collectionUid the path to the calendar
     * @param event the event to save
     * @return the id of the event
     */
    public String saveEvent(String collectionUid, Event event) throws RPCException;
    
    public String saveEvent(String collectionUid, Event event, String ticket) throws RPCException;
    
    /**
     * Removes an event with the specified id from the calendar.
     * 
     * @param collectionUid the path to the calendar containing the Event to be deleted
     * @param id the id of the Event to be deleted
     * @throws RPCException
     */
    public void removeEvent(String collectionUid, String id) throws RPCException;
    
    public void removeEvent(String collectionUid, String id, String ticket) throws RPCException;
    
    /**
     * Returns the Event with the given id. 
     * 
     * @param collectionUid the path to the calendar containing the desired Event
     * @param id the id of the desired Event
     * @return the Event with the given id, or null if none found.
     * @throws RPCException
     */
    public Event getEvent(String collectionUid, String id) throws RPCException;
    
    public Event getEvent(String collectionUid, String id, String ticket) throws RPCException;
    
    
    /**
     * Returns all the calendars for the current user.
     *  
     * @return an array of CalendarMetadata's, one for each calendar that exists
     * for the current user
     * @throws RPCException
     */
    public Calendar[] getCalendars(HttpServletRequest request) throws RPCException;
    
    /**
     * Returns the calendar with uid collectionUid.
     * 
     * @param collectionUid the uid of the calendar to return
     * @param ticket the key of a ticket that provides access to the calendar
     * @return the calendar corresponding to collectionUid 
     * @throws RPCException 
     */
    public Calendar getCalendar(String collectionUid, 
            HttpServletRequest request) throws RPCException;
    
    public Calendar getCalendar(String collectionUid, String ticket, 
            HttpServletRequest request) throws RPCException;
    
    /**
     * Creates a calendar with the given name
     * @param displayName the name as it is displayed to the user
     * @param path the name as it is displayed to the user
     * @throws RPCException
     */
    public String createCalendar(String displayName)
            throws RPCException;
    
    /**
     * Removes the calendar with the given path
     * @param calendarPath
     */
    public void removeCalendar(String uid) throws RPCException;
    
    /**
     * Returns a given preference for current user.
     * @param preferenceName the name of the preference to get
     */
    public String getPreference(String preferenceName) throws RPCException;
    
    /**
     * Sets the value of a given preference 
     * @param preferenceName the name of the preference to set
     * @param value the value to set it to
     */
    public void setPreference(String preferenceName, String value) throws RPCException;
    
    /**
     * Returns the current version of Scooby
     */
    public String getVersion();
    
    /**
     * Remove the value of a given preference for the current user
     * @param preferenceName the name of the preference to remove
     */
    public void removePreference(String preferenceName) throws RPCException;
    
    /**
     * Returns the RecurrenceRule for a particular event
     * 
     * @param collectionUid
     * @param eventIds
     */
    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid,
            String[] eventIds) throws RPCException;
    
    public Map<String, RecurrenceRule> getRecurrenceRules(String collectionUid,
            String[] eventIds, String ticket) throws RPCException;
    
    /**
     * Saves the RecurrenceRule for a particular event
     * 
     * @param collectionUid
     * @param eventId
     * @param recurrenceRule
     * @throws RPCException
     */
    public void saveRecurrenceRule(String collectionUid, String eventId,
            RecurrenceRule recurrenceRule) throws RPCException;
    
    public void saveRecurrenceRule(String collectionUid, String eventId,
            RecurrenceRule recurrenceRule, String ticket) throws RPCException;
    
    /**
     * Expands the events with the given eventIds for the specified time range. 
     * @param collectionUid the calendar in which the events are located
     * @param eventIds the id's of the events to be expanded
     * @param utcStartTime the time range start  in UTC
     * @param utcEndTime the time range end in UTC
     * @return a map with the event id's as keys, and the array of expanded Events 
     *         as values
     * @throws RPCException
     */
    public Map<String, Event[]> expandEvents(String collectionUid, String[] eventIds,
            long utcStartTime, long utcEndTime) throws RPCException;

    public Map<String, Event[]> expandEvents(String collectionUid, String[] eventIds,
            long utcStartTime, long utcEndTime, String ticket) throws RPCException;

    /**
     * Saves the specified new event and updates the event with the specified id
     * with the new recurrence end date
     * @param collectionUid
     * @param event
     * @param originalEventId
     * @param originalEventEndDate
     * @return the id of the new event
     */
    public String saveNewEventBreakRecurrence(String collectionUid, Event event,
            String originalEventId, CosmoDate originalEventEndDate) throws RPCException;

    public String saveNewEventBreakRecurrence(String collectionUid, Event event,
            String originalEventId, CosmoDate originalEventEndDate, String ticket) throws RPCException;
}
