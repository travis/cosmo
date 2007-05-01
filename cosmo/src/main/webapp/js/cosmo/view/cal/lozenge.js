/* * Copyright 2006 Open Source Applications Foundation *
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
 * @fileoverview Event lozenges that represent the span of time
 * of an event on the calendar
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 *
 * Has two sub-classes, cosmo.view.cal.lozenge.HasTimeLozenge and cosmo.view.cal.lozenge.NoTimeLozenge to represent
 * the two main areas where lozenges get displayed. HasTime are normal
 * events in the scrolling area, and NoTime are the all-day events
 * in the resizeable area at the top.
 * cosmo.view.cal.lozenge.HasTimeLozenge for a multi-day event may be a composite made up of
 * a main div element and a bunch of auxilliary divs off to the side.
 */
dojo.provide('cosmo.view.cal.lozenge');

dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.view.cal");
dojo.require("cosmo.view.cal.canvas");

cosmo.view.cal.lozenge = {};

/**
 * @object A visual lozenge to represent the span of time of a calendar
 * event
 */
cosmo.view.cal.lozenge.Lozenge = function () {
    // Properties for position and size
    this.top = 0;
    this.left = 0;
    this.height = 0;
    this.width = 0;
    // Whether or not this particular Lozenge is selected.
    // FIXME: Figure out if this is still used ... ?
    this.selected = false;
    // 30-min minimum height, minus a pixel at top and bottom
    // per retarded CSS spec for borders
    this.unit = (HOUR_UNIT_HEIGHT/(60/this.minimumMinutes))-2;
    // DOM elem ref to the primary div for the Lozenge
    this.div = null;
    // DOM elem ref for inner div of the Lozenge
    this.innerDiv = null;
    // The separator plus ID -- convenience to avoid
    // concatenating the same thing over and over
    this.idPrefix = '';
    // Array of div elems appearing to the side on multi-day normal
    // events
    this.auxDivList = [];
    // If the event has a edit/remove call processing, don't allow
    // user to move/resize
    this.inputDisabled = false;
}

// The minimum *visible* height of an event Lozenge
cosmo.view.cal.lozenge.Lozenge.prototype.minimumMinutes = 30;
/**
 * Enable/disable user input for this event -- should be disabled
 * when a remote operation is processing
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setInputDisabled = function (isDisabled) {
    if (isDisabled) {
        this.inputDisabled = true;
    }
    else {
        this.inputDisabled = false;
    }
    return this.inputDisabled;
};
/**
 * Whether or not input is disabled for this event -- usually
 * because of a remote operation processing for the event
 */
cosmo.view.cal.lozenge.Lozenge.prototype.getInputDisabled = function () {
    return this.inputDisabled;
};
/**
 * Convenience method that does all the visual update stuff
 * for a lozenge at one time
 */
cosmo.view.cal.lozenge.Lozenge.prototype.updateDisplayMain = function () {
    this.updateElements();
    this.hideProcessing();
    this.updateText();
};
/**
 * Updates the info displayed on a lozenge for the event time
 * and description
 */
cosmo.view.cal.lozenge.Lozenge.prototype.updateText = function () {
    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(this.id);
    var d = ev.data;
    var strtime = dojo.date.strftime(d.start, '%I:%M%p');
    // Trim leading zero if need be
    strtime = strtime.indexOf('0') == 0 ? strtime.substr(1) : strtime;
    // Display timezone info for event if it has one
    if (d.start.tzId) {
        strtime += ' (' + d.start.getTimezoneAbbrName() + ')';
    }
    var timeDiv = document.getElementById(this.divId + 'Start' +
        cosmo.app.pim.ID_SEPARATOR + ev.id);
    var titleDiv = document.getElementById(this.divId + 'Title' +
        cosmo.app.pim.ID_SEPARATOR + ev.id);
    if (timeDiv) {
        this.setText(timeDiv, strtime);
    }
    this.setText(titleDiv, d.title);
};
/**
 * A bit of a misnomer -- just static text at the moment
 * FIXME: Add animation -- either GIF or using CSS effects
 */
cosmo.view.cal.lozenge.Lozenge.prototype.showStatusAnim = function () {
    var titleDiv = document.getElementById(this.divId + 'Title' +
        cosmo.app.pim.ID_SEPARATOR + this.id);
    this.setText(titleDiv, 'Processing ...');
};
/**
 * Toggle cursor to 'default' while lozenge is in processing
 * state -- should not appear to be draggable
 */
cosmo.view.cal.lozenge.Lozenge.prototype.mainAreaCursorChange = function (isProc) {
    var cursorChange = '';
    // Read-only collection -- clickable but not draggable/resizable
    if (!cosmo.app.pim.currentCollection.privileges.write) {
        cursorChange = 'pointer';
    }
    // Writeable collection -- drag/resize cursors
    else {
        cursorChange = isProc ? 'progress' : 'move';
    }
    document.getElementById(this.divId + 'Content' +
        cosmo.app.pim.ID_SEPARATOR + this.id).style.cursor = cursorChange;
};
cosmo.view.cal.lozenge.Lozenge.prototype.getPlatonicLeft = function () {
    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(this.id);
    var diff = cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
        cosmo.app.pim.viewStart, ev.data.start);
    return (diff * cosmo.view.cal.canvas.dayUnitWidth);

};
cosmo.view.cal.lozenge.Lozenge.prototype.getPlatonicWidth = function () {
    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(this.id);
    var diff = (cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
        ev.data.start, ev.data.end))+3;
    return (diff * cosmo.view.cal.canvas.dayUnitWidth);
}
/**
 * Cross-browser wrapper for setting CSS opacity
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setOpacity = function (opac) {

    function setOpac(elem, o) {
        // =============
        // opac is a whole number to be used as the percent opacity
        // =============
        // IE uses a whole number as a percent (e.g. 75 for 75%)
        //  Moz/compat uses a fractional value (e.g. 0.75)
        var nDecOpacity = o/100;
        if (document.all) {
            elem.style.filter = 'alpha(opacity=' + o + ')';
        }
        elem.style.opacity = nDecOpacity;

    }
    setOpac(this.div, opac);
    if (this.composite()) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            setOpac(this.auxDivList[i], opac);
        }
    }
}
/**
 * Use DOM to set text inside a node
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setText = function (node, str) {
    if (node.firstChild) {
        node.removeChild(node.firstChild);
    }
    node.appendChild(document.createTextNode(str));
};
/**
 * Change color of lozenge to indicate (1) selected (2) normal
 * or (3) processing
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setState = function (isProc) {
    var isSel = null;
    var stateId = 0; // 1 selected 2 normal 3 processing
    // If this lozenge is processing, change to 'processing' color
    if (isProc) {
        stateId = 3;
    }
    // Change lozenge back after completing processing --
    // change it back to normal 'unselected' color, or
    // 'selected' color if this lozenge is the most recently
    // clicked one
    else {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        stateId = (selEv && (this.id == selEv.id)) ?
            1 : 2;
    }
    this.setLozengeAppearance(stateId);
}
/**
 * Change color of lozenge to indicate (1) selected (2) processing
 * or (3) normal, unselected
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setLozengeAppearance = function (stateId) {

    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(this.id);
    var useLightColor = this.useLightColor(ev);
    var imgPath = '';
    var textColor = '';
    var borderColor = '';
    var borderStyle = 'solid';
    var lozengeColor = '';
    var mainDiv = document.getElementById(this.divId + cosmo.app.pim.ID_SEPARATOR +
        this.id);
    var timeDiv = document.getElementById(this.divId + 'Start' +
        cosmo.app.pim.ID_SEPARATOR + ev.id);
    var titleDiv = document.getElementById(this.divId + 'Title' +
        cosmo.app.pim.ID_SEPARATOR + ev.id);
    colors = cosmo.view.cal.canvas.colors;

    // If this lozenge is processing, change to 'processing' color
    switch (stateId) {
        // Selected
        case 1:
            if (useLightColor) {
                textColor = colors['darkSel'];
                borderColor = colors['darkUnsel'];
                lozengeColor = colors['lightSel'];
                imgPath = '';
            }
            else {
                textColor = '#ffffff';
                borderColor = '#ffffff';
                lozengeColor = colors['darkSel'];
                imgPath = cosmo.env.getImagesUrl() + 'block_gradient_dark.png';
            }
            break;
        // Unselected
        case 2:
            if (useLightColor) {
                textColor = colors['darkSel'];
                borderColor = colors['darkUnsel'];
                lozengeColor = colors['lightUnsel'];
                imgPath = '';
            }
            else {
                textColor = '#ffffff';
                borderColor = '#ffffff';
                lozengeColor = colors['darkUnsel'];
                imgPath = cosmo.env.getImagesUrl() + 'block_gradient_light.png';
            }
            break;
        // Processing
        case 3:
            textColor = '#ffffff';
            borderColor = '#ffffff';
            lozengeColor = colors['proc'];
            imgPath = '';
            break;
        default:
            // Do nothing
            break;
    }

    if (ev.data.status && ev.data.status.indexOf('TENTATIVE') > -1) {
        borderStyle = 'dashed';
    }

    // Main div for lozenge
    // ------------
    mainDiv.style.color = textColor;
    mainDiv.style.borderColor = borderColor;
    mainDiv.style.backgroundColor = lozengeColor;
    mainDiv.style.borderStyle = borderStyle;
    // Using the AlphaImageLoader hack b0rks normal z-indexing
    // No pretty transparent PNGs for IE6 -- works nicely in IE7
    //if (!(dojo.render.html.ie && !dojo.render.html.ie7)) { // Wait for 0.4
    if (!(document.all && navigator.appVersion.indexOf('MSIE 7') == -1)) {
        if (imgPath) {
            mainDiv.style.backgroundImage = 'url(' + imgPath + ')';
        }
        else {
            mainDiv.style.backgroundImage = '';

        }
    }

    // Text colors
    // ------------
    if (timeDiv) {
        timeDiv.style.color = textColor;
    }
    titleDiv.style.color = textColor;

    // Aux divs for multi-day events
    // ------------
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            auxDiv.style.color = textColor;
            auxDiv.style.borderColor = borderColor;
            auxDiv.style.backgroundColor = lozengeColor;
            auxDiv.style.borderStyle = borderStyle;
            // Use transparent PNG background in non-IE6 browsers
            //if (!(dojo.render.html.ie && !dojo.render.html.ie7)) { // Wait for 0.4
            if (!(document.all && navigator.appVersion.indexOf('MSIE 7') == -1)) {
                if (imgPath) {
                    auxDiv.style.backgroundImage = 'url(' + imgPath + ')';
                }
                else {
                    auxDiv.style.backgroundImage = '';

                }
            }
        }
    }
}
/**
 * Use light or dark pallette colors
 */
cosmo.view.cal.lozenge.Lozenge.prototype.useLightColor = function (ev) {
    var ret = false;
    switch(true) {
        // 'FYI' events
        case (ev.data.status && ev.data.status == EventStatus.FYI):
        // @-time events
        case (!ev.data.allDay && (ev.data.start.getTime() == ev.data.end.getTime())):
        // Anytime events
        case (ev.data.anyTime == true):
            ret = true;
            break;
        default:
            // Do nothing
            break;
    }
    return ret;
};
/**
 * Make the lozenge look selected -- change color and
 * move forward to z-index of 25
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setSelected = function () {
    var auxDiv = null;

    this.setLozengeAppearance(1);

    // Set the z-index to the front
    this.div.style.zIndex = 25;
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            //auxDiv.style.background = SEL_BLOCK_COLOR;
            auxDiv.style.zIndex = 25;
        }
    }
}
/**
 * Make the lozenge look unselected -- change color and
 * move back to z-index of 1
 */
cosmo.view.cal.lozenge.Lozenge.prototype.setDeselected = function () {
    var auxDiv = null;

    this.setLozengeAppearance(2);

    // Set the z-index to the back
    this.div.style.zIndex = 1;
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            //auxDiv.style.background = UNSEL_BLOCK_COLOR;
            auxDiv.style.zIndex = 1;
        }
    }
}

/**
 * cosmo.view.cal.lozenge.HasTimeLozenge -- sub-class of Lozenge
 * Normal events, 'at-time' events -- these sit in the scrollable
 * area of the main viewing area
 */
cosmo.view.cal.lozenge.HasTimeLozenge = function (id) {
    this.id = id;
}
cosmo.view.cal.lozenge.HasTimeLozenge.prototype = new cosmo.view.cal.lozenge.Lozenge();

// Div elem prefix -- all component divs of normal event lozenges
// begin with this
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.divId = 'eventDiv';
// Does a multi-day event start before the viewable area
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.startsBeforeViewRange = false;
// Does a multi-day event extend past the viewable area
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.endsAfterViewRange = false;

/**
 * Change lozenge color to 'processing' color
 * Change the cursors for the resize handles at top and bottom,
 * and for the central content div
 * Display status animation
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.showProcessing = function () {
    this.setState(true);
    this.resizeHandleCursorChange(true);
    this.mainAreaCursorChange(true);
    this.showStatusAnim();
}

/**
 * Change the cursors for the resize handles at top and bottom
 * Change to 'default' when processing so it won't look draggable
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.resizeHandleCursorChange = function (isProc) {
    var topChange = '';
    var bottomChange = '';
    // Read-only collection -- clickable but not draggable/resizable
    if (!cosmo.app.pim.currentCollection.privileges.write) {
        topChange = 'pointer';
        bottomChange = 'pointer';
    }
    // Writeable collection -- drag/resize cursors
    else {
        topChange = isProc ? 'default' : 'n-resize';
        bottomChange = isProc ? 'default' : 's-resize';
    }
    var topDiv = document.getElementById(this.divId + 'Top' +
        cosmo.app.pim.ID_SEPARATOR + this.id);
    var bottomDiv = document.getElementById(this.divId + 'Bottom' +
        cosmo.app.pim.ID_SEPARATOR + this.id);
    topDiv.style.cursor = topChange;
    // Timed events that extend beyond the viewable area
    // will not have a bottom resize handle
    if (bottomDiv) {
        bottomDiv.style.cursor = bottomChange;
    }
}

/**
 * Return the lozenge to normal after processing
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.hideProcessing = function () {
    this.resizeHandleCursorChange(false);
    this.mainAreaCursorChange(false);
    this.setState(false);
}

/**
 * Update the lozenge properties from an event
 * Called when editing from the form, or on drop after resizing/dragging
 * the lozenge has to be updated to show the changes to the event
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.updateFromEvent = function (ev) {
    var unit = HOUR_UNIT_HEIGHT/2;
    var startPos = 0;
    var endPos = 0;
    var height = 0;
    var left = 0;
    var width = 0;

    // Events edited out of range
    // Move the lozenge from view -- if the update fails, we need
    // to put it back
    if (ev.isOutOfViewRange()) {
       startPos = -10000;
       height = 1;
       left = -10000;
       width = 1;
    }
    // Events still on the canvas
    else {
        if (this.startsBeforeViewRange) {
            startPos = 0;
            left = 0;
        }
        else {
            var formatStartTime = ev.data.start.strftimeLocalTimezone('%H:%M');
            startPos = cosmo.view.cal.canvas.calcPosFromTime(formatStartTime, 'start');
            left = (ev.data.start.getLocalDay())*cosmo.view.cal.canvas.dayUnitWidth;
        }
        var formatEndTime = ev.data.end.strftimeLocalTimezone('%H:%M');
        endPos = cosmo.view.cal.canvas.calcPosFromTime(formatEndTime, 'end');


        var w = cosmo.view.cal.canvas.dayUnitWidth;
        // Available indention space is 20% of the day width
        var g = w * 0.2;
        // Divide the available indention space among the
        // lozenges in this contiguous set of overlapping events
        g = ev.maxDepth ? (g / ev.maxDepth) : g;

        height = endPos - startPos;
        left += (ev.conflictDepth * g);
        left = parseInt(left);
        width = w - (ev.maxDepth * g);
        width = parseInt(width);

        // Set min height if not multi-day event
        // Make sure when updating that this min lozenge
        // height doesn't get applied back to the actual event
        if (!this.auxDivList.length && (height < unit)) {
            height = unit;
        }
    }
    this.left = left;
    this.top = startPos;
    // Show one-pixel border of underlying divs
    // And one-pixel border for actual lozenge div
    // (1 + (2 * 1)) = 3 pixels
    this.height = height - 3;
    this.width = width - 3;

}

/**
 * Update an event from changes to the lozenge -- usually called
 * when an event lozenge is dragged or resized
 * The updated event is then passed back to the backend for saving
 * If the save operation fails, the event can be restored from
 * the backup copy of the CalEventData in the event's dataOrig property
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.updateEvent = function (ev, dragMode) {

    var evStart = cosmo.view.cal.canvas.calcDateFromPos(this.left);
    var diff = this.auxDivList.length;
    var evEnd = cosmo.datetime.Date.add(evStart, dojo.date.dateParts.DAY, diff);
    var startTime = cosmo.view.cal.canvas.calcTimeFromPos(this.top);
    // Add +1 to height for border on background
    // Add +2 to height for border on lozenge div
    var endTime = cosmo.view.cal.canvas.calcTimeFromPos(this.top+(this.height + 3));

    var t = cosmo.datetime.util.parseTimeString(startTime);
    evStart.setHours(t.hours);
    evStart.setMinutes(t.minutes);
    var t = cosmo.datetime.util.parseTimeString(endTime);
    evEnd.setHours(t.hours);
    evEnd.setMinutes(t.minutes);

    // If the event was originally less than the minimum *visible* lozenge
    // height, preserve the original times when editing
    var origLengthMinutes = cosmo.datetime.Date.diff(dojo.date.dateParts.MINUTE,
        ev.dataOrig.start, ev.dataOrig.end);
    var newLengthMinutes = cosmo.datetime.Date.diff(dojo.date.dateParts.MINUTE,
        evStart, evEnd);

    if (origLengthMinutes < this.minimumMinutes && newLengthMinutes == this.minimumMinutes) {
       evEnd.setHours(evStart.getHours());
       // JS Dates do intelligent wraparound
       evEnd.setMinutes(evStart.getMinutes() + origLengthMinutes);
    }

    // Update cosmo.datetime.Date with new UTC values
    ev.data.start.updateFromUTC(evStart.getTime());
    ev.data.end.updateFromUTC(evEnd.getTime());
    return true;
}

/**
 * Insert a new event lozenge
 * This method places the lozenge (single- or multi-div) on the
 * scrollable area for normal events. This just puts them on the
 * canvas in a hidden state. After this we have two more steps:
 * (1) Update lozenge to reflect event's times using updateFromEvent
 * (2) Do sizing/positioning, and turn on visibility with updateDisplayMain
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.insert = function (id) {

    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(id);
    var startDay = 0;
    var endDay = 0;
    var auxDivCount = 0;
    var lozengeDiv = null;
    var lozengeDivSub = null;
    var d = null;
    var view = null;

    if (ev.startsBeforeViewRange()) {
        startDay = 0;
        this.startsBeforeViewRange = true;
    }
    else {
        startDay = ev.data.start.getLocalDay();
    }
    if (ev.endsAfterViewRange()) {
        endDay = 6;
        this.endsAfterViewRange = true;
    }
    else {
        endDay = ev.data.end.getLocalDay();
        if (ev.data.end.getHours() == 0) {
            endDay--;
        }
    }
    auxDivCount = (endDay - startDay);

    this.idPrefix = cosmo.app.pim.ID_SEPARATOR + id;
    this.width = 1;
    this.auxDivList = [];

    // Append event lozenge to appropriate screen area for the type of Lozenge
    view = document.getElementById('timedContentDiv');

    lozengeDiv = document.createElement('div');
    lozengeDivSub = document.createElement('div');

    // Event lozenge main div and components
    // -----------------------
    // Main lozenge div
    lozengeDiv.id = this.divId + this.idPrefix;
    lozengeDiv.className = 'eventLozenge';
    lozengeDiv.style.width = this.width + 'px';

    /*
    // Just a small little bit of fun to change the border style of a lozenge
    // depending on the status of the event.
    if (ev.data.status && ev.data.status.indexOf('TENTATIVE') > -1) {
        lozengeDiv.style.borderStyle = 'dashed';
    }
    */

    // Resize-up handle
    lozengeDivSub.id = this.divId + 'Top' + this.idPrefix;
    lozengeDivSub.className = 'eventResizeTop';
    lozengeDivSub.style.height = BLOCK_RESIZE_LIP_HEIGHT + 'px';
    lozengeDiv.appendChild(lozengeDivSub);

    // Central content area
    lozengeDivSub = document.createElement('div');
    lozengeDivSub.id = this.divId + 'Content' + this.idPrefix;
    lozengeDivSub.className = 'eventContent';
    lozengeDivSub.style.marginLeft = BLOCK_RESIZE_LIP_HEIGHT + 'px';
    lozengeDivSub.style.marginRight = BLOCK_RESIZE_LIP_HEIGHT + 'px';

    // Start time display
    d = document.createElement('div');
    d.id = this.divId + 'Start' + this.idPrefix;
    d.className = 'eventTime';
    d.style.width = '100%'; // Needed for IE, which sucks
    lozengeDivSub.appendChild(d);

    // Title
    d = document.createElement('div');
    d.id = this.divId + 'Title' + this.idPrefix
    d.className = 'eventTitle';
    d.style.width = '100%'; // Needed for IE, which sucks
    lozengeDivSub.appendChild(d);

    lozengeDiv.appendChild(lozengeDivSub);

    // Before adding the bottom resize handle, add any intervening
    // auxilliary div elems for multi-day events
    // ------------------
    // Multi-day events -- for events that extend past the end of
    // the week, truncate number of added div elements
    // auxDivCount = auxDivCount > maxDiff ? maxDiff : auxDivCount;
    if (auxDivCount) {
        for (var i = 0; i < auxDivCount; i++) {
            // Append previous div
            view.appendChild(lozengeDiv);

            var lozengeDiv = document.createElement('div');
            lozengeDiv.id = this.divId + cosmo.app.pim.ID_SEPARATOR +
                'aux' + (i+1) + this.idPrefix;
            lozengeDiv.className = 'eventLozenge';
            lozengeDiv.style.width = this.width + 'px';

            // Central content area
            lozengeDivSub = document.createElement('div');
            lozengeDivSub.id = this.divId + 'Content' + this.idPrefix;
            lozengeDivSub.className = 'eventContent';
            lozengeDiv.appendChild(lozengeDivSub);

            // Don't set height to 100% for empty content area of last aux div
            // It has resize handle at the bottom, so empty content area
            // gets an absolute numeric height when the Lozenge gets placed and
            // sized in updateFromEvent
            if (this.endsAfterViewRange || (i < (auxDivCount-1))) {
                lozengeDivSub.style.height = '100%';
            }
        }
    }

    // Resize-down handle -- append either to single div,
    // or to final div for multi-day event -- don't append when
    // event extends past view area
    if (!this.endsAfterViewRange) {
        lozengeDivSub = document.createElement('div');
        lozengeDivSub.id = this.divId + 'Bottom' + this.idPrefix;
        lozengeDivSub.className = 'eventResizeBottom';
        lozengeDivSub.style.height = BLOCK_RESIZE_LIP_HEIGHT + 'px';
        lozengeDiv.appendChild(lozengeDivSub);
    }

    view.appendChild(lozengeDiv);

    // DOM node references
    this.div = document.getElementById(this.divId + this.idPrefix);
    this.innerDiv = document.getElementById(this.divId + 'Content' +
        this.idPrefix);
    // DOM node refs for multi-day divs
    if (auxDivCount) {
        for (var i = 0; i < auxDivCount; i++) {
            this.auxDivList[i] = document.getElementById(this.divId +
                cosmo.app.pim.ID_SEPARATOR + 'aux' + (i+1) + this.idPrefix);
        }
    }
    // All done
    return this.div;
}

/**
 * Removes the lozenge -- including multiple divs for multi-day events
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.remove = function (id) {
    this.innerDiv.parentNode.removeChild(this.innerDiv);
    this.div.parentNode.removeChild(this.div);
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            auxDiv.parentNode.removeChild(auxDiv);
            this.auxDivList[i] = null;
        }
    }
    // Close IE memleak hole
    this.div = null;
    this.innerDiv = null;
}

/**
 * Move the left side of the lozenge to the given pixel position
 * *** Note: the pos is passed in instead of using the property
 * because during dragging, we don't continuously update the
 * lozenge properties -- we only update them on drop ***
 * @param pos The X pixel position for the lozenge
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.setLeft = function (pos) {
    var leftPos = parseInt(pos);
    var auxDiv = null;
    this.div.style.left = leftPos + 'px';
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            leftPos += cosmo.view.cal.canvas.dayUnitWidth;
            auxDiv = this.auxDivList[i];
            auxDiv.style.left = leftPos + 'px';
        }
    }
}

/**
 * Move the top side of the lozenge to the given pixel position
 * *** Note: the pos is passed in instead of using the property
 * because during dragging, we don't continuously update the
 * lozenge properties -- we only update them on drop ***
 * @param pos The Y pixel position for the lozenge
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.setTop = function (pos) {
    this.div.style.top = parseInt(pos) + 'px';
}

/**
 *
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.setWidth = function (width) {
    var w = parseInt(width);
    this.div.style.width = w + 'px';
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            auxDiv.style.width = w + 'px';
        }
    }
}

/**
 * Sizes an event lozenge vertically -- or the starting and ending
 * lozenges for a multi-day event. Note: in the case of a
 * multi-day event where the start time is later than the end time,
 * you will have a NEGATIVE value for 'size', which is WHAT YOU WANT.
 * @param size Int difference in start and end positions of the
 * event lozenge, or of start and end lozenges for a multi-day event
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.setHeight = function (size, overrideMulti) {
    var doMulti = ((this.auxDivList.length || this.endsAfterViewRange)
        && !overrideMulti);
    var mainSize = 0;
    var lastAuxSize = 0;

    // Do the head-scratching math stuff
    // -----------------------------------
    // Multi-day event
    if (doMulti) {
        // Height applied to FIRST div -- this div should stretch
        // all the rest of the way to the bottom of the scrolling area
        mainSize = (VIEW_DIV_HEIGHT - this.top);
        // Height applied to FINAL div -- this div should stretch
        // from the top of the scrolling area to the bottom of where the
        // normal size would be for a single-day event
        lastAuxSize = (this.top + size);
        lastAuxSize = lastAuxSize < this.unit ? this.unit : lastAuxSize;
    }
    // Single-day event
    else {
        // Set height for single div using the passed-in size
        size = size < this.unit ? this.unit : size;
        mainSize = size;
    }

    // Set the values
    // -----------------------------------
    // Main div and the inner content div
    this.div.style.height = mainSize + 'px';
    this.innerDiv.style.height =
        (mainSize-(BLOCK_RESIZE_LIP_HEIGHT*2)) + 'px';
    // If multi-day event, do the inner aux divs and final aux div
    if (doMulti) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            // Inner aux div(s)
            if (this.endsAfterViewRange || (i < (this.auxDivList.length-1))) {
                auxDiv.style.height = VIEW_DIV_HEIGHT + 'px';
            }
            // Final aux div
            else if (i == (this.auxDivList.length-1)) {
                // Main outer div
                auxDiv.style.height = lastAuxSize + 'px';
                // Empty internal content div
                auxDiv.firstChild.style.height =
                    (lastAuxSize-BLOCK_RESIZE_LIP_HEIGHT) + 'px';
            }
        }
    }
}

/**
 * Position and resize the lozenge, and turn on its visibility
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.updateElements = function () {
    this.setLeft(this.left);
    this.setTop(this.top);
    this.setHeight(this.height);
    this.setWidth(this.width);
    this.makeVisible();
}

cosmo.view.cal.lozenge.HasTimeLozenge.prototype.makeVisible = function () {
    // Turn on visibility for all the divs
    this.div.style.visibility = 'visible';
    if (this.auxDivList.length) {
        for (var i = 0; i < this.auxDivList.length; i++) {
            auxDiv = this.auxDivList[i];
            auxDiv.style.visibility = 'visible';
        }
    }
}

/**
 * Get the pixel position of the top of the lozenge div, or for
 * the far-left div in a multi-day event
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.getTop = function () {
    var t = this.div.offsetTop;
    return parseInt(t);
}

/**
 * Get the pixel posiiton of the bottom of the lozenge div, or for
 * the far-right div in a multi-day event
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.getBottom = function () {

    var t = 0;
    var h = 0;
    var lastAux = null;
    var ret = 0;

    // Multi-day event
    if (this.auxDivList.length) {
        lastAux = this.auxDivList[this.auxDivList.length-1];
        ret = parseInt(lastAux.offsetHeight);
    }
    // Single-day event
    else {
        t = this.div.offsetTop;
        h = this.div.offsetHeight;
        ret = parseInt(t+h);
    }
    return ret;
}

/**
 * Get the pixel position of the far-left edge of the event lozenge
 * or lozenges in a muli-day event
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.getLeft = function () {
    var l = this.div.offsetLeft;
    return parseInt(l);
}

/**
 * Is this a timed even that spans multiple days?
 */
cosmo.view.cal.lozenge.HasTimeLozenge.prototype.composite = function () {
    return this.auxDivList.length ? true : false;
}

/**
 * cosmo.view.cal.lozenge.NoTimeLozenge -- sub-class of Lozenge
 * All-day events, 'any-time' events -- these sit up in the
 * resizable area at the top of the UI
 */
cosmo.view.cal.lozenge.NoTimeLozenge = function (id) {
    this.id = id;
}
cosmo.view.cal.lozenge.NoTimeLozenge.prototype = new cosmo.view.cal.lozenge.Lozenge();

// All-day events are a fixed height --
// I just picked 16 because it looked about right
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.height = 16;
// Div elem prefix -- all component divs of normal event lozenges
// begin with this
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.divId = 'eventDivAllDay';

/**
 * Change lozenge color to 'processing' color
 * Change the cursors for the resize handles at top and bottom,
 * and for the central content div
 * Display status animation
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.showProcessing = function () {
    this.setState(true);
    this.mainAreaCursorChange(true);
    this.showStatusAnim();
}

/**
 * Return the lozenge to normal after processing
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.hideProcessing = function () {
    this.setState(false);
    this.mainAreaCursorChange(false);
}

/**
 * Update the lozenge properties from an event
 * Called when editing from the form, or on drop after resizing/dragging
 * the lozenge has to be updated to show the changes to the event
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.updateFromEvent = function (ev, temp) {
    var diff = cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
        ev.data.start, ev.data.end) + 1;
    this.left = this.getPlatonicLeft();
    this.width = (diff*cosmo.view.cal.canvas.dayUnitWidth)-3;
    if (!temp) {
        this.top = ev.allDayRow*19;
    }
}

/**
 * Update an event from changes to the lozenge -- usually called
 * when an event lozenge is dragged or resized
 * The updated event is then passed back to the backend for saving
 * If the save operation fails, the event can be restored from
 * the backup copy of the CalEventData in the event's dataOrig property
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.updateEvent = function (ev, dragMode) {
    // Dragged-to date
    var evDate = cosmo.view.cal.canvas.calcDateFromPos(this.left);
    // Difference in days
    var diff = cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
        ev.data.start, evDate);
    // Increment start and end by number of days
    // User can't resize all-day events
    ev.data.start = cosmo.datetime.Date.add(ev.data.start,
        dojo.date.dateParts.DAY, diff);
    ev.data.end = cosmo.datetime.Date.add(ev.data.end,
        dojo.date.dateParts.DAY, diff);
    return true;
}

/**
 * Calculate the width of an all-day event lozenge -- for events that
 * have an end past the current view span, make sure the width truncates
 * at the end of the view span properly -- this is currently hard-coded
 * to Saturday.
 * FIXME: Check the view type to figure out the end of the view span
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.calcWidth = function (startDay, ev) {

    var diff = 0;
    var maxDiff = (7-startDay);
    var width = 0;

    diff = (cosmo.datetime.Date.diff(dojo.date.dateParts.DAY,
        ev.data.start, ev.data.end))+1;

    diff = (diff > maxDiff) ? maxDiff : diff;
    width = (diff*cosmo.view.cal.canvas.dayUnitWidth)-1;

    return width;
}

/**
 * Insert a new event lozenge
 * This method places the lozenge on the resizable area for
 * all-day events. This just puts them on the canvas in a hidden state.
 * After this we have two more steps:
 * (1) Update lozenge to reflect event's times using updateFromEvent
 * (2) Do sizing/positioning, and turn on visibility with updateDisplayMain
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.insert = function (id) {
    var ev = cosmo.view.cal.canvas.eventRegistry.getItem(id);
    var lozengeDiv = document.createElement('div');
    var lozengeDivSub = document.createElement('div');
    var d = null;
    var view = null;

    this.idPrefix = cosmo.app.pim.ID_SEPARATOR + id;
    this.width = 1;

    // Append event lozenge to appropriate screen area for the type of Lozenge
    view = document.getElementById('allDayContentDiv');
    // Event lozenge main div and components
    // -----------------------
    // Main lozenge div
    lozengeDiv.id = this.divId + this.idPrefix;
    lozengeDiv.className = 'eventLozenge';
    // Set other style props separately because setAttribute() is broken in IE
    lozengeDiv.style.width = this.width + 'px';

    // Central content area
    lozengeDivSub.id = this.divId + 'Content' + this.idPrefix;
    lozengeDivSub.className = 'eventContent';
    lozengeDivSub.style.whiteSpace = 'nowrap';

    // Title
    d = document.createElement('div');
    d.id = this.divId + 'Title' + this.idPrefix;
    d.className = 'eventTitle';
    d.style.marginLeft = BLOCK_RESIZE_LIP_HEIGHT + 'px';
    lozengeDivSub.appendChild(d);

    lozengeDiv.appendChild(lozengeDivSub);

    view.appendChild(lozengeDiv);

    // DOM node references
    this.div = document.getElementById(this.divId + this.idPrefix);
    this.innerDiv = document.getElementById(this.divId + 'Content' +
        this.idPrefix);

    return this.div;
}

/**
 * Removes the lozenge
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.remove = function (id) {
    this.innerDiv.parentNode.removeChild(this.innerDiv);
    this.div.parentNode.removeChild(this.div);
    this.div = null;
    this.innerDiv = null;
}

/**
 * Move the left side of the lozenge to the given pixel position
 * *** Note: the pos is passed in instead of using the property
 * because during dragging, we don't continuously update the
 * lozenge properties -- we only update them on drop ***
 * @param pos The X pixel position for the lozenge
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.setLeft = function (pos) {
    this.div.style.left = parseInt(pos) + 'px';
}

/**
 * Move the top side of the lozenge to the given pixel position
 * *** Note: the pos is passed in instead of using the property
 * because during dragging, we don't continuously update the
 * lozenge properties -- we only update them on drop ***
 * @param pos The Y pixel position for the lozenge
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.setTop = function (pos) {
    this.div.style.top = parseInt(pos) + 'px';
}

/**
 * Sets the pixel width of the all-day event lozenge's
 * div element
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.setWidth = function (width) {
    this.div.style.width = parseInt(width) + 'px';
    // Needed for IE not to push the content out past
    // the width of the containing div
    this.innerDiv.style.width = parseInt(
        width - (BLOCK_RESIZE_LIP_HEIGHT*2)) + 'px';
}

/**
 * FIXME: Figure out if this is needed anymore -- aren't these
 * a fixed height?
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.setHeight = function (size) {
    size = parseInt(size);
    this.div.style.height = size + 'px';
    this.innerDiv.style.height = size + 'px';
}

/**
 * Position and resize the lozenge, and turn on its visibility
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.updateElements = function () {
    this.setLeft(this.left);
    this.setTop(this.top);
    this.setHeight(this.height);
    this.setWidth(this.width);
    this.makeVisible();
}

cosmo.view.cal.lozenge.NoTimeLozenge.prototype.makeVisible = function () {
    this.div.style.visibility = 'visible';
}

/**
 * FIXME: Figure out if this is needed anymore
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.getTop = function () {
    var t = this.div.offsetTop;
    return parseInt(t);
}

/**
 * FIXME: Figure out if this is needed anymore
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.getBottom = function () {
    var t = this.div.offsetTop;
    var h = this.div.offsetHeight;
    return parseInt(t+h);
}

/**
 * FIXME: Figure out if this is needed anymore
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.getLeft = function () {
    var l = this.div.offsetLeft;
    return parseInt(l);
}

/**
 * Non-timed events are never composed of multiple divs
 */
cosmo.view.cal.lozenge.NoTimeLozenge.prototype.composite = function () {
    return false;
}

