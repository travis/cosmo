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


dojo.provide("cosmo.datetime");

cosmo.datetime = new function () {
    this.dateParts = {
        YEAR: "yyyy",
        QUARTER:  "q",
        MONTH: "m",
        YEAR: "y",
        DAY: "d",
        WEEKDAY: "w",
        WEEK: "ww",
        HOUR: "h",
        MINUTE: "n",
        SECOND: "s"
    };
    this.fullWeekday = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday',
        'Friday', 'Saturday'];
    this.abbrWeekday = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    this.fullMonth = ['January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'];
    this.abbrMonth = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug',
        'Sep', 'Oct', 'Nov', 'Dec'];
    this.meridian = {
        AM: 'AM',
        PM: 'PM'
    };
};



