/* * Copyright 2006-2007 Open Source Applications Foundation *
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
 * summary:
 *      This module provides wrappers around dojo.io.bind to simplify using
 *      the Cosmo Management Protocol (CMP) from javascript.
 * description:
 *      For more information about CMP, please see:
 *      http://wiki.osafoundation.org/Projects/CosmoManagementProtocol
 *
 *      Most methods take handlerDicts identical to those required
 *      by dojo.io.bind.
 */

dojo.provide("cosmo.service.transport.Atom");

dojo.require("dojo.io.*");
dojo.require("dojo.string");
dojo.require("cosmo.env");
dojo.require("cosmo.util.auth");
dojo.require("cosmo.util.uri");
dojo.require("cosmo.service.transport.Rest");

dojo.declare("cosmo.service.transport.Atom", cosmo.service.transport.Rest,
    {
        
    EDIT_LINK: "atom-edit",
        
    generateUri: function(base, projection, queryHash){
        queryHash = queryHash || {};
        var queryIndex = base.indexOf("?");
        if (queryIndex > -1) {
            var queryString = base.substring(queryIndex);
            var params = cosmo.util.uri.parseQueryString(queryString);
            dojo.lang.mixin(queryHash, params);
        } else queryIndex = base.length;
        
        return base.substring(0, queryIndex) + projection + this.queryHashToString(queryHash);
    },

    createCollection: function(name){
        
    },

    getCollection: function(collectionUrl, kwArgs){
        kwArgs = kwArgs || {};

        var r = {};
        r.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/"+ collectionUrl , "/details", {});
        return this.bind(r, kwArgs);
    },

    getCollections: function(kwArgs){
        kwArgs = kwArgs || {};
        
        var r = {};
        r.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername();
        
        return this.bind(r, kwArgs);
    },
    
    getSubscriptions: function (kwArgs){
        kwArgs = kwArgs || {};
        
        var r = {};
        r.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername() + "/subscribed";

        return this.bind(r, kwArgs);
    },
    
    createSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};
        
        var r = {};
        r.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername() + "/subscribed";
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
             
        return this.bind(r, kwArgs);
    },
    
    getItemsProjections: {
        now: "dashboard-now",
        later: "dashboard-later",
        done: "dashboard-done"  
    },
    
    getNowItems: function(collection, kwArgs){
        return this.getItems(collection, {triage: "now"}, kwArgs);
    },
    
    getLaterItems: function(collection, kwArgs){
        return this.getItems(collection, {triage: "later"}, kwArgs);
    },
    
    getDoneItems: function(collection, kwArgs){
        return this.getItems(collection, {triage: "done"}, kwArgs);
    },
    
    getItems: function (collection, searchCrit, kwArgs){
        kwArgs = kwArgs || {};

        if (collection instanceof cosmo.model.Subscription){
            collection = collection.getCollection();
        }

        var query = this._generateSearchQuery(searchCrit);
        var editLink = collection.getUrls()[this.EDIT_LINK];
        
        var projection = (this.getItemsProjections[searchCrit.triage] || "full") + "/eim-json";
        var r = {};

        r.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/" + editLink, "/" + projection, query);

        return this.bind(r, kwArgs);
    },

    saveItem: function (item, postContent, kwArgs){
        kwArgs = kwArgs || {};
        
        var editLink = item.getUrls()[this.EDIT_LINK];

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/" + 
                editLink;
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "PUT";

        return this.bind(r, kwArgs);
    },
    
    getItem: function(uid, kwArgs){
        kwArgs = kwArgs || {};
        
        var query = this._generateSearchQuery(kwArgs);

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/item/" + 
                uid + "/full/eim-json" + this.queryHashToString(query);
        r.method = "GET";
        
        return this.bind(r, kwArgs);

    },
    
    expandRecurringItem: function(item, searchCrit, kwArgs){
        kwArgs = kwArgs || {};

        var query = this._generateSearchQuery(searchCrit);
        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/" + 
                item.getUrls()['expanded'] + this.queryHashToString(query);

        r.method = "GET";
        
        return this.bind(r, kwArgs);
    },
    
    createItem: function(item, postContent, collection, kwArgs){
        kwArgs = kwArgs || {};
        if (collection instanceof cosmo.model.Subscription){
            collection = collection.getCollection();
        }
        
        var editLink = collection.getUrls()[this.EDIT_LINK];

        var r = {};
        r.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/" + editLink, "");
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        return this.bind(r, kwArgs);
    },

    createSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/user/" + 
            cosmo.util.auth.getUsername() + "/subscribed";
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        return this.bind(r, kwArgs);
    },

    deleteItem: function(item, kwArgs){
        kwArgs = kwArgs || {};
        var editLink = item.getUrls()[this.EDIT_LINK];
        var r = {};
        r.url = cosmo.env.getBaseUrl() +
          "/atom/" + editLink;
        r.method = "DELETE";
        
        return this.bind(r, kwArgs);
    },

    removeItem: function(collection, item, kwArgs){

    },

    getPreference: function (key){
        return this._transport.getPreference();
    },

    setPreference: function (key, val, postContent, kwArgs){
        return this.bind(
            {
                url: cosmo.env.getBaseUrl() +
                     "/atom/user/" + cosmo.util.auth.getUsername() + "/preferences",
                postContent: postContent,
                method: "POST"
            },
            kwArgs);
    },

    removePreference: function (key){
        return this._transport.removePreference();
    },

    getPreferences: function (kwArgs){
        return this.bind(
            {url: cosmo.env.getBaseUrl() +
                  "/atom/user/" + cosmo.util.auth.getUsername() + "/preferences"
            }, 
            kwArgs);
    },
    
    setPreferences: function (prefs){
       return this._transport.getPreferences();
    },

    _generateSearchQuery: function(/*Object*/searchCrit){
        var ret = {};
        if (!searchCrit) return ret;
        if (searchCrit.start) {
            ret["start"] = dojo.date.toRfc3339(searchCrit.start);
        }
        if (searchCrit.end) {
            ret["end"] = dojo.date.toRfc3339(searchCrit.end);
        }
        return ret;
    }



    }
);

cosmo.service.transport.atom = new cosmo.service.transport.Atom();
