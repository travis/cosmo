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
dojo.provide("cosmo.ui.widget.CollectionSelector");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("cosmo.env");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.app.pim.layout");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.convenience");
dojo.require("cosmo.topics");
dojo.require("cosmo.ui.widget.CollectionDetailsDialog");
dojo.require("cosmo.ui.widget.AuthBox");

dojo.requireLocalization("cosmo.ui.widget", "CollectionSelector");

dojo.declare(
    "cosmo.ui.widget.CollectionSelector", 
    [dijit._Widget, dijit._Templated],
    {
        templatePath: dojo.moduleUrl("cosmo", "ui/widget/templates/CollectionSelector.html"),
        collection: null,
        ticketKey: '',
        displayName: "",

        l10n: dojo.i18n.getLocalization("cosmo.ui.widget", "CollectionSelector"),

        //references to various DOM nodes
        displayNameText: null, 

        constructor: function(collection, ticketKey){
            this.setCollection(collection);
            this.ticketKey = ticketKey;
        },

        setCollection: function(collection){
            this.collection = collection;
            this.displayName = collection.getDisplayName();
        },

        // Grab subscription information from server, make sure displayName is not a duplicate
        // and add the current collection to the user's subscriptions
        subscribe: function (subscription) {
            var displayName = subscription.getDisplayName()
            var collection = this.collection;
            cosmo.app.showDialog({"content" : this.l10n.loadingInfo})
            var collectionsDeferred = cosmo.app.pim.serv.getCollections();
            var subscriptionsDeferred = cosmo.app.pim.serv.getSubscriptions();
            var dList = new dojo.DeferredList(
                [collectionsDeferred, subscriptionsDeferred]
            );
            dList.addBoth(function(args){
                cosmo.app.hideDialog();
                return args;
            });
            dList.addCallback(function (results) {
                var collections = results[0][1];
                collections = collections.concat(results[1][1]);
                return collections;
            })
            dList.addCallback(dojo.hitch(this, function (collections){
                var alreadySubscribed = this._collectionWithUidExists(collections, collection.getUid());
                if (alreadySubscribed) {
                    var message = alreadySubscribed == "cosmo.model.Collection"
                        ? this.l10n.subscribedOwn
                        : this.l10n.subscribed;
                    return cosmo.app.showAndWait(message);
                } 
                
                if (!this._collectionWithDisplayNameExists(collections, displayName)) return displayName;
                else {
                    var displayNameDeferred = cosmo.app.getValue(
                        dojo.string.substitute(this.l10n.nameExists, [displayName]),
                        collection.getDisplayName(), 
                        [
                            dojo.hitch(this, function (displayName){
                                if (!this._validateDisplayName(displayName)){
                                    return this.l10n.enterNamePrompt;
                                }
                            }),
                            dojo.hitch(this, function (displayName){
                                if (this._collectionWithDisplayNameExists(collections, displayName)){
                                    return dojo.string.substitute(this.l10n.nameExists, [displayName]);
                                }
                            })
                        ]);
                    
                    return displayNameDeferred;
                }
            }));
            dList.addCallback(dojo.hitch(this, function(displayName){
                subscription.setDisplayName(displayName);
                return cosmo.app.pim.serv.createSubscription(subscription);
            }));
            return dList;
        },

        // Called by clicking on the "Add to my account...: link
        _onClickAdd: function () {
            var deferred = this._confirmSubscribe();
            deferred.addCallback(dojo.hitch(this, function(displayName){
                return new cosmo.model.Subscription({
                    displayName: displayName,
                    uid: this.collection.getUid(),
                    ticketKey: this.ticketKey
                })
            }));
            if (!cosmo.util.auth.currentlyAuthenticated()) 
                deferred.addCallback(dojo.hitch(this, this._authenticate));
            
            deferred.addCallback(dojo.hitch(this, this.subscribe));
            deferred.addCallback(dojo.hitch(this, this._redirectFunction));
            deferred.addErrback(dojo.hitch(this, function (err) {
                cosmo.app.showErr(this.l10n.addError, err);
            }));
        },

        _redirectFunction: function (collection) {
            cosmo.app.showDialog(
                {"prompt": this.l10n.redirectPrompt}
            );
            // Log the user into Cosmo and display the current collection
            location = cosmo.env.getBaseUrl() + '/pim/collection/' + this.collection.getUid();
        },

        _authenticate: function(subscription){
            var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties({
                authInitPrompt: this.l10n.authPrompt,
                authProcessingPrompt: null, // Use the default
                attemptPrompt: this.l10n.attemptPrompt,
                successPrompt: this.l10n.successPrompt,
                subscription: subscription
            });
            var authD = cosmo.app.showDialog(authBoxProps);
            cosmo.app.modalDialog.content.usernameInput.focus();
            authD.addCallback(function(){cosmo.app.hideDialog()});
            // Return subscription for callback chain
            authD.addCallback(function(){return subscription});
            return authD;
        },

        _confirmSubscribe: function(){
            confirmContent = _createElem("div");
            confirmContent.style.lineHeight = "1.5";
            confirmContent.innerHTML = 
                [this.l10n.confirmAddPre, 
                 "<input type='text' id='getValueInput' class='inputText' value='",
                 this.collection.getDisplayName(), "'/>",
                 this.l10n.confirmAddPost].join("");
            return cosmo.app.getValue(
                "", "", [],
                {
                    content: confirmContent,
                    showCancel: true,
                    defaultActionButtonText: this.l10n.confirmOkay
                }
            );
        },

        _getIndexByDisplayName: function(collections, displayName){
            for (var x = 0; x < collections.length; x++){
                if (collections[x].getDisplayName() == displayName){
                    return x;
                }
            }
            return -1;
        },

        _collectionWithDisplayNameExists: function(cols, displayName){
            var index = this._getIndexByDisplayName(cols, displayName);
            return index != -1;
        },

        _collectionWithUidExists: function(cols, uid){
            for (var x = 0; x < cols.length; x++){
                if (cols[x].getUid() == uid){
                    return cols[x].declaredClass;
                }
            }
            return false;
        },

        _validateDisplayName: function (displayName) {
            //is there anything else?
            return displayName != "";
        }
} );

