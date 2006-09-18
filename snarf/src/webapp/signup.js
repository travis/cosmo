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
 * @fileoverview Signup obj to create new Cosmo user accounts
 * using CMP (Cosmo Management Protocol)
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object Signup form object for entering data to create a new 
 * Cosmo user account. NOTE: This is not the actual <form> -- there
 * is a property 'form' in this object that points to the 
 * form DOM element.
 */
SignupForm = new function() {
    
    // form DOM element
    this.form = null;
    // List of form elements -- each item in the array is a
    // FormElem object
    this.elemList = [];
    
    
    /**
     * Elements for the signup form --
     * The validateFunc property is a closure, or series of them. 
     * Wrapping in a function makes 'this' properly reference the 
     * FormElem obj when you execute as elem.validataFunc()
     * The function returns an err msg or empty string
     * Calling with logical 'or' lets you 'chain' validation methods
     */
    this.elemList.push(new FormElem('Username', 'username', 'text',
        function() { return (this.validateRequired() || this.validateMinLength(3)) }));
    this.elemList.push(new FormElem('First Name', 'firstName', 'text',
        function() { return this.validateRequired() }));
    this.elemList.push(new FormElem('Last Name', 'lastName', 'text',
        function() { return this.validateRequired() }));
    this.elemList.push(new FormElem('E-Mail Address', 'eMail', 'text',
        function() { return (this.validateRequired() || this.validateEMail()) }));
    this.elemList.push(new FormElem('Password', 'password', 'password',
        function() { return (this.validateRequired() || this.validateMinLength(5)) }));
    this.elemList.push(new FormElem('Confirm Password', 'confirm', 'password',
        function() { return (this.validateRequired() || this.validateConfirmPass()) }));
    
    /**
     * Returns the actual form DOM element used in the signup form
     */
    this.getForm = function() {
        
        var f = null   
        var t = null;
        var b = null;
        var tr = null;
        var td = null;
        var elem = null;
        var type = '';
        
        f = document.createElement('form');
        f.id = 'accountSignupForm';
        f.onsubmit = function() { return false; };
        
        t = document.createElement('table');
        b = document.createElement('tbody');
        
        for (var i = 0; i < this.elemList.length; i++) {
            type = this.elemList[i].elemType;
            tr = document.createElement('tr');
            td = document.createElement('td');
            td.id = this.elemList[i].elemName + 'LabelCell';
            td.className = 'formLabel';
            td.appendChild(document.createTextNode(this.elemList[i].label));
            tr.appendChild(td);
            td = document.createElement('td');
            td.id = this.elemList[i].elemName + 'ElemCell';
            elem = document.createElement('input');
            elem.type = this.elemList[i].elemType;
            elem.elemName = this.elemList[i].elemName;
            elem.id = this.elemList[i].elemName;
            elem.maxlength = type == 'text' ? 32 : 16;
            elem.size = type == 'text' ? 32 : 16;
            elem.className = 'textInput';
            td.appendChild(elem);
            tr.appendChild(td);
            b.appendChild(tr)
        }
        t.appendChild(b);
        f.appendChild(t);
        this.form = f;
        
        return f;
    };
    /**
     * Returns a div wrapped around the form DOM elem for proper centering
     */
    this.getFormDiv = function() {
        var d = document.createElement('div');
        d.className = 'centerAlignInner';
        d.style.width = '400px';
        d.appendChild(this.getForm());
        return d;
    };
    /**
     * Validates all the elements by executing the func or funcs in their
     * validateFunc property. If any of them return an err msg string,
     * it inserts it into the proper place above the form elem, and the
     * entire function then returns an error string. It there are no error,
     * it returns an empty string.
     */
    this.validate = function() {
        var err = '';
        var cell = null;
        var child = null;
        var div = null;
        var errRet = '';
        
        // Go through all the FormElem objects
        for (var i = 0; i < this.elemList.length; i++) {
            cell = null;
            child = null;
            div = null;
            
            // Get ref to cell for the text input
            cell = document.getElementById(this.elemList[i].elemName + 
                'ElemCell');  
            // Remove any previous err msg div
            child = cell.firstChild;
            if (child.nodeName.toLowerCase() == 'div') {
                cell.removeChild(child);
            }
            child = cell.firstChild;
            
            // Validate by calling all the funcs stored in validateFunc
            // ===================
            err = this.elemList[i].validateFunc();
            
            // At least one err msg string returned from chain of methods
            if (err) {
                // Set master err msg for return
                errRet = 'There was an error in the form input.'
                // Insert err msg div before text input
                div = document.createElement('div');
                div.className = 'formElemErr';
                div.appendChild(document.createTextNode(err));
                cell.insertBefore(div, child);
            }
        }
        // Return main err msg
        return errRet;
    }
}
SignupForm.constructor = null;

/**
 * @object Form element obj with label, type, name, and chain of
 * functions to use when validating input
 */
function FormElem(label, elemName,  elemType, validateFunc) {
    // Displayed in left cell
    this.label = label;
    // Text input name and id
    this.elemName = elemName;
    // Type text/password
    this.elemType = elemType;
    // Chain of methods to call to validate input from this elem
    this.validateFunc = validateFunc;
    
    /**
     * Makes sure the given text input has a given length
     */
    this.validateMinLength = function(len) {
        err = '';
        val = SignupForm.form[this.elemName].value;
        if (val.length < len) {
            err = 'This field must be at least ' + len + ' characters long.';
        }
        return err;
    };
    /**
     * Makes sure the given text input is not empty
     */
    this.validateRequired = function() {
        err = '';
        val = SignupForm.form[this.elemName].value;
        if (!val) {
            err = 'This field is required.';
        }
        return err;
    };
    /**
     * Makes sure the given text input is a valid e-mail address
     */
    this.validateEMail = function() {
        // Just do really basic e-mail addr validation
        pat = /^.+@.+\..{2,3}$/;
        err = '';
        val = SignupForm.form[this.elemName].value;
        if (!pat.test(val)) {
            err = 'This field must be a valid e-mail address.';
        }
        return err;
    };
    /**
     * Makes sure the given password field matches the other
     */
    this.validateConfirmPass = function() {
        err = '';
        val = SignupForm.form[this.elemName].value;
        val2 = SignupForm.form['password'].value;
        if (val != val2) {
            err = 'This field must match Password field.';
        }
        return err;
    };
}

/**
 * @object Ties together the SignupForm and SimpleDialog objects
 * to create new Cosmo accounts with CMP.
 */
var Signup = new function() {

    // Simple Ajax object for making requests
    this.ajax = new Ajax();
    // Dialog box object
    this.dialog = SimpleDialog;
    // Signup form object
    this.signupForm = SignupForm;
    // Array of config settings when account is created
    // each item should be a ClientConfig object
    this.conf = [];
    
    /**
     * Sets the state of the dialog box -- two states: 'initial,' which
     * displays the signup form, and 'result,' which show the table with
     * the config settings for the new account.
     * The init state actually recreates the dialog each time, to start
     * with a clean slate each time you open the dialog -- so the dialog
     * does not maintain state after you close it.
     */
    this.setDialog =  function(state) {
        var self = Signup;
        
        self.dialog.title = 'Create an Account';
        
        switch (state) {
            case 'init':
                var b = null;
                
                self.dialog.promptText = 'All fields are required';
                self.dialog.clearButtonArrays();
                self.dialog.defaultAction = self.doCMPReq;
                
                // Content is Web form for signup
                self.dialog.content = self.signupForm.getFormDiv();
                
                b = document.createElement('input');
                b.type = 'button';
                b.value = 'Cancel';
                b.onclick = self.dialog.hide;
                b.className = 'buttonInput';
                self.dialog.buttonsLeft.push(b);
                
                b = document.createElement('input');
                b.type = 'button';
                b.value = 'Submit';
                b.onclick = Signup.doCMPReq;
                b.className = 'buttonInput';
                self.dialog.buttonsRight.push(b);
                
                self.dialog.create();
                
                break;
            case 'result':
                var b = null;
                
                self.dialog.promptText = 'You have successfully created this account.';
                self.dialog.promptType = 'normal';
                self.dialog.clearButtonArrays();
                self.dialog.defaultAction = self.dialog.hide;
                
                // Content is table with client config
                self.dialog.content = self.getResultTable();
                
                b = document.createElement('input');
                b.type = 'button';
                b.value = 'Close';
                b.onclick = self.dialog.hide;
                b.className = 'buttonInput';
                self.dialog.buttonsCenter.push(b);      
                
                self.dialog.putPrompt();
                self.dialog.putContent();
                self.dialog.putButtons();
                
                break;
        }
        return true;
    };
    /**
     * Shows the dialog initially -- the 'init' state actually wipes
     * and starts over with a completely new dialog box. This means 
     * the dialog doesn't maintain state after you close it.
     */
    this.showDialog = function() {
        var self = Signup;
        self.setDialog('init');
        self.dialog.show();
        
    };
    /**
     * Does the XHR request using CMP to create the new account
     * Safari's XHR implementation doesn't support PUT, so we have
     * to use POST.
     * The handleCMPReq method handles the both success and error
     * responses from the server.
     */
    this.doCMPReq = function() {
        var self = Signup;
        var form = self.signupForm.form;
        var putData = '';
        var ajax = self.ajax;
        var err = '';
        
        err = self.signupForm.validate();

        if (err) {
            self.dialog.putPrompt(err, 'err');
        }
        else {
            putData += '<?xml version="1.0" encoding="utf-8" ?>' +
                '<user xmlns="http://osafoundation.org/cosmo/CMP">' +
                '<username>' + form.username.value + '</username>' +
                '<password>' + form.password.value + '</password>' +
                '<firstName>' + form.firstName.value + '</firstName>' +
                '<lastName>' + form.lastName.value + '</lastName>' +
                '<email>' + form.eMail.value + '</email>' +
                '</user>';
            
            ajax.url = '/cosmo/cmp/signup';
            ajax.dataPayload = putData;
            ajax.responseFormat = 'object';
            ajax.method = 'POST';
            ajax.setHandlerBoth(self.handleCMPReq);
            ajax.setRequestHeader('Content-Type', 'text/xml; charset=utf-8');
            ajax.setRequestHeader('Content-Length', putData.length);
            ajax.doReq();
        }
    };
    /**
     * Handles the XHR response. Safari can't deal with any success code
     * other than '200' -- status prop of XHR is undefined in those cases.
     * But it does create the account, so we check for errors, and anything
     * not an error assumes success.
     */
    this.handleCMPReq = function(resp) {
        var self = Signup;
        var form = self.signupForm.form;
        var err = ''; 
        var username = '';
        var isSSL = false;
        var hostArr = []
        var hostName = '';
        var portNum = 0;
        var fullUrl = '';
        
        // Error -- Safari seems to report 400 and 500 series errors properly
        if (resp.status && (resp.status > 399)) {
            switch (resp.status) {
                case 431:
                    err = 'The username you entered is already in use.<br/>Please pick another username and try again.';
                    break;
                case 432:
                    err = 'The e-mail address you entered is already in use.<br/>Please enter a different e-mail address and try again.';
                    break;
                default:
                    err = 'An error occurred setting up the user account (error code ' + resp.status + ').';
                    break;
            }
        }
        
        if (err) {
            self.dialog.putPrompt(err, 'err');
        }
        // Anything but a known error assumes success
        // because Safari can't recognize anything but plain 200
        // It reports the '201: Created' as 'undefined'
        else {
            self.conf = [];
            // Username for created account
            username = form.username.value;
            // Server settings -- pull from JS
            isSSL = location.protocol.indexOf('https:') > -1 ? true : false;
            hostArr = location.hostname.split(':');
            hostName = hostArr[0];
            // Port -- if none specified use 80 (or 443 for https)
            if (location.port) {
                portNum = location.port;
            }
            else {
                portNum = isSSL ? 443 : 80;
            }
            // String to display for SSL
            isSSL = isSSL ? 'Yes' : 'No';
            // Build the full URL
            fullUrl = location.protocol + '//' + hostName;
            fullUrl += location.port ? ':' + location.port : '';
            fullUrl += '/cosmo/home/' + username + '/';
            // Array of config settings for account setup
            self.conf.push(new ClientConfig('Server', hostName));
            self.conf.push(new ClientConfig('Path', '/cosmo/home/' + username));
            self.conf.push(new ClientConfig('Username', username));
            self.conf.push(new ClientConfig('Password', '(Hidden)'));
            self.conf.push(new ClientConfig('Port Number', portNum));
            self.conf.push(new ClientConfig('Use SSL', isSSL));
            self.conf.push(new ClientConfig('Full URL', fullUrl));
            
            self.setDialog('result');
        }
    };
    /**
     * The table of config settings for setting up a sharing acount
     * and the links to create another new account or to log into
     * Cosmo
     * @returns A div containing the table of config settings
     */
    this.getResultTable = function() {
        var dO = null;
        var dI = null;
        var p = null;
        var a = null;
        var t = null;
        var b = null;
        var tr = null;
        var td = null;
        
        dO = document.createElement('div');
        dO.className = 'centerAlignOuter';
        
        dI = document.createElement('div');
        dI.className = 'centerAlignInner';
        dI.style.width = '80%';
        
        dO.appendChild(dI);
        
        p = document.createElement('div');
        p.appendChild(document.createTextNode('This is the information you need to set up a sharing account in your calendar client:'));
        p.style.fontSize = '11px';
        p.style.marginBottom = '8px';
        dI.appendChild(p);
        
        // Create the table, append rows for each ClientConfig obj
        t = document.createElement('table');
        t.style.width = '100%';        
        b = document.createElement('tbody');
        for (var i = 0; i < this.conf.length; i++) {
            tr = document.createElement('tr');
            td = document.createElement('td');
            td.className = 'clientConfigLabel';
            td.appendChild(document.createTextNode(this.conf[i].display));
            tr.appendChild(td);
            td = document.createElement('td');
            td.className = 'clientConfigValue';
            td.appendChild(document.createTextNode(this.conf[i].setting));
            tr.appendChild(td);
            b.appendChild(tr)
        }
        t.appendChild(b);
        dI.appendChild(t);
        
        p = document.createElement('div');
        p.style.marginTop = '20px';
        a = document.createElement('a');
        a.href = '/cosmo/';
        a.appendChild(document.createTextNode('Click here'));
        p.appendChild(a);
        p.appendChild(document.createTextNode(' to begin using your new account.'));
        dI.appendChild(p);
        
        p = document.createElement('div');
        p.style.marginTop = '12px';
        a = document.createElement('a');
        a.href = 'javascript:Signup.showDialog();';
        a.appendChild(document.createTextNode('Click here'));
        p.appendChild(a);
        p.appendChild(document.createTextNode(' to create another new account.'));
        dI.appendChild(p);
        
        return dO;
    };
    /**
     * Check for Enter key press, do dialog default action if 
     * dialog box is visible.
     */
    this.keyUpHandler = function(e) {
        var self = Signup;
        e = e || window.event;
        if (e.keyCode == 13 && 
            self.dialog.isDisplayed && self.dialog.defaultAction) {
            self.dialog.defaultAction();
        }
    };
}
Signup.constructor = null;

/**
 * @object Configuration setting for new user account to set up
 * cal sharing in calendaring client.
 */
function ClientConfig(display, setting) {
    this.display = display;
    this.setting = setting;
}

window.onkeyup = Signup.keyUpHandler;
