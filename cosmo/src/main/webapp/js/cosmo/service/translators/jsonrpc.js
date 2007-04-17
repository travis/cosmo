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
 * A module that provides translators from data received from a
 * JSON-RPC service to cosmo.model.Object objects.
 */
dojo.provide("cosmo.service.translators.jsonrpc");

dojo.require("cosmo.model");
dojo.require("cosmo.service.translators.common");

JAVA_JSON_MAPPING = {"java.util.Date":Date,
                     "net.fortuna.ical4j.model.Date":Date,
                     "net.fortuna.ical4j.model.DateTime":Date,
                     "org.osaf.cosmo.rpc.model.Event":CalEventData,
                     "org.osaf.cosmo.rpc.model.CosmoDate":cosmo.datetime.Date,
                     "org.osaf.cosmo.rpc.model.RecurrenceRule":RecurrenceRule,
                     "org.osaf.cosmo.rpc.model.Modification":Modification,
                     "org.osaf.cosmo.rpc.model.Calendar":CalendarMetadata,
                     "org.osaf.cosmo.rpc.model.Subscription" : Subscription,
                     "org.osaf.cosmo.rpc.model.Ticket" : Ticket };

JAVA_EXCEPTION_MAPPING = {"org.osaf.scooby.rpc.RPCException":null,//ScoobyServiceRemoteException,
                          "org.osaf.scooby.rpc.NotAuthenticatedException":null};//NotAuthenticatedException};

cosmo.service.translators.jsonrpc = {
    convertObject: function(object){
        if (!object){
    
            return object;
        }
    
        //test if the returned value is an Array
        if (typeof object == "object" && object[0]){
    
            var newArray = [];
            for (var x = 0; x < object.length; x++){
                newArray[x] = this.convertObject(object[x]);
            }
            return newArray;
        }
    
       //If it's javaclass, we might need to convert it
        if (!object.javaClass){
            return object;
        }
    
        if (object.javaClass == "java.util.HashMap"){
            return this.convertMap(object);
        }
        
        if (object.javaClass == "java.util.HashSet"){
            return this.convertSet(object);
        }
    
        var constructor = JAVA_JSON_MAPPING[object.javaClass];
        if (constructor){
           var newObject = new constructor();
    
           //copy over each property to the new object
           for (var propName in object){
               var prop = object[propName];
    
               //if the property is an object, we need to convert it
               if (prop && typeof prop == "object" ){
                   prop = this.convertObject(prop);
               }
    
                   newObject[propName] = prop;
           }
           return newObject
        }
    
        return object;
    },


    convertMap: function(map){
        map = map.map;
        var newmap = {};
        for (var propName in map){
            newmap[propName] = this.convertObject(map[propName]);
        }
        return newmap;
    },

    convertSet: function(set){
        set = set.set;
        var newset = {};
        for (var prop in set){
            prop = this.convertObject(prop);
            newset[prop] = prop;
        }
        return newset;
    }
}