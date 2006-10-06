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

cosmo.view.cal.canvas = new function() {
    
    // Need some closure for scope
    var self = this;
    // Rendering the first time
    var initRender = true; 
    // Resizeable area for all-day events -- a ResizeArea obj
    var allDayArea = null; 
    
    function $(id) {
        return document.getElementById(id);
    }
    
    function setSelectedEvent(ev) {
        // Deselect previously selected event if any
        if (self.selectedEvent) {
            self.selectedEvent.block.setDeselected();
        }
        self.selectedEvent = ev; // Pointer to the currently selected event
        ev.block.setSelected(); // Show the associated block as selected
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
    function removeEvent(key, val) {
        var ev = val;
        var selEv = self.getSelectedEvent();
        // Remove the block
        ev.block.remove();
        if (selEv && (selEv.id = ev.id)) {
            self.selectedEvent = null;
        }
        // Remove from list of visible events
        self.eventRegistry.removeItem(ev.id);
        // Bye-bye, event
        ev = null;
    }
    function removeAllEvents() {
        self.eventRegistry.each(removeEvent);
        self.eventRegistry = new Hash();
    };
    function appendLozenge(key, val) {
        var id = key;
        var ev = val;
        // Create the block and link it to the event
        ev.block = ev.data.allDay ? new NoTimeBlock(id) :
            new HasTimeBlock(id);
        ev.block.insert(id);
    }
    function updateEventsDisplay() {
        if (self.eventRegistry.length && 
            cosmo.view.cal.conflict.calc(self.eventRegistry) && 
                positionLozenges()) {
            // If no currently selected event, put selection on
            // the final one loaded
            if (!self.selectedEvent) {
                dojo.event.topic.publish('/calEvent', { 
                    'action': 'setSelected', 
                    'data': self.eventRegistry.getLast() });
            }
            dojo.event.topic.publish('/calEvent', { 'action': 
                'eventsDisplaySuccess', 'data': self.selectedEvent });
        }
    }
    function positionLozenges() {
        return self.eventRegistry.each(positionLozenge);
    };
    function positionLozenge(key, val) {
        ev = val;
        ev.block.updateFromEvent(ev);
        ev.block.updateDisplayMain();
    }
    function restoreEvent(ev) {
        if (ev.restoreFromSnapshot()) {
            ev.block.updateFromEvent(ev);
            ev.block.updateDisplayMain();
        }
    }
    
    dojo.event.topic.subscribe('/calEvent', self, 'handlePub');
    this.handlePub = function(cmd) {
        var act = cmd.action;
        var ev = cmd.data;
        switch (act) {
            case 'eventsLoadSuccess':
                self.eventRegistry = ev;
                self.eventRegistry.each(appendLozenge);
                updateEventsDisplay();
                break;
            case 'setSelected':
                setSelectedEvent(ev);
                break;
            case 'save':
                //setSelectedEvent(ev);
                break; 
            case 'saveFailed':
                if (cmd.qualifier == 'initialSave') {
                    removeEvent(ev.id, ev);
                }
                else {
                    restoreEvent(ev);
                    ev.setInputDisabled(false);
                }
                break;
            case 'saveSuccess':
                // Changes have placed the saved event off-canvas
                if (cmd.qualifier == 'offCanvas') {
                    removeEvent(ev.id, ev);
                }
                // Saved event is still in view
                else {
                    ev.setInputDisabled(false);
                    ev.block.updateDisplayMain();
                }
                updateEventsDisplay();
                break;
            case 'removeSuccess':
                removeEvent(ev.id, ev);
                updateEventsDisplay();
                break;
            default:
                // Do nothing
                break;
        }
    };
    
    // Width of day col in week view, width of event blocks --
    // Calc'd based on client window size
    // Other pieces of the app use this, so make it public
    this.dayUnitWidth = 0;
    // The list currently displayed events
    // on the calendar -- key is the CalEvent obj's id prop,
    // value is the CalEvent
    this.eventRegistry = new Hash();
    // Currently selected event
    this.selectedEvent = null;
    
    // Public methods
    // ****************
    this.render = function(vS, vE, cD) {
        
        // Bounding dates, current date -- passed in on render
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
         * Set up key container elements 
         */
        function init() {
            monthHeaderNode = $('monthHeaderDiv');
            timelineNode = $('timedHourListDiv');
            hoursNode = $('timedContentDiv');
            dayNameHeadersNode = $('dayListDiv');
            allDayColsNode = $('allDayContentDiv');
            return true;
        }
        
        /**
         * Shows list of days at the head of each column in the week view
         * Uses the Date.abbrWeekday array of names in date.js
         */
        function showDayNameHeaders() {
            
            var str = '';
            var start = HOUR_LISTING_WIDTH + 1;
            var idstr = '';
            var startdate = viewStart.getDate();
            var startmon = viewStart.getMonth();
            var daymax = daysInMonth(startmon+1);
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());
            
             // Returns the number of days in a specific month of a specific year --
             // the year is necessary to handle leap years' Feb. 29
             function daysInMonth(month, year){
                var days = 0;
                switch (month) {
                    case 4:
                    case 6:
                    case 9:
                    case 11:
                        days = 30;
                        break;
                    case 2:
                        if (year % 4 == 0){
                               days = 29;
                           }
                           else{
                               days = 28;
                          }
                          break;
                      default:
                          days = 31;
                          break;
                }
                return days;
            }
            
            // Spacer to align with the timeline that displays hours below
            // for the timed event canvas
            str += '<div id="dayListSpacer" class="dayListDayDiv"' +
                ' style="left:0px; width:' +
                (HOUR_LISTING_WIDTH - 1) + 'px; height:' +
                (DAY_LIST_DIV_HEIGHT-1) +
                'px;">&nbsp;</div>';

            // Do a week's worth of day cols with day name and date
            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                startdate = startdate > daymax ? 1 : startdate;
                // Subtract one pixel of height for 1px border per retarded CSS spec
                str += '<div class="dayListDayDiv" id="dayListDiv' + i +
                    '" style="left:' + start + 'px; width:' + (self.dayUnitWidth-1) +
                    'px; height:' + (DAY_LIST_DIV_HEIGHT-1) + 'px;';
                if (calcDay.getTime() == currDay.getTime()) {
                    str += ' background-image:url(' + cosmo.env.getImagesUrl() + 
                        'day_col_header_background.gif); background-repeat:' +
                        ' repeat-x; background-position:0px 0px;'
                }
                str += '">';
                str += Date.abbrWeekday[i] + '&nbsp;' + startdate;
                str += '</div>\n';
                start += self.dayUnitWidth;
                startdate++;
            }
            dayNameHeadersNode.innerHTML = str;
            return true;
        }

        /**
         * Draws the day columns in the resizeable all-day area
         */
        function showAllDayCols() {
            var str = '';
            var start = 0;
            var idstr = ''
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());

            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                str += '<div class="allDayListDayDiv';
                if (calcDay.getTime() == currDay.getTime()) {
                    str += ' currentDayDay'
                }
                str +='" id="allDayListDiv' + i +
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
         */
        function showHours() {
            var str = '';
            var row = '';
            var start = 0;
            var idstr = '';
            var hour = 0;
            var meridian = '';
            var calcDay = null;
            var cd = currDate;
            var currDay = new Date(cd.getFullYear(), cd.getMonth(), cd.getDate());
            var isCurrentDay = false;
            var viewDiv = null;
            var timeLineWidth = 0;
            var workingHoursBarWidth = 3;
            
            // Subtract one px for border per asinine CSS spec
            var halfHourHeight = (HOUR_UNIT_HEIGHT/2) - 1;
            
            function workingHoursLine() {
                var r = '';
                // Working/non-working hours line
                r += '<div class="';
                r += (j < 8 || j > 17) ? 'nonWorkingHours' : 'workingHours';
                r += '" style="width:' + workingHoursBarWidth + 
                    'px; height:' + (halfHourHeight+1) + 
                    'px; float:left; font-size:1px;">&nbsp;</div>';
                return r;
            }
            
            str = '';
            viewDiv = timelineNode; 
            timeLineWidth = parseInt(viewDiv.offsetWidth);
            // Subtract 1 for 1px border
            timeLineWidth = timeLineWidth - workingHoursBarWidth - 1;
            
            // Timeline of hours on left
            for (var j = 0; j < 24; j++) {
                hour = j == 12 ? getText('App.Noon') : hrMil2Std(j);
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
                row += workingHoursLine();
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
                row += workingHoursLine();
                row += '<br class="clearAll"/>'
                
                str += row;
            }
            viewDiv.innerHTML = str;

            str = '';
            viewDiv = hoursNode;

            // Do a week's worth of day cols with hours
            for (var i = 0; i < 7; i++) {
                calcDay = Date.add('d', i, viewStart);
                str += '<div class="dayDiv" id="dayDiv' + i +
                    '" style="left:' + start + 'px; width:' +
                    (cosmo.view.cal.canvas.dayUnitWidth-1) +
                    'px;"';
                str += '>';
                for (var j = 0; j < 24; j++) {
                    
                    isCurrentDay = (calcDay.getTime() == currDay.getTime());
                    
                    idstr = i + '-' + j + '00';
                    row = '';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivTop';
                    // Highlight the current day
                    if (isCurrentDay) {
                        row += ' currentDayDay'
                    }
                    // Non-working hours are gray
                    else if (j < 8 || j > 18) {
                        //row += ' nonWorkingHours';
                    }
                    row += '" style="height:' + halfHourHeight + 'px;">';
                    row += '</div>\n';
                    idstr = i + '-' + j + '30';
                    row += '<div id="hourDiv' + idstr + '" class="hourDivBottom';
                    // Highlight the current day
                    if (isCurrentDay) {
                        row += ' currentDayDay'
                    }
                    // Non-working hours are gray
                    else if (j < 8 || j > 18) {
                        //row += ' nonWorkingHours';
                    }
                    row += '" style="';
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
         * TO-DO: Change from using innerHTML to DOM methods
         */
        function showMonthHeader() {
            var vS = viewStart;
            var vE = viewEnd;
            var mS = vS.getMonth();
            var mE = vE.getMonth();
            var headerDiv = monthHeaderNode; 
            var str = '';

            // Format like 'March-April, 2006'
            if (mS < mE) {
                str += vS.strftime('%B-');
                str += vE.strftime('%B %Y');
            }
            // Format like 'December 2006-January 2007'
            else if (mS > mE) {
                str += vS.strftime('%B %Y-');
                str += vE.strftime('%B %Y');
            }
            // Format like 'April 2-8, 2006'
            else {
                str += vS.strftime('%B %Y');
            }
            if (headerDiv.firstChild) {
                headerDiv.removeChild(headerDiv.firstChild);
            }
            headerDiv.appendChild(document.createTextNode(str));
        }
        
        // Do it!
        // -----------
        if (initRender) {
            // Make the all-day event area resizeable
            // --------------
            allDayArea = new ResizeArea('allDayResizeMainDiv', 'allDayResizeHandleDiv');
            allDayArea.init('down');
            allDayArea.addAdjacent('timedScrollingMainDiv');
            allDayArea.setDragLimit();
            initRender = false;
        }
        else {
            removeAllEvents();
        }
        
        init();
        showMonthHeader();
        showDayNameHeaders();
        showAllDayCols();
        showHours();
    };
    /**
     * Get the scroll offset for the timed canvas
     */
    this.getTimedCanvasScrollTop = function() {
        // Has to be looked up every time, as value may change
        // either when user scrolls or resizes all-day event area
        var top = $('timedScrollingMainDiv').scrollTop;
        // FIXME -- viewOffset is the vertical offset of the UI
        // with the top menubar added in. This should be a property
        // of render context that the canvas can look up
        top -= Cal.viewOffset;
        // Subtract change in resized all-day event area
        top -= (allDayArea.dragSize - allDayArea.origSize);
        return top;
   };
    this.getSelectedEvent = function() {
        return self.selectedEvent;
    };
    this.cleanup = function() {
        // Let's be tidy
        self.eventRegistry = null;
        allDayArea.cleanup();
    };
}
cosmo.view.cal.canvas.constructor = null;

// Cleanup
dojo.event.browser.addListener(window, "onunload", cosmo.view.cal.canvas.cleanup, false);


