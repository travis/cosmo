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

dojo.provide('cosmo.view.list.common');

dojo.require('dojo.event.*');
dojo.require("cosmo.model");
dojo.require("cosmo.app");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.app.pim.layout");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.service.exception");


// The list of items
cosmo.view.list.itemRegistry = null;

cosmo.view.list.triageStatusCodeNumbers = {
    DONE: 300,
    NOW: 100,
    LATER: 200 };

cosmo.view.list.triageStatusCodeNumberMappings = {
    300: 'done',
    100: 'now',
    200: 'later' };

cosmo.view.list.triageStatusCodeStrings = {
    DONE: 'done',
    NOW: 'now',
    LATER: 'later' };

cosmo.view.list.loadItems = function (o) {
    var opts = o;
    var collection = opts.collection;
    var itemLoadList = null;
    var itemLoadHash = new cosmo.util.hash.Hash();
    var statuses = cosmo.view.list.triageStatusCodeStrings;
    var getStatus = opts.triageStatus;
    var loadItemsByTriage = function (stat) {
        // Load the array of items
        // ======================
        try {
            var deferred = cosmo.app.pim.serv.getItems(collection,
                { triage: stat }, { sync: true });
            var results = deferred.results;
            // Catch any error stuffed in the deferred
            if (results[1] instanceof Error) {
                showErr(results[1]);
                return false;
            }
            else {
                itemLoadList = results[0];
            }
        }
        catch (e) {
            showErr(e);
            return false;
        }
        // Create a hash from the array
        var h = cosmo.view.list.createItemRegistry(itemLoadList, stat);
        itemLoadHash.append(h);
        return true;
    };
    var showErr = function (e) {
        cosmo.app.showErr(_('Main.Error.LoadEventsFailed'),
            e);
        return false;
    };
    // Loading a specific status
    if (getStatus) {
        loadItemsByTriage(getStatus);
    }
    // Loading all the statuses
    else {
        // We don't care about the order of loading -- have to sort
        // client-side anyway
        for (var n in statuses) {
            if (!loadItemsByTriage(statuses[n])) { return false; }
        }
    }
    cosmo.view.list.itemRegistry = itemLoadHash;
    // This could be done with topics to avoid the explicit dependency
    listCanvas = cosmo.app.pim.baseLayout.mainApp.centerColumn.listCanvas;
    listCanvas.initListProps();
    listCanvas.render();
    return true;
};

cosmo.view.list.createItemRegistry = function (arrParam, statusParam) {
    var h = new cosmo.util.hash.Hash();
    var arr = [];

    // Param may be a single array, or hashmap of arrays -- one
    // for each recurring event sequence
    // ---------------------------------
    // If passed a simple array, use it as-is
    if (arrParam.length) {
        arr = arrParam;
    }
    // If passed a hashmap of arrays, suck all the array items
    // into one array
    else {
        for (var j in arrParam) {
            var a = arrParam[j];
            for (var i = 0; i < a.length; i++) {
                arr.push(a[i]);
            }
        }
    }

    for (var i = 0; i < arr.length; i++) {
        var note = arr[i];
        var eventStamp = note.getEventStamp();
        var id = note.getUid();
        var item = {};
        item.data = note;
        // Precalculate values used for sort/display
        // to avoid doing the same calculations twice
        this.setSortAndDisplay(item);
        h.setItem(id, item);
    }
    return h;
};

// Getting an appropriate value to display and to sort on
// require a lot of the same calculations -- don't do them
// twice
cosmo.view.list.setSortAndDisplay = function (item) {
    var sort = {};
    var display = {};
    var data = item.data;
    var setVals = function (key, s, d) {
        sort[key] = s; // Precalc'd values used in the sort
        display[key] = d; // Precalc'd values used in the table display
    }
    // Uid 
    var uid = data.getItemUid();
    setVals('uid', uid, uid);
    // Task-ness
    var sr = data.getTaskStamp() ? 1 : 0;
    var fm = sr ? '[x]' : '';
    setVals('task', sr, fm);
    // Title 
    var t = data.getDisplayName();
    setVals('title', t, t);
    // Who
    var m = data.getModifiedBy().userId;
    m = m ? m : cosmo.app.currentUsername; 
    setVals('who', m, m);
    // Start
    var st = data.getEventStamp();
    var dt = st ? st.getStartDate() : null;
    var sr = dt ? dt.getTime() : 0;
    var fm = dt ? dt.strftime('%b %d, %Y %I:%M%p') : '';
    setVals('startDate', sr, fm);
    // Triage
    var tr = data.getTriageStatus();
    var fm = this.triageStatusCodeNumberMappings[tr];
    setVals('triage', tr, fm);

    // Use two separate keyword/val objs since
    // access to the values are done in totally
    // separate places
    item.sort = sort;
    item.display = display;
};
