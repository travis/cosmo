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
 * @fileoverview A simple dialog box object with title, prompt, and buttons
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object A simple dialog box with title, prompt, and buttons
 * Has four basic DOM element components: title, prompt, content area
 * and button panel (from top to bottom). The button panel has three
 * clusters of buttons -- left, center, and right
 */
var SimpleDialog = new function() {

    // Default dimensions of the dialog box -- this can be changed
    // before calling the create method to change the box size
    this.width = 520;
    this.height = 460;
    // Main title of the dialog box
    this.title = '';
    // The prompt text that shows under the dialog box title
    this.promptText = '';
    // Sets CSS class for the prompt div -- uppercases first letter
    // and appends after string 'dialogPrompt', e.g., 'dialogPromptNormal'
    this.promptType = 'normal';
    // DOM element to fill the main content area between the
    // prompt and buttons
    this.content = null;
    // Arrays of buttons for the three sections of the button panel
    // Items in these arrays should be DOM elements
    this.buttonsLeft = []; // Left cluster, left-aligned
    this.buttonsCenter = []; // Center cluster, centered
    this.buttonsRight = []; // Right cluster, right-aligned
    // Whether the dialog box is visible or not
    this.isDisplayed = false;
    // Default action to take when user presses Enter while the
    // dialog box is visible
    // This should be the same action as the far-right button
    this.defaultAction = null;
    
    /**
     * Creates the DOM elements for the dialog box, and puts them
     * on the page -- the main div is absolute-positioned in the
     * horiz. center / vert. middle of the screen
     */
    this.create = function() {
        var self = SimpleDialog;
        var d = null;
        var p = null;
        var t = null;
        var pr = null;
        var c = null;
        var b = null;
        var bl = null;
        var bc = null;
        var br = null;
        var cb = null;
        
        // Reinit promptType to 'normal'
        self.promptType = 'normal';
        
        // Main outer div elem for dialog box
        // Remove if it's already on the page
        if (d = document.getElementById('dialogDiv')) {
            document.body.removeChild(d);
        }
        d = document.createElement('div');
        d.id = 'dialogDiv';
        d.style.width = self.width + 'px';
        d.style.height = self.height + 'px';
        d.style.left = ((self.getWinWidth() - self.width)/2) + 'px';
        d.style.top = ((self.getWinHeight() - self.height)/2) + 'px';
        
        // Outer padding div -- holds the four component divs below
        p = document.createElement('div');
        p.id = 'dialogPaddingDiv';
        
        // Four main component div elements, from top to bottom
        t = document.createElement('div');
        t.id = 'dialogTitleDiv';
        t.appendChild(document.createTextNode(self.title));
        pr = document.createElement('div');
        pr.id = 'dialogPromptDiv';
        c = document.createElement('div');
        c.id = 'dialogContentDiv';
        c.style.height = (self.height - 108) + 'px';
        b = document.createElement('div');
        b.id = 'dialogButtonPanelDiv';
        
        // Three button clusters -- left, center, right
        // These are appended to the button panel div
        bl = document.createElement('div');
        bl.id = 'dialogButtonLeftDiv'; 
        bc = document.createElement('div');
        bc.id = 'dialogButtonCenterDiv';
        br = document.createElement('div');
        br.id = 'dialogButtonRightDiv';
        // Clear the float property for button cluster divs
        cb = document.createElement('div');    
        cb.className = 'clearBoth';
        
        // Append the outer padding div to the main dialog div
        d.appendChild(p);
        
        // Append the four main component divs 
        // to the outer padding div
        p.appendChild(t);
        p.appendChild(pr);
        p.appendChild(c);
        p.appendChild(b);
        
        // Append the three button-cluster divs and clearAll div
        // to the button panel
        b.appendChild(bl);
        b.appendChild(bc);
        b.appendChild(br);
        b.appendChild(cb);
        
        // Put the dialog div on the page
        document.body.appendChild(d);
        
        // Insert prompt, conent, buttons according to props set
        this.putPrompt();
        this.putContent();
        this.putButtons();
        
        // Woot!
        return true;
    };
    /**
     * Shows dialog box -- sets CSS display prop and isDisplayed flag
     */
    this.show = function() {
        var d = document.getElementById('dialogDiv');
        d.style.display = 'block';
        this.isDisplayed = true;
    };
    /**
     * Hides dialog box -- sets CSS display prop and isDisplayed flag
     */
    this.hide = function() {
        var d = document.getElementById('dialogDiv');
        d.style.display = 'none';
        this.isDisplayed = false;
    }
    /**
     * Sets the text and color of the small prompt below the title
     * If args are passed in they reset the object props for text and color
     * Called with no args is just uses whatever is set for those values
     */
    this.putPrompt = function(str, type) {
        var pr = document.getElementById('dialogPromptDiv');
        var pt = '';
        
        this.promptText = str || this.promptText;
        this.promptType = type || this.promptType;
        
        // Uppercase the promptType for style className
        pt = this.promptType.substr(0, 1);
        pt = pt.toUpperCase();
        pt += this.promptType.substr(1);
        pr.className = 'dialogPrompt' + pt;
        pr.innerHTML = this.promptText;
    };
    /**
     * Sets the content in the main area in the middle of the dialog box
     * If arg is passed in, it resets the content prop and uses that
     * Otherwise is just uses what value is already set
     */
    this.putContent = function(node) {
        var c = document.getElementById('dialogContentDiv');
        
        this.content = node || this.content;
        
        while (c.firstChild) {
            c.removeChild(c.firstChild);
        }
        if (this.content) {
            c.appendChild(this.content);
        }
    };
    /**
     * Empties out all the button arrays for the button clusters
     */
    this.clearButtonArrays = function() {
        this.buttonsLeft = [];
        this.buttonsCenter = [];
        this.buttonsRight = [];
    };
    /**
     * Button panel has three left-floated divs (like table cells)
     * This method stacks buttons from each array into each section
     * in clusters -- left-, center-, and right-aligned.
     * If arrays are passed in, they override the values previously
     * set. Otherwise it uses whatever is currently in the button arrays.
     */
    this.putButtons = function(left, center, right) {
        var area = ['Left', 'Center', 'Right'];
        var bt = null;
        var list = [];
        
        this.buttonsLeft = left || this.buttonsLeft;
        this.buttonsCenter = center || this.buttonsCenter;
        this.buttonsRight = right || this.buttonsRight;
        
        // Each of the three sections
        for (var j = 0; j < area.length; j++) {
            bt = document.getElementById('dialogButton' + area[j] + 'Div');
            
            // Remove any previously set button from the div
            while (bt.firstChild) {
                bt.removeChild(bt.firstChild);
            }
            // Get the list of buttons for this cluster/section
            list = this['buttons' + area[j]];
            
            // Buttons to insert
            if (list.length) {
                // Insert each button, separated by a non-breaking space
                for (var i = 0; i < list.length; i++) {
                    bt.appendChild(list[i]);
                    if (i < (list.length-1) ) {
                        bt.appendChild(document.createTextNode('\u00A0'));
                    }
                }
            }
            // No buttons -- use non-breaking space for correct 
            // placement of floats
            else {
                bt.appendChild(document.createTextNode('\u00A0'));
            }
        }
    };
    /**
     * Cross-browser method to get the size of the browser window
     */
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
    /**
     * Cross-browser method to get the size of the browser window
     */
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
}
SimpleDialog.constructor = null;
