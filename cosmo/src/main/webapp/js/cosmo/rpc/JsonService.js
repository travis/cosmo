/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.rpc.JsonService");
dojo.require("dojo.rpc.JsonService");

dojo.declare("cosmo.rpc.JsonService", dojo.rpc.JsonService,{
    
    strictArgChecks: false,

	createRequest: function(method, params){
         return dojo.rpc.JsonService.prototype.createRequest.apply(
             this, [this.objectName + "." + method, params]);
	},
	
	processSmd: function(/*json*/ object){
	     this.objectName = object.objectName;
	     return dojo.rpc.RpcService.prototype.processSmd.apply(this, [object]);
	},
	
	bind: function(method, parameters, deferredRequestHandler, url, kwArgs){
	    var bindArgs = kwArgs || {};
	    print(bindArgs.sync)
        dojo.io.bind(dojo.lang.mixin({
			url: url||this.serviceUrl,
			postContent: this.createRequest(method, parameters),
			method: "POST",
			contentType: this.contentType,
			mimetype: "text/json",
			load: this.resultCallback(deferredRequestHandler),
			error: this.errorCallback(deferredRequestHandler),
			preventCache:this.bustCache 
		}, bindArgs));
	},
	
    generateMethod: function(/*string*/ method, /*array*/ parameters, /*string*/ url){
    	// summary
    	// generate the local bind methods for the remote object
    	return dojo.lang.hitch(this, function(){
    	    
    	    var rpcParams = Array.prototype.slice.apply(arguments, [0, parameters.length]);
            var kwArgs = arguments[parameters.length] || {};
            if (kwArgs.ticketKey){
                rpcParams.push(kwArgs.ticketKey);
    	    }
    	    
    		var deferredRequestHandler = new dojo.Deferred();

  			this.bind(method, rpcParams, deferredRequestHandler, url, kwArgs);

    		return deferredRequestHandler;
    	});
	}
});

cosmo.rpc.JsonService.defaultSMD = {
"SMDVersion":".1",
"objectName":"scoobyService",
"serviceType":"JSON-RPC",
"serviceURL": cosmo.env.getBaseUrl() + "/JSON-RPC",
"methods":[
    {'name':'getEvents', 'parameters': [{'name': 'calendarPath'}, {'name': 'beginDate'}, {'name': 'endDate'}]},

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
    {'name':'saveEvent', 'parameters': [{'name': 'event'}]},
     /**
     * Removes an event with the specified id from the calendar.
     *
     * @param calendarPath path to the calendar containing the Event to be deleted
     * @param id the id of the Event to be deleted
     * @throws ScoobyServiceException
     */
    {'name':'removeEvent', 'parameters': [{'name': 'calendarName'}, {'name': 'id'}]},
    /**
    * Returns the Event with the given id.
    *
    * @param calendarPath path to the calendar containing the desired Event
    * @param id the id of the desired Event
    * @return the Event with the given id, or null if none found.
    */
   {'name':'getEvent', 'parameters': [{'name': 'calendarName'}, {'name': 'id'}]},
    /**
    * Moves an event from one calendar to another.
    *
    * @param sourceCalendar the path to the calendar currently containing the event to be moved
    * @param id the id of the event to be moved
    * @param destinationCalendar the desired destination
    */
   {'name':'moveEvent', 'parameters': [{'name': 'sourceCalendar'}, {'name': 'id'}, {'name': 'destinationCalendar'}]},
    /**
    * Returns all the calendar names for the current users.
    *
    * @return an array of the calendar names that exist for the current user
    */
   {'name':'getCalendars', 'parameters': []},
    /**
    * Creates a calendar with the given name
    * @param calendarName
    */
   {'name':'createCalendar', 'parameters': [{'name': 'calendarName'}, {'name': 'calendarPath'}]},
   
   /**
    * Gets the preference for the given name for the current user.
    * @param preferenceName the name of the preference to retrieve
    * @return the preference
    */
   {'name':'getPreference', 'parameters': [{'name': 'preferenceName'}]},
   
   /**
    * Sets the preference for the given name for the current user
    * with the value specified.
    * @param preferenceName the name of the preference to set
    * @param value the value to set it to
    */
   {'name':'setPreference', 'parameters': [{'name': 'preferenceName'}, {'name': 'value'}]},
   
   /**
    * Removes the preference for the given name for the current user
    * @param preferenceName the name of the preference to be removed
    */
   {'name':'removePreference', 'parameters': [{'name': 'preferenceName'}]},
   
   /**
    * Gets all preferences for the current user
    */
   {'name':'getPreferences', 'parameters': []},
    /**
    * Sets all preferences for the current user
    * @param preferenceName the map of the user's new preference set
    */
   {'name':'setPreferences', 'parameters': []},
    /**
    * Sets the specified preferences for the current user
    * @param preferenceName the map of preferences to be set
    */
   {'name':'setMultiplePreferences', 'parameters': [{'name': 'preferences'}]},
   
   /**
    * Returns the RecurrenceRules for a bunch of events
    * 
    * @param calendarPath the calendar containing the events
    * @param eventIds an array of the id's of the events
    *                 for which you want the RecurrenceRules of
    * @return a map of ids --> RecurrenceRules
    */
   {'name':'getRecurrenceRules', 'parameters': [{'name': 'calendarPath'}, {'name': 'eventIds'}]},
    
        
   /**
    * Saves the RecurrenceRule for a particular event
    * 
    * @param calendarPath
    * @param eventId
    * @param recurrenceRule
    */
   {'name':'saveRecurrenceRule', 'parameters': [{'name': 'calendarPath'}, {'name': 'eventId'}, {'name': 'recurrenceRule'}]},
    /**
    * Expands the events with the given eventIds for the specified time range. 
    * @param calendarPath the calendar in which the events are located
    * @param eventIds an array of the id of the events to be expanded
    * @param utcStartTime the time range start  in UTC
    * @param utcEndTime the time range end in UTC
    * @return a map with the event id's as keys, and the array of expanded Events 
    *         as values
    */
   {'name':'expandEvents', 'parameters': [{'name': 'calendarPath'}, {'name': 'eventIds'}, {'name': 'utcStartTime'}, {'name': 'utcEndTime'}]},
    /**
    * Saves the specified new event and updates the event with the specified id
    * with the new recurrence end date   
    * @param calendarPath the calendar where the event is.
    * @param event the new event to be saved
    * @param originalEventId the id of the original event
    * @param originalEventEndDate the new recurrence end date of the original event
    * @return the id of the new event
    */
   {'name':'saveNewEventBreakRecurrence', 'parameters': [{'name': 'calendarPath'}, {'name': 'event'},
                   {'name': 'originalEventId'}, 
                   {'name': 'originalEventEndDate'}]},
   
   /**
    * Get the calendar with the specified uid.
    * @param collectionId the uid of the collection to get
    */
   {'name':'getCalendar', 'parameters': [{'name': 'collectionId'}]},
   
   /**
    * Get the subscription identified with the specified collectionId and ticketKey
    */
               {'name':'getSubscription', 'parameters': [{'name': 'collectionId'}, {'name': 'ticketKey'}]},
   
   /**
    * Get the ticket identified with the specified collectionId and ticketKey
    */
   {'name':'getTicket', 'parameters': [{'name': 'ticketKey'}, {'name': 'collectionId'}]},
   
   /**
    * Deletes the subscription with the specified collectionId and ticketKey
    */
   {'name':'deleteSubscription', 'parameters': [{'name': 'collectionId'}, {'name': 'ticketKey'}]},
   
   /**
    * Creates or updates a subscription for the current user with the specified collectionId
    * and ticketKey.
    */
   {'name':'saveSubscription', 'parameters': [{'name': 'cid'}, {'name': 'ticketKey'}, {'name': 'displayName'}]},
   
   /**
    * Gets all the subscriptions for the current user
    */
   {'name':'getSubscriptions', 'parameters': []},
   
    /**
    * Returns the version of Cosmo
    */
   {'name':'getVersion', 'parameters': []},
   
   /**
    * Changes the display name for a collection. Must be a collection
    * owned by the user
    */
   {'name':'saveDisplayName', 'parameters': [{'name': 'cid'}, {'name': 'displayName'}]} 
]}

cosmo.rpc.JsonService.getDefaultService = function(){
    var s = new cosmo.rpc.JsonService();
    s.processSmd(this.defaultSMD);
    return s;
}

