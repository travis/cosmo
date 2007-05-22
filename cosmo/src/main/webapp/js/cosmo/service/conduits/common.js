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
dojo.provide("cosmo.service.conduits.common");

dojo.declare("cosmo.service.conduits.Conduit", null, {

    _transport: null,

    _translator: null,

    initializer: function (transport, translator){
        this._transport = transport;
        this._translator = translator;
    },

    getCollections: function (kwArgs){
        var deferred = this._transport.getCollections(kwArgs);

        this._addTranslation(deferred, "translateGetCollections");
        return deferred;
    },
    
    getSubscriptions: function (kwArgs){

        var deferred = this._transport.getSubscriptions(kwArgs);
        return deferred;
    },

    /*
     * returns: dojo.Deferred with callbac that returns XML Document object.
     */
    getCollection: function(collectionUid, kwArgs){

        var deferred = this._transport.getCollection(collectionUid, kwArgs);

        this._addTranslation(deferred, "translateGetCollection");
        
        //TODO: do topic notifications
        return deferred;
    },

    getItems: function (collection, searchCriteria, kwArgs){
        if (!kwArgs) kwArgs = {};
        kwArgs.ticketKey = collection.getTicketKey() || undefined;
        var deferred = this._transport.getItems(collection.getUid(), searchCriteria, kwArgs);
        
        this._addTranslation(deferred, "translateGetItems");

        // do topic notifications
        return deferred;
    },

    getItem: function(uid, kwArgs){
        if (!kwArgs)   kwArgs = {};
        var deferred = this._transport.getItem(uid, kwArgs);

        this._addTranslation(deferred, "translateGetItems");
        
        return deferred;
    },

    saveItem: function(item, kwArgs){

        // do topic notifications
        return this._transport.saveItem(item, this._translator.itemToAtomEntry(item), kwArgs);

    },
    
    createItem: function(item, parentCollection, kwArgs){
        return this._transport.createItem(item, this._translator.itemToAtomEntry(item), 
                                          parentCollection.getUid(), kwArgs);
        
    },

    removeItem: function(collection, item, kwArgs){

        // add object translator to callback chain

        // do topic notifications
        return deferred;
    },
    
    getPreference: function (key){

    },

    setPreference: function (key, val){

    },

    removePreference: function (key){

    },
    
    getPreferences: function (){
       return {};
    },

    setPreferences: function (prefs){

    },

    setMultiplePreferences: function (prefs){

    },
    
    //TODO - actually implement this once BCM give us what we need
    getProtocolUrls:function(collectionUid, kwargs){
        var fakeDeferred = {};
        fakeDeferred.results = [{
            mc: "http://mc",
            atom: "http://atom",
            webcal: "http://webcal",
            dav: "http://dav",
            pim: "http://pim"
        },null];
        
        return fakeDeferred;
    },
    
    _addTranslation: function (deferred, translationFunction){
        deferred.addCallback(
            dojo.lang.hitch(this._translator, function (obj, xhr){
                return this[translationFunction](obj);
            })
        );
        
        deferred.addErrback(function (e, xhr){
            dojo.debug("Translation error:")
            dojo.debug(e);
            return e;
        });
        
    }
    
});

cosmo.service.conduits.getAtomPlusEimConduit = function (){
    dojo.require("cosmo.service.translators.eim");
    dojo.require("cosmo.service.transport.Atom");

    return new cosmo.service.conduits.Conduit(
        new cosmo.service.transport.Atom(),
        cosmo.service.translators.eim
    );
};


