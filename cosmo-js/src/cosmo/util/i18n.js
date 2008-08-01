/*
 * Copyright 2006-2008 Open Source Applications Foundation
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
 * summary:
 *   This module provides i18n utility functions.
 * description:
 *   This module provides i18n utility functions. Ideally,
 *   Dojo's i18n facilities should be used instead of this as much as possible,
 *   but there are still cases where a globally available i18n oracle is useful.
 */

dojo.provide("cosmo.util.i18n");
dojo.require("cosmo.ui.conf");

cosmo.util.i18n.getText = function () {
    if (!this._localtext) {
        this.setLocalizationMessages(cosmo.ui.conf.getLocalText());
    }

    var args = Array.prototype.slice.apply(arguments);
    var key = args.shift();
    var str = this._localtext[key] || "[[" + key + "]]";
    for (var i in args){
        str = str.replace(new RegExp("\{" + i + "\\}", "g"), args[i]);
    }
    return str;
};

cosmo.util.i18n.setLocalizationMessages = function(messages){
    this._localtext = messages || {};
};

cosmo.util.i18n.messageExists = function (str){
     if (cosmo.util.i18n._localtext[str]){
         return true;
     } else {
         return false;
     }
};
