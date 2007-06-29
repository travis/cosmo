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
            lazyLoader:  this.generateLazyLoader(
                dojo.lang.hitch(this, 
                                function (collection){
                                    return this.getCollection(collection.href, 
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

        this._addTranslation(deferred, "translateGetSubscriptions", 
        {
            lazyLoader:  this.generateLazyLoader(
                dojo.lang.hitch(this, 
                                function (collection){
                                    return this.getCollection(collection.href, 
                                                              {sync: true, noAuth: true}).results[0];
                                }
                )
                )
        });

        return deferred;
    },

    /*
     * returns: dojo.Deferred with callback that returns XML Document object.
     */
    getCollection: function(url, kwArgs){
        kwArgs = kwArgs || {};

        var deferred = this._transport.getCollection(url, kwArgs);
        this._addTranslation(deferred, "translateGetCollection");
        //TODO: do topic notifications
        return deferred;
    },
    
    saveCollection: function(collection, kwArgs){
       if (collection instanceof cosmo.model.Subscription){
           return this._transport.saveSubscription(collection, this._translator.subscriptionToAtomEntry(collection), kwArgs);
       } else {
           return this._transport.saveCollection(collection, this._translator.collectionToSaveRepresentation(collection), kwArgs);
       }
    },
    
    getDashboardItems: function(item, kwArgs){
        var transportFun = "";
        if (item instanceof cosmo.model.Collection || 
            item instanceof cosmo.model.Subscription){
            
            transportFunc = "getItems";
        } else if (item instanceof cosmo.model.Note
                   && !!item.getEventStamp()
                   && !!item.getEventStamp().getRrule()){
            transportFunc = "expandRecurringItem";
        } else {
            throw new Error("Can not get dashboard items for " + item);
        }
        
        var entries = [];
        var addEntriesCallback = function (partialEntries){
            for (var i = 0; i < partialEntries.length; i++){
                entries.push(partialEntries[i]);
            }
        }
        var deferred = new dojo.Deferred();
        
        var dNow = this._transport[transportFunc](item, {triage: "now"}, kwArgs)
        this._addTranslation(dNow, "translateGetDashboardItems");
        dNow.addCallback(addEntriesCallback);
        deferred.addCallback(dojo.lang.hitch(this, function () {
            return dNow;
        }));
        
        var dLater = this._transport[transportFunc](item, {triage: "later"}, kwArgs)
        this._addTranslation(dLater, "translateGetDashboardItems");
        dLater.addCallback(addEntriesCallback);
        deferred.addCallback(dojo.lang.hitch(this, function () {
            return dLater;
        }));
        
        var dDone = this._transport[transportFunc](item, {triage: "done"}, kwArgs)
        this._addTranslation(dDone, "translateGetDashboardItems");
        dDone.addCallback(addEntriesCallback);
        deferred.addCallback(dojo.lang.hitch(this, function () {
            return dDone;
        }));
        
        deferred.addCallback(dojo.lang.hitch(this, function(){
            return this._translator.entriesToItems(entries);
        }));

        deferred.addErrback(function (e, xhr){
            dojo.debug("Translation error:")
            dojo.debug(e);
            return e;
        });
        
        deferred.callback();
        return deferred;
    },

    getItems: function (collection, searchCriteria, kwArgs){
        kwArgs = kwArgs || {};
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
    
    getPreference: function (key, kwArgs){
       var deferred = this._transport.getPreference(key, kwArgs);
       this._addTranslation(deferred, "translateGetPreference");
       return deferred;
    },

    setPreference: function (key, val, kwArgs){
       return this._transport.setPreference(key, val, this._translator.keyValToPreference(key, val), kwArgs);
    },

    getPreferences: function (kwArgs){
       var deferred = this._transport.getPreferences(kwArgs);
       this._addTranslation(deferred, "translateGetPreferences");
       return deferred;
    },
    
    deletePreference: function(key, kwArgs){
       return this._transport.deletePreference(key, kwArgs);
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


