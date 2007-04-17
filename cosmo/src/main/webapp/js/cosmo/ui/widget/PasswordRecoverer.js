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
 * @fileoverview PasswordRecoverer - a widget takes a username and/or email address and 
 *                                  asks the server to send a password recovery email to
 *                                  the corresponding user.
 *
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.PasswordRecoverer");

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.dom");
dojo.require("cosmo.env");
dojo.require("cosmo.cmp");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.util.i18n");

dojo.require("cosmo.convenience");

dojo.widget.defineWidget("cosmo.ui.widget.PasswordRecoverer", dojo.widget.HtmlWidget,
    function(){

    },
    {

        templatePath: dojo.uri.dojoUri(
            "../../cosmo/ui/widget/templates/PasswordRecoverer/PasswordRecoverer.html"),
        templateCssPath: dojo.uri.dojoUri(
            "../../cosmo/ui/widget/templates/PasswordRecoverer/PasswordRecoverer.css"),

        widgetsInTemplate: true,
        displayDefaultInfo: false,
        i18nPrefix: "Account.PasswordRecover",
        
        //attach points
        tableContainer: null,
        
        errorBox: null,
        infoBox: null,
        usernameLabel: null,
        usernameInput: null,
        emailLabel: null,
        emailInput: null,

        sendButtonContainer: null,
        sendButton: null,
        
        setError: function(message){
            this.errorBox.innerHTML = message;  
        },
        
        setInfo: function(message){
            this.infoBox.innerHTML = message;  
        },

        recoverPassword: function(){
            var self = this;
            this.setError("");
            cosmo.cmp.recoverPassword(this.usernameInput.value, this.emailInput.value, 
                {error: function(type, data, xhr){
                    if (xhr.status == "404"){
                       self.setError(_(self.i18nPrefix + ".Error.404"));
                    } else {
                       self.setError(xhr.message);
                    }
                },
                 load: function(type, data, xhr){
                    self.setInfo(_(self.i18nPrefix + ".Success"));
                    self.tableContainer.style.visibility = "hidden";
                }
                });
        },
        
        fillInTemplate: function(){
            if (this.displayDefaultInfo){
                this.setInfo(_(this.i18nPrefix + ".InitialInfo"));
            }
            
            this.usernameLabel.innerHTML = _(this.i18nPrefix + ".Username");
            this.emailLabel.innerHTML = _(this.i18nPrefix + ".Email");
        }
    }
);
