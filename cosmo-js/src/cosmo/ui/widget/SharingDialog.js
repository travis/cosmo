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

dojo.provide("cosmo.ui.widget.SharingDialog");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.Menu");
dojo.require("dojo.fx");
dojo.require("dojox.uuid");
dojo.require("cosmo.data.TicketStore");

dojo.requireLocalization("cosmo.ui.widget", "SharingDialog");

dojo.declare("cosmo.ui.widget.SharingDialog", [dijit._Widget, dijit._Templated],
{
    widgetsInTemplate: true,
    templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/SharingDialog.html'),
    l10n: dojo.i18n.getLocalization("cosmo.ui.widget", "SharingDialog"),

    // init params
    store: null,
    collection: null,
    ticketStore: null,

    // collection or subscription object
    displayName: "",
    urls: null,

    // attach points
    instructionsContainer: null,
    instructionsSelector: null,
    ticketContainer: null,

    readTicket: null,
    readWriteTicket: null,

    changeInstructions: function(e){
        this.instructionsContainer.innerHTML =
            this.l10n[this.instructionsSelector.value + "Instructions"];
    },

    onTicket: function(ticket){
        if (this.readTicket)        console.debug(ticket);
    },

    inviteReadOnly: function(){

    },

    inviteReadWrite: function(){

    },

    createReadTicket: function(){
            this.createTicket("read-only");
    },

    createReadWriteTicket: function(){
            this.createTicket("read-write");
    },

    createTicket: function(type){
        var key = dojox.uuid.generateTimeBasedUuid().slice(0, 8);
        var ticket = this.ticketStore.newItem({type: type, key: key});
        this.ticketStore.save({
            onComplete: dojo.hitch(this, function(){this.onTicket(ticket);}),
            onError: dojo.hitch(this, function(e){console.debug(e);})
        });
    },

    // lifecycle methods
    postMixInProperties: function(){
        var store = this.store;
        if (store){
            var collection = this.collection;
            this.displayName = store.getValue(collection, "displayName");
            this.urls = store.getValue(collection, "urls");
            if ((!this.ticketStore) && this.urls.ticket){
                this.ticketStore =
                    new cosmo.data.TicketStore({iri: this.urls.ticket, xhrArgs: this.xhrArgs});
            }
            dojo.addOnLoad(dojo.hitch(this, function(){
                this.tickets = this.ticketStore.fetch({
                    onItem: dojo.hitch(this, "onTicket"),
                    onError: function(e){console.debug(e);}
                });
            }));
        }
    }
});

