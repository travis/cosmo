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
            dojo.event.topic.publish('/calEvent', {
                action: 'loadCollection', opts: { collection: c }, data: {}
            });
        },

        strings: {
            mainCollectionPrompt: _('Main.Collection.Prompt'),
            imgTitleAdd: _('Main.CollectionAdd.Tooltip'),
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

            // The 'currently viewing' prompt above the
            // collection selector / label
            var promptNode = _createElem('div');
            promptNode.id = 'collectionLabelPrompt';

            // Collection selector / label and 'add'/'info' icons
            var selectorNode = _createElem('div');
            selectorNode.id = 'collectionSelectorOrLabel';
            selectorNode.style.height = this.verticalHeight + 'px';

            // 'Add' or 'info' icons, with attached actions
            function renderButton() {
                var strings = self.strings;
                var imgPath = '';
                var f = null;
                // If using a ticket, add the 'Add' button
                if (passedKey) {
                    imgPath = 'subscribe';
                    imgTitle = strings.imgTitleAdd;
                    // Set up the authAction obj for the AuthBox -- this tells it
                    // what to do if the user auths successfully
                    var authAction = {
                        execInline: false,
                        authInitPrompt: 'Please enter the login information for your Cosmo account.',
                        authProcessingPrompt: null, // Use the default
                        // Action to take after successful auth -- try to add the
                        // collection subscription
                        attemptFunc: function () {

                            var subscription = new cosmo.model.Subscription({
                                displayName: curr.getDisplayName(),
                                uid: curr.getUid(),
                                ticketKey: passedKey
                            })
                            var deferred = cosmo.app.pim.serv.createSubscription(subscription);
                            deferred.addCallback(dojo.lang.hitch(this, function(x,y,z){
                                dojo.debug(x)
                                dojo.debug(y)
                                dojo.debug(z)
                                // Log the user into Cosmo and display the current collection
                                this._showPrompt(this.authAction.successPrompt);
//                                location = cosmo.env.getBaseUrl() + '/pim/collection/' + curr.getUid();
                                
                            }));
                            deferred.addErrback(dojo.lang.hitch(this, function(err, y, z){
                                dojo.debug(err)
                                dojo.debug(y)
                                dojo.debug(z)
                                cosmo.app.hideDialog();
                                cosmo.app.showErr(self.strings.collectionAddError, err);
                                return false;
                            }));
                            
                        },
                        attemptPrompt: strings.attemptPrompt,
                        successPrompt: strings.successPrompt };
                    f = function () {
                        var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties(authAction);
                        cosmo.app.showDialog(authBoxProps);
                        cosmo.app.modalDialog.content.usernameInput.focus();
                    };
                }
                // Otherwise the user is logged in -- use the 'Info' button
                else {
                    imgPath = 'details';
                    imgTitle = self.strings.imgTitleInfo;
                    f = function () {
                        var _pim = cosmo.app.pim;
                        cosmo.app.showDialog(
                            cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                            _pim.currentCollection));
                    };
                }

                var collIcon = cosmo.util.html.createRollOverMouseDownImage(
                    cosmo.env.getImagesUrl() + 'collection_' + imgPath + ".png");
                collIcon.style.cursor = 'pointer';
                collIcon.alt = imgTitle;
                collIcon.title = imgTitle;
                dojo.event.connect(collIcon, 'onclick', f);
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
            }

            // Ticket view
            function renderSingleCollectionName() {
                // Add the 'add this collection button on the left
                // ---
                renderButton();

                // Spacer
                // ---
                var d = _createElem("div");
                d.className = 'floatLeft';
                d.appendChild(cosmo.util.html.nbsp());
                selectorNode.appendChild(d);

                // Collection name label
                // ---
                var displayName = curr.getDisplayName();
                var d = _createElem("div");
                d.id = 'collectionLabelName';
                d.className = 'floatLeft labelTextHoriz';
                if (displayName.length > 13) {
                    var textNode = _createText(displayName.substr(0, 12) + '\u2026');
                    d.title = curr.displayName;
                }
                else {
                    var textNode = _createText(displayName);
                }
                d.appendChild(textNode);
                d.style.height = self.verticalHeight + 'px';
                d.style.lineHeight = self.verticalHeight + 'px';
                d.style.verticalAlign = 'middle';
                // Shave off a couple of px in IE because its
                // valign middle for text is wonky
                if (document.all) {
                    d.style.marginTop = '-2px';
                }
                selectorNode.appendChild(d);
                self.displayNameText = textNode;
            }

            // Logged-in view
            function renderSelector() {
                var o = [];
                var c = 0;
                for (var i in col) {
                    // Grab the currently selected collection's index
                    if (col[i].getDisplayName() == curr.getDisplayName()) {
                        c = i;
                    }
                    o.push( { value: i, text: col[i].getDisplayName()} );
                }

                // The collection selector
                // ---
                var d = _createElem('div');
                d.className = 'floatLeft';
                var sel = cosmo.util.html.createSelect({ id: 'calSelectElem',
                    name: 'calSelectElem',
                    options: o, className: 'selectElem' }, d);
                sel.style.width = '120px';
                // Set the select to the current collection
                cosmo.util.html.setSelect(sel, c);
                self.selector = sel;
                dojo.event.connect(sel, "onchange", self, 'selectFunction');
                selectorNode.appendChild(d);

                // Spacer
                // ---
                var d = _createElem("div");
                d.className = 'floatLeft';
                d.appendChild(cosmo.util.html.nbsp());
                selectorNode.appendChild(d);

                // Add the 'collection info' button on the right
                // ---
                renderButton();
            }

            // Set up promptNode
            // -----
            promptNode.appendChild(_createText(this.strings.mainCollectionPrompt));

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
            // Close left floats in selectorNode
            var d = _createElem("div");
            d.className = 'clearBoth';
            selectorNode.appendChild(d);

            // Append to widget domNode
            this.domNode.appendChild(promptNode);
            this.domNode.appendChild(selectorNode);
        },

        handleCollectionUpdated: function(/*cosmo.topics.CollectionUpdatedMessage*/ message){
            var updatedCollection = message.collection;
            for (var x = 0; x < this.collections.length;x++){
                var collection = this.collections[x];
                if (collection.getUid() = updatedCollection.getUid()){
                    if (collection instanceof cosmo.model.Subscription){
                        collection.setCollection(updatedCollection);
                    } else {
                        //we'll assume it's a "normal" collection
                        this.collections[x] = updatedCollection;
                    }
                }                
            }

            if (this.currentCollection.getUid() == updatedCollection.getUid()){
                this.currentCollection.collection = updatedCollection;
                if (currentCollection instanceof cosmo.model.Subscription){
                    this.currentCollection.setCollection(updatedCollection);
                } else {
                    this.currentCollection = updatedCollection;
                }
            }
            this._redraw();
        },

        handleSubscriptionUpdated: function(/*cosmo.topics.SubscriptionUpdatedMessage*/ message){
            var updatedSubscription = message.subscription;
            for (var x = 0; x < this.collections.length;x++){
                var col = this.collections[x];
                if (col instanceof cosmo.model.Subscription
                     && col.getTicketKey() == updatedSubscription.getTicketKey()
                     && col.getUid() == updatedSubscription.getUid()){
                     
                     this.collections[x] = updatedSubscription;                      
                     break;
                }
            }

            if (this.currentCollection instanceof cosmo.model.Subscription
                     && this.currentCollection.getTicketKey() == updatedSubscription.getTicketKey()
                     && this.currentCollection.getUid() == updatedSubscription.getUid()){
                this.currentCollection = updatedSubscription;
            }
            this._redraw();
        } ,

        _redraw: function(){
            while (this.domNode.firstChild){
                this.domNode.removeChild(this.domNode.firstChild);
            }
            this.fillInTemplate();
        }
} );

