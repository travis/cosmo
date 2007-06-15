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
    var key = 'sort' + col + dir;
    // Get the comparator function
    var f = cosmo.view.list.sort.sorts[key];
    // Sort the list
    if (f) {
        cosmo.view.list.itemRegistry.sort(f);
        return true;
    }
    else {
        throw('Invalid sort column.');
    }
};
cosmo.view.list.sort.defaultDirections = {
    TASK: 'Asc',
    TITLE: 'Desc',
    STARTDATE: 'Desc',
    TRIAGE: 'Desc'
};
cosmo.view.list.sort.sorts = {
    sortTaskDesc: function (a, b) {
        var getTask = function (o) {
            return o.data.getTaskStamp() ? 1 : 0;
        }
        if (getTask(a) > getTask(b)) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortTaskAsc: function (a, b) {
        var getTask = function (o) {
            return o.data.getTaskStamp() ? 1 : 0;
        }
        if (getTask(a) < getTask(b)) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortTitleDesc: function (a, b) {
        if (a.data.getDisplayName() > b.data.getDisplayName()) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortTitleAsc: function (a, b) {
        if (a.data.getDisplayName() < b.data.getDisplayName()) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortStartDateDesc: function (a, b) {
        var getDt = function (o) {
            var st = o.data.getEventStamp();
            var dt = st ? st.getStartDate().getTime() : 0;
            return dt;
        }
        if (getDt(a) > getDt(b)) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortStartDateAsc: function (a, b) {
        var getDt = function (o) {
            var st = o.data.getEventStamp();
            var dt = st ? st.getStartDate().getTime() : 0;
            return dt;
        }
        if (getDt(a) < getDt(b)) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortTriageDesc: function (a, b) {
        if (a.data.getTriageStatus() > b.data.getTriageStatus()) {
            return 1;
        }
        else {
            return -1;
        }
    },
    sortTriageAsc: function (a, b) {
        if (a.data.getTriageStatus() < b.data.getTriageStatus()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}


