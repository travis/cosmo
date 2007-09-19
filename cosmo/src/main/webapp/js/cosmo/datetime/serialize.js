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

cosmo.datetime.toIso8601 = function (/*cosmo.datetime.Date*/ date){
    // summary: returns a Date object based on an ISO 8601 formatted string (uses date and time)
    //TODO
    dojo.unimplemented();
};

cosmo.datetime.fromIso8601 = function(/*String*/formattedString, timezone){
    var date = new cosmo.datetime.Date();
    if (timezone){
        date.tzId = timezone;
    }
    else if (formattedString.substring(formattedString.length - 1).toLowerCase() == "z"){
        date.utc = true;
    }

    date.updateFromUTC(dojo.date.fromIso8601(formattedString).getTime());
    return date;
};

cosmo.datetime.fromRfc3339 = function(/*String*/rfcDate){
    return new cosmo.datetime.Date(dojo.date.fromRfc3339(rfcDate));
}


cosmo.datetime.parseIso8601Duration = 
function parseIso8601Duration(/*String*/duration){
   var r = "^(-?)P(?:(?:([0-9\.]*)Y)?(?:([0-9\.]*)M)?(?:([0-9\.]*)D)?(?:T(?:([0-9\.]*)H)?(?:([0-9\.]*)M)?(?:([0-9\.]*)S)?)?)(?:([0-9/.]*)W)?$"
   var dateArray = duration.match(r).slice(1);
   var multiplier = (dateArray[0] == "-")? -1 : 1;
   var dateHash = {
     multiplier: multiplier,
     year: (parseFloat(dateArray[1]) || 0) * multiplier,
     month: (parseFloat(dateArray[2]) || 0) * multiplier,
     day: (parseFloat(dateArray[3]) || 0) * multiplier,
     hour: (parseFloat(dateArray[4]) || 0) * multiplier,
     minute: (parseFloat(dateArray[5]) || 0) * multiplier,
     second: (parseFloat(dateArray[6]) || 0) * multiplier,
     week: (parseFloat(dateArray[7]) || 0) * multiplier
   }
   return dateHash
}

cosmo.datetime.addIso8601Duration = 
function addIso8601Duration(/*cosmo.datetime.Date*/date, 
                            /*String or Object*/duration){
    var dHash;
    if (typeof(duration) == "string"){
        dHash = cosmo.datetime.parseIso8601Duration(duration);
    } else {
        dHash = duration;
    }
    
    with (dojo.date.dateParts){
        if (dHash.year) date.add(YEAR, dHash.year);
        if (dHash.month) date.add(MONTH, dHash.month);
        if (dHash.day) date.add(DAY, dHash.day);
        if (dHash.hour) date.add(HOUR, dHash.hour);
        if (dHash.minute) date.add(MINUTE, dHash.minute);
        if (dHash.second) date.add(SECOND, dHash.second);
    }

    return date;
}

// Some variables for calculating durations
cosmo.datetime.durationsInSeconds = {
    DAY: 60*60*24,
    HOUR: 60*60,
    MINUTE: 60
}

cosmo.datetime.getDuration = function getDuration(dt1, dt2){
    
    var dur = {}
    with (cosmo.datetime.durationsInSeconds){
        var multiplier = 1;
        var secs = cosmo.datetime.Date.diff(dojo.date.dateParts.SECOND, dt1, dt2);
        if (secs < 0) {
            multiplier = -1;
            secs *= -1;
        }
        var day = Math.floor(secs / DAY);
        dur.day = day * multiplier;
        secs = secs - (day*DAY);
        var hour = Math.floor(secs / HOUR);
        dur.hour = hour * multiplier;
        secs = secs - (hour*HOUR);
        var minute = Math.floor(secs / MINUTE);
        dur.minute = minute * multiplier;
        secs = secs - (minute*MINUTE);
        dur.second = secs * multiplier;
        dur.multiplier = multiplier;
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
