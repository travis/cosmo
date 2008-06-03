/*
 * Copyright 2007 Open Source Applications Foundation
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
 * @fileoverview PasswordResetter - a widget takes a username and/or email address and
 *                                  asks the server to send a password recovery email to
 *                                  the corresponding user.
 *
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.PasswordResetter");

dojo.require("cosmo.env");
dojo.require("cosmo.cmp");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.ValidationTextBox");
dojo.require("dijit.form.Button");
dojo.require("dojo.string");

dojo.requireLocalization("cosmo.ui.widget", "PasswordResetter");

dojo.declare("cosmo.ui.widget.PasswordResetter",
             [dijit._Widget, dijit._Templated],
    {

        templatePath: dojo.moduleUrl(
            "cosmo", "ui/widget/templates/PasswordResetter.html"),

        widgetsInTemplate: true,
        displayDefaultInfo: false,

        recoveryKey: "",

        //attach points
        errorBox: null,
        infoBox: null,
        passwordLabel: null,
        passwordInput: null,
        confirmLabel: null,
        confirmInput: null,

        sendButtonContainer: null,
        sendButton: null,

        setError: function(message){
            this.errorBox.innerHTML = message;
        },

        setInfo: function(message){
            this.infoBox.innerHTML = message;
        },

        resetPassword: function(){
            this.setError("");
            if (this.validate()){
                var d = cosmo.cmp.resetPassword(this.recoveryKey, this.passwordInput.value);
                d.addCallback(dojo.hitch(this, function(data){
                    this.setInfo(dojo.string.substitute(this.l10n.success,
                                   [cosmo.env.getLoginRedirect()]));
                }));
                d.addErrback(dojo.hitch(this, function(error){
                    if (d.ioArgs.xhr.status == "404"){
                        this.setError(dojo.string.substitute(this.l10n.error404, [this.recoveryKey]));
                    } else {
                        this.setError(error.message);
                    }
                }));
            }
        },

        validate: function(){
            if (this.passwordInput.value != this.confirmInput.value){
                this.setError(this.l10n.errorPasswordMatch);
            } else if (this.passwordInput.validate() &&
                       this.confirmInput.validate()) return true;
            return false;
         },

        constructor: function(){
            this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "PasswordResetter");
        }
    }
);
