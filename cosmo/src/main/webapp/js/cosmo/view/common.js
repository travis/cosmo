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

cosmo.view.viewBase = new function () {
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
    this.filterOutRecurrenceGroup = function (reg, arr, dt, ignore) {
        // Default behavior is to remove the lozenge
        var str = ',' + arr.join() + ',';
        var h = new Hash();
        var item = null;
        var compDt = dt ? new cosmo.datetime.Date(dt.getFullYear(),
            dt.getMonth(), dt.getDate(), 23, 59) : null;
        while (item = reg.pop()) {
            var removeForDate = true;
            var keep = false;
            switch (true) {
                // Any to be specifically ignored -- this is all-mighty
                case (item.id == ignore):
                    keep = true;
                    break;
                // Any that don't have matching ids -- keep these too
                case (str.indexOf(',' + item.data.getUid() + ',') == -1):
                    keep = true;
                    break;
                // Matching ids -- candidates for removal
                case (str.indexOf(',' + item.data.getUid() + ',') > -1):
                    var eventStamp = item.data.getEventStamp();
                    var startDate = eventStamp.getStartDate();
                    var endDate = eventStamp.getEndDate();
                    // If also filtering by date, check the start date of
                    // matching items as well
                    if (compDt && (startDate.toUTC() < compDt.toUTC())) {
                        keep = true;
                    }
                    break;
                default:
                    // Throw it out
                    break;
            }
            if (keep) { h.setItem(item.id, item); }
        }
        return h;
    };
};

cosmo.view.canvasBase = new function () {
    this.getSelectedItem = function () {
        var key = cosmo.app.pim.currentCollection.getUid();
        var id = this.selectedEventIdRegistry[key];
        return this.view.itemRegistry.getItem(id);
    };
    this.setSelectedItem = function (ev) {
        var key = cosmo.app.pim.currentCollection.getUid();
        this.selectedEventIdRegistry[key] = ev.id;
        return true;
    };
    this.clearSelectedItem = function (ev) {
        var key = cosmo.app.pim.currentCollection.getUid();
        this.selectedEventIdRegistry[key] = '';
        return true;
    };
    this.getSelectedItemId = function () {
        var key = cosmo.app.pim.currentCollection.getUid();
        var id = this.selectedEventIdRegistry[key];
        return id;
    }
};

