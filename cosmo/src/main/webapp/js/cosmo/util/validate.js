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
dojo.provide("cosmo.util.validate");

dojo.require("cosmo.util.i18n");
dojo.require("cosmo.convenience");

cosmo.util.validate = new function () {

    this.dateFormat = function (str) {
        // Checks for the following valid date formats:
        // MM/DD/YY MM/DD/YYYY MM-DD-YY MM-DD-YYYY MM.DD.YY MM.DD.YYYY
        // Also separates date into month, day, and year variables
        // var pat = /^(\d{1,2})(\/|-|.)(\d{1,2})\2(\d{2}|\d{4})$/;
        // To require a 4 digit year entry, use this line instead:
        var pat = /^(\d{1,2})(\/|-)(\d{1,2})\2(\d{4})$/;
        var errMsg = '';
        
        // Check format
        var matchArray = str.match(pat);
        if (!matchArray) {
            errMsg += _('App.Error.InvalidDateFormat');
        }
        else {
            // Parse date parts into vars
            month = matchArray[1];
            day = matchArray[3];
            year = matchArray[4];
            // Month range
            if (month < 1 || month > 12) {
                errMsg += _('App.Error.InvalidMonthRange');
            }
            // Day range
            if (day < 1 || day > 31) {
                errMsg += _('App.Error.InvalidDayRange');
            }
            // Day 31 for correct months
            if ((month == 4 || month == 6 || month == 9 || month == 11) 
                && day == 31) {
                errMsg += Date.fullMonth[month-1] + ' does not have 31 days.\n';
            }
            // Leap year stuff
            if (month == 2) {
                var isLeap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
                if (day > 29 || (day == 29 && !isLeap)) {
                    errMsg += _('App.Error.FebruaryDays');
                }
            }
        }
        return errMsg;
    }
    this.timeFormat = function (str) {
        var pat = /^(\d{1,2})(:)(\d{2})$/;
        var errMsg = '';
        
        // Check format
        var matchArray = str.match(pat);
        if (!matchArray) {
            errMsg += _('App.Error.InvalidTimeFormat');
        }
        else {
            hours = matchArray[1];
            minutes = matchArray[3];
            if (hours < 1 || hours > 12) {
                errMsg += _('App.Error.InvalidHourRange');
            }
            if (minutes < 0 || minutes > 59) {
                errMsg += _('App.Error.InvalidMinutesRange');
            }
        }
        return errMsg;
    }
    /**
     * Makes sure the given text input has a given length
     * @return String, error message (empty if no err).
     */
    this.minLength = function (s, len) {
        err = '';
        val = typeof s == 'object' ? s.value : s;
        // Only bother checking length if a value is present
        // Requiring a value should be done with 'required' method
        if (val && (val.length < len)) {
            err = _('Signup.Error.MinLength') + ' (' + len + ')';
        }
        return err;
    }
    /**
     * Makes sure the given text input is not empty
     * @return String, error message (empty if no err).
     */
    this.required = function (s) {
        err = '';
        val = (s != null && typeof s == 'object') ? s.value : s;
        if (!val) {
            err = _('Signup.Error.RequiredField');
        }
        return err;
    }
    /**
     * Makes sure the given text input is a valid e-mail address
     * @return String, error message (empty if no err).
     */
    this.eMail = function (s) {
        // Just do really basic e-mail addr validation
        pat = /^.+@.+\..{2,3}$/;
        err = '';
        val = typeof s == 'object' ? s.value : s;
        if (!pat.test(val)) {
            err = _('Signup.Error.ValidEMail');
        }
        return err;
    }
    /**
     * Makes sure the given password field matches the other
     * @return String, error message (empty if no err).
     */
    this.confirmPass = function (s, sCompare) {
        err = '';
        val = typeof s == 'object' ? s.value : s;
        val2 = typeof sCompare == 'object' ? sCompare.value : s;
        if (val != val2) {
            err = _('Signup.Error.MatchPassword');
        }
        return err;
    }
}

cosmo.util.validate.constructor = null;
Validate = cosmo.util.validate;
