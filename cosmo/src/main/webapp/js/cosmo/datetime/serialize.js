/*
 * Copyright 2006-2007 Open Source Applications Foundation
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

dojo.provide("cosmo.datetime.serialize");

dojo.require("dojo.date.serialize");

cosmo.datetime.fromIso8601 = function(/*String*/formattedString){
    // summary: returns a Date object based on an ISO 8601 formatted string (uses date and time)
    return new cosmo.datetime.Date(dojo.date.setIso8601(new Date(0, 0), formattedString));
};

cosmo.datetime.fromIso8601Date = function(/*String*/formattedString){
    // summary: returns a Date object based on an ISO 8601 formatted string (date only)
    return new cosmo.datetime.Date(dojo.date.setIso8601Date(new Date(0, 0), formattedString));
};

cosmo.datetime.fromIso8601Time = function(/*String*/formattedString){
    // summary: returns a Date object based on an ISO 8601 formatted string (date only)
    return new cosmo.datetime.Date(dojo.date.setIso8601Time(new Date(0, 0), formattedString));
};

cosmo.datetime.fromRfc3339 = function(/*String*/rfcDate){
    return new cosmo.datetime.Date(dojo.date.fromRfc3339(rfcDate));
}


cosmo.datetime.parseIso8601Duration = 
function parseIso8601Duration(/*String*/duration){
   var r = "^P(?:(?:([0-9\.]*)Y)?(?:([0-9\.]*)M)?(?:([0-9\.]*)D)?(?:T(?:([0-9\.]*)H)?(?:([0-9\.]*)M)?(?:([0-9\.]*)S)?)?)(?:([0-9/.]*)W)?$"
   var dateArray = duration.match(r).slice(1);
   var dateHash = {
     year: parseFloat(dateArray[0]),
     month: parseFloat(dateArray[1]),
     day: parseFloat(dateArray[2]),
     hour: parseFloat(dateArray[3]),
     minute: parseFloat(dateArray[4]),
     second: parseFloat(dateArray[5])
   }
   return dateHash
}

cosmo.datetime.addIso8601Duration = 
function addIso8601Duration(/*cosmo.datetime.date*/date, 
                            /*String or Object*/duration){
    var dHash;
    if (typeof(duration) == "string"){
        dHash = cosmo.datetime.parseIso8601Duration(duration);
    } else {
        dHash = duration;
    }
    
    var dateArray = duration.match(cosmo.datetime.iso8601Regex).slice(1);
    with(dojo.date.dateParts){
        if (dHash.year) date.add(YEAR, dHash.year);
        if (dHash.month) date.add(MONTH, dHash.month);
        if (dHash.day) date.add(DAY, dHash.day);
        if (dHash.hour) date.add(HOUR, dHash.hour);
        if (dHash.minute) date.add(MINUTE, dHash.minute);
        if (dHash.second) date.add(SECOND, dHash.second);
    }

    return date;
}

cosmo.datetime.getDuration = function getDuration(dt1, dt2){
    var dur = {}
    var startDate = dt1.clone();
    with(dojo.date.dateParts){
        dur.year = cosmo.datetime.Date.diff(YEAR, startDate, dt2);
        startDate.add(YEAR, dur.year);
        dur.month = cosmo.datetime.Date.diff(MONTH, startDate, dt2);
        startDate.add(MONTH, dur.month);
        dur.day = cosmo.datetime.Date.diff(DAY, startDate, dt2);
        startDate.add(DAY, dur.day);
        dur.hour = cosmo.datetime.Date.diff(HOUR, startDate, dt2);
        startDate.add(HOUR, dur.hour);
        dur.minutcosme = cosmo.datetime.Date.diff(MINUTE, startDate, dt2);
        startDate.add(MINUTE, dur.minute);
        dur.second = cosmo.datetime.Date.diff(SECOND, startDate, dt2);
        startDate.add(SECOND, dur.second);
   }
   return dur;
}

cosmo.datetime.durationHashToIso8601 = 
function durationHashToIso8601(/*Object*/dur){
    var ret =  "P";
    if (dur.year) ret += dur.year + "Y";
    if (dur.month) ret += dur.month + "M";
    if (dur.day) ret += dur.day + "D";
    if (dur.hour || dur.minute || dur.second){
        ret += "T";
        if (dur.hour) ret += dur.hour + "H";
        if (dur.minute) ret += dur.minute + "M";
        if (dur.second) ret += dur.second + "S";
    }
    return ret;
}
