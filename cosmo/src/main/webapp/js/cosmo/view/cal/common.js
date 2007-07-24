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

dojo.provide('cosmo.view.cal.common');

dojo.require("cosmo.app.pim");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.model");
dojo.require("cosmo.view.cal.CalItem");
dojo.require("cosmo.view.names");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.util");
dojo.require('cosmo.view.service');
dojo.require("cosmo.service.exception");

dojo.lang.mixin(cosmo.view.cal, cosmo.view.viewBase);

cosmo.view.cal.hasBeenInitialized = false;

cosmo.view.cal.viewId = cosmo.view.names.CAL;
// Stupid order-of-loading -- this gets set in the
// canvas instance. We'll just go ahead and declare
// it here anyway, so there's an obvious declaration
cosmo.view.cal.canvasInstance =
    typeof cosmo.view.cal.canvasInstance == 'undefined' ?
    null : cosmo.view.cal.canvasInstance;
cosmo.view.cal.viewStart = null;
cosmo.view.cal.viewEnd = null;
// The list of items -- cosmo.util.hash.Hash obj
cosmo.view.cal.itemRegistry = null;

/**
 * Handle events published on the '/calEvent' channel, including
 * self-published events
 * @param cmd A JS Object, the command containing orders for
 * how to handle the published event.
 */
cosmo.view.cal.handlePub_calEvent = function (cmd) {

    if (!cosmo.view.cal.isCurrentView()) { return false; }

    // Ignore input when not the current view
    var _pim = cosmo.app.pim;
    if (_pim.currentView != _pim.views.CAL) {
        return false;
    }

    var act = cmd.action;
    var qual = cmd.qualifier || null;
    var data = cmd.data || {};
    var opts = cmd.opts;
    var delta = cmd.delta;
    switch (act) {
        case 'loadCollection':
            // Only try to load events if the collection exists
            if (cosmo.app.pim.currentCollection){
                cosmo.view.cal.triggerLoadEvents(opts);
            }
            break;
        default:
            // Do nothing
            break;
    }
};

cosmo.view.cal.triggerLoadEvents = function (o) {
    dojo.debug("trigger!");
    var _cal = cosmo.view.cal; // Scope-ness
    var opts = {};
    var goToNav = o.goTo;

    // Changing dates
    // FIXME: There is similar logic is dup'd in ...
    // view.cal.common.triggerLoadEvents
    // ui.minical.handlePub
    // ui.minical -- setSelectionSpan private function
    // ui.navbar._showMonthheader
    // These different UI widgets have to be independent
    // of the calendar view, but still display sync'd
    // information -- what's a good way to consolidate this?
    // --------
    if (goToNav) {
        dojo.debug("goto");
        // param is 'back' or 'next'
        if (typeof goToNav == 'string') {
            var key = goToNav.toLowerCase();
            var incr = key.indexOf('back') > -1 ? -1 : 1;
            queryDate = cosmo.datetime.Date.add(_cal.viewStart,
                dojo.date.dateParts.WEEK, incr);
        }
        // param is actual Date
        else {
            queryDate = goToNav;
        }
        // Update _cal.viewStart and _cal.viewEnd with new dates
        _cal.setQuerySpan(queryDate);
    }

    // Opts obj to pass to topic publishing
    opts = {
        viewStart: _cal.viewStart,
        viewEnd: _cal.viewEnd,
        currDate: cosmo.app.pim.currDate
    }
    // Pass along the original opts
    for (var n in o) { opts[n] = o[n]; }

    _cal.loadEvents(opts);
};
/**
 * Loading events in the initial app setup, and week-to-week
 * navigation.
 * @param start Number, timestamp for the start of the query
 * period
 * @param end Number, timestamp for the end of the query
 * period
 * @return Boolean, true
 */
cosmo.view.cal.loadEvents = function (o) {
    var opts = o || {};
    var _cal = cosmo.view.cal; // Scope-ness
    // Default to the app's currentCollection if one isn't passed
    var collection = opts.collection || cosmo.app.pim.currentCollection;
    if (!cosmo.app.pim.currentCollection) return;
    var start = null;
    var end = null;
    var eventLoadList = null;
    var eventLoadHash = new Hash();
    var isErr = false;
    var detail = '';
    var evData = null;
    var id = '';
    var ev = null;

    // If nothing explicit is passed for the query time-bounds,
    // initialize viewStart and viewEnd to the current week
    if (!opts.viewStart || !opts.viewEnd) {
        _cal.setQuerySpan(cosmo.app.pim.currDate);
        opts.viewStart = _cal.viewStart;
        opts.viewEnd = _cal.viewEnd;
    }

    start = opts.viewStart;
    end = opts.viewEnd;

    var handleErr = function (e) {
        if (e instanceof cosmo.service.exception.ResourceNotFoundException){
            cosmo.app.pim.reloadCollections();
        }
        cosmo.app.showErr(_('Main.Error.LoadItemsFailed'),"", e);
    };
    // Load the array of events
    // ======================
    try {
        var deferred = cosmo.app.pim.serv.getItems(collection,
            { start: start, end: end }, { sync: true });
        var results = deferred.results;
        // Catch any error stuffed in the deferred
        if (results[1]) {
            handleErr(results[1]);
            return false;
        }
        else {
            eventLoadList = results[0];
        }
    }
    catch(e) {
        handleErr(e);
        return false;
    }

    var eventLoadHash = this.createEventRegistry(eventLoadList);
    dojo.event.topic.publish('/calEvent', { action: 'eventsLoadSuccess',
        data: eventLoadHash, opts: opts });
    return true;
};
/**
 * Create a Hash of CalItem objects with data property of stamped
 * Note objects.
 * @param arrParam Either an Array, or JS Object with multiple Arrays,
 * containing stamped Note objects
 * @return Hash, the keys are the UID of the Notes, and the values are
 * the CalItem objects.
 */
cosmo.view.cal.createEventRegistry = function(arrParam) {
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
        var id = note.getItemUid();
        var ev = new cosmo.view.cal.CalItem(id, null);
        ev.data = note;
        h.setItem(id, ev);
    }
    return h;
}
/**
 * Get the start and end for the span of time to view in the cal
 */
cosmo.view.cal.setQuerySpan = function (dt) {
    this.viewStart = cosmo.datetime.util.getWeekStart(dt);
    dojo.debug("viewStart: " + this.viewStart)
    this.viewEnd = cosmo.datetime.util.getWeekEnd(dt);
    dojo.debug("viewEnd: " + this.viewEnd)
    return true;
};
/**
 * Get the datetime for midnight Sunday given the current Sunday
 * and the number of weeks to move forward or backward
 */
cosmo.view.cal.getNewViewStart = function (key) {
    var queryDate = null;
    var incr = 0;
    // Increment/decrement week
    if (key.indexOf('next') > -1) {
        incr = 1;
    }
    else if (key.indexOf('back') > -1) {
        incr = -1;
    }
    queryDate = cosmo.datetime.Date.add(this.viewStart,
        dojo.date.dateParts.WEEK, incr);
    return queryDate;
};


