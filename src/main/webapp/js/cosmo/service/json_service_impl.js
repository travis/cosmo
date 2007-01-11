/*
 * Copyright 2006 Open Source Applications Foundation
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

dojo.provide("cosmo.service.json_service_impl")

dojo.require("cosmo.service.service_stub");
dojo.require("cosmo.datetime.*");
dojo.require("cosmo.model");

JSON_SERVICE_OBJECT_NAME = "scoobyService";

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

JAVA_EXCEPTION_MAPPING = {"org.osaf.scooby.rpc.RPCException":ScoobyServiceRemoteException,
                          "org.osaf.scooby.rpc.NotAuthenticatedException":NotAuthenticatedException};

function createSetterMethodName(propName){
    var setterMethodName = "set";
    setterMethodName += propName.charAt(0).toUpperCase();
    for (var x = 1; x < propName.length; x++){
        setterMethodName += propName.charAt(x);
    }
    return setterMethodName;
}

function convertObject(object){
    if (!object){

        return object;
    }

    //test if the returned value is an Array
    if (typeof object == "object" && object[0]){

        var newArray = [];
        for (var x = 0; x < object.length; x++){
            newArray[x] = convertObject(object[x]);
        }
        return newArray;
    }

   //If it's javaclass, we might need to convert it
    if (!object.javaClass){
        return object;
    }

    if (object.javaClass == "java.util.HashMap"){
        return convertMap(object);
    }
    
    if (object.javaClass == "java.util.HashSet"){
        return convertSet(object);
    }

    var constructor = JAVA_JSON_MAPPING[object.javaClass];
    if (constructor){
       var newObject = new constructor();

       //copy over each property to the new object
       for (var propName in object){
           var prop = object[propName];

           //if the property is an object, we need to convert it
           if (prop && typeof prop == "object" ){
               prop = convertObject(prop);
           }

           //see if there is a "setter" method - important especially for Date type
           var setterMethodName = createSetterMethodName(propName);
           if (newObject[setterMethodName]){
               newObject[setterMethodName](prop);
           } else {
               newObject[propName] = prop;
           }
       }
       return newObject
    }

    return object;
}

function convertMap(map){
    map = map.map;
    var newmap = {};
    for (var propName in map){
        newmap[propName] = convertObject(map[propName]);
    }
    return newmap;
}

function convertSet(set){
    set = set.set;
    var newset = {};
    for (var prop in set){
        prop = convertObject(prop);
        newset[prop] = prop;
    }
    return newset;
}

function wrapMethod(jsonRemoteObject, jsonRemoteMethod){
    var f =  function() {

        //is this an async call?
        if (typeof arguments[0] == "function"){
            var oldCallback = arguments[0];
            var requestId = JSONRpcClient.requestId;
            //alert("requestId!!! : " + requestId);

            arguments[0] = function(result, exception){
                if(exception != null){
                    oldCallback(null, wrapException(exception), requestId);
                } else {
                    oldCallback(convertObject(result), null, requestId);
                }
            }

            return jsonRemoteMethod.apply(jsonRemoteObject, arguments);
        }

        var returnVal = null;
        try {
            returnVal = jsonRemoteMethod.apply(jsonRemoteObject, arguments);
        } catch (exception) {
            throw wrapException(exception);
        }
        return convertObject(returnVal);
    }
    return f;
}

function wrapException(exception){
    if (exception.name == "JSONRpcClientException"){
        var wrappedException = new ScoobyServiceClientException();
        wrappedException.stack = exception.stack;
        wrappedException.message = exception.message;
        return wrappedException;
    }

    var constructor = JAVA_EXCEPTION_MAPPING[exception.name];
    if (constructor){
        var wrappedException = new constructor();
        wrappedException.javaStack = exception.javaStack;
        wrappedException.message = exception.message;
        return wrappedException;
    }
    return exception;
}

ScoobyService.prototype.getServiceAccessTime = function() {
    return this.serviceAccessTime;
}

ScoobyService.prototype.resetServiceAccessTime = function() {
    var tm = new Date();
    this.serviceAccessTime = tm.getTime();
}

ScoobyService.prototype.refreshServerSession = function() {
    var req = JSONRpcClient.poolGetHTTPRequest();
    req.open('GET', '/cosmo/keepalive.jsp', true);
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            // Do nothing -- it means we successfully got
            // the keepalive page
        }
    }
    req.send(null);
}

ScoobyService.prototype.init = function() {
    //get a handle on the json object
    var jsonrpc = new JSONRpcClient("/cosmo/JSON-RPC");
    var proto = ScoobyService.prototype;
    var jsonRemoteObject = jsonrpc[JSON_SERVICE_OBJECT_NAME];

    this.resetServiceAccessTime();

    for (var propName in proto){
        if (   (propName != 'init'
                && propName != 'getServiceAccessTime'
                && propName != 'resetServiceAccessTime'
                && propName != 'refreshServerSession')
            && typeof proto[propName] == "function") {

            var remoteMethod = jsonRemoteObject[propName];
            proto[propName] = wrapMethod(jsonRemoteObject, remoteMethod);

           if (propName == "getEvents") {
               proto[propName] = wrapGetEvents(proto[propName]);
           }
        }
    }
}

/**
 * This wraps getEvents one more time, working around some problems in .1.
 *
 * The basic problem is that we have no way of telling the server what timezone
 * the client is in (yet) so the server can't resolve floating date-times. This also
 * means that the server cannot sort, and that it might return too few (or too many)
 * events, since by default the floating date events will be resolved as if UTC is
 * the timezone. So the work around is to:
 *
 *    1) ask for a 24 hours extra worth of events on either end of the range
 *    2) filter out events that aren't in the "real" range
 *    3) sort the events here, on the client side
 *
 * TODO: Fix this mess!
 *
 */
wrapGetEvents = function(getEventsFunction){
    var oldFunction = getEventsFunction;
    var newFunction = function(){
        
        var realRangeStart = arguments[1];
        var realRangeEnd   = arguments[2];

        //add some padding
        oneDay = 24 * 60 * 60 * 1000;
        var paddedRangeStart = realRangeStart - oneDay;
        var paddedRangeEnd   = realRangeEnd + oneDay;
        arguments[1] = paddedRangeStart;
        arguments[2] = paddedRangeEnd;

        var returnVal = oldFunction.apply(this, arguments);

        cosmo.model.sortEvents(returnVal);
        
        var filteredEvents = new Array();
        for (var x = 0; x < returnVal.length; x++){
                var start = returnVal[x].start.toUTC();
                var end =  returnVal[x].end ? returnVal[x].end.toUTC() : start;

                // Throw out events where both start and end are on the same
                // side of the range
                if (!(start < realRangeStart && end < realRangeStart) &&
                    !(start > realRangeEnd && end > realRangeEnd)) {
                    filteredEvents[filteredEvents.length] = returnVal[x];
                }
        }

        return filteredEvents;
    }

    return newFunction;
}