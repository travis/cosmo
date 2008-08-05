/*
 * Copyright 2008 Open Source Applications Foundation
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
dojo.provide("cosmo.ui.widget.AccountDeleter");

dojo.require("dijit.form.Button");
dojo.require("cosmo.cmp");
dojo.require("cosmo.env");

dojo.requireLocalization("cosmo.ui.widget", "AccountDeleter");
dojo.declare("cosmo.ui.widget.AccountDeleter", [dijit._Widget, dijit._Templated], {
    widgetsInTemplate: true,
    templateString: '<div><div id="${id}ConfirmMessage">${l10n.confirm} </div>'
                    + '<div id="${id}Buttons"><button dojoType="dijit.form.Button" id="${id}NoButton" dojoAttachEvent="onClick: noHandler">${l10n.no}</button>'
                    + '<button dojoType="dijit.form.Button" id="${id}YesButton" dojoAttachEvent="onClick: yesHandler">${l10n.yes}</button></div></div>',
    l10n: null,

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "AccountDeleter");
    },

    yesHandler: function(){
        cosmo.cmp.deleteAccount();
        this.deleteCallback();
        location = cosmo.env.getFullUrl("AccountDeleted");
    },

    noHandler: function(){
        this.destroy();
        this.cancelCallback();
    },

    // Callback to be overridden by client
    deleteCallback: function(){

    },

    // Callback to be overridden by client
    cancelCallback: function(){

    }
});



