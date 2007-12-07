/*
 * Copyright 2006 Open Source Applications Foundation *
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
dojo.require('cosmo.view.BaseItem');
dojo.require('cosmo.view.list.ListItem');
dojo.require('cosmo.view.dialog');
dojo.require('cosmo.view.names');
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.service.exception");

dojo.lang.mixin(cosmo.view.list, cosmo.view.viewBase);

cosmo.view.list.hasBeenInitialized = false;

cosmo.view.list.viewId = cosmo.view.names.LIST;
// Stupid order-of-loading -- this gets set in the
// canvas instance. We'll just go ahead and declare
// it here anyway, so there's an obvious declaration
cosmo.view.list.canvasInstance =
    typeof cosmo.view.list.canvasInstance == 'undefined' ?
    null : cosmo.view.list.canvasInstance;

// The list of items -- cosmo.util.hash.Hash obj
cosmo.view.list.itemRegistry = null;

cosmo.view.list.columnOrder = ['TASK', 'TITLE', 'WHO', 'START', 'TRIAGE'];
// Note: the 'display' prop is used either as the key for the localized
// string, or as the key for the image icon in imagegrid.js
cosmo.view.list.columns = {
    TASK: { name: 'task', width: '20px', display: 'taskColumn', isIcon: true, initSort: 'Asc' },
    TITLE: { name: 'title', width: '50%', display: 'Title', isIcon: false, initSort: 'Desc' },
    WHO: { name: 'who', width: '20%', display: 'UpdatedBy', isIcon: false, initSort: 'Desc' },
    START: { name: 'start', width: '30%', display: 'StartsOn', isIcon: false, initSort: 'Desc' },
    TRIAGE: { name: 'triage', width: '36px', display: 'triageStatusColumn', isIcon: true, initSort: 'Desc' }
};

cosmo.view.list.triageStatusCodeMappings = {
    300: 'Done',
    100: 'Now',
    200: 'Later' };

cosmo.view.list.triageStatusCodeReverseMappings = {
    DONE: 300,
    NOW: 100,
    LATER: 200 };

cosmo.view.list.handlePub_calEvent = function (cmd) {

    if (!cosmo.view.list.isCurrentView()) { return false; }

    var act = cmd.action;
    var qual = cmd.qualifier || null;
    var data = cmd.data || {};
    var opts = cmd.opts;
    var delta = cmd.delta;
    switch (act) {
        case 'loadCollection':
            if (opts.loadType == 'changeCollection') {
                cosmo.view.list.loadItems();
            }
            break;
        default:
            // Do nothing
            break;
    }

};

cosmo.view.list.loadItems = function (o) {
    var opts = o || {};
    var note = opts.note || null;
    // Default to the app's selectedCollection if one isn't passed
    var collection = opts.collection || cosmo.app.pim.getSelectedCollection();
    //if (!cosmo.app.pim.getSelectedCollection()) return;
    var itemLoadList = null;
    var showErr = function (e) {
        cosmo.app.showErr(_('Main.Error.LoadItemsFailed'),"",e);
        return false;
    };

    // Load the array of items
    // ======================
    var deferred;
    // User has collections loaded, we have a selected collection
    // Look up for Note if passed a Note, otherwise get for
    // the entire collection
    //
    if (cosmo.app.pim.getSelectedCollection()) {
        deferred = cosmo.app.pim.serv.getDashboardItems(note || collection);
    }
    // User has no collections
    // Create a dummy Deferred with an empty item list
    else {
        deferred = new dojo.Deferred();
        deferred.addCallback(function () { return []; });
        deferred.callback();
    }
    // Catch any error stuffed in the deferred
    deferred.addErrback(function (e){
        if (e instanceof cosmo.service.exception.ResourceNotFoundException){
            cosmo.app.pim.reloadCollections({
                removedCollection: collection,
                removedByThisUser: false
            });
            return false;
        } else {
            showErr(e);
            return false;
        }
    });
    deferred.addCallback(function (itemLoadList){
        // Create a hash from the array
        var itemRegistry = cosmo.view.list.createItemRegistry(itemLoadList);
        cosmo.view.list.itemRegistry = itemRegistry;
        
        dojo.event.topic.publish('/calEvent', { action: 'eventsLoadSuccess',
                                                data: itemRegistry, opts: opts });
    });
    return deferred;
};

cosmo.view.list.createItemRegistry = function (arrParam) {
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
        var id = note.getItemUid();
        var item = new cosmo.view.list.ListItem();
        item.id = id;
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
    var cols = cosmo.view.list.columns;
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
    setVals(cols.TASK.name, sr, fm);
    // Title
    var t = data.getDisplayName();
    setVals(cols.TITLE.name, t, t);
    // Who
    var m = data.getModifiedBy();
    m = m ? m.getUserId() : '';
    setVals(cols.WHO.name, m, m);
    // Start
    var st = data.getEventStamp();
    var dt = st ? st.getStartDate() : null;
    var sr = dt ? dt.getTime() : 0;
    var fm = dt ? dt.strftime('%b %d, %Y %I:%M %p') : '';
    setVals(cols.START.name, sr, fm);
    // Triage
    var tr = data.getTriageStatus();
    var rank = parseInt(data.getRank());
    var fm = tr ? _('Dashboard.ListEntry.Triage' +
        this.triageStatusCodeMappings[tr]) : '(NONE)';
    tr = (tr * 10000000000);
    tr = tr + rank;
    setVals(cols.TRIAGE.name, tr, fm);

    // Use two separate keyword/val objs since
    // access to the values are done in totally
    // separate places
    item.sort = sort;
    item.display = display;
};

cosmo.view.list.createNoteItem = function (s) {
    var title = s;
    var errMsg = '';
    var item = new cosmo.view.list.ListItem();
    if (!title) {
        errMsg = 'New item must have a title.';
    }
    if (errMsg) {
        cosmo.app.showErr(_('Main.Error.ItemNewSaveFailed'), errMsg);
        return false;
    }
    else {
        var note = new cosmo.model.Note();
        var id = note.getUid();
        item.id = id;
        note.setDisplayName(title);
        note.setBody('');
        //normally the delta does the autotriaging, but since this is a new event
        //there is no delta, so we do it manually.
        note.autoTriage();
        item.data = note;
        // Precalc the values used in the table display and the sort
        this.setSortAndDisplay(item);
        // Stick the item in the registry
        cosmo.view.list.itemRegistry.setItem(id, item);
        // Make service call to save the item -- success from
        // the service will publish 'saveSuccess' action to tell
        // the UI to update appropriately
        dojo.event.topic.publish('/calEvent', { action: 'save', data: item,
            qualifier: 'new', saveType: 'new' })
        return cosmo.view.list.itemRegistry.getItem(id);
    }
};


