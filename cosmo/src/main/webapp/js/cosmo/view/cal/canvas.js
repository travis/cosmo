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

dojo.provide('cosmo.view.cal.canvas');

dojo.require('dojo.event.*');
dojo.require("dojo.gfx.color.hsv");
dojo.require("cosmo.util.date");
dojo.require('cosmo.ui.event.handlers');
dojo.require('cosmo.ui.draggable');
dojo.require("cosmo.util.i18n");
var _ = cosmo.util.i18n.getText;
dojo.require("cosmo.util.hash");
dojo.require("cosmo.model");
dojo.require("cosmo.ui.resize_area");
dojo.require("cosmo.view.cal");
dojo.require('cosmo.view.cal.Lozenge');
dojo.require("cosmo.view.cal.conflict");

cosmo.view.cal.canvas = new function () {
    
    // Need some closure for scope
    var self = this;
    // Rendering the first time
    var initRender = false; 
    // Resizeable area for all-day events -- a ResizeArea obj
    var allDayArea = null; 
    // Blue, green, red, orange, gold, plum, turquoise, fuschia, indigo
    var hues = [210, 120, 0, 30, 50, 300, 170, 330, 270];

    // Avoid RSI
    function $(id) {
        return document.getElementById(id);
    }
    /**
     * Diagnostic function -- dump the content of an event
     * @param key String, the Hash key for the eventRegistry
     * @param val CalEvent object, the event whose data you
     * want to dump
     */
    function dumpEvData(key, val) {
        Log.print(key);
        Log.print(val.id)
        Log.print(val.data.id);
        Log.print(val.data.title);
    }    
    /**
     * Set the passed calendar event as the selected one on
     * canvas
     * @param ev CalEvent object, the event to select 
     */
    function setSelectedEvent(ev) {
        // Deselect previously selected event if any
        if (self.selectedEvent) {
            self.selectedEvent.lozenge.setDeselected();
        }
        self.selectedEvent = ev; // Pointer to the currently selected event
        ev.lozenge.setSelected(); // Show the associated lozenge as selected
    };
    /**
     * Removes an event lozenge from the canvas -- called in three cases:
     * (1) Actually removing an event from the calendar (this gets
     *     called after the backend successfully removes it)
     * (2) Removing an event from view because it's been edited
     *     to dates outside the viewable span
     * (3) Removing the placeholder event when initial event
     *     creation fails
     * Likely called in a loop with the Hash's 'each' method
     * @param id String, id of the event to be removed
     * @param ev CalEvent obj, the event to be removed
     */
    function removeEventFromDisplay(id, ev) {
        var selEv = self.getSelectedEvent();
        // Remove the lozenge
        ev.lozenge.remove();
        // Remove selection if need be
        if (selEv && (selEv.id == ev.id)) {
            self.selectedEvent = null;
        }
        return true;
    }
    /**
     * Remove a cal event object, usually removes the event
     * lozenge as well
     * @param ev CalEvent object, the event to select 
     * @param rem Boolean, if explicit false is passed,
     * don't remove the lozenge along with the CalEvent obj
     */
    function removeEvent(ev, rem) {
        // Default behavior is to remove the lozenge
        var removeLozenge = rem != false ? true : false;
        self.eventRegistry.removeItem(ev.id);
        if (removeLozenge) {
            removeEventFromDisplay(ev.id, ev);
        }
        ev = null;
    }
    /**
     * Clear the entire eventRegistry, usually clear the
     * lozenges from the canvas as well
     * @param rem Boolean, if explicit false is passed,
     * don't remove the lozenges along with the CalEvent objs
     */
    function removeAllEvents(rem) {
        // Default behavior is to remove the lozenge
        var removeLozenge = rem != false ? true : false;
        var ev = null;
        // Pull the last event off the eventRegistry list and remove it
        if (removeLozenge) {
            self.eventRegistry.each(removeEventFromDisplay);
        } 
        self.eventRegistry = new Hash();
        return true;
    }
    /**
     * Removes a set or sets of recurring events for an id or ids
     * @param reg An eventRegistry Hash from which to remove a group or
     * groups of recurring events
     * @param arr Array of CalEventData ids for the recurrences to
     * remove
     * @param dt A ScoobyDate,represents the end date of a 
     * recurrence -- if the dt param is present, it will remove
     * only the event instances which occur after the date
     * It will also reset the recurrence endDate for all dates
     * to the dt (the new recurrence end date) for all the events
     * that it leaves
     * @param ignore String, the CalEvent id of a single event to ignore from
     * the removal process -- used when you need to leave the
     * master event in a recurrence
     */
    function removeEventRecurrenceGroup(reg, arr, dt, ignore) {
        // Default behavior is to remove the lozenge
        var str = ',' + arr.join() + ',';
        var h = new Hash();
        var ev = null;
        var compDt = dt ? new ScoobyDate(dt.getFullYear(), 
            dt.getMonth(), dt.getDate(), 23, 59) : null;
        while (ev = reg.pop()) {
            var removeForDate = true;
            var keep = false;
            switch (true) {
                // Any to be specifically ignored -- this is all-mighty
                case (ev.id == ignore):
                    keep = true;
                    break;
                // Any that don't have matching ids -- keep these too
                case (str.indexOf(',' + ev.data.id + ',') == -1):
                    keep = true;
                    break;
                // Matching ids -- candidates for removal
                case (str.indexOf(',' + ev.data.id + ',') > -1):
                    // If also filtering by date, check the start date of
                    // matching events as well
                    if (compDt && (ev.data.start.toUTC() < compDt.toUTC())) {
                        keep = true;
                        // Reset the end date for the recurrence on the
                        // events not removed
                        ev.data.recurrenceRule.endDate = new ScoobyDate(dt.getFullYear(),
                            dt.getMonth(), dt.getDate());
                    }
                    break;
                default:
                    // Throw it out
                    break;
            }
            if (keep) { h.setItem(ev.id, ev); }
        }
        return h;
    };
    /**
     * Append an calendar event lozenge to the canvas -- likely
     * called in a loop with the Hash's 'each' method
     * @param key String, the Hash key for the eventRegistry
     * @param val CalEvent obj, value in the eventRegistry
     * for the event getting added to the canvas
     */
    function appendLozenge(key, val) {
        var id = key;
        var ev = val;
        // Create the lozenge and link it to the event
        ev.lozenge = (ev.data.allDay || ev.data.anyTime) ? new cosmo.view.cal.NoTimeLozenge(id) :
            new cosmo.view.cal.HasTimeLozenge(id);
        ev.lozenge.insert(id);
    }
    /**
     * Main function for rendering/re-rendering the cal canvas
     * @ return Boolean, true
     */
    function updateEventsDisplay() {
        if (self.eventRegistry.length && 
            cosmo.view.cal.conflict.calc(self.eventRegistry) && 
                positionLozenges()) {
            // If no currently selected event, put selection on
            // the final one loaded
            if (!self.selectedEvent) {
                dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 
                    'data': self.eventRegistry.getLast() });
            }
            dojo.event.topic.publish('/calEvent', { 'action': 
                'eventsDisplaySuccess', 'data': self.selectedEvent });
        }
        return true;
    }
    /**
     * Call positionLozenges in a loop with Hash's 'each' method
     */
    function positionLozenges() {
        return self.eventRegistry.each(positionLozenge);
    };
    /**
     * Position the lozenge on the canvase based on the 
     * CalEventData props -- happens after they're put on the canvas
     * with appendLozenge. Called in a loop with Hash's 'each' method
     * @param key String, the Hash key for the event in the
     * eventRegistry
     * @param val CalEvent object, the value in the Hash
     */
    function positionLozenge(key, val) {
        ev = val;
        ev.lozenge.updateFromEvent(ev);
        ev.lozenge.updateDisplayMain();
    }
    /**
     * Restores a cal event to it's previous state after:
     * (1) a user cancels an edit
     * (2) an update operation fails on the server
     * Restores the CalEventData from the backup snapshot, and
     * returns the lozenge to its previous position
     * @param ev CalEvent object, the event to restore
     */
    function restoreEvent(ev) {
        if (ev.restoreFromSnapshot()) {
            ev.lozenge.updateFromEvent(ev);
            ev.lozenge.updateDisplayMain();
        }
    }
    /**
     * Convenience method for wiping the cal canvas. Also
     * removes the current event selection
     */
    function wipe() {
        removeAllEvents();
        self.selectedEvent = null;
    }
    /**
     * Render the canvas after successfully loading events
     * from the server -- called for initial load, and for
     * week-to-week navigation
     * @param ev Hash, the eventRegistry of loaded events
     */
    function loadSuccess(ev) {
        removeAllEvents();
        self.eventRegistry = ev;
        self.eventRegistry.each(appendLozenge);
        // Update the view
        updateEventsDisplay();
    }
    /**
     * Handles a successful update of an event, for:
     * (1) Plain ol' single-event upates
     * (2) Removing the recurrence from a recurrence master
     *     (results in a plain, single event).
     * (3) Modifications to recurrences
     * @param cmd JS Object, the command object passed in the 
     * published 'success' event (contains the originally edited
     * event, cmd.data, and the update options, cmd.opts).
     */
    function saveSuccess(cmd) {
        var ev = cmd.data;
        var opts = cmd.opts;
        // Updating existing
        if (!cmd.qualifier.newEvent) {
            if (opts.saveType == 'recurrenceMasterRemoveRecurrence') {
                var h = self.eventRegistry.clone();
                h = removeEventRecurrenceGroup(h, [ev.data.id], null, ev.id);
                removeAllEvents();
                self.eventRegistry = h;
                self.eventRegistry.each(appendLozenge);
            }
            
            // Saved event is still in view
            if (cmd.qualifier.onCanvas) {
                ev.setInputDisabled(false);
                ev.lozenge.updateDisplayMain();
            }
            // Changes have placed the saved event off-canvas
            else if (cmd.qualifier.offCanvas) {
                removeEvent(ev);
            }
            // Neither off-canvas nor on-canvas flag set
            else {
                // Event has one of either start or end date
                // off-canvas, but not both
            }
        }
        // Don't re-render when requests are still processing
        if (!cosmo.view.cal.processingQueue.length) {
            if (cmd.qualifier.newEvent || 
                (cmd.qualifier.onCanvas && opts.saveType != 'instanceOnlyThisEvent')) {
                dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 
                    'data': cosmo.view.cal.lastSent });
            }
            updateEventsDisplay();
        }
    }
    /**
     * Handles a successful update of an event, for recurring
     * events -- for:
     * (1) Edits to an entire recurrence, 'All Events'
     * (2) Breaking a recurrence and creating a new event,
     * 'All Future Events' -- the new event may or may not 
     * have recurrence
     * @param data JS Object, the data passed from the published
     * 'success' event (data.idArr, the array of event ids to use
     * for removing the recurrence instances; data.eventRegistry,
     * the Hash of expanded events; data.saveEvent, the originally
     * clicked-on event; data.opts, the JS Object of options that
     * tells you what kind of save operation is happening)
     */
    function addSuccess(data) {
        var idArr = data.idArr;
        var evReg = data.eventRegistry;
        var ev = data.saveEvent;
        var opts = data.opts;
        var h = null;
        var idArr = [];
        
        // Copy the eventRegistry to remove any recurrence
        // instances associated with the edit
        h = self.eventRegistry.clone();
        
        // Splitting the recurrence, new event is set to frequency
        // of 'once' -- just remove previous recurrence instances
        // and keep original single lozenge for new event
        if (opts.saveType == 'instanceAllFuture' &&
            !ev.data.recurrenceRule) {
            // Master recurrence event id
            idArr.push(opts.masterEventDataId);
            h = removeEventRecurrenceGroup(h, idArr, opts.recurEnd, ev.id);
            removeAllEvents();
            self.eventRegistry = h;
            self.eventRegistry.each(appendLozenge);
        }
        // Normal split where original and new are both recurring, or
        // Change to master that just updates all of a single recurrence
        else {
            // 'All Future Events' 
            if (opts.saveType == 'instanceAllFuture') {
                // Master recurrence event id
                idArr.push(opts.instanceEvent.data.id);
                // id for first event in new recurrence
                idArr.push(ev.data.id);
            }
            // 'All Events'
            else {
                // Master recurrence id
                idArr.push(ev.data.id);
            }
            
            // Remove some of all of the recurrence instances for re-render
            // ----------------------
            // 'All Events' -- remove all instances of the recurrence
            if (opts.saveType == 'recurrenceMaster') {
                // Before wiping, remember the position of the currently selected
                // event. If the canvas renders immediately after this update, 
                // we need to keep the selection where it was.
                var currSel = self.eventRegistry.getPos(self.selectedEvent.id);
                h = removeEventRecurrenceGroup(h, idArr);
            }
            // 'All Future Events' -- remove instances appearing after the
            // new end date
            else if (opts.saveType == 'instanceAllFuture') {
                h = removeEventRecurrenceGroup(h, idArr, opts.recurEnd);
            }
            
            // Remove the original clicked-on event -- the new master
            // will be in the recurrence expansion from the server
            // ----------------------
            if (opts.saveType == 'instanceAllFuture' || 
                opts.saveType == 'singleEventAddRecurrence') {
                h.removeItem(opts.instanceEvent.id);
            }
            // Append the new recurrence expansions from the server
            // onto the eventRegistry
            h.append(evReg);
            // Clear the canvas
            removeAllEvents();
            // Swap out the eventRegistry with the new one
            self.eventRegistry = h;
            // Stick all the event lozenges on the canvas
            self.eventRegistry.each(appendLozenge);
        }
        
        // Repaint and restore selection if there are no
        // more requests processing
        if (!cosmo.view.cal.processingQueue.length) {
            updateEventsDisplay();
            if (!cosmo.view.cal.lastSent) {
                // ==========================================================
                // Nasty, tricksy selection fu -- daily recurrence can have 
                // multiple events, but can't move off of their day
                // weekly and above can move off their day, but will only
                // have a single instance visible on the canvas
                // 'instanceAllFuture' creates a new event that may or may
                // not recur
                // ==========================================================
                // Either a master recurring or a recurring new event after 
                // breaking recurrence -- figuring selection is a party  
                if (ev.data.recurrenceRule) {
                    ev = evReg.getAtPos(0);
                    // If changing a recurrence-end results in the expansion 
                    // ending before the start of the orignal clicked instance, 
                    // lozenge selection needs to go somewhere
                    if (opts.instanceEvent && ev.data.recurrenceRule.endDate && 
                        (ev.data.recurrenceRule.endDate.toUTC() < 
                            opts.instanceEvent.data.start.toUTC())) {
                        ev =  self.eventRegistry.getLast(); 
                    }
                    else {
                        // Daily recurrence events
                        if (ev.data.recurrenceRule.frequency == 'daily') {
                            // Persist selection when editing an instance, and 
                            // selecting 'All Events'
                            if (opts.saveType == 'recurrenceMaster' && 
                                opts.instanceEvent && opts.instanceEvent.data.instance) {
                                ev = self.eventRegistry.getAtPos(currSel);
                            }
                            else {
                                // Do nothing -- if original selection was on the master
                                // event, selection just stays on the first item in the
                                // recurrence expansion
                            }
                        }
                    }
                }
                dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 
                    'data': ev });
            }
            else {
                dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 
                    'data': cosmo.view.cal.lastSent });
            }
        }
    }
    /**
     * Handles a successful removal of an event 
     * @param ev CalEvent object, the removed event 
     * @param opts JS Object, options for the removal that
     * tell you what kind of remove is happening
     */
    function removeSuccess(ev, opts) {
        if (opts.removeType == 'recurrenceMaster' || 
            opts.removeType == 'instanceAllFuture') {
            var h = self.eventRegistry.clone();
            var dt = null;
            if (opts.removeType == 'instanceAllFuture') {
                dt = opts.recurEnd;
            }
            h = removeEventRecurrenceGroup(h, [ev.data.id], dt);
            removeAllEvents();
            self.eventRegistry = h;
            self.eventRegistry.each(appendLozenge);
        }
        else {
            removeEvent(ev);
        }
        updateEventsDisplay();
    }
    /**
     * Single-clicks -- do event dispatch based on ID of event's
     * DOM-element source. Includes selecting/moving/resizing event lozenges
     * and resizing all-day event area
     * Mousedown on an event always creates a Draggable in anticipation
     * of a user dragging the lozenge. Draggable is destroyed on mouseup.
     * cosmo.app.dragItem may either be set to this Draggable, or to
     * the ResizeArea for all-day events
     */
    function mouseDownHandler(e) {
        var id = '';
        var dragItem = null;
        var elem = null;
        var selObj = null;
        var s = '';
        var elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
        var id = elem.id;
        // ======================================
        // Event dispatch
        // ======================================
        switch (true) {
            // Mouse down on the hour columns -- exit to prevent text selection
            case (id.indexOf('hourDiv') > -1):
                return false;
                break;
            
            // On event lozenge -- simple select, or move/resize
            case (id.indexOf('eventDiv') > -1):
                // Get the clicked-on event
                s = Cal.getIndexEvent(id);
                selObj = cosmo.view.cal.canvas.eventRegistry.getItem(s);

                // If this object is currently in 'processing' state, ignore any input
                if (selObj.getInputDisabled()) {
                    return false;
                }
                
                // Publish selection
                var c = cosmo.view.cal.canvas;
                if (c.selectedEvent && selObj.id != c.selectedEvent.id) {
                    dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 'data': selObj });
                }
                
                // No move/resize for read-only collections
                if (Cal.currentCollection.privileges.write) {
                    // Set up Draggable and save dragMode -- user may be dragging
                    if (id.indexOf('AllDay') > -1) {
                        dragItem = new NoTimeDraggable(s);
                    }
                    else {
                        dragItem = new HasTimeDraggable(s);
                    }
                    
                    switch(true) {
                        // Main content area -- drag entire event
                        case id.indexOf('Content') > -1:
                        case id.indexOf('Title') > -1:
                        case id.indexOf('Start') > -1:
                            dragItem.init('drag');
                            break;
                        // Top lip -- resize top
                        case id.indexOf('Top') > -1:
                            dragItem.init('resizetop');
                            break;
                        // Bottom lip -- resize bottom
                        case id.indexOf('Bottom') > -1:
                            dragItem.init('resizebottom');
                            break;
                        default:
                            // Do nothing
                            break;
                    }
                    
                    // Set the Cal draggable to the dragged lozenge
                    cosmo.app.dragItem = dragItem;
                }
                break;
        }
    }
    /**
     * Double-clicks -- if event source is in the scrolling area
     * for normal events, or in the resizeable all-day event area
     * calls insertCalEventNew to create a new event
     * FIXME: This could be segregated into two functions
     * attached to each of the two canvas areas
     */
    function dblClickHandler(e) {
        var id = '';
        var elem = null;
        
        // Event creation only in write-mode
        if (Cal.currentCollection.privileges.write) {
            e = !e ? window.event : e;
            elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
            id = elem.id
            
            switch (true) {
                // On hour column -- create a new event
                case (id.indexOf('hourDiv') > -1):
                // On all-day column -- create new all-day event
                case (id.indexOf('allDayListDiv') > -1):
                    Cal.insertCalEventNew(id);
                    break;
                // On event title -- edit-in-place
                case (id.indexOf('eventDiv') > -1):
                    // Edit-in-place will go here
                    break;
            }
        }
    }
    
    // Subscribe to the '/calEvent' channel
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    
    /**
     * Handle events published on the '/calEvent' channel, including
     * self-published events
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published event.
     */
    this.handlePub = function (cmd) {
        var act = cmd.action;
        var opts = cmd.opts;
        var data = cmd.data;
        switch (act) {
            case 'eventsLoadPrepare':
                self.render(data.startDate, data.endDate, data.currDate);
                break;
            case 'eventsLoadStart':
                self.calcColors();
                wipe();
                break;
            case 'eventsLoadSuccess':
                var ev = cmd.data;
                loadSuccess(ev);
                break;
            case 'eventsAddSuccess':
                addSuccess(cmd.data);
                break;
            case 'setSelected':
                var ev = cmd.data;
                setSelectedEvent(ev);
                break;
            case 'save':
                // Do nothing
                break; 
            case 'saveFailed':
                var ev = cmd.data;
                // If the failure was a new event, remove
                // the placeholder lozenge
                if (cmd.qualifier.newEvent) {
                    removeEvent(ev);
                }
                // Otherwise put it back where it was and
                // restore to non-processing state
                else {
                    var rEv = null;
                    // Recurrence, 'All events'
                    if (opts.saveType == 'recurrenceMaster' || 
                        opts.saveType == 'instanceAllFuture') {
                        // Edit ocurring from one of the instances
                        if (opts.instanceEvent) {
                            rEv = opts.instanceEvent
                        }
                        // Edit occurring from the actual master
                        else {
                            rEv = ev;
                        }
                    }
                    // Single event
                    else {
                        var rEv = ev;
                    }
                    restoreEvent(rEv);
                    rEv.setInputDisabled(false);
                }
                break;
            case 'saveSuccess':
                saveSuccess(cmd);
                break;
            case 'removeSuccess':
                var ev = cmd.data;
                removeSuccess(ev, opts)
                break;
            case 'removeFailed':
                var ev = cmd.data;
                
                break;
            default:
                // Do nothing
                break;
        }
    };
    
    // Public props
    // ****************
    // Width of day col in week view, width of event lozenges --
    // Calc'd based on client window size
    // Other pieces of the app use this, so make it public
    this.dayUnitWidth = 0;
    // The list currently displayed events
    // on the calendar -- key is the CalEvent obj's id prop,
    // value is the CalEvent
    this.eventRegistry = new Hash();
    // Currently selected event
    this.selectedEvent = null;
    this.colors = {};
    
    // Public methods
    // ****************
    /**
     * Main rendering function for the calendar canvas called
     * on init load, and week-to-week nav
     * @param vS Date, start of the view range
     * @param vE Date, end of the view range
     * @param cD Date, the current date on the client
     */
    this.render = function (vS, vE, cD) {
        var viewStart = vS;
        var viewEnd = vE;
        var currDate = cD;
        // Key container elements
        var monthHeaderNode = null;
        var timelineNode = null;
        var hoursNode = null;
        var dayNameHeadersNode = null;
        var allDayColsNode = null;
        
        /**
         * Set up key container elements 
         * @return Boolean, true.
         */
        function init() {
            // Link local variables to DOM nodes
            monthHeaderNode = $('monthHeaderDiv');
            timelineNode = $('timedHourListDiv');
            dayNameHeadersNode = $('dayListDiv');
            hoursNode = $('timedContentDiv');
            allDayColsNode = $('allDayContentDiv');

            // All done, woot
            return true;
        }
        
        /**
         * Shows list of days at the head of each column in the week view
         * Uses the Date.abbrWeekday array of names in date.js
         * @return Boolean, true.
         */
        function showDayNameHeaders() {
            var str = '';
            var start = HOUR_LISTING_WIDTH + 1;
            var idstr = '';
            var startdate = viewStart.getDate();
            var startmon = viewStart.getMonth();
            var daymax = daysInMonth(startmon+1);
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());
            
             // Returns the number of days in a specific month of a specific year --
             // the year is necessary to handle leap years' Feb. 29
             function daysInMonth(month, year){
                var days = 0;
                switch (month) {
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        days = 30;
                        break;
                    case 2:
                        if (year % 4 == 0){
                               days = 29;
                           }
                           else{
                               days = 28;
                          }
                          break;
                      default:
                          days = 31;
                          break;
                }
                return days;
            }
            
            // Spacer to align with the timeline that displays hours below
            // for the timed event canvas
            str += '<div id="dayListSpacer" class="dayListDayDiv"' +
                ' style="left:0px; width:' +
                (HOUR_LISTING_WIDTH - 1) + 'px; height:' +
                (DAY_LIST_DIV_HEIGHT-1) +
                'px;">&nbsp;</div>';

            // Do a week's worth of day cols with day name and date
            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                startdate = startdate > daymax ? 1 : startdate;
                // Subtract one pixel of height for 1px border per retarded CSS spec
                str += '<div class="dayListDayDiv" id="dayListDiv' + i +
                    '" style="left:' + start + 'px; width:' + (self.dayUnitWidth-1) +
                    'px; height:' + (DAY_LIST_DIV_HEIGHT-1) + 'px;';
                if (calcDay.getTime() == currDay.getTime()) {
                    str += ' background-image:url(' + cosmo.env.getImagesUrl() + 
                        'day_col_header_background.gif); background-repeat:' +
                        ' repeat-x; background-position:0px 0px;'
                }
                str += '">';
                str += Date.abbrWeekday[i] + '&nbsp;' + startdate;
                str += '</div>\n';
                start += self.dayUnitWidth;
                startdate++;
            }
            dayNameHeadersNode.innerHTML = str;
            return true;
        }

        /**
         * Draws the day columns in the resizeable all-day area
         * @return Boolean, true.
         */
        function showAllDayCols() {
            var str = '';
            var start = 0;
            var idstr = ''
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());

            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                str += '<div class="allDayListDayDiv';
                if (calcDay.getTime() == currDay.getTime()) {
                    str += ' currentDayDay'
                }
                str +='" id="allDayListDiv' + i +
                '" style="left:' + start + 'px; width:' +
                (cosmo.view.cal.canvas.dayUnitWidth-1) + 'px;">&nbsp;</div>';
                start += cosmo.view.cal.canvas.dayUnitWidth;
            }
            str += '<br style="clear:both;"/>';
            allDayColsNode.innerHTML = str;
            return true;
        }

        /**
         * Draws the 12 AM to 11 PM hour-range in each day column
         * @return Boolean, true.
         */
        function showHours() {
            var str = '';
            var row = '';
            var start = 0;
            var idstr = '';
            var hour = 0;
            var meridian = '';
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());
            var isCurrentDay = false;
            var viewDiv = null;
            var timeLineWidth = 0;
            var workingHoursBarWidth = 3;
            
            // Subtract one px for border per asinine CSS spec
            var halfHourHeight = (HOUR_UNIT_HEIGHT/2) - 1;
            
            var w = '';
            // Working/non-working hours line
            w += '<div class="';
            w += (j < 8 || j > 17) ? 'nonWorkingHours' : 'workingHours';
            w += '" style="width:' + workingHoursBarWidth + 
                'px; height:' + (halfHourHeight+1) + 
                'px; float:left; font-size:1px;">&nbsp;</div>';
            var workingHoursLine = '';

            str = '';
            viewDiv = timelineNode; 
            timeLineWidth = parseInt(viewDiv.offsetWidth);
            // Subtract 1 for 1px border
            timeLineWidth = timeLineWidth - workingHoursBarWidth - 1;
            
            // Timeline of hours on left
            for (var j = 0; j < 24; j++) {
                hour = j == 12 ? _('App.Noon') : cosmo.util.date.hrMil2Std(j);
                meridian = j > 11 ? ' PM' : ' AM';
                meridian = j == 12 ? '' : '<span>' + meridian + '</span>';
                row = '';
                
                // Upper half hour
                // ==================
                row += '<div class="hourDivTop';
                row += '" style="height:' + 
                    halfHourHeight + 'px; width:' + 
                    timeLineWidth + 'px; float:left;">';
                // Hour plus AM/PM
                row += '<div class="hourDivSubLeft">' + hour + 
                    meridian + '</div>';
                row += '</div>\n';
                row += workingHoursLine;
                row += '<br class="clearAll"/>'
                
                idstr = i + '-' + j + '30';
                
                // Lower half hour
                // ==================
                row += '<div class="hourDivBottom"';
                // Make the noon border thicker
                if (j == 11) { 
                    row += ' style="height:' + (halfHourHeight-1) + 
                        'px; border-width:2px;';
                }
                else {
                    row += ' style="height:' + halfHourHeight + 'px;';
                }
                row += ' width:' + timeLineWidth + 
                    'px; float:left;">&nbsp;</div>\n';
                row += workingHoursLine;
                row += '<br class="clearAll"/>'
                
                str += row;
            }
            viewDiv.innerHTML = str;

            str = '';
            viewDiv = hoursNode;

            // Do a week's worth of day cols with hours
            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                str += '<div class="dayDiv" id="dayDiv' + i +
                    '" style="left:' + start + 'px; width:' +
                    (cosmo.view.cal.canvas.dayUnitWidth-1) +
                    'px;"';
                str += '>';
                for (var j = 0; j < 24; j++) {
                    
                    isCurrentDay = (calcDay.getTime() == currDay.getTime());
                    
                    idstr = i + '-' + j + '00';
                    row = '';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivTop';
                    // Highlight the current day
                    if (isCurrentDay) {
                        row += ' currentDayDay'
                    }
                    // Non-working hours are gray
                    else if (j < 8 || j > 18) {
                        //row += ' nonWorkingHours';
                    }
                    row += '" style="height:' + halfHourHeight + 'px;">';
                    row += '</div>\n';
                    idstr = i + '-' + j + '30';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivBottom';
                    // Highlight the current day
                    if (isCurrentDay) {
                        row += ' currentDayDay'
                    }
                    // Non-working hours are gray
                    else if (j < 8 || j > 18) {
                        //row += ' nonWorkingHours';
                    }
                    row += '" style="';
                    if (j == 11) { 
                        row += 'height:' + (halfHourHeight-1) + 
                            'px; border-width:2px;';
                    }
                    else {
                        row += 'height:' + halfHourHeight + 'px;';
                    }
                    row += '">&nbsp;</div>';
                    str += row;
                }
                str += '</div>\n';
                start += cosmo.view.cal.canvas.dayUnitWidth;
            }

            viewDiv.innerHTML = str;
            return true;
        }
        /**
         * Displays the month name at the top
         */
        function showMonthHeader() {
            var vS = viewStart;
            var vE = viewEnd;
            var mS = vS.getMonth();
            var mE = vE.getMonth();
            var headerDiv = monthHeaderNode; 
            var str = '';

            // Format like 'March-April, 2006'
            if (mS < mE) {
                str += vS.strftime('%B-');
                str += vE.strftime('%B %Y');
            }
            // Format like 'December 2006-January 2007'
            else if (mS > mE) {
                str += vS.strftime('%B %Y-');
                str += vE.strftime('%B %Y');
            }
            // Format like 'April 2-8, 2006'
            else {
                str += vS.strftime('%B %Y');
            }
            if (headerDiv.firstChild) {
                headerDiv.removeChild(headerDiv.firstChild);
            }
            headerDiv.appendChild(document.createTextNode(str));
        }
        
        // Do it!
        // -----------
        if (!initRender) {
            // Make the all-day event area resizeable
            // --------------
            allDayArea = new cosmo.ui.resize_area.ResizeArea(
                'allDayResizeMainDiv', 'allDayResizeHandleDiv');
            allDayArea.init('down');
            allDayArea.addAdjacent('timedScrollingMainDiv');
            allDayArea.setDragLimit();
        }
        else {
            removeAllEvents();
        }
        
        // Init and call all the rendering functions
        init();
        showMonthHeader();
        showDayNameHeaders();
        showAllDayCols();
        
        // Create event listeners
        if (!initRender) {
            dojo.event.connect(hoursNode, 'onmousedown', mouseDownHandler);
            dojo.event.connect(allDayColsNode, 'onmousedown', mouseDownHandler);
            dojo.event.connect(hoursNode, 'ondblclick', dblClickHandler);
            dojo.event.connect(allDayColsNode, 'ondblclick', dblClickHandler);
            
            showHours();
        }

        initRender = true;
    };
    /**
     * Get the scroll offset for the timed canvas
     * @return Number, the pixel position of the top of the timed
     * event canvas, including the menubar at the top, resizing of 
     * the all-day area, and any amount that the timed canvas
     * has scrolled.
     */
    this.getTimedCanvasScrollTop = function () {
        // Has to be looked up every time, as value may change
        // either when user scrolls or resizes all-day event area
        var top = $('timedScrollingMainDiv').scrollTop;
        // FIXME -- viewOffset is the vertical offset of the UI
        // with the top menubar added in. This should be a property
        // of render context that the canvas can look up
        top -= Cal.viewOffset;
        // Subtract change in resized all-day event area
        top -= (allDayArea.dragSize - allDayArea.origSize);
        return top;
    };
    /**
     * Get the currently selected event -- might be okay to allow
     * direct access to the property itself, but with a getter
     * you could trigger certain events if neeeded whenever something
     * grabs the selected event
     * @return CalEvent object, the currently selected event
     */
    this.getSelectedEvent = function () {
        return self.selectedEvent;
    };
    this.calcColors = function () {
        var getRGB = function (h, s, v) {
            var rgb = dojo.gfx.color.hsv2rgb(h, s, v, { 
                inputRange: [360, 100, 100], outputRange: 255 });
            return 'rgb(' + rgb.join() + ')';
        }
        var lozengeColors = {};
        var sel = cosmo.ui.cal_main.Cal.calForm.form.calSelectElem;
        var index = sel ? sel.selectedIndex : 0;
        var hue = hues[index];
        
        var o = {
            darkSel: [100, 80],
            darkUnsel: [80, 90],
            lightSel: [25, 100], 
            lightUnsel: [10, 100],
            proc: [30, 90]
        };
        for (var p in o) {
            lozengeColors[p] = getRGB(hue, o[p][0], o[p][1]);
        }
        this.colors = lozengeColors;
    };
    /**
     * Clean up event listeners and DOM refs
     */
    this.cleanup = function () {
        // Let's be tidy
        self.eventRegistry = null;
        if (allDayArea){
            allDayArea.cleanup();
        }
    };
}
cosmo.view.cal.canvas.constructor = null;

// Cleanup
dojo.event.browser.addListener(window, "onunload", cosmo.view.cal.canvas.cleanup, false);


