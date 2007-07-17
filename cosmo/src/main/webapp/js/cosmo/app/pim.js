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

// -- Create global vars, do not remove despite lack of refs in code
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.util.i18n");
dojo.require('cosmo.convenience');
// --

dojo.require("cosmo.model");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.ui.timeout");
dojo.require('cosmo.account.create');
dojo.require('cosmo.util.uri');
dojo.require('cosmo.service.conduits.common');
dojo.require('cosmo.service.tickler');
dojo.require('cosmo.app.pim.layout');
dojo.require("cosmo.view.names");

// Global variables for X and Y position for mouse
xPos = 0;
yPos = 0;

/**
 * @object The Cal singleton
 */
cosmo.app.pim = dojo.lang.mixin(new function () {

    var self = this;
    // Private variable for the list of any deleted subscriptions
    var deletedSubscriptions = [];
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
    // The path to the currently selected collection
    this.currentCollection = null;
    //The list of calendars available to the current user
    this.currentCollections = [];
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
        var startView = params.initialView || this.currentView;

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
        this.loadCollections(params);
        if (params.collectionUid){
            this._selectCollectionByUid(params.collectionUid);
        } else {
            this.currentCollection = this.currentCollections[0];
        }
        
        // Base layout
        // ===============================
        // FIXME: Safari -- Need to valign-middle the whole-screen mask
        this.baseLayout = cosmo.app.pim.layout.initBaseLayout({ domNode: $('baseLayout') });
        // Display the default view
        this.baseLayout.mainApp.centerColumn.navBar.displayView(startView);

        // Show errors for deleted subscriptions -- deletedSubscriptions
        // is a private var populated in the loadCollections method
        if (deletedSubscriptions && deletedSubscriptions.length > 0){
            for (var x = 0; x < deletedSubscriptions.length; x++){
                cosmo.app.showErr(_("Main.Error.SubscribedCollectionDeleted",
                    deletedSubscriptions[x].getDisplayName()));
            }
        }
        
        cosmo.ui.timeout.setTimeout(cosmo.app.handleTimeout);
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
        // Load/create calendar to view
        // --------------

        this.currentCollections = [];
        //If we received a ticket key, use the collectionUrl in params to load a collection
        if (params.ticketKey) {
            try {
               var collection = this.serv.getCollection(params.collectionUrl, {sync:true}).results[0];
               selectUid = collection.getUid();
            }
            catch(e) {
                cosmo.app.showErr(_('Main.Error.LoadItemsFailed'), e);
                return false;
            }
            this.currentCollections.push(collection);
        }

        // Otherwise, get all collections for this user
        else {
            var userCollections = this.serv.getCollections({sync: true}).results[0];
            for (var i = 0; i < userCollections.length; i++){
                var collection = userCollections[i];
                this.currentCollections.push(collection);
            }

            // No collections for this user
            if (this.currentCollections.length == 0){
                 //XINT
                 throw new Error("No collections!")
            }
            var subscriptions = this.serv.getSubscriptions({sync:true}).results[0];

            var result = this.filterOutDeletedSubscriptions(subscriptions);
            subscriptions = result[0];
            deletedSubscriptions = result[1];

            for (var i = 0; i < subscriptions.length; i++){
                var subscription = subscriptions[i];
                this.currentCollections.push(subscription);
            }
        }

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
        this.currentCollections.sort(f);

    };
    
    this._selectCollectionByUid = function(selectUid){
        // If we received a collectionUrl, select that collection
        for (var i = 0; i < this.currentCollections.length; i++){
            if (this.currentCollections[i].getUid() == selectUid){
                this.currentCollection = this.currentCollections[i];
                return true;
            }
        }
        
        return false;
    };
    
    this.handleCollectionUpdated = function(/*cosmo.topics.CollectionUpdatedMessage*/ message){
        var updatedCollection = message.collection;

        for (var x = 0; x < this.currentCollections.length; x++){
            var collection = this.currentCollections[x];
            if (collection instanceof cosmo.model.Collection
                   && collection.getUid() == updatedCollection.getUid()){
                this.currentCollections[x] = updatedCollection;
            }
        }
    };

    this.handleSubscriptionUpdated = function(/*cosmo.topics.SubscriptionUpdatedMessage*/ message){
        var updatedSubscription = message.subscription;
        for (var x = 0; x < this.currentCollections.length; x++){
            var subcription = this.currentCollections[x];
            if (subcription instanceof cosmo.model.Subscription
                   && subcription.getUid() == updatedSubscription.getUid()){
                this.currentCollections[x] = updatedSubscription;
            }
        }
    };

    this.filterOutDeletedSubscriptions = function(subscriptions){
        var deletedSubscriptions = [];
        var filteredSubscriptions = dojo.lang.filter(subscriptions,
            function(sub){
               if (sub.getCollectionDeleted()){
                   self.serv.deleteItem(sub);
                   deletedSubscriptions.push(sub);
                   return false;
               } else {
                   return true;
               }
        });
        return [filteredSubscriptions, deletedSubscriptions];
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
    
    this.reloadCollections = function(){
        //first get a handle on the currenct collection so we don't lose it. 
        var currentCollection = this.currentCollection;
        dojo.debug("displayName: " + this.currentCollection.getDisplayName());
        this.loadCollections({ticketKey: this.ticketKey});
        var collectionSelector = cosmo.app.pim.baseLayout.mainApp.leftSidebar.collectionSelector.widget;
        collectionSelector.updateCollectionSelectorOptions(this.currentCollections);

        if (this._selectCollectionByUid(currentCollection.getUid())){
            dojo.debug("sucess");
            collectionSelector.setSelectorByDisplayName(currentCollection.getDisplayName());
        } else {
            cosmo.app.showErr(_("Main.Error.CollectionRemoved", currentCollection.getDisplayName()));
            this.currentCollection = this.currentCollections[0];
            collectionSelector.setSelectorByDisplayName(this.currentCollection.getDisplayName());
        }
        dojo.event.topic.publish('/calEvent', { action: 'loadCollection', opts: { loadType: 'changeCollection', collection: this.currentCollection }, data: {}})
        collectionSelector.currentCollection = this.currentCollection;
        
    }
}, cosmo.app.pim);

Cal = cosmo.app.pim;

