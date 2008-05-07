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

dojo.provide("cosmo.data.ItemStore");
dojo.require("cosmo.model.Item");

dojo.declare("cosmo.data.ItemStore", null, {
    _capitalize: function(str){
        return str.charAt(0).toUpperCase() + str.slice(1);
    },
    getValue: function(    /* item */ item,
        /* attribute-name-string */ attribute,
        /* value? */ defaultValue){
        return item['get' + this._capitalize(attribute)]() || defaultValue;
    },
    getValues: function(/* item */ item,
        /* attribute-name-string */ attribute){
        var value = this.getValue(item, attribute, null);
        if (value)
            return [value];
        else
            return [];
    },
    getAttributes: function(/* item */ item){
        return item.__propertyNames;
    },
    hasAttribute: function(    /* item */ item,
        /* attribute-name-string */ attribute){
        if (this.getValue(item, attribute))
            return true;
        else return false;
    },
    containsValue: function(/* item */ item,
        /* attribute-name-string */ attribute,
        /* anything */ value){
        if (this.getValue(item, attribute) == value)
            return true;
        else return false;
    },
    isItem: function(/* anything */ something){
        return something instanceof cosmo.model.Item;
    },
    isItemLoaded: function(/* anything */ something){
        return true;
    },
    loadItem: function(/* object */ keywordArgs){
        keywordArgs.onItem.apply(scope, [keywordArgs.item]);
    },

    fetch: function(/* Object */ keywordArgs){
        throw new Error("fetch not implemented");
    },

    // convenience function, default fetch handler used or extended by subclasses
    handleFetch: function(/* list of items */ items, /* object */ keywordArgs){
        var scope = keywordArgs.scope || dojo.global;
        if (keywordArgs.onBegin)
            keywordArgs.onBegin.apply(scope, [items.length, keywordArgs]);
        if (keywordArgs.onItem){
            for (var i in items){
                keywordArgs.onItem.apply(scope, [items[i], keywordArgs]);
            }
        }
        if (keywordArgs.onComplete){
            keywordArgs.onComplete.apply(scope, [items, keywordArgs]);
        }
    },

    getFeatures: function(){
        // We haven't fully implemented Read
        return {
            'dojo.data.api.Identity': true
        };
    },

    close: function(/*dojo.data.api.Request || keywordArgs || null */ request){},

    getLabel: function(/* item */ item){
        return item.getDisplayName();
    },

    getLabelAttributes: function(/* item */ item){
        return "displayName";
    },

    // dojo.data.api.Identity
    getIdentity: function(item){
        return item.getUid();
    },

    getIdentityAttributes: function(item){
        return ['uid'];
    },

    fetchItemByIdentity: function(keywordArgs){
        console.log("fetch by id");
        var fetchArgs = {
            scope: keywordArgs.scope,
            onError: keywordArgs.onError};
        if (keywordArgs.onItem){
            fetchArgs.onItem = function(item){
                if (item.getUid() == keywordArgs.identity){
                    keywordArgs.onItem.apply(keywordArgs.scope, [item]);
                }
            }
        }
        return this.fetch(fetchArgs);
    },

    // Utility
    filterByQuery: function(itemList, query, ignoreCase){
        return dojo.filter(itemList, dojo.hitch(this, function(item){
            for (var attr in query){
                var value = this.getValue(item, attr);
                if (value && !value.match(
                    dojo.data.util.filter.patternToRegExp(query[attr], ignoreCase)))
                    return false;
            }
            return true;
        }));
    }

})
