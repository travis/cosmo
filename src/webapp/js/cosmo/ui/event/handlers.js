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
 * @fileoverview Event handlers for Cosmo. Most all UI input is handled here.
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 *
 * Rather than putting event listeners directly on the DOM elements
 * themselves, Cosmo uses meaningful IDs for all clickable elements.
 * All UI input goes through these global listeners and dispatches 
 * events to the appropriate handler based on the ID of the element 
 * being manipulated.
 * 
 * It can become a little tricky identifying exactly what element 
 * got clicked (i.e., requiring irritating regex filtering), but 
 * allows more more centralized control of the event flow.
 */
 
/**
 * Generic function to get the source elem of a UI event
 * @param e A DOM event
 * @return A DOM element that was the source of the event
 * Returns the parent node if the source was a text node
 */
function getSrcElem(e) {
    var ret = null;
    
    if (e.srcElement) ret = e.srcElement;
    else if (e.target) ret = e.target;
    
    // Disabled form elements in IE return a bogus object
    if (typeof ret.id == 'undefined') {
        return document.body;
    }
    // Look up the id of the elem or its parent
    else {
        // Look for something with an id -- not a text node
        while (!ret.id || ret.nodeType == 3) {
            ret = ret.parentNode;
        }
    }
    return ret;
};

/**
 * Double-clicks -- if event source is in the scrolling area
 * for normal events, or in the resizeable all-day event area
 * calls insertCalEventNew to create a new event
 * Eventually will also be used for edit-in-place of event info
 */
function dblClickHandler(e) {
    var strId = '';
    var objElem = null;

    e = !e ? window.event : e;
    objElem = getSrcElem(e);
    strId = objElem.id

    switch (true) {
        // On hour column -- create a new event
        case (strId.indexOf('hourDiv') > -1):
        // On all-day column -- create new all-day event
        case (strId.indexOf('allDayListDiv') > -1):
            Cal.insertCalEventNew(strId);
            break;
        // On event title -- edit-in-place
        case (strId.indexOf('eventDiv') > -1):
            // Edit-in-place will go here
            break;
    }
}

/**
 * Single-clicks -- do event dispatch based on ID of event's
 * DOM-element source. Includes selecting/moving/resizing event blocks
 * and resizing all-day event area
 * Mousedown on an event always creates a Draggable in anticipation
 * of a user dragging the block. Draggable is destroyed on mousedown.
 * The Cal's dragElem may either be set to this Draggable, or to
 * the ResizeArea for all-day events
 */
function mouseDownHandler(e) {

    var strId = '';
    var dragElem = null;
    var objElem = null;
    var selObj = null;
    var id = '';

    // =================
    // Check for client-side timeout on all mouse clicks
    // =================
    Cal.checkTimeout();

    e = !e ? window.event : e;
    objElem = getSrcElem(e);
    strId = objElem.id;

    // ======================================
    // Event dispatch
    // ======================================
    switch (true) {
        // Mouse down on the hour columns -- exit to prevent text selection
        case (strId.indexOf('hourDiv') > -1):
            return false;
            break;
        
        // On event block -- simple select, or move/resize
        case (strId.indexOf('eventDiv') > -1):
            // Get the clicked-on event
            id = Cal.getIndexEvent(strId);
            selObj = cosmo.view.cal.canvas.eventRegistry.getItem(id);

            // If this object is currently in 'processing' state, ignore any input
            if (selObj.getInputDisabled()) {
                return false;
            }
            
            // Publish selection
            dojo.event.topic.publish('/calEvent', { 'action': 'setSelected', 'data': selObj });
            
            // Set up Draggable and save dragMode -- user may be dragging
            if (strId.indexOf('AllDay') > -1) {
                dragElem = new NoTimeDraggable(id);
            }
            else {
                dragElem = new HasTimeDraggable(id);
            }
            // Set the Cal draggable to the dragged block
            Cal.dragElem = dragElem;
            switch(true) {
                // Main content area -- drag entire event
                case strId.indexOf('Content') > -1:
                case strId.indexOf('Title') > -1:
                case strId.indexOf('Start') > -1:
                    dragElem.init('drag');
                    break;
                // Top lip -- resize top
                case strId.indexOf('Top') > -1:
                    dragElem.init('resizetop');
                    break;
                // Bottom lip -- resize bottom
                case strId.indexOf('Bottom') > -1:
                    dragElem.init('resizebottom');
                    break;
                default:
                    // Do nothing
                    break;
            }

            // Update event detail form
            // ------------------------------------
            // Show clicked-on event's details in detail form
            Cal.calForm.updateFromEvent(selObj);
            // Enable both Remove and Save buttons
            Cal.calForm.setButtons(true, true);

            break;
    }
}

/**
 * Moving the mouse -- Used for dragging event blocks, 
 * or for resizing the all-day event area.
 */
function mouseMoveHandler(e) {
    var dragElem = Cal.dragElem;
    
    // Set global x-y coords
    xPos = e ? e.pageX : window.event.x;
    yPos = e ? e.pageY : (window.event.y + document.body.scrollTop);
    
    // Dragging
    if (dragElem) {
        // No dragMode -- it's the ResizeArea for all-day events
        if (!dragElem.dragMode) {
            dragElem.resize();
        }
        // Event block
        else {
            // Hand off to Draggable methods based on dragMode
            // Set by mouseDownHandler based on location of click
            switch (dragElem.dragMode) {
                case 'drag':
                    dragElem.move();
                    break;
                case 'resizetop':
                case 'resizebottom':
                    dragElem.resize();
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    }
}

/**
 * Releasing the mouse -- clicking on nav arrows, or dropping
 * after a drag
 */
function mouseUpHandler(e) {
    e = !e ? window.event : e;
    objElem = getSrcElem(e);
    strId = objElem.id;
    // Drop after dragging -- both ResizeArea or event Block
    if (Cal.dragElem) {
        Cal.dragElem.drop();
        Cal.dragElem = null;
    }
}

/**
 * All keyboard input -- includes fu for modal dialog box
 * Also had to do some hacky stuff to suppress Enter and Delete key
 * input when user is typing event detail form fields
 */
function keyUpHandler(e) {

    // =================
    // Check for client-side timeout on all keyboard input
    // =================
    // Have to return false to keep event from continuing to bubble
    // otherwise it actually ends up refreshing the session before
    // we have a chance to time out
    if (Cal.checkTimeout()) {
        return false;
    }

    e = !e ? window.event : e;
    
    // UI input is disabled, check for modal dialog box
    if (Cal.getInputDisabled()) {
        // Execute dialog's default action if user hits Enter key
        if (Cal.dialog.isDisplayed && Cal.dialog.defaultAction && 
            e.keyCode == 13) {            
            Cal.dialog.defaultAction();
        }
    }
    // Normal UI input -- Saving changes and removing events
    // (1) Needs an event to be selected
    // (2) Selected event can't be in 'procssing' state
    // (3) Don't trigger Save/Remove when typing in form fields
    // ---------------
    // TO-DO: Move redundant conditionals up a level
    else {
        var selEv = cosmo.view.cal.canvas.getSelectedEvent();
        switch (e.keyCode) {
            // Enter key
            case 13:
                if (selEv && 
                    !selEv.getInputDisabled() && 
                    Cal.calForm.detailTextInputHasFocus) {
                    //Cal.calForm.saveCalEvent();
                    dojo.event.topic.publish('/calEvent', { 'action': 'saveFromForm' });
                }
                else if (Cal.calForm.jumpToTextInputHasFocus) {
                    Cal.calForm.goJumpToDate();
                }
                break;
            // Delete key
            case 46:
                if (selEv && 
                    !selEv.getInputDisabled() && 
                    !Cal.calForm.detailTextInputHasFocus) {
                    dojo.event.topic.publish('/calEvent', { 'action': 'removeConfirm', 'data': selEv });
                    //Cal.showDialog(
                    //    cosmo.view.cal.dialog.getProps('removeConfirm'));
                }
                break;
        }
    }
}

/**
 * Do cleanup of DOM-element refs to avoid memleak in IE
 */
function cleanup() {
    //Cal.wipeView();
    Cal.cleanup();
    Cal = null;
}

