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

cosmo.app = new function () {
    var self = this;

    // App section code to run on init
    this.initObj = {};
    // warnings / confirmations
    this.modalDialog = null;
    // The item in the UI being dragged
    this.dragItem = null;
    // List of any queued-up error messages
    this.errorList = [];

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
    this.showErr = function(str, e) {
        var msg = '';
        var currErr = '';
        var but = null;

        // If the error dialog is already showing, add this message to the error queue
        if (this.modalDialog.isDisplayed) {
            this.errorList.push(str);
        }
        // Otherwise display the error dialog
        else {
            // If there are errors waiting in the queue, prepend them to the error msg
            if (this.errorList.length) {
                while (currErr = this.errorList.shift()) {
                    msg += '<div class="errText">' + currErr + '</div>';
                }
                msg += str;
            }
            // Otherwise just display the current message
            else {
                msg = '<div class="errText">' + str + '</div>';
                if (e) {
                    msg += '<div>' + e.message + '</div>'
                }
            }
            this.modalDialog.type = this.modalDialog.ERROR;
            but = new Button('okButton', 64, self.hideDialog,
                getText('App.Button.OK'), true);
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
    this.showDialog = function(props) {
        for (var p in props) {
            self.modalDialog[p] = props[p];
        }
        self.setInputDisabled(true);
        self.modalDialog.show();
    };
    /**
     * Dismiss the faux modal dialog box -- check for queued error
     * messages to display if needed
     * Put away the full-screen transparent input-blocking div
     */
    this.hideDialog = function() {
        // Hide the current error dialog
        self.modalDialog.hide();
        // If there are error messages that have been added to the queue,
        // trigger another dialog to handle them
        if (self.errorList.length) {
            self.showErr('');
        }
        else {
            self.setInputDisabled(false);
        }
    };
    this.setInputDisabled = function(isDisabled) {
        if (isDisabled) {
            //document.getElementById('fullMaskDiv').style.display = 'block'; // Block input with full-sized mask
            this.inputDisabled = true;
        }
        else {
            //document.getElementById('fullMaskDiv').style.display = 'none'; // Remove full-sized mask
            this.inputDisabled = false;
        }
        return this.inputDisabled;
    };
    /**
     * Whether or not input from the entire UI is disabled
     * Returns true when the faux modal dialog box is displayed
     */
    this.getInputDisabled = function() {
        return this.inputDisabled;
    };

}
