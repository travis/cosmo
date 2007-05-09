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

// -- FIXME: Weirdness that should be fixed
dojo.require("cosmo.service.json_service_impl");
dojo.require("cosmo.legacy.cal_event");
// --

// -- Widget includes, may not always find proper namespaced refs
// -- ie, cosmo:CollectionSelector
dojo.require("cosmo.ui.widget.CollectionSelector");
// --

dojo.require("cosmo.model");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.ui.cal_form");
dojo.require("cosmo.ui.minical");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.ContentBox");
dojo.require('cosmo.view.cal');
dojo.require('cosmo.view.cal.lozenge');
dojo.require('cosmo.view.cal.canvas');
dojo.require('cosmo.account.create');
dojo.require('cosmo.service.conduits.common');
// Props for confirmation dialogs
dojo.require('cosmo.view.cal.dialog');


// Global variables for X and Y position for mouse
xPos = 0;
yPos = 0;

/**
 * @object The Cal singleton
 */
cosmo.app.pim = new function () {

    var self = this;
    // Private variable for the list of any deleted subscriptions
    var deletedSubscriptions = [];

    // The Cosmo service -- used to talk to the backend
    this.serv = null;
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
    // The form on the page -- a CalForm obj
    this.calForm = null;
    // Current date for highlighting in the interface
    this.currDate = null;
    // The path to the currently selected collection
    this.currentCollection = null;
    //The list of calendars available to the current user
    this.currentCollections = [];
    // Anonymous ticket view, no client-side timeout
    this.authAccess = true;
    // Create the 'Welcome to Cosmo' event?
    this.createWelcomeItem = false;

    //A handle to the collection selector widget
    this._collectionSelector = null;

    // ==========================
    // Init
    // ==========================
    /**
     * Main function
     */
    this.init = function (p) {

        var params = p || {};
        var collectionUid = params.collectionUid;
        var ticketKey = params.ticketKey;

        this.authAccess = params.authAccess;
        this.currDate = new Date();

        // Create and init the Cosmo service
        // ===============================
        this.serv = cosmo.service.conduits.getAtomPlusEimConduit();
        this.serv.resetServiceAccessTime(); // Client-side keepalive

        // Base layout
        // ===============================
        // Only in logged-in view
        if (params.authAccess) {
            cosmo.topics.publish(cosmo.topics.PreferencesUpdatedMessage,
                [cosmo.account.preferences.getPreferences()]);
        }
        
        // Place all the UI DOM elements based on window size
        this.placeUI();
        this.setImagesForSkin();

        // Populate base layout with UI elements
        // ===============================
        // Load and display date info, render cal canvas
        if (this.loadLocaleDateInfo() && cosmo.view.cal.setQuerySpan(this.currDate)) {
            cosmo.view.cal.canvas.render(cosmo.view.cal.viewStart,
                cosmo.view.cal.viewEnd, this.currDate);
        }
        // Calendar event detail form
        this.calForm = new CalForm();
        this.calForm.init();
        // Initialize the color set for the cal lozenges
        cosmo.view.cal.canvas.calcColors();

        // Load data
        // ===============================
        // Load collections for this user
        this.loadCollections(ticketKey, collectionUid);
        // Load and display events

        // Load / render
        // ===============================
        // FIXME: This stuff should eventually participate in
        // the normal topic-based prepare-load-render lifecycle
        // --------------
        cosmo.view.cal.loadEvents({ collection: self.currentCollection,
            startDate: cosmo.view.cal.viewStart, endDate: cosmo.view.cal.viewEnd });

        // Render UI elements
        // ===============================
        // Display selector or single cal name
        this._collectionSelectContainer = $('calSelectNav');
        this._collectionSelector = dojo.widget.createWidget(
            'cosmo:CollectionSelector', {
                'collections': this.currentCollections,
                'currentCollection': this.currentCollection,
                'ticketKey': ticketKey
            }, this._collectionSelectContainer, 'last');

        // Minical and jump-to date
        var mcDiv = $('miniCalDiv');
        var jpDiv = $('jumpToDateDiv');
        // Place jump-to date based on mini-cal pos
        if (cosmo.ui.minical.MiniCal.init(cosmo.app.pim, mcDiv)) {
           this.calForm.addJumpToDate(jpDiv);
        }
        // Top menubar setup and positioning
        if (this.setUpMenubar()) {
            this.positionMenubarElements.apply(this);
        }
        // Add event listeners for form-element behaviors
        this.calForm.setEventListeners();

        // Final stuff / cleanup
        // ===============================
        // Scroll to 8am for normal event
        // Have to do this dead last because appending to the div
        // seems to reset the scrollTop in Safari
        $('timedScrollingMainDiv').scrollTop = parseInt(HOUR_UNIT_HEIGHT*8);

        // Hide the UI mask
        this.uiMask.hide();

        // Show errors for deleted subscriptions -- deletedSubscriptions
        // is a private var populated in the loadCollections method
        if (deletedSubscriptions && deletedSubscriptions.length > 0){
            for (var x = 0; x < deletedSubscriptions.length; x++){
                cosmo.app.showErr(_("Main.Error.SubscribedCollectionDeleted",deletedSubscriptions[x].displayName));
            }
        }

    };

    // ==========================
    // GUI element display
    // ==========================
    /**
     * Performs the absolute placement of the UI elements based
     * on the client window size
     */
    this.placeUI = function () {
        var _c = cosmo.ui.ContentBox;
        var uiMain = new _c('calDiv');
        //var uiProcessing = new _c('processingDiv');
        var menuBar = new _c('menuBarDiv');
        var leftSidebar = new _c('leftSidebarDiv');
        var rightSidebar = new _c('rightSidebarDiv');
        var topNav = new _c('calTopNavDiv');
        var dayList = new _c('dayListDiv');
        var timedMain = new _c('timedScrollingMainDiv');
        var timedContent = new _c('timedContentDiv');
        var timedHourList = new _c('timedHourListDiv');
        var eventInfo = new _c('eventInfoDiv');
        var allDayMain = new _c('allDayResizeMainDiv');
        var allDayResize = new _c('allDayResizeHandleDiv');
        var allDayContent = new _c('allDayContentDiv');
        var allDaySpacer = new _c('allDayHourSpacerDiv');
        var vOffset = 0;
        var calcHeight = 0;
        var viewport = dojo.html.getViewport();
        var winwidth = viewport.width;
        var winheight = viewport.height;

        this.uiMask = new _c('maskDiv');

        // Pare width and height down to avoid stupid scrollbars showing up
        winwidth-=3;
        winheight-=3;

       // Calculate position values for main UI display, set properties
        this.height = parseInt(winheight*DISPLAY_WIDTH_PERCENT) - TOP_MENU_HEIGHT;
        this.width = parseInt(winwidth*DISPLAY_HEIGHT_PERCENT);
        this.top = TOP_MENU_HEIGHT;
        this.left = 0;

        // Width for middle column area
        this.midColWidth = (this.width - LEFT_SIDEBAR_WIDTH - RIGHT_SIDEBAR_WIDTH);

        // Position UI elements
        // =========================
        // **** Save these coords for middle-col mask ****
        $('appLoadingMessage').style.width = PROCESSING_ANIM_WIDTH + 'px';
        $('appLoadingMessage').style.top = parseInt((winheight-PROCESSING_ANIM_HEIGHT)/2) + 'px';
        $('appLoadingMessage').style.left = parseInt((winwidth-PROCESSING_ANIM_WIDTH)/2) + 'px';
        //this.uiMask.setPosition(this.top, LEFT_SIDEBAR_WIDTH);
        //this.uiMask.setSize(this.midColWidth-2, this.height);
        // Position the processing animation
        //uiProcessing.setPosition(parseInt((winheight-PROCESSING_ANIM_HEIGHT)/2),
        //    parseInt((this.midColWidth-PROCESSING_ANIM_WIDTH)/2));

        // Menubar
        menuBar.setPosition(0, 0);
        menuBar.setSize(this.width, TOP_MENU_HEIGHT-1);

        // Main UI
        uiMain.setPosition(this.top, this.left);
        uiMain.setSize(this.width, this.height);

        // Left sidebar
        leftSidebar.setPosition(0, 0);
        leftSidebar.setSize(LEFT_SIDEBAR_WIDTH, this.height);
        // Right sidebar
        rightSidebar.setPosition(0, LEFT_SIDEBAR_WIDTH + this.midColWidth);
        rightSidebar.setSize(RIGHT_SIDEBAR_WIDTH, this.height);

        // Center column
        // Top nav
        vOffset = 0;
        // 1px for border per retarded CSS spec
        topNav.setSize(this.midColWidth-2, CAL_TOP_NAV_HEIGHT-1);
        topNav.setPosition(vOffset, LEFT_SIDEBAR_WIDTH);
        // Day listing
        vOffset += CAL_TOP_NAV_HEIGHT;
        dayList.setSize(this.midColWidth-2, DAY_LIST_DIV_HEIGHT);
        dayList.setPosition(vOffset, LEFT_SIDEBAR_WIDTH);
        // No-time event area
        vOffset += DAY_LIST_DIV_HEIGHT;
        allDayMain.setSize((this.midColWidth-2), ALL_DAY_RESIZE_AREA_HEIGHT);
        allDayMain.setPosition(vOffset, LEFT_SIDEBAR_WIDTH);
        // Resize handle
        vOffset += ALL_DAY_RESIZE_AREA_HEIGHT;
        allDayResize.setSize(this.midColWidth-1, ALL_DAY_RESIZE_HANDLE_HEIGHT);
        allDayResize.setPosition(vOffset, LEFT_SIDEBAR_WIDTH);

        allDayContent.setSize((this.midColWidth - SCROLLBAR_SPACER_WIDTH - HOUR_LISTING_WIDTH), '100%');
        allDayContent.setPosition(0, (HOUR_LISTING_WIDTH + 1));

        allDaySpacer.setSize((HOUR_LISTING_WIDTH - 1), '100%');
        allDaySpacer.setPosition(0, 0);

        // Scrollable view area
        vOffset += ALL_DAY_RESIZE_HANDLE_HEIGHT;
        calcHeight = this.height-vOffset;
        timedMain.setSize(this.midColWidth-2, calcHeight); // Variable height area
        timedMain.setPosition(vOffset, LEFT_SIDEBAR_WIDTH);
        timedContent.setSize((this.midColWidth - HOUR_LISTING_WIDTH), VIEW_DIV_HEIGHT);
        timedContent.setPosition(0, (HOUR_LISTING_WIDTH + 1));
        timedHourList.setSize(HOUR_LISTING_WIDTH - 1, VIEW_DIV_HEIGHT);
        timedHourList.setPosition(0, 0);

        // Set vertical offset for scrollable area
        this.viewOffset = vOffset;

        // Event detail form
        vOffset += calcHeight;
        // Variable height area
        eventInfo.setPosition(0, 0);

        // Set cal day column width
        cosmo.view.cal.canvas.dayUnitWidth = parseInt(
            (this.midColWidth - HOUR_LISTING_WIDTH - SCROLLBAR_SPACER_WIDTH)/7 );

        // Kill and DOM-elem references to avoid IE memleak issues --
        // leave UI Mask ref
        uiMain.cleanup(); uiMain = null;
        //uiProcessing.cleanup(); uiProcessing = null;
        leftSidebar.cleanup(); leftSidebar = null;
        rightSidebar.cleanup(); rightSidebar = null;
        topNav.cleanup(); topNav = null;
        dayList.cleanup(); dayList = null;
        timedMain.cleanup(); timedMain = null;
        eventInfo.cleanup(); eventInfo = null;
        allDayMain.cleanup(); allDayMain = null;
        allDayResize.cleanup(); allDayResize = null;

    };
    this.setUpMenubar = function (ticketKey) {
        // Logged-in view -- nothing to do
        if (this.authAccess) {
            return true;
        }
        // Add the collection subscription selector in ticket view
        else {
            var menuBarDiv = $('menuBarDiv');
            var s = _createElem('div');
            s.id = 'subscribeSelector';
            var clientOpts = cosmo.ui.widget.CollectionDetailsDialog.getClientOptions();
            clientOpts.unshift({ text: 'Subscribe with ...', value: '' });
            var selOpts = { name: 'subscriptionSelect', id: 'subscriptionSelect',
               options: clientOpts, className: 'selectElem' };
            var subscrSel = cosmo.util.html.createSelect(selOpts);
            var f = function (e) {
                // Show the subcription dialog if the empty "Subscribe with ..."
                // option is not the one selected
                var sel = e.target;
                if (sel.selectedIndex != 0) {
                //XINT
                cosmo.app.showDialog(
                    cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                        self.currentCollection.collection,
                        self.currentCollection.displayName,
                        self.currentCollection.conduit,
                        self.currentCollection.transportInfo,
                        sel.options[sel.selectedIndex].value));
                }
            };
            dojo.event.connect(subscrSel, 'onchange', f);
            s.appendChild(subscrSel);
            menuBarDiv.appendChild(s);

            var signupDiv = _createElem('div');
            signupDiv.id = 'signupGraphic';

            var w = 0;
            var p = 0;
            signupDiv.style.position = 'absolute';
            var i = cosmo.util.html.createRollOverMouseDownImage(
                    cosmo.env.getImagesUrl() + "signup.png");
            i.style.cursor = 'pointer';
            dojo.event.connect(i, 'onclick', cosmo.account.create.showForm);
            signupDiv.appendChild(i);
            menuBarDiv.appendChild(signupDiv);
        }
        return true;
    };
    this.positionMenubarElements = function () {
        var menuNav = $('menuNavItems')
        // Ticket view only
        if (!this.authAccess) {
            // Signup graphic
            // position right side of center col, and bottom align
            var signupDiv = $('signupGraphic');
            var w = signupDiv.offsetWidth + 24;
            var p = self.midColWidth  + LEFT_SIDEBAR_WIDTH - w;
            signupDiv.style.left = p + 'px';
            signupDiv.style.top = (TOP_MENU_HEIGHT - signupDiv.offsetHeight - 5) + 'px';
            // Subscription select box
            var subscribeSelector = $('subscribeSelector');
            subscribeSelector.style.position = 'absolute';
            subscribeSelector.style.left = (LEFT_SIDEBAR_WIDTH + this.midColWidth) + 'px';
            subscribeSelector.style.top = (TOP_MENU_HEIGHT - subscribeSelector.offsetHeight - 5) + 'px';

        }
        // Bottom-align menu text
        menuNav.style.top = (TOP_MENU_HEIGHT - menuNav.offsetHeight - 5) + 'px';
        $('menuBarDiv').style.visibility = 'visible';
    };
    /**
     * Set skin-specific images
     */
    this.setImagesForSkin =  function () {
        var logoDiv = $('smallLogoDiv');

        // Resize handle for all-day area
        var i = _createElem('img');
        i.src = cosmo.env.getImagesUrl() + 'resize_handle_image.gif';
        $('allDayResizeHandleDiv').appendChild(i);

        // Cosmo logo
        logoDiv.style.background =
            'url(' + cosmo.env.getImagesUrl() + LOGO_GRAPHIC_SM + ')';
        // Wheeeee, IE6 resets background tiling when you set an image background
        if (document.all && (navigator.appVersion.indexOf('MSIE 7') == -1)) {
            logoDiv.style.backgroundRepeat = 'no-repeat';
        }

    };
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
    this.loadCollections = function (ticketKey, collectionUid) {
        // Load/create calendar to view
        // --------------
        // If we received a ticket, just grab the specified collection
        if (ticketKey) {
            try {
               var collection = this.serv.getCollection(collectionUid, {ticket:ticketKey});
            }
            catch(e) {
                cosmo.app.showErr(_('Main.Error.LoadEventsFailed'), e);
                return false;
            }
            this.currentCollections.push(collection);
        }

        // Otherwise, get all collections for this user
        else {
            var userCollections = this.serv.getCollections();
            for (var i = 0; i < userCollections.length; i++){
                var collection = userCollections[i];
                this.currentCollections.push (collection);
            }

            // No collections for this user
            if (this.currentCollections.length == 0){
                 //XINT
                 throw new Error("No collections!")
            }
            var subscriptions = this.serv.getSubscriptions();
            //XINT make sure this still works!
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
                r = (a.collection.getUid() > b.collection.getUid()) ? 1 : -1;
            }
            else {
                r = (aName > bName) ? 1 : -1;
            }
            return r;
        };
        this.currentCollections.sort(f);

        // If we received a collectionUid, select that collection
        if (collectionUid){
            for (var i = 0; i < this.currentCollections.length; i++){
                if (this.currentCollections[i].collection.getUid() == collectionUid){
                    this.currentCollection = this.currentCollections[i];
                    break;
                }
            }
        }
        // Otherwise, use the first collection
        else {
            this.currentCollection = this.currentCollections[0];
        }
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

    //XINT 
    this.filterOutDeletedSubscriptions = function(subscriptions){
        var deletedSubscriptions = [];
        var filteredSubscriptions = dojo.lang.filter(subscriptions,
            function(sub){
               if (!sub.calendar){
                   self.serv.deleteSubscription(sub.uid, sub.ticket.ticketKey);
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
        this.calForm = null;
        this.allDayArea = null;
    };
}

Cal = cosmo.app.pim;

