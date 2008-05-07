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

dojo.provide("cosmo.ui.widget.TicketAdmin");
dojo.require("dijit._Templated");
dojo.require("dijit.form.Button");
dojo.require("dijit.Menu");
dojo.require("dojo.fx");
dojo.require("dojox.uuid");
dojo.require("cosmo.data.TicketStore");

dojo.requireLocalization("cosmo.ui.widget", "TicketAdmin");

dojo.declare("cosmo.ui.widget.TicketAdmin", [dijit._Widget, dijit._Templated],
{
    widgetsInTemplate: true,
    templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/TicketAdmin.html'),

    // init params
    ticketStore: null,
    urls: {},

    ticketContainer: null,

    constructor: function(){
        this.l10n = dojo.i18n.getLocalization("cosmo.ui.widget", "TicketAdmin");
    },

    onTicket: function(ticket){
        var t = new cosmo.ui.widget._SingleTicket(
            {ticketStore: this.ticketStore,
             ticket: ticket,
             urls: this.urls,
             l10n: this.l10n
            });
        dojo.place(t.domNode, this.ticketContainer, "last");
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
            onError: dojo.hitch(this, function(e){console.log(e);})
        });
    },

    showTicketRevokers: function(){
        this.forEachTicket(
            function(node){
                dijit.byNode(node).showRevoker();
            });
        this.showHideRevokeButton();
    },

    hideTicketRevokers: function(){
        this.forEachTicket(
            function(node){
                dijit.byNode(node).hideRevoker();
            });
        this.showShowRevokeButton();
    },

    showShowRevokeButton: function(){
        dojo.style(this.hideRevokeButton.domNode, "display", "none");
        dojo.style(this.showRevokeButton.domNode, "display", "block");
    },

    showHideRevokeButton: function(){
        dojo.style(this.showRevokeButton.domNode, "display", "none");
        dojo.style(this.hideRevokeButton.domNode, "display", "block");
    },

    forEachTicket: function(f){
        dojo.query(".sharingDialogTicket", this.ticketContainer).forEach(f);
    },

    // lifecycle methods
    postMixInProperties: function(){
        if ((!this.ticketStore) && this.url){
            this.ticketStore =
                new cosmo.data.TicketStore({iri: this.url, xhrArgs: this.xhrArgs});
        }
        dojo.addOnLoad(dojo.hitch(this, function(){
            this.tickets = this.ticketStore.fetch({
                onItem: dojo.hitch(this, "onTicket"),
                onError: function(e){console.log(e);}
            });
        }));
    }
});

dojo.declare("cosmo.ui.widget._SingleTicket", [dijit._Widget, dijit._Templated], {
    templatePath: dojo.moduleUrl("cosmo", 'ui/widget/templates/_SingleTicket.html'),

    ticketStore: null,
    ticket: null,
    urls: null,

    key: null,
    permission: null,
    urlsShowing: false,

    toggleUpUrl: dojo.moduleUrl(""),
    toggleDownUrl: dojo.moduleUrl(""),

    toggleUrls: function(){
        if(!this.urlsShowing) this.showUrls();
        else this.hideUrls();
    },

    showUrls: function(args){
        this.urlsShowing = true;
        dojo.addClass(this.urlToggler,"urlToggler-expanded");
        dojo.fx.wipeIn(dojo.mixin({node: this.urlsContainer, duration: 250}, args)).play();
    },

    hideUrls: function(args){
        this.urlsShowing = false;
        dojo.removeClass(this.urlToggler,"urlToggler-expanded");
        dojo.fx.wipeOut(dojo.mixin({node: this.urlsContainer, duration: 250}, args)).play();
    },

    showRevoker: function(){
        dojo.style(this.revoker, "visibility", "visible");
    },

    hideRevoker: function(){
        dojo.style(this.revoker, "visibility", "hidden");
    },

    revoke: function(){
        this.ticketStore.deleteItem(this.ticket);
        this.ticketStore.save(
            {
                onComplete: dojo.hitch(this, function(){
                    this.destroy();
                }),
                onError: function(e){
                    console.log(e);
                }
            }
        );
    },

    postMixInProperties: function(){
        this.key = this.ticketStore.getValue(this.urls.ticket, "key");
        this.type = this.ticketStore.getValue(this.urls.ticket, "type");
        if (!this.id) this.id = this.key + "SharingDialogTicket";
        var collectionUrls = this.urls;
        this.urls = {};
        for (urlName in collectionUrls){
            var url = collectionUrls[urlName];
            this.urls[urlName] = new dojo._Url(url.uri + (url.uri.indexOf("?") == -1 ? "?" : "&") + "ticket=" + this.key);
        }
    },

    postCreate: function(){
        this.hideUrls({duration: 1});
    }
});
