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

dojo.provide('cosmo.view.service');

dojo.require("cosmo.app.pim");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.model");
dojo.require("cosmo.view.BaseItem");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.util");
dojo.require('cosmo.view.dialog');
dojo.require("cosmo.service.exception");

dojo.require("cosmo.util.debug");

cosmo.view.service = new function () {
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
    // Start date for items to display
    this.viewStart = null;
    // End date for items to display
    this.viewEnd = null;
    // Options for saving/removing recurring items
    this.recurringEventOptions = {
        ALL_EVENTS: 'master',
        ALL_FUTURE_EVENTS: 'occurrenceAndFuture',
        ONLY_THIS_EVENT: 'occurrence'
    };
    // How many updates/removals are in-flight
    this.processingQueue = [];
    // Last clicked cal item -- used for selection persistence.
    this.lastSent = null;

    // Subscribe to the '/calEvent' channel
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');

    /**
     * Handle items published on the '/calEvent' channel, including
     * self-published items
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published item.
     */
    this.handlePub_calEvent = function (cmd) {
        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var data = cmd.data || {};
        var opts = cmd.opts;
        var delta = cmd.delta;
        switch (act) {
            case 'saveConfirm':
                var confirmItem = cmd.data;
                saveItemChangesConfirm(confirmItem, delta);
                break;
            case 'save':
                var saveItem = cmd.data;
                saveItemChanges(saveItem, qual, delta);
                break;
            case 'removeConfirm':
                var confirmItem = cmd.data;
                removeItemConfirm(confirmItem);
                break;
            case 'remove':
                var removeEv = cmd.data;
                removeItem(removeEv, qual);
                break;
            default:
                // Do nothing
                break;
        }
    };


    // Saving changes
    // =========================
    /**
     * Main function call for saving new items or changes to existing
     * items -- invokes confirmation dialog for changes to item properties
     * for recurring items (changes to the recurrence rule spawn no dialog).
     * For normal items, this simply passes through to saveItemChanges
     * @param item A CalEvent object, the item to be saved.
     */
    function saveItemChangesConfirm(item, delta) {
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
            dojo.event.topic.publish('/calEvent', {action: 'save', data: item, delta: delta });
        }
        else {
          cosmo.app.showDialog(cosmo.view.dialog.getProps('saveRecurConfirm',
            { changeTypes: changeTypes, delta: delta, saveItem: item }));
        }
    }

    /**
     * Called for after passthrough from saveItemChangesConfirm. Routes to
     * the right save operation (i.e., for recurring items, 'All Future,'
     * 'Only This Event,' etc.)
     * @param item A CalEvent object, the object to be saved.
     * @param qual String, flag for different variations of saving
     * recurring items. May be one of the three recurringEventOptions, or
     * the two special cases of adding recurrence to a single item or
     * removing recurrence completely from a master item for a recurrence.
     */
    function saveItemChanges(item, qual, delta) {
        dojo.debug("save item changes...");
        // f is a function object gets set based on what type
        // of edit is occurring -- executed from a very brief
        // setTimeout to allow the 'processing ...' state to
        // display
        var f = null;

        // A second function object used as a callback from
        // the first f callback function -- used when the save
        // operation needs to be made on a recurrence instance's
        // master item rather than the instance -- basically
        // just a way of chaining two async calls together
        var h = null;

        // Kill any confirmation dialog that might be showing
        if (cosmo.app.modalDialog.isDisplayed) {
            cosmo.app.hideDialog();
        }

        var opts = self.recurringEventOptions;

        // Recurring item
        if (qual) {
            switch(qual) {
                // Changing the master item in the recurring sequence
                case opts.ALL_EVENTS:
                    dojo.debug("ALL_EVENTS");
                    delta.applyToMaster();
                    f = function(){
                        doSaveItem(item,
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
                        doSaveItem(item,
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
                    var note = item.data;
                    var recurrenceId = note.recurrenceId;
                    var master = note.getMaster();
                    var newModification = false;
                    if (!master.getModification(recurrenceId)){
                         newModification = true;
                    }
                    delta.applyToOccurrence();
                    f = function () {
                        doSaveItem(item,
                        {
                          'saveType': opts.ONLY_THIS_EVENT,
                          'delta': delta,
                          'newModification': newModification
                        });
                    };
                    break;

                case 'new':
                    dojo.debug("doSaveChanges: new")
                    f = function () { doSaveItem(item, { 'new': true } ) };
                    break;
                default:
                    break;
            }
        }
        // Normal one-shot item
        else {
            dojo.debug("saveItemChanges: normal sone shot item");
            f = function () { doSaveItem(item, { } ) };
        }

        // Give a sec for the processing state to show
        dojo.debug("before set timeout");
        setTimeout(f, 500);
    }
    /**
     * Call the service to do a normal item save.
     * Creates an anonymous function to pass as the callback for the
     * async service call that saves the changes to the item.
     * Response to the async request is handled by handleSaveItem.
     * @param item A CalEvent object, the item to be saved.
     * @param opts An Object, options for the save operation.
     */
     //BOBBYNOTE: Can we collapse this into "saveItemChanges" ?
    function doSaveItem(item, opts) {
        dojo.debug("doSaveItem");
        var OPTIONS = self.recurringEventOptions;

        var deferred = null;
        var requestId = null;
        var isNew = opts['new'] || false;
        var note = item.data;
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
                            //if there was an error, pass it to handleSaveItem, with the original
                            //item
                            handleSaveItem(item, newItemDeferred.results[1], requestId,opts.saveType, delta);
                        }
                        else {
                            //get rid of the id from the processing queue
                            self.processingQueue.shift()

                            //success at saving new item (the one broken off from the original recurrence chain!
                            // Now let's save the original.
                            var originalDeferred = cosmo.app.pim.serv.saveItem(note.getMaster());
                            originalDeferred.addCallback(function(){
                                handleSaveItem(item,
                                    originalDeferred.results[1],
                                    originalDeferred.id,
                                    opts.saveType,
                                    delta,
                                    newItem);
                            });
                            self.processingQueue.push(originalDeferred.id);
                            self.lastSent = item;
                        }
                    });

                    dojo.debug("about to save note in ALL FUTURE EVENTS")
                    return;
                case OPTIONS.ONLY_THIS_EVENT:
                     if (!opts.newModification){
                         dojo.debug("doSaveItem: saving a previously created modificaiton")
                         deferred = cosmo.app.pim.serv.saveItem(note);
                     }
                     else {
                         dojo.debug("doSaveItem: creating a new modificaiton")
                         deferred = cosmo.app.pim.serv.createItem(note, cosmo.app.pim.currentCollection);
                     }
                break;
            }
        }
        else {
            dojo.debug("normal, non-recurring item")
            deferred = cosmo.app.pim.serv.saveItem(note);
        }

        var f = function () {
            var saveType = isNew ? "new" : opts.saveType;
            dojo.debug("callback: saveType="+ saveType)
            handleSaveItem(item, deferred.results[1], deferred.id, saveType, delta);
        };

        deferred.addCallback(f);
        requestId = deferred.id;

        // Add to processing queue -- canvas will not re-render until
        // queue is empty
        self.processingQueue.push(requestId);

        // Selection persistence
        // --------------------
        // In these cases, the items concerned will be re-rendered
        // after re-expanding the recurrence on the server -- no
        // way to preserve the original selection pointer. This means
        // that figuring out where selection goes will require some
        // calcluation
        if (opts.saveType == 'recurrenceMaster' ||
            opts.saveType == 'singleEventAddRecurrence') {
            self.lastSent = null;
        }
        // Just remember the original item that held the selection
        // we'll keep it there after re-render
        else {
            self.lastSent = item;
        }
    }

    /**
     * Handles the response from the async call when saving changes
     * to items.
     * @param item A CalEvent object, the original item clicked on,
     * or created by double-clicking on the cal canvas.
     * FIXME: Comments below are hopelessly out of date
     * @param newItemId String, the id for the item returned when creating a
     * new item
     * @param err A JS object, the error returned from the server when
     * a save operation fails.
     * @param reqId Number, the id of the async request.
     * @param optsParam A JS Object, options for the save operation.
     */
    function handleSaveItem(item, err, reqId, saveType, delta, newItem) {
        dojo.debug("handleSaveItem");
        var OPTIONS = self.recurringEventOptions;
        var errMsg = '';
        var act = '';

        if (err) {
            dojo.debug("handleSaveItem: err");
            // Failure -- display exception info
            act = 'saveFailed';
            // Failed update
            if (saveType != "new") {
                errMsg = _('Main.Error.EventEditSaveFailed');
            }
            // Failed create
            else {
                // FIXME: Should we be removing the item from
                // the itemRegistry/itemRegistry here?
                errMsg = _('Main.Error.EventNewSaveFailed');
            }
            cosmo.app.showErr(errMsg, getErrDetailMessage(err));
        }
        else {
            // Success
            act = 'saveSuccess';
            dojo.debug("handleSaveItem: saveSuccess");
        }

        dojo.debug("handleSaveItem: publish topic for all other cases");
        // Success/failure for all other cases
        self.processingQueue.shift();
        // Broadcast message for success/failure
        dojo.event.topic.publish('/calEvent', {
             'action': act,
             'data': item,
             'saveType': saveType,
             'delta':delta,
             'newItem':newItem
        });
    }

    // Remove
    // =========================
    /**
     * Main function call for removing items -- invokes different
     * confirmation dialog for recurring items.
     * @param item A CalEvent object, the item to be removed.
     */
     //XINT
    function removeItemConfirm(item) {
        var str = '';
        var opts = {};
        opts.masterEvent = false;
        // Recurrence is a ball-buster
        // Display the correct confirmation dialog based on
        // whether or not the item recurs
        if (item.data.recurrenceRule) {
            str = 'removeRecurConfirm';
            if (item.data.masterEvent) {
                opts.masterEvent = true;
            }
        }
        else {
            str = 'removeConfirm';
        }
        cosmo.app.showDialog(cosmo.view.dialog.getProps(str, opts));
    }
    /**
     * Called for after passthrough from removeItemConfirm. Routes to
     * the right remove operation (i.e., for recurring items, 'All Future,'
     * 'Only This Event,' etc.)
     * @param item A CalEvent object, the object to be saved.
     * @param qual String, flag for different variations of removing
     * recurring items. Will be one of the three recurringEventOptions.
     */
     //XINT
    function removeItem(item, qual) {
        // f is a function object gets set based on what type
        // of edit is occurring -- executed from a very brief
        // setTimeout to allow the 'processing ...' state to
        // display
        var f = null;
        // A second function object used as a callback from
        // the first f callback function -- used when the save
        // operation needs to be made on a recurrence instance's
        // master item rather than the instance -- basically
        // just a way of chaining two async calls together
        var h = null;
        var opts = self.recurringEventOptions;

        // Kill any confirmation dialog that might be showing
        if (cosmo.app.modalDialog.isDisplayed) {
            cosmo.app.hideDialog();
        }
        // Recurring item
        if (qual) {
            switch(qual) {
                // This is easy -- remove the master item
                case opts.ALL_EVENTS:
                    // User is removing the master item directly --
                    // no need to go look it up
                    if (item.data.masterEvent) {
                        f = function () { doRemoveItem(item, { 'removeType': 'recurrenceMaster' }) };
                    }
                    // User is removing all the items in the recurrence from an
                    // instance -- have to look up the master item. This means
                    // two chained async calls
                    else {
                        h = function (itemData, err) {
                            if (err) {
                                cosmo.app.showErr('Could not retrieve master item for this recurrence.', getErrDetailMessage(err));
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'removeFailed',
                                    'data': item });
                            }
                            else {
                                // Just use BaseItem as the only thing the remove
                                // op is interested in is the data prop
                                var removeEv = new cosmo.view.BaseItem();
                                removeEv.data = itemData;
                                doRemoveItem(removeEv, { 'removeType': 'recurrenceMaster',
                                    'instanceEvent': item });
                            }
                        };
                        f = function () {
                            var reqId = cosmo.app.pim.currentCollection.conduit.getEvent(
                                cosmo.app.pim.currentCollection.collection.uid, item.data.id,
                                cosmo.app.pim.currentCollection.transportInfo, h);
                        };
                    }
                    break;
                // 'Removing' all future items really just means setting the
                // end date on the recurrence
                case opts.ALL_FUTURE_EVENTS:
                        // Have to go get the recurrence rule -- this means two chained async calls
                        h = function (hashMap, err) {
                            if (err) {
                                cosmo.app.showErr('Could not retrieve recurrence rule for this recurrence.', getErrDetailMessage(err));
                                // Broadcast failure
                                dojo.event.topic.publish('/calEvent', { 'action': 'removeFailed',
                                    'data': item });
                            }
                            else {
                                // JS object with only one item in it -- get the RecurrenceRule
                                for (var a in hashMap) {
                                    var saveRule = hashMap[a];
                                }
                                var freq = item.data.recurrenceRule.frequency;
                                var start = item.data.start;
                                // Use the date of the selected item to figure the
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
                                doSaveRecurrenceRule(item, saveRule, { 'saveAction': 'remove',
                                    'removeType': 'instanceAllFuture', 'recurEnd': recurEnd });
                            }
                        };
                        // Look up the RecurrenceRule and pass the result on to function h
                        f = function () {
                            var reqId = cosmo.app.pim.currentCollection.conduit.getRecurrenceRules(
                                cosmo.app.pim.currentCollection.collection.uid, [item.data.id],
                                cosmo.app.pim.currentCollection.transportInfo, h);
                            };
                    break;
                // Save the RecurrenceRule with a new exception added for this instance
                case opts.ONLY_THIS_EVENT:
                    var rrule = item.data.recurrenceRule;
                    var dates = rrule.exceptionDates;
                    var d = cosmo.datetime.Date.clone(item.data.instanceDate);
                    dates.push(d);
                    rrule.removeModification(d);

                    f = function () { doSaveRecurrenceRule(item, rrule, { 'saveAction': 'remove',
                        'saveType': 'instanceOnlyThisEvent' }) };
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // Normal one-shot item
        else {
            f = function () { doRemoveItem(item, { 'removeType': 'singleEvent' }) }
        }
        // Give a sec for the processing state to show
        setTimeout(f, 500);
    }
    /**
     * Call the service to do item removal -- creates an anonymous
     * function to pass as the callback for the async service call.
     * Response to the async request is handled by handleRemoveItem.
     * @param item A CalEvent object, the item to be saved.
     * @param opts A JS Object, options for the remove operation.
     */
     //XINT
    function doRemoveItem(item, opts) {
        // Pass the original item and opts object to the handler function
        // along with the original params passed back in from the async response
        var f = function (newItemId, err, reqId) {
            handleRemoveResult(item, newItemId, err, reqId, opts); };

        var requestId = cosmo.app.pim.currentCollection.conduit.removeEvent(
            cosmo.app.pim.currentCollection.collection.uid, item.data.id,
            cosmo.app.pim.currentCollection.transportInfo, f);
    }
    /**
     * Handles the response from the async call when removing an item.
     * @param ev A CalEvent object, the original item clicked on,
     * or created by double-clicking on the cal canvas.
     * @param newItemId String, FIXME -- Why is this included in Remove?
     * @param err A JS object, the error returned from the server when
     * a remove operation fails.
     * @param reqId Number, the id of the async request.
     * @param optsParam A JS Object, options for the save operation.
     */
    //XINT
    function handleRemoveResult(item, newItemId, err, reqId, opts) {
        var removeEv = item;
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
     * @param item A CalEvent object, the item originally clicked on.
     * @param rrule A RecurrenceRule, the updated rule for saving.
     * @param opts A JS Object, options for the remove operation.
     */
    //XINT
    function doSaveRecurrenceRule(item, rrule, opts) {
        // Pass the original item and opts object to the handler function
        // along with the original params passed back in from the async response
        var f = function (ret, err, reqId) {
            handleSaveRecurrenceRule(item, err, reqId, opts); };
        var requestId = cosmo.app.pim.currentCollection.conduit.saveRecurrenceRule(
            cosmo.app.pim.currentCollection.collection.uid,
            item.data.id, rrule,
            cosmo.app.pim.currentCollection.transportInfo, f);
    }
    /**
     * Handles the response from the async call when saving changes
     * to a RecurrenceRule.
     * @param item A CalEvent object, the original item clicked on,
     * or created by double-clicking on the cal canvas.
     * @param err A JS object, the error returned from the server when
     * a remove operation fails.
     * @param reqId Number, the id of the async request.
     * @param opts A JS Object, options for the save operation.
     */
    //XINT
    function handleSaveRecurrenceRule(item, err, reqId, opts) {

        var rruleEv = item;
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

        // If the item has been edited such that it is now out of
        // the viewable range, remove the item from display
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
    };
};

