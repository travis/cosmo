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
        
        var deferred = new dojo.Deferred();

        var query = this._generateAuthQuery(kwArgs);
        
        kwArgs.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/"+ collectionUrl , "/details", 
          this.queryHashToString(query));

        var r = this.getDefaultRequest(deferred, kwArgs);
          
        dojo.io.bind(r);
        return deferred;    
    },

    getCollections: function(kwArgs){
        kwArgs = kwArgs || {};
        var deferred = new dojo.Deferred();
        
        kwArgs.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername();

        var r = this.getDefaultRequest(deferred, kwArgs);
        
        dojo.io.bind(r);
        return deferred;    
    },
    
    getSubscriptions: function (kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();
        
        kwArgs.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername() + "/subscribed";

        var r = this.getDefaultRequest(deferred, kwArgs);
        
        dojo.io.bind(r);
        return deferred;    
    },
    
    createSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();
        
        kwArgs.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername() + "/subscribed";
        
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
             
        dojo.io.bind(r);
        return deferred;     
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
        if (collection instanceof cosmo.model.Subscription){
            collection = collection.getCollection();
        }

        kwArgs = kwArgs || {};

        var deferred = new dojo.Deferred();

        var query = this._generateAuthQuery(kwArgs);
        dojo.lang.mixin(query, this._generateSearchQuery(searchCrit));
        var editLink = collection.getUrls()[this.EDIT_LINK];
        
        var projection = (this.getItemsProjections[searchCrit.triage] || "full") + "/eim-json";
        kwArgs.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/" + editLink, "/" + projection, query);

        var r = this.getDefaultRequest(deferred, kwArgs);

        dojo.io.bind(r);
        return deferred;
    },

    saveItem: function (item, postContent, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();

        var query = this._generateAuthQuery(kwArgs);
        var editLink = item.getUrls()[this.EDIT_LINK];
        kwArgs.url = cosmo.env.getBaseUrl() + "/atom/" + 
                editLink + this.queryHashToString(query);

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        r.headers['X-Http-Method-Override'] = "PUT";

        dojo.io.bind(r);
        return deferred;
    },
    
    getItem: function(uid, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();
        
        var query = this._generateAuthQuery(kwArgs);
        dojo.lang.mixin(query, this._generateSearchQuery(kwArgs));
        kwArgs.url = cosmo.env.getBaseUrl() + "/atom/item/" + 
                uid + "/full/eim-json" + this.queryHashToString(query);

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.method = "GET";
        
        dojo.io.bind(r);
        return deferred;
    },
    
    expandRecurringItem: function(item, searchCrit, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();

        var query = this._generateAuthQuery(kwArgs);
        dojo.lang.mixin(query, this._generateSearchQuery(searchCrit));
        kwArgs.url = cosmo.env.getBaseUrl() + "/atom/" + 
                item.getUrls()['expanded'] + this.queryHashToString(query);

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.method = "GET";
        
        dojo.io.bind(r);
        return deferred;
    },
    
    createItem: function(item, postContent, collection, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();
        var query = this._generateAuthQuery(kwArgs);
        var editLink = collection.getUrls()[this.EDIT_LINK];
        kwArgs.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/" + editLink, "", query);

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        dojo.io.bind(r);
        return deferred;
    },

    createSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();
        
        var query = this._generateAuthQuery(kwArgs);
        kwArgs.url = cosmo.env.getBaseUrl() + "/atom/user/" + 
            cosmo.util.auth.getUsername() + "/subscribed";

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        dojo.io.bind(r);
        return deferred;
    },

    deleteItem: function(item, kwArgs){
        kwArgs = kwArgs || {};
        
        var deferred = new dojo.Deferred();

        var query = this._generateAuthQuery(kwArgs);
        var editLink = item.getUrls()[this.EDIT_LINK];
        kwArgs.url = cosmo.env.getBaseUrl() +
          "/atom/" + editLink + 
          this.queryHashToString(query);

        var r = this.getDefaultRequest(deferred, kwArgs);
        r.method = "POST";
        r.headers['X-Http-Method-Override'] = "DELETE";
        
        dojo.io.bind(r);
        return deferred;
    },

    removeItem: function(collection, item, kwArgs){

    },

    _generateAuthQuery: function(/*Object*/kwArgs){
        if (kwArgs && kwArgs.ticketKey)
            return {ticket: kwArgs.ticketKey};
        else
            return {};
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
