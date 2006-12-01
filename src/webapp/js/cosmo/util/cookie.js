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

dojo.provide("cosmo.util.cookie");

cosmo.util.cookie = new function() {
    this.set = function(name, value, days, path) {
        var expires = '';
        var setPath = path ? path : '/';
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            expires = '; expires=' + date.toGMTString();
        }
        else {
            expires = '';
        }
        document.cookie = name + '=' + value + expires + '; path=' + setPath;
    };
    this.get = function(name) {
        var nameEqual = name + '=';
        var cookieArr = document.cookie.split(';');
        for(var i = 0; i < cookieArr.length; i++) {
            var cook = cookieArr[i];
            while (cook.charAt(0) == ' ') {
                cook = cook.substring(1,cook.length);
            }
            if (cook.indexOf(nameEqual) == 0) {
                return cook.substring(nameEqual.length, cook.length);
            }
        }
        return null;
    };
    this.destroy = function(name, path) {
        this.set(name, '', -1, path);
    };
}

var Cookie = cosmo.util.cookie;
