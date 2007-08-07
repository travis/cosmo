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
dojo.provide("cosmo.account.settings");

dojo.require("dojo.widget.*");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.ui.widget.TabContainer");
dojo.require("cosmo.account.preferences");
dojo.require("cosmo.ui.widget.About");

var originalAboutBox = null;

cosmo.account.settings = new function () {

    var self = this; // Stash a copy of this
    this.detailsForm = null; // The form containing the signup fields
    this.advancedForm = null;
    // The field that has focus
    this.focusedField = null;
    
    // Localized strings
    var strings = {
        title: _('AccountSettings.Title'),
        passwordBlank: _('AccountSettings.Prompt.BlankPassword'),
        settingsErrorLoad: _('AccountSettings.Error.Load'),
        settingsErrorUpdate: _('AccountSettings.Error.Update'),
        general: _('AccountSettings.General'),
        advanced: _('AccountSettings.Advanced'),
        about: _('AccountSettings.About'),
        advancedAccountBrowser: _('AccountSettings.Advanced.AccountBrowser')
    }
    
    dojo.lang.mixin(this, cosmo.account.accountBase);

    // Public memebers
    // ==============
    // Identify whether this is 'create' or 'settings'
    this.formType = cosmo.account.formTypes.SETTTINGS;
    // Cache of user account data
    this.accountInfo = null;
    // Array of form input fields for basic account data
    this.fieldList = [];

    // Public methods
    // ==============
    /**
     * Handles successful loading of the current account info from
     * the server -- will reload updated account info if the user
     * edits it
     * @param type String, should be 'load'
     * @param data Object, a hash of account properties
     * @param resp Object, the XHR obj
     */
    this.accountInfoLoadSuccess = function (type, data, resp) {
        this.accountInfo = data;
        this.showDialog();
    };
    /**
     * Handles errors loading the current account info from the server
     * @param type String, should be 'error'
     * @param data Object, error object -- 'message' prop contains
     *     actual error message text
     * @param resp Object, the XHR obj
     */
    this.accountInfoLoadError = function (type, data, resp) {
        var err = strings['settingsErrorLoad'];
        cosmo.app.showErr(err, data);
    };
    /**
     * Displays the multi-tab dialog with user account data and prefs
     * Loads and caches account data on first invocation of the box
     * Editing account info flushes the cache so it gets updated
     * from the server when the box is invoked again
     */
    this.showDialog = function () {
        var o = {};
        var s = document.createElement('span'); // Throwaway node to avoid doc reflow
        var tabs = [];
        var tabLabel = '';
        var tabContent = null;

        o.width = 580;
        o.height = 380;
        o.title = strings.title;
        o.prompt = ''; // This dialog has no prompt

        // No user account data cached -- grab it from the server
        // and bail out
        if (!this.accountInfo) {
            var self = this;
            var success = function (type, data, resp) {
                self.accountInfoLoadSuccess(type, data, resp); };
            var error = function (type, data, resp) {
                self.accountInfoLoadError(type, data, resp); };
            var hand = { load: success, error: error };
            cosmo.cmp.getAccount(hand, true);
            return;
        }

        // Build the list of fields based on the account info
        this.fieldList = cosmo.account.getFieldList(this.accountInfo);
        // Build the form using the list of input fields
        this.detailsForm = cosmo.account.getFormTable(this.fieldList, this);

        // Add the notice to the right of the password field
        // to indicate that leaving the fields blank mean 'no change'
        var passCell = this.detailsForm.password.parentNode;
        var d = null;
        var pass = passCell.removeChild(this.detailsForm.password);
        d = _createElem('div');
        d.className = 'floatLeft';
        d.style.width = '40%';
        d.appendChild(pass);
        passCell.appendChild(d);
        d = _createElem('div');
        d.className = 'promptText floatLeft';
        d.style.width = '59%';
        d.style.paddingLeft = '4px'
        d.appendChild(_createText(strings['passwordBlank']));
        passCell.appendChild(d);
        d = _createElem('div');
        d.className = 'clearBoth';
        passCell.appendChild(d);

        // General tab -- general account data
        // -------
        tabLabel = strings.general;
        tabContent = _createElem('div');
        tabContent.appendChild(this.detailsForm);
        tabs.push({ label: tabLabel, content: tabContent });

        // Advanced settings
        // -------
        tabLabel = strings.advanced;
        tabContent = _createElem('div');
        var advancedFormDeferred = this.getAdvancedForm();
        advancedFormDeferred.addCallback(dojo.lang.hitch(this, function (advancedForm){
            this.advancedForm = advancedForm;
            tabContent.appendChild(this.advancedForm);
            tabs.push({ label: tabLabel, content: tabContent });
    
            // About Cosmo tab
            // -------
            tabLabel = strings.about;
            var about = dojo.widget.createWidget("cosmo:About", {}, s, 'last');
            s.removeChild(about.domNode); // Detach from the throwaway node
            tabContent = about;
            originalAboutBox = about;
            tabs.push({ label: tabLabel, content: tabContent });
    
            var self = this; // For callback scope
            // Submit button and default Enter-key action
            var f = function () { self.submitSave.apply(self); };
    
            var b = null; // For dialog buttons
            var c = null; // For dialog content area
            c = dojo.widget.createWidget("cosmo:TabContainer", {
                tabs: tabs }, s, 'last');
            s.removeChild(c.domNode); // Detach from the throwaway node
            o.content = c;
            b = new cosmo.ui.button.Button({ text:_('App.Button.Close'),
                width:60, small: true, handleOnClick: function () {
                    cosmo.app.hideDialog(); } });
            o.btnsLeft = [b];
            b = new cosmo.ui.button.Button({ text:_('App.Button.Save'),
                width:60, small: true, handleOnClick: f });
            o.btnsRight = [b];
            o.defaultAction = f;
    
            cosmo.app.showDialog(o);
        }));
        return advancedFormDeferred;
    }
    /**
     * Validate the form input and submit via XHR
     */
    this.submitSave = function () {
        
        // Don't submit from keyboard input if focus is on a text field
        // Otherwise saved form field values selected by Enter key
        // will give you spurious submissions
        if (this.focusedField) { return false; }

        // save preferences asynchronously
        var prefs = {};

        prefs[cosmo.account.preferences.SHOW_ACCOUNT_BROWSER_LINK] =
            this.advancedForm.showAccountBrowser.checked;
        
        var setPreferencesDeferred = new dojo.Deferred();
        for (var pref in prefs){
            // create new function and call immediately to define scope
            var throwAway = function (){
                // capture preference key in scope
                var capturedPref = pref;
                setPreferencesDeferred.addCallback(function () {
                    return cosmo.account.preferences.setPreference(capturedPref, 
                        prefs[capturedPref]);
                }
                );
            }();
        }
        // Start preferences setting
        setPreferencesDeferred.callback();
        
        setPreferencesDeferred.addCallback(dojo.lang.hitch(this, function () {
            // Validate the form input using each field's
            // attached validators
            var fieldList = this.fieldList;
            // Validate fields with the attached validators
            // and display any inline err messages
            var err = cosmo.account.validateForm(this.detailsForm, fieldList, false);
    
            // No error -- submit updated account info via CMP
            if (!err) {
                var self = this;
                // Same handler for both success and error -- IE throws a
                // freakish '1223' HTTP code when server returns a successful
                // 204 'No content'. Dojo's io.bind doesn't recognize that as
                // success, so we have to examine status codes manually
                var f = function (type, data, resp) {
                    self.handleAccountSave(type, data, resp); };
                var hand = { handle: f };
                var account = {};
                // Create a hash from the form field values
                for (var i = 0; i < fieldList.length; i++) {
                    var f = fieldList[i];
                    var val = this.detailsForm[f.elemName].value;
                    // Only include fields with values -- throw out
                    // the 'confirm' field
                    if (val && (f.elemName != 'confirm')) {
                        account[f.elemName] = val;
                    }
                }
                // Hand off to CMP
                cosmo.cmp.modifyAccount(account, hand);
            }
        }));
        
        cosmo.util.deferred.addStdErrback(setPreferencesDeferred, _("Error.SaveSettings"), "");
    };
    /**
     * Handle both success and error responses from the CMP call
     * @param type String, may be 'load' or 'error' -- if type is
     *     'error,' this may be bogus result from weird IE 1223
     *     HTTP response code
     * @param data Object, May be data or error object
     * @param resp Object, the XHR obj
     */
    this.handleAccountSave = function (type, data, resp) {
        var stat = resp.status;
        var err = '';
        // BANDAID: Hack to get Safari a valid status code --
        // any success codes other than 200 result in resp.status
        // of 'undefined'
        if (navigator.userAgent.indexOf('Safari') > -1) {
            if (!stat) {
                stat = 200;
            }
        }
        // Add bogus 1223 HTTP status from 204s in IE as a success code
        if ((stat > 199 && stat < 300) || (stat == 1223)) {
            // Success
        }
        else {
            err = strings.settingsErrorUpdate;
        }
        // Flush the account data cache -- get the updated stuff
        // from the server if the user invokes the box again
        this.accountInfo = null;

        // Both error & success -- the dialog goes poof
        cosmo.app.hideDialog();

        // Errors, spawn a new dialog to report the error
        if (err) {
            cosmo.app.showErr(err, data);
        }
    };
    /**
     * The form with advanced account settings displayed in the
     * Advanced tab
     */
    this.getAdvancedForm = function(){
        var form = _createElem('form');
        var div = _createElem('div');
        var nbsp = function () { return cosmo.util.html.nbsp(); };
        var prefsDeferred = cosmo.account.preferences.getPreferences();
        prefsDeferred.addCallback(function(prefs){
            var checkedDefault = (prefs[cosmo.account.preferences.SHOW_ACCOUNT_BROWSER_LINK] == 'true');
            var check = cosmo.util.html.createInput({ type: 'checkbox',
                id: 'showAccountBrowser', name: 'showAccountBrowser',
                value: '', checked: checkedDefault });
    
            div.appendChild(check);
            div.appendChild(nbsp());
            div.appendChild(nbsp());
            div.appendChild(_createText(strings.advancedAccountBrowser));
            form.appendChild(div);
    
            // BANDAID: Hack to get the checkbox into Safari's
            // form elements collection
            if (navigator.userAgent.indexOf('Safari') > -1) {
                cosmo.util.html.addInputsToForm([check], form);
            }
    
            return form;
        });
        return prefsDeferred;
    };


};
