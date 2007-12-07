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

dojo.provide('cosmo.view.common');

dojo.require("cosmo.app.pim");
dojo.require("cosmo.datetime.Date");
dojo.require('cosmo.view.dialog');
dojo.require('cosmo.model.exception');

cosmo.view.recurrenceDialog = new cosmo.view.dialog.RecurrenceDialog();
cosmo.view.unsavedChangesDialog = new cosmo.view.dialog.UnsavedChangesDialog();

cosmo.view.viewBase = new function () {
    this.init = function () {
        // Subscribe to the '/calEvent' channel
        dojo.event.topic.subscribe('/calEvent', this, 'handlePub_calEvent');
        // Subscribe to the '/app' channel
        dojo.event.topic.subscribe('/app', this, 'handlePub_app');
        this.hasBeenInitialized = true;
        cosmo.view.contextMenu.menu = cosmo.view.contextMenu.menu || cosmo.view.contextMenu.createMenu();
    };

    this.isCurrentView = function () {
        return (cosmo.app.pim.currentView == this.viewId);
    };
    /**
     * Returns a new hash registry, filtering the recurring items with the
     * specified  a set or sets of recurring items for an id or ids
     *
     * WARNING: this destroys the itemRegistry (Hash) that is passed into it!
     *
     * @param reg An itemRegistry Hash from which to remove a group or
     * groups of recurring items
     * @param arr Array of Item ids for the recurrences to
     * remove
     * @param dt A cosmo.datetime.Date,represents the end date of a
     * recurrence -- if the dt param is present, it will remove
     * only the item occurrences which occur after the date
     * It will also reset the recurrence endDate for all dates
     * to the dt (the new recurrence end date) for all the items
     * that it leaves
     * @param ignore String, the CalItem id of a single item to ignore from
     * the removal process -- used when you need to leave the
     * master item in a recurrence
     * @return a new Hash to be used as your itemRegistry
     */
    this.filterOutRecurrenceGroup = function (reg, ids, o) {
        var opts = o || {};
        var ignore =  opts.ignore || null;
        var idList = ',' + ids.join() + ',';
        var newReg = new Hash();
        var origReg = reg.clone();
        var dt = opts.dateToBeginRemoval || null;
        var dateToBegin = dt ? new cosmo.datetime.Date(dt.getFullYear(),
            dt.getMonth(), dt.getDate(), 23, 59) : null;
        var filterFunc = function (id, item) {
            if (!item) { 
                throw new Error('item does not exist'); 
            }
            var keep = false;
            var masterUid = item.data.getUid();
            switch (true) {
                // Any to be specifically ignored -- this is all-mighty
                case (item.id == ignore):
                    keep = true;
                    break;
                // Any that don't have matching ids -- keep these too
                case (idList.indexOf(',' + masterUid + ',') == -1):
                    keep = true;
                    break;
                // Matching ids -- candidates for removal
                case (idList.indexOf(',' + masterUid + ',') > -1):
                    // Filtering by date -- All Future Items
                    if (dateToBegin){
                        var eventStamp = item.data.getEventStamp();
                        var startDate = eventStamp.getStartDate();
                        var endDate = eventStamp.getEndDate();
                        if (startDate.toUTC() < dateToBegin.toUTC()) {
                            keep = true;
                        }
                    }
                    break;
                default:
                    // Throw it out
                    break;
            }
            if (!keep) {
                if (opts.collectionForRemoval && item.collectionIds) {
                    item.removeCollection(opts.collectionForRemoval);
                }
                origReg.removeItem(item.id);
            }
        }
        reg.each(filterFunc);
        // Do inplace substitution of param Hash passed in
        // return it
        var item = null;
        while (item = reg.pop()) {}
        reg.append(origReg);
        return reg;
    };
    /**
     * Handle events published on the '/app' channel -- app-wide
     * events
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published event.
     */
    this.handlePub_app = function (cmd) {

        if (!this.isCurrentView()) { return false; }

        var e = cmd.appEvent;
        var t = cmd.type;
        var isValidEventSource = function (e, elem) {
            // Source of keyboard input should be
            // either the doc body, or a cal event lozenge
            var isValidDomNode = ((elem.id == 'body') ||
                (elem.id.indexOf('eventDiv') > -1));
            // Accept input from text elems, not textareas
            // (enter/delete needs to work normally in textarea)
            var isValidFormElem = ((elem.className == 'inputText' &&
                elem.type == 'text'));
            var testByCode = {
                // Enter
                13: function () { return isValidDomNode || isValidFormElem },
                // Delete -- can't work in text boxes
                46: function () { return isValidDomNode }
            };
            return testByCode[e.keyCode]();
        }
        // Handle keyboard input
        if (t == 'keyboardInput') {
            // Don't bother executing all these tests unless it's the Enter
            // or Delete key -- use case statement here so we can cleanly
            // add other keys as needed
            switch (e.keyCode) {
                case 13:
                case 46:
                    // Must have a currently selected item and a
                    // writable collection, and the caret/focus has
                    // to be somewhere appropriate for the key input
                    // in question
                    // Find whwatever elem in the DOM hier above
                    // the event source that has an id
                    var elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
                    // Currently selected item, if any
                    var item = this.canvasInstance.getSelectedItem();
                    if (item &&
                        cosmo.app.pim.getSelectedCollectionWriteable() &&
                        isValidEventSource(e, elem)) {
                        switch (e.keyCode) {
                            // Enter
                            case 13:
                                dojo.event.topic.publish('/calEvent',
                                    { 'action': 'saveFromForm' });
                                break;
                            // Delete
                            case 46:
                                dojo.event.topic.publish('/calEvent',
                                    { 'action': 'removeConfirm', 'data': item });
                                break;
                        }
                    }
                    break;
            }
        }
    };
};

cosmo.view.canvasBase = new function () {
    this.getSelectedItem = function () {
        var key = cosmo.app.pim.getSelectedCollectionId();
        var id = this.selectedItemIdRegistry[key];
        return this.view.itemRegistry.getItem(id) || null;
    };
    this.getSelectedItemCacheCopy = function () {
        var key = cosmo.app.pim.getSelectedCollectionId();
        var id = this.selectedItemIdRegistry[key];
        return this.selectedItemCache[id] || null;
    };
    this.setSelectedItem = function (p) {
        var key = cosmo.app.pim.getSelectedCollectionId();
        var id = '';
        var item = null;
        if (typeof p == 'string') {
            id = p;
            item = this.view.itemRegistry.getItem(id);
        }
        else {
            id = p.id;
            item = p;
        }
        this.selectedItemIdRegistry[key] = id;
        this.selectedItemCache[id] = item;
        return true;
    };
    this.clearSelectedItem = function () {
        var key = cosmo.app.pim.getSelectedCollectionId();
        this.selectedItemIdRegistry[key] = '';
        return true;
    };
    this.getSelectedItemId = function () {
        var key = cosmo.app.pim.getSelectedCollectionId();
        var id = this.selectedItemIdRegistry[key];
        return id;
    };
};

cosmo.view.getCurrentView = function () {
    return cosmo.view[cosmo.app.pim.currentView];
};

cosmo.view.handleUnsavedChanges = function (origSelection,
    discardFunc, cancelPreHook, savePreHook) {
    var converter = new cosmo.ui.DetailFormConverter(
        origSelection.data);
    var deltaAndError;
    try {
        deltaAndError = converter.createDelta();
    } catch (e){
        // This will happen if there was a problem in the createDelta
        // function
        if (e instanceof cosmo.model.exception.DetailItemNotDeltaItemException) {
            // If the detail item wasn't the delta item it means the ui
            // is out of sync. We really can't do anything smart in this
            // case, so just proceed with selecting the next item.
            return true;
        }
    }

    // This will be populated if there
    // was a validation error while creating
    // the delta.
    var error = deltaAndError[1];
    var delta = deltaAndError[0];
    if (error || delta.hasChanges()) {
        dojo.debug(error || delta);
        // Cancel button -- just hide the dialog, do nothing
        var cancel = function () {
            // Execute any pre-cancel code passed in
            if (typeof cancelPreHook == 'function') {
                cancelPreHook();
            }
            cosmo.app.hideDialog();
        }
        // Throw out the changes and proceed to highlight the
        // new item
        var discard = function () {
            cosmo.app.hideDialog();
            // Execute the discard function passed -- likely
            // re-invoking the original calling method with an
            // added 'true' flag for blowing by the unsaved-changes
            // check
            discardFunc();
        };
        // Save the changes
        var save = function () {
            var f = function () {
                // Execute any pre-save code passed in
                if (typeof savePreHook == 'function') {
                    savePreHook();
                }
                dojo.event.topic.publish('/calEvent',
                    { 'action': 'saveFromForm' });
            }
            // Hide the dialog first, wait for return value to
            // avoid contention for the use of the dialog box
            if (cosmo.app.hideDialog()) {
                setTimeout(f, 0);
            }
        };
        // Show the 'unsaved changes' dialog, with the appropriate
        // actions tied to each of the buttons
        cosmo.app.showDialog(cosmo.view.unsavedChangesDialog.getProps({
            cancelFunc: cancel,
            discardFunc: discard,
            saveFunc: save }));
        return false;
    }
    else {
        return true;
    }
};

cosmo.view.displayViewFromCollections = function (c) {
    var newCollection = c || null;
    if (newCollection) {
        cosmo.app.pim.setSelectedCollection(newCollection);
    }
    // Show the 'loading' message if there's a selected collection
    // to load data from
    if (cosmo.app.pim.getSelectedCollection()) {
        var loading = cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.loading;
        loading.show();
    }
    // Wrap in setTimeout so we don't lock up the UI
    // thread during the publish operation
    var f = function () { dojo.event.topic.publish('/calEvent', {
        action: 'loadCollection', opts: { loadType: 'changeCollection',
        collection: newCollection }, data: {}
    }); };
    // Make the timeout value greater than zero to
    // ensure that the 'loading' status message appears
    setTimeout(f, 35);
};


