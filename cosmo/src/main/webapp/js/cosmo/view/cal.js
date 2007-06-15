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

dojo.require("cosmo.app.pim");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.model");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.util");
dojo.require('cosmo.view.cal.dialog');
dojo.require("cosmo.service.exception");

dojo.require("cosmo.util.debug");

cosmo.view.cal = dojo.lang.mixin(new function(){

    var self = this;
    var ranges = {
        'daily': [dojo.date.dateParts.DAY, 1],
        'weekly': [dojo.date.dateParts.WEEK, 1],
        'biweekly': [dojo.date.dateParts.WEEK, 2],
        'monthly': [dojo.date.dateParts.MONTH, 1],
        'yearly': [dojo.date.dateParts.YEAR, 1]
    }
    // Public attributes
    // ********************
    // What view we're using -- currently only week view exists
    this.viewtype = 'week';
    // Start date for events to display
    this.viewStart = null;
    // End date for events to display
    this.viewEnd = null;
    // Options for saving/removing recurring events
    this.recurringEventOptions = {
        ALL_EVENTS: 'master',
        ALL_FUTURE_EVENTS: 'occurrenceAndFuture',
        ONLY_THIS_EVENT: 'occurrence'
    };
    // How many updates/removals are in-flight
    this.processingQueue = [];
    // Last clicked cal event -- used for selection persistence.
    this.lastSent = null;


    // Saving changes
    // =========================
    /**
     * Main function call for saving new events or changes to existing
     * events -- invokes confirmation dialog for changes to event properties
     * for recurring events (changes to the recurrence rule spawn no dialog).
     * For normal events, this simply passes through to saveEventChanges
     * @param ev A CalEvent object, the event to be saved.
     */
    function saveEventChangesConfirm(ev, delta) {
        var changeTypes = delta.getApplicableChangeTypes();
        var size = 0;
        var change = null;
        for (change in changeTypes){
            dojo.debug("changeType: " +change);
            if (changeTypes[change]){
                size++;
            }
        }
        
        //only one type of change
        if (size == 1){
            delta.applyChangeType(change);
            dojo.event.topic.publish('/calEvent', {action:'save', data:ev, delta:delta});
        } else {
          cosmo.app.showDialog(cosmo.view.cal.dialog.getProps('saveRecurConfirm', {changeTypes:changeTypes, delta:delta, saveItem:ev}));
        }
    }
    
    /**
     * Called for after passthrough from saveEventChangesConfirm. Routes to
     * the right save operation (i.e., for recurring events, 'All Future,'
     * 'Only This Event,' etc.)
     * @param ev A CalEvent object, the object to be saved.
     * @param qual String, flag for different variations of saving
     * recurring events. May be one of the three recurringEventOptions, or
     * the two special cases of adding recurrence to a single event or
     * removing recurrence completely from a master event for a recurrence.
     */
    function saveEventChanges(ev, qual, delta) {
        dojo.debug("save event changes...");
        // f is a function object gets set based on what type
        // of edit is occurring -- executed from a very brief
        // setTimeout to allow the 'processing ...' state to
        // display
        var f = null;
        
        // A second function object used as a callback from
        // the first f callback function -- used when the save
        // operation needs to be made on a recurrence instance's
        // master event rather than the instance -- basically
        // just a way of chaining two async calls together
        var h = null;

        // Kill any confirmation dialog that might be showing
        if (cosmo.app.modalDialog.isDisplayed) {
            cosmo.app.hideDialog();
        }

        var opts = self.recurringEventOptions;

        // Recurring event
        if (qual) {
            switch(qual) {
                // Changing the master event in the recurring sequence
                case opts.ALL_EVENTS:
                    dojo.debug("ALL_EVENTS");
                    delta.applyToMaster();
                    f = function(){
                        doSaveEvent(ev, 
                         { 
                            'saveType': opts.ALL_EVENTS,
                            'delta': delta
                         });
                    };
                    break;

                // Break the previous recurrence and start a new one
                case opts.ALL_FUTURE_EVENTS:
                    dojo.debug("ALL_FUTURE");
                    var newItem  = delta.applyToOccurrenceAndFuture();
                    f = function () { 
                        doSaveEvent(ev, 
                        { 
                          'saveType': opts.ALL_FUTURE_EVENTS,
                          'delta': delta,
                          'newItem': newItem 
                        }); 
                    };
                    break;

                // Modifications
                case opts.ONLY_THIS_EVENT:
                    dojo.debug("ONLY_THIS_EVENT");
                    var note = ev.data;
                    var recurrenceId = note.recurrenceId;
                    var master = note.getMaster();
                    var newModification = false;
                    if (!master.getModification(recurrenceId)){
                         newModification = true;    
                    }
                    delta.applyToOccurrence();
                    f = function () { 
                        doSaveEvent(ev, 
                        { 
                          'saveType': opts.ONLY_THIS_EVENT,
                          'delta': delta,
                          'newModification': newModification
                        }); 
                    };
                    break;
                    
                case 'new':
                    dojo.debug("doSaveChanges: new")
                    f = function () { doSaveEvent(ev, { 'new': true } ) };
                    break;
                default:
                    break;
            }
        }
        // Normal one-shot event
        else {
            dojo.debug("saveEventChanges: normal sone shot event");
            f = function () { doSaveEvent(ev, { } ) };
        }

        // Give a sec for the processing state to show
        dojo.debug("before set timeout");
        setTimeout(f, 500);
    }
    /**
     * Call the service to do a normal event save.
     * Creates an anonymous function to pass as the callback for the
     * async service call that saves the changes to the event.
     * Response to the async request is handled by handleSaveEvent.
     * @param ev A CalEvent object, the event to be saved.
     * @param opts An Object, options for the save operation.
     */
     //BOBBYNOTE: Can we collapse this into "saveEventChanges" ?
    function doSaveEvent(ev, opts) {
        dojo.debug("doSaveEvent");
        var OPTIONS = self.recurringEventOptions;

        var deferred = null;
        var requestId = null;
        var isNew = opts['new'] || false;
        var note = ev.data;
        var delta = opts.delta;
        var newItem = opts.newItem;
        dojo.debug("Do save: savetype: " + opts.saveType)
        dojo.debug("Do save: iznew: " + isNew)

        if (isNew){
            deferred = cosmo.app.pim.serv.createItem(note, cosmo.app.pim.currentCollection,{});
        } else if (opts.saveType) {
            dojo.debug("opty! " + opts.saveType );
            switch(opts.saveType){
                case OPTIONS.ALL_EVENTS:
                    dojo.debug("about to save note in ALL EVENTS")
                    deferred = cosmo.app.pim.serv.saveItem(note.getMaster());
                    break;
                case OPTIONS.ALL_FUTURE_EVENTS:
                    dojo.debug("about to save note in ALL FUTURE EVENTS")
                    var newItemDeferred = cosmo.app.pim.serv.
                        createItem(newItem, cosmo.app.pim.currentCollection);
                    var requestId = newItemDeferred.id;
                    self.processingQueue.push(requestId);
                    
                    newItemDeferred.addCallback(function(){
                        dojo.debug("in newItemDeferred call back")
                        if (newItemDeferred.results[1] != null){
                            //if there was an error, pass it to handleSaveEvent, with the original 
                            //item
                            handleSaveEvent(ev, newItemDeferred.results[1], requestId,opts.saveType, delta);
                        } else {
                            //get rid of the id from the processing queue
                            self.processingQueue.shift()

                            //success at saving new item (the one broken off from the original recurrence chain! 
                            // Now let's save the original.
                            var originalDeferred = cosmo.app.pim.serv.saveItem(note.getMaster());
                            originalDeferred.addCallback(function(){
                                handleSaveEvent(ev,
                                    originalDeferred.results[1], 
                                    originalDeferred.id, 
                                    opts.saveType, 
                                    delta,
                                    newItem); 
                            });
                            self.processingQueue.push(originalDeferred.id);
                            self.lastSent = ev;
                        }
                    });
                    
                    dojo.debug("about to save note in ALL FUTURE EVENTS")
                    return;
                case OPTIONS.ONLY_THIS_EVENT:
                     if (!opts.newModification){
                         dojo.debug("doSaveEvent: saving a previously created modificaiton")
                         deferred = cosmo.app.pim.serv.saveItem(note);
                     } else {
                         dojo.debug("doSaveEvent: creating a new modificaiton")
                         deferred = cosmo.app.pim.serv.createItem(note, cosmo.app.pim.currentCollection);
                     }
                break;
            }
        } else {
            dojo.debug("normal, non-recurring event")
            deferred = cosmo.app.pim.serv.saveItem(note);
        }

        var f = function () {
            var saveType = isNew ? "new" : opts.saveType;
            dojo.debug("callback: saveType="+ saveType)
            handleSaveEvent(ev, deferred.results[1], deferred.id, saveType, delta); 
        };
        
        deferred.addCallback(f);
        requestId = deferred.id;
        
        // Add to processing queue -- canvas will not re-render until
        // queue is empty
        self.processingQueue.push(requestId);

        // Selection persistence
        // --------------------
        // In these cases, the events concerned will be re-rendered
        // after re-expanding the recurrence on the server -- no
        // way to preserve the original selection pointer. This means
        // that figuring out where selection goes will require some
        // calcluation
        if (opts.saveType == 'recurrenceMaster' ||
            opts.saveType == 'singleEventAddRecurrence') {
            self.lastSent = null;
        }
        // Just remember the original event that held the selection
        // we'll keep it there after re-render
        else {
            self.lastSent = ev;
        }
    }

    /**
     * Handles the response from the async call when saving changes
     * to events.
     * @param ev A CalEvent object, the original event clicked on,
     * or created by double-clicking on the cal canvas.
     * @param newEvId String, the id for the event returned when creating a
     * new event
     * @param err A JS object, the error returned from the server when
     * a save operation fails.
     * @param reqId Number, the id of the async request.
     * @param optsParam A JS Object, options for the save operation.
     */
    function handleSaveEvent(ev, err, reqId, saveType, delta, newItem) {
        dojo.debug("handleSaveEvent");
        var OPTIONS = self.recurringEventOptions;
        var errMsg = '';
        var act = '';

        if (err) {
            dojo.debug("handleSaveEvent: err");
            // Failure -- display exception info
            act = 'saveFailed';
            // Failed update
            if (saveType != "new") {
                errMsg = _('Main.Error.EventEditSaveFailed');
            }
            // Failed create
            else {
                errMsg = _('Main.Error.EventNewSaveFailed');
            }
            cosmo.app.showErr(errMsg, getErrDetailMessage(err));
        } else {
            // Success
            act = 'saveSuccess';
            dojo.debug("handleSaveEvent: saveSuccess");
        }

        dojo.debug("handleSaveEvent: publish topic for all other cases");
        // Success/failure for all other cases
        self.processingQueue.shift();
        // Broadcast message for success/failure
        dojo.event.topic.publish('/calEvent', {
             'action': act,
             'data': ev,
             'saveType': saveType,
             'delta':delta,
             'newItem':newItem
        });
    }

    // Remove
    // =========================
    /**
     * Main function call for removing events -- invokes different
     * confirmation dialog for recurring events.
     * @param ev A CalEvent object, the event to be removed.
     */
     //XINT
    function removeEventConfirm(ev) {
        var str = '';
        var opts = {};
        opts.masterEvent = false;
        // Recurrence is a ball-buster
        // Display the correct confirmation dialog based on
        // whether or not the event recurs
        if (ev.data.recurrenceRule) {
            str = 'removeRecurConfirm';
            if (ev.data.masterEvent) {
                opts.masterEvent = true;
            }
        }
        else {
            str = 'removeConfirm';
        }
        cosmo.app.showDialog(cosmo.view.cal.dialog.getProps(str, opts));
    }
    /**
     * Called for after passthrough from removeEventConfirm. Routes to
     * the right remove operation (i.e., for recurring events, 'All Future,'
     * 'Only This Event,' etc.)
     * @param ev A CalEvent object, the object to be saved.
     * @param qual String, flag for different variations of removing
     * recurring events. Will be one of the three recurringEventOptions.
     */
     //XINT
    function removeEvent(ev, qual) {
        // f is a function object gets set based on what type
        // of edit is occurring -- executed from a very brief
        // setTimeout to allow the 'processing ...' state to
        // display
        var f = null;
        // A second function object used as a callback from
        // the first f callback function -- used when the save
        // operation needs to be made on a recurrence instance's
        // master event rather than the instance -- basically
        // just a way of chaining two async calls together
        var h = null;
        var opts = self.recurringEventOptions;

        // Kill any confirmation dialog that might be showing
        if (cosmo.app.modalDialog.isDisplayed) {
            cosmo.app.hideDialog();
        }
        // Recurring event
        if (qual) {
            switch(qual) {
                // This is easy -- remove the master event
                case opts.ALL_EVENTS:
                    // User is removing the master event directly --
                    // no need to go look it up
                    if (ev.data.masterEvent) {
                        f = function () { doRemoveEvent(ev, { 'removeType': 'recurrenceMaster' }) };
                    }
                    // User is removing all the events in the recurrence from an
                    // instance -- have to look up the master event. This means
                    // two chained async calls
                    else {
                        h = function (evData, err) {
                            if (err) {
                                cosmo.app.showErr('Could not retrieve master event for this recurrence.', getErrDetailMessage(err));
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
                        f = function () {
                            var reqId = cosmo.app.pim.currentCollection.conduit.getEvent(
                                cosmo.app.pim.currentCollection.collection.uid, ev.data.id,
                                cosmo.app.pim.currentCollection.transportInfo, h);
                        };
                    }
                    break;
                // 'Removing' all future events really just means setting the
                // end date on the recurrence
                case opts.ALL_FUTURE_EVENTS:
                        // Have to go get the recurrence rule -- this means two chained async calls
                        h = function (hashMap, err) {
                            if (err) {
                                cosmo.app.showErr('Could not retrieve recurrence rule for this recurrence.', getErrDetailMessage(err));
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'removeFailed',
                                    'data': ev });
                            }
                            else {
                                // JS object with only one item in it -- get the RecurrenceRule
                                for (var a in hashMap) {
                                    var saveRule = hashMap[a];
                                }
                                var freq = ev.data.recurrenceRule.frequency;
                                var start = ev.data.start;
                                // Use the date of the selected event to figure the
                                // new end date for the recurrence
                                var recurEnd = new cosmo.datetime.Date(start.getFullYear(),
                                    start.getMonth(), start.getDate());
                                var unit = ranges[freq][0];
                                var incr = (ranges[freq][1] * -1);
                                // New end should be one 'recurrence span' back -- e.g.,
                                // the previous day for a daily recurrence, one week back
                                // for a weekly, etc.
                                recurEnd = cosmo.datetime.Date.add(recurEnd, unit, incr);
                                saveRule.endDate = recurEnd;
                                doSaveRecurrenceRule(ev, saveRule, { 'saveAction': 'remove',
                                    'removeType': 'instanceAllFuture', 'recurEnd': recurEnd });
                            }
                        };
                        // Look up the RecurrenceRule and pass the result on to function h
                        f = function () {
                            var reqId = cosmo.app.pim.currentCollection.conduit.getRecurrenceRules(
                                cosmo.app.pim.currentCollection.collection.uid, [ev.data.id],
                                cosmo.app.pim.currentCollection.transportInfo, h);
                            };
                    break;
                // Save the RecurrenceRule with a new exception added for this instance
                case opts.ONLY_THIS_EVENT:
                    var rrule = ev.data.recurrenceRule;
                    var dates = rrule.exceptionDates;
                    var d = cosmo.datetime.Date.clone(ev.data.instanceDate);
                    dates.push(d);
                    rrule.removeModification(d);

                    f = function () { doSaveRecurrenceRule(ev, rrule, { 'saveAction': 'remove',
                        'saveType': 'instanceOnlyThisEvent' }) };
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // Normal one-shot event
        else {
            f = function () { doRemoveEvent(ev, { 'removeType': 'singleEvent' }) }
        }
        // Give a sec for the processing state to show
        setTimeout(f, 500);
    }
    /**
     * Call the service to do event removal -- creates an anonymous
     * function to pass as the callback for the async service call.
     * Response to the async request is handled by handleRemoveEvent.
     * @param ev A CalEvent object, the event to be saved.
     * @param opts A JS Object, options for the remove operation.
     */
     //XINT
    function doRemoveEvent(ev, opts) {
        // Pass the original event and opts object to the handler function
        // along with the original params passed back in from the async response
        var f = function (newEvId, err, reqId) {
            handleRemoveResult(ev, newEvId, err, reqId, opts); };

        var requestId = cosmo.app.pim.currentCollection.conduit.removeEvent(
            cosmo.app.pim.currentCollection.collection.uid, ev.data.id,
            cosmo.app.pim.currentCollection.transportInfo, f);
    }
    /**
     * Handles the response from the async call when removing an event.
     * @param ev A CalEvent object, the original event clicked on,
     * or created by double-clicking on the cal canvas.
     * @param newEvId String, FIXME -- Why is this included in Remove?
     * @param err A JS object, the error returned from the server when
     * a remove operation fails.
     * @param reqId Number, the id of the async request.
     * @param optsParam A JS Object, options for the save operation.
     */
    //XINT
    function handleRemoveResult(ev, newEvId, err, reqId, opts) {
        var removeEv = ev;
        // Simple error message to go along with details from Error obj
        var errMsg = _('Main.Error.EventRemoveFailed');
        if (err) {
            act = 'removeFailed';
            cosmo.app.showErr(errMsg, getErrDetailMessage(err));
        }
        else {
            act = 'removeSuccess';
        }

        // Broadcast success
        dojo.event.topic.publish('/calEvent', { 'action': act,
            'data': removeEv, 'opts': opts });
    }
    /**
     * Call the service to save a recurrence rule -- creates an anonymous
     * function to pass as the callback for the async service call.
     * Response to the async request is handled by handleSaveRecurrenceRule.
     * @param ev A CalEvent object, the event originally clicked on.
     * @param rrule A RecurrenceRule, the updated rule for saving.
     * @param opts A JS Object, options for the remove operation.
     */
    //XINT
    function doSaveRecurrenceRule(ev, rrule, opts) {
        // Pass the original event and opts object to the handler function
        // along with the original params passed back in from the async response
        var f = function (ret, err, reqId) {
            handleSaveRecurrenceRule(ev, err, reqId, opts); };
        var requestId = cosmo.app.pim.currentCollection.conduit.saveRecurrenceRule(
            cosmo.app.pim.currentCollection.collection.uid,
            ev.data.id, rrule,
            cosmo.app.pim.currentCollection.transportInfo, f);
    }
    /**
     * Handles the response from the async call when saving changes
     * to a RecurrenceRule.
     * @param ev A CalEvent object, the original event clicked on,
     * or created by double-clicking on the cal canvas.
     * @param err A JS object, the error returned from the server when
     * a remove operation fails.
     * @param reqId Number, the id of the async request.
     * @param opts A JS Object, options for the save operation.
     */
    //XINT
    function handleSaveRecurrenceRule(ev, err, reqId, opts) {

        var rruleEv = ev;
        // Saving the RecurrenceRule can be part of a 'remove'
        // or 'save' -- set the message for an error appropriately
        var errMsgKey = opts.saveAction == 'remove' ?
            'EventRemoveFailed' : 'EventEditSaveFailed';
        // Simple error message to go along with details from Error obj
        var errMsg = _('Main.Error.' + errMsgKey);
        var qual = {};

        if (err) {
            act = opts.saveAction + 'Failed';
            cosmo.app.showErr(errMsg, getErrDetailMessage(err));
        }
        else {
            act = opts.saveAction + 'Success';
        }

        // If the event has been edited such that it is now out of
        // the viewable range, remove the event from display
        if (rruleEv.isOutOfViewRange()) {
            qual.onCanvas = false;
        }
        // Otherwise update display
        else {
            qual.onCanvas = true;
        }

        // Sync changes to the modifications/exceptions in the
        // RecurrenceRule to all the other instances on the canvas
        if (syncRecurrence(rruleEv)) {
            // Broadcast success
            dojo.event.topic.publish('/calEvent', 
                { 'action': act,
                'data': rruleEv, 'opts': opts, 'qualifier': qual 
                });
        }
    }

    function getErrDetailMessage(err) {
        var msg = '';
        switch (true) {
            case (err instanceof cosmo.service.exception.ConcurrencyException):
                msg = _('Main.Error.Concurrency');
                break;
            default:
               msg = err;
               break;
        }
        return msg;
    }

    /**
     * Take an array of CalEventData objects, and create a Hash of
     * CalEvent objects with attached CalEventData objects.
     * @param arrParam Either an Array, or JS Object with multiple Arrays,
     * containing CalEventData objects
     * @return Hash, the keys are randomized strings, and the values are
     * the CalEvent objects.
     */
    this.createEventRegistry = function(arrParam) {
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
            var note = arr[i];
            var eventStamp = note.getEventStamp();
            var id = self.generateTempId();
            var ev = new CalEvent(id, null);
            ev.data = note;
            h.setItem(id, ev);
        }
        return h;
    }

    // Subscribe to the '/calEvent' channel
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');
    // Subscribe to the '/app' channel
    dojo.event.topic.subscribe('/app', self, 'handlePub_app');

    /**
     * Handle events published on the '/calEvent' channel, including
     * self-published events
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published event.
     */
    this.handlePub_calEvent = function (cmd) {
        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var data = cmd.data || {};
        var opts = cmd.opts;
        var delta = cmd.delta;
        switch (act) {
            case 'loadCollection':
                var f = function () { self.triggerLoadEvents(opts); };
                //self.uiMask.show();
                // Give processing message a brief instant to show
                setTimeout(f, 100);
                break;
            case 'saveConfirm':
                var confirmEv = cmd.data;
                saveEventChangesConfirm(confirmEv, delta);
                break;
            case 'save':
                var saveEv = cmd.data;
                saveEventChanges(saveEv, qual, delta);
                break;
            case 'removeConfirm':
                var confirmEv = cmd.data;
                removeEventConfirm(confirmEv);
                break;
            case 'remove':
                var removeEv = cmd.data;
                removeEvent(removeEv, qual);
                break;
            default:
                // Do nothing
                break;
        }
    };
    /**
     * Handle events published on the '/app' channel -- app-wide
     * events
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published event.
     */
    this.handlePub_app = function (cmd) {
        var e = cmd.appEvent;
        var t = cmd.type;
        // Handle keyboard input
        if (t == 'keyboardInput') {
            // Grab any elem above the event that has an id
            var elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
            var ev = cosmo.view.cal.canvas.getSelectedEvent();
            switch (e.keyCode) {
                // Enter key
                case 13:
                    // Go-to date
                    if (elem.id.toLowerCase() == 'jumpto') {
                        //cosmo.app.pim.calForm.goJumpToDate();
                    }
                    // Save an event from the Enter key -- requires:
                    //  * a selected event, not in 'processing' state
                    //  * Enter key input from either one of the event
                    //    detail form text inputs, or the document body
                    //  * Write access for the current collection
                    // Currently all other text inputs belong to the event detail form
                    // FIXME -- check for custom prop that says this field belongs
                    // to the event detail form
                    else if (ev && !ev.lozenge.getInputDisabled() &&
                        ((elem.id == 'body') || (elem.className == 'inputText' &&
                            elem.type == 'text')) &&
                        cosmo.app.pim.currentCollection.isWriteable()) {
                        dojo.event.topic.publish('/calEvent',
                            { 'action': 'saveFromForm' });
                    }
                    break;
                // Delete key
                case 46:
                    // Remove an event from the Delete key -- requires:
                    //  * A selected event, not in 'processing' state
                    //  * Enter key input must be from the document body
                    //  * Write access for the current collection
                    if (ev && !ev.lozenge.getInputDisabled() && (elem.id == 'body') &&
                        cosmo.app.pim.currentCollection.isWriteable()) {
                        dojo.event.topic.publish('/calEvent',
                            { 'action': 'removeConfirm', 'data': ev });
                    }
                    break;
            }
        }
    };
    
    this.triggerLoadEvents = function (o) {
        dojo.debug("trigger!");
        var opts = {};
        var collection = opts.collection;
        var goTo = o.goTo;
        
        // Changing collection
        // --------
        if (collection) {
            // Update pointer to currently selected collection
            self.currentCollection = collection;
            cosmo.app.pim.currentCollection = collection;
        }
        // Changing dates
        // --------
        if (goTo) {
            dojo.debug("goto");
            // param is 'back' or 'next'
            if (typeof goTo == 'string') {
                var key = goTo.toLowerCase();
                var incr = key.indexOf('back') > -1 ? -1 : 1;
                queryDate = cosmo.datetime.Date.add(cosmo.view.cal.viewStart,
                    dojo.date.dateParts.WEEK, incr);
            }
            // param is actual Date
            else {
                queryDate = goTo;
            }
            // Update cosmo.view.cal.viewStart and cosmo.view.cal.viewEnd with new dates
            self.setQuerySpan(queryDate);
        }

        // Data obj to pass to topic publishing
        opts = {
            collection: cosmo.app.pim.currentCollection,
            viewStart: cosmo.view.cal.viewStart,
            viewEnd: cosmo.view.cal.viewEnd,
            currDate: cosmo.app.pim.currDate
        }
        // Pass along the original opts
        for (var n in o) { opts[n] = o[n]; }

        self.loadEvents(opts);
    };
    /**
     * Loading events in the initial app setup, and week-to-week
     * navigation.
     * @param start Number, timestamp for the start of the query
     * period
     * @param end Number, timestamp for the end of the query
     * period
     * @return Boolean, true
     */
    this.loadEvents = function (o) {
        var opts = o;
        var collection = opts.collection;
        var start = opts.viewStart;
        var end = opts.viewEnd;
        var eventLoadList = null;
        var eventLoadHash = new Hash();
        var isErr = false;
        var detail = '';
        var evData = null;
        var id = '';
        var ev = null;
        var showErr = function (e) {
            cosmo.app.showErr(_('Main.Error.LoadEventsFailed'), 
                getErrDetailMessage(e));
        };
        // Load the array of events
        // ======================
        try {
            var deferred = cosmo.app.pim.serv.getItems(collection, 
                { start: start, end: end }, { sync: true });
            var results = deferred.results;
            // Catch any error stuffed in the deferred
            if (results[1] instanceof Error) {
                showErr(results[1]);
                return false;
            }
            else {
                eventLoadList = results[0];
            }
        }
        catch(e) {
            showErr(e);
            return false;
        }

        var eventLoadHash = self.createEventRegistry(eventLoadList);
        dojo.event.topic.publish('/calEvent', { action: 'eventsLoadSuccess',
            data: eventLoadHash, opts: opts });
        return true;
    };
    /**
     * Generate a scratch ID to use for events in the UI
     * @return A random string to use as an ID for a CalEvent obj
     */
    this.generateTempId = function () {
        var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        var len = 12;
        var randomString = '';
        for (var i = 0; i < len; i++) {
            var rnum = Math.floor(Math.random() * chars.length);
            randomString += chars.substring(rnum,rnum+1);
        }
        return 'ev' + randomString;
    };
    /**
     * Get the start and end for the span of time to view in the cal
     * Eventually this will change depending on what type of view is selected
     */
    this.setQuerySpan = function (dt) {
        dojo.debug("this? " + this == self);
        xxx = this;
        yyy = self;
        this.viewStart = cosmo.datetime.util.getWeekStart(dt);
        dojo.debug("viewSTart: " + this.viewStart)
        this.viewEnd = cosmo.datetime.util.getWeekEnd(dt);
        dojo.debug("viewEnd: " + this.viewEnd)
        return true;
};
    /**
     * Get the datetime for midnight Sunday given the current Sunday
     * and the number of weeks to move forward or backward
     */
    this.getNewViewStart = function (key) {
        var queryDate = null;
        var incr = 0;
        // Increment/decrement week
        if (key.indexOf('next') > -1) {
            incr = 1;
        }
        else if (key.indexOf('back') > -1) {
            incr = -1;
        }
        queryDate = cosmo.datetime.Date.add(this.viewStart,
            dojo.date.dateParts.WEEK, incr);
        return queryDate;
    };
}, cosmo.view.cal);

