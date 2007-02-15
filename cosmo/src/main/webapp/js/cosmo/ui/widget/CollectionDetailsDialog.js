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
dojo.require("cosmo.convenience");
dojo.require("cosmo.util.html");
dojo.require("cosmo.ui.widget.Button");
dojo.require("cosmo.ui.widget.ModalDialog");
dojo.require("cosmo.ui.widget.CollectionDetailsDialog");
dojo.require("cosmo.topics");

dojo.widget.defineWidget("cosmo.ui.widget.CollectionDetailsDialog", "html",

//init function.
dojo.widget.HtmlWidget, function(){

}
,
//Prototype Properties 
{
        //User supplied properties:
        calendar: /*CalendarMetadata*/ null,
        conduit: /*cosmo.conduits.Conduit*/ null,
        transportInfo: null,
        displayName: null, //the collection display nama
        displayedSelection: '', // The selection item to display if invoked from ticket view
        
        // Template stuff
        templatePath:dojo.uri.dojoUri(
            '../../cosmo/ui/widget/templates/CollectionDetailsDialog/CollectionDetailsDialog.html'),

        // Attach points
        table: null, //the table, which has pretty much all the content
        clientSelector: null, //Selector for different client apps
        clientInstructions: null, //Instructions for the selected client
        clientCollectionAddress: null, //The link for the selected client
        clientCollectionLink: null, //The anchor element with client collection address
        clientInstructionRows: null, //The instructions for the selected client
        protocolRows: null, //The rows for all the protocols
        collectionNameText: null, //Label with the calendar name
        collectionNameInputSpan: null, //span with textbox with the calendar name
        collectionNameInput: null, //Textbox with the calendar name
        linkSpan: null, //where to put the link image
        chandlerPlug: null, //span with info about downloading chandler
        
        //i18n
        strings: { 
            nameLabel: _("Main.CollectionDetails.NameLabel"),
            calendarLabel: _("Main.CollectionDetails.CalendarLabel"),
            selectYourClient: _("Main.CollectionDetails.SelectYourClient"),
            collectionAddress: _("Main.CollectionDetails.CollectionAddress"),
            instructions1: _("Main.CollectionDetails.Instructions.1"),
            instructions2: _("Main.CollectionDetails.Instructions.2"),
            close: _("Main.CollectionDetails.Close"),
            caldav:_("Main.CollectionDetails.caldav"),
            webcal:_("Main.CollectionDetails.webcal"),
            atom:_("Main.CollectionDetails.atom"),
            protocolInstructions:_("Main.CollectionDetails.protocolInstructions"),
            helpLink:_("Main.CollectionDetails.HelpUrl"),
            help:_("Main.CollectionDetails.Help"),
            clickHere:_("Main.CollectionDetails.ClickHere"),
            helpLink:_("Main.CollectionDetails.HelpLink"),
            chandlerPlug: _('Main.CollectionDetails.ChandlerPlug', 
                '<span style="font-variant:small-caps;"><a href="http://chandler.osafoundation.org/">', 
                '</a></span>')
        },
        
        clientsToProtocols: {
            Chandler: "dav",
            iCal: "webcal",
            FeedReader: "atom",
            Download: "webcal"
        },

        // Lifecycle functions
        fillInTemplate: function () {
           var options = cosmo.ui.widget.CollectionDetailsDialog.getClientOptions();
           cosmo.util.html.setSelectOptions(this.clientSelector, options);
           
           if (this.conduit != null && this.conduit.saveDisplayName){
               this.collectionNameInputSpan.style.display = "";
           } else {
               this.collectionNameText.style.display = "";
           }
           
           var linkImg = cosmo.util.html.createRollOverMouseDownImage(cosmo.env.getImagesUrl() + "link.png");
           var toolTip = _("Main.CollectionDetails.LinkImageToolTip", this.displayName);         
           linkImg.title = toolTip;
           linkImg.alt = toolTip;
           this.linkSpan.appendChild(linkImg);
           
           // Show the selection choice if passed from the selector in 
           // ticket view -- otherwise default to 'Chandler'
           var selectedIndex =0
           if (this.displayedSelection) {
               selectedIndex = cosmo.ui.widget.CollectionDetailsDialog.clientMappings[
                   this.displayedSelection];
           }
           this.clientSelector.selectedIndex = selectedIndex;
           
           // Chandler plug contains a URL path with quotes. The Dojo widget
           // template variable substitution 'helpfully' escapes these into
           // two quotes each
           this.chandlerPlug.innerHTML = '| ' + this.strings.chandlerPlug;

           this._handleClientChanged();
        },
        
        saveDisplayName: function(){
            this.conduit.saveDisplayName(this.calendar.uid, this._getDisplayName(), this.transportInfo);
            //TODO - This should not here. The publishing should happen at the service level, 
            //otherwise everyone who wants to user a service level method has to publish. Hard to do right
            //now with current RPC setup
            if (this.transportInfo instanceof cosmo.model.Subscription){
                 this.transportInfo.displayName = this._getDisplayName();
                 cosmo.topics.publish(cosmo.topics.SubscriptionUpdatedMessage,[this.transportInfo]);
            } else {
                 this.calendar.name = this._getDisplayName();
                 cosmo.topics.publish(cosmo.topics.CollectionUpdatedMessage,[this.calendar]);
            }
        },
        
        appendedToParent: function(parent){
            //this.table.style.height = parent.contentNode.offsetHeight + "px";
            var helpHeight = this.helpText.offsetHeight;
            var contentHeight = parent.contentNode.offsetHeight;
            var top = contentHeight - helpHeight;
            this.helpText.style.top = top + "px";
            this.helpText.style.left = "18px";
            this.helpText.style.visibility = "visible";
        },
        
        //handles when the user selects a client
        _handleClientChanged: function(){
            var client = this._getClientChoice();
            if (client == "Download"){
                this._setClientCollectionAddress(client);
                this._showClientInstructionsAndAddress(true, true);
                this._setClientInstructions(client);
                this._showProtocolRows(false);
            } else if (client == "Other"){
                this._showClientInstructionsAndAddress(false, false);
                this._showProtocolRows(true);
            } else {
                this._showClientInstructionsAndAddress(true, false);
                this._showProtocolRows(false);
                this._setClientInstructions(client);
                this._setClientCollectionAddress(client);
            }
            
            this._showChandlerPlug(client == "Chandler");
        },
                
        // Instance methods
        _showClientInstructionsAndAddress: function(show, showLink){
            var hideshow = show ? "" : "none"; 
            this.clientInstructionRows.style.display = hideshow;
            this.clientCollectionAddress.style.display = (showLink ? "none" : "") ;            
            this.clientCollectionLink.style.display = (!showLink ? "none" : "");            
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
            var x = 1;
            var d = document.createElement("div");
            while (true){
                var key = "Main.CollectionDetails.Instructions." + client + "." + x;
                if (cosmo.util.i18n.messageExists(key)){
                    var message = _(key);
                    if (x > 1){
                        d.appendChild(document.createElement("br"));
                    } 
                    d.appendChild(document.createTextNode(message));
                } else {
                    break; 
                }
                x++;
            }
            dojo.dom.replaceChildren(this.clientInstructions, d);
        }, 
        
        _setClientCollectionAddress: function(client){
            var url =  this.calendar.protocolUrls[this.clientsToProtocols[client]];
            this.clientCollectionAddress.value = url;
            this.clientCollectionLink.href = url;
        },
        
        _getDisplayName: function(){
            return this.collectionNameInput.value;
        },
        
        _showChandlerPlug: function(show){
            this.chandlerPlug.style.display = (show ? "" : "none");            
        }
 }
 );
 
 cosmo.ui.widget.CollectionDetailsDialog.getInitProperties = 
    function(/*CalendarMetadata*/ calendar, 
             /*string*/ displayName,
             /*cosmo.conduits.Conduit*/ conduit, 
             transportInfo, 
             /* string */ displayedSelection) {
    
    var dummyNode = document.createElement('span');
    var contentWidget = dojo.widget.createWidget("cosmo:CollectionDetailsDialog",
                    { calendar: calendar, 
                      displayName: displayName,
                      conduit: conduit, 
                      transportInfo: transportInfo,
                      displayedSelection: displayedSelection }, 
                 dummyNode, 'last');
    
    dummyNode.removeChild(contentWidget.domNode);  
    var btnsRight = [];

    var closeButton = dojo.widget.createWidget(
                    "cosmo:Button",
                    { text: _("Main.CollectionDetails.Close"),
                      width: "60px",
                      handleOnClick: cosmo.app.hideDialog,
                      small: true },
                      dummyNode, 'last');
                      
    btnsRight.push(closeButton);
    dummyNode.removeChild(closeButton.domNode);  

    if (conduit && conduit.saveDisplayName){
        var saveButton = dojo.widget.createWidget(
                        "cosmo:Button",
                        { text: _("Main.CollectionDetails.Save"),
                          width: "60px",
                          //TODO - Handle Errors properly!
                          handleOnClick: function(){
                                             contentWidget.saveDisplayName(); 
                                             cosmo.app.hideDialog();
                                         },
                          small: true },
                          dummyNode, 'last');
        btnsRight.push(saveButton);
        dummyNode.removeChild(saveButton.domNode);  
    }

    return {
        content: contentWidget,
        height: "300",
        width: "450",
        btnsRight: btnsRight
    };
 };

// Clients -- note: the order in which they appear here is the order in
// they will appear in the select box
cosmo.ui.widget.CollectionDetailsDialog.clients = 
    ["Chandler", "iCal", "FeedReader", "Download", "Other"];
// Reverse mappings -- set up when getClientOptions is called
cosmo.ui.widget.CollectionDetailsDialog.clientMappings = {}; 

cosmo.ui.widget.CollectionDetailsDialog.getClientOptions = function () {
    var options = [];
    var clients = cosmo.ui.widget.CollectionDetailsDialog.clients;
    for (var i = 0; i < clients.length; i++) {
        var c = clients[i];
        options.push({
            value: c,
            text: _("Main.CollectionDetails.Client." + c)
        });
        cosmo.ui.widget.CollectionDetailsDialog.clientMappings[c] = i;
    }
    return options;
}

 
