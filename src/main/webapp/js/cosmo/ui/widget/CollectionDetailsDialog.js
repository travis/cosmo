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

dojo.provide("cosmo.ui.widget.CollectionDetailsDialog");

dojo.require("dojo.widget.*");
dojo.require("dojo.html.common");

dojo.require("cosmo.env");
dojo.require("cosmo.app");
dojo.require("cosmo.util.i18n");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.ui.widget.ModalDialog");

dojo.widget.defineWidget("cosmo.ui.widget.CollectionDetailsDialog", "html",

//init function.
dojo.widget.HtmlWidget, function(){

}
,
//Prototype Properties 
{
        //User supplied properties:
        calendar: /*CalendarMetadata*/ null,
        
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/CollectionDetailsDialog/CollectionDetailsDialog.html'),

        // Attach points
        clientSelector: null, //Selector for different client apps
        clientInstructions: null, //Instructions for the selected client
        clientCollectionAddress: null, //The link for the selected client
        clientInstructionRows: null, //The instructions for the selected client
        protocolRows: null, //The rows for all the protocols
        
        //i18n
        strings: { 
            nameLabel: getText("Main.CollectionDetails.NameLabel"),
            calendarLabel: getText("Main.CollectionDetails.CalendarLabel"),
            selectYourClient: getText("Main.CollectionDetails.SelectYourClient"),
            collectionAddress: getText("Main.CollectionDetails.CollectionAddress"),
            instructions1: getText("Main.CollectionDetails.Instructions.1"),
            instructions2: getText("Main.CollectionDetails.Instructions.2"),
            close: getText("Main.CollectionDetails.Close"),
            caldav:getText("Main.CollectionDetails.caldav"),
            webcal:getText("Main.CollectionDetails.webcal"),
            atom:getText("Main.CollectionDetails.atom"),
            protocolInstructions:getText("Main.CollectionDetails.protocolInstructions")
        },
        
        //clients - note: the order in which they appear here is the order in
        //                they will appear in the select box
        clients: ["Chandler", "iCal", "FeedReader"],
        
        clientsToProtocols: {
            Chandler: "mc",
            iCal: "webcal",
            FeedReader: "atom"
        },

        // Lifecycle functions
        fillInTemplate: function(){
            //set up the select box
            var options = [];
            dojo.lang.map(this.clients,function(client){
                options.push({
                    value:client,
                    text: getText("Main.CollectionDetails.Client." + client)
                })
            }); 
            options.push({text: getText("Main.CollectionDetails.Download"),
                          value: "download"});
                          
            options.push({text: getText("Main.CollectionDetails.Other"),
                          value: "other"});
          
           cosmo.util.html.setSelectOptions(this.clientSelector, options);
           this.clientSelector.selectedIndex = 0;
           this._handleClientChanged();
        },
        
        //handles when the user selects a client
        _handleClientChanged: function(){
            var client = this._getClientChoice();
            if (client =="download"){
            } else if (client =="other"){
                this._showClientInstructionsAndAddress(false);
                this._showProtocolRows(true);
            } else {
                this._showClientInstructionsAndAddress(true);
                this._showProtocolRows(false);
                this._setClientInstructions(client);
                this._setClientCollectionAddress(client);
            }
        },
                
        // Instance methods
        _showClientInstructionsAndAddress: function(show){
            var hideshow = show ? "" : "none"; 
            this.clientInstructionRows.style.display = hideshow;
        }, 

        _showProtocolRows: function(show){
            var hideshow = show ? "" : "none"; 
            this.protocolRows.style.display = hideshow;
        }, 
        
        _getClientChoice: function(){
            var selectedIndex = this.clientSelector.selectedIndex;
            return this.clientSelector.options[selectedIndex].value;
        },
        
        _setClientInstructions: function(client){
            dojo.dom.replaceChildren(this.clientInstructions, document.createTextNode(getText("Main.CollectionDetails.Instructions." + client)));
        }, 
        
        _setClientCollectionAddress: function(client){
            var url =  this.calendar.protocolUrls[this.clientsToProtocols[client]];
            this.clientCollectionAddress.value = url;
        }
        
 }
 );
 
 cosmo.ui.widget.CollectionDetailsDialog.getInitProperties = function(/*CalendarMetadata*/ calendar){
    return {
        content: dojo.widget.createWidget("cosmo:CollectionDetailsDialog", {calendar: calendar}, null, 'last'),
        width: "450",
        btnsRight: [dojo.widget.createWidget(
                    "cosmo:Button",
                    { text: getText("Main.CollectionDetails.Close"),
                      width: "50px",
                      handleOnClick: cosmo.app.hideDialog,
                      small: false },
                      null, 'last')]
    };
 };