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
 * NOT for use in production - merely useful for situations w/o a server - like unit testing!
 */
dojo.provide("cosmo.util.jsi18n");
dojo.provide("cosmo.util.i18n");

cosmo.util.jsi18n.init = function(uri){
    var s = dojo.hostenv.getText(uri);
    var lines = s.split('\n');
    this._localtext = {};
    for (var i = 0; i < lines.length; i++) {
            var line = lines[i];

            //Skip comments
            if (dojo.string.startsWith(dojo.string.trim(line), "#")){
                continue;
            }
            
            var arr = line.split("=");
            var key = arr[0];
            var value = arr[1];
            this._localtext[key] = value;
    }
}

function getText(str) {
    return cosmo.util.jsi18n._localtext[str] || "[[" + str + "]]";
}

cosmo.util.i18n.messageExists = function(str){
     if (cosmo.util.jsi18n._localtext[str]){
         return true;
     } else {
         return false;
     }
}


cosmo.util.i18n.getText = getText