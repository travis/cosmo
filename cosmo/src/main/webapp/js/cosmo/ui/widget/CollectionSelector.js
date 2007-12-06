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
    dojo.widget.HtmlWidget, function () {},
    {
        templateString: '<span></span>',
        verticalHeight: 18,
        collection: null,
        ticketKey: '',
        MAX_DISPLAY_NAME_LENGTH: 18,
        _origSelectIndex: 0,

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
        displayNameText: null, 

        fillInTemplate: function () {
            var self = this;
            var collection = cosmo.app.pim.getSelectedCollection();
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
                            var alreadySubscribed = self._collectionWithUidExists(collections, collection.getUid());
                            if (alreadySubscribed) {
                                var message = alreadySubscribed == "cosmo.model.Collection"
                                    ? _("Main.CollectionAdd.AlreadySubscribedOwnCollection")
                                    : _("Main.CollectionAdd.AlreadySubscribedToSubscription");
                                return cosmo.app.showAndWait(message, null);
                            }

                            var displayName = "";

                            var displayNameDeferred = cosmo.app.getValue(_("Main.CollectionAdd.EnterDisplayNamePrompt"),
                                               collection.getDisplayName(), [
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
                                    uid: collection.getUid(),
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
                    location = cosmo.env.getBaseUrl() + '/pim/collection/' + collection.getUid();
                }

                // Called by clicking on the "Add to my account...: link
                var clickFunction = function () {
                    if (!cosmo.util.auth.currentlyAuthenticated()) {
                        var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties({
                            authInitPrompt: self.strings.collectionAddAuthPrompt,
                            authProcessingPrompt: null, // Use the default
                            attemptPrompt: self.strings.attemptPrompt,
                            successPrompt: self.strings.successPrompt,
                            subscription: new cosmo.model.Subscription({
                                displayName: collection.getDisplayName(),
                                uid: collection.getUid(),
                                ticketKey: passedKey
                            })
                        });
                        // If authDeferred callback fires, user is authenticated
                        // If authDeferred errback fires, there was an error authenticating
                        var authDeferred = cosmo.app.showDialog(authBoxProps);
                        cosmo.app.modalDialog.content.usernameInput.focus();
                        
                        authDeferred.addCallback(function () {
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
                        });
                    } else {
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
                var displayName = collection ? collection.getDisplayName() : "";
                var d = _createElem("div");
                d.id = 'collectionLabelName';
                d.className = 'labelTextHoriz';
                if (displayName.length > self.MAX_DISPLAY_NAME_LENGTH) {
                    var textNode = _createText(displayName.substr(0, self.MAX_DISPLAY_NAME_LENGTH - 1) + '\u2026');
                    d.title = collection.getDisplayName();
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

            // The content is several left-floated
            // divs -- this allows for correct vertical alignment of
            // the icon images with the text they sit beside
            // -----
            // Show the collection name
            renderSingleCollectionName();
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

