/*
 * Copyright 2008 Open Source Applications Foundation
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

dojo.provide("cosmo.data.CollectionStore");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.data.ItemStore");

dojo.declare("cosmo.data.CollectionStore", cosmo.data.ItemStore, {
    constructor: function(service){
        this._serv = service || cosmo.service.conduits.getAtomPlusEimConduit();
    },
    isItem: function(/* anything */ something){
        return something instanceof cosmo.model.Collection;
    },

    getFeatures: function(){
        return {'dojo.data.api.Read': true,
                'dojo.data.api.Identity': true
               };
    },

    fetch: function(/* Object */ keywordArgs){
        console.debug("fetch");
        var scope = keywordArgs.scope || dojo.global;
        var d = this._serv.getCollections();
        d.addCallback(dojo.hitch(this, function(collections){
            this.handleCollectionFetch(collections, keywordArgs);
            return collections;
        }));
        if (keywordArgs.onError){
            d.addErrback(function(e){
                keywordArgs.onError.apply(scope, [e]);
            });
        }
        return d;
    },

    handleCollectionFetch: function(collections, keywordArgs){
        if (keywordArgs.query)
            var ignoreCase = !!keywordArgs.queryOptions.ignoreCase;
        collections = this.filterByQuery(collections, keywordArgs.query, ignoreCase);
        this.handleFetch(collections, keywordArgs);
    },

    _newItems: {},

    newItem: function(properties){
        var c = new cosmo.model.Collection();
        this._newItems[c.getUid()] = c;
        return c;
    },

    _modifiedItems: {},

    setValue: function(item, attribute, value){
        item['set' + this._capitalize(attribute)](value);
        this._modifiedItems[item.getUid()] = item;
        return true;
    },

    _deletedItems: {},

    deleteItem: function(item){
        this._deletedItems[item.getUid()] = item;
        return true;
    },

    save: function(keywordArgs){
        var deferreds = [];
        for (var nid in this._newItems){
            deferred.push(this._serv.createCollection(this._newItems[nid]));
        }
        for (var mid in this._modifiedItems){
            deferred.push(this._serv.saveCollection(this._modifiedItems[mid]));
        }
        for (var did in this._deletedItems){
            deferred.push(this._serv.deleteCollection(this._deletedItems[did]));
        }
        var dl = new dojo.DeferredList(deferreds);
        var scope = keywordArgs.scope || dojo.global;
        if (keywordArgs.onComplete) dl.addCallback(dojo.hitch(scope, keywordArgs.onComplete));
        if (keywordArgs.onError) dl.addErrback(dojo.hitch(scope, keywordArgs.onError));
        return dl;
    }
});