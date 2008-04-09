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
 * @fileoverview Calendar events -- links the Lozenge to the CalItemData
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.view.cal.CalItem");

dojo.require("cosmo.app.pim");
dojo.require("cosmo.model");
dojo.require("cosmo.view.BaseItem");
dojo.require('cosmo.view.cal.common');
dojo.require('cosmo.view.cal.lozenge');

/**
 * @object CalItem -- an event on the Calendar, links to the event's
 * Lozenge and CalItemDate objects
 */
cosmo.view.cal.CalItem = function(data, collectionIds) {
    // Same as the UID for the stamped Note, used as the
    // key for the itemRegistry Hash
    // Lozenge div elements also get their id suffixes from this
    this.id = data.getItemUid() || '';
    // Points to this event's stamped Note obj
    this.data = data || null;
    // The UID for the collection containing this item
    this.collectionIds = collectionIds || [];
    // Points to this event's Lozenge obj
    this.lozenge = null;
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
    // Primary collection if item is in multiple
    this.primaryCollectionId = null;
};

cosmo.view.cal.CalItem.prototype = new cosmo.view.BaseItem();

/**
 * Is the item currently in a 'processing' state -- i.e.,
 * is something being done to it server-side
 */
cosmo.view.cal.CalItem.prototype.isDisabled = function () {
    return this.lozenge.getInputDisabled()
};
/**
 * Restores event to its position previous to an aborted save
 */
cosmo.view.cal.CalItem.prototype.cancelSave = function () {
    this.restoreEvent();
};
/**
 * Puts an event lozenge back where it originally was using the
 * snapshot backup clone of its data in the dataOrig property
 * Used when canceling a save from the confirmation dialog
 * and when a save operation fails
 */
cosmo.view.cal.CalItem.prototype.restoreEvent = function () {
    if (this.restoreFromSnapshot()) {
        this.lozenge.updateFromEvent(this);
        // Update lozenge and event detail form display
        this.lozenge.updateDisplayMain();
        this.lozenge.setInputDisabled(false);
    }
};
/**
 * Whether the start and end properties of an event put it
 * on one side or the other of the current view span
 */
cosmo.view.cal.CalItem.prototype.isOutOfViewRange = function () {
    // Event data dates are cosmo.datetime.Date, viewStart/viewEnd are Date
    // Return true only if both start and end are before view range
    // or start is after view range (we can assume end is equal to or
    // later than start)
    // Notes:
    // 1. If both the start and end are equal to the beginning
    // of the range, it's an at-time event we need to keep
    // (e.g., Start: 12 a.m. Sunday, End: 12 a.m. Sunday)
    // 2. Throw out items that start exactly at the end time of
    // the view range (we can assume end will be at the same
    // time, or later)
    var ret = ((this.startsBeforeViewRange() && 
        this.endsBeforeViewRange(true)) ||
        this.startsAfterViewRange(true));
    return ret;

};
cosmo.view.cal.CalItem.prototype.startsBeforeViewRange = 
    function (inclusive) {
    var dtA = this.data.getEventStamp().getStartDate().getTime();
    var dtB = cosmo.view.cal.viewStart.getTime();
    return inclusive ? dtA <= dtB : dtA < dtB;
};
cosmo.view.cal.CalItem.prototype.endsBeforeViewRange = 
    function (inclusive) {
    var dtA = this.data.getEventStamp().getEndDate().getTime();
    var dtB = cosmo.view.cal.viewStart.getTime();
    return inclusive ? dtA <= dtB : dtA < dtB;
};
cosmo.view.cal.CalItem.prototype.startsAfterViewRange = 
    function (inclusive) {
    var dtA = this.data.getEventStamp().getStartDate().getTime();
    var dtB = cosmo.view.cal.viewEnd.getTime();
    return inclusive ? dtA >= dtB : dtA > dtB;
};
cosmo.view.cal.CalItem.prototype.endsAfterViewRange = 
    function (inclusive) {
    var dtA = this.data.getEventStamp().getEndDate().getTime();
    var dtB = cosmo.view.cal.viewEnd.getTime();
    return inclusive ? dtA >= dtB : dtA > dtB;
};
cosmo.view.cal.CalItem.prototype.isInSelectedCollection =
    function () {
    var selCollId = cosmo.app.pim.getSelectedCollectionId();
    var selColl = cosmo.view.cal.collectionItemRegistries[selCollId];
    return !!selColl.getItem(this.id);

};
cosmo.view.cal.CalItem.prototype.removeCollection =
    function (coll) {
    var id = coll.getUid();
    if (this.primaryCollectionId == id) {
        this.primaryCollectionId = null;
    }
    var collIds = ',' + this.collectionIds.join() + ',';
    collIds = collIds.replace(',' + id + ',', ',');
    collIds = collIds.substr(1, collIds.length - 2);
    this.collectionIds = collIds.length ? collIds.split(',') : [];
};


