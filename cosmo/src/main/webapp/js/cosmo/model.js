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
dojo.provide("cosmo.model");
dojo.provide("cosmo.oldmodel");

dojo.require("cosmo.util.debug");
dojo.require("cosmo.util.hash");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");

cosmo.model.sortEvents = function(/*Array|cosmo.util.hash.Hash*/ events){
// summary: Sorts a collection of events based on start date. 
//          If the start dates are equal, longer events are  
//          sorted first

    var hash = events instanceof cosmo.util.hash.Hash;
    for (var x = 0; x < events.length; x++){
        var event = hash ? events.getAtPos(x) : events[x];
        if (event.data){
            event = event.data;
        }
        var eventStamp = event.getEventStamp();
        var startDate = eventStamp.getStartDate();
        var endDate = eventStamp.getEndDate();
        
        event.__startUTC = startDate.toUTC();
        event.__endUTC = endDate.toUTC();
    }
    
    events.sort(cosmo.model._eventComparator);

    for (var x = 0; x < events.length; x++){
        var event = hash ? events.getAtPos(x) : events[x];
        if (event.data){
            event = event.data;
        }
        delete(event.__startUTC);
        delete(event.__endUTC);
    }
}

cosmo.model._eventComparator = function (a, b) {
   // summary: used by cosmo.model.sortEvents.
   if (a.data){
       a = a.data;
   }
   if (b.data){
       b = b.data;
   }
    var aStart = a.__startUTC;
    var bStart = b.__startUTC;
    if (aStart > bStart) {
        return 1;
    }
    else if (aStart < bStart) {
        return -1;
    } else {
    // If start is equal, sort longer events first
      var aEnd = a.__endUTC;
      var bEnd = b.__endUTC;
        if (aEnd < bEnd) {
            return 1;
        }
        // Be sure to handle equal values
        else if (aEnd >= bEnd) {
            return -1;
        }
    }
};
