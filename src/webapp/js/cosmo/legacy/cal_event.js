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
        var d = this.data;
        var dO = this.dataOrig;
        var ret = [];
        var compareDateTime = function(curr, orig) {
            if (curr.toUTC() != orig.toUTC()) {
                return true;
            }
            else {
                return false;
            }
        }
        var compareRecurrence = function(curr, orig) {
            if (!curr && orig || curr && !orig) {
                return true;
            }
            else {
                if ((curr.frequency != orig.frequency) ||
                    (curr.endDate != orig.endDate)) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        var compareList = {
            'start': compareDateTime,
            'end': compareDateTime,
            'title': null,
            'description': null,
            'allDay': null,
            'pointInTime': null,
            'anyTime': null,
            'recurrenceRule': compareRecurrence,
            'status': null
        }
        
        function compareVals(prop, curr, orig, f) {
            var diff
            var a = curr || null;
            var b = orig || null
            if (f) {
                if (f(a, b)) {
                    ret.push([prop, a, b])
                }
            }
            else {
                if (a != b) {
                    ret.push([prop, a, b])
                }
            }
        }
        
        for (var i in compareList) {
            compareVals(i, d[i], dO[i], compareList[i]);
        }
        ret = ret.length ? ret : null;
        return ret;
    }
    /**
     * Allows the user to cancel before saving changes to an event
     * Restores event to its position previous to the aborted save
     */
    this.cancelSave = function() {
        // Put the block back where it was
        // FIXME: Use topics
        var ev = cosmo.view.cal.canvas.getSelectedEvent();
        ev.restoreEvent();
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
            // FIXME: Use topics
            var ev = cosmo.view.cal.canvas.getSelectedEvent();
            ev.block.updateDisplayMain();
            this.setInputDisabled(false);
        }
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
