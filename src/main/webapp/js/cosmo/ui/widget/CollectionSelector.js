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
dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");
dojo.require("cosmo.env");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");

dojo.provide("cosmo.ui.widget.CollectionSelector");

dojo.widget.defineWidget("cosmo.ui.widget.CollectionSelector", 
    dojo.widget.HtmlWidget, {
        templateString: '<span></span>',
        collections: [],
        /**
         * Inserts the select box for choosing from multiple calendars
         * Only actually called if multiple calendars exist
         */
        fillInTemplate: function () {
            var col = this.collections;
            var calSelectNav = this.domNode; 
            
            // More than one collection
            function renderSelector() {
                var o = [];
                for (var i in col) {
                    o.push( { value: i, text: col[i].displayName } );
                }
                cosmo.util.html.createSelect({ id: 'calSelectElem', name: 'calSelectElem',
                    options: o, className: 'selectElem' }, calSelectNav);
                
                // TODO replace this with an image when I get it from Priss
                var infoLinkSpan = document.createElement("span");
                var infoLink = document.createTextNode(" Info");
                infoLinkSpan.onclick = function () {
                        cosmo.app.showDialog(
                        cosmo.ui.widget.CollectionDetailsDialog.getInitProperties(
                        Cal.currentCollection.collection));
                };
                infoLinkSpan.appendChild(infoLink);
                calSelectNav.appendChild(infoLinkSpan);
            }
            
            // Single collection -- may have plus sign for subscribing
            function renderSingleCollectionName() {

            }
            
            // Multiple collections -- display selector
            if (col.length > 1) {
                renderSelector();
            }
            else {
                
            }
        }
} );

