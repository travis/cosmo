/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
 *      This module provides convenience functions for account creation.
 * description:
 *      This module provides functions creating, submitting and dealing with
 *      results of an account creation form.
 */
dojo.provide('cosmo.account.create');

dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.cmp");
dojo.require("cosmo.account.common");
dojo.require("cosmo.app");

cosmo.account.create = new function () {

    var self = this; // Stash a copy of this
    var form = null; // The form containing the signup fields
    var f = null; // Temp var

    this.fieldList = null;

    dojo.mixin(this, cosmo.account.accountBase);

    // Public members
    // =============================
    // Identify whether this is 'create' or 'settings'
    this.formType = cosmo.account.formTypes.CREATE;
    // The field that has focus
    this.focusedField = null;
    // A subscription to add to the signup request
    this.subscription = null;

    // Public methods
    // =============================
    /**
     * Sets up the dialog box with the table of form elements
     * and appropriate buttons for creating a new account.
     */
    this.showForm = function (subscription) {
        var o = {};
        var b = null;
        this.subscription = subscription;
        this.fieldList = cosmo.account.getFieldList(null, subscription);

        o.width = 540;
        o.height = 480;
        o.title = 'Create an Account';
        o.prompt = _('Signup.Prompt.AllFieldsRequired');
        form = cosmo.account.getFormTable(this.fieldList, this);
        o.content = form;
        b = new cosmo.ui.widget.Button({ text:_('App.Button.Cancel'), width:74,
                                         id: "signupCancel",
            handleOnClick: function () { cosmo.app.modalDialog.hide(); } });
        o.btnsLeft = [b];
        // Have to set empty center set of buttons -- showForm will be called
        // without buttons getting cleared by 'hide.'
        o.btnsCenter = [];
        b = new cosmo.ui.widget.Button({ text:_('App.Button.Submit'), width:74,
                                         id: "signupSubmit",
            handleOnClick: function () { self.submitCreate(); } });
        o.btnsRight = [b];
        o.defaultAction = function () { self.submitCreate(); };
        cosmo.app.showDialog(o);
		form.username.focus();
    };
    /**
     * Submit the call via XHR to cosmo.cmp to sign the user
     * up for a new account.
     */
    this.submitCreate = function () {
        // Don't submit from keyboard input if focus is on a text field
        // Otherwise saved form field values selected by Enter key
        // will give you spurious submissions
        if (this.focusedField) { return false; }

        // Validate the form input using each field's
        // attached validators
        var err = cosmo.account.validateForm(form, this.fieldList);

        if (err) {
            cosmo.app.modalDialog.setPrompt(err);
        }
        else {
            var user = this.formToUserHash();
            // Hand off to CMP
            var d = cosmo.cmp.signup(user);
            d.addCallback(handleCreateSuccess);
            d.addErrback(handleCreateError);
            return d;
        }
    };

    this.formToUserHash = function(){
        var user = {
            username: form.username.value,
            firstName: form.firstName.value,
            lastName: form.lastName.value,
            email: form.email.value,
            password: form.password.value,
            preferences: {}
        };
        if (form.subscriptionName || form.subscriptionTicket || form.subscriptionUuid){
            user.subscription = {
                name: form.subscriptionName.value,
                ticket: form.subscriptionTicket.value,
                uuid: form.subscriptionUuid.value
            };
        }
        if (form.contactPreference)
            user.preferences["Contact"] = form.contactPreference.checked.toString();
        return user;
    };
    /**
     * Set up and display the table of settings needed to
     * use the new account with an external cal client.
     * Append the Close button for the dialog
     */
    this.showResultsTable = function (user) {
        var cfg = getClientConfig(user);
        var content = getResultsTable(user, cfg);
        var prompt = _('Signup.Prompt.Success');
        var d = cosmo.app.modalDialog;
        var btnsCenter = [new cosmo.ui.widget.Button(
            { text:_('App.Button.Close'), width:74, id: "signupClose",
            handleOnClick: function () {
                if (self.subscription)
                    location = cosmo.env.getBaseUrl() + '/login';
                else
                    cosmo.app.hideDialog();
            }})];

        // Update dialog in place
        d.setPrompt(prompt);
        d.setContent(content);
        d.setButtons([], btnsCenter, []);
        d.defaultAction = function () { cosmo.app.modalDialog.hide(); };
    };

    // Private methods
    // =============================
    /**
     * Handler function for XHR call to cosmo.cmp for signup.
     * Sets error prompt if request fails, set up the results
     * table with external client config on success.
     * @return Boolean, true on success, false on failure
     */
    function handleCreateError(error) {
        var err = '';
        if (error || (error.status > 399)) {
            switch (error.status) {
            case 403:
                err = _('Signup.Error.AlreadyLoggedIn');
                break;
            case 431:
                err = _('Signup.Error.UsernameInUse');
                break;
            case 432:
                err = _('Signup.Error.EMailInUse');
                break;
            default:
                err = _('Signup.Error.Generic') + ' (error code ' +
                    error.status + ')';
                break;
            }
        }
        else {
            err = _('Signup.Error.Generic') + ' (' + error.message + ')';
        }
        cosmo.app.modalDialog.setPrompt(err);
        return error;
    }
    function handleCreateSuccess(user) {
            self.showResultsTable(user);
            return true;
    }
    /**
     * Sets up a hash of data for the info needed to configure an
     * external cal client to access this Cosmo account.
     * @return Object, hash of configuration data -- hash keys
     *     are also the keys for the i18n label strings for the
     *     data in the displayed table.
     *
     */
    function getClientConfig(user) {

        var cfg = {};
        var username = user.username;
        var homedirUrl = new dojo._Url(user.homedirUrl);
        // Server settings
        var isSSL = homedirUrl.scheme == 'https';

        var portNum = homedirUrl.port;
        // Port -- if none specified use 80 (or 443 for https)
        if (portNum == undefined) {
            portNum = isSSL ? 443 : 80;
        }

        // String to display for SSL
        isSSL = isSSL ? 'Yes' : 'No';

        // Config settings for external client setup
        cfg['Server'] = homedirUrl.host;
        cfg['Path'] = cosmo.env.getBaseUrl();
        cfg['Username'] = username;
        cfg['Password'] = '(Hidden)';
        cfg['PortNumber'] = portNum;
        cfg['UseSSL'] = isSSL;
        cfg['FullURL'] = homedirUrl.scheme + "://" + homedirUrl.host + ":" +
            portNum + cosmo.env.getBaseUrl();

        return cfg;
    }
    /**
     * Programmatically create the table to display the
     * configuation needed to set up an external cal client
     * to work with this Cosmo account. Also includes the
     * two links for (1) create another account, which clears
     * and resets the form, and (2) log in to Cosmo, which
     * simply closes the dialog.
     * @return Object (HtmlDivElement), div containing the
     *     table to append to the content area of the dialog
     */
    function getResultsTable(user, cfg) {
        var p = null;
        var a = null;
        var tr = null;
        var td = null;

        // Outer div
        var d0 = _createElem('div');

        if (user.unactivated) {
            p = _createElem('div');
            p.style.marginBottom = '16px';
            p.style.textAlign = 'center';
            p.className = 'activationPrompt';
            p.innerHTML = _('Signup.Prompt.AccountActivation');
            d0.appendChild(p);
            return d0;
        }

        p = _createElem('div');
        p.appendChild(_createText(_('Signup.Prompt.AccountSetup')));
        d0.appendChild(p);

        // Create the table, append rows for each config value
        var table = _createElem('table');
        table.className = 'dataDisplay';
        table.style.width = '80%';
        table.style.margin = 'auto';
        table.style.marginTop = '12px';

        var body = _createElem('tbody');

        // Create a row for each config setting
        for (var propName in cfg) {
            tr = _createElem('tr');

            // Prop label
            td = _createElem('td');
            td.className = 'dataDisplayLabel';
            td.appendChild(_createText(_('Signup.Config.' + propName)));
            tr.appendChild(td);

            // Prop value
            td = _createElem('td');
            td.appendChild(_createText(cfg[propName]));
            tr.appendChild(td);
            body.appendChild(tr)
        }
        table.appendChild(body);
        d0.appendChild(table);

        /*
        ***** Leave this out until we can actually do auto-login *****
        // Link to begin using new account
        if (!user.unactivated) {
            p = _createElem('div');
            p.style.marginTop = '12px';
            p.style.textAlign = 'center';
            a = _createElem('a');
            a.href = 'javascript:cosmo.app.modalDialog.hide();';
            a.appendChild(_createText(_('Signup.Links.LogInToCosmo')));
            p.appendChild(a);
            d0.appendChild(p);
        }
        */

        // Return the div containing the content
        return d0;
    }
}
