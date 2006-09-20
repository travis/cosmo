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
 * @fileoverview Login -- used for user auth
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 */

/**
 * @object Login -- singleton for doing user auth
 */
dojo.require("cosmo.env");
var Login = new function () {
    
    var self = this;
    
    this.loginFocus = false;
    this.loginReq = null;
    this.loginForm = null; 
    this.authProc = '';

    this.init = function() {
        var but = new Button('submitButton', 74, Login.doLogin, 
            getText('App.Button.Submit'));
        self.loginForm = document.getElementById('loginForm');
        self.authProc = AUTH_PROC;
        self.loginForm.j_username.focus();
        // Add logo and button
        //document.getElementById('logoDiv').innerHTML = 
        //    '<img src="' + cosmo.env.getImagesUrl() + LOGO_GRAPHIC + '" alt="">';
        document.getElementById('submitButtonDiv').appendChild(but.domNode);
    }
    this.handleLoginResp = function(str) {
        if (str.indexOf('login.js') > -1) {
            self.showErr(getText('Login.Error.AuthFailed'));
            self.loginForm.j_password.value = ''; 
        }
        else {
            self.showPrompt('normal', 'Logging you on. Please wait ...');
            var username  = self.loginForm.j_username.value;
            if (username == "root") {
                location = cosmo.env.getBaseUrl() + "/console/account";
            } else {
                location = cosmo.env.getBaseUrl() + "/pim/pim.page";
            }
        }
    }
    this.doLogin = function() {
        var un = self.loginForm.j_username.value;
        var pw = self.loginForm.j_password.value;
        var postData = {};
        var err = '';

        if (!un || !pw) {
            err = getText('Login.Error.RequiredFields');
        }
        if (err) {
            self.showErr(err);
        }
        else {
            Cookie.set('username', un);
            postData = { 'j_username': un, 'j_password': pw };
            dojo.io.bind({
                url: self.authProc,
                method: 'POST',
                content: postData,
                load: function(type, data, evt) { Login.handleLoginResp(data); },
                error: function(type, error) { alert(error.message); }
            });
        }
    }

    this.showErr = function(str) {
        this.showPrompt('error', str);
    }

    this.showPrompt = function(promptType, str) {
        var promptDiv = document.getElementById('promptDiv');
        if (promptType.toLowerCase() == 'error') {
            promptDiv.className = 'promptTextError';
        }
        else {
            promptDiv.className = 'promptText';
        }
        promptDiv.innerHTML = str;
    }
    this.keyUpHandler = function(e) {
        e = !e ? window.event : e;
        if (e.keyCode == 13 && Login.loginFocus) {
            Login.doLogin();
            return false;
        }
    }
}

Login.constructor = null;

document.onkeyup = Login.keyUpHandler;
window.onload = Login.init;

