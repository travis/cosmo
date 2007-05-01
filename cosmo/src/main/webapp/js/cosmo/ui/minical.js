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

/**
 * @fileoverview This is a renderer for a small view of a calendar, primarily
 * used for controlling navigation of the main calendar view and for switching
 * the main view apropriately. It will show up to three months of time
 * and allow for the following navigation mechanisms: next month,
 * previous month, current day.
 * @author Jeremy Epstein mailto:eggfree@eggfree.net
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.minical");

dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.datetime");

cosmo.ui.minical.MiniCal = new function() {

    var self = this;

    /** helper method to pull day data. This is a cleaner implementation
     * when we start rendering busybars.
     */
    function getElementOfAttribute(attr, elem) {
        if (elem.tagName == "BODY") return null
        return (elem.getAttribute(attr) != null ?
            elem : getElementOfAttribute(attr, elem.parentNode));
    }
    /**
     * Hide the minical until actually rendered
     */
    function hide() {
        self.displayContext.style.visibility = 'hidden';
    };
    /**
     * Show the minical after initial render
     */
    function show() {
        self.displayContext.style.visibility = 'visible';
    };

    this.controller = null;
    this.id = '';
    this.displayContext = null;
    // Current height -- determines how many month tiles to display
    this.curHeight = 0;
    // Height of a month tile
    this.tileHeight = 0;
    // current rendered months. These may go null
    this.months = [];
    // Mappings of month numbers to index in displayed month array
    this.monthMappings = [];
    // Start of currently viewable events from controller
    this.viewStart = null;
    // End of currently viewable events from controller
    this.viewEnd = null;
    // Current date from controller
    this.currDate = null;
    // The first month displayed -- 0 = Jan, 11 = Dec, etc.
    this.firstMonthDate = null;
    // Range of selected days
    this.selectedDays = [];
    // Ref to DOM node of month-to-month nav
    this.navPanel = null;
    // Ref to DOM node holding the month tiles
    this.tileCanvas = null;
    // Height of the widget
    this.height = 0;

    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    this.handlePub = function(cmd) {
        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var opts = cmd.opts || {};
        var ev = cmd.data;
        switch (act) {
            case 'eventsLoadSuccess':
                // If the update originated here at minical,
                // just update the selection, don't re-render
                if (opts.source == 'minical') {
                    // Set selection
                    self.renderSelection();
                }
                // Otherwise do a full re-render
                else {
                    self.render();
                }
                break;
            default:
                // Do nothing
                break;
        }
    };

    /**
     * Initialize minical state and render
     * Hide until rendering is completed because Firefox
     * gives a brief glimpse as styles are being applied
     */
    this.init = function(controller, displayContext) {
        self.controller = controller;
        self.displayContext = displayContext || null;
        self.id = 'miniCal';
        self.currDate = this.controller.currDate;
        hide();
        if (self.render()) {
            show();
        }
        return true;
    };

     // Interface methods (public API)

    /**
     * Does a complete render of the entire
     * minical both rendering months and rendering the selection span
     * Because this view already has a reference to the model, there
     * is no need to pass arguments
     * @return Boolean true
     */
    this.render = function() {

        /**
         * Inner function that uses DOM methods to create the
         * three-col table for the month-to-month navigation
         * Use simple event registration since month-to-month
         * nav is local to this widget
         */
        function createNavPanel() {
            // Nav panel components
            var np = document.createElement('table');
            var npBody = document.createElement('tbody');
            var npRow = document.createElement('tr');
            var npCell = null; // Temp for cell nodes

            // Set up the nav panel
            np.id = 'miniCalNavPanel';
            np.style.width = '100%';
            np.cellPadding = '0';
            np.cellSpacing = '0';
            np.appendChild(npBody);
            npBody.appendChild(npRow);
            // Left arrow
            npCell = document.createElement('td');
            npCell.id = 'miniCalNavPanelLeft';
            npCell.style.textAlign = 'left';
            npCell.style.cursor = 'pointer';
            npCell.onclick = function() { self.goMonth(-1); };
            npCell.appendChild(document.createTextNode('<'));
            npRow.appendChild(npCell);
            // Center
            npCell = document.createElement('td');
            npCell.id = 'miniCalNavPanelCenter';
            npCell.style.textAlign = 'center';
            npCell.style.cursor = 'pointer';
            npCell.onclick = self.goToday;
            npCell.appendChild(document.createTextNode('Today'));
            npRow.appendChild(npCell);
            // Right arrow
            npCell = document.createElement('td');
            npCell.id = 'miniCalNavPanelRight';
            npCell.style.textAlign = 'right';
            npCell.style.cursor = 'pointer';
            npCell.onclick = function() { self.goMonth(1); };
            npCell.appendChild(document.createTextNode('>'));
            npRow.appendChild(npCell);

            self.displayContext.appendChild(np);
            self.navPanel = np;
        }

        /**
         * Inner function that creates the div that the
         * three month tiles will be attached to
         */
        function createTileCanvas() {
            // Tile container
            var tc = document.createElement('div');
            self.displayContext.appendChild(tc);
            self.tileCanvas = tc;
        }

        /**
         * Set vertical size of container div -- have to do this after
         * dates are filled in because months can have 4-6 rows
         */
        function setContainerSize() {
            var h = document.getElementById('miniCalNavPanel').offsetHeight;
            var c = cosmo.app.pim._collectionSelectContainer.offsetHeight + 40;
            for (var i = 0; i < self.months.length; i++) {
                if (h + self.months[i].offsetHeight < (self.controller.height - c)) {
                    h += self.months[i].offsetHeight;
                }
            }
            self.displayContext.style.height = h + 'px';
            self.displayContext.style.overflow = 'hidden';
            self.height = h;
        }
        /**
         * Set the vertical position of the container div
         * Right now this is a fixed size -- later this will
         * resize with a snap-to for either two or three tiles
         */
        function setContainerPos() {
            self.displayContext.style.top =
                (self.controller.height - self.height) + 'px';
        }

        // Begin rendering
        // ==========================
        // Bail if there's no context to render in
        if (this.displayContext == null) {
            return false;
        }

        // Sync the start date of the displayed period
        // with that of the main cal
        self.syncViewStart();

        // Initial render only
        if (!self.navPanel) {
            createNavPanel();
        }
        if (!self.tileCanvas) {
            createTileCanvas();
        }

        // Init and week-to-week nav from main cal
        var compMonthDate =  new Date(self.viewStart.getFullYear(),
            self.viewStart.getMonth(), 1);
        if (!self.firstMonthDate ||
            (self.firstMonthDate.getTime() != compMonthDate.getTime())) {
            self.firstMonthDate = compMonthDate;
            self.renderMonths();
        }

        // Init render only
        // Later on this will get more interesting if we add the
        // same behavior Chandler has for resizing and snapping into place
        setContainerSize();
        setContainerPos();

        // Init and week-to-week nav from main cal
        self.renderSelection();

        return true;
    };
    /**
     * Render three blank month tiles and attach to tileCanvas
     */
    this.renderMonths = function() {

        /**
         * Generates calendar layout -- creates a blank month "tile"
         */
        function createMonthTile(tileId) {
            var colHTML = '<col style="width:14.2857%;">';
            var monthHTML = '';
            monthHTML += '<table id="' + tileId + '"style="' +
                'table-layout:fixed; width:100%;' +
                'border-collapse:collapse;">';
            monthHTML += '<colgroup>' + colHTML + colHTML +
                colHTML + colHTML + colHTML + colHTML + colHTML +
                '</colgroup>';
            monthHTML += '<thead><tr><td id="' + tileId +
                '_monthName" colspan="7" class="miniMonthName">' +
                '</td></tr>';
            monthHTML += '<tr>%daynames%</tr></thead>';
            monthHTML += '<tbody>%body%</tbody>';
            monthHTML += '</table>';
            var dayHTML = '<td id="%daynum%" class="miniBase"></td>';
            var dayHeadHTML = '<td class="miniDayName">%content%</td>';
            var weekHTML = '<tr>%content%</tr>';

            //create daynames
            var content = '';
            for (var i = 0; i < 7; i++)
            content += dayHeadHTML.replace(/%content%/,
                cosmo.datetime.abbrWeekday[i].substr(0, 1));
            monthHTML = monthHTML.replace(/%daynames%/, content);

            // Create days and weeks
            var weeks = '';
            var days = '';
            // These are 'scratch' ids that will be replaced when
            // the month is filled in with the actual month data
            for (var i = 1; i < 43; i++){
                days += dayHTML.replace(/%daynum%/, tileId + '_cell' + (i));
                if (i % 7 == 0) {
                    weeks += weekHTML.replace(/%content%/, days);
                    days = '';
                }
            }
            monthHTML = monthHTML.replace(/%body%/, weeks);
            var tileDiv = document.createElement("DIV");
            tileDiv.id = 'div' + tileId;
            tileDiv.className = 'miniMonthTile';
            self.tileCanvas.appendChild(tileDiv);
            tileDiv.innerHTML += monthHTML;
            // Return the DOM node
            return tileDiv;
        }

        /**
         * Populates months -- the top month is the current one
         * Fill in month name, dates, and dimmed 'lead in' and
         * 'lead out' dates
         */
        function fillMonths() {
            var currDate = self.currDate;
            var currDateStamp = 0;
            var dt = new Date(self.firstMonthDate.getTime());
            var dtLast = new Date(dt.getTime());
            var dayOffset = 0;
            var mon = 0;
            var idPrefix = '';
            var monthName = '';
            var monthNameDiv = null;
            var datElem = null;
            var cellNum = 0;
            var last = 0;
            var incr = 1;
            var diff = 0;

            // Empty out month index mappings
            self.monthMappings = [];

            // For highlighting the current date on the cal
            currDateStamp = new Date(currDate.getFullYear(),
                currDate.getMonth(), currDate.getDate()).getTime();

            // Set date of current month to the 1st
            dt.setDate(1);
            // Grab last date of previous month
            dtLast.setDate(0);
            last = dtLast.getDate();

            // Each entry in the month array for the view
            for (var i = 0; i < self.months.length; i++) {
                mon = dt.getMonth();
                idPrefix = self.id + '_month' + i;

                // Record which month numbers are being displayed
                self.monthMappings[mon] = i;

                dayOffset = dt.getDay();

                // Label month name
                monthName = dojo.date.strftime(dt, '%B %Y');
                monthNameDiv = document.getElementById(self.id +
                    '_month' + i + '_monthName');
                monthNameDiv.appendChild(document.createTextNode(monthName));

                // Fill in date numbers for end of previous month
                // if needed
                // -----------
                incr = 1;
                last = last - dayOffset;
                while (incr <= dayOffset) {
                   // Table cells starting at the begining of the cal tile table
                   datElem = document.getElementById(idPrefix + '_cell' + incr);

                   // Set the real id -- use the displayed date num
                   // Dimmed dates for end of previous month
                   datElem.id = datElem.id = idPrefix + '_day' + (last + incr) + '_dim';

                   // Display the date for the cell
                   datElem.appendChild(document.createTextNode(last + incr));
                   datElem.className = 'miniDimmed';
                   incr++;
                }

                // Fill in dates for the current month
                // -----------
                incr = 0;
                while (dt.getMonth() == mon) {
                    var dat = dt.getDate();

                    cellNum = dat + dayOffset;
                    datElem = document.getElementById(idPrefix + '_cell' + cellNum);

                    // Set the real id -- use the displayed date num
                    datElem.id = idPrefix + '_day' + dat;

                    // Display the date for the cell
                    datElem.appendChild(document.createTextNode(dat));
                    datElem.className = dt.getTime() == currDateStamp ?
                        'miniToday' : 'miniWeekday';
                    datElem.setAttribute("day", dt.getTime());

                    // increment date
                    dt.setDate(dat + 1);
                    incr++;
                }
                /*
                // If there's an entire empty row left, remove it
                if ((incr + dayOffset) < 36) {
                    var rows = self.months[i].getElementsByTagName('tr');
                    var tb = self.months[i].getElementsByTagName('tbody')[0];
                    tb.removeChild(rows[7]);
                }
                */

                // Grab last date of previous month
                dtLast = new Date(dt.getTime());
                dtLast.setDate(0);
                last = dtLast.getDate();

                // Fill in any leftover cells with the
                // first dates of the following month
                // -----------
                dayOffset = dtLast.getDay();
                diff = 7 - dayOffset;
                incr = 1;
                while (incr < diff) {
                   datElem = document.getElementById(idPrefix + '_cell' + (cellNum + incr));

                   // Give the cell an id that reflects the actual date num
                   // Dimmed dates for beginning of following month
                   datElem.id = datElem.id = idPrefix + '_day' + (incr) + '_dim';

                   datElem.appendChild(document.createTextNode(incr));
                   datElem.className = 'miniDimmed';
                   incr++;
                }
            }

            // Since the context is not guaranteed, use kwConnect to
            // ensure that the event is bound only once
            dojo.event.kwConnect({
                    srcObj:     self.tileCanvas,
                    srcFunc:    "onclick",
                    targetObj:  self,
                    targetFunc: 'clickHandler',
                    once:       true
                    });
        }

        // Begin rendering
        // ==========================
        var tile = null;
        self.months = [];

        // Clear the tile canvas
        while (tile = self.tileCanvas.firstChild) {
            self.tileCanvas.removeChild(tile);
        }
        // Populate the months array and tile canvas with blank month tiles
        while (self.months.length < 3) {
            tile = createMonthTile(self.id + '_month' +
                self.months.length);
            self.months.push(tile);
            self.tileCanvas.appendChild(tile);
        }
        // Fill in month names and dates for the three blank month tiles
        fillMonths();
    };
    /**
     * Render the selected range of days
     * If the selected time range is outside the displayed
     * months, the function simply bails out
     */
    this.renderSelection = function() {

        // Sync internal start date with query start for main cal
        self.syncViewStart();

        var selDays = self.selectedDays;
        var selDiv = null;
        var dt = new Date(self.viewStart.getTime());
        var viewStartMonth = self.viewStart.getMonth();
        var viewEndMonth = self.viewEnd.getMonth();
        var isStartDateMonthRendered = !isNaN(self.monthMappings[viewStartMonth]);
        var isEndDateMonthRendered = !isNaN(self.monthMappings[viewEndMonth]);
        var crossMonth = dt.getDate() > self.controller.viewEnd.getDate();
        var monIndex =  null;
        var idPrefix = self.id + '_month';

        // If the actual start month is not one of the three displayed,
        // get the start month number by subtracting one from the end
        // month number.
        // This fixes issues with selections that span two month tiles
        // when only one of the two months is showing
        monIndex = isStartDateMonthRendered ? self.monthMappings[viewStartMonth] :
            self.monthMappings[viewEndMonth] - 1;

        // Bail out if viewed range of events is not in the currently
        // displayed array of months
        if (!(isStartDateMonthRendered || isEndDateMonthRendered)) {
            return;
        }

        // Main function that actually does the selection work
        function selectCells(dt, idPrefix, monIndex, suffA, suffB) {
            var idSuffix = '';
            var datIndex = 0;
            var idStr = '';
            var selDiv = null;
            while (dt <= self.controller.viewEnd) {
                idSuffix = dt.getDate() <
                    self.viewStart.getDate() ? suffA : suffB;
                datIndex = dt.getDate();
                idStr = idPrefix + monIndex + '_day' + datIndex + idSuffix;
                selDiv = document.getElementById(idStr);
                selDiv.className = selDiv.className + ' miniSelected';
                self.selectedDays.push(selDiv);
                datIndex++;
                dt.setDate(datIndex);
            }
        }

        // Deselect selected cells if needed -- remove appended
        // ' miniSelected' (space plus miniSelected) CSS class
        while (selDiv = selDays.pop()) {
            selDiv.className = selDiv.className.replace(' miniSelected', '');
        }

        // Create new selection
        // ----------
        // Selection spans two months -- do dim/plain, then plain/dim
        // In some cases only the first or second of the two months
        // may actually be rendered -- isStart/EndDateRendered
        if (crossMonth) {
            // * First month -- actual dates and dimmed dates for next month
            if (isStartDateMonthRendered) {
                selectCells(dt, idPrefix, monIndex, '_dim', '');
            }
            if (isEndDateMonthRendered) {
                // * Second month -- dimmed dates for previous month and actual dates
                // Move to the next month
                monIndex++;
                // Reset working date
                dt = new Date(self.viewStart.getTime());
                selectCells(dt, idPrefix, monIndex, '', '_dim');
            }
        }
        // Selection is all within a single month -- do plain/plain
        else {
            selectCells(dt, idPrefix, monIndex, '', '');
        }
    }
    /**
     * Navigate forward or backward the desired number of months
     * Called from the two arrows in the month-to-month nav
     * Negative numbers go backward
     * The selection gets re-rendered so it stays in place
     * as you move month-to-month
     */
    this.goMonth = function(dir) {
        var incr = dir;
        var compMonthDate =  new Date(self.viewStart.getFullYear(),
            self.viewStart.getMonth(), 1);

        self.firstMonthDate = cosmo.datetime.Date.add(self.firstMonthDate,
            dojo.date.dateParts.MONTH, incr); // Increment the months
        self.renderMonths(); // Render the desired set of months
        self.renderSelection(); // Keep the selection where it was
    };
    /**
     * Go to today's date. This directly acceses the main controller
     * and then simply re-renders the minical just as with the
     * initial load of the calendar
     * FIXME -- this is one of the places we should look at using
     * topics and pub/sub
     */
    this.goToday = function() {
        dojo.event.topic.publish('/calEvent', {
            action: 'loadCollection', data: { goTo: self.currDate }
        });
    }
    /**
     * Handle clicks on normal dates within minical
     * Navigate to appropriate dates and re-render selection
     */
    this.clickHandler = function(event) {
        var target = (typeof event.target != "undefined") ?
            event.target : event.srcElement;
        var elem = getElementOfAttribute("day", target);
        var dt = null;
        if (elem == null) {
            return;
        }
        dt = elem.getAttribute('day');
        // Convert to int because FF saves attributes as strings
        dt = new Date(parseInt(dt));

        dojo.event.topic.publish('/calEvent', {
            action: 'loadCollection',
            data: { goTo: dt },
            opts: { source: 'minical' }
        });
    };
    /**
     * Synchronize minical viewStart property with the
     * start date for the range of displayed events in
     * the main cal
     */
    this.syncViewStart = function() {
        self.viewStart = self.controller.viewStart;
        self.viewEnd = self.controller.viewEnd;
    }
    /**
     * Prevent memleak
     */
    this.cleanup = function() {
        /* need to do dom cleanup*/
         dojo.event.kwDisconnect({
                srcObj:     self.tileCanvas,
                srcFunc:    'onclick',
                targetObj:  self,
                targetFunc: 'clickHandler',
                once:       true
                });
        self.navPanel = null;
        self.tileCanvas = null;
        while (selDiv = self.selectedDays.pop()) {
            selDiv = null;
        }
    };
}
