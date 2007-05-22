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
dojo.require("cosmo.service.transport.Rest");

dojo.declare("cosmo.service.transport.Atom", cosmo.service.transport.Rest,
    {

    createCollection: function(name){
        
    },

    getCollection: function(collectionUid, kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        var query = this._generateAuthQuery(kwArgs);
        
        r.url = cosmo.env.getBaseUrl() +
          "/atom/collection/" + collectionUid + "/details";
        
        return deferred;    
    },

    getCollections: function(kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        r.url = cosmo.env.getBaseUrl() +
          "/atom/user/" + cosmo.util.auth.getUsername();
        
        dojo.io.bind(r);
        return deferred;    
    },
    
    getSubscriptions: function (kwArgs){
        var deferred = new dojo.Deferred();
        
        //TODO
        deferred.callback([]);
        
        return deferred;
        
    },

    getItems: function (collectionUid, searchCrit, kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        var query = this._generateAuthQuery(kwArgs);
        dojo.lang.mixin(query, this._generateSearchQuery(searchCrit));

        r.url = cosmo.env.getBaseUrl() +
          "/atom/collection/" +  collectionUid + "/full/eim-json" +
          this.queryHashToString(query);

        dojo.io.bind(r);
        return deferred;

    },

    saveItem: function (item, postContent, kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        var query = this._generateAuthQuery(kwArgs);

        if (!item.editLink){
            throw new CantSaveException("No edit link on item with UID " + item.getUid());
        }
        r.contentType = "application/atom+xml";
        r.url = cosmo.env.getBaseUrl() + "/atom/" + 
                item.editLink + this.queryHashToString(query);
        r.postContent = postContent;
        r.method = "POST";
        r.headers['X-Http-Method-Override'] = "PUT";

        dojo.io.bind(r);
        return deferred;
        
    },
    
    getItem: function(uid, kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        var query = this._generateAuthQuery(kwArgs);
        
        r.url = cosmo.env.getBaseUrl() + "/atom/item/" + 
                uid + "/full/eim-json" + this.queryHashToString(query);
        r.method = "GET";
        dojo.io.bind(r);
        return deferred;
    },
    
    createItem: function(item, postContent, collectionUid, kwArgs){
        var deferred = new dojo.Deferred();
        var r = this.getDefaultRequest(deferred, kwArgs);
        
        var query = this._generateAuthQuery(kwArgs);
        
        r.url = cosmo.env.getBaseUrl() +
          "/atom/collection/" +  collectionUid + 
          this.queryHashToString(query);
        
        r.contentType = "application/atom+xml";
        r.postContent = postContent;
        r.method = "POST";

        dojo.io.bind(r);
        return deferred;
        
    },

    deleteItem: function(item, kwArgs){
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
            ret["start-min"] = dojo.date.toRfc3339(searchCrit.start);
        }
        if (searchCrit.end) {
            ret["start-max"] = dojo.date.toRfc3339(searchCrit.end);
        }
        return ret;
    }



    }
);

cosmo.service.transport.atom = new cosmo.service.transport.Atom();