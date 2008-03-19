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

//from dojo 0.4
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

//from dojo0.4
cosmo.util.lang.shallowCopy = function (obj, deep) { 
    var i, ret; 
    if (obj === null) { 
        return null; 
    } 

    if (dojo.isObject(obj)) { 
        ret = new obj.constructor; 
        for (i in obj) { 
            if (cosmo.util.lang.isUndefined(ret[i])) { 
                ret[i] = deep ? cosmo.util.lang.shallowCopy(obj[i], deep) : obj[i]; 
            } 
        } 
    } else { 
        if (dojo.isArray(obj)) { 
            ret = []; for (i = 0; i < obj.length; i++) { 
                ret[i] = deep ? cosmo.util.lang.shallowCopy(obj[i], deep) : obj[i]; 
            } 
        } else { 
            ret = obj; 
        } 
    } 
    return ret; 
}

//from dojo 0.4
cosmo.util.lang.isUndefined = function(it){
    return typeof it == "undefined" && it == undefined; 
}

//from dojo 0.4
cosmo.util.lang.unnest = function () { 
    var out = []; 
    for (var i = 0; i < arguments.length; i++) { 
        if (dojo.isArrayLike(arguments[i])) { 
            var add = cosmo.util.lang.unnest.apply(this, arguments[i]); 
            out = out.concat(add); 
        } 
        else { out.push(arguments[i]); } 
    } 
    return out; 
}