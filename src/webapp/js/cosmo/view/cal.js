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

dojo.provide('cosmo.view.cal');

cosmo.view.cal = new function() {
    
    var self = this;
    var ranges = {
        'daily': ['d', 1],
        'weekly': ['ww', 1],
        'biweekly': ['ww', 2],
        'monthly': ['m', 1],
        'yearly': ['yyyy', 1]
    }
    
    // Saving changes
    // =========================
    function saveEventChangesConfirm(ev) {
        
        // Don't bother going through the edit process if nothing 
        // has actually changed
        var changedProps = ev.hasChanged();
        var changedBasicProps = false;
        var changedRecur = false;
        var recur = ev.dataOrig.recurrenceRule;
        var qual = '';
        
        if (!changedProps) {
            return false;
        }
        for (var i = 0; i < changedProps.length; i++) {
            if (changedProps[i][0] == 'recurrenceRule') {
                changedRecur = true;
            }
            else {
                changedBasicProps = true;
            }
        }
        
        // Changing recurrence rule
        // --------------
        if (changedRecur) {
            // Event with existing recurrence
            // *** Change/remove ***
            if (recur) {
                // Removing from a master event
                if (!ev.data.recurrenceRule && ev.data.masterEvent) {
                    saveEventChanges(ev, 'recurrenceMasterRemoveRecurrence');
                }
                // Changing recurrence for master
                // Changing or 'removing' recurrence from an instance
                else {
                    var opts = self.recurringEventOptions;
                    // Master or instance?
                    // Only changing end date -- applies only to master
                    if (ev.data.recurrenceRule && 
                        (ev.data.recurrenceRule.frequency == ev.dataOrig.recurrenceRule.frequency)) {
                        qual = opts.ALL_EVENTS;
                    }
                    // Otherwise figure out if it's a master or instance
                    else {
                        qual = ev.data.masterEvent ? opts.ALL_EVENTS : opts.ALL_FUTURE_EVENTS
                    }
                    dojo.event.topic.publish('/calEvent', { 'action': 'save', 
                        'qualifier': qual, data: ev });
                }
            }
            // One-shot event -- save a RecurrenceRule
            // *** Add ***
            // 
            // -------
            else {
                saveEventChanges(ev, 'singleEventAddRecurrence');
            }
        }
        // No recurrence change
        // --------------
        else {
            // Recurring event
            // -------
            if (recur) {
                var freq = recur.frequency;
                var opts = {};
                opts.instanceOnly = false;
                opts.masterEvent = false;

                // Check to see if editing a recurrence instance to go
                // beyond the recurrence interval -- in that case only
                // mod is possible. No 'all,' no 'all future.'
                function isOutOfIntervalRange() { 
                    var ret = false;
                    var dt = ev.data.start;
                    var dtOrig = ev.dataOrig.start;
                    var origDate = new Date(dtOrig.getFullYear(), dtOrig.getMonth(), 
                        dtOrig.getDate());
                    var newDate = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate());
                    var unit = ranges[freq][0];
                    var bound = ranges[freq][1];
                    var diff = Date.diff(unit, origDate, newDate);
                    ret = (diff >= bound || diff <= (bound * -1)) ? true : false;
                    return ret;
                }
                // Change to master event in recurrence
                if (ev.data.masterEvent) {
                    opts.masterEvent = true;
                }
                // Change to instance event
                else {
                    opts.instanceOnly = isOutOfIntervalRange();
                }
                // Show the confirmation dialog
                Cal.showDialog(cosmo.view.cal.dialog.getProps('saveRecurConfirm', opts));
            }
            // One-shot event
            // -------
            else {
                saveEventChanges(ev);
            }
        }
    }
    function saveEventChanges(ev, qual) {
        
        // Kill any confirmation dialog that might be showing
        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }
        
        var opts = self.recurringEventOptions;
        
        // Lozenge stuff
        // FIXME: Actually this stuff should be oWnZ0Rd by view.cal.canvas
        // ---------
        // Reset the block because we may be changing to the new type --
        // e.g., between all-day and normal, or between normal single
        // and normal composite
        if (ev.dataOrig && !(ev.data.allDay && ev.dataOrig.allDay)) {
            ev.replaceBlock();
        }
        // Reset the block properties from the event
        ev.block.updateFromEvent(ev, true);
        // Do visual updates to size, position
        ev.block.updateElements();
        // Display processing animation
        ev.block.showProcessing();
        
        // Recurring event
        var f = null;
        if (qual) {
            switch(qual) {
                // Adding recurrence to a one-shot
                case 'singleEventAddRecurrence':
                    f = function() { doSaveEvent(ev, { 'saveType': 'singleEventAddRecurrence', 
                        'originalEvent': ev } ) };
                    break;

                // Removing recurrence (along with other possible edits)
                case 'recurrenceMasterRemoveRecurrence':
                    f = function() { doSaveEvent(ev, { 
                        'saveType': 'recurrenceMasterRemoveRecurrence', 'instanceEvent': null }) }
                    break;
                
                // Changing the recurrence master
                case opts.ALL_EVENTS:
                    if (ev.data.masterEvent) {
                        f = function() { doSaveEvent(ev, { 
                            'saveType': 'recurrenceMaster', 'instanceEvent': null }) }
                    }
                    else {
                        h = function(evData, err) {
                            if (err) {
                                Cal.showErr('Could not retrieve master event for this recurrence.', err);
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'saveFailed',
                                    'qualifier': 'editExisting', 'data': ev });
                            }
                            else {
                                // Basic properties
                                // ----------------------
                                var changedProps = ev.hasChanged();
                                var startOrEndChange = false;
                                for (var i = 0; i < changedProps.length; i++) {
                                    var propName = changedProps[i][0];
                                    var propVal = changedProps[i][1];
                                    if (propName == 'start' || propName == 'end') {
                                        startOrEndChange = true;
                                    }
                                    else {
                                        evData[propName] = propVal;
                                    }
                                }
                                // Start and end
                                // ----------------------
                                if (startOrEndChange) {
                                    var masterStart = evData.start;
                                    var masterEnd = new ScoobyDate();
                                    
                                    var origStart = ev.dataOrig.start;
                                    var newStart = ev.data.start;
                                    var minutesToEnd = ScoobyDate.diff('n', ev.data.start, ev.data.end);
                                    // Date parts for the edited instance start
                                    var mon = newStart.getMonth();
                                    var dat = newStart.getDate();
                                    var hou = newStart.getHours();
                                    var min = newStart.getMinutes();
                                    
                                    // Mod start based on edited instance
                                    switch (ev.data.recurrenceRule.frequency) {
                                        case 'daily':
                                            // Can't change anything but time
                                            break;
                                        case 'weekly':
                                        case 'biweekly':
                                            var diff = Date.diff('d', origStart, newStart);
                                            masterStart.setDate(masterStart.getDate() + diff);
                                            break;
                                        case 'monthly':
                                            masterStart.setDate(dat);
                                            break;
                                        case 'yearly':
                                            masterStart.setMonth(mon);
                                            masterStart.setDate(dat);
                                            break;
                                    }
                                    // Always set time
                                    masterStart.setHours(hou);
                                    masterStart.setMinutes(min);
                                    
                                    masterEnd = ScoobyDate.clone(masterStart);
                                    masterEnd.add('n', minutesToEnd);
                                    evData.end.setYear(masterEnd.getFullYear());
                                    evData.end.setMonth(masterEnd.getMonth());
                                    evData.end.setDate(masterEnd.getDate());
                                    evData.end.setHours(masterEnd.getHours());
                                    evData.end.setMinutes(masterEnd.getMinutes());
                                    masterEnd = evData.end;
                                }
                                
                                // doSaveEvent expects a CalEvent with attached CalEventData
                                var saveEv = new CalEvent();
                                saveEv.data = evData;
                                doSaveEvent(saveEv, { 'saveType': 'recurrenceMaster', 
                                    'instanceEvent': ev });
                            }
                        };
                        f = function() { var reqId = Cal.serv.getEvent(h, 
                            Cal.currentCalendar.path, ev.data.id); };
                    }
                    break;
                
                // Break the previous recurrence and start a new one
                case opts.ALL_FUTURE_EVENTS:
                    var newEv = new CalEvent();
                    var freq = ev.dataOrig.recurrenceRule.frequency;
                    var start = ev.dataOrig.start;
                    var startNoTime = new ScoobyDate(start.getFullYear(), 
                        start.getMonth(), start.getDate());
                    var unit = ranges[freq][0];
                    var incr = (ranges[freq][1] * -1);
                    // Instances all have the same id as the master event
                    var masterEventDataId = ev.data.id;
                    
                    // Calc the new end date for the original recurrence
                    recurEnd = ScoobyDate.add(startNoTime, unit, incr);

                    // Pass a CalEvent obj with an attached CalEventData obj
                    newEv.data = CalEventData.clone(ev.data);
                    
                    // Update the end-date of the new recurrence if needed
                    if (newEv.data.recurrenceRule && newEv.data.recurrenceRule.endDate) {
                        var recurEndOrig = newEv.data.recurrenceRule.endDate;
                        var recurEndDiff = ScoobyDate.diff('d', startNoTime, recurEndOrig);
                        newEv.data.recurrenceRule.endDate = ScoobyDate.add(newEv.data.start, 'd', recurEndDiff);
                    }
                    
                    f = function() { doSaveEventBreakRecurrence(newEv, masterEventDataId, 
                        recurEnd, { 'saveType': 'instanceAllFuture', 
                        'originalEvent': ev, 'masterEventDataId': masterEventDataId, 'recurEnd': recurEnd }); };
                    break;
                
                // Modifications
                case opts.ONLY_THIS_EVENT:
                    var rrule = ev.data.recurrenceRule;
                    var changedProps = ev.hasChanged();
                    var mod = new Modification();
                    var modEv = new CalEventData();
                    mod.instanceDate = ScoobyDate.clone(ev.data.instanceDate);
                    for (var i = 0; i < changedProps.length; i++) {
                        var propName = changedProps[i][0];
                        mod.modifiedProperties.push(propName); 
                        modEv[propName] = changedProps[i][1];
                    }
                    mod.event = modEv;
                    // **** Need ScoobyDate methods here ****
                    for (var i = 0; i < rrule.modifications.length; i++) {
                        var m = rrule.modifications[i];
                        if (m.instanceDate.toUTC() == mod.instanceDate.toUTC()) {
                            rrulerule.modifications.splice(i, 1); 
                        }
                    }
                    rrule.modifications.push(mod);
                    
                    f = function() { doSaveRecurrenceRule(ev, rrule, { 'saveAction': 'save',
                        'saveType': 'instanceOnlyThisEvent' }) };
                    break;
                
                // Default -- nothing to do
                default:
                    break;
            }
        }
        // Normal one-shot event
        else {
            f = function() { doSaveEvent(ev, { 'saveType': 'singleEvent' } ) };
        }
        
        // Give a sec for the processing state to show
        setTimeout(f, 500);
        
    }
    function doSaveEvent(ev, opts) {
        var f = function(newEvId, err, reqId) { 
            handleSaveEvent(ev, newEvId, err, reqId, opts); };
        var requestId = null;

        requestId = Cal.serv.saveEvent(
            f, Cal.currentCalendar.path, ev.data);
        // Add to processing queue
        self.processingQueue.push(requestId);

        // Selection persistence
        if (opts.saveType == 'recurrenceMaster' || 
            opts.saveType == 'singleEventAddRecurrence') {
            self.lastSent = null;
        }
        else {
            self.lastSent = ev;
        }
    }
    function doSaveEventBreakRecurrence(ev, origId, recurEnd, opts) {
        var f = function(newEvId, err, reqId) {
           handleSaveEvent(ev, newEvId, err, reqId, opts); };
        var requestId = null;
        requestId = Cal.serv.saveNewEventBreakRecurrence(
            f, Cal.currentCalendar.path, ev.data, origId, recurEnd);
        self.processingQueue.push(requestId);
        self.lastSent = null;
    }
    function handleSaveEvent(ev, newEvId, err, reqId, optsParam) {
        var saveEv = ev;
        var opts = optsParam || {};
        // Simple error message to go along with details from Error obj
        var errMsg = '';
        var act = '';
        var qual = {};
        
        qual.saveType = opts.saveType || 'singleEvent'; // Default to single event
        
        // Failure -- display exception info
        // ============
        if (err) {
            act = 'saveFailed';
            // Failed update -- fall back to original state
            if (saveEv.dataOrig) {
                errMsg = getText('Main.Error.EventEditSaveFailed');
                qual.newEvent = false;
            }
            // Failed create -- remove fake placeholder event and block
            else {
                errMsg = getText('Main.Error.EventNewSaveFailed');
                qual.newEvent = true;
            }
            Cal.showErr(errMsg, err);
        }
        // Success
        // ============
        else {
            act = 'saveSuccess';
            // Set the CalEventData ID from the value returned by server
            // New event creation and new recurring events created by
            // the 'All Future Events' option
            if (!saveEv.data.id || opts.saveType == 'instanceAllFuture') {
                qual.newEvent = true;
                saveEv.data.id = newEvId;
            }
            else {
                qual.newEvent = false;
            }
            
            // If new dates are out of range, remove the event from display
            if (saveEv.isOutOfViewRange()) {
                qual.onCanvas = false;
            }
            // Otherwise update display
            else {
                qual.onCanvas = true;
            }
        }
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
        
        // Only proceed to repaint recurrence if update succeeds
        if (act == 'saveSuccess' && 
            (opts.saveType == 'recurrenceMaster' || 
            opts.saveType == 'instanceAllFuture' ||
            opts.saveType == 'singleEventAddRecurrence')) {
            // Either single master with recurrence or 
            // master/detached-event combo both with recurrence
            if (saveEv.data.recurrenceRule) {
                loadRecurrenceExpansion(Cal.viewStart, Cal.viewEnd, saveEv, opts);
            }
            // The detached even has a frequency of 'once' -- it's a one-shot
            // No need to go to the server for expansion
            else {
                self.processingQueue.shift();
                // saveEv is the dummy CalEvent obj created for saving
                // Replace this with the original event that has an
                // associated lozenge, etc. -- give it the new CalEventData id
                // from the server
                saveEv = opts.originalEvent;
                saveEv.data.id = newEvId;
                dojo.event.topic.publish('/calEvent', { 'action': 'eventsAddSuccess', 
                   'data': { 'saveEvent': saveEv, 'eventRegistry': null, 
                   'opts': opts } });
            }
        }
        else {
            self.processingQueue.shift();
            // Broadcast message for success/failure
            dojo.event.topic.publish('/calEvent', { 'action': act, 
                'qualifier': qual, 'data': saveEv, 'opts': opts });
        }
    }
    
    // Remove
    // =========================
    function removeEventConfirm(ev) {
        var str = '';
        var opts = {};
        opts.masterEvent = false;
        // Recurrence is a ball-buster
        if (ev.data.recurrenceRule) {
            str = 'removeRecurConfirm';
            if (ev.data.masterEvent) {
                opts.masterEvent = true;
            }
        }
        else {
            str = 'removeConfirm';
        }
        Cal.showDialog(cosmo.view.cal.dialog.getProps(str, opts));
    }
    function removeEvent(ev, qual) {
        var opts = self.recurringEventOptions;
        
        // Kill any confirmation dialog that might be showing
        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }
        // Recurring event
        var f = null;
        if (qual) {
            switch(qual) {
                case opts.ALL_EVENTS:
                    if (ev.data.masterEvent) {
                        f = function() { doRemoveEvent(ev, { 'removeType': 'recurrenceMaster' }) };
                    }
                    else {
                        h = function(evData, err) {
                            if (err) {
                                Cal.showErr('Could not retrieve master event for this recurrence.', err);
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'removeFailed',
                                    'data': ev });
                            }
                            else {
                                // doRemoveEvent expects a CalEvent with attached CalEventData
                                var removeEv = new CalEvent();
                                removeEv.data = evData;
                                doRemoveEvent(removeEv, { 'removeType': 'recurrenceMaster', 
                                    'instanceEvent': ev });
                            }
                        };
                        f = function() { var reqId = Cal.serv.getEvent(h, 
                            Cal.currentCalendar.path, ev.data.id); };
                    }
                    break;
                case opts.ALL_FUTURE_EVENTS:
                        h = function(hashMap, err) {
                            if (err) {
                                Cal.showErr('Could not retrieve recurrence rule for this recurrence.', err);
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'removeFailed',
                                    'data': ev });
                            }
                            else {
                                for (var a in hashMap) {
                                    var saveRule = hashMap[a];
                                }
                                var freq = ev.data.recurrenceRule.frequency;
                                var start = ev.data.start;
                                var recurEnd = new ScoobyDate(start.getFullYear(), 
                                    start.getMonth(), start.getDate());
                                var unit = ranges[freq][0];
                                var incr = (ranges[freq][1] * -1);
                                recurEnd = ScoobyDate.add(recurEnd, unit, incr);
                                saveRule.endDate = recurEnd;
                                doSaveRecurrenceRule(ev, saveRule, { 'saveAction': 'remove', 
                                    'removeType': 'instanceAllFuture', 'recurEnd': recurEnd });
                            }
                        };
                        f = function() { var reqId = Cal.serv.getRecurrenceRules(h, 
                            Cal.currentCalendar.path, [ev.data.id]); };
                    break;
                case opts.ONLY_THIS_EVENT:
                    var rrule = ev.data.recurrenceRule;
                    var dates = rrule.exceptionDates;
                    var d = ScoobyDate.clone(ev.data.instanceDate);
                    dates.push(d);
                    
                    f = function() { doSaveRecurrenceRule(ev, rrule, { 'saveAction': 'remove',
                        'saveType': 'instanceOnlyThisEvent' }) };
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // Normal one-shot event
        else {
            f = function() { doRemoveEvent(ev, { 'removeType': 'singleEvent' }) }
        }
        f();
        
    }
    function doRemoveEvent(ev, opts) {
        var f = function(newEvId, err, reqId) { 
            handleRemoveResult(ev, newEvId, err, reqId, opts); };
        var requestId = Cal.serv.removeEvent(
            f, Cal.currentCalendar.path, ev.data.id);
    }
    function handleRemoveResult(ev, newEvId, err, reqId, opts) {
        var removeEv = ev;
        // Simple error message to go along with details from Error obj
        var errMsg = getText('Main.Error.EventRemoveFailed');
        if (err) {
            act = 'removeFailed';
            Cal.showErr(errMsg, err);
        }
        else {
            act = 'removeSuccess';
        }
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
            
        // Broadcast success
        dojo.event.topic.publish('/calEvent', { 'action': act, 
            'data': removeEv, 'opts': opts });
    }
    function doSaveRecurrenceRule(ev, rrule, opts) {
        var f = function(err, reqId) { 
            handleSaveRecurrenceRuleResult(ev, err, reqId, opts); };
        var requestId = Cal.serv.saveRecurrenceRule(
            f, Cal.currentCalendar.path, ev.data.id, rrule);
    }
    function handleSaveRecurrenceRuleResult(ev, err, reqId, opts) {
        var rruleEv = ev;
        var errMsgKey = opts.saveAction == 'remove' ? 'EventRemoveFailed' : 'EventEditSaveFailed';
        // Simple error message to go along with details from Error obj
        var errMsg = getText('Main.Error.' + errMsgKey);
        var qual = {};
        
        if (err) {
            act = opts.saveAction + 'Failed';
            Cal.showErr(errMsg, err);
        }
        else {
            act = opts.saveAction + 'Success';
        }
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
        
        // If new dates are out of range, remove the event from display
        if (rruleEv.isOutOfViewRange()) {
            qual.onCanvas = false;
        }
        // Otherwise update display
        else {
            qual.onCanvas = true;
        }
        
        // Broadcast success
        dojo.event.topic.publish('/calEvent', { 'action': act, 
            'data': rruleEv, 'opts': opts, 'qualifier': qual });
    }
    
    function loadRecurrenceExpansion(start, end, ev, opts) {
        var id = ev.data.id;
        var s = start.getTime();
        var e = end.getTime();
        var f = function(hashMap) {
            var expandEventHash = createEventRegistry(hashMap);
            self.processingQueue.shift();
            dojo.event.topic.publish('/calEvent', { 'action': 'eventsAddSuccess', 
               'data': { 'saveEvent': ev, 'eventRegistry': expandEventHash, 
               'opts': opts } });
        }

        Cal.serv.expandEvents(f, Cal.currentCalendar.path, [id], s, e); 
    }
    
    function createEventRegistry(arrParam) {
        var h = new Hash();
        var arr = [];
       
        // Param may be a single array, or hashmap of arrays -- one
        // for each recurring event sequence
        // ---------------------------------
        // If passed a simple array, use it as-is
        if (arrParam.length) {
            arr = arrParam;
        }
        // If passed a hashmap of arrays, suck all the array items
        // into one array
        else {
            for (var j in arrParam) {
                var a = arrParam[j];
                for (var i = 0; i < a.length; i++) {
                    arr.push(a[i]);
                }
            }
        }
        
        for (var i = 0; i < arr.length; i++) {
            evData = arr[i];
            // Basic paranoia checks
            if (!evData.end) {
                evData.end = ScoobyDate.clone(evData.start);
            }
            if (evData.start.timezone || evData.end.timezone) {
                if (!evData.end.timezone) {
                    evData.end.timezone =
                        ScoobyTimezone.clone(evData.start.timezone);
                }
                if (!evData.start.timezone) {
                    evData.start.timezone =
                        ScoobyTimezone.clone(evData.end.timezone);
                }
            }
            // Make exceptionDates on recurrences default to empty array  
            if (evData.recurrenceRule && !evData.recurrenceRule.exceptionDates) {
                evData.recurrenceRule.exceptionDates = [];
            }
            var id = Cal.generateTempId();
            ev = new CalEvent(id, null);
            ev.data = evData;
            h.setItem(id, ev);
        }
        return h;
    }
    
    // Public attributes
    // ********************
    // Options for saving/removing recurring events
    this.recurringEventOptions = {
        ALL_EVENTS: 'allEvents',
        ALL_FUTURE_EVENTS: 'allFuture',
        ONLY_THIS_EVENT: 'onlyThis'
    };
    // How many updates/removals are in-flight
    this.processingQueue = [];
    this.lastSent = null;
    
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    this.handlePub = function(cmd) {
        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var ev = cmd.data;
        switch (act) {
            case 'saveConfirm':
                saveEventChangesConfirm(ev);
                break;
            case 'save':
                saveEventChanges(ev, qual);
                break;
            case 'removeConfirm':
                removeEventConfirm(ev);
                break;
            case 'remove':
                removeEvent(ev, qual);
                break;
            default:
                // Do nothing
                break;
        }
    };
    
    this.loadEvents = function(start, end) {
        var s = start.getTime();
        var e = end.getTime();
        var eventLoadList = null;
        var eventLoadHash = new Hash();
        var isErr = false;
        var detail = '';
        var evData = null;
        var id = '';
        var ev = null;

        dojo.event.topic.publish('/calEvent', { 'action': 'eventsLoadStart' });
        // Load the array of events
        // ======================
        try {
            eventLoadList = Cal.serv.getEvents(Cal.currentCalendar.path, s, e);
        }
        catch(e) {
            Cal.showErr(getText('Main.Error.LoadEventsFailed'), e);
            Log.print(e.javaStack);
            return false;
        }
        var eventLoadHash = createEventRegistry(eventLoadList);
        dojo.event.topic.publish('/calEvent', { 'action': 'eventsLoadSuccess', 
            'data': eventLoadHash });
        return true;
    };
};

