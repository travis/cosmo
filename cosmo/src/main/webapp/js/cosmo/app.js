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

dojo.require('cosmo.ui.widget.ModalDialog');
dojo.require("cosmo.ui.button");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.topics");
var _ = cosmo.util.i18n.getText;
cosmo.app = new function () {
    var self = this;
    var selectBoxVisibility = {};

    // App section code to run on init
    this.initObj = {};
    
    // warnings / confirmations
    this.modalDialog = null;
    
    // The item in the UI being dragged
    this.dragItem = null;
    
    // List of any queued-up error messages
    this.errorList = [];
    
    //select boxes to show/hide when displaying modal dialog box
    this._selectBoxIds = {};

    this.init = function () {
        // Set up the modal dialog box for the app
        this.modalDialog = dojo.widget.createWidget(
            'cosmo:ModalDialog', {}, document.body, 'last');

        // Initialize the default view
        if (self.initObj.init) { self.initObj.init.apply(this.initObj, arguments)};
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
        
        // Secondary message passed 
        if (sec) {
            if (typeof sec == 'string') {
                secondaryMessage = sec;
            }
            else if (sec instanceof Error && sec.message) {
                secondaryMessage = sec.message;
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
                msg = '<div class="errText">' + pri + '</div>';
                if (secondaryMessage) {
                    msg += '<div>' + secondaryMessage + '</div>'
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
     * Display the current dialog box and throw up the transparent
     * full-screen div that blocks all user input to the UI
     */
    this.showDialog = function (props) {
        for (var p in props) {
            self.modalDialog[p] = props[p];
        }
        self.setInputDisabled(true);
        // Hide the stupid IE6 select elements that ignore z-index
        if (dojo.render.html.ie && dojo.render.ver < 7) {
            self.showHideSelectBoxes(false);
        }
        self.modalDialog.show();
        cosmo.topics.publish(cosmo.topics.ModalDialogDisplayed);
    };
    /**
     * Dismiss the faux modal dialog box -- check for queued error
     * messages to display if needed
     * Put away the full-screen transparent input-blocking div
     */
    this.hideDialog = function () {
        // Un-hide the stupid IE6 select elements that ignore z-index
        if (dojo.render.html.ie && dojo.render.ver < 7) {
            self.showHideSelectBoxes(true);
        }
        self.modalDialog.hide();
        cosmo.topics.publish(cosmo.topics.ModalDialogDismissed);
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
}
