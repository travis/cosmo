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

dojo.declare("cosmo.service.conduits.Conduit", null, {

    _transport: null,

    _translator: null,

    initializer: function (transport, translator){
        this._transport = transport;
        this._translator = translator;
        this.translateResponse = dojo.lang.hitch(this, function (obj, xhr){
            return this._translator.responseToObject(obj);
        });

    },
    
    resetServiceAccessTime: function(){
      //TODO  
    },
    
    getCollection: function(collectionUid, kwArgs){

        var d = this._transport.getCollection(collectionUid, kwArgs);

        d.addCallback(this.translateResponse);

        //TODO: do topic notifications
        return d;
    },

    getItems: function(collection, startTime, endTime, kwArgs){
        var d = this._transport.getItems(collection, startTime, endTime, kwArgs);

        d.addCallback(this.translateResponse);

        // do topic notifications
        return d;
    },

    saveItem: function(item, kwArgs){

        // add object translator to callback chain

        // do topic notifications
        return d;
    },

    removeItem: function(collection, item, kwArgs){

        // add object translator to callback chain

        // do topic notifications
        return d;
    }
});

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

cosmo.service.conduits.getAtomPlusEimConduit = function getAtomPlusEimConduit(){
    dojo.require("cosmo.service.translators.eim");
    dojo.require("cosmo.service.transport.Atom");

    return new cosmo.service.conduits.Conduit(
        new cosmo.service.transport.Atom(),
        cosmo.service.translators.eim
    );
}


