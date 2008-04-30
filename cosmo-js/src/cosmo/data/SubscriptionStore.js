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

dojo.provide("cosmo.data.SubscriptionStore");
dojo.require("cosmo.model.Item");
dojo.require("cosmo.service.conduits.common");
dojo.require("cosmo.data.ItemStore");
dojo.requireLocalization("cosmo.data", "SubscriptionStore");

dojo.declare("cosmo.data.SubscriptionStore", cosmo.data.CollectionStore, {
    l10n: dojo.i18n.getLocalization("cosmo.data", "SubscriptionStore"),
    fetch: function(/* Object */ keywordArgs){
        console.debug("fetch");
        var scope = keywordArgs.scope || dojo.global;
        var d = this._serv.getSubscriptions();
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

    save: function(keywordArgs){
        keywordArgs = keywordArgs || {};
        var deferreds = [];
        for (var nid in this._newItems){
            deferreds.push(this._serv.createSubscription(this._newItems[nid]));
            delete this._newItems[nid];
        }
        for (var mid in this._modifiedItems){
            var item = this._modifiedItems[mid];
            var modifiedD = this._serv.saveCollection(item);
            modifiedD.addCallback(function(x){
                cosmo.topics.publish(cosmo.topics.SubscriptionUpdatedMessage, [item]);
                return x;
            });
            deferreds.push(modifiedD);
            delete this._modifiedItems[mid];
        }
        for (var did in this._deletedItems){
            deferreds.push(this._serv.deleteSubscription(this._deletedItems[did]));
            delete this._deletedItems[did];
        }
        var dl = new dojo.DeferredList(deferreds);
        var scope = keywordArgs.scope || dojo.global;
        if (keywordArgs.onComplete) dl.addCallback(dojo.hitch(scope, keywordArgs.onComplete));
        if (keywordArgs.onError) dl.addErrback(dojo.hitch(scope, keywordArgs.onError));
        return dl;
    }
});