if(!dojo._hasResource["cosmo.ui.widget.PasswordResetter"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.ui.widget.PasswordResetter"] = true;
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
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("cosmo.ui.widget.Button");
dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare("cosmo.ui.widget.PasswordResetter", 
             [dijit._Widget, dijit._Templated],
    {

        templateString:"<div>\n<!--\n  Copyright 2006 Open Source Applications Foundation\n\n  Licensed under the Apache License, Version 2.0 (the \"License\");\n  you may not use this file except in compliance with the License.\n  You may obtain a copy of the License at\n\n      http://www.apache.org/licenses/LICENSE-2.0\n\n  Unless required by applicable law or agreed to in writing, software\n  distributed under the License is distributed on an \"AS IS\" BASIS,\n  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n  See the License for the specific language governing permissions and\n  limitations under the License.\n-->\n\n\t<p dojoAttachPoint=\"infoBox\" class=\"infoText\"></p>\n\t<p dojoAttachPoint=\"errorBox\" class=\"errorText\"></p>\n\t\n    <table dojoAttachPoint=\"tableContainer\" id=\"passwordResetterInputForm\" style=\"margin-left: auto; margin-right: auto; margin-top: 1em;\">\n\n        <tbody>\n            <tr>\n                <td dojoAttachPoint=\"passwordLabel\" class=\"labelTextCell\">\n                </td>\n                <td dojoAttachPoint=\"passwordInputContainer\">\n                \t<input type=\"password\" dojoAttachPoint=\"passwordInput\"/>\n                </td>\n            </tr>\n            <tr>\n                <td dojoAttachPoint=\"confirmLabel\" class=\"labelTextCell\">\n                </td>\n                <td dojoAttachPoint=\"confirmInputContainer\">\n                \t<input type=\"password\" dojoAttachPoint=\"confirmInput\"/>\n               \t</td>\n            </tr>\n            <tr>\n                <td></td>\n                <td dojoAttachPoint=\"sendButtonContainer\" id=\"sendButtonContainer\" style=\"text-align: right\">\n                \t<input dojoType=\"cosmo.ui.widget.Button\" type=\"button\" i18nText=\"Account.PasswordReset.Submit\" dojoAttachEvent=\"handleOnClick:resetPassword\"/>\n                </td>\n            </tr>\n\n        </tbody>\n    </table>\n</div>\n",

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
                var d = cosmo.cmp.resetPassword(this.recoveryKey, this.passwordInput.value);
                d.addCallback(function(data){
                    self.setInfo(_(self.i18nPrefix + ".Success",
                                   cosmo.env.getLoginRedirect()));
                });
                d.addErrback(function(error){
                    if (d.ioArgs.xhr.status == "404"){
                        self.setError(_(self.i18nPrefix + ".Error.404", self.recoveryKey));
                    } else {
                        self.setError(error.message);
                    }
                });
            } else {
                this.setError(_(this.i18nPrefix + ".Error.PasswordMatch"));
            }
        },

        postCreate: function(){
           if (this.displayDefaultInfo){
               this.setInfo(_(this.i18nPrefix + ".InitialInfo"));
           }

           this.passwordLabel.innerHTML = _(this.i18nPrefix + ".Password");
           this.confirmLabel.innerHTML = _(this.i18nPrefix + ".Confirm");
        }

    }
);

}
