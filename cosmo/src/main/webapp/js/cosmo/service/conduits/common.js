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

/**
 * Objects needed in all conduit implementations.
 * 
 * Specifically, this module contains abstract implementations
 * of Conduits. These Abstract implementations are responsible
 * for applying object translators (though they do not
 * specify _which_ translator to apply) and publishing
 * on appropriate topic channels.
 * 
 */
dojo.provide("cosmo.service.conduits.common")

cosmo.service.conduits.AbstractConduit = {
    getCollection: function(collectionUid, kwArgs){

        var d = this.doGetCollection(collectionUid, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        return d;
    },

    doGetCollection: function(collectionUid, kwArgs){
        dojo.unimplemented();
    },

    getItems: function(collection, startTime, endTime, kwArgs){
        var d = this.doGetItems(collection, startTime, endTime, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        return d;
    },

    doGetItems: function(collection, startTime, endTime, kwArgs){
        dojo.unimplemented();
    },

    saveItem: function(item, kwArgs){
        var d = this.doSaveItem(item, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        return d;
    },

    doSaveItem: function(item, kwArgs){
        dojo.unimplemented();
    },

    removeItem: function(collection, item, kwArgs){
        var d = this.doRemoveItem(collection, item, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        return d;
    },
    doRemoveItem: function(collection, item, kwArgs){
        dojo.unimplemented();
    },

    getRecurrenceRules: function(events, kwArgs){
        var d = this.doGetRecurrenceRules(events, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        return d;
        dojo.unimplemented();
    },


    saveRecurrenceRule: function(event, recurrenceRule, kwArgs){
        var d = this.doSaveRecurrenceRules(event, kwArgs);

        // add object translator to callback chain

        // do topic notifications
        dojo.unimplemented();
    },

    expandEvents: function(events, startTime, endTime, kwArgs){
        var d = this.doExpandEvents(events, startTime, endTime, kwArgs);

        // add object translator to callback chain

        // do topic notifications
    },
    doExpandEvents: function(events, startTime, endTime, kwArgs){
          dojo.unimplemeneted();
    },

    saveNewEventBreakRecurrence: function(event, originalEventUid,
        originalEventEndDate, kwArgs){
        var d = this.doSaveNewEventBreakRecurrence(event, originalEventUid,
            originalEventEndDate, kwArgs);

        // add object translator to callback chain

        // do topic notifications
    },

    doSaveNewEventBreakRecurrence: function(event, originalEventUid,
        originalEventEndDate, kwArgs){

        dojo.unimplemented();
    },
}

dojo.declare('cosmo.service.conduits.AbstractTicketedConduit', cosmo.service.conduits.AbstractConduit,
{
    initializer: function(ticket){
        this.ticket = ticket;
    },
    getTicketKey: function(){
        return ticket.ticketKey;
    },
    getTicketedKwArgs: function(kwArgs){
        var kwArgs = kwArgs || {};
        kwArgs.ticketKey = this.getTicketKey();
        return kwArgs;
    }
})

dojo.declare('cosmo.service.conduits.AbstractCurrentUserConduit', cosmo.service.conduits.AbstractConduit,
{
    getCollections: function(kwArgs){
        var d = this.doGetCollections(kwArgs);
        
        d.addCallback(
           dojo.lang.hitch(this, 
             function(result){
               return this._translator.convertObject(
                 result[0]);
           }));
        
        // do topic notifications
        return d;
    },
    
    doGetCollections: function(/*Hash*/ kwArgs){
         dojo.unimplemented();
    },
    
    getPreference: function(/*String*/ key, /*Hash*/ kwArgs){

        this.doGetPreference(key, kwArgs);
    },

    doGetPreference: function(/*String*/ key, /*Hash*/ kwArgs){
         dojo.unimplemented();
    },

    setPreference: function(/*String*/ key, /*String*/ value, 
                            /*Hash*/ kwArgs){
       this.doSetPreference(key, value, kwArgs);
    },

    doSetPreference: function(/*String*/ key, /*String*/ value, 
                              /*Hash*/ kwArgs){
        dojo.unimplemented();
    },

    removePreference: function(/*String*/ key, /*Hash*/ kwArgs){
        this.doRemovePreference(key, kwArgs);
    },

    doRemovePreference: function(/*String*/ key,  
                              /*Hash*/ kwArgs){
        dojo.unimplemented();
    },

    getPreferences: function(/*Hash*/ kwArgs){
        this.doGetPreferences(kwArgs)
    },

    doGetPreferences: function(/*Hash*/ kwArgs){
        dojo.unimplemented();
    },

    setPreferences: function(/*Hash<String, String>*/ preferences, /*Hash*/ kwArgs){
        this.doSetPreferences(preferences, kwArgs);
    },

    doSetPreferences: function(/*Hash<String, String>*/ preferences,
                              /*Hash*/ kwArgs){
        dojo.unimplemented();
    },

    setMultiplePreferences: function(/*Hash<String, String>*/ preferences, 
                                      /*Hash*/ kwArgs){
        this.doSetMultiplePreferences(preferences, kwArgs);
    },

    doSetMultiplePreferences: function(/*Hash<String, String>*/ preferences,
                              /*Hash*/ kwArgs){
        dojo.unimplemented();
    }
});




