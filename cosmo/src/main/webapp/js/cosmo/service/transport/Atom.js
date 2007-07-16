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
dojo.require("cosmo.service.exception");

dojo.declare("cosmo.service.transport.Atom", cosmo.service.transport.Rest,
    {
        
    EDIT_LINK: "atom-edit",
    PROJECTION_FULL_EIM_JSON: "/full/eim-json",
        
    generateUri: function(base, projection, queryHash){
        queryHash = queryHash || {};
        var queryIndex = base.indexOf("?");
        if (queryIndex > -1) {
            var queryString = base.substring(queryIndex);
            var params = cosmo.util.uri.parseQueryString(queryString);
            dojo.lang.mixin(queryHash, params);
        } else {
            queryIndex = base.length;
        }
        
        return base.substring(0, queryIndex) + projection + this.queryHashToString(queryHash);
    },

    createCollection: function(name){
        dojo.unimplemented("cosmo.service.transport.Atom.createCollection");
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
          "/atom/user/" + cosmo.util.auth.getUsername() + "/subscriptions";

        return this.bind(r, kwArgs);
    },
    
    getItemsProjections: {
        now: "dashboard-now",
        later: "dashboard-later",
        done: "dashboard-done"  
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
        r.url = this.generateUri(cosmo.env.getBaseUrl() + "/atom/" + 
                editLink, this.PROJECTION_FULL_EIM_JSON);
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "PUT";
        var deferred = this.bind(r, kwArgs);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
 
        return deferred;
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

        var projection = (this.getItemsProjections[searchCrit.triage] || "full") + "/eim-json";
        var r = {};
        
        var expandedLink = item.getUrls()['expanded'];
        
        var defaultProjection = "/full/eim-json";
        var projectionIndex = expandedLink.indexOf(defaultProjection);
        if (projectionIndex > 0) {
            expandedLink = expandedLink.substring(0, projectionIndex) + 
                           expandedLink.substring(projectionIndex  + defaultProjection.length);
        }

        r.url = this.generateUri(cosmo.env.getBaseUrl() +
          "/atom/" + expandedLink, "/" + projection, query);

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
          "/atom/" + editLink, this.PROJECTION_FULL_EIM_JSON);
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        var deferred = this.bind(r, kwArgs);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
        
        return deferred;
    },

    createSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/user/" + 
            cosmo.util.auth.getUsername() + "/subscriptions";
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";
        
        var deferred = this.bind(r, kwArgs)
        this.addErrorCodeToExceptionErrback(deferred, 409, cosmo.service.exception.ConflictException);
        
        return deferred;
    },

    saveSubscription: function(subscription, postContent, kwArgs){
        kwArgs = kwArgs || {};

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/" + subscription.getUrls()[this.EDIT_LINK];
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "PUT";
        
        return this.bind(r, kwArgs);
    },
    
    saveCollection: function(collection, postContent, kwArgs){
        kwArgs = kwArgs || {};

        var r = {};
        r.url = cosmo.env.getBaseUrl() + "/atom/" + collection.getUrls()[this.EDIT_LINK];
        r.contentType = "application/xhtml+xml";
        r.postContent = postContent;
        r.method = "PUT";
        return this.bind(r, kwArgs);
    },

    deleteItem: function(item, kwArgs){
        kwArgs = kwArgs || {};
        var editLink = item.getUrls()[this.EDIT_LINK];
        var r = {};
        r.url = cosmo.env.getBaseUrl() +
          "/atom/" + editLink;
        r.method = "DELETE";
        var deferred = this.bind(r, kwArgs);
        this.addErrorCodeToExceptionErrback(deferred, 423, cosmo.service.exception.CollectionLockedException);
         
        return deferred;
    },

    removeItem: function(collection, item, kwArgs){

    },

    _prefExists: function (key){
        var exists = false;
        dojo.io.bind(this.getDefaultRequest(null, {
          url: cosmo.env.getBaseUrl() +
               "/atom/user/" + cosmo.util.auth.getUsername() + "/preference" + "/"+ key,
          method: "HEAD",
          sync: true,
          handle: function (result, error, xhr){
             if (xhr.status == 200) exists = true;
          }
        }
        ));
        
        return exists;
        
    },
    
    // This is not the right way to do this. We shouldn't be guessing urls,
    // but since the design does not currently support processing the entry in this
    // layer (and thus does not have access to the urls returned by 
    // getting the preference collection) I believe this is the best option for the moment. 
    //
    // Once this layer is redesigned, we should redo this. If we get a chance, this might be something
    // to improve before preview.
    setPreference: function (key, val, postContent, kwArgs){
        var method;
        var url;

        if (this._prefExists(key)) {
            method = "PUT"
            url = cosmo.env.getBaseUrl() +
                     "/atom/user/" + cosmo.util.auth.getUsername() + "/preference/" + key;
        } else {
            method = "POST";
            url = cosmo.env.getBaseUrl() +
                     "/atom/user/" + cosmo.util.auth.getUsername() + "/preferences";
            
        }
        return this.bind(
            {
                contentType: "application/atom+xml",
                url: url,
                postContent: postContent,
                method: method
            },
            kwArgs);
    },

    getPreferences: function (kwArgs){
        return this.bind(
            {url: cosmo.env.getBaseUrl() +
                  "/atom/user/" + cosmo.util.auth.getUsername() + "/preferences"
            }, 
            kwArgs);
    },
    
    getPreference: function (key, kwArgs){
        return this.bind(
            {
                url: cosmo.env.getBaseUrl() +
                     "/atom/user/" + cosmo.util.auth.getUsername() + "/preference/" + key,
                method: "GET"
            },
            kwArgs);
    },
    
    deletePreference: function(key, kwArgs){
        return this.bind(
            {
                url: cosmo.env.getBaseUrl() +
                     "/atom/user/" + cosmo.util.auth.getUsername() + "/preference/" + key,
                method: "DELETE"
            },
            kwArgs);
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
