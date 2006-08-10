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
 * @fileoverview An event block being dragged or resized
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 *
 * Has two sub-classes, HasTimeDraggable and NoTimeDraggable to represent
 * the two main areas where blocks get displayed. HasTime are normal
 * events in the scrolling area, and NoTime are the all-day events
 * in the resizeable area at the top.
 * 
 * Lots of annoying math in here for calculating size/pos of the block.
 * Multiple levels of vertical offset from translation of absolute XY
 * for mouse pos to relative placement of dragged divs, in addition to
 * vert offset when area scrolls.
 */

/**
 * @object An event block being dragged or resized
 */
function Draggable(id) {
    // Unused -- TO-DO: Get rid of this property
    this.id = id;
    // Unused -- TO-DO: Get rid of this property
    this.div = null;
    // Dragged/resized or not
    this.dragged = false;
    // Used in calcluations for resizing
    this.height = 0;
    // The top of the block being dragged -- essentially div.offsetTop
    // because now Cal.top is always set to zero 
    this.absTop = 0;
    // The offset of the left/top edges of the dragged block from the 
    // mouse click position -- lets you keep the block a fixed pos 
    // from the location of the pointer while dragging
    this.clickOffsetX = 0;
    this.clickOffsetY = 0;
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
    this.init = function(dragMode) {
        // Reference to the main block and div for this Draggable
        var block = Cal.currSelObj.block;
        var div = block.div;
        
        this.dragMode = dragMode;
        // Snapshot measurements
        this.origDivLeft = div.offsetLeft;
        this.origDivWidth = div.offsetWidth;
        this.plantonicBlockLeft = block.getPlatonicLeft();
        this.platonicBlockWidth = block.getPlatonicWidth();
        this.height = div.offsetHeight;
        this.absTop = div.offsetTop + Cal.top;
        this.bottom = div.offsetTop + this.height;
        this.min = this.bottom-(HOUR_UNIT_HEIGHT/2)+2;
        this.clickOffsetX = xPos - this.origDivLeft;
        this.clickOffsetY = yPos - div.offsetTop;
        this.rLimit = (Cal.midColWidth - Cal.dayUnitWidth - 
            SCROLLBAR_SPACER_WIDTH - 2); 
        
        // FIXME: This causes a really irritating CSS bug in Mozilla/Firefox
        // Set opacity if moving
        //if (this.dragMode == 'drag') {
        //    Cal.currSelObj.block.setOpacity(60);
        //}
    };
    /**
     * Move the Draggable's block
     * Note that this moves the block's visual position, but the
     * properties (.top, .left, .height, .width)  don't get updated
     * until the actual drop occurs
     */
    this.move = function() {
        // Compensate for crack-fiend clicking -- make sure there's
        // a selected block
        if (!Cal.currSelObj) {
            return false;
        }
        
        // Not just a simple click
        this.dragged = true;
        
        // Block associated with this Draggable
        var moveBlock = Cal.currSelObj.block;
        // Subtract the offset to get where the left/top
        // of the block should be
        var moveX = (xPos - this.clickOffsetX);
        var moveY = (yPos - this.clickOffsetY);
        // Right-hand move constraint
        var rLimit = this.rLimit;
        // Bottom move constraint -- need to know height of block
        var bLimit = this.getBLimit(moveBlock);
        // Add drag constraints
        // -----------------------------
        moveX = moveX < 0 ? 0 : moveX; // Left bound
        moveX = moveX > rLimit ? rLimit : moveX; // Right bound
        moveY = moveY < 0 ? 0 : moveY; // Top bound
        moveY = moveY > bLimit ? bLimit : moveY; // bottom bound
        // Move the block -- don't actually reset block properties
        // until drop
        moveBlock.setLeft(moveX);
        // Don't move multi-day events vertically yet
        if (!moveBlock.auxDivList.length) {
            moveBlock.setTop(moveY);
        }
    };
    /**
     * Convenience method for ensuring the size set for the
     * associated block is at least the minimum of a 30-min. chunk
     */
    this.getSize = function(size) {
        var ret = 0;
        ret = parseInt(size);
        // Single-day events only
        // Min size is 30 minutes -- two 15-min. chunks
        if (!Cal.currSelObj.block.auxDivList.length) {
            ret = ret < this.unit ? this.unit : ret;
        }
        return ret;
    };
    /**
     * Main method for updating block/event, called after drop
     * The block properties get updated one-by-one in the drop
     * and here is where we update the event data based on the
     * updated block
     * This method also calls remoteSaveMain which save the
     * changes to the event to the backend
     */
    this.doUpdate = function() {
        var selObj = Cal.currSelObj;
        // Make backup snapshot of event data in case save/remove
        // operation fails
        selObj.makeSnapshot();
        // Update the event properties based on the block pos/size
        if (selObj.block.updateEvent(selObj, this.dragMode)) {
            // Check against the backup to make sure the event has
            // actually been edited
            if (selObj.hasChanged()) {
                selObj.setInputDisabled(true); // Disable input while processing
                // Save the changes
                // ==========================
                selObj.remoteSaveMain();
            }
            // If no real edit, then just reposition the block
            // With conflict calculations and snap-to
            else {
                selObj.block.updateFromEvent(selObj);
                selObj.block.updateElements();
            }
        }
    };
    /**
     * Abort if the Draggable object does not point to an actual div
     * or doesn't have a valid dragMode
     */
    this.paranoia = function() {
        if (!Cal.currSelObj || !Cal.dragElem.dragMode || 
            Cal.currSelObj.getInputDisabled()) {
            return false;
        }
        else {
            return true;
        }
    };
}

/**
 * HasTimeDraggable -- sub-class of Draggable
 * Normal events, 'at-time' events -- these sit in the scrollable 
 * area of the main viewing area
 */
function HasTimeDraggable(id) {
    this.id = id;
}
HasTimeDraggable.prototype = new Draggable();

/**
 * Resizes both upward and downward -- irritating math calculations
 * caused by the fact that the position measurements for the block
 * have to be set relative to the scrolling div they sit in, but the
 * X and Y for the mouse pos are absolute.
 * Of course the div they sit in can also scroll, so that adds another
 * nice layer of vertical offset.
 * Resizing up means moving the block up while simultaneously 
 * growing the height
 * Resizing down means just growing the height
 */
HasTimeDraggable.prototype.resize = function() {
    
    this.dragged = true;

    // Do intense math stuff (waves hands) 
    // to figure out top and bottom positions
    // ====================================
    // Grabbing the top resize handle, resizing upward
    // Observe min. size constraints for block size and top constraints
    // for sizing upward
    if (this.dragMode == 'resizetop') {
        this.resizeTop(yPos);
    }
    // Grabbing the bottom resize handle, resizing downward
    // Observe min. size constraints for block size and bottom
    // constraints for sizing downward 
    else if (this.dragMode == 'resizebottom') {
        this.resizeBottom(yPos);
    }
}

/**
 * Resize for top edge of normal event block -- moves div
 * element up at same rate it resizes. 
 */
HasTimeDraggable.prototype.resizeTop = function(y) {
    // The selected event
    var selObj = Cal.currSelObj;
    // Where the top edge of the block should go, given any offset for the
    // top of the calendar, and any scrolling in the scrollable area
    // Used when resizing up
    var t = (y-Cal.top)+Cal.getMainViewScrollTop();
    var size = 0;
    
    t = t > this.min ? this.min : t;
    t = t < 0 ? 0 : t;
    selObj.block.setTop(t);
    //if (!selObj.block.auxDivList.length) {
        size = this.getSize((this.absTop-yPos-Cal.getMainViewScrollTop())
            + this.height);
        
        selObj.block.setHeight(size, true);
   // }
}

/**
 * Resize for bottom edge of normal event block -- simply grows
 * height of div
 */
HasTimeDraggable.prototype.resizeBottom = function(y) {
   // The selected event
    var selObj = Cal.currSelObj;
    // Where the bottom edge of the block should go -- this is a
    // relative measurement based on pos on the scrollable area
    var b = (y-this.absTop)+Cal.getMainViewScrollTop();
    var max = (VIEW_DIV_HEIGHT - this.absTop);
    b = b > max ? max : b;
    size = this.getSize(b);
    selObj.block.setHeight(size);

}

/**
 * Happens on mouseup -- does snap-to in 15-min. increments, 
 * and updates block properties based on final block position
 * The annoying CSS 1px border thing is a major irritation here --
 * it adds to the actual size, but not to the style width/height
 * After updating pos/size properties for the block, this method
 * calls doUpdate to save the changes to block/event and do the 
 * visual update
 */
HasTimeDraggable.prototype.drop = function() {

    if (!this.dragged || !this.paranoia()) {
        return false;
    }
    
    var selObj = Cal.currSelObj;
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
    
    // Abstract away getting top and bottom -- multi-day events
    // have multiple divs, treat as a composite here
    top = selObj.block.getTop();
    left = selObj.block.getLeft();
    size = selObj.block.getBottom()-top;
    
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
        selObj.block.top = top;
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
        selObj.block.height = size;
    }
    // 1px border -- add the 1px taken out for the 1px border 
    // back to the size
    // Make an even number divisible by HOUR_UNIT_HEIGHT
    size = size + 3;
    
    // Snap-to for lateral position
    if (this.dragMode == 'drag') {
        deltaX = left % Cal.dayUnitWidth;
        // Drag is just right of day-width size -- snap left
        if (deltaX > (Cal.dayUnitWidth/2)) {
            left = left+(Cal.dayUnitWidth-deltaX);
        }
        // Drag is just left of day-width size -- snap right
        else {
            left = left-deltaX;
        }
        selObj.block.left = left;
    }
    // Do the actual update
    // =================
    this.doUpdate();
}

/**
 * Get the bottom constraint for moving/resizing a block
 */
HasTimeDraggable.prototype.getBLimit = function(moveblock) {
    return (VIEW_DIV_HEIGHT - moveblock.height);
}

HasTimeDraggable.prototype.setDragWidth = function() {}

/**
 * NoTimeDraggable -- sub-class of Draggable
 * All-day events, 'any-time' events -- these sit up in the 
 * resizable area at the top of the UI
 */
function NoTimeDraggable(id) {
    this.id = id;
}
NoTimeDraggable.prototype = new Draggable();

/**
 * Happens on mouseup -- does snap-to in 15-min. increments, 
 * and updates block properties based on final block position
 * After updating pos/size properties for the block, this method
 * calls doUpdate to save the changes to block/event and do the 
 * visual update
 */
NoTimeDraggable.prototype.drop = function() {

    if (!this.dragged || !this.paranoia()) {
        return false;
    }
    var top = 0;
    var left = 0;
    var deltaX = 0;
    var deltaY = 0;
    var selObj = Cal.currSelObj;
    top = selObj.block.getTop();
    left = selObj.block.getLeft();
    
    // Side-to-side snap
    if (this.dragMode == 'drag') {
        deltaX = left % Cal.dayUnitWidth;
        // Drag is just right of day-width size -- snap left
        if (deltaX > (Cal.dayUnitWidth/2)) {
            left = left+(Cal.dayUnitWidth-deltaX);
        }
        // Drag is just left of day-width size -- snap right
        else {
            left = left-deltaX;
        }
        selObj.block.left = left;
    }
    selObj.block.top = top;
    
    // Do the actual update
    // =================
    this.doUpdate();
}

/**
 * Get the bottom constraint for moving/resizing a block
 */
NoTimeDraggable.prototype.getBLimit = function(moveblock) {
    return (Cal.allDayArea.dragSize - moveblock.height - 8);

}

