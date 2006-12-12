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

/**
 * A cosmo.datetime.Date is how we represent date-times in Cosmo's events
 *
 * The absence of a timezone together with the utc property being false indicates
 * a floating date-time.
 *
 * @param year the full year of the date
 * @param month the month of the date. January is 0.
 * @param minutes the minutes of the time
 * @param seconds the seconds of the time
 * @param timezone the timezone of the time
 * @param utc Boolean indicates that this is a utc time
 */
dojo.provide("cosmo.datetime.Date");
dojo.require("cosmo.util.debug");

cosmo.datetime.Date = function (year, month, date, hours, minutes, seconds, tzId, utc) {
    this.year = year ? year : 0;
    this.month = month ? month : 0;
    this.date = date ? date : 0;
    this.hours= hours ? hours : 0;
    this.minutes = minutes ? minutes : 0;
    this.seconds = seconds ? seconds : 0;
    this.tzId = tzId || null; //should this default to user's default timezone? (which we don't have yet :-) )
    this.utc = utc ? utc : false;
}

//This is just an alias from ScoobyDate to comso.datetime.Date for use while we
//ferrett out all remaining ScoobyDates
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date, "ScoobyDate", "0.6");

cosmo.datetime.Date.prototype.getFullYear = function() {
    return this.getYear();
};
cosmo.datetime.Date.prototype.getYear = function() {
    return this.year;
};
cosmo.datetime.Date.prototype.getMonth = function() {
    return this.month;
};
cosmo.datetime.Date.prototype.getDate = function() {
    return this.date;
};
cosmo.datetime.Date.prototype.getHours = function() {
    return this.hours;
};
cosmo.datetime.Date.prototype.getMinutes = function() {
    return this.minutes;
};
cosmo.datetime.Date.prototype.getSeconds = function() {
    return this.seconds;
};
cosmo.datetime.Date.prototype.setFullYear = function(yea) {
    this.setYear(yea);
};
cosmo.datetime.Date.prototype.setYear = function(yea) {
    this.year = yea;
};
cosmo.datetime.Date.prototype.setMonth = function(mon) {
    this.month = mon;
};
cosmo.datetime.Date.prototype.setDate = function(dat) {
    this.date = dat;
};
cosmo.datetime.Date.prototype.setHours = function(hou, min) {
    this.hours = hou;
    if (min) {
        this.setMinutes(min);
    }
};
cosmo.datetime.Date.prototype.setMinutes = function(min) {
    this.minutes = min;
};
cosmo.datetime.Date.prototype.setSeconds = function(sec) {
    this.seconds = sec;
};

/**
 * Returns the time in milliseconds since January 1st, 1970 UTC.
 */
cosmo.datetime.Date.prototype.toUTC = function() {
    var utc = Date.UTC(this.getYear(), this.getMonth(), this.getDate(),
        this.getHours(), this.getMinutes(), this.getSeconds());
    return(utc - this.getTimezoneOffsetMs());
};

/**
 * Updates a cosmo.datetime.Date based on a UTC stamp
 * Naive implementation assumes timezone offset for new UTC does not change
 */
cosmo.datetime.Date.prototype.updateFromUTC = function(utc) {
    var dt = null;

    // Get a fake Date object in UTC frame of reference
    dt = new Date(utc + this.getTimezoneOffsetMs());

    // Update cosmo.datetime.Date values based on UTC values
    this.year = dt.getUTCFullYear();
    this.month = dt.getUTCMonth();
    this.date = dt.getUTCDate();
    this.hours = dt.getUTCHours();
    this.minutes = dt.getUTCMinutes();
    this.seconds = dt.getUTCSeconds();
};

/**
 * Updates a cosmo.datetime.Date based on a values from a local JS Date
 * Naive implementation assumes timezone offset for new UTC does not change
 * BANDAID: NEEDS TO BE REFACTORED
 */
cosmo.datetime.Date.prototype.updateFromLocalDate = function(dt) {
    // Update cosmo.datetime.Date values based on local JS date values
    // Used to make changes to dates that are already in a specific timezone
    this.year = dt.getFullYear();
    this.month = dt.getMonth();
    this.date = dt.getDate();
    this.hours = dt.getHours();
    this.minutes = dt.getMinutes();
    this.seconds = dt.getSeconds();
};

/**
 * Returns the offset from GMT in minutes
 * Minus GMT results sensibly in negative values -- this is opposite
 * behavior from JavaScript's Date object
 */
cosmo.datetime.Date.prototype.getTimezoneOffset = function() {
    var offsetMin = 0;

    // Is UTC, no need to do more work
    if (this.utc) {
        offsetMin = 0;
    }
    else {
        // Has timezone, use offset from timezone
        if (this.tzId) {
            var timezone = cosmo.datetime.timezone.getTimezone(this.tzId);
            if(timezone){
                return timezone.getOffsetInMinutes(this);
            } else {
                //couldn't find timezone just make it utc?
                return 0;
            }
        }

        // No timezone, no UTC -- must be a floating date
        else {
            offsetMin = this.getUserPrefTimezoneOffset();
        }
    }
    return offsetMin;
};

/**
 * Returns the offset from GMT in milliseconds
 */
cosmo.datetime.Date.prototype.getTimezoneOffsetMs = function() {
    return(this.getTimezoneOffset()*60*1000);
};


/**
  * One place to go to get the user-pref timezone offset for a cosmo.datetime.Date
  * This should ultimately work with the Scooby app's user prefs
  * Or independently with fallback to the normal browser local offset
  */
cosmo.datetime.Date.prototype.getUserPrefTimezoneOffset = function() {
    var offsetMin = 0;

    // Try to look up user pref
    if (typeof Pref == 'object') {
        offsetMin = Pref.getTimezoneOffset(this);
    }
    // Otherwise punt and go with the browser's offset for that date
    else {
        offsetMin = cosmo.datetime.Date.getBrowserTimezoneOffset(
            this.getYear(), this.getMonth(), this.getDate(),
            this.getHours(), this.getMinutes(), this.getSeconds());
    }
    return offsetMin;
};

/**
 * Formats the date in it's original timezone, using the strftime function
 * found in date.js.
 *
 * For example, if the timezone of the Date is EST and it's
 * Date is "1/2/2006 19:30", than that is the date that this function will
 * return no matter what your local timezone is.
 */
cosmo.datetime.Date.prototype.strftime = function(formatString){
    // No need to do any mucking around with UTC offsets or anything
    // for this function, since all we care about is the output
    var d = new Date(this.getYear(), this.getMonth(), this.getDate(),
        this.getHours(), this.getMinutes(), this.getSeconds());
    return Date.strftime(formatString, d);
};

/**
 * Increments by the desired number of specified units
 */
cosmo.datetime.Date.prototype.add = function(interv, incr) {
    var dt = null;
    var ret = null;
    // Get incremented localtime JS Date obj
    dt = Date.add(interv, incr, this.toUTC());
    // Update this date based on the new UTC
    this.updateFromUTC(dt.getTime());
};

/**
 * Formats the Date according to what time the date falls on in the user's
 * local timezone.
 *
 * For example, if this Date represents the date "1/1/2006 12am PST"
 * and your local timezone is EST, the date will be formatted as "1/1/2006 3am"
 */
cosmo.datetime.Date.prototype.strftimeLocalTimezone = function(formatString){
        var localDate = this.createLocalDate();
        return localDate.strftime(formatString);
};

/**
 * Returns what the Date representing the same point in time as this Date,
 * but in the user's local timezone.
 *
 * For example, if this Date represents the date "1/1/2006 12am PST"
 * and your local timezone is EST, the returned date will be "1/1/2006 3am EST"
 */
cosmo.datetime.Date.prototype.createLocalDate = function(){
    //For now, we can cheat using new Date() since that gives us a local date
    //with the given UTC
    var utc = this.toUTC();
    var dt = new Date(utc);
    var tz = ScoobyTimezone.getLocalTimezone(utc);
    var scoobDt = new cosmo.datetime.Date(dt.getFullYear(), dt.getMonth(),
        dt.getDate(), dt.getHours(), dt.getMinutes(),
        dt.getSeconds(), tz, false);
    return scoobDt;
};

/**
 * Returns the day of the week occupied by a Date
 * but in the user's local timezone.
 */
cosmo.datetime.Date.prototype.getLocalDay = function() {
    var localDt = new Date(this.toUTC());
    return localDt.getDay();
};

cosmo.datetime.Date.prototype.after = function(dt){
    var utc = (typeof dt == "number") ? dt : dt.toUTC();
    return  this.toUTC() > utc;
};

cosmo.datetime.Date.prototype.before = function(dt){
    var utc = (typeof dt == "number") ? dt : dt.toUTC();
    return  this.toUTC() < utc;
};

/**
 * Returns UTC timestamp for a Date
 * Leave for now for API compatibility
 */
cosmo.datetime.Date.prototype.getTime = function() {
   return this.toUTC();
};

cosmo.datetime.Date.prototype.toString = cosmo.util.debug.genericToString;

cosmo.datetime.Date.prototype.clone = function(){
    var ret = new cosmo.datetime.Date(this.getFullYear(), this.getMonth(),
            this.getDate(), this.getHours(), this.getMinutes(),
            this.getSeconds(), this.tzId, this.utc);

    return ret;
}

cosmo.datetime.Date.prototype.getTimezoneAbbrName = function(){
    if (this.tzId){
        var timezone = cosmo.datetime.timezone.getTimezone(this.tzId);
        if (timezone){
            return timezone.getAbbreviatedName(this);
        }
    }

    return "";
}

// Date static methods
// ===========================
cosmo.datetime.Date.clone = function(sdt) {
    dojo.deprecated("cosmo.datetime.Date.clone", "Use the instance method, rather than the static version", "0.6");
    var ret = new cosmo.datetime.Date(sdt.getFullYear(), sdt.getMonth(),
            sdt.getDate(), sdt.getHours(), sdt.getMinutes(),
            sdt.getSeconds(), sdt.tzId, sdt.utc);

    return ret;
}
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date.clone, "ScoobyDate.clone", "0.6");

/**
 * Returns the UTC offset (in milliseconds) for a particular date for the user's
 * current timezone.
 * Right now the user's timezone is the javascript timezone, so this implementation
 * is rather trivial. Will be useful once a settable timezone preferences exists.
 */
cosmo.datetime.Date.getBrowserTimezoneOffset = function(year, month, day, hours, minutes, seconds){
    var date = new Date(year, month, day, hours, minutes, seconds, 0);
    // Return a negative value for minus-GMT offset -- opposite behavior from JS
    return (date.getTimezoneOffset()*-1);
}
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date.getBrowserTimezoneOffset, "ScoobyDate.getBrowserTimezoneOffset", "0.6");

/**
 * Returns the difference in specified units between two Date
 */
cosmo.datetime.Date.diff = function(interv, sdt1, sdt2) {
    return Date.diff(interv, sdt1.toUTC(), sdt2.toUTC());
}
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date.diff, "ScoobyDate.diff", "0.6");


/**
 * Returns a new Date incremented the desired number of units
 */
cosmo.datetime.Date.add = function(sdt, interv, incr) {
    var ret = this.clone(sdt);
    ret.add(interv, incr);
    return ret;
}
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date.add, "ScoobyDate.add", "0.6");
