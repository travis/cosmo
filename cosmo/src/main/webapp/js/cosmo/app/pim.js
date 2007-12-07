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

dojo.provide("cosmo.app.pim");

dojo.require("dojo.html.common");
dojo.require("dojo.gfx.color.hsv");

// -- Create global vars, do not remove despite lack of refs in code
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.util.i18n");
dojo.require('cosmo.convenience');
// --
dojo.require("dojo.lang");
dojo.require("dojo.DeferredList");

dojo.require("cosmo.model");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.ui.timeout");
dojo.require('cosmo.account.create');
dojo.require('cosmo.util.uri');
dojo.require('cosmo.util.hash');
dojo.require('cosmo.util.deferred');
dojo.require('cosmo.service.conduits.common');
dojo.require('cosmo.service.tickler');
dojo.require('cosmo.app.pim.layout');
dojo.require("cosmo.view.names");
dojo.require("cosmo.account.preferences");
// Global variables for X and Y position for mouse
xPos = 0;
yPos = 0;

/**
 * @object The Cal singleton
 */
cosmo.app.pim = dojo.lang.mixin(new function () {

    var self = this;

    // Private variable for the list of any deleted subscriptions
    this._deletedSubscriptions = [];
    this._selectedCollection = null;

    // Available views
    this.views = cosmo.view.names;
    // The Cosmo service -- used to talk to the backend
    this.serv = null;
    // The base layout for the PIM -- cosmo.ui.ContentBox obj
    this.baseLayout = null;
    // The currently selected view -- list or cal
    this.currentView = this.views.LIST;

    // For calculating UI element positions
    this.top = 0;
    this.left = 0;
    this.width = 0;
    this.height = 0;
    // Vertical px pos of top of scrollable area
    // Used to cal positions for draggable elems
    // Changes when resizing all-day event area
    this.viewOffset = 0;
    // Width of the middle column of UI elements
    // All-day resizable area, scrolling area for normal events, detail form
    // Calculated based on client window size
    this.midColWidth = 0;
    // Current date for highlighting in the interface
    this.currDate = null;
    // Collections available to the user
    this.collections = new cosmo.util.hash.Hash();
    // Colors used to display the different collections
    // Blue, green, red, orange, gold, plum, turquoise, fuschia, indigo
    this.collectionHues = [210, 120, 0, 30, 50, 300, 170, 330, 270];
    // If this is true, we are in normal authenticated user mode
    this.authAccess = true;
    // Ticket passed, if any
    this.ticketKey = null;

    // ==========================
    // Init
    // ==========================
    /**
     * Main function
     */
    this.init = function (p) {
        var params = p || {};
        var collectionUrl = params.collectionUrl;
        var startView = params.initialView
            || cosmo.account.preferences.getCookiePreference(cosmo.account.preferences.DEFAULT_VIEW)
            || this.currentView;

        // Now that we've figured out the start view, remember it.
        cosmo.account.preferences.setCookiePreference(cosmo.account.preferences.DEFAULT_VIEW, startView);

        this.authAccess = params.authAccess;
        this.ticketKey = params.ticketKey;
        this.currDate = new Date();

        // Do some setup
        // ===============================
        // Create and init the Cosmo service
        this.serv = cosmo.service.tickler.wrapService(cosmo.service.conduits.getAtomPlusEimConduit());
        // Localized date strings
        this.loadLocaleDateInfo();
        // Tell the calendar view what week we're on
        //cosmo.view.cal.setQuerySpan(this.currDate)
        // Load collections for this user
        var loadCollectionsDeferred = this.loadCollections(params);
        loadCollectionsDeferred.addCallback(dojo.lang.hitch(this, function (){
            this.setDefaultSelectedCollection(params);
            // Base layout
            // ===============================
            this.baseLayout = cosmo.app.pim.layout.initBaseLayout({ domNode: $('baseLayout') });

            // Display the default view
            this.baseLayout.mainApp.centerColumn.navBar.displayView({ viewName: startView });
            // Show errors for deleted subscriptions -- this._deletedSubscriptions
            // is a private var populated in the loadCollections method
            var deletedSubscriptions = this._deletedSubscriptions;
            if (deletedSubscriptions && deletedSubscriptions.length > 0){
                for (var x = 0; x < deletedSubscriptions.length; x++){
                    var errorMessage;
                var deletedSubscription = deletedSubscriptions[x];
                    if (deletedSubscription.getCollectionDeleted()){
                        errorMessage = "Main.Error.SubscribedCollectionDeleted";
                    } else if (deletedSubscription.getTicketDeleted()){
                        errorMessage = "Main.Error.SubscribedTicketDeleted";
                    }
                    cosmo.app.showErr(_(errorMessage,
                                        deletedSubscription.getDisplayName()));
                }
            }
            if (this.authAccess){
                cosmo.ui.timeout.setTimeout(cosmo.app.handleTimeout);
            }
        }));
    };

    // ==========================
    // Localization
    // ==========================
    /**
     * Loads localized datetime info for the UI
     */
    this.loadLocaleDateInfo = function () {
        var keys = null;
        var newArr = null;
        // Use the default set of days as the keys to create an array of
        // localized versions -- e.g., 'Main.Wed' or 'Main.Thu'
        // Replace the default set with the localized set
        // --------------------
        // Weekday abbreviations array
        // ============
        keys = cosmo.datetime.abbrWeekday;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        cosmo.datetime.abbrWeekday = newArr;
        // Full month names array
        // ============
        keys = cosmo.datetime.fullMonth;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        cosmo.datetime.fullMonth = newArr;
        // AM/PM
        // ============
        newArr = [];
        newArr['AM'] = _('App.AM');
        newArr['PM'] = _('App.PM');
        Date.meridian = newArr;
        return true;
    };
    // ==========================
    // Timeout and keepalive
    // ==========================
    this.isTimedOut = function () {
        // Logged in or not, there's no client-side timeout for ticket view
        if (!this.authAccess) {
            return false;
        }
        else {
            var diff = 0;
            diff = new Date().getTime() - this.serv.getServiceAccessTime();
            if (diff > (60000*cosmo.env.getTimeoutMinutes())) {
                return true;
            }
            else {
                return false;
            }
        }
    };
    this.checkTimeout = function () {
        function isServerTimeoutSoon() {
            var ts = new Date();
            var diff = 0;
            ts = ts.getTime();
            diff = ts - self.serv.getServiceAccessTime();
            return !!(diff > (60000*(cosmo.env.getTimeoutMinutes()-2)));
        }

        // If user is client-side timed-out, kill the session cookie and redirect to login page
        if (this.isTimedOut()) {
            this.redirectTimeout();
            return true;
        }
        // Otherwise check for imminent server timeout and refresh local timing cookie
        else if (isServerTimeoutSoon()) {
            // If server-side session is about to time out, refresh it by hitting JSP page
            this.serv.refreshServerSession();
            // Reset local session timing cookie
            this.serv.resetServiceAccessTime();
            return false;
        }
    };
    this.redirectTimeout = function () {
        location = cosmo.env.getRedirectUrl();
    };

    // ==========================
    // Collections
    // ==========================
    this.loadCollections = function (params) {
        var collections = [];
        var calcColors = function (hue) {
            var getRGB = function (h, s, v) {
                var rgb = dojo.gfx.color.hsv2rgb(h, s, v, {
                    inputRange: [360, 100, 100], outputRange: 255 });
                return 'rgb(' + rgb.join() + ')';
            }
            var lozengeColors = {};
            var o = {
                darkSel: [100, 80],
                darkUnsel: [80, 90],
                lightSel: [25, 100],
                lightUnsel: [10, 100],
                proc: [30, 90]
            };
            for (var p in o) {
                lozengeColors[p] = getRGB(hue, o[p][0], o[p][1]);
            }
            return lozengeColors;
        };

        var collectionsLoadedDeferred = null;

        //If we received a ticket key, use the collectionUrl in params to load a collection
        if (params.ticketKey) {
            collectionsLoadedDeferred = this.serv.getCollection(params.collectionUrl);
            collectionsLoadedDeferred.addCallback(function (collection) {
                selectUid = collection.getUid();
                collections.push(collection);
            });

            collectionsLoadedDeferred.addErrback(function (e){
                cosmo.app.showErr(_('Main.Error.LoadItemsFailed'), e);
                return e;
            });
        }

        // Otherwise, get all collections for this user
        else {
            // User's own collections
            var userCollectionsDeferred = this.serv.getCollections();
            userCollectionsDeferred.addCallback(function (userCollections){
                for (var i = 0; i < userCollections.length; i++){
                    collections.push(userCollections[i]);
                }
            });

            // Subscriptions
            var subscriptionsDeferred = this.serv.getSubscriptions();
            subscriptionsDeferred.addCallback(dojo.lang.hitch(this, function (subscriptions){
                var result = this.filterOutDeletedSubscriptions(subscriptions);
                subscriptions = result[0];
                this._deletedSubscriptions = result[1];
                for (var i = 0; i < subscriptions.length; i++){
                    var subscription = subscriptions[i];
                    collections.push(subscription);
                }
            }));
            collectionsLoadedDeferred = new dojo.DeferredList(
                [userCollectionsDeferred, subscriptionsDeferred]
            );
            cosmo.util.deferred.addStdDLCallback(collectionsLoadedDeferred);
        }
        collectionsLoadedDeferred.addCallback(dojo.lang.hitch(this, function (){
            // Sort the collections
            var f = function (a, b) {
                var aName = a.getDisplayName().toLowerCase();
                var bName = b.getDisplayName().toLowerCase();
                var r = 0;
                if (aName == bName) {
                    r = (a.getUid() > b.getUid()) ? 1 : -1;
                }
                else {
                    r = (aName > bName) ? 1 : -1;
                }
                return r;
            };
            collections.sort(f);
            var c = collections;
            var hues = this.collectionHues;

            // Create the new list of collections
            this.collections = new cosmo.util.hash.Hash();
            for (var i = 0; i < c.length; i++) {
                var coll = c[i];
                coll.isDisplayed = false;
                coll.isOverlaid = false;
                coll.doDisplay = false;
                index = i % hues.length;
                var hue = hues[index];
                coll.hue = hue;
                coll.colors = calcColors(hue);
                this.collections.addItem(coll.getUid(), coll);
            }
        }));
        return collectionsLoadedDeferred;

    };
    this.setDefaultSelectedCollection = function (p) {
        var params = p || {};
        if (params.collectionUid){
            this._selectedCollection =
                this.collections.getItem(params.collectionUid);
        }
        else {
            this._selectedCollection = this.collections.getAtPos(0);
        }
        if (this._selectedCollection) {
            this._selectedCollection.doDisplay = true;
        }
    };
    this.setSelectedCollection = function (collection) {
        this._selectedCollection = collection || null;
    };
    this.getSelectedCollection = function () {
        return this._selectedCollection || null;
    };
    this.getSelectedCollectionId = function () {
        return this._selectedCollection ?
            this._selectedCollection.getUid() : '';
    };
    this.getSelectedCollectionWriteable = function () {
        return this._selectedCollection ?
            this._selectedCollection.getWriteable() : false;
    };
    this.filterOutDeletedSubscriptions = function(subscriptions){
        var deletedSubscriptions = [];
        var filteredSubscriptions = dojo.lang.filter(subscriptions,
            function(sub){
               if (sub.getCollectionDeleted() || sub.getTicketDeleted()){
                   var deleteDeferred = self.serv.deleteSubscription(sub);
                   cosmo.util.deferred.addStdErrback(deleteDeferred);
                   deletedSubscriptions.push(sub);
                   return false;
               } else {
                   return true;
               }
        });
        return [filteredSubscriptions, deletedSubscriptions];
    };
    this.reloadCollections = function (o) {
        var opts = o || {};
        var removedCollection = opts.removedCollection || null;
        var removedByThisUser = opts.removedByThisUser || false;
        var selectedCollection = this._selectedCollection;
        var loadCollectionsDeferred;
        // Don't bother saving state and reloading from
        // the server if it's a removal
        if (removedCollection && removedByThisUser) {
            loadCollectionsDeferred = new dojo.Deferred();
            loadCollectionsDeferred.callback();
        }
        else {
            var state = {};
            var saveState = function (id, coll) {
                state[id] = {
                    isDisplayed: coll.isDisplayed,
                    isOverlaid: coll.isOverlaid,
                    doDisplay: coll.doDisplay };
            };
            // Preserve selected/overlaid state
            this.collections.each(saveState);
            // Reload collections from the server
            var loadCollectionsDeferred = this.loadCollections({ ticketKey: this.ticketKey });
            loadCollectionsDeferred.addCallback(dojo.lang.hitch(this, function () {
                // Restore any saved state
                for (var p in state) {
                    var savedProps = state[p];
                    var loadedColl = this.collections.getItem(p);
                    if (loadedColl) {
                        for (q in savedProps) {
                            loadedColl[q] = savedProps[q];
                        }
                    }
                }
            }));
        }

        loadCollectionsDeferred.addCallback(dojo.lang.hitch(this, function (){
            // If we had an originally selected collection
            var selCollId = this.getSelectedCollectionId();
            var newSel = this.collections.getItem(selCollId);
            if (selectedCollection) {
                if (removedCollection) {
                    // If the originally selected collection is gone,
                    // and was not the one removed by the user,
                    // show the user a nice error message
                    if (!removedByThisUser) {
                        cosmo.app.showErr(_("Main.Error.CollectionRemoved",
                                            selectedCollection.getDisplayName()));
                    }
                    if (newSel) {
                        this._selectedCollection = newSel;
                        newSel.doDisplay = true;
                    }
                    // Default new selection is the first collection
                    // in the list
                    else {
                        if (this.collections.length) {
                            newSel = this.collections.getAtPos(0);
                        }
                    }
                }
                // If the originally selected collection is in
                // the new set, point to the one in the new set
                else {
                    if (newSel) {
                        this._selectedCollection = newSel;
                        newSel.doDisplay = true;
                    }
                }

            }
            // User had no collections loaded, no selected collection
            else {
                // The new selection is the first (and only) one
                if (this.collections.length) {
                    newSel = this.collections.getAtPos(0);
                }
            }
            // If there's a selected collection, display its data
            if (newSel) {
                this._selectedCollection = newSel;
                this._selectedCollection.doDisplay = true;
            }
            else {
                this._selectedCollection = null;
            }
            cosmo.view.displayViewFromCollections(this._selectedCollection);
        }));
        return loadCollectionsDeferred;
    };

    // ==========================
    // Cleanup
    // ==========================
    this.cleanup = function () {
        if (this.uiMask) {
            this.uiMask.cleanup();
        }
        if (this.allDayArea) {
            this.allDayArea.cleanup();
        }
        this.allDayArea = null;
    };

}, cosmo.app.pim);

Cal = cosmo.app.pim;

