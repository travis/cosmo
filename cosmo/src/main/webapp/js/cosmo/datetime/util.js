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

dojo.provide("cosmo.datetime.util");

dojo.require("cosmo.datetime");
dojo.require("cosmo.datetime.Date");
dojo.require("dojo.date.common");

cosmo.datetime.util = new function () {

    var stripZeroPat = /^0/;
    this.parseTimeString = function (str, opts) {
        var stripLeadingZero = function (s) {
            return s.replace(stripZeroPat, '');
        }
        var o = opts || {};
        var h = null;
        var m = null;
        if (str.indexOf(':') > -1) {
            var arr = str.split(':');
            h = arr[0];
            m = arr[1];
        }
        else {
           h = str;
           m = '00';
        }
        if (!o.returnStrings) {
            h = parseInt(stripLeadingZero(h));
            m = parseInt(stripLeadingZero(m));
        }
        return { hours: h, minutes: m };
    };
    /**
     * Convert a 24-hour formatted hour to 12-hour format
     * @param hour Integer hour number
     * @return String for hour in 12-hour format -- may be string length of one
     */
    this.hrMil2Std = function (hour) {
        var h = typeof hour == 'number' ? hour : parseInt(hour);
        var str = h > 12 ? h - 12 : h;
        str = str == 0 ? 12 : str;
        return str;
    };
    /**
     * Convert a 12-hour formatted hour with meridian flag to 24-hour format
     * @param hour Integer hour number
     * @param pm Boolean flag, if PM hour then set to true
     * @return String for hour in 24-hour format
     */
    this.hrStd2Mil = function  (hour, pm) {
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
     * Return 'AM' or 'PM' based on hour in 24-hour format
     * @param h Integer for hour in 24-hour format
     * @return String of either 'AM' or 'PM' based on hour number
     */
    this.getMeridian = function (h) {
        return h > 11 ? cosmo.datetime.meridian.PM :
            cosmo.datetime.meridian.AM;
    }
    /**
     * Adjust from 0-6 base week to 1-7 base week
     * @param d integer for day of week
     * @return Integer day number for 1-7 base week
     */
    this.adjSun = function(d) {
        return d == 0 ? 7 : d;
};
    /**
     * Get the datetime for midnight Sunday of a week given a date
     * anywhere in the week
     */
    this.getWeekStart = function (dt) {
        var diff = dt.getDay();
        var sun = new Date(dt.getTime());
        diff = 0 - diff;
        sun = cosmo.datetime.Date.add(sun, dojo.date.dateParts.DAY, diff);
        var ret = new Date(sun.getFullYear(), sun.getMonth(), sun.getDate());
        return ret;
    };
    /**
     * Get the datetime for 23:59:59 Saturday night of a week
     * given a date anywhere in the week
     */
    this.getWeekEnd = function (dt) {
        var diff = 6-dt.getDay();
        var sat = new Date(dt.getTime());
        sat = cosmo.datetime.Date.add(sat, dojo.date.dateParts.DAY, diff);
         // Make time of day 11:59:99 to get the entire day's events
        var ret = new Date(sat.getFullYear(), sat.getMonth(), sat.getDate(), 23, 59, 59);
        return ret;
    };
};


