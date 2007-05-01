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

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.dom");
dojo.require("cosmo.env");
dojo.require("cosmo.cmp");
dojo.require("cosmo.ui.button");
dojo.require("cosmo.util.i18n");

dojo.require("cosmo.convenience");

dojo.widget.defineWidget("cosmo.ui.widget.PasswordResetter", dojo.widget.HtmlWidget,
    function(){

    },
    {

        templatePath: dojo.uri.dojoUri(
            "../../cosmo/ui/widget/templates/PasswordResetter/PasswordResetter.html"),
        templateCssPath: dojo.uri.dojoUri(
            "../../cosmo/ui/widget/templates/PasswordResetter/PasswordResetter.css"),

        widgetsInTemplate: true,
        displayDefaultInfo: false,
        i18nPrefix: "Account.PasswordReset",

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
            var self = this;
            self.setError("");
            if (this.passwordInput.value == this.confirmInput.value){
                cosmo.cmp.resetPassword(this.recoveryKey, this.passwordInput.value,
                  {error: function(type, data, xhr){
                       if (xhr.status == "404"){
                           self.setError(_(self.i18nPrefix + ".Error.404", self.recoveryKey));
                       } else {
                          self.setError(data);
                       }
                     },
                   load: function(type, data, xhr){
                      self.setInfo(_(self.i18nPrefix + ".Success",
                          cosmo.env.getLoginRedirect()));
                     }
                   });
            } else {
                this.setError(_(this.i18nPrefix + ".Error.PasswordMatch"));
            }
        },

        fillInTemplate: function(){
           if (this.displayDefaultInfo){
               this.setInfo(_(this.i18nPrefix + ".InitialInfo"));
           }

           this.passwordLabel.innerHTML = _(this.i18nPrefix + ".Password");
           this.confirmLabel.innerHTML = _(this.i18nPrefix + ".Confirm");
        }

    }
);
