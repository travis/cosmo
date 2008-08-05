/*
 * Copyright 2007-2008 Open Source Applications Foundation
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
 * summary:
 *      This module provides convenience functions for user settings.
 * description:
 *      This module provides convenience functions for creating,
 *      editing and deleting server side user settings.
 */
dojo.provide("cosmo.account.settings");

dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.cmp");
dojo.require("cosmo.util.validate");
dojo.require("cosmo.ui.widget.TabContainer");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.ui.widget.AccountDeleter");
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
    };

    dojo.mixin(this, cosmo.account.accountBase);

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
    this.accountInfoLoadSuccess = function (data) {
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
    this.accountInfoLoadError = function (data) {
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
            var d = cosmo.cmp.getAccount();
            d.addCallback(dojo.hitch(this, this.accountInfoLoadSuccess));
            d.addErrback(dojo.hitch(this, this.accountInfoLoadError));
            return d;
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
        d.style.paddingLeft = '4px';
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
        advancedFormDeferred.addCallback(dojo.hitch(this, function (advancedForm){
            this.advancedForm = advancedForm;
            tabContent.appendChild(this.advancedForm);
            tabs.push({ label: tabLabel, content: tabContent });

            // About Cosmo tab
            // -------
            tabLabel = strings.about;
            var about = new cosmo.ui.widget.About({});
            tabContent = about;
            originalAboutBox = about;
            tabs.push({ label: tabLabel, content: tabContent });

            // Submit button and default Enter-key action
            var saveFunction = dojo.hitch(this, function () { this.submitSave();});

            var tabContainer = new cosmo.ui.widget.TabContainer({tabs: tabs});
            o.content = tabContainer;
            var deleteButton = new cosmo.ui.widget.Button({
                text:_('App.Button.DeleteAccount'),
                id: "settingsDeleteAccount",
                width:90, small: true, handleOnClick: function () {
                    cosmo.app.hideDialog();
                    var dialog = new dijit.Dialog({title: _('Account.Delete.DialogTitle')});
                    dialog.setContent((new cosmo.ui.widget.AccountDeleter(
                        {
                            cancelCallback: function(){
                                dialog.destroy();
                                cosmo.account.settings.showDialog();
                            }
                        })).domNode);
                    dialog.show();
                }});
            o.btnsLeft = [deleteButton];
            var closeButton = new cosmo.ui.widget.Button({
                text:_('App.Button.Close'),
                id: "settingsAdvancedClose",
                width:60, small: true, handleOnClick: function () {
                    cosmo.app.hideDialog(); } });
            var saveButton = new cosmo.ui.widget.Button({
                text:_('App.Button.Save'),
                id: "settingsAdvancedSave",
                width:60, small: true, handleOnClick: saveFunction });
            o.btnsRight = [closeButton, saveButton];
            o.defaultAction = saveFunction;

            cosmo.app.showDialog(o);
        }));
        return advancedFormDeferred;
    };
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

        var prefDeferreds = [];
        for (var pref in prefs){
            prefDeferreds.push(cosmo.account.preferences.setPreference(
                pref, prefs[pref]));
        }
        // Start preferences setting
        var setPreferencesDeferred = new dojo.DeferredList(prefDeferreds);

        setPreferencesDeferred.addCallback(dojo.hitch(this, function () {
            // Validate the form input using each field's
            // attached validators
            var fieldList = this.fieldList;
            // Validate fields with the attached validators
            // and display any inline err messages
            var err = cosmo.account.validateForm(this.detailsForm, fieldList, false);

            // No error -- submit updated account info via CMP
            if (!err) {
                var self = this;
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
                var d = cosmo.cmp.modifyAccount(account);
                d.addCallback(dojo.hitch(this, this.handleAccountSaveSuccess));
                d.addErrback(dojo.hitch(this, this.handleAccountSaveError));
                return d;
            }
        }));

        cosmo.util.deferred.addStdErrback(setPreferencesDeferred, _("Error.SaveSettings"), "");
    };

    this.handleAccountSaveSuccess = function (data) {
        this.accountInfo = null;
        cosmo.app.hideDialog();
    };

    this.handleAccountSaveError = function (err){
        this.accountInfo = null;
        cosmo.app.hideDialog();
        cosmo.app.showErr(strings.settingsErrorUpdate, data);
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
