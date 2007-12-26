/*
 * Copyright 2007 Open Source Applications Foundation
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

dojo.provide("cosmo.util.lang");

cosmo.util.lang.has = function(obj, name){
    try {
        return typeof obj[name] != "undefined"; 
    } catch (e) { 
        return false; 
    } 
}

cosmo.util.lang.isEmpty = function (obj) { 
    if (dojo.isObject(obj)) { 
        var tmp = {}; 
        var count = 0;
        for (var x in obj) { 
            if (obj[x] && !tmp[x]) { 
                count++; 
                break; 
            }
        } 
        return count == 0; 
    } else { 
        if (dojo.isArrayLike(obj) || dojo.isString(obj)) { 
            return obj.length == 0; 
        } 
    } 
}
