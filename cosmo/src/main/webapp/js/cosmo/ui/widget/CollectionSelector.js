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

dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.app.pim");
dojo.require("cosmo.app.pim.layout");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.convenience");
dojo.require("cosmo.topics");
dojo.require("cosmo.ui.widget.CollectionDetailsDialog");
dojo.require("cosmo.ui.widget.AuthBox");


dojo.widget.defineWidget("cosmo.ui.widget.CollectionSelector",
    dojo.widget.HtmlWidget,

    //initializer
    function () {
        dojo.event.topic.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName, this, this.handleCollectionUpdated);
        dojo.event.topic.subscribe(cosmo.topics.SubscriptionUpdatedMessage.topicName, this, this.handleSubscriptionUpdated);
    },
    {
        templateString: '<span></span>',
        verticalHeight: 18,
        collections: [],
        currentCollection: {},
        ticketKey: '',
        MAX_DISPLAY_NAME_LENGTH: 18,

        // Function for onchange of collection selector
        // sets local currentCollection and passes the selected
        // collection to cosmo.app.pim.loadCollectionItems
        // --------
        selectFunction: function (e) {
            var t = e.target;
            // Set local currentCollection var
            var c = this.collections[t.selectedIndex];
            this.currentCollection = c;
            cosmo.app.pim.currentCollection = c;
            // Publish this through a setTimeout call so that the
            // select box doesn't just sit open while waiting for
            // the collection data to load and the UI to render
            var loading = cosmo.app.pim.layout.baseLayout.mainApp.centerColumn.loading;
            var f = function () { dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection', opts: { loadType: 'changeCollection',
                collection: c }, data: {}
            }); };
            loading.show();
            setTimeout(f, 0);
        },

        strings: {
            collectionAddPrompt: _('Main.CollectionAdd.Prompt'),
            collectionAddAuthPrompt: _('Main.CollectionAdd.AuthPrompt'),
            collectionAddTooltip: _('Main.CollectionAdd.Tooltip'),
            imgTitleInfo: _('Main.CollectionDetail.Tooltip'),
            attemptPrompt: _('Main.CollectionAdd.AttemptPrompt'),
            collectionAddError: _('Main.CollectionAdd.ErrorPrompt'),
            successPrompt: _('Main.CollectionAdd.SuccessPrompt')
        },

        //references to various DOM nodes
        selector: null,  //the actual select element, if there are >1 collections
        displayNameText: null, //the display name if there is just one collection

        /**
         * Inserts the select box for choosing from multiple collections
         * Only actually called if multiple collections exist
         */
        fillInTemplate: function () {
            var self = this;
            var col = this.collections;
            var curr = this.currentCollection;
            var passedKey = this.ticketKey; // Indicates we're in ticket view

            // Collection selector / label and 'add'/'info' icons
            var selectorNode = _createElem('div');
            selectorNode.id = 'collectionSelectorOrLabel';

            // Ticket view
            function renderSingleCollectionName() {
                var imgPath = 'subscribe';
                var imgTitle = self.strings.imgTitleAdd;
                // Set up callback for deferred -- this is the action to take
                // if auth succeeds
                var subscribeFunction = function () {
                        cosmo.app.showDialog({"content" : _("Main.CollectionAdd.LoadingInfo")})
                        var collectionsDeferred = cosmo.app.pim.serv.getCollections();
                        var subscriptionsDeferred = cosmo.app.pim.serv.getSubscriptions();
                        var dList = new dojo.DeferredList(
                            [collectionsDeferred, subscriptionsDeferred]
                            );
                        dList.addCallback(function (results) {
                            var collections = results[0][1];
                            collections = collections.concat(results[1][1]);
                            return collections;
                        })
                        dList.addCallback(function (collections){
                            cosmo.app.hideDialog();
                            var alreadySubscribed = self._collectionWithUidExists(collections, curr.getUid());
                            if (alreadySubscribed) {
                                var message = alreadySubscribed == "cosmo.model.Collection"
                                    ? _("Main.CollectionAdd.AlreadySubscribedOwnCollection")
                                    : _("Main.CollectionAdd.AlreadySubscribedToSubscription");
                                return cosmo.app.showAndWait(message, null);
                            }
    
                            var displayName = "";
    
                            var displayNameDeferred = cosmo.app.getValue(_("Main.CollectionAdd.EnterDisplayNamePrompt"), 
                                               curr.getDisplayName(), [
                                               function (displayName){
                                                   if (!self._validateDisplayName(displayName)){
                                                       return _("Main.CollectionAdd.EnterDisplayNamePrompt");
                                                   }
                                                },
                                               function (displayName){
                                                    if (self._collectionWithDisplayNameExists(collections, displayName)){
                                                       return _("Main.CollectionAdd.DisplayNameExistsPrompt", displayName);
                                                    }
                                                }
                                               ]);

                            displayNameDeferred.addCallback(function(displayName){
                                if (displayName == null) {
                                    return null;
                                }
        
                                var subscription = new cosmo.model.Subscription({
                                    displayName: displayName,
                                    uid: curr.getUid(),
                                    ticketKey: passedKey
                                })
        
                                return cosmo.app.pim.serv.createSubscription(subscription);
                            });
                            return displayNameDeferred;
                        });
                        return dList;
                };
                var redirectFunction = function () {
                    // Make sure the modal dialog is hidden
                    try {
                        cosmo.app.hideDialog()
                    } catch (e){
                        // don't do anything on error
                    }
                    cosmo.app.showDialog(
                        {"prompt":_("Main.CollectionAdd.RedirectPrompt")}
                    );
                    // Log the user into Cosmo and display the current collection
                    location = cosmo.env.getBaseUrl() + '/pim/collection/' + curr.getUid();
                }

                // Set up the authAction obj for the AuthBox
                // Passed to cosmo.ui.widget.AuthBox.getInitProperties
                var authAction = {
                    execInline: false,
                    authInitPrompt: self.strings.collectionAddAuthPrompt,
                    authProcessingPrompt: null, // Use the default
                    // Action to take after successful auth -- try to add the
                    // collection subscription
                    attemptFunc: function () {
                        // Hide the auth dialog, as we will be opening a new one
                        // to get the subscription name.
                        cosmo.app.hideDialog();
                        var deferred = subscribeFunction();

                        deferred.addCallback(redirectFunction);
                        deferred.addErrback(dojo.lang.hitch(this, function (err) {
                            cosmo.app.hideDialog();
                            cosmo.app.showErr(self.strings.collectionAddError, err.message);
                            return false;
                        }));
                    },
                    attemptPrompt: self.strings.attemptPrompt,
                    successPrompt: self.strings.successPrompt
                };

                // Called by clicking on the "Add to my account...: link
                var clickFunction = function () {
                    if (!cosmo.util.auth.currentlyAuthenticated()) {
                        var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties(authAction);
                        cosmo.app.showDialog(authBoxProps);
                        cosmo.app.modalDialog.content.usernameInput.focus();
                    }
                    else {
                        var deferred = subscribeFunction();
                        if (deferred == null) {
                            return;
                        }
                        deferred.addCallback(redirectFunction);
                        deferred.addErrback(function (err) {
                            cosmo.app.showErr(self.strings.collectionAddError, err);
                        });
                    }
                };


                // Collection name label
                // ---
                var displayName = curr? curr.getDisplayName() : "";
                var d = _createElem("div");
                d.id = 'collectionLabelName';
                d.className = 'labelTextHoriz';
                if (displayName.length > self.MAX_DISPLAY_NAME_LENGTH) {
                    var textNode = _createText(displayName.substr(0, self.MAX_DISPLAY_NAME_LENGTH - 1) + '\u2026');
                    d.title = curr.getDisplayName();
                }
                else {
                    var textNode = _createText(displayName);
                }
                d.appendChild(textNode);
                selectorNode.appendChild(d);
                self.displayNameText = textNode;

                // Set up addCollectionPromptNode
                // -----
                var addCollectionPromptNode = _createElem('div');
                addCollectionPromptNode.id = 'addCollectionPrompt';
                var anchor = _createElem('a');
                anchor.title = self.strings.collectionAddTooltip;
                dojo.event.connect(anchor, 'onclick', clickFunction);
                anchor.appendChild(_createText(self.strings.collectionAddPrompt));
                addCollectionPromptNode.appendChild(anchor);

                // Append to widget domNode
                self.domNode.appendChild(selectorNode);
                self.domNode.appendChild(addCollectionPromptNode);
            }

            // Logged-in view
            function renderSelector() {
                var imgPath = 'details';
                var imgTitle = self.strings.imgTitleInfo;

                var clickFunction = function () {
                    var _pim = cosmo.app.pim;
                    cosmo.app.showDialog(
                        cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                        _pim.currentCollection));
                };

                // Logged-in view uses a select box -- set vertical height
                // so we can get the info icon valigned properly
                selectorNode.style.height = self.verticalHeight + 'px';

                var options = self._createOptionsFromCollections(col);

                // The collection selector
                // ---
                var d = _createElem('div');
                d.className = 'floatLeft';
                var sel = cosmo.util.html.createSelect({ id: 'calSelectElem',
                    name: 'calSelectElem',
                    options: options, className: 'selectElem' }, d);
                sel.style.width = '120px';
                
                self.selector = sel;

                // Set the select to the current collection
                if (curr) self.setSelectorByDisplayName(curr.getDisplayName());
                dojo.event.connect(sel, "onchange", self, 'selectFunction');
                selectorNode.appendChild(d);

                // Spacer
                // ---
                var d = _createElem("div");
                d.className = 'floatLeft';
                d.appendChild(cosmo.util.html.nbsp());
                selectorNode.appendChild(d);

                var collIcon = cosmo.util.html.createRollOverMouseDownImage(
                    cosmo.env.getImageUrl( 'collection_' + imgPath + ".png"));
                collIcon.style.cursor = 'pointer';
                collIcon.alt = imgTitle;
                collIcon.title = imgTitle;
                dojo.event.connect(collIcon, 'onclick', clickFunction);
                // Img is an actual DOM element, so you set the vertical-align
                // prop on the image, not on the enclosing div
                collIcon.style.verticalAlign = 'middle';

                // Image
                var d = _createElem("div");
                d.className = 'floatLeft';
                var h = self.verticalHeight;
                d.style.height = h + 'px';
                d.style.linHeight = h + 'px';
                // Use margin prop to do this in IE -- CSS vertical align
                // is b0rken
                if (document.all) {
                    var m = parseInt((h - collIcon.height)/2);
                    d.style.marginTop = m + 'px';
                }
                d.style.verticalAlign = 'middle';
                d.appendChild(collIcon);
                selectorNode.appendChild(d);

                // Close left floats in selectorNode
                var d = _createElem("div");
                d.className = 'clearBoth';
                selectorNode.appendChild(d);

                // Append to widget domNode
                self.domNode.appendChild(selectorNode);
            }


            // Set up selectorNode
            // -----
            // The content is several left-floated
            // divs -- this allows for correct vertical alignment of
            // the icon images with the text they sit beside
            // Ticket view -- just show the collection name
            if (passedKey) {
                renderSingleCollectionName();
            }
            // Logged-in view -- show the selector, even if
            // only a single collection
            else {
                renderSelector();
            }
        },

        handleCollectionUpdated: function (/*cosmo.topics.CollectionUpdatedMessage*/ message) {
            var updatedCollection = message.collection;
            for (var x = 0; x < this.collections.length;x++) {
                var collection = this.collections[x];
                if (collection.getUid() == updatedCollection.getUid()) {
                    if (collection instanceof cosmo.model.Subscription) {
                        collection.setCollection(updatedCollection);
                    }
                else {
                        //we'll assume it's a "normal" collection
                        this.collections[x] = updatedCollection;
                    }
                }
            }

            if (this.currentCollection.getUid() == updatedCollection.getUid()) {
                this.currentCollection.collection = updatedCollection;
                if (this.currentCollection instanceof cosmo.model.Subscription) {
                    this.currentCollection.setCollection(updatedCollection);
                }
            else {
                    this.currentCollection = updatedCollection;
                }
            }
            this._redraw();
        },

        handleSubscriptionUpdated: function (/*cosmo.topics.SubscriptionUpdatedMessage*/ message) {
            var updatedSubscription = message.subscription;
            for (var x = 0; x < this.collections.length;x++) {
                var col = this.collections[x];
                if (col instanceof cosmo.model.Subscription
                     && col.getTicketKey() == updatedSubscription.getTicketKey()
                     && col.getUid() == updatedSubscription.getUid()) {

                     this.collections[x] = updatedSubscription;
                     break;
                }
            }

            if (this.currentCollection instanceof cosmo.model.Subscription
                     && this.currentCollection.getTicketKey() == updatedSubscription.getTicketKey()
                     && this.currentCollection.getUid() == updatedSubscription.getUid()) {
                this.currentCollection = updatedSubscription;
            }
            this._redraw();
        },
        
        updateCollectionSelectorOptions: function(collections, currentCollection){
            this.currentCollection = currentCollection;
            this.collections = collections;
            this._redraw();
        },
        
        setSelectorByDisplayName: function(displayName){
            var index = this._getIndexByDisplayName(this.collections, displayName);
            if (index == -1){
                return false;
            }
            this.setSelectorByIndex(index);
        },
        
        setSelectorByIndex: function(index){
            cosmo.util.html.setSelect(this._getSelect(), index);    
        }, 
        
        _getSelect: function(){ return this.selector;},
        
        _getIndexByDisplayName: function(collections, displayName){
            for (var x = 0; x < collections.length; x++){
                if (collections[x].getDisplayName() == displayName){
                    return x;
                }
            }
            return -1;
        },
        
        _createOptionsFromCollections: function(collections, /*String?*/ displayName){
                var options = [];
                for (var x = 0; x < collections.length; x++) {
                    var collection = collections[x];
                    var selected = displayName && collection.displayName == displayName ? true : false; 
                    options.push( { value: x, 
                                    text: collection.getDisplayName(),
                                    selected: selected} );
                }
                return options;
        },
        
        _redraw: function () {
            while (this.domNode.firstChild) {
                this.domNode.removeChild(this.domNode.firstChild);
            }
            this.fillInTemplate();
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

