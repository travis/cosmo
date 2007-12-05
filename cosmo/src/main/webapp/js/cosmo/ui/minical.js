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
 * @authors Matthew Eernisse (mde@osafoudation.org),
 *     Jeremy Epstein (eggfree@eggfree.net)
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.minical");
dojo.require("cosmo.ui.ContentBox"); // Superclass

dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("dojo.event.*");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("cosmo.datetime.util");
dojo.require("cosmo.service.exception");

cosmo.ui.minical.MiniCal = function (p) {

    var params = p || {};
    var self = this;
    // Initialize these to the start/ends of the
    // viewable span in the calendar canvas
    // after init these vals will always be passed in
    // in the opts obj in the published message
    var defaultDate = cosmo.app.pim.currDate;
    var viewStart = null;
    var viewEnd = null;
    setSelectionSpan(defaultDate);

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
        self.domNode.style.visibility = 'hidden';
    };
    /**
     * Show the minical after initial render
     */
    function show() {
        self.domNode.style.visibility = 'visible';
    };
    // FIXME: There is similar logic is dup'd in ...
    // view.cal.common.loadItems
    // ui.minical.handlePub
    // ui.minical -- setSelectionSpan private function
    // ui.navbar._showMonthheader
    // These different UI widgets have to be independent
    // of the calendar view, but still display sync'd
    // information -- what's a good way to consolidate this?
    function setSelectionSpan(dt) {
        viewStart = cosmo.datetime.util.getWeekStart(dt);
        viewEnd = cosmo.datetime.util.getWeekEnd(dt);
    }

    this.parent = null;
    this.domNode = null;
    this.miniCalNode = null;
    this.goToDateNode = null;
    this.goToDateForm = null;
    this.goToDateButton = null;
    this.currDate = null;
    this.id = '';
    // Current height -- determines how many month tiles to display
    this.curHeight = 0;
    // Height of a month tile
    this.tileHeight = 0;
    // current rendered months. These may go null
    this.months = [];
    // Mappings of month numbers to index in displayed month array
    this.monthMappings = [];
    // Current date
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
    // Signals initial render has been completed
    this.hasBeenRendered = false;

    // Set any props based on passed-in params
    for (var n in params) { this[n] = params[n]; }
    // Default to hidden state to avoid flash of unstyled
    // state in Firefox
    hide();

    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    this.handlePub = function (cmd) {
        var act = cmd.action;
        var qual = cmd.qualifier || null;
        var opts = cmd.opts || {};
        var ev = cmd.data;
        switch (act) {
            case 'loadCollection':
                // FIXME: There is similar logic is dup'd in ...
                // view.cal.common.loadItems
                // ui.minical.handlePub
                // ui.minical -- setSelectionSpan private function
                // ui.navbar._showMonthheader
                // These different UI widgets have to be independent
                // of the calendar view, but still display sync'd
                // information -- what's a good way to consolidate this?
                if (opts.loadType == 'changeTimespan') {
                    var goToNav = opts.goTo;
                    var queryDate = null;
                    // param is 'back' or 'next'
                    if (typeof goToNav == 'string') {
                        var key = goToNav.toLowerCase();
                        var incr = key.indexOf('back') > -1 ? -1 : 1;
                        queryDate = cosmo.datetime.Date.add(viewStart,
                            dojo.date.dateParts.WEEK, incr);
                    }
                    // param is actual Date
                    else {
                        queryDate = goToNav;
                    }
                    // Span of time for selection
                    setSelectionSpan(queryDate);
                }
                // If the update originated here at minical,
                // just update the selection, don't re-render
                if (opts.source == 'minical') {
                    // Set selection
                    self.renderSelection();
                }
                // Otherwise do a full re-render
                else {
                    self.renderSelf();
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
    this.init = function (domNode, parent, currDate) {
        self.domNode = domNode || null;
        self.id = 'miniCal';
        self.currDate = currDate;
        this.parent = parent;
        hide();
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
    this.renderSelf = function () {
        function createGoToDate() {
            var table = _createElem('table');
            var tbody = _createElem('tbody');
            var tr = _createElem('tr');
            var td = null; // Temp for cell nodes
            table.style.margin = 'auto';
            table.cellPadding = '0';
            table.cellSpacing = '0';
            table.appendChild(tbody);
            tbody.appendChild(tr);

            // Text label
            td = _createElem('td');
            td.appendChild(_createText(_('Main.GoTo')));
            tr.appendChild(td);

            // Spacer
            td = _createElem('td');
            td.appendChild(cosmo.util.html.nbsp());
            tr.appendChild(td);

            // Text input
            td = _createElem('td');
            var f = _createElem('form');
            f.id = 'goToDateForm';
            self.goToDateForm = f;
            f.onsubmit = function () { return false; };
            d.appendChild(f);
            var tInput = cosmo.util.html.createInput( { type: 'text',
                name: 'goToDateInput',
                id: 'goToDateInput',
                size: 10,
                maxlength: 10,
                value: '',
                className: 'inputText' });
            f.appendChild(tInput);
            cosmo.util.html.setTextInput(tInput, 'mm/dd/yyyy', true, false);
            dojo.event.connect(tInput, 'onfocus', cosmo.util.html,
                'handleTextInputFocus');
            dojo.event.connect(tInput, 'onkeyup', self,
                'handleKeyUp');
            td.appendChild(f);
            tr.appendChild(td);

            // Spacer
            td = _createElem('td');
            td.appendChild(cosmo.util.html.nbsp());
            tr.appendChild(td);

            // Button
            td = _createElem('td');
            var buttonGoTo = new Button('goToDateButton', 36, self.doGoToDate,
                _('App.Button.Go'), true);
            self.goToButton = buttonGoTo;
            td.appendChild(buttonGoTo.domNode);
            tr.appendChild(td);

            self.goToDateNode.appendChild(table);
        }
        /**
         * Inner function that uses DOM methods to create the
         * three-col table for the month-to-month navigation
         * Use simple event registration since month-to-month
         * nav is local to this widget
         */
        function createNavPanel() {
            // Nav panel components
            var np = _createElem('table');
            var npBody = _createElem('tbody');
            var npRow = _createElem('tr');
            var npCell = null; // Temp for cell nodes

            // Set up the nav panel
            np.id = 'miniCalNavPanel';
            np.style.width = '100%';
            np.cellPadding = '0';
            np.cellSpacing = '0';
            np.appendChild(npBody);
            npBody.appendChild(npRow);
            // Left arrow
            npCell = _createElem('td');
            npCell.id = 'miniCalNavPanelLeft';
            npCell.style.textAlign = 'left';
            npCell.style.cursor = 'pointer';
            npCell.onclick = function () { self.goMonth(-1); };
            npCell.appendChild(document.createTextNode('<'));
            npRow.appendChild(npCell);
            // Center
            npCell = _createElem('td');
            npCell.id = 'miniCalNavPanelCenter';
            npCell.style.textAlign = 'center';
            npCell.style.cursor = 'pointer';
            npCell.onclick = self.goToday;
            npCell.appendChild(document.createTextNode('Today'));
            npRow.appendChild(npCell);
            // Right arrow
            npCell = _createElem('td');
            npCell.id = 'miniCalNavPanelRight';
            npCell.style.textAlign = 'right';
            npCell.style.cursor = 'pointer';
            npCell.onclick = function () { self.goMonth(1); };
            npCell.appendChild(document.createTextNode('>'));
            npRow.appendChild(npCell);

            self.miniCalNode.appendChild(np);
            self.navPanel = np;
        }

        /**
         * Inner function that creates the div that the
         * three month tiles will be attached to
         */
        function createTileCanvas() {
            // Tile container
            var tc = _createElem('div');
            self.miniCalNode.appendChild(tc);
            tc.id = 'miniCalTileCanvas';
            self.tileCanvas = tc;
        }

        /**
         * Set vertical size of container div
         */
        function setContainerSize() {
            // Get height of name-display / selector sitting above
            // to determine how many months will fit
            // Both flavors of IE don't accurately report offsetHeight
            // of the node even after it's appended to the doc, so
            // we'll just use hard-coded values we know are right for
            // the collection display in ticket view, and the collection
            // selector when logged in
            c = cosmo.app.pim.ticketKey ? 36 : COLLECTION_SELECTOR_HEIGHT;
            c += 8; // A bit of spacing
            var h = self.navPanel.offsetHeight;
            h += self.goToDateNode.offsetHeight;
            for (var i = 0; i < self.months.length; i++) {
                if (h + self.months[i].offsetHeight < (self.parent.height - c)) {
                    h += self.months[i].offsetHeight;
                }
            }
            self.domNode.style.height = h + 'px';
            self.domNode.style.overflow = 'hidden';
            self.height = h;
        }
        /**
         * Set the vertical position of the container div
         * Right now this is a fixed size -- later this will
         * resize with a snap-to for either two or three tiles
         */
        function setContainerPos() {
            self.domNode.style.top =
                (self.parent.height - self.height) + 'px';
        }

        // Begin rendering
        // ==========================
        // Bail if there's no context to render in
        if (self.domNode == null) {
            throw 'No domNode on which to render this component.';
        }

        // Go-to date node -- don't re-render this every time
        // the widget renders
        // ----------
        if (!self.hasBeenRendered) {
            var d = _createElem('div');
            d.id = 'goToDateNode';
            self.goToDateNode = d;
            self.domNode.appendChild(d);
            createGoToDate();
        }

        // Minical node -- re-render every time. First remove
        // the old miniCalNode, then rebuild and re-append
        // ----------
        if (self.miniCalNode) {
            self.domNode.removeChild(self.miniCalNode);
        }

        var d = _createElem('div');
        d.id = 'miniCalNode';
        self.miniCalNode = d;
        self.domNode.appendChild(d);
        createNavPanel();
        createTileCanvas();

        var compMonthDate =  new Date(viewStart.getFullYear(),
            viewStart.getMonth(), 1);
        self.firstMonthDate = compMonthDate;
        self.renderMonths();

        // Set size/pos
        setContainerSize();
        setContainerPos();

        // Init and week-to-week nav from main cal
        self.renderSelection();

        // Initialized as hidden, so show when render is completed
        // -- avoids flash of unstyld state in Firefox
        if (!self.hasBeenRendered) {
            show();
            self.hasBeenRendered = true;
        }

        return true;
    };
    /**
     * Render three blank month tiles and attach to tileCanvas
     */
    this.renderMonths = function () {

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
            var tileDiv = _createElem("DIV");
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
    this.renderSelection = function () {
        var selDays = self.selectedDays;
        var selDiv = null;
        var dt = new Date(viewStart.getTime());
        var viewStartMonth = viewStart.getMonth();
        var viewEndMonth = viewEnd.getMonth();
        var isStartDateMonthRendered = !isNaN(self.monthMappings[viewStartMonth]);
        var isEndDateMonthRendered = !isNaN(self.monthMappings[viewEndMonth]);
        var crossMonth = dt.getDate() > viewEnd.getDate();
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
            while (dt <= viewEnd) {
                idSuffix = dt.getDate() <
                    viewStart.getDate() ? suffA : suffB;
                datIndex = dt.getDate();
                idStr = idPrefix + monIndex + '_day' + datIndex + idSuffix;
                selDiv = document.getElementById(idStr);
                selDiv.className = selDiv.className + ' selectedItem';
                self.selectedDays.push(selDiv);
                datIndex++;
                dt.setDate(datIndex);
            }
        }

        // Deselect selected cells if needed -- remove appended
        // ' selectedItem' (space plus selectedItem) CSS class
        while (selDiv = selDays.pop()) {
            selDiv.className = selDiv.className.replace(' selectedItem', '');
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
                dt = new Date(viewStart.getTime());
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
    this.goMonth = function (dir) {
        var incr = dir;
        var compMonthDate =  new Date(viewStart.getFullYear(),
            viewStart.getMonth(), 1);

        self.firstMonthDate = cosmo.datetime.Date.add(self.firstMonthDate,
            dojo.date.dateParts.MONTH, incr); // Increment the months
        self.renderMonths(); // Render the desired set of months
        self.renderSelection(); // Keep the selection where it was
    };
    /**
     * Go to today's date. This re-renders the minical just as with the
     * initial load of the calendar
     */
    this.goToday = function () {
        // FIXME: This loads the calendar data twice
        // We need to refactor this to allow date params
        // to be passed to the initial loading of the cal view
        cosmo.app.pim.baseLayout.mainApp.centerColumn.navBar.displayView(
            { viewName: cosmo.view.names.CAL, noLoad: true });
        var f = function () {
            dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection',
                opts: { loadType: 'changeTimespan', goTo: self.currDate },
                data: {} });
        }
        setTimeout(f, 0);
    }
    /**
     * Handle clicks on normal dates within minical
     * Navigate to appropriate dates and re-render selection
     */
    this.clickHandler = function (event) {
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

        cosmo.app.pim.baseLayout.mainApp.centerColumn.navBar.displayView(
            { viewName: cosmo.view.names.CAL, noLoad: true });
        var f = function () {
            dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection',
                opts: { loadType: 'changeTimespan', goTo: dt,
                      source: 'minical' },
                data: {} });
        }
        setTimeout(f, 0);
    };
    this.doGoToDate = function () {
        var e = null;
        var err = '';
        var val = self.goToDateForm.goToDateInput.value;
        err = cosmo.util.validate.dateFormat(val);
        if (err) {
            err += '\n';
        }
        // Display error or update form and submit
        // =======================
        // Err condition
        if (err) {
            err = err.replace(/\n/g, '<br/>');
            e = new cosmo.service.exception.ServiceException();
            e.message = err;
            cosmo.app.showErr(_('Main.Error.GoToDate'), e);
            return false;
        }
        // All okey-dokey -- submit
        else {
            var d = new Date(val);
            cosmo.app.pim.baseLayout.mainApp.centerColumn.navBar.displayView(
                { viewName: cosmo.view.names.CAL, noLoad: true });
            var f = function () {
                dojo.event.topic.publish('/calEvent', {
                    action: 'loadCollection',
                    opts: { loadType: 'changeTimespan', goTo: d },
                        data: {} });
            }
            setTimeout(f, 0);
        }
    };
    this.handleKeyUp = function (e) {
        // Accept Enter key input
        if (e.keyCode == 13) {
            self.doGoToDate();
            // Stop the keyup event from propagating
            // otherwise the Enter key input will continue
            // on to *close* the error dialog box. :)
            e.stopPropagation();
            return false;
        }
    };
    /**
     * Prevent memleak
     */
    this.cleanup = function () {
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

};

cosmo.ui.minical.MiniCal.prototype =
    new cosmo.ui.ContentBox();

