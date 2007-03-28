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
dojo.provide("cosmo.model");

dojo.require("cosmo.util.debug");
dojo.require("cosmo.util.hash");


cosmo.model.CalEventData = function (id, title, description, start, end, allDay,
    pointInTime, anyTime, recurrenceRule, status, masterEvent, instance, instanceDate, loc) {

    this.id = id;
    this.title = title;
    this.location = loc ? loc : null;
    this.description = description ? description : null;
    this.start = start;
    this.end = end;
    this.allDay = allDay ? allDay : false;
    this.pointInTime = pointInTime ? pointInTime : false;
    this.anyTime = anyTime ? anyTime : false;
    this.recurrenceRule = recurrenceRule ? recurrenceRule : null;
    this.status = status ? status : null;
    this.masterEvent = masterEvent ? masterEvent : false;
    this.instance = instance ? instance : false;
    this.instanceDate = instanceDate ? instanceDate : null;
};
CalEventData = cosmo.model.CalEventData;

CalEventData.prototype = {
    toString: genericToString
}

CalEventData.clone = function(data) {
    var ret = new CalEventData(
        data.id,
        data.title,
        data.description,
        data.start.clone(),
        data.end.clone(),
        data.allDay,
        data.pointInTime,
        data.anyTime,
        RecurrenceRule.clone(data.recurrenceRule),
        data.status,
        data.masterEvent,
        data.instance,
        data.instanceDate,
        data.location,
        data.javaClass
        );
    return ret;
}

/**
 * A recurrence rule specifies how to repeat a given event.
 */
cosmo.model.RecurrenceRule = function(){
    /**
     * Specifies how often to repeat this event.
     * Must be one of the frequency constants above.
     */
    this.frequency = null;

    /**
     * The date to repeat this event until.
     * This will only be a Date, not a DateTime -- should
     * NOT include time info
     */
    this.endDate = null;

    /**
     * For events not created in Cosmo that have more complex rules than Cosmo
     * allows, a text representation of the rule appears here but is not editable.
     */
    this.customRule = null;

    /**
     * This is an array of CosmoDates, each one representing a date on which the
     * event will NOT occur
     * This must be a DateTime, not just a Date
     *
     */
    this.exceptionDates = [];

    /**
     * An array of modifications, each one representing a modification to an event
     * instance
     *
     */
    this.modifications = [];
}

RecurrenceRule = cosmo.model.RecurrenceRule;
RecurrenceRule.prototype = {
    toString: genericToString,
    
    getModification: function(/*CosmoDate*/ instanceDate, /*boolean?*/ returnIndex){
        //summary: returns the modification for the given instance date
        //desciption: if there is a modification whose instanceDate is UTC equivalent
        //            to the given date, it is returned. If no such modification exists null is returned.
        //instanceDate: the instanceDate of the desired modification
        //returnIndex: if true, returns the index of the found modification, or -1 if not found.
        
        if (!this.modifications){
            return returnIndex ? -1 : null;
        }
        
        for (var x = 0; x < this.modifications.length;x++){
            var modification = this.modifications[x];
            if (modification.instanceDate.getTime() == instanceDate.getTime()){
                return returnIndex ? x : modification;
            }
        }
        return returnIndex ? -1 : null;
    },
    
    removeModification: function(/*CosmoDate*/ instanceDate){
        var i = this.getModification(instanceDate, true);
        if (i > -1){
            this.modifications.splice(i,1);
        }
    }
}

RecurrenceRuleFrequency = {
    FREQUENCY_DAILY: "daily",
    FREQUENCY_WEEKLY: "weekly",
    FREQUENCY_BIWEEKLY: "biweekly",
    FREQUENCY_MONTHLY: "monthly",
    FREQUENCY_YEARLY: "yearly"
}

RecurrenceRule.clone = function(rule) {
    var ret = null;
    var arr = [];
    if (rule) {
        ret = new RecurrenceRule();
        ret.frequency = rule.frequency;
        ret.endDate = rule.endDate;
        ret.customRule = rule.customRule;
        ret.exceptionDates = [];
        arr = rule.exceptionDates;
        if (arr) {
            for (var i = 0; i < arr.length; i++) {
                ret.exceptionDates.push(ScoobyDate.clone(arr[i]));
            }
        }
        ret.modifications = [];
        arr = rule.modifications;
        if (arr) {
            for (var i = 0; i < arr.length; i++) {
                ret.modifications.push(Modification.clone(arr[i]));
            }
        }
    }
    return ret;
}

cosmo.model.Modification = function (){
    /**
     * References the instance date which is being modified
     *
     */
    this.instanceDate = null;

    /**
     * The list of modified properties
     *
     */
    this.modifiedProperties = [];

    /**
     * The event with the modified properties. Note: only the modified properties need to be set
     */
    this.event;
}
Modification = cosmo.model.Modification;

Modification.prototype = {
    toString: genericToString
}

Modification.clone = function(mod) {
    var ret = null;
    if (mod) {
        ret = new Modification();
        ret.instanceDate = mod.instanceDate;
        ret.modifiedProperties = [];
        var arr = mod.modifiedProperties;
        if (arr) {
            for (var i = 0; i < arr.length; i++) {
                ret.modifiedProperties.push(arr[i]);
            }
        }
        ret.event = new CalEventData();
        if (mod.event) {
            for (var i in mod.event) {
                ret.event[i] = mod.event[i];
            }
        }
    }
    return ret;
}

Modification.prototype = {
    toString: genericToString
}

EventStatus = {
    CONFIRMED: "CONFIRMED",
    TENTATIVE: "TENTATIVE",
    FYI: "CANCELLED"
}

cosmo.model.CalendarMetadata = function (){
    this.name = null;
    this.uid = null;
    this.protocolUrls = {};
}
CalendarMetadata = cosmo.model.CalendarMetadata;

cosmo.model.Subscription = function (){
    this.calendar = null;
    this.displayName = null;
    this.ticket = null;
}
Subscription = cosmo.model.Subscription;

cosmo.model.Ticket = function (){
    this.privileges = {}; //a Set
    this.ticketKey = null;
}

Ticket = cosmo.model.Ticket;


cosmo.model.sortEvents = function(/*Array|cosmo.util.hash.Hash*/ events){
// summary: Sorts a collection of events based on start date. 
//          If the start dates are equal, longer events are  
//          sorted first

    var hash = events instanceof cosmo.util.hash.Hash;
    for (var x = 0; x < events.length; x++){
        var event = hash ? events.getAtPos(x) : events[x];
        if (event.data){
            event = event.data;
        }
        event.__startUTC = event.start.toUTC();
        event.__endUTC = (event.end ? event.end.toUTC() : event.__startUTC);
    }
    
    events.sort(cosmo.model._eventComparator);

    for (var x = 0; x < events.length; x++){
        var event = hash ? events.getAtPos(x) : events[x];
        if (event.data){
            event = event.data;
        }
        delete(event.__startUTC);
        delete(event.__endUTC);
    }
}

cosmo.model._eventComparator = function (a, b) {
   // summary: used by cosmo.model.sortEvents.
   if (a.data){
       a = a.data;
   }
   if (b.data){
       b = b.data;
   }
    var aStart = a.__startUTC;
    var bStart = b.__startUTC;
    if (aStart > bStart) {
        return 1;
    }
    else if (aStart < bStart) {
        return -1;
    } else {
    // If start is equal, sort longer events first
      var aEnd = a.__endUTC;
      var bEnd = b.__endUTC;
        if (aEnd < bEnd) {
            return 1;
        }
        // Be sure to handle equal values
        else if (aEnd >= bEnd) {
            return -1;
        }
    }
};
