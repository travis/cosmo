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
    };
    function saveEventChanges(ev, qual) {
        var opts = self.recurringEventOptions;
        
        // Lozenge stuff
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
                case opts.ALL_EVENTS:
                    var saveEv = null;
                    if (ev.data.masterEvent) {
                        f = function() { doSaveSingleEvent(ev) }
                    }
                    else {
                        h = function(eData) {
                            // Master event's CalEventData is e
                            // Current instance's CalEvent is ev
                            var masterStart = eData.start;
                            var origStart = ev.dataOrig.start;
                            var newStart = ev.data.start;
                            // Date parts for the edited instance start
                            var mon = newStart.getMonth();
                            var dat = newStart.getDate();
                            var hou = newStart.getHours();
                            var min = newStart.getMinutes();
                            
                            // Mod start based on edited instance
                            switch ( ev.data.recurrenceRule.frequency) {
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
                            masterStart.setHours(min);
                            masterStart.setMinutes(min);
                            //doSaveSingleEvent(eData);
                        };
                        var saveEv = Cal.serv.getEvent(h, Cal.currentCalendar.path, ev.data.id);
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
            return;
        }
        // Normal one-shot event
        else {
            f = function() { doSaveSingleEvent(ev) }
        }
        
        // Give a sec for the processing state to show
        setTimeout(f, 500);
        
        // Kill any confirmation dialog that might be showing
        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }
    };
    function doSaveSingleEvent(e) {
        var d = e.data ? e.data : e; // Input is either a CalEvent or CalEventData
        var f = function(newEvId, err, reqId) { 
            handleSaveResult(ev, newEvId, err, reqId); };
        var requestId = null;
        
        requestId = Cal.serv.saveEvent(
            f, Cal.currentCalendar.path, d);
    };
    function handleSaveResult(ev, newEvId, err, reqId) {
        var saveEv = ev;
        // Simple error message to go along with details from Error obj
        var errMsg = '';
        
        // Failure -- display exception info
        // ============
        if (err) {
            // Failed update -- fall back to original state
            if (saveEv.dataOrig) {
                // Restore from backup snapshot
                saveEv.restoreEvent();
                // Re-enable user input on this event
                saveEv.setInputDisabled(false);
                errMsg = getText('Main.Error.EventEditSaveFailed');
            }
            // Failed create -- remove fake placeholder event and block
            else {
                // Remove all the client-side stuff associated with this event
                errMsg = getText('Main.Error.EventNewSaveFailed');
            }
            // Broadcast failure
            dojo.event.topic.publish('/calEvent', { 'action': 'saveFailed', 
                'data': saveEv });
            
            Cal.showErr(errMsg, err);
        }
        // Success
        // ============
        else {
            // Set the CalEventData ID from the value returned by server
            if (!saveEv.data.id) {
                saveEv.data.id = newEvId;
            }
            
            // If new dates are out of range, remove the event from display
            if (saveEv.isOutOfViewRange()) {
                // Broadcast success
                dojo.event.topic.publish('/calEvent', { 'action': 'saveSuccess', 
                    'qualifier': 'offCanvas', 'data': saveEv });
            }
            // Otherwise update display
            else {
                // Broadcast success
                dojo.event.topic.publish('/calEvent', { 'action': 'saveSuccess', 
                    'qualifier': 'onCanvas', 'data': saveEv });
            }
        }
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
    };
    
    // Remove
    // =========================
    function removeEventConfirm(ev) {
        var str = '';
        // Recurrence is a ball-buster
        if (ev.data.recurrenceRule) {
            str = 'removeRecurConfirm';
        }
        else {
            str = 'removeConfirm';
        }
        Cal.showDialog(cosmo.view.cal.dialog.getProps(str));
    };
    function removeEvent(ev) {
        doRemove(ev);
        
        // No currently selected event
        cosmo.view.cal.canvas.selectedEvent = null;
        
        // Kill any confirmation dialog that might be showing
        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }
    };
    function doRemove(ev) {
        var f = function(newEvId, err, reqId) { 
            handleRemoveResult(ev, newEvId, err, reqId); };
        var requestId = Cal.serv.removeEvent(
            f, Cal.currentCalendar.path, ev.data.id);
    };
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
        
        // Update entire display of events
        //Cal.updateEventsDisplay();
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
    };
    
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
                if (qual) {
                    switch(qual) {
                        case opts.ALL_EVENTS:
                            
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
                    removeEvent(ev);
                }
                break;
        }
    };
};

