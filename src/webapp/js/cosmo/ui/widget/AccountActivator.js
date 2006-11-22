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

dojo.require("dojo.widget.*");
dojo.require("dojo.event.*");
dojo.require("dojo.dom");
dojo.require("cosmo.env");
dojo.require("cosmo.cmp");

dojo.provide("cosmo.ui.widget.AccountActivator");

dojo.widget.defineWidget("cosmo.ui.widget.AccountActivator", dojo.widget.HtmlWidget, {

    templatePath: dojo.uri.dojoUri(
        "../../cosmo/ui/widget/templates/AccountActivator/AccountActivator.html"),

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

    setActivationId: function(id){
        var setActivationIdHandlerDict = {
            handle: function(type, data, evt){
                if (evt.status == 200){
                    alert("activated")
                } else if (evt.status == 403){
                    alert("couldn't find user")
                } else {
                    alert(evt.status)
                }
            }
        }

        cosmo.cmp.cmpProxy.getUserXMLByActivationId(id, setActivationIdHandlerDict);


    }

});
