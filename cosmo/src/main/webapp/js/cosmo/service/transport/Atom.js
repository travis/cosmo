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
        
    getCollection: function(collectionUid, kwArgs){

    },
    
    getItems: function (collectionUid, startTime, endTime, kwArgs){
        var d = new dojo.Deferred();
        var r = this.getDefaultRequest(d, kwArgs);
        
        r.url = cosmo.env.getBaseUrl() + 
          "/atom/collection/" +  collectionUid + "/full" +
          this._generateQueryString(kwArgs);
        
        dojo.io.bind(r);
        return d;
        
    },
    
    saveItem: function (item, kwArgs){

    },
    
    deleteItem: function(kwArgs){
    },
    
    removeItem: function(collection, item, kwArgs){

    },

    getRecurrenceRules: function(events, kwArgs){

    },

    saveRecurrenceRules: function(event, recurrenceRule, kwArgs){


    },

    expandEvents: function(events, startTime, endTime, kwArgs){

    },
    
    saveNewEventBreakRecurrence: function(event, originalEventUid,
        originalEventEndDate, kwArgs){

    },
    
    _generateQueryString: function(/*Object*/kwArgs){
        if (kwArgs && kwArgs.ticketKey)
            return "ticket=" + kwArgs.ticketKey;
        else 
            return "";
    }
       
    

    }
);
