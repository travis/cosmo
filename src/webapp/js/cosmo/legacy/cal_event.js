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

/**
 * @fileoverview Calendar events -- links the Block to the CalEventData
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object CalEvent -- an event on the Calendar, links to the event's
 * Block and CalEventDate objects
 */
function CalEvent(id, block) {
    // Randomly generated ID for each CalEvent
    // Block div elements get their id suffixes from this
    this.id = id;
    // Points to this event's Block obj
    this.block = block;
    // Points to this event's CalEventData obj
    this.data = null;
    // A backup copy (clone) of the .data property made 
    // before trying to edit
    this.dataOrig = null;
    // If the event has a edit/remove call processing, don't allow
    // user to move/resize
    this.inputDisabled = false;
    // List of conflicting events that come before this event
    this.beforeConflicts = [];
    // List of conflicting events that come after this event
    this.afterConflicts = [];
    // Indent level
    this.conflictDepth = 0;
    // Total indent level
    this.maxDepth = 0;
    // Row occupied for an untimed event
    this.allDayRow = 0;
    
    /**
     * Indicates if an event has actually been edited or not --
     * used when dragging or moving to make sure event has really
     * changed before saving
     */
    this.hasChanged = function() {
        ret = false;
        if ((this.data.start.toUTC() != this.dataOrig.start.toUTC()) ||
            (this.data.end.toUTC() != this.dataOrig.end.toUTC())) {
            ret = true;
        }
        else {
            ret = false;
        }
        return ret;
    }
    /**
     * Gateway function for saving changes to events. Currently 
     * has bandaid of throwing up confirmation dialog for editing
     * recurring events. Non-recurring events pass right through to
     * remoteSave method.
     */
    this.remoteSaveMain = function() {
        // Deal with loss of scope from callback
        var ev = Cal.currSelObj;

        // BANDAID: Keep people from shooting themselves in the foot
        // with reurring events
        if (ev.data.masterEvent || ev.data.instance) {
            msg = 'This is a recurring event. Editing recurring events is not supported in Cosmo,' +
                ' and will probably have effects you do not intend.<br/>&nbsp;<br/>Save this change?';
            Cal.showSaveConfirm(msg);
        }
        else {
            ev.remoteSave();
        }
    };
    /**
     * Main function for saving changes to events.
     * Call to the Cosmo service happens on a short timeout to
     * give time for the processing state to display. This avoids
     * horrible, Pokemon-seizure-inducing flashes on screen
     * Does the visual stuff first, then hands off to remoteSaveDelay
     */
    this.remoteSave = function() {
        // Deal with loss of scope from callback
        var ev = Cal.currSelObj;

        // Block stuff
        // =====================
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

        // Call remote save process
        // =====================
        // Paranoia check to ensure element is actually there
        if (Cal.eventRegistry.getItem(ev.id)) {
            setTimeout('Cal.eventRegistry.getItem(\'' + ev.id +
                '\').remoteSaveDelay();', 500);
        }

        if (Cal.dialog.isDisplayed) {
            Cal.hideDialog();
        }

    };
    /**
     * Allows the user to cancel before saving changes to an event
     * Restores event to its position previous to the aborted save
     */
    this.cancelSave = function() {
        // Put the block back where it was
        Cal.currSelObj.restoreEvent();
        // Hide the confirmation dialog
        if (Cal.dialog) {
            Cal.hideDialog();
        }
    };
    /**
     * Creates a brand-new block for the event
     * Resets the block because we may be changing to the new type --
     * e.g., between all-day and normal, or between normal single
     * and normal composite
     */
    this.replaceBlock = function() {
        // Remove the current block
        this.block.remove();
        // Replace with new one
        if (this.data.allDay) {
            this.block = new NoTimeBlock(this.id);
        }
        else {
            this.block = new HasTimeBlock(this.id);
        }
        this.block.insert(this.id);
    };
    /**
     * Puts an event block back where it originally was using the
     * snapshot backup clone of its data in the dataOrig property
     * Used when canceling a save from the confirmation dialog
     * and when a save operation fails
     */
    this.restoreEvent = function() {
        if (this.restoreFromSnapshot()) {
            this.block.updateFromEvent(this);
            // Update block and event detail form display
            Cal.currSelObj.block.updateDisplayMain();
            this.setInputDisabled(false);
        }
    };
    /**
     * Called from remoteSave -- this method actually calls the
     * Cosmo service to save the changes
     * Since the handler for the XHR request will have no idea
     * what event is just saved, we save all that info in the
     * asyncRegistry referenced by the requestId. The info about the
     * processing request is stored in an Async object.
     * The handler for the XHR response is handleRemoteSaveResult
     */
    this.remoteSaveDelay = function() {
        var requestId = Cal.serv.saveEvent(
            this.handleRemoteSaveResult, Cal.currentCalendar.path, this.data);
        // New Async obj to track this request
        var saveAsync = new Async(requestId, this, 'saveEvent');
        // Add to stack of processing requests
        Cal.asyncRegistry.setItem(requestId, saveAsync);
    };
    /**
     * Calls the remote service method that removes the event on 
     * the backend
     * The remote call works the same was as the save -- the info
     * about the XHR request is saved in the asyncRegistry to be
     * retrieved by the response handler
     * The handler for the XHR response is handleRemoveResult
     */
    this.remove = function() {
        var requestId = Cal.serv.removeEvent(
            this.handleRemoveResult, Cal.currentCalendar.path, this.data.id);
        // New Async obj to track this request
        var removeAsync = new Async(requestId, this, 'removeEvent');
        // Add to stack of processing requests
        Cal.asyncRegistry.setItem(requestId, removeAsync);

    };
    /**
     * Handler for XHR request from remoteSaveDelay. Looks up info
     * about the XHR request from the asyncRegistry to find the event
     * @param str ID of the CalEventData object -- the ID of the event
     * on the backend 
     * @param e Error if any
     * @param requestId Int the ID of the request, used to look up
     * which event was being processed
     */
    this.handleRemoteSaveResult = function(str, e, requestId) {
        // Look up the Async process
        var saveAsync = Cal.asyncRegistry.getItem(requestId);
        // Look up the event
        var saveObj = saveAsync.procObj;
        // Simple error message to go along with details from Error obj
        var errMsg = '';
        
        // Failure -- display exception info
        // ============
        if (e) {
            // Failed update -- fall back to original state
            if (saveObj.dataOrig) {
                // Restore from backup snapshot
                saveObj.restoreEvent();
                // Re-enable user input on this event
                saveObj.setInputDisabled(false);
                errMsg = getText('Main.Error.EventEditSaveFailed');
            }
            // Failed create -- remove fake placeholder event and block
            else {
                // Remove all the client-side stuff associated with this event
                Cal.removeCalEvent(saveObj);
                errMsg = getText('Main.Error.EventNewSaveFailed');
            }
            // Enable the remove button if it was previously disabled
            Cal.calForm.setButtons(true, true);
            
            Cal.showErr(errMsg, e);
        }
        // Success
        // ============
        else {
            // Remove from the stack of processing requests
            Cal.asyncRegistry.removeItem(requestId);
            // Set the CalEventData ID from the value returned by server
            if (!saveObj.data.id) {
                saveObj.data.id = str;
            }
            
            // If new dates are out of range, remove the event from display
            if (saveObj.isOutOfViewRange()) {
                // Remove all the client-side stuff associated with this event
                Cal.removeCalEvent(saveObj);
                // Disable the Remove and Save buttons
                Cal.calForm.setButtons(false, false);
                // Clear out form values
                Cal.calForm.clear();
            }
            // Otherwise update display
            else {
                // Update the detail form
                Cal.calForm.updateFromEvent(saveObj);
                
                // Re-enable user input on this event
                saveObj.setInputDisabled(false);
                // Enable the remove button if it was previously disabled
                Cal.calForm.setButtons(true, true);
            }

            // Update entire display of events
            Cal.updateEventsDisplay();
        }
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
    };
    /**
     * Handler for XHR request from 'remove' method. Looks up info
     * about the XHR request from the asyncRegistry to find the event
     * @param str ID of the CalEventData object -- the ID of the event
     * on the backend 
     * @param e Error if any
     * @param requestId Int the ID of the request, used to look up
     * which event was being removed
     */
    this.handleRemoveResult = function(str, e, requestId) {
        // Look up the Async process
        var removeAsync = Cal.asyncRegistry.getItem(requestId);
        // Look up the event
        var removeObj = removeAsync.procObj;
        var errMsg = getText('Main.Error.EventRemoveFailed');
        if (e) {
            Cal.showErr(errMsg, e);
        }
        else {
            // Remove all the client-side stuff associated with this event
            Cal.removeCalEvent(removeObj);
            // Disable the Remove and Save buttons
            Cal.calForm.setButtons(false, false);
        }
        
        // Update entire display of events
        Cal.updateEventsDisplay();
        
        // Resets local timer for timeout -- we know server-side
        // session has been refreshed
        // ********************
        // BANDAID: need to move this into the actual Service call
        // ********************
        Cal.serv.resetServiceAccessTime();
    };
    /**
     * Makes a clone backup of the CalEventData for the event to 
     * store in the dataOrig property. This is used to back out of
     * a cancelled/failed save operation or failed remove operation.
     */
    this.makeSnapshot = function() {
        // Make backup snapshot before saving
        // ================
        this.dataOrig = CalEventData.clone(this.data);
        return true;
    };
    /**
     * Makes a clone backup of the CalEventData for the event to 
     * store in the dataOrig property. This is used to back out of
     * a cancelled/failed save operation or failed remove operation.
     */
    this.restoreFromSnapshot = function() {
        // Restore from backup snapshot
        // ================
        this.data = CalEventData.clone(this.dataOrig);
        return true;
    };
    /**
     * Enable/disable user input for this event -- should be disabled
     * when a remote operation is processing
     * TO-DO: Since this is just a checked property, do we really need
     * setter/getter for this?
     */
    this.setInputDisabled = function(isDisabled) {
        if (isDisabled) {
            this.inputDisabled = true;
        }
        else {
            this.inputDisabled = false;
        }
        return this.inputDisabled;
    };
    /**
     * Whether or not input is disabled for this event -- usually
     * because of a remote operation processing for the event
     * TO-DO: Since this is just a checked property, do we really need
     * setter/getter for this?
     */
    this.getInputDisabled = function() {
        return this.inputDisabled;
    };
    /**
     * Whether the start and end properties of an event put it
     * on one side or the other of the current view span
     */
    this.isOutOfViewRange = function() {
        // Note event data dates are ScoobyDate, viewStart/viewEnd are Date
        // Return true only if both start and end are before view range
        // or both are after view range
        var ret = ((this.startsBeforeViewRange() && this.endsBeforeViewRange()) ||
            (this.startsAfterViewRange() && this.endsAfterViewRange()));
        return ret;

    };
    this.startsBeforeViewRange = function() {
        return (this.data.start.toUTC() < Cal.viewStart.getTime());
    };
    this.endsBeforeViewRange = function() {
        return (this.data.end.toUTC() < Cal.viewStart.getTime());
    };
    this.startsAfterViewRange = function() {
        return (this.data.start.toUTC() > Cal.viewEnd.getTime());
    };
    this.endsAfterViewRange = function() {
        return (this.data.end.toUTC() > Cal.viewEnd.getTime());
    };
    //toString = genericToString;
}
