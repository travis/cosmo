if(!dojo._hasResource["cosmo.util.string"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["cosmo.util.string"] = true;
/* * Copyright 2006-2007 Open Source Applications Foundation *
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

dojo.provide("cosmo.util.string");

cosmo.util.string.startsWith = function(str, substring){
    return str.indexOf(substring) == 0;
}

cosmo.util.string.endsWith = function(str, substring){
    if (str.length - substring.length < 0) { 
        return false; 
    } 
    return str.lastIndexOf(substring) == str.length - substring.length;
}

cosmo.util.string.capitalize = function (str) { 
    // Ported from dojo 0.4.3 because it does not exist in Dojo 1.0
    if (!dojo.isString(str)) { 
        return ""; 
    } 
    if (arguments.length == 0) { 
        str = this; 
    } 
    var words = str.split(" "); 
    for (var i = 0; i < words.length; i++) { 
        words[i] = words[i].charAt(0).toUpperCase() + words[i].substring(1); 
    } 
    return words.join(" "); 
}

cosmo.util.string.escapeXml = function(/*string*/str){
    //summary:
    //	Adds escape sequences for special characters in XML: &<>"'
	str = str.replace(/&/gm, "&amp;").replace(/</gm, "&lt;")
		.replace(/>/gm, "&gt;").replace(/"/gm, "&quot;")
        .replace(/'/gm, "&#39;");
	return str;
}

}
