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

dojo.provide('cosmo.view.cal.canvas');

dojo.require('dojo.event.*');
dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("dojo.DeferredList");
dojo.require("cosmo.app");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.datetime.Date");
dojo.require('cosmo.ui.event.handlers');
dojo.require('cosmo.view.cal.draggable');
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.convenience");
dojo.require("cosmo.model");
dojo.require("cosmo.ui.menu");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.ui.resize_area");
dojo.require("cosmo.ui.ContentBox");
dojo.require("cosmo.view.common");
dojo.require("cosmo.view.contextMenu");
dojo.require("cosmo.view.service");
dojo.require("cosmo.view.cal.common");
dojo.require('cosmo.view.cal.lozenge');
dojo.require("cosmo.view.cal.conflict");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.ui.DetailFormConverter");

cosmo.view.cal.canvas = new function () {

    // Need some closure for scope
    var self = this;
    // Rendering the first time
    var hasBeenRendered = false;
    // Resizeable area for all-day events -- a ResizeArea obj
    var allDayArea = null;
    // Ref to timed canvas content area to save DOM lookup
    var timedCanvasContentArea = null;
    // Ref to untimed canvas content area to save DOM lookup
    var untimedCanvasContentArea = null;

    dojo.lang.mixin(this, cosmo.view.canvasBase);

    // Public props
    // ****************
    // Width of day col in week view, width of event lozenges --
    // Calc'd based on client window size
    // Other pieces of the app use this, so make it public
    this.dayUnitWidth = 0;
    // Set this value from the view
    this.view = cosmo.view.cal;
    // Set self to the view's canvasInstance
    this.view.canvasInstance = self;
    // UIDs for selected events keyed by the uid of
    // the currently displayed collection
    this.selectedItemIdRegistry = {};
    // Stash references to the selected object here
    // The current itemRegistry won't always have the
    // selected item loaded. If it's not in the
    // itemRegistry, pull it from here to persist the
    // collection's selected object in the detail view
    this.selectedItemCache = {};
    // Available lozenge colors
    this.colors = {};
    // The scrolling div for timed events
    this.timedCanvas = null;
    // The scroll offset -- needed to preserve scroll position
    this.timedCanvasScrollTop = parseInt(HOUR_UNIT_HEIGHT*8);
    // Init flag -- NavBar's displayView uses this to know
    // when to call the canvas init function
    this.hasBeenInitialized = false;
    this.contextMenu = null;

    // Public methods
    // ****************
    this.init = function () {
        dojo.debug('init on cal canvas');
        // Subscribe to the '/calEvent' channel
        dojo.event.topic.subscribe('/calEvent', self, 'handlePub_calEvent');
        // Subscribe to the '/app' channel
        dojo.event.topic.subscribe('/app', self, 'handlePub_app');
        this.hasBeenInitialized = true;
    };
    /**
     * Main rendering function for the calendar canvas called
     * on init load, and week-to-week nav
     * @param vS Date, start of the view range
     * @param vE Date, end of the view range
     * @param cD Date, the current date on the client
     */
    this.render = function (vS, vE, cD) {
        var viewStart = vS;
        var viewEnd = vE;
        var currDate = cD;
        // Key container elements
        var monthHeaderNode = null;
        var timelineNode = null;
        var hoursNode = null;
        var dayNameHeadersNode = null;
        var allDayColsNode = null;

        /**
         * Shows list of days at the head of each column in the week view
         * Uses the Date.abbrWeekday array of names in date.js
         * @return Boolean, true.
         */
        function showDayNameHeaders() {
            var str = '';
            var start = HOUR_LISTING_WIDTH + 1;
            var idstr = '';
            var calcDay = null;

            // Spacer to align with the timeline that displays hours below
            // for the timed event canvas
            str += '<div id="dayListSpacer" class="dayListDayDiv"' +
                ' style="left:0px; width:' +
                (HOUR_LISTING_WIDTH - 1) + 'px; height:' +
                (DAY_LIST_DIV_HEIGHT-1) +
                'px;">&nbsp;</div>';

            // Do a week's worth of day cols with day name and date
            for (var i = 0; i < 7; i++) {
                calcDay = cosmo.datetime.Date.add(viewStart, dojo.date.dateParts.DAY, i);
                // Subtract one pixel of height for 1px border per retarded CSS spec
                str += '<div class="dayListDayDiv" id="dayListDiv' + i +
                    '" style="left:' + start + 'px; width:' + (self.dayUnitWidth-1) +
                    'px; height:' + (DAY_LIST_DIV_HEIGHT-1) + 'px;';
                str += '">';
                str += cosmo.datetime.abbrWeekday[i] + '&nbsp;' + calcDay.getDate();
                str += '</div>\n';
                start += self.dayUnitWidth;
            }
            dayNameHeadersNode.innerHTML = str;
            return true;
        }

        /**
         * Draws the day columns in the resizeable all-day area
         * @return Boolean, true.
         */
        function showAllDayCols() {
            var str = '';
            var start = 0;
            var idstr = ''
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());

            for (var i = 0; i < 7; i++) {
                str += '<div class="allDayListDayDiv" id="allDayListDiv' + i +
                '" style="left:' + start + 'px; width:' +
                (cosmo.view.cal.canvas.dayUnitWidth-1) + 'px;">&nbsp;</div>';
                start += cosmo.view.cal.canvas.dayUnitWidth;
            }
            str += '<br style="clear:both;"/>';
            allDayColsNode.innerHTML = str;
            return true;
        }

        /**
         * Draws the 12 AM to 11 PM hour-range in each day column
         * @return Boolean, true.
         */
        function showHours() {
            var start = 0;
            var idstr = '';
            var hour = 0;
            var meridian = '';
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());
            var viewDiv = null;
            var timeLineWidth = 0;
            var workingHoursBarWidth = 3;

            // Subtract one px for border per asinine CSS spec
            var halfHourHeight = (HOUR_UNIT_HEIGHT/2) - 1;

            viewDiv = timelineNode;
            timeLineWidth = parseInt(viewDiv.offsetWidth);
            // Subtract 1 for 1px border
            timeLineWidth = timeLineWidth - workingHoursBarWidth - 1;

            var str = '';
            var row = '';
            // Timeline of hours on left
            for (var j = 0; j < 24; j++) {
                hour = j == 12 ? _('App.Noon') : cosmo.datetime.util.hrMil2Std(j);
                meridian = j > 11 ? ' PM' : ' AM';
                meridian = j == 12 ? '' : '<span>' + meridian + '</span>';
                row = '';

                // Upper half hour
                // ==================
                row += '<div class="hourDivTop';
                row += '" style="height:' +
                    halfHourHeight + 'px; width:' +
                    timeLineWidth + 'px; float:left;">';
                // Hour plus AM/PM
                row += '<div class="hourDivSubLeft">' + hour +
                    meridian + '</div>';
                row += '</div>\n';
                row += '<br class="clearAll"/>'

                idstr = i + '-' + j + '30';

                // Lower half hour
                // ==================
                row += '<div class="hourDivBottom"';
                // Make the noon border thicker
                if (j == 11) {
                    row += ' style="height:' + (halfHourHeight-1) +
                        'px; border-width:2px;';
                }
                else {
                    row += ' style="height:' + halfHourHeight + 'px;';
                }
                row += ' width:' + timeLineWidth +
                    'px; float:left;">&nbsp;</div>\n';
                row += '<br class="clearAll"/>'

                str += row;
            }
            viewDiv.innerHTML = str;

            str = '';
            viewDiv = hoursNode;

            // Do a week's worth of day cols with hours
            for (var i = 0; i < 7; i++) {
                calcDay = cosmo.datetime.Date.add(viewStart, dojo.date.dateParts.DAY, i);
                str += '<div class="dayDiv" id="dayDiv' + i +
                    '" style="left:' + start + 'px; width:' +
                    (cosmo.view.cal.canvas.dayUnitWidth-1) +
                    'px;"';
                str += '>';
                for (var j = 0; j < 24; j++) {
                    idstr = i + '-' + j + '00';
                    row = '';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivTop" style="height:' + halfHourHeight + 'px;">';
                    row += '</div>\n';
                    idstr = i + '-' + j + '30';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivBottom" style="';
                    if (j == 11) {
                        row += 'height:' + (halfHourHeight-1) +
                            'px; border-width:2px;';
                    }
                    else {
                        row += 'height:' + halfHourHeight + 'px;';
                    }
                    row += '">&nbsp;</div>';
                    str += row;
                }
                str += '</div>\n';
                start += cosmo.view.cal.canvas.dayUnitWidth;
            }

            viewDiv.innerHTML = str;
            return true;
        }
        /**
         * Displays the month name at the top
         */
        function showMonthHeader() {
            return true;
            var vS = viewStart;
            var vE = viewEnd;
            var mS = vS.getMonth();
            var mE = vE.getMonth();
            var headerDiv = monthHeaderNode;
            var str = '';

            // Format like 'March-April, 2006'
            if (mS < mE) {
                str += dojo.date.strftime(vS, '%B-');
                str += dojo.date.strftime(vE, '%B %Y');
            }
            // Format like 'December 2006-January 2007'
            else if (mS > mE) {
                str += dojo.date.strftime(vS, '%B %Y-');
                str += dojo.date.strftime(vE, '%B %Y');
            }
            // Format like 'April 2-8, 2006'
            else {
                str += dojo.date.strftime(vS, '%B %Y');
            }
            if (headerDiv.firstChild) {
                headerDiv.removeChild(headerDiv.firstChild);
            }
            headerDiv.appendChild(document.createTextNode(str));
        }
        function setCurrentDayStatus() {
            // 'Today' is in the displayed view range
            var currDate = cosmo.app.pim.currDate;
            var currDateTime = currDate.getTime();
            var currDateDay = currDate.getDay();
            var currDayClass = '';
            var currDayImg = '';
            var currDayIdPrefix = 'hourDiv' + currDateDay + '-';
            if (cosmo.view.cal.viewStart.getTime() <= currDateTime &&
                cosmo.view.cal.viewEnd.getTime() > currDateTime) {
                currDayClass = ' currentDayDay';
                currDayImg = 'url(' + cosmo.env.getImageUrl('day_col_header_background.gif')+')';
            }
            else {
                currDayClass = '';
                currDayImg = '';
            }
            // Set background image or set to flat white for day name
            var d = $('dayListDiv' + currDateDay);
            d.style.backgroundImage = currDayImg;
            d.style.backgroundRepeat = 'repeat-x';
            d.style.backgroundPosition = '0px 0px';
            // Set gray or white background for all-day area
            $('allDayListDiv' + currDateDay).className = 'allDayListDayDiv' + currDayClass;
            // Reset the CSS class on all the rows in the 'today' col
            // Either highlight with gray, or reset to white
            for (var i = 0; i < 24; i++) {
                $(currDayIdPrefix + i + '00').className = 'hourDivTop' + currDayClass;
                $(currDayIdPrefix + i + '30').className = 'hourDivBottom' + currDayClass;
            }
        }
        
        // -----------
        // Do it!
        // -----------
        // Init and call all the rendering functions
        // Link local variables to DOM nodes
        monthHeaderNode = $('monthHeaderDiv');
        timelineNode = $('timedHourListDiv');
        dayNameHeadersNode = $('dayListDiv');
        hoursNode = $('timedContentDiv');
        allDayColsNode = $('allDayContentDiv');
        showMonthHeader();
        showDayNameHeaders();
        showAllDayCols();

        // On the first time rendering happens -- must happen
        // after base render
        if (!hasBeenRendered) {
            // Make the all-day event area resizeable
            // Create event listeners
            dojo.event.connect(hoursNode, 'onmousedown', mouseDownHandler);
            dojo.event.connect(allDayColsNode, 'onmousedown', mouseDownHandler);
            dojo.event.connect(hoursNode, 'ondblclick', dblClickHandler);
            dojo.event.connect(allDayColsNode, 'ondblclick', dblClickHandler);
            dojo.event.connect(hoursNode, 'oncontextmenu', self, '_handleContextForCanvasOnly') 
            dojo.event.connect(allDayColsNode, 'oncontextmenu', self, '_handleContextForCanvasOnly');
            // Get a reference to the main scrolling area for timed events;
            this.timedCanvas = $('timedCanvas');
            // Refs for appending lozenges to avoid lookup by id
            timedCanvasContentArea = $('timedContentDiv');
            untimedCanvasContentArea = $('allDayContentDiv');
        }
        allDayArea = new cosmo.ui.resize_area.ResizeArea(
            $('allDayResizeMainDiv'), $('allDayResizeHandleDiv'));
        allDayArea.init('down');
        allDayArea.addAdjacent($('timedCanvas'));
        showHours();
        setCurrentDayStatus();

        // Render event lozenges
        loadSuccess();
        // HACK: IE6's pixel-calc for 'height: 100%' gets reset
        // if you touch innerHTML on the DOM node -- the resize
        // drop method will reset this to a valid pixel height
        if (navigator.appVersion.indexOf('MSIE 6') > -1) {
            allDayArea.drop();
        }
        // Set rendered flag, used for initial-render-only functions
        hasBeenRendered = true;
    };
    this.saveTimedCanvasScrollOffset = function () {
        if (this.timedCanvas) {
            this.timedCanvasScrollTop = this.timedCanvas.scrollTop;
        }
    };
    this.resetTimedCanvasScrollOffset = function () {
        if (this.timedCanvas) {
            this.timedCanvas.scrollTop = this.timedCanvasScrollTop;
        }
    };
    /**
     * Get the scroll offset for the timed canvas
     * @return Number, the pixel position of the top of the timed
     * event canvas, including the menubar at the top, resizing of
     * the all-day area, and any amount that the timed canvas
     * has scrolled.
     */
    this.getTimedCanvasScrollTop = function () {
        // Has to be looked up every time, as value may change
        // either when user scrolls or resizes all-day event area
        var top = this.timedCanvas.scrollTop;
        // Subtract change, if any, in resized all-day event area
        var offset = cosmo.ui.resize_area.dragSize ?
            (cosmo.ui.resize_area.dragSize - ALL_DAY_RESIZE_AREA_HEIGHT) : 0;
        top -= offset;
        // Subtract height of navbar -- this lives outside the cal view
        top -=  CAL_TOP_NAV_HEIGHT;
        return top;
    };
    /**
     * Figures out the date based on Y-pos of left edge of event lozenge
     * with respect to canvas (scrollable div 'timedCanvas').
     * @param point Left edge of dragged event lozenge after snap-to.
     * @return A Date object
     */
    this.calcDateFromPos = function (point) {
        var col = parseInt(point/cosmo.view.cal.canvas.dayUnitWidth); // Number 0-6 -- day in the week
        var posdate = calcDateFromIndex(col);
        return posdate;
    };
    /**
     * Figures out the hour based on X-pos of top and bottom edges of event lozenge
     * with respect to canvas (scrollable div 'timedCanvas').
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
     * @param milTime time string in military time format
     * @param posOrientation string ('start' or 'end'), whether the position
     *    in question is for the start or end of the lozenge. This is for handling
     *    the special case of 12am being both the start and end of the day.
     * @return An integer of the X-position for the top/bottom edge of an event lozenge
     */
    this.calcPosFromTime = function (milTime, posOrientation) {
        var t = cosmo.datetime.util.parseTimeString(milTime,
            { returnStrings: true });
        var h = t.hours;
        var m = t.minutes;
        var pos = 0;
        // Handle cases where midnight is the end of the timeline
        // instead of the beginning
        // In those cases, it's logically 24:00 instead of 0:00
        if (h == 0 && posOrientation == 'end') {
            h = 24;
        }
        pos += (h*HOUR_UNIT_HEIGHT);
        pos += ((m/60)*HOUR_UNIT_HEIGHT);
        pos = parseInt(pos);
        return pos;
    };
    this.getCanvasAreaByLozengeType = function (lozengeType) {
        var types = cosmo.view.cal.lozenge.lozengeTypes;
        var r = null;
        if (lozengeType == types.TIMED) {
            return timedCanvasContentArea;
        }
        else if (lozengeType == types.UNTIMED) {
            return untimedCanvasContentArea;
        }
        else {
            throw(lozengeType + 'is not a valid lozenge type.');
        }
    };
    this.handleSelectionChange = function (e, id, discardUnsavedChanges) {
        var args = Array.prototype.slice.call(arguments);
        var s = getIndexEvent(id);
        var item = cosmo.view.cal.itemRegistry.getItem(s);
        // If this object is currently in 'processing' state, discard any input
        if (item.lozenge.getInputDisabled()) {
            return false;
        }
        var writeable = cosmo.app.pim.getSelectedCollectionWriteable();
        var c = cosmo.view.cal.canvas;
        var origSelection = c.getSelectedItem();

        // If no currently selected item, or the item clicked
        // is not the currently selected item, update the selection
        // Since there may not be an existing selection, check for
        // the non-existence first, then compare to the item if it
        // exists
        if ((!origSelection) || (origSelection.id != item.id)) {
            // Make sure the user isn't leaving unsaved edits --
            // blow by this when re-called with explicit 'discard changes'
            // Note: we have to spoon-feed the execution context to the
            // callback methods for the buttons in the dialog, hence
            // passing the 'self' param below
            if (!discardUnsavedChanges &&
                origSelection &&
                !origSelection.lozenge.getInputDisabled() && // Ignore if orig. item is already in 'processing' state
                writeable) {
                // Add the explicit ignore flag to the args
                args.push(true);
                // Discarding just re-invokes this call with the ignore flag
                var discardFunc = function () {
                    self.handleSelectionChange.apply(self, args);
                };
                if (!cosmo.view.handleUnsavedChanges(origSelection, discardFunc)) {
                    return false;
                }
            }

            // Call setSelectedCalItem here directly, and publish the
            // selected-item message on a setTimeout to speed up UI
            // response for direct clicks -- publishing 'setSelected'
            // will cause setSelectedCalItem to be called a second time
            // as the canvas responds to this message, but it doesn't
            // hurt to re-select the currently selected item
            self.setSelectedCalItem(item);
            var f = function () {
                dojo.event.topic.publish('/calEvent', {
                    'action': 'setSelected', 'data': item });
            }
            setTimeout(f, 0);
        }

        // No move/resize after displaying the unsaved changes
        // dialog, as it interferes with the normal flow of setting
        // up a draggable, also naturaly no move/resize for
        // read-only collections
        if (!discardUnsavedChanges && writeable) {
            // Right-click -- context menu
            if (e.button == 2) {
                  cosmo.ui.menu.HierarchicalMenuManager.showContextMenu(e,
                      cosmo.view.contextMenu.menu);
                  return false;
            }
            // Left-click proceed to possible move/resize
            else {
                // Set up Draggable and save dragMode -- user may be dragging
                if (id.indexOf('AllDay') > -1) {
                    dragItem = new cosmo.view.cal.draggable.NoTimeDraggable(s);
                }
                else {
                    dragItem = new cosmo.view.cal.draggable.HasTimeDraggable(s);
                }

                switch(true) {
                    // Main content area -- drag entire event
                    case id.indexOf('Content') > -1:
                    case id.indexOf('Title') > -1:
                    case id.indexOf('Start') > -1:
                        dragItem.init('drag', item);
                        break;
                    // Top lip -- resize top
                    case id.indexOf('Top') > -1:
                        dragItem.init('resizetop', item);
                        break;
                    // Bottom lip -- resize bottom
                    case id.indexOf('Bottom') > -1:
                        dragItem.init('resizebottom', item);
                        break;
                    default:
                        // Do nothing
                        break;
                }
                // Set the Cal draggable to the dragged lozenge
                cosmo.app.dragItem = dragItem;
            }
        }
    };
    /**
     * Set the passed calendar event as the selected one on
     * canvas
     * @param p CalItem object or CalItem id string, the event to select
     */
    this.setSelectedCalItem = function (/* Can be CalItem object or String id */ p) {
        // Bail out if called without a proper param passed
        // this can happen if this method is called by blindly
        // passing the result of a function without checking
        // to see if it's acutally returning a value
        if (!p){
            return;
        }

        if (typeof p == 'string') {
            sel = this.view.itemRegistry.getItem(p);
        }
        else {
            sel = p;
        }
        // Selected item is in the currently selected collection
        if (sel.isInSelectedCollection()) {
            // Deselect previously selected event if any
            var origSel = self.getSelectedItem();
            if (origSel) {
                if (origSel == sel) { return; }
                origSel.lozenge.setDeselected();
            }
        }
        // Selected item is in one of the background collections
        // in the overlay -- change this to the selected collection,
        // and foreground its lozenges
        else {
            // Remove the originally selected collection from
            // the display unless it's explicitly overlaid
            // FIXME: similar logic exists in handleClick of
            // cosmo.ui.selector. This should be refactored
            // into a method of some kind of abstracted UI-only
            // collection object
            var currColl = cosmo.app.pim.getSelectedCollection();
            var ch = $('collectionSelectorItemCheck_' + currColl.getUid());
            if (!ch || (ch && !ch.checked)) {
                currColl.doDisplay = false;
            }
            // Re-render the view, with the newly selected collection
            var collId = sel.primaryCollectionId ?
                sel.primaryCollectionId : sel.collectionIds[0];
            var coll = cosmo.app.pim.collections.getItem(collId);
            cosmo.view.displayViewFromCollections(coll);
        }
        // Pass the item or id, set as the selected item
        // for the current collection
        self.setSelectedItem(sel);

        // Show the associated lozenge as selected
        sel.lozenge.setSelected();
    };
    /**
     * Handle events published on the '/calEvent' channel, including
     * self-published events
     * @param cmd A JS Object, the command containing orders for
     * how to handle the published event.
     */
    this.handlePub_calEvent = function (cmd) {
        if (!cosmo.view.cal.isCurrentView()) { return false; }
        var act = cmd.action;
        var opts = cmd.opts;
        var data = cmd.data;
        switch (act) {
            case 'eventsLoadSuccess':
                cosmo.view.cal.itemRegistry = data;
                var c = cosmo.app.pim.baseLayout.mainApp.centerColumn.calCanvas;
                // Update viewStart, viewEnd from passed opts
                for (var n in opts) { c[n] = opts[n]; }
                c.render();
                cosmo.app.hideMask();
                break;
            case 'setSelected':
                var ev = cmd.data;
                self.setSelectedCalItem(ev);
                break;
            case 'save':
                setLozengeProcessing(cmd);
                break;
            case 'saveFailed':
                var ev = cmd.data;
                // If the failure was a new event, remove
                // the placeholder lozenge
                if (cmd.saveType == "new") {
                    removeEvent(ev);
                }
                // Otherwise put it back where it was and
                // restore to non-processing state
                else {
                    var rEv = null;
                    // Recurrence, 'All events'
                    if (cmd.saveType == 'recurrenceMaster' ||
                        cmd.saveType == 'instanceAllFuture') {
                        // Edit ocurring from one of the instances
                        if (opts.instanceEvent) {
                            rEv = cmd.instanceEvent
                        }
                        // Edit occurring from the actual master
                        else {
                            rEv = ev;
                        }
                    }
                    // Single event
                    else {
                        var rEv = ev;
                    }
                    restoreEvent(rEv);
                    rEv.lozenge.setInputDisabled(false);
                }
                break;
            case 'saveSuccess':
                if (cmd.data.data.getEventStamp()){
                    saveSuccess(cmd);
                }
                else {
                    removeRecurrenceChain(cmd.data.data.getUid());
                }
                break;
            case 'remove':
                // Show 'processing' state here
                setLozengeProcessing(cmd);
                break;
            case 'removeSuccess':
                var ev = cmd.data;
                removeSuccess(ev, opts)
                break;
            case 'removeFailed':
                break;
            default:
                // Do nothing
                break;
        }
    };

    this.handlePub_app = function (cmd) {
        if (!cosmo.view.cal.isCurrentView()) { return false; }

        var t = cmd.type;
        switch (t) {
            case 'modalDialogToggle':
                // Showing the modal dialog box: remove scrolling in the timed
                // event div below (1. Firefox Mac, the scrollbar uses a native
                // wigdet and shows through the dialog box. 2. Firefox on all
                // plaforms, overflow of 'auto' in underlying divs causes
                // carets/cursors in textboxes to disappear. This is a verified
                // Mozilla bug: https://bugzilla.mozilla.org/show_bug.cgi?id=167801
                if (typeof this.timedCanvas != 'undefined' && this.timedCanvas) {
                    this.timedCanvasScrollTop = this.timedCanvas.scrollTop; // Preserve the scroll offset
                    if (cmd.isDisplayed) {
                        if (dojo.render.html.mozilla) {
                            this.timedCanvas.style.overflow = "hidden";
                        }
                    }
                    else {
                       if (dojo.render.html.mozilla) {
                           this.timedCanvas.style.overflow = "auto";
                           this.timedCanvas.style.overflowY = "auto";
                           this.timedCanvas.style.overflowX = "hidden";
                       }
                    }
                    this.timedCanvas.scrollTop = this.timedCanvasScrollTop;
                }
                break;
        }
    };

    // Private methods
    // ****************
    this._handleContextForCanvasOnly = function (e) {
        var elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
        var id = elem.id;
        if (id.indexOf('hourDiv') == -1) {
            e.stopPropagation();
            e.preventDefault();
        }
    };
    /**
     * Removes an event lozenge from the canvas -- called in three cases:
     * (1) Actually removing an event from the calendar (this gets
     *     called after the backend successfully removes it)
     * (2) Removing an event from view because it's been edited
     *     to dates outside the viewable span
     * (3) Removing the placeholder event when initial event
     *     creation fails
     * Likely called in a loop with the Hash's 'each' method
     * @param id String, id of the event to be removed
     * @param ev CalItem obj, the event to be removed
     */
    function removeEventFromDisplay(id, ev) {
        var selEv = self.getSelectedItem();
        // Remove the lozenge
        ev.lozenge.remove();
        // Remove selection if need be
        if (selEv && (selEv.id == ev.id)) {
            self.clearSelectedItem();
        }
        return true;
    }

    /**
     * Remove a cal event object, usually removes the event
     * lozenge as well
     * @param ev CalItem object, the event to select
     */
    function removeEvent(item) {
        var currColl = cosmo.app.pim.getSelectedCollection();
        cosmo.view.cal.removeItemFromCollectionRegistry(item, currColl);
        if (item.collectionIds.length) {
            item.lozenge.setInputDisabled(false);
            item.lozenge.setDeselected();
            item.lozenge.updateDisplayMain();
        }
        else {
            cosmo.view.cal.itemRegistry.removeItem(item.id);
            removeEventFromDisplay(item.id, item);
        }
    }

    function removeRecurrenceChain(id){
        var selectedCollection = cosmo.app.pim.getSelectedCollection();
        var registry = cosmo.view.cal.collectionItemRegistries[selectedCollection.getUid()];
        var clone = registry.clone();
        clone.each(function(currentId, item){
            if (item.data.getUid() == id){
                registry.removeItem(id);
                removeEventFromDisplay(item.id, item);
            }
        });
    }

    /**
     * Clear the entire itemRegistry, usually clear the
     * lozenges from the canvas as well
     */
    function removeAllEventsFromDisplay() {
        cosmo.view.cal.itemRegistry.each(removeEventFromDisplay);
        return true;
    }
    /**
     * Append an calendar event lozenge to the canvas -- likely
     * called in a loop with the Hash's 'each' method
     * @param key String, the Hash key for the itemRegistry
     * @param val CalItem obj, value in the itemRegistry
     * for the event getting added to the canvas
     */
    function appendLozenge(key, val) {
        var id = key;
        var ev = val;
        var eventStamp = ev.data.getEventStamp();
        var allDay = eventStamp.getAllDay();
        var anyTime = eventStamp.getAnyTime();
        var types = cosmo.view.cal.lozenge.lozengeTypes;
        var t = (allDay || anyTime) ? types.UNTIMED : types.TIMED;
        // Create the lozenge and link it to the event
        ev.lozenge = cosmo.view.cal.lozenge.createNewLozenge(id, t);
        ev.lozenge.setUpDomAndAppend(ev.id,
            self.getCanvasAreaByLozengeType(t));
    }
    /**
     * Main function for rendering/re-rendering the cal canvas
     * @ return Boolean, true
     */
    function updateEventsDisplay() {
        // Current collection has items
        if (cosmo.view.cal.itemRegistry.length) {
            if (cosmo.view.cal.conflict.calc(cosmo.view.cal.itemRegistry) &&
                positionLozenges() &&
                foregroundSelectedCollection()) {
                // If the selected item is not in the on-canvas itemRegistry,
                // pull the copy from the selectedItemCache
                var sel = self.getSelectedItem() || self.getSelectedItemCacheCopy();
                dojo.event.topic.publish('/calEvent', { 'action':
                    'eventsDisplaySuccess', 'data': sel });
            }
        }
        // No items displayed in the current collection
        else {
            dojo.event.topic.publish('/calEvent', { 'action': 'noItems' });
        }
        return true;
    }
    /**
     * Call positionLozenges in a loop with Hash's 'each' method
     */
    function positionLozenges() {
        return cosmo.view.cal.itemRegistry.each(positionLozenge);
    };
    /**
     * Position the lozenge on the canvase based on the
     * Item props -- happens after they're put on the canvas
     * with appendLozenge. Called in a loop with Hash's 'each' method
     * @param key String, the Hash key for the event in the
     * itemRegistry
     * @param val CalItem object, the value in the Hash
     */
    function positionLozenge(id, item) {
        item.lozenge.updateFromEvent(item);
        item.lozenge.updateDisplayMain();
    }
    function foregroundSelectedCollection() {
        var selCollId = cosmo.app.pim.getSelectedCollectionId();
        var selColl = cosmo.view.cal.collectionItemRegistries[selCollId];
        var selItem = self.getSelectedItem();
        var f = function(id, item) {
            if (item == selItem) {
                item.lozenge.domNode.style.zIndex = 25;
            }
            else {
                item.lozenge.domNode.style.zIndex = 10;
            }
        }
        selColl.each(f);
        return true;
    }
    /**
     * Restores a cal event to it's previous state after:
     * (1) a user cancels an edit
     * (2) an update operation fails on the server
     * Restores the CalItem from the backup snapshot, and
     * returns the lozenge to its previous position
     * @param ev CalItem object, the event to restore
     */
    function restoreEvent(ev) {
        if (ev.restoreFromSnapshot()) {
            ev.lozenge.updateFromEvent(ev);
            ev.lozenge.updateDisplayMain();
        }
    }
    /**
     * Convenience method for wiping the cal canvas. Also
     * removes the current event selection
     */
    function wipe() {
        removeAllEventsFromDisplay();
        cosmo.view.cal.itemRegistry = new cosmo.util.hash.Hash();
    }
    /**
     * Render the canvas after successfully loading events
     * from the server -- called for initial load, and for
     * week-to-week navigation
     * @param ev Hash, the itemRegistry of loaded events
     */
    function loadSuccess(ev) {
        if (ev) {
            removeAllEventsFromDisplay();
            cosmo.view.cal.itemRegistry = ev;
        }
        cosmo.view.cal.itemRegistry.each(appendLozenge);
        // Update the view
        updateEventsDisplay();
    }
    /**
     * Handles a successful update of a CalEvent item
     * @param cmd JS Object, the command object passed in the
     * published 'success' message
     */
     function saveSuccess(cmd) {
        var recurOpts = cosmo.view.service.recurringEventOptions;
        var item = cmd.data
        var data = item.data;
        var saveType = cmd.saveType || null;
        dojo.debug("saveSuccess saveType: " + saveType);
        var delta = cmd.delta;
        var deferred = null;
        var newItemNote = cmd.newItemNote; // stamped Note
        var recurrenceRemoved = item.recurrenceRemoved();

        //if the event is recurring and all future or all events are changed, we need to
        //re expand the event
        if (item.data.hasRecurrence() && saveType != recurOpts.ONLY_THIS_EVENT) {
            //first remove the event and recurrences from the registry.
            var idsToRemove = [data.getUid()];
            var collectionIds = item.collectionIds.slice();
            if (saveType == recurOpts.ALL_FUTURE_EVENTS){
                idsToRemove.push(newItemNote.getUid());
            }

            cosmo.view.cal.removeRecurrenceGroupFromItsCollectionRegistries(
                collectionIds, idsToRemove);

            //now we have to expand out the item for the viewing range
            var expandDeferred1 = cosmo.app.pim.serv.expandRecurringItem(data.getMaster(),
                cosmo.view.cal.viewStart,cosmo.view.cal.viewEnd)
            var deferredArray = [expandDeferred1];
            if (saveType == recurOpts.ALL_FUTURE_EVENTS) {
              deferredArray.push(cosmo.app.pim.serv.expandRecurringItem(newItemNote,
                cosmo.view.cal.viewStart,cosmo.view.cal.viewEnd));
            }
            deferred = new dojo.DeferredList(deferredArray);

            var addExpandedOccurrences = function (results) {
                //check for errors!
                var error = cosmo.util.deferred.getFirstError(results);

                if (error){
                    cosmo.app.showErr(_$("Service.Error.ProblemGettingItems"), "", error);
                    return;
                }

                var occurrences = results[0][1];
                if (results[1]){
                    var otherOccurrences = results[1][1];
                    occurrences = occurrences.concat(otherOccurrences);
                }
                //var newHash = cosmo.view.cal.createEventRegistry(occurrences);
                //newRegistry.append(newHash);

                cosmo.view.cal.placeRecurrenceGroupInItsCollectionRegistries(
                    collectionIds, occurrences);

                removeAllEventsFromDisplay();
                self.view.itemRegistry =
                    cosmo.view.cal.createItemRegistryFromCollections();
                //self.view.itemRegistry = newRegistry;
                self.view.itemRegistry.each(appendLozenge);
            };
            deferred.addCallback(addExpandedOccurrences);
        }
        // Non-recurring (normal single item, recurrence removal), "only this item'
        else {
            // The id for the current collection -- used in creating new CalItems
            var currCollId = cosmo.app.pim.getSelectedCollectionId();
            // The item just had its recurrence removed.
            // The only item that should remain is the item that was the
            // first occurrence -- put that item on the canvas, if it's
            // actually in the current view-span
            if (recurrenceRemoved) {
                // Remove all the recurrence items from the list
                var newRegistry = self.view.filterOutRecurrenceGroup(
                    self.view.itemRegistry.clone(), [item.data.getUid()]);
                // Wipe existing list of items off the canvas
                removeAllEventsFromDisplay();
                // Update the list
                self.view.itemRegistry = newRegistry;
                // Create a new item based on the updated version of
                // the edited ocurrence's master
                var note = item.data.getMaster();
                var newItem = new cosmo.view.cal.CalItem(note, item.collectionIds.slice());
                // If the first item in the removed recurrence series
                // is in the current view span, add it to the list
                if (!newItem.isOutOfViewRange()) {
                    self.view.itemRegistry.setItem(newItem.id, newItem);
                    cosmo.view.cal.placeItemInItsCollectionRegistries(newItem);
                }
                // Repaint the updated list
                self.view.itemRegistry.each(appendLozenge);
                updateEventsDisplay();
                return;
            }
            // Single item, "only this item" change for a recurrence
            else {
                // Saved event is in the current view slice
                var inRange = !item.isOutOfViewRange();
                // Lozenge is in the current week, update it
                if (inRange) {
                    // Item being edited was off-canvas
                    if (item.lozenge.isOrphaned()){
                        var id = item.data.getItemUid();
                        // Create a new CalItem from the stamped Note on the item
                        // so we can give it a new on-canvas lozenge
                        var newItem = new cosmo.view.cal.CalItem(item.data, item.collectionIds.slice());
                        self.view.itemRegistry.setItem(newItem.id, newItem);
                        cosmo.view.cal.placeItemInItsCollectionRegistries(newItem);
                        // Repaint the updated list
                        self.view.itemRegistry.each(appendLozenge);
                        updateEventsDisplay();
                    }
                    else {
                        item.lozenge.setInputDisabled(false);
                        item.lozenge.updateDisplayMain();
                        // 'Only this event' change to a recurrence
                        // Unlock all the other items in the series
                        // from their 'processing' state
                        if (item.data.hasRecurrence()) {
                            var f = function (i, e) {
                                if (e.data.getUid() == item.data.getUid()) {
                                    e.lozenge.setInputDisabled(false);
                                    e.lozenge.updateDisplayMain();
                                }
                            }
                            cosmo.view.cal.itemRegistry.each(f);
                        }
                    }
                }
                // Lozenge was in view, event was explicitly edited
                // to a date that moves the lozenge off-canvas
                else if (cmd.qualifier && cmd.qualifier.offCanvas) {
                    removeEvent(item);
                }
                // User has navigated off the week displaying the currently
                // selected item -- the item is not in the itemRegistry,
                // it's being pulled from the selectedItemCache, so it does
                // not have a lozenge on the canvas to update -- the only
                // drawback here is that the user now gets no feedback that
                // the item has been successfully updated, because there's
                // no lozenge to see
                else if (item.lozenge.isOrphaned()) {
                    // Do nothing
                }
            }
        }

        var updateEventsCallback = function () {
            dojo.debug("updateEventsCallback")
            // Don't re-render when requests are still processing
            if (!cosmo.view.service.processingQueue.length) {
                updateEventsDisplay();

                // Anything except editing an existing event requires
                // adding the selection to an item in the itemRegistry
                if (saveType) {
                    var sel = null;
                    switch (saveType) {
                        case 'new':
                            sel = item;
                            sel.lozenge.setInputDisabled(false);
                            break;
                        case recurOpts.ALL_EVENTS:
                        case recurOpts.ONLY_THIS_EVENT:
                            sel = item.data.getItemUid();
                            break;
                        case recurOpts.ALL_FUTURE_EVENTS:
                            sel = newItemNote.getNoteOccurrence(
                                newItemNote.getEventStamp().getStartDate()).getItemUid();
                            break;
                            break;
                        default:
                            throw('Undefined saveType of "' + saveType +
                                '" in command object passed to saveSuccess');
                            break;

                    }
                    self.setSelectedCalItem(sel);
                    sel = self.getSelectedItem();
                    dojo.event.topic.publish('/calEvent', { action: 'setSelected',
                        saveType: saveType, data: sel });
                }
            }
            else {
                dojo.debug("how many left in queue: " + cosmo.view.service.processingQueue.length);
            }
        }

        if (deferred){
            deferred.addCallback(updateEventsCallback);
        }
        else {
            updateEventsCallback();
        }
    };

    /**
     * Handles a successful removal of an event
     * @param ev CalItem object, the removed event
     * @param opts JS Object, options for the removal that
     * tell you what kind of remove is happening
     */
    function removeSuccess(item, opts) {
        var recurOpts = cosmo.view.service.recurringEventOptions;
        var removeType = opts.removeType;
        dojo.debug("removeSuccess, removeType: " + removeType);

        // If the user has navigated off the week displaying the
        // current selected item, it's not in the itemRegistry,
        // it's being pulled from selectedItemCache, so its Lozenge
        // object has been 'orphaned' -- the DOM node is not on
        // the currently displayed canvas
        // just send the 'clear selected' message
        if (item.lozenge.isOrphaned()) {
            dojo.event.topic.publish('/calEvent', { 'action':
                'clearSelected', 'data': null });
            return;
        }
        if (item.data.hasRecurrence()) {
            if (removeType == recurOpts.ONLY_THIS_EVENT) {
                removeEvent(item);
                // 'Only this event' removal from a recurrence
                // Unlock all the other items in the series
                // from their 'processing' state
                var f = function (i, e) {
                    if (e.data.getUid() == item.data.getUid()) {
                        e.lozenge.setInputDisabled(false);
                        e.lozenge.updateDisplayMain();
                    }
                }
                cosmo.view.cal.itemRegistry.each(f);
            }
            else {
                removeAllEventsFromDisplay();
                var currColl = cosmo.app.pim.getSelectedCollection();
                var dt = removeType == recurOpts.ALL_FUTURE_EVENTS ?
                    item.data.getEventStamp().getRrule().getEndDate() : null;
                cosmo.view.cal.removeRecurrenceGroupFromCollectionRegistry(
                    currColl, [item.data.getUid()], { dateToBeginRemoval: dt });
                self.view.itemRegistry =
                    cosmo.view.cal.createItemRegistryFromCollections();
                self.view.itemRegistry.each(appendLozenge);
            }
        }
        else {
            removeEvent(item);
        }
        self.clearSelectedItem();
        dojo.event.topic.publish('/calEvent', { 'action':
            'clearSelected', 'data': null });
        updateEventsDisplay();
    }

    /**
     * Single-clicks -- do event dispatch based on ID of event's
     * DOM-element source. Includes selecting/moving/resizing event lozenges
     * and resizing all-day event area
     * Mousedown on an event always creates a Draggable in anticipation
     * of a user dragging the lozenge. Draggable is destroyed on mouseup.
     * cosmo.app.dragItem may either be set to this Draggable, or to
     * the ResizeArea for all-day events
     */
    function mouseDownHandler(e) {
        var id = '';
        var dragItem = null;
        var elem = null;
        var item = null;
        var s = '';
        var elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
        var id = elem.id;
        // ======================================
        // Event dispatch
        // ======================================
        switch (true) {
            // Mouse down on the hour columns -- exit to prevent text selection
            case (id.indexOf('hourDiv') > -1):
                return false;
                break;

            // On event lozenge -- simple select, or move/resize
            case (id.indexOf('eventDiv') > -1):
                // Get the clicked-on event
                self.handleSelectionChange(e, id);
                break;
        }
    }
    /**
     * Double-clicks -- if event source is in the scrolling area
     * for normal events, or in the resizeable all-day event area
     * calls createNewCalItem to create a new event
     */
    function dblClickHandler(e) {
        var id = '';
        var elem = null;
        var collection = cosmo.app.pim.getSelectedCollection();
        // User has no collections, show the user a nice error
        if (!collection) {
           cosmo.app.showErr(_('Main.Error.ItemNewSaveFailed'),
              _('Main.Error.NoCollectionsForItemSave'));
        }
        else {
            // Event creation only in write-mode
            if (collection.isWriteable()) {
                e = !e ? window.event : e;
                elem = cosmo.ui.event.handlers.getSrcElemByProp(e, 'id');
                id = elem.id

                switch (true) {
                    // On hour column -- create a new event
                    case (id.indexOf('hourDiv') > -1):
                    // On all-day column -- create new all-day event
                    case (id.indexOf('allDayListDiv') > -1):
                        createNewCalItem(id);
                        break;
                    // On event title -- edit-in-place
                    case (id.indexOf('eventDiv') > -1):
                        // Edit-in-place will go here
                        break;
                }
            }
        }
    }

    /**
     * Lozenge stuff
     */
    function setLozengeProcessing(cmd) {
        var ev = cmd.data;
        var qual = cmd.qualifier;
        var eventStamp = ev.data.getEventStamp();

        function doProcessing(rrule) {
            ev.lozenge.setInputDisabled(true);
            ev.lozenge.showProcessing();
            if (rrule) {
                var f = function (i, e) {
                    if (e.data.getUid() == ev.data.getUid()) {
                        e.lozenge.setInputDisabled(true);
                        e.lozenge.showProcessing();
                    }
                }
                cosmo.view.cal.itemRegistry.each(f);
            }
        }

        // If the user has navigated off the week displaying the
        // current selected item, it's not in the itemRegistry,
        // it's being pulled from selectedItemCache, so its Lozenge
        // object has been 'orphaned' -- the DOM node is not on
        // the currently displayed canvas
        if (ev.lozenge.isOrphaned()) { return false; }

        // If the edit removed the event stamp, just set the
        // lozenge to a processing state and wait for edit
        // to return to remove the lozenge from the canvas
        if (!eventStamp) {
            doProcessing();
            return true;
        }

        var startDate = eventStamp.getStartDate();
        var endDate = eventStamp.getEndDate();
        var allDay = eventStamp.getAllDay();
        var anyTime = eventStamp.getAnyTime();
        var rrule = eventStamp.getRrule();

        if (ev.dataOrig){
            var origEventStamp = ev.dataOrig.getEventStamp();
            var origStartDate = origEventStamp.getStartDate();
            var origEndDate = origEventStamp.getEndDate();
            var origAllDay = origEventStamp.getAllDay();
            var origAnyTime = origEventStamp.getAnyTime();
        }
        // Reset the lozenge because we may be changing to the new type
        // -- e.g., between all-day and normal, or between normal single
        // and normal composite
        if (ev.dataOrig &&
            !((allDay || anyTime) &&
            (origAllDay || origAnyTime))) {
            var types = cosmo.view.cal.lozenge.lozengeTypes;
            var t = (allDay || anyTime) ? types.UNTIMED : types.TIMED;
            // Remove the current lozenge and replace
            ev.lozenge.remove();
            ev.lozenge = cosmo.view.cal.lozenge.createNewLozenge(ev.id, t);
            ev.lozenge.setUpDomAndAppend(ev.id,
                self.getCanvasAreaByLozengeType(t));
        }
        // Reset the lozenge properties from the event
        ev.lozenge.updateFromEvent(ev, true);
        // Do visual updates to size, position, z-index
        ev.lozenge.updateElements();

        // Display processing animation
        doProcessing(rrule);
        return true;
    }

    /**
     * Insert a new calendar event -- called when
     * the user double-clicks on the cal canvas
     * @param id A string, the id of the div on the cal canvas double-clicked
     */
    function createNewCalItem(evParam) {
        dojo.debug("createNewCalItem 1");
        var item = null; // New event
        var evSource = '';
        var lozType = ''; // Lozenge type
        var types = cosmo.view.cal.lozenge.lozengeTypes;
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
        var note = new cosmo.model.Note();
        var id = note.getUid(); // Won't be recurring, don't need to use getItemUid
        var eventStamp = note.getEventStamp(true);

        // Create the CalItem obj, attach the Note obj
        // as .data with EventStamp, create the Lozenge
        // ================================
        lozType = (evParam.indexOf('allDayListDiv') > -1) ? types.UNTIMED : types.TIMED;
        evSource = 'click';
        // Set props based on when and what canvas was clicked
        if (lozType == types.TIMED) {
            dojo.debug("createNewCalItem 3");
            startstr = getIndexFromHourDiv(evParam);
            dayind = extractDayIndexFromId(startstr);
            evdate = calcDateFromIndex(dayind);
            yea = evdate.getFullYear();
            mon = evdate.getMonth();
            dat = evdate.getDate();
            startstr = extractTimeFromId(startstr);
            var t = cosmo.datetime.util.parseTimeString(startstr);
            hou = t.hours;
            min = t.minutes;
            start = new cosmo.datetime.Date(yea, mon, dat, hou, min);
            end = cosmo.datetime.Date.add(start, dojo.date.dateParts.MINUTE, 60);
        }
        else if (lozType == types.UNTIMED) {
            dayind = getIndexFromAllDayDiv(evParam);
            start = calcDateFromIndex(dayind);
            start = new cosmo.datetime.Date(start.getFullYear(),
                start.getMonth(), start.getDate());
            start.hours = 0;
            start.minutes = 0;
            end = new cosmo.datetime.Date(start.getFullYear(),
                start.getMonth(), start.getDate());
            eventStamp.setAnyTime(true);
        }

        // Put the new item in the currently selected collection
        var currCollId = cosmo.app.pim.getSelectedCollectionId();

        // Set EventStamp start and end calculated from click position
        // --------
        note.setDisplayName(_('Main.NewEvent'));
        note.setBody('');
        eventStamp.setStartDate(start);
        eventStamp.setEndDate(end);
        //normally the delta does the autotriaging, but since this is a new event
        //there is no delta, so we do it manually.
        note.autoTriage();

        // Create the CalItem
        item = new cosmo.view.cal.CalItem(note, [currCollId]);

        // Register the new event in the event list
        cosmo.view.cal.itemRegistry.setItem(item.id, item);
        // Put it in the current collection's registry
        cosmo.view.cal.placeItemInItsCollectionRegistries(item);

        // Set up the lozenge for the event, and put it
        // on the appropriate canvas
        item.lozenge = cosmo.view.cal.lozenge.createNewLozenge(item.id, lozType);
        item.lozenge.setUpDomAndAppend(item.id,
            self.getCanvasAreaByLozengeType(lozType));


        // Save new event
        dojo.event.topic.publish('/calEvent', { 'action': 'save',
            'data': item, 'qualifier': 'new' })
        return cosmo.view.cal.itemRegistry.getItem(id);
    };
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an event lozenge, and look up which event the lozenge belongs to.
     * Event lozenge components are all similarly named, beginning with 'eventDiv',
     * then followed by some indentifying text, a separator, and then the ID.
     * (e.g., 'eventDivBottom__12' or 'eventDivContent__8').
     * @return A string representing the event identifier for the event lozenge clicked on
     */
    function getIndexEvent(strId) {
        // Use regex to pull out the actual ID number
        var pat = new RegExp('^eventDiv[\\D]*__');
        var id = strId.replace(pat, '');
        return id;
    }
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an hour container, and look up which date/time the div belongs to.
     * Hour-div components are all similarly named, beginning with 'hourDiv',
     * then followed by some indentifying text, and then the date and hour
     * separated by a hyphen (e.g., 'hourDiv20051223-13' or 'hourDivSub20051016-2').
     * @return A string representing the date/time of the div clicked on
     */
    function getIndexFromHourDiv(strId) {
        var ind = strId.replace(/^hourDiv[\D]*/i, '');
        return ind;
    }
    /**
     * Takes the ID of any of the component DOM elements that collectively make up
     * an all-day event container, and look up which date the div belongs to.
     * All-day-div components are all similarly named, beginning with 'allDayListDiv',
     * then followed by some indentifying text, and then the date
     * (e.g., 'allDayListDiv20051223' or 'allDayListDivSub20051016').
     * @return A string representing the date/time of the div clicked on
     */
    function getIndexFromAllDayDiv(strId) {
        var ind = strId.replace(/^allDayListDiv[\D]*/i, '');
        return ind;
    }
    /**
     * Get the time from hyphen-separated string on a clicked-on hour div
     * @return A string of the time in military 'hh:mm' format
     */
    function extractTimeFromId(str) {
        var dt = str.split('-');
        var pat = /(00|30)$/
        var ret = dt[1].replace(pat, ':$1');
        return ret;
    }
    /**
     * Get the hour from a time-formatted string such as '23:56'
     * @return A string of the hour number
     */
    function extractHourFromTime(str) {
        arr = str.split(':');
        return arr[0];
    };
    /**
     * Get the minutes from a time-formatted string such as '23:56'
     * @return A string of the minutes
     */
    function extractMinutesFromTime(str) {
        arr = str.split(':');
        return arr[1];
    }
    /**
     * Get the date from hyphen-separated string on a clicked-on hour div
     * @return A string of the date, e.g., 20051223
     */
    function extractDayIndexFromId(str) {
        var dt = str.split('-');
        return parseInt(dt[0]);
    };
    /**
     * Calculate the date based on the day position clicked on
     * @param n Number representing day of the week
     * @return A date object representing the date clicked on
     */
    function calcDateFromIndex(n) {
        var incr = parseInt(n);
        var viewStart = cosmo.view.cal.viewStart;
        var st = viewStart.getDate();
        var ret = null;
        st += incr;
        ret = new Date(viewStart.getFullYear(), viewStart.getMonth(), st);
        return ret;
    };
}

cosmo.view.cal.canvas.Canvas = function (p) {
    var self = this;
    var params = p || {}
    this.domNode = _createElem('div');
    this.domNode.id = 'calendarCanvas';
    this.viewStart = null;
    this.viewEnd = null;
    this.currDate = null;
    for (var n in params) { this[n] = params[n]; }
    this.renderSelf = function () {

        // Rendering can be messages published to calEvent
        // or by window resizing
        if (!cosmo.view.cal.isCurrentView()) { return false; }

        this.width = this.parent.width;
        this.height = (this.parent.height - CAL_TOP_NAV_HEIGHT);
        this.domNode.style.position = 'absolute';
        this.setPosition(0, CAL_TOP_NAV_HEIGHT);
        this.setSize();
        if (!this.hasBeenRendered) {
            // Set up DOM structures, create ContentBox objs
            // for major components
            setUPDOM();
            this.hasBeenRendered = true;
        }
        // Position all the components based on current
        // viewport size and constants defined in cosmo.ui.conf
        doPositioning();

        // Set cal day column width
        cosmo.view.cal.canvas.dayUnitWidth = parseInt(
            (this.width - HOUR_LISTING_WIDTH - SCROLLBAR_SPACER_WIDTH)/7 );

        // Bolt onto older rendering code in cosmo.view.cal.canvas
        // FIXME: This is a temporary shim until we can pull
        // rendering-specific functions into cosmo.view.cal.canvas.Canvas,
        // leaving only the utility functions in cosmo.view.cal.canvas
        // ==============
        cosmo.view.cal.canvas.render(this.viewStart, this.viewEnd, this.currDate);
        // ==============
    };

    // Private methods
    function setUPDOM() {
        var d = self.domNode;
        var _html = cosmo.util.html;

        // List of day names for the week
        var t = _createElem('div');
        t.id = 'dayListDiv';
        d.appendChild(t);
        // Outside container for untimed event resizable area
        var t = _createElem('div');
        t.id = 'allDayResizeMainDiv';
        // Prevent text selection on drag
        t.onselectstart = function () { return false; };
        d.appendChild(t);
        // Left-hand spacer on untimed canvas that lines up
        // with timeline below in the timed canvas
        var sub = _createElem('div');
        sub.id = 'allDayHourSpacerDiv';
        t.appendChild(sub);
        // Untimed events canvas -- content area where events sit
        var sub = _createElem('div');
        sub.id = 'allDayContentDiv';
        t.appendChild(sub);
        // Resize handle for untimed event area
        var t = _createElem('div');
        t.id = 'allDayResizeHandleDiv';
        d.appendChild(t);
        // Outside container for timed event scrolling area
        var t = _createElem('div');
        t.id = 'timedCanvas';
        // Prevent text selection on drag
        t.onselectstart = function () { return false; };
        d.appendChild(t);
        // Left-hand timeline that lists hours of the day
        var sub = _createElem('div');
        sub.id = 'timedHourListDiv';
        t.appendChild(sub);
        // Timed events canvas -- content area where events sit
        var sub = _createElem('div');
        sub.id = 'timedContentDiv';
        t.appendChild(sub);

        self.parent.domNode.appendChild(self.domNode);
        var _c = cosmo.ui.ContentBox;
        var subList = [ 'calTopNavDiv', 'dayListDiv',
            'timedCanvas', 'timedContentDiv', 'timedHourListDiv',
            'eventInfoDiv', 'allDayResizeMainDiv', 'allDayResizeHandleDiv',
            'allDayContentDiv', 'allDayHourSpacerDiv' ];
        var vOffset = 0;
        var calcHeight = 0;
        for (var i = 0; i < subList.length; i++) {
            var key = subList[i];
            self[key] = new _c({ id: key, domNode: $(key) });
        }
    }
    function doPositioning() {
        var untimedHeight = cosmo.ui.resize_area.dragSize;
        untimedHeight = typeof untimedHeight == 'number' ?
            untimedHeight : ALL_DAY_RESIZE_AREA_HEIGHT;

        // Center column
        vOffset = 0;
        // Day listing
        var dayList = self.dayListDiv;
        dayList.setSize(self.width - 2, DAY_LIST_DIV_HEIGHT);
        dayList.setPosition(0, vOffset);
        // No-time event area
        vOffset += DAY_LIST_DIV_HEIGHT;
        var allDayMain = self.allDayResizeMainDiv;
        allDayMain.setSize((self.width - 2), untimedHeight);
        allDayMain.setPosition(0, vOffset);
        // Resize handle
        vOffset += untimedHeight;
        var allDayResize = self.allDayResizeHandleDiv;
        allDayResize.setSize(self.width - 1, ALL_DAY_RESIZE_HANDLE_HEIGHT);
        allDayResize.setPosition(0, vOffset);
        var allDayContent = self.allDayContentDiv;
        allDayContent.setSize((self.width - SCROLLBAR_SPACER_WIDTH -
            HOUR_LISTING_WIDTH - 4), '100%');
        allDayContent.setPosition((HOUR_LISTING_WIDTH + 1), 0);
        var allDaySpacer = self.allDayHourSpacerDiv
        allDaySpacer.setSize((HOUR_LISTING_WIDTH - 1), '100%');
        allDaySpacer.setPosition(0, 0);

        // Scrollable view area
        vOffset += ALL_DAY_RESIZE_HANDLE_HEIGHT + 1;
        calcHeight = self.height - vOffset;
        var timedMain = self.timedCanvas;
        timedMain.setSize(self.width - 2, calcHeight); // Variable height area
        timedMain.setPosition(0, vOffset);
        var timedContent = self.timedContentDiv;
        timedContent.setSize((self.width - HOUR_LISTING_WIDTH), VIEW_DIV_HEIGHT);
        timedContent.setPosition((HOUR_LISTING_WIDTH + 1), 0);
        var timedHourList = self.timedHourListDiv;
        timedHourList.setSize(HOUR_LISTING_WIDTH - 1, VIEW_DIV_HEIGHT);
        timedHourList.setPosition(0, 0);
    }
}
cosmo.view.cal.canvas.Canvas.prototype =
    new cosmo.ui.ContentBox();



