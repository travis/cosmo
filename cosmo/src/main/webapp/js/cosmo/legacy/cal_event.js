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
 * @fileoverview Calendar events -- links the Lozenge to the CalEventData
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.legacy.cal_event");

dojo.require("cosmo.app.pim");
dojo.require("cosmo.model");
dojo.require('cosmo.view.cal');
dojo.require('cosmo.view.cal.lozenge');

/**
 * @object CalEvent -- an event on the Calendar, links to the event's
 * Lozenge and CalEventDate objects
 */
cosmo.legacy.cal_event.CalEvent = function(id, lozenge) {
    // Randomly generated ID for each CalEvent
    // Lozenge div elements get their id suffixes from this
    this.id = id;
    // Points to this event's Lozenge obj
    this.lozenge = lozenge;
    // Points to this event's CalEventData obj
    this.data = null;
    // A backup copy (clone) of the .data property made
    // before trying to edit
    this.dataOrig = null;
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
     * Allows the user to cancel before saving changes to an event
     * Restores event to its position previous to the aborted save
     */
    this.cancelSave = function () {
        // Put the lozenge back where it was
        // FIXME: Use topics
        var ev = cosmo.view.cal.canvas.getSelectedEvent();
        ev.restoreEvent();
        // Hide the confirmation dialog
        if (cosmo.app.modalDialog) {
            cosmo.app.hideDialog();
        }
    };
    /**
     * Creates a brand-new lozenge for the event
     * Resets the lozenge because we may be changing to the new type --
     * e.g., between all-day and normal, or between normal single
     * and normal composite
     */
    this.replaceLozenge = function () {
        // Remove the current lozenge
        this.lozenge.remove();
        // Replace with new one
        if (this.data.allDay) {
            this.lozenge = new cosmo.view.cal.lozenge.NoTimeLozenge(this.id);
        }
        else {
            this.lozenge = new cosmo.view.cal.lozenge.HasTimeLozenge(this.id);
        }
        this.lozenge.insert(this.id);
    };
    /**
     * Puts an event lozenge back where it originally was using the
     * snapshot backup clone of its data in the dataOrig property
     * Used when canceling a save from the confirmation dialog
     * and when a save operation fails
     */
    this.restoreEvent = function () {
        if (this.restoreFromSnapshot()) {
            this.lozenge.updateFromEvent(this);
            // Update lozenge and event detail form display
            // FIXME: Use topics
            var ev = cosmo.view.cal.canvas.getSelectedEvent();
            ev.lozenge.updateDisplayMain();
            ev.lozenge.setInputDisabled(false);
        }
    };
    /**
     * Makes a clone backup of the CalEventData for the event to
     * store in the dataOrig property. This is used to back out of
     * a cancelled/failed save operation or failed remove operation.
     */
    this.makeSnapshot = function () {
        // Make backup snapshot before saving
        // ================
        this.dataOrig = this.data.clone();
        return true;
    };
    /**
     * Makes a clone backup of the CalEventData for the event to
     * store in the dataOrig property. This is used to back out of
     * a cancelled/failed save operation or failed remove operation.
     */
    this.restoreFromSnapshot = function () {
        // Restore from backup snapshot
        // ================
        this.data = this.dataOrig.clone();
        return true;
    };
    /**
     * Whether the start and end properties of an event put it
     * on one side or the other of the current view span
     */
    this.isOutOfViewRange = function () {
        // Note event data dates are cosmo.datetime.Date, viewStart/viewEnd are Date
        // Return true only if both start and end are before view range
        // or both are after view range
        var ret = ((this.startsBeforeViewRange() && this.endsBeforeViewRange()) ||
            (this.startsAfterViewRange() && this.endsAfterViewRange()));
        return ret;

    };
    this.startsBeforeViewRange = function () {
        return (this.data.getEventStamp().getStartDate().toUTC() < cosmo.view.cal.viewStart.getTime());
    };
    this.endsBeforeViewRange = function () {
        return (this.data.getEventStamp().getEndDate().toUTC() < cosmo.view.cal.viewStart.getTime());
    };
    this.startsAfterViewRange = function () {
        return (this.data.getEventStamp().getStartDate().toUTC() > cosmo.view.cal.viewEnd.getTime());
    };
    this.endsAfterViewRange = function () {
        return (this.data.getEventStamp().getEndDate().toUTC() > cosmo.view.cal.viewEnd.getTime());
    };
    //toString = genericToString;
}
CalEvent = cosmo.legacy.cal_event.CalEvent;
