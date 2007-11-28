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

dojo.provide('cosmo.view.list.sort');

dojo.require('cosmo.view.list.common');

cosmo.view.list.sort.doSort = function (hash, col, dir) {
    var currSortKey = col.substr(0, 1).toLowerCase() + col.substr(1);
    // Sort based on the precalc'd values in item.sort
    var currSort = function (a, b) {
        var valA = a.sort[currSortKey];
        var valB =  b.sort[currSortKey];
        valA = (typeof valA == 'string') ? valA.toLowerCase() : valA;
        valB = (typeof valB == 'string') ? valB.toLowerCase() : valB;
        if (valA == valB) {
            // If sort is already on title, secondary sort is uid
            // (it could be anything; I just picked that out of the air)
            var newKey = (currSortKey == 'title') ? 'uid' : 'title';
            if (a.sort[newKey] > b.sort[newKey]) {
                r = 1;
            }
            else {
                r = -1;
            }
        }
        else if (valA > valB) {
            r = 1;
        }
        else {
            r = -1;
        }
        // Reverse sort for Asc
        r = dir == 'Desc' ? r : (0 - r);
        return r;
    };
    // Sort the list
    cosmo.view.list.itemRegistry.sort(currSort);
    return true;
};

