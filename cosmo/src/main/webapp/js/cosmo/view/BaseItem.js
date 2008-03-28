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

dojo.provide("cosmo.view.BaseItem");

dojo.require("cosmo.model");

cosmo.view.BaseItem = function(id) {
    this.id = id || '';
}

/**
 * Makes a clone backup of the stamped Note data for the item to
 * store in the dataOrig property. This is used to back out of
 * a cancelled/failed save operation or failed remove operation.
 */
cosmo.view.BaseItem.prototype.makeSnapshot = function () {
    // Make backup snapshot before saving
    // ================
    if (this.data.isMaster()){
        this.dataOrig = this.data.clone();
        this.occurrence = false;
    } else {
        this.dataOrig = this.data.getMaster().clone();
        this.occurrence = true;
        this.recurrenceId = this.data.recurrenceId
    }
    return true;
};
/**
 * Makes a clone backup of the stamped Note data for the item to
 * store in the dataOrig property. This is used to back out of
 * a cancelled/failed save operation or failed remove operation.
 */
cosmo.view.BaseItem.prototype.restoreFromSnapshot = function () {
    // Restore from backup snapshot
    
    if (!this.dataOrig){
        return true;
    }
    
    if (!this.occurrence){
        this.data = this.dataOrig.clone();
    } else {
        this.data = this.dataOrig.getNoteOccurrence(this.recurrenceId);
    }
    return true;
};
/**
 * 
 */
cosmo.view.BaseItem.prototype.recurrenceRemoved = function () {
    return !!(this.dataOrig &&
        !this.data.hasRecurrence() && this.dataOrig.hasRecurrence());
};

