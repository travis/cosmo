
/**
 * A scooby date is how we represent date-times in Scooby's events
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
function ScoobyDate(year, month, date, hours, minutes, seconds, timezone, utc) {
    this.year = year ? year : 0;
    this.month = month ? month : 0;
    this.date = date ? date : 0;
    this.hours= hours ? hours : 0;
    this.minutes = minutes ? minutes : 0;
    this.seconds = seconds ? seconds : 0;
    this.timezone = timezone ? timezone : null;
    this.utc = utc ? utc : false;
}
    ScoobyDate.prototype.getDate = function() {
        return this.date;
    };
    
    ScoobyDate.prototype.getFullYear = function() {
        return this.getYear();
    };
    
    ScoobyDate.prototype.getHours = function() {
        return this.hours;
    };
    ScoobyDate.prototype.getMinutes = function() {
        return this.minutes;
    };
    ScoobyDate.prototype.getMonth = function() {
        return this.month;
    };
    ScoobyDate.prototype.getSeconds = function() {
        return this.seconds;
    };
    ScoobyDate.prototype.getYear = function() {
        return this.year;
    };
    ScoobyDate.prototype.setDate = function(dat) {
        this.date = dat;
    };
    ScoobyDate.prototype.setFullYear = function(yea) {
        this.setYear(yea);
    };
    ScoobyDate.prototype.setHours = function(hou, min) {
        this.hours = hou;
        if (min) {
            this.setMinutes(min);
        }
    };
    ScoobyDate.prototype.setMinutes = function(min) {
        this.minutes = min;
    };
    ScoobyDate.prototype.setMonth = function(mon) {
        this.month = mon;
    };
    ScoobyDate.prototype.setSeconds = function(sec) {
        this.seconds = sec;
    };
    ScoobyDate.prototype.setYear = function(yea) {
        this.year = yea;
    };
    /**
     * Returns the time in milliseconds since January 1st, 1970 UTC.
     */
    ScoobyDate.prototype.toUTC = function() {
        var utc = Date.UTC(this.getYear(), this.getMonth(), this.getDate(),
            this.getHours(), this.getMinutes(), this.getSeconds());
        return(utc - this.getTimezoneOffsetMs());
    };
    /**
     * Updates a ScoobyDate based on a UTC stamp
     * Naive implementation assumes timezone offset for new UTC does not change
     */
    ScoobyDate.prototype.updateFromUTC = function(utc) {
        var dt = null;
        // Get a fake Date object in UTC frame of reference
        dt = new Date(utc + this.getTimezoneOffsetMs());
        // Update ScoobyDate values based on UTC values
        this.year = dt.getUTCFullYear();
        this.month = dt.getUTCMonth();
        this.date = dt.getUTCDate();
        this.hours = dt.getUTCHours();
        this.minutes = dt.getUTCMinutes();
        this.seconds = dt.getUTCSeconds();
        // Update TZ if needed
        if (this.timezone) {
            this.timezone = ScoobyDate.getNewTimezoneForUTC(this.timezone, utc);
        }
    };
    /**
     * Updates a ScoobyDate based on a values from a local JS Date
     * Naive implementation assumes timezone offset for new UTC does not change
     * BANDAID: NEEDS TO BE REFACTORED
     */
    ScoobyDate.prototype.updateFromLocalDate = function(dt) {
        // Update ScoobyDate values based on local JS date values
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
    ScoobyDate.prototype.getTimezoneOffset = function() {
        var offsetMin = 0;

        // Is UTC, no need to do more work
        if (this.utc) {
            offsetMin = 0;
        }
        // Has timezone, use offset from timezone
        if (this.timezone) {
            offsetMin = this.timezone.minutesOffset;
        }
        // No timezone, no UTC -- must be a floating date
        else {
            offsetMin = this.getUserPrefTimezoneOffset();
        }
        return offsetMin;
    };
    /**
     * Returns the offset from GMT in milliseconds
     */
    ScoobyDate.prototype.getTimezoneOffsetMs = function() {
        return(this.getTimezoneOffset()*60*1000);
    };
    /**
      * One place to go to get the user-pref timezone offset for a ScoobyDate
      * This should ultimately work with the Scooby app's user prefs
      * Or independently with fallback to the normal browser local offset
      */
    ScoobyDate.prototype.getUserPrefTimezoneOffset = function() {
        var offsetMin = 0;
        // Try to look up user pref
        if (typeof Pref == 'object') {
            offsetMin = Pref.getTimezoneOffset(this);
        }
        // Otherwise punt and go with the browser's offset for that date
        else {
            offsetMin = ScoobyDate.getBrowserTimezoneOffset(
                this.getYear(), this.getMonth(), this.getDate(),
                this.getHours(), this.getMinutes(), this.getSeconds());
        }
        return offsetMin;
    };
    /**
     * Formats the date in it's original timezone, using the strftime function
     * found in date.js.
     *
     * For example, if the timezone of the ScoobyDate is EST and it's
     * Date is "1/2/2006 19:30", than that is the date that this function will
     * return no matter what your local timezone is.
     */
    ScoobyDate.prototype.strftime = function(formatString){
        // No need to do any mucking around with UTC offsets or anything
        // for this function, since all we care about is the output
        var d = new Date(this.getYear(), this.getMonth(), this.getDate(),
            this.getHours(), this.getMinutes(), this.getSeconds());
        return Date.strftime(formatString, d);
    };
    /**
     * Increments by the desired number of specified units
     */
    ScoobyDate.prototype.add = function(interv, incr) {
        var dt = null;
        var ret = null;
        // Get incremented localtime JS Date obj
        dt = Date.add(interv, incr, this.toUTC());
        // Update this date based on the new UTC
        this.updateFromUTC(dt.getTime());
    };
    /**
     * Formats the ScoobyDate according to what time the date falls on in the user's
     * local timezone.
     *
     * For example, if this ScoobyDate represents the date "1/1/2006 12am PST"
     * and your local timezone is EST, the date will be formatted as "1/1/2006 3am"
     */
    ScoobyDate.prototype.strftimeLocalTimezone = function(formatString){
            var localScoobyDate = this.createLocalScoobyDate();
            return localScoobyDate.strftime(formatString);
    };
    /**
     * Returns what the ScoobyDate representing the same point in time as this ScoobyDate,
     * but in the user's local timezone.
     *
     * For example, if this ScoobyDate represents the date "1/1/2006 12am PST"
     * and your local timezone is EST, the returned date will be "1/1/2006 3am EST"
     */
    ScoobyDate.prototype.createLocalScoobyDate = function(){
        //For now, we can cheat using new Date() since that gives us a local date
        //with the given UTC
        var utc = this.toUTC();
        var dt = new Date(utc);
        var tz = ScoobyTimezone.getLocalTimezone(utc);
        var scoobDt = new ScoobyDate(dt.getFullYear(), dt.getMonth(),
            dt.getDate(), dt.getHours(), dt.getMinutes(),
            dt.getSeconds(), tz, false);
        return scoobDt;
    };
    /**
     * Returns the day of the week occupied by a ScoobyDate
     * but in the user's local timezone.
     */
    ScoobyDate.prototype.getLocalDay = function() {
        var localDt = new Date(this.toUTC());
        return localDt.getDay();
    };
    ScoobyDate.prototype.after = function(dt){
        var utc = (typeof dt == "number") ? dt : dt.toUTC();
        return  this.toUTC() > utc;
    };
    ScoobyDate.prototype.before = function(dt){
        var utc = (typeof dt == "number") ? dt : dt.toUTC();
        return  this.toUTC() < utc;
    };
    /**
     * Returns UTC timestamp for a ScoobyDate
     * Leave for now for API compatibility
     */
    ScoobyDate.prototype.getTime = function() {
       return this.toUTC();
    };
    
    ScoobyDate.prototype.toString = genericToString;


// ScoobyDate static methods
// ===========================
ScoobyDate.clone = function(sdt) {
    var ret = new ScoobyDate(sdt.getFullYear(), sdt.getMonth(),
            sdt.getDate(), sdt.getHours(), sdt.getMinutes(),
            sdt.getSeconds(), null, sdt.utc);
    if (sdt.timezone) {
        ret.timezone = new ScoobyTimezone(sdt.timezone.id,
            sdt.timezone.minutesOffset, sdt.timezone.name);
    }
    return ret;
}

/**
 * Returns the UTC offset (in milliseconds) for a particular date for the user's
 * current timezone.
 * Right now the user's timezone is the javascript timezone, so this implementation
 * is rather trivial. Will be useful once a settable timezone preferences exists.
 */
ScoobyDate.getBrowserTimezoneOffset = function(year, month, day, hours, minutes, seconds){
    var date = new Date(year, month, day, hours, minutes, seconds, 0);
    // Return a negative value for minus-GMT offset -- opposite behavior from JS
    return (date.getTimezoneOffset()*-1);
}

/**
 * Returns the difference in specified units between two ScoobyDates
 */
ScoobyDate.diff = function(interv, sdt1, sdt2) {
    return Date.diff(interv, sdt1.toUTC(), sdt2.toUTC());
}

/**
 * Returns a new ScoobyDate incremented the desired number of units
 */
ScoobyDate.add = function(sdt, interv, incr) {
    var ret = ScoobyDate.clone(sdt);
    ret.add(interv, incr);
    return ret;
}

/**
 * Returns a new ScoobyTimezone given an existing ScoobyTimezone and UTC stamp
 * The timezone minutesOffset and name may change, timezone id should be constant
 * This function is just stubbed out for now -- returns same data passed in
 * Later this will likely have to be migrated into the Scooby service
 * so we can grab this info off the server
 */
ScoobyDate.getNewTimezoneForUTC = function(tz, utc) {
    return tz;
}

/**
 * A ScoobyTimeZone is a reference to a timezone on the Scooby Server. It does not
 * fully specify a timezone, but merely indicates a reference and the UTC offset in
 * minutes for the date to which it belongs. Notice that this offset is not a constant
 * due to observances like Daylight Savings Time
 *
 * @param timezoneID the unique ID referencing the timezone
 * @param minutesOffset the number of minutes that this timezone differs from
 *                      UTC at a particular moment.
 */
function ScoobyTimezone(id, minutesOffset, name){
    this.id = id;
    this.minutesOffset = minutesOffset;
    this.name = name;

}

ScoobyTimezone.prototype.toString = genericToString;

/**
 * Returns the local timezone with offset set properly for a particular date.
 */
ScoobyTimezone.getLocalTimezone = function(utc){
   var dt = new Date(utc);
   var dtStr = dt.toString();
   var nameArr = [];
   var nameStr = '';

   // Hack to get something reasonable for the name
   // Use for the ID too, for now
   nameArr = dtStr.match(/\(\S+\)/);
   nameStr = (nameArr && nameArr[0]) ? nameArr[0].replace(/\)/, '').replace(/\(/, '') : 'Unknown';

   var timezone = new ScoobyTimezone(nameStr, (dt.getTimezoneOffset()/-1), nameStr);
   return timezone;
}

ScoobyTimezone.clone = function(stz) {
    return new ScoobyTimezone(stz.id, stz.minutesOffset, stz.name);
}