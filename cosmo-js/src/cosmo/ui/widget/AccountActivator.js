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
 * @fileoverview AccountActivator - a widget that can display and activate a user based
 *     on an activation id.
 *
 * @author Travis Vachon travis@osafoundation.org
 * @license Apache License 2.0
 */

dojo.provide("cosmo.ui.widget.AccountActivator");

dojo.require("cosmo.env");
dojo.require("cosmo.cmp");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");
dojo.require("dijit._Templated");

dojo.declare("cosmo.ui.widget.AccountActivator", [dijit._Templated],
    {

        templatePath: dojo.moduleUrl(
            "cosmo", "/ui/widget/templates/AccountActivator.html"),

        //properties to be set by tag or constructor
        activationId: "",

        //attach points
        usernameLabel: null,
        usernameText: null,
        nameLabel: null,
        nameText: null,
        emailLabel: null,
        emailText: null,
        urlLabel: null,
        urlText: null,
        homedirUrlLabel: null,
        homedirUrlText: null,
        activateButtonContainer: null,

        postCreate: function (){
            var button = new cosmo.ui.widget.Button(
                {text: _("Activation.Activate"),
                 id: "accountActivateButton"});

            this.activateButtonContainer.appendChild(button.domNode);

            dojo.connect(button, "handleOnClick",this, "_activateEventHandler");

        },

        setActivationId: function (id){
            var self = this;
            self.activationId = id;

            var d = cosmo.cmp.getUserByActivationId(id, setActivationIdHandlerDict);
            d.addCallback(function(){
                self.usernameText.innerHTML = user.username;
                self.nameText.innerHTML = user.firstName + " " + user.lastName;
                self.emailText.innerHTML = user.email;
                self.urlText.innerHTML = user.url;
                self.homedirUrlText.innerHTML = user.homedirUrl;
            });

            d.addErrback(function(){
                if (d.ioArgs.xhr.status == 403){
                    alert(_("Account.Activate.UserNotFound"));
                } else {
                    alert(_("Account.Activate.UserNotFound") + ": " +
                          evt.status);
                }
            });



        },

        _activateEventHandler: function(){
            var d = this.activate();
            d.addCallback(dojo.hitch(this, this.activateSuccess));
            d.addErrback(dojo.hitch(this, this.activateFailure));
        },

        activateSuccess: function(data){},

        activateFailure: function(err){},

        activate: function (id){
            var activationId = (id == undefined) ? this.activationId : id;

            if (activationId == "" || activationId == undefined){
                throw new Error("Activation id not specified");
            } else {
                cosmo.cmp.activate(activationId);
            }
        }
    }
);
