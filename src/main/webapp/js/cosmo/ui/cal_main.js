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

dojo.provide("cosmo.ui.cal_main");

dojo.require("cosmo.util.date");
dojo.require("cosmo.conduits");
dojo.require("cosmo.ui.conf");
dojo.require("cosmo.ui.minical");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.model");
dojo.require("cosmo.ui.cal_form");
dojo.require("cosmo.ui.contentcontainer");
dojo.require("cosmo.facade.pref");
dojo.require("cosmo.service.json_service_impl");
dojo.require("cosmo.legacy.cal_event");
dojo.require('cosmo.view.cal');
dojo.require('cosmo.view.cal.Lozenge');
dojo.require('cosmo.view.cal.canvas');
dojo.require('cosmo.account.create');
dojo.require('cosmo.convenience');
dojo.require('cosmo.ui.widget.CollectionSelector');
dojo.require('cosmo.ui.widget.AuthBox');

// Global variables for X and Y position for mouse
xPos = 0;
yPos = 0;

/**
 * @object The Cal singleton
 */
cosmo.ui.cal_main.Cal = new function () {
    
    var self = this;
    
    // Constants
    this.ID_SEPARATOR = '__';
    
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
    // What view we're using -- currently only week view exists
    this.viewtype = 'week';
    // Start date for events to display
    this.viewStart = null;
    // End date for events to display
    this.viewEnd = null;
    // Current date for highlighting in the interface
    this.currDate = null;
    
    // The path to the currently selected collection
    this.currentCollection = null;
    
    //The list of calendars available to the current user
    this.currentCollections = [];

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
    this.init = function (collectionUid, ticketKey) {
        
        // Props for confirmation dialogs
        // --------------
        dojo.require('cosmo.view.cal.dialog');
        
        var viewDiv = null;
        var allDayDiv = null;
        
        this.currDate = new Date();

        // Create and init the Cosmo service
        // --------------
        this.serv = new ScoobyService();
        // Client-side keepalive
        this.serv.resetServiceAccessTime(); 
        this.serv.init();

        // Load user prefs
        // --------------
        Pref.init();

        // Place all the UI DOM elements based on window size
        // --------------

        viewDiv = document.getElementById('timedScrollingMainDiv');
        allDayDiv = document.getElementById('allDayContentDiv');
        this.placeUI();
        this.setImagesForSkin();
        this.setUpNavButtons();


        // Load and display date info, render cal canvas
        // --------------
        if (this.loadLocaleDateInfo() && this.getQuerySpan(this.currDate)) {
            cosmo.view.cal.canvas.render(this.viewStart, this.viewEnd, 
                this.currDate);
        }

        // Calendar event detail form 
        this.calForm = new CalForm();
        this.calForm.init();
        
        // Load/create calendar to view
        // --------------
        // If we received a ticket, just grab the specified collection
        if (ticketKey){
            var collection = this.serv.getCalendar(collectionUid, ticketKey);
            var ticket = this.serv.getTicket(ticketKey, collectionUid);
            this.currentCollections.push(
                {
                    collection: collection,
                    transportInfo: ticket,
                    conduit: cosmo.conduits.AnonymousTicketedConduit,
                    displayName: collection.name,
                    privileges: ticket.privileges
                }
                );
        }
        
        // Otherwise, get all calendars for this user
        else {
            var userCollections = this.serv.getCalendars();
            for (var i = 0; i < userCollections.length; i++){
                var collection = userCollections[i];
                this.currentCollections.push(
                    {
                    collection: collection,
                    transportInfo: null,
                    conduit: cosmo.conduits.OwnedCollectionConduit,
                    displayName: collection.name,
                    privileges: {'read':'read', 'write':'write'}
                    }
                );
            }
            
            var subscriptions = this.serv.getSubscriptions();
            for (var i = 0; i < subscriptions.length; i++){
                var subscription = subscriptions[i];
                this.currentCollections.push(
                    {
                    collection: subscription.calendar,
                    transportInfo: subscription,
                    conduit: cosmo.conduits.SubscriptionConduit,
                    displayName: subscription.displayName,
                    privileges: subscription.ticket.privileges
                    }
                );
            }
            
            // No cals for this user
            if (this.currentCollections.length == 0){
                // Create initial cal
                try {
                    var uid = this.serv.createCalendar('Cosmo');
                }
                catch(e) {
                    cosmo.app.showErr(_('Main.Error.InitCalCreateFailed'), e);
                    return false;
                }
                var collection = this.serv.getCalendar(uid);
                this.currentCollections.push(
                    {
                    collection: collection,
                    transportInfo: null,
                    conduit: cosmo.conduits.OwnedCollectionConduit,
                    displayName: collection.name,
                    privileges: {'read':'read', 'write':'write'}
                    }
                );
                
                // Add 'Welcome to Cosmo' Event
                this.createWelcomeItem = true;
            }
        }
        
        // Sort the collections, for Pete's sake
        var f = function (a, b) {
            return (a.displayName.toLowerCase() >= b.displayName.toLowerCase()) ? 1 : -1;
        };
        this.currentCollections.sort(f); 

        // If we received a collectionUid, select that collection
        if (collectionUid){
            for (var i = 0; i < this.currentCollections.length; i++){
                if (this.currentCollections[i].collection.uid == collectionUid){
                    this.currentCollection = this.currentCollections[i];
                    break;
                }
            }
        }
        // Otherwise, use the first collection 
        else {
            this.currentCollection = this.currentCollections[0];
        }
        
        // Display selector or single cal name
        this._collectionSelectContainer = document.getElementById('calSelectNav');
        this._collectionSelector = dojo.widget.createWidget(
            'cosmo:CollectionSelector', { 
                'collections': this.currentCollections, 
                'currentCollection': this.currentCollection,
                'ticketKey': ticketKey,
                'selectFunction': function(){var f = Cal.goSelCal; Cal.showMaskDelayNav(f); }
            }, this._collectionSelectContainer, 'last');

        // Load minical and jump-to date
        var mcDiv = document.getElementById('miniCalDiv');
        var jpDiv = document.getElementById('jumpToDateDiv');
        // Place jump-to date based on mini-cal pos
        if (MiniCal.init(Cal, mcDiv)) {
           this.calForm.addJumpToDate(jpDiv);
        }
        
        // Add the collection subscription selector in ticket view
        if (ticketKey) {
            var s = $('subscribeSelector');
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
                cosmo.app.showDialog(
                    cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                        Cal.currentCollection.collection,
                        Cal.currentCollection.displayName,
                        Cal.currentCollection.conduit,
                        Cal.currentCollection.transportInfo,
                        sel.options[sel.selectedIndex].value));
                }
            };

            dojo.event.connect(subscrSel, 'onchange', f);
            s.style.position = 'absolute';
            s.style.left = (LEFT_SIDEBAR_WIDTH + this.midColWidth) + 'px'; 
            s.appendChild(subscrSel);
        }
        
        // Load and display events
        // --------------
        cosmo.view.cal.loadEvents(self.viewStart, self.viewEnd);

        this.uiMask.hide();
        
        // Scroll to 8am for normal event
        // Have to do this dead last because appending to the div
        // seems to reset the scrollTop in Safari
        viewDiv.scrollTop = parseInt(HOUR_UNIT_HEIGHT*8);
        
        // BANDAID for IE -- dummy element to force 100% height to render
        if (document.all) {
            var dummyElem = document.createElement('div');
            allDayDiv.appendChild(dummyElem);
        }
        
        // Add event listeners for form-element behaviors
        this.calForm.setEventListeners();
    };

    // ==========================
    // GUI element display
    // ==========================

    /**
     * Performs the absolute placement of the UI elements based
     * on the client window size
     */
    this.placeUI = function () {
        var uiMain = new ContentContainer('calDiv');
        var uiMask = new ContentContainer('maskDiv');
        var uiProcessing = new ContentContainer('processingDiv');
        var menuBar = new ContentContainer('menuBarDiv');
        var leftSidebar = new ContentContainer('leftSidebarDiv');
        var rightSidebar = new ContentContainer('rightSidebarDiv');
        var topNav = new ContentContainer('calTopNavDiv');
        var dayList = new ContentContainer('dayListDiv');
        var timedMain = new ContentContainer('timedScrollingMainDiv');
        var timedContent = new ContentContainer('timedContentDiv');
        var timedHourList = new ContentContainer('timedHourListDiv');
        var eventInfo = new ContentContainer('eventInfoDiv');
        var allDayMain = new ContentContainer('allDayResizeMainDiv');
        var allDayResize = new ContentContainer('allDayResizeHandleDiv');
        var allDayContent = new ContentContainer('allDayContentDiv');
        var allDaySpacer = new ContentContainer('allDayHourSpacerDiv');
        var vOffset = 0;
        var calcHeight = 0;

        var winwidth = this.getWinWidth();
        var winheight = this.getWinHeight();

        // Pare width and height down to avoid stupid scrollbars showing up
        winwidth-=3;
        winheight-=3;

        // Set reference to UI mask to use later
        this.uiMask = uiMask;

       // Calculate position values for main UI display, set properties
        this.height = parseInt(winheight*DISPLAY_WIDTH_PERCENT) - TOP_MENU_HEIGHT;
        this.width = parseInt(winwidth*DISPLAY_HEIGHT_PERCENT);
        this.top = TOP_MENU_HEIGHT;
        this.left = 0;

        // Width for middle column area
        this.midColWidth = (this.width - LEFT_SIDEBAR_WIDTH - RIGHT_SIDEBAR_WIDTH);

        // Position UI elements
        // =========================
        // UI Mask -- same position, in front -- hide after events load
        this.uiMask.setPosition(this.top, LEFT_SIDEBAR_WIDTH);
        this.uiMask.setSize(this.midColWidth-2, this.height);
        // Position the processing animation
        uiProcessing.setPosition(parseInt((winheight-PROCESSING_ANIM_HEIGHT)/2),
            parseInt((this.midColWidth-PROCESSING_ANIM_WIDTH)/2));
        
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
        eventInfo.setSize(RIGHT_SIDEBAR_WIDTH-12, EVENT_DETAIL_FORM_HEIGHT);
        eventInfo.setPosition(4, 8);

        // Set cal day column width
        cosmo.view.cal.canvas.dayUnitWidth = parseInt(
            (this.midColWidth - HOUR_LISTING_WIDTH - SCROLLBAR_SPACER_WIDTH)/7 );

        // Kill and DOM-elem references to avoid IE memleak issues --
        // leave UI Mask ref
        uiMain.cleanup(); uiMain = null;
        uiProcessing.cleanup(); uiProcessing = null;
        leftSidebar.cleanup(); leftSidebar = null;
        rightSidebar.cleanup(); rightSidebar = null;
        topNav.cleanup(); topNav = null;
        dayList.cleanup(); dayList = null;
        timedMain.cleanup(); timedMain = null;
        eventInfo.cleanup(); eventInfo = null;
        allDayMain.cleanup(); allDayMain = null;
        allDayResize.cleanup(); allDayResize = null;
    };
    /**
     * Set skin-specific images
     */
    this.setImagesForSkin =  function () {
        var logoDiv = $('smallLogoDiv');
        var signupDiv = $('signupGraphic');
        
        // Resize handle for all-day area
        var i = _createElem('img');
        i.src = cosmo.env.getImagesUrl() + 'resize_handle_image.gif';
        document.getElementById('allDayResizeHandleDiv').appendChild(i);
       
        // Cosmo logo
        logoDiv.style.background =
            'url(' + cosmo.env.getImagesUrl() + LOGO_GRAPHIC_SM + ')';
        // Wheeeee, IE6 resets background tiling when you set an image background
        if (document.all && (navigator.appVersion.indexOf('MSIE 7') == -1)) {
            logoDiv.style.backgroundRepeat = 'no-repeat';
        }
        
        // Signup graphic div is only on the page in ticket mode
        if (signupDiv) {
            var w = 0;
            var p = 0;
            signupDiv.style.position = 'absolute';
            signupDiv.style.visibility = 'hidden';
            var i = cosmo.util.html.createRollOverMouseDownImage(
                    cosmo.env.getImagesUrl() + "signup.png");
            i.style.cursor = 'pointer';
            dojo.event.connect(i, 'onclick', cosmo.account.create.showForm);
            signupDiv.appendChild(i);
            w = signupDiv.offsetWidth + 24;
            p = Cal.midColWidth  + LEFT_SIDEBAR_WIDTH - w;
            signupDiv.style.left = p + 'px';
            signupDiv.style.visibility = 'visible';
        }
    };
    /**
     * Loads localized Date information into the arrays in date.js
     * It uses the default English values in those arrays
     * as keys to look up the localized info
     * from the i18n.js (generated by i18n.jsp) page
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
        keys = Date.abbrWeekday;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        Date.abbrWeekday = newArr;
        // Full month names array
        // ============
        keys = Date.fullMonth;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(_('App.' + keys[i]));
        }
        Date.fullMonth = newArr;
        // AM/PM
        // ============
        newArr = [];
        newArr['AM'] = _('App.AM');
        newArr['PM'] = _('App.PM');
        Date.meridian = newArr;
        return true;
    };
    this.getWinHeight = function () {
        // IE
        // *** Note: IE requires the body style to include'height:100%;'
        // *** to get the actual window height
        if (document.all) {
            return document.body.clientHeight;
        }
        // Moz/compat
        else {
            return window.innerHeight;
        }
    };
    this.getWinWidth =  function () {
        // IE
        if (document.all) {
            return document.body.clientWidth;
        }
        // Moz/compat
        else {
            return window.innerWidth;
        }
    };
    
    // ==========================
    // Loading and displaying events
    // ==========================
    
    
    /**
     * Insert a new calendar event -- can be called two ways:
     * (1) Double-clicking on the cal canvas
     * (2) When the user has no calendar, Cosmo creates a new calendar
     *     and adds the 'Welcome to Cosmo' event with this method
     * @param id A string, the id of the div on the cal canvas double-clicked
     * @param newCal Boolean, whether or not this is a 'Welcome' event
     * for a newly created calendar
     */
    this.insertCalEventNew = function (evParam, newCal) {
        var ev = null; // New event
        var evSource = '';
        var evType = '';
        var allDay = false;
        var lozenge = null; // New blank lozenge
        var startstr = '';
        var evdate = '';
        var dayind = 0;
        var yea = 0;
        var mon = 0;
        var dat = 0;
        var hou = 0;
        var min = 0;
        var start = null;
        var end = null;
        var id = '';
        var evTitle = '';
        var evDesc = '';
        
        // ID for the lozenge -- random strings, also used for div elem IDs
        id = Cal.generateTempId();
        
        // Create the CalEvent obj, attach the CalEventData obj, create the Lozenge 
        // ================================
        evType = (evParam.indexOf('allDayListDiv') > -1) ? 'allDayMain' : 'normal';
        evSource = 'click';
        // Create the lozenge
        if (evType =='normal') {
            lozenge = new cosmo.view.cal.HasTimeLozenge(id);
            allDay = false;
            startstr = Cal.getIndexFromHourDiv(evParam);
            dayind = Cal.extractDayIndexFromId(startstr);
            evdate = Cal.calcDateFromIndex(dayind);
            yea = evdate.getFullYear();
            mon = evdate.getMonth();
            dat = evdate.getDate();
            startstr = Cal.extractTimeFromId(startstr);
            hou = parseInt(Cal.extractHourFromTime(startstr));
            min = parseInt(Cal.extractMinutesFromTime(startstr));
            start = new ScoobyDate(yea, mon, dat, hou, min);
            end = ScoobyDate.add(start, 'n', 60);
        }
        else if (evType == 'allDayMain') {
            lozenge = new cosmo.view.cal.NoTimeLozenge(id);
            allDay = true;
            dayind = Cal.getIndexFromAllDayDiv(evParam);
            start = Cal.calcDateFromIndex(dayind);
            start = new ScoobyDate(start.getFullYear(),
                start.getMonth(), start.getDate());
            start.hours = 0;
            start.minutes = 0;
            end = new ScoobyDate(start.getFullYear(),
                start.getMonth(), start.getDate());
        }
        
        // Create the CalEvent, connect it to its lozenge
        ev = new CalEvent(id, lozenge);
        
        // Set CalEventData start and end calculated from click position
        // --------
        evTitle = newCal ? 'Welcome to Cosmo!' : _('Main.NewEvent');
        evDesc = newCal ? 'Welcome to Cosmo!' : '';
        ev.data = new CalEventData(null, evTitle, evDesc,
            start, end, allDay);
        
        // Register the new event in the event list
        // ================================
        cosmo.view.cal.canvas.eventRegistry.setItem(id, ev);
        
        // Update the lozenge
        // ================================
        if (lozenge.insert(id)) { // Insert the lozenge on the view
            // Save new event
            dojo.event.topic.publish('/calEvent', { 'action': 'save', 'data': ev })
        }
        return cosmo.view.cal.canvas.eventRegistry.getItem(id);
    };
    
    // ==========================
    // Navigating and changing calendars
    // ==========================
    // *** FIXME: Unify mask-display/nav methods ***
    /**
     * Set up the week-to-week navigation button panel 
     */
    this.setUpNavButtons = function () {
        var navButtons = null;
        var leftClick =  null;
        var rightClick = null;
        leftClick = function () { Cal.uiMask.show(); setTimeout('Cal.goView("back");', 300); }
        rightClick = function () { Cal.uiMask.show(); setTimeout('Cal.goView("next");', 300); }
        navButtons = new NavButtonSet('viewNav', leftClick, rightClick);
        document.getElementById('viewNavButtons').appendChild(navButtons.domNode);
    };
    /**
     * Used to navigate from view span to view span, e.g., week-to-week
     */
    this.goView = function (id) {
        var key = id.toLowerCase();
        var queryDate = Cal.getNewViewStart(key);
        this.goViewQueryDate(queryDate);
    };
    this.goViewQueryDate = function (queryDate) {
        Cal.calForm.clear();
        Cal.getQuerySpan(new Date(queryDate)); // Get the new query span week
        // Draw the calendar canvas
        cosmo.view.cal.canvas.render(this.viewStart, this.viewEnd, this.currDate);
        // Load and display events
        cosmo.view.cal.loadEvents(self.viewStart, self.viewEnd);
        MiniCal.render();
        Cal.uiMask.hide();
    };
    /**
     * Used to ensure the 'processing' text shows briefly
     * Prevent seizure-inducing flashes of the mask div
     * Execute whatever function is passed after showing the mask
     */
    this.showMaskDelayNav = function (f) {
        self.uiMask.show();
        setTimeout(f, 200);
    }
    /**
     * Change to a new selected calendar
     * from setTimeout, so use Cal object reference
     */
    this.goSelCal = function () {
        var selectElement = Cal.calForm.form.calSelectElem;
        var index = selectElement.options[selectElement.selectedIndex].value;
        Cal.currentCollection = Cal.currentCollections[index];
        // Load and display events
        cosmo.view.cal.loadEvents(self.viewStart, self.viewEnd);
        Cal.uiMask.hide();
    };

    // ==========================
    // Time/position calculations
    // ==========================
    /**
     * Figures out the date based on Y-pos of left edge of event lozenge
     * with respect to canvas (scrollable div 'timedScrollingMainDiv').
     * @param point Left edge of dragged event lozenge after snap-to.
     * @return A Date object
     */
    this.calcDateFromPos = function (point) {
        var col = parseInt(point/cosmo.view.cal.canvas.dayUnitWidth); // Number 0-6 -- day in the week
        var posdate = this.calcDateFromIndex(col);
        return posdate;
    };
    /**
     * Figures out the hour based on X-pos of top and bottom edges of event lozenge
     * with respect to canvas (scrollable div 'timedScrollingMainDiv').
     * @param point top or bottom edge of dragged event lozenge after snap-to.
     * @return A time string in in military time format
     */
    this.calcTimeFromPos = function (point) {
        var h = 0;
        var m = 0;
        h = parseInt(point/HOUR_UNIT_HEIGHT);
        h = h.toString();
        m = (((point % HOUR_UNIT_HEIGHT)/HOUR_UNIT_HEIGHT)*60);
        h = h.length < 2 ? '0' + h : h;
        m = m == 0 ? '00' : m.toString();
        return h + ':' + m;
    };
    /**
     * Figures out the X-position for the top or bottom edge of an event lozenge
     * based on a military time.
     * @param miltime time string in military time format
     * @return An integer of the X-position for the top/bottom edge of an event lozenge
     */
    this.calcPosFromTime = function (miltime) {
        var h = this.extractHourFromTime(miltime);
        var m = this.extractMinutesFromTime(miltime);
        var pos = 0;

        pos += (h*HOUR_UNIT_HEIGHT);
        pos += ((m/60)*HOUR_UNIT_HEIGHT);
        pos = parseInt(pos);
        return pos;
    };
    /**
     * Calculate the date based on the day position clicked on
     * @param n Number representing day of the week
     * @return A date object representing the date clicked on
     */
    this.calcDateFromIndex = function (n) {
        var incr = parseInt(n);
        var st = this.viewStart.getDate();
        var ret = null;
        st += incr;
        ret = new Date(this.viewStart.getFullYear(), this.viewStart.getMonth(), st);
        return ret;
    };

    // ==========================
    // DOM-element info extraction and IDs
    // ==========================
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an event lozenge, and look up which event the lozenge belongs to.
     * Event lozenge components are all similarly named, beginning with 'eventDiv',
     * then followed by some indentifying text, a separator, and then the ID.
     * (e.g., 'eventDivBottom__12' or 'eventDivContent__8').
     * @return A string representing the event identifier for the event lozenge clicked on
     */
    this.getIndexEvent = function (strId) {
        // Use regex to pull out the actual ID number
        var pat = new RegExp('^eventDiv[\\D]*' + Cal.ID_SEPARATOR);
        var id = strId.replace(pat, '');
        return id;
    };
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an hour container, and look up which date/time the div belongs to.
     * Hour-div components are all similarly named, beginning with 'hourDiv',
     * then followed by some indentifying text, and then the date and hour
     * separated by a hyphen (e.g., 'hourDiv20051223-13' or 'hourDivSub20051016-2').
     * @return A string representing the date/time of the div clicked on
     */
    this.getIndexFromHourDiv = function (strId) {
        var ind = strId.replace(/^hourDiv[\D]*/i, '');
        return ind;
    };
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an all-day event container, and look up which date the div belongs to.
     * All-day-div components are all similarly named, beginning with 'allDayListDiv',
     * then followed by some indentifying text, and then the date
     * (e.g., 'allDayListDiv20051223' or 'allDayListDivSub20051016').
     * @return A string representing the date/time of the div clicked on
     */
    this.getIndexFromAllDayDiv = function (strId) {
        var ind = strId.replace(/^allDayListDiv[\D]*/i, '');
        return ind;
    };
    /**
     * Get the time from hyphen-separated string on a clicked-on hour div
     * @return A string of the time in military 'hh:mm' format
     */
    this.extractTimeFromId = function (str) {
        var dt = str.split('-');
        var pat = /(00|30)$/
        var ret = dt[1].replace(pat, ':$1');
        return ret;
    };
    /**
     * Get the hour from a time-formatted string such as '23:56'
     * @return A string of the hour number
     */
    this.extractHourFromTime = function (str) {
        arr = str.split(':');
        return arr[0];
    };
    /**
     * Get the minutes from a time-formatted string such as '23:56'
     * @return A string of the minutes
     */
    this.extractMinutesFromTime = function (str) {
        arr = str.split(':');
        return arr[1];
    };
    /**
     * Get the date from hyphen-separated string on a clicked-on hour div
     * @return A string of the date, e.g., 20051223
     */
    this.extractDayIndexFromId = function (str) {
        var dt = str.split('-');
        return parseInt(dt[0]);
    };
    /**
     * Generate a scratch ID to use for events in the UI
     * @return A random string to use as an ID for a CalEvent obj
     */
    this.generateTempId = function () {
        var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        var len = 12;
        var randomString = '';
        for (var i = 0; i < len; i++) {
            var rnum = Math.floor(Math.random() * chars.length);
            randomString += chars.substring(rnum,rnum+1);
        }
        return 'ev' + randomString;
    };

    // ==========================
    // Cal-specific time manip functions
    // ==========================
    /**
     * Get the start and end for the span of time to view in the cal
     * Eventually this will change depending on what type of view is selected
     */
    this.getQuerySpan = function (dt) {
        this.viewStart = this.getWeekStart(dt);
        this.viewEnd = this.getWeekEnd(dt);
        return true;
    };
    /**
     * Get the datetime for midnight Sunday of a week given a date
     * anywhere in the week
     */
    this.getWeekStart = function (dt) {
        var diff = dt.getDay();
        var sun = new Date(dt.getTime());
        diff = 0 - diff;
        sun.add('d', diff);
        var ret = new Date(sun.getFullYear(), sun.getMonth(), sun.getDate());
        return ret;
    };
    /**
     * Get the datetime for 23:59:59 Saturday night of a week
     * given a date anywhere in the week
     */
    this.getWeekEnd = function (dt) {
        var diff = 6-dt.getDay();
        var sat = new Date(dt.getTime());
        sat.add('d', diff);
         // Make time of day 11:59:99 to get the entire day's events
        var ret = new Date(sat.getFullYear(), sat.getMonth(), sat.getDate(), 23, 59, 59);
        return ret;
    };
    /**
     * Get the datetime for midnight Sunday given the current Sunday
     * and the number of weeks to move forward or backward
     */
    this.getNewViewStart = function (key) {
        var queryDate = null;
        // Increment/decrement week
        if (key.indexOf('next') > -1) {
            queryDate = Date.add('ww', 1, this.viewStart);
        }
        else if (key.indexOf('back') > -1) {
            queryDate = Date.add('ww', -1, this.viewStart);
        }
        return queryDate;
    };

    // ==========================
    // Timeout and keepalive
    // ==========================
    this.isTimedOut = function () {
        var diff = 0;
        diff = new Date().getTime() - this.serv.getServiceAccessTime();
        if (diff > (60000*TIMEOUT_MIN)) {
            return true;
        }
        else {
            return false;
        }
    };
    this.isServerTimeoutSoon = function () {
        var ts = new Date();
        var diff = 0;
        ts = ts.getTime();
        diff = ts - this.serv.getServiceAccessTime();
        return (diff > (60000*(TIMEOUT_MIN-2))) ? true : false
    };
    this.checkTimeout = function () {
        // If user is client-side timed-out, kill the session cookie and redirect to login page
        if (this.isTimedOut()) {
            this.redirectTimeout();
            return true;
        }
        // Otherwise check for imminent server timeout and refresh local timing cookie
        else if (this.isServerTimeoutSoon()) {
            // If server-side session is about to time out, refresh it by hitting JSP page
            this.serv.refreshServerSession();
            // Reset local session timing cookie
            this.serv.resetServiceAccessTime(); 
            return false;
        }
    };

    this.handleCollectionUpdated = function(/*cosmo.topics.CollectionUpdatedMessage*/ message){
        var updatedCollection = message.collection;
        var updateCollection = function(collection){
            if (collection.collection.uid == updatedCollection.uid){
                collection.collection = updatedCollection;
            }
            if (!collection.transportInfo){
                collection.displayName = updatedCollection.name;
            }
        }

        updateCollection(this.currentCollection);        
        dojo.lang.map(this.currentCollections, updateCollection);
    }
    
    this.handleSubscriptionUpdated = function(/*cosmo.topics.SubscriptionUpdatedMessage*/ message){
        var updatedSubscription = message.subscription;
        var updateCollection = function(collection){
            var transportInfo = collection.transportInfo;
            if (transportInfo && transportInfo instanceof cosmo.model.Subscription){
                if (transportInfo.calendar.uid = updatedSubscription.calendar.uid){
                    collection.transportInfo = updatedSubscription;
                    collection.displayName = updatedSubscription.displayName;
                }
            }
        }
        updateCollection(this.currentCollection);        
        dojo.lang.map(this.currentCollections, updateCollection);
    }

    this.redirectTimeout = function () {
        location = cosmo.env.getRedirectUrl();
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
// Cal is a singleton
cosmo.ui.cal_main.Cal.constructor = null

Cal = cosmo.ui.cal_main.Cal;

