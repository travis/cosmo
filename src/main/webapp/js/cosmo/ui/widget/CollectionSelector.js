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
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.topics");

dojo.widget.defineWidget("cosmo.ui.widget.CollectionSelector", 
    dojo.widget.HtmlWidget,
    
    //initializer
    function () {
        dojo.event.topic.subscribe(cosmo.topics.CollectionUpdatedMessage.topicName, this, this.handleCollectionUpdated);
        dojo.event.topic.subscribe(cosmo.topics.SubscriptionUpdatedMessage.topicName, this, this.handleSubscriptionUpdated);
    },
    {
    
        templateString: '<span></span>',
        collections: [],
        currentCollection: {},
        selectFunction: null,
        ticketKey: '',
        
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
            var passedKey = this.ticketKey;
            var collSelectNode = this.domNode; 
            
            function $(s) {
                return document.getElementById(s);
            }
            function _createElem(s) {
                return document.createElement(s);
            }
            function _createText(s) {
                return document.createTextNode(s);
            }
            
            // Break into a couple of different functions depending
            // on ticket/account, and single/multiple collections
            // --------------------
            // More than one collection
            function renderSelector() {
                var o = [];
                var c = 0;
                for (var i in col) {
                    // Grab the currently selected collection's index
                    if (col[i].displayName == curr.displayName) {
                        c = i;
                    }
                    o.push( { value: i, text: col[i].displayName } );
                }
                var d = _createElem('div');
                d.className = 'floatLeft';
                var sel = cosmo.util.html.createSelect({ id: 'calSelectElem', name: 'calSelectElem',
                    options: o, className: 'selectElem' }, d);
                sel.style.width = '120px';
                // Set the select to the current collection
                cosmo.util.html.setSelect(sel, c);
                self.selector = sel;
                dojo.event.connect(sel, "onchange", function(){
                    self.selectFunction();
                    self.currentCollection = self.collections[sel.selectedIndex];
                });
                collSelectNode.appendChild(d);
                
            }
            function renderButton() {
                var imgPath = '';
                var f = null;
                var _ = cosmo.util.i18n.getText;
                // If using a ticket, add the 'Add' button
                if (passedKey) {
                    imgPath = 'subscribe';
                    imgTitle = _('Main.CollectionAdd.Tooltip');
                    // Set up the authAction obj for the AuthBox -- this tells it
                    // what to do if the user auths successfully
                    var authAction = { 
                        execInline: false,
                        authInitPrompt: 'Please enter the login information for your Cosmo account.',
                        authProcessingPrompt: null, // Use the default
                        // Action to take after successful auth -- try to add the
                        // collection subscription
                        attemptFunc: function () {
                            // Special Doug Henning section -- some closure to get the
                            // callback from the async response to exec in the AuthBox
                            // scope. Here I'm going back into the AuthBox to call
                            // the response handler, which just executes successFunc below
                            // I could just set up an anon function here to handle the
                            // response, but I think it's clearer having a success handler 
                            // specifically defined
                            var self = this; // Reference to the AuthBox
                            var n = function (a, b, c) {
                                self.handleAuthActionResp.apply(self, [a, b, c]) 
                            };
                            Cal.serv.saveSubscription(n, curr.collection.uid, passedKey, 
                                curr.displayName)
                        },
                        attemptPrompt: _('Main.CollectionAdd.AttemptPrompt'), 
                        successFunc: function (whatIsThisParam, err, requestId) {
                            var msg = this.authAction.successPrompt; // 'this' is the AuthBox
                            if (err) {
                                cosmo.app.hideDialog();
                                cosmo.app.showErr(_('Main.CollectionAdd.ErrorPrompt'), err);
                                return false;
                            }
                            else {
                                var successProps =  cosmo.ui.widget.AuthBox.getSuccessProperties(msg);
                                cosmo.app.hideDialog();
                                cosmo.app.showDialog(successProps);
                            }
                        },
                        successPrompt: _('Main.CollectionAdd.SuccessPrompt') };
                    f = function () {
                        var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties(authAction);
                        cosmo.app.showDialog(authBoxProps);
                        cosmo.app.modalDialog.content.usernameInput.focus();
                    };
                }
                // Otherwise the user is logged in -- use the 'Info' button
                else {
                    imgPath = 'details';
                    imgTitle = _('Main.CollectionDetails.Tooltip');
                    f = function () {
                        cosmo.app.showDialog(
                            cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                            Cal.currentCollection.collection,
                            Cal.currentCollection.displayName,
                            Cal.currentCollection.conduit,
                            Cal.currentCollection.transportInfo));
                    };
                }
                var d = null;
                var collectionIcon = cosmo.util.html.createRollOverMouseDownImage(
                    cosmo.env.getImagesUrl() + 'collection_' + imgPath + ".png");
                collectionIcon.style.cursor = 'pointer';
                collectionIcon.title = imgTitle;
                dojo.event.connect(collectionIcon, 'onclick', f);
                
                // Non-breaking space
                d = _createElem("div");
                d.className = 'floatLeft';
                d.appendChild(_createText('\u00A0'));
                collSelectNode.appendChild(d);
                // Image
                d = _createElem("div");
                d.className = 'floatLeft';
                d.appendChild(collectionIcon);
                collSelectNode.appendChild(d);
            }
            
            function renderSingleCollectionName() {
                var d = _createElem('div');
                d.id = 'collectionLabelPrompt';
                d.appendChild(_createText('You are currently viewing:'));
                collSelectNode.appendChild(d);
                d = _createElem("div");
                d.id = 'collectionLabelName';
                d.className = 'floatLeft labelTextXL';
                var textNode = _createText(curr.displayName)
                d.appendChild(textNode);
                collSelectNode.appendChild(d);
                self.displayNameText = textNode;
            }
            
            // Multiple collections -- display selector
            if (col.length > 1) {
                renderSelector();
            }
            else {
                renderSingleCollectionName();
            }
            renderButton();

            // Close the left float
            var d = _createElem("div");
            d.className = 'clearBoth';
            collSelectNode.appendChild(d);
        },
        
        handleCollectionUpdated: function(/*cosmo.topics.CollectionUpdatedMessage*/ message){
            var updatedCollection = message.collection;
            for (var x = 0; x < this.collections.length;x++){
                if (this.collections[x].collection.uid == updatedCollection.uid){
                    this.collections[x].collection = updatedCollection;
                    if (!this.collections[x].transportInfo){
                        this.collections[x].displayName = updatedCollection.name;
                    }
                    break;
                }
            }
            this._redraw();
        },
        
        handleSubscriptionUpdated: function(/*cosmo.topics.SubscriptionUpdatedMessage*/ message){
            var updatedSubscription = message.subscription;
            for (var x = 0; x < this.collections.length;x++){
                var col = this.collections[x];
                if (col.transportInfo && 
                    col.transportInfo instanceof cosmo.model.Subscription &&
                    col.transportInfo.calendar.uid == updatedSubscription.calendar.uid &&
                    col.transportInfo.ticketKey == updatedSubscription.ticketKey){
                    col.transportInfo = updatedSubscription;
                    col.displayName = updatedSubscription.displayName;
                    break;
                }
            }
            
            if (this.currentCollection.transportInfo 
                   && this.currentCollection.transportInfo.calendar.uid == updatedSubscription.calendar.uid
                   && this.currentCollection.transportInfo.ticketKey == updatedSubscription.ticketKey ){
                this.currentCollection.displayName = updatedSubscription.displayName;
                this.currentCollection.transportInfo = updatedSubscription;
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

