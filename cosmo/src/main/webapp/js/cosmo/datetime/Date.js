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

dojo.require("dojo.date.common");
dojo.require("dojo.date.format");
dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.timezone");
dojo.require("cosmo.util.debug");
dojo.require("cosmo.facade.pref");

cosmo.datetime.Date = function () {
    
    var args = Array.prototype.slice.apply(arguments);
    var t = null;
    var dt = null;
    var tz = null;
    var utc = false;

    // No args -- create a floating date based on the current local offset
    if (args.length == 0) {
        dt = new Date();
    }
    // Date string or timestamp -- assumes floating
    else if (args.length == 1) {
        dt = new Date(args[0]);
    }
    // year, month, [date,] [hours,] [minutes,] [seconds,] [milliseconds,] [tzId,] [utc]
    else {
        t = args[args.length-1];
        // Last arg is utc
        if (typeof t == 'boolean') {
            utc = args.pop();
            tz = args.pop();
        }
        // Last arg is tzId
        else if (typeof t == 'string') {
            tz = args.pop();
            if (tz == 'Etc/UTC' || tz == 'Etc/GMT') {
                utc = true;
            }
        }

        // Date string (e.g., '12/27/2006')
        t = args[args.length-1];
        if (typeof t == 'string') {
            dt = new Date(args[0]);
        }
        // Date part numbers
        else {
            var a = [];
            for (var i = 0; i < 8; i++) {
                a[i] = args[i] || 0;
            }
            dt = new Date(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]);
        }
    }
    this.year = 0;
    this.month = 0;
    this.date = 0;
    this.hours= 0;
    this.minutes = 0;
    this.seconds = 0;
    this.milliseconds = 0;
    this.tzId = tz || null; 
    this.utc = utc || false;
    
    this.setFromDateObjProxy(dt);
    this._strftimeCache = [null, null];
}

//This is just an alias from ScoobyDate to comso.datetime.Date for use while we
//ferrett out all remaining ScoobyDates
cosmo.util.debug.aliasToDeprecatedFuncion(cosmo.datetime.Date, "ScoobyDate", "0.6");

cosmo.datetime.Date.prototype.getFullYear = function() {
    return this.year;
};
cosmo.datetime.Date.prototype.getYear = function() {
    return this.getFullYear();
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
cosmo.datetime.Date.prototype.getMilliseconds = function() {
    return this.milliseconds;
};
cosmo.datetime.Date.prototype.getUTCDate = function () { 
    return this.getUTCDateProxy().getUTCDate(); 
};
cosmo.datetime.Date.prototype.getUTCDay = function () { 
    return this.getUTCDateProxy().getUTCDay(); 
};
cosmo.datetime.Date.prototype.getUTCFullYear = function () { 
    return this.getUTCDateProxy().getUTCFullYear(); 
};
cosmo.datetime.Date.prototype.getUTCHours = function () { 
    return this.getUTCDateProxy().getUTCHours(); 
};
cosmo.datetime.Date.prototype.getUTCMilliseconds = function () { 
    return this.getUTCDateProxy().getUTCMilliseconds(); 
};
cosmo.datetime.Date.prototype.getUTCMinutes = function () { 
    return this.getUTCDateProxy().getUTCMinutes(); 
};
cosmo.datetime.Date.prototype.getUTCMonth = function () { 
    return this.getUTCDateProxy().getUTCMonth(); 
};
cosmo.datetime.Date.prototype.getUTCSeconds = function () { 
    return this.getUTCDateProxy().getUTCSeconds(); 
};
cosmo.datetime.Date.prototype.setFullYear = function(n) {
    this.setAttribute('year', n);
};
cosmo.datetime.Date.prototype.setYear = function(n) {
    this.setAttribute('year', n);
};
cosmo.datetime.Date.prototype.setMonth = function(n) {
    this.setAttribute('month', n);
};
cosmo.datetime.Date.prototype.setDate = function(n) {
    this.setAttribute('date', n);
};
cosmo.datetime.Date.prototype.setHours = function(n) {
    this.setAttribute('hours', n);
};
cosmo.datetime.Date.prototype.setMinutes = function(n) {
    this.setAttribute('minutes', n);
};
cosmo.datetime.Date.prototype.setSeconds = function(n) {
    this.setAttribute('seconds', n);
};
cosmo.datetime.Date.prototype.setMilliseconds = function(n) {
    this.setAttribute('milliseconds', n);
};
cosmo.datetime.Date.prototype.setUTCDate = function (n) { 
    this.setUTCAttribute('date', n); 
};
cosmo.datetime.Date.prototype.setUTCFullYear = function (n) { 
    this.setUTCAttribute('year', n); 
};
cosmo.datetime.Date.prototype.setUTCHours = function (n) { 
    this.setUTCAttribute('hours', n); 
};
cosmo.datetime.Date.prototype.setUTCMilliseconds = function (n) { 
    this.setUTCAttribute('milliseconds', n); 
};
cosmo.datetime.Date.prototype.setUTCMinutes = function (n) { 
    this.setUTCAttribute('minutes', n); 
};
cosmo.datetime.Date.prototype.setUTCMonth = function (n) { 
    this.setUTCAttribute('month', n); 
};
cosmo.datetime.Date.prototype.setUTCSeconds = function (n) { 
    this.setUTCAttribute('seconds', n); 
};
cosmo.datetime.Date.prototype.setFromDateObjProxy = function (dt, fromUTC) {
    this.year = fromUTC ? dt.getUTCFullYear() : dt.getFullYear();
    this.month = fromUTC ? dt.getUTCMonth() : dt.getMonth();
    this.date = fromUTC ? dt.getUTCDate() : dt.getDate();
    this.hours = fromUTC ? dt.getUTCHours() : dt.getHours();
    this.minutes = fromUTC ? dt.getUTCMinutes() : dt.getMinutes();
    this.seconds = fromUTC ? dt.getUTCSeconds() : dt.getSeconds();
    this.milliseconds = fromUTC ? dt.getUTCMilliseconds() : dt.getMilliseconds();
};
cosmo.datetime.Date.prototype.getUTCDateProxy = function () {
    var dt = new Date(Date.UTC(this.year, this.month, this.date, 
        this.hours, this.minutes, this.seconds, this.milliseconds));
    dt.setUTCMinutes(dt.getUTCMinutes() - this.getTimezoneOffset());
    return dt;
};
cosmo.datetime.Date.prototype.setAttribute = function (unit, n) {
    if (isNaN(n)) { throw('Units must be a number.'); }
    var dt = new Date(this.year, this.month, this.date,
        this.hours, this.minutes, this.seconds, this.milliseconds);
    var meth = unit == 'year' ? 'FullYear' : unit.substr(0, 1).toUpperCase() +
        unit.substr(1);
    dt['set' + meth](n);
    this.setFromDateObjProxy(dt);
};
cosmo.datetime.Date.prototype.setUTCAttribute = function (unit, n) {
    if (isNaN(n)) { throw('Units must be a number.'); }
    var meth = unit == 'year' ? 'FullYear' : unit.substr(0, 1).toUpperCase() +
        unit.substr(1);
    var dt = this.getUTCDateProxy();
    dt['setUTC' + meth](n);
    dt.setUTCMinutes(dt.getUTCMinutes() + this.getTimezoneOffset());
    this.setFromDateObjProxy(dt, true);
};

/**
 * Returns the time in milliseconds since January 1st, 1970 UTC.
 */
cosmo.datetime.Date.prototype.toUTC = function() {
    var utc = Date.UTC(this.getYear(), this.getMonth(), this.getDate(),
        this.getHours(), this.getMinutes(), this.getSeconds(), this.getMilliseconds());
    return(utc + this.getTimezoneOffsetMs());
};

/**
 * Updates a cosmo.datetime.Date based on a UTC stamp
 * Naive implementation assumes timezone offset for new UTC does not change
 */
cosmo.datetime.Date.prototype.updateFromUTC = function(utc) {
    var dt = null;

    // Get a fake Date object in UTC frame of reference
    dt = new Date(utc - this.getTimezoneOffsetMs());

    // Update cosmo.datetime.Date values based on UTC values
    this.year = dt.getUTCFullYear();
    this.month = dt.getUTCMonth();
    this.date = dt.getUTCDate();
    this.hours = dt.getUTCHours();
    this.minutes = dt.getUTCMinutes();
    this.seconds = dt.getUTCSeconds();
    this.milliseconds = dt.getUTCMilliseconds();
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
    this.milliseconds = dt.getMilliseconds();
};

/**
 * Returns the offset from GMT in minutes
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
            if (timezone){
                return (timezone.getOffsetInMinutes(this)*-1);
            } 
            else {
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
cosmo.datetime.Date.prototype.strftime = function strftime(formatString){
    if (this._strftimeCache[0] == this.hash()){
        return this._strftimeCache[1];
    }
    // No need to do any mucking around with UTC offsets or anything
    // for this function, since all we care about is the output
    var d = new Date(this.getYear(), this.getMonth(), this.getDate(),
        this.getHours(), this.getMinutes(), this.getSeconds());
    var formatted = dojo.date.strftime(d, formatString);
    this._strftimeCache = [this.hash(), formatted];
    return formatted;
};

/**
 * Increments by the desired number of specified units
 */
cosmo.datetime.Date.prototype.add = function(interv, incr) {
    var dt = null;
    var ret = null;
    // Get incremented Date 
    // 'n', 'd', etc., string keys
    if (typeof interv == 'string') {
    dt = Date.add(interv, incr, this.toUTC());
    }
    // dojo.date.dateParts 
    else {
        dt = dojo.date.add(this.toUTC(), interv, incr);
    }
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
        dt.getSeconds(), dt.getMilliseconds(), tz);
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
            this.getSeconds(), this.getMilliseconds(), this.tzId);

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

cosmo.datetime.Date.prototype.equals = function dateEquals(/*cosmo.datetime.Date*/ that){
return  that != null && 
        this.year == that.year &&
        this.month == that.month &&
        this.date == that.date &&
        this.hours == that.hours &&
        this.minutes == that.minutes &&
        this.seconds == that.seconds &&
        this.milliseconds == that.milliseconds &&
        this.tzId == that.tzId &&
        this.utc == that.utc;

}

cosmo.datetime.Date.prototype.hash = function dateHash(){
    var hash =   this.year + ":" 
               + this.month + ":"    
               + this.date + ":"    
               + this.hours + ":"    
               + this.minutes + ":"    
               + this.seconds + ":"    
               + this.milliseconds + ":"
               + this.tzId;  
    return hash;
}

// Date static methods
// ===========================
cosmo.datetime.Date.clone = function(sdt) {
    dojo.deprecated("cosmo.datetime.Date.clone", 
    "Use the instance method, rather than the static version", "0.6");
    var ret = new cosmo.datetime.Date(sdt.getFullYear(), sdt.getMonth(),
            sdt.getDate(), sdt.getHours(), sdt.getMinutes(),
            sdt.getSeconds(), sdt.getMilliseconds(), sdt.tzId);

    return ret;
}
cosmo.util.debug.aliasToDeprecatedFuncion(
    cosmo.datetime.Date.clone, "ScoobyDate.clone", "0.6");

/**
 * Returns the UTC offset (in milliseconds) for a particular date for the user's
 * current timezone.
 * Right now the user's timezone is the javascript timezone, so this implementation
 * is rather trivial. Will be useful once a settable timezone preferences exists.
 */
cosmo.datetime.Date.getBrowserTimezoneOffset = function(year, month, day, hours, minutes, seconds){
    var date = new Date(year, month, day, hours, minutes, seconds, 0);
    return date.getTimezoneOffset();
}
cosmo.util.debug.aliasToDeprecatedFuncion(
    cosmo.datetime.Date.getBrowserTimezoneOffset, 
    "ScoobyDate.getBrowserTimezoneOffset", "0.6");

/**
 * Returns the difference in specified units between two Date
 */
cosmo.datetime.Date.diff = function(interv, sdt1, sdt2) {
    var ret = null;
    if (typeof interv == 'string') {
        ret = Date.diff(interv, sdt1.getTime(), sdt2.getTime());
}
    else {
        ret = dojo.date.diff(sdt1.getTime(), sdt2.getTime(), interv);
    }
    return ret;
}
cosmo.util.debug.aliasToDeprecatedFuncion(
    cosmo.datetime.Date.diff, "ScoobyDate.diff", "0.6");


/**
 * Returns a new Date incremented the desired number of units
 */
cosmo.datetime.Date.add = function(dt, interv, incr) {
    var d = null;
    // 'n', 'd', etc., string keys
    if (typeof interv == 'string') {
        d = Date.add(interv, incr, dt.getTime());
    }
    // dojo.date.dateParts
    else {
        d = dojo.date.add(dt.getTime(), interv, incr);
    }
    // JS Date
    if (dt instanceof Date) {
        return d;
    }
    // cosmo.datetime.Date
    else if (dt instanceof cosmo.datetime.Date) {
        var ret = this.clone(dt);
        ret.updateFromUTC(d.getTime());
    return ret;
}
    else {
        throw('dt is not a usable Date object.');
    }
}

cosmo.util.debug.aliasToDeprecatedFuncion(
    cosmo.datetime.Date.add, "ScoobyDate.add", "0.6");
