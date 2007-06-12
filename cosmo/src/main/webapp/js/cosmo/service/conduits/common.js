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
        kwArgs = kwArgs || {};

        var deferred = this._transport.getCollections(kwArgs);

        this._addTranslation(deferred, "translateGetCollections", 
        {
            lazyLoader: this.generateLazyLoader(
                dojo.lang.hitch(this, 
                                function (collection){
                                    return this.getCollection(collection.getUid(), 
                                                              {sync: true}).results[0];
                                }
                )
                )
        } );

        return deferred;
    },
    
    generateLazyLoader: function (getFunction){
        return function(oldObject, propertyNames){
            var newObject = getFunction(oldObject);
            for (var i = 0; i < propertyNames.length; i++){
                var propertyName = propertyNames[i];
                var setterName = "set" + dojo.string.capitalize(propertyName);
                var getterName = "get" + dojo.string.capitalize(propertyName);
                oldObject[setterName](newObject[getterName]());
            }
        }
    },
    
    getSubscriptions: function (kwArgs){
        kwArgs = kwArgs || {};

        var deferred = this._transport.getSubscriptions(kwArgs);

        this._addTranslation(deferred, "translateGetSubscriptions");

        return deferred;
    },

    /*
     * returns: dojo.Deferred with callbac that returns XML Document object.
     */
    getCollection: function(collectionUid, kwArgs){
        kwArgs = kwArgs || {};

        var deferred = this._transport.getCollection(collectionUid, kwArgs);

        this._addTranslation(deferred, "translateGetCollection");
        
        //TODO: do topic notifications
        return deferred;
    },

    getItems: function (collection, searchCriteria, kwArgs){
        kwArgs = kwArgs || {};
        if (collection.getTicketKey){
            kwArgs.ticketKey = collection.getTicketKey();
        }
        var deferred = this._transport.getItems(collection, searchCriteria, kwArgs);
        
        this._addTranslation(deferred, "translateGetItems");

        // do topic notifications
        return deferred;
    },

    getItem: function(uid, kwArgs){
        kwArgs = kwArgs || {};

        var deferred = this._transport.getItem(uid, kwArgs);

        this._addTranslation(deferred, "translateGetItem");
        
        return deferred;
    },
    
    expandRecurringItem: function(item, start, end, kwArgs){
        kwArgs = kwArgs || {};
        var deferred = this._transport.expandRecurringItem(item, 
            {start: start, end: end}, kwArgs);

        this._addTranslation(deferred, "translateGetItems");
        
        return deferred;
    },

    saveItem: function(item, kwArgs){
        kwArgs = kwArgs || {};

        // do topic notifications
        var deferred = this._transport.saveItem(item, this._translator.itemToAtomEntry(item), kwArgs);
        var translationArgs = {};                                  
        if (item instanceof cosmo.model.NoteOccurrence) {
            translationArgs.masterItem = item.getMaster();
            translationArgs.oldObject = item;
        } else if (item instanceof cosmo.model.Note) {
            translationArgs.oldObject = item;
        }
        this._addTranslation(deferred, "translateSaveCreateItem", translationArgs);
        
        return deferred;

    },
    
    createItem: function(item, parentCollection, kwArgs){
        kwArgs = kwArgs || {};

        var deferred =  this._transport.createItem(item, this._translator.itemToAtomEntry(item),
                                          parentCollection, kwArgs);
        var translationArgs = {};                                  
        if (item instanceof cosmo.model.NoteOccurrence) {
            translationArgs.masterItem = item.getMaster();
            translationArgs.oldObject = item;
        } else if (item instanceof cosmo.model.Note) {
            translationArgs.oldObject = item;
        }
        this._addTranslation(deferred, "translateSaveCreateItem", translationArgs);
        return deferred;
        
    },

    deleteItem: function(item, kwArgs){
        kwArgs = kwArgs || {};

        return this._transport.deleteItem(item, kwArgs);
    },
    
    createSubscription: function(subscription, kwArgs){
        kwArgs = kwArgs || {};
        
        return this._transport.createSubscription(subscription, 
            this._translator.subscriptionToAtomEntry(subscription), 
            kwArgs);
            
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
    
    _addTranslation: function (deferred, translationFunction, kwArgs){
        deferred.addCallback(
            dojo.lang.hitch(this._translator, function (obj, xhr){
                return this[translationFunction](obj, kwArgs);
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


