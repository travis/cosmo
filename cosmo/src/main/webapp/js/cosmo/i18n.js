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
 * Provides i18n support.
 */

dojo.provide("cosmo.i18n");
dojo.require("cosmo.ui.conf");
dojo.require("dojo.string");
dojo.requireLocalization("cosmo", "global");

cosmo.i18n = {
    l10n: dojo.i18n.getLocalization("cosmo", "global"),
    getText: function () {
        var args = Array.prototype.slice.apply(arguments);
        var key = args.shift();
        var str = this.l10n[key] || "[[" + key + "]]";
        return dojo.string.substitute(str, args);
    },

    messageExists: function (str){
        if (this.l10n[str]){
            return true;
        } else {
            return false;
        }
    },

    weekdayKeys: ['App.Sun', 'App.Mon', 'App.Tue', 'App.Wed', 'App.Thu', 'App.Fri', 'App.Sat'],
    monthKeys: ['App.January', 'App.February', 'App.March', 'App.April', 'App.May', 'App.June',
                'App.July', 'App.August', 'App.September', 'App.October', 'App.November', 'App.December'],
    meridianKeys: {
        AM: 'App.AM',
        PM: 'App.PM'
    }
}
