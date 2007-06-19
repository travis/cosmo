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
    WHO: 'Desc',
    TITLE: 'Desc',
    STARTDATE: 'Desc',
    TRIAGE: 'Desc'
};
cosmo.view.list.sort.sorts = {
    sortTaskDesc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTask(a, b);
    },
    sortTaskAsc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTask(b, a);
    },
    sortTask: function (a, b) {
        var getTask = function (o) {
            return o.data.getTaskStamp() ? 1 : 0;
        }
        var valA = getTask(a);
        var valB = getTask(b);
        if (valA > valB) {
            return 1;
        }
        else if (valA < valB) {
            return -1;
        }
        // Secondary sort on Title
        else {
            return cosmo.view.list.sort.sorts.sortTitleDesc(a, b);
        }
    },
    sortTitleDesc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTitle(a, b);
    },
    sortTitleAsc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTitle(b, a);
    },
    sortTitle: function (a, b) {
        var valA = a.data.getDisplayName();
        var valB = b.data.getDisplayName();
        if (valA > valB) {
            return 1;
        }
        else if (valA < valB) {
            return -1;
        }
        // Secondary sort -- doesn't have to be the UID, but
        // if you're going to change it to something else,
        // do the code inline here -- don't use a function
        // that falls back to sorting on Title; you'll end up
        // in an endless loop if the item has identical values
        // for both columns
        else {
            if (a.data.getItemUid() > b.data.getItemUid()) {
                return 1;
            }
            else {
                return -1;
            }
        }
    },
    sortWhoDesc: function (a, b) {
    },
    sortWhoAsc: function (a, b) {
    },
    sortWho: function (a, b) {

    },
    sortStartDateDesc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortStartDate(a, b);
    },
    sortStartDateAsc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortStartDate(b, a);
    },
    sortStartDate: function (a, b) {
        var getDt = function (o) {
            var st = o.data.getEventStamp();
            var dt = st ? st.getStartDate().getTime() : 0;
            return dt;
        }
        var valA = getDt(a);
        var valB = getDt(b);
        if (valA > valB) {
            return 1;
        }
        else if (valA < valB) {
            return -1;
        }
        // Secondary sort on Title
        else {
            return cosmo.view.list.sort.sorts.sortTitleDesc(a, b);
        }
    },
    sortTriageDesc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTriage(a, b);
    },
    sortTriageAsc: function (a, b) {
        return cosmo.view.list.sort.sorts.sortTriage(b, a);
    },
    sortTriage: function (a, b) {
        var valA = a.data.getTriageStatus();
        var valB = b.data.getTriageStatus();
        if (valA > valB) {
            return 1;
        }
        else if (valA < valB) {
            return -1;
        }
        // Secondary sort on Title
        else {
            return cosmo.view.list.sort.sorts.sortTitleDesc(a, b);
        }
    },
}

