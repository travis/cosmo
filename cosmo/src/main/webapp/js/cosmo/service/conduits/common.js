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

//TODO: remove once we move create/delete into Atom
dojo.require("cosmo.caldav");

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

        this._addTranslation(deferred, "translateGetCollections");

        // Flesh out each collection
        deferred.addCallback(dojo.lang.hitch(this, function(collections){
            var collectionDetailDeferreds = [];
            for (var i = 0; i < collections.length; i++){
                collectionDetailDeferreds.push(
                    this.getCollection(collections[i].href, kwArgs)
                );
            }
            return new dojo.DeferredList(collectionDetailDeferreds);
        }));

        deferred.addCallback(this._extractDeferredListResults);

        return deferred;
    },
    
    _extractDeferredListResults: function (results){
        var list = [];
        for (var i = 0; i < results.length; i++){
            if (results[i][0]){
                list.push(results[i][1]);
            } else {
                cosmo.app.showErr("", "", results[i][1]);
            }
        }
        return list;
    },

    getSubscriptions: function (kwArgs){
        kwArgs = kwArgs || {};

        var deferred = this._transport.getSubscriptions(kwArgs);

        this._addTranslation(deferred, "translateGetSubscriptions");

        // Flesh out each collection
        deferred.addCallback(dojo.lang.hitch(this, function(subscriptions){
            collectionDetailDeferreds = [];
            for (var i = 0; i < subscriptions.length; i++){
                //capturing scope
                (dojo.lang.hitch(this, function (){
                    var subscription = subscriptions[i];
                    var deferred = this.getCollection(subscription.getCollection().href, kwArgs);
                    deferred.addCallback(function(collection){
                        subscription.setCollection(collection);
                    });
                    collectionDetailDeferreds.push(deferred);
                }))();
            }
            var deferredList = new dojo.DeferredList(collectionDetailDeferreds);
            // Finally, return subscription list from last deferred
            deferredList.addCallback(function(){
                return subscriptions;
            });
            return deferredList;
        }));
        deferred.addErrback(function(e){cosmo.app.showErr("", "", e)});
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
        var transportFunc = "";
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

        var deferred = this._transport[transportFunc](item, {projection: "/dashboard/eim-json"}, kwArgs);
        this._addTranslation(deferred, "translateGetItems");
        
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

    setModbyUser: function (item){
        item.getModifiedBy().setUserId(cosmo.util.auth.getUsername() || "");
    },

    saveItem: function(item, kwArgs){
        kwArgs = kwArgs || {};

        item.getModifiedBy().setAction(cosmo.model.ACTION_EDITED);
        this.setModbyUser(item);

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
    
    saveThisAndFuture: function(oldOccurrence, newItem, kwArgs){
        kwArgs = kwArgs || {};
        newItem.getModifiedBy().setAction(cosmo.model.ACTION_CREATED);
        this.setModbyUser(newItem);
        oldOccurrence.getModifiedBy().setAction(cosmo.model.ACTION_EDITED);
        this.setModbyUser(oldOccurrence);
        var x = this._translator.itemToAtomEntry(newItem)
        var deferred = this._transport.saveThisAndFuture(oldOccurrence, 
            x, kwArgs);
            
        var translationArgs = {
            "oldObject": newItem
        };
        this._addTranslation(deferred, "translateSaveCreateItem", translationArgs);
        
        return deferred;
    },

    createItem: function(item, parentCollection, kwArgs){
        kwArgs = kwArgs || {};

        item.getModifiedBy().setAction(cosmo.model.ACTION_CREATED);
        this.setModbyUser(item);

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
        return this._transport.deleteItem(item, kwArgs);
    },

    removeItem: function(item, collection, kwArgs){
        kwArgs = kwArgs || {};

        return this._transport.removeItem(item, collection, kwArgs);
    },
    
    createSubscription: function(subscription, kwArgs){
        kwArgs = kwArgs || {};
        
        return this._transport.createSubscription(subscription, 
            this._translator.subscriptionToAtomEntry(subscription), 
            kwArgs);
    },

    deleteSubscription: function(subscription, kwArgs){
        return this._transport.deleteSubscription(subscription, kwArgs);
    },
    
    // This is hacky, TODO: point to Atom for 0.10
    createCollection: function (name, kwArgs){
        return this._transport.bind({
            method: cosmo.caldav.METHOD_MKCALENDAR,
            url: cosmo.env.getFullUrl("Dav") + 
                "/" + encodeURIComponent(cosmo.util.auth.getUsername()) + 
                "/" + encodeURIComponent(name)
        });
    },

    // Also hacky, TODO: point to Atom for 0.10
    deleteCollection: function (collection, kwArgs){
        var name = collection.getDisplayName();
        return this._transport.bind({
            method: cosmo.caldav.METHOD_DELETE,
            url: collection.getUrl("dav")
        });
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
    var urlCache = new cosmo.service.UrlCache();
    
    return new cosmo.service.conduits.Conduit(
        new cosmo.service.transport.Atom(urlCache),
        new cosmo.service.translators.Eim(urlCache)
    );
};


