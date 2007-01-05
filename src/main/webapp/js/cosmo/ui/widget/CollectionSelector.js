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

dojo.widget.defineWidget("cosmo.ui.widget.CollectionSelector", 
    dojo.widget.HtmlWidget, {
        templateString: '<span></span>',
        collections: [],
        currentCollection: {},
        ticketKey: '',
        /**
         * Inserts the select box for choosing from multiple collections
         * Only actually called if multiple collections exist
         */
        fillInTemplate: function () {
            var col = this.collections;
            var curr = this.currentCollection;
            var key = this.ticketKey;
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
                var sel = cosmo.util.html.createSelect({ id: 'calSelectElem', name: 'calSelectElem',
                    options: o, className: 'selectElem' }, collSelectNode);
                sel.style.width = '120px';
                // Set the select to the current collection
                cosmo.util.html.setSelect(sel, c);
                
            }
            function renderButton() {
                var str = '';
                var f = null;
                // If using a ticket, add the 'Add' button
                if (key) {
                    str = 'add';
                    var authAction = { 
                        async: true,
                        attemptFunc: function () {
                            var self = this;
                            Cal.serv.saveSubscription(function (a, b, c) { 
                                self._handleAuthActionResp.apply(self, [a, b, c]) }, 
                                curr.collection.uid, key, curr.displayName)
                        },
                        attemptPrompt: 'Adding this collection to your account ...', 
                        successFunc: function (whatIsThisParam, err, requestId) {
                            if (err) {
                                cosmo.app.hideDialog();
                                cosmo.app.showErr('Error: could not add the collection to your account.', err);
                                return false;
                            }
                            else {
                                var successProps =  cosmo.ui.widget.AuthBox.getSuccessProperties(
                                    'Successfully added this collection to your account.');
                                cosmo.app.hideDialog();
                                cosmo.app.showDialog(successProps);
                            }
                        } };
                    f = function () {
                        var authBoxProps = cosmo.ui.widget.AuthBox.getInitProperties(authAction);
                        cosmo.app.showDialog(authBoxProps);
                        cosmo.app.modalDialog.content.usernameInput.focus();
                    };
                }
                // Otherwise the user is logged in -- use the 'Info' button
                else {
                    str = 'info';
                    f = function () {
                        cosmo.app.showDialog(
                            cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                            Cal.currentCollection.collection,
                            Cal.currentCollection.displayName,
                            Cal.currentCollection.conduit,
                            Cal.currentCollection.transportInfo));
                    };
                }
                var btnSpan = _createElem("span");
                var btnLink = _createElem('a');
                var btnText = _createText('[' + str + ']');
                dojo.event.connect(btnLink, 'onclick', f);
                btnLink.href = '#';
                btnLink.appendChild(btnText);
                btnSpan.appendChild(_createText('\u00A0'));
                btnSpan.appendChild(btnLink);
                collSelectNode.appendChild(btnSpan);
            }
            function renderSingleCollectionName() {
                var d = _createElem('div');
                d.id = 'collectionLabelPrompt';
                d.appendChild(_createText('You are currently viewing:'));
                collSelectNode.appendChild(d);
                var s = _createElem('span');
                s.id = 'collectionLabelName';
                s.className = 'labelTextXL';
                s.appendChild(_createText(curr.displayName));
                collSelectNode.appendChild(s);
            }
            
            // Multiple collections -- display selector
            if (col.length > 1) {
                renderSelector();
            }
            else {
                renderSingleCollectionName();
            }
            renderButton();
        }
} );

