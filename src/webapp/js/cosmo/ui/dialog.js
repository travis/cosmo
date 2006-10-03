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
 * @fileoverview Displays dialog box with message and buttons
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 *
 * Displays a formatted HTML message, and contains a button panel
 * at the bottom with three clusters of buttons -- left, center,
 * and right. The defaultAction property should point to a JS
 * function which gets executed if the user hits the Enter key
 * while the dialog box is displayed. This action should correspond
 * to the handler for whatever button is bottom-right (for multiple
 * buttons) or the centered single button if there is only one
 */

/**
 * @object A dialog box with a message and buttons at the bottom
 */
function Dialog() {

    // Dialog types
    this.INFO = 'info',
    this.ERROR = 'error',
    this.CONFIRM = 'confirm',
    // TO-DO: Change 'type' to dialogType
    this.type = '',
    // Whether or not the dialog box is currently showing
    this.isDisplayed = false,
    // Size of the box -- default to app's DIALOG_BOX_WIDTH and
    // DIALOG_BOX_HEIGHT
    this.width = null;
    this.height = null;
    // Text of the message displayed in the dialog
    this.msg = '',
    // Arrays of Button objects -- left ones cluster on the 
    // botton-left, center ones cluster in bottom-center, 
    // right ones cluster on bottom-right
    this.btnsLeft = [],
    this.btnsCenter = [],
    this.btnsRight = [],
    // A JS function -- the action to take when the Enter key 
    // is pressed while the dialog is showing -- should always 
    // correspond to the action tied to the bottom right-hand button
    // for multiple buttons, or the single centered button if there's
    // only one
    this.defaultAction = null,
    
    /**
     * Main function for displaying the dialog
     * Sets up buttons and message, and calls method to create
     * DOM elements to display the box
     */
    this.show = function(display, btnsLeft, btnsCenter, btnsRight) {
        this.btnsLeft = btnsLeft ? btnsLeft : this.btnsLeft;
        this.btnsCenter = btnsCenter ? btnsCenter : this.btnsCenter;
        this.btnsRight = btnsRight ? btnsRight : this.btnsRight;
        this.msg = display ? display : this.msg;
        this.width = this.width || DIALOG_BOX_WIDTH;
        this.height = this.height || DIALOG_BOX_HEIGHT;
        this.create();
    };
    /**
     * Main function for dismissing the dialog
     * Removes the DOM elements, clears the button array, and
     * removes the default-action function
     */
    this.hide = function() {
        this.destroy();
        this.clearButtons();
        this.width = null;
        this.height = null;
        this.defaultAction = null;
    };
    /**
     * Creates and appends the DOM elements for the box, inserts
     * message markup into the box with innerHTML
     * Creates a ButtonPanel for the bottom of the dialog, and
     * populates each section (left, center, right) from the 
     * corresponding arrays of Button objects
     * TO-DO: Change the name of this method to something more
     * specific like 'createDOMElements'
     * isDisplayed property should get set up in this.show
     */
    this.create = function() {
        var winwidth = Cal.getWinWidth();
        var winheight = Cal.getWinHeight();
        var dia = document.createElement('div');
        var dcontent = document.createElement('div');
        var dtext = document.createElement('div');
        var dbutton = document.createElement('div');
        var panel = null;
        
        dia.id = 'fauxPopDiv';
        // Center the dialog in the window
        dia.style.width = parseInt(this.width) + 'px';
        dia.style.height = parseInt(this.height) + 'px';
        dia.style.left = parseInt((winwidth - this.width)/2) + 'px';
        dia.style.top = parseInt((winheight - this.height)/2) + 'px';
        dcontent.id = 'fauxPopContentDiv';
        dtext.id = 'fauxPopTextDiv';
        dtext.innerHTML = this.msg;
        dbutton.id = 'fauxPopButtonDiv';
        
        // Create new ButtonPanel to hold the three groups of buttons
        panel = new ButtonPanel(this.btnsLeft, this.btnsCenter, this.btnsRight);
        dbutton.appendChild(panel);
        
        dcontent.appendChild(dtext);
        dcontent.appendChild(dbutton);
        dia.appendChild(dcontent);
        document.body.appendChild(dia);
        
        this.isDisplayed = true;
        
        dia = null;
        dtext = null;
        dcontent = null;
    };
    /**
     * Remove all the DOM elements for the box/buttons
     * TO-DO: Change the name of this method to something more
     * specific like 'destroyDOMElements'
     * isDisplayed property should get set up in this.hide
     */
    this.destroy = function() {
        var dia = document.getElementById('fauxPopDiv');
        dia.parentNode.removeChild(dia);
        this.isDisplayed = false;
        dia = null;
    };
    /**
     * Reset arrays for Button objects to empty
     */
    this.clearButtons = function() {
        this.btnsLeft = [];
        this.btnsCenter = [];
        this.btnsRight = [];
    };
}
