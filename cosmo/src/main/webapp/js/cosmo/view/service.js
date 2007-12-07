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
dojo.require("cosmo.service.exception");

dojo.require("cosmo.util.debug");

cosmo.view.service = new function () {
    var self = this;
    
    this._ranges = {
        'daily': [dojo.date.dateParts.DAY, 1],
        'weekly': [dojo.date.dateParts.WEEK, 1],
        'biweekly': [dojo.date.dateParts.WEEK, 2],
        'monthly': [dojo.date.dateParts.MONTH, 1],
        'yearly': [dojo.date.dateParts.YEAR, 1]
    };

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
     * @param item A ListItem/CalItem object, the item to be saved.
     */
    function saveItemChangesConfirm(item, delta) {
        var changeTypes = delta.getApplicableChangeTypes();
        var change = null;
        var changeCount = 0;
        for (change in changeTypes){
            dojo.debug("changeType: " +change);
            if (changeTypes[change]){
                changeCount++;
            }
        }

        if (!item.data.hasRecurrence()){
            delta.applyChangeType(change);
            dojo.event.topic.publish('/calEvent', {action: 'save', data: item, delta: delta });
        }
        else {
            confirmType = '';
            // If only the ALL_EVENTS type of change is possible
            // because of a modification to the recurrence rule,
            // we display a special dialog box.
            if (changeCount == 1 &&
                changeTypes[cosmo.view.service.recurringEventOptions.ALL_EVENTS] &&
                delta.isStampPropertyChanged("event", "rrule")) {
                confirmType = 'saveRecurConfirmAllEventsOnly';
            }
            else {
                confirmType = 'saveRecurConfirm';
            }
            var props = cosmo.view.recurrenceDialog.getProps(confirmType,
                { changeTypes: changeTypes, delta: delta, saveItem: item });
            dojo.debug(props.content);
            cosmo.app.showDialog(props);
        }
    }

    /**
     * Called for after passthrough from saveItemChangesConfirm. Routes to
     * the right save operation (i.e., for recurring items, 'All Future,'
     * 'Only This Event,' etc.)
     * @param item A ListItem/CalItem object, the object to be saved.
     * @param qual String, flag for different variations of saving
     * recurring items. May be one of the three recurringEventOptions, or
     * the two special cases of adding recurrence to a single item or
     * removing recurrence completely from a master item for a recurrence.
     */
    function saveItemChanges(item, qual, delta) {
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
                          'newItemNote': newItem
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
        setTimeout(f, 35);
    }
    /**
     * Call the service to do a normal item save.
     * Creates an anonymous function to pass as the callback for the
     * async service call that saves the changes to the item.
     * Response to the async request is handled by handleSaveItem.
     * @param item A ListItem/CalItem object, the item to be saved.
     * @param opts An Object, options for the save operation.
     */
     //BOBBYNOTE: Can we collapse this into "saveItemChanges" ?
    function doSaveItem(item, opts) {
        var OPTIONS = self.recurringEventOptions;

        var deferred = null;
        var requestId = null;
        var isNew = opts['new'] || false;
        var note = item.data;
        var delta = opts.delta;
        var newItem = opts.newItemNote;
        dojo.debug("Do save: savetype: " + opts.saveType)
        dojo.debug("Do save: iznew: " + isNew)

        if (isNew){
            deferred = cosmo.app.pim.serv.createItem(note, cosmo.app.pim.getSelectedCollection(),{});
        } else if (opts.saveType) {
            dojo.debug("opty! " + opts.saveType );
            switch(opts.saveType){
                case OPTIONS.ALL_EVENTS:
                    dojo.debug("about to save note in ALL EVENTS")
                    deferred = cosmo.app.pim.serv.saveItem(note.getMaster());
                    break;
                case OPTIONS.ALL_FUTURE_EVENTS:
                    dojo.debug("about to save note in ALL FUTURE EVENTS")
                    //saveThisAndFuture(oldOccurrence, newMaster, kwArgs)
                    var newItemDeferred = cosmo.app.pim.serv.saveThisAndFuture(note, newItem);
                    var requestId = newItemDeferred.id;
                    self.processingQueue.push(requestId);


                    
                    newItemDeferred.addCallback(function(){
                        handleSaveItem(item,
                            null,
                            newItemDeferred.id,
                            opts.saveType,
                            delta,
                            newItem);
                    });
              //    function handleSaveItem(item, err, reqId, saveType, delta, newItem) {
                    newItemDeferred.addErrback(function(error){
                        //if there was an error, pass it to handleSaveItem, with the original
                        //item
                        handleSaveItem(item, error, newItemDeferred.id, opts.saveType, delta);
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
                         deferred = cosmo.app.pim.serv.createItem(note, cosmo.app.pim.getSelectedCollection());
                     }
                break;
            }
        }
        else {
            dojo.debug("normal, non-recurring item")
            deferred = cosmo.app.pim.serv.saveItem(note);
        }

        var saveType = isNew ? "new" : opts.saveType;

        deferred.addCallback(function () {
            handleSaveItem(item, null, deferred.id, saveType, delta, newItem);
        });

        deferred.addErrback(function(error){
               handleSaveItem(item, error, deferred.id, saveType, delta, newItem);
        })
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
     * @param item A ListItem/CalItem object, the original item clicked on,
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
        var OPTIONS = self.recurringEventOptions;
        var errMsg = '';
        var act = '';
         
        if (err){
            // Failure -- display exception info
            act = 'saveFailed';
            
            if (err instanceof cosmo.service.exception.ResourceNotFoundException){
                //let's see if it was the collection that got deleted, or the item
                //itself.
                var collection = cosmo.app.pim.getSelectedCollection();
                var deferred = cosmo.app.pim.serv.getCollection(collection.getUrls().self);
                deferred.addErrback(function (){
                    //reload collections will handle showing the error message, as it will try and load the 
                    //original collection
                    return cosmo.app.pim.reloadCollections({ removedCollection: collection,
                        removedByThisUser: false });
                } );
                deferred.addCallback(function (){
                    errMsg = _('Main.Error.EventEditSaveFailed.EventRemoved');
                    cosmo.app.showErr(errMsg);
                    //easiest thing to do here just reload the collections, since it would be a pain to figure
                    //out if you need to re-expand if the removed item was an occurrence, etc. 
                    var reloadDeferred = cosmo.app.pim.reloadCollections();
                    reloadDeferred.addCallback(function(){self.processingQueue.shift()});
                    return reloadDeferred;
                });
            } else if (err instanceof cosmo.service.exception.CollectionLockedException){
                    errMsg = _('Main.Error.EventEditSaveFailed.CollectionLocked');
                    cosmo.app.showErr(errMsg);
                    self.processingQueue.shift();
                    return;
            } else {
                //generic error handling.
                if (saveType != "new") {
                    errMsg = _('Main.Error.ItemEditSaveFailed');
                } else {
                    // Failed create
                    // FIXME: Should we be removing the item from
                    // the itemRegistry/itemRegistry here?
                    errMsg = _('Main.Error.ItemNewSaveFailed');
                }
                cosmo.app.showErr(errMsg, getErrDetailMessage(err), err);

            }
        } else {
            // Success
            act = 'saveSuccess';
        }

        // Success/failure for all other cases
        self.processingQueue.shift();
        // Broadcast message for success/failure
        // Do it in window scope to avoid trapping all the
        // subsequent UI code errors in the addErrBack for
        // the service Deferred
        var f = function () {
            dojo.event.topic.publish('/calEvent', {
                 'action': act,
                 'data': item,
                 'saveType': saveType,
                 'delta':delta,
                 'newItemNote':newItem
            });
        }
        setTimeout(f, 0);
    }

    // Remove
    // =========================
    /**
     * Main function call for removing items -- invokes different
     * confirmation dialog for recurring items.
     * @param item A ListItem/CalItem object, the item to be removed.
     */
    function removeItemConfirm(item) {
        dojo.debug("removeItemConfirm!");
        var confirmType = '';
        var opts = {};
        opts.masterEvent = false;
        // Display the correct confirmation dialog based on
        // whether or not the item recurs
        if (item.data.hasRecurrence()) {
            if (item.collectionIds.length > 1) {
                confirmType = 'removeRecurConfirmAllEventsOnly';
            }
            else {
                confirmType = 'removeRecurConfirm';
            }
        }
        else {
            confirmType = 'removeConfirm';
        }
        cosmo.app.showDialog(cosmo.view.recurrenceDialog.getProps(
            confirmType, opts));
    }

    /**
     * Called for after passthrough from removeItemConfirm. Routes to
     * the right remove operation (i.e., for recurring items, 'All Future,'
     * 'Only This Event,' etc.)
     * @param item A ListItem/CalItem object, the object to be saved.
     * @param qual String, flag for different variations of removing
     * recurring items. Will be one of the three recurringEventOptions.
     */
    function removeItem(item, qual) {
        dojo.debug("remove Item: item: " +item);
        dojo.debug("qual: "+ qual);

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

        // Recurring item
        if (qual) {
            switch(qual) {
                // This is easy -- remove the master item
                case opts.ALL_EVENTS:
                    f = function () {
                        doRemoveItem(item, { 'removeType': opts.ALL_EVENTS});
                    };
                    break;
                // 'Removing' all future items really just means setting the
                // end date on the recurrence
                case opts.ALL_FUTURE_EVENTS:
                    f = function () {
                        doRemoveItem(item, { 'removeType': opts.ALL_FUTURE_EVENTS});
                    };
                    break;
                // Save the RecurrenceRule with a new exception added for this instance
                case opts.ONLY_THIS_EVENT:
                    f = function () {
                        doRemoveItem(item, { 'removeType': opts.ONLY_THIS_EVENT});
                    };
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // Normal one-shot item
        else {
            dojo.debug("normal one shot item");
            f = function () { doRemoveItem(item, { 'removeType': 'singleEvent' }) }
        }
        // Give a sec for the processing state to show
        setTimeout(f, 500);
    }
    /**
     * Call the service to do item removal -- creates an anonymous
     * function to pass as the callback for the async service call.
     * Response to the async request is handled by handleRemoveItem.
     * @param item A ListItem/CalItem object, the item to be saved.
     * @param opts A JS Object, options for the remove operation.
     */
    function doRemoveItem(item, opts) {
        dojo.debug("doRemoveItem: opts.removeType:" + opts.removeType);
        var OPTIONS = self.recurringEventOptions;
        var deferred = null;
        var reqId = null;


        if (opts.removeType == "singleEvent"){
            deferred = cosmo.app.pim.serv.removeItem(item.data, cosmo.app.pim.getSelectedCollection());
            reqId = deferred.id;
        } else if (opts.removeType == OPTIONS.ALL_EVENTS){
            deferred = cosmo.app.pim.serv.removeItem(item.data.getMaster(), cosmo.app.pim.getSelectedCollection());
            reqId = deferred.id;
        } else if (opts.removeType == OPTIONS.ALL_FUTURE_EVENTS){
            var data = item.data;
            var endDate = data.getEventStamp().getEndDate().clone();
            var master = data.getMaster();
            var masterEventStamp = master.getEventStamp();
            var oldRecurrenceRule = masterEventStamp.getRrule();
            var unit = self._ranges[oldRecurrenceRule.getFrequency()][0];
            var incr = (self._ranges[oldRecurrenceRule.getFrequency()][1] * -1);
            // New end should be one 'recurrence span' back -- e.g.,
            // the previous day for a daily recurrence, one week back
            // for a weekly, etc.
            endDate.add(unit, incr);
            endDate.setHours(0);
            endDate.setMinutes(0);
            endDate.setSeconds(0);

            var newRecurrenceRule = new cosmo.model.RecurrenceRule({
                frequency: oldRecurrenceRule.getFrequency(),
                endDate: endDate,
                isSupported: oldRecurrenceRule.isSupported(),
                unsupportedRule: oldRecurrenceRule.getUnsupportedRule()
            });
            masterEventStamp.setRrule(newRecurrenceRule);
            deferred = cosmo.app.pim.serv.saveItem(master);
            if (data.getMaster().getModification(data.recurrenceId)){
                deferred.addCallback(function(){cosmo.app.pim.serv.deleteItem(data)});
            }
            reqId = deferred.id;
        } else if (opts.removeType == OPTIONS.ONLY_THIS_EVENT){
            var data = item.data;
            var master = data.getMaster();
            var masterEventStamp = master.getEventStamp();
            var recurrenceId = data.recurrenceId;
            var exDate = null;
            if (masterEventStamp.getStartDate().isFloating()){
                exDate = recurrenceId.clone()
            } else {
                var tzId = masterEventStamp.getStartDate().tzId || "utc";
                exDate = recurrenceId.createDateForTimezone(tzId);
            }
            if (masterEventStamp.getExdates() == null){
                masterEventStamp.setExdates([]);
            }
            masterEventStamp.getExdates().push(exDate)
            deferred = cosmo.app.pim.serv.saveItem(master);
            if (data.getMaster().getModification(data.recurrenceId)){
                deferred.addCallback(function(){cosmo.app.pim.serv.deleteItem(data)});
            }
            reqId = deferred.id;
        }

        // Pass the original item and opts object to the handler function
        // along with the original params passed back in from the async response
        var callback = function () {
            handleRemoveResult(item, null, reqId, opts);
        };
        
        var errback = function(error){
            handleRemoveResult(item, error, reqId, opts);
        }

        deferred.addCallback(callback);
        deferred.addErrback(errback)
    }
    /**
     * Handles the response from the async call when removing an item.
     * @param item A ListItem/CalItem object, the original item clicked on,
     * or created by double-clicking on the cal canvas.
     * @param err A JS object, the error returned from the server when
     * a remove operation fails.
     * @param reqId Number, the id of the async request.
     * @param optsParam A JS Object, options for the save operation.
     */
    function handleRemoveResult(item, err, reqId, opts) {
        var removeEv = item;
        // Simple error message to go along with details from Error obj
        var errMsg = _('Main.Error.ItemRemoveFailed');
        if (err) {
            act = 'removeFailed';
            cosmo.app.showErr(errMsg, getErrDetailMessage(err), err);
            item.restoreEvent();
        }
        else {
            act = 'removeSuccess';
        }

        // Broadcast message for success/failure
        // Do it in window scope to avoid trapping all the
        // subsequent UI code errors in the addErrBack for
        // the service Deferred
        var f = function () {
            dojo.event.topic.publish('/calEvent', { 'action': act,
                'data': removeEv, 'opts': opts });
            dojo.event.topic.publish('/calEvent', { action: 'setSelected', data: null});
        }
        setTimeout(f, 0);
    }

    function getErrDetailMessage(err) {
        var msg = '';
        switch (true) {
            case (err instanceof (cosmo.service.exception.ConcurrencyException)):
                msg = _('Main.Error.Concurrency');
                break;
            default:
               msg = null;
               break;
        }
        return msg;
    };
};

