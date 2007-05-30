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

dojo.provide('cosmo.app');

dojo.require("dojo.event.*");
dojo.require('cosmo.ui.widget.ModalDialog');
dojo.require("cosmo.ui.button");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.topics");
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.popup");

cosmo.app = new function () {
    var self = this;
    var selectBoxVisibility = {};

    // App section code to run on init
    this.initObj = {};
    this.initParams = {};
    // warnings / confirmations
    this.modalDialog = null;
    // Full-screen div for masking while UI renders
    this.maskNode = null;
    // The item in the UI being dragged
    this.dragItem = null;
    // List of any queued-up error messages
    this.errorList = [];
    //select boxes to show/hide when displaying modal dialog box
    this._selectBoxIds = {};

    this.init = function () {
        self.maskNode = $('maskDiv');
        self.showMask();
        // Set up the modal dialog box for the app
        self.modalDialog = dojo.widget.createWidget(
            'cosmo:ModalDialog', {}, document.body, 'last');
        dojo.event.topic.subscribe(
            cosmo.topics.PreferencesUpdatedMessage.topicName, self, 'updateUIFromPrefs');
        // Initialize the default view
        if (typeof self.initObj.init == 'function') { 
            self.initObj.init(self.initParams);
        };
        self.hideMask();
    }

    // ==========================
    // Modal dialog boxes
    // ==========================
    /**
     * Show error dialog with either simple text msg, error object, or both
     * If new errors get spawned while this is processing, it queues the
     * messages for display after users dismisses the faux modal disalog box
     */
    this.showErr = function (pri, sec) {
        var msg = '';
        var secondaryMessage = null; // Secondary message, if any
        var trace = null; // Stack trace, if any

        // Secondary message passed
        if (sec) {
            if (typeof sec == 'string') {
                secondaryMessage = sec;
            }
            else if (sec instanceof Error && sec.message) {
                secondaryMessage = sec.message;
                trace = sec.javaStack;
            }
        }

        // If the error dialog is already showing, add this message to the error queue
        if (this.modalDialog.isDisplayed) {
            this.errorList.push(pri);
        }
        // Otherwise display the error dialog
        else {
            // If there are errors waiting in the queue, prepend them to the error msg
            if (this.errorList.length) {
                var currErr = '';
                while (currErr = this.errorList.shift()) {
                    msg += '<div class="errText">' + currErr + '</div>';
                }
                msg += pri;
            }
            // Otherwise just display the current message
            else {
                msg = _createElem('div');
                // Primary error message -- simple message
                var d = _createElem('div');
                d.className = 'errText';
                d.innerHTML = pri;
                pri = d;
                msg.appendChild(pri);
                // Secondary message -- some details or string from the server
                if (secondaryMessage) {
                    d = _createElem('div');
                    d.innerHTML = secondaryMessage;
                    secondaryMessage = d;
                    msg.appendChild(secondaryMessage);
                    // If we have a trace, give the user the option of seeing it
                    // in a pop-up window
                    if (trace) {
                        d = _createElem('div');
                        d.innerHTML = '<pre>' + trace + '</pre>';
                        trace = d;
                        var full = _createElem('div');
                        // Use a clone of the secondary message node
                        full.appendChild(secondaryMessage.cloneNode(true));
                        full.appendChild(trace);
                        var f = function () {
                            cosmo.util.popup.openFullSize(full);
                        }
                        d = _createElem('div');
                        d.style.marginTop = '8px';
                        var a = document.createElement('a');
                        // Avoid use of the ugly hack of '#' href prop 
                        // Give the anchor some help to act like a real link
                        a.className = 'jsLink'; 
                        dojo.event.connect(a, 'onclick', f);
                        a.appendChild(_createText('Click here for details ...'));
                        d.appendChild(a);
                        msg.appendChild(d);
                    }
                }
            }
            this.modalDialog.type = this.modalDialog.ERROR;
            var but = new Button('okButton', 64, self.hideDialog,
                _('App.Button.OK'), true);
            this.modalDialog.btnsCenter[0] = but;
            this.modalDialog.defaultAction = self.hideDialog;
            this.modalDialog.content = msg;
            this.showDialog();
        }
    };
    /**
     * Displays the mask that covers the entire browser window
     * until the UI is completely rendered
     */
    this.showMask = function () {
        if (this.maskNode) {
            this.showHideSelectBoxes(false);
            this.maskNode.style.display = 'block';
        }
    };
    /**
     * Hides the mask that covers the entire browser window
     * once the UI is completely rendered
     */
    this.hideMask = function () {
        if (this.maskNode) {
            this.maskNode.style.display = 'none';
            this.showHideSelectBoxes(true);
        }
    };
    /**
     * Display the modal dialog box with whatever set of attributes
     * is passed to it
     */
    this.showDialog = function (props) {
        for (var p in props) {
            self.modalDialog[p] = props[p];
        }
        self.setInputDisabled(true);
        // Hide the stupid IE6 select elements that ignore z-index
        // This is done app-wide right here instead of delegating
        // to individual widgets since select elements can be grabbed
        // for the entire DOM tree at one time
        if (dojo.render.html.ie && dojo.render.ver < 7) {
            self.showHideSelectBoxes(false);
        }
        // Publish message to individual widgets as well -- allow
        // UI components to run any browser-specific hacks needed --
        // e.g., hide scrollbars, turn off overflow to avoid
        // disappearing cursor, etc.
        cosmo.topics.publish(cosmo.topics.ModalDialogToggle, { isDisplayed: true });
        self.modalDialog.show();
    };
    /**
     * Dismiss the faux modal dialog box -- check for queued error
     * messages to display if needed
     */
    this.hideDialog = function () {
        self.modalDialog.hide();
        // Un-hide the stupid IE6 select elements that ignore z-index
        // This is done app-wide right here instead of delegating
        // to individual widgets since select elements can be grabbed
        // for the entire DOM tree at one time
        if (dojo.render.html.ie && dojo.render.ver < 7) {
            self.showHideSelectBoxes(true);
        }
        // Publish to individual widgets as well -- allow
        // UI components to shut off any browser-specific hacks needed
        // in the display of the dialog box -- e.g., turning overflow
        // back on, etc.
        cosmo.topics.publish(cosmo.topics.ModalDialogToggle, { isDisplayed: false });
        // If there are error messages that have been added to the queue,
        // trigger another dialog to handle them
        if (self.errorList.length) {
            self.showErr('');
        }
        else {
            self.setInputDisabled(false);
        }
    };
    this.setInputDisabled = function (isDisabled) {
        if (isDisabled) {
            this.inputDisabled = true;
        }
        else {
            this.inputDisabled = false;
        }
        return this.inputDisabled;
    };

    /**
     * Whether or not input from the entire UI is disabled
     * Returns true when the faux modal dialog box is displayed
     */
    this.getInputDisabled = function () {
        return this.inputDisabled;
    };
    this.showHideSelectBoxes = function (show) {
        var selects = document.body.getElementsByTagName('select');
        var vis = show ? 'visible' : 'hidden';
        dojo.lang.map(selects, function (sel, show){
            if (sel.style) {
                // Storing state, or state has been stored
                if (sel.id && sel.style.visibility) {
                    // Preserve styles expressly set to 'hidden'
                    if (show) {
                        if (typeof selectBoxVisibility[sel.id] != 'undefined') {
                            sel.style.visibility = selectBoxVisibility[sel.id];
                        }
                        else {
                            sel.style.visibility = vis;
                        }
                    }
                    // Save state if necessary, and hide
                    else {
                        selectBoxVisibility[sel.id] = sel.style.visibility;
                        sel.style.visibility = vis;
                    }
                }
                else {
                    sel.style.visibility = vis;
                }
            }
        });
        // Reset the state map when finished
        if (show) {
           selectBoxVisibility = {};
        }
    };
};
