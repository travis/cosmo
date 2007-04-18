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
 * @fileoverview An event lozenge being dragged or resized
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 *
 * Has two sub-classes, HasTimeDraggable and NoTimeDraggable to represent
 * the two main areas where lozenges get displayed. HasTime are normal
 * events in the scrolling area, and NoTime are the all-day events
 * in the resizeable area at the top.
 *
 * Lots of annoying math in here for calculating size/pos of the lozenge.
 * Multiple levels of vertical offset from translation of absolute XY
 * for mouse pos to relative placement of dragged divs, in addition to
 * vert offset when area scrolls.
 */

dojo.provide("cosmo.ui.draggable");
dojo.require("dojo.event.topic");
/**
 * @object An event lozenge being dragged or resized
 */
cosmo.ui.draggable.Draggable = function (id) {
    // Unused -- TO-DO: Get rid of this property
    this.id = id;
    // Unused -- TO-DO: Get rid of this property
    this.div = null;
    // Dragged/resized or not
    this.dragged = false;
    // Used in calcluations for resizing
    this.height = 0;
    // The top of the lozenge being dragged -- essentially div.offsetTop
    // because now Cal.top is always set to zero
    this.absTop = 0;
    // The offset of the left/top edges of the dragged lozenge from the
    // mouse click position -- lets you keep the lozenge a fixed pos
    // from the location of the pointer while dragging
    this.clickOffsetX = 0;
    this.clickOffsetY = 0;
    // Used for dragging multi-day timed events
    this.composite = false;
    this.clickOffsetLozengeTop = 0;
    this.clickOffsetLozengeBottom = 0;
    // Scroll offset for timed calendar canvas
    this.scrollOffset = 0;
    // Dragging vs. resizing -- 'drag', 'resizetop', or 'resizebottom'
    this.dragMode = null;
    // 30 min. increment size -- subtract 2px for stupid CSS borders
    this.unit = ((HOUR_UNIT_HEIGHT/2)-2);
    // Minimum height for resizing a single-day normal event
    this.min = 0;
    // Right-hand side limit for dragging
    this.rLimit = 0;
    /**
     * Sets up the Draggable -- putting in 'init' func allows code
     * to be shared between the sub-classes
     */
    this.init = function (dragMode) {
        // Reference to the main lozenge and div for this Draggable
        var ev = cosmo.view.cal.canvas.getSelectedEvent();
        var lozenge = ev.lozenge;
        var div = lozenge.div;

        this.dragMode = dragMode;
        // Snapshot measurements
        this.origDivLeft = div.offsetLeft;
        this.origDivWidth = div.offsetWidth;
        this.plantonicLozengeLeft = lozenge.getPlatonicLeft();
        this.platonicLozengeWidth = lozenge.getPlatonicWidth();
        this.height = div.offsetHeight;
        this.absTop = div.offsetTop + Cal.top;
        this.bottom = div.offsetTop + this.height;
        this.min = this.bottom-(HOUR_UNIT_HEIGHT/2)+2;
        this.clickOffsetX = xPos - this.origDivLeft;
        this.clickOffsetY = yPos - div.offsetTop;
        this.rLimit = (Cal.midColWidth - cosmo.view.cal.canvas.dayUnitWidth -
            SCROLLBAR_SPACER_WIDTH - 2);
        this.scrollOffset = cosmo.view.cal.canvas.getTimedCanvasScrollTop();
        if (lozenge.composite()) {
            this.composite = true;
            var list = lozenge.auxDivList;
            var last = list[list.length-1];
            var offsetBottom = last.offsetTop + last.offsetHeight;
            this.clickOffsetLozengeTop = this.getLocalMouseYPos(yPos) - div.offsetTop;
            this.clickOffsetLozengeBottom = this.getLocalMouseYPos(yPos) - offsetBottom;
        }

    };
    this.doDrag = function () {
        // Hand off to Draggable methods based on dragMode
        // Set by mouseDownHandler based on location of click

        // Set opacity effect
        this.doDragEffect('on');

        switch (this.dragMode) {
            case 'drag':
                this.move();
                break;
            case 'resizetop':
            case 'resizebottom':
                this.resize();
                break;
            default:
                // Do nothing
                break;
        }
    };
    /**
     * Move the Draggable's lozenge
     * Note that this moves the lozenge's visual position, but the
     * properties (.top, .left, .height, .width)  don't get updated
     * until the actual drop occurs
     */
    this.move = function () {
        // Compensate for crack-fiend clicking -- make sure there's
        // a selected lozenge
        if (!cosmo.view.cal.canvas.getSelectedEvent()) {
            return false;
        }
        // Not just a simple click
        this.dragged = true;
        // Lozenge associated with this Draggable
        var moveLozenge = cosmo.view.cal.canvas.getSelectedEvent().lozenge;
        // Subtract the offset to get where the left/top
        // of the lozenge should be
        var moveX = (xPos - this.clickOffsetX);
        // Right-hand move constraint
        var rLimit = this.rLimit;
        // Add drag constraints
        // -----------------------------
        moveX = moveX < 0 ? 0 : moveX; // Left bound
        moveX = moveX > rLimit ? rLimit : moveX; // Right bound
        // Move the lozenge -- don't actually reset lozenge properties
        // until drop
        moveLozenge.setLeft(moveX);
        // Multi-day timed events -- special drag behavior where
        // top of first div and bottom of last div both move
        // relative to the position of the original click
        if (this.composite) {
            var p = 0;
            p = yPos - this.clickOffsetLozengeBottom;
            this.resizeBottom(p);
            p = yPos - this.clickOffsetLozengeTop;
            this.resizeTop(p);
        }
        else {
            var moveY = (yPos - this.clickOffsetY);
            // Bottom move constraint -- need to know height of lozenge
            var bLimit = this.getBLimit(moveLozenge);
            moveY = moveY < 0 ? 0 : moveY; // Top bound
            moveY = moveY > bLimit ? bLimit : moveY; // bottom bound
            moveLozenge.setTop(moveY);
        }
    };
    /**
     * Convenience method for ensuring the size set for the
     * associated lozenge is at least the minimum of a 30-min. chunk
     */
    this.getSize = function (size) {
        var ret = 0;
        ret = parseInt(size);
        // Single-day events only
        // Min size is 30 minutes -- two 15-min. chunks
        if (!cosmo.view.cal.canvas.getSelectedEvent().lozenge.auxDivList.length) {
            ret = ret < this.unit ? this.unit : ret;
        }
        return ret;
    };
    /**
     * Main method for updating lozenge/event, called after drop
     * The lozenge properties get updated one-by-one in the drop
     * and here is where we update the event data based on the
     * updated lozenge
     * This method also calls remoteSaveMain which save the
     * changes to the event to the backend
     */
    this.doUpdate = function () {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        // Make backup snapshot of event data in case save/remove
        // operation fails
        selEv.makeSnapshot();
        // Update the event properties based on the lozenge pos/size
        if (selEv.lozenge.updateEvent(selEv, this.dragMode)) {
            // Check against the backup to make sure the event has
            // actually been edited
            if (selEv.hasChanged().count > 0) {
                //selEv.setInputDisabled(true); // Disable input while processing
                // Save the changes
                // ==========================
                dojo.event.topic.publish('/calEvent', { 'action': 'saveConfirm', 'data': selEv });
            }
            // If no real edit, then just reposition the lozenge
            // With conflict calculations and snap-to
            else {
                selEv.lozenge.updateFromEvent(selEv);
                selEv.lozenge.updateElements();
            }
        }
    };
    /**
     * Abort if the Draggable object does not point to an actual div
     * or doesn't have a valid dragMode
     */
    this.paranoia = function () {
        var ev = cosmo.view.cal.canvas.getSelectedEvent();
        if (!ev || !cosmo.app.dragItem.dragMode ||
            ev.lozenge.getInputDisabled()) {
            return false;
        }
        else {
            return true;
        }
    };
    this.doDragEffect = function (dragState) {
        var o = dragState == 'on' ? 60 : 100;
        cosmo.view.cal.canvas.getSelectedEvent().lozenge.setOpacity(o);
    };
}


Draggable = cosmo.ui.draggable.Draggable;

/**
 * HasTimeDraggable -- sub-class of Draggable
 * Normal events, 'at-time' events -- these sit in the scrollable
 * area of the main viewing area
 */
cosmo.ui.draggable.HasTimeDraggable = function (id) {
    this.id = id;
}
HasTimeDraggable = cosmo.ui.draggable.HasTimeDraggable;

HasTimeDraggable.prototype = new Draggable();

/**
 * Resizes both upward and downward -- irritating math calculations
 * caused by the fact that the position measurements for the lozenge
 * have to be set relative to the scrolling div they sit in, but the
 * X and Y for the mouse pos are absolute.
 * Of course the div they sit in can also scroll, so that adds another
 * nice layer of vertical offset.
 * Resizing up means moving the lozenge up while simultaneously
 * growing the height
 * Resizing down means just growing the height
 */
HasTimeDraggable.prototype.resize = function () {

    this.dragged = true;

    // Do intense math stuff (waves hands)
    // to figure out top and bottom positions
    // ====================================
    // Grabbing the top resize handle, resizing upward
    // Observe min. size constraints for lozenge size and top constraints
    // for sizing upward
    if (this.dragMode == 'resizetop') {
        this.resizeTop(yPos);
    }
    // Grabbing the bottom resize handle, resizing downward
    // Observe min. size constraints for lozenge size and bottom
    // constraints for sizing downward
    else if (this.dragMode == 'resizebottom') {
        this.resizeBottom(yPos);
    }
}

/**
 * Resize for top edge of normal event lozenge -- moves div
 * element up at same rate it resizes.
 */
HasTimeDraggable.prototype.resizeTop = function (y) {
    // The selected event
    var selEv = cosmo.view.cal.canvas.getSelectedEvent();
    // Where the top edge of the lozenge should go, given any offset for the
    // top of the calendar, and any scrolling in the scrollable area
    // Used when resizing up
    var t = this.getLocalMouseYPos(y);

    t = t > this.min ? this.min : t;
    t = t < 0 ? 0 : t;
    var size = this.getSize((this.absTop - yPos - this.scrollOffset)
        + this.height);

    selEv.lozenge.setHeight(size, true);
    selEv.lozenge.setTop(t);
}

/**
 * Resize for bottom edge of normal event lozenge -- simply grows
 * height of div
 */
HasTimeDraggable.prototype.resizeBottom = function (y) {
   // The selected event
    var selEv = cosmo.view.cal.canvas.getSelectedEvent();
    // Where the bottom edge of the lozenge should go -- this is a
    // relative measurement based on pos on the scrollable area
    var b = (y - this.absTop) + this.scrollOffset;
    var max = ((VIEW_DIV_HEIGHT + TOP_MENU_HEIGHT) - this.absTop);
    b = b > max ? max : b;
    var size = this.getSize(b);
    selEv.lozenge.setHeight(size);

}

/**
 * Happens on mouseup -- does snap-to in 15-min. increments,
 * and updates lozenge properties based on final lozenge position
 * The annoying CSS 1px border thing is a major irritation here --
 * it adds to the actual size, but not to the style width/height
 * After updating pos/size properties for the lozenge, this method
 * calls doUpdate to save the changes to lozenge/event and do the
 * visual update
 */
HasTimeDraggable.prototype.drop = function () {

    if (!this.dragged || !this.paranoia()) {
        return false;
    }

    var selEv = cosmo.view.cal.canvas.getSelectedEvent();
    var unit = HOUR_UNIT_HEIGHT/4; // 15-min. increments
    var top = 0;
    var size = 0;
    var left = 0;
    var deltaX = 0;
    var deltaY = 0;
    var y = 0;
    var m = 0;
    var d = 0;
    var starttime = 0;
    var endtime = 0;

    // Reset opacity to normal
    this.doDragEffect('off');

    // Abstract away getting top and bottom -- multi-day events
    // have multiple divs, treat as a composite here
    top = selEv.lozenge.getTop();
    left = selEv.lozenge.getLeft();
    size = selEv.lozenge.getBottom() - top;

    // Snap-to for top position: both simple move and resize up
    if (this.dragMode == 'drag' || this.dragMode == 'resizetop') {
        deltaY = top % unit;
        // Drag/resize is just above 15-min. increment -- snap downwards
        if (deltaY > (unit/2)) {
            top = top+(unit-deltaY);
        }
        // Drag/resize is just below 15-min. increment -- snap upwards
        else {
            top = top-deltaY;
        }
        selEv.lozenge.top = top;
    }
    // Snap-to for size: both resize up and resize down
    if (this.dragMode.indexOf('resize') > -1) {
        deltaY = size % unit; // Do snap-to
        // Subtract one pixel for 1px border on background
        size = size - 3;
        // consistent with initial creation code
        // Resize is just above 15-min. increment -- snap downwards
        if (deltaY > (unit/2)) {
            size = size+(unit-deltaY);
        }
        // Resize is just above 15-min. increment -- snap downwards
        else {
            size = size-deltaY;
        }
        selEv.lozenge.height = size;
    }
    // 1px border -- add the 1px taken out for the 1px border
    // back to the size
    // Make an even number divisible by HOUR_UNIT_HEIGHT
    size = size + 3;

    // Snap-to for lateral position
    if (this.dragMode == 'drag') {
        deltaX = left % cosmo.view.cal.canvas.dayUnitWidth;
        // Drag is just right of day-width size -- snap left
        if (deltaX > (cosmo.view.cal.canvas.dayUnitWidth/2)) {
            left = left+(cosmo.view.cal.canvas.dayUnitWidth-deltaX);
        }
        // Drag is just left of day-width size -- snap right
        else {
            left = left-deltaX;
        }
        selEv.lozenge.left = left;
    }
    // Do the actual update
    // =================
    this.doUpdate();
}

/**
 * Get the bottom constraint for moving/resizing a lozenge
 */
HasTimeDraggable.prototype.getBLimit = function (movelozenge) {
    return (VIEW_DIV_HEIGHT - movelozenge.height);
}

HasTimeDraggable.prototype.setDragWidth = function () {}

HasTimeDraggable.prototype.getLocalMouseYPos = function (y) {
    var localY = (y - Cal.top) + this.scrollOffset;
    return localY;
};

/**
 * NoTimeDraggable -- sub-class of Draggable
 * All-day events, 'any-time' events -- these sit up in the
 * resizable area at the top of the UI
 */
cosmo.ui.draggable.NoTimeDraggable = function (id) {
    this.id = id;
}

NoTimeDraggable = cosmo.ui.draggable.NoTimeDraggable;

NoTimeDraggable.prototype = new Draggable();

/**
 * Happens on mouseup -- does snap-to in 15-min. increments,
 * and updates lozenge properties based on final lozenge position
 * After updating pos/size properties for the lozenge, this method
 * calls doUpdate to save the changes to lozenge/event and do the
 * visual update
 */
NoTimeDraggable.prototype.drop = function () {

    if (!this.dragged || !this.paranoia()) {
        return false;
    }
    var top = 0;
    var left = 0;
    var deltaX = 0;
    var deltaY = 0;
    var selEv = cosmo.view.cal.canvas.getSelectedEvent();
    top = selEv.lozenge.getTop();
    left = selEv.lozenge.getLeft();

    // Reset opacity to normal
    cosmo.view.cal.canvas.getSelectedEvent().lozenge.setOpacity(100);

    // Side-to-side snap
    if (this.dragMode == 'drag') {
        deltaX = left % cosmo.view.cal.canvas.dayUnitWidth;
        // Drag is just right of day-width size -- snap left
        if (deltaX > (cosmo.view.cal.canvas.dayUnitWidth/2)) {
            left = left+(cosmo.view.cal.canvas.dayUnitWidth-deltaX);
        }
        // Drag is just left of day-width size -- snap right
        else {
            left = left-deltaX;
        }
        selEv.lozenge.left = left;
    }
    selEv.lozenge.top = top;

    // Do the actual update
    // =================
    this.doUpdate();
}

/**
 * Get the bottom constraint for moving/resizing a lozenge
 */
NoTimeDraggable.prototype.getBLimit = function (movelozenge) {
    // This area resizes vertically -- imposing a drag limit makes no sense here
    return 10000000;

}


