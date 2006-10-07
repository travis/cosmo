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
    
    // Saving changes
    // =========================
    function saveEventChangesConfirm(ev) {
        var recur = ev.data.recurrenceRule;
        // Recurrence is a ball-buster
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
                var origDate = new Date(dtOrig.getFullYear(), dtOrig.getMonth(), dtOrig.getDate());
                var newDate = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate());
                var ranges = {
                    'daily': ['d', 1],
                    'weekly': ['ww', 1],
                    'biweekly': ['ww', 2],
                    'monthly': ['m', 1],
                    'yearly': ['yyyy', 1]
                }
                var unit = ranges[freq][0];
                var bound = ranges[freq][1];
                var diff = Date.diff(unit, origDate, newDate);
                ret = (diff >= bound || diff <= (bound * -1)) ? true : false;
                return ret;
            }
            if (ev.data.masterEvent) {
                opts.masterEvent = true;
            }
            else {
                opts.instanceOnly = isOutOfIntervalRange();
            }
            Cal.showDialog(cosmo.view.cal.dialog.getProps('saveRecurConfirm', opts));
        }
        else {
            saveEventChanges(ev);
        }
    }
    function saveEventChanges(ev, qual) {
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
                        f = function() { doSaveEvent(ev, { 'saveType': 'recurrenceMaster', 'instanceEventId': null }) }
                    }
                    else {
                        h = function(evData, err) {
                            if (err) {
                                Cal.showErr('Could not retrieve master event for this recurrence.', err);
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'saveFailed',
                                    'qualifier': 'editExiting', 'data': ev });
                            }
                            else {
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
                                
                                //Log.print(minutesToEnd);
                                //Log.print(masterStart.toString());
                                //Log.print(masterEnd.toString());
                                
                                // doSaveEvent expects a CalEvent with attached CalEventData
                                var saveEv = new CalEvent();
                                saveEv.data = evData;
                                doSaveEvent(saveEv, { 'saveType': 'recurrenceMaster', 'instanceEventId': ev.data.id });
                            }
                        };
                        f = function() { var reqId = Cal.serv.getEvent(h, Cal.currentCalendar.path, ev.data.id); };
                    }
                    break;
                case opts.ALL_FUTURE_EVENTS:
                    
                    break;
                case opts.ONLY_THIS_EVENT:
                    
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // Normal one-shot event
        else {
            f = function() { doSaveEvent(ev, { 'saveType': 'singleEvent' }) }
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
            if (!saveEv.data.id) {
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
        
        if (opts.saveType == 'recurrenceMaster') {
            loadRecurrenceExpansion(saveEv.data.id, Cal.viewStart, Cal.viewEnd);
        }
        else {
            // Broadcast message for success/failure
            dojo.event.topic.publish('/calEvent', { 'action': act, 
                'qualifier': qual, 'data': saveEv });
        }
    }
    
    // Remove
    // =========================
    function removeEventConfirm(ev, qual) {
        var str = '';
        // Recurrence is a ball-buster
        if (ev.data.recurrenceRule) {
            str = 'removeRecurConfirm';
        }
        else {
            str = 'removeConfirm';
        }
        Cal.showDialog(cosmo.view.cal.dialog.getProps(str));
    }
    function removeEvent(ev) {
        doRemove(ev);
        
        // No currently selected event
        cosmo.view.cal.canvas.selectedEvent = null;
        
        // Kill any confirmation dialog that might be showing
        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }
    }
    function doRemove(ev) {
        var f = function(newEvId, err, reqId) { 
            handleRemoveResult(ev, newEvId, err, reqId); };
        var requestId = Cal.serv.removeEvent(
            f, Cal.currentCalendar.path, ev.data.id);
    }
    function handleRemoveResult(ev, newEvId, err, reqId) {
        var removeEv = ev;
        // Simple error message to go along with details from Error obj
        var errMsg = getText('Main.Error.EventRemoveFailed');
        if (err) {
            Cal.showErr(errMsg, err);
        }
        else {
            // Broadcast success
            dojo.event.topic.publish('/calEvent', { 'action': 'removeSuccess', 
                'data': removeEv });
        }
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
    }
    function loadRecurrenceExpansion(id, start, end) {
        var s = start.getTime();
        var e = end.getTime();
        var f = function(arr) {
            var expandEventHash = createEventRegistry(arr);
            dojo.event.topic.publish('/calEvent', { 'action': 'eventsAddSuccess', 
                'id': id, 'data': expandEventHash });
        }
        Cal.serv.expandEvent(f, Cal.currentCalendar.path, id, s, e); 
    }
    
    function createEventRegistry(arr) {
        var h = new Hash();
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

