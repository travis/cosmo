/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.service.conduits.jsonrpc");

dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.rpc.JsonService");
dojo.require("cosmo.service.translators.jsonrpc");

cosmo.service.conduits.jsonrpc.eventListToUids = function(events){
    var eventUids = []
    dojo.lang.map(events, function(e){eventUids.push(e.uid)});
    return eventUids;
}

dojo.declare('cosmo.service.conduits.jsonrpc.TicketedConduit', 
    cosmo.service.conduits.AbstractTicketedConduit,
{
    initializer: function(jsonService){
       this.jsonService = jsonService? jsonService : cosmo.rpc.JsonService.getDefaultService();
    },    
    
    jsonService: null,

	_translator: cosmo.service.translators.jsonrpc,
    
    eventListToUids: cosmo.service.conduits.jsonrpc.eventListToUids,

    doGetCollection: function(collectionUid){

        this.jsonService.getCollection(collectionUid,
            this.getTicketedKwArgs(kwArgs));
    },
    doGetItems: function(collection, startTime, endTime){

        this.jsonService.getItems(collection.uid, startTime, endTime,
            this.getTicketedKwArgs(kwArgs))
    },
    doSaveItem: function(item){

        this.jsonService.saveItem(item,
            this.getTicketedKwArgs(kwArgs));
    },
    doDeleteItem: function(){

        this.jsonService.deleteItem(item,
            this.getTicketedKwArgs(kwArgs));
    },
    doRemoveItem: function(collection, item, kwArgs){

        this.jsonService.removeItem(collection.uid, item.uid,
            this.getTicketedKwArgs(kwArgs));
    },

    doGetRecurrenceRules: function(events, kwArgs){

        this.jsonService.getRecurrenceRules(eventUids,
            this.getTicketedKwArgs(kwArgs));
    },

    doSaveRecurrenceRules: function(event, recurrenceRule, kwArgs){

        this.jsonService.saveRecurrenceRule(event.uid, recurrenceRule,
            this.getTicketedKwArgs(kwArgs));
    },

    doExpandEvents: function(events, startTime, endTime, kwArgs){

        this.jsonService.expandEvents(this.eventListToUids(events),
            startTime, endTime, this.getTicketedKwArgs(kwArgs));
    },
    doSaveNewEventBreakRecurrence: function(event, originalEventUid,
        originalEventEndDate, kwArgs){

        this.jsonService.saveNewEventBreakRecurrence(event, originalEventUid,
                 originalEventEndDate, this.getTicketedKwArgs(kwArgs));
    }

});

dojo.declare("cosmo.service.conduits.jsonrpc.CurrentUserConduit",
             cosmo.service.conduits.AbstractCurrentUserConduit,
{
    initializer: function(jsonService){
       this.jsonService = jsonService? jsonService : cosmo.rpc.JsonService.getDefaultService();
    },    

    jsonService: null,
    
	_translator: cosmo.service.translators.jsonrpc,
    
    eventListToUids: cosmo.service.conduits.jsonrpc.eventListToUids,
    
    doGetCollections: function(kwArgs){
         return this.jsonService.getCalendars(kwArgs);
    },

    doGetCollection: function(collectionUid){

        this.jsonService.getCollection(collectionUid);
    },

    doGetItems: function(collection, startTime, endTime){
        this.jsonService.getItems(collection.uid, startTime, endTime)
    },

    doSaveItem: function(item){
        this.jsonService.saveItem(item);
    },

    doDeleteItem: function(){
        this.jsonService.deleteItem(item);
    },

    doRemoveItem: function(collection, item){
        this.jsonService.removeItem(collection.uid, item.uid);
    },

    doGetRecurrenceRules: function(events){

        this.jsonService.getRecurrenceRules(
            this.eventListToUids(events));
    },

    doSaveRecurrenceRules: function(event, recurrenceRule){
        this.jsonService.saveRecurrenceRule(event.uid, recurrenceRule);
    },

    doExpandEvents: function(events, startTime, endTime){

        this.jsonService.expandEvents(this.eventListToUids(events),
            startTime, endTime);
    },

    doSaveNewEventBreakRecurrence: function(event, originalEventUid, originalEventEndDate){
        this.jsonService.saveNewEventBreakRecurrence(event, originalEventUid,
                 originalEventEndDate);
    }
});