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

dojo.provide("cosmo.datetime.parse");

cosmo.datetime.parse = new function () {
    
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

};

