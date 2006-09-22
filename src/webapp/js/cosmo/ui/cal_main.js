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

// Global variables for X and Y position for mouse
var xPos = 0;
var yPos = 0;

/**
 * @object The Cal singleton
 */
var Cal = new function() {
    
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
    // The currently selected event -- a CalEvent obj
    // Because of problems with loss of scope from callbacks, this
    // gets used a lot in CalEvent methods called on the selected event
    this.currSelObj = null;
    // The element currently being dragged -- a Draggable obj
    this.dragElem = null;
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
    // The dialog box used to display
    // warnings / confirmations -- a Dialog obj
    this.dialog = new Dialog();
    // Resizeable area for all-day and any-time events --
    // a ResizeArea obj
    this.allDayArea = null;
    // Dummy recurrence rule for getting recurrence options
    this.recurTemplate = new RecurrenceRule();
    this.recurTemplate.options = ['FREQUENCY_DAILY','FREQUENCY_WEEKLY', 
        'FREQUENCY_BIWEEKLY', 'FREQUENCY_MONTHLY', 'FREQUENCY_YEARLY'];

    this.statusTemplate = new StatusTemplate();
    this.statusTemplate.options = ['CONFIRMED', 'TENTATIVE', 'CANCELLED'];

    // The list of events currently displayed
    // on the calendar -- key is the CalEvent obj's id prop,
    // value is the CalEvent
    this.eventRegistry = new Hash();

    // Remote calls for saving or removing --
    // tracking list used for callback reference
    this.asyncRegistry = new Hash();

    // List of any queued-up error messages
    this.errorList = [];

    // Used when the modal dialog box
    // in Cal.dialog is showing. Currently input is actually disabled
    // with a div that fully covers the entire UI, block/none display
    // this property also does get set to true/false at the same time
    this.inputDisabled = false;

    // Used with the local UI timeout calculations
    // A related property is Cal.serv.serviceAccessTime in the Cosmo service
    this.inputTimestamp = null;

    // The path to the currently selected calendar
    this.currentCalendar = null;

    //The list of calendars available to the current user
    this.calendars = [];

    // ==========================
    // Init
    // ==========================
    /**
     * Main function -- place absolute-positioned UI elements, wipe canvas clean,
     * load the current week, and show the hours in each day col
     */
    this.init = function() {

        var viewDiv = null;
        var allDayDiv = null;
        
        this.currDate = new Date();
        this.calendars = [];

        // Create and init the Cosmo service
        // --------------
        this.serv = new ScoobyService();
        this.serv.init();

        // Load user prefs
        // --------------
        Pref.init();

        // Place all the UI DOM elements based on window size
        // --------------
        if (this.createUI()) {
            viewDiv = document.getElementById('timedScrollingMainDiv');
            allDayDiv = document.getElementById('allDayContentDiv');
            this.placeUI();
            this.setImagesForSkin();
        }

        // Load and display date info for UI elements
        // --------------
        if (this.loadLocaleDateInfo() && this.getQuerySpan(this.currDate)) {
            cosmo.view.cal.canvas.render(this.viewStart, this.viewEnd, this.currDate);
        }

        // Create the calendar form obj -- clear on page reload
        // --------------
        this.calForm = new CalForm();
        this.calForm.init();

        // Create the resizable all-day event area
        // --------------
        this.allDayArea = new ResizeArea('allDayResizeMainDiv', 'allDayResizeHandleDiv');
        this.allDayArea.init('down');
        this.allDayArea.addAdjacent('timedScrollingMainDiv');
        this.allDayArea.setDragLimit();
        
        // Display day grid on all-day event area
        //this.showAllDayList();

        // Set the initial timestamp for client-side timeout/keepalive fu
        this.setInputTimestamp();

        // Load/create calendar to view
        // --------------
        // Get stored cals for this user
        this.calendars = this.serv.getCalendars();
        //TODO need to sort
        this.calendars.sort(); // Sort by alpha
        // No cals for this user
        if (!this.calendars.length){
            // Create initial cal
            try {
                this.serv.createCalendar('Cosmo', 'Cosmo');
            }
            catch(e) {
                Cal.showErr(getText('Main.Error.InitCalCreateFailed'), e);
                return false;
            }
            // Set it as the default
            this.currentCalendar = new CalendarMetadata();
            this.currentCalendar.name = 'Cosmo';
            this.currentCalendar.path = 'Cosmo';

            // Add 'Welcome to Cosmo' Event
            this.insertCalEventNew('hourDiv3-900', true);
        }
        // Cals exist for this user
        else {
            // If more than one cal exists, create the cal selector nav
            if (this.calendars.length > 1) {
                this.calForm.addCalSelector(this.calendars);
            }
            // Set the first cal as the default
            this.currentCalendar = this.calendars[0];
        }

        // Add event listeners to the form elements
        this.calForm.setEventListeners();

        // Load and display events
        // --------------
        this.loadDisplayEvents();
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
        
        // Load minical and jump-to date
        var mcDiv = document.getElementById('miniCalDiv');
        var jpDiv = document.getElementById('jumpToDateDiv');
        // Place jump-to date based on mini-cal pos
        if (MiniCal.init(Cal, mcDiv)) {
           this.calForm.addJumpToDate(jpDiv);
        }
    };

    // ==========================
    // GUI element display
    // ==========================
    this.createUI = function() {
        //FIXME this method should go away
        return true;
    }

    /**
     * Performs the absolute placement of the UI elements based
     * on the client window size
     */
    this.placeUI = function() {
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
    this.setImagesForSkin =  function() {
        var skinImagesDir = 'templates/' + TEMPLATE_DIRECTORY + '/images/';
        var handleImg = null;
        var navButtons = null;
        var leftClick =  null;
        var rightClick = null;
        
        handleImg = document.createElement('img');
        handleImg.src = cosmo.env.getImagesUrl() + 'resize_handle_image.gif';
       
        leftClick = function() { Cal.uiMask.show(); setTimeout('Cal.goView("back");', 100); }
        rightClick = function() { Cal.uiMask.show(); setTimeout('Cal.goView("next");', 100); }
        navButtons = new NavButtonSet('viewNav', leftClick, rightClick);
       
       
        document.getElementById('allDayResizeHandleDiv').appendChild(handleImg);
        document.getElementById('viewNavButtons').appendChild(navButtons.domNode);
        document.getElementById('smallLogoDiv').style.background =
            'url(' + cosmo.env.getImagesUrl() + LOGO_GRAPHIC_SM + ')';
    };
    /**
     * Loads localized Date information into the arrays in date.js
     * It uses the default English values in those arrays
     * as keys to look up the localized info
     * from the i18n.js (generated by i18n.jsp) page
     */
    this.loadLocaleDateInfo = function() {
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
            newArr.push(getText('App.' + keys[i]));
        }
        Date.abbrWeekday = newArr;
        // Full month names array
        // ============
        keys = Date.fullMonth;
        newArr = [];
        for (var i = 0; i < keys.length; i++) {
            newArr.push(getText('App.' + keys[i]));
        }
        Date.fullMonth = newArr;
        // AM/PM
        // ============
        newArr = [];
        newArr['AM'] = getText('App.AM');
        newArr['PM'] = getText('App.PM');
        Date.meridian = newArr;
        return true;
    };
    /**
     * Get the scroll offset for the scrollable view area ('timedScrollingMainDiv')
     * @return An integer representing the scroll offset for the scrollable view area.
     */
    this.getMainViewScrollTop = function() {
        // Has to be looked up every time, as value may change
        // either when user scrolls or resizes all-day event area
        var top = document.getElementById('timedScrollingMainDiv').scrollTop;
        top -= this.viewOffset;
        top -= (this.allDayArea.dragSize-this.allDayArea.origSize); // Subtract change in resized all-day event area
        return top;

    };
    this.getWinHeight = function() {
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
    this.getWinWidth =  function() {
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
     * Load and display the events for the selected calendar
     * for the selected span of time
     */
    this.loadDisplayEvents = function() {
        // Load saved events
        if (Cal.loadEvents() &&
            cosmo.view.cal.conflict.sortAndClearEvents(Cal.eventRegistry) &&
            cosmo.view.cal.conflict.checkConflicts(Cal.eventRegistry) &&
            cosmo.view.cal.conflict.stackUntimed(Cal.eventRegistry) &&
            Cal.placeBlocks()) {
            // Give the final event the selection and display in form
            if (Cal.eventRegistry.length) {
               Cal.setSelected(Cal.eventRegistry.getLast());
            }
            if (Cal.currSelObj) {
                // Show the last event -- the selected one, in the info form
                Cal.calForm.updateFromEvent(Cal.currSelObj);
            }
        }
    };
    this.updateAllEventsDisplayed = function() {
        return (cosmo.view.cal.conflict.sortAndClearEvents(Cal.eventRegistry) &&
            cosmo.view.cal.conflict.checkConflicts(Cal.eventRegistry) && 
            cosmo.view.cal.conflict.stackUntimed(Cal.eventRegistry) && Cal.placeBlocks());
    }
    /**
     * Load the events from the backend
     * Calls Cal.insertCalEventLoaded for each CalEventDate obj loaded
     * Returns true when finished
     */
    this.loadEvents = function() {
        var eventLoadList = null;
        var isErr = false;
        var detail = '';
        var evData = null;
        var ev = null;

        // Load the array of events
        // ======================
        try {
            eventLoadList = this.serv.getEvents(this.currentCalendar.path,
                this.viewStart.getTime(), this.viewEnd.getTime());
        }
        catch(e) {
            Cal.showErr(getText('Main.Error.LoadEventsFailed'), e);
            Log.print(e.javaStack);
            return false;
        }
        // Create blocks for all the events
        // ======================
        for (var i = 0; i < eventLoadList.length; i++) {
            evData = eventLoadList[i];
            // Basic paranoia checks
            if (!evData.end) {
                evData.end = ScoobyDate.clone(evData.start);
            }
            if (evData.start.timezone || evData.end.timezone) {
                if (!evData.end.timezone) {
                    evData.end.timezone =
                        ScoobyTimezone.clone(evData.start.timezone);
                }
                if (!evData.start.timezone) {
                    evData.start.timezone =
                        ScoobyTimezone.clone(evData.end.timezone);
                }
            }
            Cal.insertCalEventLoaded(evData);
        }
        return true;

    };
    
    /**
     * Insert a new calendar event -- can be called two ways:
     * (1) Double-clicking on the cal canvas
     * (2) When the user has no calendar, Cosmo creates a new calendar
     *     and adds the 'Welcome to Cosmo' event with this method
     * @param id A string, the id of the div on the cal canvas double-clicked
     * @param newCal Boolean, whether or not this is a 'Welcome' event
     * for a newly created calendar
     */
    this.insertCalEventNew = function(evParam, newCal) {
        var ev = null; // New event
        var evSource = '';
        var evType = '';
        var allDay = false;
        var block = null; // New blank block
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

        // ID for the block -- random strings, also used for div elem IDs
        id = Cal.generateTempId();

        // Create the CalEvent obj, attach the CalEventData obj, create the Block
        // ================================
        evType = (evParam.indexOf('allDayListDiv') > -1) ? 'allDayMain' : 'normal';
        evSource = 'click';
        // Create the block
        if (evType =='normal') {
            block = new HasTimeBlock(id);
            allDay = false;
            startstr = Cal.getIndexFromHourDiv(evParam);
            dayind = Cal.extractDayIndexFromId(startstr);
            evdate = Cal.calcDateFromIndex(dayind);
            yea = evdate.getFullYear();
            mon = evdate.getMonth();
            dat = evdate.getDate();
            startstr = Cal.extractTimeFromId(startstr);
            hou = Cal.extractHourFromTime(startstr);
            min = Cal.extractMinutesFromTime(startstr);
            start = new ScoobyDate(yea, mon, dat, hou, min);
            end = ScoobyDate.add(start, 'n', 60);
        }
        else if (evType == 'allDayMain') {
            block = new NoTimeBlock(id);
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

        // Create the CalEvent, connect it to its block
        ev = new CalEvent(id, block);

        // Set CalEventData start and end calculated from click position
        // --------
        evTitle = newCal ? 'Welcome to Cosmo!' : getText('Main.NewEvent');
        evDesc = newCal ? 'Welcome to Cosmo!' : '';
        ev.data = new CalEventData(null, evTitle, evDesc,
            start, end, allDay);

        // Register the new event in the event list
        // ================================
        Cal.eventRegistry.setItem(id, ev);

        // Update the block
        // ================================
        if (block.insert(id)) { // Insert the block on the view
            this.setSelected(ev);
            // Save on the backend if it's a new event
            ev.remoteSaveMain();
        }
        return Cal.eventRegistry.getItem(id);
    };
    /**
     * Main method for creating the CalEvent and Block obj for
     * events loaded from the server
     */
    this.insertCalEventLoaded = function(evData) {
            // ID for the block -- random strings, also used for div elem IDs
            var id = Cal.generateTempId();
            // Create a blank CalEvent obj to fill in with data from server
            ev = new CalEvent(id, null);
            ev.data = evData;

            // Register the new event in the event list
            // ================================
            Cal.eventRegistry.setItem(id, ev);

            // Create the block and link it to the event
            ev.block = ev.data.allDay ? new NoTimeBlock(id) :
                new HasTimeBlock(id);
            ev.block.insert(id);
    };
    /**
     * Update the display of all blocks on the canvas
     */
    this.placeBlocks = function() {
        return Cal.eventRegistry.each(Cal.placeBlock);
    };
    this.placeBlock = function(key, val) {
        ev = val;
        ev.block.updateFromEvent(ev);
        ev.block.updateDisplayMain();
    }

    // ==========================
    // Saving and removing events
    // ==========================
    /**
     * Called when user clicks the Save button on the event info form
     * Updates the properties of the selected event and saves the
     * changes to the backend. Handler for the save process will update
     * the block pos/size when the save successfully completes
     */
    this.saveCalEvent = function() {
        var selEvent = Cal.currSelObj;
        var startpos = 0;
        var endpos = 0;
        var height = 0;
        var errMsg = '';

        // Give timeout check in onclick handler a chance to work
        if (Cal.isTimedOut()) {
            return false;
        }
        // Make backup snapshot
        selEvent.makeSnapshot();
        // Update CalEvent obj
        if (Cal.calForm.updateEvent(selEvent)) {
            // Save the changes to the backend -- handler for remote save
            // process will update block position and size
            selEvent.remoteSaveMain();
        }
    };
    /**
     * Called when the user clicks the 'Remove' button from the confirmation
     * dialog box for removal -- calls removeCalEvent which removes the event
     */
    this.removeCalEventConfirmed = function() {
        // Use asolute references to Cal instead of 'this'
        // because this method is called from Button context
        Cal.currSelObj.remove();
        // Clear out the data in the event detail form
        Cal.calForm.clear();
        // No currently selected event
        Cal.currSelObj = null;
        // Dissapear the dialog
        Cal.hideDialog();
    };
    /**
     * Removes a cal event from the canvas -- called in three cases:
     * (1) Actually removing an event from the calendar (this gets
     *     called after the backend successfully removes it)
     * (2) Removing an event from view because it's been edited
     *     to dates outside the viewable span
     * (3) Removing the placeholder event when initial event
     *     creation fails
     * @param ev CalEvent obj, the event to be removed
     */
    this.removeCalEvent = function(ev) {
        // Remove from list of visible events
        Cal.eventRegistry.removeItem(ev.id);
        // Remove the block
        ev.block.remove();
        // If this was the selected event, set currSelObj to null
        if (Cal.currSelObj && (Cal.currSelObj.id = ev.id)) {
            Cal.currSelObj = null;
        }
        // Bye-bye, event
        ev = null;
    };
    /**
     * Set a particular calendar event as the currently selected one
     * @param ev The CalEvent obj for the element clicked on
     */
    this.setSelected = function(ev) {
        // Deselect previously selected event if any
        if (this.currSelObj) {
            this.currSelObj.block.setDeselected();
        }
        this.currSelObj = ev; // Pointer to the currently selected event
        ev.block.setSelected(); // Show the associated block as selected
        // Enable the Remove and Save buttons
        Cal.calForm.setButtons(true, true);
    };

    // ==========================
    // Navigating and changing calendars
    // ==========================
    /**
     * Removes all events from the display
     * Used when navigating through view spans, e.g., week-to-week,
     * and when changing selected calendars
     */
    this.wipeView = function() {
        var events = this.eventRegistry;
        var ev = null;
        this.currSelObj = null; // Kill selected event
        // Pull the last event off the eventRegistry list and remove it
        // Don't use 'pop' -- removeCalEvent takes the item off the eventRegistry
        while (ev = events.getLast()) {
            this.removeCalEvent(ev, false);
        }
    };
    /**
     * Used to navigate from view span to view span, e.g., week-to-week
     */
    this.goView = function(id) {
        var key = id.toLowerCase();
        var queryDate = Cal.getNewViewStart(key);
        this.goViewQueryDate(queryDate);
        MiniCal.render();
    };

    this.goViewQueryDate = function(queryDate) {
        Cal.wipeView();
        Cal.calForm.clear();
        Cal.eventRegistry = new Hash();
        Cal.asyncRegistry = new Hash();
        Cal.getQuerySpan(new Date(queryDate)); // Get the new query span week
        // Draw the calendar canvas
        cosmo.view.cal.canvas.render(this.viewStart, this.viewEnd, this.currDate);
        // Load and display events
        Cal.loadDisplayEvents();
        Cal.uiMask.hide();
    };
    /**
     * Used to ensure the 'processing' text shows briefly
     * Prevent seizure-inducing flashes of the mask div
     */
    this.goSelCalMask = function() {
        Cal.uiMask.show();
        setTimeout(Cal.goSelCal, 100);
    };
    /**
     * Change to a new selected calendar -- called on a 100 MS delay
     * from setTimeout, so use Cal object reference
     */
    this.goSelCal = function() {
        var selectElement = Cal.calForm.form.calSelectElem;
        var index = selectElement.options[selectElement.selectedIndex].value;
        Cal.currentCalendar = Cal.calendars[index];
        Cal.wipeView();
        // Load and display events
        Cal.loadDisplayEvents();
        Cal.uiMask.hide();
    };

    // ==========================
    // Modal dialog boxes
    // ==========================
    /**
     * Show error dialog with either simple text msg, error object, or both
     * If new errors get spawned while this is processing, it queues the
     * messages for display after users dismisses the faux modal disalog box
     */
    this.showErr = function(str, e) {
        var msg = '';
        var currErr = '';
        var but = null;

        // If the error dialog is already showing, add this message to the error queue
        if (this.dialog.isDisplayed) {
            this.errorList.push(str);
        }
        // Otherwise display the error dialog
        else {
            // If there are errors waiting in the queue, prepend them to the error msg
            if (this.errorList.length) {
                while (currErr = this.errorList.shift()) {
                    msg += '<div class="errText">' + currErr + '</div>';
                }
                msg += str;
            }
            // Otherwise just display the current message
            else {
                msg = '<div class="errText">' + str + '</div>';
                if (e) {
                    msg += '<div>' + e.message + '</div>'
                }
            }
            this.dialog.type = this.dialog.ERROR;
            but = new Button('okButton', 64, Cal.hideDialog,
                getText('App.Button.OK'), true);
            this.dialog.btnsCenter[0] = but;
            this.dialog.defaultAction = Cal.hideDialog;
            this.dialog.msg = msg;
            this.showDialog();
        }
    };
    /**
     * Display confirmation dialog when user clicks Remove button
     * or uses Delete button on keyboard to remove an event
     * When the dialog box is displayed, hitting Enter will call
     * whatever method is attached to the defaultAction property
     * of the dialog box
     */
    this.showRemoveConfirm = function() {
        var but = null;
        var msg = '';

        // BANDAID: Warning specifically for recurring events
        if (Cal.currSelObj.data.masterEvent || Cal.currSelObj.data.instance) {
            msg = 'This is a recurring event. Removing recurring events is not supported in Cosmo,' +
                ' and will probably have effects you do not intend.<br/>&nbsp;<br/>Remove this event?';
        }
        else {
            msg = getText('Main.Prompt.EventRemoveConfirm');
        }

        // Use asolute references to Cal instead of 'this'
        // because this method is called from Button context
        Cal.dialog.type = Cal.dialog.CONFIRM;
        but = new Button('cancelButtonDialog', 74, Cal.hideDialog,
            getText('App.Button.Cancel'), true);
        Cal.dialog.btnsLeft[0] = but;
        but = new Button('removeButtonDialog', 74, Cal.removeCalEventConfirmed,
            getText('App.Button.Remove'), true);
        Cal.dialog.btnsRight[0] = but;
        Cal.dialog.defaultAction = Cal.removeCalEventConfirmed;
        Cal.dialog.msg = msg;
        Cal.showDialog();
    };
    /**
     * Display confirmation dialog when user attempts to edit a
     * recurring event -- not currently a supported action
     * When the dialog box is displayed, hitting Enter will call
     * whatever method is attached to the defaultAction property
     * of the dialog box
     */
    this.showSaveConfirm = function(str) {
        var but = null;
        var msg = str;
        // Use asolute references to Cal instead of 'this'
        // because this method is called from Button context
        Cal.dialog.type = Cal.dialog.CONFIRM;
        but = new Button('saveButtonDialog', 74, Cal.currSelObj.cancelSave,
            getText('App.Button.Cancel'), true);
        Cal.dialog.btnsLeft[0] = but;
        but = new Button('saveButtonDialog', 74, Cal.currSelObj.remoteSave,
            getText('App.Button.Save'), true);
        Cal.dialog.btnsRight[0] = but;
        Cal.dialog.defaultAction = Cal.currSelObj.remoteSave;
        Cal.dialog.msg = msg;
        Cal.showDialog();
    };
    /**
     * Display the current dialog box and throw up the transparent
     * full-screen div that blocks all user input to the UI
     */
    this.showDialog = function() {
        Cal.setInputDisabled(true);
        Cal.dialog.show();
    };
    /**
     * Dismiss the faux modal dialog box -- check for queued error
     * messages to display if needed
     * Put away the full-screen transparent input-blocking div
     */
    this.hideDialog = function() {
        // Hide the current error dialog
        Cal.dialog.hide();
        // If there are error messages that have been added to the queue,
        // trigger another dialog to handle them
        if (Cal.errorList.length) {
            Cal.showErr('');
        }
        else {
            Cal.setInputDisabled(false);
        }
    };
    this.setInputDisabled = function(isDisabled) {
        if (isDisabled) {
            document.getElementById('fullMaskDiv').style.display = 'block'; // Block input with full-sized mask
            this.inputDisabled = true;
        }
        else {
            document.getElementById('fullMaskDiv').style.display = 'none'; // Remove full-sized mask
            this.inputDisabled = false;
        }
        return this.inputDisabled;
    };
    /**
     * Whether or not input from the entire UI is disabled
     * Returns true when the faux modal dialog box is displayed
     */
    this.getInputDisabled = function() {
        return this.inputDisabled;
    };

    // ==========================
    // Time/position calculations
    // ==========================
    /**
     * Figures out the date based on Y-pos of left edge of event block
     * with respect to canvas (scrollable div 'timedScrollingMainDiv').
     * @param point Left edge of dragged event block after snap-to.
     * @return A Date object
     */
    this.calcDateFromPos = function(point) {
        var col = parseInt(point/cosmo.view.cal.canvas.dayUnitWidth); // Number 0-6 -- day in the week
        var posdate = this.calcDateFromIndex(col);
        return posdate;
    };
    /**
     * Figures out the hour based on X-pos of top and bottom edges of event block
     * with respect to canvas (scrollable div 'timedScrollingMainDiv').
     * @param point top or bottom edge of dragged event block after snap-to.
     * @return A time string in in military time format
     */
    this.calcTimeFromPos = function(point) {
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
     * Figures out the X-position for the top or bottom edge of an event block
     * based on a military time.
     * @param miltime time string in military time format
     * @return An integer of the X-position for the top/bottom edge of an event block
     */
    this.calcPosFromTime = function(miltime) {
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
    this.calcDateFromIndex = function(n) {
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
     * an event block, and look up which event the block belongs to.
     * Event block components are all similarly named, beginning with 'eventDiv',
     * then followed by some indentifying text, a separator, and then the ID.
     * (e.g., 'eventDivBottom__12' or 'eventDivContent__8').
     * @return A string representing the event identifier for the event block clicked on
     */
    this.getIndexEvent = function(strId) {
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
    this.getIndexFromHourDiv = function(strId) {
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
    this.getIndexFromAllDayDiv = function(strId) {
        var ind = strId.replace(/^allDayListDiv[\D]*/i, '');
        return ind;
    };
    /**
     * Get the time from hyphen-separated string on a clicked-on hour div
     * @return A string of the time in military 'hh:mm' format
     */
    this.extractTimeFromId = function(str) {
        var dt = str.split('-');
        var pat = /(00|30)$/
        var ret = dt[1].replace(pat, ':$1');
        return ret;
    };
    /**
     * Get the hour from a time-formatted string such as '23:56'
     * @return A string of the hour number
     */
    this.extractHourFromTime = function(str) {
        arr = str.split(':');
        return arr[0];
    };
    /**
     * Get the minutes from a time-formatted string such as '23:56'
     * @return A string of the minutes
     */
    this.extractMinutesFromTime = function(str) {
        arr = str.split(':');
        return arr[1];
    };
    /**
     * Get the date from hyphen-separated string on a clicked-on hour div
     * @return A string of the date, e.g., 20051223
     */
    this.extractDayIndexFromId = function(str) {
        var dt = str.split('-');
        return parseInt(dt[0]);
    };
    /**
     * Generate a scratch ID to use for events in the UI
     * @return A random string to use as an ID for a CalEvent obj
     */
    this.generateTempId = function() {
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
    this.getQuerySpan = function(dt) {
        this.viewStart = this.getWeekStart(dt);
        this.viewEnd = this.getWeekEnd(dt);
        return true;
    };
    /**
     * Get the datetime for midnight Sunday of a week given a date
     * anywhere in the week
     */
    this.getWeekStart = function(dt) {
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
    this.getWeekEnd = function(dt) {
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
    this.getNewViewStart = function(key) {
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
    this.setInputTimestamp = function() {
        /*
         * =========================
         * FIXME: Unify the Cal.inputTimestamp
         * and Cal.serv.serviceAccessTime properties
         * They represent the same thing, so right now it's redundant
         * We have to set them both for this stuff to work
         * =========================
         */
        var ts = new Date();
        ts = ts.getTime();
        this.inputTimestamp = ts;
        Cookie.set('inputTimestamp', ts);
        this.serv.resetServiceAccessTime();
    };
    this.isTimedOut = function() {
        var ts = new Date();
        var diff = 0;
        ts = ts.getTime();
        diff = ts - this.inputTimestamp;
        if (diff > (60000*TIMEOUT_MIN)) {
            return true;
        }
        else {
            return false;
        }
    };
    this.isServerTimeoutSoon = function() {
        var ts = new Date();
        var diff = 0;
        ts = ts.getTime();
        diff = ts - this.serv.getServiceAccessTime();
        return (diff > (60000*(TIMEOUT_MIN-2))) ? true : false
    };
    this.checkTimeout = function() {
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
            this.setInputTimestamp();
            return false;
        }
    };
    this.redirectTimeout = function() {
        location = cosmo.env.getRedirectUrl();
    },

    // ==========================
    // Cleanup
    // ==========================
    this.cleanup = function() {
        if (this.uiMask) {
            this.uiMask.cleanup();
        }
        if (this.allDayArea) {
            this.allDayArea.cleanup();
        }
        this.calForm = null;
        this.allDayArea = null;
        this.eventRegistry = null;
        this.asyncRegistry = null;
    };
}

// Cal is a singleton
Cal.constructor = null;
