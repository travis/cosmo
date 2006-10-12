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

function CalEventData(id, title, description, start, end, allDay,
    pointInTime, anyTime, recurrenceRule, status, masterEvent, instance) {

    this.id = id;
    this.title = title;
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
};

CalEventData.prototype = {
    toString: genericToString
}

CalEventData.clone = function(data) {
    var ret = new CalEventData(
        data.id,
        data.title,
        data.description,
        ScoobyDate.clone(data.start),
        ScoobyDate.clone(data.end),
        data.allDay,
        data.pointInTime,
        data.anyTime,
        RecurrenceRule.clone(data.recurrenceRule),
        data.status,
        data.masterEvent,
        data.instance,
        data.javaClass);
    return ret;
}


/**
 * A recurrence rule specifies how to repeat a given event.
 */
function RecurrenceRule(){
    /**
     * Frequencies for recurrence.
     */
    this.FREQUENCY_WEEKLY = "weekly";
    this.FREQUENCY_DAILY = "daily";
    this.FREQUENCY_MONTHLY = "monthly";
    this.FREQUENCY_YEARLY = "yearly";
    this.FREQUENCY_BIWEEKLY = "biweekly";

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

RecurrenceRule.prototype = {
    toString: genericToString
}

RecurrenceRule.clone = function(rule) {
    var ret = null;
    if (rule) {
        ret = new RecurrenceRule();
        ret.frequency = rule.frequency;
        ret.endDate = rule.endDate;
        ret.customRule = rule.customRule;
    }
    return ret;
}

function Modification(){
    /**
     * References the instance date which is being modified
     *
     */
    this.instanceDate = null;
    
    /**
     * The list of modified properties
     *
     */
    this.modifiedProperties = null;
    
    /**
     * The event with the modified properties. Note: only the modified properties need to be set
     */
    this.event;
}

Modification.prototype = {
    toString: genericToString
}

function StatusTemplate() {
    this.CONFIRMED = "confirmed";
    this.TENTATIVE = "tentative";
    this.CANCELLED = "fyi";
}

function CalendarMetadata(){
    this.name = null;
    this.path = null;
}
