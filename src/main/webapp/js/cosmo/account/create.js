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

dojo.provide('cosmo.account.create');

dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.cmp");
dojo.require("dojo.uri");

var userAccount = {};

cosmo.account.create = new function () {
   
    var self = this; // Stash a copy of this
    var form = null; // The form containing the signup fields
    var fieldList = []; // List of form fields
    var f = null; // Temp var
    
    // Form fields
    // -----------------
    f = new Field(_('Signup.Form.Username'), 'username');
    f.validators = function (elem) { return (validateRequired(elem) || 
        validateMinLength(elem, 3)) };
    fieldList.push(f);
    
    f = new Field(_('Signup.Form.FirstName'), 'firstName');
    f.validators = function (elem) { return validateRequired(elem) };
    fieldList.push(f);
    
    f = new Field(_('Signup.Form.LastName'), 'lastName');
    f.validators = function (elem) { return validateRequired(elem) };
    fieldList.push(f);
    
    f = new Field(_('Signup.Form.EMailAddress'), 'email');
    f.validators = function (elem) { return (validateRequired(elem) || 
        validateEMail(elem)) };
    fieldList.push(f);
    
    f = new Field(_('Signup.Form.Password'), 'password', 'password');
    f.validators = function (elem) { return (validateRequired(elem) || 
        validateMinLength(elem, 5)) };
    fieldList.push(f);
    
    f = new Field(_('Signup.Form.ConfirmPassword'), 'confirm', 'password');
    f.validators = function (elem1, elem2) { return (validateRequired(elem1) || 
        validateConfirmPass(elem1, elem2)) };
    fieldList.push(f);
    
    
    // Private methods
    // =============================
    // Shortcuts for DOM methods
    function createElem(t) {
        return document.createElement(t);
    }
    function createText(s) {
        return document.createTextNode(s);
    }
    
    /**
     * Data object for form fields
     * @param label String, the label that appears next to the form field.
     * @param elemName String, the name/id of the form element
     * @param elemType String, the type of form elem -- text/password
     * @param validators Function, an or-chain of functions to execute
     *     for validation of the element value.
     */
    function Field(label, elemName, elemType, validators) {
        this.label = label || '';
        this.elemName = elemName || '';
        this.elemType = elemType || 'text';
        this.validators = validators || function () {};
    }
    /**
     * Programmatically creates the table of form elements
     * used for signup. Loops through fieldList for all the
     * form fields.
     * @return Object (HtmlFormElement), form to append to the 
     *     content area of the modal dialog box.
     */
    function getFormTable() {
        var table = null;
        var body = null;
        var tr = null;
        var td = null;
        var elem = null;
        
        form = createElem('form');
        form.id = 'accountSignupForm';
        form.onsubmit = function () { return false; };
        
        table = createElem('table');
        body = createElem('tbody');
        
        // Table row for each form field
        for (var i = 0; i < fieldList.length; i++) {
            var f = fieldList[i];
            var type = f.elemType;
            
            // Create row
            tr = createElem('tr');
            
            // Label cell
            td = createElem('td');
            td.id = f.elemName + 'LabelCell';
            td.className = 'labelTextHoriz labelTextCell';
            // Label
            td.appendChild(createText(f.label + ':'));
            tr.appendChild(td);
            
            // Form field cell
            td = createElem('td');
            td.id = f.elemName + 'ElemCell';
            // Form field
            elem = createElem('input');
            elem.type = f.elemType;
            elem.elemName = f.elemName;
            elem.id = f.elemName;
            elem.maxlength = type == 'text' ? 32 : 16;
            elem.size = type == 'text' ? 32 : 16;
            elem.className = 'inputText';
            td.appendChild(elem);
            
            tr.appendChild(td);
            body.appendChild(tr)
        }
        table.appendChild(body);
        form.appendChild(table);
        return form;
    }
    /**
     * Validates the input from all the form fields -- calls the
     * associated validator prop (an or-chain of functions) for
     * each item, and appends errors per-element. Returns a single
     * error msg if any inputs yields an error.
     * @return String, global error message for form (empty
     *     if no element yielded an error).
     */
    function validateForm() {
        var err = '';
        var errRet = '';
        for (var i = 0; i < fieldList.length; i++) {
            var f = fieldList[i];
            cell = document.getElementById(f.elemName + 
                'ElemCell');  
            err = f.validators(form[f.elemName], form['password']);
            // Remove any previous err msg div
            child = cell.firstChild;
            if (child.nodeName.toLowerCase() == 'div') {
                cell.removeChild(child);
            }
            child = cell.firstChild;
            // At least one err msg string returned from chain of methods
            if (err) {
                // Set master err msg for return
                errRet = _('Signup.Error.Main'); 
                // Insert err msg div before text input
                div = createElem('div');
                div.className = 'inputError';
                div.appendChild(createText(err));
                cell.insertBefore(div, child);
            }
        }
        return errRet;
    }
    /**
     * Makes sure the given text input has a given length
     * @return String, error message (empty if no err).
     */
    function validateMinLength(elem, len) {
        err = '';
        val = elem.value;
        if (val.length < len) {
            err = _('Signup.Error.MinLength') + ' (' + len + ')';
        }
        return err;
    }
    /**
     * Makes sure the given text input is not empty
     * @return String, error message (empty if no err).
     */
    function validateRequired(elem) {
        err = '';
        val = elem.value;
        if (!val) {
            err = _('Signup.Error.RequiredField');
        }
        return err;
    }
    /**
     * Makes sure the given text input is a valid e-mail address
     * @return String, error message (empty if no err).
     */
    function validateEMail(elem) {
        // Just do really basic e-mail addr validation
        pat = /^.+@.+\..{2,3}$/;
        err = '';
        val = elem.value;
        if (!pat.test(val)) {
            err = _('Signup.Error.ValidEMail');
        }
        return err;
    }
    /**
     * Makes sure the given password field matches the other
     * @return String, error message (empty if no err).
     */
    function validateConfirmPass(elem, elemCompare) {
        err = '';
        val = elem.value;
        val2 = elemCompare.value;
        if (val != val2) {
            err = _('Signup.Error.MatchPassword');
        }
        return err;
    }
    /**
     * Handler function for XHR call to cosmo.cmp for signup. 
     * Sets error prompt if request fails, set up the results 
     * table with external client config on success.
     * @return Boolean, true on success, false on failure
     */
    function handleCreateResult(type, data, resp) {
        var err = '';
        if (type == 'error') {
            if (resp.status && (resp.status > 399)) {
                switch (resp.status) {
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
                            resp.status + ')';
                        break;
                }
            }
            else {
                err = _('Signup.Error.Generic') + ' (' + data.message + ')';
            }
            cosmo.app.modalDialog.setPrompt(err);
            return false;
        }
        else {
            accountUser = data;
            self.showResultsTable(data);
            return true;
        }
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
        var homedirUrl = new dojo.uri.Uri(user.homedirUrl);
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
        cfg['Path'] = homedirUrl.path;
        cfg['Username'] = username;
        cfg['Password'] = '(Hidden)';
        cfg['PortNumber'] = portNum;
        cfg['UseSSL'] = isSSL;
        cfg['FullURL'] = user.homedirUrl;

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
        var dO = createElem('div');
        
        if (user.unactivated) {
            p = createElem('div');
            p.style.marginBottom = '16px';
            p.style.textAlign = 'center';
            p.className = 'borderBox';
            p.appendChild(_createText(_('Signup.Prompt.AccountActivation')));
            dO.appendChild(p);
        }
        
        p = createElem('div');
        p.appendChild(_createText(_('Signup.Prompt.AccountSetup')));
        dO.appendChild(p);
        
        // Create the table, append rows for each config value
        var table = createElem('table');
        table.className = 'dataDisplay';
        table.style.width = '80%';
        table.style.margin = 'auto'; 
        table.style.marginTop = '12px';
        
        var body = createElem('tbody');

        // Create a row for each config setting
        for (var propName in cfg) {
            tr = createElem('tr');
            
            // Prop label
            td = createElem('td');
            td.className = 'dataDisplayLabel';
            td.appendChild(createText(_('Signup.Config.' + propName)));
            tr.appendChild(td);
            
            // Prop value
            td = createElem('td');
            td.appendChild(createText(cfg[propName]));
            tr.appendChild(td);
            body.appendChild(tr)
        }
        table.appendChild(body);
        dO.appendChild(table);
        
        /*
        ***** Leave this out until we can actually do auto-login *****
        // Link to begin using new account
        if (!user.unactivated) {
            p = createElem('div');
            p.style.marginTop = '12px';
            p.style.textAlign = 'center';
            a = createElem('a');
            a.href = 'javascript:cosmo.app.modalDialog.hide();';
            a.appendChild(createText(_('Signup.Links.LogInToCosmo')));
            p.appendChild(a);
            dO.appendChild(p);
        }
        */
        
        // Return the div containing the content
        return dO;
    }
    
    // Public methods
    // =============================
    /**
     * Sets up the dialog box with the table of form elements
     * and appropriate buttons for creating a new account.
     */
    this.showForm = function () {
        var o = {};
        o.width = 540;
        o.height = 480;
        o.title = 'Create an Account';
        o.prompt = _('Signup.Prompt.AllFieldsRequired');
        o.content = getFormTable();
        o.btnsLeft = [dojo.widget.createWidget("cosmo:Button", 
            { text:_('App.Button.Cancel'), width:74, 
            handleOnClick: function () { cosmo.app.modalDialog.hide(); } })];
        // Have to set empty center set of buttons -- showForm will be called
        // without buttons getting cleared by 'hide.'
        o.btnsCenter = []; 
        o.btnsRight = [dojo.widget.createWidget("cosmo:Button", 
            { text:_('App.Button.Submit'), width:74,
            handleOnClick: function () { self.submitCreate(); } })];
        o.defaultAction = function () { self.submitCreate(); };

        cosmo.app.modalDialog.show(o);
    };
    /**
     * Submit the call via XHR to cosmo.cmp to sign the user 
     * up for a new account.
     */
    this.submitCreate = function () {
        // Validate the form input using each field's
        // attached validators
        var err = validateForm();
        
        if (err) {
            cosmo.app.modalDialog.setPrompt(err);
        }
        else {
            var hand = { load: handleCreateResult, error: handleCreateResult };
            var user = {};
            // Create a hash from the form field values
            for (var i = 0; i < fieldList.length; i++) {
                f = fieldList[i];
                user[f.elemName] = form[f.elemName].value;
            }
            // Hand off to CMP
            cosmo.cmp.cmpProxy.signup(user, hand);
        }
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
        var btnsCenter = [dojo.widget.createWidget("cosmo:Button", 
            { text:_('App.Button.Close'), width:74, 
            handleOnClick: function () { cosmo.app.modalDialog.hide(); } })];
        
        // Update dialog in place
        d.setPrompt(prompt);
        d.setContent(content);
        d.setButtons([], btnsCenter, []);
        d.defaultAction = function () { cosmo.app.modalDialog.hide(); };
    };
}
