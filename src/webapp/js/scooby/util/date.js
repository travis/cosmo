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
 * @fileoverview JavaScript implementation of Date.strftime, Date.add, Date.diff
 * @author Matthew Eernisse mailto:mde@osafoundation.org
 * @license Apache License 2.0
 * @version 0.1
 */

Date.formats = {
    'a': function(dt) { return Date.abbrWeekday[dt.getDay()]; },
    'A': function(dt) { return Date.fullWeekday[dt.getDay()]; },
    'b': function(dt) { return Date.abbrMonth[dt.getMonth()]; },
    'B': function(dt) { return Date.fullMonth[dt.getMonth()]; },
    'c': function(dt) { return dt.strftime('%a %b %d %T %Y'); },
    'C': function(dt) { return calcCentury(dt.getFullYear());; },
    'd': function(dt) { return lPadStr(dt.getDate(), 2, '0'); },
    'D': function(dt) { return dt.strftime('%m/%d/%y') },
    'e': function(dt) { return lPadStr(dt.getDate(), 2, ' '); },
    'h': function(dt) { return dt.strftime('%b'); },
    'H': function(dt) { return lPadStr(dt.getHours(), 2, '0'); },
    'I': function(dt) { return lPadStr(hrMil2Std(dt.getHours()), 2, '0'); },
    'j': function(dt) { return lPadStr(calcDays(dt), 3, '0'); },
    'k': function(dt) { return lPadStr(dt.getHours(), 2, ' '); },
    'l': function(dt) { return lPadStr(hrMil2Std(dt.getHours()), 2, ' '); },
    'm': function(dt) { return lPadStr((dt.getMonth()+1), 2, '0'); },
    'M': function(dt) { return lPadStr(dt.getMinutes(), 2, '0'); },
    'p': function(dt) { return meridian(dt.getHours()); },
    'r': function(dt) { return dt.strftime('%I:%M:%S %p'); },
    'R': function(dt) { return dt.strftime('%H:%M'); },
    'S': function(dt) { return lPadStr(dt.getSeconds(), 2, '0'); },
    'T': function(dt) { return dt.strftime('%H:%M:%S'); },
    'u': function(dt) { return adjSun(dt.getDay()); },
    'w': function(dt) { return dt.getDay(); },
    'x': function(dt) { return dt.strftime('%D'); },
    'X': function(dt) { return dt.strftime('%T'); },
    'y': function(dt) { return yearTwoDigits(dt.getFullYear()); },
    'Y': function(dt) { return lPadStr(dt.getFullYear(), 4, '0'); },
    '%': function(dt) { return '%'; }
}
// Create regex from all the above formats
Date.supported = new RegExp('%[' + getStftimeFormats() + ']{1}', 'g');

Date.fullWeekday = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 
    'Friday', 'Saturday'];
Date.abbrWeekday = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
Date.fullMonth = ['January', 'February', 'March', 'April', 'May', 'June', 
    'July', 'August', 'September', 'October', 'November', 'December'];
Date.abbrMonth = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 
    'Sep', 'Oct', 'Nov', 'Dec'];

// Allow localized AM/PM string
Date.meridian = {
    'AM': 'AM',
    'PM': 'PM'
}

/**
 * Format a JavaScript Unix timestamp as a string 
 * @param format Indicates format or formats to return
 * @param dtparam Unix timestamp to format (optional) -- also accepts JS Date obj. 
 * If param is not passed, the current datetime is used.
 * @return A string formatted according to the givent format, using the given
 * timestamp, or the current datetime if no timestamp is given. 
 * This naive implementation uses only English/US-formats for the locale.
 */

/*
 * Instance method that you can call on Date objects
 * var dt = new Date(); dt = dt.strftime('%Y-%m-%d %T'); return dt;
 * ========================
 */
Date.prototype.strftime = function(format) {
    return Date.strftime(format, this);
}

/*
 * Static method that takes a JS Date obj or UTC timestamp
 * ========================
 */
Date.strftime = function(format, dt) {
    var dtParam = null;
    var patArr = [];
    var dtArr = [];
    var str = format;
    
    // If no dt, use current date
    dtParam = dt ? dt : new Date();
    // Allow either Date obj or UTC stamp
    dtParam = typeof dt == 'number' ? new Date(dt) : dt;
    
    // Grab all instances of expected formats into array
    while (patArr = Date.supported.exec(format)) {
        dtArr.push(patArr[0]);
    }
    
    // Process any hits
    for (var i = 0; i < dtArr.length; i++) {
        key = dtArr[i].replace(/%/, '');
        str = str.replace('%'+key, Date.formats[key](dtParam));
    }
    return str;
}

/**
 * Avoid the JavaScript 'Year 1000' annoyance by simply truncating getFullYear
 * to get the shortened year number.
 * @param yr Integer year number
 * @return String for last two digits of year (e.g., '07', '02')
 */
function yearTwoDigits(yr) {
    // Add a millenium to take care of years before the year 1000, 
    // since we're only taking the last two digits
    var millenYear = yr + 1000;
    var str = millenYear.toString();
    str = str.substr(2); // Get the last two digits
    return str
}

/**
 * Convert a 24-hour formatted hour to 12-hour format
 * @param hour Integer hour number
 * @return String for hour in 12-hour format -- may be string length of one
 */
function hrMil2Std(hour) {
    var h = typeof hour == 'number' ? hour : parseInt(hour);
    var str = h > 12 ? h - 12 : h;
    str = str == 0 ? 12 : str;
    return str;
}

/**
 * Convert a 12-hour formatted hour with meridian flag to 24-hour format
 * @param hour Integer hour number
 * @param pm Boolean flag, if PM hour then set to true
 * @return String for hour in 24-hour format
 */
function hrStd2Mil(hour, pm) {
    var h = typeof hour == 'number' ? hour : parseInt(hour);
    var str = '';
    // PM
    if (pm) {
        str = h < 12 ? (h+12) : h;
    }
    // AM
    else {
        str = h == 12 ? 0 : h;
    }
    return str;
}

/**
 * Make sure a string is specified length
 * @param instr Any string or var with toString method
 * @return String with specified length with spacer char added
 */
function lPadStr(instr, len, spacer) {
    var str = instr.toString();
    var sp = spacer ? spacer : ' '; // spacer char optional, default to space
    while (str.length < len) {
        str = sp+str;
    }
    return str;
}

/**
 * Return 'AM' or 'PM' based on hour in 24-hour format
 * @param h Integer for hour in 24-hour format
 * @return String of either 'AM' or 'PM' based on hour number
 */
function meridian(h) {
    return h > 11 ? Date.meridian['PM'] : Date.meridian['AM'];
}

/**
 * Calculate the century to which a particular year belongs
 * @param y Integer year number
 * @return Integer century number
 */
function calcCentury(y) {
    var ret = parseInt(y/100);
    ret = ret.toString();
    return lPadStr(ret);
}

/**
 * Calculate the day number in the year a particular date is on
 * @param dt JavaScript date object
 * @return Integer day number in the year for the given date
 */
function calcDays(dt) {
    var first = new Date(dt.getFullYear(), 0, 1);
    var diff = 0;
    var ret = 0;
    first = first.getTime();
    diff = (dt.getTime() - first);
    ret = parseInt(((((diff/1000)/60)/60)/24))+1;
    return ret;
}

/**
 * Adjust from 0-6 base week to 1-7 base week
 * @param d integer for day of week
 * @return Integer day number for 1-7 base week
 */
function adjSun(d) {
    return d == 0 ? 7 : d;
}

/**
 * Get a string of all the supported strtime formats in Date.strftime
 * @return String of all the supported formats appended together
 */
function getStftimeFormats() {
    var str = '';
    for(var i in Date.formats)  {
        str += i;
    }
    return str;
}

/*
 * Instance method that you can call on Date objects
 * var dt = new Date(); 
 * dt.add('d', 5); 
 * return dt;
 * ========================
 */
Date.prototype.add = function(interv, incr) {
    var ndt = Date.add(interv, incr, this);
    this.setTime(ndt.getTime());
}

/*
 * Static method that requires a Date object passed in
 * ========================
 */
Date.add = function(interv, incr, dt) {
    /*
    yyyy - Year
    q - Quarter
    m - Month
    y - Day of year
    d - Day
    w - Weekday
    ww - Week of year
    h - Hour
    n - Minute
    s - Second
    */
    
    var dtParam = (typeof dt == 'number') ? new Date(dt) : dt; // Convert Unix timestamp to Date obj if need be
    var intervFlag = interv.toLowerCase();
    var yea = dtParam.getFullYear();
    var mon = dtParam.getMonth();
    var dat = dtParam.getDate();
    var hou = dtParam.getHours();
    var min = dtParam.getMinutes();
    var sec = dtParam.getSeconds();
    var retDate = new Date(dt);
    
    switch(intervFlag) {
        case 'yyyy':
            retDate.setFullYear(yea + incr);
            break;
        case 'q':
            //var currQ = parseInt(mon/3)+1;
            //var incrYear = (incr > 0) ? parseInt(incr/4)+1 : parseInt(incr/4);
            
            // Naive implementation of quarters as just adding three months
            incr*=3;
            
            break;
        case 'm':               
            if (incr < 0){
                while (incr < 0){
                    incr++;
                    mon--;
                    if(mon < 0){
                        mon=11;
                        yea--;
                    }
                }                   
            }
            else {
                while(incr > 0){
                    incr--;
                    mon++;
                    if (mon > 11){
                        mon=0;
                        yea++;
                    }
                }
            } 
            retDate.setMonth(mon);
            retDate.setFullYear(yea);
            if (retDate.getMonth() != mon){
                // Overshot month due to date 
                // Go to last day of previous month 
                retDate.setDate(0) 
            }
            break;
        case 'd':
        case 'y':
            retDate.setDate(dat + incr);
            break;
        case 'h':
            retDate = dtParam.getTime();
            retDate = retDate + (incr*1000*60*60);
            retDate = new Date(retDate);
            break;
        case 'n':
            retDate = dtParam.getTime();
            retDate = retDate + (incr*1000*60);
            retDate = new Date(retDate);
            break;
        case 's':
            retDate = dtParam.getTime();
            retDate = retDate + (incr*1000);
            retDate = new Date(retDate);
            break;
        case 'w':
            var weekSpan = parseInt(incr/5); // Number of weeks spanned
            // If less than one week, check for intervening weekend
            if (weekSpan == 0) {
                if ((dtParam.getDay() + incr) > 4) {
                    retDate.setDate(dat + incr + 2);
                }
                else {
                    retDate.setDate(dat + incr);
                }
            }
            // Otherwise add along with intervening weekend days
            else {
                retDate.setDate(dat + incr + (2*weekSpan));
            }
            break;
        case 'ww':
            retDate.setDate(dat + (incr*7));
            break;
        default:
            // Do nothing
            break;
        
    } 
    return retDate;
}

/*
 * Instance method that you can call on Date objects
 * var dt = new Date('10/31/2112'); 
 * var diff = dt.diff('d', new Date('12/31/2112')); 
 * return diff;
 * ========================
 */
Date.prototype.diff = function(interv, dt2) {
    return Date.diff(interv, this, dt2);
}

/*
 * Static method that requires a Date object passed in
 * ========================
 */
Date.diff = function(interv, dt1, dt2) {
    /*
    yyyy - Year
    q - Quarter
    m - Month
    y - Day of year
    d - Day
    w - Weekday
    ww - Week of year
    h - Hour
    n - Minute
    s - Second
    */
    
    // Convert Unix timestamp to Date obj if need be
    var dtParam1 = (typeof dt1 == 'number') ? new Date(dt1) : dt1;
    // Convert Unix timestamp to Date obj if need be
    var dtParam2 = (typeof dt2 == 'number') ? new Date(dt2) : dt2; 
    var intervFlag = interv.toLowerCase();
    // Year, month
    var yeaDiff = dtParam2.getFullYear() - dtParam1.getFullYear();
    var monDiff = (dtParam2.getMonth() - dtParam1.getMonth()) + (yeaDiff * 12);
    // All others -- build incrementally
    var msDiff = dtParam2.getTime() - dtParam1.getTime(); // Millisecs
    var secDiff = msDiff/1000;
    var minDiff = secDiff/60;
    var houDiff = minDiff/60;
    var dayDiff = houDiff/24;
    // Counts number of seven-day intervals, not calendar weeks
    var weeDiff = dayDiff/7; 
    var ret = 0;
    
    switch(intervFlag) {
        case 'yyyy':
            ret = yeaDiff;
            break;
        case 'q':
            var mA = dtParam1.getMonth();
            var mB = dtParam2.getMonth();
            // Figure out which quarter the months are in
            var qA = parseInt(mA / 3) + 1;
            var qB = parseInt(mB / 3) + 1;
            // Add quarters for any year difference between the dates
            qB += (yeaDiff * 4);
            ret = qB - qA;
            break;
        case 'm':
            ret = monDiff;
            break;            
        case 'd':
        case 'y':
            // Daylight savings time switchover
            // Value will be over or under by an hour
            ret = dayDiff;
            break;
        case 'h':
            ret = houDiff;
            break;
        case 'n':
            ret = minDiff;
            break;
        case 's':
            ret = secDiff;
            break;
        case 'w':
            ret = dayDiff;
            break;
        case 'ww':
            ret = weeDiff;
            break;
        default:
            // Do nothing
            break;
        
    } 
    ret = Math.round(ret);
    return ret; // Return an integer
}
