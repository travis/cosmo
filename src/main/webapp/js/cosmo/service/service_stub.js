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

dojo.provide("cosmo.service.service_stub")
/**
 * The ScoobyService is the place where all client/server communcations are
 * brokered. The methods here are all empty because the intent is that you
 * can have different implementations for different types of RPC (eg. JSON-RPC, XML-RPC.)
 * Whichever implementation is loaded will replace the methods here with concrete implementations.
 *
 * All methods here can be called two ways - synchronously or asynchronously. For synchcronous
 * calls just call them method as declared below. For example:
 *
 *    <code> var events = scoobyService.getEvents("Cosmo", beginDate, endDate);</code>
 *
 * For asynchronous calls you simply pass a callback as the first argument to any of the methods.
 * The callback is of the form
 *
 *    <code>function callback(result, exception)</code>
 *
 * Note that for synchronous calls exceptions are thrown, while for asynch calls they are simply
 * passed to the callback. In other words, for asycnh calls you don't catch exceptions, just check
 * for the presence of an exception. If it's not null, a problem occured.
 */
ScoobyService = function() {};
ScoobyService.prototype = {

    serviceAccessTime: 0,

     /**
      * Initializes the service. Must be called before using the service.
      */
     init: function() {},

     getServiceAccessTime: function() {},
     resetServiceAccessTime: function() {},
     refreshServerSession: function() {},

     /**
      * Gets all the events between the two dates, sorted in ascending order on
      * begin date
      *
      * @param calendarPath the path to the calendar to save to
      * @param beginDate the date from which to start the search
      * @param endDate the date to end the search on
      * @return an array of CalEvents
      */
    getEvents: function(calendarPath, beginDate, endDate){},

     /**
      * Persists an event to the calendar. If there is no id specified in the given Event
      * a new Event object will be persisted and the id will be returned. If there is an
      * id, the event with the same id in the same calendar will be updated. If there is an
      * id but there is no matching event, a ScoobyService exception will be thrown
      *
      * @param calendarPath
      * @param event
      * @return unique id
      */
     saveEvent: function(event){},

     /**
      * Removes an event with the specified id from the calendar.
      *
      * @param calendarPath path to the calendar containing the Event to be deleted
      * @param id the id of the Event to be deleted
      * @throws ScoobyServiceException
      */
    removeEvent: function(calendarName, id){},

    /**
     * Returns the Event with the given id.
     *
     * @param calendarPath path to the calendar containing the desired Event
     * @param id the id of the desired Event
     * @return the Event with the given id, or null if none found.
     */
    getEvent: function(calendarName, id){},

    /**
     * Moves an event from one calendar to another.
     *
     * @param sourceCalendar the path to the calendar currently containing the event to be moved
     * @param id the id of the event to be moved
     * @param destinationCalendar the desired destination
     */
    moveEvent: function(sourceCalendar, id, destinationCalendar){},

    /**
     * Returns all the calendar names for the current users.
     *
     * @return an array of the calendar names that exist for the current user
     */
    getCalendars: function() {},

    /**
     * Creates a calendar with the given name
     * @param calendarName
     */
    createCalendar: function(calendarName, calendarPath){},
    
    /**
     * Gets the preference for the given name for the current user.
     * @param preferenceName the name of the preference to retrieve
     * @return the preference
     */
    getPreference: function(preferenceName){},
    
    /**
     * Sets the preference for the given name for the current user
     * with the value specified.
     * @param preferenceName the name of the preference to set
     * @param value the value to set it to
     */
    setPreference: function(preferenceName, value){},
    
    /**
     * Returns the RecurrenceRules for a bunch of events
     * 
     * @param calendarPath the calendar containing the events
     * @param eventIds an array of the id's of the events
     *                 for which you want the RecurrenceRules of
     * @return a map of ids --> RecurrenceRules
     */
     getRecurrenceRules: function(calendarPath, eventIds){},
     
         
    /**
     * Saves the RecurrenceRule for a particular event
     * 
     * @param calendarPath
     * @param eventId
     * @param recurrenceRule
     */
     saveRecurrenceRule: function(calendarPath, eventId, recurrenceRule){},

    /**
     * Expands the events with the given eventIds for the specified time range. 
     * @param calendarPath the calendar in which the events are located
     * @param eventIds an array of the id of the events to be expanded
     * @param utcStartTime the time range start  in UTC
     * @param utcEndTime the time range end in UTC
     * @return a map with the event id's as keys, and the array of expanded Events 
     *         as values
     */
    expandEvents: function(calendarPath, eventIds, utcStartTime, utcEndTime){},

    /**
     * Saves the specified new event and updates the event with the specified id
     * with the new recurrence end date   
     * @param calendarPath the calendar where the event is.
     * @param event the new event to be saved
     * @param originalEventId the id of the original event
     * @param originalEventEndDate the new recurrence end date of the original event
     * @return the id of the new event
     */
    saveNewEventBreakRecurrence: function(calendarPath, event,
        originalEventId, originalEventEndDate){},
    
    /**
     * Get the calendar with the specified uid.
     * @param collectionId the uid of the collection to get
     */
    getCalendar: function(collectionId){},
    
    /**
     * Get the subscription identified with the specified collectionId and ticketKey
     */
    getSubscription: function(collectionId, ticketKey){},
    
    /**
     * Get the ticket identified with the specified collectionId and ticketKey
     */
    getTicket: function(ticketKey, collectionId){},
    
    /**
     * Deletes the subscription with the specified collectionId and ticketKey
     */
    deleteSubscription: function(collectionId, ticketKey){},
    
    /**
     * Creates or updates a subscription for the current user with the specified collectionId
     * and ticketKey.
     */
    saveSubscription: function(cid, ticketKey, displayName){},
    
    /**
     * Gets all the subscriptions for the current user
     */
    getSubscriptions: function(){},
    

    /**
     * Returns the version of Cosmo
     */
    getVersion: function(){}
}

/**
 * This is the root of all exceptions thrown by the ScoobyService
 */
ScoobyServiceException = function(){};
ScoobyServiceException.prototype = new Error();


//TODO This is not how to do inheritance. 

/**
 * This exception is the root for all exceptions that occur on the client side
 */
ScoobyServiceClientException = function(){};
ScoobyServiceClientException.prototype = new ScoobyServiceException();

/**
 * This exception is the root for all exceptions that occur on the server side
 */
ScoobyServiceRemoteException = function(){};
ScoobyServiceRemoteException.prototype = new ScoobyServiceException();

/**
 * This exception is thrown when the client makes a call on the server side but is not
 * authenticated.
 */
NotAuthenticatedException = function(){};
NotAuthenticatedException.prototype = new ScoobyServiceRemoteException();
