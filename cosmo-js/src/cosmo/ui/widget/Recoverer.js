if(!dojo._hasResource["cosmo.ui.widget.Recoverer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.ui.widget.Recoverer"] = true;
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
 * @fileoverview Recoverer - a widget takes a username and/or email address and
 *                                  asks the server to send a password recovery email to
 *                                  the corresponding user.
 *
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.Recoverer");

dojo.require("cosmo.env");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.declare("cosmo.ui.widget.Recoverer", [dijit._Widget, dijit._Templated],
    {

        templateString:"<div>\n<!--\n  Copyright 2006 Open Source Applications Foundation\n\n  Licensed under the Apache License, Version 2.0 (the \"License\");\n  you may not use this file except in compliance with the License.\n  You may obtain a copy of the License at\n\n      http://www.apache.org/licenses/LICENSE-2.0\n\n  Unless required by applicable law or agreed to in writing, software\n  distributed under the License is distributed on an \"AS IS\" BASIS,\n  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n  See the License for the specific language governing permissions and\n  limitations under the License.\n-->\n\n\t<p dojoAttachPoint=\"infoBox\" class=\"infoText\"></p>\n\t<p dojoAttachPoint=\"errorBox\" class=\"errorText\"></p>\n\t\n    <table dojoAttachPoint=\"tableContainer\" id=\"recovererInputForm\" style=\"margin-left: auto; margin-right: auto; margin-top: 1em;\">\n\n        <tbody>\n            <tr>\n                <td dojoAttachPoint=\"usernameLabel\" class=\"labelTextCell\">\n                </td>\n                <td dojoAttachPoint=\"usernameInputContainer\">\n                \t<input type=\"text\" dojoAttachPoint=\"usernameInput\"/>\n                </td>\n            </tr>\n            <tr>\n            \t<td class=\"labelTextCell\">${orText}</td>\n            \t<td>&nbsp;</td>\n            </tr>\n            <tr>\n                <td dojoAttachPoint=\"emailLabel\" class=\"labelTextCell\">\n                </td>\n                <td dojoAttachPoint=\"emailInputContainer\">\n                \t<input type=\"text\" dojoAttachPoint=\"emailInput\"/>\n               \t</td>\n            </tr>\n            <tr>\n                <td></td>\n                <td dojoAttachPoint=\"sendButtonContainer\" id=\"sendButtonContainer\" style=\"text-align: right\">\n                   \t<input dojoType=\"cosmo.ui.widget.Button\" dojoAttachPoint=\"sendButton\" type=\"button\" i18nText=\"${i18nPrefix}Submit\" dojoAttachEvent=\"handleOnClick:recover\" value=\"Submit\"/>\n                </td>\n            </tr>\n\n        </tbody>\n    </table>\n</div>\n",

        widgetsInTemplate: true,
        displayDefaultInfo: false,
        i18nPrefix: "",
        recoverFunctionModule: "",
        recoverFunctionName: "",

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

        orText: _("Account.Recoverer.Or"),

        // Must return instance of dojo.Deferred
        recoverFunction: function(){},

        setError: function(message){
            this.errorBox.innerHTML = message;
        },

        setInfo: function(message){
            this.infoBox.innerHTML = message;
        },

        recover: function(){
            var self = this;
            this.setError("");
            var d = this.recoverFunction(this.usernameInput.value, this.emailInput.value);
            d.addCallback(function(data){
                    self.setInfo(_(self.i18nPrefix + "Success"));
                    self.tableContainer.style.visibility = "hidden";
            });
            d.addErrback(function(error){
                if (d.ioArgs.xhr.status == "404"){
                    self.setError(_(self.i18nPrefix + "Error.404"));
                } else {
                    self.setError(xhr.message);
                }
            });
        },

        postCreate: function(){
            if (this.displayDefaultInfo){
                this.setInfo(_(this.i18nPrefix + "InitialInfo"));
            }

            this.usernameLabel.innerHTML = _(this.i18nPrefix + "Username");
            this.emailLabel.innerHTML = _(this.i18nPrefix + "Email");
        },

        postMixInProperties: function(){
            dojo.require(this.recoverFunctionModule);
            var module = dojo.getObject(this.recoverFunctionModule) || dojo.global;
            this.recoverFunction = dojo.hitch(module, this.recoverFunctionName);
        }
    }
);

}
